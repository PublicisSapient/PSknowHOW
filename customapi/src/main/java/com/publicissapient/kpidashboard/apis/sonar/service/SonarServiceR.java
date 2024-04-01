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

package com.publicissapient.kpidashboard.apis.sonar.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.kpiintegration.service.KpiIntegrationServiceImpl;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.sonar.factory.SonarKPIServiceFactory;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author prigupta8
 * @implNote {@link KpiIntegrationServiceImpl }
 */
@Service
@Slf4j
public class SonarServiceR {

	@Autowired
	private KpiHelperService kpiHelperService;

	@Autowired
	private FilterHelperService filterHelperService;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private UserAuthorizedProjectsService authorizedProjectsService;

	private boolean referFromProjectCache = true;

	/**
	 * Process Sonar KPI request for Kanban projects
	 * 
	 * @param kpiRequest
	 *            kpiRequest
	 * @return {@code List<KpiElement>}
	 */
	@SuppressWarnings({ "PMD.AvoidCatchingGenericException" })
	public List<KpiElement> process(KpiRequest kpiRequest) {

		log.info("[SONAR][{}]. Processing KPI calculation for data {}", kpiRequest.getRequestTrackerId(),
				kpiRequest.getKpiList());
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

				Object cachedData = cacheService.getFromApplicationCache(projectKeyCache, KPISource.SONAR.name(),
						groupId, kpiRequest.getSprintIncluded());
				getDataFromCache(cachedData, kpiRequest);
				TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
						filteredAccountDataList, null, filterHelperService.getFirstHierarachyLevel(),
						filterHelperService.getHierarchyIdLevelMap(false)
								.getOrDefault(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT, 0));

				calculateKPIAggregatedMetrics(kpiRequest, responseList, treeAggregatorDetail);

				List<KpiElement> missingKpis = filterKips(origRequestedKpis, responseList);
				responseList.addAll(missingKpis);
				setIntoKnowHowCache(kpiRequest, responseList, groupId, projectKeyCache);
			} else {
				responseList.addAll(origRequestedKpis);
			}
		} catch (ApplicationException enfe) {

			log.error("[SONAR][{}]. Error while KPI calculation for data. No data found {} {}",
					kpiRequest.getRequestTrackerId(), kpiRequest.getKpiList(), enfe);
		}
		return responseList;
	}

	/**
	 * Calculates all KPI aggregated metrics
	 * 
	 * @param kpiRequest
	 *            kpiRequest
	 * @param kpiElement
	 *            kpiElement
	 * @param treeAggregatorDetail
	 *            treeAggregatorDetail
	 * @return kpielement
	 */
	private KpiElement calculateAllKPIAggregatedMetrics(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) {
		long startTime;

		try {
			KPICode kpi = KPICode.getKPI(kpiElement.getKpiId());

			SonarKPIService<?, ?, ?> sonarKPIService = null;
			sonarKPIService = SonarKPIServiceFactory.getSonarKPIService(kpi.name());

			startTime = System.currentTimeMillis();

			TreeAggregatorDetail treeAggregatorDetailClone = (TreeAggregatorDetail) SerializationUtils
					.clone(treeAggregatorDetail);
			List<Node> projectNodes = treeAggregatorDetailClone.getMapOfListOfProjectNodes()
					.get(CommonConstant.PROJECT.toLowerCase());

			if (!projectNodes.isEmpty() && (projectNodes.size() > 1
					|| kpiHelperService.isMandatoryFieldValuePresentOrNot(kpi, projectNodes.get(0)))) {
				kpiElement = sonarKPIService.getKpiData(kpiRequest, kpiElement, treeAggregatorDetailClone);
				kpiElement.setResponseCode(CommonConstant.KPI_PASSED);
			} else if (!kpiHelperService.isMandatoryFieldValuePresentOrNot(kpi, projectNodes.get(0))) {
				// mandatory fields not found
				kpiElement.setResponseCode(CommonConstant.MANDATORY_FIELD_MAPPING);
			}
			if (log.isInfoEnabled()) {
				log.info("[SONAR-{}-TIME][{}]. KPI took {} ms", kpi.name(), kpiRequest.getRequestTrackerId(),
						System.currentTimeMillis() - startTime);
			}
		} catch (ApplicationException exception) {
			log.error("[SONAR][{}]. Error while KPI calculation for data. No data found {} {}",
					kpiRequest.getRequestTrackerId(), kpiRequest.getKpiList(), exception.getStackTrace());
		} catch (Exception exception) {
			kpiElement.setResponseCode(CommonConstant.KPI_FAILED);
			log.error("[PARALLEL_JIRA_SERVICE].Exception occurred", exception);
			return kpiElement;
		}
		return kpiElement;

	}

	/**
	 * Sets KPI reponse List into KnowHow Cache
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
	private void setIntoKnowHowCache(KpiRequest kpiRequest, List<KpiElement> responseList, Integer groupId,
			String[] projectKeyCache) {
		Integer projectLevel = filterHelperService.getHierarchyIdLevelMap(false)
				.get(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT);
		if (!kpiRequest.getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& projectLevel >= kpiRequest.getLevel()) {
			cacheService.setIntoApplicationCache(projectKeyCache, responseList, KPISource.SONAR.name(), groupId,
					kpiRequest.getSprintIncluded());
		}
	}

	private void getDataFromCache(Object cachedData, KpiRequest kpiRequest) {
		if (!kpiRequest.getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& null != cachedData)
			log.info("[JIRA][{}]. Fetching value from cache for {}", kpiRequest.getRequestTrackerId(),
					kpiRequest.getIds());
	}

	private void calculateKPIAggregatedMetrics(KpiRequest kpiRequest, List<KpiElement> responseList,
			TreeAggregatorDetail treeAggregatorDetail) {
		for (KpiElement kpiEle : kpiRequest.getKpiList()) {
			responseList.add(calculateAllKPIAggregatedMetrics(kpiRequest, kpiEle, treeAggregatorDetail));
		}
	}

	private List<KpiElement> filterKips(List<KpiElement> origRequestedKpis, List<KpiElement> responseList) {
		return origRequestedKpis.stream().filter(reqKpi -> responseList.stream()
				.noneMatch(responseKpi -> reqKpi.getKpiId().equals(responseKpi.getKpiId()))).toList();
	}

	/**
	 * 
	 * @param kpiRequest
	 *            kpiRequest
	 * @param filteredAccountDataList
	 *            filteredAccountDataList
	 * @return array of string
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
	 * @param kpiRequest
	 *            kpiRequest
	 * @param filteredAccountDataList
	 *            filteredAccountDataList
	 * @return list of hierrachy
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
	 * 
	 * This method is called when the request for kpi is done from exposed API
	 * 
	 * @param kpiRequest
	 *            SONAR KPI request true if flow for precalculated, false for direct
	 *            * flow.
	 * @return List of KPI data
	 */
	public List<KpiElement> processWithExposedApiToken(KpiRequest kpiRequest) {
		referFromProjectCache = false;
		List<KpiElement> kpiElementList = process(kpiRequest);
		referFromProjectCache = true;
		return kpiElementList;
	}

}
