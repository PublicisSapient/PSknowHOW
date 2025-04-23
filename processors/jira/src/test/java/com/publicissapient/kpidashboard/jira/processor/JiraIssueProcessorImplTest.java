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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.BeanUtils;

import com.atlassian.jira.rest.client.api.StatusCategory;
import com.atlassian.jira.rest.client.api.domain.BasicComponent;
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
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilter;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.Assignee;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.dataFactories.ConnectionsDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.ProjectBasicConfigDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.ToolConfigDataFactory;
import com.publicissapient.kpidashboard.jira.helper.AdditionalFilterHelper;
import com.publicissapient.kpidashboard.jira.model.JiraProcessor;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.repository.JiraProcessorRepository;

@RunWith(MockitoJUnitRunner.class)
public class JiraIssueProcessorImplTest {

	@InjectMocks
	JiraIssueProcessorImpl transformFetchedIssueToJiraIssue;
	List<Issue> issues = new ArrayList<>();
	ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();

	ProjectConfFieldMapping projectConfFieldMapping1 = ProjectConfFieldMapping.builder().build();

	ProjectConfFieldMapping projectConfFieldMapping2 = ProjectConfFieldMapping.builder().build();
	@Mock
	JiraProcessor jiraProcessor;

	FieldMapping fieldMapping;
	List<ProjectBasicConfig> projectConfigsList;
	List<ProjectToolConfig> projectToolConfigsForJQL;
	List<ProjectToolConfig> projectToolConfigsForBoard;
	Optional<Connection> connection;
	List<FieldMapping> fieldMappingList = new ArrayList<>();

	List<FieldMapping> fieldMappingListForIfCase = new ArrayList<>();

	List<FieldMapping> fieldMappingListForElseIfCase = new ArrayList<>();
	@Mock
	Runtime runtime;
	@Mock
	Executors executors;
	@Mock
	ExecutorService executorService;
	List<IssueField> issueFieldList = new ArrayList<>();
	@Mock
	private JiraIssueRepository jiraIssueRepository;
	@Mock
	private JiraProcessorRepository jiraProcessorRepository;
	@Mock
	private JiraProcessorConfig jiraProcessorConfig;
	@Mock
	private AdditionalFilterHelper additionalFilterHelper;
	@Mock
	private AssigneeDetailsRepository assigneeDetailsRepository;

	@Mock
	private AssigneeDetails assigneeDetails;

	Set<Assignee> assigneeSetToSave = new HashSet<>();

	@Before
	public void setup() throws URISyntaxException, JSONException {
		projectConfigsList = getMockProjectConfig();
		projectToolConfigsForJQL = getMockProjectToolConfigForJQL();
		projectToolConfigsForBoard = getMockProjectToolConfigForBoard();
		connection = getMockConnection();
		Assignee assignee = Assignee.builder().assigneeId("31").assigneeName("User 1").build();
		Assignee assignee1 = Assignee.builder().assigneeId("32").assigneeName("User 2").build();
		assigneeSetToSave.add(assignee);
		assigneeSetToSave.add(assignee1);
		AssigneeDetails assigneeDetailsToBeSave = new AssigneeDetails("63c04dc7b7617e260763ca4e",
				ProcessorConstants.JIRA, assigneeSetToSave, 3);
		when(assigneeDetails.getBasicProjectConfigId()).thenReturn("63c04dc7b7617e260763ca4e");
		when(assigneeDetailsRepository.findByBasicProjectConfigIdAndSource(any(), any()))
				.thenReturn(assigneeDetailsToBeSave);
		createIssue();
		createIssuefieldsList();
		prepareFiledMapping(0);
		prepareFiledMapping(1);
		prepareFiledMapping(2);
		createProjectConfigMapForJQL();
		createProjectConfigMapForBoard();
		createProjectConfigMapForElse();
	}

	@Test
	public void convertToJiraIssue() throws URISyntaxException, JSONException, InterruptedException {
		when(jiraProcessorConfig.getJiraDirectTicketLinkKey()).thenReturn("browse/");
		when(jiraIssueRepository.findByIssueIdAndBasicProjectConfigId(any(), any()))
				.thenReturn(JiraIssue.builder().build());
//		when(jiraProcessorConfig.getRcaValuesForCodeIssue()).thenReturn(Arrays.asList("code", "coding"));
		when(additionalFilterHelper.getAdditionalFilter(any(), any()))
				.thenReturn(getMockAdditionalFilterFromJiraIssue());
		assertEquals(JiraIssue.class,
				(transformFetchedIssueToJiraIssue.convertToJiraIssue(issues.get(0), projectConfFieldMapping, "", new ObjectId("5e16c126e4b098db673cc372")))
						.getClass());

	}

