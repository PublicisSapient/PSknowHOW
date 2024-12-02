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
package com.publicissapient.kpidashboard.jira.jobs;

import com.publicissapient.kpidashboard.jira.tasklet.KanbanJiraIssueReleaseStatusTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.publicissapient.kpidashboard.jira.aspect.TrackExecutionTime;
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
import com.publicissapient.kpidashboard.jira.model.CompositeResult;
import com.publicissapient.kpidashboard.jira.model.ReadData;
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

/**
 * @author pankumar8
 *
 */
@Configuration
public class JiraProcessorJob {

	@Autowired
	IssueBoardReader issueBoardReader;

	@Autowired
	IssueJqlReader issueJqlReader;

	@Autowired
	IssueSprintReader issueSprintReader;

	@Autowired
	IssueScrumProcessor issueScrumProcessor;

	@Autowired
	IssueScrumWriter issueScrumWriter;

	@Autowired
	IssueKanbanWriter issueKanbanWriter;

	@Autowired
	MetaDataTasklet metaDataTasklet;

	@Autowired
	SprintScrumBoardTasklet sprintScrumBoardTasklet;

	@Autowired
	JiraIssueReleaseStatusTasklet jiraIssueReleaseStatusTasklet;

	@Autowired
	KanbanJiraIssueReleaseStatusTasklet kanbanJiraIssueReleaseStatusTasklet;

	@Autowired
	SprintReportTasklet sprintReportTasklet;

	@Autowired
	ScrumReleaseDataTasklet scrumReleaseDataTasklet;

	@Autowired
	KanbanReleaseDataTasklet kanbanReleaseDataTasklet;

	@Autowired
	JiraIssueBoardWriterListener jiraIssueBoardWriterListener;

	@Autowired
	JiraIssueJqlWriterListener jiraIssueJqlWriterListener;

	@Autowired
	JobListenerScrum jobListenerScrum;

	@Autowired
	JobListenerKanban jobListenerKanban;

	@Autowired
	JiraIssueSprintJobListener jiraIssueSprintJobListener;

	@Autowired
	IssueKanbanProcessor issueKanbanProcessor;

	@Autowired
	KanbanJiraIssueWriterListener kanbanJiraIssueWriterListener;

	@Autowired
	KanbanJiraIssueJqlWriterListener kanbanJiraIssueJqlWriterListener;

	@Autowired
	JiraProcessorConfig jiraProcessorConfig;

	@Autowired
	JobRepository jobRepository;

	@Autowired
	PlatformTransactionManager transactionManager;

	@Autowired
	BuilderFactory builderFactory;

	@Autowired
	JobStepProgressListener jobStepProgressListener;

	/** Scrum projects for board job : Start **/
	/**
	 * @return Job
	 */
	@TrackExecutionTime
	@Bean
	public Job fetchIssueScrumBoardJob() {
		return builderFactory.getJobBuilder("FetchIssueScrum Board Job", jobRepository)
				.incrementer(new RunIdIncrementer()).start(metaDataStep()).next(sprintReportStep())
				.next(processProjectStatusStep()).next(fetchIssueScrumBoardChunkStep()).next(scrumReleaseDataStep())
				.listener(jobListenerScrum).build();
	}

	private Step metaDataStep() {
		return builderFactory.getStepBuilder("Fetch Metadata", jobRepository)
				.tasklet(metaDataTasklet, transactionManager).listener(jobStepProgressListener).build();
	}

	private Step sprintReportStep() {
		return builderFactory.getStepBuilder("Fetch Sprint Report Scrum Board", jobRepository)
				.tasklet(sprintScrumBoardTasklet, transactionManager).listener(jobStepProgressListener).build();
	}

	private Step processProjectStatusStep() {
		return builderFactory.getStepBuilder("Fetch Release Status Scrum", jobRepository)
				.tasklet(jiraIssueReleaseStatusTasklet, transactionManager).listener(jobStepProgressListener).build();
	}

	private Step processKanbanProjectStatusStep() {
		return builderFactory.getStepBuilder("Fetch Release Status Kanban", jobRepository)
				.tasklet(kanbanJiraIssueReleaseStatusTasklet, transactionManager).listener(jobStepProgressListener).build();
	}

	@TrackExecutionTime
	private Step fetchIssueScrumBoardChunkStep() {
		return builderFactory.getStepBuilder("Fetch Issues Scrum Board", jobRepository)
				.<ReadData, CompositeResult>chunk(getChunkSize(), transactionManager).reader(issueBoardReader)
				.processor(issueScrumProcessor).writer(issueScrumWriter).listener(jiraIssueBoardWriterListener).build();
	}

