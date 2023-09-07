package com.publicissapient.kpidashboard.jira.processor;

import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.jira.model.CompositeResult;
import com.publicissapient.kpidashboard.jira.model.ReadData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.codehaus.jettison.json.JSONException;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

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

	@Override
	public CompositeResult process(ReadData readData) throws Exception {
		CompositeResult kanbanCompositeResult = null;
		KanbanJiraIssue kanbanJiraIssue = convertIssueToKanbanJiraIssue(readData);
		log.info("-----------Kanban Processor------------");
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
