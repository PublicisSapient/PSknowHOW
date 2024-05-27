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
package com.publicissapient.kpidashboard.apis.jira.service.releasedashboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import com.publicissapient.kpidashboard.apis.model.ReleaseFilter;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueReleaseStatusRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * This class handle all Release JIRA based KPI request and call each KPIs
 * service in thread. It is responsible for cache of KPI data at different
 * level.
 *
 * @author purgupta2
 */
@Slf4j
@Service
public class JiraReleaseServiceR implements JiraNonTrendKPIServiceR {

	private final ThreadLocal<List<JiraIssue>> threadLocalJiraIssues = ThreadLocal.withInitial(ArrayList::new);
	private final ThreadLocal<List<JiraIssueCustomHistory>> threadLocalHistory = ThreadLocal
			.withInitial(ArrayList::new);
	private final ThreadLocal<List<JiraIssue>> threadReleaseIssues = ThreadLocal.withInitial(ArrayList::new);
	private final ThreadLocal<Set<JiraIssue>> threadSubtaskDefects = ThreadLocal.withInitial(HashSet::new);
	JiraIssueReleaseStatus jiraIssueReleaseStatus = new JiraIssueReleaseStatus();
	@Autowired
	private KpiHelperService kpiHelperService;
	@Autowired
	private FilterHelperService filterHelperService;
	@Autowired
	private CacheService cacheService;
	@Autowired
	private JiraIssueRepository jiraIssueRepository;
	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private JiraIssueReleaseStatusRepository jiraIssueReleaseStatusRepository;
	private List<JiraIssue> jiraIssueList;
	private List<JiraIssue> jiraIssueReleaseList;
	private Set<JiraIssue> subtaskDefectReleaseList;
	private List<JiraIssueCustomHistory> jiraIssueCustomHistoryList;
	private List<String> releaseList;
	private boolean referFromProjectCache = true;

	/**
	 * This method process scrum jira based release kpis request, cache data and
	 * call service in multiple thread.
	 *
	 * @param kpiRequest
	 *            JIRA KPI request true if flow for precalculated, false for direct
	 *            flow.
	 * @return List of KPI data
	 * @throws EntityNotFoundException
	 *             EntityNotFoundException
	 */
	@SuppressWarnings({ "PMD.AvoidCatchingGenericException", "unchecked" })
	@Override
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
			List<AccountHierarchyData> filteredAccountDataList = getFilteredAccountHierarchyData(kpiRequest);

