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

package com.publicissapient.kpidashboard.apis.projectconfig.basic.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.publicissapient.kpidashboard.apis.abac.ProjectAccessManager;
import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.capacity.service.CapacityMasterService;
import com.publicissapient.kpidashboard.apis.cleanup.ToolDataCleanUpService;
import com.publicissapient.kpidashboard.apis.cleanup.ToolDataCleanUpServiceFactory;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.KpiDataCacheService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.errors.ProjectNotFoundException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.projectconfig.basic.model.HierarchyResponseDTO;
import com.publicissapient.kpidashboard.apis.projectconfig.fieldmapping.service.FieldMappingService;
import com.publicissapient.kpidashboard.apis.projectconfig.projecttoolconfig.service.ProjectToolConfigServiceImpl;
import com.publicissapient.kpidashboard.apis.rbac.accessrequests.service.AccessRequestsHelperService;
import com.publicissapient.kpidashboard.apis.repotools.service.RepoToolsConfigServiceImpl;
import com.publicissapient.kpidashboard.apis.testexecution.service.TestExecutionService;
import com.publicissapient.kpidashboard.apis.userboardconfig.service.UserBoardConfigService;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.HierarchyValue;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.application.dto.ProjectBasicConfigDTO;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.BoardMetadata;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.rbac.AccessRequest;
import com.publicissapient.kpidashboard.common.model.rbac.ProjectBasicConfigNode;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
import com.publicissapient.kpidashboard.common.repository.jira.BoardMetadataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.HappinessKpiDataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author narsingh9
 *
 */
@Service
@Slf4j
public class ProjectBasicConfigServiceImpl implements ProjectBasicConfigService {

	@Autowired
	private ProjectBasicConfigRepository basicConfigRepository;

	@Autowired
	private FilterHelperService filterHelperService;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private KpiDataCacheService kpiDataCacheService;

	@Autowired
	private UserAuthorizedProjectsService userAuthorizedProjectsService;

	@Autowired
	private TokenAuthenticationService tokenAuthenticationService;

	@Autowired
	private FieldMappingService fieldMappingService;

	@Autowired
	private ProjectToolConfigRepository toolRepository;

	@Autowired
	private ToolDataCleanUpServiceFactory dataCleanUpServiceFactory;

	@Autowired
	private DeleteProjectTraceLogService deleteProjectTraceLogService;

	@Autowired
	private BoardMetadataRepository boardMetadataRepository;

	@Autowired
	private AccessRequestsHelperService accessRequestsHelperService;

	@Autowired
	private ProjectAccessManager projectAccessManager;

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private SprintRepository sprintRepository;

	@Autowired
	private CapacityMasterService capacityMasterService;

	@Autowired
	private TestExecutionService testExecutionService;

	@Autowired
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

	@Autowired
	private AssigneeDetailsRepository assigneeDetailsRepository;

	@Autowired
	private RepoToolsConfigServiceImpl repoToolsConfigService;

	@Autowired
	private HappinessKpiDataRepository happinessKpiDataRepository;

	@Autowired
	private ProjectToolConfigServiceImpl projectToolConfigService;

	@Autowired
	private UserBoardConfigService userBoardConfigService;

	/**
	 * method to save basic configuration
	 *
	 * @param projectBasicConfigDTO
	 *            object to be saved
	 * @return ServiceResponse object
	 */
	@Override
	public ServiceResponse addBasicConfig(ProjectBasicConfigDTO projectBasicConfigDTO) {
		ServiceResponse response;
		ProjectBasicConfig basicConfig = basicConfigRepository
				.findByProjectName(projectBasicConfigDTO.getProjectName());
		String username = authenticationService.getLoggedInUser();
		if (basicConfig != null) {
			response = new ServiceResponse(false, "Try with different Project name.", null);
		} else {
			ModelMapper mapper = new ModelMapper();
			basicConfig = mapper.map(projectBasicConfigDTO, ProjectBasicConfig.class);
			basicConfig.setCreatedAt(DateUtil.dateTimeFormatter(LocalDateTime.now(), DateUtil.TIME_FORMAT));
			basicConfig.setCreatedBy(authenticationService.getLoggedInUser());
			tokenAuthenticationService.updateExpiryDate(username, LocalDateTime.now().toString());
			String accessRoleOfParent = projectAccessManager.getAccessRoleOfNearestParent(basicConfig, username);

			if (accessRoleOfParent == null) {

				ProjectBasicConfig savedProjectBasicConfig = saveBasicConfig(basicConfig);
				cloneProjectToolConfigAndDependencies(savedProjectBasicConfig);
				if (!projectAccessManager.getUserInfo(username).getAuthorities().contains(Constant.ROLE_SUPERADMIN)) {
					addNewProjectIntoUserInfo(savedProjectBasicConfig, username);
				}
				performFilterOperation(basicConfigDtoCreation(savedProjectBasicConfig, mapper), false);
				response = new ServiceResponse(true, "Added Successfully.", savedProjectBasicConfig);

			} else if (Constant.ROLE_SUPERADMIN.equals(accessRoleOfParent)
					|| Constant.ROLE_PROJECT_ADMIN.equals(accessRoleOfParent)) {
				ProjectBasicConfig savedProjectBasicConfig = saveBasicConfig(basicConfig);
				cloneProjectToolConfigAndDependencies(savedProjectBasicConfig);
				performFilterOperation(basicConfigDtoCreation(savedProjectBasicConfig, mapper), false);
				response = new ServiceResponse(true, "Added Successfully.", savedProjectBasicConfig);

			} else {
				response = new ServiceResponse(false,
						"You do not have admin access to any of selected hierarchy levels", basicConfig);

			}
		}
		return response;
	}

