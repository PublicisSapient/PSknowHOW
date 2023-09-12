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

package com.publicissapient.kpidashboard.apis.repotools;

import java.net.URI;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolConfig;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolKpiBulkMetricResponse;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolKpiRequestBody;

import lombok.extern.slf4j.Slf4j;

/**
 * rest template for repo tools
 */
@Slf4j
public class RepoToolsClient {

	private RestTemplate restTemplate;
	private static final String X_API_KEY = "X_API_KEY";
	private HttpHeaders httpHeaders;

	/**
	 * enroll project
	 * 
	 * @param repoToolConfig
	 * @param repoToolsUrl
	 * @param apiKey
	 * @return http status
	 */
	public int enrollProjectCall(RepoToolConfig repoToolConfig, String repoToolsUrl, String apiKey) {
		setHttpHeaders(apiKey);
		Gson gson = new Gson();
		String payload = gson.toJson(repoToolConfig);
		log.info("enroll project request {} {}", repoToolsUrl, repoToolConfig);
		URI url = URI.create(repoToolsUrl);
		HttpEntity<String> entity = new HttpEntity<>(payload, httpHeaders);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
		log.debug(response.getBody());
		return response.getStatusCode().value();
	}

	/**
	 * scann a project
	 * 
	 * @param projectKey
	 * @param repoToolsUrl
	 * @param apiKey
	 * @return http status
	 */
	public int triggerScanCall(String projectKey, String repoToolsUrl, String apiKey) {
		setHttpHeaders(apiKey);
		String triggerScanUrl = String.format(repoToolsUrl, projectKey);
		log.info("trigger project scan request {} {}", triggerScanUrl);
		HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
		ResponseEntity<String> response = restTemplate.exchange(triggerScanUrl, HttpMethod.GET, entity, String.class);
		return response.getStatusCode().value();

	}

	/**
	 * get kpi data of a project
	 * 
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
		log.info("kpi request payload for {} {}", repoToolsUrl, repoToolKpiRequestBody.toString());
		HttpEntity<String> entity = new HttpEntity<>(payload, httpHeaders);
		ResponseEntity<RepoToolKpiBulkMetricResponse> response = restTemplate.exchange(URI.create(repoToolsUrl),
				HttpMethod.POST, entity, RepoToolKpiBulkMetricResponse.class);
		return response.getBody();

	}

	/**
	 * delete a project
	 * 
	 * @param repoToolsUrl
	 * @param apiKey
	 * @return http status
	 */
	public int deleteProject(String repoToolsUrl, String apiKey) {
		setHttpHeaders(apiKey);
		HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
		log.info("delete project request {}", repoToolsUrl);
		ResponseEntity<JsonNode> response = restTemplate.exchange(URI.create(repoToolsUrl), HttpMethod.DELETE, entity,
				JsonNode.class);
		return response.getStatusCode().value();
	}

	/**
	 * delete repository of the project
	 * 
	 * @param deleteRepoUrl
	 * @param apiKey
	 * @return http status
	 */
	public int deleteRepositories(String deleteRepoUrl, String apiKey) {
		setHttpHeaders(apiKey);
		log.info("delete project request {}", deleteRepoUrl);
		URI url = URI.create(deleteRepoUrl);
		HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
		return response.getStatusCode().value();

	}

	/**
	 * set headers for api call
	 * 
	 * @param apiKey
	 */
	public void setHttpHeaders(String apiKey) {
		httpHeaders = new HttpHeaders();
		this.restTemplate = new RestTemplate();
		httpHeaders.add(X_API_KEY, apiKey);
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
	}

}
