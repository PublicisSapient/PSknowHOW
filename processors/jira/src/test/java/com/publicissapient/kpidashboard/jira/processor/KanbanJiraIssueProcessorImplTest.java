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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONArray;
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
import org.springframework.beans.BeanUtils;

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
import com.atlassian.jira.rest.client.api.domain.TimeTracking;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.api.domain.Visibility;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilter;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.Assignee;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.dataFactories.ConnectionsDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.ProjectBasicConfigDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.ToolConfigDataFactory;
import com.publicissapient.kpidashboard.jira.helper.AdditionalFilterHelper;
import com.publicissapient.kpidashboard.jira.model.JiraProcessor;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.repository.JiraProcessorRepository;
import com.publicissapient.kpidashboard.jira.service.JiraCommonService;

@RunWith(MockitoJUnitRunner.class)
public class KanbanJiraIssueProcessorImplTest {

	@Mock
	FieldMapping fieldMapping;
	@InjectMocks
	KanbanJiraIssueProcessorImpl transformFetchedIssueToKanbanJiraIssue;
	List<Issue> issues = new ArrayList<>();
	ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
	@Mock
	JiraProcessor jiraProcessor;
	List<ProjectBasicConfig> projectConfigsList;
	List<ProjectToolConfig> projectToolConfigs;
	Optional<Connection> connection;
	List<FieldMapping> fieldMappingList;
	@Mock
	private KanbanJiraIssueRepository kanbanJiraRepo;
	@Mock
	private JiraProcessorRepository jiraProcessorRepository;
	@Mock
	private JiraProcessorConfig jiraProcessorConfig;
	@Mock
	private AdditionalFilterHelper additionalFilterHelper;
	@Mock
	private JiraCommonService jiraCommonService;
	@Mock
	private AssigneeDetailsRepository assigneeDetailsRepository;

	@Mock
	private AssigneeDetails assigneeDetails;

	@Mock
	private IssueField issueField;

	@Mock
	private KanbanJiraIssue jiraIssue;

	private Map<String, IssueField> fields;

	Set<Assignee> assigneeSetToSave = new HashSet<>();

	@Before
	public void setup() throws URISyntaxException, JSONException {
		issueField = mock(IssueField.class);
		fieldMapping = getMockFieldMapping();
		projectConfigsList = getMockProjectConfig();
		projectToolConfigs = getMockProjectToolConfig();
		connection = getMockConnection();
		fieldMappingList = getMockFieldMappingList();
		Assignee assignee = Assignee.builder().assigneeId("2424").assigneeName("User 1").build();
		Assignee assignee1 = Assignee.builder().assigneeId("24324").assigneeName("User 2").build();
		assigneeSetToSave.add(assignee);
		assigneeSetToSave.add(assignee1);
		AssigneeDetails assigneeDetailsToBeSave = new AssigneeDetails("63c04dc7b7617e260763ca4e", ProcessorConstants.JIRA,
				assigneeSetToSave, 3);
		when(assigneeDetails.getBasicProjectConfigId()).thenReturn("63c04dc7b7617e260763ca4e");
		when(assigneeDetailsRepository.findByBasicProjectConfigIdAndSource(any(), any()))
				.thenReturn(assigneeDetailsToBeSave);
		createProjectConfigMap();
		createIssue();
		fields = new HashMap<>();
	}

	@Test
	public void convertToJiraIssue() throws JSONException {
		when(jiraProcessorConfig.getJiraDirectTicketLinkKey()).thenReturn("browse/");
		when(kanbanJiraRepo.findByIssueIdAndBasicProjectConfigId(any(), any()))
				.thenReturn(new KanbanJiraIssue());
		//		when(jiraProcessorConfig.getRcaValuesForCodeIssue()).thenReturn(Arrays.asList("code",
		// "coding"));
		when(additionalFilterHelper.getAdditionalFilter(any(), any()))
				.thenReturn(getMockAdditionalFilterFromJiraIssue());
		Assert.assertEquals(
				KanbanJiraIssue.class,
				(transformFetchedIssueToKanbanJiraIssue.convertToKanbanJiraIssue(
								issues.get(0),
								projectConfFieldMapping,
								"111",
								new ObjectId("5e16c126e4b098db673cc372")))
						.getClass());
	}

