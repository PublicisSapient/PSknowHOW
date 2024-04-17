/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicissapient.kpidashboard.common.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.zephyr.ProgressStatus;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anisingh4
 */
@Service
@Slf4j
public class ProcessorExecutionTraceLogServiceImpl implements ProcessorExecutionTraceLogService {

	@Autowired
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

	@Override
	public void save(ProcessorExecutionTraceLog processorExecutionTracelog) {
		log.info(
				"last execution time of {} for project {} is {}. status is {} and lastSuccessfulRun is {} and LastEnableAssigneeToggleState is {} ",
				processorExecutionTracelog.getProcessorName(), processorExecutionTracelog.getBasicProjectConfigId(),
				processorExecutionTracelog.getExecutionEndedAt(), processorExecutionTracelog.isExecutionSuccess(),
				processorExecutionTracelog.getLastSuccessfulRun(),
				processorExecutionTracelog.isLastEnableAssigneeToggleState());

		Optional<ProcessorExecutionTraceLog> existingTraceLogOptional = processorExecutionTraceLogRepository
				.findByProcessorNameAndBasicProjectConfigId(processorExecutionTracelog.getProcessorName(),
						processorExecutionTracelog.getBasicProjectConfigId());
		existingTraceLogOptional.ifPresent(existingProcessorExecutionTraceLog -> {
			processorExecutionTracelog.setId(existingProcessorExecutionTraceLog.getId());
			if (MapUtils.isNotEmpty(existingProcessorExecutionTraceLog.getLastSavedEntryUpdatedDateByType())
					&& MapUtils.isEmpty(processorExecutionTracelog.getLastSavedEntryUpdatedDateByType())) {
				processorExecutionTracelog.setLastSavedEntryUpdatedDateByType(
						existingProcessorExecutionTraceLog.getLastSavedEntryUpdatedDateByType());
			}

		});
		processorExecutionTraceLogRepository.save(processorExecutionTracelog);
	}

	@Override
	public List<ProcessorExecutionTraceLog> getTraceLogs() {
		return processorExecutionTraceLogRepository.findAll();
	}

	@Override
	public List<ProcessorExecutionTraceLog> getTraceLogs(String processorName, String basicProjectConfigId) {
		List<ProcessorExecutionTraceLog> traceLogs = getTraceLogs();
		List<ProcessorExecutionTraceLog> resultTraceLogs = new ArrayList<>();

		if (StringUtils.isEmpty(processorName) && StringUtils.isEmpty(basicProjectConfigId)) {
			resultTraceLogs.addAll(traceLogs);
		} else if (StringUtils.isNotEmpty(processorName) && StringUtils.isEmpty(basicProjectConfigId)) {
			List<ProcessorExecutionTraceLog> traceLogsByProcessorName = traceLogs.stream()
					.filter(traceLog -> processorName.equalsIgnoreCase(traceLog.getProcessorName()))
					.collect(Collectors.toList());
			resultTraceLogs.addAll(traceLogsByProcessorName);
		} else if (StringUtils.isEmpty(processorName) && StringUtils.isNotEmpty(basicProjectConfigId)) {
			List<ProcessorExecutionTraceLog> traceLogsByProject = traceLogs.stream()
					.filter(traceLog -> basicProjectConfigId.equalsIgnoreCase(traceLog.getBasicProjectConfigId()))
					.collect(Collectors.toList());
			resultTraceLogs.addAll(traceLogsByProject);
		} else {
			List<ProcessorExecutionTraceLog> traceLogsByProcessorAndProject = traceLogs.stream()
					.filter(traceLog -> processorName.equalsIgnoreCase(traceLog.getProcessorName()))
					.filter(traceLog -> basicProjectConfigId.equalsIgnoreCase(traceLog.getBasicProjectConfigId()))
					.collect(Collectors.toList());
			resultTraceLogs.addAll(traceLogsByProcessorAndProject);

		}

		return resultTraceLogs.stream()
				.sorted(Comparator.comparing(ProcessorExecutionTraceLog::getExecutionEndedAt).reversed())
				.collect(Collectors.toList());
	}

	/**
	 * Save the progress status of a processor in the trace log
	 * 
	 * @param processorName
	 *            projectId
	 * @param basicProjectConfigId
	 *            Name of the processor
	 * @param progressStatus
	 *            Progress status of the processor
	 */
	@Override
	public void saveProgressStatusInTraceLog(String processorName, String basicProjectConfigId,
			ProgressStatus progressStatus) {
		Optional<ProcessorExecutionTraceLog> existingTraceLog = processorExecutionTraceLogRepository
				.findByProcessorNameAndBasicProjectConfigId(processorName, basicProjectConfigId);
		log.info("Saving the progress of {} processor of step {} for projectId {} ", ProcessorConstants.JIRA,
				progressStatus.getStepName(), basicProjectConfigId);
		ProcessorExecutionTraceLog processorExecutionTraceLog = existingTraceLog
				.orElseGet(ProcessorExecutionTraceLog::new);

		processorExecutionTraceLog.setBasicProjectConfigId(basicProjectConfigId);
		processorExecutionTraceLog.setProcessorName(processorName);
		List<ProgressStatus> progressStatusList = Optional
				.ofNullable(processorExecutionTraceLog.getProgressStatusList()).orElseGet(ArrayList::new);
		progressStatusList.add(progressStatus);
		processorExecutionTraceLog.setExecutionOngoing(true);
		processorExecutionTraceLog.setProgressStatusList(progressStatusList);
		processorExecutionTraceLogRepository.save(processorExecutionTraceLog);
	}

	/**
	 * Set the executionOngoing flag for a processor
	 * 
	 * @param processorName
	 *            Name of Processor
	 * @param basicProjectConfigId
	 *            ProjectId
	 * @param executionOngoing
	 *            Flag is processor execution ongoing
	 */

	@Override
	public void setExecutionOngoingForProcessor(String processorName, String basicProjectConfigId,
			boolean executionOngoing) {
		Optional<ProcessorExecutionTraceLog> existingTraceLog = processorExecutionTraceLogRepository
				.findByProcessorNameAndBasicProjectConfigId(processorName, basicProjectConfigId);
		ProcessorExecutionTraceLog processorExecutionTraceLog = existingTraceLog
				.orElseGet(ProcessorExecutionTraceLog::new);
		processorExecutionTraceLog.setBasicProjectConfigId(basicProjectConfigId);
		processorExecutionTraceLog.setExecutionOngoing(executionOngoing);
		processorExecutionTraceLog.setProcessorName(processorName);
		// If executionOngoing is true, clean progressStatusList
		if (executionOngoing) {
			processorExecutionTraceLog.setProgressStatusList(new ArrayList<>());
		}
		log.info("ProjectId {} for processor {} executionOngoing to {} ", basicProjectConfigId, executionOngoing,
				processorName);
		processorExecutionTraceLogRepository.save(processorExecutionTraceLog);
	}
}
