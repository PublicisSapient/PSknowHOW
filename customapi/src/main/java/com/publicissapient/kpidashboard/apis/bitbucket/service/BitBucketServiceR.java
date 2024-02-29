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

package com.publicissapient.kpidashboard.apis.bitbucket.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.ProjectFilter;
import org.apache.commons.lang.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.bitbucket.factory.BitBucketKPIServiceFactory;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.errors.EntityNotFoundException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;

import lombok.extern.slf4j.Slf4j;

/**
 * Bitbucket service to process bitbucket data.
 *
 * @author anisingh4
 */
@Service
@Slf4j
public class BitBucketServiceR {

	@Autowired
	private KpiHelperService kpiHelperService;

	@Autowired
	private FilterHelperService filterHelperService;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private UserAuthorizedProjectsService authorizedProjectsService;

	@SuppressWarnings("unchecked")
	public List<KpiElement> process(KpiRequest kpiRequest) throws EntityNotFoundException {

		log.info("[BITBUCKET][{}]. Processing KPI calculation for data {}", kpiRequest.getRequestTrackerId(),
				kpiRequest.getKpiList());
		List<KpiElement> origRequestedKpis = kpiRequest.getKpiList().stream().map(KpiElement::new)
				.collect(Collectors.toList());
		List<KpiElement> responseList = new ArrayList<>();
		String[] projectKeyCache = null;
		try {
			Integer groupId = kpiRequest.getKpiList().get(0).getGroupId();
			String groupName = filterHelperService.getHierarachyLevelId(kpiRequest.getLevel(), kpiRequest.getLabel(),
					false);
			if (null != groupName) {
				kpiRequest.setLabel(groupName.toUpperCase());
			}
			List<AccountHierarchyData> filteredAccountDataList = filterHelperService.getFilteredBuilds(kpiRequest,
					groupName);
			if (!CollectionUtils.isEmpty(filteredAccountDataList)) {

			projectKeyCache = getProjectKeyCache(kpiRequest, filteredAccountDataList);
			filteredAccountDataList = getAuthorizedFilteredList(kpiRequest, filteredAccountDataList);
			if (filteredAccountDataList.isEmpty()) {
				return responseList;
			}

			Object cachedData = cacheService.getFromApplicationCache(projectKeyCache, KPISource.BITBUCKET.name(),
					groupId, kpiRequest.getSprintIncluded());
			if (!kpiRequest.getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
					&& null != cachedData) {
				log.info("[BITBUCKET][{}]. Fetching value from cache for {}", kpiRequest.getRequestTrackerId(),
						kpiRequest.getIds());
				return (List<KpiElement>) cachedData;
			}

			Node filteredNode = getFilteredNodes(kpiRequest, filteredAccountDataList);
			kpiRequest.setXAxisDataPoints(Integer.parseInt(kpiRequest.getIds()[0]));
			kpiRequest.setDuration(kpiRequest.getSelectedMap().get(CommonConstant.date).get(0));
			List<ParallelBitBucketServices> listOfTask = new ArrayList<>();
			for (KpiElement kpiEle : kpiRequest.getKpiList()) {

				listOfTask.add(
						new ParallelBitBucketServices(kpiRequest, responseList, kpiEle, filteredNode));
			}

			ForkJoinTask.invokeAll(listOfTask);
			List<KpiElement> missingKpis = origRequestedKpis.stream()
					.filter(reqKpi -> responseList.stream()
							.noneMatch(responseKpi -> reqKpi.getKpiId().equals(responseKpi.getKpiId())))
					.collect(Collectors.toList());
			responseList.addAll(missingKpis);
			setIntoApplicationCache(kpiRequest, responseList, groupId, projectKeyCache);
		} else {
			responseList.addAll(origRequestedKpis);
		}

		} catch (Exception e) {
			log.error("[BITBUCKET][{}]. Error while KPI calculation for data {} {}", kpiRequest.getRequestTrackerId(),
					kpiRequest.getKpiList(), e);
			throw new HttpMessageNotWritableException(e.getMessage(), e);
		}

		return responseList;
	}

