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

package com.publicissapient.kpidashboard.common.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

public class JiraIssueRepositoryTest {
	private static final String generalUseDate = "2015-11-01T00:00:00Z";
	private static final String generalUseDate2 = "2015-12-01T00:00:00Z";
	private static final String generalUseDate3 = "2015-12-15T00:00:00Z";
	private static final ObjectId jiraCollectorId = new ObjectId();
	private static JiraIssue mockV1JiraIssue;
	private static JiraIssue mockJiraJiraIssue;
	private static JiraIssue mockJiraJiraIssue2;
	private static JiraIssue mockJiraJiraIssue3;
	private static JiraIssue mockJiraJiraIssue4;
	private static JiraIssue mockJiraJiraIssue5;
	private static JiraIssue mockJiraJiraIssue6;
	private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	private static final String maxDateWinner = df.format(new Date());
	private static Calendar cal = Calendar.getInstance();
	private static String maxDateLoser = new String();
	private static String currentSprintEndDate = new String();
	// private static final ObjectId v1CollectorId = new ObjectId();
	@Mock
	private JiraIssueRepository featureRepo;

	public static List<Pattern> convertToPatternList(List<String> stringList) {
		List<Pattern> regexList = new ArrayList<>();
		for (String value : stringList) {
			regexList.add(Pattern.compile(value, Pattern.CASE_INSENSITIVE));
		}
		return regexList;
	}

	public static Pattern convertToPatternText(String text) {
		return Pattern.compile(text, Pattern.CASE_INSENSITIVE);
	}

