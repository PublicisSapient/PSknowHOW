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
import com.publicissapient.kpidashboard.jira.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.service.FetchScrumReleaseData;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@StepScope
public class ScrumReleaseDataTasklet implements Tasklet {
	@Autowired
	FetchProjectConfiguration fetchProjectConfiguration;

	@Autowired
	JiraClient jiraClient;

	@Autowired
	FetchScrumReleaseData fetchScrumReleaseData;

	@Autowired
	JiraProcessorConfig jiraProcessorConfig;

	private String projectId;

	@Autowired
	public ScrumReleaseDataTasklet(@Value("#{jobParameters['projectId']}") String projectId) {
		this.projectId = projectId;
	}

	@Override
	public RepeatStatus execute(StepContribution sc, ChunkContext cc) throws Exception {
		log.info("**** ReleaseData fetch started ****");
		try {
			ProjectConfFieldMapping projConfFieldMapping = fetchProjectConfiguration.fetchConfiguration(projectId);
			KerberosClient krb5Client = null;
			jiraClient.getClient(projConfFieldMapping, krb5Client);
			fetchScrumReleaseData.processReleaseInfo(projConfFieldMapping, krb5Client);
		} catch (Exception e) {
			log.error("Exception while fetching ReleaseData", e);
		}
		log.info("**** ReleaseData fetch ended ****");
		return RepeatStatus.FINISHED;
	}

}
