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
package com.publicissapient.kpidashboard.jira.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.jira.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;

import lombok.extern.slf4j.Slf4j;

/**
 * @author pankumar8
 *
 */
@Slf4j
@Service
public class JobScheduler {

	private static final String NUMBER_OF_PROCESSOR_AVAILABLE_MSG = "Total number of processor available : {} = number or projects run in parallel";
	private static final String PROJECT_ID = "projectId";
	private static final String CURRENTTIME = "currentTime";
	@Autowired
	JobLauncher jobLauncher;
	@Qualifier("fetchIssueScrumBoardJob")
	@Autowired
	Job fetchIssueScrumBoardJob;
	@Qualifier("fetchIssueScrumJqlJob")
	@Autowired
	Job fetchIssueScrumJqlJob;
	@Qualifier("fetchIssueKanbanBoardJob")
	@Autowired
	Job fetchIssueKanbanBoardJob;
	@Qualifier("fetchIssueKanbanJqlJob")
	@Autowired
	Job fetchIssueKanbanJqlJob;
	@Autowired
	private FetchProjectConfiguration fetchProjectConfiguration;

	/**
	 * This method is used to start scrum job setup with board
	 *
	 */
	@Async
	@Scheduled(cron = "${jira.scrumBoardCron}")
	public void startScrumBoardJob() {
		log.info("Request come for job for Scrum project configured with board via cron");

		List<String> scrumBoardbasicProjConfIds = fetchProjectConfiguration.fetchBasicProjConfId(JiraConstants.JIRA,
				false, false);
		log.info("Scrum - Board Wise Projects : {}", scrumBoardbasicProjConfIds);
		List<JobParameters> parameterSets = getDynamicParameterSets(scrumBoardbasicProjConfIds);
		log.info(NUMBER_OF_PROCESSOR_AVAILABLE_MSG, Runtime.getRuntime().availableProcessors());
		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		for (JobParameters params : parameterSets) {
			executorService.submit(() -> {
				try {
					jobLauncher.run(fetchIssueScrumBoardJob, params);
				} catch (Exception e) {
					log.info(
							"Jira Scrum data for board fetch failed for BasicProjectConfigId : {}, with exception : {}",
							params.getString(PROJECT_ID), e);
				}
			});
		}
		executorService.shutdown();
	}

	/**
	 * This method is used to start scrum job setup with JQL
	 *
	 */
	@Async
	@Scheduled(cron = "${jira.scrumJqlCron}")
	public void startScrumJqlJob() {
		log.info("Request coming for job for Scrum project configured with JQL via cron");

		List<String> scrumBoardbasicProjConfIds = fetchProjectConfiguration.fetchBasicProjConfId(JiraConstants.JIRA,
				true, false);

		List<JobParameters> parameterSets = getDynamicParameterSets(scrumBoardbasicProjConfIds);
		log.info(NUMBER_OF_PROCESSOR_AVAILABLE_MSG, Runtime.getRuntime().availableProcessors());
		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		for (JobParameters params : parameterSets) {
			executorService.submit(() -> {
				try {
					jobLauncher.run(fetchIssueScrumJqlJob, params);
				} catch (Exception e) {
					log.info("Jira Scrum data for JQL fetch failed for BasicProjectConfigId : {}, with exception : {}",
							params.getString(PROJECT_ID), e);
				}
			});
		}
		executorService.shutdown();
	}

	/**
	 * This method is used to start Kanban job setup with Board
	 *
	 */
	@Async
	@Scheduled(cron = "${jira.kanbanBoardCron}")
	public void startKanbanJob() {
		log.info("Request coming for job for Kanban project configured with Board via cron");
		List<String> kanbanBoardbasicProjConfIds = fetchProjectConfiguration.fetchBasicProjConfId(JiraConstants.JIRA,
				false, true);
		List<JobParameters> parameterSets = getDynamicParameterSets(kanbanBoardbasicProjConfIds);
		log.info(NUMBER_OF_PROCESSOR_AVAILABLE_MSG, Runtime.getRuntime().availableProcessors());
		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		for (JobParameters params : parameterSets) {
			executorService.submit(() -> {
				try {
					jobLauncher.run(fetchIssueKanbanBoardJob, params);
				} catch (Exception e) {
					log.info(
							"Jira Kanban data for board fetch failed for BasicProjectConfigId : {}, with exception : {}",
							params.getString(PROJECT_ID), e);
				}
			});
		}
		executorService.shutdown();
	}

	/**
	 * This method is used to start Kanban job setup with JQL
	 *
	 */
	@Async
	@Scheduled(cron = "${jira.kanbanJqlCron}")
	public void startKanbanJqlJob() {
		log.info("Request coming for job for Kanban project configured with JQL via cron");

		List<String> scrumBoardbasicProjConfIds = fetchProjectConfiguration.fetchBasicProjConfId(JiraConstants.JIRA,
				true, true);

		List<JobParameters> parameterSets = getDynamicParameterSets(scrumBoardbasicProjConfIds);
		log.info(NUMBER_OF_PROCESSOR_AVAILABLE_MSG, Runtime.getRuntime().availableProcessors());
		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		for (JobParameters params : parameterSets) {
			executorService.submit(() -> {
				try {
					jobLauncher.run(fetchIssueKanbanJqlJob, params);
				} catch (Exception e) {
					log.info("Jira Kanban data for JQL fetch failed for BasicProjectConfigId : {}, with exception : {}",
							params.getString(PROJECT_ID), e);
				}
			});
		}
		executorService.shutdown();
	}

	private List<JobParameters> getDynamicParameterSets(List<String> scrumBoardbasicProjConfIds) {
		List<JobParameters> parameterSets = new ArrayList<>();

		scrumBoardbasicProjConfIds.forEach(configId -> {
			JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
			// Add dynamic parameters as needed
			jobParametersBuilder.addString(PROJECT_ID, configId);
			jobParametersBuilder.addLong(CURRENTTIME, System.currentTimeMillis());

			JobParameters params = jobParametersBuilder.toJobParameters();
			parameterSets.add(params);
		});

		return parameterSets;
	}

}
