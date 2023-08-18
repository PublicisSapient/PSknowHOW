package com.publicissapient.kpidashboard.jira.reader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.jira.client.JiraClient;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.model.ReadData;
import com.publicissapient.kpidashboard.jira.service.FetchEpicData;
import com.publicissapient.kpidashboard.jira.service.JiraCommonService;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.util.StringUtils;

@Slf4j
@Component
@StepScope
public class IssueScrumBoardReader implements ItemReader<ReadData> {

	@Autowired
	FetchProjectConfiguration fetchProjectConfiguration;

	@Autowired
	JiraClient jiraClient;

	@Autowired
	JiraCommonService jiraCommonService;

	@Autowired
	JiraIssueRepository jiraIssueRepository;

	@Autowired
	ProcessorExecutionTraceLogService processorExecutionTraceLogService;

	@Autowired
	JiraProcessorConfig jiraProcessorConfig;

	@Autowired
	FetchEpicData fetchEpicData;

	@Autowired
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepo;

	private Iterator<Map.Entry<String, List<ProjectConfFieldMapping>>> projectConfMapIterator;
	private Iterator<BoardDetails> boardIterator;
	private Iterator<Issue> issueIterator;
	private Iterator<ProjectConfFieldMapping> projectConfigIterator;
	int pageSize = 50;
	private ProjectConfFieldMapping projectConfFieldMapping;
	int pageNumber = 0;
	String boardId = "";
	List<Issue> issues = new ArrayList<>();
	Map<String, Map<String, String>> projectBoardWiseDeltaDate;
	int boardIssueSize=0;

	public void initializeReader() {
		log.info("**** Jira Issue fetch for Scrum started * * *");
		pageSize = jiraProcessorConfig.getPageSize();
		Map<String, List<ProjectConfFieldMapping>> projConfFieldMappingByUrl = new LinkedHashMap<>();
		projConfFieldMappingByUrl = fetchProjectConfiguration.fetchConfiguration(false);
		if (MapUtils.isNotEmpty(projConfFieldMappingByUrl)) {
			projectConfMapIterator = projConfFieldMappingByUrl.entrySet().iterator();
		}
	}

	@Override
	public ReadData read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

		if (null == projectConfMapIterator) {
			log.info("Gathering data for batch");
			initializeReader();
		}
		ReadData readData = null;
		if (projectConfigIterator == null || !projectConfigIterator.hasNext()) {
			if (projectConfMapIterator.hasNext()) {
				Map.Entry<String, List<ProjectConfFieldMapping>> entry = projectConfMapIterator.next();
				projectConfigIterator = entry.getValue().iterator();
			}

		}
		if (boardIterator == null || !boardIterator.hasNext()) {
			if (projectConfigIterator.hasNext()) {
				projectConfFieldMapping = projectConfigIterator.next();
				if (CollectionUtils.isNotEmpty(projectConfFieldMapping.getProjectToolConfig().getBoards())) {
					boardIterator = projectConfFieldMapping.getProjectToolConfig().getBoards().iterator();
				}
			}
		}
		if (issueIterator == null || !issueIterator.hasNext()) {
			KerberosClient krb5Client = null;
			List<Issue> epicIssues;
			ProcessorJiraRestClient client = jiraClient.getClient(projectConfFieldMapping, krb5Client);
			if (null == issueIterator || boardIssueSize < pageSize) {
				if (boardIterator.hasNext()) {
					BoardDetails boardDetails = boardIterator.next();
					boardId = boardDetails.getBoardId();
					fetchIssues(krb5Client, client);
					epicIssues = fetchEpics(krb5Client, client);
					if (CollectionUtils.isNotEmpty(epicIssues)) {
						issues.addAll(epicIssues);
					}
				}

			} else {
				fetchIssues(krb5Client, client);
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
		}

		if ((null == projectConfMapIterator) || (!projectConfigIterator.hasNext() && !boardIterator.hasNext()
				&& (!issueIterator.hasNext() && boardIssueSize < pageSize))) {
			log.info("Data of all projects has been fetched");
			readData = null;
		}
		return readData;

	}

