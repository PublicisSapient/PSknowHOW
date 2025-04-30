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

package com.publicissapient.kpidashboard.jira.service;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
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
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.BeanUtils;

import com.atlassian.jira.rest.client.api.SearchRestClient;
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
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.Visibility;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.ErrorDetail;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectVersion;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.jira.client.CustomAsynchronousIssueRestClient;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.dataFactories.ConnectionsDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.ProjectBasicConfigDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.ToolConfigDataFactory;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

import io.atlassian.util.concurrent.Promise;

@RunWith(MockitoJUnitRunner.class)
public class JiraCommonServiceTest {

	@Mock
	private JiraProcessorConfig jiraProcessorConfig;

	@Mock
	SearchRestClient searchRestClient;

	@Mock
	CustomAsynchronousIssueRestClient customAsynchronousIssueRestClient;

	@Mock
	Promise<SearchResult> promisedRs;

	SearchResult searchResult;

	@Mock
	private ToolCredentialProvider toolCredentialProvider;

	@Mock
	private AesEncryptionService aesEncryptionService;

	@Mock
	private JiraToolConfig jiraToolConfig;

	@InjectMocks
	JiraCommonService jiraCommonService;

	@Mock
	ProcessorJiraRestClient jiraRestClient;

	@Mock
	KerberosClient krb5Client;
	@Mock
	private SearchResult mockSearchResult;
	@Mock
	private StepContext mockStepContext;
	@Mock
	private JobExecution mockJobExecution;
	@Mock
	private ExecutionContext mockExecutionContext;
	@Mock
	private StepExecution mockStepExecution;
	@Mock
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;


	private ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
	@Mock
	private ProjectConfFieldMapping projectConfFieldMapping1;
	@Mock
	private ProcessorToolConnectionService processorToolConnectionService;

	List<ProjectBasicConfig> projectConfigsList;
	List<ProjectToolConfig> projectToolConfigsJQL;
	List<ProjectToolConfig> projectToolConfigsBoard;
	Optional<Connection> connection;
	FieldMapping fieldMapping = new FieldMapping();

	List<Issue> issues = new ArrayList<>();