	@Before
	public void setUp() {
		// Date-time modifications
		cal.setTime(new Date());
		cal.add(Calendar.DAY_OF_YEAR, -1);
		maxDateLoser = df.format(cal.getTime());
		cal.add(Calendar.DAY_OF_YEAR, +13);
		currentSprintEndDate = df.format(cal.getTime());

		// Helper mock data
		List<String> sOwnerNames = new ArrayList<String>();
		sOwnerNames.add("Goku");
		sOwnerNames.add("Gohan");
		sOwnerNames.add("Picolo");
		List<String> sOwnerDates = new ArrayList<String>();
		sOwnerNames.add(generalUseDate);
		sOwnerNames.add(generalUseDate);
		sOwnerNames.add(generalUseDate);
		List<String> sOwnerStates = new ArrayList<String>();
		sOwnerNames.add("Active");
		sOwnerNames.add("Active");
		sOwnerNames.add("Deleted");
		List<String> sOwnerIds = new ArrayList<String>();
		sOwnerNames.add("9001");
		sOwnerNames.add("8999");
		sOwnerNames.add("7999");
		List<String> sOwnerBools = new ArrayList<String>();
		sOwnerNames.add("True");
		sOwnerNames.add("False");
		sOwnerNames.add("True");

		// VersionOne Mock Feature
		mockV1JiraIssue = new JiraIssue();
		mockV1JiraIssue.setProcessorId(jiraCollectorId);
		mockV1JiraIssue.setIsDeleted("True");
		mockV1JiraIssue.setChangeDate(generalUseDate);
		mockV1JiraIssue.setEpicAssetState("Active");
		mockV1JiraIssue.setEpicBeginDate(generalUseDate);
		mockV1JiraIssue.setEpicChangeDate(generalUseDate);
		mockV1JiraIssue.setEpicEndDate(generalUseDate);
		mockV1JiraIssue.setEpicID("E-12345");
		mockV1JiraIssue.setEpicIsDeleted("False");
		mockV1JiraIssue.setEpicName("Test Epic 1");
		mockV1JiraIssue.setEpicNumber("12938715");
		mockV1JiraIssue.setEpicType("Feature");
		mockV1JiraIssue.setEstimate("5");
		mockV1JiraIssue.setIssueId("B-12345");
		mockV1JiraIssue.setName("Test Story 1");
		mockV1JiraIssue.setNumber("12345416");
		mockV1JiraIssue.setOwnersChangeDate(sOwnerDates);
		mockV1JiraIssue.setOwnersFullName(sOwnerNames);
		mockV1JiraIssue.setOwnersID(sOwnerIds);
		mockV1JiraIssue.setOwnersIsDeleted(sOwnerBools);
		mockV1JiraIssue.setOwnersShortName(sOwnerNames);
		mockV1JiraIssue.setOwnersState(sOwnerStates);
		mockV1JiraIssue.setOwnersUsername(sOwnerNames);
		mockV1JiraIssue.setProjectBeginDate(generalUseDate);
		mockV1JiraIssue.setProjectChangeDate(generalUseDate);
		mockV1JiraIssue.setProjectEndDate(generalUseDate);
		mockV1JiraIssue.setProjectID("Scope:231870");
		mockV1JiraIssue.setProjectIsDeleted("False");
		mockV1JiraIssue.setProjectName("Test Scope 1");
		mockV1JiraIssue.setProjectPath("Top -> Middle -> Bottome -> " + mockV1JiraIssue.getProjectName());
		mockV1JiraIssue.setProjectState("Active");
		mockV1JiraIssue.setSprintAssetState("Inactive");
		mockV1JiraIssue.setSprintBeginDate(generalUseDate);
		mockV1JiraIssue.setSprintChangeDate(generalUseDate);
		mockV1JiraIssue.setSprintEndDate(maxDateWinner);
		mockV1JiraIssue.setSprintID("Timebox:12781205");
		mockV1JiraIssue.setSprintIsDeleted("False");
		mockV1JiraIssue.setSprintName("Test Sprint 1");
		mockV1JiraIssue.setState("Inactive");
		mockV1JiraIssue.setStatus("Accepted");
		mockV1JiraIssue.setTeamAssetState("Active");
		mockV1JiraIssue.setTeamChangeDate(generalUseDate);
		mockV1JiraIssue.setTeamID("Team:124127");
		mockV1JiraIssue.setTeamIsDeleted("False");
		mockV1JiraIssue.setTeamName("Protectors of Earth");

		// Jira Mock Feature
		// Mock feature 1
		mockJiraJiraIssue = new JiraIssue();
		mockJiraJiraIssue.setProcessorId(jiraCollectorId);
		mockJiraJiraIssue.setIsDeleted("False");
		mockJiraJiraIssue.setChangeDate(maxDateWinner);
		mockJiraJiraIssue.setEpicAssetState("Active");
		mockJiraJiraIssue.setEpicBeginDate("");
		mockJiraJiraIssue.setEpicChangeDate(maxDateWinner);
		mockJiraJiraIssue.setEpicEndDate("");
		mockJiraJiraIssue.setEpicID("32112345");
		mockJiraJiraIssue.setEpicIsDeleted("");
		mockJiraJiraIssue.setEpicName("Test Epic 1");
		mockJiraJiraIssue.setEpicNumber("12938715");
		mockJiraJiraIssue.setEpicType("");
		mockJiraJiraIssue.setEstimate("40");
		mockJiraJiraIssue.setIssueId("0812345");
		mockJiraJiraIssue.setName("Test Story 2");
		mockJiraJiraIssue.setNumber("12345416");
		mockJiraJiraIssue.setOwnersChangeDate(sOwnerDates);
		mockJiraJiraIssue.setOwnersFullName(sOwnerNames);
		mockJiraJiraIssue.setOwnersID(sOwnerIds);
		mockJiraJiraIssue.setOwnersIsDeleted(sOwnerBools);
		mockJiraJiraIssue.setOwnersShortName(sOwnerNames);
		mockJiraJiraIssue.setOwnersState(sOwnerStates);
		mockJiraJiraIssue.setOwnersUsername(sOwnerNames);
		mockJiraJiraIssue.setProjectBeginDate(maxDateWinner);
		mockJiraJiraIssue.setProjectChangeDate(maxDateWinner);
		mockJiraJiraIssue.setProjectEndDate(maxDateWinner);
		mockJiraJiraIssue.setProjectID("583482");
		mockJiraJiraIssue.setProjectIsDeleted("False");
		mockJiraJiraIssue.setProjectName("Saiya-jin Warriors");
		mockJiraJiraIssue.setProjectPath("");
		mockJiraJiraIssue.setProjectState("Active");
		mockJiraJiraIssue.setSprintAssetState("Active");
		mockJiraJiraIssue.setSprintBeginDate(maxDateLoser);
		mockJiraJiraIssue.setSprintChangeDate(maxDateWinner);
		mockJiraJiraIssue.setSprintEndDate(currentSprintEndDate);
		mockJiraJiraIssue.setSprintID("1232512");
		mockJiraJiraIssue.setSprintIsDeleted("False");
		mockJiraJiraIssue.setSprintName("Test Sprint 2");
		mockJiraJiraIssue.setState("Active");
		mockJiraJiraIssue.setStatus("In Progress");
		mockJiraJiraIssue.setTeamAssetState("Active");
		mockJiraJiraIssue.setTeamChangeDate(maxDateWinner);
		mockJiraJiraIssue.setTeamID("08374321");
		mockJiraJiraIssue.setTeamIsDeleted("False");
		mockJiraJiraIssue.setTeamName("Saiya-jin Warriors");
		mockJiraJiraIssue.setTypeName("Risk");
		mockJiraJiraIssue.setPriority("P2 - Critical");
		mockJiraJiraIssue.setStatus("Open");

		// Mock feature 2
		mockJiraJiraIssue2 = new JiraIssue();
		mockJiraJiraIssue2.setProcessorId(jiraCollectorId);
		mockJiraJiraIssue2.setIsDeleted("False");
		mockJiraJiraIssue2.setChangeDate(maxDateLoser);
		mockJiraJiraIssue2.setEpicAssetState("Active");
		mockJiraJiraIssue2.setEpicBeginDate("");
		mockJiraJiraIssue2.setEpicChangeDate(maxDateLoser);
		mockJiraJiraIssue2.setEpicEndDate("");
		mockJiraJiraIssue2.setEpicID("32112345");
		mockJiraJiraIssue2.setEpicIsDeleted("");
		mockJiraJiraIssue2.setEpicName("Test Epic 1");
		mockJiraJiraIssue2.setEpicNumber("12938715");
		mockJiraJiraIssue2.setEpicType("");
		mockJiraJiraIssue2.setEstimate("40");
		mockJiraJiraIssue2.setIssueId("0812346");
		mockJiraJiraIssue2.setName("Test Story 3");
		mockJiraJiraIssue2.setNumber("12345417");
		mockJiraJiraIssue2.setOwnersChangeDate(sOwnerDates);
		mockJiraJiraIssue2.setOwnersFullName(sOwnerNames);
		mockJiraJiraIssue2.setOwnersID(sOwnerIds);
		mockJiraJiraIssue2.setOwnersIsDeleted(sOwnerBools);
		mockJiraJiraIssue2.setOwnersShortName(sOwnerNames);
		mockJiraJiraIssue2.setOwnersState(sOwnerStates);
		mockJiraJiraIssue2.setOwnersUsername(sOwnerNames);
		mockJiraJiraIssue2.setProjectBeginDate(maxDateLoser);
		mockJiraJiraIssue2.setProjectChangeDate(maxDateLoser);
		mockJiraJiraIssue2.setProjectEndDate(maxDateLoser);
		mockJiraJiraIssue2.setProjectID("583483");
		mockJiraJiraIssue2.setProjectIsDeleted("False");
		mockJiraJiraIssue2.setProjectName("Not Cell!");
		mockJiraJiraIssue2.setProjectPath("");
		mockJiraJiraIssue2.setProjectState("Active");
		mockJiraJiraIssue2.setSprintAssetState("Active");
		mockJiraJiraIssue2.setSprintBeginDate(maxDateLoser);
		mockJiraJiraIssue2.setSprintChangeDate(maxDateWinner);
		mockJiraJiraIssue2.setSprintEndDate(currentSprintEndDate);
		mockJiraJiraIssue2.setSprintID("1232512");
		mockJiraJiraIssue2.setSprintIsDeleted("False");
		mockJiraJiraIssue2.setSprintName("Test Sprint 3");
		mockJiraJiraIssue2.setState("Active");
		// mockJiraFeature2.setStatus("In Progress");
		mockJiraJiraIssue2.setTeamAssetState("Active");
		mockJiraJiraIssue2.setTeamChangeDate(maxDateLoser);
		mockJiraJiraIssue2.setTeamID("08374329");
		mockJiraJiraIssue2.setTeamIsDeleted("False");
		mockJiraJiraIssue2.setTeamName("Interlopers");
		mockJiraJiraIssue2.setTypeName("Issue");
		mockJiraJiraIssue2.setPriority("P2 - Critical");
		mockJiraJiraIssue2.setStatus("Open");

		// Mock feature 3
		mockJiraJiraIssue3 = new JiraIssue();
		mockJiraJiraIssue3.setProcessorId(jiraCollectorId);
		mockJiraJiraIssue3.setIsDeleted("False");
		mockJiraJiraIssue3.setChangeDate(generalUseDate2);
		mockJiraJiraIssue3.setEpicAssetState("Active");
		mockJiraJiraIssue3.setEpicBeginDate("");
		mockJiraJiraIssue3.setEpicChangeDate(maxDateLoser);
		mockJiraJiraIssue3.setEpicEndDate("");
		mockJiraJiraIssue3.setEpicID("32112345");
		mockJiraJiraIssue3.setEpicIsDeleted("");
		mockJiraJiraIssue3.setEpicName("Test Epic 1");
		mockJiraJiraIssue3.setEpicNumber("12938715");
		mockJiraJiraIssue3.setEpicType("");
		mockJiraJiraIssue3.setEstimate("80");
		mockJiraJiraIssue3.setIssueId("0812342");
		mockJiraJiraIssue3.setName("Test Story 4");
		mockJiraJiraIssue3.setNumber("12345412");
		mockJiraJiraIssue3.setOwnersChangeDate(sOwnerDates);
		mockJiraJiraIssue3.setOwnersFullName(sOwnerNames);
		mockJiraJiraIssue3.setOwnersID(sOwnerIds);
		mockJiraJiraIssue3.setOwnersIsDeleted(sOwnerBools);
		mockJiraJiraIssue3.setOwnersShortName(sOwnerNames);
		mockJiraJiraIssue3.setOwnersState(sOwnerStates);
		mockJiraJiraIssue3.setOwnersUsername(sOwnerNames);
		mockJiraJiraIssue3.setProjectBeginDate(maxDateLoser);
		mockJiraJiraIssue3.setProjectChangeDate(maxDateLoser);
		mockJiraJiraIssue3.setProjectEndDate(maxDateLoser);
		mockJiraJiraIssue3.setProjectID("583483");
		mockJiraJiraIssue3.setProjectIsDeleted("False");
		mockJiraJiraIssue3.setProjectName("Not Cell!");
		mockJiraJiraIssue3.setProjectPath("");
		mockJiraJiraIssue3.setProjectState("Active");
		mockJiraJiraIssue3.setSprintAssetState("Active");
		mockJiraJiraIssue3.setSprintBeginDate(maxDateLoser);
		mockJiraJiraIssue3.setSprintChangeDate(maxDateWinner);
		mockJiraJiraIssue3.setSprintEndDate(currentSprintEndDate);
		mockJiraJiraIssue3.setSprintID("1232512");
		mockJiraJiraIssue3.setSprintIsDeleted("False");
		mockJiraJiraIssue3.setSprintName("Test Sprint 3");
		mockJiraJiraIssue3.setState("Active");
		mockJiraJiraIssue3.setStatus("In Progress");
		mockJiraJiraIssue3.setTeamAssetState("Active");
		mockJiraJiraIssue3.setTeamChangeDate(maxDateLoser);
		mockJiraJiraIssue3.setTeamID("08374329");
		mockJiraJiraIssue3.setTeamIsDeleted("False");
		mockJiraJiraIssue3.setTeamName("Interlopers");
		mockJiraJiraIssue3.setTypeName("Issue");
		mockJiraJiraIssue3.setPriority("P2 - Critical");
		mockJiraJiraIssue3.setStatus("Open");

		// Mock feature 4
		mockJiraJiraIssue4 = new JiraIssue();
		mockJiraJiraIssue4.setProcessorId(jiraCollectorId);
		mockJiraJiraIssue4.setIsDeleted("False");
		mockJiraJiraIssue4.setChangeDate(generalUseDate3);
		mockJiraJiraIssue4.setEpicAssetState("Active");
		mockJiraJiraIssue4.setEpicBeginDate("");
		mockJiraJiraIssue4.setEpicChangeDate(maxDateLoser);
		mockJiraJiraIssue4.setEpicEndDate("");
		mockJiraJiraIssue4.setEpicID("32112345");
		mockJiraJiraIssue4.setEpicIsDeleted("");
		mockJiraJiraIssue4.setEpicName("Test Epic 1");
		mockJiraJiraIssue4.setEpicNumber("12938715");
		mockJiraJiraIssue4.setEpicType("");
		mockJiraJiraIssue4.setEstimate("45");
		mockJiraJiraIssue4.setIssueId("0812344");
		mockJiraJiraIssue4.setName("Test Story 4");
		mockJiraJiraIssue4.setNumber("12345414");
		mockJiraJiraIssue4.setOwnersChangeDate(sOwnerDates);
		mockJiraJiraIssue4.setOwnersFullName(sOwnerNames);
		mockJiraJiraIssue4.setOwnersID(sOwnerIds);
		mockJiraJiraIssue4.setOwnersIsDeleted(sOwnerBools);
		mockJiraJiraIssue4.setOwnersShortName(sOwnerNames);
		mockJiraJiraIssue4.setOwnersState(sOwnerStates);
		mockJiraJiraIssue4.setOwnersUsername(sOwnerNames);
		mockJiraJiraIssue4.setProjectBeginDate(maxDateLoser);
		mockJiraJiraIssue4.setProjectChangeDate(maxDateLoser);
		mockJiraJiraIssue4.setProjectEndDate(maxDateLoser);
		mockJiraJiraIssue4.setProjectID("583483");
		mockJiraJiraIssue4.setProjectIsDeleted("False");
		mockJiraJiraIssue4.setProjectName("Not Cell!");
		mockJiraJiraIssue4.setProjectPath("");
		mockJiraJiraIssue4.setProjectState("Active");
		mockJiraJiraIssue4.setSprintAssetState("Active");
		mockJiraJiraIssue4.setSprintBeginDate(maxDateLoser);
		mockJiraJiraIssue4.setSprintChangeDate(maxDateWinner);
		mockJiraJiraIssue4.setSprintEndDate(currentSprintEndDate);
		mockJiraJiraIssue4.setSprintID("1232512");
		mockJiraJiraIssue4.setSprintIsDeleted("False");
		mockJiraJiraIssue4.setSprintName("Test Sprint 3");
		mockJiraJiraIssue4.setState("Active");
		mockJiraJiraIssue4.setStatus("In Progress");
		mockJiraJiraIssue4.setTeamAssetState("Active");
		mockJiraJiraIssue4.setTeamChangeDate(maxDateLoser);
		mockJiraJiraIssue4.setTeamID("08374329");
		mockJiraJiraIssue4.setTeamIsDeleted("False");
		mockJiraJiraIssue4.setTeamName("Interlopers");
		mockJiraJiraIssue4.setTypeName("Issue");
		mockJiraJiraIssue4.setPriority("P2 - Critical");
		mockJiraJiraIssue4.setStatus("Open");

		// Mock feature 5
		mockJiraJiraIssue5 = new JiraIssue();
		mockJiraJiraIssue5.setProcessorId(jiraCollectorId);
		mockJiraJiraIssue5.setIsDeleted("False");
		mockJiraJiraIssue5.setChangeDate(generalUseDate3);
		mockJiraJiraIssue5.setEpicAssetState("Active");
		mockJiraJiraIssue5.setEpicBeginDate("");
		mockJiraJiraIssue5.setEpicChangeDate(maxDateLoser);
		mockJiraJiraIssue5.setEpicEndDate("");
		mockJiraJiraIssue5.setEpicID("32112345");
		mockJiraJiraIssue5.setEpicIsDeleted("");
		mockJiraJiraIssue5.setEpicName("Test Epic 1");
		mockJiraJiraIssue5.setEpicNumber("12938715");
		mockJiraJiraIssue5.setEpicType("");
		mockJiraJiraIssue5.setEstimate("45");
		mockJiraJiraIssue5.setIssueId("0812344");
		mockJiraJiraIssue5.setName("Test Story 4");
		mockJiraJiraIssue5.setNumber("12345414");
		mockJiraJiraIssue5.setOwnersChangeDate(sOwnerDates);
		mockJiraJiraIssue5.setOwnersFullName(sOwnerNames);
		mockJiraJiraIssue5.setOwnersID(sOwnerIds);
		mockJiraJiraIssue5.setOwnersIsDeleted(sOwnerBools);
		mockJiraJiraIssue5.setOwnersShortName(sOwnerNames);
		mockJiraJiraIssue5.setOwnersState(sOwnerStates);
		mockJiraJiraIssue5.setOwnersUsername(sOwnerNames);
		mockJiraJiraIssue5.setProjectBeginDate(maxDateLoser);
		mockJiraJiraIssue5.setProjectChangeDate(maxDateLoser);
		mockJiraJiraIssue5.setProjectEndDate(maxDateLoser);
		mockJiraJiraIssue5.setProjectID("583483");
		mockJiraJiraIssue5.setProjectIsDeleted("False");
		mockJiraJiraIssue5.setProjectName("SRDEVOPSDA-TEST");
		mockJiraJiraIssue5.setProjectPath("");
		mockJiraJiraIssue5.setProjectState("Active");
		mockJiraJiraIssue5.setSprintAssetState("Active");
		mockJiraJiraIssue5.setSprintBeginDate(maxDateLoser);
		mockJiraJiraIssue5.setSprintChangeDate(maxDateWinner);
		mockJiraJiraIssue5.setSprintEndDate(currentSprintEndDate);
		mockJiraJiraIssue5.setSprintID("1232512");
		mockJiraJiraIssue5.setSprintIsDeleted("False");
		mockJiraJiraIssue5.setSprintName("Test Sprint 3");
		mockJiraJiraIssue5.setState("Active");
		mockJiraJiraIssue5.setStatus("In Progress");
		mockJiraJiraIssue5.setTeamAssetState("Active");
		mockJiraJiraIssue5.setTeamChangeDate(maxDateLoser);
		mockJiraJiraIssue5.setTeamID("08374329");
		mockJiraJiraIssue5.setTeamIsDeleted("False");
		mockJiraJiraIssue5.setTeamName("Interlopers");
		mockJiraJiraIssue5.setPriority("P2 - Critical");
		mockJiraJiraIssue5.setBufferedEstimateTime(1400);
		mockJiraJiraIssue5.setEstimateTime(1500);
		mockJiraJiraIssue5.setSprintName("");
		mockJiraJiraIssue5.setTypeName("Defect");

		// Mock feature 6
		mockJiraJiraIssue6 = new JiraIssue();
		mockJiraJiraIssue6.setProcessorId(jiraCollectorId);
		mockJiraJiraIssue6.setIsDeleted("False");
		mockJiraJiraIssue6.setChangeDate(generalUseDate3);
		mockJiraJiraIssue6.setEpicAssetState("Active");
		mockJiraJiraIssue6.setEpicBeginDate("");
		mockJiraJiraIssue6.setEpicChangeDate(maxDateLoser);
		mockJiraJiraIssue6.setEpicEndDate("");
		mockJiraJiraIssue6.setEpicID("32112345");
		mockJiraJiraIssue6.setEpicIsDeleted("");
		mockJiraJiraIssue6.setEpicName("Test Epic 1");
		mockJiraJiraIssue6.setEpicNumber("12938715");
		mockJiraJiraIssue6.setEpicType("");
		mockJiraJiraIssue6.setEstimate("45");
		mockJiraJiraIssue6.setIssueId("0812344");
		mockJiraJiraIssue6.setName("Test Story 4");
		mockJiraJiraIssue6.setNumber("12345414");
		mockJiraJiraIssue6.setOwnersChangeDate(sOwnerDates);
		mockJiraJiraIssue6.setOwnersFullName(sOwnerNames);
		mockJiraJiraIssue6.setOwnersID(sOwnerIds);
		mockJiraJiraIssue6.setOwnersIsDeleted(sOwnerBools);
		mockJiraJiraIssue6.setOwnersShortName(sOwnerNames);
		mockJiraJiraIssue6.setOwnersState(sOwnerStates);
		mockJiraJiraIssue6.setOwnersUsername(sOwnerNames);
		mockJiraJiraIssue6.setProjectBeginDate(maxDateLoser);
		mockJiraJiraIssue6.setProjectChangeDate(maxDateLoser);
		mockJiraJiraIssue6.setProjectEndDate(maxDateLoser);
		mockJiraJiraIssue6.setProjectID("583483");
		mockJiraJiraIssue6.setProjectIsDeleted("False");
		mockJiraJiraIssue6.setProjectName("SR_ DevOps Dashboard");
		mockJiraJiraIssue6.setProjectPath("");
		mockJiraJiraIssue6.setProjectState("Active");
		mockJiraJiraIssue6.setSprintAssetState("Active");
		mockJiraJiraIssue6.setSprintBeginDate(maxDateLoser);
		mockJiraJiraIssue6.setSprintChangeDate(maxDateWinner);
		mockJiraJiraIssue6.setSprintEndDate(currentSprintEndDate);
		mockJiraJiraIssue6.setSprintID("");
		mockJiraJiraIssue6.setSprintIsDeleted("False");
		mockJiraJiraIssue6.setSprintName("Test Sprint 3");
		mockJiraJiraIssue6.setState("Backlog");
		mockJiraJiraIssue6.setStatus("Backlog");
		mockJiraJiraIssue6.setTeamAssetState("Active");
		mockJiraJiraIssue6.setTeamChangeDate(maxDateLoser);
		mockJiraJiraIssue6.setTeamID("08374329");
		mockJiraJiraIssue6.setTeamIsDeleted("False");
		mockJiraJiraIssue6.setTeamName("Interlopers");
		mockJiraJiraIssue6.setPriority("P2 - Critical");
		mockJiraJiraIssue6.setBufferedEstimateTime(1400);
		mockJiraJiraIssue6.setEstimateTime(1500);
		mockJiraJiraIssue6.setSprintName("");
		mockJiraJiraIssue6.setTypeName("Story");

	}

