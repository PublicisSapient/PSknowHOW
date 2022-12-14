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

	@JsonProperty("Sprint Name")
	private String sprintName;

	@JsonProperty("Original Time Estimate (in hours)")
	private String originalTimeEstimate;

	@JsonProperty("Total Time Spent (in hours)")
	private String totalTimeSpent;

	@JsonProperty("Closed")
	private String closedStatus;

	@JsonProperty("Project Name")
	private String projectName;

	@JsonProperty("Issue Type")
	private String issueType;

	@JsonProperty("Resolution Time(In Days)")
	private String resolutionTime;

	@JsonProperty("Story ID")
	private Map<String, String> storyId;

	@JsonProperty("Issue Description")
	private String issueDesc;

	@JsonProperty("Story Size(In story point)")
	private String storyPoints;

	@JsonProperty("Intake to DOR(In Days)")
	private String intakeToDOR;

	@JsonProperty("DOR to DOD (In Days)")
	private String dorToDod;

	@JsonProperty("DOD TO Live (In Days)")
	private String dodToLive;

	@JsonProperty("Open to Triage(In Days)")
	private String openToTriage;

	@JsonProperty("Triage to Complete (In Days)")
	private String triageToComplete;

	@JsonProperty("Complete TO Live (In Days)")
	private String completeToLive;

	@JsonProperty("Lead Time (In Days)")
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

	@JsonProperty("Defect ID")
	private Map<String, String> defectId;

	@JsonProperty("Created Defect ID")
	private Map<String, String> createdDefectId;

	@JsonProperty("Test Case ID")
	private String testCaseId;

	@JsonProperty("Automated")
	private String automated;

	@JsonProperty("Project")
	private String project;

	@JsonProperty("Job Name")
	private String jobName;

	@JsonProperty("Unit Coverage")
	private String unitCoverage;

	@JsonProperty("Tech Debt (in days)")
	private String techDebt;

	@JsonProperty("Sonar Violations")
	private String sonarViolation;

	@JsonProperty("Weeks")
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

    @JsonProperty("Ticket Issue ID")
    private Map<String, String> ticketIssue;

    @JsonProperty("Closed Ticket Issue ID")
    private Map<String, String> closedTicket;

	@JsonProperty("Start Time")
	private String startTime;

	@JsonProperty("End Time")
	private String endTime;

	@JsonProperty("Duration")
	private String duration;

	@JsonProperty("Build Status")
	private String buildStatus;

	@JsonProperty("Started By")
	private String startedBy;

	@JsonProperty("Build Url")
	private Map<String, String> buildUrl;

	@JsonProperty("Repository Url")
	private Map<String, String> repositoryURL;

	@JsonProperty("Branch")
	private String branch;

	@JsonProperty("Mean Time To Merge (In Hours)")
	private String meanTimetoMerge;

	@JsonProperty("Day")
	private String days;

	@JsonProperty("No. Of Commit")
	private String numberOfCommit;

	@JsonProperty("No. of Merge")
	private String numberOfMerge;

	@JsonProperty("Created Date")
	private String createdDate;

    @JsonProperty("Closed Ticket Issue Type")
    private String closedTicketIssueType;

    @JsonProperty("Day/Week/Month")
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
