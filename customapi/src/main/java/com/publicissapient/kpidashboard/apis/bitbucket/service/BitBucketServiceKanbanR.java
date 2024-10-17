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
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.bitbucket.factory.BitBucketKPIServiceFactory;
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
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.ProjectFilter;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;

import lombok.extern.slf4j.Slf4j;

/**
 * Bit bucket service to process Kpi data.
 *
 * @author prijain3
 */
@Service
@Slf4j
public class BitBucketServiceKanbanR {

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
	 * Process kpi request and returns bitbucket kpi response object.
	 *
	 * @param kpiRequest
	 *            the kpi request
	 * @return the list
	 * @throws EntityNotFoundException
	 *             the entity not found exception
	 */
	@SuppressWarnings("unchecked")
	public List<KpiElement> process(KpiRequest kpiRequest) throws EntityNotFoundException {

		log.info("[BITBUCKET KANBAN][{}]. Processing KPI calculation for data {}", kpiRequest.getRequestTrackerId(),
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
			kanbanProjectKeyCache = getProjectKeyCache(kpiRequest, filteredAccountDataList);

			filteredAccountDataList = getAuthorizedFilteredList(kpiRequest, filteredAccountDataList);

			if (filteredAccountDataList.isEmpty()) {
				return responseList;
			}
			Integer groupId = kpiRequest.getKpiList().get(0).getGroupId();

			populateKanbanKpiRequest(kpiRequest);
			Object cachedData = cacheService.getFromApplicationCache(kanbanProjectKeyCache,
					KPISource.BITBUCKETKANBAN.name(), groupId, null);
			if (!kpiRequest.getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
					&& null != cachedData) {
				log.info("[BITBUCKET KANBAN][{}]. Fetching value from cache for {}", kpiRequest.getRequestTrackerId(),
						kpiRequest.getIds());
				return (List<KpiElement>) cachedData;
			}
			kpiRequest.setXAxisDataPoints(Integer.parseInt(kpiRequest.getIds()[0]));
			kpiRequest.setDuration(kpiRequest.getSelectedMap().get(CommonConstant.date).get(0));
			Node filteredNode = getFilteredNodes(kpiRequest, filteredAccountDataList);

			for (KpiElement kpiEle : kpiRequest.getKpiList()) {
				responseList.add(calculateAllKPIAggregatedMetrics(kpiRequest, kpiEle, filteredNode));
			}

			setIntoApplicationCache(kpiRequest, responseList, groupId, kanbanProjectKeyCache);

		} catch (EntityNotFoundException enfe) {

			log.error("[BITBUCKET KANBAN][{}]. Error while KPI calculation for data. No data found {} {}",
					kpiRequest.getRequestTrackerId(), kpiRequest.getKpiList(), enfe);
			throw enfe;
		} catch (Exception e) {
			log.error("[BITBUCKET KANBAN][{}]. Error while KPI calculation for data {} {}",
					kpiRequest.getRequestTrackerId(), kpiRequest.getKpiList(), e);
			throw new HttpMessageNotWritableException(e.getMessage(), e);
		}

		return responseList;
	}

	private Node getFilteredNodes(KpiRequest kpiRequest, List<AccountHierarchyDataKanban> filteredAccountDataList) {
		Node filteredNode = filteredAccountDataList.get(0).getNode().get(kpiRequest.getLevel() - 1);
		filteredNode.setProjectFilter(new ProjectFilter(filteredNode.getId(), filteredNode.getName(),
				filteredNode.getAccountHierarchyKanban().getBasicProjectConfigId()));

		return filteredNode;
	}

	/**
	 * @param kpiRequest
	 *            kpiRequest
	 * @param filteredAccountDataList
	 *            filteredAccountDataList
	 * @return array of string
	 */
	private String[] getProjectKeyCache(KpiRequest kpiRequest,
			List<AccountHierarchyDataKanban> filteredAccountDataList) {
		return authorizedProjectsService.getKanbanProjectKey(filteredAccountDataList, kpiRequest);
	}

	/**
	 * @param kpiRequest
	 *            kpiRequest
	 * @param filteredAccountDataList
	 *            filteredAccountDataList
	 * @return list of hierarchy
	 */
	private List<AccountHierarchyDataKanban> getAuthorizedFilteredList(KpiRequest kpiRequest,
			List<AccountHierarchyDataKanban> filteredAccountDataList) {
		kpiHelperService.kpiResolution(kpiRequest.getKpiList());
		if (!authorizedProjectsService.ifSuperAdminUser()) {
			filteredAccountDataList = authorizedProjectsService.filterKanbanProjects(filteredAccountDataList);
		}
		return filteredAccountDataList;
	}

	/**
	 *
	 * @param kpiRequest
	 *            kpiRequest
	 * @param kpiElement
	 *            kpiElement
	 * @param filteredNode
	 *            filteredNode
	 * @return KpiElement kpiElement
	 */
	private KpiElement calculateAllKPIAggregatedMetrics(KpiRequest kpiRequest, KpiElement kpiElement,
			Node filteredNode) {

		BitBucketKPIService<?, ?, ?> bitBucketKPIService = null;
		KPICode kpi = KPICode.getKPI(kpiElement.getKpiId());
		try {
			bitBucketKPIService = BitBucketKPIServiceFactory.getBitBucketKPIService(kpi.name());
			long startTime = System.currentTimeMillis();

			Node filteredNodeClone = (Node) SerializationUtils.clone(filteredNode);
			if (Objects.nonNull(filteredNodeClone)
					&& kpiHelperService.isToolConfigured(kpi, kpiElement, filteredNodeClone)) {
				kpiElement = bitBucketKPIService.getKpiData(kpiRequest, kpiElement, filteredNodeClone);
				kpiElement.setResponseCode(CommonConstant.KPI_PASSED);
				kpiHelperService.isMandatoryFieldSet(kpi, kpiElement, filteredNodeClone);
			}

			long processTime = System.currentTimeMillis() - startTime;
			log.info("[BITBUCKET-KANBAN-{}-TIME][{}]. KPI took {} ms", kpi.name(), kpiRequest.getRequestTrackerId(),
					processTime);
		} catch (ApplicationException exception) {
			kpiElement.setResponseCode(CommonConstant.KPI_FAILED);
			log.error("Kpi not found", exception);
		} catch (Exception exception) {
			kpiElement.setResponseCode(CommonConstant.KPI_FAILED);
			log.error("[BITBUCKET KANBAN][{}]. Error while KPI calculation for data {} {}",
					kpiRequest.getRequestTrackerId(), kpiRequest.getKpiList(), exception);
			return kpiElement;
		}
		return kpiElement;

	}

	/**
	 * Sets cache
	 *
	 * @param kpiRequest
	 *            kpiRequest
	 * @param responseList
	 *            responseList
	 * @param groupId
	 *            groupId
	 * @param kanbanProjectKeyCache
	 *            kanbanProjectKeyCache
	 */
	private void setIntoApplicationCache(KpiRequest kpiRequest, List<KpiElement> responseList, Integer groupId,
			String[] kanbanProjectKeyCache) {
		Integer projectLevel = filterHelperService.getHierarchyIdLevelMap(true)
				.get(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT);
		if (!kpiRequest.getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& projectLevel >= kpiRequest.getLevel()) {
			cacheService.setIntoApplicationCache(kanbanProjectKeyCache, responseList, KPISource.BITBUCKETKANBAN.name(),
					groupId, null);
		}

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