	/**
	 * Clone Tool Configurations, Field Mappings, and Board Metadata for Cloned
	 * Project
	 *
	 * @param savedProjectBasicConfig
	 *            savedProjectBasicConfig
	 */
	private void cloneProjectToolConfigAndDependencies(ProjectBasicConfig savedProjectBasicConfig) {
		if (savedProjectBasicConfig.getClonedFrom() == null) {
			return;
		}

		// Step 1: Clone tool configurations
		List<ProjectToolConfig> clonedToolConfigs = cloneToolConfigurations(savedProjectBasicConfig);

		// Step 2: Clone field mappings
		cloneFieldMapping(savedProjectBasicConfig, clonedToolConfigs);

		// Step 3: Clone board metadata
		cloneBoardMetadata(savedProjectBasicConfig, clonedToolConfigs);
	}

	private List<ProjectToolConfig> cloneToolConfigurations(ProjectBasicConfig savedProjectBasicConfig) {
		List<ProjectToolConfig> toolConfigList = projectToolConfigService
				.getProjectToolConfigsByProjectId(savedProjectBasicConfig.getClonedFrom());
		List<ProjectToolConfig> clonedToolConfigs = new ArrayList<>();
		List<String> scmToolList = Arrays.asList(Constant.TOOL_GITHUB, Constant.TOOL_GITLAB, Constant.TOOL_BITBUCKET,
				Constant.TOOL_AZUREREPO);
		for (ProjectToolConfig toolConfig : toolConfigList) {
			if (scmToolList.contains(toolConfig.getToolName()) && savedProjectBasicConfig.isDeveloperKpiEnabled())
				continue;
			try {
				ProjectToolConfig clonedToolConfig = toolConfig.clone();
				clonedToolConfig.setId(null);
				clonedToolConfig.setBasicProjectConfigId(savedProjectBasicConfig.getId());
				clonedToolConfig
						.setCreatedAt(DateUtil.dateTimeFormatter(LocalDateTime.now(), CommonConstant.TIME_FORMAT));
				clonedToolConfig.setCreatedBy(authenticationService.getLoggedInUser());
				clonedToolConfig
						.setUpdatedAt(DateUtil.dateTimeFormatter(LocalDateTime.now(), CommonConstant.TIME_FORMAT));
				clonedToolConfig.setProjectId(savedProjectBasicConfig.getId().toString());
				clonedToolConfigs.add(clonedToolConfig);
			} catch (CloneNotSupportedException e) {
				log.error("Error in cloning ProjectToolConfig", e);
			}
		}

		return projectToolConfigService.saveProjectToolConfigs(clonedToolConfigs);
	}

	private void cloneFieldMapping(ProjectBasicConfig savedProjectBasicConfig,
			List<ProjectToolConfig> clonedToolConfigs) {
		FieldMapping originalFieldMapping = fieldMappingService
				.getFieldMappingByBasicconfigId(savedProjectBasicConfig.getClonedFrom().toString());
		Optional<ProjectToolConfig> toolConfigOptional = clonedToolConfigs.stream().filter(
				tool -> Constant.TOOL_JIRA.equals(tool.getToolName()) || Constant.TOOL_AZURE.equals(tool.getToolName()))
				.findFirst();

		if (originalFieldMapping != null && toolConfigOptional.isPresent()) {
			try {
				FieldMapping clonedFieldMapping = originalFieldMapping.clone();
				clonedFieldMapping.setId(null);
				clonedFieldMapping.setProjectToolConfigId(toolConfigOptional.get().getId());
				clonedFieldMapping.setBasicProjectConfigId(savedProjectBasicConfig.getId());
				clonedFieldMapping
						.setUpdatedAt(DateUtil.dateTimeFormatter(LocalDateTime.now(), CommonConstant.TIME_FORMAT));
				clonedFieldMapping.setUpdatedBy(authenticationService.getLoggedInUser());
				clonedFieldMapping.setProjectId(savedProjectBasicConfig.getId().toString());
				fieldMappingService.saveFieldMapping(clonedFieldMapping);
			} catch (CloneNotSupportedException e) {
				log.error("Error in cloning FieldMapping", e);
			}
		}
	}

