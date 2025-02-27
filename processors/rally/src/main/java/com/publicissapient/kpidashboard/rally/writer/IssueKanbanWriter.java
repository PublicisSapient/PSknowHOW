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
package com.publicissapient.kpidashboard.rally.writer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.rally.model.CompositeResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.model.application.ProjectHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.Assignee;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.common.service.ProjectHierarchyService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author purgupta2
 */
@Slf4j
@Component
public class IssueKanbanWriter implements ItemWriter<CompositeResult> {

	@Autowired
	private KanbanJiraIssueRepository kanbanJiraIssueRepository;
	@Autowired
	private KanbanJiraIssueHistoryRepository kanbanJiraIssueHistoryRepository;
	@Autowired
	private ProjectHierarchyService projectHierarchyService;
	@Autowired
	private AssigneeDetailsRepository assigneeDetailsRepository;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.batch.item.ItemWriter#write(java.util.List)
	 */
	@Override
	public void write(Chunk<? extends CompositeResult> kanbanCompositeResults) throws Exception {
		Map<String, KanbanJiraIssue> jiraIssues = new HashMap<>();
		Map<String, KanbanIssueCustomHistory> kanbanIssueCustomHistory = new HashMap<>();
		Set<ProjectHierarchy> projectHierarchies = new HashSet<>();
		Map<String, AssigneeDetails> assigneesToSave = new HashMap<>();
		Set<Assignee> assignee = new HashSet<>();

		for (CompositeResult kanbanCompositeResult : kanbanCompositeResults) {
			if (null != kanbanCompositeResult.getKanbanJiraIssue()) {
				String key = kanbanCompositeResult.getKanbanJiraIssue().getNumber() + "," +
						kanbanCompositeResult.getKanbanJiraIssue().getBasicProjectConfigId();
				jiraIssues.putIfAbsent(key, kanbanCompositeResult.getKanbanJiraIssue());
			}
			if (null != kanbanCompositeResult.getKanbanIssueCustomHistory()) {
				String key = kanbanCompositeResult.getKanbanIssueCustomHistory().getStoryID() + "," +
						kanbanCompositeResult.getKanbanIssueCustomHistory().getBasicProjectConfigId();
				kanbanIssueCustomHistory.putIfAbsent(key, kanbanCompositeResult.getKanbanIssueCustomHistory());
			}
			if (CollectionUtils.isNotEmpty(kanbanCompositeResult.getProjectHierarchies())) {
				projectHierarchies.addAll(kanbanCompositeResult.getProjectHierarchies());
			}
			addAssignees(assigneesToSave, assignee, kanbanCompositeResult);
		}
		if (MapUtils.isNotEmpty(jiraIssues)) {
			writeKanbanJiraItem(jiraIssues);
		}
		if (MapUtils.isNotEmpty(kanbanIssueCustomHistory)) {
			writeKanbanJiraHistory(kanbanIssueCustomHistory);
		}
		if (CollectionUtils.isNotEmpty(projectHierarchies)) {
			writeKanbanAccountHierarchy(projectHierarchies);
		}
		if (MapUtils.isNotEmpty(assigneesToSave)) {
			writeAssigneeDetails(assigneesToSave);
		}
	}

	/**
	 * Adding assignees to map
	 *
	 * @param assigneesToSave
	 * @param assignee
	 * @param kanbanCompositeResult
	 */
	private static void addAssignees(Map<String, AssigneeDetails> assigneesToSave, Set<Assignee> assignee,
			CompositeResult kanbanCompositeResult) {
		if (kanbanCompositeResult.getAssigneeDetails() != null &&
				CollectionUtils.isNotEmpty(kanbanCompositeResult.getAssigneeDetails().getAssignee())) {
			assignee.addAll(kanbanCompositeResult.getAssigneeDetails().getAssignee());
			kanbanCompositeResult.getAssigneeDetails().setAssignee(assignee);
			assigneesToSave.put(kanbanCompositeResult.getAssigneeDetails().getBasicProjectConfigId(),
					kanbanCompositeResult.getAssigneeDetails());
		}
	}

	public void writeKanbanJiraItem(Map<String, KanbanJiraIssue> jiraItems) {
		log.info("Writing issues to kanban_jira_Issue Collection");
		List<KanbanJiraIssue> jiraIssues = new ArrayList<>(jiraItems.values());
		kanbanJiraIssueRepository.saveAll(jiraIssues);
	}

	public void writeKanbanJiraHistory(Map<String, KanbanIssueCustomHistory> kanbanIssueCustomHistory) {
		log.info("Writing issues to kanban_jira_Issue_custom_history Collection");
		List<KanbanIssueCustomHistory> jiraIssueCustomHistories = new ArrayList<>(kanbanIssueCustomHistory.values());
		kanbanJiraIssueHistoryRepository.saveAll(jiraIssueCustomHistories);
	}

	public void writeKanbanAccountHierarchy(Set<ProjectHierarchy> projectHierarchySet) {
		log.info("Writing issues to kanban_account_hierarchy Collection");
		projectHierarchyService.saveAll(projectHierarchySet);
	}

	public void writeAssigneeDetails(Map<String, AssigneeDetails> assigneesToSave) {
		log.info("Writing assingees to asignee_details Collection");
		List<AssigneeDetails> assignees = assigneesToSave.values().stream().collect(Collectors.toList());
		assigneeDetailsRepository.saveAll(assignees);
	}
}
