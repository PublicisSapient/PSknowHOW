package com.publicissapient.kpidashboard.jiratest.processor.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.zephyr.TestCaseDetails;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.jiratest.adapter.impl.async.impl.ProcessorAsynchSearchRestClient;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.repository.zephyr.TestCaseDetailsRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.jiratest.adapter.helper.JiraRestClientFactory;
import com.publicissapient.kpidashboard.jiratest.adapter.impl.async.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jiratest.config.JiraTestProcessorConfig;
import com.publicissapient.kpidashboard.jiratest.model.JiraInfo;
import com.publicissapient.kpidashboard.jiratest.model.JiraTestProcessor;
import com.publicissapient.kpidashboard.jiratest.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jiratest.oauth.JiraOAuthProperties;
import com.publicissapient.kpidashboard.jiratest.repository.JiraTestProcessorRepository;

import io.atlassian.util.concurrent.Promise;
import org.springframework.web.client.RestClientException;

@ExtendWith(SpringExtension.class)
class JiraTestServiceImplTest {

	@InjectMocks
	JiraTestServiceImpl jiraTestServiceImpl;
	@Mock
	ProcessorJiraRestClient client;
	@Mock
	SearchRestClient searchRestClient;
	@Mock
	Promise<SearchResult> promisedRs;
	Iterable<Issue> issueIterable;
	List<Issue> issues = new ArrayList<>();
	@Mock
	private TestCaseDetailsRepository testCaseDetailsRepository;
	@Mock
	private JiraTestProcessorConfig jiraTestProcessorConfig;
	@Mock
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;
	@Mock
	private AesEncryptionService aesEncryptionService;
	@Mock
	private JiraTestProcessorRepository jiraTestProcessorRepository;
	@Mock
	private JiraRestClientFactory jiraRestClientFactory;
	@Mock
	private JiraOAuthProperties jiraOAuthProperties;
	@Mock
	private ToolCredentialProvider toolCredentialProvider;

	@Mock
	private ConnectionRepository connectionRepository;
	@Mock
	private ProjectConfFieldMapping projectConfig;
	@Mock
	private ProcessorToolConnection processorToolConnection;
	@Mock
	private ProcessorAsynchSearchRestClient processorSearchClient;
	@Mock
	private Map<String, IssueField> customFieldMap;
	@Mock
	private ProcessorToolConnection jiraTestToolInfo;

	private static ProjectConfFieldMapping getProjectConfFieldMapping() {
		ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
		projectConfFieldMapping.setProjectKey("XYZ");
		projectConfFieldMapping.setProjectName("JIRA TEST Scrum");
		projectConfFieldMapping.setBasicProjectConfigId(new ObjectId("625fd013572701449a44b3de"));
		projectConfFieldMapping.setKanban(false);
		projectConfFieldMapping.setProcessorToolConnection(getJiraToolConfig());
		return projectConfFieldMapping;
	}

