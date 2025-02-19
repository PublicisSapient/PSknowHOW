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

package com.publicissapient.kpidashboard.jira.processor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.atlassian.jira.rest.client.api.StatusCategory;
import com.atlassian.jira.rest.client.api.domain.BasicPriority;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.BasicUser;
import com.atlassian.jira.rest.client.api.domain.BasicVotes;
import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.ChangelogItem;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.FieldType;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.api.domain.IssueLinkType;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Resolution;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.Visibility;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueHistoryRepository;
import com.publicissapient.kpidashboard.jira.dataFactories.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.KanbanJiraIssueDataFactory;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

@RunWith(MockitoJUnitRunner.class)
public class KanbanJiraIssueHistoryProcessorImplTest {

	KanbanJiraIssue jiraIssue;
	@InjectMocks
	private KanbanJiraIssueHistoryProcessorImpl createJiraIssueHistory;
	@Mock
	private KanbanIssueCustomHistory jiraIssueCustomHistory;
	@Mock
	private KanbanJiraIssueHistoryRepository kanbanIssueHistoryRepo;
	@Mock
	private FieldMapping fieldMapping;
	private List<ChangelogGroup> changeLogList = new ArrayList<>();
	private Issue issue;

	@Before
	public void setUp() throws URISyntaxException, JSONException {

		jiraIssueCustomHistory = new KanbanIssueCustomHistory();
		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/kanban_project_field_mappings.json");
		fieldMapping = fieldMappingDataFactory.findById("6335e4ea4fd2f76a82111843");
		createIssue();
		jiraIssue = getMockKanbanJiraIssue();
	}

	@Test
	public void createIssueCustomHistory() {
		when(kanbanIssueHistoryRepo.findByStoryIDAndBasicProjectConfigId(any(), any()))
				.thenReturn(new KanbanIssueCustomHistory());
		Assert.assertEquals(
				"6335368249794a18e8a4479f",
				createJiraIssueHistory
						.convertToKanbanIssueHistory(issue, createProjectConfig(), jiraIssue)
						.getBasicProjectConfigId());
	}

	@Test
	public void createIssueCustomHistory2() {
		KanbanIssueCustomHistory jiraIssueHistory = new KanbanIssueCustomHistory();
		jiraIssueHistory.setStoryID("12344");
		List<KanbanIssueCustomHistory> kanbanIssueCustomHistoryList = new ArrayList<>();
		kanbanIssueCustomHistoryList.add(jiraIssueHistory);
		when(kanbanIssueHistoryRepo.findByStoryIDAndBasicProjectConfigId(any(), any()))
				.thenReturn(kanbanIssueCustomHistoryList.get(0));
		Assert.assertEquals("6335368249794a18e8a4479f", createJiraIssueHistory
				.convertToKanbanIssueHistory(issue, createProjectConfig(), jiraIssue).getBasicProjectConfigId());
	}

	private ProjectConfFieldMapping createProjectConfig() {
		ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
		projectConfFieldMapping.setBasicProjectConfigId(new ObjectId("63c04dc7b7617e260763ca4e"));
		projectConfFieldMapping.setFieldMapping(fieldMapping);
		Connection connection = Connection.builder().cloudEnv(false).build();
		JiraToolConfig jiraToolConfig = JiraToolConfig.builder().connection(Optional.ofNullable(connection)).build();
		projectConfFieldMapping.setJira(jiraToolConfig);

		return projectConfFieldMapping;
	}

	private KanbanJiraIssue getMockKanbanJiraIssue() {
		KanbanJiraIssueDataFactory jiraIssueDataFactory = KanbanJiraIssueDataFactory
				.newInstance("/json/default/kanban_jira_issue.json");
		return jiraIssueDataFactory.findTopByBasicProjectConfigId("6335368249794a18e8a4479f");
	}