	private void cloneBoardMetadata(ProjectBasicConfig savedProjectBasicConfig,
			List<ProjectToolConfig> clonedToolConfigs) {
		BoardMetadata originalBoardMetadata = boardMetadataRepository
				.findByProjectBasicConfigId(savedProjectBasicConfig.getClonedFrom());
		Optional<ProjectToolConfig> toolConfigOptional = clonedToolConfigs.stream().filter(
				tool -> Constant.TOOL_JIRA.equals(tool.getToolName()) || Constant.TOOL_AZURE.equals(tool.getToolName()))
				.findFirst();

		if (originalBoardMetadata != null && toolConfigOptional.isPresent()) {
			try {
				BoardMetadata clonedBoardMetadata = originalBoardMetadata.clone();
				clonedBoardMetadata.setId(null);
				clonedBoardMetadata.setProjectToolConfigId(toolConfigOptional.get().getId());
				clonedBoardMetadata.setProjectBasicConfigId(savedProjectBasicConfig.getId());
				boardMetadataRepository.save(clonedBoardMetadata);
			} catch (CloneNotSupportedException e) {
				log.error("Error in cloning BoardMetadata", e);
			}
		}
	}

	private void addNewProjectIntoUserInfo(ProjectBasicConfig basicConfig, String username) {
		projectAccessManager.addNewProjectIntoUserInfo(basicConfig, username);
	}

	private ProjectBasicConfig saveBasicConfig(ProjectBasicConfig basicConfig) {
		return basicConfigRepository.save(basicConfig);
	}

	/**
	 * method to update basic configuration
	 *
	 * @param projectBasicConfigDTO
	 *            for updation
	 * @return ServiceResponse object
	 */
	@Override
	public ServiceResponse updateBasicConfig(String basicConfigId, ProjectBasicConfigDTO projectBasicConfigDTO) {
		ServiceResponse response;
		Optional<ProjectBasicConfig> savedConfigOpt = basicConfigRepository.findById(new ObjectId(basicConfigId));
		ProjectBasicConfig diffIdSameName = basicConfigRepository
				.findByProjectNameAndIdNot(projectBasicConfigDTO.getProjectName(), new ObjectId(basicConfigId));
		if (savedConfigOpt.isPresent()) {
			if (!Optional.ofNullable(diffIdSameName).isPresent()) {
				ProjectBasicConfig savedConfig = savedConfigOpt.get();
				ModelMapper mapper = new ModelMapper();
				ProjectBasicConfig basicConfig = mapper.map(projectBasicConfigDTO, ProjectBasicConfig.class);
				if (isAssigneeUpdated(basicConfig, savedConfig)) {
					List<ProcessorExecutionTraceLog> traceLogs = processorExecutionTraceLogRepository
							.findByProcessorNameAndBasicProjectConfigIdIn(ProcessorConstants.JIRA,
									Collections.singletonList(basicConfigId));
					if (!traceLogs.isEmpty()) {
						for (ProcessorExecutionTraceLog traceLog : traceLogs) {
							if (traceLog != null) {
								traceLog.setLastSuccessfulRun(null);
								traceLog.setLastSavedEntryUpdatedDateByType(new HashMap<>());
							}
						}
						processorExecutionTraceLogRepository.saveAll(traceLogs);
					}
					AssigneeDetails assigneeDetails = assigneeDetailsRepository
							.findByBasicProjectConfigId(basicConfigId);
					if (assigneeDetails != null) {
						assigneeDetailsRepository.delete(assigneeDetails);
					}
				}
				basicConfig.setCreatedAt(savedConfig.getCreatedAt());
				basicConfig.setUpdatedAt(DateUtil.dateTimeFormatter(LocalDateTime.now(), DateUtil.TIME_FORMAT));
				basicConfig.setUpdatedBy(authenticationService.getLoggedInUser());
				ProjectBasicConfig updatedBasicConfig = basicConfigRepository.save(basicConfig);
				performFilterOperation(basicConfigDtoCreation(updatedBasicConfig, mapper), true);
				//clear kpi data cache for the project for all KPIs
				kpiDataCacheService.clearCacheForProject(basicConfigId);
				response = new ServiceResponse(true, "Updated Successfully.", updatedBasicConfig);
			} else {
				response = new ServiceResponse(false, "Try with different project name.", null);
			}
		} else {
			response = new ServiceResponse(false, "Basic Config with id " + basicConfigId + " not present.", null);
		}
		return response;
	}

