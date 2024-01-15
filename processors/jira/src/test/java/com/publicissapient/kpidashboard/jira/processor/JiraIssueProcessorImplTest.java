package com.publicissapient.kpidashboard.jira.processor;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.atlassian.jira.rest.client.api.domain.TimeTracking;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import org.apache.commons.beanutils.BeanUtils;
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
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.Visibility;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilter;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
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
	@Mock
	JiraProcessor jiraProcessor;

	FieldMapping fieldMapping;
	List<ProjectBasicConfig> projectConfigsList;
	List<ProjectToolConfig> projectToolConfigs;
	Optional<Connection> connection;
	List<FieldMapping> fieldMappingList = new ArrayList<>();
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

	@Before
	public void setup() throws URISyntaxException, JSONException {
		// fieldMapping=getMockFieldMapping();
		projectConfigsList = getMockProjectConfig();
		projectToolConfigs = getMockProjectToolConfig();
		connection = getMockConnection();
		// fieldMappingList=getMockFieldMappingList();
		createIssue();
		createIssuefieldsList();
		prepareFiledMapping();
		createProjectConfigMap();
	}

	@Test
	public void convertToJiraIssue() throws URISyntaxException, JSONException, InterruptedException {
		when(jiraProcessorRepository.findByProcessorName(ProcessorConstants.JIRA)).thenReturn(jiraProcessor);
		when(jiraProcessor.getId()).thenReturn(new ObjectId("5e16c126e4b098db673cc372"));// 63b3f50b6d8d7f44def6ec2f
		when(jiraProcessorConfig.getJiraDirectTicketLinkKey()).thenReturn("browse/");
		when(jiraIssueRepository.findByIssueIdAndBasicProjectConfigId(any(), any()))
				.thenReturn(JiraIssue.builder().build());
		when(jiraProcessorConfig.getRcaValuesForCodeIssue()).thenReturn(Arrays.asList("code", "coding"));
		when(additionalFilterHelper.getAdditionalFilter(any(), any()))
				.thenReturn(getMockAdditionalFilterFromJiraIssue());
		assertEquals(JiraIssue.class,
				(transformFetchedIssueToJiraIssue.convertToJiraIssue(issues.get(0), projectConfFieldMapping, ""))
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

	private void createIssue() throws URISyntaxException {
		BasicProject basicProj = new BasicProject(new URI("self"), "proj1", 1l, "project1");
		IssueType issueType1 = new IssueType(new URI("self"), 1l, "Story", false, "desc", new URI("iconURI"));
		IssueType issueType2 = new IssueType(new URI("self"), 2l, "Defect", true, "desc", new URI("iconURI"));
		Status status1 = new Status(new URI("self"), 1l, "Ready for Sprint Planning", "desc", new URI("iconURI"),
				new StatusCategory(new URI("self"), "name", 1l, "key", "colorname"));
		BasicPriority basicPriority = new BasicPriority(new URI("self"), 1l, "priority1");
		Resolution resolution = new Resolution(new URI("self"), 1l, "resolution", "resolution");
		Map<String, URI> avatarMap = new HashMap<>();
		avatarMap.put("48x48", new URI("value"));
		URI uri = new URI("self");
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

		TimeTracking timeTracking=new TimeTracking(8,8,8);

		Issue issue = new Issue("summary1", new URI("self"), "key1", 1l, basicProj, issueType2, status1, "story",
				basicPriority, resolution, new ArrayList<>(), user1, user1, DateTime.now(), DateTime.now(),
				DateTime.now(), new ArrayList<>(), new ArrayList<>(), component, timeTracking, issueFieldList, comments, null,
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

	private void prepareFiledMapping() {
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

		fieldMapping.setJiraTechDebtIdentification(CommonConstant.CUSTOM_FIELD);
		fieldMapping.setJiraTechDebtCustomField("customfield_14141");

		jiraType = new ArrayList<>();
		jiraType.add("TECH_DEBT");
		fieldMapping.setJiraTechDebtValue(jiraType);
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

		fieldMapping.setProductionDefectIdentifier("Component");
		fieldMapping.setJiraBugRaisedByQAIdentification("Labels");
		fieldMapping.setJiraBugRaisedByQAValue(Arrays.asList("label1"));
		fieldMapping.setProductionDefectComponentValue("component1");
		fieldMapping.setTestingPhaseDefectsIdentifier("Component");
		fieldMapping.setTestingPhaseDefectComponentValue("component1");
		fieldMapping.setJiraProductionIncidentIdentification("CustomField");
		fieldMapping.setJiraProdIncidentRaisedByCustomField("CustomField");
		fieldMappingList.add(fieldMapping);

	}

	private void createProjectConfigMap() {
		ProjectBasicConfig projectConfig = projectConfigsList.get(1);
		try {
			BeanUtils.copyProperties(projectConfFieldMapping, projectConfig);
		} catch (IllegalAccessException | InvocationTargetException e) {
		}
		projectConfFieldMapping.setProjectBasicConfig(projectConfig);
		projectConfFieldMapping.setKanban(projectConfig.getIsKanban());
		projectConfFieldMapping.setBasicProjectConfigId(projectConfig.getId());
		projectConfFieldMapping.setJira(getJiraToolConfig());
		projectConfFieldMapping.setJiraToolConfigId(projectToolConfigs.get(0).getId());
		projectConfFieldMapping.setFieldMapping(fieldMappingList.get(0));
	}

	private JiraToolConfig getJiraToolConfig() {
		JiraToolConfig toolObj = new JiraToolConfig();
		try {
			BeanUtils.copyProperties(toolObj, projectToolConfigs.get(0));
		} catch (IllegalAccessException | InvocationTargetException e) {

		}
		toolObj.setConnection(connection);
		return toolObj;
	}

	// private List<FieldMapping> getMockFieldMappingList() {
	// FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
	// .newInstance("/json/default/field_mapping.json");
	// return fieldMappingDataFactory.getFieldMappings();
	// }

	private void createIssuefieldsList() {
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
		JSONObject jsonObject1 = new JSONObject(map);
		jsonArrayList1.add(jsonObject1);
		issueField = new IssueField("40", "Test_Automation", null, new JSONArray(jsonArrayList1));
		issueFieldList.add(issueField);

		issueField = new IssueField("", "Fix Version", null, "KnowHowv6.7");
		issueFieldList.add(issueField);

		issueField = new IssueField("", "Due_Date", null, "");
		issueFieldList.add(issueField);

		issueField = new IssueField("parent", "Due_Date", null, jsonObject1);
		issueFieldList.add(issueField);

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
			Method method = JiraIssueProcessorImpl.class.getDeclaredMethod("setEpicIssueData", FieldMapping.class, JiraIssue.class, Map.class);
			method.setAccessible(true);

			// Act
			method.invoke(transformFetchedIssueToJiraIssue, fieldMapping, jiraIssue, fields);

			// Assert
//			assertEquals( /* expected value */, jiraIssue.getJobSize(), 0.001); // Add assertions for other fields
	}

	@Test
	public void testSetSubTaskLinkage() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		// Arrange
		FieldMapping fieldMapping=new FieldMapping();
		fieldMapping.setJiraSubTaskIdentification(Arrays.asList("Story"));

		Map<String, IssueField> fields = new HashMap<>();
		fields.put("8.0", new IssueField("", "8.0", null, "8.0"));

		// Set up other mocks and required behaviors

		// Use reflection to access the private method
		Method method = JiraIssueProcessorImpl.class.getDeclaredMethod("setSubTaskLinkage", JiraIssue.class, FieldMapping.class, Issue.class, Map.class);
		method.setAccessible(true);

		JiraIssue jiraIssue=new JiraIssue();
		jiraIssue.setTypeName("Story");
		// Act
		method.invoke(transformFetchedIssueToJiraIssue, jiraIssue, fieldMapping, issues.get(0), new HashMap<>());

		// Assert
		// Add assertions based on the expected behavior of your method
//		verify(jiraIssueMock).setParentStoryId(anySet());
	}

	@Test
	public void testExcludeLinks() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		when(jiraProcessorConfig.getExcludeLinks()).thenReturn(Arrays.asList("xyz"));
		Method method = JiraIssueProcessorImpl.class.getDeclaredMethod("excludeLinks", Issue.class, Set.class);
		method.setAccessible(true);
		method.invoke(transformFetchedIssueToJiraIssue, issues.get(0), new HashSet<>());
	}

	@Test
	public void testSetJiraAssigneeDetailsWhenUserIsNull() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Method method = JiraIssueProcessorImpl.class.getDeclaredMethod("setJiraAssigneeDetails", JiraIssue.class, User.class, ProjectConfFieldMapping.class);
		method.setAccessible(true);
		method.invoke(transformFetchedIssueToJiraIssue, new JiraIssue(), null,projectConfFieldMapping);
	}

	@Test
	public void testGetRootCauses() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Method method = JiraIssueProcessorImpl.class.getDeclaredMethod("getRootCauses", FieldMapping.class, Map.class);
		method.setAccessible(true);
		FieldMapping fieldMapping=new FieldMapping();
		fieldMapping.setRootCause("code_issue");
		Map<String,String> map = new HashMap<>();
		map.put("self", "https://jiradomain.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "code");
		map.put("id", "19121");
		JSONObject jsonObject = new JSONObject(map);
		List<Object> rcaList = new ArrayList<>();
		rcaList.add(jsonObject);
		IssueField issueField = new IssueField("customfield_19121", "code_issue", null, new JSONArray(rcaList));
		Map<String,IssueField> fields = new HashMap<>();
		fields.put("code_issue",issueField);

		method.invoke(transformFetchedIssueToJiraIssue,fieldMapping,fields);
	}

	@Test
	public void testProcessSprintData() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Method method = JiraIssueProcessorImpl.class.getDeclaredMethod("processSprintData", JiraIssue.class, IssueField.class, ProjectConfFieldMapping.class);
		method.setAccessible(true);
		method.invoke(transformFetchedIssueToJiraIssue, new JiraIssue(), null, projectConfFieldMapping);
	}
}