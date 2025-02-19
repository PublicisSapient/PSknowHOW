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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.bson.types.ObjectId;
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
import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import com.publicissapient.kpidashboard.jira.aspect.TrackExecutionTime;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.helper.ReaderRetryHelper;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.model.ReadData;
import com.publicissapient.kpidashboard.jira.repository.JiraProcessorRepository;
import com.publicissapient.kpidashboard.jira.service.FetchEpicData;
import com.publicissapient.kpidashboard.jira.service.JiraClientService;
import com.publicissapient.kpidashboard.jira.service.JiraCommonService;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.util.StringUtils;

/**
 * @author pankumar8
 */
@Slf4j
@Component
@StepScope
public class IssueBoardReader implements ItemReader<ReadData> {

	private static final String NOBOARD_MSG = "noBoard";
	@Autowired
	FetchProjectConfiguration fetchProjectConfiguration;
	@Autowired
	JiraClientService jiraClientService;
	@Autowired
	JiraCommonService jiraCommonService;
	@Autowired
	JiraProcessorConfig jiraProcessorConfig;
	@Autowired
	FetchEpicData fetchEpicData;
	int pageSize = 50;
	int pageNumber = 0;
	String boardId = "";
	List<Issue> issues = new ArrayList<>();
	Map<String, Map<String, String>> projectBoardWiseDeltaDate = new HashMap<>();
	int boardIssueSize = 0;
	boolean fetchLastIssue;
	private ReaderRetryHelper retryHelper;
	@Autowired
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepo;
	@Autowired
	private JiraProcessorRepository jiraProcessorRepository;
	private Iterator<BoardDetails> boardIterator;
	private Iterator<Issue> issueIterator;
	ProjectConfFieldMapping projectConfFieldMapping;
	KerberosClient krb5Client;
	ProcessorJiraRestClient client;

	@Value("#{jobParameters['projectId']}")
	private String projectId;

	@Value("#{jobParameters['processorId']}")
	private String processorId;