	private boolean isAssigneeUpdated(ProjectBasicConfig unsavedBasicConfig, ProjectBasicConfig savedConfig) {

		return unsavedBasicConfig.isSaveAssigneeDetails() != savedConfig.isSaveAssigneeDetails();
	}

	/**
	 * method to perform filter operation
	 *
	 * @param basicConfig
	 * @param filterCleanRequired
	 */
	private void performFilterOperation(final ProjectBasicConfigDTO basicConfig, boolean filterCleanRequired) {
		if (filterCleanRequired) {
			filterHelperService.cleanFilterData(basicConfig);
		}
		filterHelperService.filterCreation(basicConfig);
		clearCache(basicConfig);
	}

	/**
	 * Clears filter and jira related cache.
	 *
	 * @param basicConfig
	 *            ProjectConfig
	 */
	private void clearCache(final ProjectBasicConfigDTO basicConfig) {
		if (basicConfig.getIsKanban()) {
			cacheService.clearCache(CommonConstant.CACHE_ACCOUNT_HIERARCHY_KANBAN);
			cacheService.clearCache(CommonConstant.JIRAKANBAN_KPI_CACHE);
		} else {
			cacheService.clearCache(CommonConstant.JIRA_KPI_CACHE);
			cacheService.clearCache(CommonConstant.CACHE_ACCOUNT_HIERARCHY);
		}
		cacheService.clearCache(CommonConstant.CACHE_PROJECT_CONFIG_MAP);
		cacheService.clearCache(CommonConstant.CACHE_PROJECT_BASIC_TREE);
		if (basicConfig.getClonedFrom() != null) {
			cacheService.clearCache(CommonConstant.CACHE_FIELD_MAPPING_MAP);
			cacheService.clearCache(CommonConstant.CACHE_PROJECT_TOOL_CONFIG);
			cacheService.clearCache(CommonConstant.CACHE_PROJECT_TOOL_CONFIG_MAP);
			cacheService.clearCache(CommonConstant.CACHE_BOARD_META_DATA_MAP);
		}
	}

	/**
	 * method to add required fields for filter operation
	 *
	 * @param basicConfig
	 * @param mapper
	 */
	private ProjectBasicConfigDTO basicConfigDtoCreation(final ProjectBasicConfig basicConfig, ModelMapper mapper) {
		return mapper.map(basicConfig, ProjectBasicConfigDTO.class);
	}

	@Override
	public ProjectBasicConfig getProjectBasicConfigs(String basicProjectConfigId) {
		Optional<ProjectBasicConfig> config = Optional.empty();
		ProjectBasicConfig projectBasicConfig = null;
		if (!StringUtils.isBlank(basicProjectConfigId)) {
			if (userAuthorizedProjectsService.ifSuperAdminUser()) {
				config = basicConfigRepository.findById(new ObjectId(basicProjectConfigId));
			} else {
				Set<String> basicProjectConfigIds = tokenAuthenticationService.getUserProjects();
				if (Optional.ofNullable(basicProjectConfigIds).isPresent()
						&& basicProjectConfigIds.contains(basicProjectConfigId)) {
					config = basicConfigRepository.findById(new ObjectId(basicProjectConfigId));
				}
			}
		}
		log.info("For projectId: {} : Returning getProjectBasicConfig response: {}", basicProjectConfigId, config);
		if (config.isPresent()) {
			projectBasicConfig = config.get();
			projectBasicConfigSortedBasedOnHierarchyLevel(projectBasicConfig);
		}
		return projectBasicConfig;
	}

	@Override
	public void projectBasicConfigSortedBasedOnHierarchyLevel(ProjectBasicConfig projectBasicConfig) {
		List<HierarchyValue> sortedHierarchy = CollectionUtils.emptyIfNull(projectBasicConfig.getHierarchy()).stream()
				.sorted(Comparator
						.comparing((HierarchyValue hierarchyValue) -> hierarchyValue.getHierarchyLevel().getLevel()))
				.collect(Collectors.toList());
		projectBasicConfig.setHierarchy(sortedHierarchy);
	}

