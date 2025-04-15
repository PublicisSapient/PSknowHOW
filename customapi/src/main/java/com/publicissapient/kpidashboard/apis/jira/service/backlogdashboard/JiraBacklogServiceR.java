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
package com.publicissapient.kpidashboard.apis.jira.service.backlogdashboard;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.errors.EntityNotFoundException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.factory.JiraNonTrendKPIServiceFactory;
import com.publicissapient.kpidashboard.apis.jira.service.JiraNonTrendKPIServiceR;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.ProjectFilter;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueReleaseStatusRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * This class handle all Backlog JIRA based KPI request and call each KPIs
 * service in thread. It is responsible for cache of KPI data at different
 * level.
 *
 * @author purgupta2
 */
@Service
@Slf4j
public class JiraBacklogServiceR implements JiraNonTrendKPIServiceR {

	private final ThreadLocal<List<JiraIssue>> threadLocalJiraIssues = ThreadLocal.withInitial(ArrayList::new);
	private final ThreadLocal<List<JiraIssueCustomHistory>> threadLocalHistory = ThreadLocal.withInitial(ArrayList::new);
	private final ThreadLocal<Set<String>> threadLocalExcludeActiveSprint = ThreadLocal.withInitial(HashSet::new);
	JiraIssueReleaseStatus jiraIssueReleaseStatus = new JiraIssueReleaseStatus();
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
	private CustomApiConfig customApiConfig;
	@Autowired
	private ConfigHelperService configHelperService;
	private List<SprintDetails> futureSprintDetails;
	private List<JiraIssue> jiraIssueList;
	private List<JiraIssueCustomHistory> jiraIssueCustomHistoryList;
	private Set<String> excludeActiveSprintIssueIds;
	private boolean referFromProjectCache = true;

	/**
	 * This method process scrum jira based Backlog kpis request, cache data and
	 * call service in multiple thread.
	 *
	 * @param kpiRequest
	 *          JIRA KPI request true if flow for precalculated, false for direct
	 *          flow.
	 * @return List of KPI data
	 * @throws EntityNotFoundException
	 *           EntityNotFoundException
	 */
	@SuppressWarnings({"PMD.AvoidCatchingGenericException", "unchecked"})
	@Override
	public List<KpiElement> process(KpiRequest kpiRequest) throws EntityNotFoundException {

		log.info("Processing KPI calculation for data {}", kpiRequest.getKpiList());
		List<KpiElement> origRequestedKpis = kpiRequest.getKpiList().stream().map(KpiElement::new).toList();
		List<KpiElement> responseList = new ArrayList<>();
		String[] projectKeyCache = null;
		try {
			Integer groupId = kpiRequest.getKpiList().get(0).getGroupId();
			String groupName = filterHelperService.getHierarachyLevelId(kpiRequest.getLevel(), kpiRequest.getLabel(), false);
			if (null != groupName) {
				kpiRequest.setLabel(groupName.toUpperCase());
			} else {
				log.error("label name for selected hierarchy not found");
			}
			List<AccountHierarchyData> filteredAccountDataList = getFilteredAccountHierarchyData(kpiRequest);
			if (!CollectionUtils.isEmpty(filteredAccountDataList)) {
				projectKeyCache = kpiHelperService.getProjectKeyCache(kpiRequest, filteredAccountDataList,
						referFromProjectCache);

				filteredAccountDataList = kpiHelperService.getAuthorizedFilteredList(kpiRequest, filteredAccountDataList,
						referFromProjectCache);
				if (filteredAccountDataList.isEmpty()) {
					return responseList;
				}
				Object cachedData = cacheService.getFromApplicationCache(projectKeyCache, KPISource.JIRA.name(), groupId,
						kpiRequest.getSprintIncluded());
				if (!kpiRequest.getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase()) &&
						null != cachedData && isLeadTimeDuration(kpiRequest.getKpiList())) {
					log.info("Fetching value from cache for {}", Arrays.toString(kpiRequest.getIds()));
					return (List<KpiElement>) cachedData;
				}

				Node filteredNode = getFilteredNodes(kpiRequest, filteredAccountDataList);

				if (!CollectionUtils.isEmpty(origRequestedKpis) &&
						StringUtils.isNotEmpty(origRequestedKpis.get(0).getKpiCategory())) {
					updateJiraIssueList(filteredAccountDataList);
				}
				// set filter value to show on trend line. If subprojects are
				// in
				// selection then show subprojects on trend line else show
				// projects
				kpiRequest.setFilterToShowOnTrend(groupName);

				List<ParallelJiraServices> listOfTask = new ArrayList<>();
				for (KpiElement kpiEle : kpiRequest.getKpiList()) {
					listOfTask.add(new ParallelJiraServices(kpiRequest, responseList, kpiEle, filteredNode));
				}

				ForkJoinTask.invokeAll(listOfTask);
				List<KpiElement> missingKpis = origRequestedKpis.stream().filter(
						reqKpi -> responseList.stream().noneMatch(responseKpi -> reqKpi.getKpiId().equals(responseKpi.getKpiId())))
						.toList();
				responseList.addAll(missingKpis);

				kpiHelperService.setIntoApplicationCache(kpiRequest, responseList, groupId, projectKeyCache);
			} else {
				responseList.addAll(origRequestedKpis);
			}

		} catch (Exception e) {
			log.error("Error while KPI calculation for data {}", kpiRequest.getKpiList(), e);
			throw new HttpMessageNotWritableException(e.getMessage(), e);
		} finally {
			threadLocalJiraIssues.remove();
			threadLocalHistory.remove();
			threadLocalExcludeActiveSprint.remove();
		}