	private static ProcessorToolConnection getJiraToolConfig() {
		ProcessorToolConnection toolInfo = new ProcessorToolConnection();
		toolInfo.setId(new ObjectId("625fd013572701449a44b556"));
		toolInfo.setBasicProjectConfigId(new ObjectId("625fd013572701449a44b3de"));
		toolInfo.setToolName(ProcessorConstants.JIRA_TEST);
		toolInfo.setUrl("https://abc.com/jira");
		toolInfo.setApiEndPoint("rest/api/2/");
		toolInfo.setUsername("test");
		toolInfo.setPassword("password");
		toolInfo.setProjectKey("testProjectKey");
		toolInfo.setConnectionId(new ObjectId("625d0d9d10ce157f45918b5c"));
		String[] testCaseType = new String[2];
		testCaseType[0] = "Test";
		testCaseType[1] = "TestCase";
		toolInfo.setTestAutomatedIdentification("CustomField");
		toolInfo.setJiraTestCaseType(testCaseType);
		toolInfo.setTestAutomationCompletedIdentification("CustomField");
		toolInfo.setTestRegressionIdentification("Labels");
		toolInfo.setTestAutomationCompletedByCustomField("customfield_43701");
		toolInfo.setTestRegressionByCustomField("customfield_43702");
		List<String> automatedTestValue = new ArrayList<>();
		automatedTestValue.add("Automation");
		toolInfo.setJiraAutomatedTestValue(automatedTestValue);
		List<String> testRegressionValue = new ArrayList<>();
		testRegressionValue.add("RegressionLabel");
		toolInfo.setJiraRegressionTestValue(testRegressionValue);
		List<String> canBeAutomatedTestValue = new ArrayList<>();
		canBeAutomatedTestValue.add("Y");
		toolInfo.setJiraCanBeAutomatedTestValue(canBeAutomatedTestValue);
		List<String> testCaseStatus = new ArrayList<>();
		testCaseStatus.add("Abandoned");
		toolInfo.setTestCaseStatus(testCaseStatus);
		toolInfo.setCloudEnv(false);
		return toolInfo;
	}

	@Test
	void processesJiraIssues() {
		ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
		projectBasicConfig.setId(new ObjectId("632eb205e0fd283f9bb747ad"));

		ProjectConfFieldMapping projectConfFieldMapping = getProjectConfFieldMapping();

		when(jiraTestProcessorConfig.getAesEncryptionKey()).thenReturn("AesEncryptionKey");
		when(aesEncryptionService.decrypt(anyString(), anyString())).thenReturn("PLAIN_TEXT_PASSWORD");
		JiraInfo jiraInfo = JiraInfo.builder()
				.jiraConfigBaseUrl(projectConfFieldMapping.getProcessorToolConnection().getUrl())
				.username(projectConfFieldMapping.getProcessorToolConnection().getUsername())
				.password("PLAIN_TEXT_PASSWORD").jiraConfigProxyUrl(null).jiraConfigProxyPort(null).build();

		when(jiraRestClientFactory.getJiraClient(jiraInfo)).thenReturn(client);
		when(jiraTestProcessorConfig.getStartDate()).thenReturn("2020-01-01T00:00:00.0000000");
		when(jiraTestProcessorConfig.getMinsToReduce()).thenReturn(30L);
		when(jiraTestProcessorConfig.getPageSize()).thenReturn(30);
		when(client.getProcessorSearchClient()).thenReturn(searchRestClient);
		when(searchRestClient.searchJql(anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anySet()))
				.thenReturn(promisedRs);
		SearchResult sr = Mockito.mock(SearchResult.class);
		when(promisedRs.claim()).thenReturn(sr);
		prepareIssuesData();
		when(sr.getIssues()).thenReturn(issueIterable);

		JiraTestProcessor jiraProcessor = new JiraTestProcessor();
		Optional<Connection> connectionOptional = Optional.ofNullable(new Connection());
		when(jiraTestProcessorConfig.getJiraServerGetUserApi()).thenReturn("user/search?username=");
		// when(getUserTimeZone(projectConfFieldMapping)).thenReturn("Indian/Maldives");
		when(testCaseDetailsRepository.findTopByBasicProjectConfigId(any())).thenReturn(null);
		when(jiraTestProcessorRepository.findByProcessorName(Mockito.anyString())).thenReturn(jiraProcessor);
		doNothing().when(processorExecutionTraceLogService).save(Mockito.any());
		when(connectionRepository.findById(any())).thenReturn(connectionOptional);
		assertEquals(0, jiraTestServiceImpl.processesJiraIssues(projectConfFieldMapping));
	}

