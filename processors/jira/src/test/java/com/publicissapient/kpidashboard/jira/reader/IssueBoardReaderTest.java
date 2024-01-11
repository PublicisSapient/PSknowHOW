package com.publicissapient.kpidashboard.jira.reader;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import org.apache.commons.beanutils.BeanUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.jira.client.JiraClient;
import com.publicissapient.kpidashboard.jira.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.jira.dataFactories.ConnectionsDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.ProjectBasicConfigDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.ToolConfigDataFactory;
import com.publicissapient.kpidashboard.jira.helper.ReaderRetryHelper;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.model.ReadData;
import com.publicissapient.kpidashboard.jira.service.FetchEpicData;
import com.publicissapient.kpidashboard.jira.service.JiraCommonService;

@RunWith(MockitoJUnitRunner.class)
public class IssueBoardReaderTest {

	@Mock
	private FetchProjectConfiguration fetchProjectConfiguration;

	@Mock
	private JiraClient jiraClient;

	@Mock
	private JiraCommonService jiraCommonService;

	@Mock
	private JiraProcessorConfig jiraProcessorConfig;

	@Mock
	private FetchEpicData fetchEpicData;

	@Mock
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepo;

	@Mock
	ItemReader itemReader;

	@InjectMocks
	private IssueBoardReader issueBoardReader;

	int pageSize = 50;
	int pageNumber = 0;
	String boardId = "";
	List<Issue> issues1 = new ArrayList<>();
	Map<String, Map<String, String>> projectBoardWiseDeltaDate = new HashMap<>();
	int boardIssueSize = 0;
	Boolean fetchLastIssue = false;
	@Mock
	private ReaderRetryHelper retryHelper;
	private Iterator<BoardDetails> boardIterator;
	private Iterator<Issue> issueIterator;
	private ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();;
	private String projectId = "63bfa0d5b7617e260763ca21";
	List<ProjectBasicConfig> projectConfigsList;
	List<ProjectToolConfig> projectToolConfigs;
	Optional<Connection> connection;
	FieldMapping fieldMapping = new FieldMapping();
	KerberosClient kerberosClient;
	ProcessorJiraRestClient processorJiraRestClient;

	@Before
	public void setup() {
		projectToolConfigs = getMockProjectToolConfig();
		projectConfigsList = getMockProjectConfig();
		connection = getMockConnection();
		fieldMapping = getMockFieldMapping();
		createProjectConfigMap();
		boardIterator = projectConfFieldMapping.getProjectToolConfig().getBoards().iterator();
		Issue issue = new Issue("summary1", null, "key1", 1l, null, null, null, "story",
				null, null, new ArrayList<>(), null, null, DateTime.now(), DateTime.now(),
				DateTime.now(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null, null, null,
				null, null, null, null, null, Arrays.asList("expandos"), null,
				Arrays.asList(), null, new HashSet<>(Arrays.asList("label1")));
		issues1.add(issue);
		issueIterator = issues1.iterator();
	}

	@Test
	public void readIssues() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		when(jiraProcessorConfig.getPageSize()).thenReturn(1);
		when(fetchProjectConfiguration.fetchConfiguration(any())).thenReturn(projectConfFieldMapping);
		when(retryHelper.executeWithRetry(any())).thenReturn(issues1);
		ReadData readData = issueBoardReader.read();
//		assertNull(readData);
	}

//	@Test
//	public void readIssuesWithException() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
//		when(jiraProcessorConfig.getPageSize()).thenReturn(1);
//		when(fetchProjectConfiguration.fetchConfiguration(any())).thenReturn(projectConfFieldMapping);
//		doThrow(new Exception())
//				.when(retryHelper).executeWithRetry(any());
//		ReadData readData = issueBoardReader.read();
////		assertNull(readData);
//	}

	@Test
	public void testGetDeltaDateFromTraceLog() throws Exception {

		issueBoardReader.projectConfFieldMapping=projectConfFieldMapping;
		// Use reflection to access the private method
		Method method = IssueBoardReader.class.getDeclaredMethod("getDeltaDateFromTraceLog");
		method.setAccessible(true);  // Make the private method accessible

		// Invoke the private method
		String result = (String) method.invoke(issueBoardReader);

		// Add assertions based on your actual implementation
		// Add additional assertions based on your actual implementation
	}

