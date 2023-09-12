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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.publicissapient.kpidashboard.common.model.ProcessorExecutionBasicConfig;
import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.jira.cache.JiraProcessorCacheEvictor;
import com.publicissapient.kpidashboard.jira.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;

import lombok.extern.slf4j.Slf4j;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author pankumar8
 *
 */
@RestController
@RequestMapping("/api/job")
@Slf4j
public class JobController {

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
	private JiraProcessorCacheEvictor jiraProcessorCacheEvictor;

	private static String PROJECT_ID = "projectId";
	private static String SPRINT_ID = "sprintId";
	private static String CURRENTTIME = "currentTime";

	/**
	 * This method is used to start job for the Scrum projects with board setup
	 * 
	 * @return ResponseEntity
	 * @throws Exception
	 */

	@GetMapping("/startscrumboardjob")
	public ResponseEntity<String> startScrumBoardJob() throws Exception {
		log.info("Request come for job for Scrum project configured with board via controller");
		int completedProjects = 0;
		int totalProjects = 0;
		List<String> scrumBoardbasicProjConfIds = fetchProjectConfiguration.fetchBasicProjConfId(JiraConstants.JIRA,
				false, false);
		totalProjects = scrumBoardbasicProjConfIds.size();
		log.info("Total projects to fun for Scrum - Board Wise : {}", totalProjects);
		log.info("Scrum - Board Wise Projects : {}", scrumBoardbasicProjConfIds);
		List<JobParameters> parameterSets = getDynamicParameterSets(scrumBoardbasicProjConfIds);

		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		for (JobParameters params : parameterSets) {
			executorService.submit(() -> {
				try {
					jobLauncher.run(fetchIssueScrumBoardJob, params);
				} catch (Exception e) {
					log.info("Jira Scrum data for board fetch failed for BasicProjectConfigId : {}",
							params.getString(PROJECT_ID));
					e.printStackTrace();
				}
			});
			completedProjects++;
		}
		if (completedProjects == totalProjects) {
			jiraProcessorCacheEvictor.evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT,
					CommonConstant.CACHE_ACCOUNT_HIERARCHY);
			jiraProcessorCacheEvictor.evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.JIRA_KPI_CACHE);
		}
		executorService.shutdown();
		return ResponseEntity.ok().body("job started for scrum board");
	}

	/**
	 * This method is used to start job for the Scrum projects with JQL setup
	 * 
	 * @return ResponseEntity
	 * @throws Exception
	 */

	@GetMapping("/startscrumjqljob")
	public ResponseEntity<String> startScrumJqlJob() throws Exception {
		log.info("Request coming for job for Scrum project configured with JQL");

		List<String> scrumBoardbasicProjConfIds = fetchProjectConfiguration.fetchBasicProjConfId(JiraConstants.JIRA,
				true, false);

		List<JobParameters> parameterSets = getDynamicParameterSets(scrumBoardbasicProjConfIds);

		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		for (JobParameters params : parameterSets) {
			executorService.submit(() -> {
				try {
					jobLauncher.run(fetchIssueScrumJqlJob, params);
				} catch (Exception e) {
					log.info("Jira Scrum data for JQL fetch failed for BasicProjectConfigId : {}",
							params.getString(PROJECT_ID));
					e.printStackTrace();
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
	 * @throws Exception
	 */
	@GetMapping("/startkanbanboardjob")
	public ResponseEntity<String> startKanbanJob() throws Exception {
		log.info("Request coming for job");
		List<String> kanbanBoardbasicProjConfIds = fetchProjectConfiguration.fetchBasicProjConfId(JiraConstants.JIRA,
				false, true);
		List<JobParameters> parameterSets = getDynamicParameterSets(kanbanBoardbasicProjConfIds);

		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		for (JobParameters params : parameterSets) {
			executorService.submit(() -> {
				try {
					jobLauncher.run(fetchIssueKanbanBoardJob, params);
				} catch (Exception e) {
					log.info("Jira Kanban data for board fetch failed for BasicProjectConfigId : {}",
							params.getString(PROJECT_ID));
					e.printStackTrace();
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
	 * @throws Exception
	 */
	@GetMapping("/startkanbanjqljob")
	public ResponseEntity<String> startKanbanJqlJob() throws Exception {
		log.info("Request coming for job for Kanban project configured with JQL");

		List<String> scrumBoardbasicProjConfIds = fetchProjectConfiguration.fetchBasicProjConfId(JiraConstants.JIRA,
				true, true);

		List<JobParameters> parameterSets = getDynamicParameterSets(scrumBoardbasicProjConfIds);

		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		for (JobParameters params : parameterSets) {
			executorService.submit(() -> {
				try {
					jobLauncher.run(fetchIssueKanbanJqlJob, params);
				} catch (Exception e) {
					log.info("Jira Kanban data for JQL fetch failed for BasicProjectConfigId : {}",
							params.getString(PROJECT_ID));
					e.printStackTrace();
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
	 * @return ResponseEntity
	 * @throws Exception
	 */
	@GetMapping("/startfetchsprintjob")
	public ResponseEntity<String> startFetchSprintJob(@RequestBody String sprintId) throws Exception {
		log.info("Request coming for fetching sprint job");
		JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();

		jobParametersBuilder.addString(SPRINT_ID, sprintId);
		jobParametersBuilder.addLong(CURRENTTIME, System.currentTimeMillis());
		JobParameters params = jobParametersBuilder.toJobParameters();
		try {
			jobLauncher.run(fetchIssueSprintJob, params);
		} catch (Exception e) {
			log.info("Jira Sprint data fetch failed for SprintId : {}", params.getString(SPRINT_ID));
			e.printStackTrace();
		}
		return ResponseEntity.ok().body("job started for Sprint : " + sprintId);

	}

	/**
	 * This method is used to fetch the jira issues based on project id
	 * 
	 * @param processorExecutionBasicConfig
	 * @return ResponseEntity
	 * @throws Exception
	 */

	@PostMapping("/startprojectwiseissuejob")
	public ResponseEntity<String> startProjectWiseIssueJob(@RequestBody ProcessorExecutionBasicConfig processorExecutionBasicConfig) throws Exception {
		log.info("Request coming for fetching sprint job");
		JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();

		String basicProjectConfigId = processorExecutionBasicConfig.getProjectBasicConfigIds().get(0);
		jobParametersBuilder.addString(PROJECT_ID, basicProjectConfigId);
		jobParametersBuilder.addLong(CURRENTTIME, System.currentTimeMillis());
		JobParameters params = jobParametersBuilder.toJobParameters();
		try {
			Optional<ProjectBasicConfig> projBasicConfOpt = projectConfigRepository
					.findById(new ObjectId(basicProjectConfigId));
			if (projBasicConfOpt.isPresent()) {
				ProjectBasicConfig projectBasicConfig = projBasicConfOpt.get();
				List<ProjectToolConfig> projectToolConfigs = toolRepository
						.findByToolNameAndBasicProjectConfigId(JiraConstants.JIRA, projectBasicConfig.getId());
				if (projectBasicConfig.isKanban()) {
					// Project is kanban
					if (CollectionUtils.isNotEmpty(projectToolConfigs)) {
						ProjectToolConfig projectToolConfig = projectToolConfigs.get(0);
						if (projectToolConfig.isQueryEnabled()) {
							// JQL is setup for the project
							jobLauncher.run(fetchIssueKanbanJqlJob, params);
						} else {
							// Board is setup for the project
							jobLauncher.run(fetchIssueKanbanBoardJob, params);
						}
					}
				} else {
					// Project is Scrum
					if (CollectionUtils.isNotEmpty(projectToolConfigs)) {
						ProjectToolConfig projectToolConfig = projectToolConfigs.get(0);
						if (projectToolConfig.isQueryEnabled()) {
							// JQL is setup for the project
							jobLauncher.run(fetchIssueScrumJqlJob, params);
						} else {
							// Board is setup for the project
							jobLauncher.run(fetchIssueScrumBoardJob, params);
						}
					}
				}
			}
		} catch (Exception e) {
			log.info("Jira fetch failed for BasicProjectConfigId : {}", params.getString(PROJECT_ID));
			e.printStackTrace();
		}
		return ResponseEntity.ok().body("job started for BasicProjectConfigId : " + basicProjectConfigId);

	}

}
