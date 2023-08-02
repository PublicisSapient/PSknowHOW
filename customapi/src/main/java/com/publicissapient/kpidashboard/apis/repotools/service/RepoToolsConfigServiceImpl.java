package com.publicissapient.kpidashboard.apis.repotools.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.repotools.RepoToolsClient;
import com.publicissapient.kpidashboard.apis.repotools.model.*;
import com.publicissapient.kpidashboard.apis.util.RestAPIUtils;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.generic.Processor;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorItem;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorItemRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorRepository;
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
	private RepoToolsProviderRepository repoToolsProviderRepository;

	@Autowired
	private ProjectBasicConfigRepository projectBasicConfigRepository;
	
	@Autowired
	CustomApiConfig customApiConfig;

	@Autowired
	private RestAPIUtils restAPIUtils;

    @Autowired
    private ConnectionRepository connectionRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private ProcessorItemRepository processorItemRepository;

	@Autowired
	private ProcessorRepository processorRepository;

	private static final String QUERY_PARAMS = "?projects=%s&start_date=%s&end_date=%s&frequency=%s";
	private static final String METRIC = "/metric/";
	public static final String TOOL_BRANCH = "branch";
	public static final String SCM = "scm";
	public static final String REPO_NAME = "repoName";
	public static final String REPO_BRANCH = "defaultBranch";
	public static final String DELETE_REPO = "/project/delete/%s/?only_data=%s";


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
					connection.getBaseUrl(), repoToolsProvider.getRepoToolProvider(), connection.getSshUrl(),
					projectToolConfig.getBranch(),
					createProjectCode(projectToolConfig.getBasicProjectConfigId().toString()),
					fistScan.toString().replace("T", " "), toolCredential, branchNames);
			RepoToolsClient repoToolsClient = new RepoToolsClient();
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
		List<ProjectToolConfig> projectRepos = basicProjectconfigIdList.stream()
				.map(id -> projectToolConfigService.getProjectToolConfigsByConfigIdAndType(id, CommonConstant.REPO_TOOLS))
				.flatMap(List::stream).collect(Collectors.toList());
		try {
			for (ProjectToolConfig projectToolConfig : projectRepos) {
				String projectRepoName = projectToolConfig.getRepositoryName();
				if (projectRepoName != null) {
					String projectCode = createProjectCode(projectToolConfig.getBasicProjectConfigId().toString());
					RepoToolsClient repoToolsClient = new RepoToolsClient();
					httpStatus = repoToolsClient.triggerScanCall(projectCode, customApiConfig.getRepoToolURL(),
							restAPIUtils.decryptPassword(customApiConfig.getRepoToolAPIKey()));
					processorItemRepository.save(createProcessorItem(projectToolConfig, processor.getId()));
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
		RepoToolsClient repoToolsClient = new RepoToolsClient();
		if (projectToolConfigList.size() == 1) {
			String masterSystemId = basicProjectConfigId + connectionId.toString();
			httpStatus = repoToolsClient.deleteRepositories(masterSystemId, customApiConfig.getRepoToolURL(),
					restAPIUtils.decryptPassword(customApiConfig.getRepoToolAPIKey()));
		} else {
			List<String> branchNames = projectToolConfigList.stream().map(ProjectToolConfig::getBranch)
					.filter(Objects::nonNull).collect(Collectors.toList());
			Connection connection = connectionRepository.findById(connectionId).get();
			httpStatus = configureRepoToolProject(projectToolConfigList.get(0), connection, branchNames);
		}
		return httpStatus != HttpStatus.NOT_FOUND.value();
	}

	public List<RepoToolKpiMetricResponse> getRepoToolKpiMetrics(List<String> projectCodeList, String repoToolKpi,
															   String startDate, String endDate, String frequency) {
		RepoToolsClient repoToolsClient = new RepoToolsClient();
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
	

	private ProcessorItem createProcessorItem(ProjectToolConfig tool, ObjectId processorId) {
		ProcessorItem item = new ProcessorItem();
		item.setToolConfigId(tool.getId());
		item.setProcessorId(processorId);
		item.setActive(Boolean.TRUE);
		item.getToolDetailsMap().put(TOOL_BRANCH, tool.getBranch());
		item.getToolDetailsMap().put(SCM, tool.getToolName());
		item.getToolDetailsMap().put(REPO_NAME, tool.getRepositoryName());
		item.getToolDetailsMap().put(REPO_BRANCH, tool.getDefaultBranch());
		return item;
	}

	public void deleteRepoToolProject(ProjectBasicConfig projectBasicConfig, Boolean onlyData) {
		String deleteUrl = customApiConfig.getRepoToolURL() + String.format(DELETE_REPO,
				projectBasicConfig.getProjectName() + "_" + projectBasicConfig.getId(), onlyData);
		RepoToolsClient repoToolsClient = new RepoToolsClient();
		repoToolsClient.deleteProject(deleteUrl, customApiConfig.getRepoToolAPIKey());
	}

}