	public void initializeReader(String projectId) {
		pageSize = jiraProcessorConfig.getPageSize();
		projectConfFieldMapping = fetchProjectConfiguration.fetchConfiguration(projectId);
		retryHelper = new ReaderRetryHelper();
		krb5Client = jiraClientService.getKerberosClientMap(projectId);
		client = jiraClientService.getRestClientMap(projectId);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.batch.item.ItemReader#read()
	 */
	@Override
	public ReadData read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

		if (null == projectConfFieldMapping) {
			log.info("Gathering data to fetch jira issues for the project : {}", projectId);
			initializeReader(projectId);
		}
		ReadData readData = null;
		if (!fetchLastIssue) {
			if (boardIterator == null &&
					CollectionUtils.isNotEmpty(projectConfFieldMapping.getProjectToolConfig().getBoards())) {
				boardIterator = projectConfFieldMapping.getProjectToolConfig().getBoards().iterator();
			}
			if (checkIssueIterator()) {
				List<Issue> epicIssues;
				if (checkIssue()) {
					pageNumber = 0;
					if (boardIterator.hasNext()) {
						BoardDetails boardDetails = boardIterator.next();
						boardId = boardDetails.getBoardId();
						fetchIssues(client);
						epicIssues = fetchEpics(krb5Client, client);
						if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(epicIssues)) {
							issues.addAll(epicIssues);
						}
					}
				} else {
					fetchIssues(client);
				}

				if (CollectionUtils.isNotEmpty(issues)) {
					issueIterator = issues.iterator();
				}
			}
			if (null != issueIterator && issueIterator.hasNext()) {
				Issue issue = issueIterator.next();
				readData = new ReadData();
				readData.setIssue(issue);
				readData.setProjectConfFieldMapping(projectConfFieldMapping);
				readData.setBoardId(boardId);
				readData.setSprintFetch(false);
				readData.setProcessorId(new ObjectId(processorId));
			}

			if ((null == projectConfFieldMapping) || !boardIterator.hasNext() &&
					(issueIterator != null && !issueIterator.hasNext() && boardIssueSize < pageSize)) {
				log.info("Data has been fetched for the project : {}", getProjectName());
				fetchLastIssue = true;
				return readData;
			}
		}
		return readData;
	}

	private String getProjectName() {
		return projectConfFieldMapping == null ? "" : projectConfFieldMapping.getProjectName();
	}

	private boolean checkIssue() {
		return null == issueIterator || boardIssueSize < pageSize;
	}

	private boolean checkIssueIterator() {
		return issueIterator == null || !issueIterator.hasNext();
	}

	private void fetchIssues(ProcessorJiraRestClient client) throws Exception {

		ReaderRetryHelper.RetryableOperation<Void> retryableOperation = () -> {
			log.info("Reading issues for project: {} boardid: {}, page No: {}", projectConfFieldMapping.getProjectName(),
					boardId, pageNumber / pageSize);

			String deltaDate = getDeltaDateFromTraceLog();

			issues = jiraCommonService.fetchIssueBasedOnBoard(projectConfFieldMapping, client, pageNumber, boardId,
					deltaDate);
			boardIssueSize = issues.size();
			pageNumber += pageSize;
			return null;
		};

		try {
			retryHelper.executeWithRetry(retryableOperation);
		} catch (Exception e) {
			log.error("Exception while fetching issues for project: {} boardid: {}, page No: {}", getProjectName(), boardId,
					pageNumber / pageSize);
			log.error("All retries attempts are failed");

			throw e;
		}
	}

	private String getDeltaDateFromTraceLog() {
		String deltaDate = DateUtil.dateTimeFormatter(
				LocalDateTime.now().minusMonths(jiraProcessorConfig.getPrevMonthCountToFetchData()),
				JiraConstants.QUERYDATEFORMAT);

		if (MapUtils.isEmpty(projectBoardWiseDeltaDate) ||
				MapUtils.isEmpty(projectBoardWiseDeltaDate.get(projectConfFieldMapping.getBasicProjectConfigId().toString()))) {
			setLastSuccessfulRunFromTraceLog(deltaDate);
		}

		if (MapUtils.isNotEmpty(projectBoardWiseDeltaDate) && MapUtils
				.isNotEmpty(projectBoardWiseDeltaDate.get(projectConfFieldMapping.getBasicProjectConfigId().toString()))) {
			deltaDate = updateDeltaDateFromBoardWiseData(deltaDate);
		}

		return deltaDate;
	}

	private void setLastSuccessfulRunFromTraceLog(String deltaDate) {
		log.info("fetching project status from trace log for project: {} board id :{}", getProjectName(), boardId);

		List<ProcessorExecutionTraceLog> procExecTraceLogs = processorExecutionTraceLogRepo
				.findByProcessorNameAndBasicProjectConfigIdAndProgressStatsFalse(JiraConstants.JIRA,
						projectConfFieldMapping.getBasicProjectConfigId().toString());

		if (CollectionUtils.isNotEmpty(procExecTraceLogs)) {
			Map<String, String> boardWiseDate = new HashMap<>();
			String lastSuccessfulRun = deltaDate;

			for (ProcessorExecutionTraceLog processorExecutionTraceLog : procExecTraceLogs) {
				lastSuccessfulRun = processorExecutionTraceLog.getLastSuccessfulRun();

				if (!StringUtils.isBlank(processorExecutionTraceLog.getBoardId())) {
					boardWiseDate.put(processorExecutionTraceLog.getBoardId(), lastSuccessfulRun);
				}
			}

			if (MapUtils.isEmpty(boardWiseDate)) {
				log.info("project: {} found but board {} not found in trace log so data will be fetched from beginning",
						getProjectName(), boardId);

				if (lastSuccessfulRun == null) {
					lastSuccessfulRun = deltaDate;
				}

				boardWiseDate.put(NOBOARD_MSG, lastSuccessfulRun);
			}

			projectBoardWiseDeltaDate.put(projectConfFieldMapping.getBasicProjectConfigId().toString(), boardWiseDate);
		} else {
			log.info("project: {} not found in trace log so data will be fetched from beginning", getProjectName());
			Map<String, String> boardWiseDate = new HashMap<>();
			if (StringUtils.isEmpty(boardId)) {
				boardWiseDate.put(boardId, deltaDate);
			} else {
				boardWiseDate.put(NOBOARD_MSG, deltaDate);
			}
			projectBoardWiseDeltaDate.put(projectConfFieldMapping.getBasicProjectConfigId().toString(), boardWiseDate);
		}
	}

	private String updateDeltaDateFromBoardWiseData(String deltaDate) {
		Map<String, String> boardWiseDate = projectBoardWiseDeltaDate
				.get(projectConfFieldMapping.getBasicProjectConfigId().toString());

		if (!StringUtils.isBlank(boardWiseDate.get(NOBOARD_MSG))) {
			deltaDate = boardWiseDate.get(NOBOARD_MSG);
		} else {
			String lastSuccessRun = boardWiseDate.get(boardId);

			log.info("project: {} and board {} found in trace log. Data will be fetched from one day before {}",
					getProjectName(), boardId, lastSuccessRun);

			if (!StringUtils.isBlank(lastSuccessRun)) {
				deltaDate = lastSuccessRun;
			}
		}

		return deltaDate;
	}

	@TrackExecutionTime
	private List<Issue> fetchEpics(KerberosClient krb5Client, ProcessorJiraRestClient client) throws Exception {

		ReaderRetryHelper.RetryableOperation<List<Issue>> retryableOperation = () -> {
			log.info("Reading epics for project: {} boardid: {}", getProjectName(), boardId);
			return fetchEpicData.fetchEpic(projectConfFieldMapping, boardId, client, krb5Client);
		};
		try {
			return retryHelper.executeWithRetry(retryableOperation);
		} catch (Exception e) {
			log.error("Exception while fetching epics for project: {} boardid: {}", getProjectName(), boardId);
			log.error("All retries attempts are failed");
			// Re-throw the exception to allow for retries
			throw e;
		}
	}
}