		return responseList;
	}

	private Node getFilteredNodes(KpiRequest kpiRequest, List<AccountHierarchyData> filteredAccountDataList) {
		Node filteredNode = filteredAccountDataList.get(0).getNode().get(kpiRequest.getLevel() - 1);

		filteredNode.setProjectFilter(new ProjectFilter(filteredNode.getId(), filteredNode.getName(),
				filteredNode.getProjectHierarchy().getBasicProjectConfigId()));
		return filteredNode;
	}

	private List<AccountHierarchyData> getFilteredAccountHierarchyData(KpiRequest kpiRequest) {
		List<AccountHierarchyData> accountDataListAll = (List<AccountHierarchyData>) cacheService
				.cacheAccountHierarchyData();

		List<AccountHierarchyData> hierarchyData = new ArrayList<>();

		String targetNodeId = kpiRequest.getSelectedMap().get(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT).get(0);

		for (AccountHierarchyData data : accountDataListAll) {
			if (data.getLeafNodeId().equalsIgnoreCase(targetNodeId) ||
					data.getNode().stream().anyMatch(node -> node.getId().equalsIgnoreCase(targetNodeId))) {
				hierarchyData.add(data);
				break;
			}
		}

		return hierarchyData;
	}

	private void updateJiraIssueList(List<AccountHierarchyData> filteredAccountDataList) {
		futureProjectWiseSprintDetails(filteredAccountDataList.get(0).getBasicProjectConfigId(),
				SprintDetails.SPRINT_STATE_FUTURE);
		fetchActiveSprintIssues(filteredAccountDataList.get(0).getBasicProjectConfigId());
		fetchJiraIssues(filteredAccountDataList.get(0).getBasicProjectConfigId().toString());
		fetchJiraIssuesCustomHistory(filteredAccountDataList.get(0).getBasicProjectConfigId().toString());
		fetchJiraIssueReleaseForProject(filteredAccountDataList.get(0).getBasicProjectConfigId().toString());
	}

	private boolean isLeadTimeDuration(List<KpiElement> kpiList) {
		return kpiList.size() != 1 || !kpiList.get(0).getKpiId().equalsIgnoreCase("kpi171");
	}

	public void futureProjectWiseSprintDetails(ObjectId basicProjectConfigId, String sprintState) {
		futureSprintDetails = sprintRepository
				.findByBasicProjectConfigIdAndStateIgnoreCaseOrderByStartDateASC(basicProjectConfigId, sprintState);
	}

	public void fetchJiraIssues(String basicProjectConfigId) {
		jiraIssueList = jiraIssueRepository.findByBasicProjectConfigIdAndNumberNotIn(basicProjectConfigId,
				excludeActiveSprintIssueIds);
	}

	public void fetchJiraIssuesCustomHistory(String basicProjectConfigId) {
		jiraIssueCustomHistoryList = jiraIssueCustomHistoryRepository
				.findByBasicProjectConfigIdAndStoryIDNotIn(basicProjectConfigId, excludeActiveSprintIssueIds);
	}

	public void fetchActiveSprintIssues(ObjectId projectId) {
		if (configHelperService.getFieldMapping(projectId).isIncludeActiveSprintInBacklogKPI()) {
			excludeActiveSprintIssueIds = new HashSet<>();
		} else {
			excludeActiveSprintIssueIds = sprintRepository
					.findByBasicProjectConfigIdAndStateIgnoreCaseOrderByStartDateASC(projectId, SprintDetails.SPRINT_STATE_ACTIVE)
					.stream().flatMap(sprint -> sprint.getTotalIssues().stream()).map(SprintIssue::getNumber)
					.collect(Collectors.toSet());
		}
	}

	public void fetchJiraIssueReleaseForProject(String basicProjectConfigId) {
		jiraIssueReleaseStatus = jiraIssueReleaseStatusRepository.findByBasicProjectConfigId(basicProjectConfigId);
	}

	public JiraIssueReleaseStatus getJiraIssueReleaseForProject() {
		return jiraIssueReleaseStatus;
	}

	public List<JiraIssue> getJiraIssuesForCurrentSprint() {
		return threadLocalJiraIssues.get();
	}

	public List<JiraIssueCustomHistory> getJiraIssuesCustomHistoryForCurrentSprint() {
		return threadLocalHistory.get();
	}

	public Set<String> getIssuesOfActiveSprint() {
		return threadLocalExcludeActiveSprint.get();
	}

	/**
	 * This method return list of 5 distinct future sprint names
	 *
	 * @return return list of sprintNames
	 */
	public List<String> getFutureSprintsList() {
		List<String> sprintNames = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(futureSprintDetails)) {
			sprintNames = futureSprintDetails.stream()
					.sorted(Comparator.comparing(SprintDetails::getStartDate, Comparator.nullsLast(Comparator.naturalOrder())))
					.map(SprintDetails::getSprintName).distinct().limit(customApiConfig.getSprintCountForBackLogStrength())
					.toList();
		}
		return sprintNames;
	}

	public class ParallelJiraServices extends RecursiveAction {
		@Serial
		private static final long serialVersionUID = 1L;
		private final KpiRequest kpiRequest;
		private final transient List<KpiElement> responseList;
		private final transient KpiElement kpiEle;
		Node filteredAccountData;

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
				Node filteredAccountData) {
			super();
			this.kpiRequest = kpiRequest;
			this.responseList = responseList;
			this.kpiEle = kpiEle;
			this.filteredAccountData = filteredAccountData;
		}

		/**
		 * {@inheritDoc}
		 *
		 * @return
		 */
		@Override
		public void compute() {

			threadLocalJiraIssues.set(jiraIssueList);
			threadLocalHistory.set(jiraIssueCustomHistoryList);
			threadLocalExcludeActiveSprint.set(excludeActiveSprintIssueIds);
			responseList.add(calculateAllKPIAggregatedMetrics(kpiRequest, kpiEle, filteredAccountData));
		}

		/**
		 * This method call by multiple thread, take object of specific KPI and call
		 * method of these KPIs
		 *
		 * @param kpiRequest
		 *          JIRA KPI request
		 * @param kpiElement
		 *          kpiElement object
		 * @param filteredAccountNode
		 *          filter tree object
		 * @return Kpielement
		 */
		private KpiElement calculateAllKPIAggregatedMetrics(KpiRequest kpiRequest, KpiElement kpiElement,
				Node filteredAccountNode) {
			try {

				KPICode kpi = KPICode.getKPI(kpiElement.getKpiId());

				JiraBacklogKPIService jiraKPIService = (JiraBacklogKPIService) JiraNonTrendKPIServiceFactory
						.getJiraKPIService(kpi.name());
				long startTime = System.currentTimeMillis();
				if (KPICode.THROUGHPUT.equals(kpi)) {
					log.info("No need to fetch Throughput KPI data");
				} else {
					Node nodeDataClone = (Node) SerializationUtils.clone(filteredAccountNode);
					if (Objects.nonNull(nodeDataClone) && kpiHelperService.isToolConfigured(kpi, kpiElement, nodeDataClone)) {
						kpiElement = jiraKPIService.getKpiData(kpiRequest, kpiElement, nodeDataClone);
						kpiElement.setResponseCode(CommonConstant.KPI_PASSED);
						kpiHelperService.isMandatoryFieldSet(kpi, kpiElement, nodeDataClone);
					}

					long processTime = System.currentTimeMillis() - startTime;
					log.info("[JIRA-{}-TIME][{}]. KPI took {} ms", kpi.name(), kpiRequest.getRequestTrackerId(), processTime);
				}
			} catch (ApplicationException exception) {
				kpiElement.setResponseCode(CommonConstant.KPI_FAILED);
				log.error("Kpi not found", exception);
			} catch (Exception exception) {
				kpiElement.setResponseCode(CommonConstant.KPI_FAILED);
				log.error("[PARALLEL_JIRA_BACKLOG_SERVICE].Exception occurred", exception);
				return kpiElement;
			}
			return kpiElement;
		}
	}

	public List<KpiElement> processWithExposedApiToken(KpiRequest kpiRequest) throws EntityNotFoundException {
		referFromProjectCache = false;
		List<KpiElement> kpiElementList = process(kpiRequest);
		referFromProjectCache = true;
		return kpiElementList;
	}
}