	private void fetchIssues(KerberosClient krb5Client, ProcessorJiraRestClient client) {
		log.info("Reading issues for project : {} boardid : {} , page No : {}",
				projectConfFieldMapping.getProjectName(), boardId, pageNumber / pageSize);

		String deltaDate = getDeltaDateFromTraceLog();

		issues = jiraCommonService.fetchIssueBasedOnBoard(projectConfFieldMapping, client, krb5Client, pageNumber,
				boardId, deltaDate);
		boardIssueSize=issues.size();
		pageNumber += pageSize;
	}

	private String getDeltaDateFromTraceLog() {
		String deltaDate = jiraProcessorConfig.getStartDate();
		if (MapUtils.isEmpty(projectBoardWiseDeltaDate) || MapUtils
				.isEmpty(projectBoardWiseDeltaDate.get(projectConfFieldMapping.getBasicProjectConfigId().toString()))) {
			log.info("fetching project status from trace log for project: {} board id :{}",
					projectConfFieldMapping.getProjectName(), boardId);
			List<ProcessorExecutionTraceLog> procExecTraceLogs = processorExecutionTraceLogRepo
					.findByProcessorNameAndBasicProjectConfigIdIn(JiraConstants.JIRA,
							Arrays.asList(projectConfFieldMapping.getBasicProjectConfigId().toString()));
			if (CollectionUtils.isNotEmpty(procExecTraceLogs)) {
				projectBoardWiseDeltaDate = new HashMap<>();
				Map<String, String> boardWiseDate = new HashMap<>();
				String lastSuccessfulRun = jiraProcessorConfig.getStartDate();
				for (ProcessorExecutionTraceLog processorExecutionTraceLog : procExecTraceLogs) {
					lastSuccessfulRun = processorExecutionTraceLog.getLastSuccessfulRun();
					if (!StringUtils.isBlank(processorExecutionTraceLog.getBoardId())) {
						boardWiseDate.put(processorExecutionTraceLog.getBoardId(), lastSuccessfulRun);
					}
				}
				// this code is to support backward compatibility. Initially no
				// board was saved with project in in trace log
				if (MapUtils.isEmpty(boardWiseDate)) {
					log.info(
							"project: {} found but board {} not found in trace log so data will be fetched from beginning",
							projectConfFieldMapping.getProjectName(), boardId);
					if (null == lastSuccessfulRun) {
						lastSuccessfulRun = jiraProcessorConfig.getStartDate();
					}
					boardWiseDate.put("noBoard", lastSuccessfulRun);
				}
				projectBoardWiseDeltaDate.put(projectConfFieldMapping.getBasicProjectConfigId().toString(),
						boardWiseDate);
			} else {
				log.info("project: {} not found in trace log so data will be fetched from beginning",
						projectConfFieldMapping.getProjectName());
			}
		}
		if (MapUtils.isNotEmpty(projectBoardWiseDeltaDate) && MapUtils.isNotEmpty(
				projectBoardWiseDeltaDate.get(projectConfFieldMapping.getBasicProjectConfigId().toString()))) {
			Map<String, String> boardWiseDate = projectBoardWiseDeltaDate
					.get(projectConfFieldMapping.getBasicProjectConfigId().toString());
			// this code is to support backward compatibility. Initially no
			// board was saved with project in in trace log
			if (!StringUtils.isBlank(boardWiseDate.get("noBoard"))) {
				deltaDate = boardWiseDate.get("noBoard");
			} else {
				String lastSuccessRun = boardWiseDate.get(boardId);
				log.info("project: {} and board {} found in trace log. Data will be fetched from {}",
						projectConfFieldMapping.getProjectName(),boardId,lastSuccessRun);
				if (!StringUtils.isBlank(lastSuccessRun)) {
					deltaDate = lastSuccessRun;
				}
			}
		}
		return deltaDate;
	}

	private List<Issue> fetchEpics(KerberosClient krb5Client, ProcessorJiraRestClient client) {
		log.info("Reading epics for project : {} boardid : {} ", projectConfFieldMapping.getProjectName(), boardId);
		List<Issue> epicIssues = new ArrayList<>();
		try {
			epicIssues = fetchEpicData.fetchEpic(projectConfFieldMapping, boardId, client, krb5Client);
		} catch (InterruptedException ie) {
			log.error("Interrupted exception occured while fetching epic issues for boards", ie);
		}
		return epicIssues;

	}

}