	@Test
	public void convertToJiraIssueWhenException() throws JSONException {
		Assert.assertEquals(null, (transformFetchedIssueToKanbanJiraIssue.convertToKanbanJiraIssue(null,
				projectConfFieldMapping, "111", new ObjectId("5e16c126e4b098db673cc372"))));
	}

	@Test
	public void updateAssigneeDetailsToggleWise() {
		transformFetchedIssueToKanbanJiraIssue.updateAssigneeDetailsToggleWise(new KanbanJiraIssue(),
				projectConfFieldMapping, Arrays.asList("1234"), Arrays.asList("username"), Arrays.asList("username"));
	}

	private Optional<Connection> getMockConnection() {
		ConnectionsDataFactory connectionDataFactory = ConnectionsDataFactory.newInstance("/json/default/connections.json");
		return connectionDataFactory.findConnectionById("5fd99f7bc8b51a7b55aec836");
	}

	private List<ProjectToolConfig> getMockProjectToolConfig() {
		ToolConfigDataFactory projectToolConfigDataFactory = ToolConfigDataFactory
				.newInstance("/json/default/project_tool_configs.json");
		return projectToolConfigDataFactory.findByToolNameAndBasicProjectConfigId(ProcessorConstants.JIRA,
				"63c04dc7b7617e260763ca4e");
	}

	private List<AdditionalFilter> getMockAdditionalFilterFromJiraIssue() {
		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance("/json/default/jira_issues.json");
		return jiraIssueDataFactory.getAdditionalFilter();
	}

	private List<ProjectBasicConfig> getMockProjectConfig() {
		ProjectBasicConfigDataFactory projectConfigDataFactory = ProjectBasicConfigDataFactory
				.newInstance("/json/default/project_basic_configs.json");
		return projectConfigDataFactory.getProjectBasicConfigs();
	}

	private FieldMapping getMockFieldMapping() {
		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/kanban_project_field_mappings.json");
		return fieldMappingDataFactory.findByBasicProjectConfigId("6335368249794a18e8a4479f");
	}