	@Override
	public List<ProjectBasicConfig> getAllProjectsBasicConfigs() {
		List<ProjectBasicConfig> list = new ArrayList<>();
		List<ProjectBasicConfig> configListSortBasedOnLevels = new ArrayList<>();
		if (userAuthorizedProjectsService.ifSuperAdminUser()) {
			list.addAll(basicConfigRepository.findAll());
		} else {
			Set<String> basicProjectConfigIds = tokenAuthenticationService.getUserProjects();
			if (Optional.ofNullable(basicProjectConfigIds).isPresent()) {
				Set<ObjectId> configIds = new HashSet<>();
				for (String id : basicProjectConfigIds) {
					configIds.add(new ObjectId(id));
				}
				list.addAll(basicConfigRepository.findByIdIn(configIds));

			}
		}
		if (CollectionUtils.isNotEmpty(list)) {
			list.stream().forEach(projectBasicConfig -> {
				projectBasicConfigSortedBasedOnHierarchyLevel(projectBasicConfig);
				configListSortBasedOnLevels.add(projectBasicConfig);
			});
		}
		log.info("Returning getProjectBasicConfig response: {}", list);
		return list;
	}

	@Override
	public List<ProjectBasicConfig> getAllProjectsBasicConfigsWithoutPermission() {
		return basicConfigRepository.findAll();
	}

	@Override
	public ProjectBasicConfig deleteProject(String basicProjectConfigId) {
		ProjectBasicConfig projectBasicConfig = getProjectBasicConfigs(basicProjectConfigId);
		if (projectBasicConfig == null) {
			String errorMsg = "No project found with id " + basicProjectConfigId;
			log.error(errorMsg);
			throw new ProjectNotFoundException(errorMsg);
		} else {
			deleteToolsAndCleanData(projectBasicConfig);
			deleteFilterData(projectBasicConfig);
			deleteFieldMappingAndBoardMetadata(projectBasicConfig);
			deleteUploadData(projectBasicConfig);
			deleteBasicConfig(projectBasicConfig);
			removeProjectUserInfo(projectBasicConfig);
			rejectAccessRequestsWithProject(projectBasicConfig);
			deleteSprintDetailsData(projectBasicConfig);
			deleteAssigneeDetails(projectBasicConfig);
			deleteHappinessKpiDetails(projectBasicConfig);
			addToTraceLog(projectBasicConfig);
			deleteProjectBoardConfig(projectBasicConfig);
			cleanAllCache();

		}

		return projectBasicConfig;
	}

	private void deleteHappinessKpiDetails(ProjectBasicConfig projectBasicConfig) {
		happinessKpiDataRepository.deleteByBasicProjectConfigId(projectBasicConfig.getId());
	}

	private void deleteAssigneeDetails(ProjectBasicConfig projectBasicConfig) {
		AssigneeDetails assigneeDetails = assigneeDetailsRepository
				.findByBasicProjectConfigId(projectBasicConfig.getId().toString());
		if (assigneeDetails != null) {
			assigneeDetailsRepository.delete(assigneeDetails);
		}
	}

	public void deleteRepoToolProject(ProjectBasicConfig projectBasicConfig, Boolean isRepoTool) {

		if (isRepoTool.equals(Boolean.TRUE)) {
			repoToolsConfigService.deleteRepoToolProject(projectBasicConfig, false);
		}

	}

	private void rejectAccessRequestsWithProject(ProjectBasicConfig projectBasicConfig) {
		log.info("removing project [{}, {}] from project access requests", projectBasicConfig.getProjectName(),
				projectBasicConfig.getId().toHexString());

		List<AccessRequest> accessRequests = accessRequestsHelperService
				.getAccessRequestsByProject(projectBasicConfig.getId().toHexString());
		accessRequests
				.forEach(accessRequest -> projectAccessManager.rejectAccessRequest(accessRequest.getId().toHexString(),
						"Rejected! due to project (" + projectBasicConfig.getProjectName() + ") was deleted", null));

	}

	private void deleteFieldMappingAndBoardMetadata(ProjectBasicConfig projectBasicConfig) {
		fieldMappingService.deleteByBasicProjectConfigId(projectBasicConfig.getId());
		boardMetadataRepository.deleteByProjectBasicConfigId(projectBasicConfig.getId());
	}

	private void deleteUploadData(ProjectBasicConfig projectBasicConfig) {
		capacityMasterService.deleteCapacityByProject(projectBasicConfig.getIsKanban(), projectBasicConfig.getId());
		testExecutionService.deleteTestExecutionByProject(projectBasicConfig.getIsKanban(),
				projectBasicConfig.getId().toHexString());
	}

	private void deleteFilterData(ProjectBasicConfig projectBasicConfig) {
		filterHelperService.deleteAccountHierarchiesOfProject(projectBasicConfig.getId(),
				projectBasicConfig.getIsKanban());
	}

