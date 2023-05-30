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

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashSet;
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

import com.publicissapient.kpidashboard.common.constant.BuildStatus;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.Deployment;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.githubaction.config.Constants;
import com.publicissapient.kpidashboard.githubaction.config.GitHubActionConfig;
import com.publicissapient.kpidashboard.githubaction.customexception.FetchingBuildException;
import com.publicissapient.kpidashboard.githubaction.processor.adapter.GitHubActionClient;
import com.publicissapient.kpidashboard.githubaction.util.GitHubActionURIBuilder;
import com.publicissapient.kpidashboard.githubaction.util.ProcessorUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GitHubActionBuildClient implements GitHubActionClient {

	public static final String WORKFLOW_RUNS = "workflow_runs";
	private static final String PAGE_PARAM = "?page=";
	@Autowired
	private AesEncryptionService aesEncryptionService;
	@Autowired
	private GitHubActionConfig gitHubActionConfig;
	@Autowired
	private RestTemplate restTemplate;

	@Override
	public Set<Build> getBuildJobsFromServer(ProcessorToolConnection githubServer, ProjectBasicConfig proBasicConfig)
			throws FetchingBuildException {
		log.debug("Enter getBuildJobsFromServer");
		String restUri = null;
		Set<Build> builds = new LinkedHashSet<>();
		try {
			String decryptedApiToken = decryptApiToken(githubServer.getAccessToken());
			String restUrl = new GitHubActionURIBuilder(githubServer).build();
			restUri = URLDecoder.decode(restUrl, "UTF-8");
			log.debug("REST URL {}", restUri);
			boolean hasMorePage = true;
			int nextPage = 1;
			while (hasMorePage) {

				ResponseEntity<String> respPayload = getResponse(githubServer.getUsername(), decryptedApiToken,
						restUri);
				if (respPayload == null)
					break;
				JSONArray responseJson = getJSONFromResponse(respPayload.getBody(), WORKFLOW_RUNS);
				initializeBuildDetails(githubServer, builds, respPayload.getBody(), proBasicConfig);
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
			log.error("Error Fetching the jobs on instance: {}, {}", githubServer.getUrl(), e);
			throw new FetchingBuildException("Failed to fetch builds", e);
		}
		return builds;
	}

	private void initializeBuildDetails(ProcessorToolConnection githubServer, Set<Build> builds, String responseJson,
			ProjectBasicConfig proBasicConfig) {
		log.debug("Enter initializeBuildDetails");

		try {
			JSONParser parser = new JSONParser();
			JSONObject jsonJob = (JSONObject) parser.parse(responseJson);
			processJobDetailsRecursively(jsonJob, builds, proBasicConfig);
		} catch (ParseException e) {
			log.error(String.format("Parsing jobs details on instance: %s", githubServer.getUrl()), e);
		}

	}

	private void processJobDetailsRecursively(JSONObject jsonJob, Set<Build> result,
			ProjectBasicConfig proBasicConfig) {

		log.info("Entered inside method processJobDetailsRecursively");
		JSONArray jsonBuilds = ProcessorUtils.getJsonArray(jsonJob, Constants.BUILDS);
		if (!jsonBuilds.isEmpty()) {

			Set<Build> builds = new LinkedHashSet<>();
			for (Object build : jsonBuilds) {
				createBuildDetailsObject(builds, build, proBasicConfig);
			}
			// add the builds to the job
			result.addAll(builds);
		}

	}

	private void createBuildDetailsObject(Set<Build> builds, Object build, ProjectBasicConfig proBasicConfig) {
		JSONObject jsonBuild = (JSONObject) build;

		// A basic Build object. This will be fleshed out later if this
		// is a new Build.

		String buildNumber = jsonBuild.get(Constants.NUMBER).toString();

		Build gitHubActionBuild = new Build();
		String buildURL = ProcessorUtils.getString(jsonBuild, Constants.URL);
		if (proBasicConfig.isSaveAssigneeDetails()) {
			gitHubActionBuild.setStartedBy(ProcessorUtils.authorName(jsonBuild));
		}
		gitHubActionBuild.setBuildUrl(buildURL);
		gitHubActionBuild.setNumber(buildNumber);
		gitHubActionBuild.setStartTime(
				Instant.parse(ProcessorUtils.getString(jsonBuild, Constants.RUN_STARTED_AT)).toEpochMilli());
		gitHubActionBuild
				.setEndTime(Instant.parse(ProcessorUtils.getString(jsonBuild, Constants.UPDATED_AT)).toEpochMilli());
		gitHubActionBuild.setDuration(gitHubActionBuild.getEndTime() - gitHubActionBuild.getStartTime());
		gitHubActionBuild.setTimestamp(System.currentTimeMillis());
		gitHubActionBuild.setBuildStatus(getBuildStatus(jsonBuild));
		builds.add(gitHubActionBuild);

	}

	@Override
	public Map<Deployment, Set<Deployment>> getDeployJobsFromServer(ProcessorToolConnection githubServer,
			ProjectBasicConfig proBasicConfig) throws FetchingBuildException {
		return new HashMap<>();
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

	protected JSONArray getJSONFromResponse(String payload, String key) throws ParseException {
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(payload);
		return (JSONArray) jsonObject.get(key);
	}

	private BuildStatus getBuildStatus(JSONObject buildJson) {
		String status = buildJson.get(Constants.RESULT).toString();
		switch (status) {
		case "success":
			return BuildStatus.SUCCESS;
		case "unstable":
			return BuildStatus.UNSTABLE;
		case "failure":
			return BuildStatus.FAILURE;
		case "cancelled":
			return BuildStatus.ABORTED;
		default:
			return BuildStatus.UNKNOWN;
		}
	}
}
