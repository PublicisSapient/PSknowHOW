package com.publicissapient.kpidashboard.jira.reader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
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
import com.publicissapient.kpidashboard.jira.client.JiraClient;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.model.ReadData;
import com.publicissapient.kpidashboard.jira.service.FetchIssueSprint;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@StepScope
public class IssueSprintReader implements ItemReader<ReadData> {

	@Autowired
	FetchProjectConfiguration fetchProjectConfiguration;

	@Autowired
	JiraClient jiraClient;

	@Autowired
	JiraProcessorConfig jiraProcessorConfig;

	@Autowired
	FetchIssueSprint fetchIssueSprint;

	private Iterator<Issue> issueIterator;
	int pageSize = 50;
	private ProjectConfFieldMapping projectConfFieldMapping;
	int pageNumber = 0;
	List<Issue> issues = new ArrayList<>();
	int issueSize = 0;

	private String sprintId;

	@Autowired
	public IssueSprintReader(@Value("#{jobParameters['sprintId']}") String sprintId) {
		this.sprintId = sprintId;
	}

	public void initializeReader(String sprintId) {
		log.info("**** Jira Issue fetch started * * *");
		pageSize = jiraProcessorConfig.getPageSize();
		projectConfFieldMapping = fetchProjectConfiguration.fetchConfigurationBasedOnSprintId(sprintId);
	}

	@Override
	public ReadData read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

		if (null == projectConfFieldMapping) {
			log.info("Gathering data for batch - Scrum projects with JQL configuration");
			initializeReader(sprintId);
		}
		ReadData readData = null;
		if (null != projectConfFieldMapping) {
			try {
				KerberosClient krb5Client = null;
				ProcessorJiraRestClient client = jiraClient.getClient(projectConfFieldMapping, krb5Client);
				if (null == issueIterator) {
					pageNumber = 0;
					fetchIssues(client);
				}

				if (null != issueIterator && !issueIterator.hasNext()) {
					fetchIssues(client);
				}

				if (null != issueIterator && issueIterator.hasNext()) {
					Issue issue = issueIterator.next();
					readData = new ReadData();
					readData.setIssue(issue);
					readData.setProjectConfFieldMapping(projectConfFieldMapping);
					readData.setSprintFetch(true);
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

	private void fetchIssues(ProcessorJiraRestClient client) {
		log.info("Reading issues for project : {}, page No : {}", projectConfFieldMapping.getProjectName(),
				pageNumber / pageSize);
		issues = fetchIssueSprint.fetchIssuesSprintBasedOnJql(projectConfFieldMapping, client, pageNumber, sprintId);
		issueSize = issues.size();
		pageNumber += pageSize;
		if (CollectionUtils.isNotEmpty(issues)) {
			issueIterator = issues.iterator();
		}
	}

}
