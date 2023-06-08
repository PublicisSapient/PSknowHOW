package com.publicissapient.kpidashboard.jira.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;

@Component
public class IssueScrumProcessor implements ItemProcessor<Issue,JiraIssue> {

	@Override
	public JiraIssue process(Issue issue) throws Exception {
		System.out.println("in item processor");
		JiraIssue jiraIssue=new JiraIssue();
		jiraIssue.setNumber(issue.getKey());
		jiraIssue.setIssueId(issue.getSummary());
		return jiraIssue;
	}

}