	private void createIssue() throws URISyntaxException, JSONException {
		BasicProject basicProj = new BasicProject(new URI("self"), "proj1", 1l, "project1");
		IssueType issueType1 = new IssueType(new URI("self"), 1l, "Story", false, "desc", new URI("iconURI"));
		IssueType issueType2 = new IssueType(new URI("self"), 2l, "Defect", false, "desc", new URI("iconURI"));
		IssueType issueType3 = new IssueType(new URI("self"), 3l, "Epic", false, "desc", new URI("iconURI"));

		Status status1 = new Status(new URI("self"), 1l, "Ready for Sprint Planning", "desc", new URI("iconURI"),
				new StatusCategory(new URI("self"), "name", 1l, "key", "colorname"));
		BasicPriority basicPriority = new BasicPriority(new URI("self"), 1l, "priority1");
		Resolution resolution = new Resolution(new URI("self"), 1l, "resolution", "resolution");
		Map<String, URI> avatarMap = new HashMap<>();
		avatarMap.put("48x48", new URI("value"));
		URI uri = new URI("https://dummy.com/jira/rest/api/2/user?username=user1");
		User user1 = new User(uri, "user1", "user1", "userAccount", "user1@xyz.com", true, null, avatarMap, null);

		List<IssueField> issueFields = new ArrayList<>();

		Map<String, String> map = new HashMap<>();
		map.put("customfield_19121", "Client Testing (UAT)");
		map.put("self", "https://jiradomain.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "Component");
		map.put("id", "20810");
		JSONObject value = new JSONObject(map);
		IssueField issueField = new IssueField("customfield_19121", "Component", null, value);
		issueFields.add(issueField);

		map = new HashMap<>();
		map.put("self", "https://jiradomain.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "Epic");
		map.put("id", "18182");
		IssueField issueField1 = new IssueField("customfield_14502", "Epic Name", null, new JSONObject(map));
		issueFields.add(issueField1);

		IssueField issueField2 = new IssueField("customfield_20803", "StoryPoints", null, Integer.parseInt("5"));
		issueFields.add(issueField2);

		map = new HashMap<>();
		map.put("self", "https://jiradomain.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "Status");
		map.put("id", "13131");
		IssueField issueField3 = new IssueField("customfield_13131", "Status", null, new JSONObject(map));
		issueFields.add(issueField3);

		IssueField issueField4 = new IssueField("aggregatetimespent", "aggregatetimespent", null, 300);
		issueFields.add(issueField4);

		IssueField issueField5 = new IssueField("epicLick", "epicLick", null, "epic");
		issueFields.add(issueField5);

		TimeTracking timeTracking = new TimeTracking(8, 8, 8);

		Comment comment = new Comment(new URI("self"), "body", null, null, DateTime.now(), DateTime.now(),
				new Visibility(Visibility.Type.ROLE, "abc"), 1l);
		List<Comment> comments = Arrays.asList(comment);
		BasicVotes basicVotes = new BasicVotes(new URI("self"), 1, true);
		BasicUser basicUser = new BasicUser(new URI("self"), "basicuser", "basicuser", "accountId");
		Worklog worklog = new Worklog(new URI("self"), new URI("self"), basicUser, basicUser, null, DateTime.now(),
				DateTime.now(), DateTime.now(), 60, null);
		List<Worklog> workLogs = Arrays.asList(worklog);
		ChangelogItem changelogItem = new ChangelogItem(FieldType.JIRA, "field1", "from", "fromString", "to", "toString");
		ChangelogGroup changelogGroup = new ChangelogGroup(basicUser, DateTime.now(), Arrays.asList(changelogItem));

		Collection<Version> fixVersions = new ArrayList<>();
		Version version = new Version(new URI("https://dummy.com/jira/rest/api/2/version/143417"), 143417L, "",
				"KnowHOW v6.8.0", false, true, DateTime.now());
		fixVersions.add(version);

		Issue issue = new Issue("summary1", new URI("self"), "key1", 1l, basicProj, issueType2, status1, "story",
				basicPriority, resolution, new ArrayList<>(), user1, user1, DateTime.now(), DateTime.now(), DateTime.now(),
				new ArrayList<>(), fixVersions, new ArrayList<>(), timeTracking, issueFields, comments, null,
				createIssueLinkData(), basicVotes, workLogs, null, Arrays.asList("expandos"), null,
				Arrays.asList(changelogGroup), null, new HashSet<>(Arrays.asList("label1")));

		Issue issue1 = new Issue("summary1", new URI("self"), "key2", 1l, basicProj, issueType3, status1, "epic",
				basicPriority, resolution, new ArrayList<>(), user1, user1, DateTime.now(), DateTime.now(), DateTime.now(),
				new ArrayList<>(), fixVersions, new ArrayList<>(), timeTracking, issueFields, comments, null,
				createIssueLinkData(), basicVotes, workLogs, null, Arrays.asList("expandos"), null,
				Arrays.asList(changelogGroup), null, new HashSet<>(Arrays.asList("label2")));
		issues.add(issue);
		issues.add(issue1);
	}

	private List<IssueLink> createIssueLinkData() throws URISyntaxException {
		List<IssueLink> issueLinkList = new ArrayList<>();
		URI uri = new URI("https://testDomain.com/jira/rest/api/2/issue/12344");
		IssueLinkType linkType = new IssueLinkType("Blocks", "blocks", IssueLinkType.Direction.OUTBOUND);
		IssueLink issueLink = new IssueLink("IssueKey", uri, linkType);
		issueLinkList.add(issueLink);

		return issueLinkList;
	}

	private void createProjectConfigMap() {
		ProjectBasicConfig projectConfig = projectConfigsList.get(0);
		BeanUtils.copyProperties(projectConfig, projectConfFieldMapping);
		projectConfFieldMapping.setProjectBasicConfig(projectConfig);
		projectConfFieldMapping.setKanban(projectConfig.getIsKanban());
		projectConfFieldMapping.setBasicProjectConfigId(projectConfig.getId());
		projectConfFieldMapping.setJira(getJiraToolConfig());
		projectConfFieldMapping.setJiraToolConfigId(projectToolConfigs.get(0).getId());
		projectConfFieldMapping.setFieldMapping(fieldMapping);
	}

	private JiraToolConfig getJiraToolConfig() {
		JiraToolConfig toolObj = new JiraToolConfig();
		BeanUtils.copyProperties(projectToolConfigs.get(0), toolObj);
		toolObj.setConnection(connection);
		return toolObj;
	}

	private List<FieldMapping> getMockFieldMappingList() {
		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/field_mapping.json");
		return fieldMappingDataFactory.getFieldMappings();
	}

	@Test
	public void testSetEpicIssueData() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		// Arrange
		FieldMapping fieldMapping = new FieldMapping(); // Set up your FieldMapping instance
		fieldMapping.setEpicJobSize("8.0");
		fieldMapping.setEpicRiskReduction("8.0");
		fieldMapping.setEpicTimeCriticality("8.0");
		fieldMapping.setEpicUserBusinessValue("8.0");
		fieldMapping.setEpicWsjf("8.0");
		fieldMapping.setEpicPlannedValue("8.0");
		fieldMapping.setEpicAchievedValue("8.0");
		fieldMapping.setEpicName("customfield_14502");

		KanbanJiraIssue jiraIssue = new KanbanJiraIssue(); // Set up your JiraIssue instance
		jiraIssue.setBusinessValue(8.0);
		jiraIssue.setRiskReduction(8.0);
		jiraIssue.setTimeCriticality(8.0);
		Map<String, IssueField> fields = new HashMap<>();
		fields.put("8.0", new IssueField("", "8.0", null, "8.0"));
		fields.put("8.0", new IssueField("", "8.0", null, "8.0"));
		fields.put("8.0", new IssueField("", "8.0", null, "8.0"));
		fields.put("8.0", new IssueField("", "8.0", null, "8.0"));
		fields.put("8.0", new IssueField("", "8.0", null, "8.0"));
		fields.put("8.0", new IssueField("", "8.0", null, "8.0"));
		fields.put("8.0", new IssueField("", "8.0", null, "8.0"));

		// Use reflection to access the private method
		Method method = KanbanJiraIssueProcessorImpl.class.getDeclaredMethod("setEpicIssueData", FieldMapping.class,
				KanbanJiraIssue.class, Map.class);
		method.setAccessible(true);

		// Act
		method.invoke(transformFetchedIssueToKanbanJiraIssue, fieldMapping, jiraIssue, fields);
	}

	@Test
	public void testSetStoryLinkWithDefect()
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		when(jiraProcessorConfig.getExcludeLinks()).thenReturn(Arrays.asList("Blocks"));
		KanbanJiraIssue jiraIssue = new KanbanJiraIssue();
		jiraIssue.setTypeName("Bug");
		Method method =
				KanbanJiraIssueProcessorImpl.class.getDeclaredMethod(
						"setStoryLinkWithDefect", Issue.class, KanbanJiraIssue.class);
		method.setAccessible(true);
		method.invoke(transformFetchedIssueToKanbanJiraIssue, issues.get(0), jiraIssue);
	}

	@Test
	public void testSetJiraAssigneeDetailsWhenUserIsNull()
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Method method = KanbanJiraIssueProcessorImpl.class.getDeclaredMethod("setJiraAssigneeDetails",
				KanbanJiraIssue.class, User.class, ProjectConfFieldMapping.class);
		method.setAccessible(true);
		method.invoke(transformFetchedIssueToKanbanJiraIssue, new KanbanJiraIssue(), null, projectConfFieldMapping);
	}

