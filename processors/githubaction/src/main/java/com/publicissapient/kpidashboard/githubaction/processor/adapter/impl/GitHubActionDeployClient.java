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

package com.publicissapient.kpidashboard.githubaction.processor.adapter.impl;

import static com.publicissapient.kpidashboard.common.util.DateUtil.TIME_FORMAT;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.common.constant.DeploymentStatus;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.Deployment;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import com.publicissapient.kpidashboard.githubaction.config.Constants;
import com.publicissapient.kpidashboard.githubaction.config.GitHubActionConfig;
import com.publicissapient.kpidashboard.githubaction.customexception.FetchingBuildException;
import com.publicissapient.kpidashboard.githubaction.processor.adapter.GitHubActionClient;
import com.publicissapient.kpidashboard.githubaction.util.GitHubActionURIBuilder;
import com.publicissapient.kpidashboard.githubaction.util.ProcessorUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GitHubActionDeployClient implements GitHubActionClient {

	private static final String PAGE_PARAM = "?page=";
	private static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	@Autowired
	private AesEncryptionService aesEncryptionService;
	@Autowired
	private GitHubActionConfig gitHubActionConfig;
	@Autowired
	private RestTemplate restTemplate;

	@Override
	public Set<Build> getBuildJobsFromServer(ProcessorToolConnection gitHubServer, ProjectBasicConfig proBasicConfig)
			throws FetchingBuildException {
		return new HashSet<>();
	}

	@Override
	public Map<Deployment, Set<Deployment>> getDeployJobsFromServer(ProcessorToolConnection gitHubServer,
			ProjectBasicConfig proBasicConfig) throws FetchingBuildException {
		log.debug("Enter getDeployJobsFromServer");
		String restUri = null;
		Map<Deployment, Set<Deployment>> deploys = new LinkedHashMap<>();
		try {
			String decryptedApiToken = decryptApiToken(gitHubServer.getAccessToken());
			String restUrl = new GitHubActionURIBuilder(gitHubServer).deployGithub();
			restUri = URLDecoder.decode(restUrl, "UTF-8");
			log.debug("REST URL {}", restUri);
			boolean hasMorePage = true;
			int nextPage = 1;
			while (hasMorePage) {

				ResponseEntity<String> respPayload = getResponse(gitHubServer.getUsername(), decryptedApiToken,
						restUri);
				if (respPayload == null)
					break;
				JSONArray responseJson = getJSONFromResponse(respPayload.getBody());
				initializeDeployments(deploys, responseJson, gitHubServer, decryptedApiToken);
				nextPage++;
				if (StringUtils.containsIgnoreCase(restUri, PAGE_PARAM)) {
					restUri = restUri.replace(PAGE_PARAM + (nextPage - 1), PAGE_PARAM + nextPage);
				} else {
					restUri = restUri.concat(PAGE_PARAM + nextPage);
				}
				if (responseJson.isEmpty()) {
					hasMorePage = false;
				}

			}

		} catch (RestClientException | URISyntaxException | UnsupportedEncodingException | ParseException e) {
			log.error("Error Fetching the jobs on instance: {}, {}", gitHubServer.getUrl(), e);
			throw new FetchingBuildException("Failed to fetch builds", e);
		}
		return deploys;
	}

	private void initializeDeployments(Map<Deployment, Set<Deployment>> result, JSONArray jsonArray,
			ProcessorToolConnection gitHubServer, String decryptedApiToken) throws ParseException {

		for (Object jsonObj : jsonArray) {
			JSONObject deploymentObject = (JSONObject) jsonObj;

			String startDate = String.valueOf(deploymentObject.get(Constants.CREATED_AT));
			String endDate = String.valueOf(deploymentObject.get(Constants.UPDATED_AT));
			String number = String.valueOf(deploymentObject.get(Constants.DEPLOYNUMBER));
			String env = ProcessorUtils.getString(deploymentObject, Constants.ENVIRONMENT);
			long createdDate = Instant.parse(ProcessorUtils.getString(deploymentObject, Constants.CREATED_AT))
					.toEpochMilli();
			long updatedDate = Instant.parse(ProcessorUtils.getString(deploymentObject, Constants.UPDATED_AT))
					.toEpochMilli();

			Deployment deployment = new Deployment();
			deployment.setProjectToolConfigId(gitHubServer.getId());
			deployment.setBasicProjectConfigId(gitHubServer.getBasicProjectConfigId());
			deployment.setEnvName(env);
			deployment.setNumber(number);

			String statusesURL = ProcessorUtils.getString(deploymentObject, Constants.STATUSES_URL);
			ResponseEntity<String> respPayload = getResponse(gitHubServer.getUsername(), decryptedApiToken,
					statusesURL);
			if (respPayload != null) {
				JSONArray responseJson = getJSONFromResponse(respPayload.getBody());
				JSONObject statusObject = (JSONObject) responseJson.get(0);
				deployment.setDeploymentStatus(getDeploymentStatus(statusObject));
			}

			if (StringUtils.isNotEmpty(startDate)) {

				deployment.setStartTime(DateUtil.dateTimeConverter(startDate, DATETIME_FORMAT, TIME_FORMAT));
				deployment.setEndTime(DateUtil.dateTimeConverter(endDate, DATETIME_FORMAT, TIME_FORMAT));
				deployment.setDuration(updatedDate - createdDate);
			}

			if (checkDeploymentConditionsNotNull(deployment)) {
				if (result.containsKey(deployment)) {
					Set<Deployment> deploymentSet = result.get(deployment);
					deploymentSet.add(deployment);
				} else {
					Set<Deployment> deploymentSet = new HashSet<>();
					deploymentSet.add(deployment);
					result.put(deployment, deploymentSet);
				}
			}

		}
	}

	protected String decryptApiToken(String apiToken) {
		return StringUtils.isNotEmpty(apiToken)
				? aesEncryptionService.decrypt(apiToken, gitHubActionConfig.getAesEncryptionKey())
				: "";
	}

	protected ResponseEntity<String> getResponse(String userName, String apiToken, String url) {
		HttpEntity<HttpHeaders> httpEntity = null;
		if (userName != null && apiToken != null) {

			final HttpHeaders privateToken = new HttpHeaders();
			privateToken.set("Authorization", "token " + apiToken);
			httpEntity = new HttpEntity<>(privateToken);
		}
		return restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
	}

	protected JSONArray getJSONFromResponse(String payload) throws ParseException {
		JSONParser parser = new JSONParser();
		return (JSONArray) parser.parse(payload);
	}

	private boolean checkDeploymentConditionsNotNull(Deployment deployment) {
		if (deployment.getEnvName() == null || deployment.getStartTime() == null || deployment.getEndTime() == null) {
			log.error("deployments conditions not satisfied so that data is not saved in db {}", deployment);
			return false;
		} else {
			return true;
		}
	}

	private DeploymentStatus getDeploymentStatus(JSONObject jsonDeploy) {
		String status = String.valueOf(jsonDeploy.get(Constants.DEPLOYMENTSTATUS));
		switch (status) {
		case "success":
			return DeploymentStatus.SUCCESS;
		case "error":
			return DeploymentStatus.FAILURE;
		case "in_progress":
			return DeploymentStatus.IN_PROGRESS;
		case "failure":
			return DeploymentStatus.FAILURE;
		case "inactive":
			return DeploymentStatus.INACTIVE;
		default:
			return DeploymentStatus.UNKNOWN;
		}
	}

}
