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

package com.publicissapient.kpidashboard.teamcity.processor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import org.bson.types.ObjectId;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.executor.ProcessorJobExecutor;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.BuildRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.teamcity.config.TeamcityConfig;
import com.publicissapient.kpidashboard.teamcity.factory.TeamcityClientFactory;
import com.publicissapient.kpidashboard.teamcity.model.TeamcityJob;
import com.publicissapient.kpidashboard.teamcity.model.TeamcityProcessor;
import com.publicissapient.kpidashboard.teamcity.processor.adapter.TeamcityClient;
import com.publicissapient.kpidashboard.teamcity.repository.TeamcityJobRepository;
import com.publicissapient.kpidashboard.teamcity.repository.TeamcityProcessorRepository;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

/**
 * ProcessorJobExecutor that fetches Build log information from Teamcity.
 */

@Component
@Slf4j
public class TeamcityProcessorJobExecutor extends ProcessorJobExecutor<TeamcityProcessor> {

	private static final String JOBNAME = "jobName";
	private static final String PROCESSOR_EXECUTION_UID = "processorExecutionUid";
	private static final String PROCESSOR_START_TIME = "processorStartTime";
	private static final String INSTANCE_URL = "instanceUrl";
	private static final String TOTAL_UPDATED_COUNT = "totalUpdatedCount";
	private static final String PROCESSOR_END_TIME = "processorEndTime";
	private static final String EXECUTION_TIME = "executionTime";
	private static final String EXECUTION_STATUS = "executionStatus";
	private static final String TEAMCITY_CLIENT = "teamcityClient";

	@Autowired
	private TeamcityProcessorRepository teamcityProcessorRepository;

	@Autowired

	private TeamcityJobRepository teamcityJobRepository;
	@Autowired
	private BuildRepository buildRepository;

	@Autowired
	private TeamcityConfig teamcityConfig;

	@Autowired
	private ProjectToolConfigRepository projectToolConfigRepository;

	@Autowired
	private TeamcityClientFactory teamcityClientFactory;

	@Autowired
	private ProjectBasicConfigRepository projectConfigRepository;

	private TeamcityClient teamcityClient;

	@Autowired
	AesEncryptionService aesEncryptionService;
	
	@Autowired
	private ProcessorToolConnectionService processorToolConnectionService;

	@Autowired
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

	/**
	 * Provides Teamcity TaskScheduler.
	 * 
	 * @param taskScheduler
	 *            the task scheduler
	 */
	@Autowired
	public TeamcityProcessorJobExecutor(TaskScheduler taskScheduler) {
		super(taskScheduler, ProcessorConstants.TEAMCITY);

	}

	/**
	 * Provides Processor.
	 * 
	 * @return the TeamcityProcessor
	 */
	@Override
	public TeamcityProcessor getProcessor() {
		return TeamcityProcessor.buildProcessor();
	}

	/**
	 * Provides Processor Repository.
	 * 
	 * @return the ProcessorRepository
	 *
	 */
	@Override
	public ProcessorRepository<TeamcityProcessor> getProcessorRepository() {
		return teamcityProcessorRepository;
	}

	/**
	 * Provides cron expression.
	 * 
	 * @return the cron expression
	 */
	@Override
	public String getCron() {
		return teamcityConfig.getCron();
	}

