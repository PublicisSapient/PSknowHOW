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

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.errors.EntityNotFoundException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.factory.JiraKPIServiceFactory;
import com.publicissapient.kpidashboard.apis.model.*;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.*;
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
	private UserAuthorizedProjectsService authorizedProjectsService;

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

	private final ThreadLocal<List<SprintDetails>> threadLocalSprintDetails = ThreadLocal.withInitial(ArrayList::new);
	private final ThreadLocal<List<JiraIssue>> threadLocalJiraIssues = ThreadLocal.withInitial(ArrayList::new);
	private final ThreadLocal<List<JiraIssueCustomHistory>> threadLocalHistory = ThreadLocal
			.withInitial(ArrayList::new);
	private final ThreadLocal<List<JiraIssue>> threadReleaseIssues = ThreadLocal.withInitial(ArrayList::new);
	private final ThreadLocal<Set<JiraIssue>> threadSubtaskDefects = ThreadLocal.withInitial(HashSet::new);
	private List<SprintDetails> sprintDetails;
	private List<SprintDetails> futureSprintDetails;
	JiraIssueReleaseStatus jiraIssueReleaseStatus = new JiraIssueReleaseStatus();
	private List<JiraIssue> jiraIssueList;
	private List<JiraIssue> jiraIssueReleaseList;
	private Set<JiraIssue> subtaskDefectReleaseList;
	private List<JiraIssueCustomHistory> jiraIssueCustomHistoryList;
	private List<String> releaseList;

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

				TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
						filteredAccountDataList, null, filterHelperService.getFirstHierarachyLevel(),
						filterHelperService.getHierarchyIdLevelMap(false)
								.getOrDefault(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT, 0));

				if (!CollectionUtils.isEmpty(origRequestedKpis)
						&& StringUtils.isNotEmpty(origRequestedKpis.get(0).getKpiCategory())) {
					updateJiraIssueList(kpiRequest, origRequestedKpis, filteredAccountDataList, treeAggregatorDetail);
				}
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
				List<KpiElement> missingKpis = origRequestedKpis.stream()
						.filter(reqKpi -> responseList.stream()
								.noneMatch(responseKpi -> reqKpi.getKpiId().equals(responseKpi.getKpiId())))
						.collect(Collectors.toList());
				responseList.addAll(missingKpis);
			} else {
				responseList.addAll(origRequestedKpis);
			}

		} catch (Exception e) {
			log.error("Error while KPI calculation for data {}", kpiRequest.getKpiList(), e);
			throw new HttpMessageNotWritableException(e.getMessage(), e);
		} finally {
			threadLocalSprintDetails.remove();
			threadLocalJiraIssues.remove();
			threadLocalHistory.remove();
			threadReleaseIssues.remove();
			threadSubtaskDefects.remove();
		}

		return responseList;
	}

	private void updateJiraIssueList(KpiRequest kpiRequest, List<KpiElement> origRequestedKpis,
			List<AccountHierarchyData> filteredAccountDataList, TreeAggregatorDetail treeAggregatorDetail) {
		if (origRequestedKpis.get(0).getKpiCategory().equalsIgnoreCase(CommonConstant.ITERATION)) {
			fetchSprintDetails(kpiRequest.getIds());
			List<String> sprintIssuesList = createIssuesList(
					filteredAccountDataList.get(0).getBasicProjectConfigId().toString());
			fetchJiraIssues(filteredAccountDataList.get(0).getBasicProjectConfigId().toString(), sprintIssuesList,
					CommonConstant.ITERATION);
			fetchJiraIssuesCustomHistory(filteredAccountDataList.get(0).getBasicProjectConfigId().toString(),
					sprintIssuesList, CommonConstant.ITERATION);
		} else if (origRequestedKpis.get(0).getKpiCategory().equalsIgnoreCase(CommonConstant.RELEASE)) {
			releaseList = getReleaseList(treeAggregatorDetail);
			fetchJiraIssues(filteredAccountDataList.get(0).getBasicProjectConfigId().toString(), releaseList,
					CommonConstant.RELEASE);
			fetchJiraIssuesCustomHistory(filteredAccountDataList.get(0).getBasicProjectConfigId().toString(),
					releaseList, CommonConstant.RELEASE);
			fetchJiraIssueReleaseForProject(filteredAccountDataList.get(0).getBasicProjectConfigId().toString(),
					CommonConstant.RELEASE);
		} else if (origRequestedKpis.get(0).getKpiCategory().equalsIgnoreCase(CommonConstant.BACKLOG)) {
			futureProjectWiseSprintDetails(filteredAccountDataList.get(0).getBasicProjectConfigId(),
					SprintDetails.SPRINT_STATE_FUTURE);
			fetchJiraIssues(filteredAccountDataList.get(0).getBasicProjectConfigId().toString(), new ArrayList<>(),
					CommonConstant.BACKLOG);
			fetchJiraIssuesCustomHistory(filteredAccountDataList.get(0).getBasicProjectConfigId().toString(),
					new ArrayList<>(), CommonConstant.BACKLOG);
			fetchJiraIssueReleaseForProject(filteredAccountDataList.get(0).getBasicProjectConfigId().toString(),
					CommonConstant.BACKLOG);
		}
	}

	/**
	 * creating release List on the basis of releaseId
	 * 
	 * @param treeAggregatorDetail
	 * @return release names
	 */
	private List<String> getReleaseList(TreeAggregatorDetail treeAggregatorDetail) {
		List<Node> nodes = treeAggregatorDetail.getMapOfListOfLeafNodes().get(Filters.RELEASE.toString().toLowerCase());
		List<String> processedList = new ArrayList<>();
		if (!CollectionUtils.isEmpty(nodes)) {
			nodes.forEach(releaseNode -> {
				String projectName = CommonConstant.UNDERSCORE + releaseNode.getProjectFilter().getName();
				processedList.add(releaseNode.getReleaseFilter().getName().split(projectName)[0]);
			});
		}
		return processedList;
	}

	/**
	 * @param kpiRequest
	 *            kpiRequest
	 * @param filteredAccountDataList
	 *            filteredAccountDataList
	 * @return list of AccountHierarchyData
	 */
	private List<AccountHierarchyData> getAuthorizedFilteredList(KpiRequest kpiRequest,
			List<AccountHierarchyData> filteredAccountDataList) {
		kpiHelperService.kpiResolution(kpiRequest.getKpiList());
		if (!authorizedProjectsService.ifSuperAdminUser()) {
			filteredAccountDataList = authorizedProjectsService.filterProjects(filteredAccountDataList);
		}

		return filteredAccountDataList;
	}

	/**
	 * @param kpiRequest
	 *            kpiRequest
	 * @param filteredAccountDataList
	 *            filteredAccountDataList
	 */
	private String[] getProjectKeyCache(KpiRequest kpiRequest, List<AccountHierarchyData> filteredAccountDataList) {
		String[] projectKeyCache;
		if (!authorizedProjectsService.ifSuperAdminUser()) {
			projectKeyCache = authorizedProjectsService.getProjectKey(filteredAccountDataList, kpiRequest);
		} else {
			projectKeyCache = kpiRequest.getIds();
		}

		return projectKeyCache;
	}

	/**
	 *
	 * @param kpiRequest
	 *            kpiRequest
	 * @param responseList
	 *            responseList
	 * @param groupId
	 *            groupId
	 */
	private void setIntoApplicationCache(KpiRequest kpiRequest, List<KpiElement> responseList, Integer groupId,
			String[] projects) {
		Integer sprintLevel = filterHelperService.getHierarchyIdLevelMap(false)
				.get(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT);

		if (!kpiRequest.getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& sprintLevel >= kpiRequest.getLevel() && isLeadTimeDuration(kpiRequest.getKpiList())) {
			cacheService.setIntoApplicationCache(projects, responseList, KPISource.JIRA.name(), groupId,
					kpiRequest.getSprintIncluded());
		}

	}

	private boolean isLeadTimeDuration(List<KpiElement> kpiList) {
		return kpiList.size() != 1 || !kpiList.get(0).getKpiId().equalsIgnoreCase("kpi171");
	}

	public void fetchSprintDetails(String[] sprintId) {
		sprintDetails = sprintRepository.findBySprintIDIn(Arrays.stream(sprintId).collect(Collectors.toList()));
	}

	public void futureProjectWiseSprintDetails(ObjectId basicProjectConfigId, String sprintState) {
		futureSprintDetails = sprintRepository
				.findByBasicProjectConfigIdAndStateIgnoreCaseOrderByStartDateASC(basicProjectConfigId, sprintState);
	}

	public SprintDetails getCurrentSprintDetails() {
		return threadLocalSprintDetails.get().stream().findFirst().orElse(null);
	}

	public void setSprintDetails(List<SprintDetails> modifiedSprintDetails) {
		sprintDetails = modifiedSprintDetails;
	}

	public void fetchJiraIssues(String basicProjectConfigId, List<String> sprintIssuesList, String board) {
		if (board.equalsIgnoreCase(CommonConstant.ITERATION)) {
			jiraIssueList = jiraIssueRepository.findByNumberInAndBasicProjectConfigId(sprintIssuesList,
					basicProjectConfigId);
		} else if (board.equalsIgnoreCase(CommonConstant.BACKLOG)) {
			jiraIssueList = jiraIssueRepository.findByBasicProjectConfigIdIn(basicProjectConfigId);
		} else if (board.equalsIgnoreCase(CommonConstant.RELEASE)) {
			jiraIssueReleaseList = jiraIssueRepository
					.findByBasicProjectConfigIdAndReleaseVersionsReleaseNameIn(basicProjectConfigId, sprintIssuesList);
			Set<String> storyIDs = jiraIssueReleaseList.stream().filter(
					jiraIssue -> !jiraIssue.getTypeName().equalsIgnoreCase(NormalizedJira.DEFECT_TYPE.getValue()))
					.map(JiraIssue::getNumber).collect(Collectors.toSet());
			subtaskDefectReleaseList = fetchSubTaskDefectsRelease(basicProjectConfigId, storyIDs);
		}
	}

	private List<String> createIssuesList(String basicProjectConfigId) {
		List<String> totalIssuesList = new ArrayList<>();
		sprintDetails.stream().filter(sd -> sd.getBasicProjectConfigId().toString().equals(basicProjectConfigId))
				.forEach(sprintDetails1 -> {
					if (!CollectionUtils.isEmpty(sprintDetails1.getCompletedIssues())) {
						totalIssuesList.addAll(sprintDetails1.getCompletedIssues().stream().map(SprintIssue::getNumber)
								.collect(Collectors.toList()));
					}
					if (!CollectionUtils.isEmpty(sprintDetails1.getNotCompletedIssues())) {
						totalIssuesList.addAll(sprintDetails1.getNotCompletedIssues().stream()
								.map(SprintIssue::getNumber).collect(Collectors.toList()));
					}
					if (!CollectionUtils.isEmpty(sprintDetails1.getPuntedIssues())) {
						totalIssuesList.addAll(sprintDetails1.getPuntedIssues().stream().map(SprintIssue::getNumber)
								.collect(Collectors.toList()));
					}
					if (!CollectionUtils.isEmpty(sprintDetails1.getCompletedIssuesAnotherSprint())) {
						totalIssuesList.addAll(sprintDetails1.getCompletedIssuesAnotherSprint().stream()
								.map(SprintIssue::getNumber).collect(Collectors.toList()));
					}
					if (!CollectionUtils.isEmpty(sprintDetails1.getAddedIssues())) {
						totalIssuesList.addAll(sprintDetails1.getAddedIssues());
					}
				});
		return totalIssuesList;
	}

	public List<JiraIssue> getJiraIssuesForCurrentSprint() {
		return threadLocalJiraIssues.get();
	}

	public List<JiraIssue> getJiraIssuesForSelectedRelease() {
		return threadReleaseIssues.get();
	}

	public Set<JiraIssue> getSubTaskDefects() {
		return threadSubtaskDefects.get();
	}

	public void fetchJiraIssuesCustomHistory(String basicProjectConfigId, List<String> sprintIssuesList, String board) {
		if (board.equalsIgnoreCase(CommonConstant.ITERATION)) {
			jiraIssueCustomHistoryList = jiraIssueCustomHistoryRepository.findByStoryIDInAndBasicProjectConfigIdIn(
					sprintIssuesList, Collections.singletonList(basicProjectConfigId));
		} else if (board.equalsIgnoreCase(CommonConstant.BACKLOG)) {
			jiraIssueCustomHistoryList = jiraIssueCustomHistoryRepository
					.findByBasicProjectConfigIdIn(basicProjectConfigId);
		} else {
			jiraIssueCustomHistoryList = jiraIssueCustomHistoryRepository.findByFilterAndFromReleaseMap(
					Collections.singletonList(basicProjectConfigId),
					CommonUtils.convertToPatternListForSubString(releaseList));
		}
	}

	public List<JiraIssueCustomHistory> getJiraIssuesCustomHistoryForCurrentSprint() {
		return threadLocalHistory.get();
	}

	public void fetchJiraIssueReleaseForProject(String basicProjectConfigId, String board) {

		if (board.equalsIgnoreCase(CommonConstant.BACKLOG) || board.equalsIgnoreCase(CommonConstant.RELEASE)) {
			jiraIssueReleaseStatus = jiraIssueReleaseStatusRepository.findByBasicProjectConfigId(basicProjectConfigId);
		}
	}

	/**
	 * This method is used to fetch subtask defects which are not tagged to release
	 *
	 * @param projectConfigId
	 *            projectConfigId
	 * @param storyIDs
	 *            storyIDs
	 * @return return
	 */
	private Set<JiraIssue> fetchSubTaskDefectsRelease(String projectConfigId, Set<String> storyIDs) {
		ObjectId basicProjectConfigId = new ObjectId(projectConfigId);
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
		if (CollectionUtils.isNotEmpty(storyIDs) && fieldMapping != null
				&& CollectionUtils.isNotEmpty(fieldMapping.getJiraSubTaskDefectType())) {
			return jiraIssueRepository.findByBasicProjectConfigIdAndDefectStoryIDInAndOriginalTypeIn(projectConfigId,
					storyIDs, fieldMapping.getJiraSubTaskDefectType());
		}
		return new HashSet<>();
	}

	public JiraIssueReleaseStatus getJiraIssueReleaseForProject() {
		return jiraIssueReleaseStatus;
	}

	public List<String> getReleaseList() {
		return releaseList;
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
					.sorted(Comparator.comparing(SprintDetails::getStartDate,
							Comparator.nullsLast(Comparator.naturalOrder())))
					.map(SprintDetails::getSprintName).distinct()
					.limit(customApiConfig.getSprintCountForBackLogStrength()).collect(Collectors.toList());

		}
		return sprintNames;
	}

	/**
	 * for checking available data In Cache Before DB Hit
	 *
	 * @param <S>
	 * @param projects
	 * @param projectsFromCache
	 * @param trendValueList
	 * @param kpiCode
	 * @param sprintIncluded
	 *
	 * @auther deepak
	 */

	public <S> void checkAvailableDataInCacheBeforeDBHit(List<Node> projects, List<Node> projectsFromCache,
			List<S> trendValueList, KPICode kpiCode, List<String> sprintIncluded) {
		projects.forEach(project -> {
			List<DataCountKpiData> dataCountKpisDataList = (List<DataCountKpiData>) cacheService
					.getFromApplicationCache(new String[] { project.getId() }, "JIRA", sprintIncluded);
			if (isNotEmpty(dataCountKpisDataList)) {
				List<S> dataCountList = dataCountKpisDataList.stream()
						.filter(dataCountKpiData -> kpiCode.equals(dataCountKpiData.getKpiName())).findFirst()
						.map(DataCountKpiData::getDataCountList).orElse(new ArrayList<S>());
				if (isNotEmpty(dataCountList)) {
					trendValueList.addAll(dataCountList);
					projectsFromCache.add(project);
				}
			}
		});
	}

	/**
	 * for updating data in cache after DB Hit
	 *
	 * @param <S>
	 * @param map
	 * @param kpiCode
	 * @param sprintIncluded
	 *
	 * @auther deepak
	 */
	public <S> void updateCacheAfterDBHit(Map<String, List<S>> map, KPICode kpiCode, List<String> sprintIncluded) {
		if (MapUtils.isNotEmpty(map))
			map.forEach((projectCacheKey, dataCountCacheValue) -> {
				List<DataCountKpiData> dataCountKpiDataList = (List<DataCountKpiData>) cacheService
						.getFromApplicationCache(new String[] { projectCacheKey }, "JIRA", sprintIncluded);
				if (isEmpty(dataCountKpiDataList))
					dataCountKpiDataList = new ArrayList<>();
				dataCountKpiDataList.add(new DataCountKpiData(kpiCode, dataCountCacheValue));
				cacheService.setIntoApplicationCache(new String[] { projectCacheKey }, dataCountKpiDataList, "JIRA", sprintIncluded);
			});
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
				threadLocalSprintDetails.set(sprintDetails);
				threadLocalJiraIssues.set(jiraIssueList);
				threadLocalHistory.set(jiraIssueCustomHistoryList);
				threadReleaseIssues.set(jiraIssueReleaseList);
				threadSubtaskDefects.set(subtaskDefectReleaseList);
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
		private <S> void calculateAllKPIAggregatedMetrics(KpiRequest kpiRequest, List<KpiElement> responseList,
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
				List<Node> projects = treeAggregatorDetailClone.getMapOfListOfProjectNodes().get("project");
				List<Node> projectsFromCache = new ArrayList<>();
				List<S> trendValueList = new ArrayList<>();
				checkAvailableDataInCacheBeforeDBHit(projects, projectsFromCache, trendValueList,
						KPICode.getKPI(kpiElement.getKpiId()), kpiRequest.getSprintIncluded());
				kpiElement.setProjectsFromCache(projectsFromCache);
				kpiElement.setTrendValueListFormCache(trendValueList);
				KpiElement kpiElementRes = jiraKPIService.getKpiData(kpiRequest, kpiElement, treeAggregatorDetailClone);
				updateCacheAfterDBHit(kpiElementRes.getMapForCache(), KPICode.getKPI(kpiElement.getKpiId()),
						kpiRequest.getSprintIncluded());
				responseList.add(kpiElementRes);

				long processTime = System.currentTimeMillis() - startTime;
				log.info("[JIRA-{}-TIME][{}]. KPI took {} ms", kpi.name(), kpiRequest.getRequestTrackerId(),
						processTime);
			}
		}

	}

}
