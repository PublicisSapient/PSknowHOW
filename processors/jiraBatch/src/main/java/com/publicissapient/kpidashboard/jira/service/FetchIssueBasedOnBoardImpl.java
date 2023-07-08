package com.publicissapient.kpidashboard.jira.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FetchIssueBasedOnBoardImpl implements FetchIssueBasedOnBoard {

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private JiraCommonService jiraCommonService;

	@Override
	public List<Issue> fetchIssues(Map.Entry<String, ProjectConfFieldMapping> entry, ProcessorJiraRestClient client,
			KerberosClient krb5Client) {

		ProjectConfFieldMapping projectConfig = entry.getValue();

		boolean dataExist = false;
		dataExist = (jiraIssueRepository
				.findTopByBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString()) != null);

		Set<SprintDetails> setForCacheClean = new HashSet<>();

		return jiraCommonService.fetchIssueBasedOnBoard(entry, client, krb5Client, dataExist, setForCacheClean);

	}
}