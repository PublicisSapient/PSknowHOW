/*
 *
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.publicissapient.kpidashboard.jira.jobs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.publicissapient.kpidashboard.jira.tasklet.KanbanJiraIssueReleaseStatusTasklet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.builder.TaskletStepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.transaction.PlatformTransactionManager;

import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.helper.BuilderFactory;
import com.publicissapient.kpidashboard.jira.listener.JiraIssueBoardWriterListener;
import com.publicissapient.kpidashboard.jira.listener.JiraIssueJqlWriterListener;
import com.publicissapient.kpidashboard.jira.listener.JiraIssueSprintJobListener;
import com.publicissapient.kpidashboard.jira.listener.JobListenerKanban;
import com.publicissapient.kpidashboard.jira.listener.JobListenerScrum;
import com.publicissapient.kpidashboard.jira.listener.JobStepProgressListener;
import com.publicissapient.kpidashboard.jira.listener.KanbanJiraIssueJqlWriterListener;
import com.publicissapient.kpidashboard.jira.listener.KanbanJiraIssueWriterListener;
import com.publicissapient.kpidashboard.jira.processor.IssueKanbanProcessor;
import com.publicissapient.kpidashboard.jira.processor.IssueScrumProcessor;
import com.publicissapient.kpidashboard.jira.reader.IssueBoardReader;
import com.publicissapient.kpidashboard.jira.reader.IssueJqlReader;
import com.publicissapient.kpidashboard.jira.reader.IssueSprintReader;
import com.publicissapient.kpidashboard.jira.tasklet.JiraIssueReleaseStatusTasklet;
import com.publicissapient.kpidashboard.jira.tasklet.KanbanReleaseDataTasklet;
import com.publicissapient.kpidashboard.jira.tasklet.MetaDataTasklet;
import com.publicissapient.kpidashboard.jira.tasklet.ScrumReleaseDataTasklet;
import com.publicissapient.kpidashboard.jira.tasklet.SprintReportTasklet;
import com.publicissapient.kpidashboard.jira.tasklet.SprintScrumBoardTasklet;
import com.publicissapient.kpidashboard.jira.writer.IssueKanbanWriter;
import com.publicissapient.kpidashboard.jira.writer.IssueScrumWriter;

@RunWith(MockitoJUnitRunner.class)
public class JiraProcessorJobTest {
	@Mock
	private IssueBoardReader issueBoardReader;

	@Mock
	private IssueJqlReader issueJqlReader;

	@Mock
	private KanbanJiraIssueReleaseStatusTasklet kanbanJiraIssueReleaseStatusTasklet;

	@Mock
	private IssueSprintReader issueSprintReader;

	@Mock
	private IssueScrumProcessor issueScrumProcessor;

	@Mock
	private IssueScrumWriter issueScrumWriter;

	@Mock
	private IssueKanbanWriter issueKanbanWriter;

	@Mock
	private MetaDataTasklet metaDataTasklet;

	@Mock
	private SprintScrumBoardTasklet sprintScrumBoardTasklet;

	@Mock
	private JiraIssueReleaseStatusTasklet jiraIssueReleaseStatusTasklet;

	@Mock
	private SprintReportTasklet sprintReportTasklet;

	@Mock
	private ScrumReleaseDataTasklet scrumReleaseDataTasklet;

	@Mock
	private KanbanReleaseDataTasklet kanbanReleaseDataTasklet;

	@Mock
	private JiraIssueBoardWriterListener jiraIssueBoardWriterListener;

	@Mock
	private JiraIssueJqlWriterListener jiraIssueJqlWriterListener;

	@Mock
	private JobListenerScrum jobListenerScrum;

	@Mock
	private JobListenerKanban jobListenerKanban;

	@Mock
	private JiraIssueSprintJobListener jiraIssueSprintJobListener;

	@Mock
	private IssueKanbanProcessor issueKanbanProcessor;

	@Mock
	private KanbanJiraIssueWriterListener kanbanJiraIssueWriterListener;

	@Mock
	private KanbanJiraIssueJqlWriterListener kanbanJiraIssueJqlWriterListener;

	@Mock
	private JiraProcessorConfig jiraProcessorConfig;

	@Mock
	private JobStepProgressListener jobStepProgressListener;


	@InjectMocks
	private JiraProcessorJob jiraProcessorJob;

	@Mock
	ItemReader reader;

	@Mock
	ItemProcessor processor;

	@Mock
	PlatformTransactionManager transactionManager;

	@Mock
	BuilderFactory builderFactory;

	@Mock
	JobRepository jobRepository;

	@Test
	public void testFetchIssueScrumBoardJob() throws Exception {
		// Mock the necessary objects
		Job job = mock(Job.class);
		JobBuilder jobBuilder = mock(JobBuilder.class);
		SimpleJobBuilder simpleJobBuilder = mock(SimpleJobBuilder.class);
		when(builderFactory.getJobBuilder(any(String.class),any(JobRepository.class))).thenReturn(jobBuilder);
		// Configure the mock objects
		when(jobBuilder.incrementer(any(RunIdIncrementer.class))).thenReturn(jobBuilder);
		when(jobBuilder.start(any(Step.class))).thenReturn(simpleJobBuilder);
		when(simpleJobBuilder.next(any(Step.class))).thenReturn(simpleJobBuilder);
		when(simpleJobBuilder.listener(jobListenerScrum)).thenReturn(simpleJobBuilder);
		when(simpleJobBuilder.listener(jobListenerKanban)).thenReturn(simpleJobBuilder);
		when(simpleJobBuilder.listener(jiraIssueSprintJobListener)).thenReturn(simpleJobBuilder);
		when(simpleJobBuilder.build()).thenReturn(job);

		StepBuilder stepBuilder = mock(StepBuilder.class);
		TaskletStepBuilder taskletStepBuilder = mock(TaskletStepBuilder.class);
		TaskletStep taskletStep = mock(TaskletStep.class);
		when(builderFactory.getStepBuilder(any(String.class), any(JobRepository.class))).thenReturn(stepBuilder);
		when(stepBuilder.tasklet(any(Tasklet.class), any(PlatformTransactionManager.class))).thenReturn(taskletStepBuilder);
		when(taskletStepBuilder.build()).thenReturn(taskletStep);
		when(taskletStepBuilder.listener(any(StepExecutionListener.class))).thenReturn(taskletStepBuilder);
		when(taskletStepBuilder.build()).thenReturn(taskletStep);
		SimpleStepBuilder simpleStepBuilder = mock(SimpleStepBuilder.class);
		when(stepBuilder.chunk(any(Integer.class), any(PlatformTransactionManager.class))).thenReturn(simpleStepBuilder);
		when(simpleStepBuilder.reader(any(ItemReader.class))).thenReturn(simpleStepBuilder);
		when(simpleStepBuilder.processor(any(ItemProcessor.class))).thenReturn(simpleStepBuilder);
		when(simpleStepBuilder.writer(any(ItemWriter.class))).thenReturn(simpleStepBuilder);
		when(simpleStepBuilder.listener(any(ItemWriteListener.class))).thenReturn(simpleStepBuilder);
		when(simpleStepBuilder.build()).thenReturn(taskletStep);
		jiraProcessorJob.fetchIssueScrumBoardJob();
		jiraProcessorJob.fetchIssueKanbanBoardJob();
		jiraProcessorJob.fetchIssueScrumJqlJob();
		jiraProcessorJob.fetchIssueSprintJob();
		jiraProcessorJob.fetchIssueKanbanJqlJob();

	}
}