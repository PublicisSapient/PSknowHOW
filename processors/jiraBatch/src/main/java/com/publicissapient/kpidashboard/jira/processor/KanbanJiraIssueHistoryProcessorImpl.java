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
package com.publicissapient.kpidashboard.jira.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.ChangelogItem;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueHistoryRepository;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.util.JiraIssueClientUtil;
import com.publicissapient.kpidashboard.jira.util.JiraProcessorUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author purgupta2
 *
 */
@Slf4j
@Service
public class KanbanJiraIssueHistoryProcessorImpl implements KanbanJiraIssueHistoryProcessor {

	@Autowired
	private KanbanJiraIssueHistoryRepository kanbanJiraIssueHistoryRepository;

	Map<String, Map<String, KanbanIssueCustomHistory>> projectWiseIssues;

	@Override
	public KanbanIssueCustomHistory convertToKanbanIssueHistory(Issue issue, ProjectConfFieldMapping projectConfig,
			KanbanJiraIssue kanbanJiraIssue) {
		log.info("Converting issue to KanbanJiraIssueHistory for the project : {}", projectConfig.getProjectName());
		String issueNumber = JiraProcessorUtil.deodeUTF8String(issue.getKey());
		FieldMapping fieldMapping = projectConfig.getFieldMapping();
		KanbanIssueCustomHistory jiraIssueHistory = searchIssueInDb(projectConfig, issueNumber);
		setJiraIssueHistory(jiraIssueHistory, kanbanJiraIssue, issue, fieldMapping);

		return jiraIssueHistory;
	}

	private KanbanIssueCustomHistory searchIssueInDb(ProjectConfFieldMapping projectConfig, String issueId) {

		KanbanIssueCustomHistory jiraIssue = new KanbanIssueCustomHistory();
		if (MapUtils.isEmpty(projectWiseIssues)) {
			populateProjectWiseIssues(projectConfig);
		}
		if (MapUtils.isNotEmpty(projectWiseIssues)) {
			Map<String, KanbanIssueCustomHistory> issueIdWiseJiraIssue = projectWiseIssues
					.get(projectConfig.getBasicProjectConfigId().toString());
			if (MapUtils.isNotEmpty(issueIdWiseJiraIssue)) {
				jiraIssue = initializeJiraIssue(issueId, issueIdWiseJiraIssue);
			} else if (MapUtils.isNotEmpty(projectWiseIssues)) {
				projectWiseIssues = new HashMap<>();
				populateProjectWiseIssues(projectConfig);
				Map<String, KanbanIssueCustomHistory> updatedIdWiseIssues = projectWiseIssues
						.get(projectConfig.getBasicProjectConfigId().toString());
				if (MapUtils.isNotEmpty(updatedIdWiseIssues)) {
					jiraIssue = initializeJiraIssue(issueId, updatedIdWiseIssues);
				}

			}
		}
		return jiraIssue;
	}

	private KanbanIssueCustomHistory initializeJiraIssue(String issueId,
			Map<String, KanbanIssueCustomHistory> issueIdWiseJiraIssue) {
		KanbanIssueCustomHistory jiraIssue = null;
		jiraIssue = issueIdWiseJiraIssue.get(issueId);
		if (null == jiraIssue) {
			jiraIssue = new KanbanIssueCustomHistory();
		}
		issueIdWiseJiraIssue = null;
		return jiraIssue;
	}

