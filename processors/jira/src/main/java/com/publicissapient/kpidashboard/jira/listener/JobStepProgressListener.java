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

import java.time.LocalDateTime;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.zephyr.ProgressStatus;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@StepScope
public class JobStepProgressListener implements StepExecutionListener {

	@Autowired
	ProcessorExecutionTraceLogService processorExecutionTraceLogService;

	@Value("#{jobParameters['projectId']}")
	private String projectId;

	/**
	 * (non-Javadoc)
	 * 
	 * @param stepExecution
	 *            instance of {@link StepExecution}.
	 */
	@Override
	public void beforeStep(StepExecution stepExecution) {
		// in future we can use this method to do something before saving data in db
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @param stepExecution
	 *            instance of {@link StepExecution}.
	 * @return null
	 */
	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		String stepName = stepExecution.getStepName();
		BatchStatus status = stepExecution.getStatus();
		LocalDateTime startTime = stepExecution.getStartTime();
		log.info("Step {} done with status {}", stepName, status);
		ProgressStatus progressStatus = new ProgressStatus();
		progressStatus.setStepName(stepName);
		progressStatus.setStatus(status.toString());
		progressStatus.setStartTime(String.valueOf(startTime));

		processorExecutionTraceLogService.saveProgressStatusInTraceLog(ProcessorConstants.JIRA, projectId,
				progressStatus);

		return null;
	}
}