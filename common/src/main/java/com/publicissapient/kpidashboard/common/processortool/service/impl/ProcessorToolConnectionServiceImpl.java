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

package com.publicissapient.kpidashboard.common.processortool.service.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author narsingh9 implementation class of ServerDetailsService
 */
@Service
@Slf4j
public class ProcessorToolConnectionServiceImpl implements ProcessorToolConnectionService {

	@Autowired
	private ProjectToolConfigRepository projectToolConfigRepository;

	@Autowired
	private ConnectionRepository connectionRepository;

	/**
	 * find all tools and map it with their respective connections
	 * 
	 * @param toolName
	 *            name of tool to retrieve
	 * @return List of ServerDetails
	 */
	@Override
	public List<ProcessorToolConnection> findByTool(String toolName) {
		List<ProjectToolConfig> projectToolConfigList = projectToolConfigRepository.findByToolName(toolName);
		return getProcessorConnectionList(projectToolConfigList);
	}

	@Override
	public List<ProcessorToolConnection> findByToolAndBasicProjectConfigId(String toolName, ObjectId configId) {
		List<ProjectToolConfig> projectToolConfigList = projectToolConfigRepository
				.findByToolNameAndBasicProjectConfigId(toolName, configId);
		return getProcessorConnectionList(projectToolConfigList);
	}

	/**
	 * mapping method for ProjectToolConfig and Connections Details
	 * 
	 * @param projectToolList
	 *            all toolConfig related to tool
	 * @return {@link ProcessorToolConnection} object
	 */
	private List<ProcessorToolConnection> getProcessorConnectionList(List<ProjectToolConfig> projectToolList) {
		List<ProcessorToolConnection> toolConfigConnectionList = new LinkedList<>();
		if (Optional.ofNullable(projectToolList).isPresent() && !projectToolList.isEmpty()) {
			List<Connection> connectionList = connectionRepository.findByIdIn(
					projectToolList.stream().map(ProjectToolConfig::getConnectionId).collect(Collectors.toSet()));
			if (Optional.ofNullable(connectionList).isPresent() && !connectionList.isEmpty()) {
				Map<ObjectId, Connection> connectionMap = connectionList.stream()
						.collect(Collectors.toMap(Connection::getId, Function.identity()));
				projectToolList.stream()
						.filter(projectTool -> Optional.ofNullable(connectionMap.get(projectTool.getConnectionId()))
								.isPresent())
						.forEach(projectTool -> toolConfigConnectionList.add(createProcessorToolConnectionObject(
								projectTool, connectionMap.get(projectTool.getConnectionId()))));
			}

		}
		return toolConfigConnectionList;
	}

