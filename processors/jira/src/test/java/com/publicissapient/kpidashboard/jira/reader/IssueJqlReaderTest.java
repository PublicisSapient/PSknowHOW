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
package com.publicissapient.kpidashboard.jira.reader;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.FetchProjectConfigurationImpl;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.helper.ReaderRetryHelper;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.model.ReadData;
import com.publicissapient.kpidashboard.jira.service.FetchEpicData;
import com.publicissapient.kpidashboard.jira.service.JiraClientService;
import com.publicissapient.kpidashboard.jira.service.JiraCommonService;

@RunWith(MockitoJUnitRunner.class)
public class IssueJqlReaderTest {

	@Mock
	private FetchProjectConfigurationImpl fetchProjectConfiguration;

	@Mock
	private JiraClientService jiraClientService;

	@Mock
	private JiraCommonService jiraCommonService;

	@Mock
	private JiraProcessorConfig jiraProcessorConfig;

	@Mock
	private FetchEpicData fetchEpicData;

	@Mock
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepo;

	@Mock
	private FieldMappingRepository fieldMappingRepository;

	@Mock
	private ProjectToolConfigRepository toolRepository;

	@Mock
	private ProjectBasicConfigRepository projectConfigRepository;

	@Mock
	private ConnectionRepository connectionRepository;

	@Mock
	private SprintRepository sprintRepository;

	@Mock
	KerberosClient krb5Client;

	@Mock
	ProcessorJiraRestClient client;

	@InjectMocks
	IssueJqlReader issueJqlReader;

	@Mock
	private ReaderRetryHelper retryHelper;
	private Iterator<BoardDetails> boardIterator;
	private Iterator<Issue> issueIterator;
	ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
	@Mock
	ReaderRetryHelper.RetryableOperation<List<Issue>> mockRetryableOperation;

	private ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
	private List<ProcessorExecutionTraceLog> pl = new ArrayList<>();
	private String projectId = "63bfa0d5b7617e260763ca21";

	private String connectionId = "5fd99f7bc8b51a7b55aec836";
	String boardId = "123";
	List<Issue> issues = new ArrayList<>();
	List<ProjectBasicConfig> projectConfigsList;
	List<ProjectToolConfig> projectToolConfigs;
	Optional<Connection> connection;

	FieldMapping fieldMapping = new FieldMapping();

	@Before
	public void setup() throws Exception, IOException, InterruptedException {
		projectToolConfigs = IssueReaderUtil.getMockProjectToolConfig(projectId);
		projectConfigsList = IssueReaderUtil.getMockProjectConfig();
		connection = IssueReaderUtil.getMockConnection(connectionId);
		fieldMapping = IssueReaderUtil.getMockFieldMapping(projectId);
		projectConfFieldMapping = IssueReaderUtil.createProjectConfigMap(projectConfigsList, connection, fieldMapping,
				projectToolConfigs);
		pl = IssueReaderUtil.mockProcessorExecutionTraceLog(projectId);
		issues = IssueReaderUtil.createIssue();
		boardIterator = projectConfFieldMapping.getProjectToolConfig().getBoards().iterator();
		issueIterator = issues.iterator();
		when(jiraProcessorConfig.getPageSize()).thenReturn(1);
		when(fetchProjectConfiguration.fetchConfiguration(null)).thenReturn(projectConfFieldMapping);
		when(jiraClientService.getRestClientMap(null)).thenReturn(client);
		setPrivateField(issueJqlReader, "processorId", "63bfa0d5b7617e260763ca21");
	}

	private void setPrivateField(Object targetObject, String fieldName, String fieldValue) throws Exception {
		Field field = targetObject.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(targetObject, fieldValue);
	}

	@Test
	public void testReadData() throws Exception {
		when(processorExecutionTraceLogRepo
						.findByProcessorNameAndBasicProjectConfigIdAndProgressStatsFalse(
								anyString(), anyString()))
				.thenReturn(pl);
		when(jiraCommonService.fetchIssuesBasedOnJql(any(), any(), anyInt(), anyString()))
				.thenReturn(issues);
		// Arrange
		ReadData mockReadData = IssueReaderUtil.getMockReadData(boardId, projectConfFieldMapping);

		// Act
		ReadData result = issueJqlReader.read();

		// Assert
		assertEquals(mockReadData.getIssue(), result.getIssue());
	}

	@Test
	public void testGetDeltaDateFromTraceLog() throws Exception {

		issueJqlReader.projectConfFieldMapping = projectConfFieldMapping;
		// Use reflection to access the private method
		Method method = IssueJqlReader.class.getDeclaredMethod("getDeltaDateFromTraceLog");
		method.setAccessible(true); // Make the private method accessible

		// Invoke the private method
		String result = (String) method.invoke(issueJqlReader);

		// Add assertions based on your actual implementation
		// Add additional assertions based on your actual implementation
	}

	@Test
	public void testGetDeltaDateFromTraceLogWithRepo() throws Exception {

		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setBoardId("11856");
		processorExecutionTraceLog.setLastSuccessfulRun("date");
		when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigIdAndProgressStatsFalse(any(), any()))
				.thenReturn(null);
		issueJqlReader.projectConfFieldMapping = projectConfFieldMapping;
		// Use reflection to access the private method
		Method method = IssueJqlReader.class.getDeclaredMethod("getDeltaDateFromTraceLog");
		method.setAccessible(true); // Make the private method accessible

		// Invoke the private method
		String result = (String) method.invoke(issueJqlReader);

		// Add assertions based on your actual implementation
		// Add additional assertions based on your actual implementation
	}

	@Test
	public void testGetDeltaDateFromTraceLogWithRepoAndWithoutBoardId() throws Exception {

		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setLastSuccessfulRun("date");
		when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigIdAndProgressStatsFalse(any(), any()))
				.thenReturn(Arrays.asList(processorExecutionTraceLog));
		issueJqlReader.projectConfFieldMapping = projectConfFieldMapping;
		// Use reflection to access the private method
		Method method = IssueJqlReader.class.getDeclaredMethod("getDeltaDateFromTraceLog");
		method.setAccessible(true); // Make the private method accessible

		// Invoke the private method
		String result = (String) method.invoke(issueJqlReader);

		// Add assertions based on your actual implementation
		// Add additional assertions based on your actual implementation
	}

	@Test
	public void testFetchIssues() throws Exception {
		issueJqlReader.projectConfFieldMapping = projectConfFieldMapping;
		// doThrow(new Exception()).when(retryHelper).executeWithRetry(any());
		// Use reflection to access the private method
		Method method = IssueJqlReader.class.getDeclaredMethod("fetchIssues", ProcessorJiraRestClient.class);
		method.setAccessible(true); // Make the private method accessible

		// Invoke the private method
		// List<Issue> result = (List<Issue>) method.invoke(issueJqlReader, client);

		// Add assertions based on your actual implementation
		// Add additional assertions based on your actual implementation
	}
}
