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

package com.publicissapient.kpidashboard.jira.listener;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.test.util.ReflectionTestUtils;

import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.IterationData;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.jira.cache.JiraProcessorCacheEvictor;
import com.publicissapient.kpidashboard.jira.client.CustomAsynchronousIssueRestClient;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.reader.IssueReaderUtil;
import com.publicissapient.kpidashboard.jira.service.JiraClientService;
import com.publicissapient.kpidashboard.jira.service.JiraCommonService;
import com.publicissapient.kpidashboard.jira.service.NotificationHandler;
import com.publicissapient.kpidashboard.jira.service.OngoingExecutionsService;
import com.publicissapient.kpidashboard.jira.service.OutlierSprintStrategy;
import com.publicissapient.kpidashboard.jira.service.ProjectHierarchySyncService;

import io.atlassian.util.concurrent.Promise;

@RunWith(MockitoJUnitRunner.class)
public class JobListenerScrumTest {

	@Mock
	private NotificationHandler handler;

	@Mock
	private FieldMappingRepository fieldMappingRepository;

	@Mock
	private JiraClientService jiraClientService;

	@Mock
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepo;

	@Mock
	private JiraProcessorCacheEvictor jiraProcessorCacheEvictor;

	@Mock
	private OngoingExecutionsService ongoingExecutionsService;

	@Mock
	private ProjectBasicConfigRepository projectBasicConfigRepository;

	@Mock
	private JiraCommonService jiraCommonService;

	@Mock
	private ProcessorJiraRestClient client;

	@Mock
	private KerberosClient kerberosClient;

	@Mock
	FetchProjectConfiguration fetchProjectConfiguration;

	@Mock
	SearchRestClient searchRestClient;

	@Mock
	CustomAsynchronousIssueRestClient customAsynchronousIssueRestClient;

	@InjectMocks
	private JobListenerScrum jobListenerScrum;

	@Mock
	JiraIssueRepository jiraIssueRepository;

	@Mock
	Promise<SearchResult> promisedRs;

	@Mock
	SearchResult searchResult;

	@Mock
	private ProjectHierarchySyncService projectHierarchySyncService;

	@Mock
	private OutlierSprintStrategy outlierSprintStrategy;

	private JobExecution jobExecution;

	private String projectId = "63bfa0d5b7617e260763ca21";
	private String connectionId = "5fd99f7bc8b51a7b55aec836";
	String boardId = "123";
	List<Issue> issues = new ArrayList<>();
	List<ProjectBasicConfig> projectConfigsList;
	List<ProjectToolConfig> projectToolConfigs;
	Optional<Connection> connection;

	FieldMapping fieldMapping = new FieldMapping();
	ProjectConfFieldMapping projectConfigMap;

	@Before
	public void setUp() {
		jobExecution = MetaDataInstanceFactory.createJobExecution();
		when(jiraClientService.isContainRestClient(projectId)).thenReturn(true);
		when(jiraClientService.getRestClientMap(projectId)).thenReturn(client);
		when(client.getProcessorSearchClient()).thenReturn(searchRestClient);
		when(client.getCustomIssueClient()).thenReturn(customAsynchronousIssueRestClient);

		projectToolConfigs = IssueReaderUtil.getMockProjectToolConfig(projectId);
		projectConfigsList = IssueReaderUtil.getMockProjectConfig();
		connection = IssueReaderUtil.getMockConnection(connectionId);
		fieldMapping = IssueReaderUtil.getMockFieldMapping(projectId);
		projectConfigMap = IssueReaderUtil.createProjectConfigMap(projectConfigsList, connection, fieldMapping,
				projectToolConfigs);

		// Set the projectId field in jobListenerScrum
		ReflectionTestUtils.setField(jobListenerScrum, "projectId", projectId);
	}

