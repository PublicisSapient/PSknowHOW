/*
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.publicissapient.kpidashboard.jira.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.ProgressStatus;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author shunaray
 */
@Component
@Slf4j
@StepScope
public class JobStepProgressListener implements StepExecutionListener {

	@Autowired
	ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

	@Value("#{jobParameters['projectId']}")
	private String projectId;

	/**
	 * (non-Javadoc)
	 *
	 * @param stepExecution
	 *          instance of {@link StepExecution}.
	 */
	@Override
	public void beforeStep(StepExecution stepExecution) {
		// in the future, we can use this method to do something before saving data in
		// db
	}

	/**
	 * (non-Javadoc)
	 *
	 * @param stepExecution
	 *          instance of {@link StepExecution}.
	 * @return null
	 */
	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		String stepName = stepExecution.getStepName();
		BatchStatus status = stepExecution.getStatus();
		ProgressStatus progressStatus = new ProgressStatus();
		progressStatus.setStepName(stepName);
		progressStatus.setStatus(status.toString());
		progressStatus.setEndTime(System.currentTimeMillis());
		log.info("Step {} done with status {}", stepName, status);
		saveProgressStatusInTraceLog(ProcessorConstants.JIRA, projectId, progressStatus);
		return null;
	}

	/**
	 * Save the progress status of a processor in the trace log
	 *
	 * @param processorName
	 *          projectId
	 * @param basicProjectConfigId
	 *          Name of the processor
	 * @param progressStatus
	 *          Progress status of the processor
	 */
	public void saveProgressStatusInTraceLog(String processorName, String basicProjectConfigId,
			ProgressStatus progressStatus) {
		Optional<ProcessorExecutionTraceLog> existingTraceLog = processorExecutionTraceLogRepository
				.findByProcessorNameAndBasicProjectConfigIdAndProgressStatsTrue(processorName, basicProjectConfigId);
		ProcessorExecutionTraceLog processorExecutionTraceLog = existingTraceLog.orElseGet(ProcessorExecutionTraceLog::new);

		processorExecutionTraceLog.setBasicProjectConfigId(basicProjectConfigId);
		processorExecutionTraceLog.setProcessorName(processorName);
		processorExecutionTraceLog.setProgressStats(true);
		List<ProgressStatus> progressStatusList = Optional.ofNullable(processorExecutionTraceLog.getProgressStatusList())
				.orElseGet(ArrayList::new);
		progressStatusList.add(progressStatus);
		processorExecutionTraceLog.setExecutionOngoing(true);
		processorExecutionTraceLog.setProgressStatusList(progressStatusList);
		log.info("Saving the progress of {} processor of step {} for projectId {} ", ProcessorConstants.JIRA,
				progressStatus.getStepName(), basicProjectConfigId);
		processorExecutionTraceLogRepository.save(processorExecutionTraceLog);
	}
}
