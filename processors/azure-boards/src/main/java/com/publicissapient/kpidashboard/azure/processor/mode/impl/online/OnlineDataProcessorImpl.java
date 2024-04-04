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

package com.publicissapient.kpidashboard.azure.processor.mode.impl.online;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.azure.adapter.AzureAdapter;
import com.publicissapient.kpidashboard.azure.adapter.helper.AzureRestClientFactory;
import com.publicissapient.kpidashboard.azure.adapter.impl.OnlineAdapter;
import com.publicissapient.kpidashboard.azure.adapter.impl.async.ProcessorAzureRestClient;
import com.publicissapient.kpidashboard.azure.client.azureissue.AzureIssueClientFactory;
import com.publicissapient.kpidashboard.azure.config.AzureProcessorConfig;
import com.publicissapient.kpidashboard.azure.model.AzureServer;
import com.publicissapient.kpidashboard.azure.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.azure.processor.mode.ModeBasedProcessor;
import com.publicissapient.kpidashboard.azure.util.AzureConstants;
import com.publicissapient.kpidashboard.azure.util.AzureProcessorUtil;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.repository.jira.BoardMetadataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.MetadataIdentifierRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OnlineDataProcessorImpl extends ModeBasedProcessor {

	@Autowired
	AzureRestClientFactory azureRestClientFactory;
	@Autowired
	private AzureProcessorConfig azureProcessorConfig;
	@Autowired
	private FieldMappingRepository fieldMappingRepository;
	@Autowired
	private AzureIssueClientFactory azureIssueClientFactory;
	@Autowired
	private ProcessorAzureRestClient processorAzureRestClient;
	@Autowired
	private BoardMetadataRepository boardMetadataRepository;
	@Autowired
	private MetadataIdentifierRepository metadataIdentifierRepository;
	@Autowired
	private AesEncryptionService aesEncryptionService;
	@Autowired
	private ConnectionRepository connectionRepository;
	@Autowired
	private ProjectToolConfigRepository toolRepository;

	/**
	 * Validates and collects Azure issues using JIA API for projects with
	 * onlinemode
	 *
	 * @param projectConfigList
	 *            List of all configured projects
	 */
	@Override
	public Map<String, Integer> validateAndCollectIssues(List<ProjectBasicConfig> projectConfigList) {
		List<FieldMapping> fieldMappingList = fieldMappingRepository.findAll();
		ExecutorService executor = null;
		Map<String, Integer> issueCountMap = new HashMap<>();
		issueCountMap.put(AzureConstants.SCRUM_DATA, 0);
		issueCountMap.put(AzureConstants.KANBAN_DATA, 0);
		try {

			Map<String, ProjectConfFieldMapping> onlineLineprojectConfigMap = createProjectConfigMap(
					getRelevantProjects(projectConfigList), fieldMappingList);
			MDC.put("OnlineProjectCount", String.valueOf(onlineLineprojectConfigMap.size()));
			executor = Executors.newFixedThreadPool(azureProcessorConfig.getThreadPoolSize());

			CountDownLatch latch = new CountDownLatch(onlineLineprojectConfigMap.size());
			for (Map.Entry<String, ProjectConfFieldMapping> entry : onlineLineprojectConfigMap.entrySet()) {

				// Placeholder for Oauth Client implementation.
				AzureServer azureServer = prepareAzureServer(entry.getValue());
				AzureAdapter azureAdapter = new OnlineAdapter(azureProcessorConfig, processorAzureRestClient,
						azureServer);
				Runnable worker = new AzureOnlineRunnable(latch, azureAdapter, entry.getValue(),
						entry.getValue().getProjectKey(), azureIssueClientFactory, azureProcessorConfig,
						boardMetadataRepository, metadataIdentifierRepository, fieldMappingRepository,
						azureRestClientFactory);// NOPMD
				executor.execute(worker);
			}

			latch.await();

			Integer scrumIssueCount = onlineLineprojectConfigMap.values().stream().filter(x -> !x.isKanban())
					.mapToInt(ProjectConfFieldMapping::getIssueCount).sum();
			Integer kanbanIssueCount = onlineLineprojectConfigMap.values().stream()
					.filter(ProjectConfFieldMapping::isKanban).mapToInt(ProjectConfFieldMapping::getIssueCount).sum();
			issueCountMap.put(AzureConstants.SCRUM_DATA, scrumIssueCount);
			issueCountMap.put(AzureConstants.KANBAN_DATA, kanbanIssueCount);
		} catch (InterruptedException ex) {
			log.error("Error while executing an online azure project", ex);
			Thread.currentThread().interrupt();
		} finally {
			if (executor != null) {
				executor.shutdown();
			}
		}
		return issueCountMap;
	}

	private AzureServer prepareAzureServer(ProjectConfFieldMapping projectConfig) {
		AzureServer azureServer = new AzureServer();
		azureServer.setPat(decryptKey(projectConfig.getAzure().getConnection().getPat()));
		azureServer.setUrl(AzureProcessorUtil.encodeSpaceInUrl(projectConfig.getAzure().getConnection().getBaseUrl()));
		azureServer.setApiVersion(projectConfig.getAzure().getApiVersion());
		azureServer.setUsername(projectConfig.getAzure().getConnection().getUsername());
		return azureServer;
	}

	private String decryptKey(String encryptedKey) {
		return Optional
				.ofNullable(aesEncryptionService.decrypt(encryptedKey, azureProcessorConfig.getAesEncryptionKey()))
				.orElse(encryptedKey);
	}

	@Override
	public List<ProjectBasicConfig> getRelevantProjects(List<ProjectBasicConfig> projectConfigList) {
		List<ProjectBasicConfig> onlineAzureProjects = new ArrayList<>();
		for (ProjectBasicConfig config : projectConfigList) {
			List<ProjectToolConfig> azureBoardsDetails = toolRepository
					.findByToolNameAndBasicProjectConfigId(ProcessorConstants.AZURE, config.getId());
			if (CollectionUtils.isNotEmpty(azureBoardsDetails) && null != azureBoardsDetails.get(0).getConnectionId()) {
				Optional<Connection> azureConn = connectionRepository
						.findById(azureBoardsDetails.get(0).getConnectionId());
				if (azureConn.isPresent() && !azureConn.get().isOffline()) {
					onlineAzureProjects.add(config);
				}
			}
		}

		return onlineAzureProjects;
	}
}