package com.publicissapient.kpidashboard.jira.service;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class OngoingExecutionsService {

	private final ConcurrentHashMap<String, Boolean> ongoingExecutions = new ConcurrentHashMap<>();

	public boolean isExecutionInProgress(String basicProjectConfigId) {
		return ongoingExecutions.containsKey(basicProjectConfigId);
	}

	public void markExecutionInProgress(String basicProjectConfigId) {
		ongoingExecutions.put(basicProjectConfigId, true);
	}

	public void markExecutionAsCompleted(String basicProjectConfigId) {
		ongoingExecutions.remove(basicProjectConfigId);
	}
}
