package com.publicissapient.kpidashboard.jira.config;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
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
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

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
		List<ProjectBasicConfig> allProjects = projectConfigRepository.findByKanban(isKanban);
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
		ProjectBasicConfig projectBasicConfig = projectConfigRepository
				.findById(sprintDetails.getBasicProjectConfigId()).orElse(new ProjectBasicConfig());

		FieldMapping fieldMapping = fieldMappingRepository
				.findByBasicProjectConfigId(sprintDetails.getBasicProjectConfigId());
		List<ProjectToolConfig> projectToolConfigs = toolRepository
				.findByBasicProjectConfigId(sprintDetails.getBasicProjectConfigId());
		if (CollectionUtils.isNotEmpty(projectToolConfigs)) {
			ProjectToolConfig projectToolConfig = projectToolConfigs.get(0);
			if (null != projectToolConfig.getConnectionId()) {
				Optional<Connection> jiraConnOpt = connectionRepository.findById(projectToolConfig.getConnectionId());
				JiraToolConfig jiraToolConfig = createJiraToolConfig(projectToolConfig, jiraConnOpt);
				projectConfFieldMapping = createProjectConfFieldMapping(fieldMapping, projectBasicConfig,
						projectToolConfig, jiraToolConfig);
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
				.findByToolNameAndBasicProjectConfigId(ProcessorConstants.JIRA, projectConfigId);
		if (CollectionUtils.isNotEmpty(projectToolConfigs)) {
			ProjectToolConfig projectToolConfig = projectToolConfigs.get(0);
			if (null != projectToolConfig.getConnectionId()) {
				Optional<Connection> jiraConnOpt = connectionRepository.findById(projectToolConfig.getConnectionId());
				JiraToolConfig jiraToolConfig = createJiraToolConfig(projectToolConfig, jiraConnOpt);
				projectConfFieldMapping = createProjectConfFieldMapping(fieldMapping, projectBasicConfig,
						projectToolConfig, jiraToolConfig);
			}
		}
		return projectConfFieldMapping;
	}

	private JiraToolConfig createJiraToolConfig(ProjectToolConfig projectToolConfig, Optional<Connection> jiraConnOpt) {
		JiraToolConfig jiraToolConfig = new JiraToolConfig();
		try {
			BeanUtils.copyProperties(jiraToolConfig, projectToolConfig);
		} catch (IllegalAccessException | InvocationTargetException e) {
			log.error("Could not set JiraToolConfig", e);
		}
		if (jiraConnOpt.isPresent()) {

			jiraToolConfig.setConnection(jiraConnOpt);
		}
		return jiraToolConfig;
	}

	private ProjectConfFieldMapping createProjectConfFieldMapping(FieldMapping fieldMapping,
			ProjectBasicConfig projectConfig, ProjectToolConfig projectToolConfig, JiraToolConfig jiraToolConfig) {
		ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();

		if (projectConfig != null) {
			projectConfFieldMapping.setProjectBasicConfig(projectConfig);
			projectConfFieldMapping.setBasicProjectConfigId(projectConfig.getId());
			projectConfFieldMapping.setKanban(projectConfig.getIsKanban());
			projectConfFieldMapping.setBasicProjectConfigId(projectConfig.getId());
			projectConfFieldMapping.setProjectName(projectConfig.getProjectName());
		}

		if (jiraToolConfig != null) {
			projectConfFieldMapping.setJira(jiraToolConfig);
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
