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

package com.publicissapient.kpidashboard.apis.jira.service.iterationdashboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Service;

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
import com.publicissapient.kpidashboard.apis.model.SprintFilter;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * This class handle all Iteration JIRA based KPI request and call each KPIs
 * service in thread. It is responsible for cache of KPI data at different
 * level.
 *
 * @author purgupta2
 */
@Slf4j
@Service
public class JiraIterationServiceR implements JiraNonTrendKPIServiceR {

	private final ThreadLocal<List<SprintDetails>> threadLocalSprintDetails = ThreadLocal.withInitial(ArrayList::new);
	private final ThreadLocal<List<JiraIssue>> threadLocalJiraIssues = ThreadLocal.withInitial(ArrayList::new);
	private final ThreadLocal<List<JiraIssueCustomHistory>> threadLocalHistory = ThreadLocal
			.withInitial(ArrayList::new);
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
	private List<SprintDetails> sprintDetails;
	private List<JiraIssue> jiraIssueList;
	private List<JiraIssueCustomHistory> jiraIssueCustomHistoryList;

	/**
	 * This method process scrum jira based Iteration kpis request, cache data and
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
			List<AccountHierarchyData> filteredAccountDataList = getFilteredAccountHierarchyData(kpiRequest);
			// List<AccountHierarchyData> filteredAccountDataList =
			// filterHelperService.getFilteredBuilds(kpiRequest,
			// groupName);

			if (!CollectionUtils.isEmpty(filteredAccountDataList)) {
				projectKeyCache = kpiHelperService.getProjectKeyCache(kpiRequest, filteredAccountDataList);

				filteredAccountDataList = kpiHelperService.getAuthorizedFilteredList(kpiRequest,
						filteredAccountDataList);
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

				// List<Node> filteredNodes = filteredAccountDataList.stream()
				// .flatMap(accountHierarchyData ->
				// accountHierarchyData.getNode().stream()
				// .filter(node ->
				// accountHierarchyData.getLeafNodeId().equalsIgnoreCase(node.getId()))
				// )
				// .collect(Collectors.toList());

				Node filteredNode = getFilteredNodes(kpiRequest, filteredAccountDataList);

				if (!CollectionUtils.isEmpty(origRequestedKpis)
						&& StringUtils.isNotEmpty(origRequestedKpis.get(0).getKpiCategory())) {
					updateJiraIssueList(kpiRequest, filteredAccountDataList);
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
						threadLocalSprintDetails.set(sprintDetails);
						threadLocalJiraIssues.set(jiraIssueList);
						threadLocalHistory.set(jiraIssueCustomHistoryList);

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

				// List<ParallelJiraServices> listOfTask = new ArrayList<>();
				// for (KpiElement kpiEle : kpiRequest.getKpiList()) {
				//
				// listOfTask.add(new ParallelJiraServices(kpiRequest, responseList, kpiEle,
				// filteredNodes.get(0)));
				// }
				//
				// ForkJoinTask.invokeAll(listOfTask);
				List<KpiElement> missingKpis = origRequestedKpis.stream()
						.filter(reqKpi -> responseList.stream()
								.noneMatch(responseKpi -> reqKpi.getKpiId().equals(responseKpi.getKpiId())))
						.collect(Collectors.toList());
				responseList.addAll(missingKpis);

				kpiHelperService.setIntoApplicationCache(kpiRequest, responseList, groupId, projectKeyCache);
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
		}

		return responseList;
	}

	private Node getFilteredNodes(KpiRequest kpiRequest, List<AccountHierarchyData> filteredAccountDataList) {
		Node filteredNode = filteredAccountDataList.get(0).getNode().get(kpiRequest.getLevel() - 1);
		Node parentNode = filteredAccountDataList.get(0).getNode().get(kpiRequest.getLevel() - 2);
		filteredNode.setParent(parentNode);

		filteredNode.setProjectFilter(new ProjectFilter(filteredNode.getParent().getId(),
				filteredNode.getParent().getName(), filteredNode.getAccountHierarchy().getBasicProjectConfigId()));
		filteredNode.setSprintFilter(new SprintFilter(filteredNode.getId(), filteredNode.getName(),
				filteredNode.getAccountHierarchy().getBeginDate(), filteredNode.getAccountHierarchy().getEndDate()));

		return filteredNode;
	}

	private List<AccountHierarchyData> getFilteredAccountHierarchyData(KpiRequest kpiRequest) {
		List<AccountHierarchyData> accountDataListAll = (List<AccountHierarchyData>) cacheService
				.cacheAccountHierarchyData();

		return accountDataListAll.stream()
				.filter(accountHierarchyData -> accountHierarchyData.getLeafNodeId()
						.equalsIgnoreCase(kpiRequest.getSelectedMap().get(CommonConstant.SPRINT).get(0)))
				.collect(Collectors.toList());
	}

	private void updateJiraIssueList(KpiRequest kpiRequest, List<AccountHierarchyData> filteredAccountDataList) {
		fetchSprintDetails(kpiRequest.getIds());
		List<String> sprintIssuesList = createIssuesList(
				filteredAccountDataList.get(0).getBasicProjectConfigId().toString());
		fetchJiraIssues(filteredAccountDataList.get(0).getBasicProjectConfigId().toString(), sprintIssuesList);
		fetchJiraIssuesCustomHistory(filteredAccountDataList.get(0).getBasicProjectConfigId().toString(),
				sprintIssuesList);
	}

	public void fetchSprintDetails(String[] sprintId) {
		sprintDetails = sprintRepository.findBySprintIDIn(Arrays.stream(sprintId).collect(Collectors.toList()));
	}

	public SprintDetails getCurrentSprintDetails() {
		return threadLocalSprintDetails.get().stream().findFirst().orElse(null);
	}

	public void setSprintDetails(List<SprintDetails> modifiedSprintDetails) {
		sprintDetails = modifiedSprintDetails;
	}

	public void fetchJiraIssues(String basicProjectConfigId, List<String> sprintIssuesList) {
		jiraIssueList = jiraIssueRepository.findByNumberInAndBasicProjectConfigId(sprintIssuesList,
				basicProjectConfigId);
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

	public void fetchJiraIssuesCustomHistory(String basicProjectConfigId, List<String> sprintIssuesList) {
		jiraIssueCustomHistoryList = jiraIssueCustomHistoryRepository.findByStoryIDInAndBasicProjectConfigIdIn(
				sprintIssuesList, Collections.singletonList(basicProjectConfigId));

	}

	public List<JiraIssueCustomHistory> getJiraIssuesCustomHistoryForCurrentSprint() {
		return threadLocalHistory.get();
	}

	private void calculateAllKPIAggregatedMetrics(KpiRequest kpiRequest, List<KpiElement> responseList,
			KpiElement kpiElement, Node filteredAccountNode) throws ApplicationException {

		JiraIterationKPIService jiraKPIService = null;
		KPICode kpi = KPICode.getKPI(kpiElement.getKpiId());
		jiraKPIService = (JiraIterationKPIService) JiraNonTrendKPIServiceFactory.getJiraKPIService(kpi.name());
		if (jiraKPIService == null) {
			throw new ApplicationException(JiraNonTrendKPIServiceFactory.class,
					"Jira KPI Service Factory not initalized");
		}
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

	// public class ParallelJiraServices extends RecursiveAction {
	// private static final long serialVersionUID = 1L;
	// private final KpiRequest kpiRequest;
	// private final transient List<KpiElement> responseList;
	// private final transient KpiElement kpiEle;
	// Node filteredAccountData;
	// /*
	// * @param kpiRequest
	// *
	// * @param responseList
	// *
	// * @param kpiEle
	// *
	// * @param treeAggregatorDetail
	// */
	// public ParallelJiraServices(KpiRequest kpiRequest, List<KpiElement>
	// responseList, KpiElement kpiEle,
	// Node filteredAccountData) {
	// super();
	// this.kpiRequest = kpiRequest;
	// this.responseList = responseList;
	// this.kpiEle = kpiEle;
	// this.filteredAccountData = filteredAccountData;
	// }
	//
	// /**
	// * {@inheritDoc}
	// * @return
	// */
	// @Override
	// public void compute() {
	// try {
	// threadLocalSprintDetails.set(sprintDetails);
	// threadLocalJiraIssues.set(jiraIssueList);
	// threadLocalHistory.set(jiraIssueCustomHistoryList);
	// calculateAllKPIAggregatedMetrics(kpiRequest, responseList, kpiEle,
	// filteredAccountData);
	// } catch (Exception e) {
	// log.error("[PARALLEL_JIRA_SERVICE].Exception occurred", e);
	// }
	// }
	//
	// /**
	// * This method call by multiple thread, take object of specific KPI and call
	// * method of these KPIs
	// *
	// * @param kpiRequest
	// * JIRA KPI request
	// * @param responseList
	// * List of KpiElements having data of each KPI
	// * @param kpiElement
	// * kpiElement object
	// * @param filteredAccountNodeData
	// * filter tree object
	// * @throws ApplicationException
	// * ApplicationException
	// */
	// private void calculateAllKPIAggregatedMetrics(KpiRequest kpiRequest,
	// List<KpiElement> responseList,
	// KpiElement kpiElement, Node filteredAccountNodeData) throws
	// ApplicationException {
	//
	// JiraIterationKPIService<?, ?, ?> jiraKPIService = null;
	// KPICode kpi = KPICode.getKPI(kpiElement.getKpiId());
	// jiraKPIService =
	// JiraIterationKPIServiceFactory.getJiraKPIService(kpi.name());
	// if (jiraKPIService == null) {
	// throw new ApplicationException(JiraKPIServiceFactory.class, "Jira KPI Service
	// Factory not initalized");
	// }
	// long startTime = System.currentTimeMillis();
	// if (KPICode.THROUGHPUT.equals(kpi)) {
	// log.info("No need to fetch Throughput KPI data");
	// } else {
	// Node nodeDataClone = (Node) SerializationUtils
	// .clone(filteredAccountNodeData);
	// responseList.add(jiraKPIService.getKpiData(kpiRequest, kpiElement,
	// nodeDataClone));
	//
	// long processTime = System.currentTimeMillis() - startTime;
	// log.info("[JIRA-{}-TIME][{}]. KPI took {} ms", kpi.name(),
	// kpiRequest.getRequestTrackerId(),
	// processTime);
	// }
	// }
	// }

}
