package com.publicissapient.kpidashboard.jira.reader;

import java.util.Map;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.jira.client.JiraClient;
import com.publicissapient.kpidashboard.jira.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IssueScrumReader implements ItemReader<JiraIssue> {

	@Autowired
	FetchProjectConfiguration fetchProjectConfiguration;
	
	@Autowired
	JiraClient jiraClient;

	@Override
	public JiraIssue read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		log.info("**** Jira Issue fetch for Scrum started * * *");
		Map<String, ProjectConfFieldMapping> projConfFieldMapping=fetchProjectConfiguration.fetchConfiguration(false);
		
		
		return null;
	}

}
