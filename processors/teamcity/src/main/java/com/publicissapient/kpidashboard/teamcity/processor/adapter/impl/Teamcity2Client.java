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

package com.publicissapient.kpidashboard.teamcity.processor.adapter.impl;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
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

import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.util.RestOperationsFactory;
import com.publicissapient.kpidashboard.teamcity.config.Constants;
import com.publicissapient.kpidashboard.teamcity.config.TeamcityConfig;
import com.publicissapient.kpidashboard.teamcity.processor.adapter.TeamcityClient;
import com.publicissapient.kpidashboard.teamcity.util.ProcessorUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Teamcity2Client implementation that uses RestTemplate and JSONSimple to fetch
 * information from Teamcity instances.
 */
@Component
@Slf4j
public class Teamcity2Client implements TeamcityClient {
	private static final String ZERO_AS_STR = "0";

	private final RestOperations rest;
	private final TeamcityConfig teamcityConfig;

	/**
	 * Instantiate Teamcity2Client.
	 * 
	 * @param restOperationsFactory
	 *            the object supplier for RestOperations
	 * @param teamcityConfig
	 *            the Teamcity configuration details
	 */
	@Autowired
	public Teamcity2Client(RestOperationsFactory<RestOperations> restOperationsFactory, TeamcityConfig teamcityConfig) {
		this.rest = restOperationsFactory.getTypeInstance();
		this.teamcityConfig = teamcityConfig;
	}

	/**
	 * Rebuilds the API endpoint because the buildUrl obtained via Teamcity API.
	 *
	 * @param build
	 *            the build
	 * @param server
	 *            the server
	 * @return the build job URL
	 * @throws URISyntaxException
	 *             if there is any illegal character in URI
	 * @throws MalformedURLException
	 *             if there is an invalid URL
	 * @throws UnsupportedEncodingException
	 *             if there is wrong encoding specified
	 */
	public static String rebuildJobUrl(String build, String server)
			throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {
		URL instanceUrl = new URL(server);
		String userInfo = instanceUrl.getUserInfo();
		String instanceProtocol = instanceUrl.getProtocol();

		// decode to handle spaces in the job name.
		URL buildUrl = new URL(URLDecoder.decode(build, "UTF-8"));
		String buildPath = buildUrl.getPath();

		String host = buildUrl.getHost();
		int port = buildUrl.getPort();
		if (-1 == port) {
			port = instanceUrl.getPort();
		}
		URI newUri = new URI(instanceProtocol, userInfo, host, port, buildPath, null, null);
		return newUri.toString();
	}

	/**
	 * Provides Instance Jobs.
	 *
	 * @param toolConfig
	 *            the tool configuration details
	 * @return the map of teamcity jobs and build
	 */
	public Map<ObjectId, Set<Build>> getInstanceJobs(ProcessorToolConnection toolConfig) {
		log.debug("Enter getInstanceJobs");
		Map<ObjectId, Set<Build>> result = new LinkedHashMap<>();

		JSONObject jobs = getJobs(toolConfig);

		int jobsCount = getJobsCount(jobs);
		log.info("Number of jobs {}", jobsCount);

		int index = 0;
		int pageSize = teamcityConfig.getPageSize() <= 0 ? Constants.DEFAULT_PAGE_SIZE : teamcityConfig.getPageSize();

		while (index < jobsCount) {
			try {
				String url = ProcessorUtils.joinURL(toolConfig.getUrl(), Constants.JOBS_URL_SUFFIX, Constants.JOB_ID,
						getProjectId(jobs, index));
				ResponseEntity<String> responseEntity = doRestCall(url, toolConfig);
				if (responseEntity == null || StringUtils.isEmpty(responseEntity.getBody())
						|| processResponse(toolConfig, result, responseEntity.getBody())) {
					break;
				}

			} catch (RestClientException rce) {
				log.error("client exception loading jobs details", rce);
				throw rce;
			}
			index += pageSize;
		}
		return result;
	}

	private JSONObject getJobs(ProcessorToolConnection toolConfig) {
		String url = ProcessorUtils.joinURL(toolConfig.getUrl(), Constants.JOBS_URL_SUFFIX);
		ResponseEntity<String> responseEntity = doRestCall(url, toolConfig);
		JSONObject object = new JSONObject();

		try {

			if (responseEntity == null) {
				return object;
			}
			String returnJSON = responseEntity.getBody();
			if (StringUtils.isEmpty(returnJSON)) {
				return object;
			}

			JSONParser parser = new JSONParser();
			object = (JSONObject) parser.parse(returnJSON);
		} catch (ParseException e) {
			log.error(String.format("Parsing jobs on instance: %s", url), e);
		}
		return object;

	}

	private String getProjectId(JSONObject jobs, int counter) {
		JSONArray jobsArray = ProcessorUtils.getJsonArray(jobs, "project");
		JSONObject projectDetails = (JSONObject) jobsArray.get(counter);
		return projectDetails.get("id").toString();
	}

	private boolean processResponse(ProcessorToolConnection toolConfig, Map<ObjectId, Set<Build>> result,
			String returnJSON) {
		try {
			JSONParser parser = new JSONParser();
			JSONObject object = (JSONObject) parser.parse(returnJSON);
			JSONArray jobs = ProcessorUtils.getJsonArray(object, "jobs");
			if (CollectionUtils.isEmpty(jobs)) {
				return true;
			}

			for (Object job : jobs) {
				JSONObject jsonJob = (JSONObject) job;

				final String jobName = ProcessorUtils.getString(jsonJob, "name");
				final String jobURL = ProcessorUtils.getString(jsonJob, Constants.URL);
				log.debug("Process jobName {}  jobURL {} ", jobName, jobURL);

				recursiveGetJobDetails(jsonJob, jobName, jobURL, toolConfig.getUrl(), result, toolConfig);
			}
		} catch (ParseException e) {
			log.error(String.format("Parsing jobs details on instance: %s", toolConfig.getUrl()), e);
		}
		return false;
	}

