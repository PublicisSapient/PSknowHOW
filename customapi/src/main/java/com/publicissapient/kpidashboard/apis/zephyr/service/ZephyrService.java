/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.zephyr.service;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.errors.EntityNotFoundException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.kpiintegration.service.KpiIntegrationServiceImpl;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.apis.zephyr.factory.ZephyrKPIServiceFactory;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;

import lombok.extern.slf4j.Slf4j;

/**
 * Service to calculate the Zephyr KPI. This service interacts with the cache
 * (for list of builds), mongoDB (for KPI data build wise) and
 * aggregationService (for aggregation).
 *
 * @author tauakram
 * @implNote {@link KpiIntegrationServiceImpl }
 *
 */

@Service
@Slf4j
public class ZephyrService {

	@Autowired
	private KpiHelperService kpiHelperService;

	@Autowired
	private FilterHelperService filterHelperService;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private UserAuthorizedProjectsService authorizedProjectsService;

	@Autowired
	private CustomApiConfig customApiConfig;

	private boolean referFromProjectCache = true;

	/**
	 * Process the zephyr based KPI requests.
	 *
	 * @param kpiRequest
	 *            kpiRequest
	 * @return list of kpielement
	 * @throws EntityNotFoundException
	 *             EntityNotFoundException
	 */
	@SuppressWarnings({ "unchecked" })
	public List<KpiElement> process(KpiRequest kpiRequest) throws EntityNotFoundException {

		log.info("[ZEPHYR][{}]. Processing KPI calculation for data {}", kpiRequest.getIds(), kpiRequest.getKpiList());
		List<KpiElement> origRequestedKpis = kpiRequest.getKpiList().stream().map(KpiElement::new).toList();
		List<KpiElement> responseList = new ArrayList<>();
		String[] projectKeyCache = null;
		try {
			Integer groupId = kpiRequest.getKpiList().get(0).getGroupId();
			String groupName = filterHelperService.getHierarachyLevelId(kpiRequest.getLevel(), kpiRequest.getLabel(),
					false);
			if (null != groupName) {
				kpiRequest.setLabel(groupName.toUpperCase());
			} else {
				log.error("label name for selected hierarchy not found");
			}
			List<AccountHierarchyData> filteredAccountDataList = filterHelperService.getFilteredBuilds(kpiRequest,
					groupName);
			if (!CollectionUtils.isEmpty(filteredAccountDataList)) {
				projectKeyCache = getProjectKeyCache(kpiRequest, filteredAccountDataList);
				filteredAccountDataList = getAuthorizedFilteredList(kpiRequest, filteredAccountDataList);
				if (filteredAccountDataList.isEmpty()) {
					return responseList;
				}

				Object cachedData = cacheService.getFromApplicationCache(projectKeyCache, KPISource.ZEPHYR.name(),
						groupId, kpiRequest.getSprintIncluded());
				if (!kpiRequest.getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
						&& null != cachedData) {
					log.info("[ZEPHYR][{}]. Fetching value from cache for {}", kpiRequest.getRequestTrackerId(),
							kpiRequest.getIds());
					return (List<KpiElement>) cachedData;
				}

				TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
						filteredAccountDataList, null, filterHelperService.getFirstHierarachyLevel(),
						filterHelperService.getHierarchyIdLevelMap(false)
								.getOrDefault(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT, 0));

				updateTreeAggregatorDetail(kpiRequest, treeAggregatorDetail);

				kpiRequest.setFilterToShowOnTrend(Constant.PROJECT);

				List<ParallelZephyrServices> listOfTask = new ArrayList<>();
				for (KpiElement kpiEle : kpiRequest.getKpiList()) {

					listOfTask.add(new ParallelZephyrServices(kpiRequest, responseList, kpiEle, treeAggregatorDetail));
				}

				ForkJoinTask.invokeAll(listOfTask);
				List<KpiElement> missingKpis = origRequestedKpis.stream().filter(reqKpi -> responseList.stream()
						.noneMatch(responseKpi -> reqKpi.getKpiId().equals(responseKpi.getKpiId()))).toList();
				responseList.addAll(missingKpis);
				setIntoApplicationCache(kpiRequest, responseList, groupId, projectKeyCache);
			} else {
				responseList.addAll(origRequestedKpis);
			}
		} catch (Exception e) {
			log.error("[ZEPHYR][{}]. Error while KPI calculation for data {} {}", kpiRequest.getIds(),
					kpiRequest.getKpiList(), e);
			throw new HttpMessageNotWritableException(e.getMessage(), e);
		}

