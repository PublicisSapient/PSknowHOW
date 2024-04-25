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

import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.codehaus.jettison.json.JSONException;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.jira.model.CompositeResult;
import com.publicissapient.kpidashboard.jira.model.ReadData;

import lombok.extern.slf4j.Slf4j;

/**
 * @author purgupta2
 *
 */
@Slf4j
@Component
public class IssueKanbanProcessor implements ItemProcessor<ReadData, CompositeResult> {

	@Autowired
	private KanbanJiraIssueProcessor kanbanJiraIssueProcessor;

	@Autowired
	private KanbanJiraIssueHistoryProcessor kanbanJiraHistoryProcessor;

	@Autowired
	private KanbanJiraIssueAccountHierarchyProcessor kanbanJiraIssueAccountHierarchyProcessor;

	@Autowired
	private KanbanJiraIssueAssigneeProcessor kanbanJiraIssueAssigneeProcessor;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.item.ItemProcessor#process(java.lang.Object)
	 */
	@Override
	public CompositeResult process(ReadData readData) throws Exception {
		CompositeResult kanbanCompositeResult = null;
		log.debug("Kanban processing started for the project : {}",
				readData.getProjectConfFieldMapping().getProjectName());
		KanbanJiraIssue kanbanJiraIssue = convertIssueToKanbanJiraIssue(readData);
		if (null != kanbanJiraIssue) {
			kanbanCompositeResult = new CompositeResult();
			KanbanIssueCustomHistory kanbanIssueCustomHistory = convertIssueToKanbanIssueHistory(readData,
					kanbanJiraIssue);
			Set<KanbanAccountHierarchy> accountHierarchies = createKanbanAccountHierarchies(kanbanJiraIssue, readData);
			AssigneeDetails assigneeDetails = createAssigneeDetails(readData, kanbanJiraIssue);
			kanbanCompositeResult.setKanbanJiraIssue(kanbanJiraIssue);
			kanbanCompositeResult.setKanbanIssueCustomHistory(kanbanIssueCustomHistory);
			if (CollectionUtils.isNotEmpty(accountHierarchies)) {
				kanbanCompositeResult.setKanbanAccountHierarchies(accountHierarchies);
			}
			if (null != assigneeDetails) {
				kanbanCompositeResult.setAssigneeDetails(assigneeDetails);
			}
		}
		return kanbanCompositeResult;
	}

	private KanbanJiraIssue convertIssueToKanbanJiraIssue(ReadData readData) throws JSONException {
		return kanbanJiraIssueProcessor.convertToKanbanJiraIssue(readData.getIssue(),
				readData.getProjectConfFieldMapping(), readData.getBoardId());
	}

	private KanbanIssueCustomHistory convertIssueToKanbanIssueHistory(ReadData readData,
			KanbanJiraIssue kanbanJiraIssue) throws JSONException {
		return kanbanJiraHistoryProcessor.convertToKanbanIssueHistory(readData.getIssue(),
				readData.getProjectConfFieldMapping(), kanbanJiraIssue);
	}

	private Set<KanbanAccountHierarchy> createKanbanAccountHierarchies(KanbanJiraIssue kanbanJiraIssue,
			ReadData readData) {
		return kanbanJiraIssueAccountHierarchyProcessor.createKanbanAccountHierarchy(kanbanJiraIssue,
				readData.getProjectConfFieldMapping());

	}

	private AssigneeDetails createAssigneeDetails(ReadData readData, KanbanJiraIssue kanbanJiraIssue) {
		return kanbanJiraIssueAssigneeProcessor.createKanbanAssigneeDetails(readData.getProjectConfFieldMapping(),
				kanbanJiraIssue);
	}

}
