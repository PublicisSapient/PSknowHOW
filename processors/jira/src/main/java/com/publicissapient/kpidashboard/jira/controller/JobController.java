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
package com.publicissapient.kpidashboard.jira.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.common.model.ProcessorExecutionBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.jira.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.service.OngoingExecutionsService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author pankumar8
 *
 */
@RestController
@RequestMapping("/api/job")
@Slf4j
public class JobController {

	private static final String NUMBER_OF_PROCESSOR_AVAILABLE_MSG = "Total number of processor available : {} = number or projects run in parallel";
	private static final String PROJECT_ID = "projectId";
	private static final String SPRINT_ID = "sprintId";
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
	@Qualifier("fetchIssueSprintJob")
	@Autowired
	Job fetchIssueSprintJob;
	@Autowired
	private ProjectToolConfigRepository toolRepository;
	@Autowired
	private ProjectBasicConfigRepository projectConfigRepository;
	@Autowired
	private FetchProjectConfiguration fetchProjectConfiguration;
	@Autowired
	private OngoingExecutionsService ongoingExecutionsService;

	/**
	 * This method is used to start job for the Scrum projects with board setup
	 * 
	 * @return ResponseEntity
	 */

	@GetMapping("/startscrumboardjob")
	public ResponseEntity<String> startScrumBoardJob() {
		log.info("Request come for job for Scrum project configured with board via controller");
		int totalProjects = 0;
		List<String> scrumBoardbasicProjConfIds = fetchProjectConfiguration.fetchBasicProjConfId(JiraConstants.JIRA,
				false, false);
		totalProjects = scrumBoardbasicProjConfIds.size();
		log.info("Total projects to fun for Scrum - Board Wise : {}", totalProjects);
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
		return ResponseEntity.ok().body("job started for scrum board");
	}

	/**
	 * This method is used to start job for the Scrum projects with JQL setup
	 * 
	 * @return ResponseEntity
	 */

