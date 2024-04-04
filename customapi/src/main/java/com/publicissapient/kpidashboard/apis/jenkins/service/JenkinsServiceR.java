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

package com.publicissapient.kpidashboard.apis.jenkins.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.errors.EntityNotFoundException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jenkins.factory.JenkinsKPIServiceFactory;
import com.publicissapient.kpidashboard.apis.kpiintegration.service.KpiIntegrationServiceImpl;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;

import lombok.extern.slf4j.Slf4j;

/**
 * @implNote {@link KpiIntegrationServiceImpl }
 */
@Service
@Slf4j
public class JenkinsServiceR {

	@Autowired
	private FilterHelperService filterHelperService;

	@Autowired
	private KpiHelperService kpiHelperService;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private UserAuthorizedProjectsService authorizedProjectsService;

	private boolean referFromProjectCache = true;

	@SuppressWarnings({ "unchecked" })
	public List<KpiElement> process(KpiRequest kpiRequest) throws EntityNotFoundException {

		log.info("[JENKINS][{}]. Processing KPI calculation for data {}", kpiRequest.getRequestTrackerId(),
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

				Object cachedData = cacheService.getFromApplicationCache(projectKeyCache, KPISource.JENKINS.name(),
						groupId, kpiRequest.getSprintIncluded());
				if (!kpiRequest.getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
						&& null != cachedData) {
					log.info("[JENKINS][{}]. Fetching value from cache for {}", kpiRequest.getRequestTrackerId(),
							kpiRequest.getIds());
					return (List<KpiElement>) cachedData;
				}

				TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
						filteredAccountDataList, null, filterHelperService.getFirstHierarachyLevel(),
						filterHelperService.getHierarchyIdLevelMap(false)
								.getOrDefault(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT, 0));

				for (KpiElement kpiEle : kpiRequest.getKpiList()) {
					responseList.add(calculateAllKPIAggregatedMetrics(kpiRequest, kpiEle, treeAggregatorDetail));
				}
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
			log.error("[JIRA][{}]. Error while KPI calculation for data {} {}", kpiRequest.getRequestTrackerId(),
					kpiRequest.getKpiList(), e);
			throw new HttpMessageNotWritableException(e.getMessage(), e);
		}

		return responseList;
	}

	/**
	 * @param kpiRequest
	 * @param filteredAccountDataList
	 * @return
	 */
	private List<AccountHierarchyData> getAuthorizedFilteredList(KpiRequest kpiRequest,
			List<AccountHierarchyData> filteredAccountDataList) {

		kpiHelperService.kpiResolution(kpiRequest.getKpiList());
		if (Boolean.TRUE.equals(referFromProjectCache) && !authorizedProjectsService.ifSuperAdminUser()) {
			filteredAccountDataList = authorizedProjectsService.filterProjects(filteredAccountDataList);
		}

		return filteredAccountDataList;
	}

	/**
	 * @param kpiRequest
	 * @param filteredAccountDataList
	 */
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
	 * 
	 * @param kpiRequest
	 * @param kpiElement
	 * @param treeAggregatorDetail
	 * @return KpiElement kpiElement
	 */
	private KpiElement calculateAllKPIAggregatedMetrics(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) {

		JenkinsKPIService<?, ?, ?> jenkinsKPIService = null;
		KPICode kpi = KPICode.getKPI(kpiElement.getKpiId());
		try {
			jenkinsKPIService = JenkinsKPIServiceFactory.getJenkinsKPIService(kpi.name());

			long startTime = System.currentTimeMillis();

			TreeAggregatorDetail treeAggregatorDetailClone = (TreeAggregatorDetail) SerializationUtils
					.clone(treeAggregatorDetail);
			kpiElement = jenkinsKPIService.getKpiData(kpiRequest, kpiElement, treeAggregatorDetailClone);
			kpiElement.setResponseCode(CommonConstant.KPI_PASSED);
			long processTime = System.currentTimeMillis() - startTime;
			log.info("[JENKINS-{}-TIME][{}]. KPI took {} ms", kpi.name(), kpiRequest.getRequestTrackerId(),
					processTime);

		} catch (ApplicationException exception) {
			log.error("Kpi not found", exception);
		} catch (Exception exception) {
			kpiElement.setResponseCode(CommonConstant.KPI_FAILED);
			log.error("Error while KPI calculation for data {}", kpiRequest.getKpiList(), exception);
			return kpiElement;
		}
		return kpiElement;

	}

	/**
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

			cacheService.setIntoApplicationCache(projectKeyCache, responseList, KPISource.JENKINS.name(), groupId,
					kpiRequest.getSprintIncluded());
		}
	}

	/**
	 * This method is called when the request for kpi is done from exposed API
	 *
	 * @param kpiRequest
	 *            Jenkins KPI request true if flow for precalculated, false for
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
