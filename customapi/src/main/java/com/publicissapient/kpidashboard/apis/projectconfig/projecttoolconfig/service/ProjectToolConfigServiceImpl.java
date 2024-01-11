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

package com.publicissapient.kpidashboard.apis.projectconfig.projecttoolconfig.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.cleanup.ToolDataCleanUpService;
import com.publicissapient.kpidashboard.apis.cleanup.ToolDataCleanUpServiceFactory;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.errors.ToolNotFoundException;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.repotools.service.RepoToolsConfigServiceImpl;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfigDTO;
import com.publicissapient.kpidashboard.common.model.application.Subproject;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.SubProjectRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author yasbano
 * @author dilipKr
 *
 */
@Service
@Slf4j
public class ProjectToolConfigServiceImpl implements ProjectToolConfigService {

	private static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	private static final String SUCCESS_MSG = "Successfully fetched all records for projectToolConfig";
	private static final String TOOL_NOT_FOUND = "Tool not found";
	@Autowired
	private ProjectToolConfigRepository toolRepository;
	@Autowired
	private ConnectionRepository connectionRepository;
	@Autowired
	private SubProjectRepository subProjectRepository;
	@Autowired
	private CacheService cacheService;
	@Autowired
	private ToolDataCleanUpServiceFactory dataCleanUpServiceFactory;
	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private RepoToolsConfigServiceImpl repoToolsConfigService;

	/**
	 * make a copy of the list so the original list is not changed, and remove() is
	 * supported
	 *
	 * @param l1
	 * @param l2
	 * @return
	 */
	private static boolean compareTwoListOfObjects(List<?> l1, List<?> l2) {

		ArrayList<?> cp = new ArrayList<>(l1);
		for (Object o : l2) {
			if (!cp.remove(o)) {
				return false;
			}
		}
		return cp.isEmpty();
	}

	/**
	 * Fetch all ProjectToolConfig data.
	 *
	 * @return ServiceResponse with data object,message and status flag. Status flag
	 *         is true, if data is found else false.
	 */
	@Override
	public ServiceResponse getAllProjectTool() {
		final List<ProjectToolConfig> toolConfigList = toolRepository.findAll();
		if (CollectionUtils.isEmpty(toolConfigList)) {
			log.info("No record found for projectToolConfig");
			return new ServiceResponse(false, "No record found for projectToolConfig", toolConfigList);
		}
		mapJiraSubProject(toolConfigList);
		log.info(SUCCESS_MSG);
		return new ServiceResponse(true, "Fetched all records for projectToolConfig", toolConfigList);
	}

	/**
	 * Fetch a ProjectToolConfig by toolType. *
	 *
	 * @param toolType
	 *            as toolType
	 *
	 * @return ServiceResponse with data object,message and status flag. Status flag
	 *         is true, if data is found else false.
	 */
	@Override
	public ServiceResponse getProjectToolByType(String toolType) {
		if (StringUtils.isEmpty(toolType)) {
			log.info("Tool type is either null or empty");
			return new ServiceResponse(false, "Tool type is either null or empty in request parameter", toolType);
		}
		final List<ProjectToolConfig> toolTypeList = toolRepository.findByToolName(toolType);
		if (CollectionUtils.isEmpty(toolTypeList)) {
			log.info("No projectToolConfig found for @{}", toolType);
			return new ServiceResponse(false, "No projectToolConfig found for " + toolType, null);
		}
		List<ProjectToolConfigDTO> projectConfToolDtoList = mapJiraSubProject(toolTypeList);
		log.info("Successfully fetched projectToolConfig for @{}", toolType);
		return new ServiceResponse(true, "Successfully fetched projectToolConfig for " + toolType,
				projectConfToolDtoList);
	}

	/**
	 * Create and save a connection in the database.
	 *
	 * @param projectToolConfig
	 *            as project_tool_configs
	 *
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */

