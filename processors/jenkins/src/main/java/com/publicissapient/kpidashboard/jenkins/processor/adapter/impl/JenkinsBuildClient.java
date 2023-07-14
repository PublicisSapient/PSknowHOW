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

package com.publicissapient.kpidashboard.jenkins.processor.adapter.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import com.publicissapient.kpidashboard.common.constant.BuildStatus;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.Deployment;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.util.RestOperationsFactory;
import com.publicissapient.kpidashboard.jenkins.config.Constants;
import com.publicissapient.kpidashboard.jenkins.config.JenkinsConfig;
import com.publicissapient.kpidashboard.jenkins.model.JenkinsProcessor;
import com.publicissapient.kpidashboard.jenkins.processor.adapter.JenkinsClient;
import com.publicissapient.kpidashboard.jenkins.util.ProcessorUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * JenkinsClient implementation that uses RestTemplate and JSONSimple to fetch
 * information from Jenkins instances.
 */
@Component
@Slf4j
public class JenkinsBuildClient implements JenkinsClient {
	private static final String ZERO_AS_STR = "0";

	private final RestOperations restOperations;
	private final JenkinsConfig jenkinsConfig;

	/**
	 * Instantiate DefaultJenkinsClient.
	 *
	 * @param restOperationsFactory
	 *            the object supplier for RestOperations
	 * @param config
	 *            the Jenkins configuration details
	 */
	@Autowired
	public JenkinsBuildClient(RestOperationsFactory<RestOperations> restOperationsFactory, JenkinsConfig config) {
		this.restOperations = restOperationsFactory.getTypeInstance();
		this.jenkinsConfig = config;
	}

	/**
	 * Provides Build Status.
	 *
	 * @param buildJson
	 *            the build as JSON object
	 * @return the build status
	 */
	private BuildStatus getBuildStatus(JSONObject buildJson) {
		String status = buildJson.get(Constants.RESULT).toString();
		switch (status) {
		case "SUCCESS":
			return BuildStatus.SUCCESS;
		case "UNSTABLE":
			return BuildStatus.UNSTABLE;
		case "FAILURE":
			return BuildStatus.FAILURE;
		case "ABORTED":
			return BuildStatus.ABORTED;
		default:
			return BuildStatus.UNKNOWN;
		}
	}

	/**
	 * Makes Rest Call.
	 *
	 * @param sUrl
	 *            the rest call URL
	 * @param jenkinsServer
	 *            the connection properties for Jenkins server
	 * @return the response entity
	 * 
	 */
	public ResponseEntity<String> doRestCall(String sUrl, ProcessorToolConnection jenkinsServer) {
		log.debug("Enter makeRestCall {}", sUrl);
		URI theUri = URI.create(sUrl);
		String userInfo = theUri.getUserInfo();

		if (StringUtils.isEmpty(userInfo)) {
			userInfo = getUserInfo(sUrl, jenkinsServer);
		}

		if (StringUtils.isNotEmpty(userInfo)) {
			return restOperations.exchange(theUri, HttpMethod.GET,
					new HttpEntity<>(ProcessorUtils.createHeaders(userInfo)), String.class);
		} else {
			return restOperations.exchange(theUri, HttpMethod.GET, null, String.class);
		}

	}

	/**
	 * Gets user credentials info
	 *
	 * @param sUrl
	 *            the url
	 * @param jenkinsServer
	 *            jenkins server url
	 * @return user info eg. usernaem:passkey
	 */
	private String getUserInfo(String sUrl, ProcessorToolConnection jenkinsServer) {
		String userInfo = "";

		if (ProcessorUtils.isSameServerInfo(sUrl, jenkinsServer.getUrl())) {

			if (StringUtils.isNotEmpty(jenkinsServer.getUsername())
					&& StringUtils.isNotEmpty(jenkinsServer.getApiKey())) {
				userInfo = jenkinsServer.getUsername() + ":" + jenkinsServer.getApiKey();
			} else {
				log.warn(
						"Credentials for the following url was not found. This could happen if the domain/subdomain/IP address in the build url returned by Jenkins and the Jenkins instance url in your Speedy configuration do not match: {} ",
						sUrl);
			}

		}

		return userInfo;
	}

	/**
	 * Provides Log.
	 *
	 * @param buildUrl
	 *            the build url
	 * @param jenkinsServer
	 *            the connection properties for Jenkins server
	 * @return the log
	 */
	public String getLog(String buildUrl, ProcessorToolConnection jenkinsServer) {

		return doRestCall(ProcessorUtils.joinURL(buildUrl, "consoleText"), jenkinsServer).getBody();

	}

	@Override
	public Map<ObjectId, Set<Build>> getBuildJobsFromServer(ProcessorToolConnection jenkinsServer,
			ProjectBasicConfig proBasicConfig) {
		log.debug("Enter getBuildJobsFromServer");
		Map<ObjectId, Set<Build>> result = new LinkedHashMap<>();

		try {
			String jobName = jenkinsServer.getJobName().replace("/", "/job/");
			String query = Constants.JOB_URL_END_POINT.replace("BUILD_NAME", jobName);

			String url = ProcessorUtils.joinURL(jenkinsServer.getUrl(), query + Constants.JOB_FIELDS + ","
					+ ProcessorUtils.buildJobQueryString(jenkinsConfig, Constants.CHILD_JOBS_TREE));
			ResponseEntity<String> responseEntity = doRestCall(url, jenkinsServer);
			if (responseEntity != null && StringUtils.isNotEmpty(responseEntity.getBody())) {
				processJobResponse(jenkinsServer, result, responseEntity.getBody(), proBasicConfig);
			}

		} catch (RestClientException rce) {
			log.error(String.format("Error getting details for instance : %s, job : %s", jenkinsServer.getUrl(),
					jenkinsServer.getJobName()), rce);
		}
		return result;
	}

