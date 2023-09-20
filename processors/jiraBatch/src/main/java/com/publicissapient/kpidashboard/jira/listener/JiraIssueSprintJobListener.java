package com.publicissapient.kpidashboard.jira.listener;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.SprintTraceLog;
import com.publicissapient.kpidashboard.common.repository.application.SprintTraceLogRepository;
import com.publicissapient.kpidashboard.jira.cache.JiraProcessorCacheEvictor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@JobScope
public class JiraIssueSprintJobListener extends JobExecutionListenerSupport {

	@Autowired
	SprintTraceLogRepository sprintTraceLogRepository;

	@Autowired
	JiraProcessorCacheEvictor processorCacheEvictor;

	private String sprintId;

	@Autowired
	public JiraIssueSprintJobListener(@Value("#{jobParameters['sprintId']}") String sprintId) {
		this.sprintId = sprintId;
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		long endTime = System.currentTimeMillis();
		// saving the execution details
		SprintTraceLog fetchDetails = sprintTraceLogRepository.findBySprintId(sprintId);
		fetchDetails.setLastSyncDateTime(endTime);
		if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
			fetchDetails.setErrorInFetch(false);
			fetchDetails.setFetchSuccessful(true);
			// clearing cache
			processorCacheEvictor.evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.JIRA_KPI_CACHE);

		} else {
			fetchDetails.setErrorInFetch(true);
			fetchDetails.setFetchSuccessful(false);
		}
		sprintTraceLogRepository.save(fetchDetails);

	}
}
