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

import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.item.Chunk;

import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.model.CompositeResult;

@RunWith(MockitoJUnitRunner.class)
public class JiraIssueBoardWriterListenerTest {

	@Mock
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepo;

	@InjectMocks
	private JiraIssueBoardWriterListener listener;

	@Mock
	JiraProcessorConfig jiraProcessorConfig;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testAfterWrite() {
		// Arrange
		Chunk<CompositeResult> compositeResults = new Chunk<>();

		// Create a JiraIssue for testing
		JiraIssue jiraIssue = new JiraIssue();
		jiraIssue.setBasicProjectConfigId("testProjectId");
		jiraIssue.setBoardId("testBoardId");
		jiraIssue.setTypeName("");
		jiraIssue.setChangeDate("2022-01-01T12:34:56"); // Change date in the required format
		CompositeResult compositeResult = new CompositeResult();
		compositeResult.setJiraIssue(jiraIssue);
		compositeResults.add(compositeResult);

		// Mock the repository's behavior
		when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigIdIn(eq(JiraConstants.JIRA), anyList()))
				.thenReturn(List.of()); // For the case where trace log
		// is not present

		// Act
		listener.afterWrite(compositeResults);

		// Verify
		verify(processorExecutionTraceLogRepo, times(1)).saveAll(anyList());

		// Assert
		List<ProcessorExecutionTraceLog> savedLogs = new ArrayList<>();

		// Additional assertions based on the requirements of your application
		assertEquals(0, savedLogs.size());
	}

	@Test
	public void onWriteError_LogsError() {
		// Arrange
		Exception testException = new RuntimeException("Test exception");
		Chunk<CompositeResult> compositeResults = createSampleCompositeResults();

		// Act
		listener.onWriteError(testException, compositeResults);
	}

	private Chunk<CompositeResult> createSampleCompositeResults() {
		Chunk<CompositeResult> compositeResults = new Chunk<>();

		JiraIssue jiraIssue1 = new JiraIssue();
		jiraIssue1.setBasicProjectConfigId("Project1");
		jiraIssue1.setBoardId("Board1");
		jiraIssue1.setChangeDate(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
		jiraIssue1.setTypeName("Story");
		jiraIssue1.setChangeDate("");

		CompositeResult result1 = new CompositeResult();
		result1.setJiraIssue(jiraIssue1);
		compositeResults.add(result1);

		// Add more sample data as needed

		return compositeResults;
	}

	@Test
	public void afterWrite_ExistingLog_Success() {
		// Arrange
		Chunk<CompositeResult> compositeResults = createSampleCompositeResults();

		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setBasicProjectConfigId("abc");
		processorExecutionTraceLog.setBoardId("abc");
		processorExecutionTraceLog.setProcessorName(JiraConstants.JIRA);

		// Mock the repository behavior
		when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigIdIn(anyString(), anyList()))
				.thenReturn(List.of(processorExecutionTraceLog));

		// Act
		listener.afterWrite(compositeResults);

		// Assert
		verify(processorExecutionTraceLogRepo, times(1)).saveAll(anyList());
	}

	@Test
	public void afterWrite_ExistingLog_Success_last_Success() {
		// Arrange
		Chunk<CompositeResult> compositeResults = createSampleCompositeResults();

		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setBasicProjectConfigId("abc");
		processorExecutionTraceLog.setBoardId("abc");
		processorExecutionTraceLog.setProcessorName(JiraConstants.JIRA);
		processorExecutionTraceLog.setLastSuccessfulRun("2022-02-02T10:00:00");

		// Mock the repository behavior
		when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigIdIn(anyString(), anyList()))
				.thenReturn(List.of(processorExecutionTraceLog));

		// Act
		listener.afterWrite(compositeResults);

		// Assert
		verify(processorExecutionTraceLogRepo, times(1)).saveAll(anyList());
	}

	@Test
	public void testAfterWriteWithEmptyValue() {
		listener.afterWrite(new Chunk<>());
	}
}
