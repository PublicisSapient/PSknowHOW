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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.model.ChangeFailureRateInfo;
import com.publicissapient.kpidashboard.apis.model.CodeBuildTimeInfo;
import com.publicissapient.kpidashboard.apis.model.DeploymentFrequencyInfo;
import com.publicissapient.kpidashboard.apis.model.CustomDateRange;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.jira.IssueDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.testexecution.KanbanTestExecution;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Sets;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.common.model.application.LeadTimeData;
import com.publicissapient.kpidashboard.common.model.application.ProjectVersion;
import com.publicissapient.kpidashboard.common.model.application.ResolutionTimeValidation;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.model.testexecution.TestExecution;
import com.publicissapient.kpidashboard.common.model.zephyr.TestCaseDetails;
import com.publicissapient.kpidashboard.common.util.DateUtil;

/**
 * The class contains mapping of kpi and Excel columns.
 *
 * @author pkum34
 */
public class KPIExcelUtility {

    private static final String MONTH_YEAR_FORMAT = "MMM yyyy";
    private static final String DATE_YEAR_MONTH_FORMAT = "dd-MMM-yy";

    private static final String DATE_FORMAT_PRODUCTION_DEFECT_AGEING = "yyyy-MM-dd";
    private static final DecimalFormat df2 = new DecimalFormat(".##");

    private KPIExcelUtility() {
    }