	@GetMapping("/startscrumjqljob")
	public ResponseEntity<String> startScrumJqlJob() {
		log.info("Request come for job for Scrum project configured with JQL via controller");

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
		return ResponseEntity.ok().body("job started for scrum JQL");
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

	/**
	 * This method is used to start job for the Kanban projects with board setup
	 * 
	 * @return ResponseEntity
	 */
	@GetMapping("/startkanbanboardjob")
	public ResponseEntity<String> startKanbanJob() {
		log.info("Request come for job for Kanban project configured with Board via controller");
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
		return ResponseEntity.ok().body("job started for Kanban Board");
	}

	/**
	 * This method is used to start job for the Kanban projects with JQL setup
	 * 
	 * @return ResponseEntity
	 * 
	 */
	@GetMapping("/startkanbanjqljob")
	public ResponseEntity<String> startKanbanJqlJob() {
		log.info("Request come for job for Kanban project configured with JQL via controller");

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
		return ResponseEntity.ok().body("job started for Kanban JQL");
	}

	/**
	 * This method is used to fetch the sprint report data
	 * 
	 * @param sprintId
	 *            sprintId
	 * @return ResponseEntity
	 */
	@Async
	@PostMapping("/startfetchsprintjob")
	public ResponseEntity<String> startFetchSprintJob(@RequestBody String sprintId) {
		log.info("Request coming for fetching sprint job");
		JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();

		jobParametersBuilder.addString(SPRINT_ID, sprintId);
		jobParametersBuilder.addLong(CURRENTTIME, System.currentTimeMillis());
		JobParameters params = jobParametersBuilder.toJobParameters();
		try {
			jobLauncher.run(fetchIssueSprintJob, params);
		} catch (Exception e) {
			log.info("Jira Sprint data fetch failed for SprintId : {}, with exception : {}",
					params.getString(SPRINT_ID), e);
		}
		return ResponseEntity.ok().body("job started for Sprint : " + sprintId);

	}

	/**
	 * This method is used to fetch the jira issues based on project id
	 * 
	 * @param processorExecutionBasicConfig
	 *            processorExecutionBasicConfig
	 * @return ResponseEntity
	 */
	@PostMapping("/startprojectwiseissuejob")
	public ResponseEntity<String> startProjectWiseIssueJob(
			@RequestBody ProcessorExecutionBasicConfig processorExecutionBasicConfig) {
		log.info("Request coming for fetching issue job");

		String basicProjectConfigId = processorExecutionBasicConfig.getProjectBasicConfigIds().get(0);

		if (ongoingExecutionsService.isExecutionInProgress(basicProjectConfigId)) {
			log.error("An execution is already in progress");
			return ResponseEntity.badRequest()
					.body("Jira processor run is already in progress for this project. Please try after some time.");
		}

		// Mark the execution as in progress before starting the job asynchronously
		ongoingExecutionsService.markExecutionInProgress(basicProjectConfigId);

		// Start the job asynchronously
		CompletableFuture.runAsync(() -> {
			JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
			jobParametersBuilder.addString(PROJECT_ID, basicProjectConfigId);
			jobParametersBuilder.addLong(CURRENTTIME, System.currentTimeMillis());
			JobParameters params = jobParametersBuilder.toJobParameters();

			try {
				Optional<ProjectBasicConfig> projBasicConfOpt = projectConfigRepository
						.findById(new ObjectId(basicProjectConfigId));

				runProjectBasedOnConfig(basicProjectConfigId, params, projBasicConfOpt);
			} catch (Exception e) {
				log.error("Jira fetch failed for BasicProjectConfigId : {}, with exception : {}",
						params.getString(PROJECT_ID), e);
			}
		});
		return ResponseEntity.ok().body("Job started for BasicProjectConfigId: " + basicProjectConfigId);
	}

	private void runProjectBasedOnConfig(String basicProjectConfigId, JobParameters params,
			Optional<ProjectBasicConfig> projBasicConfOpt) throws JobExecutionAlreadyRunningException,
			JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		if (projBasicConfOpt.isPresent()) {
			ProjectBasicConfig projectBasicConfig = projBasicConfOpt.get();
			List<ProjectToolConfig> projectToolConfigs = toolRepository
					.findByToolNameAndBasicProjectConfigId(JiraConstants.JIRA, projectBasicConfig.getId());

			if (projectBasicConfig.isKanban()) {
				// Project is kanban
				launchJobBasedOnQueryEnabledForKanban(basicProjectConfigId, params, projectToolConfigs);
			} else {
				// Project is Scrum
				launchJobBasedOnQueryEnabledForScrum(basicProjectConfigId, params, projectToolConfigs);
			}
		}
	}

	private void launchJobBasedOnQueryEnabledForScrum(String basicProjectConfigId, JobParameters params,
			List<ProjectToolConfig> projectToolConfigs) throws JobExecutionAlreadyRunningException, JobRestartException,
			JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		if (CollectionUtils.isNotEmpty(projectToolConfigs)) {
			ProjectToolConfig projectToolConfig = projectToolConfigs.get(0);

			if (projectToolConfig.isQueryEnabled()) {
				// JQL is setup for the project
				jobLauncher.run(fetchIssueScrumJqlJob, params);
			} else {
				// Board is setup for the project
				jobLauncher.run(fetchIssueScrumBoardJob, params);
			}
		} else {
			log.info("removing project with basicProjectConfigId {}", basicProjectConfigId);
			// Mark the execution as completed
			ongoingExecutionsService.markExecutionAsCompleted(basicProjectConfigId);
		}
	}

	private void launchJobBasedOnQueryEnabledForKanban(String basicProjectConfigId, JobParameters params,
			List<ProjectToolConfig> projectToolConfigs) throws JobExecutionAlreadyRunningException, JobRestartException,
			JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		if (CollectionUtils.isNotEmpty(projectToolConfigs)) {
			ProjectToolConfig projectToolConfig = projectToolConfigs.get(0);

			if (projectToolConfig.isQueryEnabled()) {
				// JQL is setup for the project
				jobLauncher.run(fetchIssueKanbanJqlJob, params);
			} else {
				// Board is setup for the project
				jobLauncher.run(fetchIssueKanbanBoardJob, params);
			}
		} else {
			log.info("removing project with basicProjectConfigId {}", basicProjectConfigId);
			// Mark the execution as completed
			ongoingExecutionsService.markExecutionAsCompleted(basicProjectConfigId);
		}
	}

}
