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
package com.publicissapient.kpidashboard.jira.listener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueV2;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.model.CompositeResult;

import lombok.extern.slf4j.Slf4j;

/**
 * @author shunaray
 *
 */
@Component
@Slf4j
public class JiraIssueV2BoardWriterListener implements ItemWriteListener<CompositeResult> {

	@Autowired
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepo;

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

		log.info("Write listner called for scrum board project and saving status in Processor execution Trace log");

		List<ProcessorExecutionTraceLog> processorExecutionToSave = new ArrayList<>();
		List<JiraIssueV2> jiraIssues = compositeResults.getItems().stream().map(CompositeResult::getJiraIssueV2)
				.toList();

		Map<String, Map<String, List<JiraIssueV2>>> projectBoardWiseIssues = jiraIssues.stream()
				.filter(issue -> !issue.getTypeName().equalsIgnoreCase(JiraConstants.EPIC))
				.collect(Collectors.groupingBy(JiraIssueV2::getBasicProjectConfigId,
						Collectors.groupingBy(JiraIssueV2::getBoardId)));

		for (Map.Entry<String, Map<String, List<JiraIssueV2>>> entry : projectBoardWiseIssues.entrySet()) {
			String basicProjectConfigId = entry.getKey();
			Map<String, List<JiraIssueV2>> boardWiseIssues = entry.getValue();
			for (Map.Entry<String, List<JiraIssueV2>> boardData : boardWiseIssues.entrySet()) {
				String boardId = boardData.getKey();
				JiraIssueV2 firstIssue = boardData.getValue().stream()
						.max(Comparator
								.comparing((JiraIssueV2 jiraIssue) -> LocalDateTime.parse(jiraIssue.getChangeDate(),
										DateTimeFormatter.ofPattern(JiraConstants.JIRA_ISSUE_CHANGE_DATE_FORMAT))))
						.orElse(null);
				if (firstIssue != null) {
					Optional<ProcessorExecutionTraceLog> procTraceLog = processorExecutionTraceLogRepo
							.findByProcessorNameAndBasicProjectConfigId(ProcessorConstants.JIRA_V2,
									basicProjectConfigId);
					ProcessorExecutionTraceLog processorExecutionTraceLog;
					processorExecutionTraceLog = procTraceLog.orElseGet(ProcessorExecutionTraceLog::new);
					setTraceLog(processorExecutionTraceLog, basicProjectConfigId, boardId, firstIssue.getChangeDate(),
							processorExecutionToSave);
				}
			}

		}
		if (CollectionUtils.isNotEmpty(processorExecutionToSave)) {
			processorExecutionTraceLogRepo.saveAll(processorExecutionToSave);
		}
	}

	private void setTraceLog(ProcessorExecutionTraceLog processorExecutionTraceLog, String basicProjectConfigId,
			String boardId, String changeDate, List<ProcessorExecutionTraceLog> processorExecutionToSave) {
		final String formattedLastSuccess = DateUtil.dateTimeConverter(changeDate,
				JiraConstants.JIRA_ISSUE_CHANGE_DATE_FORMAT, DateUtil.DATE_TIME_FORMAT);
		processorExecutionTraceLog.setBasicProjectConfigId(basicProjectConfigId);
		Map<String, String> boardWiseLastSyncDate = processorExecutionTraceLog.getBoardWiseLastSyncDate();
		if (boardWiseLastSyncDate == null) {
			boardWiseLastSyncDate = new HashMap<>();
		}
		boardWiseLastSyncDate.put(boardId, formattedLastSuccess);
		processorExecutionTraceLog.setBoardWiseLastSyncDate(boardWiseLastSyncDate);
		processorExecutionTraceLog.setLastSuccessfulRun(formattedLastSuccess);
		processorExecutionTraceLog.setProcessorName(ProcessorConstants.JIRA_V2);
		processorExecutionToSave.add(processorExecutionTraceLog);
	}

	@Override
	public void onWriteError(Exception exception, Chunk<? extends CompositeResult> compositeResult) {
		log.error("Exception occured while writing jira Issue ", exception);
	}
}