	@Override
	public ServiceResponse saveProjectToolDetails(ProjectToolConfig projectToolConfig) {
		if (null == projectToolConfig) {
			log.info("projectToolConfig object is empty");
			return new ServiceResponse(false, "projectToolConfig  cannot be empty", null);
		}
		if (!valid(projectToolConfig)) {
			log.info("projectToolName is empty");
			return new ServiceResponse(false, " projectToolName,connectionId,getBasicProjectConfigId, cannot be empty",
					null);
		}

		if (projectToolConfig.getToolName().equalsIgnoreCase(ProcessorConstants.JIRA)
				&& hasTool(projectToolConfig.getBasicProjectConfigId(), ProcessorConstants.JIRA)) {
			return new ServiceResponse(false, "Jira already configured for this project", null);
		}
		if (projectToolConfig.getToolName().equalsIgnoreCase(ProcessorConstants.REPO_TOOLS)) {
			ServiceResponse repoToolServiceResponse = setRepoToolConfig(projectToolConfig);
			if(Boolean.FALSE.equals(repoToolServiceResponse.getSuccess()))
				return repoToolServiceResponse;
		}

		if (projectToolConfig.getToolName().equalsIgnoreCase(ProcessorConstants.JIRA_TEST)
				&& hasTool(projectToolConfig.getBasicProjectConfigId(), ProcessorConstants.JIRA_TEST)) {
			return new ServiceResponse(false, "Jira Test already configured for this project", null);
		}

		if (projectToolConfig.getToolName().equalsIgnoreCase(ProcessorConstants.AZURE)
				&& hasTool(projectToolConfig.getBasicProjectConfigId(), ProcessorConstants.AZURE)) {
			return new ServiceResponse(false, "Azure already configured for this project", null);
		}

		if (projectToolConfig.getToolName().equalsIgnoreCase(ProcessorConstants.ZEPHYR)
				&& hasTool(projectToolConfig.getBasicProjectConfigId(), ProcessorConstants.ZEPHYR)) {
			return new ServiceResponse(false, "Zephyr already configured for this project", null);
		}

		log.info("Successfully pushed project_tools into db");
		projectToolConfig.setCreatedAt(DateUtil.dateTimeFormatter(LocalDateTime.now(), TIME_FORMAT));
		projectToolConfig.setUpdatedAt(DateUtil.dateTimeFormatter(LocalDateTime.now(), TIME_FORMAT));
		toolRepository.save(projectToolConfig);
		cacheService.clearCache(CommonConstant.CACHE_TOOL_CONFIG_MAP);
		cacheService.clearCache(CommonConstant.CACHE_PROJECT_TOOL_CONFIG_MAP);
		return new ServiceResponse(true, "created and saved new project_tools", projectToolConfig);
	}

	/**
	 * Update project_tool_configs data.
	 *
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */

