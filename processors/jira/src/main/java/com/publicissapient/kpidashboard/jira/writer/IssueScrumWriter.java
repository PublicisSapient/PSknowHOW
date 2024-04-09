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
package com.publicissapient.kpidashboard.jira.writer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.Assignee;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.jira.model.CompositeResult;

import lombok.extern.slf4j.Slf4j;

/**
 * @author pankumar8
 *
 */
@Slf4j
@Component
public class IssueScrumWriter implements ItemWriter<CompositeResult> {

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Autowired
	private AccountHierarchyRepository accountHierarchyRepository;

	@Autowired
	private AssigneeDetailsRepository assigneeDetailsRepository;

	@Autowired
	private SprintRepository sprintRepository;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.batch.item.ItemWriter#write(java.util.List)
	 */
	@Override
	public void write(List<? extends CompositeResult> compositeResults) throws Exception {
		Map<String, JiraIssue> jiraIssues = new HashMap<>();
		Map<String, JiraIssueCustomHistory> jiraHistoryItems = new HashMap<>();
		Set<AccountHierarchy> accountHierarchies = new HashSet<>();
		Map<String, AssigneeDetails> assigneesToSave = new HashMap<>();
		Set<SprintDetails> sprintDetailsSet = new HashSet<>();
		Set<Assignee> assignee = new HashSet<>();

		for (CompositeResult compositeResult : compositeResults) {
			if (null != compositeResult.getJiraIssue()) {
				String key = compositeResult.getJiraIssue().getNumber() + ","
						+ compositeResult.getJiraIssue().getBasicProjectConfigId();
				jiraIssues.putIfAbsent(key, compositeResult.getJiraIssue());
			}
			if (null != compositeResult.getJiraIssueCustomHistory()) {
				String key = compositeResult.getJiraIssueCustomHistory().getStoryID() + ","
						+ compositeResult.getJiraIssueCustomHistory().getBasicProjectConfigId();
				jiraHistoryItems.putIfAbsent(key, compositeResult.getJiraIssueCustomHistory());
			}
			if (null != compositeResult.getSprintDetailsSet()) {
				sprintDetailsSet.addAll(compositeResult.getSprintDetailsSet());
			}
			if (CollectionUtils.isNotEmpty(compositeResult.getAccountHierarchies())) {
				accountHierarchies.addAll(compositeResult.getAccountHierarchies());
			}
			if (CollectionUtils.isNotEmpty(compositeResult.getAssigneeDetails().getAssignee())) {
				assignee.addAll(compositeResult.getAssigneeDetails().getAssignee());
				compositeResult.getAssigneeDetails().setAssignee(assignee);
				assigneesToSave.put(compositeResult.getAssigneeDetails().getBasicProjectConfigId(),
						compositeResult.getAssigneeDetails());
			}
		}

		if (MapUtils.isNotEmpty(jiraIssues)) {
			writeJiraItem(jiraIssues);
		}
		if (MapUtils.isNotEmpty(jiraHistoryItems)) {
			writeJiraHistory(jiraHistoryItems);
		}
		if (CollectionUtils.isNotEmpty(sprintDetailsSet)) {
			writeSprintDetail(sprintDetailsSet);
		}
		if (CollectionUtils.isNotEmpty(accountHierarchies)) {
			writeAccountHierarchy(accountHierarchies);
		}
		if (MapUtils.isNotEmpty(assigneesToSave)) {
			writeAssigneeDetails(assigneesToSave);
		}
	}

	private void writeJiraItem(Map<String, JiraIssue> jiraItems) {
		log.info("Writing issues to Jira_Issue Collection");
		List<JiraIssue> jiraIssues = new ArrayList<>(jiraItems.values());
		jiraIssueRepository.saveAll(jiraIssues);
	}

	private void writeJiraHistory(Map<String, JiraIssueCustomHistory> jiraHistoryItems) {
		log.info("Writing issues to Jira_Issue_custom_history Collection");
		List<JiraIssueCustomHistory> jiraIssueCustomHistories = new ArrayList<>(jiraHistoryItems.values());
		jiraIssueCustomHistoryRepository.saveAll(jiraIssueCustomHistories);
	}

	private void writeSprintDetail(Set<SprintDetails> sprintDetailsSet) {
		log.info("Writing issues to SprintDetails Collection");
		sprintRepository.saveAll(sprintDetailsSet);
	}

	private void writeAccountHierarchy(Set<AccountHierarchy> accountHierarchies) {
		log.info("Writing issues to account_hierarchy Collection");
		accountHierarchyRepository.saveAll(accountHierarchies);
	}

	private void writeAssigneeDetails(Map<String, AssigneeDetails> assigneesToSave) {
		log.info("Writing assignees to assignee_details Collection");
		List<AssigneeDetails> assignees = assigneesToSave.values().stream().collect(Collectors.toList());
		assigneeDetailsRepository.saveAll(assignees);
	}
}
