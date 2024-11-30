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

import com.publicissapient.kpidashboard.jira.service.CreateJiraIssueReleaseStatusImpl;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.service.CreateJiraIssueReleaseStatus;
import com.publicissapient.kpidashboard.jira.service.JiraClientService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author pankumar8
 */
@Slf4j
@Component
@StepScope
public class JiraIssueReleaseStatusTasklet implements Tasklet {

	@Autowired
	FetchProjectConfiguration fetchProjectConfiguration;

	@Autowired
	CreateJiraIssueReleaseStatusImpl createJiraIssueReleaseStatus;

	@Autowired
	JiraProcessorConfig jiraProcessorConfig;

	@Autowired
	JiraClientService jiraClientService;

	@Value("#{jobParameters['projectId']}")
	private String projectId;

	/**
	 * @param sc
	 *            StepContribution
	 * @param cc
	 *            ChunkContext
	 * @return RepeatStatus
	 * @throws Exception
	 *             Exception
	 */
	@Override
	public RepeatStatus execute(StepContribution sc, ChunkContext cc) throws Exception {
		ProjectConfFieldMapping projConfFieldMapping = fetchProjectConfiguration.fetchConfiguration(projectId);
        ProcessorJiraRestClient client = jiraClientService.getRestClientMap(projectId);
		log.info("Fetching release statuses for the project : {}", projConfFieldMapping.getProjectName());
		createJiraIssueReleaseStatus.processAndSaveProjectStatusCategory(client, projectId);
		return RepeatStatus.FINISHED;
	}

}
