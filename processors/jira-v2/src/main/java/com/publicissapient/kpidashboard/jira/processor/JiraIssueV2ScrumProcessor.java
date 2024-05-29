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

import java.io.IOException;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.model.jira.JiraIssueV2;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetailsV2;
import com.publicissapient.kpidashboard.jira.model.CompositeResult;
import com.publicissapient.kpidashboard.jira.model.ReadData;

import lombok.extern.slf4j.Slf4j;

/**
 * @author pankumar8
 *
 */
@Slf4j
@Component
public class JiraIssueV2ScrumProcessor implements ItemProcessor<ReadData, CompositeResult> {

	@Autowired
	private JiraIssueV2Processor jiraissueV2Processor;

//	@Autowired
//	private JiraIssueHistoryProcessor jiraIssueHistoryProcessor;
//
//	@Autowired
//	private JiraIssueAccountHierarchyProcessor jiraIssueAccountHierarchyProcessor;
//
//	@Autowired
//	private JiraIssueAssigneeProcessor jiraIssueAssigneeProcessor;

	@Autowired
	private SprintV2DataProcessor sprintV2DataProcessor;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.item.ItemProcessor#process(java.lang.Object)
	 */
	@Override
	public CompositeResult process(ReadData readData) throws Exception {
		log.debug("Scrum processing started for the project : {}",
				readData.getProjectConfFieldMapping().getProjectName());
		CompositeResult compositeResult = null;
		JiraIssueV2 jiraIssue = convertIssueToJiraIssueV2(readData);
		if (null != jiraIssue) {
			compositeResult = new CompositeResult();
//			JiraIssueCustomHistory jiraIssueCustomHistory = convertIssueToJiraIssueHistory(readData, jiraIssue);
			Set<SprintDetailsV2> sprintDetailsSet = null;
//			Set<AccountHierarchy> accountHierarchies = null;
//			AssigneeDetails assigneeDetails = null;
			if (!readData.isSprintFetch()) {
				sprintDetailsSet = processSprintData(readData);
//				accountHierarchies = createAccountHierarchies(jiraIssue, readData, sprintDetailsSet);
//				assigneeDetails = createAssigneeDetails(readData, jiraIssue);
			}
			if (StringUtils.isEmpty(readData.getBoardId()) && CollectionUtils.isNotEmpty(sprintDetailsSet)) {
				compositeResult.setSprintDetailsV2Set(sprintDetailsSet);
			}
			compositeResult.setJiraIssueV2(jiraIssue);
//			compositeResult.setJiraIssueCustomHistory(jiraIssueCustomHistory);
//			if (CollectionUtils.isNotEmpty(accountHierarchies)) {
//				compositeResult.setAccountHierarchies(accountHierarchies);
//			}
//			if (null != assigneeDetails) {
//				compositeResult.setAssigneeDetails(assigneeDetails);
//			}
		}
		return compositeResult;

	}

	private JiraIssueV2 convertIssueToJiraIssueV2(ReadData readData) throws JSONException {
		return jiraissueV2Processor.convertToJiraIssueV2(readData.getIssue(), readData.getProjectConfFieldMapping(),
				readData.getBoardId());
	}

//	private JiraIssueCustomHistory convertIssueToJiraIssueHistory(ReadData readData, JiraIssueV2 jiraIssue)
//			throws JSONException {
//		return jiraIssueHistoryProcessor.convertToJiraIssueHistory(readData.getIssue(),
//				readData.getProjectConfFieldMapping(), jiraIssue);
//	}

	private Set<SprintDetailsV2> processSprintData(ReadData readData) throws IOException {
		return sprintV2DataProcessor.processSprintV2Data(readData.getIssue(), readData.getProjectConfFieldMapping(),
				readData.getBoardId());
	}

//	private Set<AccountHierarchy> createAccountHierarchies(JiraIssueV2 jiraIssue, ReadData readData,
//														   Set<SprintDetails> sprintDetailsSet) {
//		return jiraIssueAccountHierarchyProcessor.createAccountHierarchy(jiraIssue,
//				readData.getProjectConfFieldMapping(), sprintDetailsSet);
//
//	}

//	private AssigneeDetails createAssigneeDetails(ReadData readData, JiraIssueV2 jiraIssue) {
//		return jiraIssueAssigneeProcessor.createAssigneeDetails(readData.getProjectConfFieldMapping(), jiraIssue);
//
//	}

}