	@After
	public void tearDown() {
		mockV1JiraIssue = null;
		mockJiraJiraIssue = null;
		mockJiraJiraIssue2 = null;
		mockJiraJiraIssue3 = null;
		mockJiraJiraIssue4 = null;
		mockJiraJiraIssue5 = null;
		mockJiraJiraIssue6 = null;
		featureRepo.deleteAll();
	}

	@Test
	public void validateConnectivity_HappyPath() {
		MockitoAnnotations.initMocks(this);
		featureRepo.save(mockV1JiraIssue);
		featureRepo.save(mockJiraJiraIssue);
		featureRepo.save(mockJiraJiraIssue2);
		List<JiraIssue> jiraIssueList = new ArrayList<>();
		JiraIssue jiraIssue = new JiraIssue();
		jiraIssueList.add(jiraIssue);
		when(featureRepo.findAll()).thenReturn(jiraIssueList);
		assertTrue("Happy-path MongoDB connectivity validation for the FeatureRepository has failed",
				jiraIssueList.iterator().hasNext());
	}

	@Test
	public void testGetFeatureIdById_HappyPath() {
		MockitoAnnotations.initMocks(this);
		featureRepo.save(mockV1JiraIssue);
		featureRepo.save(mockJiraJiraIssue);
		featureRepo.save(mockJiraJiraIssue2);
		String testStoryId = "0812345";
		List<JiraIssue> jiraIssueList = new ArrayList<>();
		JiraIssue jiraIssue = new JiraIssue();
		jiraIssue.setIssueId("0812345");
		jiraIssue.setBasicProjectConfigId("676987987897");
		jiraIssueList.add(jiraIssue);
		when(featureRepo.findByIssueIdAndBasicProjectConfigId(StringEscapeUtils.escapeHtml4(testStoryId),
				"676987987897")).thenReturn(jiraIssueList.get(0));
		assertEquals("Expected feature ID did not match actual feature ID", testStoryId,
				jiraIssueList.get(0).getIssueId().toString());
	}