	@Override
	public ServiceResponse modifyProjectToolById(ProjectToolConfig projectToolConfig, String projectToolId) {
		if (projectToolId == null || !ObjectId.isValid(projectToolId)) {
			log.info("Id not valid");
			return new ServiceResponse(false, "Invalid projectTool id " + projectToolId, null);
		}
		final ProjectToolConfig projectTool = toolRepository.findById(projectToolId);
		if (null == projectTool) {
			return new ServiceResponse(false, "No id in project_tools collection", projectTool);
		}
		if (!valid(projectToolConfig)) {
			log.info("projectToolName is empty");
			return new ServiceResponse(false, " projectToolName,connectionId,getBasicProjectConfigId, cannot be empty",
					null);
		}

		if (CollectionUtils.isNotEmpty(projectToolConfig.getBoards())
				&& CollectionUtils.isNotEmpty(projectTool.getBoards())
				&& !compareTwoListOfObjects(projectTool.getBoards(), projectToolConfig.getBoards())) {
			cleanData(projectTool);
		}


		projectTool.setToolName(projectToolConfig.getToolName());
		projectTool.setBasicProjectConfigId(projectToolConfig.getBasicProjectConfigId());
		projectTool.setConnectionId(projectToolConfig.getConnectionId());
		projectTool.setProjectId(projectToolConfig.getProjectId());
		projectTool.setProjectKey(projectToolConfig.getProjectKey());
		projectTool.setJobName(projectToolConfig.getJobName());
		projectTool.setJobType(projectToolConfig.getJobType());
		projectTool.setBranch(projectToolConfig.getBranch());
		projectTool.setEnv(projectToolConfig.getEnv());
		projectTool.setApiVersion(projectToolConfig.getApiVersion());
		projectTool.setRepoSlug(projectToolConfig.getRepoSlug());
		projectTool.setBitbucketProjKey(projectToolConfig.getBitbucketProjKey());
		projectTool.setUpdatedAt(DateUtil.dateTimeFormatter(LocalDateTime.now(), TIME_FORMAT));
		projectTool.setBoardQuery(projectToolConfig.getBoardQuery());
		projectTool.setBoards(projectToolConfig.getBoards());
		projectTool.setQueryEnabled(projectToolConfig.isQueryEnabled());
		projectTool.setRegressionAutomationLabels(projectToolConfig.getRegressionAutomationLabels());
		projectTool.setTestAutomationStatusLabel(projectToolConfig.getTestAutomationStatusLabel());
		projectTool.setAutomatedTestValue(projectToolConfig.getAutomatedTestValue());
		projectTool.setTestAutomated(projectToolConfig.getTestAutomated());
		projectTool.setCanNotAutomatedTestValue(projectToolConfig.getCanNotAutomatedTestValue());
		projectTool.setTestRegressionLabel(projectToolConfig.getTestRegressionLabel());
		projectTool.setTestRegressionValue(projectToolConfig.getTestRegressionValue());
		projectTool.setRegressionAutomationFolderPath(projectToolConfig.getRegressionAutomationFolderPath());
		projectTool.setInSprintAutomationFolderPath(projectToolConfig.getInSprintAutomationFolderPath());
		projectTool.setOrganizationKey(projectToolConfig.getOrganizationKey());
		projectTool.setJiraTestCaseType(projectToolConfig.getJiraTestCaseType());
		projectTool.setTestAutomatedIdentification(projectToolConfig.getTestAutomatedIdentification());
		projectTool
				.setTestAutomationCompletedIdentification(projectToolConfig.getTestAutomationCompletedIdentification());
		projectTool.setTestRegressionIdentification(projectToolConfig.getTestRegressionIdentification());
		projectTool
				.setTestAutomationCompletedByCustomField(projectToolConfig.getTestAutomationCompletedByCustomField());
		projectTool.setTestRegressionByCustomField(projectToolConfig.getTestRegressionByCustomField());
		projectTool.setJiraAutomatedTestValue(projectToolConfig.getJiraAutomatedTestValue());
		projectTool.setJiraRegressionTestValue(projectToolConfig.getJiraRegressionTestValue());
		projectTool.setJiraCanBeAutomatedTestValue(projectToolConfig.getJiraCanBeAutomatedTestValue());
		projectTool.setTestCaseStatus(projectToolConfig.getTestCaseStatus());
		projectTool.setMetadataTemplateCode(projectToolConfig.getMetadataTemplateCode());
		projectTool.setGitLabSdmID(projectToolConfig.getGitLabSdmID());
		projectTool.setAzureIterationStatusFieldUpdate(projectToolConfig.isAzureIterationStatusFieldUpdate());
		projectTool.setProjectComponent(projectToolConfig.getProjectComponent());
		log.info("Successfully update project_tools  into db");
		toolRepository.save(projectTool);
		cacheService.clearCache(CommonConstant.CACHE_TOOL_CONFIG_MAP);
		cacheService.clearCache(CommonConstant.CACHE_PROJECT_TOOL_CONFIG_MAP);
		if (projectTool.getToolName().equalsIgnoreCase(ProcessorConstants.ZEPHYR)
				|| projectTool.getToolName().equalsIgnoreCase(ProcessorConstants.JIRA_TEST)) {
			cacheService.clearCache(CommonConstant.TESTING_KPI_CACHE);
		}
		return new ServiceResponse(true, "updated the project_tools Successfully", projectTool);
	}

