package com.publicissapient.kpidashboard.apis.repotools.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.repotools.RepoToolsClient;
import com.publicissapient.kpidashboard.apis.repotools.model.*;
import com.publicissapient.kpidashboard.apis.util.RestAPIUtils;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.generic.Processor;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorItem;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorItemRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.repotools.repository.RepoToolsProviderRepository;
import com.publicissapient.kpidashboard.apis.projectconfig.projecttoolconfig.service.ProjectToolConfigServiceImpl;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import org.springframework.web.client.RestTemplate;

@Service
public class RepoToolsConfigServiceImpl {

	@Autowired
	private ProjectToolConfigRepository projectToolConfigRepository;

	@Autowired
	private RepoToolsProviderRepository repoToolsProviderRepository;

	@Autowired
	CustomApiConfig customApiConfig;

	@Autowired
	private RestAPIUtils restAPIUtils;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private ProcessorItemRepository processorItemRepository;

	@Autowired
	private ProcessorRepository processorRepository;

	@Autowired
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;
	@Autowired
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

	private static final String METRIC = "/metric/";
	public static final String TOOL_BRANCH = "branch";
	public static final String SCM = "scm";
	public static final String REPO_NAME = "repoName";
	public static final String REPO_BRANCH = "defaultBranch";
	public static final String DELETE_REPO = "/project/delete/%s/?only_data=%s";

	private RepoToolsClient repoToolsClient;

	public RepoToolsClient createRepoToolsClient() {
		return new RepoToolsClient();
	}


	/**
	 * enroll a project to the repo tool
	 * @param projectToolConfig
	 * @param connection
	 * @param branchNames
	 * @return
	 */
	public int configureRepoToolProject(ProjectToolConfig projectToolConfig, Connection connection,
			List<String> branchNames) {
		int httpStatus = HttpStatus.NOT_FOUND.value();
		try {

			// create scanning account
			ToolCredential toolCredential = new ToolCredential(connection.getUsername(), connection.getAccessToken(),
					connection.getEmail());
			LocalDateTime fistScan = LocalDateTime.now().minusMonths(6);
			RepoToolsProvider repoToolsProvider = repoToolsProviderRepository
					.findByToolName(connection.getRepoToolProvider().toLowerCase());

			// create configuration details for repo tool
			RepoToolConfig repoToolConfig = new RepoToolConfig(projectToolConfig.getRepositoryName(),
					projectToolConfig.getIsNew(), projectToolConfig.getBasicProjectConfigId().toString(),
					connection.getHttpUrl(), repoToolsProvider.getRepoToolProvider(), connection.getSshUrl(),
					projectToolConfig.getDefaultBranch(),
					createProjectCode(projectToolConfig.getBasicProjectConfigId().toString()),
					fistScan.toString().replace("T", " "), toolCredential, branchNames);

			// api call to enroll the project
			httpStatus = repoToolsClient.enrollProjectCall(repoToolConfig, customApiConfig.getRepoToolURL(),
					restAPIUtils.decryptPassword(customApiConfig.getRepoToolAPIKey()));

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return httpStatus;
	}

	/**
	 * trigger repo tool scanning process
	 * @param basicProjectconfigIdList
	 * @return
	 */
	public int triggerScanRepoToolProject(List<String> basicProjectconfigIdList) {
		int httpStatus = HttpStatus.NOT_FOUND.value();
		Processor processor = processorRepository.findByProcessorName(CommonConstant.REPO_TOOLS);

		// get repo tools configuration from ProjectToolConfig
		List<ProjectToolConfig> projectRepos = projectToolConfigRepository.findByToolNameAndBasicProjectConfigId(
				CommonConstant.REPO_TOOLS, new ObjectId(basicProjectconfigIdList.get(0)));

		List<ProcessorExecutionTraceLog> processorExecutionTraceLogList = new ArrayList<>();

		try {

			List<ProjectToolConfig> projectToolConfigList = projectRepos.stream()
					.filter(projectToolConfig -> projectToolConfig.getBasicProjectConfigId()
							.equals(new ObjectId(basicProjectconfigIdList.get(0))))
					.collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(projectToolConfigList)) {
				String projectCode = createProjectCode(basicProjectconfigIdList.get(0));

				// create ProcessorExecutionTraceLog
				ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLog(
						new ObjectId(basicProjectconfigIdList.get(0)).toHexString());
				processorExecutionTraceLog.setExecutionStartedAt(System.currentTimeMillis());
				repoToolsClient = createRepoToolsClient();

				// api call to start project scanning
				httpStatus = repoToolsClient.triggerScanCall(projectCode, customApiConfig.getRepoToolURL(),
						restAPIUtils.decryptPassword(customApiConfig.getRepoToolAPIKey()));

				// save ProcessorItemRepository for all the ProjectToolConfig
				processorItemRepository.saveAll(createProcessorItemList(projectToolConfigList, processor.getId()));

				if (httpStatus == HttpStatus.OK.value()) {
					// save ProcessorExecutionTraceLog
					processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
					processorExecutionTraceLog.setExecutionSuccess(true);
					processorExecutionTraceLogList.add(processorExecutionTraceLog);
					processorExecutionTraceLogService.save(processorExecutionTraceLog);
				}
			}
		} catch (HttpClientErrorException ex) {
			httpStatus = ex.getStatusCode().value();
		}
		return httpStatus;
	}

