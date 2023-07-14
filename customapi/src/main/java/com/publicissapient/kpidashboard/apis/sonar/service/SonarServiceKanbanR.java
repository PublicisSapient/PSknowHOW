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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.errors.EntityNotFoundException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyDataKanban;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.sonar.factory.SonarKPIServiceFactory;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;

import lombok.extern.slf4j.Slf4j;

/**
 * @author prijain3
 */
@Service
@Slf4j
public class SonarServiceKanbanR {

	@Autowired
	private KpiHelperService kpiHelperService;

	@Autowired
	private FilterHelperService filterHelperService;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private UserAuthorizedProjectsService authorizedProjectsService;

	/**
	 * Process Sonar KPI request
	 * 
	 * @param kpiRequest
	 * @return {@code List<KpiElement>}
	 */
	@SuppressWarnings({ "unchecked", "PMD.AvoidCatchingGenericException" })
	public List<KpiElement> process(KpiRequest kpiRequest) {

		log.info("[SONAR KANBAN][{}]. Processing KPI calculation for data {}", kpiRequest.getRequestTrackerId(),
				kpiRequest.getKpiList());
		List<KpiElement> responseList = new ArrayList<>();
		String[] kanbanProjectKeyCache = null;
		try {
			String groupName = filterHelperService.getHierarachyLevelId(kpiRequest.getLevel(), kpiRequest.getLabel(),
					true);
			if (null != groupName) {
				kpiRequest.setLabel(groupName.toUpperCase());
			} else {
				log.error("label name for selected hierarchy not found");
			}
			List<AccountHierarchyDataKanban> filteredAccountDataList = filterHelperService
					.getFilteredBuildsKanban(kpiRequest, groupName);

			kpiHelperService.kpiResolution(kpiRequest.getKpiList());
			if (!authorizedProjectsService.ifSuperAdminUser()) {
				kanbanProjectKeyCache = authorizedProjectsService.getKanbanProjectKey(filteredAccountDataList,
						kpiRequest);

				filteredAccountDataList = authorizedProjectsService.filterKanbanProjects(filteredAccountDataList);

				if (filteredAccountDataList.isEmpty()) {
					return responseList;
				}
			} else {
				kanbanProjectKeyCache = authorizedProjectsService.getKanbanProjectKey(filteredAccountDataList,
						kpiRequest);
			}

			Integer groupId = kpiRequest.getKpiList().get(0).getGroupId();

			populateKanbanKpiRequest(kpiRequest);
			List<KpiElement> cachedData = getCachedData(kpiRequest, kanbanProjectKeyCache, groupId);
			if (CollectionUtils.isNotEmpty(cachedData)) {
				return cachedData;
			}

			TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest, null,
					filteredAccountDataList, filterHelperService.getFirstHierarachyLevel(), filterHelperService
							.getHierarchyIdLevelMap(false).getOrDefault(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT, 0));
			for (KpiElement kpiEle : kpiRequest.getKpiList()) {

				calculateAllKPIAggregatedMetrics(kpiRequest, responseList, kpiEle, treeAggregatorDetail);
			}

			setIntoApplicationCache(kpiRequest, responseList, groupId, kanbanProjectKeyCache);

		} catch (EntityNotFoundException | ApplicationException enfe) {

			log.error("[SONAR KANBAN][{}]. Error while KPI calculation for data. No data found {} {}",
					kpiRequest.getRequestTrackerId(), kpiRequest.getKpiList(), enfe);
		}
		return responseList;
	}

	/**
	 * Calculates all KPI aggregated metrics
	 * 
	 * @param kpiRequest
	 * @param responseList
	 * @param kpiElement
	 * @param treeAggregatorDetail
	 * @throws ApplicationException
	 */
	private void calculateAllKPIAggregatedMetrics(KpiRequest kpiRequest, List<KpiElement> responseList,
			KpiElement kpiElement, TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		long startTime;

		KPICode kpi = KPICode.getKPI(kpiElement.getKpiId());

		SonarKPIService<?, ?, ?> sonarKPIService = null;
		sonarKPIService = SonarKPIServiceFactory.getSonarKPIService(kpi.name());

		startTime = System.currentTimeMillis();

		TreeAggregatorDetail treeAggregatorDetailClone = (TreeAggregatorDetail) SerializationUtils
				.clone(treeAggregatorDetail);
		responseList.add(sonarKPIService.getKpiData(kpiRequest, kpiElement, treeAggregatorDetailClone));
		if (log.isInfoEnabled()) {
			log.info("[SONAR-KANBAN-{}-TIME][{}]. CODEQUALITY took {} ms", kpi.name(), kpiRequest.getRequestTrackerId(),
					System.currentTimeMillis() - startTime);
		}

	}

	/**
	 * Sets KPI reponse List into application Cache
	 * 
	 * @param kpiRequest
	 * @param responseList
	 * @param groupId
	 * @param kanbanProjectKeyCache
	 */
	private void setIntoApplicationCache(KpiRequest kpiRequest, List<KpiElement> responseList, Integer groupId,
			String[] kanbanProjectKeyCache) {
		Integer projectLevel = filterHelperService.getHierarchyIdLevelMap(true)
				.get(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT);
		if (!kpiRequest.getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& projectLevel >= kpiRequest.getLevel()) {
			cacheService.setIntoApplicationCache(kanbanProjectKeyCache, responseList, KPISource.SONARKANBAN.name(),
					groupId, null);
		}
	}

	private List<KpiElement> getCachedData(KpiRequest kpiRequest, String[] kanbanProjectKeyCache, Integer groupId) {
		Object cachedData = cacheService.getFromApplicationCache(kanbanProjectKeyCache, KPISource.SONARKANBAN.name(),
				groupId, null);
		if (!kpiRequest.getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& null != cachedData) {
			log.info("[SONAR KANBAN][{}]. Fetching value from cache for {}", kpiRequest.getRequestTrackerId(),
					kpiRequest.getIds());
			return (List<KpiElement>) cachedData;
		}
		return new ArrayList<>();
	}

	private void populateKanbanKpiRequest(KpiRequest kpiRequest) {
		String id = kpiRequest.getIds()[0];
		if (NumberUtils.isCreatable(id)) {
			kpiRequest.setKanbanXaxisDataPoints(Integer.parseInt(id));
		}

		List<String> durationList = kpiRequest.getSelectedMap().get(CommonConstant.date);
		if (CollectionUtils.isNotEmpty(durationList)) {
			String duration = durationList.get(0);
			if (NumberUtils.isCreatable(duration)) {
				kpiRequest.setDuration(CommonConstant.DAYS);
			} else {
				kpiRequest.setDuration(duration.toUpperCase());
			}
		}
	}
}