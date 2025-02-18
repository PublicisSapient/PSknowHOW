package com.publicissapient.kpidashboard.jira.service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OngoingExecutionsService {

	@Autowired
	ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

	private final ConcurrentHashMap<String, Boolean> ongoingExecutions = new ConcurrentHashMap<>();

	public boolean isExecutionInProgress(String basicProjectConfigId) {
		return ongoingExecutions.containsKey(basicProjectConfigId);
	}

	public void markExecutionInProgress(String basicProjectConfigId) {
		ongoingExecutions.put(basicProjectConfigId, true);
		setExecutionOngoingForProcessor(ProcessorConstants.JIRA, basicProjectConfigId, true);
	}

	public void markExecutionAsCompleted(String basicProjectConfigId) {
		ongoingExecutions.remove(basicProjectConfigId);
		setExecutionOngoingForProcessor(ProcessorConstants.JIRA, basicProjectConfigId, false);
	}

	/**
	 * Set the executionOngoing flag for a processor
	 *
	 * @param processorName
	 *          Name of Processor
	 * @param basicProjectConfigId
	 *          ProjectId
	 * @param executionOngoing
	 *          Flag is processor execution ongoing
	 */
	public void setExecutionOngoingForProcessor(String processorName, String basicProjectConfigId,
			boolean executionOngoing) {
		Optional<ProcessorExecutionTraceLog> existingTraceLog = processorExecutionTraceLogRepository
				.findByProcessorNameAndBasicProjectConfigIdAndProgressStatsTrue(processorName, basicProjectConfigId);
		ProcessorExecutionTraceLog processorExecutionTraceLog = existingTraceLog.orElseGet(ProcessorExecutionTraceLog::new);
		processorExecutionTraceLog.setBasicProjectConfigId(basicProjectConfigId);
		processorExecutionTraceLog.setExecutionOngoing(executionOngoing);
		processorExecutionTraceLog.setProgressStats(true);
		processorExecutionTraceLog.setProcessorName(processorName);
		if (executionOngoing) {
			processorExecutionTraceLog.setProgressStatusList(new ArrayList<>()); // clear the prev record
			processorExecutionTraceLog.setErrorMessage(null); // Clear the error message
			processorExecutionTraceLog.setFailureLog(null); // Clear the failure log message
			processorExecutionTraceLog.setAdditionalInfo(null); // clearing additional info msg
			processorExecutionTraceLog.setErrorDetailList(new ArrayList<>());
		}
		log.info("ProjectId {} for processor {} executionOngoing to {} ", basicProjectConfigId, processorName,
				executionOngoing);
		processorExecutionTraceLogRepository.save(processorExecutionTraceLog);
	}
}
