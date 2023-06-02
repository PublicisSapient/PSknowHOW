package com.publicissapient.kpidashboard.jira.config;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
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
	
	private List<String> projectsBasicConfigIds;

	@Override
	public Map<String, ProjectConfFieldMapping> fetchConfiguration(Boolean isKanban) {
		List<ProjectBasicConfig> allProjects = projectConfigRepository.findByKanban(isKanban);
		List<FieldMapping> fieldMappingList = fieldMappingRepository.findAll();

		List<String> selectedProjectsBasicIds = getProjectsBasicConfigIds();
		if (CollectionUtils.isEmpty(selectedProjectsBasicIds)) {
			return createProjectConfigMap(getRelevantProjects(allProjects), fieldMappingList);
		}

		List<ProjectBasicConfig> projectBasicConfigs = CollectionUtils.emptyIfNull(allProjects).stream().filter(
				projectBasicConfig -> selectedProjectsBasicIds.contains(projectBasicConfig.getId().toHexString()))
				.collect(Collectors.toList());
		log.info("ProjectBasicConfig: " + projectBasicConfigs);
		return createProjectConfigMap(projectBasicConfigs, fieldMappingList);
	}

	private List<String> getProjectsBasicConfigIds() {
		return projectsBasicConfigIds;
	}

	private Map<String, ProjectConfFieldMapping> createProjectConfigMap(List<ProjectBasicConfig> projectConfigList,
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
			//
		});
		return projectConfigMap;
	}

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

	private ObjectId getToolConfigId(ObjectId basicProjectConfigId) {
		List<ProjectToolConfig> boardsDetails = toolRepository
				.findByToolNameAndBasicProjectConfigId(ProcessorConstants.JIRA, basicProjectConfigId);
		return CollectionUtils.isNotEmpty(boardsDetails) ? boardsDetails.get(0).getId() : null;
	}
	
	private List<ProjectBasicConfig> getRelevantProjects(List<ProjectBasicConfig> projectConfigList) {
		List<ProjectBasicConfig> onlineJiraProjects = new ArrayList<>();
		for (ProjectBasicConfig config : projectConfigList) {
			List<ProjectToolConfig> jiraDetails = toolRepository
					.findByToolNameAndBasicProjectConfigId(ProcessorConstants.JIRA, config.getId());
			if (CollectionUtils.isNotEmpty(jiraDetails) && jiraDetails.get(0).getConnectionId() != null) {
				Optional<Connection> jiraConn = connectionRepository.findById(jiraDetails.get(0).getConnectionId());
				if (jiraConn.isPresent() && !jiraConn.get().isOffline()) {
					onlineJiraProjects.add(config);
				}
			}
		}

		return onlineJiraProjects;
	}

}
