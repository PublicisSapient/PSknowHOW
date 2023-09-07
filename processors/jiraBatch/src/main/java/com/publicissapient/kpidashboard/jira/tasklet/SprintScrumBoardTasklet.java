package com.publicissapient.kpidashboard.jira.tasklet;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.jira.client.JiraClient;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.service.FetchSprintReport;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@StepScope
public class SprintScrumBoardTasklet implements Tasklet {

	@Autowired
	FetchProjectConfiguration fetchProjectConfiguration;

	@Autowired
	JiraClient jiraClient;

	@Autowired
	private FetchSprintReport fetchSprintReport;

	@Autowired
	private SprintRepository sprintRepository;

	private String projectId;

	@Autowired
	public SprintScrumBoardTasklet(@Value("#{jobParameters['projectId']}") String projectId) {
		this.projectId = projectId;
	}

	@Override
	public RepeatStatus execute(StepContribution sc, ChunkContext cc) throws Exception {
		log.info("**** Sprint report for Scrum Board started * * *");
		try {
		ProjectConfFieldMapping projConfFieldMapping = fetchProjectConfiguration.fetchConfiguration(projectId);
				KerberosClient krb5Client = null;
				ProcessorJiraRestClient client = jiraClient.getClient(projConfFieldMapping, krb5Client);
				Set<SprintDetails> setForCacheClean = new HashSet<>();
				List<SprintDetails> sprintDetailsList = fetchSprintReport
						.createSprintDetailBasedOnBoard(projConfFieldMapping, setForCacheClean, krb5Client);
				sprintRepository.saveAll(sprintDetailsList);
		} catch (Exception e) {
			log.error("Exception while fetching sprint data for scrum project and board setup", e);
		}
		log.info("**** Sprint report for Scrum Board ended * * *");
		return RepeatStatus.FINISHED;
	}

}
