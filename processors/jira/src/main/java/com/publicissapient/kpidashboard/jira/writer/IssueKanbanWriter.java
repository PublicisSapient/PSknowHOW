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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.jira.model.CompositeResult;

import lombok.extern.slf4j.Slf4j;

/**
 * @author purgupta2
 *
 */
@Slf4j
@Component
public class IssueKanbanWriter implements ItemWriter<CompositeResult> {

	@Autowired
	private KanbanJiraIssueRepository kanbanJiraIssueRepository;
	@Autowired
	private KanbanJiraIssueHistoryRepository kanbanJiraIssueHistoryRepository;
	@Autowired
	private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepository;
	@Autowired
	private AssigneeDetailsRepository assigneeDetailsRepository;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.item.ItemWriter#write(java.util.List)
	 */
	@Override
	public void write(Chunk<? extends CompositeResult> kanbanCompositeResults) throws Exception {
		List<KanbanJiraIssue> jiraIssues = new ArrayList<>();
		List<KanbanIssueCustomHistory> kanbanIssueCustomHistory = new ArrayList<>();
		Set<KanbanAccountHierarchy> accountHierarchies = new HashSet<>();
		Map<String, AssigneeDetails> assigneesToSave = new HashMap<>();

		for (CompositeResult kanbanCompositeResult : kanbanCompositeResults) {
			if (null != kanbanCompositeResult.getKanbanJiraIssue()) {
				jiraIssues.add(kanbanCompositeResult.getKanbanJiraIssue());
			}
			if (null != kanbanCompositeResult.getKanbanIssueCustomHistory()) {
				kanbanIssueCustomHistory.add(kanbanCompositeResult.getKanbanIssueCustomHistory());
			}
			if (CollectionUtils.isNotEmpty(kanbanCompositeResult.getKanbanAccountHierarchies())) {
				accountHierarchies.addAll(kanbanCompositeResult.getKanbanAccountHierarchies());
			}
			if (null != kanbanCompositeResult.getAssigneeDetails()) {
				assigneesToSave.put(kanbanCompositeResult.getAssigneeDetails().getBasicProjectConfigId(),
						kanbanCompositeResult.getAssigneeDetails());
			}
		}
		if (CollectionUtils.isNotEmpty(jiraIssues)) {
			writeKanbanJiraItem(jiraIssues);
		}
		if (CollectionUtils.isNotEmpty(kanbanIssueCustomHistory)) {
			writeKanbanJiraHistory(kanbanIssueCustomHistory);
		}
		if (CollectionUtils.isNotEmpty(accountHierarchies)) {
			writeKanbanAccountHierarchy(accountHierarchies);
		}
		if (MapUtils.isNotEmpty(assigneesToSave)) {
			writeAssigneeDetails(assigneesToSave);
		}

	}

	public void writeKanbanJiraItem(List<KanbanJiraIssue> jiraItems) {
		log.info("Writing issues to kanban_jira_Issue Collection");
		kanbanJiraIssueRepository.saveAll(jiraItems);
	}

	public void writeKanbanJiraHistory(List<KanbanIssueCustomHistory> kanbanIssueCustomHistory) {
		log.info("Writing issues to kanban_jira_Issue_custom_history Collection");
		kanbanJiraIssueHistoryRepository.saveAll(kanbanIssueCustomHistory);
	}

	public void writeKanbanAccountHierarchy(Set<KanbanAccountHierarchy> accountHierarchies) {
		log.info("Writing issues to kanban_account_hierarchy Collection");
		kanbanAccountHierarchyRepository.saveAll(accountHierarchies);
	}

	public void writeAssigneeDetails(Map<String, AssigneeDetails> assigneesToSave) {
		log.info("Writing assingees to asignee_details Collection");
		List<AssigneeDetails> assignees = assigneesToSave.values().stream().collect(Collectors.toList());
		assigneeDetailsRepository.saveAll(assignees);
	}

}