	private void deleteToolsAndCleanData(ProjectBasicConfig projectBasicConfig) {
		List<String> scmToolList = Arrays.asList(ProcessorConstants.BITBUCKET, ProcessorConstants.GITLAB,
				ProcessorConstants.GITHUB, ProcessorConstants.AZUREREPO);
		List<ProjectToolConfig> tools = toolRepository.findByBasicProjectConfigId(projectBasicConfig.getId());
		Boolean isRepoTool = tools.stream()
				.anyMatch(toolConfig -> scmToolList.contains(toolConfig.getToolName())
						&& projectBasicConfig.isDeveloperKpiEnabled());
		deleteRepoToolProject(projectBasicConfig, isRepoTool);
		CollectionUtils.emptyIfNull(tools).forEach(tool -> {

			ToolDataCleanUpService dataCleanUpService = dataCleanUpServiceFactory.getService(tool.getToolName());
			dataCleanUpService.clean(tool.getId().toHexString());
			deleteTool(tool);
			log.info("{} tool with id {} deleted" + tool.getToolName(), tool.getId().toHexString());

		});
	}

	private void deleteTool(ProjectToolConfig tool) {
		toolRepository.deleteById(tool.getId());
	}

	private void deleteBasicConfig(ProjectBasicConfig projectBasicConfig) {
		basicConfigRepository.delete(projectBasicConfig);
		log.info("Project {} with id {} deleted", projectBasicConfig.getProjectName(),
				projectBasicConfig.getId().toHexString());
	}

	private void removeProjectUserInfo(ProjectBasicConfig projectBasicConfig) {
		projectAccessManager.removeProjectAccessFromAllUsers(projectBasicConfig.getId().toHexString());
	}

	private void addToTraceLog(ProjectBasicConfig projectBasicConfig) {
		deleteProjectTraceLogService.save(projectBasicConfig);
		log.info("traceLog saved for project {}", projectBasicConfig.getProjectName());
	}

	private void deleteProjectBoardConfig(ProjectBasicConfig projectBasicConfig) {
		log.info("Deleting project board config for project: {}", projectBasicConfig.getProjectName());
		userBoardConfigService.deleteProjectBoardConfig(projectBasicConfig.getId().toString());
	}

	private void cleanAllCache() {
		cacheService.clearAllCache();
		log.info("cache cleared");

	}

	private void deleteSprintDetailsData(ProjectBasicConfig projectBasicConfig) {
		sprintRepository.deleteByBasicProjectConfigId(projectBasicConfig.getId());
	}

	/**
	 * Service to fetch the list of all project basic configuration
	 * 
	 * @return {@code List<ProjectBasicConfigDTO>} : empty list incase no data found
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<ProjectBasicConfigDTO> getAllProjectsBasicConfigsDTOWithoutPermission() {
		List<ProjectBasicConfigDTO> projectBasicList = Lists.newArrayList();
		Map<String, ProjectBasicConfig> basicConfigMap = (Map<String, ProjectBasicConfig>) cacheService
				.cacheProjectConfigMapData();

		if (MapUtils.isNotEmpty(basicConfigMap)) {
			List<ProjectBasicConfig> projectList = new ArrayList<>(basicConfigMap.values());
			ModelMapper mapper = new ModelMapper();
			projectBasicList = projectList.stream().map(pbc -> mapper.map(pbc, ProjectBasicConfigDTO.class))
					.collect(Collectors.toList());
		}
		return projectBasicList;
	}

	/**
	 * Service to fetch the map of all project basic configuration
	 * 
	 * @return {@code Map<String, ProjectBasicConfigDTO>} : empty map incase no data
	 *         found
	 */
	public Map<String, ProjectBasicConfigDTO> getBasicConfigsDTOMapWithoutPermission() {
		List<ProjectBasicConfigDTO> dtoList = getAllProjectsBasicConfigsDTOWithoutPermission();
		Map<String, ProjectBasicConfigDTO> map = Maps.newHashMap();
		if (CollectionUtils.isNotEmpty(dtoList)) {
			dtoList.forEach(basicConfig -> map.put(basicConfig.getId().toString(), basicConfig));
		}
		return map;
	}

	/**
	 * Get all basic config, create tree and return root node
	 */
	public ProjectBasicConfigNode getBasicConfigTree() {
		Set<ProjectBasicConfigNode> basicConfigs = getBasicConfigNodes();
		ProjectBasicConfigNode projectBasicConfigNode = new ProjectBasicConfigNode();
		if (CollectionUtils.isNotEmpty(basicConfigs)) {
			projectBasicConfigNode = createTree(basicConfigs);
		}

		return projectBasicConfigNode;

	}

