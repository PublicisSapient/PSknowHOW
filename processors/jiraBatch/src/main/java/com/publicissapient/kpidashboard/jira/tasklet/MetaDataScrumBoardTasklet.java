package com.publicissapient.kpidashboard.jira.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
@StepScope
public class MetaDataScrumBoardTasklet implements Tasklet {
	@Autowired
	FetchProjectConfiguration fetchProjectConfiguration;

	@Autowired
	JiraClient jiraClient;

	@Autowired
	CreateMetadata createMetadata;

	@Autowired
	JiraProcessorConfig jiraProcessorConfig;

	private String projectId;

	@Autowired
	public MetaDataScrumBoardTasklet(@Value("#{jobParameters['projectId']}") String projectId) {
		this.projectId = projectId;
	}

	@Override
	public RepeatStatus execute(StepContribution sc, ChunkContext cc) throws Exception {
		log.info("**** Metadata fetch started ****");
//		Map<String, List<ProjectConfFieldMapping>> projConfFieldMapping=new HashMap<>();
		try {
			ProjectConfFieldMapping projConfFieldMapping = fetchProjectConfiguration.fetchConfiguration(projectId);
			if (jiraProcessorConfig.isFetchMetadata()) {
				KerberosClient krb5Client = null;
				ProcessorJiraRestClient client = jiraClient.getClient(projConfFieldMapping, krb5Client);
				createMetadata.collectMetadata(projConfFieldMapping, client);
			}
		}catch (Exception e) {
			log.error("Exception while fetching metadata", e);
		}
		log.info("**** Metadata fetch for Scrum Board ended ****");
		return RepeatStatus.FINISHED;
	}

}
