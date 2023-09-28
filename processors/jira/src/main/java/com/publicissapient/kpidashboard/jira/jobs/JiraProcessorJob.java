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

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.publicissapient.kpidashboard.jira.aspect.TrackExecutionTime;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.listener.JiraIssueBoardWriterListener;
import com.publicissapient.kpidashboard.jira.listener.JiraIssueJqlWriterListener;
import com.publicissapient.kpidashboard.jira.listener.JiraIssueSprintJobListener;
import com.publicissapient.kpidashboard.jira.listener.JobListenerScrum;
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
	JobBuilderFactory jobBuilderFactory;

	@Autowired
	StepBuilderFactory stepBuilderFactory;

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
	JobListenerScrum jobListenerKanban;

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

	/** Scrum projects for board job : Start **/
	/**
	 * @return Job
	 */
	@TrackExecutionTime
	@Bean
	public Job fetchIssueScrumBoardJob() {
		return jobBuilderFactory.get("FetchIssueScrum Board Job").incrementer(new RunIdIncrementer())
				.start(metaDataStep()).next(sprintReportStep()).next(processProjectStatusStep())
				.next(fetchIssueScrumBoardChunkStep()).next(scrumReleaseDataStep()).listener(jobListenerScrum).build();
	}

	private Step metaDataStep() {
		return stepBuilderFactory.get("Fetch metadata").tasklet(metaDataTasklet).build();
	}

	private Step sprintReportStep() {
		return stepBuilderFactory.get("Fetch Sprint Report-Scrum-board").tasklet(sprintScrumBoardTasklet).build();
	}

	private Step processProjectStatusStep() {
		return stepBuilderFactory.get("processProjectStatus-Scrum-board").tasklet(jiraIssueReleaseStatusTasklet)
				.build();
	}

	@TrackExecutionTime
	private Step fetchIssueScrumBoardChunkStep() {
		return stepBuilderFactory.get("Fetch Issue-Scrum-board").<ReadData, CompositeResult>chunk(getChunkSize())
				.reader(issueBoardReader).processor(issueScrumProcessor).writer(issueScrumWriter)
				.listener(jiraIssueBoardWriterListener).build();
	}

	private Step scrumReleaseDataStep() {
		return stepBuilderFactory.get("ScrumReleaseData-Scrum-board").tasklet(scrumReleaseDataTasklet).build();
	}

	/** Scrum projects for board job : End **/

	/** Scrum projects for Jql job : Start **/
	/**
	 * @return Job
	 */
	@TrackExecutionTime
	@Bean
	public Job fetchIssueScrumJqlJob() {
		return jobBuilderFactory.get("FetchIssueScrum JQL Job").incrementer(new RunIdIncrementer())
				.start(metaDataStep()).next(processProjectStatusStep()).next(fetchIssueScrumJqlChunkStep())
				.next(scrumReleaseDataStep()).listener(jobListenerScrum).build();
	}

	@TrackExecutionTime
	private Step fetchIssueScrumJqlChunkStep() {
		return stepBuilderFactory.get("Fetch Issue-Scrum-Jql").<ReadData, CompositeResult>chunk(getChunkSize())
				.reader(issueJqlReader).processor(issueScrumProcessor).writer(issueScrumWriter)
				.listener(jiraIssueJqlWriterListener).build();
	}

	/** Scrum projects for Jql job : End **/

	/** Kanban projects for board job : Start **/
	/**
	 * @return Job
	 */
	@TrackExecutionTime
	@Bean
	public Job fetchIssueKanbanBoardJob() {
		return jobBuilderFactory.get("FetchIssueKanban Job").incrementer(new RunIdIncrementer()).start(metaDataStep())
				.next(fetchIssueKanbanBoardChunkStep()).next(kanbanReleaseDataStep()).listener(jobListenerKanban)
				.build();
	}

	@TrackExecutionTime
	private Step fetchIssueKanbanBoardChunkStep() {
		return stepBuilderFactory.get("Fetch Issue-Kanban-board").<ReadData, CompositeResult>chunk(getChunkSize())
				.reader(issueBoardReader).processor(issueKanbanProcessor).writer(issueKanbanWriter)
				.listener(kanbanJiraIssueWriterListener).build();
	}

	private Step kanbanReleaseDataStep() {
		return stepBuilderFactory.get("KanbanReleaseData-Kanban-board").tasklet(kanbanReleaseDataTasklet).build();
	}

	/** Kanban projects for board job : End **/

	/** Kanban projects for Jql job : Start **/
	/**
	 * @return Job
	 */
	@TrackExecutionTime
	@Bean
	public Job fetchIssueKanbanJqlJob() {
		return jobBuilderFactory.get("FetchIssueKanban JQL Job").incrementer(new RunIdIncrementer())
				.start(metaDataStep()).next(fetchIssueKanbanJqlChunkStep()).next(kanbanReleaseDataStep())
				.listener(jobListenerKanban).build();
	}

	@TrackExecutionTime
	private Step fetchIssueKanbanJqlChunkStep() {
		return stepBuilderFactory.get("Fetch Issue-Kanban-Jql").<ReadData, CompositeResult>chunk(getChunkSize())
				.reader(issueJqlReader).processor(issueKanbanProcessor).writer(issueKanbanWriter)
				.listener(kanbanJiraIssueJqlWriterListener).build();
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
		return jobBuilderFactory.get("fetchIssueSprint Job").incrementer(new RunIdIncrementer()).start(sprintDataStep())
				.next(fetchIssueSprintChunkStep()).listener(jiraIssueSprintJobListener).build();
	}

	private Step sprintDataStep() {
		return stepBuilderFactory.get("Fetch Sprint Data").tasklet(sprintReportTasklet).build();
	}

	@TrackExecutionTime
	private Step fetchIssueSprintChunkStep() {
		return stepBuilderFactory.get("Fetch Issue-Sprint").<ReadData, CompositeResult>chunk(getChunkSize())
				.reader(issueSprintReader).processor(issueScrumProcessor).writer(issueScrumWriter).build();
	}

	private Integer getChunkSize() {
		return jiraProcessorConfig.getChunkSize();
	}

}
