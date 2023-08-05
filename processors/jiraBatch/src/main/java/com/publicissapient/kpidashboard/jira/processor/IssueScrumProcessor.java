package com.publicissapient.kpidashboard.jira.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.jira.model.CompositeResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IssueScrumProcessor implements ItemProcessor<Issue,CompositeResult> {

	@Override
	public CompositeResult process(Issue issue) throws Exception {
		CompositeResult compositeResult=new CompositeResult();
		compositeResult.setJiraIssue(convertIssueToJiraIssue(issue));
		return compositeResult;
		
	}
	
	private JiraIssue convertIssueToJiraIssue(Issue issue) {
		JiraIssue jiraIssue=new JiraIssue();
		jiraIssue.setNumber(issue.getKey());
		jiraIssue.setIssueId(issue.getSummary());
		return jiraIssue;
	}

}
