package com.publicissapient.kpidashboard.jira.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.tracelog.PSLogData;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ValidateData {

	protected static final String QUERYDATEFORMAT = "yyyy-MM-dd HH:mm";

	@Autowired
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

	public void check(int total, int savedCount, boolean processorFetchingComplete, PSLogData psLogData,
			Map<String, LocalDateTime> lastSavedJiraIssueChangedDateByType, ProjectConfFieldMapping projectConfig,
			ProcessorExecutionTraceLog processorExecutionTraceLog) {

		boolean isAttemptSuccess = isAttemptSuccess(total, savedCount, processorFetchingComplete, psLogData);
		psLogData.setAction(CommonConstant.PROJECT_EXECUTION_STATUS);
		if (!isAttemptSuccess) {
			if(MapUtils.isNotEmpty( lastSavedJiraIssueChangedDateByType)) {
				lastSavedJiraIssueChangedDateByType.clear();
			}
			processorExecutionTraceLog.setLastSuccessfulRun(null);
			psLogData.setProjectExecutionStatus(String.valueOf(isAttemptSuccess));
			log.error("Error in Fetching Issues", kv(CommonConstant.PSLOGDATA, psLogData));
		} else {
			processorExecutionTraceLog
					.setLastSuccessfulRun(DateUtil.dateTimeFormatter(LocalDateTime.now(), QUERYDATEFORMAT));
		}
		saveExecutionTraceLog(processorExecutionTraceLog, lastSavedJiraIssueChangedDateByType, isAttemptSuccess,
				projectConfig.getProjectBasicConfig());
	}

	private boolean isAttemptSuccess(int total, int savedCount, boolean processorFetchingComplete,
			PSLogData psLogData) {
		psLogData.setTotalFetchedIssues(String.valueOf(total));
		psLogData.setTotalSavedIssues(String.valueOf(savedCount));
		return savedCount > 0 && total == savedCount && processorFetchingComplete;
	}

	private void saveExecutionTraceLog(ProcessorExecutionTraceLog processorExecutionTraceLog,
			Map<String, LocalDateTime> lastSavedJiraIssueChangedDateByType, boolean isSuccess,
			ProjectBasicConfig projectBasicConfig) {

		if (MapUtils.isNotEmpty( lastSavedJiraIssueChangedDateByType)) {
			processorExecutionTraceLog.setLastSavedEntryUpdatedDateByType(null);
		} else {
			processorExecutionTraceLog.setLastSavedEntryUpdatedDateByType(lastSavedJiraIssueChangedDateByType);
		}
		processorExecutionTraceLog.setExecutionSuccess(isSuccess);
		processorExecutionTraceLog.setLastEnableAssigneeToggleState(projectBasicConfig.isSaveAssigneeDetails());
		processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
		savingTraceLogToLog(processorExecutionTraceLog);
		processorExecutionTraceLogService.save(processorExecutionTraceLog);
	}

	private void savingTraceLogToLog(ProcessorExecutionTraceLog processorExecutionTraceLog) {
		PSLogData traceLog = new PSLogData();
		traceLog.setAction(CommonConstant.PROJECT_EXECUTION_STATUS);
		traceLog.setExecutionEndedAt(
				DateUtil.convertMillisToDateTime(processorExecutionTraceLog.getExecutionEndedAt()));
		traceLog.setExecutionStartedAt(
				DateUtil.convertMillisToDateTime(processorExecutionTraceLog.getExecutionStartedAt()));
		traceLog.setLastSuccessfulRun(processorExecutionTraceLog.getLastSuccessfulRun());
		traceLog.setProjectExecutionStatus(String.valueOf(processorExecutionTraceLog.isExecutionSuccess()));
		traceLog.setLastEnableAssigneeToggleState(
				String.valueOf(processorExecutionTraceLog.isLastEnableAssigneeToggleState()));
		List<String> logJiraIssueChange = new ArrayList<>();
		if (MapUtils.isNotEmpty(processorExecutionTraceLog.getLastSavedEntryUpdatedDateByType())) {
			processorExecutionTraceLog.getLastSavedEntryUpdatedDateByType()
					.forEach((issue, updateDated) -> logJiraIssueChange
							.add(issue + CommonConstant.ARROW + updateDated.toString() + CommonConstant.NEWLINE));
			traceLog.setLastSavedJiraIssueChangedDateByType(logJiraIssueChange);
		}
		log.info("last execution time of {} for project {} is {}. status is {}",
				processorExecutionTraceLog.getProcessorName(), processorExecutionTraceLog.getBasicProjectConfigId(),
				processorExecutionTraceLog.getExecutionEndedAt(), processorExecutionTraceLog.isExecutionSuccess(),
				kv(CommonConstant.PSLOGDATA, traceLog));
	}

}
