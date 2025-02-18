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

import java.io.IOException;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.SprintTraceLog;
import com.publicissapient.kpidashboard.common.repository.application.SprintTraceLogRepository;
import com.publicissapient.kpidashboard.jira.cache.JiraProcessorCacheEvictor;
import com.publicissapient.kpidashboard.jira.service.JiraClientService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@JobScope
public class JiraIssueSprintJobListener implements JobExecutionListener {

	@Autowired
	SprintTraceLogRepository sprintTraceLogRepository;

	@Autowired
	JiraProcessorCacheEvictor processorCacheEvictor;

	@Autowired
	JiraClientService jiraClientService;

	@Value("#{jobParameters['sprintId']}")
	private String sprintId;

	@Override
	public void beforeJob(JobExecution jobExecution) {
		// in future we can use this method to do something before saving data in db
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.batch.core.listener.JobExecutionListenerSupport#afterJob(
	 * org.springframework.batch.core.JobExecution)
	 */
	@Override
	public void afterJob(JobExecution jobExecution) {
		log.info("****** Creating Sprint trace log ********");
		long endTime = System.currentTimeMillis();
		// saving the execution details

		SprintTraceLog sprintTrace = sprintTraceLogRepository.findFirstBySprintId(sprintId);
		sprintTrace.setLastSyncDateTime(endTime);
		if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
			sprintTrace.setErrorInFetch(false);
			sprintTrace.setFetchSuccessful(true);
			// clearing cache
			processorCacheEvictor.evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.JIRA_KPI_CACHE);
			processorCacheEvictor.evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.CACHE_PROJECT_TOOL_CONFIG);
			processorCacheEvictor.evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.CACHE_PROJECT_KPI_DATA);

		} else {
			sprintTrace.setErrorInFetch(true);
			sprintTrace.setFetchSuccessful(false);
		}
		log.info("Saving sprint Trace Log for sprintId: {}", sprintId);
		sprintTraceLogRepository.save(sprintTrace);
		if (jiraClientService.isContainRestClient(sprintId)) {
			try {
				jiraClientService.getRestClientMap(sprintId).close();
			} catch (IOException e) {
				throw new RuntimeException("Failed to close rest client", e); // NOSONAR
			}
			jiraClientService.removeRestClientMapClientForKey(sprintId);
			jiraClientService.removeKerberosClientMapClientForKey(sprintId);
		}
	}
}