	@Override
	public List<ProjectToolConfigDTO> getProjectToolConfigs(String basicProjectConfigId) {

		final List<ProjectToolConfig> toolConfigList = toolRepository
				.findByBasicProjectConfigId(new ObjectId(basicProjectConfigId));
		List<ProjectToolConfigDTO> projectConfToolDtoList = mapJiraSubProject(toolConfigList);
		log.info(SUCCESS_MSG);
		return projectConfToolDtoList;
	}

	@Override
	public List<ProjectToolConfigDTO> getProjectToolConfigs(String basicProjectConfigId, String type) {
		final List<ProjectToolConfig> toolConfigList = toolRepository.findByToolNameAndBasicProjectConfigId(type,
				new ObjectId(basicProjectConfigId));
		List<ProjectToolConfigDTO> projectConfToolDtoList = mapJiraSubProject(toolConfigList);
		log.info(SUCCESS_MSG);
		return projectConfToolDtoList;
	}

	@Override
	public boolean deleteTool(String basicProjectConfigId, String projectToolId) {

		List<ProjectToolConfig> toolList = toolRepository
				.findByBasicProjectConfigId(new ObjectId(basicProjectConfigId));
		Optional<ProjectToolConfig> optionalProjectToolConfig = toolList.stream()
				.filter(projectToolConfig -> projectToolConfig.getId().equals(new ObjectId(projectToolId))).findFirst();
		if (!optionalProjectToolConfig.isPresent()) {
			throw new ToolNotFoundException(TOOL_NOT_FOUND);
		}
		ProjectToolConfig tool = optionalProjectToolConfig.get();
		if (isValidTool(basicProjectConfigId, tool)) {
			if (isRepoTool(tool)
					&& !repoToolsConfigService.updateRepoToolProjectConfiguration(
							toolList.stream()
									.filter(projectToolConfig -> projectToolConfig.getToolName()
											.equals(CommonConstant.REPO_TOOLS))
									.collect(Collectors.toList()),
							tool, basicProjectConfigId)) {
				return false;
			}
			cleanData(tool);
			toolRepository.deleteById(new ObjectId(projectToolId));

			log.info("tool with id {} deleted", projectToolId);

			return true;
		} else {
			log.error("basicConfigId = {}, toolConfigId = {} - not found", basicProjectConfigId, projectToolId);
			throw new ToolNotFoundException(TOOL_NOT_FOUND);
		}

	}

	private boolean isRepoTool(ProjectToolConfig tool){
		return tool.getToolName().equalsIgnoreCase(Constant.REPO_TOOLS);
	}

	private void cleanData(ProjectToolConfig tool) {
		ToolDataCleanUpService dataCleanUpService = dataCleanUpServiceFactory.getService(tool.getToolName());
		if (dataCleanUpService != null) {
			dataCleanUpService.clean(tool.getId().toHexString());
		}
	}

	private boolean isValidTool(String basicProjectConfigId, ProjectToolConfig tool) {

		return tool != null && tool.getBasicProjectConfigId().toHexString().equals(basicProjectConfigId);
	}

	public boolean valid(ProjectToolConfig projectToolConfig) {
		if (StringUtils.isEmpty(projectToolConfig.getToolName())) {
			log.info("toolName is null");
			return false;
		}
		if (!Optional.ofNullable(projectToolConfig.getBasicProjectConfigId()).isPresent()) {
			log.info("projectConfigId is null");
			return false;
		}
		if (!Optional.ofNullable(projectToolConfig.getConnectionId()).isPresent()) {
			log.info("connectionId is null");
			return false;
		}

		return true;
	}

	private List<ProjectToolConfigDTO> mapJiraSubProject(List<ProjectToolConfig> toolTypeList) {
		List<ProjectToolConfigDTO> projectConfToolDtoList;
		projectConfToolDtoList = mapSubProjects(toolTypeList);
		List<ObjectId> toolConfiragrationIds = toolTypeList.stream().map(ProjectToolConfig::getId)
				.collect(Collectors.toList());
		List<Subproject> subProjectList = subProjectRepository.findBytoolConfigIdIn(toolConfiragrationIds);
		Map<ObjectId, List<Subproject>> subProjectMap = subProjectList.stream()
				.collect(Collectors.groupingBy(Subproject::getToolConfigId));
		if (CollectionUtils.isNotEmpty(subProjectList)) {
			projectConfToolDtoList.forEach(e -> e.setSubprojects(subProjectMap.get(new ObjectId(e.getId()))));
		}
		return projectConfToolDtoList;
	}