	/**
	 * create a project code for repo tool enrollment
	 * @param basicProjectConfigId
	 * @return
	 */
	public String createProjectCode(String basicProjectConfigId) {
		ProjectBasicConfig projectBasicConfig = configHelperService.getProjectConfig(basicProjectConfigId);
		return projectBasicConfig.getProjectName() + "_" + basicProjectConfigId;
	}

	/**
	 * update a project enrolled in repo tool
	 * @param basicProjectConfigId
	 * @param connectionId
	 * @return
	 */
	public boolean updateRepoToolProjectConfiguration(List<ProjectToolConfig> toolList, ObjectId connectionId,
			String basicProjectConfigId) {
		int httpStatus = HttpStatus.NOT_FOUND.value();
		int count = (int) toolList.stream()
				.filter(projectToolConfig -> projectToolConfig.getToolName().equals(CommonConstant.REPO_TOOLS)).count();
		repoToolsClient = createRepoToolsClient();
		if (count > 1) {
			// delete only the repository
			httpStatus = repoToolsClient.deleteRepositories(connectionId.toString(), customApiConfig.getRepoToolURL(),
					restAPIUtils.decryptPassword(customApiConfig.getRepoToolAPIKey()));
		} else {
			// delete the project from repo tool if only one repository is present
			ProjectBasicConfig projectBasicConfig = configHelperService.getProjectConfig(basicProjectConfigId);
			httpStatus = deleteRepoToolProject(projectBasicConfig, false);
		}
		return httpStatus == HttpStatus.OK.value();
	}

	/**
	 * get metrics from repo tool kpis fo different projects
	 * @param projectCode
	 * @param repoToolKpi
	 * @param startDate
	 * @param endDate
	 * @param frequency
	 * @return
	 */
	public List<RepoToolKpiMetricResponse> getRepoToolKpiMetrics(List<String> projectCode, String repoToolKpi,
			String startDate, String endDate, String frequency) {
		repoToolsClient = createRepoToolsClient();
		String repoToolUrl = customApiConfig.getRepoToolURL() + METRIC + repoToolKpi;
		String repoToolApiKey = restAPIUtils.decryptPassword(customApiConfig.getRepoToolAPIKey());
		List<RepoToolKpiMetricResponse> repoToolKpiMetricRespons = new ArrayList<>();
		RepoToolKpiRequestBody repoToolKpiRequestBody = new RepoToolKpiRequestBody(projectCode,
				startDate, endDate, frequency);
		try {
			String url = String.format(repoToolUrl, startDate, endDate, frequency);
			RepoToolKpiBulkMetricResponse repoToolKpiBulkMetricResponse = repoToolsClient.kpiMetricCall(url,
					repoToolApiKey, repoToolKpiRequestBody);
			repoToolKpiMetricRespons = repoToolKpiBulkMetricResponse.getValues().stream().flatMap(List::stream)
					.collect(Collectors.toList());
		} catch (HttpClientErrorException ex) {
			ex.printStackTrace();
		}
		return repoToolKpiMetricRespons;
	}

	/**
	 * create ProcessorItemList for scanning
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
	 * @param projectBasicConfig
	 * @param onlyData
	 * @return
	 */
	public int deleteRepoToolProject(ProjectBasicConfig projectBasicConfig, Boolean onlyData) {
		String deleteUrl = customApiConfig.getRepoToolURL() + String.format(DELETE_REPO,
				projectBasicConfig.getProjectName() + "_" + projectBasicConfig.getId(), onlyData);
		repoToolsClient = createRepoToolsClient();
		return repoToolsClient.deleteProject(deleteUrl,
				restAPIUtils.decryptPassword(customApiConfig.getRepoToolAPIKey()));
	}

	/**
	 * create ProcessorExecutionTraceLog to track repo tool project scan
	 * @param basicProjectConfigId
	 * @return
	 */
	private ProcessorExecutionTraceLog createTraceLog(String basicProjectConfigId) {
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setProcessorName(ProcessorConstants.REPO_TOOLS);
		processorExecutionTraceLog.setBasicProjectConfigId(basicProjectConfigId);
		Optional<ProcessorExecutionTraceLog> existingTraceLogOptional = processorExecutionTraceLogRepository
				.findByProcessorNameAndBasicProjectConfigId(ProcessorConstants.REPO_TOOLS, basicProjectConfigId);
		existingTraceLogOptional.ifPresent(
				existingProcessorExecutionTraceLog -> processorExecutionTraceLog.setLastEnableAssigneeToggleState(
						existingProcessorExecutionTraceLog.isLastEnableAssigneeToggleState()));
		return processorExecutionTraceLog;
	}

}
