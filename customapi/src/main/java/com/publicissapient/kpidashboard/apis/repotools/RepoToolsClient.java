package com.publicissapient.kpidashboard.apis.repotools;

import com.google.gson.Gson;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolConfig;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolKpiBulkMetricResponse;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolKpiMetricResponse;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolKpiRequestBody;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.spring.web.json.Json;

import java.net.URI;
import java.util.List;

/**
 * rest template for repo tools
 */
public class RepoToolsClient {

	private RestTemplate restTemplate;
	private static final String REPO_TOOLS_ENROLL_URL = "/beta/repositories/";

	private static final String REPO_TOOLS_DELETE_REPO_URL = "/beta/repositories/%s";
	private static final String REPO_TOOLS_TRIGGER_SCAN_URL = "/metric/%s/trigger-scan";

	private static final String X_API_KEY = "X_API_KEY";
	private HttpHeaders httpHeaders;

	public RepoToolsClient() {
		this.restTemplate = new RestTemplate();
	}

	/**
	 * enroll project
	 * @param repoToolConfig
	 * @param repoToolsUrl
	 * @param apiKey
	 * @return http status
	 */
	public int enrollProjectCall(RepoToolConfig repoToolConfig, String repoToolsUrl, String apiKey) {
		setHttpHeaders(apiKey);
		Gson gson = new Gson();
		String payload = gson.toJson(repoToolConfig);
		URI url = URI.create(repoToolsUrl + REPO_TOOLS_ENROLL_URL);
		HttpEntity<String> entity = new HttpEntity<>(payload, httpHeaders);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
		return response.getStatusCode().value();
	}

	/**
	 * scann a project
	 * @param projectKey
	 * @param repoToolsUrl
	 * @param apiKey
	 * @return http status
	 */
	public int triggerScanCall(String projectKey, String repoToolsUrl, String apiKey) {
		setHttpHeaders(apiKey);
		String triggerScanUrl = String.format(REPO_TOOLS_TRIGGER_SCAN_URL, projectKey);
		URI url = URI.create(repoToolsUrl + triggerScanUrl);
		HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
		return response.getStatusCode().value();

	}

	/**
	 * get kpi data of a project
	 * @param repoToolsUrl
	 * @param apiKey
	 * @param repoToolKpiRequestBody
	 * @return kpi data
	 */
	public RepoToolKpiBulkMetricResponse kpiMetricCall(String repoToolsUrl, String apiKey,
			RepoToolKpiRequestBody repoToolKpiRequestBody) {
		setHttpHeaders(apiKey);
		Gson gson = new Gson();
		String payload = gson.toJson(repoToolKpiRequestBody);
		HttpEntity<String> entity = new HttpEntity<>(payload, httpHeaders);
		ResponseEntity<RepoToolKpiBulkMetricResponse> response = restTemplate.exchange(URI.create(repoToolsUrl),
				HttpMethod.POST, entity, RepoToolKpiBulkMetricResponse.class);
		return response.getBody();

	}

	/**
	 * delete a project
	 * @param repoToolsUrl
	 * @param apiKey
	 * @return http status
	 */
	public int deleteProject(String repoToolsUrl, String apiKey) {
		setHttpHeaders(apiKey);
		HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
		ResponseEntity<JsonNode> response = restTemplate.exchange(URI.create(repoToolsUrl), HttpMethod.DELETE, entity,
				JsonNode.class);
		return response.getStatusCode().value();
	}

	/**
	 * delete repository of the project
	 * @param masterSystemId
	 * @param repoToolsUrl
	 * @param apiKey
	 * @return http status
	 */
	public int deleteRepositories(String masterSystemId, String repoToolsUrl, String apiKey) {
		String deleteRepoUrl = String.format(REPO_TOOLS_DELETE_REPO_URL, masterSystemId);
		setHttpHeaders(apiKey);
		URI url = URI.create(repoToolsUrl + deleteRepoUrl);
		HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
		return response.getStatusCode().value();

	}

	/**
	 * set headers for api call
	 * @param apiKey
	 */
	public void setHttpHeaders(String apiKey) {
		httpHeaders = new HttpHeaders();
		httpHeaders.add(X_API_KEY, apiKey);
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
	}

}