	/**
	 * Processes Teamcity build data.
	 * 
	 * @param processor
	 *            the teamcity processor instance
	 */
	@Override
	public boolean execute(TeamcityProcessor processor) {
		long startTime = System.currentTimeMillis();
		boolean executionStatus = true;
		String uid = UUID.randomUUID().toString();
		MDC.put(PROCESSOR_EXECUTION_UID, uid);
		MDC.put(PROCESSOR_START_TIME, String.valueOf(startTime));

		List<ProjectBasicConfig> projectConfigList = getSelectedProjects();
		clearSelectedBasicProjectConfigIds();

		Set<ObjectId> udId = new HashSet<>();
		udId.add(processor.getId());
		List<TeamcityJob> existingJobs = teamcityJobRepository.findByProcessorIdIn(udId);
		List<TeamcityJob> activeJobs = new ArrayList<>();

		Set<String> jobNameSet = new HashSet<>();
		int count = 0;

		MDC.put("TotalSelectedProjectsForProcessing", String.valueOf(projectConfigList.size()));
		for (ProjectBasicConfig proBasicConfig : projectConfigList) {
			List<ProcessorToolConnection> teamcityJobList = processorToolConnectionService
					.findByToolAndBasicProjectConfigId(ProcessorConstants.TEAMCITY, proBasicConfig.getId());
			teamcityJobList.forEach(job -> jobNameSet.add(job.getJobName()));

			for (ProcessorToolConnection teamcityServer : teamcityJobList) {

				// String instanceUrl : collector.getBuildServers()
				teamcityServer.setPassword(decryptPassword(teamcityServer.getPassword()));
				String instanceUrl = teamcityServer.getUrl();
				MDC.put(INSTANCE_URL, instanceUrl);
				ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLog(
						proBasicConfig.getId().toHexString());
				try {
					processorExecutionTraceLog.setExecutionStartedAt(startTime);
					teamcityClient = teamcityClientFactory.getTeamcityClient(TEAMCITY_CLIENT);

					Map<TeamcityJob, Set<Build>> buildsByJob = teamcityClient.getInstanceJobs(teamcityServer);
					log.info("Fetched jobs at : {}", startTime);
					activeJobs.addAll(buildsByJob.keySet());
					addNewJobs(buildsByJob.keySet(), existingJobs, processor, jobNameSet);

					List<TeamcityJob> processorItems = teamcityJobRepository.findByProcessorId(processor.getId());
					List<TeamcityJob> toBeEnabledJob = new ArrayList<>();

					addTeamcityJob(teamcityServer, processorItems, toBeEnabledJob);

					if (!CollectionUtils.isEmpty(toBeEnabledJob)) {
						teamcityJobRepository.saveAll(toBeEnabledJob);
					}

					int updatedJobs = addNewBuilds(findActiveJobs(processor, instanceUrl), buildsByJob, teamcityServer, proBasicConfig);
					count += updatedJobs;
					log.info("Finished : {}", System.currentTimeMillis());
					processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
					processorExecutionTraceLog.setExecutionSuccess(true);
					processorExecutionTraceLogService.save(processorExecutionTraceLog);

				} catch (RestClientException exception) {
					executionStatus = false;
					processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
					processorExecutionTraceLog.setExecutionSuccess(executionStatus);
					processorExecutionTraceLogService.save(processorExecutionTraceLog);
					log.error(String.format("Error getting jobs for: %s", instanceUrl), exception);
				}

			}
		}
		MDC.put(TOTAL_UPDATED_COUNT, String.valueOf(count));

		removeDiscardedJobs(activeJobs, existingJobs, processor.getId());

		long endTime = System.currentTimeMillis();

		MDC.put(PROCESSOR_END_TIME, String.valueOf(endTime));
		MDC.put(EXECUTION_TIME, String.valueOf(endTime - startTime));
		MDC.put(EXECUTION_STATUS, String.valueOf(executionStatus));
		log.info("Processor execution finished");
		MDC.clear();
		return executionStatus;
	}

	private void addTeamcityJob(ProcessorToolConnection teamcityServer, List<TeamcityJob> processorItems,
			List<TeamcityJob> toBeEnabledJob) {
		for (TeamcityJob jenJob : processorItems) {
			if (teamcityServer.getUrl().equals(jenJob.getToolDetailsMap().get(INSTANCE_URL))
					&& teamcityServer.getJobName().equals(jenJob.getToolDetailsMap().get(JOBNAME))) {
				TeamcityJob tmpTeamcityJob = jenJob;
				tmpTeamcityJob.setActive(true);
				tmpTeamcityJob.setVersion((short) 2);
				tmpTeamcityJob.setToolConfigId(teamcityServer.getId());
				toBeEnabledJob.add(tmpTeamcityJob);
			}
		}
	}

	/**
	 * Delete orphaned job processor items.
	 *
	 * @param activeJobs
	 *            the active Teamcity jobs
	 * @param existingJobs
	 *            the existing Teamcity jobs
	 * 
	 * @param processorId
	 *            the Teamcity processor id
	 */
	private void removeDiscardedJobs(List<TeamcityJob> activeJobs, List<TeamcityJob> existingJobs,
			ObjectId processorId) {

		List<TeamcityJob> deleteJobList = new ArrayList<>();
		for (TeamcityJob job : existingJobs) {
			if (job.getVersion() != null && job.getVersion() == 2) {
				continue;
			}

			if (!job.getProcessorId().equals(processorId)) {
				deleteJobList.add(job);
			}

			if (!activeJobs.contains(job)) {
				deleteJobList.add(job);
			}
		}
		if (!CollectionUtils.isEmpty(deleteJobList)) {
			teamcityJobRepository.deleteAll(deleteJobList);
		}
	}

	private ProcessorExecutionTraceLog createTraceLog(String basicProjectConfigId) {
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setProcessorName(ProcessorConstants.TEAMCITY);
		processorExecutionTraceLog.setBasicProjectConfigId(basicProjectConfigId);
		return processorExecutionTraceLog;
	}