	private List<ProjectToolConfigDTO> mapSubProjects(List<ProjectToolConfig> toolTypeList) {
		List<ProjectToolConfigDTO> projectConfToolDtoList = new ArrayList<>();
		toolTypeList.forEach(e -> {
			ProjectToolConfigDTO projectConfToolDto = new ProjectToolConfigDTO();
			projectConfToolDto.setApiVersion(e.getApiVersion());
			projectConfToolDto.setBasicProjectConfigId(e.getBasicProjectConfigId());
			projectConfToolDto.setBranch(e.getBranch());
			projectConfToolDto.setDefaultBranch(e.getDefaultBranch());
			projectConfToolDto.setConnectionId(e.getConnectionId());
			projectConfToolDto.setId(e.getId().toString());
			projectConfToolDto.setToolName(e.getToolName());
			projectConfToolDto.setProjectId(e.getProjectId());
			projectConfToolDto.setProjectKey(e.getProjectKey());
			projectConfToolDto.setJobName(e.getJobName());
			projectConfToolDto.setJobType(e.getJobType());
			projectConfToolDto.setEnv(e.getEnv());
			projectConfToolDto.setRepoSlug(e.getRepoSlug());
			projectConfToolDto.setRepositoryName(e.getRepositoryName());
			projectConfToolDto.setBitbucketProjKey(e.getBitbucketProjKey());
			projectConfToolDto.setCreatedAt(e.getCreatedAt());
			projectConfToolDto.setUpdatedAt(e.getUpdatedAt());
			projectConfToolDto.setQueryEnabled(e.isQueryEnabled());
			projectConfToolDto.setBoardQuery(e.getBoardQuery());
			projectConfToolDto.setBoards(e.getBoards());
			projectConfToolDto.setMetadataTemplateCode(e.getMetadataTemplateCode());
			projectConfToolDto.setRegressionAutomationLabels(e.getRegressionAutomationLabels());
			projectConfToolDto.setTestAutomationStatusLabel(e.getTestAutomationStatusLabel());
			projectConfToolDto.setAutomatedTestValue(e.getAutomatedTestValue());
			projectConfToolDto.setTestAutomated(e.getTestAutomated());
			projectConfToolDto.setCanNotAutomatedTestValue(e.getCanNotAutomatedTestValue());
			projectConfToolDto.setTestRegressionLabel(e.getTestRegressionLabel());
			projectConfToolDto.setTestRegressionValue(e.getTestRegressionValue());
			projectConfToolDto.setRegressionAutomationFolderPath(e.getRegressionAutomationFolderPath());
			projectConfToolDto.setInSprintAutomationFolderPath(e.getInSprintAutomationFolderPath());
			projectConfToolDto.setOrganizationKey(e.getOrganizationKey());
			projectConfToolDto.setDeploymentProjectId(e.getDeploymentProjectId());
			projectConfToolDto.setDeploymentProjectName(e.getDeploymentProjectName());
			projectConfToolDto.setParameterNameForEnvironment(e.getParameterNameForEnvironment());
			projectConfToolDto.setConnectionName(getConnection(e.getConnectionId()).getConnectionName());
			projectConfToolDtoList.add(projectConfToolDto);
			projectConfToolDto.setJiraTestCaseType(e.getJiraTestCaseType());
			projectConfToolDto.setTestAutomatedIdentification(e.getTestAutomatedIdentification());
			projectConfToolDto.setTestAutomationCompletedIdentification(e.getTestAutomationCompletedIdentification());
			projectConfToolDto.setTestRegressionIdentification(e.getTestRegressionIdentification());
			projectConfToolDto.setTestAutomationCompletedByCustomField(e.getTestAutomationCompletedByCustomField());
			projectConfToolDto.setTestRegressionByCustomField(e.getTestRegressionByCustomField());
			projectConfToolDto.setJiraAutomatedTestValue(e.getJiraAutomatedTestValue());
			projectConfToolDto.setJiraRegressionTestValue(e.getJiraRegressionTestValue());
			projectConfToolDto.setJiraCanBeAutomatedTestValue(e.getJiraCanBeAutomatedTestValue());
			projectConfToolDto.setTestCaseStatus(e.getTestCaseStatus());
			projectConfToolDto.setGitLabSdmID(e.getGitLabSdmID());
			projectConfToolDto.setAzureIterationStatusFieldUpdate(e.isAzureIterationStatusFieldUpdate());
			projectConfToolDto.setIsNew(e.getIsNew());
			projectConfToolDto.setConnectionName(getConnection(e.getConnectionId()).getConnectionName());
			projectConfToolDto.setProjectComponent(e.getProjectComponent());
		});

		return projectConfToolDtoList;
	}