	private void prepareIssuesData() {

		Set<String> labelSet1 = new HashSet<>();
		labelSet1.add("Labels1");
		labelSet1.add("testLabels");
		labelSet1.add("RegressionLabel");
		Set<String> labelSet2 = new HashSet<>();
		labelSet2.add("Labels2");

		List<String> automatedTestValue = new ArrayList<>();
		automatedTestValue.add("Automation");
		List<String> canBeAutomatedTestValue = new ArrayList<>();
		canBeAutomatedTestValue.add("Y");
		List<IssueField> issuesFields = new ArrayList<>();
		IssueField customIssueField1 = new IssueField("customfield_43701", "Automation Field", "List",
				automatedTestValue);
		IssueField customIssueField2 = new IssueField("customfield_43702", "Can Be Automation", "List",
				canBeAutomatedTestValue);
		issuesFields.add(customIssueField1);
		issuesFields.add(customIssueField2);
		Iterable<IssueField> issueFieldIterable = new Iterable<IssueField>() {
			@Override
			public Iterator<IssueField> iterator() {
				return issuesFields.iterator();
			}
		};

		Issue issue1 = new Issue("summary 1", null, "XYZ-1", 101L, null,
				new IssueType(null, 11L, "Test", true, "Description 1", null),
				new Status(null, null, "Open", null, null, null), "description", null, null, null, null, null,
				DateTime.now(), DateTime.now(), null, null, null, null, null, issuesFields, null, null, null, null,
				null, null, null, null, null, null, labelSet1);
		Issue issue2 = new Issue("summary", null, "XYZ-2", 102L, null,
				new IssueType(null, 11L, "TestCase", true, "Description 2", null),
				new Status(null, null, "In Process", null, null, null), "description", null, null, null, null, null,
				DateTime.now(), DateTime.now(), null, null, null, null, null, issuesFields, null, null, null, null,
				null, null, null, null, null, null, labelSet1);
		Issue issue3 = new Issue("summary", null, "XYZ-3", 103L, null,
				new IssueType(null, 11L, "Test", true, "Description 3", null),
				new Status(null, null, "In Testing", null, null, null), "description", null, null, null, null, null,
				DateTime.now(), DateTime.now(), null, null, null, null, null, issuesFields, null, null, null, null,
				null, null, null, null, null, null, labelSet2);
		Issue issue4 = new Issue("summary", null, "XYZ-4", 104L, null,
				new IssueType(null, 11L, "TestCase", true, "Description 4", null),
				new Status(null, null, "Pending", null, null, null), "description", null, null, null, null, null,
				DateTime.now(), DateTime.now(), null, null, null, null, null, issuesFields, null, null, null, null,
				null, null, null, null, null, null, labelSet1);
		Issue issue5 = new Issue("summary", null, "XYZ-5", 105L, null,
				new IssueType(null, 11L, "Test", true, "Description 5", null),
				new Status(null, null, "Abandoned", null, null, null), "description", null, null, null, null, null,
				DateTime.now(), DateTime.now(), null, null, null, null, null, issuesFields, null, null, null, null,
				null, null, null, null, null, null, labelSet1);
		issues.add(issue1);
		issues.add(issue2);
		issues.add(issue3);
		issueIterable = new Iterable<Issue>() {
			@Override
			public Iterator<Issue> iterator() {
				return issues.iterator();
			}
		};
		SearchResult searchResult = new SearchResult(0, 0, 1, issueIterable);
	}

	@Test
	void testGetUserTimeZoneRestClientException() {
		// Mock the behavior of projectConfig
		when(projectConfig.getProcessorToolConnection()).thenReturn(processorToolConnection);

		// Mock RestClientException behavior
		when(processorToolConnection.getUrl()).thenThrow(new RestClientException("Rest client error"));

		// Call the method you want to test, and expect an exception
		RestClientException exception = assertThrows(RestClientException.class, () -> {
			jiraTestServiceImpl.getUserTimeZone(projectConfig);
		});

		// Optionally, you can assert on the exception message or other details
		Assertions.assertEquals("Rest client error", exception.getMessage());

		// Verify that expected methods were called with the expected arguments
		verify(projectConfig, times(1)).getProcessorToolConnection();
	}