	@Test
	public void testGetDeltaDateFromTraceLogWithRepo() throws Exception {

		ProcessorExecutionTraceLog processorExecutionTraceLog=new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setBoardId("11856");
		processorExecutionTraceLog.setLastSuccessfulRun("date");
		when(processorExecutionTraceLogRepo
						.findByProcessorNameAndBasicProjectConfigIdIn(any(),any())).thenReturn(Arrays.asList(processorExecutionTraceLog));
		issueBoardReader.projectConfFieldMapping=projectConfFieldMapping;
		// Use reflection to access the private method
		Method method = IssueBoardReader.class.getDeclaredMethod("getDeltaDateFromTraceLog");
		method.setAccessible(true);  // Make the private method accessible

		// Invoke the private method
		String result = (String) method.invoke(issueBoardReader);

		// Add assertions based on your actual implementation
		// Add additional assertions based on your actual implementation
	}

	@Test
	public void testGetDeltaDateFromTraceLogWithRepoAndWithoutBoardId() throws Exception {

		ProcessorExecutionTraceLog processorExecutionTraceLog=new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setLastSuccessfulRun("date");
		when(processorExecutionTraceLogRepo
				.findByProcessorNameAndBasicProjectConfigIdIn(any(),any())).thenReturn(Arrays.asList(processorExecutionTraceLog));
		issueBoardReader.projectConfFieldMapping=projectConfFieldMapping;
		// Use reflection to access the private method
		Method method = IssueBoardReader.class.getDeclaredMethod("getDeltaDateFromTraceLog");
		method.setAccessible(true);  // Make the private method accessible

		// Invoke the private method
		String result = (String) method.invoke(issueBoardReader);

		// Add assertions based on your actual implementation
		// Add additional assertions based on your actual implementation
	}

	@Test
	public void testFetchEpic() throws Exception {
		issueBoardReader.projectConfFieldMapping=projectConfFieldMapping;
		doThrow(new Exception()).when(retryHelper).executeWithRetry(any());
		// Use reflection to access the private method
		Method method = IssueBoardReader.class.getDeclaredMethod("fetchEpics", KerberosClient.class, ProcessorJiraRestClient.class);
		method.setAccessible(true);  // Make the private method accessible

		// Invoke the private method
		List<Issue> result = (List<Issue>) method.invoke(issueBoardReader,kerberosClient,processorJiraRestClient);

		// Add assertions based on your actual implementation
		// Add additional assertions based on your actual implementation
	}

	@Test
	public void testFetchIssues() throws Exception {
		issueBoardReader.projectConfFieldMapping=projectConfFieldMapping;
		doThrow(new Exception()).when(retryHelper).executeWithRetry(any());
		// Use reflection to access the private method
		Method method = IssueBoardReader.class.getDeclaredMethod("fetchIssues", ProcessorJiraRestClient.class);
		method.setAccessible(true);  // Make the private method accessible

		// Invoke the private method
		List<Issue> result = (List<Issue>) method.invoke(issueBoardReader,processorJiraRestClient);

		// Add assertions based on your actual implementation
		// Add additional assertions based on your actual implementation
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
		projectConfFieldMapping.setFieldMapping(fieldMapping);
		projectConfFieldMapping.setProjectToolConfig(projectToolConfigs.get(0));
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

	private List<ProjectToolConfig> getMockProjectToolConfig() {
		ToolConfigDataFactory projectToolConfigDataFactory = ToolConfigDataFactory
				.newInstance("/json/default/project_tool_configs.json");
		return projectToolConfigDataFactory.findByToolNameAndBasicProjectConfigId(ProcessorConstants.JIRA,
				"63bfa0d5b7617e260763ca21");
	}

	private FieldMapping getMockFieldMapping() {
		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/field_mapping.json");
		return fieldMappingDataFactory.findByBasicProjectConfigId("63bfa0d5b7617e260763ca21");
	}
}
