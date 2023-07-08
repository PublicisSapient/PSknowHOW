package com.publicissapient.kpidashboard.jira.tasklet;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SprintScrumBoardTasklet implements Tasklet {
	@Autowired
	FetchProjectConfiguration fetchProjectConfiguration;

	@Autowired
	JiraClient jiraClient;

	@Autowired
	private FetchSprintReport fetchSprintReport;

	@Autowired
	private SprintRepository sprintRepository;

	@Override
	public RepeatStatus execute(StepContribution sc, ChunkContext cc) throws Exception {
		log.info("**** Sprint report for Scrum Board started * * *");
		Map<String, List<ProjectConfFieldMapping>> projConfFieldMapping = fetchProjectConfiguration
				.fetchConfiguration(false);
		for (Map.Entry<String, List<ProjectConfFieldMapping>> entry : projConfFieldMapping.entrySet()) {
			for (ProjectConfFieldMapping projectConfFieldMapping : entry.getValue()) {
				KerberosClient krb5Client = null;
				ProcessorJiraRestClient client = jiraClient.getClient(projectConfFieldMapping, krb5Client);
				Set<SprintDetails> setForCacheClean = new HashSet<>();
				List<SprintDetails> sprintDetailsList = fetchSprintReport
						.createSprintDetailBasedOnBoard(projectConfFieldMapping, setForCacheClean, krb5Client);
				sprintRepository.saveAll(sprintDetailsList);
			}
		}
		log.info("**** Sprint report for Scrum Board ended * * *");
		return RepeatStatus.FINISHED;
	}

}