	@Test
	public void testGetIssues() {

		// Create a sample ProjectConfFieldMapping
		ProjectConfFieldMapping projectConfig = new ProjectConfFieldMapping();
		projectConfig.setProjectKey("YourProjectKey");

		// Create a sample startDateTimeByIssueType map
		Map<String, LocalDateTime> startDateTimeByIssueType = new HashMap<>();
		LocalDateTime localDateTime = LocalDateTime.now();
		startDateTimeByIssueType.put("issueType1", localDateTime);

		// Create a sample userTimeZone
		String userTimeZone = "UTC";

		// Mock the behavior of client and related objects
		when(client.getProcessorSearchClient()).thenReturn(processorSearchClient);
		when(processorSearchClient.searchJql(anyString(), anyInt(), anyInt(), any())).thenReturn(promisedRs);

		// Call the method you want to test
		SearchResult searchResult = jiraTestServiceImpl.getIssues(projectConfig, startDateTimeByIssueType, userTimeZone, 0, false, client);
		// Verify that expected methods were called with the expected arguments
		verify(client, times(1)).getProcessorSearchClient();
		verify(processorSearchClient, times(1)).searchJql(anyString(), anyInt(), anyInt(), any());
	}

	@Test
	public void testGetIssuesRestClientException() {

		// Create a sample ProjectConfFieldMapping
		ProjectConfFieldMapping projectConfig = new ProjectConfFieldMapping();
		projectConfig.setProjectKey("YourProjectKey");

		// Create a sample startDateTimeByIssueType map
		Map<String, LocalDateTime> startDateTimeByIssueType = new HashMap<>();
		java.time.LocalDateTime localDateTime = LocalDateTime.now();
		startDateTimeByIssueType.put("issueType1", localDateTime);

		// Create a sample userTimeZone
		String userTimeZone = "UTC";

		// Mock the behavior of client to throw a RestClientException
		when(client.getProcessorSearchClient()).thenThrow(new RestClientException("Rest client error"));

		// Call the method you want to test, and expect an exception
		RestClientException exception = assertThrows(RestClientException.class, () -> {
			jiraTestServiceImpl.getIssues(projectConfig, startDateTimeByIssueType, userTimeZone, 0, false, client);
		});

		// Optionally, you can assert on the exception message or other details
		assertEquals("Rest client error", exception.getMessage());

		// Verify that expected methods were called with the expected arguments
		verify(client, times(1)).getProcessorSearchClient();
	}

	private SearchResult createSampleSearchResult() {
		// Create and return a sample SearchResult object for testing
		// You can customize this based on your test case
		return new SearchResult(0, 0, 1, issueIterable);
	}

	@Test
	public void testSetRegressionLabel() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

		// Create a sample TestCaseDetails object
		TestCaseDetails testCaseDetails = new TestCaseDetails();
		testCaseDetails.setLabels(Collections.singletonList("existingLabel"));

		// Mock the behavior of jiraTestToolInfo
		List<String> testRegressionValue = new ArrayList<>();
		testRegressionValue.add("RegressionLabel");
		when(jiraTestToolInfo.getJiraRegressionTestValue()).thenReturn(testRegressionValue);
		when(jiraTestToolInfo.getTestRegressionByCustomField()).thenReturn("customField");

		// Create an instance of the private method using reflection
		Method setRegressionLabelMethod = JiraTestServiceImpl.class.getDeclaredMethod("setRegressionLabel",
				ProcessorToolConnection.class, Map.class, TestCaseDetails.class);
		setRegressionLabelMethod.setAccessible(true);

		// Invoke the private method
		setRegressionLabelMethod.invoke(jiraTestServiceImpl, getJiraToolConfig(), customFieldMap, testCaseDetails);

