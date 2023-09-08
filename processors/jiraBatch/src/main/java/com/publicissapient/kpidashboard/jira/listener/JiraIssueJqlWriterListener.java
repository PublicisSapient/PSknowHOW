package com.publicissapient.kpidashboard.jira.listener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.model.CompositeResult;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JiraIssueJqlWriterListener implements ItemWriteListener<CompositeResult> {

	@Autowired
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepo;

	@Override
	public void beforeWrite(List<? extends CompositeResult> compositeResult) {

	}

	@Override
	public void afterWrite(List<? extends CompositeResult> compositeResults) {
		log.info("Saving status in Processor execution Trace log");

		List<ProcessorExecutionTraceLog> processorExecutionToSave = new ArrayList<>();
		List<JiraIssue> jiraIssues = compositeResults.stream().map(CompositeResult::getJiraIssue)
				.collect(Collectors.toList());

		Map<String, List<JiraIssue>> projectWiseIssues = jiraIssues.stream()
				.collect(Collectors.groupingBy(JiraIssue::getBasicProjectConfigId));

		for (Map.Entry<String, List<JiraIssue>> entry : projectWiseIssues.entrySet()) {
			String basicProjectConfigId = entry.getKey();
			JiraIssue firstIssue = entry.getValue().stream()
					.sorted(Comparator
							.comparing((JiraIssue jiraIssue) -> LocalDateTime.parse(jiraIssue.getChangeDate(),
									DateTimeFormatter.ofPattern(JiraConstants.JIRA_ISSUE_CHANGE_DATE_FORMAT)))
							.reversed())
					.findFirst().orElse(null);
			if (firstIssue != null) {
				Optional<ProcessorExecutionTraceLog> procTraceLog = processorExecutionTraceLogRepo
						.findByProcessorNameAndBasicProjectConfigId(ProcessorConstants.JIRA, basicProjectConfigId);
				if (procTraceLog.isPresent()) {
					ProcessorExecutionTraceLog processorExecutionTraceLog = procTraceLog.get();
					setTraceLog(processorExecutionTraceLog, basicProjectConfigId, firstIssue.getChangeDate(),
							processorExecutionToSave);
				} else {
					ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
					setTraceLog(processorExecutionTraceLog, basicProjectConfigId, firstIssue.getChangeDate(),
							processorExecutionToSave);
				}
			}

		}
		if (CollectionUtils.isNotEmpty(processorExecutionToSave)) {
			processorExecutionTraceLogRepo.saveAll(processorExecutionToSave);
		}
	}

	private void setTraceLog(ProcessorExecutionTraceLog processorExecutionTraceLog, String basicProjectConfigId,
			String changeDate, List<ProcessorExecutionTraceLog> processorExecutionToSave) {
		processorExecutionTraceLog.setBasicProjectConfigId(basicProjectConfigId);
		processorExecutionTraceLog.setExecutionSuccess(true);
		processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
		processorExecutionTraceLog.setLastSuccessfulRun(DateUtil.dateTimeConverter(changeDate,
				JiraConstants.JIRA_ISSUE_CHANGE_DATE_FORMAT, DateUtil.DATE_TIME_FORMAT));
		processorExecutionTraceLog.setProcessorName(JiraConstants.JIRA);
		processorExecutionToSave.add(processorExecutionTraceLog);
	}

	@Override
	public void onWriteError(Exception exception, List<? extends CompositeResult> compositeResult) {
		log.error("Exception occured while writing jira Issue ", exception);
	}
}