	@Override
	public Map<String, Set<Deployment>> getDeployJobsFromServer(ProcessorToolConnection jenkinsServer,
			JenkinsProcessor processor) {
		return new HashMap<>();
	}

	private void processJobResponse(ProcessorToolConnection jenkinsServer, Map<ObjectId, Set<Build>> result,
			String returnJSON, ProjectBasicConfig proBasicConfig) {
		try {
			JSONParser parser = new JSONParser();
			JSONObject jsonJob = (JSONObject) parser.parse(returnJSON);

			final String jobName = ProcessorUtils.getString(jsonJob, Constants.NAME);
			final String jobURL = ProcessorUtils.getString(jsonJob, Constants.URL);

			log.debug("Process jobName {}  jobURL {} ", jobName, jobURL);

			processJobDetailsRecursively(jsonJob, jobName, jobURL, jenkinsServer.getUrl(), result, jenkinsServer,
					proBasicConfig);
		} catch (ParseException e) {
			log.error(String.format("Parsing jobs details on instance: %s", jenkinsServer.getUrl()), e);
		}
	}

	/**
	 * Provides Job details recursively.
	 *
	 * @param jsonJob
	 *            the job detail in json
	 * @param jobName
	 *            the job name
	 * @param jobURL
	 *            the job URL
	 * @param instanceUrl
	 *            the jenkins instance URL
	 * @param result
	 *            the list of build
	 * @param proBasicConfig
	 */
	private void processJobDetailsRecursively(JSONObject jsonJob, String jobName, String jobURL, String instanceUrl,
			Map<ObjectId, Set<Build>> result, ProcessorToolConnection jenkinsServer,
			ProjectBasicConfig proBasicConfig) {
		log.debug("recursiveGetJobDetails: jobName {} jobURL: {}", jobName, jobURL);

		JSONArray jsonBuilds = ProcessorUtils.getJsonArray(jsonJob, Constants.BUILDS);
		if (!jsonBuilds.isEmpty()) {

			Set<Build> builds = new LinkedHashSet<>();
			for (Object build : jsonBuilds) {
				createBuildDetailsObject(jenkinsServer, builds, build, proBasicConfig);
			}
			// add the builds to the job
			result.put(jenkinsServer.getId(), builds);
		}
		JSONArray childJobs = ProcessorUtils.getJsonArray(jsonJob, Constants.JOBS);

		for (Object childJob : childJobs) {
			final String name = ProcessorUtils.getString((JSONObject) childJob, Constants.NAME);
			final String url = ProcessorUtils.getString((JSONObject) childJob, Constants.URL);

			JSONObject jsonSubJob = (JSONObject) childJob;
			processJobDetailsRecursively(jsonSubJob, jobName + "/" + name, url, instanceUrl, result, jenkinsServer,
					proBasicConfig);
		}

	}

	/**
	 * @param jenkinsServer
	 * @param builds
	 * @param build
	 * @param proBasicConfig
	 */
	private void createBuildDetailsObject(ProcessorToolConnection jenkinsServer, Set<Build> builds, Object build,
			ProjectBasicConfig proBasicConfig) {
		JSONObject jsonBuild = (JSONObject) build;

		// A basic Build object. This will be fleshed out later if this
		// is a new Build.

		String buildNumber = jsonBuild.get(Constants.NUMBER).toString();
		boolean building = (boolean) jsonBuild.get(Constants.BUILDING);
		log.debug(" buildNumber: {}", buildNumber);

		// Ignore jobs that are building
		if (!ZERO_AS_STR.equals(buildNumber) && !building) {

			Build jenkinsBuild = new Build();
			String buildURL = ProcessorUtils.getString(jsonBuild, Constants.URL);
			String hostIp = jenkinsConfig.getDockerHostIp();
			if (StringUtils.isEmpty(hostIp)) {
				log.debug("Adding Build: {}", buildURL);
			} else {
				buildURL = buildURL.replace("localhost", hostIp);
				log.debug("Adding build & Updated URL to map LocalHost for Docker: {}", buildURL);
			}
			if (proBasicConfig.isSaveAssigneeDetails()) {
				jenkinsBuild.setStartedBy(ProcessorUtils.firstCulprit(jsonBuild));
			}
			jenkinsBuild.setBuildUrl(buildURL);
			jenkinsBuild.setNumber(buildNumber);
			jenkinsBuild.setStartTime((Long) jsonBuild.get(Constants.TIMESTAMP));
			jenkinsBuild.setDuration((Long) jsonBuild.get(Constants.DURATION));
			jenkinsBuild.setTimestamp(System.currentTimeMillis());
			jenkinsBuild.setEndTime(jenkinsBuild.getStartTime() + jenkinsBuild.getDuration());
			jenkinsBuild.setBuildStatus(getBuildStatus(jsonBuild));

			if (jenkinsConfig.isIncludeLogs()) {
				jenkinsBuild.setLog(getLog(buildURL, jenkinsServer));
			}

			builds.add(jenkinsBuild);
		}
	}

}
