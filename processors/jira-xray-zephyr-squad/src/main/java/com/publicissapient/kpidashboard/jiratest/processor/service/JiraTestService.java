package com.publicissapient.kpidashboard.jiratest.processor.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.publicissapient.kpidashboard.jiratest.adapter.impl.async.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jiratest.model.ProjectConfFieldMapping;

public interface JiraTestService {

	/**
	 * Explicitly updates queries for the source system, and initiates the update to
	 * MongoDB from those calls.
	 *
	 * @param projectConfig
	 *            Project Configuration Mapping
	 */
	int processesJiraIssues(ProjectConfFieldMapping projectConfig);

	/**
	 * Purges the issues provided
	 *
	 * @param purgeIssuesList
	 *            List of issues to be purged
	 * @param projectConfig
	 *            Project Configuration Mapping
	 */
	void purgeJiraIssues(List<Issue> purgeIssuesList, ProjectConfFieldMapping projectConfig);

	/**
	 * Gets all issues from JIRA
	 *
	 * @param projectConfig
	 *            projectConfig Object
	 * @param startDateTimeByIssueType
	 *            map of start dataTime of issue types
	 * @param userTimeZone
	 *            user timezone
	 * @param pageStart
	 *            page start
	 * @param dataExist
	 *            data exist in db or not
	 * @return list of issues
	 */
	SearchResult getIssues(ProjectConfFieldMapping projectConfig, Map<String, LocalDateTime> startDateTimeByIssueType,
			String userTimeZone, int pageStart, boolean dataExist, ProcessorJiraRestClient client);

	/**
	 * Gets page size from feature settings
	 *
	 * @return pageSize
	 */
	int getPageSize();

	/**
	 * Gets the timeZone of user who is logged in jira
	 *
	 * @param projectConfig
	 *            user provided project configuration
	 * @return String of UserTimeZone
	 */

}
