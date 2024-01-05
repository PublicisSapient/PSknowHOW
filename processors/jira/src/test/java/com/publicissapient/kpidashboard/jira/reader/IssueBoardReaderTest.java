package com.publicissapient.kpidashboard.jira.reader;

import static org.junit.Assert.assertNull;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
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
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
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

	@InjectMocks
	IssueBoardReader issueBoardReader;

	int pageSize = 50;
	int pageNumber = 0;
	String boardId = "";
	List<Issue> issues = new ArrayList<>();
	Map<String, Map<String, String>> projectBoardWiseDeltaDate = new HashMap<>();
	int boardIssueSize = 0;
	Boolean fetchLastIssue = false;
	private ReaderRetryHelper retryHelper;
	private Iterator<BoardDetails> boardIterator;
	private Iterator<Issue> issueIterator;
	private ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();;
	private String projectId = "63bfa0d5b7617e260763ca21";
	List<ProjectBasicConfig> projectConfigsList;
	List<ProjectToolConfig> projectToolConfigs;
	Optional<Connection> connection;
	FieldMapping fieldMapping = new FieldMapping();

	@Before
	public void setup() {

		projectToolConfigs = getMockProjectToolConfig();
		projectConfigsList = getMockProjectConfig();
		connection = getMockConnection();
		fieldMapping = getMockFieldMapping();
		createProjectConfigMap();
		boardIterator = projectConfFieldMapping.getProjectToolConfig().getBoards().iterator();
		issueIterator = issues.iterator();
	}

	//@Test
	public void readIssues() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		Mockito.when(jiraProcessorConfig.getPageSize()).thenReturn(1);
		Mockito.when(fetchProjectConfiguration.fetchConfiguration(projectId)).thenReturn(projectConfFieldMapping);
		ReadData readData = issueBoardReader.read();
		assertNull(readData);
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