	@Test
	public void convertToJiraIssue2() throws URISyntaxException, JSONException, InterruptedException {
		when(jiraProcessorConfig.getJiraDirectTicketLinkKey()).thenReturn("browse/");
		when(jiraIssueRepository.findByIssueIdAndBasicProjectConfigId(any(), any()))
				.thenReturn(JiraIssue.builder().build());
//		when(jiraProcessorConfig.getRcaValuesForCodeIssue()).thenReturn(Arrays.asList("code", "coding"));
		when(additionalFilterHelper.getAdditionalFilter(any(), any()))
				.thenReturn(getMockAdditionalFilterFromJiraIssue());
		assertEquals(JiraIssue.class,
				(transformFetchedIssueToJiraIssue.convertToJiraIssue(issues.get(0), projectConfFieldMapping1, "", new ObjectId("5e16c126e4b098db673cc372")))
						.getClass());

	}

	@Test
	public void convertToJiraIssue3() throws URISyntaxException, JSONException, InterruptedException {
		when(jiraProcessorConfig.getJiraDirectTicketLinkKey()).thenReturn("browse/");
		when(jiraIssueRepository.findByIssueIdAndBasicProjectConfigId(any(), any()))
				.thenReturn(JiraIssue.builder().build());
//		when(jiraProcessorConfig.getRcaValuesForCodeIssue()).thenReturn(Arrays.asList("code", "coding"));
		when(additionalFilterHelper.getAdditionalFilter(any(), any()))
				.thenReturn(getMockAdditionalFilterFromJiraIssue());
		assertEquals(JiraIssue.class,
				(transformFetchedIssueToJiraIssue.convertToJiraIssue(issues.get(0), projectConfFieldMapping2, "", new ObjectId("5e16c126e4b098db673cc372")))
						.getClass());

	}

	@Test
	public void updateAssigneeDetailsToggleWise() {
		transformFetchedIssueToJiraIssue.updateAssigneeDetailsToggleWise(new JiraIssue(), projectConfFieldMapping,
				Arrays.asList("1234"), Arrays.asList("username"), Arrays.asList("username"));
	}

	private Optional<Connection> getMockConnection() {
		ConnectionsDataFactory connectionDataFactory = ConnectionsDataFactory
				.newInstance("/json/default/connections.json");
		return connectionDataFactory.findConnectionById("5fd99f7bc8b51a7b55aec836");
	}

	private List<ProjectToolConfig> getMockProjectToolConfigForJQL() {
		ToolConfigDataFactory projectToolConfigDataFactory = ToolConfigDataFactory
				.newInstance("/json/default/project_tool_configs.json");
		return projectToolConfigDataFactory.findByToolNameAndBasicProjectConfigId(ProcessorConstants.JIRA,
				"63c04dc7b7617e260763ca4e");
	}

