package com.publicissapient.kpidashboard.jira.reader;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import com.publicissapient.kpidashboard.jira.client.JiraClient;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.model.ReadData;
import com.publicissapient.kpidashboard.jira.service.JiraCommonService;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.util.StringUtils;

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

	@Autowired
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepo;

	private Iterator<Issue> issueIterator;
	int pageSize = 50;
	private ProjectConfFieldMapping projectConfFieldMapping;
	int pageNumber = 0;
	List<Issue> issues = new ArrayList<>();
	Map<String, String> projectWiseDeltaDate;
	int issueSize = 0;

	private String projectId;

	@Autowired
	public IssueJqlReader(@Value("#{jobParameters['projectId']}") String projectId) {
		this.projectId = projectId;
	}

	public void initializeReader(String projectId) {
		log.info("**** Jira Issue fetch started * * *");
		pageSize = jiraProcessorConfig.getPageSize();
		projectConfFieldMapping = fetchProjectConfiguration.fetchConfiguration(projectId);
	}

	@Override
	public ReadData read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

		if (null == projectConfFieldMapping) {
			log.info("Gathering data for batch - Scrum projects with JQL configuration");
			initializeReader(projectId);
		}
		ReadData readData = null;
		if (null != projectConfFieldMapping) {
			try {
				KerberosClient krb5Client = null;
				ProcessorJiraRestClient client = jiraClient.getClient(projectConfFieldMapping, krb5Client);
				if (null == issueIterator) {
					pageNumber = 0;
					fetchIssues(krb5Client, client);
				}

				if (null != issueIterator && !issueIterator.hasNext()) {
					fetchIssues(krb5Client, client);
				}

				if (null != issueIterator && issueIterator.hasNext()) {
					Issue issue = issueIterator.next();
					readData = new ReadData();
					readData.setIssue(issue);
					readData.setProjectConfFieldMapping(projectConfFieldMapping);
				}

				if (null == issueIterator || (!issueIterator.hasNext() && issueSize < pageSize)) {
					log.info("Data has been fetched for the project : {}", projectConfFieldMapping.getProjectName());
					readData = null;
				}
			} catch (Exception e) {
				log.error("Exception while fetching data for the project {}", projectConfFieldMapping.getProjectName(),
						e);
				readData = null;
			}
		}
		return readData;

	}

	private void fetchIssues(KerberosClient krb5Client, ProcessorJiraRestClient client) {
		log.info("Reading issues for project : {}, page No : {}", projectConfFieldMapping.getProjectName(),
				pageNumber / pageSize);
		String deltaDate = getDeltaDateFromTraceLog();
		issues = jiraCommonService.fetchIssuesBasedOnJql(projectConfFieldMapping, client, krb5Client, pageNumber,
				deltaDate);
		issueSize = issues.size();
		pageNumber += pageSize;
		if (CollectionUtils.isNotEmpty(issues)) {
			issueIterator = issues.iterator();
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
				String lastSuccessfulRun = null;
				for (ProcessorExecutionTraceLog processorExecutionTraceLog : procExecTraceLogs) {
					lastSuccessfulRun = processorExecutionTraceLog.getLastSuccessfulRun();
				}
				if (!StringUtils.isBlank(lastSuccessfulRun)) {
					log.info("project: {}  found in trace log. Data will be fetched from one day before {}",
							projectConfFieldMapping.getProjectName(), lastSuccessfulRun);
					deltaDate = lastSuccessfulRun;
				}
				projectWiseDeltaDate = new HashMap<>();
				projectWiseDeltaDate.put(projectConfFieldMapping.getBasicProjectConfigId().toString(), deltaDate);
			} else {
				log.info("project: {} not found in trace log so data will be fetched from beginning",
						projectConfFieldMapping.getProjectName());
			}
		}
		if (MapUtils.isNotEmpty(projectWiseDeltaDate) && !StringUtils
				.isBlank(projectWiseDeltaDate.get(projectConfFieldMapping.getBasicProjectConfigId().toString()))) {
			deltaDate = projectWiseDeltaDate.get(projectConfFieldMapping.getBasicProjectConfigId().toString());
		}

		return deltaDate;
	}
}
