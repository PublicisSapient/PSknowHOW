/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package com.publicissapient.kpidashboard.jira.tasklet;

import java.util.List;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.jira.aspect.TrackExecutionTime;
import com.publicissapient.kpidashboard.jira.client.JiraClient;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.service.FetchSprintReport;

import lombok.extern.slf4j.Slf4j;

/**
 * @author pankumar8
 */
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

	/**
	 * @param sc
	 *            StepContribution
	 * @param cc
	 *            ChunkContext
	 * @return RepeatStatus
	 * @throws Exception
	 *             Exception
	 */
	@TrackExecutionTime
	@Override
	public RepeatStatus execute(StepContribution sc, ChunkContext cc) throws Exception {
		log.info("**** Sprint report for Scrum Board started * * *");
		ProjectConfFieldMapping projConfFieldMapping = fetchProjectConfiguration.fetchConfiguration(projectId);
		log.info("Fetching spring reports for the project : {}", projConfFieldMapping.getProjectName());
		KerberosClient krb5Client = null;
		try (ProcessorJiraRestClient client = jiraClient.getClient(projConfFieldMapping, krb5Client)) {
			List<BoardDetails> boardDetailsList = projConfFieldMapping.getProjectToolConfig().getBoards();
			for (BoardDetails boardDetails : boardDetailsList) {
				List<SprintDetails> sprintDetailsList = fetchSprintReport
						.createSprintDetailBasedOnBoard(projConfFieldMapping, krb5Client, boardDetails);
				sprintRepository.saveAll(sprintDetailsList);
			}
		}
		log.info("**** Sprint report for Scrum Board ended * * *");
		return RepeatStatus.FINISHED;
	}

}