	private List<ProjectToolConfig> getMockProjectToolConfigForBoard() {
		ToolConfigDataFactory projectToolConfigDataFactory = ToolConfigDataFactory
				.newInstance("/json/default/project_tool_configs.json");
		return projectToolConfigDataFactory.findByToolNameAndBasicProjectConfigId(ProcessorConstants.JIRA,
				"63bfa0d5b7617e260763ca21");
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

	private void createIssue() throws URISyntaxException, JSONException {
		BasicProject basicProj = new BasicProject(new URI("self"), "proj1", 1l, "project1");
		IssueType issueType1 = new IssueType(new URI("self"), 1l, "Story", false, "desc", new URI("iconURI"));
		IssueType issueType2 = new IssueType(new URI("self"), 2l, "Defect", true, "desc", new URI("iconURI"));
		Status status1 = new Status(new URI("self"), 1l, "Ready for Sprint Planning", "desc", new URI("iconURI"),
				new StatusCategory(new URI("self"), "name", 1l, "key", "colorname"));
		BasicPriority basicPriority = new BasicPriority(new URI("self"), 1l, "priority1");
		Resolution resolution = new Resolution(new URI("self"), 1l, "resolution", "resolution");
		Map<String, URI> avatarMap = new HashMap<>();
		avatarMap.put("48x48", new URI("value"));
		URI uri = new URI("https://dummy.com/jira/rest/api/2/user?username=user1");
		User user1 = new User(uri, "user1", "user1", "userAccount", "user1@xyz.com", true, null, avatarMap, null);
		Map<String, String> map = new HashMap<>();
		map.put("customfield_19121", "Client Testing (UAT)");
		map.put("self", "https://jiradomain.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "Component");
		map.put("id", "20810");
		JSONObject value = new JSONObject(map);
		IssueField issueField = new IssueField("customfield_19121", "Component", null, value);
		List<IssueField> issueFields = Arrays.asList(issueField);

		Comment comment = new Comment(new URI("self"), "body", null, null, DateTime.now(), DateTime.now(),
				new Visibility(Visibility.Type.ROLE, "abc"), 1l);
		List<Comment> comments = Arrays.asList(comment);
		BasicVotes basicVotes = new BasicVotes(new URI("self"), 1, true);
		BasicUser basicUser = new BasicUser(new URI("self"), "basicuser", "basicuser", "accountId");
		Worklog worklog = new Worklog(new URI("self"), new URI("self"), basicUser, basicUser, null, DateTime.now(),
				DateTime.now(), DateTime.now(), 60, null);
		List<Worklog> workLogs = Arrays.asList(worklog);
		ChangelogItem changelogItem = new ChangelogItem(FieldType.JIRA, "field1", "from", "fromString", "to",
				"toString");
		ChangelogGroup changelogGroup = new ChangelogGroup(basicUser, DateTime.now(), Arrays.asList(changelogItem));
		BasicComponent basicComponent = new BasicComponent(new URI("self"), 1l, "component1", "abc");
		List<BasicComponent> component = Collections.singletonList(basicComponent);

		TimeTracking timeTracking = new TimeTracking(8, 8, 8);

		Collection<Version> fixVersions = new ArrayList<>();
		Version version = new Version(new URI("https://dummy.com/jira/rest/api/2/version/143417"), 143417L, "",
				"KnowHOW v6.8.0", false, true, DateTime.now());
		fixVersions.add(version);

		Issue issue = new Issue("summary1", new URI("self"), "key1", 1l, basicProj, issueType2, status1, "story",
				basicPriority, resolution, new ArrayList<>(), user1, user1, DateTime.now(), DateTime.now(),
				DateTime.now(), new ArrayList<>(), fixVersions, component, timeTracking, issueFieldList, comments, null,
				createIssueLinkData(), basicVotes, workLogs, null, Arrays.asList("expandos"), null,
				Arrays.asList(changelogGroup), null, new HashSet<>(Arrays.asList("label1")));
		issues.add(issue);

	}

	private List<IssueLink> createIssueLinkData() throws URISyntaxException {
		List<IssueLink> issueLinkList = new ArrayList<>();
		URI uri = new URI("https://testDomain.com/jira/rest/api/2/issue/12344");
		IssueLinkType linkType = new IssueLinkType("Blocks", "blocks", IssueLinkType.Direction.OUTBOUND);
		IssueLink issueLink = new IssueLink("IssueKey", uri, linkType);
		issueLinkList.add(issueLink);

		return issueLinkList;
	}

	private void prepareFiledMapping(int caseIfElse) {
		FieldMapping fieldMapping = new FieldMapping();
		fieldMapping.setBasicProjectConfigId(new ObjectId("63bfa0d5b7617e260763ca21"));
		fieldMapping.setSprintName("customfield_12700");
		List<String> jiraType = new ArrayList<>();
		jiraType.add("Defect");
		fieldMapping.setJiradefecttype(jiraType);
		jiraType = new ArrayList<>(
				Arrays.asList(new String[] { "Story", "Defect", "Pre Story", "Feature", "Enabler Story" }));
		String[] jiraIssueType = new String[] { "Story", "Defect", "Pre Story", "Feature", "Enabler Story" };
		fieldMapping.setJiraIssueTypeNames(jiraIssueType);
		fieldMapping.setRootCause("customfield_19121");

		jiraType = new ArrayList<>();
		jiraType.add("Story");
		fieldMapping.setJiraDefectInjectionIssueType(jiraType);
		fieldMapping.setJiraTechDebtIssueType(jiraType);
		fieldMapping.setJiraDefectSeepageIssueType(jiraType);
		fieldMapping.setJiraDefectRemovalStatus(jiraType);
		fieldMapping.setJiraDefectRejectionlIssueType(jiraType);
		fieldMapping.setJiraTestAutomationIssueType(jiraType);
		fieldMapping.setJiraDefectRejectionlIssueType(jiraType);
		fieldMapping.setJiraDefectCountlIssueType(jiraType);
		fieldMapping.setJiraIntakeToDorIssueType(jiraType);
		fieldMapping.setJiraBugRaisedByCustomField("customfield_12121");
		fieldMapping.setEpicLink("customfield_12121");

		fieldMapping.setJiraDefectRejectionStatus("Dropped");
		fieldMapping.setJiraBugRaisedByIdentification("CustomField");

		jiraType = new ArrayList<>();
		jiraType.add("Ready for Sign-off");
		fieldMapping.setJiraDod(jiraType);

		jiraType = new ArrayList<>();
		jiraType.add("Closed");
		fieldMapping.setJiraDefectRemovalStatus(jiraType);

		fieldMapping.setJiraStoryPointsCustomField("customfield_56789");

		jiraType = new ArrayList<>();
		jiraType.add("40");

		jiraType = new ArrayList<>();
		jiraType.add("Client Testing (UAT)");
		fieldMapping.setJiraBugRaisedByValue(jiraType);

		jiraType = new ArrayList<>();
		jiraType.add("Story");
		jiraType.add("Feature");
		fieldMapping.setJiraSprintVelocityIssueType(jiraType);

		jiraType = new ArrayList<>(Arrays.asList(new String[] { "Story", "Defect", "Pre Story", "Feature" }));
		fieldMapping.setJiraSprintCapacityIssueType(jiraType);

		fieldMapping.setJiraIssueEpicType(jiraType);

		jiraType = new ArrayList<>();
		jiraType.add("Closed");
		fieldMapping.setJiraIssueDeliverdStatus(jiraType);

		fieldMapping.setJiraDor("In Progress");
		fieldMapping.setJiraLiveStatus("Closed");
		fieldMapping.setRootCauseValue(Arrays.asList("Coding", "None"));

		jiraType = new ArrayList<>(Arrays.asList(new String[] { "Story", "Pre Story" }));
		fieldMapping.setJiraStoryIdentification(jiraType);

		fieldMapping.setJiraDefectCreatedStatus("Open");

		jiraType = new ArrayList<>();
		jiraType.add("Ready for Sign-off");
		fieldMapping.setJiraDod(jiraType);
		fieldMapping.setStoryFirstStatus("In Analysis");
		jiraType = new ArrayList<>();
		jiraType.add("In Analysis");
		jiraType.add("In Development");
		fieldMapping.setJiraStatusForDevelopment(jiraType);

		jiraType = new ArrayList<>();
		jiraType.add("Ready for Testing");
		fieldMapping.setJiraStatusForQa(jiraType);

		List<String> jiraSegData = new ArrayList<>();
		jiraSegData.add("Tech Story");
		jiraSegData.add("Task");

		jiraSegData = new ArrayList<>();
		jiraSegData.add("Tech Story");



		if(caseIfElse == 1){
			jiraType = new ArrayList<>();
			jiraType.add("label1");

			fieldMapping.setJiraTechDebtIdentification(CommonConstant.LABELS);
			fieldMapping.setJiraTechDebtValue(jiraType);

//			fieldMapping.setJiraBugRaisedByQAIdentification(CommonConstant.LABELS);
//			fieldMapping.setJiraBugRaisedByQAValue(jiraType);

			fieldMapping.setProductionDefectIdentifier(CommonConstant.LABELS);
			fieldMapping.setProductionDefectValue(jiraType);

			fieldMapping.setJiraDueDateField(CommonConstant.DUE_DATE);

			fieldMapping.setJiraProductionIncidentIdentification(CommonConstant.LABELS);
			fieldMapping.setJiraProdIncidentRaisedByValue(Arrays.asList("label1"));

			fieldMapping.setTestingPhaseDefectsIdentifier(CommonConstant.LABELS);
			fieldMapping.setTestingPhaseDefectValue(jiraType);

		} else if(caseIfElse == 2){
			fieldMapping.setJiraTechDebtIdentification(CommonConstant.ISSUE_TYPE);
			jiraType = new ArrayList<>();
			jiraType.add("Defect");
			jiraType.add("Bug");
			jiraType.add("Story");
			fieldMapping.setJiraTechDebtValue(jiraType);

//			fieldMapping.setJiraBugRaisedByQAIdentification(CommonConstant.CUSTOM_FIELD);
//			fieldMapping.setJiraBugRaisedByQACustomField("customfield_14141");
//			fieldMapping.setJiraBugRaisedByQAValue(Arrays.asList("label1"));

			fieldMapping.setProductionDefectIdentifier(CommonConstant.COMPONENT);
			fieldMapping.setProductionDefectValue(jiraType);
			fieldMapping.setProductionDefectComponentValue("component1");

			fieldMapping.setJiraDueDateField(CommonConstant.DUE_DATE);
			fieldMapping.setJiraDueDateCustomField("customfield_56444");
			fieldMapping.setJiraDevDueDateCustomField("customfield_56444");

			fieldMapping.setTestingPhaseDefectsIdentifier(CommonConstant.COMPONENT);
			fieldMapping.setTestingPhaseDefectComponentValue("component1");

			fieldMapping.setProductionDefectIdentifier(CommonConstant.COMPONENT);
		} else {
			jiraType = new ArrayList<>();
			jiraType.add("TECH_DEBT");
			fieldMapping.setJiraTechDebtValue(jiraType);
			fieldMapping.setJiraTechDebtIdentification(CommonConstant.CUSTOM_FIELD);
			fieldMapping.setJiraTechDebtCustomField("customfield_14141");

//			fieldMapping.setJiraBugRaisedByQAIdentification(CommonConstant.ISSUE_TYPE);

			fieldMapping.setProductionDefectIdentifier(CommonConstant.CUSTOM_FIELD);
			fieldMapping.setProductionDefectCustomField("customfield_14141");
			fieldMapping.setProductionDefectValue(jiraType);

			fieldMapping.setJiraProductionIncidentIdentification(CommonConstant.CUSTOM_FIELD);
			fieldMapping.setJiraProdIncidentRaisedByCustomField("customfield_14141");
			fieldMapping.setJiraProdIncidentRaisedByValue(Arrays.asList("TECH_DEBT"));

			fieldMapping.setTestingPhaseDefectsIdentifier(CommonConstant.CUSTOM_FIELD);
			fieldMapping.setTestingPhaseDefectValue(Arrays.asList("label1"));
			fieldMapping.setTestingPhaseDefectCustomField("customfield_14141");
//			fieldMapping.setJiraBugRaisedByQAValue(Arrays.asList("label1"));

		}

		fieldMapping.setJiraStatusMappingCustomField("customfield_14502");
		fieldMapping.setEpicName("customfield_14502");

		if(caseIfElse == 1){
			fieldMappingListForIfCase.add(fieldMapping);
		} else if(caseIfElse == 2) {
			fieldMappingListForElseIfCase.add(fieldMapping);
		} else {
			fieldMappingList.add(fieldMapping);
		}


	}

	private void createProjectConfigMapForJQL() {
		ProjectBasicConfig projectConfig = projectConfigsList.get(1);
		BeanUtils.copyProperties(projectConfig, projectConfFieldMapping);
		projectConfFieldMapping.setProjectBasicConfig(projectConfig);
		projectConfFieldMapping.setKanban(projectConfig.getIsKanban());
		projectConfFieldMapping.setBasicProjectConfigId(projectConfig.getId());
		projectConfFieldMapping.setJira(getJiraToolConfig(projectToolConfigsForJQL.get(0)));
		projectConfFieldMapping.setJiraToolConfigId(projectToolConfigsForJQL.get(0).getId());
		projectConfFieldMapping.setFieldMapping(fieldMappingList.get(0));
	}

	private void createProjectConfigMapForBoard() {
		ProjectBasicConfig projectConfig = projectConfigsList.get(0);
		BeanUtils.copyProperties(projectConfig, projectConfFieldMapping1);
		projectConfFieldMapping1.setProjectBasicConfig(projectConfig);
		projectConfFieldMapping1.setKanban(projectConfig.getIsKanban());
		projectConfFieldMapping1.setBasicProjectConfigId(projectConfig.getId());
		projectConfFieldMapping1.setJira(getJiraToolConfig(projectToolConfigsForBoard.get(0)));
		projectConfFieldMapping1.setJiraToolConfigId(projectToolConfigsForBoard.get(0).getId());
		projectConfFieldMapping1.setFieldMapping(fieldMappingListForIfCase.get(0));
	}

	private void createProjectConfigMapForElse() {
		ProjectBasicConfig projectConfig = projectConfigsList.get(0);
		BeanUtils.copyProperties(projectConfig, projectConfFieldMapping2);
		projectConfFieldMapping2.setProjectBasicConfig(projectConfig);
		projectConfFieldMapping2.setKanban(projectConfig.getIsKanban());
		projectConfFieldMapping2.setBasicProjectConfigId(projectConfig.getId());
		projectConfFieldMapping2.setJira(getJiraToolConfig(projectToolConfigsForBoard.get(0)));
		projectConfFieldMapping2.setJiraToolConfigId(projectToolConfigsForBoard.get(0).getId());
		projectConfFieldMapping2.setFieldMapping(fieldMappingListForElseIfCase.get(0));
	}

	private JiraToolConfig getJiraToolConfig(ProjectToolConfig projectToolConfig) {
		JiraToolConfig toolObj = new JiraToolConfig();
		BeanUtils.copyProperties(projectToolConfig, toolObj);
		toolObj.setConnection(connection);
		return toolObj;
	}

	private void createIssuefieldsList() throws JSONException {
		Map<String, Object> map = new HashMap<>();
		map.put("customfield_12121", "Client Testing (UAT)");
		map.put("self", "https://jiradomain.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "Client Testing (UAT)");
		map.put("id", "12121");
		IssueField issueField = new IssueField("customfield_12121", "UAT", null, new JSONObject(map));
		issueFieldList.add(issueField);

		JSONArray array = null;

		List<Object> sprintList = new ArrayList<>();
		String sprint = "com.atlassian.greenhopper.service.sprint.Sprint@6fc7072e[id=23356,rapidViewId=11649,state=CLOSED,name=TEST | 06 Jan - 19 Jan,startDate=2020-01-06T11:38:31.937Z,endDate=2020-01-19T11:38:00.000Z,completeDate=2020-01-20T11:15:21.528Z,sequence=22778,goal=]";
		sprintList.add(sprint);
		array = new JSONArray(sprintList);

		issueField = new IssueField("customfield_12700", "Sprint", null, array);
		issueFieldList.add(issueField);

		List<String> list = new ArrayList<>();
		list.add("BrandName-12");
		issueField = new IssueField("customfield_48531", "Bran", null, new JSONArray(list));
		issueFieldList.add(issueField);

		issueField = new IssueField("customfield_56789", "StoryPoints", null, Integer.parseInt("5"));
		issueFieldList.add(issueField);

		issueField = new IssueField("customfield_56444", "Due Date", null, "2022-12-14'T'03:22:33.012Z");
		issueFieldList.add(issueField);

		map = new HashMap<>();
		map.put("self", "https://jiradomain.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "TECH_DEBT");
		map.put("id", "14141");
		issueField = new IssueField("customfield_14141", "StoryPoints", null, new JSONObject(map));
		issueFieldList.add(issueField);

		map = new HashMap<>();
		map.put("self", "https://jiradomain.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "Mobile");
		map.put("id", "18181");
		issueField = new IssueField("customfield_18181", "Device Platform", null, new JSONObject(map));
		issueFieldList.add(issueField);

		map = new HashMap<>();
		map.put("self", "https://jiradomain.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "Epic");
		map.put("id", "18182");
		issueField = new IssueField("customfield_14502", "Epic Name", null, new JSONObject(map));
		issueFieldList.add(issueField);

		map = new HashMap<>();
		map.put("self", "https://jiradomain.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "code");
		map.put("id", "19121");
		JSONObject jsonObject = new JSONObject(map);
		List<Object> rcaList = new ArrayList<>();
		rcaList.add(jsonObject);
		issueField = new IssueField("customfield_19121", "code_issue", null, new JSONArray(rcaList));
		issueFieldList.add(issueField);

		map = new HashMap<>();
		map.put("self", "https://jiradomain.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "stage");
		map.put("id", "13131");
		issueField = new IssueField("customfield_13131", "stage", null, new JSONObject(map));
		issueFieldList.add(issueField);

		List<JSONObject> jsonArrayList1 = new ArrayList<>();
		map = new HashMap<>();
		map.put("self", "https://jiradomain.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "40");
		map.put("id", "Test_Automation");
		map.put("key", "ABC-123");
		JSONObject jsonObject1 = new JSONObject(map);
		jsonArrayList1.add(jsonObject1);
		IssueField issueField1 = new IssueField("40", "Test_Automation", null, new JSONArray(jsonArrayList1));
		issueFieldList.add(issueField1);

		IssueField issueField2 = new IssueField("fivVesion", "Fix Version", null, "KnowHowv6.7");
		issueFieldList.add(issueField2);

		IssueField issueField3 = new IssueField("duedate", "Due_Date", null, "2022-12-14'T'03:22:33.012Z");
		issueFieldList.add(issueField3);

		IssueField issueField4 = new IssueField("aggregatetimespent", "aggregatetimespent", null, 300);
		issueFieldList.add(issueField4);

		IssueField issueField5 = new IssueField("aggregatetimeestimate", "aggregatetimeestimate", null, 360);
		issueFieldList.add(issueField5);

		IssueField issueField6 = new IssueField("aggregatetimeoriginalestimate", "aggregatetimeoriginalestimate", null,
				300);
		issueFieldList.add(issueField6);

		IssueField issueField7 = new IssueField("parent", "Due_Date", null, jsonObject1);
		issueFieldList.add(issueField7);

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

		JiraIssue jiraIssue = new JiraIssue(); // Set up your JiraIssue instance
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
		// Add other fields as needed

		// Use reflection to access the private method
		Method method = JiraIssueProcessorImpl.class.getDeclaredMethod("setEpicIssueData", FieldMapping.class,
				JiraIssue.class, Map.class);
		method.setAccessible(true);

		// Act
		method.invoke(transformFetchedIssueToJiraIssue, fieldMapping, jiraIssue, fields);

		// Assert
		// assertEquals( /* expected value */, jiraIssue.getJobSize(), 0.001); // Add
		// assertions for other fields
	}

	@Test
	public void testSetSubTaskLinkage()
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		// Arrange
		FieldMapping fieldMapping = new FieldMapping();
		fieldMapping.setJiraSubTaskIdentification(Arrays.asList("Story"));

		Map<String, IssueField> fields = new HashMap<>();
		fields.put("8.0", new IssueField("", "8.0", null, "8.0"));

		// Set up other mocks and required behaviors

		// Use reflection to access the private method
		Method method = JiraIssueProcessorImpl.class.getDeclaredMethod("setSubTaskLinkage", JiraIssue.class,
				FieldMapping.class, Issue.class, Map.class);
		method.setAccessible(true);

		JiraIssue jiraIssue = new JiraIssue();
		jiraIssue.setTypeName("Story");
		// Act
		method.invoke(transformFetchedIssueToJiraIssue, jiraIssue, fieldMapping, issues.get(0), new HashMap<>());

		// Assert
		// Add assertions based on the expected behavior of your method
		// verify(jiraIssueMock).setParentStoryId(anySet());
	}

	@Test
	public void testExcludeLinks() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		when(jiraProcessorConfig.getExcludeLinks()).thenReturn(Arrays.asList("xyz"));
		Method method = JiraIssueProcessorImpl.class.getDeclaredMethod("excludeLinks", Issue.class, Set.class);
		method.setAccessible(true);
		method.invoke(transformFetchedIssueToJiraIssue, issues.get(0), new HashSet<>());
	}

	@Test
	public void testSetJiraAssigneeDetailsWhenUserIsNull()
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Method method = JiraIssueProcessorImpl.class.getDeclaredMethod("setJiraAssigneeDetails", JiraIssue.class,
				User.class, ProjectConfFieldMapping.class);
		method.setAccessible(true);
		method.invoke(transformFetchedIssueToJiraIssue, new JiraIssue(), null, projectConfFieldMapping);
	}

	@Test
	public void testGetRootCauses() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, JSONException {
		Method method = JiraIssueProcessorImpl.class.getDeclaredMethod("getRootCauses", FieldMapping.class, Map.class);
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

		method.invoke(transformFetchedIssueToJiraIssue, fieldMapping, fields);
	}

	@Test
	public void testProcessSprintData()
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Method method = JiraIssueProcessorImpl.class.getDeclaredMethod("processSprintData", JiraIssue.class,
				IssueField.class, ProjectConfFieldMapping.class);
		method.setAccessible(true);
		method.invoke(transformFetchedIssueToJiraIssue, new JiraIssue(), null, projectConfFieldMapping);
	}

	@Test
	public void testSetAssigneeName() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		when(assigneeDetailsRepository.findByBasicProjectConfigIdAndSource(any(),any())).thenReturn(null);
		Method method = JiraIssueProcessorImpl.class.getDeclaredMethod("setAssigneeName", String.class, String.class);
		method.setAccessible(true);
		method.invoke(transformFetchedIssueToJiraIssue, "assigneeId", "basicProjectConfigId");
	}
	@Test
	public void calculateEstimationForActualEstimates() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		IssueField issueField = new IssueField("DTS123","KnowJ","None",5.0);
		Method method = JiraIssueProcessorImpl.class.getDeclaredMethod("calculateEstimation",IssueField.class,String.class);
		method.setAccessible(true);
		method.invoke(transformFetchedIssueToJiraIssue,issueField, JiraConstants.ACTUAL_ESTIMATION);
	}
	@Test
	public void calculateEstimationForBufferEstimates() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		IssueField issueField = new IssueField("DTS123","KnowJ","None",5.0);
		Method method = JiraIssueProcessorImpl.class.getDeclaredMethod("calculateEstimation",IssueField.class,String.class);
		method.setAccessible(true);
		method.invoke(transformFetchedIssueToJiraIssue,issueField, JiraConstants.BUFFERED_ESTIMATION);
	}
	@Test
	public void calculateEstimationForStoryPoints() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		IssueField issueField = new IssueField("DTS123","KnowJ","None",5.0);
		Method method = JiraIssueProcessorImpl.class.getDeclaredMethod("calculateEstimation",IssueField.class,String.class);
		method.setAccessible(true);
		method.invoke(transformFetchedIssueToJiraIssue,issueField, JiraConstants.STORY_POINT);
	}
	@Test
	public void calculateEstimationForNone() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		IssueField issueField = new IssueField("DTS123","KnowJ","None",5.0);
		Method method = JiraIssueProcessorImpl.class.getDeclaredMethod("calculateEstimation",IssueField.class,String.class);
		method.setAccessible(true);
		method.invoke(transformFetchedIssueToJiraIssue,issueField, "None");
	}
	@Test
	public void shouldEstimationBeCalculated() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		IssueField issueField = new IssueField("DTS123","KnowJ","None",5.0);
		Method method = JiraIssueProcessorImpl.class.getDeclaredMethod("shouldEstimationBeCalculated",IssueField.class);
		method.setAccessible(true);
		method.invoke(transformFetchedIssueToJiraIssue,issueField);
	}
	@Test
	public void calculateEstimation() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		IssueField issueField = new IssueField("DTS123","KnowJ","None",5.0);
		Method method = JiraIssueProcessorImpl.class.getDeclaredMethod("calculateEstimation",IssueField.class);
		method.setAccessible(true);
		method.invoke(transformFetchedIssueToJiraIssue,issueField);
	}
	@Test
	public void setSpecificFieldTestPhase() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		JiraIssue jiraIssue = new JiraIssue();
		List list = new ArrayList();
		list.add("UAT");
		list.add("RT");
		Method method = JiraIssueProcessorImpl.class.getDeclaredMethod("setSpecificField",JiraIssue.class,String.class,List.class);
		method.setAccessible(true);
		method.invoke(transformFetchedIssueToJiraIssue,jiraIssue,"TestPhase",list);
	}
	@Test
	public void setSpecificFieldUAT() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		JiraIssue jiraIssue = new JiraIssue();
		List list = new ArrayList();
		list.add("UAT");
		list.add("RT");
		Method method = JiraIssueProcessorImpl.class.getDeclaredMethod("setSpecificField",JiraIssue.class,String.class,List.class);
		method.setAccessible(true);
		method.invoke(transformFetchedIssueToJiraIssue,jiraIssue,"UAT",list);
	}

	@Test
	public void testSetLateRefinement188() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, JSONException {
		// Arrange
		FieldMapping fieldMapping = new FieldMapping();
		fieldMapping.setJiraRefinementCriteriaKPI188(CommonConstant.CUSTOM_FIELD);
		fieldMapping.setJiraRefinementByCustomFieldKPI188("customfield_14141");
		fieldMapping.setJiraRefinementMinLengthKPI188("2");
		fieldMapping.setJiraRefinementKeywordsKPI188(Arrays.asList("keyword1", "keyword2"));

		JiraIssue jiraIssue = new JiraIssue();

		Map<String, IssueField> fields = new HashMap<>();
		Map<String, String> fieldValueMap = new HashMap<>();
		fieldValueMap.put("value", "keyword1 keyword3");
		IssueField issueField = new IssueField("customfield_14141", "Custom Field", null, new JSONObject(fieldValueMap));
		fields.put("customfield_14141", issueField);

		Issue issue = issues.get(0);

		// Use reflection to access the private method
		Method method = JiraIssueProcessorImpl.class.getDeclaredMethod("setLateRefinement188", FieldMapping.class, JiraIssue.class, Map.class, Issue.class);
		method.setAccessible(true);

		// Act
		method.invoke(transformFetchedIssueToJiraIssue, fieldMapping, jiraIssue, fields, issue);

		// Assert
		assertNotNull(jiraIssue.getUnRefinedValue188());
		assertTrue(jiraIssue.getUnRefinedValue188().contains("keyword3"));
	}
}