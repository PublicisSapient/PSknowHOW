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

import java.util.concurrent.CountDownLatch;

import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.azure.adapter.AzureAdapter;
import com.publicissapient.kpidashboard.azure.adapter.helper.AzureRestClientFactory;
import com.publicissapient.kpidashboard.azure.client.azureissue.AzureIssueClient;
import com.publicissapient.kpidashboard.azure.client.azureissue.AzureIssueClientFactory;
import com.publicissapient.kpidashboard.azure.client.metadata.MetaDataClientImpl;
import com.publicissapient.kpidashboard.azure.config.AzureProcessorConfig;
import com.publicissapient.kpidashboard.azure.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.jira.BoardMetadataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.MetadataIdentifierRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AzureOnlineRunnable implements Runnable {// NOPMD

	private CountDownLatch latch;
	private ProjectConfFieldMapping onlineprojectConfigMap;
	private AzureAdapter azureAdapter;
	private String projectKey;
	private AzureIssueClientFactory factory;
	private AzureProcessorConfig azureConfig;
	private BoardMetadataRepository boardMetadataRepository;
	private MetadataIdentifierRepository metadataIdentifierRepository;
	private FieldMappingRepository fieldMappingRepository;
	private AzureRestClientFactory azureRestClientFactory;

	/**
	 * Sets the configurations and variables
	 *
	 * @param latch
	 *            latch
	 * @param azureAdapter
	 *            JiraAdapter
	 * @param onlineprojectConfigMap
	 *            OnlineConfigurationMap
	 * @param projectKey
	 *            projectKey
	 * @param factory
	 *            JiraIssueClientFactory
	 * @param azureConfig
	 *            AzureProcessorConfig
	 * @param boardMetadataRepository
	 *            board metadata repo
	 *
	 * @param metadataIdentifierRepository
	 *            metadata identifier
	 *
	 * @param fieldMappingRepository
	 *            fieldmapping repo
	 *
	 *
	 */
	public AzureOnlineRunnable(CountDownLatch latch, AzureAdapter azureAdapter, // NOSONAR
			ProjectConfFieldMapping onlineprojectConfigMap, String projectKey, AzureIssueClientFactory factory, // NOSONAR
			AzureProcessorConfig azureConfig, BoardMetadataRepository boardMetadataRepository, // NOSONAR
			MetadataIdentifierRepository metadataIdentifierRepository, FieldMappingRepository fieldMappingRepository,
			AzureRestClientFactory azureRestClientFactory)// NOSONAR
	{
		this.latch = latch;
		this.azureAdapter = azureAdapter;
		this.onlineprojectConfigMap = onlineprojectConfigMap;
		this.projectKey = projectKey;
		this.factory = factory;
		this.azureConfig = azureConfig;
		this.boardMetadataRepository = boardMetadataRepository;
		this.metadataIdentifierRepository = metadataIdentifierRepository;
		this.fieldMappingRepository = fieldMappingRepository;
		this.azureRestClientFactory = azureRestClientFactory;
	}

	public AzureOnlineRunnable() {
	}

	@Override
	public void run() {

		try {
			long start = System.currentTimeMillis();
			MDC.put("ProjectDataStartTime", String.valueOf(start));
			MDC.put("ProjectKey", projectKey);
			if (azureConfig.isFetchMetadata()) {
				collectMetadata(azureAdapter, onlineprojectConfigMap);
			}
			collectAzureIssueData(azureAdapter, onlineprojectConfigMap, projectKey);

			// Placeholder for Release data implementation

			long end = System.currentTimeMillis();
			MDC.put("ProjectDataEndTime", String.valueOf(end));

		} catch (Exception ex) {
			log.error("Exception in processing Azure Project", ex);
		} finally {
			log.info("run() complete.");
			latch.countDown();
		}

	}

	/**
	 * Collects JiraIssue Data
	 *
	 * @param azureAdapter
	 *            JiraAdapter to create Connection
	 * @param projectConfig
	 *            Project Configuration map
	 * @param projectKey
	 *            ProjectKey
	 */
	private void collectAzureIssueData(AzureAdapter azureAdapter, ProjectConfFieldMapping projectConfig,
			String projectKey) {
		long storyDataStart = System.currentTimeMillis();
		MDC.put("storyDataStartTime", String.valueOf(storyDataStart));
		projectConfig.setIssueCount(0);
		// Scrum Or Kanban AzureIssue Client
		AzureIssueClient azureIssueClient = factory.getAzureIssueDataClient(projectConfig);
		int count = azureIssueClient.processesAzureIssues(projectConfig, projectKey, azureAdapter);
		projectConfig.setIssueCount(count);
		MDC.put("AzureIssueCount", String.valueOf(count));
		long end = System.currentTimeMillis();
		MDC.put("storyDataEndTime", String.valueOf(end));
	}

	/**
	 * @param azureAdapter
	 *            to create connection.
	 *
	 * @param projectConfig
	 *            for procesing purpose.
	 */
	private void collectMetadata(AzureAdapter azureAdapter, ProjectConfFieldMapping projectConfig) {
		if (null == boardMetadataRepository.findByProjectBasicConfigId(projectConfig.getBasicProjectConfigId())) {
			long metaDataStart = System.currentTimeMillis();
			MDC.put("meraDataStartTime", String.valueOf(metaDataStart));
			MetaDataClientImpl metadata = new MetaDataClientImpl(azureAdapter, boardMetadataRepository,
					fieldMappingRepository, metadataIdentifierRepository);
			boolean isSuccess = metadata.processMetadata(projectConfig);
			if (isSuccess) {
				azureRestClientFactory.cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT,
						CommonConstant.CACHE_FIELD_MAPPING_MAP);
				azureRestClientFactory.cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT,
						CommonConstant.CACHE_PROJECT_CONFIG_MAP);
			}
			MDC.put("Fetched metadata", String.valueOf(isSuccess));
			long end = System.currentTimeMillis();
			MDC.put("meraDataStartTime", String.valueOf(end));
		}
	}

}