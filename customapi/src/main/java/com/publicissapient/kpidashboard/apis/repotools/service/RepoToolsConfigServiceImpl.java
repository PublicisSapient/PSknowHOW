package com.publicissapient.kpidashboard.apis.repotools.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.repotools.repository.RepoToolsProviderRepository;
import com.publicissapient.kpidashboard.apis.projectconfig.projecttoolconfig.service.ProjectToolConfigServiceImpl;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;

@Service
public class RepoToolsConfigServiceImpl {

	@Autowired
	private ProjectToolConfigServiceImpl projectToolConfigService;

	@Autowired
	private ProjectToolConfigRepository projectToolConfigRepository;

	@Autowired
	private RepoToolsProviderRepository repoToolsProviderRepository;

	@Autowired
	private ProjectBasicConfigRepository projectBasicConfigRepository;
	
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

	private static final String QUERY_PARAMS = "?projects=%s&start_date=%s&end_date=%s&frequency=%s";
	private static final String METRIC = "/metric/";
	public static final String TOOL_BRANCH = "branch";
	public static final String SCM = "scm";
	public static final String REPO_NAME = "repoName";
	public static final String REPO_BRANCH = "defaultBranch";
	public static final String DELETE_REPO = "/project/delete/%s/?only_data=%s";


	private RepoToolsClient repoToolsClient; // Declare RepoToolsClient instance

	// Other constants and methods

	public RepoToolsClient createRepoToolsClient() {
		return new RepoToolsClient(); // Instantiate and return a new RepoToolsClient
	}


