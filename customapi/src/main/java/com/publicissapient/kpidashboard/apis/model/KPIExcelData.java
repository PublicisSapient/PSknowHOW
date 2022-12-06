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

package com.publicissapient.kpidashboard.apis.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.publicissapient.kpidashboard.apis.constant.KPIExcelConstant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the Excel Data for KPIs
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class KPIExcelData {

	@JsonProperty(KPIExcelConstant.SPRINT_NAME)
	private String sprintName;

	@JsonProperty(KPIExcelConstant.ORIGINAL_TIME_ESTIMATE_IN_HOURS)
	private String originalTimeEstimate;

	@JsonProperty("Total Time Spent (in hours)")
	private String totalTimeSpent;

	@JsonProperty("Closed")
	private String closedStatus;

	@JsonProperty(KPIExcelConstant.PROJECT_NAME)
	private String projectName;

	@JsonProperty(KPIExcelConstant.ISSUE_TYPE)
	private String issueType;

	@JsonProperty(KPIExcelConstant.RESOLUTION_TIME)
	private String resolutionTime;

	@JsonProperty(KPIExcelConstant.STORY_ID)
	private Map<String, String> storyId;

	@JsonProperty(KPIExcelConstant.ISSUE_DESCRIPTION)
	private String issueDesc;

	@JsonProperty(KPIExcelConstant.STORY_SIZE_SP)
	private String storyPoints;

	@JsonProperty(KPIExcelConstant.INTAKE_TO_DOR)
	private String intakeToDOR;

	@JsonProperty(KPIExcelConstant.DOR_TO_DOD)
	private String dorToDod;

	@JsonProperty(KPIExcelConstant.DOD_TO_LIVE)
	private String dodToLive;

	@JsonProperty(KPIExcelConstant.OPEN_TO_TRIAGE)
	private String openToTriage;

	@JsonProperty(KPIExcelConstant.TRIAGE_TO_COMPLETE)
	private String triageToComplete;

	@JsonProperty(KPIExcelConstant.COMPLETE_TO_LIVE)
	private String completeToLive;

	@JsonProperty(KPIExcelConstant.LEAD_TIME)
	private String leadTime;

	@JsonProperty("Linked Defects")
	private Map<String, String> linkedDefects;

	@JsonProperty("First Time Pass")
	private String firstTimePass;

	@JsonProperty("Escaped Defect")
	private String escapedDefect;

	@JsonProperty("Defect Removed")
	private String removedDefect;

	@JsonProperty("Defect Rejected")
	private String rejectedDefect;

	@JsonProperty("Priority")
	private String priority;

	@JsonProperty("Root Cause")
	private List<String> rootCause;

	@JsonProperty("Resolved")
	private String resolvedTickets;

	@JsonProperty(KPIExcelConstant.DEFECT_ID)
	private Map<String, String> defectId;

	@JsonProperty("Created Defect ID")
	private Map<String, String> createdDefectId;

	@JsonProperty("Test Case ID")
	private String testCaseId;

	@JsonProperty("Automated")
	private String automated;

	@JsonProperty(KPIExcelConstant.PROJECT)
	private String project;

	@JsonProperty(KPIExcelConstant.JOB_NAME)
	private String jobName;

	@JsonProperty("Unit Coverage")
	private String unitCoverage;

	@JsonProperty("Tech Debt (in days)")
	private String techDebt;

	@JsonProperty("Sonar Violations")
	private String sonarViolation;

	@JsonProperty(KPIExcelConstant.WEEKS)
	private String weeks;

	@JsonProperty("Linked Story ID")
	private Map<String, String> linkedStory;

	@JsonProperty("Total Build Count")
	private String buildCount;

	@JsonProperty("Total Build Failure Count")
	private String buildFailureCount;

	@JsonProperty("Build Failure Percentage")
	private String buildFailurePercentage;

	@JsonProperty("Total Test")
	private String totalTest;

	@JsonProperty("Executed Test")
	private String executedTest;

	@JsonProperty("Execution %")
	private String executionPercentage;

	@JsonProperty("Passed Test")
	private String passedTest;

	@JsonProperty("Passed %")
	private String passedPercentage;

	@JsonProperty("Epic ID")
	private Map<String, String> epicID;

	@JsonProperty("Cost of Delay")
	private Double costOfDelay;

	@JsonProperty("Epic Name")
	private String epicName;

	@JsonProperty("Epic End Date")
	private String epicEndDate;

	@JsonProperty("Release Name")
	private String releaseName;

	@JsonProperty("Release Description")
	private String releaseDesc;

	@JsonProperty("Release End Date")
	private String releaseEndDate;

	@JsonProperty("Date")
	private String date;

	@JsonProperty("Environment")
	private String deploymentEnvironment;

	@JsonProperty("Month")
	private String month;

	@JsonProperty("Defects Without Story Link")
	private Map<String, String> defectWithoutStoryLink;

	@JsonProperty("Linked to Story")
	private String isTestLinkedToStory;

	@JsonProperty("Status")
	private String status;

	@JsonProperty("Issue Status")
	private String issueStatus;

    @JsonProperty("Execution Date")
    private String executionDate;

    @JsonProperty(KPIExcelConstant.TICKET_ISSUE_ID)
    private Map<String, String> ticketIssue;

    @JsonProperty("Closed Ticket Issue ID")
    private Map<String, String> closedTicket;

	@JsonProperty(KPIExcelConstant.START_TIME)
	private String startTime;

	@JsonProperty(KPIExcelConstant.END_TIME)
	private String endTime;

	@JsonProperty(KPIExcelConstant.DURATION)
	private String duration;

	@JsonProperty(KPIExcelConstant.BUILD_STATUS)
	private String buildStatus;

	@JsonProperty("Started By")
	private String startedBy;

	@JsonProperty(KPIExcelConstant.BUILD_URL)
	private Map<String, String> buildUrl;

	@JsonProperty(KPIExcelConstant.REPOSITORY_URL)
	private Map<String, String> repositoryURL;

	@JsonProperty(KPIExcelConstant.BRANCH)
	private String branch;

	@JsonProperty(KPIExcelConstant.MEAN_TIME_TO_MERGE)
	private String meanTimetoMerge;

	@JsonProperty(KPIExcelConstant.DAY)
	private String days;

	@JsonProperty(KPIExcelConstant.COMMITS)
	private String numberOfCommit;

	@JsonProperty(KPIExcelConstant.MERGE)
	private String numberOfMerge;

	@JsonProperty("Created Date")
	private String createdDate;

    @JsonProperty("Closed Ticket Issue Type")
    private String closedTicketIssueType;

    @JsonProperty(KPIExcelConstant.DAY_WEEK_MONTH)
    private String dayWeekMonth;

    @JsonProperty("Issue Priority")
    private String issuePriority;

	@JsonProperty("Size (In Story Points)")
	private String sizeInStoryPoints;

	@JsonProperty("Start Date")
	private String startDate;

	@JsonProperty("End Date")
	private String endDate;

	@JsonProperty("Estimated Capacity (in hours)")
	private String estimatedCapacity;

	@JsonProperty("Linked Defects to Story")
	private Map<String, String> linkedDefectsStory;
}
