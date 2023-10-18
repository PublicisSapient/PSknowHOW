package com.publicissapient.kpidashboard.jira.client.jiraissue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.ObjectUtils;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.atlassian.jira.rest.client.api.ExpandableProperty;
import com.atlassian.jira.rest.client.api.domain.BasicUser;
import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.ChangelogItem;
import com.atlassian.jira.rest.client.api.domain.FieldType;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.Priority;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.User;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.jira.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

@ExtendWith(SpringExtension.class)
public class HandleJiraHistoryTest {

	Map<String, IssueField> fields = new HashMap<>();
	@InjectMocks
	private HandleJiraHistory handleJiraHistory;
	@Mock
	private JiraIssueCustomHistory jiraIssueCustomHistory;
	@Mock
	private FieldMapping fieldMapping;
	private List<ChangelogGroup> changeLogList = new ArrayList<>();
	private Issue issue;

	private ProjectConfFieldMapping	projectConfFieldMapping;

	@BeforeEach
	public void setUp() throws URISyntaxException {

		jiraIssueCustomHistory = new JiraIssueCustomHistory();
		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/field_mapping.json");
		fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);

		projectConfFieldMapping =  getProjectConfFieldMapping(fieldMapping);

		ChangelogGroup changelogGroup;
		changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""),
				new DateTime("2023-02-28T03:57:59.000+0000"), Arrays.asList(new ChangelogItem(FieldType.JIRA, "status",
						"10003", "In Development", "15752", "Code Review")));
		changeLogList.add(changelogGroup);
		changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""),
				new DateTime("2023-02-28T03:57:59.000+0000"),
				Arrays.asList(new ChangelogItem(FieldType.JIRA, "priority", "10003", "P1", "15752", "P2")));
		changeLogList.add(changelogGroup);
		changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""),
				new DateTime("2023-02-28T03:57:59.000+0000"),
				Arrays.asList(new ChangelogItem(FieldType.JIRA, "assignee", "10003", "Harsh", "15752", "Akshat")));
		changeLogList.add(changelogGroup);
		changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""),
				new DateTime("2023-02-28T03:57:59.000+0000"), Arrays.asList(new ChangelogItem(FieldType.JIRA,
						"fix version", "10003", "KnowHOW v6.7.0", "15752", "KnowHOW v6.8.0")));
		changeLogList.add(changelogGroup);
		changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""),
				new DateTime("2023-02-28T03:57:59.000+0000"), Arrays.asList(new ChangelogItem(FieldType.JIRA,
						"fix version", "10003", "KnowHOW v6.8.0", "15752", "KnowHOW v6.9.0")));
		changeLogList.add(changelogGroup);
		changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""),
				new DateTime("2023-02-28T03:59:59.000+0000"), Arrays.asList(new ChangelogItem(FieldType.JIRA,
						"fix version", "10003", "KnowHOW v6.7.0", "15752", "KnowHOW v6.8.0")));
		changeLogList.add(changelogGroup);
		changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""),
				new DateTime("2023-02-28T03:57:59.000+0000"),
				Arrays.asList(new ChangelogItem(FieldType.JIRA, "Labels", "10003", "L1", "15752", "L2")));
		changeLogList.add(changelogGroup);
		changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""),
				new DateTime("2023-02-28T03:57:59.000+0000"), Arrays.asList(new ChangelogItem(FieldType.CUSTOM,
						"Due Date", "2023-02-21", "2023-02-21 00:00:00.0", "2023-02-24", "2023-02-24 00:00:00.0")));
		changeLogList.add(changelogGroup);
		changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""),
				new DateTime("2023-02-28T03:57:59.000+0000"),
				Arrays.asList(new ChangelogItem(FieldType.CUSTOM, "Sprint", "10003",
						"KnowHOW | PI_12| ITR_4, KnowHOW | PI_12| ITR_5", "15752",
						"KnowHOW | PI_12| ITR_5, KnowHOW | PI_12| ITR_6")));
		changeLogList.add(changelogGroup);

		fields.put("customfield_11528", new IssueField("", "Due Date", "", null));
		fields.put("customfield_12700", new IssueField("", "Sprint", "", Arrays.asList(
				"com.atlassian.greenhopper.service.sprint.Sprint@6fc7072e[id=23356,rapidViewId=11649,state=CLOSED,name=TEST | 06 Jan - 19 Jan,startDate=2020-01-06T11:38:31.937Z,endDate=2020-01-19T11:38:00.000Z,completeDate=2020-01-20T11:15:21.528Z,sequence=22778,goal=]",
				"com.atlassian.greenhopper.service.sprint.Sprint@6fc7072e[id=23356,rapidViewId=11649,state=CLOSED,name=TEST | 06 Jan - 19 Jan,startDate=2020-01-06T11:38:31.937Z,endDate=2020-01-19T11:38:00.000Z,completeDate=2020-01-20T11:15:21.528Z,sequence=22778,goal=]")));

		Map<String, URI> avatarUris = new HashMap<>();
		avatarUris.put("48x48", URI.create(""));
		issue = new Issue("summary", null, "key", 121L, null, null, new Status(null, null, "Open", null, null, null),
				"description", new Priority(null, null, "P4-Minor", null, null, null), null, null, null,
				new User(null, null, "Harsh Gupta", "", false, new ExpandableProperty<>(Collections.singleton("")),
						avatarUris, "1"),
				DateTime.now(), DateTime.now(), DateTime.now(), null, new ArrayList<>(), null, null, null, null, null,
				null, null, null, null, null, null, null, null, new HashSet<>());
	}

	@Test
	public void testSetJiraFieldChangeLog1() {

		handleJiraHistory.setJiraIssueCustomHistoryUpdationLog(jiraIssueCustomHistory, changeLogList, projectConfFieldMapping,
				fields, issue);
		Assert.assertEquals(jiraIssueCustomHistory.getStatusUpdationLog().size(), 2);
		Assert.assertEquals(jiraIssueCustomHistory.getAssigneeUpdationLog().size(), 2);
		Assert.assertEquals(jiraIssueCustomHistory.getLabelUpdationLog().size(), 2);
		Assert.assertEquals(jiraIssueCustomHistory.getFixVersionUpdationLog().size(), 3);
		Assert.assertEquals(jiraIssueCustomHistory.getPriorityUpdationLog().size(), 2);
		Assert.assertEquals(jiraIssueCustomHistory.getSprintUpdationLog().size(), 1);
		Assert.assertEquals(jiraIssueCustomHistory.getDueDateUpdationLog().size(), 1);

	}

	@Test
	public void testSetJiraFieldChangeLog2() {
		if (ObjectUtils.isNotEmpty(changeLogList))
			changeLogList.clear();
		handleJiraHistory.setJiraIssueCustomHistoryUpdationLog(jiraIssueCustomHistory, changeLogList, projectConfFieldMapping,
				fields, issue);
		Assert.assertEquals(jiraIssueCustomHistory.getStatusUpdationLog().size(), 1);
		Assert.assertEquals(jiraIssueCustomHistory.getAssigneeUpdationLog().size(), 1);
		Assert.assertEquals(jiraIssueCustomHistory.getLabelUpdationLog().size(), 0);
		Assert.assertEquals(jiraIssueCustomHistory.getFixVersionUpdationLog().size(), 0);
		Assert.assertEquals(jiraIssueCustomHistory.getPriorityUpdationLog().size(), 1);
		Assert.assertEquals(jiraIssueCustomHistory.getDueDateUpdationLog().size(), 0);
		Assert.assertEquals(jiraIssueCustomHistory.getSprintUpdationLog().size(), 0);
	}

	@Test
	public void testJiraDueDateChangeLog() throws URISyntaxException {
		fieldMapping.setJiraDueDateCustomField("");
		fieldMapping.setJiraDueDateField("Due Date");
		ChangelogGroup changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""),
				new DateTime("2023-02-28T03:57:59.000+0000"), Arrays.asList(new ChangelogItem(FieldType.JIRA, "dueDate",
						"2023-02-21", "2023-02-21 00:00:00.0", "2023-02-24", "2023-02-24 00:00:00.0")));
		changeLogList.add(changelogGroup);
		handleJiraHistory.setJiraIssueCustomHistoryUpdationLog(jiraIssueCustomHistory, changeLogList, projectConfFieldMapping,
				fields, issue);
		Assert.assertEquals(jiraIssueCustomHistory.getStatusUpdationLog().size(), 2);
		Assert.assertEquals(jiraIssueCustomHistory.getAssigneeUpdationLog().size(), 2);
		Assert.assertEquals(jiraIssueCustomHistory.getLabelUpdationLog().size(), 2);
		Assert.assertEquals(jiraIssueCustomHistory.getFixVersionUpdationLog().size(), 3);
		Assert.assertEquals(jiraIssueCustomHistory.getPriorityUpdationLog().size(), 2);
		Assert.assertEquals(jiraIssueCustomHistory.getSprintUpdationLog().size(), 1);
		Assert.assertEquals(jiraIssueCustomHistory.getDueDateUpdationLog().size(), 2);
	}

	private static ProjectConfFieldMapping getProjectConfFieldMapping(FieldMapping fieldMapping) {
		ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
		projectConfFieldMapping.setJira(getJiraToolConfig(fieldMapping));
		projectConfFieldMapping.setFieldMapping(fieldMapping);
		projectConfFieldMapping.setBasicProjectConfigId(new ObjectId("632eb205e0fd283f9bb747ad"));
		projectConfFieldMapping.setIssueCount(2);
		projectConfFieldMapping.setKanban(true);
		projectConfFieldMapping.setSprintCount(12);
		projectConfFieldMapping.setProjectName("TestHOW");
		projectConfFieldMapping.setJiraToolConfigId(new ObjectId("632eb205e0fd283f9bb747ad"));
		projectConfFieldMapping.setBasicProjectConfigId(new ObjectId("632eb205e0fd283f9bb747ad"));
		return projectConfFieldMapping;
	}

	private static JiraToolConfig getJiraToolConfig(FieldMapping fieldMapping) {
		JiraToolConfig jiraToolConfig = new JiraToolConfig();
		jiraToolConfig.setBasicProjectConfigId("632eb205e0fd283f9bb747ad");
		jiraToolConfig.setProjectId("123");
		jiraToolConfig.setProjectKey("123");
		jiraToolConfig.setFieldMapping(fieldMapping);
		jiraToolConfig.setCreatedAt("2021-07-26T10:22:12.0000000");
		jiraToolConfig.setUpdatedAt("2021-07-26T10:22:12.0000000");
		jiraToolConfig.setQueryEnabled(false);
		jiraToolConfig.setBoardQuery("query");
		Connection connection = getConnectionObject();
		jiraToolConfig.setConnection(Optional.of(connection));
		BoardDetails boardDetails = new BoardDetails();
		boardDetails.setBoardName("TestHOW");
		boardDetails.setBoardId("123");
		ArrayList<BoardDetails> al = new ArrayList<>();
		al.add(boardDetails);
		jiraToolConfig.setBoards(al);
		return jiraToolConfig;
	}

	private static Connection getConnectionObject() {
		Connection connection = new Connection();
		connection.setType("Defect");
		connection.setConnectionName("TEST");
		connection.setCloudEnv(true);
		connection.setBaseUrl("url");
		connection.setUsername("test");
		connection.setPassword("testPassword");
		connection.setApiEndPoint("url");
		connection.setConsumerKey("123");
		connection.setPrivateKey("123");
		connection.setApiKey("123");
		connection.setClientSecretKey("999");
		connection.setIsOAuth(true);
		connection.setClientId("111");
		connection.setTenantId("111");
		connection.setPat("pat");
		connection.setApiKeyFieldName("apiKey");
		connection.setAccessToken("accessToken");
		connection.setOffline(true);
		connection.setOfflineFilePath("offlineFilePath");
		connection.setCreatedAt("now");
		connection.setUpdatedAt("later");
		connection.setUpdatedBy("TestHOW");
		connection.setConnPrivate(true);
		connection.setUpdatedBy("TestHOW");
		ArrayList<String> alStrings = new ArrayList<>();
		alStrings.add("TestHOW");
		connection.setConnectionUsers(alStrings);
		return connection;
	}
}
