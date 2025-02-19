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

package com.publicissapient.kpidashboard.apis.repotools.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.repotools.RepoToolsClient;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolConfig;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolConnModel;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolConnectionDetail;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolKpiBulkMetricResponse;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolKpiMetricResponse;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolKpiRequestBody;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolsProvider;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolsStatusResponse;
import com.publicissapient.kpidashboard.apis.repotools.repository.RepoToolsProviderRepository;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.generic.Processor;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorItem;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorItemRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RepoToolsConfigServiceImpl {

	@Autowired
	private ProjectToolConfigRepository projectToolConfigRepository;

	@Autowired
	private RepoToolsProviderRepository repoToolsProviderRepository;

	@Autowired
	CustomApiConfig customApiConfig;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private ProcessorItemRepository processorItemRepository;

	@Autowired
	private ProcessorRepository<?> processorRepository;

	@Autowired
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;
	@Autowired
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;
	@Autowired
	private ConnectionRepository connectionRepository;
	@Autowired
	private AesEncryptionService aesEncryptionService;
	@Autowired
	private RepoToolsClient repoToolsClient;
	@Autowired
	private ObjectMapper objectMapper;

	public static final String TOOL_BRANCH = "branch";
	public static final String SCM = "scm";
	public static final String REPO_NAME = "repoName";
	public static final String REPO_BRANCH = "defaultBranch";
	public static final String PROJECT = "/projects/";
	public static final String REPOS = "/repos/";
	public static final String AZURE_PROVIDER = "azure";
	public static final String BITBUCKET_PROVIDER = "bitbucket_oauth2";
	public static final String WARNING = "WARNING";

	/**
	 * Configures and enrolls a project in the repo tool.
	 *
	 * @param projectToolConfig
	 *          the ProjectToolConfig object containing project tool configuration
	 * @param connection
	 *          the Connection object containing connection details
	 * @param branchNames
	 *          the list of branch names to be scanned
	 * @return a ServiceResponse object indicating the success or failure of the
	 *         operation
	 */
	public ServiceResponse configureRepoToolProject(ProjectToolConfig projectToolConfig, Connection connection,
			List<String> branchNames) {
		try {
			RepoToolConfig repoToolConfig = new RepoToolConfig();
			// create configuration details for repo tool
			setToolWiseRepoToolConfig(connection, projectToolConfig, repoToolConfig);
			repoToolConfig.setIsNew(projectToolConfig.getIsNew());
			repoToolConfig.setHttpUrl(projectToolConfig.getGitFullUrl());
			repoToolConfig.setDefaultBranch(projectToolConfig.getBranch());
			repoToolConfig.setProjectCode(createProjectCode(projectToolConfig.getBasicProjectConfigId().toString()));
			repoToolConfig.setFirstScanFrom(LocalDateTime.now().minusMonths(3).toString().replace("T", " "));
			repoToolConfig.setScanningBranches(branchNames);
			repoToolConfig.setIsCloneable(true);
			// api call to enroll the project
			repoToolsClient.enrollProjectCall(repoToolConfig,
					customApiConfig.getRepoToolURL() + customApiConfig.getRepoToolEnrollProjectUrl(),
					customApiConfig.getRepoToolAPIKey());
		} catch (HttpClientErrorException | HttpServerErrorException ex) {
			log.error("Exception occcured while enrolling project {}", projectToolConfig.getBasicProjectConfigId().toString(),
					ex);
			String errorMessage = ex.getResponseBodyAsString();
			try {
				errorMessage = objectMapper.readTree(errorMessage).get("error").asText();
			} catch (Exception e) {
				log.error("Error parsing JSON response", e);
				errorMessage = ex.getStatusCode().value() == HttpStatus.BAD_REQUEST.value()
						? "Project with similar configuration already exists"
						: "";
			}
			return new ServiceResponse(false, errorMessage, null);
		}
		return new ServiceResponse(true, "", null);
	}

	/**
	 * Configures the RepoToolConfig object based on the tool-specific details.
	 *
	 * @param connection
	 *          the Connection object containing connection details
	 * @param projectToolConfig
	 *          the ProjectToolConfig object containing project tool configuration
	 * @param repoToolConfig
	 *          the RepoToolConfig object to be configured
	 */
	private void setToolWiseRepoToolConfig(Connection connection, ProjectToolConfig projectToolConfig,
			RepoToolConfig repoToolConfig) {
		// Split the Git URL to extract the repository name
		String[] split = projectToolConfig.getGitFullUrl().split("/");
		String name = split[split.length - 1];
		if (name.contains("."))
			name = name.split(".git")[0];

		String accessToken = "";

		// Configure the RepoToolConfig based on the tool name
		switch (projectToolConfig.getToolName()) {
			case Constant.TOOL_GITHUB :
				accessToken = connection.getAccessToken();
				repoToolConfig.setProvider(Constant.TOOL_GITHUB.toLowerCase());
				repoToolConfig.setName(projectToolConfig.getRepositoryName());
				break;
			case Constant.TOOL_BITBUCKET :
				accessToken = connection.getPassword();
				repoToolConfig.setApiEndPoint(connection.getApiEndPoint() + PROJECT + split[split.length - 2] + REPOS + name);
				repoToolConfig.setProvider(BITBUCKET_PROVIDER);
				projectToolConfig.setRepositoryName(projectToolConfig.getRepoSlug());
				repoToolConfig.setName(projectToolConfig.getRepoSlug());
				break;
			case Constant.TOOL_GITLAB :
				accessToken = connection.getAccessToken();
				repoToolConfig.setProvider(ProcessorConstants.GITLAB.toLowerCase());
				projectToolConfig.setRepositoryName(name);
				repoToolConfig.setName(name);
				break;
			case Constant.TOOL_AZUREREPO :
				accessToken = connection.getPat();
				repoToolConfig.setProvider(AZURE_PROVIDER);
				repoToolConfig.setOrganization(split[3]);
				repoToolConfig.setName(projectToolConfig.getRepositoryName());
				break;
			default :
				throw new IllegalStateException("Unexpected value: " + projectToolConfig.getToolName());
		}

		// Set the scanning account details in the RepoToolConfig
		repoToolConfig.setScanningAccount(new ToolCredential(connection.getUsername(),
				aesEncryptionService.decrypt(accessToken, customApiConfig.getAesEncryptionKey()), connection.getEmail()));

		// Set the project code in the RepoToolConfig
		repoToolConfig.setProjectCode(projectToolConfig.getBasicProjectConfigId().toString().concat(name));
	}

	/**
	 * Triggers a scan for a repo tool project.
	 *
	 * @param processorName
	 *          the name of the processor
	 * @param basicProjectConfigId
	 *          the ID of the basic project configuration
	 * @return the HTTP status code of the scan trigger request
	 */
	public int triggerScanRepoToolProject(String processorName, String basicProjectConfigId) {
		int httpStatus = HttpStatus.NOT_FOUND.value();
		Processor processor = processorRepository.findByProcessorName(processorName);
		// get repo tools configuration from ProjectToolConfig
		List<ProjectToolConfig> projectRepos = projectToolConfigRepository
				.findByToolNameAndBasicProjectConfigId(processorName, new ObjectId(basicProjectConfigId));
		try {

			List<ProjectToolConfig> projectToolConfigList = projectRepos.stream().filter(
					projectToolConfig -> projectToolConfig.getBasicProjectConfigId().equals(new ObjectId(basicProjectConfigId)))
					.toList();
			if (CollectionUtils.isNotEmpty(projectToolConfigList)) {
				String projectCode = createProjectCode(basicProjectConfigId);

				// api call to start project scanning
				httpStatus = repoToolsClient.triggerScanCall(projectCode,
						customApiConfig.getRepoToolURL() + customApiConfig.getRepoToolTriggerScan(),
						customApiConfig.getRepoToolAPIKey());

				// save ProcessorItemRepository for all the ProjectToolConfig
				processorItemRepository.saveAll(createProcessorItemList(projectToolConfigList, processor));
			}
		} catch (Exception ex) {
			log.error("Exception occurred while scanning project", ex);
		}
		return httpStatus;
	}

	/**
	 * create a project code for repo tool enrollment
	 *
	 * @param basicProjectConfigId
	 * @return
	 */
	public String createProjectCode(String basicProjectConfigId) {
		return (basicProjectConfigId).replaceAll("\\s", "");
	}

	/**
	 * Updates the configuration of a project enrolled in the repo tool.
	 *
	 * @param toolList
	 *          the list of ProjectToolConfig objects representing the tools
	 *          associated with the project
	 * @param tool
	 *          the ProjectToolConfig object representing the tool to be updated
	 * @param basicProjectConfigId
	 *          the ID of the basic project configuration
	 * @return true if the project configuration was successfully updated, false
	 *         otherwise
	 */
	public boolean updateRepoToolProjectConfiguration(List<ProjectToolConfig> toolList, ProjectToolConfig tool,
			String basicProjectConfigId) {
		int httpStatus = HttpStatus.NOT_FOUND.value();
		if (toolList.size() > 1) {
			toolList.remove(tool);
			if (CollectionUtils.isNotEmpty(getToolByRepo(tool, toolList))) {
				return true;
			} else {
				// delete only the repository
				String deleteRepoUrl = customApiConfig.getRepoToolURL() +
						String.format(customApiConfig.getRepoToolDeleteRepoUrl(),
								URLEncoder.encode(createProjectCode(basicProjectConfigId), StandardCharsets.UTF_8),
								tool.getRepositoryName());
				httpStatus = repoToolsClient.deleteRepositories(deleteRepoUrl, customApiConfig.getRepoToolAPIKey());
			}
		} else {
			try {
				ProjectBasicConfig projectBasicConfig = configHelperService.getProjectConfig(basicProjectConfigId);
				// delete the project from repo tool if only one repository is present
				httpStatus = deleteRepoToolProject(projectBasicConfig, false);
			} catch (Exception ex) {
				log.error("Exception while deleting project {}", ex);
			}
		}
		return httpStatus == HttpStatus.OK.value();
	}

	/**
	 * Filters the list of ProjectToolConfig objects based on the tool's repository
	 * details.
	 *
	 * @param tool
	 *          the ProjectToolConfig object representing the tool to be matched
	 * @param projectToolConfigList
	 *          the list of ProjectToolConfig objects to be filtered
	 * @return a list of ProjectToolConfig objects that match the repository details
	 *         of the given tool
	 */
	private List<ProjectToolConfig> getToolByRepo(ProjectToolConfig tool, List<ProjectToolConfig> projectToolConfigList) {
		if (tool.getToolName().equalsIgnoreCase(Constant.TOOL_GITLAB))
			return projectToolConfigList.stream().filter(toolConfig -> toolConfig.getProjectId().equals(tool.getProjectId()))
					.toList();
		else if (tool.getToolName().equalsIgnoreCase(Constant.TOOL_BITBUCKET))
			return projectToolConfigList.stream().filter(toolConfig -> toolConfig.getRepoSlug().equals(tool.getRepoSlug()))
					.toList();
		else {
			return projectToolConfigList.stream()
					.filter(toolConfig -> toolConfig.getRepositoryName().equals(tool.getRepositoryName())).toList();
		}
	}

	/**
	 * get metrics from repo tool kpis fo different projects
	 *
	 * @param projectCode
	 * @param repoToolKpi
	 * @param startDate
	 * @param endDate
	 * @param frequency
	 * @return
	 */
	public List<RepoToolKpiMetricResponse> getRepoToolKpiMetrics(List<String> projectCode, String repoToolKpi,
			String startDate, String endDate, String frequency) {
		String repoToolUrl = customApiConfig.getRepoToolURL().concat(repoToolKpi);
		String repoToolApiKey = customApiConfig.getRepoToolAPIKey();
		List<RepoToolKpiMetricResponse> repoToolKpiMetricRespons = new ArrayList<>();
		RepoToolKpiRequestBody repoToolKpiRequestBody = new RepoToolKpiRequestBody(
				projectCode.stream().map(code -> code.replaceAll("\\s", "")).toList(), startDate, endDate, frequency);
		try {
			String url = String.format(repoToolUrl, startDate, endDate, frequency);
			RepoToolKpiBulkMetricResponse repoToolKpiBulkMetricResponse = repoToolsClient.kpiMetricCall(url, repoToolApiKey,
					repoToolKpiRequestBody);
			repoToolKpiMetricRespons = repoToolKpiBulkMetricResponse.getValues().stream().flatMap(List::stream).toList();
		} catch (Exception ex) {
			log.error("Exception while fetching KPI data {}", projectCode, ex);
		}
		return repoToolKpiMetricRespons;
	}

	/**
	 * Creates a list of ProcessorItem objects from a list of ProjectToolConfig
	 * objects and a Processor.
	 *
	 * @param toolList
	 *          the list of ProjectToolConfig objects
	 * @param processor
	 *          the Processor object
	 * @return a list of ProcessorItem objects
	 */
	private List<ProcessorItem> createProcessorItemList(List<ProjectToolConfig> toolList, Processor processor) {
		List<ProcessorItem> processorItemList = new ArrayList<>();
		toolList.forEach(tool -> {
			ProcessorItem item = new ProcessorItem();
			item.setToolConfigId(tool.getId());
			item.setProcessorId(processor.getId());
			item.setActive(Boolean.TRUE);
			item.getToolDetailsMap().put(TOOL_BRANCH, tool.getBranch());
			item.getToolDetailsMap().put(SCM, tool.getToolName());
			item.getToolDetailsMap().put(REPO_NAME, tool.getRepositoryName());
			item.getToolDetailsMap().put(REPO_BRANCH, tool.getDefaultBranch());
			processorItemList.add(item);
		});

		return processorItemList;
	}

	/**
	 * delete project or project data enrolled in the repo tool
	 *
	 * @param projectBasicConfig
	 * @param onlyData
	 * @return
	 */
	public int deleteRepoToolProject(ProjectBasicConfig projectBasicConfig, Boolean onlyData) {
		String projectCode = (projectBasicConfig.getId().toString()).replaceAll("\\s", "");
		String deleteUrl = customApiConfig.getRepoToolURL() +
				String.format(customApiConfig.getRepoToolDeleteProjectUrl(), projectCode, onlyData);
		int httpStatus = HttpStatus.NOT_FOUND.value();
		try {
			httpStatus = repoToolsClient.deleteProject(deleteUrl, customApiConfig.getRepoToolAPIKey());
		} catch (Exception ex) {
			log.error("Exception while deleting project {}", projectBasicConfig.getProjectName(), ex);
		}
		return httpStatus;
	}

	/**
	 * create and save ProcessorExecutionTraceLog to track repo tool project scan
	 *
	 * @param repoToolsStatusResponse
	 *          Object containing repo tool scanning status
	 */
	public void saveRepoToolProjectTraceLog(RepoToolsStatusResponse repoToolsStatusResponse) {

		String basicProjectConfigId = repoToolsStatusResponse.getProject()
				.substring(repoToolsStatusResponse.getProject().lastIndexOf('_') + 1);
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		String repoToolProvider = repoToolsStatusResponse.getRepositoryProvider();
		if (repoToolProvider.equalsIgnoreCase(Constant.TOOL_GITHUB))
			processorExecutionTraceLog.setProcessorName(Constant.TOOL_GITHUB);
		else if (repoToolProvider.equalsIgnoreCase(Constant.TOOL_GITLAB))
			processorExecutionTraceLog.setProcessorName(Constant.TOOL_GITLAB);
		else if (repoToolProvider.equalsIgnoreCase(BITBUCKET_PROVIDER))
			processorExecutionTraceLog.setProcessorName(Constant.TOOL_BITBUCKET);
		else if (repoToolProvider.equalsIgnoreCase(AZURE_PROVIDER))
			processorExecutionTraceLog.setProcessorName(Constant.TOOL_AZUREREPO);
		processorExecutionTraceLog.setBasicProjectConfigId(basicProjectConfigId);
		Optional<ProcessorExecutionTraceLog> existingTraceLogOptional = processorExecutionTraceLogRepository
				.findByProcessorNameAndBasicProjectConfigId(repoToolsStatusResponse.getRepositoryProvider(),
						basicProjectConfigId);
		existingTraceLogOptional.ifPresent(existingProcessorExecutionTraceLog -> processorExecutionTraceLog
				.setLastEnableAssigneeToggleState(existingProcessorExecutionTraceLog.isLastEnableAssigneeToggleState()));
		boolean isWarning = WARNING.equalsIgnoreCase(repoToolsStatusResponse.getStatus());
		processorExecutionTraceLog.setExecutionWarning(isWarning);
		processorExecutionTraceLog
				.setExecutionSuccess(Constant.SUCCESS.equalsIgnoreCase(repoToolsStatusResponse.getStatus()));
		processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
		if (Boolean.TRUE.equals(isWarning))
			processorExecutionTraceLog
					.setExecutionResumesAt(LocalDateTime.now().plusHours(1).toInstant(ZoneOffset.UTC).toEpochMilli());
		processorExecutionTraceLogService.save(processorExecutionTraceLog);
	}

	public int updateRepoToolConnection(Connection conn) {
		List<RepoToolConnectionDetail> repoToolConnectionDetails = new ArrayList<>();
		try {
			RepoToolsProvider repoToolsProvider = repoToolsProviderRepository
					.findByToolName(conn.getRepoToolProvider().toLowerCase());
			RepoToolConnectionDetail repoToolConnectionDetail = new RepoToolConnectionDetail();
			repoToolConnectionDetail.setEmail(conn.getEmail());
			repoToolConnectionDetail
					.setPassword(aesEncryptionService.decrypt(conn.getAccessToken(), customApiConfig.getAesEncryptionKey()));
			repoToolConnectionDetail.setUsername(conn.getUsername());
			repoToolConnectionDetail.setProvider(repoToolsProvider.getRepoToolProvider());
			repoToolConnectionDetails.add(repoToolConnectionDetail);
			RepoToolConnModel repoToolConnModel = new RepoToolConnModel(repoToolConnectionDetails);

			// api call to update the detail
			repoToolsClient.updateConnection(repoToolConnModel,
					customApiConfig.getRepoToolURL() + customApiConfig.getRepoToolUpdateConnectionUrl(),
					customApiConfig.getRepoToolAPIKey());
			return HttpStatus.OK.value();
		} catch (HttpClientErrorException | HttpServerErrorException ex) {
			log.error("Exception occcured while updating conneection  {}", repoToolConnectionDetails, ex);
		}
		return HttpStatus.BAD_REQUEST.value();
	}

	/**
	 * get repository members from repo tool
	 *
	 * @param basicProjectConfigId
	 *          basic project config id
	 * @return list of repo members
	 */
	public JsonNode getProjectRepoToolMembers(String basicProjectConfigId) {
		String projectCode = createProjectCode(basicProjectConfigId);
		JsonNode jsonNode = null;
		try {
			String membersUrl = customApiConfig.getRepoToolURL() +
					String.format(customApiConfig.getRepoToolMembersUrl(), projectCode);
			jsonNode = repoToolsClient.fetchProjectRepoToolMembers(membersUrl, customApiConfig.getRepoToolAPIKey());
		} catch (Exception ex) {
			log.error("Exception occured while fetching emails ", ex);
		}
		return jsonNode;
	}
}
