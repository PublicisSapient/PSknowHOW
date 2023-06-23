package com.publicissapient.kpidashboard.apis.debbie.service;

import java.util.List;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.debbie.DebbieClient;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.debbie.model.DebbieConfig;
import com.publicissapient.kpidashboard.apis.debbie.model.DebbieTools;
import com.publicissapient.kpidashboard.apis.debbie.repository.DebbieToolsRepository;
import com.publicissapient.kpidashboard.apis.projectconfig.projecttoolconfig.service.ProjectToolConfigServiceImpl;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfigDTO;
import com.publicissapient.kpidashboard.common.model.connection.Connection;

@Service
public class DebbieConfigServiceImpl {

	@Autowired
	private ProjectToolConfigServiceImpl projectToolConfigService;

	@Autowired
	private DebbieToolsRepository debbieToolsRepository;

	public int configureDebbieProject(ProjectToolConfig projectToolConfig, Connection connection, List<String> branchNames) {
		int httpStatus = HttpStatus.NOT_FOUND.value();
		try {
			ToolCredential toolCredential = new ToolCredential(connection.getUsername(), connection.getAccessToken(),
					connection.getEmail());
			DebbieTools debbieTools = debbieToolsRepository.findByToolName(connection.getType());
			DebbieConfig debbieConfig = new DebbieConfig(projectToolConfig.getRepositoryName(),
					projectToolConfig.getIsNew(), projectToolConfig.getBasicProjectConfigId().toString(),
					connection.getBaseUrl(), debbieTools.getDebbieProvider(), connection.getHttpUrl(),
					projectToolConfig.getBranch(), createProjectCode(projectToolConfig.getRepositoryName(),
							projectToolConfig.getBasicProjectConfigId()),
					toolCredential);
			DebbieClient debbieClient = new DebbieClient();
			httpStatus = debbieClient.enrollProjectCall(debbieConfig);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return httpStatus;
	}

	public int triggerScanDebbieProject(List<String> basicProjectconfigIdList) {
		int httpStatus = HttpStatus.NOT_FOUND.value();
		List<ProjectToolConfigDTO> projectRepos = basicProjectconfigIdList.stream()
				.map(id -> projectToolConfigService.getProjectToolConfigs(id, CommonConstant.DEBBIE_TOOLS))
				.flatMap(List::stream).collect(Collectors.toList());
		try {
			for (ProjectToolConfigDTO projectToolConfigDTO : projectRepos) {
				String projectRepoName = projectToolConfigDTO.getRepositoryName();
				if (projectRepoName != null) {
					String projectCode = createProjectCode(projectToolConfigDTO.getRepositoryName(),
							projectToolConfigDTO.getBasicProjectConfigId());
					DebbieClient debbieClient = new DebbieClient();
					httpStatus = debbieClient.triggerScanCall(projectCode);
				}
			}
		} catch (HttpClientErrorException ex) {
			httpStatus = ex.getStatusCode().value();
		}
		return httpStatus;
	}

	public String createProjectCode(String repoName, ObjectId basicProjectConfigId) {
		return repoName + "_" + basicProjectConfigId.toString();
	}
}
