package com.publicissapient.kpidashboard.apis.githubaction.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.githubaction.model.GithubActionWorkflowsDTO;
import com.publicissapient.kpidashboard.apis.util.RestAPIUtils;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GithubActionToolConfigServiceImpl {

	public static final String WORKFLOWS = "workflows";
	public static final String ID = "id";
	public static final String PATH = "path";
	private static final String RESOURCE_JOBS_ENDPOINT = "/actions/workflows";
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private RestAPIUtils restAPIUtils;
	@Autowired
	private ConnectionRepository connectionRepository;
	@Autowired
	private AesEncryptionService aesEncryptionService;
	@Autowired
	private CustomApiConfig customApiConfig;

	public List<GithubActionWorkflowsDTO> getGitHubWorkFlowList(String connectionId, String repoName) {

		List<GithubActionWorkflowsDTO> responseDTOList = new ArrayList<>();
		Optional<Connection> optConnection = connectionRepository.findById(new ObjectId(connectionId));
		if (optConnection.isPresent()) {
			Connection connection = optConnection.get();
			String baseUrl = connection.getBaseUrl() == null ? null : connection.getBaseUrl().trim();
			String repositoryName = repoName;
			String repositoryOwner = connection.getUsername() == null ? null : connection.getUsername().trim();
			String accessToken = connection.getAccessToken() == null ? null
					: aesEncryptionService.decrypt(connection.getAccessToken(), customApiConfig.getAesEncryptionKey());

			String url = baseUrl + "/repos/" + repositoryOwner + "/" + repositoryName + RESOURCE_JOBS_ENDPOINT;

			HttpEntity<?> httpEntity = new HttpEntity<>(RestAPIUtils.getHeaders(accessToken, true));
			try {

				ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);

				if (response.getStatusCode() == HttpStatus.OK) {
					JSONParser respParser = new JSONParser();
					JSONObject object = (JSONObject) respParser.parse(response.getBody());
					JSONArray workflows = restAPIUtils.getJsonArrayFromJSONObj(object, WORKFLOWS);

					for (Object job : workflows) {
						GithubActionWorkflowsDTO githubActionWorkflowsDTO = new GithubActionWorkflowsDTO();
						final String workflowId = restAPIUtils.convertToString((JSONObject) job, ID);
						final String path = restAPIUtils.convertToString((JSONObject) job, PATH);
						int jobIndex = path.indexOf(WORKFLOWS);
						String jobName = path.substring(jobIndex + 10, path.length() - 5);
						githubActionWorkflowsDTO.setWorkflowName(jobName);
						githubActionWorkflowsDTO.setWorkflowID(workflowId);
						responseDTOList.add(githubActionWorkflowsDTO);
					}

				} else {
					String statusCode = response.getStatusCode().toString();
					log.error("Error while fetching getJenkinsJobNameList from {}. with status {}", url, statusCode);
				}

			} catch (Exception exception) {
				log.error("Error while fetching getJenkinsJobNameList from {}:  {}", url, exception.getMessage());
			}
		}
		return responseDTOList;
	}
}
