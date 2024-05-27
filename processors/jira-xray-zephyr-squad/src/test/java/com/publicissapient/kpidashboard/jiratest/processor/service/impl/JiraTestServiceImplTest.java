package com.publicissapient.kpidashboard.jiratest.processor.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.joda.time.DateTime;
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
		when(jiraTestProcessorConfig.getJiraServerGetUserApi()).thenReturn("user/search?username=");
		// when(getUserTimeZone(projectConfFieldMapping)).thenReturn("Indian/Maldives");
		when(testCaseDetailsRepository.findTopByBasicProjectConfigId(any())).thenReturn(null);
		when(jiraTestProcessorRepository.findByProcessorName(Mockito.anyString())).thenReturn(jiraProcessor);
		doNothing().when(processorExecutionTraceLogService).save(Mockito.any());
		assertEquals(3, jiraTestServiceImpl.processesJiraIssues(projectConfFieldMapping));
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

}