    /**
     * This method populate the excel data for DIR KPI
     *
     * @param sprint
     * @param storyIds
     * @param defects
     * @param kpiExcelData
     * @param issueData
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
                                                     List<KPIExcelData> kpiExcelData, Map<String, JiraIssue> issueData) {
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
                        excelData.setStoryPoints(jiraIssue.getStoryPoints().toString());
                    }
                }
                kpiExcelData.add(excelData);
            });
        }
    }


    public static void populateFTPRExcelData(String sprint, List<String> storyIds, List<JiraIssue> ftprStories,
                                             List<KPIExcelData> kpiExcelData, Map<String, JiraIssue> issueData) {
        List<String> collect = ftprStories.stream().map(JiraIssue::getNumber).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(storyIds)) {
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
     * @param totalBugList
     * @param conditionDefects
     * @param kpiExcelData
     * @param kpiId
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
        if(CollectionUtils.isNotEmpty(jiraIssues)) {
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

    /**
     * TO GET Constant.EXCEL_YES/"N" from complete list of defects if defect is
     * present in conditional list then Constant.EXCEL_YES else
     * Constant.EMPTY_STRING kpi specific
     *
     * @param sprint
     * @param totalStoriesMap
     * @param conditionStories
     * @param kpiExcelData
     */
    public static void populateCreatedVsResolvedExcelData(String sprint, Map<String, JiraIssue> totalStoriesMap,
                                                          List<JiraIssue> conditionStories, List<KPIExcelData> kpiExcelData) {
        if (MapUtils.isNotEmpty(totalStoriesMap)) {
            List<String> conditionalList = conditionStories.stream().map(JiraIssue::getNumber)
                    .collect(Collectors.toList());
            totalStoriesMap.forEach((storyId, jiraIssue) -> {
                String present = conditionalList.contains(storyId) ? Constant.EXCEL_YES : Constant.EMPTY_STRING;
                KPIExcelData excelData = new KPIExcelData();
                excelData.setSprintName(sprint);
                excelData.setIssueDesc(checkEmptyName(jiraIssue));
                Map<String, String> storyDetails = new HashMap<>();
                storyDetails.put(storyId, checkEmptyURL(jiraIssue));
                excelData.setCreatedDefectId(storyDetails);
                excelData.setResolvedTickets(present);

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
				}
				if (kpiId.equalsIgnoreCase(KPICode.SONAR_TECH_DEBT.getKpiId())
						|| kpiId.equalsIgnoreCase(KPICode.SONAR_TECH_DEBT_KANBAN.getKpiId())) {
					excelData.setTechDebt(kpiSpecificDataList.get(i));
				}
				if (kpiId.equalsIgnoreCase(KPICode.SONAR_VIOLATIONS.getKpiId())
						|| kpiId.equalsIgnoreCase(KPICode.SONAR_VIOLATIONS_KANBAN.getKpiId())) {
					excelData.setSonarViolation(kpiSpecificDataList.get(i));
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
            description = StringUtils.isEmpty(jiraIssue.getDescription()) ? Constant.EMPTY_STRING : jiraIssue.getDescription();
        }
        if (object instanceof JiraIssueCustomHistory) {
            JiraIssueCustomHistory jiraIssue = (JiraIssueCustomHistory) object;
            description = StringUtils.isEmpty(jiraIssue.getDescription()) ? Constant.EMPTY_STRING : jiraIssue.getDescription();
        }
        if (object instanceof ResolutionTimeValidation) {
            ResolutionTimeValidation resolutionTimeValidation = (ResolutionTimeValidation) object;
            description = StringUtils.isEmpty(resolutionTimeValidation.getIssueDescription()) ? Constant.EMPTY_STRING : resolutionTimeValidation.getIssueDescription();
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
            url = StringUtils.isEmpty(resolutionTimeValidation.getUrl()) ? Constant.EMPTY_STRING : resolutionTimeValidation.getUrl();
        }
		if (object instanceof IssueDetails) {
			IssueDetails issueDetails = (IssueDetails) object;
			url = StringUtils.isEmpty(issueDetails.getUrl()) ? Constant.EMPTY_STRING : issueDetails.getUrl();
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
			Set<IssueDetails> issueDetailsSet, List<KPIExcelData> kpiExcelData) {
		if (CollectionUtils.isEmpty(issueDetailsSet)) {
			if (MapUtils.isNotEmpty(totalStoriesMap)) {
				totalStoriesMap.forEach((storyId, jiraIssue) -> {
					KPIExcelData excelData = new KPIExcelData();
					excelData.setSprintName(sprint);
					Map<String, String> storyDetails = new HashMap<>();
					storyDetails.put(storyId, checkEmptyURL(jiraIssue));
					excelData.setStoryId(storyDetails);
					excelData.setIssueDesc(checkEmptyName(jiraIssue));
					excelData.setStoryPoints(jiraIssue.getStoryPoints().toString());

					kpiExcelData.add(excelData);
				});
			}
		} else {
			for (IssueDetails issueDetails : issueDetailsSet) {
				KPIExcelData excelData = new KPIExcelData();
				excelData.setSprintName(sprint);
				Map<String, String> storyDetails = new HashMap<>();
				storyDetails.put(issueDetails.getSprintIssue().getNumber(), checkEmptyURL(issueDetails));
				excelData.setStoryId(storyDetails);
				excelData.setIssueDesc(checkEmptyName(issueDetails));
				excelData.setStoryPoints(
						Optional.ofNullable(issueDetails.getSprintIssue().getStoryPoints()).orElse(0.0).toString());
				kpiExcelData.add(excelData);
			}
		}
	}

	public static void populateSprintPredictability(String sprint, Set<IssueDetails> issueDetailsSet,
			List<KPIExcelData> kpiExcelData) {
		if (CollectionUtils.isNotEmpty(issueDetailsSet)) {
			for (IssueDetails issueDetails : issueDetailsSet) {
				KPIExcelData excelData = new KPIExcelData();
				excelData.setSprintName(sprint);
				Map<String, String> storyDetails = new HashMap<>();
				storyDetails.put(issueDetails.getSprintIssue().getNumber(), checkEmptyURL(issueDetails));
				excelData.setStoryId(storyDetails);
				excelData.setIssueDesc(checkEmptyName(issueDetails));
				excelData.setStoryPoints(
						Optional.ofNullable(issueDetails.getSprintIssue().getStoryPoints()).orElse(0.0).toString());
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
				Double daysLogged = 0.0d;
				Double daysEstimated = 0.0d;
				if (issue.getTimeSpentInMinutes() != null) {
					daysLogged = Double.valueOf(issue.getTimeSpentInMinutes()) / 60;
				}
				excelData.setTotalTimeSpent(String.valueOf(daysLogged));

				if (issue.getOriginalEstimateMinutes() != null) {
					daysEstimated = Double.valueOf(issue.getOriginalEstimateMinutes()) / 60;
				}
				excelData.setOriginalTimeEstimate(String.valueOf(daysEstimated));
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

	public static void populateLeadTime(List<KPIExcelData> kpiExcelData, String projectName, LeadTimeData leadTimeData) {

		if (!leadTimeData.getIssueNumber().isEmpty()) {
			for (int i = 0; i < leadTimeData.getIssueNumber().size(); i++) {
				KPIExcelData excelData = new KPIExcelData();
				excelData.setProjectName(projectName);
				Map<String, String> storyId = new HashMap<>();
				storyId.put(leadTimeData.getIssueNumber().get(i), leadTimeData.getUrlList().get(i));
				excelData.setStoryId(storyId);
				excelData.setIssueDesc(leadTimeData.getIssueDiscList().get(i));
				excelData.setIntakeToDOR(leadTimeData.getIntakeToDor().get(i));
				excelData.setDorToDod(leadTimeData.getDorToDOD().get(i));
				excelData.setDodToLive(leadTimeData.getDodToLive().get(i));
				excelData.setLeadTime(leadTimeData.getIntakeToLive().get(i));

				kpiExcelData.add(excelData);
			}
		}
	}


	/**
     * TO GET Constant.EXCEL_YES/"N" from complete list of defects if defect is
     * present in conditional list then Constant.EXCEL_YES else
     * Constant.EMPTY_STRING kpi specific
     *
     * @param sprint
     * @param totalStoriesMap
     * @param conditionStories
     * @param kpiExcelData
     */

	public static void populateCommittmentReliability(String sprint, Map<String, JiraIssue> totalStoriesMap,
			List<JiraIssue> conditionStories, List<KPIExcelData> kpiExcelData) {
		if (MapUtils.isNotEmpty(totalStoriesMap)) {
			List<String> conditionalList = conditionStories.stream().map(JiraIssue::getNumber)
					.collect(Collectors.toList());
			totalStoriesMap.forEach((storyId, jiraIssue) -> {
				String present = conditionalList.contains(storyId) ? Constant.EXCEL_YES : Constant.EMPTY_STRING;
				KPIExcelData excelData = new KPIExcelData();
				excelData.setSprintName(sprint);
				excelData.setIssueDesc(checkEmptyName(jiraIssue));
				Map<String, String> storyDetails = new HashMap<>();
				storyDetails.put(storyId, checkEmptyURL(jiraIssue));
				excelData.setStoryId(storyDetails);
				excelData.setClosedStatus(present);

                kpiExcelData.add(excelData);

            });
        }
    }

    public static void populateCODExcelData(String projectName, List<JiraIssue> epicList,
                                            List<KPIExcelData> kpiExcelData) {
        if(CollectionUtils.isNotEmpty(epicList)) {
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
                                .optionalStart().appendPattern(".").appendFraction(ChronoField.MICRO_OF_SECOND, 1, 9, false)
                                .optionalEnd().toFormatter();
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

    public static void populateKanbanCODExcelData(String projectName, List<KanbanJiraIssue> epicList,
                                                  List<KPIExcelData> kpiExcelData) {
        if(CollectionUtils.isNotEmpty(epicList)) {
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
                                .optionalStart().appendPattern(".").appendFraction(ChronoField.MICRO_OF_SECOND, 1, 9, false)
                                .optionalEnd().toFormatter();
                        LocalDateTime dateTime = LocalDateTime.parse(epic.getChangeDate(), formatter);
                        month = dateTime.format(DateTimeFormatter.ofPattern(MONTH_YEAR_FORMAT));
                        epicEndDate = dateTime.toString();
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
        if(CollectionUtils.isNotEmpty(projectVersionList)) {
            projectVersionList.forEach(pv -> {
                KPIExcelData excelData = new KPIExcelData();
                excelData.setProjectName(projectName);
                excelData.setReleaseName(pv.getDescription());
                excelData.setReleaseDesc(pv.getName());
                excelData.setReleaseEndDate(pv.getReleaseDate().toString(DATE_YEAR_MONTH_FORMAT));
                excelData.setMonth(pv.getReleaseDate().toString(MONTH_YEAR_FORMAT));
                kpiExcelData.add(excelData);

            });
        }

    }

    public static void populateDeploymentFrequencyExcelData(String projectName, DeploymentFrequencyInfo deploymentFrequencyInfo,
                                                            List<KPIExcelData> kpiExcelData) {
        if(deploymentFrequencyInfo!=null) {
            for (int i = 0; i < deploymentFrequencyInfo.getJobNameList().size(); i++) {
                KPIExcelData excelData = new KPIExcelData();
                excelData.setProjectName(projectName);
                excelData.setDate(deploymentFrequencyInfo.getDeploymentDateList().get(i));
                excelData.setJobName(deploymentFrequencyInfo.getJobNameList().get(i));
                excelData.setMonth(deploymentFrequencyInfo.getMonthList().get(i));
                excelData.setDeploymentEnvironment(deploymentFrequencyInfo.getEnvironmentList().get(i));
                excelData.setMonth(deploymentFrequencyInfo.getMonthList().get(i));
                kpiExcelData.add(excelData);

            }
        }

    }

    public static void populateDefectWithoutIssueLinkExcelData(List<JiraIssue> defectWithoutStory,
                                                               List<KPIExcelData> kpiExcelData, String sprintName) {
        if(CollectionUtils.isNotEmpty(defectWithoutStory)) {
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

	public static void populateStoryCountExcelData(String sprint, List<KPIExcelData> kpiExcelData,
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
                    excelData.setWeeks(m.getKey());
                    excelData.setMeanTimetoMerge(m.getValue().toString());
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
                    excelData.setDays(date);
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
		if(CollectionUtils.isNotEmpty(defectList)) {
            defectList.forEach(defect -> {
                KPIExcelData excelData = new KPIExcelData();
                Map<String, String> defectLink = new HashMap<>();
                defectLink.put(defect.getNumber(), checkEmptyURL(defect));
                excelData.setProjectName(projectName);
                excelData.setDefectId(defectLink);
                excelData.setPriority(defect.getPriority());
                String date = Constant.EMPTY_STRING;
                if (defect.getCreatedDate() != null) {
                    DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern(DateUtil.TIME_FORMAT)
                            .optionalStart().appendPattern(".").appendFraction(ChronoField.MICRO_OF_SECOND, 1, 9, false)
                            .optionalEnd().toFormatter();
                    LocalDateTime dateTime = LocalDateTime.parse(defect.getCreatedDate(), formatter);
                    date = dateTime.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PRODUCTION_DEFECT_AGEING));
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
		if(CollectionUtils.isNotEmpty(kanbanJiraIssues)) {
            kanbanJiraIssues.forEach(kanbanIssues -> {
                KPIExcelData excelData = new KPIExcelData();
                Map<String, String> storyMap = new HashMap<>();
                storyMap.put(kanbanIssues.getNumber(), checkEmptyURL(kanbanIssues));
                excelData.setProject(projectName);
                excelData.setTicketIssue(storyMap);
                excelData.setPriority(kanbanIssues.getPriority());
                excelData.setCreatedDate(LocalDate.parse(kanbanIssues.getCreatedDate().split("T")[0]).toString());
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
				excelData.setCreatedDate(LocalDate.parse(kanbanJiraIssue.getCreatedDate().split("T")[0]).toString());
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
        if(CollectionUtils.isNotEmpty(velocityList)) {
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
                codeBuildUrl.put(codeBuildTimeInfo.getBuildUrlList().get(i), codeBuildTimeInfo.getBuildUrlList().get(i));
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
					excelData.setDays(date);
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
        excelData.setStartDate(dateRange.getStartDate().toString());
		if (CommonConstant.DAYS.equalsIgnoreCase(duration)) {
			excelData.setEndDate(dateRange.getStartDate().toString());
		} else {
			excelData.setEndDate(dateRange.getEndDate().toString());
		}
		excelData.setEstimatedCapacity(df2.format(capacity));
		kpiExcelData.add(excelData);
	}

}