	private Set<ProjectBasicConfigNode> getBasicConfigNodes() {
		List<ProjectBasicConfig> projectBasicConfigs = basicConfigRepository.findAll();
		Set<ProjectBasicConfigNode> projectBasicConfigNodes = new LinkedHashSet<>();
		if (CollectionUtils.isNotEmpty(projectBasicConfigs)) {

			ProjectBasicConfigNode projectBasicConfigRootNode = new ProjectBasicConfigNode();
			projectBasicConfigRootNode.setValue(Filters.ROOT.name());
			projectBasicConfigRootNode.setParent(null);
			projectBasicConfigRootNode.setGroupName(Filters.ROOT.name());
			projectBasicConfigRootNode.setChildren(new ArrayList<>());
			projectBasicConfigNodes.add(projectBasicConfigRootNode);
			for (ProjectBasicConfig projectBasicConfig : projectBasicConfigs) {
				ProjectBasicConfigNode projectBasicConfigLevelWiseNode = projectBasicConfigRootNode;
				if (CollectionUtils.isNotEmpty(projectBasicConfig.getHierarchy())) {
					for (HierarchyValue hierarchyValue : projectBasicConfig.getHierarchy()) {
						HierarchyLevel hierarchyLevel = hierarchyValue.getHierarchyLevel();
						ProjectBasicConfigNode projectBasicHierarchyNode = createProjectBasicNode(
								hierarchyValue.getValue(),
								new ArrayList<>(Arrays.asList(projectBasicConfigLevelWiseNode)),
								hierarchyLevel.getHierarchyLevelId());
						projectBasicConfigNodes.add(projectBasicHierarchyNode);
						projectBasicConfigLevelWiseNode = projectBasicHierarchyNode;
					}
				}
				ProjectBasicConfigNode projectBasicProjNode = createProjectBasicNode(
						projectBasicConfig.getId().toString(),
						new ArrayList<>(Arrays.asList(projectBasicConfigLevelWiseNode)), Filters.PROJECT.name());
				projectBasicConfigNodes.add(projectBasicProjNode);
			}
		}
		return projectBasicConfigNodes;
	}

	private ProjectBasicConfigNode createProjectBasicNode(String value, List<ProjectBasicConfigNode> parent,
			String groupName) {
		ProjectBasicConfigNode projectBasicNode = new ProjectBasicConfigNode();
		projectBasicNode.setValue(value);
		projectBasicNode.setParent(parent);
		projectBasicNode.setGroupName(groupName);
		projectBasicNode.setChildren(new ArrayList<>());
		return projectBasicNode;
	}

	private ProjectBasicConfigNode createTree(Set<ProjectBasicConfigNode> nodes) {

		Map<String, ProjectBasicConfigNode> mapTmp = new HashMap<>();

		// Save all nodes to a map
		for (ProjectBasicConfigNode current : nodes) {
			mapTmp.put(current.getValue() + "-" + current.getGroupName(), current);
		}

		// loop and assign parent/child relationships
		for (ProjectBasicConfigNode current : nodes) {
			List<ProjectBasicConfigNode> parentConfig = current.getParent();
			if (CollectionUtils.isNotEmpty(parentConfig)) {
				List<ProjectBasicConfigNode> finalParentNodes = new ArrayList<>();
				for (ProjectBasicConfigNode parent : parentConfig) {
					String parentValue = parent.getValue();
					String groupName = parent.getGroupName();
					if (mapTmp.containsKey(parentValue + "-" + groupName)) {
						ProjectBasicConfigNode parentNode = mapTmp.get(parentValue + "-" + groupName);
						parentNode.addChild(current);
						finalParentNodes.add(parentNode);
						mapTmp.put(parentValue + "-" + groupName, parentNode);
						mapTmp.put(current.getValue() + "-" + current.getGroupName(), current);
					}
					current.setParent(finalParentNodes);
				}
			}
		}

		// get the root
		ProjectBasicConfigNode root = null;
		for (ProjectBasicConfigNode node : mapTmp.values()) {
			if (node.getParent() == null) {
				root = node;
				break;
			}
		}

		return root;
	}

	/**
	 * Method to find out the node from a project basic config tree
	 */
	public ProjectBasicConfigNode findNode(ProjectBasicConfigNode node, String searchValue, String groupName) {
		if (node.getValue().equalsIgnoreCase(searchValue) && node.getGroupName().equalsIgnoreCase(groupName)) {
			return node;
		}
		ProjectBasicConfigNode result = null;
		for (ProjectBasicConfigNode child : node.getChildren()) {
			result = findNode(child, searchValue, groupName);
			if (result != null)
				return result;
		}
		return result;
	}

	/**
	 * Method to find out all the child nodes of a tree
	 */
	public void findChildren(ProjectBasicConfigNode node, List<ProjectBasicConfigNode> children) {
		for (ProjectBasicConfigNode child : node.getChildren()) {
			children.add(child);
			findChildren(child, children);
		}
	}

