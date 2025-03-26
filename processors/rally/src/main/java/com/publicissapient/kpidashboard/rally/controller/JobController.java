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
package com.publicissapient.kpidashboard.rally.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.publicissapient.kpidashboard.rally.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.rally.constant.RallyConstants;
import com.publicissapient.kpidashboard.rally.repository.RallyProcessorRepository;
import com.publicissapient.kpidashboard.rally.service.OngoingExecutionsService;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author pankumar8
 */
@RestController
@RequestMapping("/api/job")
@Slf4j
public class JobController {

	private static final String NUMBER_OF_PROCESSOR_AVAILABLE_MSG = "Total number of processor available : {} = number or projects run in parallel";
	private static final String PROJECT_ID = "projectId";
	private static final String SPRINT_ID = "sprintId";
	private static final String CURRENTTIME = "currentTime";
	private static final String IS_SCHEDULER = "isScheduler";
	private static final String VALUE = "false";
	private static final String PROCESSOR_ID = "processorId";
	@Autowired
	JobLauncher jobLauncher;

	@Qualifier("fetchIssueScrumRqlJob")
	@Autowired
	Job fetchIssueScrumRqlJob;

	@Qualifier("fetchIssueSprintJob")
	@Autowired
	Job fetchIssueSprintJob;

	@Qualifier("runMetaDataStep")
	@Autowired
	Job runMetaDataStep;

	@Autowired
	private ProjectToolConfigRepository toolRepository;

	@Autowired
	private ProjectBasicConfigRepository projectConfigRepository;

	@Autowired
	private FetchProjectConfiguration fetchProjectConfiguration;

	@Autowired
	private OngoingExecutionsService ongoingExecutionsService;

	@Autowired
	private RallyProcessorRepository rallyProcessorRepository;

	/**
	 * This method is used to start job for the Scrum projects with JQL setup
	 *
	 * @return ResponseEntity
	 */
	@GetMapping("/startscrumjqljob")
	public ResponseEntity<String> startScrumJqlJob() {
		log.info("Request come for job for Scrum project configured with JQL via controller");

		List<String> scrumBoardbasicProjConfIds = fetchProjectConfiguration.fetchBasicProjConfId(RallyConstants.RALLY,
				true, false);

		List<JobParameters> parameterSets = getDynamicParameterSets(scrumBoardbasicProjConfIds);
		log.info(NUMBER_OF_PROCESSOR_AVAILABLE_MSG, Runtime.getRuntime().availableProcessors());

		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		for (JobParameters params : parameterSets) {
			executorService.submit(() -> {
				try {
					jobLauncher.run(fetchIssueScrumRqlJob, params);
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
		return getJobParameters(scrumBoardbasicProjConfIds, rallyProcessorRepository, PROJECT_ID, CURRENTTIME,
				IS_SCHEDULER, VALUE, PROCESSOR_ID);
	}

	public static List<JobParameters> getJobParameters(List<String> scrumBoardbasicProjConfIds,
			RallyProcessorRepository rallyProcessorRepository, String projectId, String currenttime, String isScheduler,
			String value, String processorId) {
		List<JobParameters> parameterSets = new ArrayList<>();
		ObjectId jiraProcessorId = rallyProcessorRepository.findByProcessorName(ProcessorConstants.JIRA).getId();
		scrumBoardbasicProjConfIds.forEach(configId -> {
			JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
			// Add dynamic parameters as needed
			jobParametersBuilder.addString(projectId, configId);
			jobParametersBuilder.addLong(currenttime, System.currentTimeMillis());
			jobParametersBuilder.addString(isScheduler, value);
			jobParametersBuilder.addString(processorId, jiraProcessorId.toString());

			JobParameters params = jobParametersBuilder.toJobParameters();
			parameterSets.add(params);
		});

		return parameterSets;
	}

	/**
	 * This method is used to fetch the sprint report data
	 *
	 * @param sprintId
	 *            sprintId
	 * @return ResponseEntity
	 */
	@PostMapping(value = "/startfetchsprintjob", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> startFetchSprintJob(@RequestBody String sprintId) {
		log.info("Request coming for fetching sprint job");
		ObjectId jiraProcessorId = rallyProcessorRepository.findByProcessorName(ProcessorConstants.JIRA).getId();
		CompletableFuture.runAsync(() -> {
			JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
			jobParametersBuilder.addString(SPRINT_ID, sprintId);
			jobParametersBuilder.addLong(CURRENTTIME, System.currentTimeMillis());
			jobParametersBuilder.addString(PROCESSOR_ID, jiraProcessorId.toString());
			JobParameters params = jobParametersBuilder.toJobParameters();
			try {
				jobLauncher.run(fetchIssueSprintJob, params);
			} catch (Exception e) {
				log.info("Jira Sprint data fetch failed for SprintId : {}, with exception : {}",
						params.getString(SPRINT_ID), e);
			}
		});
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
		ObjectId jiraProcessorId = rallyProcessorRepository.findByProcessorName(ProcessorConstants.RALLY).getId();
		// Start the job asynchronously
		CompletableFuture.runAsync(() -> {
			JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
			jobParametersBuilder.addString(PROJECT_ID, basicProjectConfigId);
			jobParametersBuilder.addLong(CURRENTTIME, System.currentTimeMillis());
			jobParametersBuilder.addString(IS_SCHEDULER, VALUE);
			jobParametersBuilder.addString(PROCESSOR_ID, jiraProcessorId.toString());
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

	/**
	 * This method is used to fetch the metadata
	 *
	 * @param projectBasicConfigId
	 *            projectBasicConfigId
	 * @return ResponseEntity
	 */
	@PostMapping(value = "/runMetadataStep", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> runMetadataStep(@RequestBody String projectBasicConfigId) {
		log.info("Request coming for fetching sprint job");
		ObjectId jiraProcessorId = rallyProcessorRepository.findByProcessorName(ProcessorConstants.JIRA).getId();
		CompletableFuture.runAsync(() -> {
			JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
			jobParametersBuilder.addString(PROJECT_ID, projectBasicConfigId);
			jobParametersBuilder.addLong(CURRENTTIME, System.currentTimeMillis());
			jobParametersBuilder.addString(PROCESSOR_ID, jiraProcessorId.toString());
			jobParametersBuilder.addString(IS_SCHEDULER, VALUE);
			JobParameters params = jobParametersBuilder.toJobParameters();
			try {
				jobLauncher.run(runMetaDataStep, params);
			} catch (Exception e) {
				log.info("Jira Metadata failed for ProjectBasicConfigId : {}, with exception : {}",
						params.getString(PROJECT_ID), e);
			}
		});
		return ResponseEntity.ok().body("job started for Project : " + projectBasicConfigId);
	}

	private void runProjectBasedOnConfig(String basicProjectConfigId, JobParameters params,
			Optional<ProjectBasicConfig> projBasicConfOpt) throws JobExecutionAlreadyRunningException,
			JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		if (projBasicConfOpt.isPresent()) {
			ProjectBasicConfig projectBasicConfig = projBasicConfOpt.get();
			List<ProjectToolConfig> projectToolConfigs = toolRepository
					.findByToolNameAndBasicProjectConfigId(RallyConstants.RALLY, projectBasicConfig.getId());

			if (!projectBasicConfig.isKanban()) {
				// Project is scrum
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
				jobLauncher.run(fetchIssueScrumRqlJob, params);
			}
		} else {
			log.info("removing project with basicProjectConfigId {}", basicProjectConfigId);
			// Mark the execution as completed
			ongoingExecutionsService.markExecutionAsCompleted(basicProjectConfigId);
		}
	}

}
