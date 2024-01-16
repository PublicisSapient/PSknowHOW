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

package com.publicissapient.kpidashboard.apis.util;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Sets;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.model.ChangeFailureRateInfo;
import com.publicissapient.kpidashboard.apis.model.CodeBuildTimeInfo;
import com.publicissapient.kpidashboard.apis.model.CustomDateRange;
import com.publicissapient.kpidashboard.apis.model.DeploymentFrequencyInfo;
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.LeadTimeChangeData;
import com.publicissapient.kpidashboard.apis.model.MeanTimeRecoverData;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.CycleTimeValidationData;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.LeadTimeData;
import com.publicissapient.kpidashboard.common.model.application.ProjectVersion;
import com.publicissapient.kpidashboard.common.model.application.ResolutionTimeValidation;
import com.publicissapient.kpidashboard.common.model.jira.HappinessKpiData;
import com.publicissapient.kpidashboard.common.model.jira.IssueDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.ReleaseVersion;
import com.publicissapient.kpidashboard.common.model.jira.UserRatingData;
import com.publicissapient.kpidashboard.common.model.testexecution.KanbanTestExecution;
import com.publicissapient.kpidashboard.common.model.testexecution.TestExecution;
import com.publicissapient.kpidashboard.common.model.zephyr.TestCaseDetails;
import com.publicissapient.kpidashboard.common.util.DateUtil;

/**
 * The class contains mapping of kpi and Excel columns.
 *
 * @author pkum34
 */
public class KPIExcelUtility {

	public static final String TIME = "0d ";
	private static final String MONTH_YEAR_FORMAT = "MMM yyyy";
	private static final String DATE_YEAR_MONTH_FORMAT = "dd-MMM-yy";
	private static final String ITERATION_DATE_FORMAT = "yyyy-MM-dd";
	private static final DecimalFormat df2 = new DecimalFormat(".##");
	private static final String STATUS = "Status";
	private static final String WEEK = "Week";

	private KPIExcelUtility() {
	}

	/**
	 * This method populate the excel data for DIR KPI
	 *
	 * @param sprint
	 * 	sprint
	 * @param storyIds
	 * 	storyIds
	 * @param defects
	 * 	defects
	 * @param kpiExcelData
	 * 	kpiExcelData
	 * @param issueData
	 * 	issueData
	 */
	public static void populateDirExcelData(String sprint, List<String> storyIds, List<JiraIssue> defects,
			List<KPIExcelData> kpiExcelData, Map<String, JiraIssue> issueData) {
		if (CollectionUtils.isNotEmpty(storyIds)) {
			storyIds.forEach(story -> {
				Map<String, String> linkedDefects = new HashMap<>();
				defects.stream().filter(d -> d.getDefectStoryID().contains(story))
						.forEach(defect -> linkedDefects.putIfAbsent(defect.getNumber(), checkEmptyURL(defect)));
				KPIExcelData excelData = new KPIExcelData();
				excelData.setSprintName(sprint);
				excelData.setLinkedDefects(linkedDefects);
				if (MapUtils.isNotEmpty(issueData)) {
					JiraIssue jiraIssue = issueData.get(story);
					if (null != jiraIssue) {
						excelData.setIssueDesc(checkEmptyName(jiraIssue));
						Map<String, String> storyId = new HashMap<>();
						storyId.put(story, checkEmptyURL(jiraIssue));
						excelData.setStoryId(storyId);
					}
				}
				kpiExcelData.add(excelData);
			});
		}
	}

