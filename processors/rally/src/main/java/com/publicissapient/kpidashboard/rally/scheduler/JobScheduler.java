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
package com.publicissapient.kpidashboard.rally.scheduler;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.publicissapient.kpidashboard.rally.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.rally.repository.RallyProcessorRepository;
import com.publicissapient.kpidashboard.rally.service.OngoingExecutionsService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;

import lombok.extern.slf4j.Slf4j;

import static com.publicissapient.kpidashboard.rally.controller.JobController.getJobParameters;

/**
 * @author pankumar8
 */
@Slf4j
@Service
public class JobScheduler {

	private static final String NUMBER_OF_PROCESSOR_AVAILABLE_MSG = "Total number of processor available : {} = number or projects run in parallel";
	private static final String PROJECT_ID = "projectId";
	private static final String CURRENTTIME = "currentTime";
	private static final String IS_SCHEDULER = "isScheduler";
	private static final String VALUE = "true";
	private static final String PROCESSOR_ID = "processorId";
	@Autowired
	JobLauncher jobLauncher;

	@Qualifier("fetchIssueScrumRqlJob")
	@Autowired
	Job fetchIssueScrumJqlJob;

	@Autowired
	private FetchProjectConfiguration fetchProjectConfiguration;
	@Autowired
	private OngoingExecutionsService ongoingExecutionsService;
	@Autowired
	private RallyProcessorRepository rallyProcessorRepository;

	/** This method is used to start scrum job setup with JQL */
	@Async
	@Scheduled(cron = "${rally.scrumRqlCron}")
	public void startScrumJqlJob() {
		log.info("Request coming for job for Scrum project configured with JQL via cron");

		List<String> scrumBoardbasicProjConfIds = fetchProjectConfiguration.fetchBasicProjConfId(ProcessorConstants.RALLY, true,
				false);

		List<JobParameters> parameterSets = getDynamicParameterSets(scrumBoardbasicProjConfIds);
		log.info(NUMBER_OF_PROCESSOR_AVAILABLE_MSG, Runtime.getRuntime().availableProcessors());
		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		for (JobParameters params : parameterSets) {
			executorService.submit(() -> {
				final String projectId = params.getString(PROJECT_ID);
				if (!ongoingExecutionsService.isExecutionInProgress(projectId)) {
					try {
						// making execution onGoing for project
						ongoingExecutionsService.markExecutionInProgress(projectId);
						jobLauncher.run(fetchIssueScrumJqlJob, params);
					} catch (Exception e) {
						log.info("Rally Scrum data for JQL fetch failed for BasicProjectConfigId : {}, with exception : {}",
								projectId, e);
						ongoingExecutionsService.markExecutionAsCompleted(projectId);
					}
				}
			});
		}
		executorService.shutdown();
	}
	private List<JobParameters> getDynamicParameterSets(List<String> scrumBoardbasicProjConfIds) {
		return getJobParameters(scrumBoardbasicProjConfIds, rallyProcessorRepository, PROJECT_ID, CURRENTTIME, IS_SCHEDULER, VALUE, PROCESSOR_ID);
	}
}