	private Step scrumReleaseDataStep() {
		return builderFactory.getStepBuilder("Fetch Release Data Scrum", jobRepository)
				.tasklet(scrumReleaseDataTasklet, transactionManager).listener(jobStepProgressListener).build();
	}

	/** Scrum projects for board job : End **/

	/** Scrum projects for Jql job : Start **/
	/**
	 * @return Job
	 */
	@TrackExecutionTime
	@Bean
	public Job fetchIssueScrumJqlJob() {
		return builderFactory.getJobBuilder("FetchIssueScrum JQL Job", jobRepository)
				.incrementer(new RunIdIncrementer()).start(metaDataStep()).next(processProjectStatusStep())
				.next(fetchIssueScrumJqlChunkStep()).next(scrumReleaseDataStep()).listener(jobListenerScrum).build();
	}

	@TrackExecutionTime
	private Step fetchIssueScrumJqlChunkStep() {
		return builderFactory.getStepBuilder("Fetch Issues Scrum Jql", jobRepository)
				.<ReadData, CompositeResult>chunk(getChunkSize(), this.transactionManager).reader(issueJqlReader)
				.processor(issueScrumProcessor).writer(issueScrumWriter).listener(jiraIssueJqlWriterListener).build();
	}

	/** Scrum projects for Jql job : End **/

	/** Kanban projects for board job : Start **/
	/**
	 * @return Job
	 */
	@TrackExecutionTime
	@Bean
	public Job fetchIssueKanbanBoardJob() {
		return builderFactory.getJobBuilder("FetchIssueKanban Job", jobRepository).incrementer(new RunIdIncrementer())
				.start(metaDataStep()).next(fetchIssueKanbanBoardChunkStep()).next(kanbanReleaseDataStep())
				.next(processKanbanProjectStatusStep()).listener(jobListenerKanban).build();

	}

	@TrackExecutionTime
	private Step fetchIssueKanbanBoardChunkStep() {
		return builderFactory.getStepBuilder("Fetch Issues Kanban Board", jobRepository)
				.<ReadData, CompositeResult>chunk(getChunkSize(), this.transactionManager).reader(issueBoardReader)
				.processor(issueKanbanProcessor).writer(issueKanbanWriter).listener(kanbanJiraIssueWriterListener)
				.build();
	}

	private Step kanbanReleaseDataStep() {
		return builderFactory.getStepBuilder("Fetch Release Data Kanban", jobRepository)
				.tasklet(kanbanReleaseDataTasklet, transactionManager).listener(jobStepProgressListener).build();
	}

	/** Kanban projects for board job : End **/

	/** Kanban projects for Jql job : Start **/
	/**
	 * @return Job
	 */
	@TrackExecutionTime
	@Bean
	public Job fetchIssueKanbanJqlJob() {
		return builderFactory.getJobBuilder("FetchIssueKanban JQL Job", jobRepository)
				.incrementer(new RunIdIncrementer()).start(metaDataStep()).next(fetchIssueKanbanJqlChunkStep())
				.next(kanbanReleaseDataStep()).next(processKanbanProjectStatusStep()).listener(jobListenerKanban).build();
	}

	@TrackExecutionTime
	private Step fetchIssueKanbanJqlChunkStep() {
		return builderFactory.getStepBuilder("Fetch Issues Kanban Jql", jobRepository)
				.<ReadData, CompositeResult>chunk(getChunkSize(), transactionManager).reader(issueJqlReader)
				.processor(issueKanbanProcessor).writer(issueKanbanWriter).listener(kanbanJiraIssueJqlWriterListener)
				.build();
	}

	/** Kanban projects for Jql job : End **/

	/**
	 * This method is setup job for fetching sprint details based on sprint id
	 * 
	 * @return job
	 */
	@TrackExecutionTime
	@Bean
	public Job fetchIssueSprintJob() {
		return builderFactory.getJobBuilder("fetchIssueSprint Job", jobRepository).incrementer(new RunIdIncrementer())
				.start(sprintDataStep()).next(fetchIssueSprintChunkStep()).listener(jiraIssueSprintJobListener).build();
	}

	private Step sprintDataStep() {
		return builderFactory.getStepBuilder("Fetch Sprint Data", jobRepository)
				.tasklet(sprintReportTasklet, transactionManager).build();
	}

	@TrackExecutionTime
	private Step fetchIssueSprintChunkStep() {
		return builderFactory.getStepBuilder("Fetch Issue-Sprint", jobRepository)
				.<ReadData, CompositeResult>chunk(getChunkSize(), this.transactionManager).reader(issueSprintReader)
				.processor(issueScrumProcessor).writer(issueScrumWriter).build();
	}

	private Integer getChunkSize() {
		return jiraProcessorConfig.getChunkSize();
	}

}
