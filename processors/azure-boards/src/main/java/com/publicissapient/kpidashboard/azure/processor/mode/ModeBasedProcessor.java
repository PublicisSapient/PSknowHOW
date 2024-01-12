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

package com.publicissapient.kpidashboard.azure.processor.mode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.publicissapient.kpidashboard.azure.model.AzureToolConfig;
import com.publicissapient.kpidashboard.azure.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ModeBasedProcessor { // NOSONAR

	@Autowired
	private ProjectToolConfigRepository toolRepository;

	@Autowired
	private ConnectionRepository connectionRepository;

	/**
	 * Validate and Collects Issues and data
	 * 
	 * @param projectConfigList
	 *            projectConfiguration list
	 * @return Map of validateAndCollectIssues
	 */
	public abstract Map<String, Integer> validateAndCollectIssues(List<ProjectBasicConfig> projectConfigList);

	/**
	 * Adds corresponding projectConfig and Fieldmapping to single
	 * ProjectConfFieldMapping
	 * 
	 * @param projectConfigList
	 *            List of project configurations
	 * @param fieldMappingList
	 *            List of all the Field mappings
	 * @return Map of Project Key and ProjectConfFieldMapping
	 */
	public Map<String, ProjectConfFieldMapping> createProjectConfigMap(List<ProjectBasicConfig> projectConfigList,
			List<FieldMapping> fieldMappingList) {
		Map<String, ProjectConfFieldMapping> projectConfigMap = new HashMap<>();
		CollectionUtils.emptyIfNull(projectConfigList).forEach(projectConfig -> {
			ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
			BeanUtils.copyProperties(projectConfFieldMapping, projectConfig);
			projectConfFieldMapping.setKanban(projectConfig.getIsKanban());
			projectConfFieldMapping.setBasicProjectConfigId(projectConfig.getId());
			projectConfFieldMapping.setAzure(getAzureToolConfig(projectConfig.getId()));
			projectConfFieldMapping.setProjectKey(getAzureProjectKey(projectConfig.getId()));
			projectConfFieldMapping.setAzureBoardToolConfigId(getToolConfigId(projectConfig.getId()));
			projectConfFieldMapping.setProjectBasicConfig(projectConfig);
			CollectionUtils.emptyIfNull(fieldMappingList).stream()
					.filter(fieldMapping -> projectConfig.getId().equals(fieldMapping.getBasicProjectConfigId()))
					.forEach(projectConfFieldMapping::setFieldMapping);
			projectConfigMap.putIfAbsent(projectConfig.getProjectName(), projectConfFieldMapping);
		});
		return projectConfigMap;
	}

	/**
	 * Gets the toolConfigId of the azure board tool
	 * 
	 * @param basicProjectConfigId
	 * @return toolConfigId for azure board
	 */
	private ObjectId getToolConfigId(ObjectId basicProjectConfigId) {
		List<ProjectToolConfig> boardsDetails = toolRepository
				.findByToolNameAndBasicProjectConfigId(ProcessorConstants.AZURE, basicProjectConfigId);
		return CollectionUtils.isNotEmpty(boardsDetails) ? boardsDetails.get(0).getId() : null;
	}

	/**
	 * This method gets list of RelevantProjects based on mode
	 * 
	 * @param projectConfigList
	 *            list of all the projects present in the DB
	 * @return relevant project list i.e. online project list or offline project
	 *         list
	 */
	public abstract List<ProjectBasicConfig> getRelevantProjects(List<ProjectBasicConfig> projectConfigList);

	/**
	 * Gets AzureProjectKey
	 * 
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 * @return ProjectKey
	 */
	private String getAzureProjectKey(ObjectId basicProjectConfigId) {
		List<ProjectToolConfig> azureBoardsDetails = toolRepository
				.findByToolNameAndBasicProjectConfigId(ProcessorConstants.AZURE, basicProjectConfigId);
		return azureBoardsDetails.isEmpty() ? StringUtils.EMPTY :
				Optional.ofNullable(azureBoardsDetails.get(0)).map(ProjectToolConfig::getProjectKey).orElse(StringUtils.EMPTY);
	}

	/**
	 * Gets AzureToolConfig
	 * 
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 * @return AzureToolConfig
	 */
	private AzureToolConfig getAzureToolConfig(ObjectId basicProjectConfigId) {
		AzureToolConfig toolObj = new AzureToolConfig();
		List<ProjectToolConfig> azureBoardsDetails = toolRepository
				.findByToolNameAndBasicProjectConfigId(ProcessorConstants.AZURE, basicProjectConfigId);
		if (CollectionUtils.isNotEmpty(azureBoardsDetails)) {
			BeanUtils.copyProperties(toolObj, azureBoardsDetails.get(0));
			if (Optional.ofNullable(azureBoardsDetails.get(0).getConnectionId()).isPresent()) {
				Optional<Connection> conn = connectionRepository.findById(azureBoardsDetails.get(0).getConnectionId());
				if (conn.isPresent()) {
					toolObj.setConnection(conn.get());
				}
			}
		}
		return toolObj;
	}

}
