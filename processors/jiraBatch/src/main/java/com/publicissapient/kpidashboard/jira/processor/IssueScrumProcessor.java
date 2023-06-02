package com.publicissapient.kpidashboard.jira.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;

@Component
public class IssueScrumProcessor implements ItemProcessor<JiraIssue,JiraIssue> {

	@Override
	public JiraIssue process(JiraIssue issue) throws Exception {
		System.out.println("in item processor");
		return issue;
	}

}