	private void populateProjectWiseIssues(ProjectConfFieldMapping projectConfig) {
		log.info("Checking if jira history issue exist in Db for  project : {}", projectConfig.getProjectName());
		List<KanbanIssueCustomHistory> existingJiraHistoryIssues = kanbanJiraIssueHistoryRepository
				.findByBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString());
		if (CollectionUtils.isNotEmpty(existingJiraHistoryIssues)) {
			Map<String, KanbanIssueCustomHistory> issueIdWiseJiraIssue = existingJiraHistoryIssues.stream()
					.collect(Collectors.toMap(KanbanIssueCustomHistory::getStoryID, Function.identity()));
			projectWiseIssues = new HashMap<>();
			projectWiseIssues.put(projectConfig.getBasicProjectConfigId().toString(), issueIdWiseJiraIssue);
		}
	}

	public void setJiraIssueHistory(KanbanIssueCustomHistory jiraIssueHistory, KanbanJiraIssue jiraIssue, Issue issue,
			FieldMapping fieldMapping) {

		jiraIssueHistory.setProjectID(jiraIssue.getProjectName());
		jiraIssueHistory.setProjectComponentId(jiraIssue.getProjectID());
		jiraIssueHistory.setProjectKey(jiraIssue.getProjectKey());
		jiraIssueHistory.setProjectName(jiraIssue.getProjectName());
		jiraIssueHistory.setPriority(jiraIssue.getPriority());
		jiraIssueHistory.setRootCauseList(jiraIssue.getRootCauseList());
		jiraIssueHistory.setStoryType(jiraIssue.getTypeName());
		jiraIssueHistory.setAdditionalFilters(jiraIssue.getAdditionalFilters());
		jiraIssueHistory.setUrl(jiraIssue.getUrl());
		jiraIssueHistory.setDescription(jiraIssue.getName());
		// This method is not setup method. write it to keep
		// custom history
		processJiraIssueHistory(jiraIssueHistory, jiraIssue, issue, fieldMapping);
		jiraIssueHistory.setBasicProjectConfigId(jiraIssue.getBasicProjectConfigId());
	}

	private void processJiraIssueHistory(KanbanIssueCustomHistory jiraIssueCustomHistory, KanbanJiraIssue jiraIssue,
			Issue issue, FieldMapping fieldMapping) {
		List<ChangelogGroup> changeLogList = JiraIssueClientUtil.sortChangeLogGroup(issue);
		List<ChangelogGroup> modChangeLogList = new ArrayList<>();
		for (ChangelogGroup changeLog : changeLogList) {
			List<ChangelogItem> changeLogCollection = Lists.newArrayList(changeLog.getItems().iterator());
			ChangelogGroup grp = new ChangelogGroup(changeLog.getAuthor(), changeLog.getCreated(), changeLogCollection);
			modChangeLogList.add(grp);
		}

		if (null != jiraIssue.getDevicePlatform()) {
			jiraIssueCustomHistory.setDevicePlatform(jiraIssue.getDevicePlatform());
		}

		if (null == jiraIssueCustomHistory.getStoryID()) {
			addStoryHistory(jiraIssueCustomHistory, jiraIssue, issue, modChangeLogList, fieldMapping);
		} else {
			addHistoryInJiraIssue(jiraIssueCustomHistory, jiraIssue, modChangeLogList);
		}

	}

	private void addStoryHistory(KanbanIssueCustomHistory jiraIssueCustomHistory, KanbanJiraIssue jiraIssue,
			Issue issue, List<ChangelogGroup> changeLogList, FieldMapping fieldMapping) {
		List<KanbanIssueHistory> kanbanIssueHistoryList = getChangeLog(jiraIssue, changeLogList,
				issue.getCreationDate(), fieldMapping);
		jiraIssueCustomHistory.setStoryID(jiraIssue.getNumber());
		jiraIssueCustomHistory.setHistoryDetails(kanbanIssueHistoryList);
		jiraIssueCustomHistory.setCreatedDate(issue.getCreationDate().toString());
		// estimate
		jiraIssueCustomHistory.setEstimate(jiraIssue.getEstimate());
		jiraIssueCustomHistory.setBufferedEstimateTime(jiraIssue.getBufferedEstimateTime());
		if (NormalizedJira.DEFECT_TYPE.getValue().equalsIgnoreCase(jiraIssue.getTypeName())) {
			jiraIssueCustomHistory.setDefectStoryID(jiraIssue.getDefectStoryID());
		}

	}

	private void addHistoryInJiraIssue(KanbanIssueCustomHistory jiraIssueCustomHistory, KanbanJiraIssue jiraIssue,
			List<ChangelogGroup> changeLogList) {
		if (NormalizedJira.DEFECT_TYPE.getValue().equalsIgnoreCase(jiraIssue.getTypeName())) {
			jiraIssueCustomHistory.setDefectStoryID(jiraIssue.getDefectStoryID());
		}
		createKanbanIssueHistory(jiraIssueCustomHistory, changeLogList);
		jiraIssueCustomHistory.setEstimate(jiraIssue.getEstimate());
	}

	private void createKanbanIssueHistory(KanbanIssueCustomHistory jiraIssueCustomHistory,
			List<ChangelogGroup> changeLogList) {
		List<KanbanIssueHistory> issueHistoryList = new ArrayList<>();
		for (ChangelogGroup history : changeLogList) {
			for (ChangelogItem changelogItem : history.getItems()) {
				if (changelogItem.getField().equalsIgnoreCase(JiraConstants.STATUS)) {
					KanbanIssueHistory kanbanIssueHistory = new KanbanIssueHistory();
					kanbanIssueHistory.setStatus(changelogItem.getToString());
					kanbanIssueHistory.setActivityDate(history.getCreated().toString());
					issueHistoryList.add(kanbanIssueHistory);

				}
			}
			jiraIssueCustomHistory.setHistoryDetails(issueHistoryList);
		}

	}

	private List<KanbanIssueHistory> getChangeLog(KanbanJiraIssue jiraIssue, List<ChangelogGroup> changeLogList,
			DateTime issueCreatedDate, FieldMapping fieldMapping) {
		List<KanbanIssueHistory> historyDetails = new ArrayList<>();
		// creating first entry of issue
		if (null != issueCreatedDate) {
			KanbanIssueHistory kanbanHistory = new KanbanIssueHistory();
			kanbanHistory.setActivityDate(issueCreatedDate.toString());
			kanbanHistory.setStatus(fieldMapping.getStoryFirstStatus());
			historyDetails.add(kanbanHistory);
		}
		if (CollectionUtils.isNotEmpty(changeLogList)) {
			for (ChangelogGroup history : changeLogList) {
				historyDetails.addAll(getIssueHistory(jiraIssue, history));
			}
		}
		return historyDetails;
	}

	private List<KanbanIssueHistory> getIssueHistory(KanbanJiraIssue jiraIssue, ChangelogGroup history) {
		List<KanbanIssueHistory> historyDetails = new ArrayList<>();
		for (ChangelogItem changelogItem : history.getItems()) {
			if (changelogItem.getField().equalsIgnoreCase(JiraConstants.TEST_AUTOMATED)) {
				if (changelogItem.getToString().equalsIgnoreCase(JiraConstants.YES)) {
					jiraIssue.setTestAutomatedDate(JiraProcessorUtil
							.getFormattedDate(JiraProcessorUtil.deodeUTF8String(history.getCreated().toString())));
				} else {
					jiraIssue.setTestAutomatedDate("");
				}
			}

			if (changelogItem.getField().equalsIgnoreCase(JiraConstants.STATUS)) {
				KanbanIssueHistory kanbanHistory = new KanbanIssueHistory();
				kanbanHistory.setActivityDate(history.getCreated().toString());
				kanbanHistory.setStatus(changelogItem.getToString());
				historyDetails.add(kanbanHistory);
			}
		}
		return historyDetails;

	}

	@Override
	public void cleanAllObjects() {
		projectWiseIssues = null;
	}
}