	// private void prepareFieldMapping() {
	// FieldMapping fieldMapping = new FieldMapping();
	// fieldMapping.setBasicProjectConfigId(new
	// ObjectId("5b674d58f47cae8935b1b26f"));
	// fieldMapping.setSprintName("customfield_12700");
	// List<String> jiraType = new ArrayList<>();
	// jiraType.add("Defect");
	// fieldMapping.setJiradefecttype(jiraType);
	// jiraType = new ArrayList<>(
	// Arrays.asList(new String[] { "Story", "Defect", "Pre Story", "Feature",
	// "Enabler Story" }));
	// String[] jiraIssueType = new String[] { "Story", "Defect", "Pre Story",
	// "Feature", "Enabler Story" };
	// fieldMapping.setJiraIssueTypeNames(jiraIssueType);
	// fieldMapping.setRootCause("customfield_19121");
	//
	// jiraType = new ArrayList<>();
	// jiraType.add("Story");
	// fieldMapping.setJiraDefectInjectionIssueType(jiraType);
	// fieldMapping.setJiraTechDebtIssueType(jiraType);
	// fieldMapping.setJiraDefectSeepageIssueType(jiraType);
	// fieldMapping.setJiraDefectRemovalStatus(jiraType);
	// fieldMapping.setJiraDefectRejectionlIssueType(jiraType);
	// fieldMapping.setJiraTestAutomationIssueType(jiraType);
	// fieldMapping.setJiraDefectRejectionlIssueType(jiraType);
	// fieldMapping.setJiraDefectCountlIssueType(jiraType);
	// fieldMapping.setJiraIntakeToDorIssueType(jiraType);
	// fieldMapping.setJiraBugRaisedByCustomField("customfield_12121");
	//
	// fieldMapping.setJiraTechDebtIdentification(CommonConstant.CUSTOM_FIELD);
	// fieldMapping.setJiraTechDebtCustomField("customfield_14141");
	//
	// jiraType = new ArrayList<>();
	// jiraType.add("TECH_DEBT");
	// fieldMapping.setJiraTechDebtValue(jiraType);
	// fieldMapping.setJiraDefectRejectionStatus("Dropped");
	// fieldMapping.setJiraBugRaisedByIdentification("CustomField");
	//
	// jiraType = new ArrayList<>();
	// jiraType.add("Ready for Sign-off");
	// fieldMapping.setJiraDod(jiraType);
	//
	// jiraType = new ArrayList<>();
	// jiraType.add("Closed");
	// fieldMapping.setJiraDefectRemovalStatus(jiraType);
	//
	// fieldMapping.setJiraStoryPointsCustomField("customfield_56789");
	//
	// jiraType = new ArrayList<>();
	// jiraType.add("40");
	//
	// jiraType = new ArrayList<>();
	// jiraType.add("Client Testing (UAT)");
	// fieldMapping.setJiraBugRaisedByValue(jiraType);
	//
	// jiraType = new ArrayList<>();
	// jiraType.add("Story");
	// jiraType.add("Feature");
	// fieldMapping.setJiraSprintVelocityIssueType(jiraType);
	//
	// jiraType = new ArrayList<>(Arrays.asList(new String[] { "Story", "Defect",
	// "Pre Story", "Feature" }));
	// fieldMapping.setJiraSprintCapacityIssueType(jiraType);
	//
	// jiraType = new ArrayList<>();
	// jiraType.add("Closed");
	// fieldMapping.setJiraIssueDeliverdStatus(jiraType);
	//
	// fieldMapping.setJiraDor("In Progress");
	// fieldMapping.setJiraLiveStatus("Closed");
	// fieldMapping.setRootCauseValue(Arrays.asList("Coding", "None"));
	//
	// jiraType = new ArrayList<>(Arrays.asList(new String[] { "Story", "Pre Story"
	// }));
	// fieldMapping.setJiraStoryIdentification(jiraType);
	//
	// fieldMapping.setJiraDefectCreatedStatus("Open");
	//
	// jiraType = new ArrayList<>();
	// jiraType.add("Ready for Sign-off");
	// fieldMapping.setJiraDod(jiraType);
	// fieldMapping.setStoryFirstStatus("In Analysis");
	// jiraType = new ArrayList<>();
	// jiraType.add("In Analysis");
	// jiraType.add("In Development");
	// fieldMapping.setJiraStatusForDevelopment(jiraType);
	//
	// jiraType = new ArrayList<>();
	// jiraType.add("Ready for Testing");
	// fieldMapping.setJiraStatusForQa(jiraType);
	//
	// List<String> jiraSegData = new ArrayList<>();
	// jiraSegData.add("Tech Story");
	// jiraSegData.add("Task");
	//
	// jiraSegData = new ArrayList<>();
	// jiraSegData.add("Tech Story");
	// fieldMappingList.add(fieldMapping);
	//
	// // FieldMapping on 2nd project
	//
	// fieldMapping = new FieldMapping();
	// fieldMapping.setBasicProjectConfigId(new
	// ObjectId("5b719d06a500d00814bfb2b9"));
	// jiraType = new ArrayList<>();
	// jiraType.add("Defect");
	// fieldMapping.setJiradefecttype(jiraType);
	//
	// jiraIssueType = new String[] { "Support Request", "Incident", "Project
	// Request", "Member Account Request",
	// "TEST Consulting Request", "Test Case" };
	// fieldMapping.setJiraIssueTypeNames(jiraIssueType);
	// fieldMapping.setStoryFirstStatus("Open");
	//
	// fieldMapping.setRootCause("customfield_19121");
	//
	// fieldMapping.setJiraDefectRejectionStatus("Dropped");
	// fieldMapping.setJiraBugRaisedByIdentification("CustomField");
	//
	// jiraType = new ArrayList<>();
	// jiraType.add("Ready for Sign-off");
	// fieldMapping.setJiraDod(jiraType);
	//
	// jiraType = new ArrayList<>();
	// jiraType.add("Closed");
	// fieldMapping.setJiraDefectRemovalStatus(jiraType);
	//
	// jiraType = new ArrayList<>();
	// jiraType.add("40");
	//
	// fieldMapping.setJiraStoryPointsCustomField("customfield_56789");
	// fieldMapping.setJiraTechDebtIdentification("CustomField");
	//
	// jiraType = new ArrayList<>(Arrays.asList(new String[] { "Support Request",
	// "Incident", "Project Request",
	// "Member Account Request", "TEST Consulting Request", "Test Case" }));
	// fieldMapping.setTicketCountIssueType(jiraType);
	// fieldMapping.setEnvImpacted("customfield_13131");
	// fieldMapping.setJiraTicketVelocityIssueType(jiraType);
	// fieldMapping.setKanbanJiraTechDebtIssueType(jiraType);
	// fieldMapping.setKanbanCycleTimeIssueType(jiraType);
	//
	// jiraType = new ArrayList<>();
	// jiraType.add("Resolved");
	// fieldMapping.setTicketDeliverdStatus(jiraType);
	// fieldMapping.setJiraTicketResolvedStatus(jiraType);
	//
	// jiraType = new ArrayList<>();
	// jiraType.add("Reopen");
	// fieldMapping.setTicketReopenStatus(jiraType);
	//
	// jiraType = new ArrayList<>();
	// jiraType.add("Closed");
	// fieldMapping.setJiraTicketClosedStatus(jiraType);
	//
	// jiraType = new ArrayList<>();
	// jiraType.add("Assigned");
	// fieldMapping.setJiraTicketTriagedStatus(jiraType);
	//
	// fieldMapping.setJiraLiveStatus("Closed");
	// fieldMapping.setRootCauseValue(Arrays.asList("Coding", "None"));
	//
	// fieldMapping.setEpicName("customfield_14502");
	// jiraType = new ArrayList<>();
	// jiraType.add("Ready for Sign-off");
	// fieldMapping.setJiraDod(jiraType);
	//
	// jiraSegData = new ArrayList<>();
	// jiraSegData.add("Tech Story");
	// jiraSegData.add("Task");
	//
	// jiraSegData = new ArrayList<>();
	// jiraSegData.add("In Analysis");
	// jiraSegData.add("In Development");
	// fieldMapping.setJiraStatusForDevelopment(jiraSegData);
	//
	// jiraSegData = new ArrayList<>();
	// jiraSegData.add("Ready for Testing");
	// fieldMapping.setJiraStatusForQa(jiraSegData);
	// fieldMapping.setDevicePlatform("customfield_18181");
	//
	// jiraSegData = new ArrayList<>();
	// jiraSegData.add("segregationLabel");
	// fieldMappingList.add(fieldMapping);
	//
	// }