		// Perform assertions based on your expected behavior
		List<String> expectedLabels = Arrays.asList("existingLabel", "value1", "value2");
		Assertions.assertNotNull(testCaseDetails.getLabels());
	}

	@Test
	public void testEncodeCredentialsToBase64() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		// Define test input values
		String username = "testUsername";
		String password = "testPassword";

		// Create an instance of the private method using reflection
		Method encodeCredentialsToBase64Method = JiraTestServiceImpl.class.getDeclaredMethod("encodeCredentialsToBase64",
				String.class, String.class);
		encodeCredentialsToBase64Method.setAccessible(true);

		// Invoke the private method
		String encodedCredentials = (String) encodeCredentialsToBase64Method.invoke(jiraTestServiceImpl, username, password);

		// Perform assertions based on your expected behavior
		String expectedEncodedCredentials = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
		Assertions.assertEquals(expectedEncodedCredentials, encodedCredentials);
	}

	@Test
	public void testFindLastSavedTestCaseDetailsByType() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		// Define test input values
		List<TestCaseDetails> testCaseDetails = new ArrayList<>();
		// Add test data to testCaseDetails as needed

		Map<String, LocalDateTime> lastSavedTestCasesChangedDateByType = new HashMap<>();
		// Add test data to lastSavedTestCasesChangedDateByType as needed

		// Create an instance of the private method using reflection
		Method findLastSavedTestCaseDetailsByTypeMethod = JiraTestServiceImpl.class.getDeclaredMethod("findLastSavedTestCaseDetailsByType",
				List.class, Map.class);
		findLastSavedTestCaseDetailsByTypeMethod.setAccessible(true);

		// Invoke the private method
		findLastSavedTestCaseDetailsByTypeMethod.invoke(jiraTestServiceImpl, testCaseDetails, lastSavedTestCasesChangedDateByType);

		// Perform assertions based on your expected behavior
		// Modify this assertion based on your actual requirements
		Assertions.assertNotNull(lastSavedTestCasesChangedDateByType);

		// Verify that expected methods were called with the expected arguments
		// Add verification steps as needed
	}

	@Test
	public void testUpdatedDateToSave() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

		// Define test input values
		LocalDateTime capturedDate = LocalDateTime.of(2023, 8, 16, 12, 0); // Example date
		LocalDateTime currentIssueDate = LocalDateTime.of(2023, 8, 17, 12, 0); // Example date

		// Create an instance of the private method using reflection
		Method updatedDateToSaveMethod = JiraTestServiceImpl.class.getDeclaredMethod("updatedDateToSave",
				LocalDateTime.class, LocalDateTime.class);
		updatedDateToSaveMethod.setAccessible(true);

		// Invoke the private method
		LocalDateTime result = (LocalDateTime) updatedDateToSaveMethod.invoke(jiraTestServiceImpl, capturedDate, currentIssueDate);

		// Perform assertions based on your expected behavior
		// Modify this assertion based on your actual requirements
		Assertions.assertEquals(currentIssueDate, result);

		// Verify that expected methods were called with the expected arguments
		// Add verification steps as needed
	}
	@Test
	public void testPurgeJiraIssues() {
		// Define test input values
		List<Issue> purgeIssuesList = Collections.emptyList();
		ProjectConfFieldMapping projectConfig = new ProjectConfFieldMapping();

		List<TestCaseDetails> testCaseDetailsList = Collections.emptyList();

		// Mock the behavior of the testCaseDetailsRepository
		when(testCaseDetailsRepository.findByNumberAndBasicProjectConfigId(anyString(), anyString())).thenReturn(testCaseDetailsList);

		// Call the method to be tested
		jiraTestServiceImpl.purgeJiraIssues(purgeIssuesList, projectConfig);

		// Verify that the method calls and interactions occurred as expected
		verify(testCaseDetailsRepository, times(0)).delete(any(TestCaseDetails.class));
	}

}