	@Test
	public void testFindTopByOrderByChangeDateDesc_HappyPath() {
		MockitoAnnotations.initMocks(this);
		featureRepo.save(mockV1JiraIssue);
		featureRepo.save(mockJiraJiraIssue);
		featureRepo.save(mockJiraJiraIssue2);
		featureRepo.save(mockJiraJiraIssue3);
		featureRepo.save(mockJiraJiraIssue4);
		List<JiraIssue> jiraIssueList = new ArrayList<>();
		JiraIssue jiraIssue = new JiraIssue();
		jiraIssue.setChangeDate(maxDateWinner);
		jiraIssueList.add(jiraIssue);
		when(featureRepo.findTopByProcessorIdAndChangeDateGreaterThanOrderByChangeDateDesc(jiraCollectorId,
				maxDateLoser)).thenReturn(jiraIssueList);
		assertEquals("Expected feature max change date did not match actual feature max change date", maxDateWinner,
				jiraIssueList.get(0).getChangeDate().toString());
	}

	@Test
	public void testFindTopByOrderByChangeDateDesc_BVA() {
		MockitoAnnotations.initMocks(this);
		featureRepo.save(mockV1JiraIssue);
		featureRepo.save(mockJiraJiraIssue);
		featureRepo.save(mockJiraJiraIssue2);
		featureRepo.save(mockJiraJiraIssue3);
		featureRepo.save(mockJiraJiraIssue4);
		// Setting slight differences in testable values for last change date
		int lastDigit = Integer
				.parseInt(maxDateWinner.substring(maxDateWinner.length() - 3, maxDateWinner.length() - 1));
		int biggerThanDigit = lastDigit + 1;
		int smallerThanDigit = lastDigit - 1;
		String biggerThanDigitConv;
		String smallerThanDigitConv;
		if (biggerThanDigit < 10) {
			biggerThanDigitConv = "0" + Integer.toString(biggerThanDigit);
		} else {
			biggerThanDigitConv = Integer.toString(biggerThanDigit);
		}
		if (smallerThanDigit < 10) {
			smallerThanDigitConv = "0" + Integer.toString(smallerThanDigit);
		} else {
			smallerThanDigitConv = Integer.toString(smallerThanDigit);
		}
		String biggerThanWinner = maxDateWinner.substring(0, maxDateWinner.length() - 3) + biggerThanDigitConv + "Z";
		String smallerThanWinner = maxDateWinner.substring(0, maxDateWinner.length() - 3) + smallerThanDigitConv + "Z";
		List<JiraIssue> jiraIssueList = new ArrayList<>();
		JiraIssue jiraIssue = new JiraIssue();
		jiraIssueList.add(jiraIssue);
		when(featureRepo.findTopByProcessorIdAndChangeDateGreaterThanOrderByChangeDateDesc(jiraCollectorId,
				smallerThanWinner)).thenReturn(jiraIssueList);
		assertEquals("Actual size should result in a size of 0", 0, featureRepo
				.findTopByProcessorIdAndChangeDateGreaterThanOrderByChangeDateDesc(jiraCollectorId, maxDateWinner)
				.size());
		assertEquals("Actual size should result in a size of 0", 0, featureRepo
				.findTopByProcessorIdAndChangeDateGreaterThanOrderByChangeDateDesc(jiraCollectorId, biggerThanWinner)
				.size());
		assertEquals("Actual size should result in a size of 1", 1, jiraIssueList.size());
	}

