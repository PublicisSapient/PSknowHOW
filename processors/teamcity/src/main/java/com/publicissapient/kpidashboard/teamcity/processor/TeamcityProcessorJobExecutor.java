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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.executor.ProcessorJobExecutor;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.BuildRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.teamcity.config.TeamcityConfig;
import com.publicissapient.kpidashboard.teamcity.factory.TeamcityClientFactory;
import com.publicissapient.kpidashboard.teamcity.model.TeamcityProcessor;
import com.publicissapient.kpidashboard.teamcity.processor.adapter.TeamcityClient;
import com.publicissapient.kpidashboard.teamcity.repository.TeamcityProcessorRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * ProcessorJobExecutor that fetches Build log information from Teamcity.
 */

@Component
@Slf4j
public class TeamcityProcessorJobExecutor extends ProcessorJobExecutor<TeamcityProcessor> {
	private static final String PROCESSOR_EXECUTION_UID = "processorExecutionUid";
	private static final String PROCESSOR_START_TIME = "processorStartTime";
	private static final String INSTANCE_URL = "instanceUrl";
	private static final String TOTAL_UPDATED_COUNT = "totalUpdatedCount";
	private static final String PROCESSOR_END_TIME = "processorEndTime";
	private static final String EXECUTION_TIME = "executionTime";
	private static final String EXECUTION_STATUS = "executionStatus";
	private static final String TEAMCITY_CLIENT = "teamcityClient";
	@Autowired
	AesEncryptionService aesEncryptionService;
	@Autowired
	private TeamcityProcessorRepository teamcityProcessorRepository;
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
	@Autowired
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;
	private TeamcityClient teamcityClient;
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
		List<Build> savedTotalBuilds = new ArrayList<>();
		Set<String> jobNameSet = new HashSet<>();
		int count = 0;

		MDC.put("TotalSelectedProjectsForProcessing", String.valueOf(projectConfigList.size()));
		for (ProjectBasicConfig proBasicConfig : projectConfigList) {
			List<ProcessorToolConnection> teamcityJobList = processorToolConnectionService
					.findByToolAndBasicProjectConfigId(ProcessorConstants.TEAMCITY, proBasicConfig.getId());
			teamcityJobList.forEach(job -> jobNameSet.add(job.getJobName()));

			for (ProcessorToolConnection teamcityServer : teamcityJobList) {

				teamcityServer.setPassword(decryptPassword(teamcityServer.getPassword()));
				String instanceUrl = teamcityServer.getUrl();
				MDC.put(INSTANCE_URL, instanceUrl);
				ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLog(
						proBasicConfig.getId().toHexString());
				try {
					processorExecutionTraceLog.setExecutionStartedAt(startTime);
					teamcityClient = teamcityClientFactory.getTeamcityClient(TEAMCITY_CLIENT);

					Map<ObjectId, Set<Build>> buildsByJob = teamcityClient.getInstanceJobs(teamcityServer);
					log.info("Fetched jobs at : {}", startTime);

					int updatedJobs = addNewBuilds(savedTotalBuilds, buildsByJob, teamcityServer, processor.getId(),
							proBasicConfig);
					count += updatedJobs;
					log.info("Finished : {}", System.currentTimeMillis());
					processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
					processorExecutionTraceLog.setExecutionSuccess(true);
					processorExecutionTraceLog.setLastEnableAssigneeToggleState(proBasicConfig.isSaveAssigneeDetails());
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

		long endTime = System.currentTimeMillis();

		MDC.put(PROCESSOR_END_TIME, String.valueOf(endTime));
		MDC.put(EXECUTION_TIME, String.valueOf(endTime - startTime));
		MDC.put(EXECUTION_STATUS, String.valueOf(executionStatus));
		log.info("Processor execution finished");
		MDC.clear();
		return executionStatus;
	}

	@Override
	public boolean executeSprint(String sprintId) {
		return false;
	}

	private ProcessorExecutionTraceLog createTraceLog(String basicProjectConfigId) {
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setProcessorName(ProcessorConstants.TEAMCITY);
		processorExecutionTraceLog.setBasicProjectConfigId(basicProjectConfigId);
		Optional<ProcessorExecutionTraceLog> existingTraceLogOptional = processorExecutionTraceLogRepository
				.findByProcessorNameAndBasicProjectConfigId(ProcessorConstants.TEAMCITY, basicProjectConfigId);
		existingTraceLogOptional.ifPresent(
				existingProcessorExecutionTraceLog -> processorExecutionTraceLog.setLastEnableAssigneeToggleState(
						existingProcessorExecutionTraceLog.isLastEnableAssigneeToggleState()));
		return processorExecutionTraceLog;
	}

	/**
	 * Iterates over fetched build jobs and adds new builds to the database.
	 *
	 * @param savedTotalBuilds
	 *            the list of builds total for each projects
	 * @param buildsByJob
	 *            the build by job
	 * @param teamcityServer
	 *            the teamcity server
	 * @param proBasicConfig
	 * @return adds new build
	 */
	private int addNewBuilds(List<Build> savedTotalBuilds, Map<ObjectId, Set<Build>> buildsByJob,
			ProcessorToolConnection teamcityServer, ObjectId processorId, ProjectBasicConfig proBasicConfig) {
		long start = System.currentTimeMillis();
		int count = 0;
		List<Build> buildsToSave = new ArrayList<>();
		// process new builds in the order of their build numbers - this has
		// implication to handling of commits in BuildEventListener
		ArrayList<Build> builds = new ArrayList<>(nullSafe(buildsByJob.get(teamcityServer.getId())));
		builds.sort((Build b1, Build b2) -> Integer.valueOf(b1.getNumber()) - Integer.valueOf(b2.getNumber()));
		for (Build buildSummary : builds) {
			Build buildData = buildRepository.findByProjectToolConfigIdAndNumber(teamcityServer.getId(),
					buildSummary.getNumber());
			if (buildData == null) {
				Build build = teamcityClient.getBuildDetails(buildSummary.getBuildUrl(), teamcityServer.getUrl(),
						teamcityServer, proBasicConfig);
				if (build != null) {
					build.setBuildJob(teamcityServer.getJobName());
					build.setProcessorId(processorId);
					build.setBasicProjectConfigId(teamcityServer.getBasicProjectConfigId());
					build.setProjectToolConfigId(teamcityServer.getId());
					count++;
				}
			} else {
				if (proBasicConfig.isSaveAssigneeDetails() && buildData.getStartedBy() == null
						&& buildSummary.getStartedBy() != null) {
					buildData.setStartedBy(buildSummary.getStartedBy());
					buildsToSave.add(buildData);
				}
			}
		}

		if (CollectionUtils.isNotEmpty(buildsToSave)) {
			savedTotalBuilds.addAll(buildsToSave);
			buildRepository.saveAll(buildsToSave);
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
