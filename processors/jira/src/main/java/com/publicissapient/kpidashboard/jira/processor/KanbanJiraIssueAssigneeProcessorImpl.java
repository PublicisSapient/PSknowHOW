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

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.jira.Assignee;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

import lombok.extern.slf4j.Slf4j;

/**
 * @author purgupta2
 *
 */
@Slf4j
@Service
public class KanbanJiraIssueAssigneeProcessorImpl implements KanbanJiraIssueAssigneeProcessor {

	@Autowired
	private AssigneeDetailsRepository assigneeDetailsRepository;

	@Override
	public AssigneeDetails createKanbanAssigneeDetails(ProjectConfFieldMapping projectConfig,
			KanbanJiraIssue jiraIssue) {
		log.info("Creating assignee details for the Kanban project : {}", projectConfig.getProjectName());
		AssigneeDetails assigneeDetails = assigneeDetailsRepository.findByBasicProjectConfigIdAndSource(
				projectConfig.getBasicProjectConfigId().toString(), ProcessorConstants.JIRA);

		Set<Assignee> assigneeSetToSave = new LinkedHashSet<>();
		if (StringUtils.isNotEmpty(jiraIssue.getAssigneeId()) && StringUtils.isNotEmpty(jiraIssue.getAssigneeName())) {
			Assignee assignee = new Assignee(jiraIssue.getAssigneeId(), jiraIssue.getAssigneeName());
			assigneeSetToSave.add(assignee);
			if (assigneeDetails == null) {
				assigneeDetails = new AssigneeDetails();
				assigneeDetails.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString());
				assigneeDetails.setSource(ProcessorConstants.JIRA);
				assigneeDetails.setAssignee(assigneeSetToSave);
				if (!projectConfig.getProjectBasicConfig().isSaveAssigneeDetails()) {
					assigneeDetails.setAssigneeSequence(2);
				}
			} else if (!assigneeDetails.getAssignee().contains(assignee)) {
				Set<Assignee> updatedAssigneeSetToSave = new HashSet<>();
				updatedAssigneeSetToSave.addAll(assigneeDetails.getAssignee());
				updatedAssigneeSetToSave.addAll(assigneeSetToSave);
				assigneeDetails.setAssignee(updatedAssigneeSetToSave);
				if (!projectConfig.getProjectBasicConfig().isSaveAssigneeDetails()) {
					assigneeDetails.setAssigneeSequence(assigneeDetails.getAssigneeSequence() + 1);
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
		return assigneeDetails;
	}

}
