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
package com.publicissapient.kpidashboard.rally.jobs;

import com.publicissapient.kpidashboard.rally.aspect.TrackExecutionTime;
import com.publicissapient.kpidashboard.rally.config.RallyProcessorConfig;
import com.publicissapient.kpidashboard.rally.helper.BuilderFactory;
import com.publicissapient.kpidashboard.rally.listener.*;
import com.publicissapient.kpidashboard.rally.model.CompositeResult;
import com.publicissapient.kpidashboard.rally.model.ReadData;
import com.publicissapient.kpidashboard.rally.processor.IssueScrumProcessor;
import com.publicissapient.kpidashboard.rally.reader.*;
import com.publicissapient.kpidashboard.rally.tasklet.*;
import com.publicissapient.kpidashboard.rally.writer.IssueScrumWriter;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class RallyProcessorJob {

	@Autowired
	IssueRqlReader issueRqlReader;

	@Autowired
	IssueSprintReader issueSprintReader;

	@Autowired
	IssueScrumProcessor issueScrumProcessor;

	@Autowired
	IssueScrumWriter issueScrumWriter;

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
	JiraIssueJqlWriterListener jiraIssueJqlWriterListener;

	@Autowired
	JobListenerScrum jobListenerScrum;

	@Autowired
	JiraIssueSprintJobListener jiraIssueSprintJobListener;

	@Autowired
	RallyProcessorConfig rallyProcessorConfig;

	@Autowired
	JobRepository jobRepository;

	@Autowired
	PlatformTransactionManager transactionManager;

	@Autowired
	BuilderFactory builderFactory;

	@Autowired
	JobStepProgressListener jobStepProgressListener;

	private Step processProjectStatusStep() {
		return builderFactory.getStepBuilder("Fetch Release Status Scrum", jobRepository)
				.tasklet(jiraIssueReleaseStatusTasklet, transactionManager).listener(jobStepProgressListener).build();
	}

	private Step scrumReleaseDataStep() {
		return builderFactory.getStepBuilder("Fetch Release Data Scrum", jobRepository)
				.tasklet(scrumReleaseDataTasklet, transactionManager).listener(jobStepProgressListener).build();
	}


	/** Scrum projects for Jql job : Start * */
	/**
	 * @return Job
	 */
	@TrackExecutionTime
	@Bean
	public Job fetchIssueScrumRqlJob(@Qualifier("fetchIssueSprintJob") Job fetchIssueScrumRqlJob) {
		return builderFactory.getJobBuilder("FetchIssueScrum RQL Job", jobRepository).incrementer(new RunIdIncrementer())
				.start(metaDataStep()).next(processProjectStatusStep()).next(fetchIssueScrumRqlChunkStep())
				.next(scrumReleaseDataStep()).listener(jobListenerScrum).build();
	}

	@TrackExecutionTime
	private Step fetchIssueScrumRqlChunkStep() {
		return builderFactory.getStepBuilder("Fetch Issues Scrum Rql", jobRepository)
				.<ReadData, CompositeResult>chunk(getChunkSize(), this.transactionManager).reader(issueRqlReader)
				.processor(issueScrumProcessor).writer(issueScrumWriter).listener(jiraIssueJqlWriterListener).build();
	}

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

	/**
	 * This method is setup job for fetching sprint details based on sprint id
	 *
	 * @return job
	 */
	@TrackExecutionTime
	@Bean
	public Job runMetaDataStep() {
		return builderFactory
				.getJobBuilder("runMetaDataStep Job", jobRepository).incrementer(new RunIdIncrementer()).start(builderFactory
						.getStepBuilder("Fetch Metadata", jobRepository).tasklet(metaDataTasklet, transactionManager).build())
				.build();
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
		return rallyProcessorConfig.getChunkSize();
	}

	private Step metaDataStep() {
		return builderFactory.getStepBuilder("Fetch Metadata", jobRepository).tasklet(metaDataTasklet, transactionManager)
				.listener(jobStepProgressListener).build();
	}
}
