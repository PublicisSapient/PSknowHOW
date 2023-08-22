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

import java.util.List;

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

	public int enrollProjectCall(RepoToolConfig repoToolConfig, String REPO_TOOLSUrl, String apiKey) {
		setHttpHeaders(apiKey);
		Gson gson = new Gson();
		String payload = gson.toJson(repoToolConfig);
		HttpEntity<String> entity = new HttpEntity<>(payload, httpHeaders);
		ResponseEntity<String> response = restTemplate.exchange(REPO_TOOLSUrl + REPO_TOOLS_ENROLL_URL, HttpMethod.POST,
				entity, String.class);
		return response.getStatusCode().value();
	}

	public int deleteRepositories(String masterSystemId, String REPO_TOOLSUrl, String apiKey) {
		String deleteRepoUrl = String.format(REPO_TOOLS_DELETE_REPO_URL, masterSystemId);
		setHttpHeaders(apiKey);
		HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
		ResponseEntity<String> response = restTemplate.exchange(REPO_TOOLSUrl + deleteRepoUrl, HttpMethod.DELETE,
				entity, String.class);
		return response.getStatusCode().value();

	}

	public int triggerScanCall(String projectKey, String REPO_TOOLSUrl, String apiKey) {
		setHttpHeaders(apiKey);
		String triggerScanUrl = String.format(REPO_TOOLS_TRIGGER_SCAN_URL, projectKey);
		HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
		ResponseEntity<String> response = restTemplate.exchange(REPO_TOOLSUrl + triggerScanUrl, HttpMethod.GET, entity,
				String.class);
		return response.getStatusCode().value();

	}

	public RepoToolKpiBulkMetricResponse kpiMetricCall(String REPO_TOOLSKpiUrl, String apiKey,
			RepoToolKpiRequestBody repoToolKpiRequestBody) {
		setHttpHeaders(apiKey);
		Gson gson = new Gson();
		String payload = gson.toJson(repoToolKpiRequestBody);
		HttpEntity<String> entity = new HttpEntity<>(payload, httpHeaders);
		ResponseEntity<RepoToolKpiBulkMetricResponse> response = restTemplate.exchange(REPO_TOOLSKpiUrl, HttpMethod.POST,
				entity, RepoToolKpiBulkMetricResponse.class);
		return response.getBody();

	}

	public List<RepoToolKpiMetricResponse> kpiMetricRepoActivityCall(String projectKey, String REPO_TOOLSKpiUrl,
			String apiKey) {
		setHttpHeaders(apiKey);
		REPO_TOOLSKpiUrl = String.format(REPO_TOOLSKpiUrl, projectKey);
		HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
		ResponseEntity<RepoToolKpiBulkMetricResponse> response = restTemplate.exchange(REPO_TOOLSKpiUrl, HttpMethod.GET,
				entity, RepoToolKpiBulkMetricResponse.class);
		RepoToolKpiBulkMetricResponse repoToolKpiBulkMetricResponse = response.getBody();
		return repoToolKpiBulkMetricResponse.getValues().get(0);

	}

	public int deleteProject(String REPO_TOOLSUrl, String apiKey) {
		setHttpHeaders(apiKey);
		HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
		ResponseEntity<JsonNode> response = restTemplate.exchange(REPO_TOOLSUrl, HttpMethod.DELETE, entity,
				JsonNode.class);
		return response.getStatusCode().value();
	}

	public void setHttpHeaders(String apiKey) {
		httpHeaders = new HttpHeaders();
		httpHeaders.add(X_API_KEY, apiKey);
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
	}

}