	public static void populateDefectDensityExcelData(String sprint, List<String> storyIds, List<JiraIssue> defects,
			List<KPIExcelData> kpiExcelData, Map<String, JiraIssue> issueData, FieldMapping fieldMapping) {
		if (CollectionUtils.isNotEmpty(storyIds)) {
			storyIds.forEach(story -> {
				Map<String, String> linkedDefects = new HashMap<>();
				defects.stream().filter(d -> d.getDefectStoryID().contains(story))
						.forEach(defect -> linkedDefects.putIfAbsent(defect.getNumber(), checkEmptyURL(defect)));
				KPIExcelData excelData = new KPIExcelData();
				excelData.setSprintName(sprint);
				excelData.setLinkedDefectsStory(linkedDefects);
				if (MapUtils.isNotEmpty(issueData)) {
					JiraIssue jiraIssue = issueData.get(story);
					if (null != jiraIssue) {
						excelData.setIssueDesc(checkEmptyName(jiraIssue));
						Map<String, String> storyId = new HashMap<>();
						storyId.put(story, checkEmptyURL(jiraIssue));
						excelData.setStoryId(storyId);
						if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
								&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
							excelData.setStoryPoint(jiraIssue.getStoryPoints().toString());
						} else if (null != jiraIssue.getAggregateTimeOriginalEstimateMinutes()) {
							Double originalEstimateInHours = Double.valueOf(jiraIssue.getAggregateTimeOriginalEstimateMinutes())
									/ 60;
							excelData.setStoryPoint(originalEstimateInHours / fieldMapping.getStoryPointToHourMapping()
									+ "/" + originalEstimateInHours + " hrs");
						}
					}
				}
				kpiExcelData.add(excelData);
			});
		}
	}

	public static void populateFTPRExcelData(String sprint, List<String> storyIds, List<JiraIssue> ftprStories,
			List<KPIExcelData> kpiExcelData, Map<String, JiraIssue> issueData) {
		List<String> collect = ftprStories.stream().map(JiraIssue::getNumber).collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(storyIds)) {
			storyIds.forEach(story -> {
				KPIExcelData excelData = new KPIExcelData();
				excelData.setSprintName(sprint);
				if (MapUtils.isNotEmpty(issueData)) {
					JiraIssue jiraIssue = issueData.get(story);
					if (null != jiraIssue) {
						excelData.setIssueDesc(checkEmptyName(jiraIssue));
						Map<String, String> storyId = new HashMap<>();
						storyId.put(story, checkEmptyURL(jiraIssue));
						excelData.setStoryId(storyId);
					}
				}
				excelData.setFirstTimePass(collect.contains(story) ? Constant.EXCEL_YES : Constant.EMPTY_STRING);
				kpiExcelData.add(excelData);
			});
		}
	}

	/**
	 * TO GET Constant.EXCEL_YES/"N" from complete list of defects if defect is
	 * present in conditional list then Constant.EXCEL_YES else "N" kpi specific
	 *
	 * @param sprint
	 * 	sprint
	 * @param totalBugList
	 * 	Map of total bug
	 * @param conditionDefects
	 * 	conditionDefects
	 * @param kpiExcelData
	 * 	kpiExcelData
	 * @param kpiId
	 * 	kpiId
	 */
	public static void populateDefectRelatedExcelData(String sprint, Map<String, JiraIssue> totalBugList,
			List<JiraIssue> conditionDefects, List<KPIExcelData> kpiExcelData, String kpiId) {

		if (MapUtils.isNotEmpty(totalBugList)) {
			List<String> conditionalList = conditionDefects.stream().map(JiraIssue::getNumber)
					.collect(Collectors.toList());
			totalBugList.forEach((defectId, jiraIssue) -> {
				String present = conditionalList.contains(defectId) ? Constant.EXCEL_YES : Constant.EMPTY_STRING;
				KPIExcelData excelData = new KPIExcelData();
				excelData.setSprintName(sprint);
				excelData.setIssueDesc(checkEmptyName(jiraIssue));
				Map<String, String> defectIdDetails = new HashMap<>();
				defectIdDetails.put(defectId, checkEmptyURL(jiraIssue));
				excelData.setDefectId(defectIdDetails);
				if (kpiId.equalsIgnoreCase(KPICode.DEFECT_REMOVAL_EFFICIENCY.getKpiId())) {
					excelData.setRemovedDefect(present);
				}
				if (kpiId.equalsIgnoreCase(KPICode.DEFECT_SEEPAGE_RATE.getKpiId())) {
					excelData.setEscapedDefect(present);
				}
				if (kpiId.equalsIgnoreCase(KPICode.DEFECT_REJECTION_RATE.getKpiId())) {
					excelData.setRejectedDefect(present);
				}

				kpiExcelData.add(excelData);
			});
		}
	}

	/**
	 * to get direct related values of a jira issue like priority/RCA from total
	 * list
	 *
	 * @param sprint
	 * @param jiraIssues
	 * @param kpiExcelData
	 * @param kpiId
	 */
	public static void populateDefectRelatedExcelData(String sprint, List<JiraIssue> jiraIssues,
			List<KPIExcelData> kpiExcelData, String kpiId) {
		if (CollectionUtils.isNotEmpty(jiraIssues)) {
			jiraIssues.stream().forEach(jiraIssue -> {
				KPIExcelData excelData = new KPIExcelData();
				excelData.setSprintName(sprint);
				excelData.setIssueDesc(checkEmptyName(jiraIssue));
				Map<String, String> defectIdDetails = new HashMap<>();
				defectIdDetails.put(jiraIssue.getNumber(), checkEmptyURL(jiraIssue));
				excelData.setDefectId(defectIdDetails);
				if (kpiId.equalsIgnoreCase(KPICode.DEFECT_COUNT_BY_PRIORITY.getKpiId())) {
					excelData.setPriority(jiraIssue.getPriority());
				}
				if (kpiId.equalsIgnoreCase(KPICode.DEFECT_COUNT_BY_RCA.getKpiId())) {
					excelData.setRootCause(jiraIssue.getRootCauseList());
				}

				kpiExcelData.add(excelData);
			});
		}
	}

	public static void populateDefectRCAandStatusRelatedExcelData(String sprint, List<JiraIssue> jiraIssues,
			List<JiraIssue> createDuringIteration, List<KPIExcelData> kpiExcelData, FieldMapping fieldMapping) {
		if (CollectionUtils.isNotEmpty(jiraIssues)) {
			jiraIssues.stream().forEach(jiraIssue -> {
				KPIExcelData excelData = new KPIExcelData();
				String present = createDuringIteration.contains(jiraIssue) ? Constant.EXCEL_YES : Constant.EMPTY_STRING;
				excelData.setSprintName(sprint);
				Map<String, String> defectIdDetails = new HashMap<>();
				defectIdDetails.put(jiraIssue.getNumber(), checkEmptyURL(jiraIssue));
				excelData.setDefectId(defectIdDetails);
				excelData.setIssueDesc(checkEmptyName(jiraIssue));
				excelData.setIssueStatus(jiraIssue.getStatus());
				excelData.setIssueType(jiraIssue.getTypeName());
				populateAssignee(jiraIssue, excelData);
				if (null != jiraIssue.getStoryPoints() && StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
						&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
					excelData.setStoryPoint(String.valueOf(jiraIssue.getStoryPoints()));
				}
				if (null != jiraIssue.getAggregateTimeOriginalEstimateMinutes()
						&& StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
						&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.ACTUAL_ESTIMATION)) {
					excelData.setStoryPoint((jiraIssue.getAggregateTimeOriginalEstimateMinutes() / 60 + " hrs"));
				}
				excelData.setRootCause(jiraIssue.getRootCauseList());
				excelData.setPriority(jiraIssue.getPriority());
				excelData.setCreatedDuringIteration(present);
				kpiExcelData.add(excelData);
			});
		}
	}

	/**
	 * TO GET Constant.EXCEL_YES/"N" from complete list of defects if defect is
	 * present in conditional list then Constant.EXCEL_YES else
	 * Constant.EMPTY_STRING kpi specific
	 *
	 * @param sprint
	 * @param totalStoriesMap
	 * @param closedConditionStories
	 * @param createdConditionStories
	 * @param kpiExcelData
	 */
	public static void populateCreatedVsResolvedExcelData(String sprint, Map<String, JiraIssue> totalStoriesMap,
			List<JiraIssue> closedConditionStories, List<JiraIssue> createdConditionStories,
			List<KPIExcelData> kpiExcelData) {
		if (MapUtils.isNotEmpty(totalStoriesMap)) {
			List<String> closedConditionalList = closedConditionStories.stream().map(JiraIssue::getNumber)
					.collect(Collectors.toList());
			List<String> createdConditionalList = createdConditionStories.stream().map(JiraIssue::getNumber)
					.collect(Collectors.toList());
			totalStoriesMap.forEach((storyId, jiraIssue) -> {
				String present = closedConditionalList.contains(storyId) ? Constant.EXCEL_YES : Constant.EMPTY_STRING;
				String createdAfterSprint = createdConditionalList.contains(storyId) ? Constant.EXCEL_YES
						: Constant.EMPTY_STRING;
				KPIExcelData excelData = new KPIExcelData();
				excelData.setSprintName(sprint);
				excelData.setIssueDesc(checkEmptyName(jiraIssue));
				Map<String, String> storyDetails = new HashMap<>();
				storyDetails.put(storyId, checkEmptyURL(jiraIssue));
				excelData.setCreatedDefectId(storyDetails);
				excelData.setResolvedTickets(present);
				excelData.setDefectAddedAfterSprintStart(createdAfterSprint);

				kpiExcelData.add(excelData);
			});
		}
	}

	public static void populateRegressionAutomationExcelData(String sprintProject,
			Map<String, TestCaseDetails> totalStoriesMap, List<TestCaseDetails> conditionStories,
			List<KPIExcelData> kpiExcelData, String kpiId, String date) {
		if (MapUtils.isNotEmpty(totalStoriesMap)) {
			List<String> conditionalList = conditionStories.stream().map(TestCaseDetails::getNumber)
					.collect(Collectors.toList());
			totalStoriesMap.forEach((storyId, jiraIssue) -> {
				String present = conditionalList.contains(storyId) ? Constant.EXCEL_YES : Constant.EMPTY_STRING;
				KPIExcelData excelData = new KPIExcelData();
				if (kpiId.equalsIgnoreCase(KPICode.REGRESSION_AUTOMATION_COVERAGE.getKpiId())) {
					excelData.setSprintName(sprintProject);
				} else {
					excelData.setProject(sprintProject);
					excelData.setDayWeekMonth(date);
				}
				excelData.setTestCaseId(storyId);
				excelData.setAutomated(present);
				kpiExcelData.add(excelData);
			});
		}
	}

	public static void populateSonarKpisExcelData(String projectName, List<String> jobList,
			List<String> kpiSpecificDataList, List<String> versionDate, List<KPIExcelData> kpiExcelData, String kpiId) {
		if (CollectionUtils.isNotEmpty(jobList)) {
			for (int i = 0; i < jobList.size(); i++) {
				KPIExcelData excelData = new KPIExcelData();
				excelData.setProject(projectName);
				excelData.setJobName(jobList.get(i));

				if (kpiId.equalsIgnoreCase(KPICode.UNIT_TEST_COVERAGE.getKpiId())
						|| kpiId.equalsIgnoreCase(KPICode.UNIT_TEST_COVERAGE_KANBAN.getKpiId())) {
					excelData.setUnitCoverage(kpiSpecificDataList.get(i));
				} else if (kpiId.equalsIgnoreCase(KPICode.SONAR_TECH_DEBT.getKpiId())
						|| kpiId.equalsIgnoreCase(KPICode.SONAR_TECH_DEBT_KANBAN.getKpiId())) {
					excelData.setTechDebt(kpiSpecificDataList.get(i));
				} else if (kpiId.equalsIgnoreCase(KPICode.SONAR_VIOLATIONS.getKpiId())
						|| kpiId.equalsIgnoreCase(KPICode.SONAR_VIOLATIONS_KANBAN.getKpiId())) {
					excelData.setSonarViolation(kpiSpecificDataList.get(i));
				} else if (kpiId.equalsIgnoreCase(KPICode.SONAR_CODE_QUALITY.getKpiId())) {
					excelData.setCodeQuality(kpiSpecificDataList.get(i));
				}
				setSonarKpiWeekDayMonthColumn(versionDate.get(i), excelData, kpiId);
				kpiExcelData.add(excelData);
			}
		}
	}

	private static void setSonarKpiWeekDayMonthColumn(String versionDate, KPIExcelData excelData, String kpiId) {
		if (kpiId.equalsIgnoreCase(KPICode.UNIT_TEST_COVERAGE.getKpiId())
				|| kpiId.equalsIgnoreCase(KPICode.SONAR_TECH_DEBT.getKpiId())
				|| kpiId.equalsIgnoreCase(KPICode.SONAR_VIOLATIONS.getKpiId())) {
			excelData.setWeeks(versionDate);
		} else if (kpiId.equalsIgnoreCase(KPICode.SONAR_CODE_QUALITY.getKpiId())) {
			excelData.setMonth(versionDate);
		} else {
			excelData.setDayWeekMonth(versionDate);
		}
	}

	public static void populateInSprintAutomationExcelData(String sprint, List<TestCaseDetails> allTestList,
			List<TestCaseDetails> automatedList, Set<JiraIssue> linkedStories, List<KPIExcelData> kpiExcelData) {

		if (CollectionUtils.isNotEmpty(allTestList)) {
			List<String> conditionalList = automatedList.stream().map(TestCaseDetails::getNumber)
					.collect(Collectors.toList());
			allTestList.forEach(testIssue -> {
				String present = conditionalList.contains(testIssue.getNumber()) ? Constant.EXCEL_YES
						: Constant.EMPTY_STRING;
				Map<String, String> linkedStoriesMap = new HashMap<>();
				linkedStories.stream().filter(story -> testIssue.getDefectStoryID().contains(story.getNumber()))
						.forEach(story -> linkedStoriesMap.putIfAbsent(story.getNumber(), checkEmptyURL(story)));

				KPIExcelData excelData = new KPIExcelData();
				excelData.setSprintName(sprint);
				excelData.setTestCaseId(testIssue.getNumber());
				excelData.setLinkedStory(linkedStoriesMap);
				excelData.setAutomated(present);
				kpiExcelData.add(excelData);
			});
		}

	}

	private static String checkEmptyName(Object object) {
		String description = "";
		if (object instanceof JiraIssue) {
			JiraIssue jiraIssue = (JiraIssue) object;
			description = StringUtils.isEmpty(jiraIssue.getName()) ? Constant.EMPTY_STRING : jiraIssue.getName();
		}
		if (object instanceof KanbanJiraIssue) {
			KanbanJiraIssue jiraIssue = (KanbanJiraIssue) object;
			description = StringUtils.isEmpty(jiraIssue.getName()) ? Constant.EMPTY_STRING : jiraIssue.getName();
		}
		if (object instanceof KanbanIssueCustomHistory) {
			KanbanIssueCustomHistory jiraIssue = (KanbanIssueCustomHistory) object;
			description = StringUtils.isEmpty(jiraIssue.getDescription()) ? Constant.EMPTY_STRING
					: jiraIssue.getDescription();
		}
		if (object instanceof JiraIssueCustomHistory) {
			JiraIssueCustomHistory jiraIssue = (JiraIssueCustomHistory) object;
			description = StringUtils.isEmpty(jiraIssue.getDescription()) ? Constant.EMPTY_STRING
					: jiraIssue.getDescription();
		}
		if (object instanceof ResolutionTimeValidation) {
			ResolutionTimeValidation resolutionTimeValidation = (ResolutionTimeValidation) object;
			description = StringUtils.isEmpty(resolutionTimeValidation.getIssueDescription()) ? Constant.EMPTY_STRING
					: resolutionTimeValidation.getIssueDescription();
		}
		if (object instanceof IssueDetails) {
			IssueDetails issueDetails = (IssueDetails) object;
			description = StringUtils.isEmpty(issueDetails.getDesc()) ? Constant.EMPTY_STRING : issueDetails.getDesc();
		}

		return description;
	}

	private static String checkEmptyURL(Object object) {
		String url = "";
		if (object instanceof JiraIssue) {
			JiraIssue jiraIssue = (JiraIssue) object;
			url = StringUtils.isEmpty(jiraIssue.getUrl()) ? Constant.EMPTY_STRING : jiraIssue.getUrl();
		}
		if (object instanceof KanbanJiraIssue) {
			KanbanJiraIssue jiraIssue = (KanbanJiraIssue) object;
			url = StringUtils.isEmpty(jiraIssue.getUrl()) ? Constant.EMPTY_STRING : jiraIssue.getUrl();
		}
		if (object instanceof KanbanIssueCustomHistory) {
			KanbanIssueCustomHistory jiraIssue = (KanbanIssueCustomHistory) object;
			url = StringUtils.isEmpty(jiraIssue.getUrl()) ? Constant.EMPTY_STRING : jiraIssue.getUrl();
		}
		if (object instanceof JiraIssueCustomHistory) {
			JiraIssueCustomHistory jiraIssue = (JiraIssueCustomHistory) object;
			url = StringUtils.isEmpty(jiraIssue.getUrl()) ? Constant.EMPTY_STRING : jiraIssue.getUrl();
		}
		if (object instanceof ResolutionTimeValidation) {
			ResolutionTimeValidation resolutionTimeValidation = (ResolutionTimeValidation) object;
			url = StringUtils.isEmpty(resolutionTimeValidation.getUrl()) ? Constant.EMPTY_STRING
					: resolutionTimeValidation.getUrl();
		}
		if (object instanceof IssueDetails) {
			IssueDetails issueDetails = (IssueDetails) object;
			url = StringUtils.isEmpty(issueDetails.getUrl()) ? Constant.EMPTY_STRING : issueDetails.getUrl();
		}
		if (object instanceof LeadTimeChangeData) {
			LeadTimeChangeData leadTimeChangeData = (LeadTimeChangeData) object;
			url = StringUtils.isEmpty(leadTimeChangeData.getUrl()) ? Constant.EMPTY_STRING : leadTimeChangeData.getUrl();
		}
		return url;

	}

	public static void populateChangeFailureRateExcelData(String projectName,
			ChangeFailureRateInfo changeFailureRateInfo, List<KPIExcelData> kpiExcelData) {
		List<String> buildJobNameList = changeFailureRateInfo.getBuildJobNameList();
		if (CollectionUtils.isNotEmpty(buildJobNameList)) {
			for (int i = 0; i < changeFailureRateInfo.getBuildJobNameList().size(); i++) {
				KPIExcelData excelData = new KPIExcelData();
				excelData.setProject(projectName);
				excelData.setJobName(buildJobNameList.get(i));
				excelData.setWeeks(changeFailureRateInfo.getDateList().get(i));
				excelData.setBuildCount(changeFailureRateInfo.getTotalBuildCountList().get(i).toString());
				excelData.setBuildFailureCount(changeFailureRateInfo.getTotalBuildFailureCountList().get(i).toString());
				excelData.setBuildFailurePercentage(
						changeFailureRateInfo.getBuildFailurePercentageList().get(i).toString());
				kpiExcelData.add(excelData);
			}
		}

	}

	public static void populateTestExcecutionExcelData(String sprintProjectName, TestExecution testDetail,
			KanbanTestExecution kanbanTestExecution, double executionPercentage, double passPercentage,
			List<KPIExcelData> kpiExcelData) {

		if (testDetail != null) {
			KPIExcelData excelData = new KPIExcelData();
			excelData.setSprintName(sprintProjectName);
			excelData.setTotalTest(testDetail.getTotalTestCases().toString());
			excelData.setExecutedTest(testDetail.getExecutedTestCase().toString());
			excelData.setExecutionPercentage(String.valueOf(executionPercentage));
			excelData.setPassedTest(testDetail.getPassedTestCase().toString());
			excelData.setPassedPercentage(String.valueOf(passPercentage));
			kpiExcelData.add(excelData);
		}
		if (kanbanTestExecution != null) {
			KPIExcelData excelData = new KPIExcelData();
			excelData.setProject(sprintProjectName);
			excelData.setTotalTest(kanbanTestExecution.getTotalTestCases().toString());
			excelData.setExecutedTest(kanbanTestExecution.getExecutedTestCase().toString());
			excelData.setExecutionPercentage(String.valueOf(executionPercentage));
			excelData.setPassedTest(kanbanTestExecution.getPassedTestCase().toString());
			excelData.setPassedPercentage(String.valueOf(passPercentage));
			excelData.setExecutionDate(kanbanTestExecution.getExecutionDate());
			kpiExcelData.add(excelData);
		}
	}

	public static void populateSprintVelocity(String sprint, Map<String, JiraIssue> totalStoriesMap,
			List<KPIExcelData> kpiExcelData, FieldMapping fieldMapping) {

		if (MapUtils.isNotEmpty(totalStoriesMap)) {
			totalStoriesMap.forEach((storyId, jiraIssue) -> {
				KPIExcelData excelData = new KPIExcelData();
				excelData.setSprintName(sprint);
				Map<String, String> storyDetails = new HashMap<>();
				storyDetails.put(storyId, checkEmptyURL(jiraIssue));
				excelData.setStoryId(storyDetails);
				excelData.setIssueDesc(checkEmptyName(jiraIssue));
				if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
						&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
					Double roundingOff = roundingOff(Optional.ofNullable(jiraIssue.getStoryPoints()).orElse(0.0));
					excelData.setStoryPoint(roundingOff.toString());
				} else if (null != jiraIssue.getAggregateTimeOriginalEstimateMinutes()) {
					Double totalOriginalEstimate = Double.valueOf(jiraIssue.getAggregateTimeOriginalEstimateMinutes()) / 60;
					excelData.setStoryPoint(roundingOff(totalOriginalEstimate / fieldMapping.getStoryPointToHourMapping()) + "/"
							+ roundingOff(totalOriginalEstimate) + " hrs");
				}
				kpiExcelData.add(excelData);
			});
		}
	}

	public static void populateSprintPredictability(String sprint, Set<IssueDetails> issueDetailsSet,
			List<KPIExcelData> kpiExcelData, FieldMapping fieldMapping) {
		if (CollectionUtils.isNotEmpty(issueDetailsSet)) {
			for (IssueDetails issueDetails : issueDetailsSet) {
				KPIExcelData excelData = new KPIExcelData();
				excelData.setSprintName(sprint);
				Map<String, String> storyDetails = new HashMap<>();
				storyDetails.put(issueDetails.getSprintIssue().getNumber(), checkEmptyURL(issueDetails));
				excelData.setStoryId(storyDetails);
				excelData.setIssueDesc(checkEmptyName(issueDetails));
				if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
						&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
					excelData.setStoryPoint(
							Optional.ofNullable(issueDetails.getSprintIssue().getStoryPoints()).orElse(0.0).toString());
				} else if (null != issueDetails.getSprintIssue().getOriginalEstimate()) {
					Double totalOriginalEstimate = issueDetails.getSprintIssue().getOriginalEstimate() / 60;
					Double totalOriginalEstimateInHours = totalOriginalEstimate / 60;
					excelData.setStoryPoint(totalOriginalEstimateInHours / fieldMapping.getStoryPointToHourMapping()
							+ "/" + totalOriginalEstimate / 60 + " hrs");
				}
				kpiExcelData.add(excelData);
			}
		}
	}

	public static void populateSprintCapacity(String sprint, List<JiraIssue> totalStoriesList,
			List<KPIExcelData> kpiExcelData) {

		if (CollectionUtils.isNotEmpty(totalStoriesList)) {
			totalStoriesList.stream().forEach(issue -> {

				KPIExcelData excelData = new KPIExcelData();
				excelData.setSprintName(sprint);
				Map<String, String> storyDetails = new HashMap<>();
				storyDetails.put(issue.getNumber(), checkEmptyURL(issue));
				excelData.setStoryId(storyDetails);
				excelData.setIssueDesc(checkEmptyName(issue));
				String daysLogged = "0.0";
				String daysEstimated = "0.0";
				if (issue.getTimeSpentInMinutes() != null && issue.getTimeSpentInMinutes() > 0) {
					daysLogged = df2.format(Double.valueOf(issue.getTimeSpentInMinutes()) / 60);
				}
				excelData.setTotalTimeSpent(daysLogged);

				if (issue.getAggregateTimeOriginalEstimateMinutes() != null && issue.getAggregateTimeOriginalEstimateMinutes() > 0) {
					daysEstimated = df2.format(Double.valueOf(issue.getAggregateTimeOriginalEstimateMinutes()) / 60);
				}
				excelData.setOriginalTimeEstimate(daysEstimated);
				kpiExcelData.add(excelData);

			});
		}
	}

	public static void populateAverageResolutionTime(String sprintName,
			List<ResolutionTimeValidation> sprintWiseResolution, List<KPIExcelData> kpiExcelData) {

		if (CollectionUtils.isNotEmpty(sprintWiseResolution)) {
			sprintWiseResolution.stream().forEach(resolutionTimeValidation -> {
				KPIExcelData excelData = new KPIExcelData();
				excelData.setSprintName(sprintName);
				Map<String, String> storyDetails = new HashMap<>();
				storyDetails.put(resolutionTimeValidation.getIssueNumber(), checkEmptyURL(resolutionTimeValidation));
				excelData.setStoryId(storyDetails);
				excelData.setIssueDesc(checkEmptyName(resolutionTimeValidation));
				excelData.setIssueType(resolutionTimeValidation.getIssueType());
				excelData.setResolutionTime(resolutionTimeValidation.getResolutionTime().toString());
				kpiExcelData.add(excelData);

			});
		}
	}

	public static void populateSprintCountExcelData(String sprint, Map<String, JiraIssue> totalStoriesMap,
			List<KPIExcelData> kpiExcelData) {

		if (MapUtils.isNotEmpty(totalStoriesMap)) {
			totalStoriesMap.forEach((storyId, jiraIssue) -> {
				KPIExcelData excelData = new KPIExcelData();
				excelData.setSprintName(sprint);
				Map<String, String> storyDetails = new HashMap<>();
				storyDetails.put(storyId, checkEmptyURL(jiraIssue));
				excelData.setStoryId(storyDetails);
				excelData.setIssueDesc(checkEmptyName(jiraIssue));

				kpiExcelData.add(excelData);
			});
		}
	}

	public static void populateLeadTime(List<CycleTimeValidationData> cycleTimeList, List<KPIExcelData> excelDataList) {
		for (CycleTimeValidationData leadTimeData : cycleTimeList) {
			KPIExcelData excelData = new KPIExcelData();
			Map<String, String> storyId = new HashMap<>();
			storyId.put(leadTimeData.getIssueNumber(), leadTimeData.getUrl());
			excelData.setIssueID(storyId);
			excelData.setIssueDesc(leadTimeData.getIssueDesc());
			excelData.setIssueType(leadTimeData.getIssueType());
			excelData.setCreatedDate(DateUtil.dateTimeConverter(leadTimeData.getIntakeDate().toString().split("T")[0],
					DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT));
			excelData.setCloseDate(DateUtil.dateTimeConverter(leadTimeData.getLiveDate().toString().split("T")[0],
					DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT));
			excelData.setLeadTime(CommonUtils.convertIntoDays(Math.toIntExact(leadTimeData.getLeadTime())));
			excelDataList.add(excelData);
		}
	}

	/**
	 *
	 * @param cycleTimeList
	 * 			cycleTimeList
	 * @param excelDataList
	 * 			excelDataList
	 */
	public static void populateCycleTime(List<CycleTimeValidationData> cycleTimeList, List<KPIExcelData> excelDataList) {
		for (CycleTimeValidationData leadTimeData : cycleTimeList) {
			KPIExcelData excelData = new KPIExcelData();
			Map<String, String> storyId = new HashMap<>();
			storyId.put(leadTimeData.getIssueNumber(), leadTimeData.getUrl());
			excelData.setIssueID(storyId);
			excelData.setIssueDesc(leadTimeData.getIssueDesc());
			excelData.setIssueType(leadTimeData.getIssueType());
			if(ObjectUtils.isNotEmpty(leadTimeData.getIntakeTime()))
				excelData.setIntakeToDOR(CommonUtils.convertIntoDays(Math.toIntExact(leadTimeData.getIntakeTime())));
			if(ObjectUtils.isNotEmpty(leadTimeData.getDorTime()))
				excelData.setDorToDod(CommonUtils.convertIntoDays(Math.toIntExact(leadTimeData.getDorTime())));
			if(ObjectUtils.isNotEmpty(leadTimeData.getDodTime()))
				excelData.setDodToLive(CommonUtils.convertIntoDays(Math.toIntExact(leadTimeData.getDodTime())));
			excelDataList.add(excelData);
		}
	}

	/**
	 * TO GET Constant.EXCEL_YES/"N" from complete list of defects if defect is
	 * present in conditional list then Constant.EXCEL_YES else
	 * Constant.EMPTY_STRING kpi specific
	 *
	 * @param sprint
	 * @param totalStoriesMap
	 * @param initialIssueNumber
	 * @param kpiExcelData
	 */

	public static void populateCommittmentReliability(String sprint, Map<String, JiraIssue> totalStoriesMap,
			Set<JiraIssue> initialIssueNumber, List<KPIExcelData> kpiExcelData, FieldMapping fieldMapping) {
		if (MapUtils.isNotEmpty(totalStoriesMap)) {

			totalStoriesMap.forEach((storyId, jiraIssue) -> {
				KPIExcelData excelData = new KPIExcelData();
				excelData.setSprintName(sprint);
				excelData.setIssueDesc(checkEmptyName(jiraIssue));
				Map<String, String> storyDetails = new HashMap<>();
				storyDetails.put(storyId, checkEmptyURL(jiraIssue));
				excelData.setStoryId(storyDetails);
				excelData.setIssueType(jiraIssue.getTypeName());
				excelData.setIssueStatus(jiraIssue.getStatus());
				if (initialIssueNumber.contains(jiraIssue)) {
					excelData.setInitialCommited("Y");
				}
				if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
						&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
					excelData.setStoryPoint(Optional.ofNullable(jiraIssue.getStoryPoints()).orElse(0.0).toString());
				} else if (null != jiraIssue.getAggregateTimeOriginalEstimateMinutes()) {
					excelData.setStoryPoint(jiraIssue.getAggregateTimeOriginalEstimateMinutes() / 60 + " hrs");
				}

				kpiExcelData.add(excelData);

			});
		}
	}

	public static void populateCODExcelData(String projectName, List<JiraIssue> epicList,
			List<KPIExcelData> kpiExcelData) {
		if (CollectionUtils.isNotEmpty(epicList)) {
			epicList.forEach(epic -> {
				if (null != epic) {
					Map<String, String> epicLink = new HashMap<>();
					epicLink.put(epic.getNumber(), checkEmptyURL(epic));
					KPIExcelData excelData = new KPIExcelData();
					excelData.setProjectName(projectName);
					excelData.setEpicID(epicLink);
					excelData.setEpicName(checkEmptyName(epic));
					excelData.setCostOfDelay(epic.getCostOfDelay());
					String month = Constant.EMPTY_STRING;
					String epicEndDate = Constant.EMPTY_STRING;
					if (epic.getChangeDate() != null) {
						DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern(DateUtil.TIME_FORMAT)
								.optionalStart().appendPattern(".")
								.appendFraction(ChronoField.MICRO_OF_SECOND, 1, 9, false).optionalEnd().toFormatter();
						LocalDateTime dateTime = LocalDateTime.parse(epic.getChangeDate(), formatter);
						month = dateTime.format(DateTimeFormatter.ofPattern(MONTH_YEAR_FORMAT));
						epicEndDate = dateTime.format(DateTimeFormatter.ofPattern(DATE_YEAR_MONTH_FORMAT));
					}
					excelData.setMonth(month);
					excelData.setEpicEndDate(epicEndDate);
					kpiExcelData.add(excelData);
				}
			});
		}
	}

	public static void populatePIPredictabilityExcelData(String projectName, List<JiraIssue> epicList,
			List<KPIExcelData> kpiExcelData) {
		if (CollectionUtils.isNotEmpty(epicList)) {
			epicList.forEach(epic -> {
				if (null != epic) {
					Map<String, String> epicLink = new HashMap<>();
					epicLink.put(epic.getNumber(), checkEmptyURL(epic));
					KPIExcelData excelData = new KPIExcelData();
					excelData.setProjectName(projectName);
					excelData.setEpicID(epicLink);
					excelData.setEpicName(checkEmptyName(epic));
					excelData.setStatus(epic.getStatus());
					excelData.setPiName(epic.getReleaseVersions().get(0).getReleaseName());
					excelData.setPlannedValue(String.valueOf(epic.getEpicPlannedValue()));
					excelData.setAchievedValue(String.valueOf(epic.getEpicAchievedValue()));
					kpiExcelData.add(excelData);
				}
			});
		}
	}

	public static void populateKanbanCODExcelData(String projectName, List<KanbanJiraIssue> epicList,
			List<KPIExcelData> kpiExcelData) {
		if (CollectionUtils.isNotEmpty(epicList)) {
			epicList.forEach(epic -> {
				if (!epic.getProjectName().isEmpty()) {
					Map<String, String> epicLink = new HashMap<>();
					epicLink.put(epic.getNumber(), checkEmptyURL(epic));
					KPIExcelData excelData = new KPIExcelData();
					excelData.setProjectName(projectName);
					excelData.setEpicID(epicLink);
					excelData.setEpicName(checkEmptyName(epic));
					excelData.setCostOfDelay(epic.getCostOfDelay());
					String month = Constant.EMPTY_STRING;
					String epicEndDate = Constant.EMPTY_STRING;
					if (epic.getChangeDate() != null) {
						DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern(DateUtil.TIME_FORMAT)
								.optionalStart().appendPattern(".")
								.appendFraction(ChronoField.MICRO_OF_SECOND, 1, 9, false).optionalEnd().toFormatter();
						LocalDateTime dateTime = LocalDateTime.parse(epic.getChangeDate(), formatter);
						month = dateTime.format(DateTimeFormatter.ofPattern(MONTH_YEAR_FORMAT));
						epicEndDate = dateTime.format(DateTimeFormatter.ofPattern(DATE_YEAR_MONTH_FORMAT));
					}
					excelData.setMonth(month);
					excelData.setEpicEndDate(epicEndDate);
					kpiExcelData.add(excelData);
				}
			});
		}
	}

	public static void populateReleaseFreqExcelData(List<ProjectVersion> projectVersionList, String projectName,
			List<KPIExcelData> kpiExcelData) {
		if (CollectionUtils.isNotEmpty(projectVersionList)) {
			projectVersionList.forEach(pv -> {
				KPIExcelData excelData = new KPIExcelData();
				excelData.setProjectName(projectName);
				excelData.setReleaseName(pv.getName());
				excelData.setReleaseDesc(pv.getDescription());
				excelData.setReleaseEndDate(pv.getReleaseDate().toString(DATE_YEAR_MONTH_FORMAT));
				excelData.setMonth(pv.getReleaseDate().toString(MONTH_YEAR_FORMAT));
				kpiExcelData.add(excelData);

			});
		}

	}

	public static void populateDeploymentFrequencyExcelData(String projectName,
			DeploymentFrequencyInfo deploymentFrequencyInfo, List<KPIExcelData> kpiExcelData) {
		if (deploymentFrequencyInfo != null) {
			for (int i = 0; i < deploymentFrequencyInfo.getJobNameList().size(); i++) {
				KPIExcelData excelData = new KPIExcelData();
				excelData.setProjectName(projectName);
				excelData.setDate(deploymentFrequencyInfo.getDeploymentDateList().get(i));
				excelData.setJobName(deploymentFrequencyInfo.getJobNameList().get(i));
				excelData.setWeeks(deploymentFrequencyInfo.getMonthList().get(i));
				excelData.setDeploymentEnvironment(deploymentFrequencyInfo.getEnvironmentList().get(i));
				kpiExcelData.add(excelData);

			}
		}

	}

	public static void populateDefectWithoutIssueLinkExcelData(List<JiraIssue> defectWithoutStory,
			List<KPIExcelData> kpiExcelData, String sprintName) {
		if (CollectionUtils.isNotEmpty(defectWithoutStory)) {
			defectWithoutStory.forEach(defect -> {
				if (null != defect) {
					KPIExcelData excelData = new KPIExcelData();
					Map<String, String> defectLink = new HashMap<>();
					defectLink.put(defect.getNumber(), checkEmptyURL(defect));
					excelData.setProjectName(sprintName);
					excelData.setDefectWithoutStoryLink(defectLink);
					excelData.setIssueDesc(checkEmptyName(defect));
					excelData.setPriority(defect.getPriority());
					kpiExcelData.add(excelData);
				}
			});
		}
	}

	public static void populateTestWithoutStoryExcelData(String projectName, Map<String, TestCaseDetails> totalTestMap,
			List<TestCaseDetails> testWithoutStory, List<KPIExcelData> kpiExcelData) {
		if (MapUtils.isNotEmpty(totalTestMap)) {
			List<String> testWithoutStoryIdList = testWithoutStory.stream().map(TestCaseDetails::getNumber)
					.collect(Collectors.toList());
			totalTestMap.forEach((testId, testCaseDetails) -> {
				String isDefectPresent = testWithoutStoryIdList.contains(testId) ? Constant.EMPTY_STRING
						: Constant.EXCEL_YES;
				KPIExcelData excelData = new KPIExcelData();
				excelData.setProjectName(projectName);
				excelData.setTestCaseId(testId);
				excelData.setIsTestLinkedToStory(isDefectPresent);
				kpiExcelData.add(excelData);
			});
		}
	}

	public static void populateIssueCountExcelData(String sprint, List<KPIExcelData> kpiExcelData,
			List<JiraIssue> allJiraIssueList, List<String> totalPresentJiraIssue) {

		if (CollectionUtils.isNotEmpty(allJiraIssueList)) {
			allJiraIssueList.stream().filter(issue -> totalPresentJiraIssue.contains(issue.getNumber()))
					.forEach(sprintIssue -> {
						KPIExcelData excelData = new KPIExcelData();
						excelData.setSprintName(sprint);
						Map<String, String> storyDetails = new HashMap<>();
						storyDetails.put(sprintIssue.getNumber(), checkEmptyURL(sprintIssue));
						excelData.setStoryId(storyDetails);
						excelData.setIssueDesc(checkEmptyName(sprintIssue));
						kpiExcelData.add(excelData);
					});
		}
	}

	public static void populateCodeBuildTime(List<KPIExcelData> kpiExcelData, String projectName,
			CodeBuildTimeInfo codeBuildTimeInfo) {

		for (int i = 0; i < codeBuildTimeInfo.getBuildJobList().size(); i++) {
			KPIExcelData excelData = new KPIExcelData();
			excelData.setProjectName(projectName);
			excelData.setJobName(codeBuildTimeInfo.getBuildJobList().get(i));
			Map<String, String> buildUrl = new HashMap<>();
			buildUrl.put(codeBuildTimeInfo.getBuildUrlList().get(i), codeBuildTimeInfo.getBuildUrlList().get(i));
			excelData.setBuildUrl(buildUrl);
			excelData.setStartTime(codeBuildTimeInfo.getBuildStartTimeList().get(i));
			excelData.setEndTime(codeBuildTimeInfo.getBuildEndTimeList().get(i));
			excelData.setWeeks(codeBuildTimeInfo.getWeeksList().get(i));
			excelData.setBuildStatus(codeBuildTimeInfo.getBuildStatusList().get(i));
			excelData.setDuration(codeBuildTimeInfo.getDurationList().get(i));
			kpiExcelData.add(excelData);

		}
	}

	public static void populateMeanTimeMergeExcelData(String projectName, List<Map<String, Double>> repoWiseMRList,
			List<String> repoList, List<String> branchList, List<KPIExcelData> kpiExcelData) {

		if (CollectionUtils.isNotEmpty(repoWiseMRList)) {
			for (int i = 0; i < repoWiseMRList.size(); i++) {
				Map<String, Double> repoWiseMap = repoWiseMRList.get(i);
				for (Map.Entry<String, Double> m : repoWiseMap.entrySet()) {
					KPIExcelData excelData = new KPIExcelData();
					excelData.setProject(projectName);
					Map<String, String> repoUrl = new HashMap<>();
					repoUrl.put(repoList.get(i), repoList.get(i));
					excelData.setRepositoryURL(repoUrl);
					excelData.setBranch(branchList.get(i));
					excelData.setDaysWeeks(m.getKey());
					excelData.setMeanTimetoMerge(m.getValue().toString());
					kpiExcelData.add(excelData);
				}

			}
		}

	}

	public static void populatePickupTimeExcelData(String projectName, List<Map<String, Double>> repoWiseMRList,
			List<String> repoList, List<String> branchList, List<KPIExcelData> kpiExcelData) {

		if (CollectionUtils.isNotEmpty(repoWiseMRList)) {
			for (int i = 0; i < repoWiseMRList.size(); i++) {
				Map<String, Double> repoWiseMap = repoWiseMRList.get(i);
				for (Map.Entry<String, Double> m : repoWiseMap.entrySet()) {
					KPIExcelData excelData = new KPIExcelData();
					excelData.setProject(projectName);
					Map<String, String> repoUrl = new HashMap<>();
					repoUrl.put(repoList.get(i), repoList.get(i));
					excelData.setRepositoryURL(repoUrl);
					excelData.setBranch(branchList.get(i));
					excelData.setDaysWeeks(m.getKey());
					excelData.setPickupTime(m.getValue().toString());
					kpiExcelData.add(excelData);
				}

			}
		}

	}

	public static void populatePRSizeExcelData(String projectName, List<Map<String, Long>> repoWiseMRList,
			List<String> repoList, List<String> branchList, List<KPIExcelData> kpiExcelData) {

		if (CollectionUtils.isNotEmpty(repoWiseMRList)) {
			for (int i = 0; i < repoWiseMRList.size(); i++) {
				Map<String, Long> repoWiseMap = repoWiseMRList.get(i);
				for (Map.Entry<String, Long> m : repoWiseMap.entrySet()) {
					KPIExcelData excelData = new KPIExcelData();
					excelData.setProject(projectName);
					Map<String, String> repoUrl = new HashMap<>();
					repoUrl.put(repoList.get(i), repoList.get(i));
					excelData.setRepositoryURL(repoUrl);
					excelData.setBranch(branchList.get(i));
					excelData.setDaysWeeks(m.getKey());
					excelData.setPrSize(m.getValue().toString());
					kpiExcelData.add(excelData);
				}

			}
		}

	}

	public static void populateCodeCommit(String projectName, List<Map<String, Long>> repoWiseCommitList,
			List<String> repoList, List<String> branchList, List<KPIExcelData> kpiExcelData,
			List<Map<String, Long>> repoWiseMergeRequestList) {

		if (CollectionUtils.isNotEmpty(repoWiseCommitList) && CollectionUtils.isNotEmpty(repoWiseMergeRequestList)) {
			for (int i = 0; i < repoWiseCommitList.size(); i++) {
				Map<String, Long> repoWiseCommitMap = repoWiseCommitList.get(i);
				Map<String, Long> repoWiseMergeMap = repoWiseMergeRequestList.get(i);

				for (String date : Sets.union(repoWiseCommitMap.keySet(), repoWiseMergeMap.keySet())) {

					Long commitHours = repoWiseCommitMap.get(date);
					Long mergeHours = repoWiseMergeMap.get(date);
					KPIExcelData excelData = new KPIExcelData();
					excelData.setProjectName(projectName);
					Map<String, String> repoUrl = new HashMap<>();
					repoUrl.put(repoList.get(i), repoList.get(i));
					excelData.setRepositoryURL(repoUrl);
					excelData.setBranch(branchList.get(i));
					excelData.setDaysWeeks(date);
					excelData.setNumberOfCommit(commitHours.toString());
					excelData.setNumberOfMerge(mergeHours.toString());
					kpiExcelData.add(excelData);
				}

			}

		}
	}

	public static void populateKanbanLeadTime(List<KPIExcelData> kpiExcelData, String projectName,
			LeadTimeData leadTimeDataKanban) {

		if (!leadTimeDataKanban.getIssueNumber().isEmpty()) {
			for (int i = 0; i < leadTimeDataKanban.getIssueNumber().size(); i++) {
				KPIExcelData excelData = new KPIExcelData();
				excelData.setProjectName(projectName);
				Map<String, String> storyId = new HashMap<>();
				storyId.put(leadTimeDataKanban.getIssueNumber().get(i), leadTimeDataKanban.getUrlList().get(i));
				excelData.setStoryId(storyId);
				excelData.setIssueDesc(leadTimeDataKanban.getIssueDiscList().get(i));
				excelData.setOpenToTriage(leadTimeDataKanban.getOpenToTriage().get(i));
				excelData.setTriageToComplete(leadTimeDataKanban.getTriageToComplete().get(i));
				excelData.setCompleteToLive(leadTimeDataKanban.getCompleteToLive().get(i));
				excelData.setLeadTime(leadTimeDataKanban.getLeadTime().get(i));

				kpiExcelData.add(excelData);
			}
		}
	}

	public static void populateProductionDefectAgingExcelData(String projectName, List<JiraIssue> defectList,
			List<KPIExcelData> kpiExcelData) {
		if (CollectionUtils.isNotEmpty(defectList)) {
			defectList.forEach(defect -> {
				KPIExcelData excelData = new KPIExcelData();
				Map<String, String> defectLink = new HashMap<>();
				defectLink.put(defect.getNumber(), checkEmptyURL(defect));
				excelData.setProjectName(projectName);
				excelData.setDefectId(defectLink);
				excelData.setPriority(defect.getPriority());
				String date = Constant.EMPTY_STRING;
				if (defect.getCreatedDate() != null) {
					date = DateUtil.dateTimeConverter(defect.getCreatedDate(), DateUtil.DATE_FORMAT,
							DateUtil.DISPLAY_DATE_FORMAT);
				}
				excelData.setCreatedDate(date);
				excelData.setIssueDesc(checkEmptyName(defect));
				excelData.setStatus(defect.getJiraStatus());
				kpiExcelData.add(excelData);
			});
		}
	}

	public static void populateOpenTicketByAgeingExcelData(String projectName, List<KanbanJiraIssue> kanbanJiraIssues,
			List<KPIExcelData> kpiExcelData) {
		if (CollectionUtils.isNotEmpty(kanbanJiraIssues)) {
			kanbanJiraIssues.forEach(kanbanIssues -> {
				KPIExcelData excelData = new KPIExcelData();
				Map<String, String> storyMap = new HashMap<>();
				storyMap.put(kanbanIssues.getNumber(), checkEmptyURL(kanbanIssues));
				excelData.setProject(projectName);
				excelData.setTicketIssue(storyMap);
				excelData.setPriority(kanbanIssues.getPriority());
				excelData.setCreatedDate(DateUtil.dateTimeConverter(kanbanIssues.getCreatedDate(), DateUtil.TIME_FORMAT,
						DateUtil.DISPLAY_DATE_FORMAT));
				excelData.setIssueStatus(kanbanIssues.getJiraStatus());
				kpiExcelData.add(excelData);
			});
		}
	}

	/**
	 * prepare data for excel for cumulative kpi of Kanban on the basis of field.
	 * field can be RCA/priority/status field values as per field of jira
	 *
	 * @param projectName
	 * @param jiraHistoryFieldAndDateWiseIssueMap
	 * @param fieldValues
	 * @param kanbanJiraIssues
	 * @param excelDataList
	 * @param kpiId
	 */
	public static void prepareExcelForKanbanCumulativeDataMap(String projectName,
			Map<String, Map<String, Set<String>>> jiraHistoryFieldAndDateWiseIssueMap, Set<String> fieldValues,
			Set<KanbanIssueCustomHistory> kanbanJiraIssues, List<KPIExcelData> excelDataList, String date,
			String kpiId) {

		Map<String, Set<String>> fieldWiseIssuesLatestMap = filterKanbanDataBasedOnFieldLatestCumulativeData(
				jiraHistoryFieldAndDateWiseIssueMap, fieldValues);

		Map<String, Set<String>> fieldWiseIssues = fieldWiseIssuesLatestMap.entrySet().stream()
				.sorted((i1, i2) -> i1.getKey().compareTo(i2.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		fieldWiseIssues.entrySet().forEach(dateSet -> {
			String field = dateSet.getKey();
			dateSet.getValue().stream().forEach(values -> {
				KanbanIssueCustomHistory kanbanJiraIssue = kanbanJiraIssues.stream()
						.filter(issue -> issue.getStoryID().equalsIgnoreCase(values)).findFirst().get();
				KPIExcelData excelData = new KPIExcelData();
				excelData.setProject(projectName);
				Map<String, String> ticketMap = new HashMap<>();
				ticketMap.put(kanbanJiraIssue.getStoryID(), checkEmptyURL(kanbanJiraIssue));
				excelData.setTicketIssue(ticketMap);
				if (kpiId.equalsIgnoreCase(KPICode.NET_OPEN_TICKET_COUNT_BY_STATUS.getKpiId())) {
					excelData.setIssueStatus(field);
				}
				if (kpiId.equalsIgnoreCase(KPICode.NET_OPEN_TICKET_COUNT_BY_RCA.getKpiId())) {
					excelData.setRootCause(Arrays.asList(field));
				}
				if (kpiId.equalsIgnoreCase(KPICode.TICKET_COUNT_BY_PRIORITY.getKpiId())) {
					excelData.setPriority(field);
				}
				excelData.setCreatedDate(DateUtil.dateTimeConverter(kanbanJiraIssue.getCreatedDate(),
						DateUtil.TIME_FORMAT, DateUtil.DISPLAY_DATE_FORMAT));
				excelData.setDayWeekMonth(date);
				excelDataList.add(excelData);
			});
		});

	}

	/**
	 * prepare excel data only Today Cumulative data so that only latest data values
	 * of field(status/rca/priority)
	 *
	 * @param jiraHistoryFieldAndDateWiseIssueMap
	 * @param fieldValues
	 * @return
	 */
	private static Map<String, Set<String>> filterKanbanDataBasedOnFieldLatestCumulativeData(
			Map<String, Map<String, Set<String>>> jiraHistoryFieldAndDateWiseIssueMap, Set<String> fieldValues) {
		String date = LocalDate.now().toString();
		Map<String, Set<String>> fieldWiseIssuesLatestMap = new HashMap<>();
		fieldValues.forEach(field -> {
			Set<String> ids = jiraHistoryFieldAndDateWiseIssueMap.get(field).getOrDefault(date, new HashSet<>())
					.stream().filter(Objects::nonNull).collect(Collectors.toSet());
			fieldWiseIssuesLatestMap.put(field, ids);
		});
		return fieldWiseIssuesLatestMap;
	}

	public static void populateOpenVsClosedExcelData(String date, String projectName,
			List<KanbanJiraIssue> dateWiseIssueTypeList, List<KanbanIssueCustomHistory> dateWiseIssueClosedStatusList,
			List<KPIExcelData> excelDataList, String kpiId) {
		if (CollectionUtils.isNotEmpty(dateWiseIssueTypeList)
				|| CollectionUtils.isNotEmpty(dateWiseIssueClosedStatusList)) {
			dateWiseIssueTypeList.forEach(issue -> {
				KPIExcelData kpiExcelDataObject = new KPIExcelData();
				kpiExcelDataObject.setProject(projectName);
				kpiExcelDataObject.setDayWeekMonth(date);
				Map<String, String> storyDetails = new HashMap<>();
				storyDetails.put(issue.getNumber(), checkEmptyURL(issue));
				kpiExcelDataObject.setTicketIssue(storyDetails);
				if (kpiId.equalsIgnoreCase(KPICode.TICKET_OPEN_VS_CLOSED_RATE_BY_TYPE.getKpiId())) {
					kpiExcelDataObject.setIssueType(issue.getTypeName());
				} //
				if (kpiId.equalsIgnoreCase(KPICode.TICKET_OPEN_VS_CLOSE_BY_PRIORITY.getKpiId())) {
					kpiExcelDataObject.setIssuePriority(issue.getPriority());
				}
				kpiExcelDataObject.setStatus("Open");
				excelDataList.add(kpiExcelDataObject);
			});

			dateWiseIssueClosedStatusList.forEach(issue -> {
				KPIExcelData kpiExcelDataObject = new KPIExcelData();
				kpiExcelDataObject.setProject(projectName);
				kpiExcelDataObject.setDayWeekMonth(date);
				Map<String, String> storyDetails = new HashMap<>();
				storyDetails.put(issue.getStoryID(), checkEmptyURL(issue));
				kpiExcelDataObject.setTicketIssue(storyDetails);
				if (kpiId.equalsIgnoreCase(KPICode.TICKET_OPEN_VS_CLOSED_RATE_BY_TYPE.getKpiId())) {
					kpiExcelDataObject.setIssueType(issue.getStoryType());
				}
				if (kpiId.equalsIgnoreCase(KPICode.TICKET_OPEN_VS_CLOSE_BY_PRIORITY.getKpiId())) {
					kpiExcelDataObject.setIssuePriority(issue.getPriority());
				}
				kpiExcelDataObject.setStatus("Closed");
				excelDataList.add(kpiExcelDataObject);
			});
		}
	}

	public static void populateTicketVelocityExcelData(List<KanbanIssueCustomHistory> velocityList, String projectName,
			String date, List<KPIExcelData> kpiExcelData) {
		if (CollectionUtils.isNotEmpty(velocityList)) {
			velocityList.forEach(kanbanIssueCustomHistory -> {
				KPIExcelData excelData = new KPIExcelData();
				excelData.setProjectName(projectName);
				excelData.setDayWeekMonth(date);
				excelData.setIssueType(kanbanIssueCustomHistory.getStoryType());
				excelData.setSizeInStoryPoints(kanbanIssueCustomHistory.getEstimate());
				if (kanbanIssueCustomHistory.getStoryID() != null) {
					Map<String, String> storyId = new HashMap<>();
					storyId.put(kanbanIssueCustomHistory.getStoryID(), checkEmptyURL(kanbanIssueCustomHistory));
					excelData.setTicketIssue(storyId);
				}
				kpiExcelData.add(excelData);

			});
		}

	}

	public static void populateCodeBuildTimeExcelData(CodeBuildTimeInfo codeBuildTimeInfo, String projectName,
			List<KPIExcelData> kpiExcelData) {
		if (codeBuildTimeInfo != null)
			for (int i = 0; i < codeBuildTimeInfo.getBuildJobList().size(); i++) {
				KPIExcelData excelData = new KPIExcelData();
				excelData.setProjectName(projectName);
				excelData.setJobName(codeBuildTimeInfo.getBuildJobList().get(i));
				excelData.setStartTime(codeBuildTimeInfo.getBuildStartTimeList().get(i));
				excelData.setEndTime(codeBuildTimeInfo.getBuildEndTimeList().get(i));
				excelData.setDuration(codeBuildTimeInfo.getDurationList().get(i));
				Map<String, String> codeBuildUrl = new HashMap<>();
				codeBuildUrl.put(codeBuildTimeInfo.getBuildUrlList().get(i),
						codeBuildTimeInfo.getBuildUrlList().get(i));
				excelData.setBuildUrl(codeBuildUrl);
				excelData.setBuildStatus(codeBuildTimeInfo.getBuildStatusList().get(i));
				kpiExcelData.add(excelData);

			}
	}

	public static void populateCodeCommitKanbanExcelData(String projectName, List<Map<String, Long>> repoWiseCommitList,
			List<String> repoList, List<String> branchList, List<KPIExcelData> kpiExcelData) {
		if (CollectionUtils.isNotEmpty(repoWiseCommitList) && CollectionUtils.isNotEmpty(repoList)
				&& CollectionUtils.isNotEmpty(branchList)) {
			for (int i = 0; i < repoWiseCommitList.size(); i++) {
				for (String date : repoWiseCommitList.get(i).keySet()) {
					KPIExcelData excelData = new KPIExcelData();
					excelData.setProjectName(projectName);
					Map<String, String> repoUrl = new HashMap<>();
					repoUrl.put(repoList.get(i), repoList.get(i));
					excelData.setRepositoryURL(repoUrl);
					excelData.setBranch(branchList.get(i));
					excelData.setDaysWeeks(date);
					excelData.setNumberOfCommit(repoWiseCommitList.get(i).get(date).toString());
					kpiExcelData.add(excelData);
				}
			}
		}

	}

	public static void populateTeamCapacityKanbanExcelData(Double capacity, List<KPIExcelData> kpiExcelData,
			String projectName, CustomDateRange dateRange, String duration) {

		KPIExcelData excelData = new KPIExcelData();
		excelData.setProjectName(projectName);
		excelData.setStartDate(DateUtil.localDateTimeConverter(dateRange.getStartDate()));
		if (CommonConstant.DAYS.equalsIgnoreCase(duration)) {
			excelData.setEndDate(DateUtil.localDateTimeConverter(dateRange.getStartDate()));
		} else {
			excelData.setEndDate(DateUtil.localDateTimeConverter(dateRange.getEndDate()));
		}
		excelData.setEstimatedCapacity(df2.format(capacity));
		kpiExcelData.add(excelData);
	}

	/**
	 * Method to populate assignee name in kpi's
	 *
	 * @param jiraIssue
	 * @param object
	 */
	public static void populateAssignee(JiraIssue jiraIssue, Object object) {
		String assigneeName = jiraIssue.getAssigneeName() != null ? jiraIssue.getAssigneeName() : " - ";
		if (object instanceof IterationKpiModalValue) {
			((IterationKpiModalValue) object).setAssignee(assigneeName);
		} else if (object instanceof KPIExcelData) {
			((KPIExcelData) object).setAssignee(assigneeName);
		}
	}

	/**
	 * Common method to populate modal window of Iteration KPI's
	 *
	 * @param overAllModalValues
	 * @param modalValues
	 * @param jiraIssue
	 * @param fieldMapping
	 * @param modalObjectMap
	 */
	public static void populateIterationKPI(List<IterationKpiModalValue> overAllModalValues,
			List<IterationKpiModalValue> modalValues, JiraIssue jiraIssue, FieldMapping fieldMapping,
			Map<String, IterationKpiModalValue> modalObjectMap) {
		IterationKpiModalValue jiraIssueModalObject = modalObjectMap.get(jiraIssue.getNumber());
		jiraIssueModalObject.setIssueId(jiraIssue.getNumber());
		jiraIssueModalObject.setIssueURL(jiraIssue.getUrl());
		jiraIssueModalObject.setDescription(jiraIssue.getName());
		jiraIssueModalObject.setIssueStatus(jiraIssue.getStatus());
		jiraIssueModalObject.setIssueType(jiraIssue.getTypeName());
		jiraIssueModalObject.setPriority(jiraIssue.getPriority());
		KPIExcelUtility.populateAssignee(jiraIssue, jiraIssueModalObject);
		if (null != jiraIssue.getStoryPoints() && StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
				&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
			jiraIssueModalObject.setIssueSize(df2.format(jiraIssue.getStoryPoints()));
		}
		if (null != jiraIssue.getOriginalEstimateMinutes()
				&& StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
				&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.ACTUAL_ESTIMATION)) {
			Double originalEstimateInHours = Double.valueOf(jiraIssue.getOriginalEstimateMinutes()) / 60;
			jiraIssueModalObject.setIssueSize(df2.format(originalEstimateInHours / fieldMapping.getStoryPointToHourMapping()) + "/"
					+ originalEstimateInHours + " hrs");
		}
		jiraIssueModalObject.setDueDate((StringUtils.isNotEmpty(jiraIssue.getDueDate())) ? DateUtil.dateTimeConverter(
				jiraIssue.getDueDate(), DateUtil.TIME_FORMAT_WITH_SEC, DateUtil.DISPLAY_DATE_FORMAT) : "-");
		jiraIssueModalObject.setChangeDate(
				(StringUtils.isNotEmpty(jiraIssue.getChangeDate())) ? jiraIssue.getChangeDate().split("T")[0] : "-");
		jiraIssueModalObject.setCreatedDate(
				(StringUtils.isNotEmpty(jiraIssue.getCreatedDate())) ? jiraIssue.getCreatedDate().split("T")[0] : "-");
		jiraIssueModalObject.setUpdatedDate(
				(StringUtils.isNotEmpty(jiraIssue.getUpdateDate())) ? jiraIssue.getUpdateDate().split("T")[0] : "-");
		jiraIssueModalObject.setLabels(jiraIssue.getLabels());
		jiraIssueModalObject.setRootCauseList(jiraIssue.getRootCauseList());
		jiraIssueModalObject.setOwnersFullName(jiraIssue.getOwnersFullName());
		jiraIssueModalObject.setSprintName(jiraIssue.getSprintName());
		jiraIssueModalObject.setResolution(jiraIssue.getResolution());
		if (CollectionUtils.isNotEmpty(jiraIssue.getReleaseVersions())) {
			List<ReleaseVersion> releaseVersions = jiraIssue.getReleaseVersions();
			jiraIssueModalObject.setReleaseName(releaseVersions.get(releaseVersions.size() - 1).getReleaseName());
		}
		if (jiraIssue.getOriginalEstimateMinutes() != null) {
			jiraIssueModalObject
					.setOriginalEstimateMinutes(CommonUtils.convertIntoDays(jiraIssue.getOriginalEstimateMinutes()));
		} else {
			jiraIssueModalObject.setOriginalEstimateMinutes("0d");
		}
		if (jiraIssue.getRemainingEstimateMinutes() != null) {
			String remEstimate = CommonUtils.convertIntoDays(jiraIssue.getRemainingEstimateMinutes());
			jiraIssueModalObject.setRemainingEstimateMinutes(remEstimate);
			jiraIssueModalObject.setRemainingTimeInDays(remEstimate);
		} else {
			jiraIssueModalObject.setRemainingEstimateMinutes(Constant.DASH);
		}
		jiraIssueModalObject.setTimeSpentInMinutes(CommonUtils.convertIntoDays(jiraIssue.getTimeSpentInMinutes()));
		if (jiraIssue.getDevDueDate() != null)
			jiraIssueModalObject.setDevDueDate(jiraIssue.getDevDueDate().split("T")[0]);
		else
			jiraIssueModalObject.setDevDueDate(Constant.DASH);

		if (modalValues!=null && overAllModalValues!=null){
			modalValues.add(jiraIssueModalObject);
			overAllModalValues.add(jiraIssueModalObject);
		}
		else{
			modalObjectMap.computeIfPresent(jiraIssue.getNumber(),(k,v)->jiraIssueModalObject);
		}

	}

	/**
	 * This Method is used to populate Excel Data for Rejection Refinement KPI
	 *
	 * @param excelDataList
	 * @param issuesExcel
	 * @param weekAndTypeMap
	 * @param jiraDateMap
	 */
	public static void populateRefinementRejectionExcelData(List<KPIExcelData> excelDataList,
			List<JiraIssue> issuesExcel, Map<String, Map<String, List<JiraIssue>>> weekAndTypeMap,
			Map<String, LocalDateTime> jiraDateMap) {

		if (CollectionUtils.isNotEmpty(issuesExcel)) {
			issuesExcel.forEach(e -> {

				HashMap<String, String> data = getStatusNameAndWeekName(weekAndTypeMap, e);
				KPIExcelData excelData = new KPIExcelData();
				Map<String, String> epicLink = new HashMap<>();
				epicLink.put(e.getNumber(), checkEmptyURL(e));
				excelData.setChangeDate(DateUtil.localDateTimeConverter(LocalDate.parse(
						jiraDateMap.entrySet().stream().filter(f -> f.getKey().equalsIgnoreCase(e.getNumber()))
								.findFirst().get().getValue().toString().split("\\.")[0],
						DateTimeFormatter.ofPattern(DateUtil.TIME_FORMAT))));

				excelData.setIssueID(epicLink);
				excelData.setPriority(e.getPriority());
				excelData.setIssueDesc(e.getName());
				excelData.setStatus(e.getStatus());
				excelData.setIssueStatus(data.get(STATUS));
				excelData.setWeeks(data.get(WEEK));
				excelDataList.add(excelData);
			});
		}
	}

	/**
	 * This Method is used for fetching status and Weekname to show the data in
	 * excel data record
	 *
	 * @param weekAndTypeMap
	 * @param e
	 */
	private static HashMap<String, String> getStatusNameAndWeekName(
			Map<String, Map<String, List<JiraIssue>>> weekAndTypeMap, JiraIssue e) {
		HashMap<String, String> data = new HashMap<>();
		for (String week : weekAndTypeMap.keySet()) {
			for (String type : weekAndTypeMap.get(week).keySet()) {
				for (JiraIssue issue : weekAndTypeMap.get(week).get(type)) {
					if (issue.getNumber().equalsIgnoreCase(e.getNumber())) {
						data.put(STATUS, type);
						data.put(WEEK, week);
					}
				}
			}
		}
		return data;
	}

	public static void populateReleaseDefectRelatedExcelData(List<JiraIssue> jiraIssues,
															 List<KPIExcelData> kpiExcelData, FieldMapping fieldMapping) {
		if (CollectionUtils.isNotEmpty(jiraIssues)) {
			jiraIssues.stream().forEach(jiraIssue -> {
				KPIExcelData excelData = new KPIExcelData();
				excelData.setSprintName(jiraIssue.getSprintName());
				Map<String, String> issueDetails = new HashMap<>();
				issueDetails.put(jiraIssue.getNumber(), checkEmptyURL(jiraIssue));
				excelData.setIssueID(issueDetails);
				excelData.setIssueDesc(checkEmptyName(jiraIssue));
				excelData.setIssueStatus(jiraIssue.getStatus());
				excelData.setIssueType(jiraIssue.getTypeName());
				populateAssignee(jiraIssue, excelData);
				excelData.setRootCause(jiraIssue.getRootCauseList());
				excelData.setPriority(jiraIssue.getPriority());
				if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
						&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
					Double roundingOff = roundingOff(Optional.ofNullable(jiraIssue.getStoryPoints()).orElse(0.0));
					excelData.setStoryPoint(roundingOff.toString());
				} else if (null != jiraIssue.getAggregateTimeOriginalEstimateMinutes()) {
					Double totalOriginalEstimate = Double.valueOf(jiraIssue.getAggregateTimeOriginalEstimateMinutes()) / 60;
					excelData.setStoryPoint(roundingOff(totalOriginalEstimate / fieldMapping.getStoryPointToHourMapping()) + "/"
							+ roundingOff(totalOriginalEstimate) + " hrs");
				}
				kpiExcelData.add(excelData);
			});
		}
	}

	public static void populateBacklogCountExcelData(List<JiraIssue> jiraIssues, List<KPIExcelData> kpiExcelData) {
		if (CollectionUtils.isNotEmpty(jiraIssues)) {
			jiraIssues.stream().forEach(jiraIssue -> {
				KPIExcelData excelData = new KPIExcelData();
				Map<String, String> issueDetails = new HashMap<>();
				issueDetails.put(jiraIssue.getNumber(), checkEmptyURL(jiraIssue));
				excelData.setIssueID(issueDetails);
				excelData.setIssueDesc(checkEmptyName(jiraIssue));
				excelData.setIssueStatus(jiraIssue.getStatus());
				excelData.setIssueType(jiraIssue.getTypeName());
				populateAssignee(jiraIssue, excelData);
				excelData.setPriority(jiraIssue.getPriority());
				excelData.setStoryPoints(jiraIssue.getStoryPoints().toString());
				String date = Constant.EMPTY_STRING;
				if (jiraIssue.getCreatedDate() != null) {
					date = DateUtil.dateTimeConverter(jiraIssue.getCreatedDate(), DateUtil.DATE_FORMAT,
							DateUtil.DISPLAY_DATE_FORMAT);
				}
				excelData.setCreatedDate(date);
				String updateDate = Constant.EMPTY_STRING;
				if (jiraIssue.getUpdateDate() != null) {
					updateDate = DateUtil.dateTimeConverter(jiraIssue.getUpdateDate(),
							DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT);
				}
				excelData.setUpdatedDate(updateDate);
				kpiExcelData.add(excelData);
			});
		}
	}

	public static void populateFlowKPI(Map<String, Map<String, Integer>> dateTypeCountMap,
			List<KPIExcelData> excelData) {
		for (Map.Entry<String, Map<String, Integer>> entry : dateTypeCountMap.entrySet()) {
			String date = entry.getKey();
			Map<String, Integer> typeCountMap = entry.getValue();
			KPIExcelData kpiExcelData = new KPIExcelData();
			if (MapUtils.isNotEmpty(typeCountMap)) {
				kpiExcelData
						.setDate(DateUtil.dateTimeConverter(date, DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT));
				kpiExcelData.setCount(typeCountMap);
				excelData.add(kpiExcelData);
			}
		}
	}

	public static void populateHappinessIndexExcelData(String sprintName, List<KPIExcelData> excelDataList,
			List<HappinessKpiData> happinessKpiSprintDataList) {
		Map<Pair<String, String>, List<Integer>> userRatingsForSprintMap = new HashMap<>();
		if (CollectionUtils.isNotEmpty(happinessKpiSprintDataList)) {
			happinessKpiSprintDataList.forEach(data -> {
				List<UserRatingData> userRatingList = data.getUserRatingList();
				userRatingList.forEach(user -> populateUserMapForHappinessIndexKpi(userRatingsForSprintMap, user));
			});

			userRatingsForSprintMap.forEach((k, v) -> {
				KPIExcelData excelData = new KPIExcelData();
				excelData.setSprintName(sprintName);
				excelData.setUserName(k.getValue());
				Integer averageUserRatingPerSprint = v.stream().mapToInt(Integer::intValue).sum() / v.size();
				excelData.setSprintRating(averageUserRatingPerSprint);
				excelDataList.add(excelData);
			});

		}

	}

	private static void populateUserMapForHappinessIndexKpi(
			Map<Pair<String, String>, List<Integer>> userRatingsForSprintMap, UserRatingData user) {
		if (Objects.nonNull(user.getRating()) && !user.getRating().equals(0)) {
			Pair<String, String> userIdentifier = Pair.of(user.getUserId(), user.getUserName());
			List<Integer> userRatings = userRatingsForSprintMap.getOrDefault(userIdentifier, new ArrayList<>());
			userRatings.add(user.getRating());
			userRatingsForSprintMap.put(userIdentifier, userRatings);
		}
	}

	public static double roundingOff(double value) {
		return (double) Math.round(value * 100) / 100;
	}

	public static void populateBacklogDefectCountExcelData(List<JiraIssue> jiraIssues,
			List<KPIExcelData> kpiExcelData) {
		if (CollectionUtils.isNotEmpty(jiraIssues)) {
			jiraIssues.forEach(jiraIssue -> {
				KPIExcelData excelData = new KPIExcelData();
				Map<String, String> issueDetails = new HashMap<>();
				issueDetails.put(jiraIssue.getNumber(), checkEmptyURL(jiraIssue));
				excelData.setIssueID(issueDetails);
				excelData.setIssueDesc(checkEmptyName(jiraIssue));
				excelData.setIssueStatus(jiraIssue.getStatus());
				excelData.setIssueType(jiraIssue.getOriginalType());
				populateAssignee(jiraIssue, excelData);
				excelData.setPriority(jiraIssue.getPriority());
				excelData.setStoryPoints(jiraIssue.getStoryPoints().toString());
				List<String> sprintStatusList = Arrays.asList(CommonConstant.ACTIVE, CommonConstant.FUTURE);
				excelData.setSprintName(StringUtils.isNotEmpty(jiraIssue.getSprintName())
						&& StringUtils.isNotEmpty(jiraIssue.getSprintAssetState())
						&& sprintStatusList.contains(jiraIssue.getSprintAssetState()) ? jiraIssue.getSprintName()
								: "-");
				String date = Constant.EMPTY_STRING;
				if (jiraIssue.getCreatedDate() != null) {
					date = DateUtil.dateTimeConverter(jiraIssue.getCreatedDate(), DateUtil.DATE_FORMAT,
							DateUtil.DISPLAY_DATE_FORMAT);
				}
				excelData.setCreatedDate(date);
				String updateDate = Constant.EMPTY_STRING;
				if (jiraIssue.getUpdateDate() != null) {
					updateDate = DateUtil.dateTimeConverter(jiraIssue.getUpdateDate(),
							DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT);
				}
				excelData.setUpdatedDate(updateDate);
				kpiExcelData.add(excelData);
			});
		}
	}

	public static void populateReleaseDefectWithTestPhasesRelatedExcelData(List<JiraIssue> jiraIssues,
			List<KPIExcelData> kpiExcelData) {
		if (CollectionUtils.isNotEmpty(jiraIssues)) {
			jiraIssues.forEach(jiraIssue -> {
				KPIExcelData excelData = new KPIExcelData();
				Map<String, String> issueDetails = new HashMap<>();
				issueDetails.put(jiraIssue.getNumber(), checkEmptyURL(jiraIssue));
				excelData.setIssueID(issueDetails);
				excelData.setIssueDesc(checkEmptyName(jiraIssue));
				excelData.setIssueType(jiraIssue.getTypeName());
				excelData.setPriority(jiraIssue.getPriority());
				excelData.setSprintName(jiraIssue.getSprintName());
				populateAssignee(jiraIssue, excelData);
				excelData.setIssueStatus(jiraIssue.getStatus());
				kpiExcelData.add(excelData);
			});
		}
	}

	public static void populateScopeChurn(String sprintName, Map<String, JiraIssue> totalSprintStoryMap,
			Map<String, String> addedIssueDateMap, Map<String, String> removedIssueDateMap,
			List<KPIExcelData> excelDataList, FieldMapping fieldMapping) {
		if (MapUtils.isNotEmpty(totalSprintStoryMap)) {

			totalSprintStoryMap.forEach((storyId, jiraIssue) -> {
				KPIExcelData excelData = new KPIExcelData();
				excelData.setSprintName(sprintName);
				excelData.setIssueDesc(checkEmptyName(jiraIssue));
				Map<String, String> storyDetails = new HashMap<>();
				storyDetails.put(storyId, checkEmptyURL(jiraIssue));
				excelData.setIssueID(storyDetails);
				excelData.setIssueType(jiraIssue.getTypeName());
				excelData.setIssueDesc(checkEmptyName(jiraIssue));
				excelData.setIssueStatus(jiraIssue.getStatus());
				if (addedIssueDateMap.containsKey(jiraIssue.getNumber())) {
					excelData.setScopeChange(CommonConstant.ADDED);
					excelData.setScopeChangeDate(addedIssueDateMap.get(jiraIssue.getNumber()));
				}
				if (removedIssueDateMap.containsKey(jiraIssue.getNumber())) {
					excelData.setScopeChange(CommonConstant.REMOVED);
					excelData.setScopeChangeDate(removedIssueDateMap.get(jiraIssue.getNumber()));
				}
				if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
						&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
					excelData.setStoryPoint(Optional.ofNullable(jiraIssue.getStoryPoints()).orElse(0.0).toString());
				} else if (null != jiraIssue.getAggregateTimeOriginalEstimateMinutes()) {
					excelData.setStoryPoint(jiraIssue.getAggregateTimeOriginalEstimateMinutes() / 60 + " hrs");
				}
				excelDataList.add(excelData);

			});
		}
	}

	public static void populateIterationReadinessExcelData(List<JiraIssue> jiraIssues, List<KPIExcelData> kpiExcelData,
			FieldMapping fieldMapping) {
		if (CollectionUtils.isNotEmpty(jiraIssues)) {
			jiraIssues.stream().forEach(jiraIssue -> {
				KPIExcelData excelData = new KPIExcelData();
				excelData.setSprintName(jiraIssue.getSprintName());
				Map<String, String> issueDetails = new HashMap<>();
				issueDetails.put(jiraIssue.getNumber(), checkEmptyURL(jiraIssue));
				excelData.setIssueID(issueDetails);
				excelData.setIssueDesc(checkEmptyName(jiraIssue));
				excelData.setIssueStatus(jiraIssue.getStatus());
				excelData.setIssueType(jiraIssue.getTypeName());
				if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
						&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
					Double roundingOff = roundingOff(Optional.ofNullable(jiraIssue.getStoryPoints()).orElse(0.0));
					excelData.setStoryPoint(roundingOff.toString());
				} else if (null != jiraIssue.getAggregateTimeOriginalEstimateMinutes()) {
					Double totalOriginalEstimate = Double.valueOf(jiraIssue.getAggregateTimeOriginalEstimateMinutes())
							/ 60;
					excelData.setStoryPoint(
							roundingOff(totalOriginalEstimate / fieldMapping.getStoryPointToHourMapping()) + "/"
									+ roundingOff(totalOriginalEstimate) + " hrs");
				}
				String date = Constant.EMPTY_STRING;
				if (!jiraIssue.getSprintBeginDate().isEmpty()) {
					date = DateUtil.dateTimeConverter(jiraIssue.getSprintBeginDate(), ITERATION_DATE_FORMAT,
							DateUtil.DISPLAY_DATE_FORMAT);
				}
				excelData.setSprintStartDate(date);
				kpiExcelData.add(excelData);
			});
		}
	}

	public static void populateLeadTimeForChangeExcelData(String projectName,
			Map<String, List<LeadTimeChangeData>> leadTimeMapTimeWise, List<KPIExcelData> kpiExcelData , String leadTimeConfigRepoTool) {

		if (MapUtils.isNotEmpty(leadTimeMapTimeWise)) {
			leadTimeMapTimeWise.forEach((weekOrMonthName, leadTimeListCurrentTime) -> {
				leadTimeListCurrentTime.stream().forEach(leadTimeChangeData -> {
					KPIExcelData excelData = new KPIExcelData();
					excelData.setProjectName(projectName);
					excelData.setDate(weekOrMonthName);
					if(CommonConstant.REPO.equals(leadTimeConfigRepoTool)) {
						excelData.setMergeDate(leadTimeChangeData.getClosedDate());
						excelData.setMergeRequestId(leadTimeChangeData.getMergeID());
						excelData.setBranch(leadTimeChangeData.getFromBranch());
					} else {
						excelData.setCompletionDate(leadTimeChangeData.getClosedDate());
					}
					Map<String, String> issueDetails = new HashMap<>();
					issueDetails.put(leadTimeChangeData.getStoryID(), checkEmptyURL(leadTimeChangeData));
					excelData.setStoryId(issueDetails);
					excelData.setLeadTime(leadTimeChangeData.getLeadTimeInDays());
					excelData.setReleaseDate(leadTimeChangeData.getReleaseDate());
					kpiExcelData.add(excelData);
				});
			});
		}
	}


	public static void populateEpicProgessExcelData(Map<String, String> epicWiseIssueSize,
			Map<String, JiraIssue> epicIssues, List<KPIExcelData> excelDataList) {
		epicWiseIssueSize.forEach((epicNumber, issue) -> {
			KPIExcelData excelData = new KPIExcelData();
			JiraIssue jiraIssue = epicIssues.get(epicNumber);
			if (jiraIssue != null) {
				Map<String, String> storyDetails = new HashMap<>();
				storyDetails.put(epicNumber, checkEmptyURL(jiraIssue));
				excelData.setEpicID(storyDetails);
				excelData.setEpicName(checkEmptyName(jiraIssue));
				excelData.setEpicStatus(
						StringUtils.isNotEmpty(jiraIssue.getStatus()) ? jiraIssue.getStatus() : Constant.BLANK);
				excelData.setStoryPoint(issue);
				excelDataList.add(excelData);
			}
		});
	}

	/**
	 * Method to populate Modal Window of Mean Time to Recover
	 *
	 * @param projectName
	 *            Name of Project
	 * @param meanTimeRecoverMapTimeWise
	 *            Map<String, List<MeanTimeRecoverData>>
	 * @param kpiExcelData
	 *            List<KPIExcelData>
	 */
	public static void populateMeanTimeToRecoverExcelData(String projectName,
			Map<String, List<MeanTimeRecoverData>> meanTimeRecoverMapTimeWise, List<KPIExcelData> kpiExcelData) {
		if (MapUtils.isNotEmpty(meanTimeRecoverMapTimeWise)) {
			meanTimeRecoverMapTimeWise.forEach((weekOrMonthName,
					meanRecoverListCurrentTime) -> meanRecoverListCurrentTime.forEach(meanTimeRecoverData -> {
						KPIExcelData excelData = new KPIExcelData();
						excelData.setProjectName(projectName);
						excelData.setDate(weekOrMonthName);
						Map<String, String> issueDetails = new HashMap<>();
						issueDetails.put(meanTimeRecoverData.getStoryID(),
								StringUtils.isEmpty(meanTimeRecoverData.getUrl()) ? Constant.EMPTY_STRING
										: meanTimeRecoverData.getUrl());
						excelData.setStoryId(issueDetails);
						excelData.setIssueType(meanTimeRecoverData.getIssueType());
						excelData.setIssueDesc(meanTimeRecoverData.getDesc());
						excelData.setCompletionDate(meanTimeRecoverData.getClosedDate());
						excelData.setCreatedDate(meanTimeRecoverData.getCreatedDate());
						excelData.setTimeToRecover(meanTimeRecoverData.getTimeToRecover());
						kpiExcelData.add(excelData);
					}));
		}

	}

	public static void populateFlowEfficiency(LinkedHashMap<JiraIssueCustomHistory, Double> flowEfficiency,
			List<String> waitTimeList, List<String> totalTimeList, List<KPIExcelData> excelDataList) {
		AtomicInteger i = new AtomicInteger();
		flowEfficiency.forEach((issue, value) -> {
			KPIExcelData kpiExcelData = new KPIExcelData();
			Map<String, String> url = new HashMap<>();
			url.put(issue.getStoryID(), checkEmptyURL(issue));
			kpiExcelData.setIssueID(url);
			kpiExcelData.setIssueType(issue.getStoryType());
			kpiExcelData.setIssueDesc(issue.getDescription());
			kpiExcelData.setSizeInStoryPoints(issue.getEstimate());
			kpiExcelData.setWaitTime(waitTimeList.get(i.get()));
			kpiExcelData.setTotalTime(totalTimeList.get(i.get()));
			kpiExcelData.setFlowEfficiency(value.longValue());
			excelDataList.add(kpiExcelData);
			i.set(i.get() + 1);
		});
	}

	public static void populateLeadTime(LinkedHashMap<JiraIssueCustomHistory, Double> flowEfficiency,
			List<String> waitTimeList, List<String> totalTimeList, List<KPIExcelData> excelDataList) {
		AtomicInteger i = new AtomicInteger();
		flowEfficiency.forEach((issue, value) -> {
			KPIExcelData kpiExcelData = new KPIExcelData();
			Map<String, String> url = new HashMap<>();
			url.put(issue.getStoryID(), checkEmptyURL(issue));
			kpiExcelData.setIssueID(url);
			kpiExcelData.setIssueType(issue.getStoryType());
			kpiExcelData.setIssueDesc(issue.getDescription());
			kpiExcelData.setSizeInStoryPoints(issue.getEstimate());
			kpiExcelData.setWaitTime(waitTimeList.get(i.get()));
			kpiExcelData.setTotalTime(totalTimeList.get(i.get()));
			kpiExcelData.setFlowEfficiency(value.longValue());
			excelDataList.add(kpiExcelData);
			i.set(i.get() + 1);
		});
	}

	/**
	 * Method to populate the Release BurnUp Excel
	 *
	 * @param jiraIssues
	 *            jiraIssues
	 * @param issueWiseReleaseTagDateMap
	 *            issueWiseReleaseTagDateMap
	 * @param completeDateIssueMap
	 *            completeDateIssueMap
	 * @param devCompleteDateIssueMap
	 *            devCompleteDateIssueMap
	 * @param kpiExcelData
	 *            kpiExcelData
	 * @param fieldMapping
	 *            fieldMapping
	 */
	public static void populateReleaseBurnUpExcelData(List<JiraIssue> jiraIssues,
			Map<String, LocalDate> issueWiseReleaseTagDateMap, Map<String, LocalDate> completeDateIssueMap,
			Map<String, LocalDate> devCompleteDateIssueMap, List<KPIExcelData> kpiExcelData,
			FieldMapping fieldMapping) {
		if (CollectionUtils.isNotEmpty(jiraIssues)) {
			jiraIssues.forEach(jiraIssue -> {
				KPIExcelData excelData = new KPIExcelData();
				Map<String, String> issueDetails = new HashMap<>();
				issueDetails.put(jiraIssue.getNumber(), checkEmptyURL(jiraIssue));
				excelData.setIssueID(issueDetails);
				excelData.setIssueDesc(checkEmptyName(jiraIssue));
				excelData.setIssueStatus(jiraIssue.getStatus());
				excelData.setIssueType(jiraIssue.getTypeName());
				populateAssignee(jiraIssue, excelData);
				excelData.setPriority(jiraIssue.getPriority());
				if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
						&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
					double roundingOff = roundingOff(Optional.ofNullable(jiraIssue.getStoryPoints()).orElse(0.0));
					excelData.setStoryPoint(Double.toString(roundingOff));
				} else if (null != jiraIssue.getAggregateTimeOriginalEstimateMinutes()) {
					double totalOriginalEstimate = Double.valueOf(jiraIssue.getAggregateTimeOriginalEstimateMinutes()) / 60;
					excelData.setStoryPoint(
							roundingOff(totalOriginalEstimate / fieldMapping.getStoryPointToHourMapping()) + "/"
									+ roundingOff(totalOriginalEstimate) + " hrs");
				}
				excelData.setLatestReleaseTagDate(DateUtil.dateTimeConverter(
						String.valueOf(issueWiseReleaseTagDateMap.get(jiraIssue.getNumber())), DateUtil.DATE_FORMAT,
						DateUtil.DISPLAY_DATE_FORMAT));
				excelData.setDevCompleteDate(
						DateUtil.dateTimeConverter(String.valueOf(devCompleteDateIssueMap.get(jiraIssue.getNumber())),
								DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT));
				excelData.setCompletionDate(
						DateUtil.dateTimeConverter(String.valueOf(completeDateIssueMap.get(jiraIssue.getNumber())),
								DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT));
				kpiExcelData.add(excelData);
			});
		}

	}
}