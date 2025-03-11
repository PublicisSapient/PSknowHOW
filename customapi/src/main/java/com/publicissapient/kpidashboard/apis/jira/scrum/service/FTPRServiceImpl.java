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

import static com.publicissapient.kpidashboard.apis.util.IterationKpiHelper.getFilteredJiraIssue;

import java.util.*;
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
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.iterationdashboard.JiraIterationKPIService;
import com.publicissapient.kpidashboard.apis.model.*;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.IterationKpiHelper;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FTPRServiceImpl extends JiraIterationKPIService {

	public static final String UNCHECKED = "unchecked";
	private static final String ISSUES = "issues";
	private static final String SPRINT_DETAILS = "sprint details";

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private KpiHelperService kpiHelperService;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	/**
	 * @param fieldMapping
	 *          fieldMapping of the project
	 * @param allIssues
	 *          all issues of sprint
	 * @return totalStoryList after filtering
	 */
	private static List<JiraIssue> getTotalStoryList(FieldMapping fieldMapping, List<JiraIssue> allIssues) {
		List<JiraIssue> totalStoryList = new ArrayList<>();
		if (Optional.ofNullable(fieldMapping.getJiraKPI135StoryIdentification()).isPresent()) {
			totalStoryList = allIssues.stream()
					.filter(jiraIssue -> fieldMapping.getJiraKPI135StoryIdentification().contains(jiraIssue.getTypeName()))
					.collect(Collectors.toList());

			// exclude the issue from total stories based on defect rejection status
			if (Optional.ofNullable(fieldMapping.getJiraDefectRejectionStatusKPI135()).isPresent()) {
				totalStoryList = totalStoryList.stream()
						.filter(jiraIssue -> !jiraIssue.getStatus().equals(fieldMapping.getJiraDefectRejectionStatusKPI135()))
						.collect(Collectors.toList());
			}
			if (CollectionUtils.isNotEmpty(fieldMapping.getJiraLabelsKPI135())) {
				totalStoryList = totalStoryList.stream().filter(jiraIssue -> fieldMapping.getJiraLabelsKPI135().stream()
						.anyMatch(label -> jiraIssue.getLabels().contains(label))).collect(Collectors.toList());
			}
		}
		return totalStoryList;
	}

	private static Set<String> getDefectIds(List<JiraIssue> totalDefects) {
		Set<String> listOfStory = new HashSet<>();
		if (CollectionUtils.isNotEmpty(totalDefects)) {
			listOfStory = totalDefects.stream().map(JiraIssue::getDefectStoryID).flatMap(Set::stream)
					.collect(Collectors.toSet());
		}
		return listOfStory;
	}

	private static void getNotFtprDefects(Map<String, Set<String>> projectWiseRCA, Set<JiraIssue> defects,
			List<JiraIssue> notFTPRDefects) {
		for (JiraIssue jiraIssue : defects) {
			// Filter priorityRemaining based on configured Root Causes (RCA) for the
			// project, or include if no RCA is configured.
			if (CollectionUtils.isNotEmpty(projectWiseRCA.get(jiraIssue.getBasicProjectConfigId()))) {
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

	private double calculateFTPR(double priorityWiseFTPS, double priorityWiseTotalStory) {
		return roundingOff((priorityWiseFTPS * 100) / priorityWiseTotalStory);
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node sprintNode)
			throws ApplicationException {
		projectWiseLeafNodeValue(sprintNode, kpiElement, kpiRequest);
		return kpiElement;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(Node leafNode, String startDate, String endDate,
			KpiRequest kpiRequest) {

		Map<String, Object> resultListMap = new HashMap<>();
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
				// to modify sprint details on the basis of configuration for the project
				List<JiraIssueCustomHistory> totalHistoryList = getJiraIssuesCustomHistoryFromBaseClass();
				List<JiraIssue> totalJiraIssueList = getJiraIssuesFromBaseClass();
				Set<String> issueList = totalJiraIssueList.stream().map(JiraIssue::getNumber).collect(Collectors.toSet());

				sprintDetails = IterationKpiHelper.transformIterSprintdetail(totalHistoryList, issueList, dbSprintDetail,
						new ArrayList<>(), fieldMapping.getJiraIterationCompletionStatusKPI135(),
						leafNode.getProjectFilter().getBasicProjectConfigId());

				List<String> completedIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.COMPLETED_ISSUES);
				if (CollectionUtils.isNotEmpty(completedIssues)) {
					List<JiraIssue> filteredJiraIssue = getFilteredJiraIssue(completedIssues, totalJiraIssueList);
					List<String> defectTypes = new ArrayList<>(
							Optional.ofNullable(fieldMapping.getJiradefecttype()).orElse(Collections.emptyList()));
					Set<String> completedSprintReportDefects = new HashSet<>();
					Set<String> completedSprintReportStories = new HashSet<>();
					filteredJiraIssue.forEach(sprintIssue -> {
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

					Set<JiraIssue> filtersIssuesList = KpiDataHelper.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(
							sprintDetails, sprintDetails.getCompletedIssues(), filteredJiraIssue);

					// fetched all defects which is linked to current sprint report stories
					List<JiraIssue> linkedDefects = jiraIssueRepository.findLinkedDefects(mapOfFilters,
							completedSprintReportStories, uniqueProjectMap);
					List<JiraIssue> completedIssueList = new ArrayList<>();
					completedIssueList.addAll(filtersIssuesList);
					completedIssueList.addAll(linkedDefects);

					Collection<JiraIssue> issues = completedIssueList.stream()
							.collect(Collectors.toMap(JiraIssue::getNumber, Function.identity(), (e1, e2) -> e2, LinkedHashMap::new))
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

	private void projectWiseLeafNodeValue(Node latestSprint, KpiElement kpiElement, KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();
		ObjectId basicProjectConfigId = latestSprint.getProjectFilter().getBasicProjectConfigId();
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprint, null, null, kpiRequest);

		List<JiraIssue> allIssues = (List<JiraIssue>) resultMap.get(ISSUES);

		if (CollectionUtils.isNotEmpty(allIssues)) {
			log.info("First Time Pass rate -> request id : {} total jira Issues : {}", requestTrackerId,
					allIssues.size());
			List<JiraIssue> totalJiraIssues = new ArrayList<>();
			Map<String, Map<String, Integer>> projectWisePriority = new HashMap<>();
			Map<String, List<String>> configPriority = customApiConfig.getPriority();
			Map<String, Set<String>> projectWiseRCA = new HashMap<>();
			Map<String, Map<String, List<String>>> droppedDefects = new HashMap<>();

			// Total stories from issues completed collection in a sprint
			List<JiraIssue> totalStoryList = getTotalStoryList(fieldMapping, allIssues);

			KpiHelperService.addPriorityCountProjectWise(projectWisePriority, configPriority, latestSprint,
					fieldMapping.getDefectPriorityKPI135());
			KpiHelperService.addRCAProjectWise(projectWiseRCA, basicProjectConfigId.toString(),
					fieldMapping.getIncludeRCAForKPI135());
			KpiHelperService.getDroppedDefectsFilters(droppedDefects, basicProjectConfigId,
					fieldMapping.getResolutionTypeForRejectionKPI135(),
					fieldMapping.getJiraDefectRejectionStatusKPI135());
			KpiHelperService.getDefectsWithoutDrop(droppedDefects, allIssues, totalJiraIssues);

			List<String> defectTypes = new ArrayList<>(
					Optional.ofNullable(fieldMapping.getJiradefecttype()).orElse(Collections.emptyList()));
			defectTypes.add(NormalizedJira.DEFECT_TYPE.getValue());
			List<JiraIssue> allDefects = totalJiraIssues.stream()
					.filter(issue -> defectTypes.contains(issue.getTypeName())).collect(Collectors.toList());
			List<JiraIssue> ftprStory = new ArrayList<>(totalStoryList);

			Set<String> listOfStory = getDefectIds(allDefects);

			removeStoriesWithDefect(ftprStory, projectWisePriority, projectWiseRCA, allDefects, droppedDefects);

			List<String> storyIds = getIssueIds(ftprStory);
			List<JiraIssueCustomHistory> storiesHistory = jiraIssueCustomHistoryRepository.findByStoryIDIn(storyIds);
			if (CollectionUtils.isNotEmpty(ftprStory) && CollectionUtils.isNotEmpty(storiesHistory)) {

				ftprStory.removeIf(issue -> kpiHelperService.hasReturnTransactionOrFTPRRejectedStatus(issue,
						storiesHistory, fieldMapping.getJiraStatusForDevelopmentKPI135(),
						fieldMapping.getJiraStatusForQaKPI135(), fieldMapping.getJiraFtprRejectStatusKPI135()));
			}

			// Creating map of modal Objects
			Map<String, IssueKpiModalValue> issueKpiModalObject = KpiDataHelper.createMapOfIssueModal(totalStoryList);
			for (JiraIssue issue : totalStoryList) {
				KPIExcelUtility.populateIssueModal(issue, fieldMapping, issueKpiModalObject);
				IssueKpiModalValue data = issueKpiModalObject.get(issue.getNumber());
				setKPISpecificData(data, listOfStory, allDefects, ftprStory, issue);
			}

			kpiElement.setSprint(latestSprint.getName());
			kpiElement.setModalHeads(KPIExcelColumn.FIRST_TIME_PASS_RATE_ITERATION.getColumns());
			kpiElement.setIssueData(new HashSet<>(issueKpiModalObject.values()));
			kpiElement.setDataGroup(createDataGroup((double) totalStoryList.size(), (double) ftprStory.size()));
		}
	}

	/**
	 * @param totalStories
	 * @param ftpStoryCount
	 * @return
	 */
	private KpiDataGroup createDataGroup(Double totalStories, Double ftpStoryCount) {
		KpiDataGroup dataGroup = new KpiDataGroup();
		List<KpiData> dataGroup1 = new ArrayList<>();
		dataGroup1.add(createKpiData("First Time Pass Stories", 1, "", ftpStoryCount));
		dataGroup1.add(createKpiData("Total Stories", 2, "", totalStories));
		dataGroup1.add(createKpiData("%", 3, "", calculateFTPR(ftpStoryCount, totalStories)));
		dataGroup.setDataGroup1(dataGroup1);
		return dataGroup;
	}

	/**
	 * Creates kpi data object.
	 *
	 * @param name
	 * @param order
	 * @param unit
	 * @return
	 */
	private KpiData createKpiData(String name, Integer order, String unit, Double kpiValue) {
		KpiData data = new KpiData();
		data.setName(name);
		data.setOrder(order);
		data.setUnit(unit);
		data.setShowAsLegend(false);
		data.setKpiValue(kpiValue);
		return data;
	}

	@NotNull
	private List<String> getIssueIds(List<JiraIssue> issuesBySprintAndType) {
		List<String> storyIds = new ArrayList<>();
		CollectionUtils.emptyIfNull(issuesBySprintAndType).forEach(story -> storyIds.add(story.getNumber()));
		return storyIds;
	}

	private void removeStoriesWithDefect(List<JiraIssue> totalJiraIssues,
			Map<String, Map<String, Integer>> projectWisePriority, Map<String, Set<String>> projectWiseRCA,
			List<JiraIssue> totalDefects, Map<String, Map<String, List<String>>> statusConfigsOfRejectedStoriesByProject) {

		Set<JiraIssue> defects = new HashSet<>();
		List<JiraIssue> defectListWoDrop = new ArrayList<>();
		KpiHelperService.getDefectsWithoutDrop(statusConfigsOfRejectedStoriesByProject, totalDefects, defectListWoDrop);
		defectListWoDrop.forEach(d -> totalJiraIssues.forEach(i -> {
			if (i.getProjectName().equalsIgnoreCase(d.getProjectName())) {
				defects.add(d);
			}
		}));

		final List<JiraIssue> remainingDefects = KpiHelperService.excludeDefectByPriorityCount(projectWisePriority,
				defects);

		List<JiraIssue> notFTPRDefects = new ArrayList<>();
		getNotFtprDefects(projectWiseRCA, defects, notFTPRDefects);

		Set<String> storyIdsWithDefect = new HashSet<>();
		remainingDefects.forEach(pi -> notFTPRDefects.forEach(ri -> {
			if (pi.getNumber().equalsIgnoreCase(ri.getNumber())) {
				storyIdsWithDefect.addAll(ri.getDefectStoryID());
			}
		}));
		totalJiraIssues.removeIf(issue -> storyIdsWithDefect.contains(issue.getNumber()));
	}

	private void setKPISpecificData(IssueKpiModalValue jiraIssueModalObject, Set<String> listOfStory,
			List<JiraIssue> allDefects, List<JiraIssue> ftprStory, JiraIssue jiraIssue) {
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
