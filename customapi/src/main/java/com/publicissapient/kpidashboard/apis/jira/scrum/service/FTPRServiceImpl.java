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

package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.IterationKpiData;
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.IterationKpiHelper;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FTPRServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	public static final String UNCHECKED = "unchecked";
	public static final String DEFECT = "Defect";
	public static final String PERCENTAGE = "percentage";
	private static final String ISSUES = "issues";
	private static final String FIRST_TIME_PASS_STORIES = "First Time Pass Stories";
	private static final String TOTAL_STORIES = "Total Stories";
	private static final String FIRST_TIME_PASS_PERCENTAGE = "First Time Pass Rate";
	private static final String OVERALL = "Overall";
	private static final String SPRINT_DETAILS = "sprint details";

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private KpiHelperService kpiHelperService;

	@Autowired
	private SprintRepository sprintRepository;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private FilterHelperService flterHelperService;

	/**
	 *
	 * @param fieldMapping fieldMapping of the project
	 * @param allIssues all issues of sprint
	 * @param totalStoryList totalStoryList
	 * @return totalStoryList after filtering
	 */
	private static List<JiraIssue> getTotalStoryList(FieldMapping fieldMapping, List<JiraIssue> allIssues,
			List<JiraIssue> totalStoryList) {
		if (Optional.ofNullable(fieldMapping.getJiraKPI135StoryIdentification()).isPresent()) {
			totalStoryList = allIssues.stream().filter(
					jiraIssue -> fieldMapping.getJiraKPI135StoryIdentification().contains(jiraIssue.getTypeName()))
					.collect(Collectors.toList());

			// exclude the issue from total stories based on defect rejection status
			if (Optional.ofNullable(fieldMapping.getJiraDefectRejectionStatusKPI135()).isPresent()) {
				totalStoryList = totalStoryList.stream().filter(
						jiraIssue -> !jiraIssue.getStatus().equals(fieldMapping.getJiraDefectRejectionStatusKPI135()))
						.collect(Collectors.toList());
			}
			if (CollectionUtils.isNotEmpty(fieldMapping.getJiraLabelsKPI135())) {
				totalStoryList = totalStoryList.stream().filter(jiraIssue -> fieldMapping.getJiraLabelsKPI135().stream()
						.anyMatch(label -> jiraIssue.getLabels().contains(label))).collect(Collectors.toList());
			}
		}
		return totalStoryList;
	}

	private static Set<String> getDefectIds(Set<String> listOfStory, List<JiraIssue> totalDeffects) {
		if (CollectionUtils.isNotEmpty(totalDeffects)) {
			listOfStory = totalDeffects.stream().map(JiraIssue::getDefectStoryID).flatMap(Set::stream)
					.collect(Collectors.toSet());
		}
		return listOfStory;
	}

	private static void getNotFtprDefects(Map<String, Set<String>> projectWiseRCA, Set<JiraIssue> defects,
			List<JiraIssue> notFTPRDefects) {
		for (JiraIssue jiraIssue : defects) {
			// Filter priorityRemaining based on configured Root Causes (RCA) for the project, or include if no RCA is configured.
			if (org.apache.commons.collections4.CollectionUtils
					.isNotEmpty(projectWiseRCA.get(jiraIssue.getBasicProjectConfigId()))) {
				for (String toFindRca : jiraIssue.getRootCauseList()) {
					if ((projectWiseRCA.get(jiraIssue.getBasicProjectConfigId()).contains(toFindRca.toLowerCase()))) {
						notFTPRDefects.add(jiraIssue);
					}
				}
			} else {
				notFTPRDefects.add(jiraIssue);
			}
		}
	}

	private static void getRemainingDefects(Map<String, List<String>> projectWisePriority, Set<JiraIssue> defects,
			List<JiraIssue> remainingDefects) {
		for (JiraIssue jiraIssue : defects) {
			if (org.apache.commons.collections4.CollectionUtils
					.isNotEmpty(projectWisePriority.get(jiraIssue.getBasicProjectConfigId()))) {
				if (!(projectWisePriority.get(jiraIssue.getBasicProjectConfigId())
						.contains(jiraIssue.getPriority().toLowerCase()))) {
					remainingDefects.add(jiraIssue);
				}
			} else {
				remainingDefects.add(jiraIssue);
			}
		}
	}

	private double calculateFTPR(double priorityWiseFTPS, double priorityWiseTotalStory) {
		return roundingOff((priorityWiseFTPS * 100) / priorityWiseTotalStory);
	}

	@Override
	public Integer calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		DataCount trendValue = new DataCount();
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {

			Filters filters = Filters.getFilter(k);
			if (Filters.SPRINT == filters) {
				projectWiseLeafNodeValue(v, trendValue, kpiElement, kpiRequest);
			}
		});

		return kpiElement;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {

		Map<String, Object> resultListMap = new HashMap<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);
		if (null != leafNode) {
			log.info("First Time Pass rate -> Requested sprint : {}", leafNode.getName());
			String basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId().toString();
			List<String> defectType = new ArrayList<>();
			Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
			Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
			SprintDetails dbSprintDetail = getSprintDetailsFromBaseClass();
			SprintDetails sprintDetails;
			if (null != dbSprintDetail) {
				FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
						.get(leafNode.getProjectFilter().getBasicProjectConfigId());
				// to modify sprintdetails on the basis of configuration for the project
				List<JiraIssueCustomHistory> totalHistoryList = getJiraIssuesCustomHistoryFromBaseClass();
				List<JiraIssue> totalJiraIssueList = getJiraIssuesFromBaseClass();
				Set<String> issueList = totalJiraIssueList.stream().map(JiraIssue::getNumber)
						.collect(Collectors.toSet());

				sprintDetails = IterationKpiHelper.transformIterSprintdetail(totalHistoryList, issueList,
						dbSprintDetail, new ArrayList<>(), fieldMapping.getJiraIterationCompletionStatusKPI135(),
						leafNode.getProjectFilter().getBasicProjectConfigId());

				List<String> completedIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.COMPLETED_ISSUES);
				if (CollectionUtils.isNotEmpty(completedIssues)) {
					List<JiraIssue> jiraIssueList = jiraIssueRepository
							.findByNumberInAndBasicProjectConfigId(completedIssues, basicProjectConfigId);
					List<String> defectTypes = Optional.ofNullable(fieldMapping).map(FieldMapping::getJiradefecttype)
							.orElse(Collections.emptyList());
					Set<String> completedSprintReportDefects = new HashSet<>();
					Set<String> completedSprintReportStories = new HashSet<>();
					sprintDetails.getCompletedIssues().stream().forEach(sprintIssue -> {
						if (defectTypes.contains(sprintIssue.getTypeName())) {
							completedSprintReportDefects.add(sprintIssue.getNumber());
						} else {
							completedSprintReportStories.add(sprintIssue.getNumber());
						}
					});

					Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
					defectType.add(NormalizedJira.DEFECT_TYPE.getValue());
					mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
							CommonUtils.convertToPatternList(defectType));
					uniqueProjectMap.put(basicProjectConfigId, mapOfProjectFilters);
					mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
							Collections.singletonList(basicProjectConfigId));

					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getCompletedIssues(), jiraIssueList);

					// fetched all defects which is linked to current sprint report stories
					List<JiraIssue> linkedDefects = jiraIssueRepository.findLinkedDefects(mapOfFilters,
							completedSprintReportStories, uniqueProjectMap);
					List<JiraIssue> completedIssueList = new ArrayList<>();
					completedIssueList.addAll(filtersIssuesList);
					completedIssueList.addAll(linkedDefects);

					Collection<JiraIssue> issues = completedIssueList.stream().collect(Collectors
							.toMap(JiraIssue::getNumber, Function.identity(), (e1, e2) -> e2, LinkedHashMap::new))
							.values();

					resultListMap.put(ISSUES, new ArrayList<>(issues));
					resultListMap.put(SPRINT_DETAILS, sprintDetails);
				}
			}
		}
		return resultListMap;
	}

	@Override
	public String getQualifierType() {
		return KPICode.FIRST_TIME_PASS_RATE_ITERATION.name();
	}

	private void projectWiseLeafNodeValue(List<Node> sprintLeafNodeList, DataCount trendValue, KpiElement kpiElement,
			KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();
		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));
		List<Node> latestSprintNode = new ArrayList<>();
		Node latestSprint = sprintLeafNodeList.get(0);
		Optional.ofNullable(latestSprint).ifPresent(latestSprintNode::add);
		ObjectId basicProjectConfigId = latestSprint.getProjectFilter().getBasicProjectConfigId();
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);

		List<JiraIssue> allIssues = (List<JiraIssue>) resultMap.get(ISSUES);

		if (CollectionUtils.isNotEmpty(allIssues)) {
			log.info("First Time Pass rate -> request id : {} total jira Issues : {}", requestTrackerId,
					allIssues.size());
			// Creating map of modal Objects
			Map<String, IterationKpiModalValue> modalObjectMap = KpiDataHelper.createMapOfModalObject(allIssues);
			List<JiraIssue> totalStoryList = new ArrayList<>();
			List<JiraIssue> totalJiraIssues = new ArrayList<>();
			Map<String, List<String>> projectWisePriority = new HashMap<>();
			Map<String, List<String>> configPriority = customApiConfig.getPriority();
			Map<String, Set<String>> projectWiseRCA = new HashMap<>();
			Map<String, Map<String, List<String>>> droppedDefects = new HashMap<>();
			Set<String> listOfStory = new HashSet<>();

			// Total stories from issues completed collection in a sprint
			totalStoryList = getTotalStoryList(fieldMapping, allIssues, totalStoryList);

			KpiHelperService.addPriorityProjectWise(projectWisePriority, configPriority, latestSprint,
					fieldMapping.getDefectPriorityKPI135());
			KpiHelperService.addRCAProjectWise(projectWiseRCA, latestSprint, fieldMapping.getIncludeRCAForKPI135());
			KpiHelperService.getDroppedDefectsFilters(droppedDefects, basicProjectConfigId,
					fieldMapping.getResolutionTypeForRejectionKPI135(),
					fieldMapping.getJiraDefectRejectionStatusKPI135());
			KpiHelperService.getDefectsWithoutDrop(droppedDefects, allIssues, totalJiraIssues);

			List<String> defectTypes = Optional.ofNullable(fieldMapping).map(FieldMapping::getJiradefecttype)
					.orElse(Collections.emptyList());
			defectTypes.add(NormalizedJira.DEFECT_TYPE.getValue());
			List<JiraIssue> allDefects = totalJiraIssues.stream()
					.filter(issue -> defectTypes.contains(issue.getTypeName())).collect(Collectors.toList());
			List<JiraIssue> ftprStory = new ArrayList<>();
			ftprStory.addAll(totalStoryList);

			listOfStory = getDefectIds(listOfStory, allDefects);

			removeStoriesWithDefect(ftprStory, projectWisePriority, projectWiseRCA, allDefects, droppedDefects);

			List<String> storyIds = getIssueIds(ftprStory);
			List<JiraIssueCustomHistory> storiesHistory = jiraIssueCustomHistoryRepository.findByStoryIDIn(storyIds);
			if (CollectionUtils.isNotEmpty(ftprStory) && CollectionUtils.isNotEmpty(storiesHistory)) {

				ftprStory.removeIf(issue -> kpiHelperService.hasReturnTransactionOrFTPRRejectedStatus(issue,
						storiesHistory, fieldMapping.getJiraStatusForDevelopmentKPI135(),
						fieldMapping.getJiraStatusForQaKPI135(), fieldMapping.getJiraFtprRejectStatusKPI135()));

			}

			List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
			List<Integer> overAllFTPS = Arrays.asList(0);
			List<Integer> overAllStory = Arrays.asList(0);
			List<IterationKpiModalValue> overAllmodalValues = new ArrayList<>();
			List<IterationKpiModalValue> modalValues = new ArrayList<>();

			for (JiraIssue jiraIssue : totalStoryList) {

				overAllStory.set(0, overAllStory.get(0) + 1);

				if (CollectionUtils.isNotEmpty(ftprStory) && ftprStory.contains(jiraIssue)) {
					overAllFTPS.set(0, overAllFTPS.get(0) + 1);
				}

				KPIExcelUtility.populateIterationKPI(overAllmodalValues, modalValues, jiraIssue, fieldMapping,
						modalObjectMap);
				setKPISpecificData(modalObjectMap, listOfStory, allDefects, ftprStory, jiraIssue);

			}

			List<IterationKpiData> data = new ArrayList<>();

			IterationKpiData overAllFTPRStories = new IterationKpiData(FIRST_TIME_PASS_STORIES,
					Double.valueOf(overAllFTPS.get(0)), null, null, null, null);

			IterationKpiData overAllStories = new IterationKpiData(TOTAL_STORIES, Double.valueOf(overAllStory.get(0)),
					null, null, null, overAllmodalValues);

			IterationKpiData overAllFTPRPercentage = new IterationKpiData(FIRST_TIME_PASS_PERCENTAGE,
					calculateFTPR(overAllFTPS.get(0), overAllStory.get(0)), null, null, Constant.PERCENTAGE, null);

			data.add(overAllFTPRStories);
			data.add(overAllStories);
			data.add(overAllFTPRPercentage);
			IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, null, data);
			iterationKpiValues.add(overAllIterationKpiValue);

			trendValue.setValue(iterationKpiValues);
			kpiElement.setSprint(latestSprint.getName());
			kpiElement.setModalHeads(KPIExcelColumn.FIRST_TIME_PASS_RATE_ITERATION.getColumns());
			kpiElement.setTrendValueList(trendValue);
		}

	}

	@NotNull
	private List<String> getIssueIds(List<JiraIssue> issuesBySprintAndType) {
		List<String> storyIds = new ArrayList<>();
		org.apache.commons.collections4.CollectionUtils.emptyIfNull(issuesBySprintAndType)
				.forEach(story -> storyIds.add(story.getNumber()));
		return storyIds;
	}

	private void removeStoriesWithDefect(List<JiraIssue> totalJiraIssues, Map<String, List<String>> projectWisePriority,
			Map<String, Set<String>> projectWiseRCA, List<JiraIssue> totalDeffects,
			Map<String, Map<String, List<String>>> statusConfigsOfRejectedStoriesByProject) {

		Set<JiraIssue> defects = new HashSet<>();
		List<JiraIssue> defectListWoDrop = new ArrayList<>();
		KpiHelperService.getDefectsWithoutDrop(statusConfigsOfRejectedStoriesByProject, totalDeffects,
				defectListWoDrop);
		defectListWoDrop.stream().forEach(d -> totalJiraIssues.stream().forEach(i -> {
			if (i.getProjectName().equalsIgnoreCase(d.getProjectName())) {
				defects.add(d);
			}
		}));

		List<JiraIssue> remainingDefects = new ArrayList<>();
		getRemainingDefects(projectWisePriority, defects, remainingDefects);

		List<JiraIssue> notFTPRDefects = new ArrayList<>();
		getNotFtprDefects(projectWiseRCA, defects, notFTPRDefects);

		Set<String> storyIdsWithDefect = new HashSet<>();
		remainingDefects.stream().forEach(pi -> notFTPRDefects.stream().forEach(ri -> {
			if (pi.getNumber().equalsIgnoreCase(ri.getNumber())) {
				storyIdsWithDefect.addAll(ri.getDefectStoryID());
			}
		}));
		totalJiraIssues.removeIf(issue -> storyIdsWithDefect.contains(issue.getNumber()));
	}

	private void setKPISpecificData(Map<String, IterationKpiModalValue> modalObjectMap, Set<String> listOfStory,
			List<JiraIssue> allDefects, List<JiraIssue> ftprStory, JiraIssue jiraIssue) {
		IterationKpiModalValue jiraIssueModalObject = modalObjectMap.get(jiraIssue.getNumber());
		if (CollectionUtils.isNotEmpty(listOfStory) && listOfStory.contains(jiraIssue.getNumber())) {

			Map<String, String> linkedDefects = new HashMap<>();
			allDefects.stream().filter(d -> d.getDefectStoryID().contains(jiraIssue.getNumber()))
					.forEach(defect -> linkedDefects.putIfAbsent(defect.getNumber(), defect.getUrl()));

			jiraIssueModalObject.setLinkedDefefect(linkedDefects);

			Map<String, String> linkedDefectsPriority = new HashMap<>();
			allDefects.stream().filter(d -> d.getDefectStoryID().contains(jiraIssue.getNumber()))
					.forEach(defect -> linkedDefectsPriority.putIfAbsent(defect.getNumber(), defect.getPriority()));
			jiraIssueModalObject.setLinkedDefefectPriority(linkedDefectsPriority);
		}

		if (CollectionUtils.isNotEmpty(ftprStory) && ftprStory.contains(jiraIssue)) {
			jiraIssueModalObject.setFirstTimePass("Y");
		}
	}

}