	/**
	 * Iterates over the enabled build jobs and adds new builds to the database.
	 *
	 * @param enabledJobs
	 *            the list of enabled Teamcity job
	 * @param buildsByJob
	 *            the build by job
	 * @param teamcityServer
	 *            the teamcity server
	 * @return adds new build
	 */
	private int addNewBuilds(List<TeamcityJob> enabledJobs, Map<TeamcityJob, Set<Build>> buildsByJob,
			ProcessorToolConnection teamcityServer,ProjectBasicConfig proBasicConfig) {
		long start = System.currentTimeMillis();
		int count = 0;
		for (TeamcityJob job : enabledJobs) {
			// process new builds in the order of their build numbers - this has
			// implication to handling of commits in BuildEventListener
			ArrayList<Build> builds = new ArrayList<>(nullSafe(buildsByJob.get(job)));
			builds.sort((Build b1, Build b2) -> Integer.valueOf(b1.getNumber()) - Integer.valueOf(b2.getNumber()));
			for (Build buildSummary : builds) {
				if (isNewBuild(job, buildSummary)) {
					Build build = teamcityClient.getBuildDetails(buildSummary.getBuildUrl(), job.getInstanceUrl(),
							teamcityServer, proBasicConfig);
					if (build != null) {
						build.setProcessorItemId(job.getId());
						build.setBuildJob(job.getJobName());
						buildRepository.save(build);
						count++;
					}
				}
			}
		}
		log.info("New builds", start, count);
		return count;
	}

	/**
	 * Checks if builds is null or not.
	 * 
	 * @param builds
	 *            the build list
	 * @return builds if not null
	 */
	private Set<Build> nullSafe(Set<Build> builds) {
		return builds == null ? new HashSet<>() : builds;
	}

	/**
	 * Adds new TeamcityJobs to the database as disabled jobs.
	 * 
	 * @param jobs
	 *            the Teamcity jobs name
	 * @param existingJobs
	 *            the existing Teamcity jobs
	 * @param processor
	 *            the Teamcity processor
	 * @param jobNameSet
	 *            the list of job name
	 */
	private void addNewJobs(Set<TeamcityJob> jobs, List<TeamcityJob> existingJobs, TeamcityProcessor processor,
			Set<String> jobNameSet) {
		long start = System.currentTimeMillis();
		int count = 0;

		List<TeamcityJob> newJobs = new ArrayList<>();
		for (TeamcityJob job : jobs) {

			TeamcityJob existing = null;
			if (!CollectionUtils.isEmpty(existingJobs) && existingJobs.contains(job)) {
				existing = existingJobs.get(existingJobs.indexOf(job));
			}

			if (existing == null) {
				job.setProcessorId(processor.getId());
				job.setActive(true);
				job.setDesc(job.getJobName());
				newJobs.add(job);
				count++;
			} else if (shouldActivateJob(jobNameSet, job)) {
				existing.setActive(true);
				teamcityJobRepository.save(existing);
			}
		}
		// save all in one shot
		if (!CollectionUtils.isEmpty(newJobs)) {
			teamcityJobRepository.saveAll(newJobs);
		}
		log.info("New jobs", start, count);
	}

	/**
	 * Enables Teamcity Job.
	 * 
	 * @param jobNameSet
	 *            the list of Teamcity jobs
	 * @param job
	 *            the Teamcity job
	 * @return boolean
	 */
	private boolean shouldActivateJob(Set<String> jobNameSet, TeamcityJob job) {
		return jobNameSet.contains(job.getJobName());
	}

	/**
	 * Finds enabled Jobs.
	 * 
	 * @param processor
	 *            teamcity processor
	 * @param instanceUrl
	 *            teamcity build server url
	 * @return List<TeamcityJob>
	 */
	private List<TeamcityJob> findActiveJobs(TeamcityProcessor processor, String instanceUrl) {
		return teamcityJobRepository.findEnabledJobs(processor.getId(), instanceUrl);
	}

	/**
	 * Provides Existing Jobs.
	 * 
	 * @param processor
	 *            the Teamcity processor
	 * @param job
	 *            the Teamcity job
	 * @return the TeamcityJob
	 */
	@SuppressWarnings("unused")
	private TeamcityJob getExistingJob(TeamcityProcessor processor, TeamcityJob job) {
		return teamcityJobRepository.findJob(processor.getId(), job.getInstanceUrl(), job.getJobName());
	}

	/**
	 * Checks whether the build is new.
	 * 
	 * @param job
	 *            the Teamcity jobs
	 * @param build
	 *            the Teamcity build
	 * @return boolean
	 */
	private boolean isNewBuild(TeamcityJob job, Build build) {
		return buildRepository.findByProcessorItemIdAndNumber(job.getId(), build.getNumber()) == null;
	}

	private String decryptPassword(String encryptedValue) {
		return aesEncryptionService.decrypt(encryptedValue, teamcityConfig.getAesEncryptionKey());
	}

	private List<ProjectBasicConfig> getSelectedProjects() {
		List<ProjectBasicConfig> allProjects = projectConfigRepository.findAll();
		MDC.put("TotalConfiguredProject", String.valueOf(CollectionUtils.emptyIfNull(allProjects).size()));

		List<String> selectedProjectsBasicIds = getProjectsBasicConfigIds();
		if (CollectionUtils.isEmpty(selectedProjectsBasicIds)) {
			return allProjects;
		}
		return CollectionUtils.emptyIfNull(allProjects).stream().filter(
				projectBasicConfig -> selectedProjectsBasicIds.contains(projectBasicConfig.getId().toHexString()))
				.collect(Collectors.toList());
	}

	private void clearSelectedBasicProjectConfigIds() {
		setProjectsBasicConfigIds(null);
	}
}
