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
package com.publicissapient.kpidashboard.jira.reader;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import com.publicissapient.kpidashboard.jira.aspect.TrackExecutionTime;
import com.publicissapient.kpidashboard.jira.client.JiraClient;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.helper.ReaderRetryHelper;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.model.ReadData;
import com.publicissapient.kpidashboard.jira.service.JiraCommonService;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.util.StringUtils;

/**
 * @author pankumar8
 */
@Slf4j
@Component
@StepScope
public class IssueJqlReader implements ItemReader<ReadData> {

	@Autowired
	FetchProjectConfiguration fetchProjectConfiguration;

	@Autowired
	JiraClient jiraClient;

	@Autowired
	JiraCommonService jiraCommonService;

	@Autowired
	JiraProcessorConfig jiraProcessorConfig;
	int pageSize = 50;
	int pageNumber = 0;
	List<Issue> issues = new ArrayList<>();
	Map<String, String> projectWiseDeltaDate;
	int issueSize = 0;
	Boolean fetchLastIssue = false;
	@Autowired
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepo;
	private Iterator<Issue> issueIterator;
	private ProjectConfFieldMapping projectConfFieldMapping;
	private String projectId;
	private ReaderRetryHelper retryHelper;

	@Autowired
	public IssueJqlReader(@Value("#{jobParameters['projectId']}") String projectId) {
		this.projectId = projectId;
		this.retryHelper = new ReaderRetryHelper();
	}

	public void initializeReader(String projectId) {
		log.info("**** Jira Issue fetch started * * *");
		pageSize = jiraProcessorConfig.getPageSize();
		projectConfFieldMapping = fetchProjectConfiguration.fetchConfiguration(projectId);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.batch.item.ItemReader#read()
	 */
	@Override
	public ReadData read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

		if (null == projectConfFieldMapping) {
			log.info("Gathering data for batch - Scrum projects with JQL configuration for the project : {} ",
					projectId);
			initializeReader(projectId);
		}
		ReadData readData = null;
		if (null != projectConfFieldMapping && !fetchLastIssue) {
			KerberosClient krb5Client = null;
			try (ProcessorJiraRestClient client = jiraClient.getClient(projectConfFieldMapping, krb5Client)) {
				if (issueIterator == null || !issueIterator.hasNext()) {
					fetchIssues(client);
					if (CollectionUtils.isNotEmpty(issues)) {
						issueIterator = issues.iterator();
					}
				}

				if (null != issueIterator && issueIterator.hasNext()) {
					Issue issue = issueIterator.next();
					readData = new ReadData();
					readData.setIssue(issue);
					readData.setProjectConfFieldMapping(projectConfFieldMapping);
					readData.setSprintFetch(false);
				}

				if (null == issueIterator || (!issueIterator.hasNext() && issueSize < pageSize)) {
					log.info("Data has been fetched for the project : {}", projectConfFieldMapping.getProjectName());
					fetchLastIssue = true;
					return readData;
				}
			}
		}
		return readData;

	}

	@TrackExecutionTime
	private void fetchIssues(ProcessorJiraRestClient client) throws Exception {

		ReaderRetryHelper.RetryableOperation<Void> retryableOperation = () -> {
			log.info("Reading issues for project : {}, page No : {}", projectConfFieldMapping.getProjectName(),
					pageNumber / pageSize);
			String deltaDate = getDeltaDateFromTraceLog();
			issues = jiraCommonService.fetchIssuesBasedOnJql(projectConfFieldMapping, client, pageNumber, deltaDate);
			issueSize = issues.size();
			pageNumber += pageSize;
			return null;
		};

		try {
			retryHelper.executeWithRetry(retryableOperation);
		} catch (Exception e) {
			log.error("Exception while fetching issues for project: {}, page No: {}",
					projectConfFieldMapping.getProjectName(), pageNumber / pageSize);
			log.error("All retries attempts are failed");
			throw e;
		}
	}

	private String getDeltaDateFromTraceLog() {
		String deltaDate = DateUtil.dateTimeFormatter(
				LocalDateTime.now().minusMonths(jiraProcessorConfig.getPrevMonthCountToFetchData()),
				JiraConstants.QUERYDATEFORMAT);
		if (MapUtils.isEmpty(projectWiseDeltaDate) || StringUtils
				.isBlank(projectWiseDeltaDate.get(projectConfFieldMapping.getBasicProjectConfigId().toString()))) {
			log.info("fetching project status from trace log for project: {}",
					projectConfFieldMapping.getProjectName());
			List<ProcessorExecutionTraceLog> procExecTraceLogs = processorExecutionTraceLogRepo
					.findByProcessorNameAndBasicProjectConfigIdIn(JiraConstants.JIRA,
							Arrays.asList(projectConfFieldMapping.getBasicProjectConfigId().toString()));
			if (CollectionUtils.isNotEmpty(procExecTraceLogs)) {
				String lastSuccessfulRun = deltaDate;
				for (ProcessorExecutionTraceLog processorExecutionTraceLog : procExecTraceLogs) {
					lastSuccessfulRun = processorExecutionTraceLog.getLastSuccessfulRun();
				}
				log.info("project: {}  found in trace log. Data will be fetched from one day before {}",
						projectConfFieldMapping.getProjectName(), lastSuccessfulRun);
				projectWiseDeltaDate = new HashMap<>();
				projectWiseDeltaDate.put(projectConfFieldMapping.getBasicProjectConfigId().toString(),
						lastSuccessfulRun);
			} else {
				log.info("project: {} not found in trace log so data will be fetched from beginning",
						projectConfFieldMapping.getProjectName());
				projectWiseDeltaDate = new HashMap<>();
				projectWiseDeltaDate.put(projectConfFieldMapping.getBasicProjectConfigId().toString(), deltaDate);
			}
		}
		if (MapUtils.isNotEmpty(projectWiseDeltaDate) && !StringUtils
				.isBlank(projectWiseDeltaDate.get(projectConfFieldMapping.getBasicProjectConfigId().toString()))) {
			deltaDate = projectWiseDeltaDate.get(projectConfFieldMapping.getBasicProjectConfigId().toString());
		}

		return deltaDate;
	}
}
