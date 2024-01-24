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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.errors.EntityNotFoundException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyDataKanban;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.apis.zephyr.factory.ZephyrKPIServiceFactory;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;

import lombok.extern.slf4j.Slf4j;

/**
 * Service to calculate the Zephyr KPI for kanban. This service interacts with
 * the cache (for list of builds), mongoDB (for KPI data build wise) and
 * aggregationService (for aggregation).
 *
 * @author pkum34
 */

@Service
@Slf4j
public class ZephyrServiceKanban {

	@Autowired
	private KpiHelperService kpiHelperService;

	@Autowired
	private FilterHelperService filterHelperService;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private UserAuthorizedProjectsService authorizedProjectsService;

	/**
	 * Processes the zephyr based KPI requests for kanban.
	 *
	 * @param kpiRequest
	 * @return
	 * @throws EntityNotFoundException
	 */
	@SuppressWarnings({ "unchecked" })
	public List<KpiElement> process(KpiRequest kpiRequest) throws EntityNotFoundException {

		log.info("[ZEPHYR KANBAN][{}]. Processing KPI calculation for data {}", kpiRequest.getIds(),
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
			if (!CollectionUtils.isEmpty(filteredAccountDataList)) {

				kanbanProjectKeyCache = getProjectKeyCache(kpiRequest, filteredAccountDataList);
				filteredAccountDataList = getAuthorizedFilteredList(kpiRequest, filteredAccountDataList);
				if (filteredAccountDataList.isEmpty()) {
					return responseList;
				}
				Integer groupId = kpiRequest.getKpiList().get(0).getGroupId();

				populateKanbanKpiRequest(kpiRequest);
				Object cachedData = cacheService.getFromApplicationCache(kanbanProjectKeyCache,
						KPISource.ZEPHYRKANBAN.name(), groupId, null);
				if (!kpiRequest.getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
						&& null != cachedData) {
					log.info("[ZEPHYR KANBAN][{}]. Fetching value from cache for {}", kpiRequest.getRequestTrackerId(),
							kpiRequest.getIds());
					return (List<KpiElement>) cachedData;
				}

				TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
						null, filteredAccountDataList, filterHelperService.getFirstHierarachyLevel(),
						filterHelperService.getHierarchyIdLevelMap(false)
								.getOrDefault(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT, 0));

				for (KpiElement kpiEle : kpiRequest.getKpiList()) {

					calculateAllKPIAggregatedMetrics(kpiRequest, responseList, kpiEle, treeAggregatorDetail);
				}

				setIntoApplicationCache(kpiRequest, responseList, groupId, kanbanProjectKeyCache);
			}

		} catch (EntityNotFoundException enfe) {

			log.error("[ZEPHYR KANBAN][{}]. Error while KPI calculation for data. No data found {} {}",
					kpiRequest.getIds(), kpiRequest.getKpiList(), enfe);
			throw enfe;

		} catch (Exception e) {
			log.error("[ZEPHYR KANBAN][{}]. Error while KPI calculation for data {} {}", kpiRequest.getIds(),
					kpiRequest.getKpiList(), e);
			throw new HttpMessageNotWritableException(e.getMessage(), e);
		}

		return responseList;
	}

	private void populateKanbanKpiRequest(KpiRequest kpiRequest) {
		String id = kpiRequest.getIds()[0];
		if (NumberUtils.isCreatable(id)) {
			kpiRequest.setKanbanXaxisDataPoints(Integer.parseInt(id));
		}

		List<String> durationList = kpiRequest.getSelectedMap().get(CommonConstant.date);
		if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(durationList)) {
			String duration = durationList.get(0);
			if (NumberUtils.isCreatable(duration)) {
				kpiRequest.setDuration(CommonConstant.DAYS);
			} else {
				kpiRequest.setDuration(duration.toUpperCase());
			}
		}
	}

	/**
	 * @param kpiRequest
	 * @param filteredAccountDataList
	 * @return
	 */
	private List<AccountHierarchyDataKanban> getAuthorizedFilteredList(KpiRequest kpiRequest,
			List<AccountHierarchyDataKanban> filteredAccountDataList) {
		kpiHelperService.kpiResolution(kpiRequest.getKpiList());
		if (!authorizedProjectsService.ifSuperAdminUser()) {
			filteredAccountDataList = authorizedProjectsService.filterKanbanProjects(filteredAccountDataList);
		}
		return filteredAccountDataList;
	}

	private String[] getProjectKeyCache(KpiRequest kpiRequest,
			List<AccountHierarchyDataKanban> filteredAccountDataList) {
		return authorizedProjectsService.getKanbanProjectKey(filteredAccountDataList, kpiRequest);
	}

	/**
	 * Calculates all kpi aggregated metrics.
	 *
	 * @param kpiRequest
	 * @param responseList
	 * @param kpiElement
	 * @param treeAggregatorDetail
	 * @throws ApplicationException
	 */
	private void calculateAllKPIAggregatedMetrics(KpiRequest kpiRequest, List<KpiElement> responseList,
			KpiElement kpiElement, TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		KPICode kpi = KPICode.getKPI(kpiElement.getKpiId());
		long startTime = System.currentTimeMillis();

		TreeAggregatorDetail treeAggregatorDetailRegPercent = (TreeAggregatorDetail) SerializationUtils
				.clone(treeAggregatorDetail);

		ZephyrKPIService<?, ?, ?> zephyrKPIService = ZephyrKPIServiceFactory.getZephyrKPIService(kpi.name());
		if (zephyrKPIService != null) {
			responseList.add(zephyrKPIService.getKpiData(kpiRequest, kpiElement, treeAggregatorDetailRegPercent));
		}

		long processTime = System.currentTimeMillis() - startTime;
		log.info("[ZEPHYR-KANBAN-{}-TIME][{}]. KPI took {} ms", kpi.name(), kpiRequest.getRequestTrackerId(),
				processTime);
	}

	/**
	 * Sets cache.
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
			cacheService.setIntoApplicationCache(kanbanProjectKeyCache, responseList, KPISource.ZEPHYRKANBAN.name(),
					groupId, null);
		}
	}

}