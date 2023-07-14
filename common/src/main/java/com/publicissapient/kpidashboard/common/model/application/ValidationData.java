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

package com.publicissapient.kpidashboard.common.model.application;//NOPMD

//Do not remove NOPMD comment. This is for ignoring ExcessivePublicCount violation

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Reprsents data to show on excel for different KPIs.
 */
@SuppressWarnings("PMD.TooManyFields")
@JsonInclude(Include.NON_NULL)
@Data
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ValidationData {

	@JsonProperty("Closed Stories")
	private List<String> closedStoryKeyList;

	@JsonProperty("Stories")
	private List<String> storyKeyList;

	@JsonProperty("Total Stories")
	private List<String> totalStories;

	@JsonProperty("Open Stories Including Dropped Stories")
	private List<String> totalStoryKeyList;

	@JsonProperty("In Progress Stories")
	private List<String> progressKeyList;

	@JsonProperty("Tickets")
	private List<String> ticketKeyList;

	@JsonProperty("Defects")
	private List<String> defectKeyList;

	@JsonProperty("Rejected Defects")
	private List<String> rejectedDefectKeyList;

	@JsonProperty("Total Defect")
	private List<String> totalDefectKeyList;

	@JsonProperty("Priority")
	private List<String> defectPriorityList;

	@JsonProperty("RootCause")
	private List<String> defectRootCauseList;

	@JsonProperty("Sprint Name")
	private List<String> sprintNameList;

	@JsonProperty("Date")
	private List<String> dateList;

	@JsonProperty("Story Point")
	private List<String> storyPointList;

	@JsonProperty("TechDebt(In Story Points)")
	private List<String> techDebtList;

	@JsonProperty("Total Defect Count")
	private Map<String, List<String>> totalDefectWithAging;

	@JsonProperty("Build Name")
	private List<String> buildNameList;

	@JsonProperty("Build Time")
	private List<String> buildTimeList;

	@JsonProperty("Job Name")
	private List<String> jobName;

	@JsonProperty("Start Time")
	private List<String> startTime;

	@JsonProperty("End Time")
	private List<String> endTime;

	@JsonProperty("Duration")
	private List<String> duration;

	@JsonProperty("Build Status")
	private List<String> buildStatus;

	@JsonProperty("Started By")
	private List<String> startedBy;

	@JsonProperty("Build Url")
	private List<String> buildUrl;

	@JsonProperty("Sub Filters")
	private List<String> subFiltersList;

	@JsonProperty("Execution Date")
	private List<String> executionDateList;

	@JsonProperty("Module Name")
	private List<String> moduleNameList;

	@JsonProperty("Release Name")
	private List<String> releaseNameList;

	@JsonProperty("Release Description")
	private List<String> descriptionList;

	@JsonProperty("Automated Test")
	private List<String> automatedTestList;

	@JsonProperty("Total Test")
	private List<String> totalTestList;

	@JsonProperty("Executed Test")
	private List<String> executedTestList;

	@JsonProperty("Execution %")
	private List<String> executedPercentageList;

	@JsonProperty("Passed Test")
	private List<String> passedTestList;

	@JsonProperty("Pass %")
	private List<String> passedPercentageList;

	@JsonProperty("Failed Test")
	private List<String> failedTestList;

	@JsonProperty("Failed Due To")
	private List<Map<String, Integer>> failedRcaList;

	@JsonProperty("Cycle Time")
	private List<CycleTimeValidationData> cycleTimeList;

	@JsonProperty("Lead Time")
	private List<LeadTimeValidationDataForKanban> leadTimeList;

	@JsonProperty("Version Date")
	private List<String> versionDate;

	@JsonProperty("Project Name")
	private List<String> projectName;

	@JsonProperty("Tech Debt")
	private List<String> sonarTechDebtList;

	@JsonProperty("Unit Coverage")
	private List<String> coverageList;

	@JsonProperty("Code Quality")
	private List<String> qualityList;

	@JsonProperty("Sonar Violations")
	private List<String> violationList;

	@JsonProperty("Repository Url")
	private List<String> repoList;

	@JsonProperty("Branch")
	private List<String> branchList;

	@JsonProperty("Day-wise Commit")
	private List<Map<String, Long>> dayWiseCommitList;

	@JsonProperty("Estimate Time (in hours)")
	private List<String> estimateTimeList;

	@JsonProperty("Total Time Spent (in hours)")
	private List<String> loggedTimeList;

	@JsonProperty("Total Time remaining (in hours)")
	private List<String> missingWorkLogList;

	@JsonProperty("DIR Value")
	private List<String> dirValueList;

	@JsonProperty("Average of Closed Tickets")
	private List<String> avgClosedTicketsList;

	@JsonProperty("Resolved Tickets")
	private List<String> resolvedTickets;

	@JsonProperty("Closed Tickets")
	private List<String> closedTickets;

	@JsonProperty("Work In Progress Tickets")
	private List<String> wipTickets;

	@JsonProperty("Issue Type")
	private List<String> issueTypeList;

	@JsonProperty("Cost Of Delay")
	private List<Double> costOfDelayList;

	@JsonProperty("Epic End Date")
	private List<String> epicEndDateList;

	@JsonProperty("Status")
	private List<String> status;

	@JsonProperty("Remaining Time")
	private List<String> remainingTimeList;

	@JsonProperty("Testcases Without Story Link")
	private List<String> testWithoutStory;

	@JsonProperty("Defects Without Story Link")
	private List<String> defectWithoutStoryList;

	@JsonProperty("First Time Pass Stories")
	private List<String> firstTimePassStories;

	@JsonProperty("Average Resolution Time")
	private List<ResolutionTimeValidation> resolutionTimeIssues;

	@JsonProperty("Mean Time To Merge")
	private List<Map<String, Double>> weekWiseMergeReqList;

	@JsonProperty("Day-wise Merge")
	private List<Map<String, Long>> dayWiseMergeList;

	@JsonProperty("Epic Id")
	private List<String> epicIdList;

	@JsonProperty("Epic Name")
	private List<String> epicNameList;

	@JsonProperty("Total Test Case")
	private List<String> totalTests;

	@JsonProperty("Total Build Count")
	private List<Integer> totalBuildCountList;

	@JsonProperty("Total Build Failure Count")
	private List<Integer> totalBuildFailureCountList;

	@JsonProperty("Build Failure Percentage")
	private List<Double> buildFailurePercentageList;

	@JsonProperty("Weeks")
	private List<String> weeksList;

	@JsonProperty("Month")
	private List<String> monthList;

	@JsonProperty("Environment")
	private List<String> environmentList;

	@JsonProperty("Opened Ticket")
	private List<String> openedTicketList;

	@JsonProperty("Opened Ticket Issue Type")
	private List<String> openedTicketIssueTypeList;

	@JsonProperty("Opened Ticket Priority")
	private List<String> openedTicketPriorityList;

	@JsonProperty("Closed Ticket")
	private List<String> closedTicketList;

	@JsonProperty("Closed Ticket Issue Type")
	private List<String> closedTicketIssueTypeList;

	@JsonProperty("Closed Ticket Priority")
	private List<String> closedTicketPriorityList;

	@JsonProperty("Issues")
	private List<String> issues;

	@JsonProperty("Created Tickets")
	private List<String> createdTicketList;

	@JsonProperty("Defects Added after Sprint Start")
	private List<String> defectsAddedAfterSprint;
}
