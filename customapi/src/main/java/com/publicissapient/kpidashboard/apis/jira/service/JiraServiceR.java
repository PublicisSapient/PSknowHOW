/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.jira.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.errors.EntityNotFoundException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.factory.JiraKPIServiceFactory;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueReleaseStatusRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * This class handle all Scrum JIRA based KPI request and call each KPIs service
 * in thread. It is responsible for cache of KPI data at different level.
 *
 * @author tauakram
 *
 */
@Service
@Slf4j
public class JiraServiceR {

	@Autowired
	private KpiHelperService kpiHelperService;

	@Autowired
	private FilterHelperService filterHelperService;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private SprintRepository sprintRepository;
	@Autowired
	private JiraIssueRepository jiraIssueRepository;
	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	@Autowired
	private JiraIssueReleaseStatusRepository jiraIssueReleaseStatusRepository;
	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private CustomApiConfig customApiConfig;

	/**
	 * This method process scrum JIRA based kpi request, cache data and call service
	 * in multiple thread.
	 *
	 * @param kpiRequest
	 *            JIRA KPI request true if flow for precalculated, false for direct
	 *            flow.
	 * @return List of KPI data
	 * @throws EntityNotFoundException
	 *             EntityNotFoundException
	 */
	@SuppressWarnings({ "PMD.AvoidCatchingGenericException", "unchecked" })
	public List<KpiElement> process(KpiRequest kpiRequest) throws EntityNotFoundException {

		log.info("Processing KPI calculation for data {}", kpiRequest.getKpiList());
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
				projectKeyCache = kpiHelperService.getProjectKeyCache(kpiRequest, filteredAccountDataList);

				filteredAccountDataList = kpiHelperService.getAuthorizedFilteredList(kpiRequest, filteredAccountDataList);
				if (filteredAccountDataList.isEmpty()) {
					return responseList;
				}
				Object cachedData = cacheService.getFromApplicationCache(projectKeyCache, KPISource.JIRA.name(),
						groupId, kpiRequest.getSprintIncluded());
				if (!kpiRequest.getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
						&& null != cachedData && isLeadTimeDuration(kpiRequest.getKpiList())) {
					log.info("Fetching value from cache for {}", Arrays.toString(kpiRequest.getIds()));
					return (List<KpiElement>) cachedData;
				}

				TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
						filteredAccountDataList, null, filterHelperService.getFirstHierarachyLevel(),
						filterHelperService.getHierarchyIdLevelMap(false)
								.getOrDefault(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT, 0));

				// set filter value to show on trend line. If subprojects are
				// in
				// selection then show subprojects on trend line else show
				// projects
				kpiRequest.setFilterToShowOnTrend(groupName);

				List<ParallelJiraServices> listOfTask = new ArrayList<>();
				for (KpiElement kpiEle : kpiRequest.getKpiList()) {

					listOfTask.add(new ParallelJiraServices(kpiRequest, responseList, kpiEle, treeAggregatorDetail));
				}

				ForkJoinTask.invokeAll(listOfTask);
				List<KpiElement> missingKpis = origRequestedKpis.stream().filter(reqKpi -> responseList.stream()
						.noneMatch(responseKpi -> reqKpi.getKpiId().equals(responseKpi.getKpiId()))).toList();
				responseList.addAll(missingKpis);

				kpiHelperService.setIntoApplicationCache(kpiRequest, responseList, groupId, projectKeyCache);
			} else {
				responseList.addAll(origRequestedKpis);
			}

		} catch (Exception e) {
			log.error("Error while KPI calculation for data {}", kpiRequest.getKpiList(), e);
			throw new HttpMessageNotWritableException(e.getMessage(), e);
		}

		return responseList;
	}

	private boolean isLeadTimeDuration(List<KpiElement> kpiList) {
		return kpiList.size() != 1 || !kpiList.get(0).getKpiId().equalsIgnoreCase("kpi171");
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

		/*
		 * @param kpiRequest
		 *
		 * @param responseList
		 *
		 * @param kpiEle
		 *
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
		 * @param treeAggregatorDetail
		 *            filter tree object
		 * @throws ApplicationException
		 *             ApplicationException
		 */
		private void calculateAllKPIAggregatedMetrics(KpiRequest kpiRequest, List<KpiElement> responseList,
				KpiElement kpiElement, TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

			JiraKPIService<?, ?, ?> jiraKPIService = null;

			KPICode kpi = KPICode.getKPI(kpiElement.getKpiId());

			jiraKPIService = JiraKPIServiceFactory.getJiraKPIService(kpi.name());

			long startTime = System.currentTimeMillis();

			if (KPICode.THROUGHPUT.equals(kpi)) {
				log.info("No need to fetch Throughput KPI data");
			} else {
				TreeAggregatorDetail treeAggregatorDetailClone = (TreeAggregatorDetail) SerializationUtils
						.clone(treeAggregatorDetail);
				responseList.add(jiraKPIService.getKpiData(kpiRequest, kpiElement, treeAggregatorDetailClone));

				long processTime = System.currentTimeMillis() - startTime;
				log.info("[JIRA-{}-TIME][{}]. KPI took {} ms", kpi.name(), kpiRequest.getRequestTrackerId(),
						processTime);
			}
		}

	}

}
