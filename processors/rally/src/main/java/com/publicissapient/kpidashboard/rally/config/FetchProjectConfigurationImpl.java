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

package com.publicissapient.kpidashboard.rally.config;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.rally.model.RallyToolConfig;
import com.publicissapient.kpidashboard.rally.model.ProjectConfFieldMapping;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FetchProjectConfigurationImpl implements FetchProjectConfiguration {

	@Autowired
	private FieldMappingRepository fieldMappingRepository;

	@Autowired
	private ProjectToolConfigRepository toolRepository;

	@Autowired
	private ProjectBasicConfigRepository projectConfigRepository;

	@Autowired
	private ConnectionRepository connectionRepository;

	@Autowired
	private SprintRepository sprintRepository;

	@Override
	public List<String> fetchBasicProjConfId(String toolName, boolean queryEnabled, boolean isKanban) {
		List<ProjectBasicConfig> allProjects = projectConfigRepository.findByKanbanAndProjectOnHold(isKanban, false);
		List<ObjectId> projectConfigsIds = allProjects.stream().map(projConf -> projConf.getId())
				.collect(Collectors.toList());
		List<ProjectToolConfig> projectToolConfigs = toolRepository
				.findByToolNameAndQueryEnabledAndBasicProjectConfigIdIn(toolName, queryEnabled, projectConfigsIds);
		return projectToolConfigs.stream().map(toolConfig -> toolConfig.getBasicProjectConfigId().toString())
				.collect(Collectors.toList());
	}

	@Override
	public ProjectConfFieldMapping fetchConfigurationBasedOnSprintId(String sprintId) {
		ProjectConfFieldMapping projectConfFieldMapping = null;
		SprintDetails sprintDetails = sprintRepository.findBySprintID(sprintId);
		ProjectBasicConfig projectBasicConfig = projectConfigRepository.findById(sprintDetails.getBasicProjectConfigId())
				.orElse(new ProjectBasicConfig());

		FieldMapping fieldMapping = fieldMappingRepository
				.findByBasicProjectConfigId(sprintDetails.getBasicProjectConfigId());
		List<ProjectToolConfig> projectToolConfigs = toolRepository
				.findByBasicProjectConfigId(sprintDetails.getBasicProjectConfigId());
		if (CollectionUtils.isNotEmpty(projectToolConfigs)) {
			ProjectToolConfig projectToolConfig = projectToolConfigs.get(0);
			if (null != projectToolConfig.getConnectionId()) {
				Optional<Connection> jiraConnOpt = connectionRepository.findById(projectToolConfig.getConnectionId());
				RallyToolConfig rallyToolConfig = createJiraToolConfig(projectToolConfig, jiraConnOpt);
				projectConfFieldMapping = createProjectConfFieldMapping(fieldMapping, projectBasicConfig, projectToolConfig,
						rallyToolConfig);
			}
		}
		return projectConfFieldMapping;
	}

	@Override
	public ProjectConfFieldMapping fetchConfiguration(String projectId) {
		ObjectId projectConfigId = new ObjectId(projectId);
		ProjectConfFieldMapping projectConfFieldMapping = null;
		ProjectBasicConfig projectBasicConfig = projectConfigRepository.findById(projectConfigId).orElse(null);
		FieldMapping fieldMapping = fieldMappingRepository.findByBasicProjectConfigId(projectConfigId);
		List<ProjectToolConfig> projectToolConfigs = toolRepository
				.findByToolNameAndBasicProjectConfigId(ProcessorConstants.RALLY, projectConfigId);
		if (CollectionUtils.isNotEmpty(projectToolConfigs)) {
			ProjectToolConfig projectToolConfig = projectToolConfigs.get(0);
			if (null != projectToolConfig.getConnectionId()) {
				Optional<Connection> jiraConnOpt = connectionRepository.findById(projectToolConfig.getConnectionId());
				RallyToolConfig rallyToolConfig = createJiraToolConfig(projectToolConfig, jiraConnOpt);
				projectConfFieldMapping = createProjectConfFieldMapping(fieldMapping, projectBasicConfig, projectToolConfig,
						rallyToolConfig);
			}
		}
		return projectConfFieldMapping;
	}

	private RallyToolConfig createJiraToolConfig(ProjectToolConfig projectToolConfig, Optional<Connection> jiraConnOpt) {
		RallyToolConfig rallyToolConfig = new RallyToolConfig();
		// Todo: check the beanUtils func changed to import
		// org.springframework.beans.BeanUtils;
		BeanUtils.copyProperties(projectToolConfig, rallyToolConfig);

		if (jiraConnOpt.isPresent()) {

			rallyToolConfig.setConnection(jiraConnOpt);
		}
		return rallyToolConfig;
	}

	private ProjectConfFieldMapping createProjectConfFieldMapping(FieldMapping fieldMapping,
			ProjectBasicConfig projectConfig, ProjectToolConfig projectToolConfig, RallyToolConfig rallyToolConfig) {
		ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();

		if (projectConfig != null) {
			projectConfFieldMapping.setProjectBasicConfig(projectConfig);
			projectConfFieldMapping.setBasicProjectConfigId(projectConfig.getId());
			projectConfFieldMapping.setKanban(projectConfig.getIsKanban());
			projectConfFieldMapping.setBasicProjectConfigId(projectConfig.getId());
			projectConfFieldMapping.setProjectName(projectConfig.getProjectName());
		}

		if (rallyToolConfig != null) {
			projectConfFieldMapping.setJira(rallyToolConfig);
		}

		if (projectToolConfig != null) {
			projectConfFieldMapping.setProjectToolConfig(projectToolConfig);
			projectConfFieldMapping.setJiraToolConfigId(projectToolConfig.getId());
		}

		if (fieldMapping != null) {
			projectConfFieldMapping.setFieldMapping(fieldMapping);
		}

		return projectConfFieldMapping;
	}
}