	/**
	 * Provides the number of jobs first so that we don't get 500 internal server
	 * logError when paging with index out of bounds.
	 *
	 * @return the number of jobs
	 */
	private int getJobsCount(JSONObject jobs) {
		int count = 0;
		JSONArray jobsArray = ProcessorUtils.getJsonArray(jobs, "project");
		count = jobsArray.size();

		return count;
	}

	/**
	 * Provides Job Details recursively.
	 *
	 * @param jsonJob
	 *            the job detail in json
	 * @param jobName
	 *            the job name
	 * @param jobURL
	 *            the job URL
	 * @param instanceUrl
	 *            the teamcity instance URL
	 *
	 * @param result
	 *            the list of build
	 */
	private void recursiveGetJobDetails(JSONObject jsonJob, String jobName, String jobURL, String instanceUrl,
			Map<ObjectId, Set<Build>> result, ProcessorToolConnection toolConfig) {
		log.debug("recursiveGetJobDetails: jobName {} jobURL: {}", jobName, jobURL);

		JSONObject jsonBuildRoot = (JSONObject) jsonJob.get("buildTypes");

		JSONArray jsonBuilds = ProcessorUtils.getJsonArray(jsonBuildRoot, "buildType");
		if (!jsonBuilds.isEmpty()) {

			Set<Build> builds = new LinkedHashSet<>();
			for (Object build : jsonBuilds) {
				JSONObject jsonBuild = (JSONObject) build;

				// A basic Build object. This will be fleshed out later if this
				// is a new Build.
				String hostIp = teamcityConfig.getDockerHostIp();
				JSONObject buildDetails = getBuildInfo(jsonBuild.get("href").toString(), instanceUrl, toolConfig);
				if (null != buildDetails) {
					String buildNumber = getBuildNumber(buildDetails);

					if (!ZERO_AS_STR.equals(buildNumber)) {
						Build teamcityBuild = new Build();
						teamcityBuild.setNumber(buildNumber);
						String buildURL = ProcessorUtils.joinURL(instanceUrl, buildDetails.get("href").toString());
						if (StringUtils.isNotEmpty(hostIp)) {
							buildURL = buildURL.replace("localhost", hostIp);
							log.debug("Adding build & Updated URL to map LocalHost for Docker: {}", buildURL);
						}

						teamcityBuild.setBuildUrl(buildURL);
						builds.add(teamcityBuild);
					}
				}
			}
			// add the builds to the job
			result.put(toolConfig.getId(), builds);
		}
	}

	private String getBuildNumber(JSONObject buildDetails) {
		String buildNumber = StringUtils.EMPTY;
		JSONObject buildSettings = (JSONObject) buildDetails.get("settings");
		JSONArray buildProperties = ProcessorUtils.getJsonArray(buildSettings, "property");
		for (Object buildProperty : buildProperties) {
			JSONObject property = (JSONObject) buildProperty;
			if (property.get("name") != null
					&& property.get("name").toString().equalsIgnoreCase("buildNumberCounter")) {
				buildNumber = property.get("value").toString();
			}
		}

		log.debug(" buildNumber: {}", buildNumber);
		return buildNumber;

	}

	private JSONObject getBuildInfo(String buildUrl, String hostName, ProcessorToolConnection toolConfig) {
		String url = ProcessorUtils.joinURL(hostName, buildUrl);
		ResponseEntity<String> result = doRestCall(url, toolConfig);
		String resultJSON = result.getBody();
		if (StringUtils.isEmpty(resultJSON)) {
			log.error("Error getting build details for. URL = {}", url);
			return null;
		}

		JSONParser parser = new JSONParser();
		JSONObject buildJson = new JSONObject();
		try {
			buildJson = (JSONObject) parser.parse(resultJSON);
		} catch (ParseException e) {
			log.error(String.format("Error in parsing build response: %s", buildUrl), e);
		}
		return buildJson;
	}

	/**
	 * Makes rest call.
	 * 
	 * @param sUrl
	 *            the url
	 * @param toolConfig
	 *            tool config
	 * @return response
	 */
	protected ResponseEntity<String> doRestCall(String sUrl, ProcessorToolConnection toolConfig) {
		log.debug("Enter makeRestCall {}", sUrl);
		URI thisuri = URI.create(sUrl);
		String userInfo = thisuri.getUserInfo();

		if (StringUtils.isEmpty(userInfo) && ProcessorUtils.isSameServerInfo(sUrl, toolConfig.getUrl())) {

			if (StringUtils.isNotEmpty(toolConfig.getUsername()) && StringUtils.isNotEmpty(toolConfig.getApiKey())) {
				userInfo = toolConfig.getUsername() + ":" + toolConfig.getApiKey();
			} else {
				log.warn(
						"Credentials for the following url was not found. This could happen if the domain/subdomain/IP address in the build url returned by Teamcity and the Teamcity instance url in your configuration do not match: {} ",
						sUrl);
			}

		}

		if (StringUtils.isNotEmpty(userInfo)) {
			return rest.exchange(thisuri, HttpMethod.GET, new HttpEntity<>(ProcessorUtils.createHeaders(userInfo)),
					String.class);
		} else {
			return rest.exchange(thisuri, HttpMethod.GET, null, String.class);
		}

	}

	/**
	 * Provides Build Details.
	 */
	@Override
	public Build getBuildDetails(String buildUrl, String instanceUrl, ProcessorToolConnection teamcityServer,
			ProjectBasicConfig proBasicConfig) {
		return null;
	}
}