	@Test
	public void testGetRootCauses()
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, JSONException {
		Method method = KanbanJiraIssueProcessorImpl.class.getDeclaredMethod("getRootCauses", FieldMapping.class,
				Map.class);
		method.setAccessible(true);
		FieldMapping fieldMapping = new FieldMapping();
		fieldMapping.setRootCause("code_issue");
		Map<String, String> map = new HashMap<>();
		map.put("self", "https://jiradomain.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "code");
		map.put("id", "19121");
		JSONObject jsonObject = new JSONObject(map);
		List<Object> rcaList = new ArrayList<>();
		rcaList.add(jsonObject);
		IssueField issueField = new IssueField("customfield_19121", "code_issue", null, new JSONArray(rcaList));
		Map<String, IssueField> fields = new HashMap<>();
		fields.put("code_issue", issueField);

		method.invoke(transformFetchedIssueToKanbanJiraIssue, fieldMapping, fields);
	}

	@Test
	public void testSetAssigneeName()
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		when(assigneeDetailsRepository.findByBasicProjectConfigIdAndSource(any(), any()))
				.thenReturn(null);
		Method method =
				KanbanJiraIssueProcessorImpl.class.getDeclaredMethod(
						"setAssigneeName", String.class, String.class);
		method.setAccessible(true);
		method.invoke(transformFetchedIssueToKanbanJiraIssue, "assigneeId", "basicProjectConfigId");
	}

