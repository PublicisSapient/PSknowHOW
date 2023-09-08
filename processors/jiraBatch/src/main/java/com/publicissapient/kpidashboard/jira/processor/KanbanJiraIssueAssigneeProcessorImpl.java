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

@Slf4j
@Service
public class KanbanJiraIssueAssigneeProcessorImpl implements KanbanJiraIssueAssigneeProcessor {

	@Autowired
	private AssigneeDetailsRepository assigneeDetailsRepository;

	AssigneeDetails assigneeDetails;

	@Override
	public AssigneeDetails createKanbanAssigneeDetails(ProjectConfFieldMapping projectConfig,
			KanbanJiraIssue jiraIssue) {

		if (null == assigneeDetails || !assigneeDetails.getBasicProjectConfigId()
				.equalsIgnoreCase(projectConfig.getBasicProjectConfigId().toString())) {
			log.info("Fetching assignee details for the project : {}", projectConfig.getProjectName());
			assigneeDetails = assigneeDetailsRepository.findByBasicProjectConfigIdAndSource(
					projectConfig.getBasicProjectConfigId().toString(), ProcessorConstants.JIRA);
		}

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

	@Override
	public void cleanAllObjects() {
		assigneeDetails = null;
	}

}
