package com.publicissapient.kpidashboard.jira.service;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;

@Service
public class OngoingExecutionsService {

	@Autowired
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

	private final ConcurrentHashMap<String, Boolean> ongoingExecutions = new ConcurrentHashMap<>();

	public boolean isExecutionInProgress(String basicProjectConfigId) {
		return ongoingExecutions.containsKey(basicProjectConfigId);
	}

	public void markExecutionInProgress(String basicProjectConfigId) {
		ongoingExecutions.put(basicProjectConfigId, true);
		processorExecutionTraceLogService.setExecutionOngoingForProcessor(ProcessorConstants.JIRA, basicProjectConfigId,
				true);
	}

	public void markExecutionAsCompleted(String basicProjectConfigId) {
		ongoingExecutions.remove(basicProjectConfigId);
		processorExecutionTraceLogService.setExecutionOngoingForProcessor(ProcessorConstants.JIRA, basicProjectConfigId,
				false);
	}
}