	@Test
	public void testAfterJob_SuccessExecution_JqlUnMatchedData() throws Exception {
		projectConfigMap.getJira().setBoardQuery("abc");
		fieldMapping.setNotificationEnabler(true);
		when(fetchProjectConfiguration.fetchConfiguration(projectId)).thenReturn(projectConfigMap);
		when(searchRestClient.searchJql(anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anySet()))
				.thenReturn(promisedRs);
		when(promisedRs.claim()).thenReturn(searchResult);
		when(jiraIssueRepository.countByBasicProjectConfigIdAndExcludeTypeName(projectId, "")).thenReturn(5L);
		when(searchResult.getTotal()).thenReturn(0);
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setFirstRunDate(LocalDateTime.now().minusMonths(12).toString());
		when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigIdIn(anyString(), any()))
				.thenReturn(Collections.singletonList(processorExecutionTraceLog));
		// Simulate a failed job
		jobExecution.setStatus(BatchStatus.STARTED);
		// Act
		jobListenerScrum.afterJob(jobExecution);
		verify(ongoingExecutionsService).markExecutionAsCompleted(projectId);
	}

	@Test
	public void testAfterJob_SuccessExecution_JqlMatchedData() throws Exception {
		projectConfigMap.getJira().setBoardQuery("abc");
		fieldMapping.setNotificationEnabler(true);
		when(fetchProjectConfiguration.fetchConfiguration(projectId)).thenReturn(projectConfigMap);
		when(searchRestClient.searchJql(anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anySet()))
				.thenReturn(promisedRs);
		when(promisedRs.claim()).thenReturn(searchResult);
		when(jiraIssueRepository.countByBasicProjectConfigIdAndExcludeTypeName(projectId, "")).thenReturn(5L);
		when(searchResult.getTotal()).thenReturn(5);
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setFirstRunDate(LocalDateTime.now().minusMonths(12).toString());
		when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigIdIn(anyString(), any()))
				.thenReturn(Collections.singletonList(processorExecutionTraceLog));
		// Simulate a failed job
		jobExecution.setStatus(BatchStatus.STARTED);
		// Act
		jobListenerScrum.afterJob(jobExecution);
		verify(ongoingExecutionsService).markExecutionAsCompleted(projectId);
	}

	@Test
	public void testAfterJob_SuccessExecution_JqlNoResult() throws Exception {
		projectConfigMap.getJira().setBoardQuery("abc");
		fieldMapping.setNotificationEnabler(true);
		when(fetchProjectConfiguration.fetchConfiguration(projectId)).thenReturn(projectConfigMap);
		when(searchRestClient.searchJql(anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anySet()))
				.thenReturn(promisedRs);
		when(promisedRs.claim()).thenReturn(null);
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setFirstRunDate(LocalDateTime.now().minusMonths(12).toString());
		when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigIdIn(anyString(), any()))
				.thenReturn(Collections.singletonList(processorExecutionTraceLog));
		// Simulate a failed job
		jobExecution.setStatus(BatchStatus.STARTED);
		// Act
		jobListenerScrum.afterJob(jobExecution);
		verify(ongoingExecutionsService).markExecutionAsCompleted(projectId);
	}

	@Test
	public void testAfterJob_SuccessExecution_BoardMatchedData() throws Exception {
		projectConfigMap.getJira().setBoardQuery("abc");
		fieldMapping.setNotificationEnabler(true);
		when(customAsynchronousIssueRestClient.searchBoardIssue(anyString(), anyString(), Mockito.anyInt(),
				Mockito.anyInt(), Mockito.anySet())).thenReturn(promisedRs);
		when(promisedRs.claim()).thenReturn(searchResult);
		when(jiraIssueRepository.countByBasicProjectConfigIdAndExcludeTypeName(projectId, "Epic")).thenReturn(5L);
		when(searchResult.getTotal()).thenReturn(5);
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setBoardId("9");
		processorExecutionTraceLog.setFirstRunDate(LocalDateTime.now().minusMonths(12).toString());
		when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigIdIn(anyString(), any()))
				.thenReturn(Collections.singletonList(processorExecutionTraceLog));
		// Simulate a failed job
		jobExecution.setStatus(BatchStatus.STARTED);
		// Act
		jobListenerScrum.afterJob(jobExecution);
		verify(ongoingExecutionsService).markExecutionAsCompleted(projectId);
	}

	@Test
	public void testAfterJob_SuccessExecution_BoardNoResult() throws Exception {
		projectConfigMap.getJira().setBoardQuery("abc");
		fieldMapping.setNotificationEnabler(true);
		when(customAsynchronousIssueRestClient.searchBoardIssue(anyString(), anyString(), Mockito.anyInt(),
				Mockito.anyInt(), Mockito.anySet())).thenReturn(promisedRs);
		when(promisedRs.claim()).thenReturn(null);
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setBoardId("9");
		processorExecutionTraceLog.setFirstRunDate(LocalDateTime.now().minusMonths(12).toString());
		when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigIdIn(anyString(), any()))
				.thenReturn(Collections.singletonList(processorExecutionTraceLog));
		// Simulate a failed job
		jobExecution.setStatus(BatchStatus.STARTED);
		// Act
		jobListenerScrum.afterJob(jobExecution);
		verify(ongoingExecutionsService).markExecutionAsCompleted(projectId);
	}

	@Test
	public void testAfterJob_SuccessExecution_BoardUnMatchedData() throws Exception {
		projectConfigMap.getJira().setBoardQuery("abc");
		fieldMapping.setNotificationEnabler(true);
		when(customAsynchronousIssueRestClient.searchBoardIssue(anyString(), anyString(), Mockito.anyInt(),
				Mockito.anyInt(), Mockito.anySet())).thenReturn(promisedRs);
		when(promisedRs.claim()).thenReturn(searchResult);
		when(jiraIssueRepository.countByBasicProjectConfigIdAndExcludeTypeName(projectId, "Epic")).thenReturn(5L);
		when(searchResult.getTotal()).thenReturn(0);
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setBoardId("9");
		processorExecutionTraceLog.setFirstRunDate(LocalDateTime.now().minusMonths(12).toString());
		when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigIdIn(anyString(), any()))
				.thenReturn(Collections.singletonList(processorExecutionTraceLog));
		// Simulate a failed job
		jobExecution.setStatus(BatchStatus.STARTED);
		// Act
		jobListenerScrum.afterJob(jobExecution);
		verify(ongoingExecutionsService).markExecutionAsCompleted(projectId);
	}

	@Test
	public void testBeforeJob() {
		jobListenerScrum.beforeJob(jobExecution);
	}

	@Test
	public void testAfterJob_FailedExecution() throws Exception {
		projectConfigMap.getJira().setBoardQuery("abc");
		fieldMapping.setNotificationEnabler(true);
		when(fieldMappingRepository.findByProjectConfigId(projectId)).thenReturn(fieldMapping);
		when(projectBasicConfigRepository.findByStringId(projectId))
				.thenReturn(Optional.ofNullable(projectConfigsList.get(0)));
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setBoardId("9");
		processorExecutionTraceLog.setFirstRunDate(LocalDateTime.now().minusMonths(12).toString());
		when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigIdIn(anyString(), any()))
				.thenReturn(Collections.singletonList(processorExecutionTraceLog));
		when(jiraCommonService.getApiHost()).thenReturn("xyz");
		StepExecution stepExecution = jobExecution.createStepExecution("xyz");
		stepExecution.setStatus(BatchStatus.FAILED);
		stepExecution.addFailureException(new Throwable("Exception"));
		// Simulate a failed job
		jobExecution.setStatus(BatchStatus.FAILED);

		// Act
		jobListenerScrum.afterJob(jobExecution);

		verify(ongoingExecutionsService).markExecutionAsCompleted(projectId);
	}

	@Test
	public void testAfterJob_WithException() throws Exception {
		// Act
		jobListenerScrum.afterJob(null);

		verify(ongoingExecutionsService).markExecutionAsCompleted(projectId);
	}

	@Test
	public void testAfterJob_FailedExecution_progress_stats() throws Exception {
		FieldMapping fieldMapping = new FieldMapping();
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setProgressStats(true);
		fieldMapping.setNotificationEnabler(true);
		when(fieldMappingRepository.findByProjectConfigId(projectId)).thenReturn(fieldMapping);
		ProjectBasicConfig projectBasicConfig = ProjectBasicConfig.builder().projectName("xyz").build();
		when(projectBasicConfigRepository.findByStringId(projectId)).thenReturn(Optional.ofNullable(projectBasicConfig));
		when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigIdIn(anyString(), any()))
				.thenReturn(Collections.singletonList(processorExecutionTraceLog));
		when(jiraCommonService.getApiHost()).thenReturn("xyz");
		StepExecution stepExecution = jobExecution.createStepExecution("xyz");
		stepExecution.setStatus(BatchStatus.FAILED);
		stepExecution.addFailureException(new Throwable("Exception"));
		// Simulate a failed job
		jobExecution.setStatus(BatchStatus.FAILED);

		// Act
		jobListenerScrum.afterJob(jobExecution);

		verify(ongoingExecutionsService).markExecutionAsCompleted(projectId);
	}

	@Test
	public void testAfterJob_SuccessExecution_WithOutlierSprintMap() throws Exception {
		// Arrange
		projectConfigMap.getJira().setBoardQuery("abc");
		fieldMapping.setNotificationEnabler(true);
		when(fetchProjectConfiguration.fetchConfiguration(projectId)).thenReturn(projectConfigMap);
		when(searchRestClient.searchJql(anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anySet()))
				.thenReturn(promisedRs);
		when(promisedRs.claim()).thenReturn(searchResult);
		when(jiraIssueRepository.countByBasicProjectConfigIdAndExcludeTypeName(projectId, "")).thenReturn(5L);
		when(searchResult.getTotal()).thenReturn(5);

		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setFirstRunDate(LocalDateTime.now().minusMonths(12).toString());
		processorExecutionTraceLog.setProgressStats(true); // Ensure progressStats is true

		when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigIdIn(anyString(), any()))
				.thenReturn(Collections.singletonList(processorExecutionTraceLog));

		Map<String, List<String>> outlierSprintMap = Collections.singletonMap("sprint1",
				Collections.singletonList("issue1"));
		when(outlierSprintStrategy.execute(new ObjectId(projectId))).thenReturn(outlierSprintMap);

		// Simulate a successful job
		jobExecution.setStatus(BatchStatus.COMPLETED);

		// Act
		jobListenerScrum.afterJob(jobExecution);

		// Assert
		verify(ongoingExecutionsService).markExecutionAsCompleted(projectId);
		List<IterationData> expected = Collections
				.singletonList(new IterationData("sprint1", Collections.singletonList("issue1")));
		assertEquals(expected, processorExecutionTraceLog.getAdditionalInfo());
	}
}
