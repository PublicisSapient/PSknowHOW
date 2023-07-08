package com.publicissapient.kpidashboard.jira.tasklet;

import java.util.List;
import java.util.Map;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.jira.client.JiraClient;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.service.CreateMetadata;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MetaDataScrumBoardTasklet implements Tasklet {
	@Autowired
	FetchProjectConfiguration fetchProjectConfiguration;

	@Autowired
	JiraClient jiraClient;

	@Autowired
	CreateMetadata createMetadata;

	@Autowired
	JiraProcessorConfig jiraProcessorConfig;

	@Override
	public RepeatStatus execute(StepContribution sc, ChunkContext cc) throws Exception {
		log.info("**** Metadata fetch for Scrum Board started * * *");
		Map<String, List<ProjectConfFieldMapping>> projConfFieldMapping = fetchProjectConfiguration
				.fetchConfiguration(false);
		if (jiraProcessorConfig.isFetchMetadata()) {
			projConfFieldMapping.forEach((url, projConfFieldMappings) -> {
				projConfFieldMappings.forEach(projectConfFieldMapping -> {
					KerberosClient krb5Client = null;
					ProcessorJiraRestClient client = jiraClient.getClient(projectConfFieldMapping, krb5Client);
					createMetadata.collectMetadata(projectConfFieldMapping, client);
				});
			});
		}
		log.info("**** Metadata fetch for Scrum Board ended * * *");
		return RepeatStatus.FINISHED;
	}

}
