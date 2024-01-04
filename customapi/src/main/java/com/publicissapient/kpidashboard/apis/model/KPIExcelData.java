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

	@JsonProperty("Sprint Rating")
	private Integer sprintRating;

	@JsonProperty("User Name")
	private String userName;

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

	@JsonProperty("Issue ID")
	private Map<String, String> issueID;

	@JsonProperty("Issue Description")
	private String issueDesc;

	@JsonProperty("Story Size(In story point)")
	private String storyPoints;

	@JsonProperty("Intake to DOR")
	private String intakeToDOR;

	@JsonProperty("DOR to DOD")
	private String dorToDod;

	@JsonProperty("DOD to Live")
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

	@JsonProperty("Code Quality")
	private String codeQuality;

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

	@JsonProperty("Closed Date")
	private String closeDate;

	@JsonProperty("Updated Date")
	private String updatedDate;

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

	@JsonProperty("Sprint Start Date")
	private String sprintStartDate;

	@JsonProperty("End Date")
	private String endDate;

	@JsonProperty("Estimated Capacity (in hours)")
	private String estimatedCapacity;

	@JsonProperty("Linked Defects to Story")
	private Map<String, String> linkedDefectsStory;

	@JsonProperty("Size(story point/hours)")
	private String storyPoint;

	@JsonProperty("Planned Completion Date (Due Date)")
	private String dueDate;

	@JsonProperty("Remaining Estimate")
	private String remainingEstimateMinutes;

	@JsonProperty("Potential Delay(in days)")
	private String potentialDelay;

	@JsonProperty("Predicted Completion Date")
	private String predictedCompletionDate;

	@JsonProperty("Actual Completion Date")
	private String actualCompletionDate;

	@JsonProperty("Assignee")
	private String assignee;

	@JsonProperty("Defect added after Sprint Start")
	private String defectAddedAfterSprintStart;

	@JsonProperty("Change Date")
	private String changeDate;

	@JsonProperty("Created during Iteration")
	private String createdDuringIteration;

	@JsonProperty("Count")
	private Map<String, Integer> count;

	@JsonProperty("Initial Commitment")
	private String initialCommited;

	@JsonProperty("PI Name")
	private String piName;

	@JsonProperty("Planned Value")
	private String plannedValue;

	@JsonProperty("Achieved Value")
	private String achievedValue;

	@JsonProperty("Scope Change Date")
	private String scopeChangeDate;

	@JsonProperty("Scope Change (Added/Removed)")
	private String scopeChange;

	@JsonProperty("Epic Status")
	private String epicStatus;

	@JsonProperty("Completion Date")
	private String completionDate;

	@JsonProperty("Merge Date")
	private String mergeDate;

	@JsonProperty("Release Date")
	private String releaseDate;

	@JsonProperty("Merge Request Id")
	private String mergeRequestId;

	@JsonProperty("Pickup Time (In Hours)")
	private String pickupTime;

	@JsonProperty("PR Size (No. of lines)")
	private String prSize;

	@JsonProperty("Days/Weeks")
	private String daysWeeks;

	@JsonProperty("Time to Recover (In Hours)")
	private String timeToRecover;

	@JsonProperty("Wait Time")
	private String waitTime;

	@JsonProperty("Total Time")
	private String totalTime;

	@JsonProperty("Flow Efficiency")
	private Long flowEfficiency;

	@JsonProperty("Release Tag Date (Latest)")
	private String latestReleaseTagDate;

	@JsonProperty("Dev Completion Date")
	private String devCompleteDate;


}
