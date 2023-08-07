package com.publicissapient.kpidashboard.jira.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.jira.model.CompositeResult;
import com.publicissapient.kpidashboard.jira.model.ReadData;
import org.codehaus.jettison.json.JSONException;

@Component
public class IssueScrumProcessor implements ItemProcessor<ReadData, CompositeResult> {

	@Autowired
	private JiraIssueProcessor jiraIssueProcessor;

	@Override
	public CompositeResult process(ReadData readData) throws Exception {
		CompositeResult compositeResult = null;
		JiraIssue jiraIssue=convertIssueToJiraIssue(readData);
		if(null!=jiraIssue) {
			compositeResult = new CompositeResult();
			compositeResult.setJiraIssue(jiraIssue);
		}
		return compositeResult;

	}

	private JiraIssue convertIssueToJiraIssue(ReadData readData) throws JSONException {
		return jiraIssueProcessor.convertToJiraIssue(readData.getIssue(), readData.getProjectConfFieldMapping());
	}

}
