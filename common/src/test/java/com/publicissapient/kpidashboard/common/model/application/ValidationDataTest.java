/*
 *
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.publicissapient.kpidashboard.common.model.application;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

public class ValidationDataTest {
	@Mock
	List<String> closedStoryKeyList;
	@Mock
	List<String> storyKeyList;
	@Mock
	List<String> totalStories;
	@Mock
	List<String> totalStoryKeyList;
	@Mock
	List<String> progressKeyList;
	@Mock
	List<String> ticketKeyList;
	@Mock
	List<String> defectKeyList;
	@Mock
	List<String> rejectedDefectKeyList;
	@Mock
	List<String> totalDefectKeyList;
	@Mock
	List<String> defectPriorityList;
	@Mock
	List<String> defectRootCauseList;
	@Mock
	List<String> sprintNameList;
	@Mock
	List<String> dateList;
	@Mock
	List<String> storyPointList;
	@Mock
	List<String> techDebtList;
	@Mock
	Map<String, List<String>> totalDefectWithAging;
	@Mock
	List<String> buildNameList;
	@Mock
	List<String> buildTimeList;
	@Mock
	List<String> jobName;
	@Mock
	List<String> startTime;
	@Mock
	List<String> endTime;
	@Mock
	List<String> duration;
	@Mock
	List<String> buildStatus;
	@Mock
	List<String> startedBy;
	@Mock
	List<String> buildUrl;
	@Mock
	List<String> subFiltersList;
	@Mock
	List<String> executionDateList;
	@Mock
	List<String> moduleNameList;
	@Mock
	List<String> releaseNameList;
	@Mock
	List<String> descriptionList;
	@Mock
	List<String> automatedTestList;
	@Mock
	List<String> totalTestList;
	@Mock
	List<String> executedTestList;
	@Mock
	List<String> executedPercentageList;
	@Mock
	List<String> passedTestList;
	@Mock
	List<String> passedPercentageList;
	@Mock
	List<String> failedTestList;
	@Mock
	List<Map<String, Integer>> failedRcaList;
	@Mock
	List<CycleTimeValidationData> cycleTimeList;
	@Mock
	List<LeadTimeValidationDataForKanban> leadTimeList;
	@Mock
	List<String> versionDate;
	@Mock
	List<String> projectName;
	@Mock
	List<String> sonarTechDebtList;
	@Mock
	List<String> coverageList;
	@Mock
	List<String> qualityList;
	@Mock
	List<String> violationList;
	@Mock
	List<String> repoList;
	@Mock
	List<String> branchList;
	@Mock
	List<Map<String, Long>> dayWiseCommitList;
	@Mock
	List<String> estimateTimeList;
	@Mock
	List<String> loggedTimeList;
	@Mock
	List<String> missingWorkLogList;
	@Mock
	List<String> dirValueList;
	@Mock
	List<String> avgClosedTicketsList;
	@Mock
	List<String> resolvedTickets;
	@Mock
	List<String> closedTickets;
	@Mock
	List<String> wipTickets;
	@Mock
	List<String> issueTypeList;
	@Mock
	List<Double> costOfDelayList;
	@Mock
	List<String> epicEndDateList;
	@Mock
	List<String> status;
	@Mock
	List<String> remainingTimeList;
	@Mock
	List<String> testWithoutStory;
	@Mock
	List<String> defectWithoutStoryList;
	@Mock
	List<String> firstTimePassStories;
	@Mock
	List<ResolutionTimeValidation> resolutionTimeIssues;
	@Mock
	List<Map<String, Double>> weekWiseMergeReqList;
	@Mock
	List<Map<String, Long>> dayWiseMergeList;
	@Mock
	List<String> epicIdList;
	@Mock
	List<String> epicNameList;
	@Mock
	List<String> totalTests;
	@Mock
	List<Integer> totalBuildCountList;
	@Mock
	List<Integer> totalBuildFailureCountList;
	@Mock
	List<Double> buildFailurePercentageList;
	@Mock
	List<String> weeksList;
	@Mock
	List<String> monthList;
	@Mock
	List<String> environmentList;
	@Mock
	List<String> openedTicketList;
	@Mock
	List<String> openedTicketIssueTypeList;
	@Mock
	List<String> openedTicketPriorityList;
	@Mock
	List<String> closedTicketList;
	@Mock
	List<String> closedTicketIssueTypeList;
	@Mock
	List<String> closedTicketPriorityList;
	@Mock
	List<String> issues;
	@Mock
	List<String> createdTicketList;
	@Mock
	List<String> defectsAddedAfterSprint;
	@InjectMocks
	ValidationData validationData;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testEquals() throws Exception {
		boolean result = validationData.equals("o");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testCanEqual() throws Exception {
		boolean result = validationData.canEqual("other");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testSetClosedStoryKeyList() throws Exception {
		validationData.setClosedStoryKeyList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetStoryKeyList() throws Exception {
		validationData.setStoryKeyList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetTotalStories() throws Exception {
		validationData.setTotalStories(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetTotalStoryKeyList() throws Exception {
		validationData.setTotalStoryKeyList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetProgressKeyList() throws Exception {
		validationData.setProgressKeyList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetTicketKeyList() throws Exception {
		validationData.setTicketKeyList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetDefectKeyList() throws Exception {
		validationData.setDefectKeyList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetRejectedDefectKeyList() throws Exception {
		validationData.setRejectedDefectKeyList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetTotalDefectKeyList() throws Exception {
		validationData.setTotalDefectKeyList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetDefectPriorityList() throws Exception {
		validationData.setDefectPriorityList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetDefectRootCauseList() throws Exception {
		validationData.setDefectRootCauseList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetSprintNameList() throws Exception {
		validationData.setSprintNameList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetDateList() throws Exception {
		validationData.setDateList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetStoryPointList() throws Exception {
		validationData.setStoryPointList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetTechDebtList() throws Exception {
		validationData.setTechDebtList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetTotalDefectWithAging() throws Exception {
		validationData.setTotalDefectWithAging(new HashMap<String, List<String>>() {
			{
				put("String", Arrays.<String>asList("String"));
			}
		});
	}

	@Test
	public void testSetBuildNameList() throws Exception {
		validationData.setBuildNameList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetBuildTimeList() throws Exception {
		validationData.setBuildTimeList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJobName() throws Exception {
		validationData.setJobName(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetStartTime() throws Exception {
		validationData.setStartTime(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetEndTime() throws Exception {
		validationData.setEndTime(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetDuration() throws Exception {
		validationData.setDuration(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetBuildStatus() throws Exception {
		validationData.setBuildStatus(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetStartedBy() throws Exception {
		validationData.setStartedBy(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetBuildUrl() throws Exception {
		validationData.setBuildUrl(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetSubFiltersList() throws Exception {
		validationData.setSubFiltersList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetExecutionDateList() throws Exception {
		validationData.setExecutionDateList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetModuleNameList() throws Exception {
		validationData.setModuleNameList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetReleaseNameList() throws Exception {
		validationData.setReleaseNameList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetDescriptionList() throws Exception {
		validationData.setDescriptionList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetAutomatedTestList() throws Exception {
		validationData.setAutomatedTestList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetTotalTestList() throws Exception {
		validationData.setTotalTestList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetExecutedTestList() throws Exception {
		validationData.setExecutedTestList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetExecutedPercentageList() throws Exception {
		validationData.setExecutedPercentageList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetPassedTestList() throws Exception {
		validationData.setPassedTestList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetPassedPercentageList() throws Exception {
		validationData.setPassedPercentageList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetFailedTestList() throws Exception {
		validationData.setFailedTestList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetFailedRcaList() throws Exception {
		validationData.setFailedRcaList(Arrays.<Map<String, Integer>>asList(new HashMap<String, Integer>() {
			{
				put("String", Integer.valueOf(0));
			}
		}));
	}

	@Test
	public void testSetCycleTimeList() throws Exception {
		validationData.setCycleTimeList(Arrays.<CycleTimeValidationData>asList(
				new CycleTimeValidationData("issueNumber", "url", "issueDesc", "issueType", null, null, null, null,
						Long.valueOf(1), Long.valueOf(1), Long.valueOf(1), Long.valueOf(1))));
	}

	@Test
	public void testSetLeadTimeList() throws Exception {
		validationData.setLeadTimeList(Arrays.<LeadTimeValidationDataForKanban>asList(
				new LeadTimeValidationDataForKanban("url", "issueDesc", "issueNumber", null, null, null, null)));
	}

	@Test
	public void testSetVersionDate() throws Exception {
		validationData.setVersionDate(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetProjectName() throws Exception {
		validationData.setProjectName(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetSonarTechDebtList() throws Exception {
		validationData.setSonarTechDebtList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetCoverageList() throws Exception {
		validationData.setCoverageList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetQualityList() throws Exception {
		validationData.setQualityList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetViolationList() throws Exception {
		validationData.setViolationList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetRepoList() throws Exception {
		validationData.setRepoList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetBranchList() throws Exception {
		validationData.setBranchList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetDayWiseCommitList() throws Exception {
		validationData.setDayWiseCommitList(Arrays.<Map<String, Long>>asList(new HashMap<String, Long>() {
			{
				put("String", Long.valueOf(1));
			}
		}));
	}

	@Test
	public void testSetEstimateTimeList() throws Exception {
		validationData.setEstimateTimeList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetLoggedTimeList() throws Exception {
		validationData.setLoggedTimeList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetMissingWorkLogList() throws Exception {
		validationData.setMissingWorkLogList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetDirValueList() throws Exception {
		validationData.setDirValueList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetAvgClosedTicketsList() throws Exception {
		validationData.setAvgClosedTicketsList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetResolvedTickets() throws Exception {
		validationData.setResolvedTickets(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetClosedTickets() throws Exception {
		validationData.setClosedTickets(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetWipTickets() throws Exception {
		validationData.setWipTickets(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetIssueTypeList() throws Exception {
		validationData.setIssueTypeList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetCostOfDelayList() throws Exception {
		validationData.setCostOfDelayList(Arrays.<Double>asList(Double.valueOf(0)));
	}

	@Test
	public void testSetEpicEndDateList() throws Exception {
		validationData.setEpicEndDateList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetStatus() throws Exception {
		validationData.setStatus(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetRemainingTimeList() throws Exception {
		validationData.setRemainingTimeList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetTestWithoutStory() throws Exception {
		validationData.setTestWithoutStory(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetDefectWithoutStoryList() throws Exception {
		validationData.setDefectWithoutStoryList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetFirstTimePassStories() throws Exception {
		validationData.setFirstTimePassStories(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetResolutionTimeIssues() throws Exception {
		validationData.setResolutionTimeIssues(Arrays.<ResolutionTimeValidation>asList(new ResolutionTimeValidation(
				"issueNumber", "url", "issueDescription", "issueType", Double.valueOf(0))));
	}

	@Test
	public void testSetWeekWiseMergeReqList() throws Exception {
		validationData.setWeekWiseMergeReqList(Arrays.<Map<String, Double>>asList(new HashMap<String, Double>() {
			{
				put("String", Double.valueOf(0));
			}
		}));
	}

	@Test
	public void testSetDayWiseMergeList() throws Exception {
		validationData.setDayWiseMergeList(Arrays.<Map<String, Long>>asList(new HashMap<String, Long>() {
			{
				put("String", Long.valueOf(1));
			}
		}));
	}

	@Test
	public void testSetEpicIdList() throws Exception {
		validationData.setEpicIdList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetEpicNameList() throws Exception {
		validationData.setEpicNameList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetTotalTests() throws Exception {
		validationData.setTotalTests(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetTotalBuildCountList() throws Exception {
		validationData.setTotalBuildCountList(Arrays.<Integer>asList(Integer.valueOf(0)));
	}

	@Test
	public void testSetTotalBuildFailureCountList() throws Exception {
		validationData.setTotalBuildFailureCountList(Arrays.<Integer>asList(Integer.valueOf(0)));
	}

	@Test
	public void testSetBuildFailurePercentageList() throws Exception {
		validationData.setBuildFailurePercentageList(Arrays.<Double>asList(Double.valueOf(0)));
	}

	@Test
	public void testSetWeeksList() throws Exception {
		validationData.setWeeksList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetMonthList() throws Exception {
		validationData.setMonthList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetEnvironmentList() throws Exception {
		validationData.setEnvironmentList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetOpenedTicketList() throws Exception {
		validationData.setOpenedTicketList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetOpenedTicketIssueTypeList() throws Exception {
		validationData.setOpenedTicketIssueTypeList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetOpenedTicketPriorityList() throws Exception {
		validationData.setOpenedTicketPriorityList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetClosedTicketList() throws Exception {
		validationData.setClosedTicketList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetClosedTicketIssueTypeList() throws Exception {
		validationData.setClosedTicketIssueTypeList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetClosedTicketPriorityList() throws Exception {
		validationData.setClosedTicketPriorityList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetIssues() throws Exception {
		validationData.setIssues(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetCreatedTicketList() throws Exception {
		validationData.setCreatedTicketList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetDefectsAddedAfterSprint() throws Exception {
		validationData.setDefectsAddedAfterSprint(Arrays.<String>asList("String"));
	}

	@Test
	public void testToString() throws Exception {
		String result = validationData.toString();
		Assert.assertNotNull(result);
	}
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme