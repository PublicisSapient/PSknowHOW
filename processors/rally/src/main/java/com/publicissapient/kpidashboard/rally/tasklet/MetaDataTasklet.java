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
package com.publicissapient.kpidashboard.rally.tasklet;

import java.util.Optional;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.rally.aspect.TrackExecutionTime;
import com.publicissapient.kpidashboard.rally.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.rally.config.RallyProcessorConfig;
import com.publicissapient.kpidashboard.rally.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.rally.service.CreateMetadata;
import com.publicissapient.kpidashboard.rally.service.JiraClientService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author pankumar8
 */
@Slf4j
@Component
@StepScope
public class MetaDataTasklet implements Tasklet {
	@Autowired
	FetchProjectConfiguration fetchProjectConfiguration;

	@Autowired
	JiraClientService jiraClientService;

	@Autowired
	CreateMetadata createMetadata;

	@Autowired
	RallyProcessorConfig rallyProcessorConfig;

	@Value("#{jobParameters['projectId']}")
	private String projectId;

	@Value("#{jobParameters['isScheduler']}")
	private String isScheduler;

	/**
	 * @param sc
	 *          StepContribution
	 * @param cc
	 *          ChunkContext
	 * @return RepeatStatus
	 * @throws Exception
	 *           Exception
	 */
	@TrackExecutionTime
	@Override
	public RepeatStatus execute(StepContribution sc, ChunkContext cc) throws Exception {
		ProjectConfFieldMapping projConfFieldMapping = fetchProjectConfiguration.fetchConfiguration(projectId);
		log.info("Fetching metadata for the project : {}", projConfFieldMapping.getProjectName());
		Optional<Connection> connectionOptional = projConfFieldMapping.getJira().getConnection();
		KerberosClient krb5Client = null;
		if (connectionOptional.isPresent() && connectionOptional.get().isJaasKrbAuth()) {
			Connection connection = connectionOptional.get();
			krb5Client = new KerberosClient(connection.getJaasConfigFilePath(), connection.getKrb5ConfigFilePath(),
					connection.getJaasUser(), connection.getSamlEndPoint(), connection.getBaseUrl());
			jiraClientService.setKerberosClientMap(projectId, krb5Client);
		}
//		ProcessorJiraRestClient client = rallyClient.getClient(projConfFieldMapping, krb5Client);
//		jiraClientService.setRestClientMap(projectId, client);
		if (rallyProcessorConfig.isFetchMetadata()) {
			createMetadata.collectMetadata(projConfFieldMapping, isScheduler);
		}
		return RepeatStatus.FINISHED;
	}
}