	@Test
	public void setEpicLinkedTest()
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, JSONException {
		KanbanJiraIssue jiraIssue = new KanbanJiraIssue();
		FieldMapping fieldMapping = new FieldMapping();
		fieldMapping.setRootCause("code_issue");
		fieldMapping.setEpicLink("Epic123");
		Map<String, String> map = new HashMap<>();
		map.put("self", "https://jiradomain.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "code");
		map.put("id", "19121");
		JSONObject jsonObject = new JSONObject(map);
		List<Object> rcaList = new ArrayList<>();
		rcaList.add(jsonObject);
		IssueField issueField = new IssueField("customfield_19121", "code_issue", null, new JSONArray(rcaList));
		Map<String, IssueField> fields = new HashMap<>();
		fields.put("code_issue", issueField);
		fields.put("Epic123", issueField);
		Method method = KanbanJiraIssueProcessorImpl.class.getDeclaredMethod("setEpicLinked", FieldMapping.class,
				KanbanJiraIssue.class, Map.class);
		method.setAccessible(true);
		method.invoke(transformFetchedIssueToKanbanJiraIssue, fieldMapping, jiraIssue, fields);
	}

	@Test
	public void testCalculateEstimationForActualEstimation() throws Exception {
		when(issueField.getValue()).thenReturn(7200);

		Method method =
				KanbanJiraIssueProcessorImpl.class.getDeclaredMethod(
						"calculateEstimation", IssueField.class, String.class);
		method.setAccessible(true);

		Double estimation =
				(Double)
						method.invoke(
								transformFetchedIssueToKanbanJiraIssue,
								issueField,
								JiraConstants.ACTUAL_ESTIMATION);
		Assert.assertEquals(2.0, estimation, 0.001);
	}

	@Test
	public void testCalculateEstimationForBufferedEstimation() throws Exception {
		when(issueField.getValue()).thenReturn(3600);

		Method method =
				KanbanJiraIssueProcessorImpl.class.getDeclaredMethod(
						"calculateEstimation", IssueField.class, String.class);
		method.setAccessible(true);

		Double estimation =
				(Double)
						method.invoke(
								transformFetchedIssueToKanbanJiraIssue,
								issueField,
								JiraConstants.BUFFERED_ESTIMATION);
		Assert.assertEquals(1.0, estimation, 0.001);
	}

	@Test
	public void testCalculateEstimationForStoryPoint() throws Exception {
		when(issueField.getValue()).thenReturn("3.0");

		Method method =
				KanbanJiraIssueProcessorImpl.class.getDeclaredMethod(
						"calculateEstimation", IssueField.class, String.class);
		method.setAccessible(true);

		Double estimation =
				(Double)
						method.invoke(
								transformFetchedIssueToKanbanJiraIssue, issueField, JiraConstants.STORY_POINT);
		Assert.assertEquals(3.0, estimation, 0.001);
	}

	@Test
	public void testCalculateEstimationForUnknownCriteria() throws Exception {
		Method method = KanbanJiraIssueProcessorImpl.class.getDeclaredMethod("calculateEstimation", IssueField.class,
				String.class);
		method.setAccessible(true);

		Double estimation = (Double) method.invoke(transformFetchedIssueToKanbanJiraIssue, issueField, "UnknownCriteria");
		Assert.assertEquals(0.0, estimation, 0.001);
	}

	@Test
	public void testSetEpicLinked_StringEpicLink() throws Exception {
		// Arrange
		FieldMapping fieldMapping = mock(FieldMapping.class);
		KanbanJiraIssue jiraIssue = mock(KanbanJiraIssue.class);
		Map<String, IssueField> fields = new HashMap<>();
		fields.put("epicLinkField", new IssueField("epicLinkField", "Epic Link", null, "EPIC-123"));

		when(fieldMapping.getEpicLink()).thenReturn("epicLinkField");

		// Act
		Method method = KanbanJiraIssueProcessorImpl.class.getDeclaredMethod("setEpicLinked", FieldMapping.class,
				KanbanJiraIssue.class, Map.class);
		method.setAccessible(true);
		method.invoke(transformFetchedIssueToKanbanJiraIssue, fieldMapping, jiraIssue, fields);

		// Assert
		verify(jiraIssue).setEpicLinked("EPIC-123");
	}

	@Test
	public void testSetEpicLinked_JSONObjectEpicLinkWithKey() throws Exception {
		// Arrange
		FieldMapping fieldMapping = mock(FieldMapping.class);
		KanbanJiraIssue jiraIssue = mock(KanbanJiraIssue.class);
		Map<String, IssueField> fields = new HashMap<>();
		fields.put("epicLinkField", new IssueField("epicLinkField", "Epic Link", null, "EPIC-123"));

		when(fieldMapping.getEpicLink()).thenReturn("epicLinkField");
		JSONObject epicLinkJson = new JSONObject();
		epicLinkJson.put("key", "EPIC-123");
		fields.put("epicLinkField", new IssueField("epicLinkField", "Epic Link", null, epicLinkJson));

		Method method = KanbanJiraIssueProcessorImpl.class.getDeclaredMethod("setEpicLinked", FieldMapping.class,
				KanbanJiraIssue.class, Map.class);
		method.setAccessible(true);
		method.invoke(transformFetchedIssueToKanbanJiraIssue, fieldMapping, jiraIssue, fields);

		verify(jiraIssue).setEpicLinked("EPIC-123");
	}

	@Test
	public void testSetEpicLinked_JSONObjectEpicLinkWithoutKey() throws Exception {
		// Arrange
		FieldMapping fieldMapping = mock(FieldMapping.class);
		KanbanJiraIssue jiraIssue = mock(KanbanJiraIssue.class);
		Map<String, IssueField> fields = new HashMap<>();
		fields.put("epicLinkField", new IssueField("epicLinkField", "Epic Link", null, "EPIC-123"));

		when(fieldMapping.getEpicLink()).thenReturn("epicLinkField");
		JSONObject epicLinkJson = new JSONObject();
		fields.put("epicLinkField", new IssueField("epicLinkField", "Epic Link", null, epicLinkJson));

		Method method = KanbanJiraIssueProcessorImpl.class.getDeclaredMethod("setEpicLinked", FieldMapping.class,
				KanbanJiraIssue.class, Map.class);
		method.setAccessible(true);
		method.invoke(transformFetchedIssueToKanbanJiraIssue, fieldMapping, jiraIssue, fields);

		verify(jiraIssue, never()).setEpicLinked(anyString());
	}

	@Test
	public void testSetEpicLinked_NullEpicLink() throws Exception {
		// Arrange
		FieldMapping fieldMapping = mock(FieldMapping.class);
		KanbanJiraIssue jiraIssue = mock(KanbanJiraIssue.class);
		Map<String, IssueField> fields = new HashMap<>();
		fields.put("epicLinkField", new IssueField("epicLinkField", "Epic Link", null, "EPIC-123"));

		when(fieldMapping.getEpicLink()).thenReturn("epicLinkField");
		fields.put("epicLinkField", new IssueField("epicLinkField", "Epic Link", null, null));

		Method method = KanbanJiraIssueProcessorImpl.class.getDeclaredMethod("setEpicLinked", FieldMapping.class,
				KanbanJiraIssue.class, Map.class);
		method.setAccessible(true);
		method.invoke(transformFetchedIssueToKanbanJiraIssue, fieldMapping, jiraIssue, fields);

		verify(jiraIssue, never()).setEpicLinked(anyString());
	}

	@Test
	public void testSetEpicLinked_FieldNotPresent() throws Exception {
		// Arrange
		FieldMapping fieldMapping = mock(FieldMapping.class);
		KanbanJiraIssue jiraIssue = mock(KanbanJiraIssue.class);
		Map<String, IssueField> fields = new HashMap<>();

		when(fieldMapping.getEpicLink()).thenReturn("epicLinkField");

		// Act
		Method method = KanbanJiraIssueProcessorImpl.class.getDeclaredMethod("setEpicLinked", FieldMapping.class,
				KanbanJiraIssue.class, Map.class);
		method.setAccessible(true);
		method.invoke(transformFetchedIssueToKanbanJiraIssue, fieldMapping, jiraIssue, fields);

		// Assert
		verify(jiraIssue, never()).setEpicLinked(anyString());
	}
}
