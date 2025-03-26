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
package com.publicissapient.kpidashboard.rally.processor;

import java.io.IOException;
import java.util.Set;

import com.publicissapient.kpidashboard.rally.model.CompositeResult;
import com.publicissapient.kpidashboard.rally.model.ReadData;
import com.publicissapient.kpidashboard.rally.service.RallyCommonService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.model.application.ProjectHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

import lombok.extern.slf4j.Slf4j;

/**
 * @author pankumar8
 */
@Slf4j
@Component
public class IssueScrumProcessor implements ItemProcessor<ReadData, CompositeResult> {

	@Autowired
	private RallyIssueProcessor rallyIssueProcessor;

	@Autowired
	private RallyIssueHistoryProcessor rallyIssueHistoryProcessor;

	@Autowired
	private RallyIssueAccountHierarchyProcessor rallyIssueAccountHierarchyProcessor;

	@Autowired
	private RallyIssueAssigneeProcessor rallyIssueAssigneeProcessor;

	@Autowired
	private SprintDataProcessor sprintDataProcessor;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.batch.item.ItemProcessor#process(java.lang.Object)
	 */
	@Override
	public CompositeResult process(ReadData readData) throws Exception {
		log.debug("Scrum processing started for the project : {}", readData.getProjectConfFieldMapping().getProjectName());
		CompositeResult compositeResult = null;
		JiraIssue jiraIssue = convertIssueToJiraIssue(readData);
		if (null != jiraIssue) {
			compositeResult = new CompositeResult();
			JiraIssueCustomHistory jiraIssueCustomHistory = convertIssueToJiraIssueHistory(readData, jiraIssue);
			Set<SprintDetails> sprintDetailsSet = null;
			Set<ProjectHierarchy> projectHierarchies = null;
			AssigneeDetails assigneeDetails = null;
			if (!readData.isSprintFetch()) {
				sprintDetailsSet = processSprintData(readData);
				projectHierarchies = createAccountHierarchies(jiraIssue, readData, sprintDetailsSet);
				assigneeDetails = createAssigneeDetails(readData, jiraIssue);
			}
			if (StringUtils.isEmpty(readData.getBoardId()) && CollectionUtils.isNotEmpty(sprintDetailsSet)) {
				compositeResult.setSprintDetailsSet(sprintDetailsSet);
			}
			compositeResult.setJiraIssue(jiraIssue);
			compositeResult.setJiraIssueCustomHistory(jiraIssueCustomHistory);
			if (CollectionUtils.isNotEmpty(projectHierarchies)) {
				compositeResult.setProjectHierarchies(projectHierarchies);
			}
			if (null != assigneeDetails) {
				compositeResult.setAssigneeDetails(assigneeDetails);
			}
		}
		return compositeResult;
	}

	private JiraIssue convertIssueToJiraIssue(ReadData readData) throws JSONException {
	    return rallyIssueProcessor.convertToJiraIssue(
	        readData.getHierarchicalRequirement(),
	        readData.getProjectConfFieldMapping(),
	        readData.getBoardId(),
	        readData.getProcessorId()
	    );
	}

	private JiraIssueCustomHistory convertIssueToJiraIssueHistory(ReadData readData, JiraIssue jiraIssue)
			throws JSONException {
		return rallyIssueHistoryProcessor.convertToJiraIssueHistory(readData.getHierarchicalRequirement(),
				readData.getProjectConfFieldMapping(), jiraIssue);
	}

	private Set<SprintDetails> processSprintData(ReadData readData) throws IOException {
		return sprintDataProcessor.processSprintData(readData.getHierarchicalRequirement(), readData.getProjectConfFieldMapping(),
				readData.getBoardId(), readData.getProcessorId());
	}

	private Set<ProjectHierarchy> createAccountHierarchies(JiraIssue jiraIssue, ReadData readData,
			Set<SprintDetails> sprintDetailsSet) {
		return rallyIssueAccountHierarchyProcessor.createAccountHierarchy(jiraIssue, readData.getProjectConfFieldMapping(),
				sprintDetailsSet);
	}

	private AssigneeDetails createAssigneeDetails(ReadData readData, JiraIssue jiraIssue) {
		return rallyIssueAssigneeProcessor.createAssigneeDetails(readData.getProjectConfFieldMapping(), jiraIssue);
	}
}
