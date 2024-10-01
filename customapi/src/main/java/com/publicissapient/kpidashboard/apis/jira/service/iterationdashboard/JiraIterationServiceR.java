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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
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
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
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
	private boolean referFromProjectCache = true;

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
			List<AccountHierarchyData> filteredAccountDataList = getFilteredAccountHierarchyData(kpiRequest, groupName);

			if (!CollectionUtils.isEmpty(filteredAccountDataList)) {
				projectKeyCache = kpiHelperService.getProjectKeyCache(kpiRequest, filteredAccountDataList,
						referFromProjectCache);

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
				if (filteredNode != null) {
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
							responseList.add(calculateAllKPIAggregatedMetrics(kpiRequest, kpiEle, filteredNode));
						}, executorService);
						futures.add(future);
					}

					CompletableFuture<Void>[] futureArray = futures.toArray(new CompletableFuture[0]);

					CompletableFuture<Void> allOf = CompletableFuture.allOf(futureArray);
					allOf.join(); // Wait for all tasks to complete

					executorService.shutdown();

					List<KpiElement> missingKpis = origRequestedKpis.stream()
							.filter(reqKpi -> responseList.stream()
									.noneMatch(responseKpi -> reqKpi.getKpiId().equals(responseKpi.getKpiId())))
							.toList();
					responseList.addAll(missingKpis);

					kpiHelperService.setIntoApplicationCache(kpiRequest, responseList, groupId, projectKeyCache);
				}
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
		Optional<Node> sprintNode = filteredAccountDataList.get(0).getNode().stream()
				.filter(node -> node.getGroupName().equalsIgnoreCase(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT))
				.findFirst();
		Optional<Node> projectNode = filteredAccountDataList.get(0).getNode().stream()
				.filter(node -> node.getGroupName().equalsIgnoreCase(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT))
				.findFirst();
		if (sprintNode.isPresent() && projectNode.isPresent()) {
			Node parentNode = projectNode.get();
			Node filteredNode = sprintNode.get();

			filteredNode.setParent(parentNode);

			filteredNode.setProjectFilter(new ProjectFilter(filteredNode.getParent().getId(),
					filteredNode.getParent().getName(), filteredNode.getAccountHierarchy().getBasicProjectConfigId()));
			filteredNode.setSprintFilter(new SprintFilter(filteredNode.getId(), filteredNode.getName(),
					filteredNode.getAccountHierarchy().getBeginDate(),
					filteredNode.getAccountHierarchy().getEndDate()));

			return filteredNode;
		}
		return null;
	}

	private List<AccountHierarchyData> getFilteredAccountHierarchyData(KpiRequest kpiRequest, String groupName) {
		List<AccountHierarchyData> accountDataListAll = (List<AccountHierarchyData>) cacheService
				.cacheSprintLevelData();

		List<String> selectedValue = kpiRequest.getSelectedMap().getOrDefault(groupName, Collections.emptyList());
		List<String> orDefault = kpiRequest.getSelectedMap().getOrDefault(CommonConstant.SPRINT,
				Collections.emptyList());
		return accountDataListAll.stream()
				.filter(data -> data.getNode().stream().anyMatch(
						d -> d.getGroupName().equalsIgnoreCase(groupName) && selectedValue.contains(d.getId())))
				.filter(data -> data.getNode().stream().anyMatch(
						d -> d.getGroupName().equalsIgnoreCase(CommonConstant.SPRINT) && orDefault.contains(d.getId())))
				.collect(Collectors.toList());
	}

	private void updateJiraIssueList(KpiRequest kpiRequest, List<AccountHierarchyData> filteredAccountDataList) {
		fetchSprintDetails(kpiRequest.getSelectedMap().get(CommonConstant.SPRINT));
		String basicConfigId = filteredAccountDataList.get(0).getBasicProjectConfigId().toString();
		List<String> sprintIssuesList = createIssuesList(basicConfigId);
		fetchJiraIssues(kpiRequest, basicConfigId, sprintIssuesList);
		fetchJiraIssuesCustomHistory(basicConfigId);
	}

	public void fetchSprintDetails(List<String> sprintId) {
		sprintDetails = sprintRepository.findBySprintIDIn(sprintId);
	}

	public SprintDetails getCurrentSprintDetails() {
		return threadLocalSprintDetails.get().stream().findFirst().orElse(null);
	}

	public void setSprintDetails(List<SprintDetails> modifiedSprintDetails) {
		sprintDetails = modifiedSprintDetails;
	}

	public void fetchJiraIssues(KpiRequest kpiRequest, String basicConfigId, List<String> sprintIssuesList) {
		Map<String, Object> mapOfFilter = new HashMap<>();
		createAdditionalFilterMap(kpiRequest, mapOfFilter);
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		uniqueProjectMap.put(basicConfigId, mapOfFilter);
		jiraIssueList = jiraIssueRepository.findIssueByNumberWithAdditionalFilter(new HashSet<>(sprintIssuesList),
				uniqueProjectMap);
	}

	private List<String> createIssuesList(String basicProjectConfigId) {
		List<String> totalIssuesList = new ArrayList<>();
		sprintDetails.stream().filter(sd -> sd.getBasicProjectConfigId().toString().equals(basicProjectConfigId))
				.forEach(sprintDetails1 -> {
					if (!CollectionUtils.isEmpty(sprintDetails1.getCompletedIssues())) {
						totalIssuesList.addAll(
								sprintDetails1.getCompletedIssues().stream().map(SprintIssue::getNumber).toList());
					}
					if (!CollectionUtils.isEmpty(sprintDetails1.getNotCompletedIssues())) {
						totalIssuesList.addAll(
								sprintDetails1.getNotCompletedIssues().stream().map(SprintIssue::getNumber).toList());
					}
					if (!CollectionUtils.isEmpty(sprintDetails1.getPuntedIssues())) {
						totalIssuesList
								.addAll(sprintDetails1.getPuntedIssues().stream().map(SprintIssue::getNumber).toList());
					}
					if (!CollectionUtils.isEmpty(sprintDetails1.getCompletedIssuesAnotherSprint())) {
						totalIssuesList.addAll(sprintDetails1.getCompletedIssuesAnotherSprint().stream()
								.map(SprintIssue::getNumber).toList());
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

	public void fetchJiraIssuesCustomHistory(String basicProjectConfigId) {
		List<String> issueList = jiraIssueList.stream().map(JiraIssue::getNumber).toList();
		jiraIssueCustomHistoryList = jiraIssueCustomHistoryRepository
				.findByStoryIDInAndBasicProjectConfigIdIn(issueList, Collections.singletonList(basicProjectConfigId));

	}

	public List<JiraIssueCustomHistory> getJiraIssuesCustomHistoryForCurrentSprint() {
		return threadLocalHistory.get();
	}

	private KpiElement calculateAllKPIAggregatedMetrics(KpiRequest kpiRequest, KpiElement kpiElement,
			Node filteredAccountNode) {
		try {
			KPICode kpi = KPICode.getKPI(kpiElement.getKpiId());

			JiraIterationKPIService jiraKPIService = (JiraIterationKPIService) JiraNonTrendKPIServiceFactory
					.getJiraKPIService(kpi.name());
			long startTime = System.currentTimeMillis();
			if (KPICode.THROUGHPUT.equals(kpi)) {
				log.info("No need to fetch Throughput KPI data");
			} else {
				Node nodeDataClone = (Node) SerializationUtils.clone(filteredAccountNode);
				if (Objects.nonNull(nodeDataClone)
						&& kpiHelperService.isToolConfigured(kpi, kpiElement, nodeDataClone)) {
					kpiElement = jiraKPIService.getKpiData(kpiRequest, kpiElement, nodeDataClone);
					kpiElement.setResponseCode(CommonConstant.KPI_PASSED);
					kpiHelperService.isMandatoryFieldSet(kpi, kpiElement, nodeDataClone);
				}
				long processTime = System.currentTimeMillis() - startTime;
				log.info("[JIRA-{}-TIME][{}]. KPI took {} ms", kpi.name(), kpiRequest.getRequestTrackerId(),
						processTime);
			}
		} catch (ApplicationException exception) {
			kpiElement.setResponseCode(CommonConstant.KPI_FAILED);
			log.error("Kpi not found", exception);
		} catch (Exception exception) {
			kpiElement.setResponseCode(CommonConstant.KPI_FAILED);
			log.error("Error while KPI calculation for data {}", kpiRequest.getKpiList(), exception);
			return kpiElement;
		}
		return kpiElement;
	}

	public List<KpiElement> processWithExposedApiToken(KpiRequest kpiRequest) throws EntityNotFoundException {
		referFromProjectCache = false;
		List<KpiElement> kpiElementList = process(kpiRequest);
		referFromProjectCache = true;
		return kpiElementList;
	}

	public void createAdditionalFilterMap(KpiRequest kpiRequest, Map<String, Object> mapOfFilters) {
		Map<String, AdditionalFilterCategory> addFilterCat = filterHelperService.getAdditionalFilterHierarchyLevel();
		Map<String, AdditionalFilterCategory> addFilterCategory = addFilterCat.entrySet().stream()
				.collect(Collectors.toMap(entry -> entry.getKey().toUpperCase(), Map.Entry::getValue));

		if (MapUtils.isNotEmpty(kpiRequest.getSelectedMap())) {
			for (Map.Entry<String, List<String>> entry : kpiRequest.getSelectedMap().entrySet()) {
				if (CollectionUtils.isNotEmpty(entry.getValue())
						&& null != addFilterCategory.get(entry.getKey().toUpperCase())) {
					mapOfFilters.put(JiraFeature.ADDITIONAL_FILTERS_FILTERID.getFieldValueInFeature(),
							Arrays.asList(entry.getKey()));
					mapOfFilters.put(JiraFeature.ADDITIONAL_FILTERS_FILTERVALUES_VALUEID.getFieldValueInFeature(),
							entry.getValue());
				}
			}
		}
	}

}