			if (!CollectionUtils.isEmpty(filteredAccountDataList)) {
				projectKeyCache = kpiHelperService.getProjectKeyCache(kpiRequest, filteredAccountDataList, referFromProjectCache);

				filteredAccountDataList = kpiHelperService.getAuthorizedFilteredList(kpiRequest,
						filteredAccountDataList, referFromProjectCache);
				if (filteredAccountDataList.isEmpty()) {
					return responseList;
				}
				Object cachedData = cacheService.getFromApplicationCache(projectKeyCache, KPISource.JIRA.name(),
						groupId, kpiRequest.getSprintIncluded());
				if (!kpiRequest.getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
						&& null != cachedData) {
					log.info("Fetching value from cache for {}", Arrays.toString(kpiRequest.getIds()));
					return (List<KpiElement>) cachedData;
				}

				Node filteredNode = getFilteredNodes(kpiRequest, filteredAccountDataList);

				if (!CollectionUtils.isEmpty(origRequestedKpis)
						&& StringUtils.isNotEmpty(origRequestedKpis.get(0).getKpiCategory())) {
					updateJiraIssueList(filteredAccountDataList, filteredNode);
				}
				// set filter value to show on trend line. If subprojects are
				// in
				// selection then show subprojects on trend line else show
				// projects
				kpiRequest.setFilterToShowOnTrend(groupName);

				ExecutorService executorService = Executors
						.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

				List<CompletableFuture<Void>> futures = new ArrayList<>();

				for (KpiElement kpiEle : kpiRequest.getKpiList()) {
					CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
						threadLocalJiraIssues.set(jiraIssueList);
						threadLocalHistory.set(jiraIssueCustomHistoryList);
						threadReleaseIssues.set(jiraIssueReleaseList);
						threadSubtaskDefects.set(subtaskDefectReleaseList);

						try {
							calculateAllKPIAggregatedMetrics(kpiRequest, responseList, kpiEle, filteredNode);
						} catch (Exception e) {
							log.error("Error while KPI calculation for data {}", kpiRequest.getKpiList(), e);
						}
					}, executorService);
					futures.add(future);
				}

				CompletableFuture<Void>[] futureArray = futures.toArray(new CompletableFuture[0]);

				CompletableFuture<Void> allOf = CompletableFuture.allOf(futureArray);
				allOf.join(); // Wait for all tasks to complete

				executorService.shutdown();
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
		} finally {
			threadLocalJiraIssues.remove();
			threadLocalHistory.remove();
			threadReleaseIssues.remove();
			threadSubtaskDefects.remove();
		}

		return responseList;
	}

	private List<AccountHierarchyData> getFilteredAccountHierarchyData(KpiRequest kpiRequest) {
		List<AccountHierarchyData> accountDataListAll = (List<AccountHierarchyData>) cacheService
				.cacheAccountHierarchyData();

		return accountDataListAll.stream()
				.filter(accountHierarchyData -> accountHierarchyData.getLeafNodeId()
						.equalsIgnoreCase(kpiRequest.getSelectedMap().get(CommonConstant.RELEASE.toLowerCase()).get(0)))
				.toList();
	}

	private Node getFilteredNodes(KpiRequest kpiRequest, List<AccountHierarchyData> filteredAccountDataList) {
		Node filteredNode = filteredAccountDataList.get(0).getNode().get(kpiRequest.getLevel() - 1);
		Node parentNode = filteredAccountDataList.get(0).getNode().get(kpiRequest.getLevel() - 2);
		filteredNode.setParent(parentNode);

		filteredNode.setProjectFilter(new ProjectFilter(filteredNode.getParent().getId(),
				filteredNode.getParent().getName(), filteredNode.getAccountHierarchy().getBasicProjectConfigId()));
		filteredNode.setReleaseFilter(new ReleaseFilter(filteredNode.getId(), filteredNode.getName(),
				filteredNode.getAccountHierarchy().getBeginDate(), filteredNode.getAccountHierarchy().getEndDate()));

		return filteredNode;
	}

	private void updateJiraIssueList(List<AccountHierarchyData> filteredAccountDataList, Node filteredNode) {
		releaseList = getReleaseList(filteredNode);
		fetchJiraIssues(filteredAccountDataList.get(0).getBasicProjectConfigId().toString(), releaseList);
		fetchJiraIssuesCustomHistory(filteredAccountDataList.get(0).getBasicProjectConfigId().toString());
		fetchJiraIssueReleaseForProject(filteredAccountDataList.get(0).getBasicProjectConfigId().toString());
	}

	/**
	 * creating release List on the basis of releaseId
	 *
	 * @param filteredNode
	 *            filteredNode
	 * @return release names
	 */
	private List<String> getReleaseList(Node filteredNode) {
		List<String> processedList = new ArrayList<>();
		String projectName = CommonConstant.UNDERSCORE + filteredNode.getProjectFilter().getName();
		processedList.add(filteredNode.getReleaseFilter().getName().split(projectName)[0]);
		return processedList;
	}

	public void fetchJiraIssues(String basicProjectConfigId, List<String> sprintIssuesList) {
		jiraIssueReleaseList = jiraIssueRepository
				.findByBasicProjectConfigIdAndReleaseVersionsReleaseNameIn(basicProjectConfigId, sprintIssuesList);
		Set<String> storyIDs = jiraIssueReleaseList.stream()
				.filter(jiraIssue -> !jiraIssue.getTypeName().equalsIgnoreCase(NormalizedJira.DEFECT_TYPE.getValue()))
				.map(JiraIssue::getNumber).collect(Collectors.toSet());
		subtaskDefectReleaseList = fetchSubTaskDefectsRelease(basicProjectConfigId, storyIDs);
	}

	public List<JiraIssue> getJiraIssuesForSelectedRelease() {
		return threadReleaseIssues.get();
	}

	public void fetchJiraIssuesCustomHistory(String basicProjectConfigId) {
		jiraIssueCustomHistoryList = jiraIssueCustomHistoryRepository.findByFilterAndFromReleaseMap(
				Collections.singletonList(basicProjectConfigId),
				CommonUtils.convertToPatternListForSubString(releaseList));
	}

	public void fetchJiraIssueReleaseForProject(String basicProjectConfigId) {
		jiraIssueReleaseStatus = jiraIssueReleaseStatusRepository.findByBasicProjectConfigId(basicProjectConfigId);
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

	public Set<JiraIssue> getSubTaskDefects() {
		return threadSubtaskDefects.get();
	}

	public List<JiraIssueCustomHistory> getJiraIssuesCustomHistoryForCurrentSprint() {
		return threadLocalHistory.get();
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
	 *            filtered node object
	 * @throws ApplicationException
	 *             ApplicationException
	 */
	private void calculateAllKPIAggregatedMetrics(KpiRequest kpiRequest, List<KpiElement> responseList,
			KpiElement kpiElement, Node filteredAccountNode) throws ApplicationException {

		JiraReleaseKPIService jiraKPIService = null;
		KPICode kpi = KPICode.getKPI(kpiElement.getKpiId());
		jiraKPIService = (JiraReleaseKPIService) JiraNonTrendKPIServiceFactory.getJiraKPIService(kpi.name());
		long startTime = System.currentTimeMillis();
		if (KPICode.THROUGHPUT.equals(kpi)) {
			log.info("No need to fetch Throughput KPI data");
		} else {
			Node nodeDataClone = (Node) SerializationUtils.clone(filteredAccountNode);
			responseList.add(jiraKPIService.getKpiData(kpiRequest, kpiElement, nodeDataClone));

			long processTime = System.currentTimeMillis() - startTime;
			log.info("[JIRA-{}-TIME][{}]. KPI took {} ms", kpi.name(), kpiRequest.getRequestTrackerId(), processTime);
		}
	}

	public List<KpiElement> processWithExposedApiToken(KpiRequest kpiRequest) throws EntityNotFoundException {
		referFromProjectCache = false;
		List<KpiElement> kpiElementList = process(kpiRequest);
		referFromProjectCache = true;
		return kpiElementList;
	}

}