	public int configureRepoToolProject(ProjectToolConfig projectToolConfig, Connection connection,
			List<String> branchNames) {
		int httpStatus = HttpStatus.NOT_FOUND.value();
		try {
			ToolCredential toolCredential = new ToolCredential(connection.getUsername(), connection.getAccessToken(),
					connection.getEmail());
			LocalDateTime fistScan = LocalDateTime.now().minusMonths(6);
			RepoToolsProvider repoToolsProvider = repoToolsProviderRepository
					.findByToolName(connection.getRepoToolProvider().toLowerCase());
			RepoToolConfig repoToolConfig = new RepoToolConfig(projectToolConfig.getRepositoryName(),
					projectToolConfig.getIsNew(), projectToolConfig.getBasicProjectConfigId().toString(),
					connection.getHttpUrl(), repoToolsProvider.getRepoToolProvider(), connection.getSshUrl(),
					projectToolConfig.getDefaultBranch(),
					createProjectCode(projectToolConfig.getBasicProjectConfigId().toString()),
					fistScan.toString().replace("T", " "), toolCredential, branchNames);
			repoToolsClient = createRepoToolsClient();
			httpStatus = repoToolsClient.enrollProjectCall(repoToolConfig, customApiConfig.getRepoToolURL(),
					restAPIUtils.decryptPassword(customApiConfig.getRepoToolAPIKey()));

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return httpStatus;
	}

	public int triggerScanRepoToolProject(List<String> basicProjectconfigIdList) {
		int httpStatus = HttpStatus.NOT_FOUND.value();
		Processor processor = processorRepository.findByProcessorName(CommonConstant.REPO_TOOLS);
		List<ProjectToolConfig> projectRepos = basicProjectconfigIdList
				.stream().map(id -> projectToolConfigRepository
						.findByToolNameAndBasicProjectConfigId(CommonConstant.REPO_TOOLS, new ObjectId(id)))
				.flatMap(List::stream).collect(Collectors.toList());

		List<ProcessorExecutionTraceLog> processorExecutionTraceLogList = new ArrayList<>();

		try {
			for (String basicProjectconfigId : basicProjectconfigIdList) {

				List<ProjectToolConfig> projectToolConfigList = projectRepos.stream()
						.filter(projectToolConfig -> projectToolConfig.getBasicProjectConfigId()
								.equals(new ObjectId(basicProjectconfigId)))
						.collect(Collectors.toList());
				if (CollectionUtils.isNotEmpty(projectToolConfigList)) {
					String projectCode = createProjectCode(basicProjectconfigId);
					ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLog(
							new ObjectId(basicProjectconfigId).toHexString());
					processorExecutionTraceLog.setExecutionStartedAt(System.currentTimeMillis());
					repoToolsClient = createRepoToolsClient();
					httpStatus = repoToolsClient.triggerScanCall(projectCode, customApiConfig.getRepoToolURL(),
							restAPIUtils.decryptPassword(customApiConfig.getRepoToolAPIKey()));
					processorItemRepository.saveAll(createProcessorItemList(projectToolConfigList, processor.getId()));
					if (httpStatus == HttpStatus.OK.value()) {
						processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
						processorExecutionTraceLog.setExecutionSuccess(true);
						processorExecutionTraceLogList.add(processorExecutionTraceLog);
						processorExecutionTraceLogService.save(processorExecutionTraceLog);
					}
				}
			}
		} catch (HttpClientErrorException ex) {
			httpStatus = ex.getStatusCode().value();
		}
		return httpStatus;
	}

	public String createProjectCode(String basicProjectConfigId) {
		ProjectBasicConfig projectBasicConfig = configHelperService.getProjectConfig(basicProjectConfigId);
		return projectBasicConfig.getProjectName() + "_" + basicProjectConfigId;
	}

	public boolean updateRepoToolProjectConfiguration(String basicProjectConfigId, ObjectId connectionId) {
		int httpStatus = HttpStatus.NOT_FOUND.value();
		List<ProjectToolConfig> projectToolConfigList = projectToolConfigService
				.getProjectToolConfigsByConnectionId(basicProjectConfigId, connectionId);
		repoToolsClient = createRepoToolsClient();
		if (projectToolConfigList.size() == 1) {
			ProjectBasicConfig projectBasicConfig = configHelperService.getProjectConfig(basicProjectConfigId);
			httpStatus = deleteRepoToolProject(projectBasicConfig, false);
		} else {
			String masterSystemId = basicProjectConfigId + connectionId.toString();
			httpStatus = repoToolsClient.deleteRepositories(masterSystemId, customApiConfig.getRepoToolURL(),
					restAPIUtils.decryptPassword(customApiConfig.getRepoToolAPIKey()));
		}
		return httpStatus == HttpStatus.OK.value();
	}

	public List<RepoToolKpiMetricResponse> getRepoToolKpiMetrics(List<String> projectCodeList, String repoToolKpi,
															   String startDate, String endDate, String frequency) {
		repoToolsClient = createRepoToolsClient();
		String repoToolUrl = customApiConfig.getRepoToolURL() + METRIC + repoToolKpi + QUERY_PARAMS;
		String repoToolApiKey = restAPIUtils.decryptPassword(customApiConfig.getRepoToolAPIKey());
		List<RepoToolKpiMetricResponse> repoToolKpiMetricRespons = new ArrayList<>();
		StringJoiner projectCodeParam = new StringJoiner(",", "[\"", "\"]");
		for (String project : projectCodeList) {
			projectCodeParam.add(project);
		}
		try {
			String url = String.format(repoToolUrl, projectCodeParam, startDate, endDate, frequency);
			RepoToolKpiBulkMetricResponse repoToolKpiBulkMetricResponse = repoToolsClient.kpiMetricCall(url, repoToolApiKey);
			repoToolKpiMetricRespons = repoToolKpiBulkMetricResponse.getValues().stream().flatMap(List::stream)
					.collect(Collectors.toList());
		} catch (HttpClientErrorException ex) {
			ex.printStackTrace();
		}
		return repoToolKpiMetricRespons;
	}
	

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

	public int  deleteRepoToolProject(ProjectBasicConfig projectBasicConfig, Boolean onlyData) {
		String deleteUrl = customApiConfig.getRepoToolURL() + String.format(DELETE_REPO,
				projectBasicConfig.getProjectName() + "_" + projectBasicConfig.getId(), onlyData);
		repoToolsClient = createRepoToolsClient();
		return  repoToolsClient.deleteProject(deleteUrl, restAPIUtils.decryptPassword(customApiConfig.getRepoToolAPIKey()));
	}

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