	/**
	 * Method to find out all the parent nodes of a tree
	 */
	public void findParents(List<ProjectBasicConfigNode> nodes, List<ProjectBasicConfigNode> parents) {
		if (CollectionUtils.isNotEmpty(nodes)) {
			for (ProjectBasicConfigNode node : nodes) {
				if (node.getParent() != null) {
					parents.addAll(node.getParent());
					findParents(node.getParent(), parents);
				}
			}
		}
	}

	/**
	 * Method to find out all the leafnode of a tree
	 */
	public void findLeaf(ProjectBasicConfigNode node, List<ProjectBasicConfigNode> leafNodes) {
		if (node.getChildren().isEmpty()) {
			leafNodes.add(node);
		}
		for (ProjectBasicConfigNode child : node.getChildren()) {
			findLeaf(child, leafNodes);
		}
	}

	@Override
	public List<HierarchyResponseDTO> getHierarchyData() {
		List<ProjectBasicConfigDTO> basicConfigDTOS = getAllProjectsBasicConfigsDTOWithoutPermission();
		List<ProjectBasicConfigDTO> scrumProjectBasicConfigList = basicConfigDTOS.stream()
				.filter(projectBasicConfigDTO -> !projectBasicConfigDTO.getIsKanban()).collect(Collectors.toList());
		Map<ObjectId, List<SprintDetails>> groupedByProject = getTop5SprintDetailsGroupedByProject(
				scrumProjectBasicConfigList.stream().map(ProjectBasicConfigDTO::getId).toList());
		List<HierarchyResponseDTO> hierarchyResponseDTOS = new ArrayList<>();
		for (ProjectBasicConfigDTO projectBasicConfig : scrumProjectBasicConfigList) {
			HierarchyResponseDTO dto = new HierarchyResponseDTO();
			dto.setProjectId(projectBasicConfig.getId().toString());
			dto.setProjectName(projectBasicConfig.getProjectName());
			projectBasicConfig.getHierarchy().forEach(hirarchy -> {
				int level = hirarchy.getHierarchyLevel().getLevel();
				String value = hirarchy.getValue();
				switch (level) {
				case 1 -> dto.setHierarchyLevelOne(value);
				case 2 -> dto.setHierarchyLevelTwo(value);
				case 3 -> dto.setHierarchyLevelThree(value);
				default -> dto.setHierarchyLevelFour(value);
				}
			});
			dto.setSprintDetailsList(groupedByProject.getOrDefault(projectBasicConfig.getId(), new ArrayList<>()));
			hierarchyResponseDTOS.add(dto);
		}
		return hierarchyResponseDTOS.stream().sorted(Comparator.comparing(HierarchyResponseDTO::getProjectName))
				.collect(Collectors.toList());
	}

	public Map<ObjectId, List<SprintDetails>> getTop5SprintDetailsGroupedByProject(
			List<ObjectId> basicProjectConfigIds) {
		List<String> sprintStatusList = new ArrayList<>();
		sprintStatusList.add(SprintDetails.SPRINT_STATE_CLOSED);
		sprintStatusList.add(SprintDetails.SPRINT_STATE_CLOSED.toLowerCase());
		List<SprintDetails> sprintDetailsList = sprintRepository
				.findByBasicProjectConfigIdInAndStateInOrderByStartDateASC(basicProjectConfigIds,
						sprintStatusList);

		// Sort by beginDate in descending order

		sprintDetailsList.sort((s1, s2) -> s2.getStartDate().compareTo(s1.getStartDate()));
		// Group by basicProjectConfigId and limit to top 5
		return sprintDetailsList.stream()
				.collect(Collectors.groupingBy(SprintDetails::getBasicProjectConfigId, Collectors.collectingAndThen(
						Collectors.toList(), list -> list.stream().limit(5).collect(Collectors.toList()))));
	}

	/**
	 * Filters out HierarchyResponseDTOs where none of the tools are connected.
	 *
	 * @param hierarchyResponseDTOS
	 *            the list of HierarchyResponseDTOs to filter
	 * @return a list of HierarchyResponseDTOs with at least one connected tool
	 */
	public List<HierarchyResponseDTO> filterHierarchyDTOsWithConnectedTools(
			List<HierarchyResponseDTO> hierarchyResponseDTOS) {
		Map<ObjectId, Map<String, List<ProjectToolConfig>>> projectToolConfigMapData = getProjectToolConfigMapData();
		return hierarchyResponseDTOS.stream()
				.filter(hierarchyResponsedto -> MapUtils
						.isNotEmpty(projectToolConfigMapData.get(new ObjectId(hierarchyResponsedto.getProjectId()))))
				.collect(Collectors.toList());
	}

	private Map<ObjectId, Map<String, List<ProjectToolConfig>>> getProjectToolConfigMapData() {
		return (Map<ObjectId, Map<String, List<ProjectToolConfig>>>) cacheService.cacheProjectToolConfigMapData();
	}

}