	private void createIssue() throws URISyntaxException, JSONException {
		BasicProject basicProj = new BasicProject(new URI("self"), "proj1", 1l, "project1");
		IssueType issueType1 = new IssueType(new URI("self"), 1l, "Story", false, "desc", new URI("iconURI"));
		IssueType issueType2 = new IssueType(new URI("self"), 2l, "Defect", false, "desc", new URI("iconURI"));
		Status status1 = new Status(new URI("self"), 1l, "Ready for Sprint Planning", "desc", new URI("iconURI"),
				new StatusCategory(new URI("self"), "name", 1l, "key", "colorname"));
		BasicPriority basicPriority = new BasicPriority(new URI("self"), 1l, "priority1");
		Resolution resolution = new Resolution(new URI("self"), 1l, "resolution", "resolution");
		Map<String, URI> avatarMap = new HashMap<>();
		avatarMap.put("48x48", new URI("value"));
		User user1 = new User(new URI("self"), "user1", "user1", "userAccount", "user1@xyz.com", true, null, avatarMap,
				null);
		Map<String, String> map = new HashMap<>();
		map.put("customfield_12121", "Client Testing (UAT)");
		map.put("self", "https://abc.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "Component");
		map.put("id", "20810");
		JSONObject value = new JSONObject(map);
		IssueField issueField = new IssueField("20810", "Component", null, value);
		List<IssueField> issueFields = Arrays.asList(issueField);
		Comment comment = new Comment(new URI("self"), "body", null, null, DateTime.now(), DateTime.now(),
				new Visibility(Visibility.Type.ROLE, "abc"), 1l);
		List<Comment> comments = Arrays.asList(comment);
		BasicVotes basicVotes = new BasicVotes(new URI("self"), 1, true);
		BasicUser basicUser = new BasicUser(new URI("self"), "basicuser", "basicuser", "accountId");
		Worklog worklog = new Worklog(new URI("self"), new URI("self"), basicUser, basicUser, null, DateTime.now(),
				DateTime.now(), DateTime.now(), 60, null);
		List<Worklog> workLogs = Arrays.asList(worklog);
		ChangelogItem changelogItem = new ChangelogItem(FieldType.JIRA, "Status", "from", "fromString", "to", "toString");
		ChangelogGroup changelogGroup = new ChangelogGroup(basicUser, DateTime.now(), Arrays.asList(changelogItem));
		changeLogList.add(changelogGroup);

		issue = new Issue("summary1", new URI("self"), "key1", 1l, basicProj, issueType1, status1, "story", basicPriority,
				resolution, new ArrayList<>(), user1, user1, DateTime.now(), DateTime.now(), DateTime.now(), new ArrayList<>(),
				null, new ArrayList<>(), null, issueFields, comments, null, createIssueLinkData(), basicVotes, workLogs, null,
				Arrays.asList("expandos"), null, Arrays.asList(changelogGroup), null, new HashSet<>(Arrays.asList("label1")));
		// Issue issue1 = new Issue("summary1", new URI("self"), "key1", 1l, basicProj,
		// issueType2, status1, "Defect",
		// basicPriority, resolution, new ArrayList<>(), user1, user1, DateTime.now(),
		// DateTime.now(),
		// DateTime.now(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
		// null, issueFields, comments,
		// null, createIssueLinkData(), basicVotes, workLogs, null,
		// Arrays.asList("expandos"), null,
		// Arrays.asList(changelogGroup), null, new HashSet<>(Arrays.asList("label1")));
		// issues.add(issue);
		// issues.add(issue1);
	}

	private List<IssueLink> createIssueLinkData() throws URISyntaxException {
		List<IssueLink> issueLinkList = new ArrayList<>();
		URI uri = new URI("https://abc.com/jira/rest/api/2/issue/12344");
		IssueLinkType linkType = new IssueLinkType("Blocks", "blocks", IssueLinkType.Direction.OUTBOUND);
		IssueLink issueLink = new IssueLink("IssueKey", uri, linkType);
		issueLinkList.add(issueLink);

		return issueLinkList;
	}
}
