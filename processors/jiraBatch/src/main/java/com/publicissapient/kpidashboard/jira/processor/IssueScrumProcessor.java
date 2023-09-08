package com.publicissapient.kpidashboard.jira.processor;

import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.jira.model.CompositeResult;
import com.publicissapient.kpidashboard.jira.model.ReadData;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IssueScrumProcessor implements ItemProcessor<ReadData, CompositeResult> {

	@Autowired
	private JiraIssueProcessor jiraIssueProcessor;

	@Autowired
	private JiraIssueHistoryProcessor jiraIssueHistoryProcessor;

	@Autowired
	private JiraIssueAccountHierarchyProcessor jiraIssueAccountHierarchyProcessor;

	@Autowired
	private JiraIssueAssigneeProcessor jiraIssueAssigneeProcessor;

	@Autowired
	private SprintDataProcessor sprintDataProcessor;

	@Override
	public CompositeResult process(ReadData readData) throws Exception {
		CompositeResult compositeResult = null;
		log.info("**** Scrum Processor ****");
		JiraIssue jiraIssue = convertIssueToJiraIssue(readData);
		if (null != jiraIssue) {
			compositeResult = new CompositeResult();
			JiraIssueCustomHistory jiraIssueCustomHistory = convertIssueToJiraIssueHistory(readData, jiraIssue);
			Set<SprintDetails> sprintDetailsSet= processSprintData(readData);
			Set<AccountHierarchy> accountHierarchies=null;
			AssigneeDetails assigneeDetails=null;
			if (!readData.isSprintFetch()) {
				accountHierarchies = createAccountHierarchies(jiraIssue, readData, sprintDetailsSet);
				assigneeDetails = createAssigneeDetails(readData, jiraIssue);
			}
			if(StringUtils.isEmpty(readData.getBoardId()) && CollectionUtils.isNotEmpty(sprintDetailsSet)) {
				compositeResult.setSprintDetailsSet(sprintDetailsSet);
			}
			compositeResult.setJiraIssue(jiraIssue);
			compositeResult.setJiraIssueCustomHistory(jiraIssueCustomHistory);
			if (CollectionUtils.isNotEmpty(accountHierarchies)) {
				compositeResult.setAccountHierarchies(accountHierarchies);
			}
			if (null != assigneeDetails) {
				compositeResult.setAssigneeDetails(assigneeDetails);
			}
		}
		return compositeResult;

	}

	private JiraIssue convertIssueToJiraIssue(ReadData readData) throws JSONException {
		return jiraIssueProcessor.convertToJiraIssue(readData.getIssue(), readData.getProjectConfFieldMapping(),
				readData.getBoardId());
	}

	private JiraIssueCustomHistory convertIssueToJiraIssueHistory(ReadData readData, JiraIssue jiraIssue)
			throws JSONException {
		return jiraIssueHistoryProcessor.convertToJiraIssueHistory(readData.getIssue(),
				readData.getProjectConfFieldMapping(), jiraIssue);
	}

	private Set<SprintDetails> processSprintData(ReadData readData){
		return sprintDataProcessor.processSprintData(readData.getIssue(),readData.getProjectConfFieldMapping(),readData.getBoardId());
	}

	private Set<AccountHierarchy> createAccountHierarchies(JiraIssue jiraIssue, ReadData readData, Set<SprintDetails> sprintDetailsSet) {
		return jiraIssueAccountHierarchyProcessor.createAccountHierarchy(jiraIssue,
				readData.getProjectConfFieldMapping(), sprintDetailsSet);

	}

	private AssigneeDetails createAssigneeDetails(ReadData readData, JiraIssue jiraIssue) {
		return jiraIssueAssigneeProcessor.createAssigneeDetails(readData.getProjectConfFieldMapping(), jiraIssue);

	}

}