	private Connection getConnection(ObjectId connectionId) {
		Optional<Connection> optConnection = connectionRepository.findById(connectionId);
		if (optConnection.isPresent()) {
			return optConnection.get();
		}
		return null;
	}

	private boolean hasTool(ObjectId basicProjectConfigId, String type) {
		List<ProjectToolConfig> tools = toolRepository.findByToolNameAndBasicProjectConfigId(type,
				basicProjectConfigId);
		return CollectionUtils.isNotEmpty(tools);
	}

	private List<ProjectToolConfig> getRepoTool(ObjectId basicProjectConfigId, Connection connection, String type) {
		List<ProjectToolConfig> tools = toolRepository.findByToolNameAndBasicProjectConfigId(type,
				basicProjectConfigId);
		return tools.stream().filter(projectToolConfig -> projectToolConfig.getConnectionId().equals(connection.getId()))
						.collect(Collectors.toList());
	}

	private ServiceResponse setRepoToolConfig(ProjectToolConfig projectToolConfig) {
		Connection connection = getConnection(projectToolConfig.getConnectionId());
		List<ProjectToolConfig> repoConfigList = getRepoTool(projectToolConfig.getBasicProjectConfigId(), connection,
				ProcessorConstants.REPO_TOOLS);
		List<String> branchList = repoConfigList.stream().map(ProjectToolConfig::getBranch).filter(Objects::nonNull)
				.collect(Collectors.toList());
		projectToolConfig.setIsNew(CollectionUtils.isEmpty(repoConfigList));
		if (projectToolConfig.getBranch() == null) {
			branchList.add(projectToolConfig.getDefaultBranch());
			projectToolConfig.setBranch(projectToolConfig.getDefaultBranch());
		} else
			branchList.add(projectToolConfig.getBranch());
		int httpStatus = repoToolsConfigService.configureRepoToolProject(projectToolConfig, connection, branchList);
		if (httpStatus == HttpStatus.NOT_FOUND.value())
			return new ServiceResponse(false, "", null);
		if (httpStatus == HttpStatus.BAD_REQUEST.value())
			return new ServiceResponse(false, "Project with similar configuration already exists", null);
		if (httpStatus == HttpStatus.INTERNAL_SERVER_ERROR.value())
			return new ServiceResponse(false, "Invalid Repository Name", null);
		return new ServiceResponse(true, "", null);
	}

	@Override
	public boolean cleanToolData(String basicProjectConfigId, String projectToolId) {
		ProjectToolConfig tool = toolRepository.findById(projectToolId);
		if (isValidTool(basicProjectConfigId, tool)) {
			if (isRepoTool(tool)) {
				repoToolsConfigService.deleteRepoToolProject(configHelperService.getProjectConfig(basicProjectConfigId),
						true);
			}
			cleanData(tool);
			return true;
		} else {
			log.error("basicConfigId = {}, toolConfigId = {} - not found", basicProjectConfigId, projectToolId);
			throw new ToolNotFoundException(TOOL_NOT_FOUND);
		}
	}

}