	private Node getFilteredNodes(KpiRequest kpiRequest, List<AccountHierarchyData> filteredAccountDataList) {
		Node filteredNode = filteredAccountDataList.get(0).getNode().get(kpiRequest.getLevel() - 1);

		if (null != filteredNode.getAccountHierarchy()) {
			filteredNode.setProjectFilter(new ProjectFilter(filteredNode.getId(), filteredNode.getName(),
					filteredNode.getAccountHierarchy().getBasicProjectConfigId()));
		}

		return filteredNode;
	}

	/**
	 * @param kpiRequest
	 * @param filteredAccountDataList
	 * @return
	 */
	private List<AccountHierarchyData> getAuthorizedFilteredList(KpiRequest kpiRequest,
			List<AccountHierarchyData> filteredAccountDataList) {
		kpiHelperService.kpiResolution(kpiRequest.getKpiList());
		if (!authorizedProjectsService.ifSuperAdminUser()) {
			filteredAccountDataList = authorizedProjectsService.filterProjects(filteredAccountDataList);
		}
		return filteredAccountDataList;
	}

	private String[] getProjectKeyCache(KpiRequest kpiRequest, List<AccountHierarchyData> filteredAccountDataList) {

		return authorizedProjectsService.getProjectKey(filteredAccountDataList, kpiRequest);

	}

	/**
	 * Cache response.
	 *
	 * @param kpiRequest
	 * @param responseList
	 * @param groupId
	 * @param projectKeyCache
	 */
	private void setIntoApplicationCache(KpiRequest kpiRequest, List<KpiElement> responseList, Integer groupId,
			String[] projectKeyCache) {
		Integer projectLevel = filterHelperService.getHierarchyIdLevelMap(false)
				.get(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT);
		if (!kpiRequest.getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& projectLevel >= kpiRequest.getLevel()) {

			cacheService.setIntoApplicationCache(projectKeyCache, responseList, KPISource.BITBUCKET.name(), groupId,
					kpiRequest.getSprintIncluded());
		}
	}

	public class ParallelBitBucketServices extends RecursiveAction {

		private static final long serialVersionUID = 1L;
		private final KpiRequest kpiRequest;
		private final transient List<KpiElement> responseList;
		private final transient KpiElement kpiEle;
		private final Node filteredNode;

		/**
		 *
		 * @param kpiRequest
		 * @param responseList
		 * @param kpiEle
		 * @param filteredNode
		 */
		public ParallelBitBucketServices(KpiRequest kpiRequest, List<KpiElement> responseList, KpiElement kpiEle,
				Node filteredNode) {
			super();
			this.kpiRequest = kpiRequest;
			this.responseList = responseList;
			this.kpiEle = kpiEle;
			this.filteredNode = filteredNode;
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("PMD.AvoidCatchingGenericException")
		@Override
		public void compute() {
			try {
				calculateAllKPIAggregatedMetrics(kpiRequest, responseList, kpiEle, filteredNode);
			} catch (Exception e) {
				log.error("[PARALLEL_JIRA_SERVICE].Exception occurred", e);
			}
		}

		/**
		 * This method call by multiple thread, take object of specific KPI and call
		 * method of these KPIs
		 *
		 * @param kpiRequest
		 *            JIRA KPI request
		 * @param responseList
		 *            List of KpiElements having data of each KPI
		 * @param kpiElement
		 *            kpiElement object
		 * @param filteredAccountNode
		 *            filter tree object
		 * @throws ApplicationException
		 *             ApplicationException
		 */
		private void calculateAllKPIAggregatedMetrics(KpiRequest kpiRequest, List<KpiElement> responseList,
				KpiElement kpiElement, Node filteredAccountNode) throws ApplicationException {

			BitBucketKPIService<?, ?, ?> bitBucketKPIService = null;

			KPICode kpi = KPICode.getKPI(kpiElement.getKpiId());

			bitBucketKPIService = BitBucketKPIServiceFactory.getBitBucketKPIService(kpi.name());

			long startTime = System.currentTimeMillis();

			Node nodeDataClone = (Node) SerializationUtils.clone(filteredAccountNode);
			responseList.add(bitBucketKPIService.getKpiData(kpiRequest, kpiElement, nodeDataClone));

			long processTime = System.currentTimeMillis() - startTime;
			log.info("[BITBUCKET-{}-TIME][{}]. KPI took {} ms", kpi.name(), kpiRequest.getRequestTrackerId(), processTime);

		}

	}
}