		return responseList;
	}

	/**
	 * @param kpiRequest
	 *            kpiRequest
	 * @param filteredAccountDataList
	 *            filteredAccountDataList
	 * @return list of hierarchy
	 */
	private List<AccountHierarchyData> getAuthorizedFilteredList(KpiRequest kpiRequest,
			List<AccountHierarchyData> filteredAccountDataList) {
		kpiHelperService.kpiResolution(kpiRequest.getKpiList());
		if (Boolean.TRUE.equals(referFromProjectCache) && !authorizedProjectsService.ifSuperAdminUser()) {
			filteredAccountDataList = authorizedProjectsService.filterProjects(filteredAccountDataList);
		}
		return filteredAccountDataList;
	}

	private String[] getProjectKeyCache(KpiRequest kpiRequest, List<AccountHierarchyData> filteredAccountDataList) {
		String[] projectKeyCache;

		if (Boolean.TRUE.equals(referFromProjectCache) && !authorizedProjectsService.ifSuperAdminUser()) {
			projectKeyCache = authorizedProjectsService.getProjectKey(filteredAccountDataList, kpiRequest);
		} else {
			projectKeyCache = kpiRequest.getIds();
		}

		return projectKeyCache;
	}

	/**
	 * Sets cache.
	 *
	 * @param kpiRequest
	 *            kpiRequest
	 * @param responseList
	 *            responseList
	 * @param groupId
	 *            groupId
	 * @param projectKeyCache
	 *            projectKeyCache
	 */
	private void setIntoApplicationCache(KpiRequest kpiRequest, List<KpiElement> responseList, Integer groupId,
			String[] projectKeyCache) {
		Integer sprintLevel = filterHelperService.getHierarchyIdLevelMap(false)
				.get(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT);
		if (!kpiRequest.getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& sprintLevel >= kpiRequest.getLevel()) {
			cacheService.setIntoApplicationCache(projectKeyCache, responseList, KPISource.ZEPHYR.name(), groupId,
					kpiRequest.getSprintIncluded());
		}

	}

	/**
	 * updates the TreeAggregatorDetail object based on the KpiRequest. If the
	 * selectedMap in the KpiRequest does not contain the HIERARCHY_LEVEL_ID_SPRINT,
	 * filter out the sprint by sprintCountForKpiCalculation property
	 *
	 * @param kpiRequest
	 *            KpiRequest object containing the selectedMap.
	 * @param treeAggregatorDetail
	 *            The TreeAggregatorDetail object to be updated.
	 */
	private void updateTreeAggregatorDetail(KpiRequest kpiRequest, TreeAggregatorDetail treeAggregatorDetail) {
		if (MapUtils.isNotEmpty(kpiRequest.getSelectedMap())
				&& CollectionUtils.isEmpty(kpiRequest.getSelectedMap().get(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT))
				&& MapUtils.isNotEmpty(treeAggregatorDetail.getMapOfListOfLeafNodes())) {
			List<Node> sprintList = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(
					treeAggregatorDetail.getMapOfListOfLeafNodes().get(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT))) {
				treeAggregatorDetail.getMapOfListOfLeafNodes().get(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT).stream()
						.collect(Collectors.groupingBy(Node::getParentId)).forEach((proj, sprints) -> {
							if (sprints.size() > customApiConfig.getSprintCountForKpiCalculation()) {
								sprintList.addAll(new ArrayList<>(
										sprints.subList(0, customApiConfig.getSprintCountForKpiCalculation())));
							} else {
								sprintList.addAll(sprints);
							}
						});
				treeAggregatorDetail.getMapOfListOfLeafNodes().put(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT,
						sprintList);
			}
		}
	}

	/**
	 * This class is used to call Zephyr based KPIs service in parallel.
	 *
	 * @author prijain3
	 *
	 */
	public class ParallelZephyrServices extends RecursiveAction {

		@Serial
		private static final long serialVersionUID = 1L;
		private final KpiRequest kpiRequest;
		private final transient List<KpiElement> responseList;
		private final transient KpiElement kpiEle;
		private final TreeAggregatorDetail treeAggregatorDetail;

		/*
		 * @param kpiRequest
		 *
		 * @param responseList
		 *
		 * @param kpiEle
		 *
		 * @param treeAggregatorDetail
		 */
		public ParallelZephyrServices(KpiRequest kpiRequest, List<KpiElement> responseList, KpiElement kpiEle,
				TreeAggregatorDetail treeAggregatorDetail) {
			super();
			this.kpiRequest = kpiRequest;
			this.responseList = responseList;
			this.kpiEle = kpiEle;
			this.treeAggregatorDetail = treeAggregatorDetail;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void compute() {
			responseList.add(calculateAllKPIAggregatedMetrics(kpiRequest, kpiEle, treeAggregatorDetail));
		}

		/**
		 * This method call by multiple thread, take object of specific KPI and call
		 * method of these KPIs
		 *
		 * @param kpiRequest
		 *            kpiRequest
		 * @param kpiElement
		 *            kpiElement
		 * @param treeAggregatorDetail
		 *            treeAggregatorDetail
		 * @return KpiElement kpielement
		 */
		private KpiElement calculateAllKPIAggregatedMetrics(KpiRequest kpiRequest, KpiElement kpiElement,
				TreeAggregatorDetail treeAggregatorDetail) {

			try {
				KPICode kpi = KPICode.getKPI(kpiElement.getKpiId());
				ZephyrKPIService<?, ?, ?> zephyrKPIService = ZephyrKPIServiceFactory.getZephyrKPIService(kpi.name());
				long startTime = System.currentTimeMillis();
				TreeAggregatorDetail treeAggregatorDetailClone = (TreeAggregatorDetail) SerializationUtils
						.clone(treeAggregatorDetail);
				List<Node> projectNodes = treeAggregatorDetailClone.getMapOfListOfProjectNodes()
						.get(CommonConstant.PROJECT.toLowerCase());

				if (!projectNodes.isEmpty()
						&& (projectNodes.size() > 1 || kpiHelperService.isRequiredTestToolConfigured(kpi, kpiElement,
								projectNodes.get(0).getProjectFilter().getBasicProjectConfigId()))) {
					kpiElement = zephyrKPIService.getKpiData(kpiRequest, kpiElement, treeAggregatorDetailClone);
					kpiElement.setResponseCode(CommonConstant.KPI_PASSED);
					if (projectNodes.size() == 1) {
						kpiHelperService.isMandatoryFieldSet(kpi, kpiElement, projectNodes.get(0));
					}
				}

				long processTime = System.currentTimeMillis() - startTime;
				log.info("[ZEPHYR-{}-TIME][{}]. KPI took {} ms", kpi.name(), kpiRequest.getRequestTrackerId(),
						processTime);
			} catch (ApplicationException exception) {
				kpiElement.setResponseCode(CommonConstant.KPI_FAILED);
				log.error("Kpi not found", exception);
			} catch (Exception exception) {
				kpiElement.setResponseCode(CommonConstant.KPI_FAILED);
				log.error("[PARALLEL_ZEPHYR_SERVICE].Exception occured", exception);
				return kpiElement;
			}
			return kpiElement;
		}
	}

	/**
	 * This method is called when the request for kpi is done from exposed API
	 *
	 * @param kpiRequest
	 *            Zephyr KPI request true if flow for precalculated, false for
	 *            direct flow.
	 * @return List of KPI data
	 * @throws EntityNotFoundException
	 *             EntityNotFoundException
	 */
	public List<KpiElement> processWithExposedApiToken(KpiRequest kpiRequest) throws EntityNotFoundException {
		referFromProjectCache = false;
		List<KpiElement> kpiElementList = process(kpiRequest);
		referFromProjectCache = true;
		return kpiElementList;
	}

}
