package com.publicissapient.kpidashboard.jira.tasklet;

import java.util.Map;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.jira.client.JiraClient;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.service.CreateMetadata;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MetaDataScrumTasklet implements Tasklet {
	@Autowired
	FetchProjectConfiguration fetchProjectConfiguration;

	@Autowired
	JiraClient jiraClient;

	@Autowired
	CreateMetadata createMetadata;

	@Override
	public RepeatStatus execute(StepContribution sc, ChunkContext cc) throws Exception {
		log.info("**** Jira Issue fetch for Scrum started * * *");
		Map<String, ProjectConfFieldMapping> projConfFieldMapping = fetchProjectConfiguration.fetchConfiguration(false);
		for (Map.Entry<String, ProjectConfFieldMapping> entry : projConfFieldMapping.entrySet()) {
			ProcessorJiraRestClient client = jiraClient.getClient(entry);
			createMetadata.collectMetadata(entry.getValue(), client);
		}
		return RepeatStatus.FINISHED;
	}

}