	/**
	 * create ProcessorToolConnection object
	 * 
	 * @param toolConfig
	 *            {@link ProjectToolConfig} object
	 * @param connection
	 *            {@link Connection} object
	 * @return ServerDetails object
	 */
	private ProcessorToolConnection createProcessorToolConnectionObject(ProjectToolConfig toolConfig,
			Connection connection) {
		ProcessorToolConnection processorToolConnection = new ProcessorToolConnection();
		processorToolConnection.setId(toolConfig.getId());
		processorToolConnection.setJobName(toolConfig.getJobName());
		processorToolConnection.setToolName(toolConfig.getToolName());
		processorToolConnection.setBasicProjectConfigId(toolConfig.getBasicProjectConfigId());
		processorToolConnection.setProjectId(toolConfig.getProjectId());
		processorToolConnection.setProjectKey(toolConfig.getProjectKey());
		processorToolConnection.setBranch(toolConfig.getBranch());
		processorToolConnection.setNewRelicApiQuery(toolConfig.getNewRelicApiQuery());
		processorToolConnection.setNewRelicAppNames(toolConfig.getNewRelicAppNames());
		processorToolConnection.setEnv(toolConfig.getEnv());
		processorToolConnection.setRepoSlug(toolConfig.getRepoSlug());
		processorToolConnection.setBitbucketProjKey(toolConfig.getBitbucketProjKey());
		processorToolConnection.setApiVersion(toolConfig.getApiVersion());
		processorToolConnection.setQueryEnabled(toolConfig.isQueryEnabled());
		processorToolConnection.setBoardQuery(toolConfig.getBoardQuery());
		processorToolConnection.setBoards(toolConfig.getBoards());
		processorToolConnection.setRepositoryName(toolConfig.getRepositoryName());
		processorToolConnection.setWorkflowID(toolConfig.getWorkflowID());
		processorToolConnection.setConnectionId(toolConfig.getConnectionId());
		processorToolConnection.setConnectionName(connection.getConnectionName());
		processorToolConnection.setUrl(connection.getBaseUrl());
		processorToolConnection.setUsername(connection.getUsername());
		processorToolConnection.setPassword(connection.getPassword());
		processorToolConnection.setPatOAuthToken(connection.getPatOAuthToken());
		processorToolConnection.setBearerToken(connection.isBearerToken());
		processorToolConnection.setType(connection.getType());
		processorToolConnection.setApiEndPoint(connection.getApiEndPoint());
		processorToolConnection.setConsumerKey(connection.getConsumerKey());
		processorToolConnection.setPrivateKey(connection.getPrivateKey());
		processorToolConnection.setApiKey(connection.getApiKey());
		processorToolConnection.setClientSecretKey(connection.getClientSecretKey());
		processorToolConnection.setOAuth(connection.getIsOAuth());
		processorToolConnection.setClientId(connection.getClientId());
		processorToolConnection.setTenantId(connection.getTenantId());
		processorToolConnection.setPat(connection.getPat());
		processorToolConnection.setApiKeyFieldName(connection.getApiKeyFieldName());
		processorToolConnection.setAccessToken(connection.getAccessToken());
		processorToolConnection.setOffline(connection.isOffline());
		processorToolConnection.setOfflineFilePath(connection.getOfflineFilePath());
		processorToolConnection.setCloudEnv(connection.isCloudEnv());
		processorToolConnection.setAccessTokenEnabled(connection.isAccessTokenEnabled());
		processorToolConnection.setRegressionAutomationLabels(toolConfig.getRegressionAutomationLabels());
		processorToolConnection.setTestAutomationStatusLabel(toolConfig.getTestAutomationStatusLabel());
		processorToolConnection.setAutomatedTestValue(toolConfig.getAutomatedTestValue());
		processorToolConnection.setTestAutomated(toolConfig.getTestAutomated());
		processorToolConnection.setCanNotAutomatedTestValue(toolConfig.getCanNotAutomatedTestValue());
		processorToolConnection.setTestRegressionLabel(toolConfig.getTestRegressionLabel());
		processorToolConnection.setTestRegressionValue(toolConfig.getTestRegressionValue());
		processorToolConnection.setRegressionAutomationFolderPath(toolConfig.getRegressionAutomationFolderPath());
		processorToolConnection.setInSprintAutomationFolderPath(toolConfig.getInSprintAutomationFolderPath());
		processorToolConnection.setOrganizationKey(toolConfig.getOrganizationKey());
		processorToolConnection.setJobType(toolConfig.getJobType());
		processorToolConnection.setDeploymentProjectName(toolConfig.getDeploymentProjectName());
		processorToolConnection.setDeploymentProjectId(toolConfig.getDeploymentProjectId());
		processorToolConnection.setParameterNameForEnvironment(toolConfig.getParameterNameForEnvironment());
		processorToolConnection.setVault(connection.isVault());
		processorToolConnection.setJiraTestCaseType(toolConfig.getJiraTestCaseType());
		processorToolConnection.setTestAutomatedIdentification(toolConfig.getTestAutomatedIdentification());
		processorToolConnection
				.setTestAutomationCompletedIdentification(toolConfig.getTestAutomationCompletedIdentification());
		processorToolConnection.setTestRegressionIdentification(toolConfig.getTestRegressionIdentification());
		processorToolConnection
				.setTestAutomationCompletedByCustomField(toolConfig.getTestAutomationCompletedByCustomField());
		processorToolConnection.setTestRegressionByCustomField(toolConfig.getTestRegressionByCustomField());
		processorToolConnection.setJiraAutomatedTestValue(toolConfig.getJiraAutomatedTestValue());
		processorToolConnection.setJiraRegressionTestValue(toolConfig.getJiraRegressionTestValue());
		processorToolConnection.setJiraCanBeAutomatedTestValue(toolConfig.getJiraCanBeAutomatedTestValue());
		processorToolConnection.setTestCaseStatus(toolConfig.getTestCaseStatus());
		processorToolConnection.setPatOAuthToken(connection.getPatOAuthToken());
		processorToolConnection.setAzureIterationStatusFieldUpdate(toolConfig.isAzureIterationStatusFieldUpdate());
		processorToolConnection.setProjectComponent(toolConfig.getProjectComponent());
		return processorToolConnection;
	}

}
