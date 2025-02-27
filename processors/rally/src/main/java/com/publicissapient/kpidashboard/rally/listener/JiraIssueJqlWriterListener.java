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
package com.publicissapient.kpidashboard.rally.listener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.rally.config.RallyProcessorConfig;
import com.publicissapient.kpidashboard.rally.constant.RallyConstants;
import com.publicissapient.kpidashboard.rally.model.CompositeResult;
import com.publicissapient.kpidashboard.rally.util.JiraProcessorUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.Chunk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author pankumar8
 */
@Component
@Slf4j
public class JiraIssueJqlWriterListener implements ItemWriteListener<CompositeResult> {
	@Autowired
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepo;
	@Autowired
	private RallyProcessorConfig rallyProcessorConfig;

	@Override
	public void beforeWrite(Chunk<? extends CompositeResult> compositeResult) {
		// in future we can use this method to do something before saving data in db
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.batch.core.ItemWriteListener#afterWrite(java.util.List)
	 */
	@Override
	public void afterWrite(Chunk<? extends CompositeResult> compositeResults) {
		log.info("Saving status in Processor execution Trace log for Scrum Jql project");

		List<ProcessorExecutionTraceLog> processorExecutionToSave = new ArrayList<>();
		List<JiraIssue> jiraIssues = compositeResults.getItems().stream().map(CompositeResult::getJiraIssue).toList();

		Map<String, List<JiraIssue>> projectWiseIssues = jiraIssues.stream()
				.collect(Collectors.groupingBy(JiraIssue::getBasicProjectConfigId));
		// getting step context
		StepContext stepContext = StepSynchronizationManager.getContext();
		for (Map.Entry<String, List<JiraIssue>> entry : projectWiseIssues.entrySet()) {
			processProject(entry, stepContext, processorExecutionToSave);
		}
		if (CollectionUtils.isNotEmpty(processorExecutionToSave)) {
			processorExecutionTraceLogRepo.saveAll(processorExecutionToSave);
		}
	}

	private void processProject(Map.Entry<String, List<JiraIssue>> entry, StepContext stepContext,
			List<ProcessorExecutionTraceLog> processorExecutionToSave) {
		String basicProjectConfigId = entry.getKey();
		List<ProcessorExecutionTraceLog> procTraceLogList = processorExecutionTraceLogRepo
				.findByProcessorNameAndBasicProjectConfigIdIn(ProcessorConstants.JIRA,
						Collections.singletonList(basicProjectConfigId));
		ProcessorExecutionTraceLog progressStatsTraceLog = procTraceLogList.stream()
				.filter(ProcessorExecutionTraceLog::isProgressStats).findFirst().orElse(new ProcessorExecutionTraceLog());
		JiraIssue firstIssue = entry.getValue().stream()
				.sorted(Comparator.comparing((JiraIssue jiraIssue) -> LocalDateTime.parse(jiraIssue.getChangeDate(),
						DateTimeFormatter.ofPattern(RallyConstants.JIRA_ISSUE_CHANGE_DATE_FORMAT))).reversed())
				.findFirst().orElse(null);
		if (firstIssue != null) {
			processTraceLogs(stepContext, processorExecutionToSave, procTraceLogList, basicProjectConfigId, firstIssue,
					progressStatsTraceLog);
		}
	}

	private void processTraceLogs(StepContext stepContext, List<ProcessorExecutionTraceLog> processorExecutionToSave,
			List<ProcessorExecutionTraceLog> procTraceLogList, String basicProjectConfigId, JiraIssue firstIssue,
			ProcessorExecutionTraceLog progressStatsTraceLog) {
		boolean isAnyLastSuccessfulRunPresent = procTraceLogList.stream()
				.anyMatch(traceLog -> traceLog.getLastSuccessfulRun() != null && !traceLog.getLastSuccessfulRun().isEmpty());
		if (CollectionUtils.isNotEmpty(procTraceLogList) && isAnyLastSuccessfulRunPresent) {
			for (ProcessorExecutionTraceLog processorExecutionTraceLog : procTraceLogList) {
				if (processorExecutionTraceLog.isProgressStats()) {
					JiraProcessorUtil.saveChunkProgressInTrace(processorExecutionTraceLog, stepContext);
				}
				setTraceLog(processorExecutionTraceLog, basicProjectConfigId, firstIssue.getChangeDate(),
						processorExecutionToSave);
			}
		} else {
			ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
			processorExecutionTraceLog.setFirstRunDate(
					DateUtil.dateTimeFormatter(LocalDateTime.now().minusMonths(rallyProcessorConfig.getPrevMonthCountToFetchData())
							.minusDays(rallyProcessorConfig.getDaysToReduce()), RallyConstants.QUERYDATEFORMAT));
			setTraceLog(processorExecutionTraceLog, basicProjectConfigId, firstIssue.getChangeDate(),
					processorExecutionToSave);
			progressStatsTraceLog.setLastSuccessfulRun(DateUtil.dateTimeConverter(firstIssue.getChangeDate(),
					RallyConstants.JIRA_ISSUE_CHANGE_DATE_FORMAT, DateUtil.DATE_TIME_FORMAT));
			Optional.ofNullable(JiraProcessorUtil.saveChunkProgressInTrace(progressStatsTraceLog, stepContext))
					.ifPresent(processorExecutionToSave::add);
		}
	}

	private void setTraceLog(ProcessorExecutionTraceLog processorExecutionTraceLog, String basicProjectConfigId,
			String changeDate, List<ProcessorExecutionTraceLog> processorExecutionToSave) {
		processorExecutionTraceLog.setBasicProjectConfigId(basicProjectConfigId);
		processorExecutionTraceLog.setLastSuccessfulRun(
				DateUtil.dateTimeConverter(changeDate, RallyConstants.JIRA_ISSUE_CHANGE_DATE_FORMAT, DateUtil.DATE_TIME_FORMAT));
		processorExecutionTraceLog.setProcessorName(RallyConstants.JIRA);
		processorExecutionToSave.add(processorExecutionTraceLog);
	}

	@Override
	public void onWriteError(Exception exception, Chunk<? extends CompositeResult> compositeResult) {
		log.error("Exception occured while writing jira Issue for Scrum jql project ", exception);
	}
}
