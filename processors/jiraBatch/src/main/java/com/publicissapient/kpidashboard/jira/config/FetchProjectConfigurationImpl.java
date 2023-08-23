package com.publicissapient.kpidashboard.jira.config;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
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
	public ProjectConfFieldMapping fetchConfiguration(String projectId) {
		ObjectId projectConfigId=new ObjectId(projectId);
		ProjectBasicConfig projectBasicConfig = projectConfigRepository.findById(projectConfigId).orElse(null);
//		List<ObjectId> projectConfigsIds = allProjects.stream().map(projConf -> projConf.getId())
//				.collect(Collectors.toList());
		FieldMapping fieldMapping = fieldMappingRepository.findByBasicProjectConfigId(projectConfigId);
//		Map<ObjectId, FieldMapping> basicConfigWiseFieldMapping = fieldMappingList.stream()
//				.collect(Collectors.toMap(FieldMapping::getBasicProjectConfigId, Function.identity()));

		List<ProjectToolConfig> projectToolConfigs = toolRepository.findByBasicProjectConfigId(projectConfigId);
		Map<ObjectId, ProjectToolConfig> basicConfigWiseConfig = projectToolConfigs.stream()
				.collect(Collectors.toMap(ProjectToolConfig::getBasicProjectConfigId, Function.identity()));

		List<Connection> jiraConnections = connectionRepository.findByType(ProcessorConstants.JIRA);
		Map<ObjectId, Connection> idWiseConnection = jiraConnections.stream()
				.collect(Collectors.toMap(Connection::getId, Function.identity()));

		ProjectToolConfig projToolConfig = basicConfigWiseConfig.get(projectBasicConfig.getId());
		Connection jiraConn = idWiseConnection.get(projToolConfig.getConnectionId());


			if (null != projToolConfig) {
				JiraToolConfig jiraToolConfig = createJiraToolConfig(projToolConfig, idWiseConnection);
				return createProjectConfFieldMapping(
						fieldMapping, projectBasicConfig, projToolConfig, jiraToolConfig);
			}

		log.info("ProjectBasicConfig: " + projectBasicConfig);
		return null;
	}

//	private List<String> getProjectsBasicConfigIds() {
//		return Arrays.asList("64ddd2fa367b2722eb69f941");
//	}
//
//	private Map<String, List<ProjectConfFieldMapping>> createProjectConfigMap(
//			List<ProjectBasicConfig> projectConfigList, Map<ObjectId, FieldMapping> basicConfigWiseFieldMapping,
//			Map<ObjectId, ProjectToolConfig> basicConfigWiseConfig, Map<ObjectId, Connection> idWiseConnection) {
//		Map<String, List<ProjectConfFieldMapping>> urlWiseprojectConfig = new HashMap<>();
//		CollectionUtils.emptyIfNull(projectConfigList).forEach(projectConfig -> {
//			ProjectToolConfig projectToolConfig = basicConfigWiseConfig.get(projectConfig.getId());
//			if (null != projectToolConfig) {
//				JiraToolConfig jiraToolConfig = createJiraToolConfig(projectToolConfig, idWiseConnection);
//				ProjectConfFieldMapping projectConfFieldMapping = createProjectConfFieldMapping(
//						fieldMapping, projectConfig, projectToolConfig, jiraToolConfig);
//				Optional<Connection> conn = jiraToolConfig.getConnection();
//				if (conn.isPresent()) {
//					urlWiseprojectConfig.computeIfAbsent(conn.get().getBaseUrl(), k -> new ArrayList<>())
//							.add(projectConfFieldMapping);
//				}
//			}
//		});
//		return urlWiseprojectConfig;
//	}
//
	private ProjectConfFieldMapping createProjectConfFieldMapping(
			FieldMapping fieldMapping, ProjectBasicConfig projectConfig,
			ProjectToolConfig projectToolConfig, JiraToolConfig jiraToolConfig) {
		ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
		projectConfFieldMapping.setProjectBasicConfig(projectConfig);
		projectConfFieldMapping.setBasicProjectConfigId(projectConfig.getId());
		projectConfFieldMapping.setKanban(projectConfig.getIsKanban());
		projectConfFieldMapping.setBasicProjectConfigId(projectConfig.getId());
		projectConfFieldMapping.setJira(jiraToolConfig);
		projectConfFieldMapping.setProjectToolConfig(projectToolConfig);
		projectConfFieldMapping.setJiraToolConfigId(projectToolConfig.getId());
		projectConfFieldMapping.setFieldMapping(fieldMapping);
		projectConfFieldMapping.setProjectName(projectConfig.getProjectName());
		return projectConfFieldMapping;
	}

	private JiraToolConfig createJiraToolConfig(ProjectToolConfig projectToolConfig,
			Map<ObjectId, Connection> idWiseConnection) {
		JiraToolConfig JiraToolConfig = new JiraToolConfig();
		try {
			BeanUtils.copyProperties(JiraToolConfig, projectToolConfig);
		} catch (IllegalAccessException | InvocationTargetException e) {
			log.error("Could not set JiraToolConfig", e);
		}
		if (projectToolConfig.getConnectionId() != null) {
			Optional<Connection> conn = Optional.of(idWiseConnection.get(projectToolConfig.getConnectionId()));
			if (conn.isPresent()) {
				JiraToolConfig.setConnection(conn);
			}
		}
		return JiraToolConfig;
	}
//
//	private List<ProjectBasicConfig> getRelevantProjects(List<ProjectBasicConfig> projectConfigList,
//			Map<ObjectId, ProjectToolConfig> basicConfigWiseConfig, Map<ObjectId, Connection> idWiseConnection) {
//		List<ProjectBasicConfig> onlineJiraProjects = new ArrayList<>();
//
//		for (ProjectBasicConfig config : projectConfigList) {
//			ProjectToolConfig projToolConfig = basicConfigWiseConfig.get(config.getId());
//			if (null != projToolConfig && null != projToolConfig.getConnectionId()) {
//				Connection jiraConn = idWiseConnection.get(projToolConfig.getConnectionId());
//				if (null != jiraConn && !jiraConn.isOffline()) {
//					onlineJiraProjects.add(config);
//				}
//			}
//		}
//
//		return onlineJiraProjects;
//	}

}