	// @Rule
	// public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		projectConfigsList = getMockProjectConfig();
		connection = getMockConnection();
		fieldMapping = getMockFieldMapping();
		createIssue();
		// when(jiraProcessorConfig.getAesEncryptionKey()).thenReturn("AesEncryptionKey");
		// when(aesEncryptionService.decrypt(ArgumentMatchers.anyString(),
		// ArgumentMatchers.anyString()))
		// .thenReturn("PLAIN_TEXT_PASSWORD");
	}

	@Test
	public void fetchIssuesBasedOnJqlTest() throws InterruptedException {
		projectToolConfigsJQL = getMockProjectToolConfig("63c04dc7b7617e260763ca4e");
		createProjectConfigMap(true);
		Mockito.when(jiraProcessorConfig.getPageSize()).thenReturn(50);
		when(jiraRestClient.getProcessorSearchClient()).thenReturn(searchRestClient);
		when(searchRestClient.searchJql(anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anySet()))
				.thenReturn(promisedRs);
		when(promisedRs.claim()).thenReturn(searchResult);
		String deltaDate = "2023-10-20 08:22";
		List<Issue> issues = jiraCommonService.fetchIssuesBasedOnJql(projectConfFieldMapping, jiraRestClient, 50,
				deltaDate);
		Assert.assertEquals(2, issues.size());
	}

	@Test
	public void fetchIssueBasedOnBoardTest() throws InterruptedException, IOException {
		projectToolConfigsBoard = getMockProjectToolConfig("63bfa0d5b7617e260763ca21");
		createProjectConfigMap(false);
		Mockito.when(jiraProcessorConfig.getPageSize()).thenReturn(50);
		when(jiraRestClient.getCustomIssueClient()).thenReturn(customAsynchronousIssueRestClient);
		when(customAsynchronousIssueRestClient.searchBoardIssue(anyString(), anyString(), Mockito.anyInt(),
				Mockito.anyInt(), Mockito.anySet())).thenReturn(promisedRs);
		when(promisedRs.claim()).thenReturn(searchResult);
		String deltaDate = "2023-10-20 08:22";
		List<Issue> issues = jiraCommonService.fetchIssueBasedOnBoard(projectConfFieldMapping, jiraRestClient, 50,
				"1111", deltaDate);
		Assert.assertEquals(2, issues.size());
	}

	@Test
	public void getVersionTest() throws IOException, ParseException {
		projectToolConfigsBoard = getMockProjectToolConfig("63bfa0d5b7617e260763ca21");
		createProjectConfigMap(false);
		when(jiraProcessorConfig.getJiraVersionApi()).thenReturn("rest/api/7/project/{projectKey}/versions");
		URL mockedUrl = new URL("https://www.testurl.in/");
		Connection connection1 = new Connection();
		connection1.setBaseUrl("https://www.testurl.in/");
		when(projectConfFieldMapping1.getJira()).thenReturn(jiraToolConfig);
		when(jiraToolConfig.getConnection()).thenReturn(Optional.of(connection1));
		when(jiraToolConfig.getProjectKey()).thenReturn("1234567");
		HttpURLConnection mockedConnection = mock(HttpURLConnection.class);
		String responseData = "Sample response data";
		InputStream inputStream = new ByteArrayInputStream(responseData.getBytes(StandardCharsets.UTF_8));
		assertThrows(Exception.class, () -> {
			jiraCommonService.getVersion(projectConfFieldMapping1, krb5Client);
		});
//		Assert.assertEquals(0, versions.size());
	}
	// @Test(expected = RestClientException.class)
	// public void getVersionTest1() throws IOException, ParseException {
	// projectToolConfigsBoard =
	// getMockProjectToolConfig("63bfa0d5b7617e260763ca21");
	// createProjectConfigMap(false);
	// when(jiraProcessorConfig.getJiraVersionApi()).thenReturn("rest/api/7/project/{projectKey}/versions");
	// URL mockedUrl = new URL("https://www.testurl.com/");
	// Connection connection1 = new Connection();
	// connection1.setBaseUrl("https://www.testurl.com/");
	// when(projectConfFieldMapping1.getJira()).thenThrow(new
	// RestClientException(new RestClientException(new Exception())));
	// }

	@Test
	public void testGetApiHost() {
		when(jiraProcessorConfig.getUiHost()).thenReturn("localhost");
		try {
			jiraCommonService.getApiHost();
		} catch (UnknownHostException e) {

		}

	}

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
		map.put("self", "https://jiradomain.com/jira/rest/api/2/customFieldOption/20810");
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
		ChangelogItem changelogItem = new ChangelogItem(FieldType.JIRA, "field1", "from", "fromString", "to",
				"toString");
		ChangelogGroup changelogGroup = new ChangelogGroup(basicUser, DateTime.now(), Arrays.asList(changelogItem));

		Issue issue = new Issue("summary1", new URI("self"), "key1", 1l, basicProj, issueType1, status1, "story",
				basicPriority, resolution, new ArrayList<>(), user1, user1, DateTime.now(), DateTime.now(),
				DateTime.now(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null, issueFields, comments,
				null, createIssueLinkData(), basicVotes, workLogs, null, Arrays.asList("expandos"), null,
				Arrays.asList(changelogGroup), null, new HashSet<>(Arrays.asList("label1")));
		Issue issue1 = new Issue("summary1", new URI("self"), "key1", 1l, basicProj, issueType2, status1, "Defect",
				basicPriority, resolution, new ArrayList<>(), user1, user1, DateTime.now(), DateTime.now(),
				DateTime.now(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null, issueFields, comments,
				null, createIssueLinkData(), basicVotes, workLogs, null, Arrays.asList("expandos"), null,
				Arrays.asList(changelogGroup), null, new HashSet<>(Arrays.asList("label1")));
		issues.add(issue);
		issues.add(issue1);

		searchResult = new SearchResult(0, 10, 2, issues);

	}

	private List<IssueLink> createIssueLinkData() throws URISyntaxException {
		List<IssueLink> issueLinkList = new ArrayList<>();
		URI uri = new URI("https://testDomain.com/jira/rest/api/2/issue/12344");
		IssueLinkType linkType = new IssueLinkType("Blocks", "blocks", IssueLinkType.Direction.OUTBOUND);
		IssueLink issueLink = new IssueLink("IssueKey", uri, linkType);
		issueLinkList.add(issueLink);

		return issueLinkList;
	}

	private void createProjectConfigMap(boolean jql) {
		ProjectBasicConfig projectConfig = projectConfigsList.get(1);
		BeanUtils.copyProperties(projectConfig, projectConfFieldMapping);
		projectConfFieldMapping.setProjectBasicConfig(projectConfig);
		projectConfFieldMapping.setKanban(projectConfig.getIsKanban());
		projectConfFieldMapping.setBasicProjectConfigId(projectConfig.getId());
		if (jql) {
			projectConfFieldMapping.setJira(getJiraToolConfig(true));
			projectConfFieldMapping.setJiraToolConfigId(projectToolConfigsJQL.get(0).getId());
			projectConfFieldMapping.setProjectToolConfig(projectToolConfigsJQL.get(0));
		} else {
			projectConfFieldMapping.setJira(getJiraToolConfig(false));
			projectConfFieldMapping.setJiraToolConfigId(projectToolConfigsBoard.get(0).getId());
			projectConfFieldMapping.setProjectToolConfig(projectToolConfigsBoard.get(0));
		}
		projectConfFieldMapping.setFieldMapping(fieldMapping);

	}

	private JiraToolConfig getJiraToolConfig(boolean jql) {
		JiraToolConfig toolObj = new JiraToolConfig();
		if (jql) {
			BeanUtils.copyProperties(projectToolConfigsJQL.get(0), toolObj);
		} else {
			BeanUtils.copyProperties(projectToolConfigsBoard.get(0), toolObj);
		}
		toolObj.setConnection(connection);
		return toolObj;
	}

	private Optional<Connection> getMockConnection() {
		ConnectionsDataFactory connectionDataFactory = ConnectionsDataFactory
				.newInstance("/json/default/connections.json");
		return connectionDataFactory.findConnectionById("5fd99f7bc8b51a7b55aec836");
	}

	private List<ProjectBasicConfig> getMockProjectConfig() {
		ProjectBasicConfigDataFactory projectConfigDataFactory = ProjectBasicConfigDataFactory
				.newInstance("/json/default/project_basic_configs.json");
		return projectConfigDataFactory.getProjectBasicConfigs();
	}

	private List<ProjectToolConfig> getMockProjectToolConfig(String basicConfigId) {
		ToolConfigDataFactory projectToolConfigDataFactory = ToolConfigDataFactory
				.newInstance("/json/default/project_tool_configs.json");
		return projectToolConfigDataFactory.findByToolNameAndBasicProjectConfigId(ProcessorConstants.JIRA,
				basicConfigId);
	}

	private FieldMapping getMockFieldMapping() {
		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/field_mapping.json");
		return fieldMappingDataFactory.findByBasicProjectConfigId("63c04dc7b7617e260763ca4e");
	}

	@Test
	public void testGetApiHostWithUiHost() throws UnknownHostException {
		// Arrange
		when(jiraProcessorConfig.getUiHost()).thenReturn("example.com");
		String expected = "https:\\\\example.com";

		// Act
		String result = jiraCommonService.getApiHost();

		// Assert
		assertTrue(result.contains("example.com"));
	}

	@Test
	public void testGetApiHostWithoutUiHost() {
		// Arrange
		when(jiraProcessorConfig.getUiHost()).thenReturn(null);

		// Act and Assert
		UnknownHostException exception = assertThrows(UnknownHostException.class, () -> {
			jiraCommonService.getApiHost();
		});

		assertEquals("Api host not found in properties.", exception.getMessage());
	}

	@Test
	public void testParseVersionData()
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ParseException {
		// Define sample dataFromServer
		String dataFromServer = "[{\"id\":\"1\",\"name\":\"Version 1\",\"archived\":\"false\",\"released\":\"true\",\"startDate\":\"2022-01-01\",\"releaseDate\":\"2022-02-01\"}]";

		// Create a List to hold the parsed ProjectVersion objects
		List<ProjectVersion> projectVersionDetailList = new ArrayList<>();

		// Get the private method using reflection
		Method parseVersionData = JiraCommonService.class.getDeclaredMethod("parseVersionData", String.class,
				List.class);
		parseVersionData.setAccessible(true);

		// Invoke the private method
		parseVersionData.invoke(jiraCommonService, dataFromServer, projectVersionDetailList);

		// Assert the results
		assertEquals(1, projectVersionDetailList.size());
		ProjectVersion projectVersion = projectVersionDetailList.get(0);
		assertEquals("Version 1", projectVersion.getName());
		assertEquals(false, projectVersion.isArchived());
		assertEquals(true, projectVersion.isReleased());
		// Add more assertions based on your data
	}

	@Test
	public void testParseVersionData1()
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ParseException {
		// Define sample dataFromServer
		String dataFromServer = "[{\"id\":\"1\",\"archived\":\"false\",\"released\":\"true\",\"startDate\":\"2022-01-01\",\"releaseDate\":\"2022-02-01\"}]";

		// Create a List to hold the parsed ProjectVersion objects
		List<ProjectVersion> projectVersionDetailList = new ArrayList<>();

		// Get the private method using reflection
		Method parseVersionData = JiraCommonService.class.getDeclaredMethod("parseVersionData", String.class,
				List.class);
		parseVersionData.setAccessible(true);

		// Invoke the private method
		parseVersionData.invoke(jiraCommonService, dataFromServer, projectVersionDetailList);

		// Assert the results
		assertEquals(1, projectVersionDetailList.size());
		ProjectVersion projectVersion = projectVersionDetailList.get(0);
		assertEquals(false, projectVersion.isArchived());
		assertEquals(true, projectVersion.isReleased());
		// Add more assertions based on your data
	}

	@Test
	public void testParseVersionData2()
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ParseException {
		// Define sample dataFromServer
		String dataFromServer = "[{\"id\"jm,ndzmn:\"1\",\"name\":\"Version 1\",\"archived\":\"false\",\"released\":\"true\",\"startDate\":\"2022-01-01\",\"releaseDate\":\"2022-02-01\"}]";

		// Create a List to hold the parsed ProjectVersion objects
		List<ProjectVersion> projectVersionDetailList = new ArrayList<>();

		// Get the private method using reflection
		Method parseVersionData = JiraCommonService.class.getDeclaredMethod("parseVersionData", String.class,
				List.class);
		parseVersionData.setAccessible(true);

		assertThrows(Exception.class,
				() -> parseVersionData.invoke(jiraCommonService, dataFromServer, projectVersionDetailList));

	}

	@Test
	public void testParseVersionData3()
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ParseException {
		// Define sample dataFromServer
		String dataFromServer = "";

		// Create a List to hold the parsed ProjectVersion objects
		List<ProjectVersion> projectVersionDetailList = new ArrayList<>();

		// Get the private method using reflection
		Method parseVersionData = JiraCommonService.class.getDeclaredMethod("parseVersionData", String.class,
				List.class);
		parseVersionData.setAccessible(true);

	}

	@Test
	public void testSaveSearchDetailsInContext() {
		// Arrange
		int pageStart = 0; // Set your desired page start value
		int pageSize = 10; // Set your desired page size
		int totalIssues = 100; // Set your desired total issues count
		String boardId = "test id";
		when(mockStepContext.getStepExecution()).thenReturn(mockStepExecution);
		when(mockStepExecution.getJobExecution()).thenReturn(mockJobExecution);
		when(mockJobExecution.getExecutionContext()).thenReturn(mockExecutionContext);
		when(mockSearchResult.getTotal()).thenReturn(totalIssues);
		// Act
		jiraCommonService.saveSearchDetailsInContext(mockSearchResult, pageStart,boardId, mockStepContext);

		// Assert
		verify(mockExecutionContext).putInt(JiraConstants.TOTAL_ISSUES, totalIssues);
		verify(mockExecutionContext).putInt(JiraConstants.PAGE_START, pageStart);
	}

	@Test
	public void testProcessClientError() throws IOException {
		// Arrange
		int responseCode = 401;
		String errorMessage = "Not Found";
		String contentMsg = "dummy content message";

		HttpURLConnection mockConnection = mock(HttpURLConnection.class);
		URL mockUrl = mock(URL.class);
		when(mockConnection.getResponseCode()).thenReturn(responseCode);
		when(mockConnection.getErrorStream()).thenReturn(new ByteArrayInputStream(errorMessage.getBytes(StandardCharsets.UTF_8)));
		when(mockUrl.openConnection()).thenReturn(mockConnection);

		Connection mockConnectionObject = mock(Connection.class);
		when(mockConnectionObject.getId()).thenReturn(new ObjectId("668517f812811950be19353f"));
		Optional<Connection> connectionOptional = Optional.of(mockConnectionObject);

		// Act
		Exception exception = assertThrows(IOException.class, () -> {
			jiraCommonService.getDataFromServer(mockUrl, connectionOptional, new ObjectId("668517f812811950be19353f"));
		});

		// Assert
		String expectedMessage = "Error: 401 - Not Found";
		assertEquals(expectedMessage, exception.getMessage());
	}

	@Test
	public void testGetDataFromServerWith404Error() throws IOException {
		// Arrange
		int responseCode = 404;
		String errorMessage = "Not Found";
		String contentMsg = "dummy content message";

		HttpURLConnection mockConnection = mock(HttpURLConnection.class);
		URL mockUrl = mock(URL.class);
		when(mockUrl.toString()).thenReturn("http://example.com");
		when(mockConnection.getURL()).thenReturn(mockUrl);
		when(mockConnection.getResponseCode()).thenReturn(responseCode);
		when(mockConnection.getErrorStream()).thenReturn(new ByteArrayInputStream(errorMessage.getBytes(StandardCharsets.UTF_8)));
		when(mockConnection.getContent()).thenReturn(new ByteArrayInputStream(contentMsg.getBytes(StandardCharsets.UTF_8)));
		when(mockUrl.openConnection()).thenReturn(mockConnection);
		ProcessorExecutionTraceLog mockTraceLog = mock(ProcessorExecutionTraceLog.class);
		List<ErrorDetail> mockErrorDetailList = new ArrayList<>();
		when(mockTraceLog.getErrorDetailList()).thenReturn(mockErrorDetailList);
		Optional<ProcessorExecutionTraceLog> existingTraceLog = Optional.of(mockTraceLog);
		Connection mockConnectionObject = mock(Connection.class);
		when(mockConnectionObject.getId()).thenReturn(new ObjectId("668517f812811950be19353f"));
		when(processorExecutionTraceLogRepository
				.findByProcessorNameAndBasicProjectConfigIdAndProgressStatsTrue(anyString(),any())).thenReturn(existingTraceLog);
		Optional<Connection> connectionOptional = Optional.of(mockConnectionObject);

		// Act
		String result = jiraCommonService.getDataFromServer(mockUrl, connectionOptional, new ObjectId("668517f812811950be19353f"));
		// Assert
		assertEquals(contentMsg, result);
		verify(processorExecutionTraceLogRepository).save(mockTraceLog);
	}}
