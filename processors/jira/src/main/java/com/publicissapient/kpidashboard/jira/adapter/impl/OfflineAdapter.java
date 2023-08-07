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

package com.publicissapient.kpidashboard.jira.adapter.impl;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.IssuelinksType;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.publicissapient.kpidashboard.common.model.application.ProjectVersion;
import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.jira.adapter.JiraAdapter;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j
// @Service
public class OfflineAdapter implements JiraAdapter {

	private final List<Version> version;
	private final JiraProcessorConfig jiraProcessorConfig;
	private final SearchResult searchResult;

	/**
	 * @param jiraProcessorConfig
	 *            Jira Processor Configuration
	 * @param searchResult
	 *            Atlassian SearchResult instance
	 * @param version
	 *            list of version for a Jira
	 */
	public OfflineAdapter(JiraProcessorConfig jiraProcessorConfig, SearchResult searchResult, List<Version> version) {

		this.jiraProcessorConfig = jiraProcessorConfig;
		this.searchResult = searchResult;
		this.version = version;

	}

	/**
	 * Gets all issues from file
	 * 
	 * @param projectConfig
	 *            projectConfig
	 * @param startDateTimeByIssueType
	 *            map of start dataTime of issue types
	 * @param userTimeZone
	 *            user timezone
	 * @param pageStart
	 *            page start
	 * @param dataExist
	 *            data exist on db or not
	 * @return list of issues
	 */
	@Override
	public SearchResult getIssues(BoardDetails boardDetails, ProjectConfFieldMapping projectConfig,
			String startDateTimeByIssueType, String userTimeZone, int pageStart, boolean dataExist)
			throws InterruptedException {
		long t0 = System.currentTimeMillis();
		long t2 = System.currentTimeMillis();
		Iterable<Issue> jiraRawRs = searchResult.getIssues();
		long t3 = System.currentTimeMillis();
		long issueParseTime = t3 - t2;
		log.info("time taken to parse issue : {} ", issueParseTime);
		if (jiraRawRs != null) {
			int pageEnd = Math.min(pageStart + getPageSize() - 1, searchResult.getTotal());
			log.info(String.format("Processing issues %d - %d out of %d", pageStart, pageEnd, searchResult.getTotal()));
		}
		long tn = System.currentTimeMillis();
		long issueFetchTime = tn - t0;
		log.info("time taken to fetch issues : {} ", issueFetchTime);
		return searchResult;
	}

	@Override
	public SearchResult getIssues(ProjectConfFieldMapping projectConfig,
			Map<String, LocalDateTime> startDateTimeByIssueType, String userTimeZone, int pageStart,
			boolean dataExist) {
		return null;
	}

	@Override
	public List<Field> getField() {
		return new ArrayList<>();
	}

	@Override
	public List<IssueType> getIssueType() {
		return new ArrayList<>();

	}

	@Override
	public List<Status> getStatus() {
		return new ArrayList<>();
	}

	@Override
	public List<IssuelinksType> getIssueLinkTypes() {
		return new ArrayList<>();

	}

	/**
	 * Gets page size from feature settings
	 *
	 * @return pageSize
	 */
	@Override
	public int getPageSize() {
		return jiraProcessorConfig.getPageSize();
	}

	/**
	 * In offline method there is no user login to just return blank
	 *
	 * @param projectConfig
	 *            user provided project configuration
	 */
	@Override
	public String getUserTimeZone(ProjectConfFieldMapping projectConfig) {
		return "";
	}

	@Override
	public void getSprintReport(ProjectConfFieldMapping projectConfig, String sprintId, String boardId,
			SprintDetails sprintDetails, SprintDetails dbSprintDetails) {
		log.info(sprintId);
		log.info(boardId);
	}

	@Override
	public List<Issue> getEpic(ProjectConfFieldMapping projectConfig, String boardId) throws InterruptedException {
		return null;
	}

	@Override
	public String getDataFromClient(ProjectConfFieldMapping projectConfig, URL url) throws IOException {
		return null;
	}

	@Override
	public List<ProjectVersion> getVersion(ProjectConfFieldMapping projectConfig) {
		return null;
	}

	@Override
	public SearchResult getIssuesSprint(ProjectConfFieldMapping projectConfig, int i, List<String> issueKeys)
			throws InterruptedException {
		return null;
	}

}
