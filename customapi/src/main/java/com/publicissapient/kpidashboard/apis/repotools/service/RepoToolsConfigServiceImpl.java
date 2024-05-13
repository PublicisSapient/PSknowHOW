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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolConnModel;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolConnectionDetail;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolsStatusResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.repotools.RepoToolsClient;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolConfig;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolKpiBulkMetricResponse;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolKpiMetricResponse;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolKpiRequestBody;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolsProvider;
import com.publicissapient.kpidashboard.apis.repotools.repository.RepoToolsProviderRepository;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
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

	public static final String TOOL_BRANCH = "branch";
	public static final String SCM = "scm";
	public static final String REPO_NAME = "repoName";
	public static final String REPO_BRANCH = "defaultBranch";
	public static final String BITBUCKET = "bitbucket";
	public static final String BITBUCKET_CLOUD_IDENTIFIER = "bitbucket.org";
	public static final String PROJECT = "/projects/";
	public static final String REPOS = "/repos/";



	/**
	 * enroll a project to the repo tool
	 * 
	 * @param projectToolConfig
	 * @param connection
	 * @param branchNames
	 * @return
	 */
	public int configureRepoToolProject(ProjectToolConfig projectToolConfig, Connection connection,
			List<String> branchNames) {
		int httpStatus;
		try {
			// create scanning account
			ToolCredential toolCredential = new ToolCredential(connection.getUsername(),
					aesEncryptionService.decrypt(connection.getAccessToken(), customApiConfig.getAesEncryptionKey()),
					connection.getEmail());
			LocalDateTime fistScan = LocalDateTime.now().minusMonths(6);
			RepoToolsProvider repoToolsProvider = repoToolsProviderRepository
					.findByToolName(connection.getRepoToolProvider().toLowerCase());
			String[] split = projectToolConfig.getGitFullUrl().split("/");
			String name = split[split.length - 1];
			if (name.contains("."))
				name = name.split(".git")[0];
			projectToolConfig.setRepositoryName(name);
			String apiEndPoint = null;
			if (repoToolsProvider.getToolName().equalsIgnoreCase(BITBUCKET)
					&& !projectToolConfig.getGitFullUrl().contains(BITBUCKET_CLOUD_IDENTIFIER)) {
				apiEndPoint = connection.getApiEndPoint() + PROJECT + split[split.length - 2] + REPOS + name;
			}
			// create configuration details for repo tool
			RepoToolConfig repoToolConfig = new RepoToolConfig(name, projectToolConfig.getIsNew(),
					projectToolConfig.getBasicProjectConfigId().toString().concat(name), projectToolConfig.getGitFullUrl(),
					apiEndPoint, repoToolsProvider.getRepoToolProvider(), projectToolConfig.getDefaultBranch(),
					createProjectCode(projectToolConfig.getBasicProjectConfigId().toString()),
					fistScan.toString().replace("T", " "), toolCredential, branchNames, false);

			// api call to enroll the project
			httpStatus = repoToolsClient.enrollProjectCall(repoToolConfig,
					customApiConfig.getRepoToolURL() + customApiConfig.getRepoToolEnrollProjectUrl(),
					customApiConfig.getRepoToolAPIKey());

		} catch (HttpClientErrorException | HttpServerErrorException ex) {
			log.error("Exception occcured while enrolling project {}",
					projectToolConfig.getBasicProjectConfigId().toString(), ex);
			httpStatus = ex.getStatusCode().value();
		}
		return httpStatus;
	}

	/**
	 * trigger repo tool scanning process
	 * 
	 * @param basicProjectconfigIdList
	 * @return
	 */
	public int triggerScanRepoToolProject(List<String> basicProjectconfigIdList) {
		int httpStatus = HttpStatus.NOT_FOUND.value();
		Processor processor = processorRepository.findByProcessorName(CommonConstant.REPO_TOOLS);

		// get repo tools configuration from ProjectToolConfig
		List<ProjectToolConfig> projectRepos = projectToolConfigRepository.findByToolNameAndBasicProjectConfigId(
				CommonConstant.REPO_TOOLS, new ObjectId(basicProjectconfigIdList.get(0)));
		try {

			List<ProjectToolConfig> projectToolConfigList = projectRepos.stream()
					.filter(projectToolConfig -> projectToolConfig.getBasicProjectConfigId()
							.equals(new ObjectId(basicProjectconfigIdList.get(0))))
					.collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(projectToolConfigList)) {
				String projectCode = createProjectCode(basicProjectconfigIdList.get(0));

				// api call to start project scanning
				httpStatus = repoToolsClient.triggerScanCall(projectCode,
						customApiConfig.getRepoToolURL() + customApiConfig.getRepoToolTriggerScan(),
						customApiConfig.getRepoToolAPIKey());

				// save ProcessorItemRepository for all the ProjectToolConfig
				processorItemRepository.saveAll(createProcessorItemList(projectToolConfigList, processor.getId()));

			}
		} catch (Exception ex) {
			log.error("Exception occcured while scanning project {}", basicProjectconfigIdList, ex);
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
		ProjectBasicConfig projectBasicConfig = configHelperService.getProjectConfig(basicProjectConfigId);
		return (projectBasicConfig.getProjectName() + "_" + basicProjectConfigId).replaceAll("\\s", "");
	}

	/**
	 * update a project enrolled in repo tool
	 * 
	 * @param toolList
	 * @param tool
	 * @param basicProjectConfigId
	 * @return
	 */
	public boolean updateRepoToolProjectConfiguration(List<ProjectToolConfig> toolList, ProjectToolConfig tool,
			String basicProjectConfigId) {
		int httpStatus = HttpStatus.NOT_FOUND.value();
		if (toolList.size() > 1) {
			toolList.remove(tool);
			if (toolList.size() > 0) {
				// configure debbie project with
				List<String> branch = new ArrayList<>();
				toolList.forEach(projectToolConfig -> branch.add(projectToolConfig.getBranch()));
				Connection connection = connectionRepository.findById(tool.getConnectionId()).orElse(new Connection());
				toolList.get(0).setIsNew(false);
				httpStatus = configureRepoToolProject(toolList.get(0), connection, branch);
			} else {
				// delete only the repository
				String deleteRepoUrl = customApiConfig.getRepoToolURL()
						+ String.format(customApiConfig.getRepoToolDeleteRepoUrl(),
								createProjectCode(basicProjectConfigId), tool.getRepositoryName());
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
				projectCode.stream().map(code -> code.replaceAll("\\s", "")).collect(Collectors.toList()), startDate,
				endDate, frequency);
		try {
			String url = String.format(repoToolUrl, startDate, endDate, frequency);
			RepoToolKpiBulkMetricResponse repoToolKpiBulkMetricResponse = repoToolsClient.kpiMetricCall(url,
					repoToolApiKey, repoToolKpiRequestBody);
			repoToolKpiMetricRespons = repoToolKpiBulkMetricResponse.getValues().stream().flatMap(List::stream)
					.collect(Collectors.toList());
		} catch (Exception ex) {
			log.error("Exception while fetching KPI data {}", projectCode, ex);
		}
		return repoToolKpiMetricRespons;
	}

	/**
	 * create ProcessorItemList for scanning
	 * 
	 * @param toolList
	 * @param processorId
	 * @return
	 */
	private List<ProcessorItem> createProcessorItemList(List<ProjectToolConfig> toolList, ObjectId processorId) {
		List<ProcessorItem> processorItemList = new ArrayList<>();
		toolList.forEach(tool -> {
			ProcessorItem item = new ProcessorItem();
			item.setToolConfigId(tool.getId());
			item.setProcessorId(processorId);
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
		String projectCode = (projectBasicConfig.getProjectName() + "_" + projectBasicConfig.getId()).replaceAll("\\s", "");
		String deleteUrl = customApiConfig.getRepoToolURL()
				+ String.format(customApiConfig.getRepoToolDeleteProjectUrl(),
						projectCode, onlyData);
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
	 * @param repoToolsStatusResponse Object containing repo tool scanning status
	 */
	public void saveRepoToolProjectTraceLog(RepoToolsStatusResponse repoToolsStatusResponse) {

		String basicProjectConfigId = repoToolsStatusResponse.getProject()
				.substring(repoToolsStatusResponse.getProject().lastIndexOf('_') + 1);
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setProcessorName(ProcessorConstants.REPO_TOOLS);
		processorExecutionTraceLog.setBasicProjectConfigId(basicProjectConfigId);
		Optional<ProcessorExecutionTraceLog> existingTraceLogOptional = processorExecutionTraceLogRepository
				.findByProcessorNameAndBasicProjectConfigId(ProcessorConstants.REPO_TOOLS, basicProjectConfigId);
		existingTraceLogOptional.ifPresent(
				existingProcessorExecutionTraceLog -> processorExecutionTraceLog.setLastEnableAssigneeToggleState(
						existingProcessorExecutionTraceLog.isLastEnableAssigneeToggleState()));
		processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
		processorExecutionTraceLog.setExecutionSuccess(
				Constant.SUCCESS.equalsIgnoreCase(repoToolsStatusResponse.getStatus()));
		processorExecutionTraceLogService.save(processorExecutionTraceLog);

	}

	public int updateRepoToolConnection(Connection conn)
	{
		List<RepoToolConnectionDetail> repoToolConnectionDetails = new ArrayList<>();
		try {
			RepoToolConnectionDetail repoToolConnectionDetail = new RepoToolConnectionDetail();
			repoToolConnectionDetail.setEmail(conn.getEmail());
			repoToolConnectionDetail.setPassword(
					aesEncryptionService.decrypt(conn.getAccessToken(), customApiConfig.getAesEncryptionKey()));
			repoToolConnectionDetail.setUsername(conn.getUsername());
			repoToolConnectionDetail.setProvider(conn.getRepoToolProvider());
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

}
