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
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.service.ProjectHierarchyService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author pankumar8
 */
@Slf4j
@Component
public class IssueScrumWriter implements ItemWriter<CompositeResult> {

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Autowired
	private ProjectHierarchyService projectHierarchyService;

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
	public void write(Chunk<? extends CompositeResult> compositeResults) throws Exception {
		Map<String, JiraIssue> jiraIssues = new HashMap<>();
		Map<String, JiraIssueCustomHistory> jiraHistoryItems = new HashMap<>();
		Set<ProjectHierarchy> projectHierarchies = new HashSet<>();
		Map<String, AssigneeDetails> assigneesToSave = new HashMap<>();
		Set<SprintDetails> sprintDetailsSet = new HashSet<>();
		Set<Assignee> assignee = new HashSet<>();

		for (CompositeResult compositeResult : compositeResults) {
			if (null != compositeResult.getJiraIssue()) {
				String key = compositeResult.getJiraIssue().getNumber() + "," +
						compositeResult.getJiraIssue().getBasicProjectConfigId();
				jiraIssues.putIfAbsent(key, compositeResult.getJiraIssue());
			}
			if (null != compositeResult.getJiraIssueCustomHistory()) {
				String key = compositeResult.getJiraIssueCustomHistory().getStoryID() + "," +
						compositeResult.getJiraIssueCustomHistory().getBasicProjectConfigId();
				jiraHistoryItems.putIfAbsent(key, compositeResult.getJiraIssueCustomHistory());
			}
			if (null != compositeResult.getSprintDetailsSet()) {
				sprintDetailsSet.addAll(compositeResult.getSprintDetailsSet());
			}
			if (CollectionUtils.isNotEmpty(compositeResult.getProjectHierarchies())) {
				projectHierarchies.addAll(compositeResult.getProjectHierarchies());
			}
			addAssigness(assigneesToSave, assignee, compositeResult);
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
		if (CollectionUtils.isNotEmpty(projectHierarchies)) {
			writeAccountHierarchy(projectHierarchies);
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
	 * @param compositeResult
	 */
	private static void addAssigness(Map<String, AssigneeDetails> assigneesToSave, Set<Assignee> assignee,
			CompositeResult compositeResult) {
		if (compositeResult.getAssigneeDetails() != null &&
				CollectionUtils.isNotEmpty(compositeResult.getAssigneeDetails().getAssignee())) {
			assignee.addAll(compositeResult.getAssigneeDetails().getAssignee());
			compositeResult.getAssigneeDetails().setAssignee(assignee);
			assigneesToSave.put(compositeResult.getAssigneeDetails().getBasicProjectConfigId(),
					compositeResult.getAssigneeDetails());
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
		for (SprintDetails sprintDetails : sprintDetailsSet) {
			// Check if the sprint already exists in the repository
			SprintDetails existingSprint = sprintRepository.findBySprintID(sprintDetails.getSprintID());

			if (existingSprint == null) {
				// If the sprint does not exist, save it as a new entry
				sprintRepository.save(sprintDetails);
				log.info("New sprint saved with ID: " + sprintDetails.getSprintID());
			} else {
				// If the sprint exists, update the existing entry
				updateExistingSprint(existingSprint, sprintDetails);
				sprintRepository.save(existingSprint); // Save the updated sprint
				log.info("Updated existing sprint with ID: " + sprintDetails.getSprintID());
			}
		}
	}

	/**
	 * Updates the existing sprint details with new data.
	 *
	 * @param existingSprint The existing sprint details from the repository.
	 * @param newSprint      The new sprint details to update the existing one.
	 */
	private void updateExistingSprint(SprintDetails existingSprint, SprintDetails newSprint) {
		// Update fields from newSprint to existingSprint
		existingSprint.setSprintName(newSprint.getSprintName());
		existingSprint.setStartDate(newSprint.getStartDate());
		existingSprint.setEndDate(newSprint.getEndDate());
		existingSprint.setCompleteDate(newSprint.getCompleteDate());
		existingSprint.setBasicProjectConfigId(newSprint.getBasicProjectConfigId());
		existingSprint.setProcessorId(newSprint.getProcessorId());
		existingSprint.setState(newSprint.getState());

		// Merge total issues instead of overriding
		if (newSprint.getTotalIssues() != null) {
			if (existingSprint.getTotalIssues() == null) {
				existingSprint.setTotalIssues(new HashSet<>()); // Initialize if null
			}
			existingSprint.getTotalIssues().addAll(newSprint.getTotalIssues());
		}

		// Merge completed issues instead of overriding
		if (newSprint.getCompletedIssues() != null) {
			if (existingSprint.getCompletedIssues() == null) {
				existingSprint.setCompletedIssues(new HashSet<>()); // Initialize if null
			}
			existingSprint.getCompletedIssues().addAll(newSprint.getCompletedIssues());
		}

		// Merge not completed issues instead of overriding
		if (newSprint.getNotCompletedIssues() != null) {
			if (existingSprint.getNotCompletedIssues() == null) {
				existingSprint.setNotCompletedIssues(new HashSet<>()); // Initialize if null
			}
			existingSprint.getNotCompletedIssues().addAll(newSprint.getNotCompletedIssues());
		}
	}

	private void writeAccountHierarchy(Set<ProjectHierarchy> projectHierarchies) {
		log.info("Writing issues to project hierarchy Collection");
		projectHierarchyService.saveAll(projectHierarchies);
	}

	private void writeAssigneeDetails(Map<String, AssigneeDetails> assigneesToSave) {
		log.info("Writing assignees to assignee_details Collection");
		List<AssigneeDetails> assignees = assigneesToSave.values().stream().collect(Collectors.toList());
		assigneeDetailsRepository.saveAll(assignees);
	}
}