	@Test
	public void testFindTopByOrderByChangeDateDesc_RealisticDeltaStartDate() {
		MockitoAnnotations.initMocks(this);
		featureRepo.save(mockV1JiraIssue);
		featureRepo.save(mockJiraJiraIssue);
		featureRepo.save(mockJiraJiraIssue2);
		featureRepo.save(mockJiraJiraIssue3);
		featureRepo.save(mockJiraJiraIssue4);
		List<JiraIssue> jiraIssueList1 = new ArrayList<>();
		JiraIssue jiraIssue1 = new JiraIssue();
		jiraIssue1.setChangeDate(maxDateWinner);
		jiraIssueList1.add(jiraIssue1);
		List<JiraIssue> jiraIssueList2 = new ArrayList<>();
		when(featureRepo.findTopByProcessorIdAndChangeDateGreaterThanOrderByChangeDateDesc(jiraCollectorId,
				"2015-10-01T00:00:00Z")).thenReturn(jiraIssueList1);
		when(featureRepo.findTopByProcessorIdAndChangeDateGreaterThanOrderByChangeDateDesc(jiraCollectorId,
				maxDateWinner)).thenReturn(jiraIssueList2);
		assertTrue("Actual size should result in a size of 1", jiraIssueList1.size() == 1);
		assertTrue("Actual size should result in a size of 0", jiraIssueList2.size() == 0);
		assertTrue("Expected response of the maximum change date did not match the actual match change date",
				jiraIssueList1.get(0).getChangeDate().toString().equalsIgnoreCase(maxDateWinner));
	}
}