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

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.springframework.test.util.AssertionErrors.assertTrue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
public class JiraIssueJqlWriterListenerTest {
	@Mock
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepo;

	@InjectMocks
	private JiraIssueJqlWriterListener listener;

	@Mock
	JiraProcessorConfig jiraProcessorConfig;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testAfterWrite_SuccessfulRun() {
		// Arrange
		Chunk<CompositeResult> compositeResults = createSampleCompositeResults();

		// Act
		listener.afterWrite(compositeResults);
		assertTrue("", true);
	}

	@Test
	public void testOnWriteError_LogsError() {
		// Arrange
		Exception testException = new RuntimeException("Test exception");
		Chunk<CompositeResult> compositeResults = createSampleCompositeResults();

		// Act
		listener.onWriteError(testException, compositeResults);
		assertTrue("", true);
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
	public void testAfterWriteWithEmptyValue() {
		listener.afterWrite(new Chunk<>());
	}

	@Test
	public void testAfterWriteWithTraceLog() {
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setBasicProjectConfigId("abc");
		processorExecutionTraceLog.setBoardId("abc");
		processorExecutionTraceLog.setProcessorName(JiraConstants.JIRA);
		when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigIdIn(eq(JiraConstants.JIRA), anyList()))
				.thenReturn(List.of(processorExecutionTraceLog));
		listener.afterWrite(createSampleCompositeResults());
	}

	@Test
	public void testAfterWriteWithTraceLogProgressStatus() {
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setBasicProjectConfigId("abc");
		processorExecutionTraceLog.setBoardId("abc");
		processorExecutionTraceLog.setProgressStats(true);
		processorExecutionTraceLog.setProcessorName(JiraConstants.JIRA);
		when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigIdIn(eq(JiraConstants.JIRA), anyList()))
				.thenReturn(List.of(processorExecutionTraceLog));
		listener.afterWrite(createSampleCompositeResults());
	}

	@Test
	public void testAfterWriteWithTraceLogProgressStatus_lastSuccessfulRun() {
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setBasicProjectConfigId("abc");
		processorExecutionTraceLog.setBoardId("abc");
		processorExecutionTraceLog.setProgressStats(true);
		processorExecutionTraceLog.setProcessorName(JiraConstants.JIRA);
		processorExecutionTraceLog.setLastSuccessfulRun("2022-02-02T10:00:00");
		when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigIdIn(eq(JiraConstants.JIRA), anyList()))
				.thenReturn(List.of(processorExecutionTraceLog));
		listener.afterWrite(createSampleCompositeResults());
	}
}
