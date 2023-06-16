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

package com.publicissapient.kpidashboard.jira.processor.mode;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.context.ExecutionLogContext;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

@Slf4j
public abstract class ModeBasedProcessor { // NOSONAR

	public ModeBasedProcessor() {
	}

	@Autowired
	private ProjectToolConfigRepository toolRepository;

	@Autowired
	private ConnectionRepository connectionRepository;

	private ExecutionLogContext executionLogContext;

	public ExecutionLogContext getExecutionLogContext() {
		return executionLogContext;
	}

	public void setExecutionLogContext(ExecutionLogContext executionLogContext) {
		this.executionLogContext = executionLogContext;
	}

	public void destroyLogContext() {
		if (this.executionLogContext != null) {
			this.executionLogContext.destroy();
			this.executionLogContext = null;
		}
	}

	/**
	 * Validate and Collects Issues and data
	 * 
	 * @param projectConfigList
	 *            projectConfiguration list
	 * @return Map of issueCount
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
			try {
				BeanUtils.copyProperties(projectConfFieldMapping, projectConfig);
				projectConfFieldMapping.setProjectBasicConfig(projectConfig);
				projectConfFieldMapping.setKanban(projectConfig.getIsKanban());
				projectConfFieldMapping.setBasicProjectConfigId(projectConfig.getId());
				projectConfFieldMapping.setJira(getJiraToolConfig(projectConfig.getId()));
				projectConfFieldMapping.setJiraToolConfigId(getToolConfigId(projectConfig.getId()));

			} catch (IllegalAccessException e) {
				log.error("Error while copying Project Config to ProjectConfFieldMapping", e);
			} catch (InvocationTargetException e) {
				log.error("Error while copying Project Config to ProjectConfFieldMapping invocation error", e);
			}
			CollectionUtils.emptyIfNull(fieldMappingList).stream()
					.filter(fieldMapping -> projectConfig.getId().equals(fieldMapping.getBasicProjectConfigId()))
					.forEach(fieldMapping -> projectConfFieldMapping.setFieldMapping(fieldMapping));
			projectConfigMap.putIfAbsent(projectConfig.getProjectName(), projectConfFieldMapping);
		});
		return projectConfigMap;
	}

	/**
	 * Gets the toolConfigId of the jira board tool
	 * 
	 * @param basicProjectConfigId
	 * @return toolConfigId for jira board
	 */
	private ObjectId getToolConfigId(ObjectId basicProjectConfigId) {
		List<ProjectToolConfig> boardsDetails = toolRepository
				.findByToolNameAndBasicProjectConfigId(ProcessorConstants.JIRA, basicProjectConfigId);
		return CollectionUtils.isNotEmpty(boardsDetails) ? boardsDetails.get(0).getId() : null;
	}

	/**
	 * This method gets JiraToolConfig
	 * 
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 * @return JiraTool Config
	 */
	private JiraToolConfig getJiraToolConfig(ObjectId basicProjectConfigId) {
		JiraToolConfig toolObj = new JiraToolConfig();
		List<ProjectToolConfig> jiraDetails = toolRepository
				.findByToolNameAndBasicProjectConfigId(ProcessorConstants.JIRA, basicProjectConfigId);
		if (CollectionUtils.isNotEmpty(jiraDetails)) {

			try {
				BeanUtils.copyProperties(toolObj, jiraDetails.get(0));
			} catch (IllegalAccessException | InvocationTargetException e) {
				log.error("Could not set JiraToolConfig", e);
			}
			if (jiraDetails.get(0).getConnectionId() != null) {
				Optional<Connection> conn = connectionRepository.findById(jiraDetails.get(0).getConnectionId());
				if (conn.isPresent()) {
					toolObj.setConnection(conn);
				}
			}
		}
		return toolObj;
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

}
