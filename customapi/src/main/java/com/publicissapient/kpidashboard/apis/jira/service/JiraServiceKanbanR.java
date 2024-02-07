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

package com.publicissapient.kpidashboard.apis.jira.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.errors.EntityNotFoundException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.factory.JiraKPIServiceFactory;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyDataKanban;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;

import lombok.extern.slf4j.Slf4j;

/**
 * This class handle all Kanban JIRA based KPI request and call each KPIs
 * service in thread. It is responsible for cache of KPI data at different
 * level.
 * 
 * @author priyanka jain
 *
 */
@Service
@Slf4j
public class JiraServiceKanbanR {

	@Autowired
	private KpiHelperService kpiHelperService;

	@Autowired
	private FilterHelperService filterHelperService;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private UserAuthorizedProjectsService authorizedProjectsService;

	/**
	 * This method process Kanban JIRA based kpi request, cache data and call
	 * service in multiple thread.
	 * 
	 * @param kpiRequest
	 *            JIRA KPI request
	 * @return List of KPI data
	 * @throws EntityNotFoundException
	 */

	@SuppressWarnings({ "unchecked", "PMD.AvoidCatchingGenericException" })
	public List<KpiElement> process(KpiRequest kpiRequest) throws EntityNotFoundException {

		log.info("[JIRA KANBAN][{}]. Processing KPI calculation for data {}", kpiRequest.getRequestTrackerId(),
				kpiRequest.getKpiList());
		List<KpiElement> origRequestedKpis = kpiRequest.getKpiList().stream().map(KpiElement::new)
				.collect(Collectors.toList());
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
			populateKanbanKpiRequest(kpiRequest);
			List<AccountHierarchyDataKanban> filteredAccountDataList = filterHelperService
					.getFilteredBuildsKanban(kpiRequest, groupName);
			kanbanProjectKeyCache = getProjectKeyCache(kpiRequest, filteredAccountDataList);

			filteredAccountDataList = getAuthorizedFilteredList(kpiRequest, filteredAccountDataList);

			if (filteredAccountDataList.isEmpty()) {
				return responseList;
			}
			Integer groupId = kpiRequest.getKpiList().get(0).getGroupId();
			Object cachedData = cacheService.getFromApplicationCache(kanbanProjectKeyCache, KPISource.JIRAKANBAN.name(),
					groupId, null);
			if (!kpiRequest.getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
					&& null != cachedData) {
				log.info("[JIRA KANBAN][{}]. Fetching value from cache for {}", kpiRequest.getRequestTrackerId(),
						kpiRequest.getIds());
				return (List<KpiElement>) cachedData;
			}

			TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest, null,
					filteredAccountDataList, filterHelperService.getFirstHierarachyLevel(), filterHelperService
							.getHierarchyIdLevelMap(false).getOrDefault(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT, 0));

			List<ParallelJiraServices> listOfTask = new ArrayList<>();
			for (KpiElement kpiEle : kpiRequest.getKpiList()) {

				listOfTask.add(new ParallelJiraServices(kpiRequest, responseList, kpiEle, treeAggregatorDetail));
			}

			ForkJoinTask.invokeAll(listOfTask);
			List<KpiElement> missingKpis = origRequestedKpis.stream()
					.filter(reqKpi -> responseList.stream()
							.noneMatch(responseKpi -> reqKpi.getKpiId().equals(responseKpi.getKpiId())))
					.collect(Collectors.toList());
			responseList.addAll(missingKpis);
			setIntoApplicationCache(kpiRequest, responseList, groupId, kanbanProjectKeyCache);

		} catch (EntityNotFoundException enfe) {

			log.error("[JIRA KANBAN][{}]. Error while KPI calculation for data. No data found {} {}",
					kpiRequest.getRequestTrackerId(), kpiRequest.getKpiList(), enfe);
			throw enfe;

		} catch (Exception e) {
			log.error("[JIRA KANBAN][{}]. Error while KPI calculation for data {} {}", kpiRequest.getRequestTrackerId(),
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
	private String[] getProjectKeyCache(KpiRequest kpiRequest,
			List<AccountHierarchyDataKanban> filteredAccountDataList) {
		return authorizedProjectsService.getKanbanProjectKey(filteredAccountDataList, kpiRequest);
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

	/**
	 * Sets cache.
	 * 
	 * @param kpiRequest
	 * @param responseList
	 * @param groupId
	 */
	private void setIntoApplicationCache(KpiRequest kpiRequest, List<KpiElement> responseList, Integer groupId,
			String[] projects) {
		Integer projectLevel = filterHelperService.getHierarchyIdLevelMap(true)
				.get(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT);
		if (!kpiRequest.getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& projectLevel >= kpiRequest.getLevel()) {
			cacheService.setIntoApplicationCache(projects, responseList, KPISource.JIRAKANBAN.name(), groupId, null);
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

	/**
	 * This class is used to call JIRA based KPIs service in parallel.
	 *
	 * @author pankumar8
	 */
	public class ParallelJiraServices extends RecursiveAction {

		private static final long serialVersionUID = 1L;
		private final KpiRequest kpiRequest;
		private final transient List<KpiElement> responseList;
		private final transient KpiElement kpiEle;
		private final TreeAggregatorDetail treeAggregatorDetail;

		/**
		 * Constructor
		 *
		 * @param kpiRequest
		 * @param responseList
		 * @param kpiEle
		 * @param treeAggregatorDetail
		 */
		public ParallelJiraServices(KpiRequest kpiRequest, List<KpiElement> responseList, KpiElement kpiEle,
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
		@SuppressWarnings("PMD.AvoidCatchingGenericException")
		@Override
		public void compute() {
			try {
				calculateAllKPIAggregatedMetrics(kpiRequest, responseList, kpiEle, treeAggregatorDetail);
			} catch (Exception e) {
				log.error("[PARALLEL_JIRA_SERVICE].Exception occured {}", e);
				log.error("" + kpiEle);
			}
		}

		/**
		 * This method call by multiple thread, take object of specific KPI and call
		 * method of these KPIs
		 *
		 * @param kpiRequest
		 *            JIRA KPI request
		 * @param responseList
		 *            List of KpiElement having data of each KPIs
		 * @param kpiElement
		 * @param treeAggregatorDetail
		 *            filter tree object
		 * @throws ApplicationException
		 * @throws EntityNotFoundException
		 */
		private void calculateAllKPIAggregatedMetrics(KpiRequest kpiRequest, List<KpiElement> responseList,
				KpiElement kpiElement, TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

			long startTime;
			JiraKPIService<?, ?, ?> jiraKPIService = null;
			KPICode kpi = KPICode.getKPI(kpiElement.getKpiId());
			jiraKPIService = JiraKPIServiceFactory.getJiraKPIService(kpi.name());

			startTime = System.currentTimeMillis();

			TreeAggregatorDetail treeAggregatorDetailClone = (TreeAggregatorDetail) SerializationUtils
					.clone(treeAggregatorDetail);
			responseList.add(jiraKPIService.getKpiData(kpiRequest, kpiElement, treeAggregatorDetailClone));

			long processTime = System.currentTimeMillis() - startTime;
			log.info("[JIRA-KANBAN-{}-TIME][{}]. KPI took {} ms", kpi.name(), kpiRequest.getRequestTrackerId(),
					processTime);

		}
	}

}