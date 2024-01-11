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

import com.publicissapient.kpidashboard.common.constant.BuildStatus;
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
 * TeamcityClient implementation that uses RestTemplate and JSONSimple to fetch
 * information from Teamcity instances.
 */
@SuppressWarnings("PMD.GodClass")
@Component
@Slf4j
public class DefaultTeamcityClient implements TeamcityClient {
	private static final String ZERO_AS_STR = "0";
	private static final String NUMBER = "number";

	private static final String PROJECT = "project";
	private static final String NAME = "name";
	private static final String PARSING_ERROR = "Parsing jobs on instance: %s";
	private final RestOperations restOperations;
	private final TeamcityConfig teamcityConfig;

	/**
	 * Instantiate DefaultTeamcityClient.
	 *
	 * @param restOperationsFactory
	 *            the object supplier for RestOperations
	 * @param config
	 *            the Teamcity configuration details
	 */
	@Autowired
	public DefaultTeamcityClient(RestOperationsFactory<RestOperations> restOperationsFactory, TeamcityConfig config) {
		this.restOperations = restOperationsFactory.getTypeInstance();
		this.teamcityConfig = config;
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

		// adding to correcc the code analysis issue
		String host = instanceUrl.getHost();

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
	 * @param teamcityServer
	 *            the connection properties for Teamcity server
	 * @return the map of teamcity jobs and build
	 */
	@Override
	public Map<ObjectId, Set<Build>> getInstanceJobs(ProcessorToolConnection teamcityServer) {
		log.debug("Enter getInstanceJobs");
		Map<ObjectId, Set<Build>> result = new LinkedHashMap<>();

		JSONObject jobs = getJobs(teamcityServer);

		int jobsCount = getJobsCount(jobs);
		log.info("Number of jobs {}", jobsCount);

		int index = 0;
		int pageSize = teamcityConfig.getPageSize() <= 0 ? Constants.DEFAULT_PAGE_SIZE : teamcityConfig.getPageSize();

		while (index < jobsCount) {
			try {

				String url = ProcessorUtils.joinURL(teamcityServer.getUrl(), Constants.JOBS_URL_SUFFIX);
				ResponseEntity<String> responseEntity = doRestCall(url, teamcityServer);
				if (responseEntity == null || StringUtils.isEmpty(responseEntity.getBody())
						|| processResponse(teamcityServer, result, responseEntity.getBody())) {
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

	private boolean processResponse(ProcessorToolConnection teamcityServer, Map<ObjectId, Set<Build>> result,
			String returnJSON) {
		try {
			JSONParser parser = new JSONParser();
			JSONObject object = (JSONObject) parser.parse(returnJSON);
			JSONArray jobs = ProcessorUtils.getJsonArray(object, PROJECT);
			if (CollectionUtils.isEmpty(jobs)) {
				return true;
			}

			for (Object job : jobs) {
				JSONObject jsonJob = (JSONObject) job;
				if (ProcessorUtils.getString(jsonJob, "id").equals("_Root")) {
					continue;
				}

				final String jobName = ProcessorUtils.getString(jsonJob, NAME);
				final String jobURL = ProcessorUtils.getString(jsonJob, Constants.URL);

				log.debug("Process jobName {}  jobURL {}", jobName, jobURL);
				if (jobName.trim().equals(teamcityServer.getJobName().trim())) {
					recursiveGetJobDetails(jobName, jobURL, teamcityServer.getUrl(), result, teamcityServer);
				}
			}
		} catch (ParseException e) {
			log.error(String.format("Parsing jobs details on instance: %s", teamcityServer.getUrl()), e);
		}
		return false;
	}

	private JSONObject getJobs(ProcessorToolConnection teamcityServer) {
		String url = ProcessorUtils.joinURL(teamcityServer.getUrl(), Constants.JOBS_URL_SUFFIX);
		ResponseEntity<String> responseEntity = doRestCall(url, teamcityServer);
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
			log.error(String.format(PARSING_ERROR, url), e);
		}
		return object;

	}

	/**
	 * Provides the number of jobs first so that we don't get 500 internal server
	 * logError when paging with index out of bounds.
	 *
	 * @param jobs
	 *            the connection properties for Teamcity server
	 * @return the number of jobs
	 */
	private int getJobsCount(JSONObject jobs) {
		int count = 0;
		JSONArray jobsArray = ProcessorUtils.getJsonArray(jobs, PROJECT);
		count = jobsArray.size();

		return count;
	}

	/**
	 * Provides Job details recursively.
	 *
	 * @param jobName
	 *            the job name
	 * @param jobURL
	 *            the job URL
	 * @param instanceUrl
	 *            the teamcity instance URL
	 * @param result
	 *            the list of build
	 */
	private void recursiveGetJobDetails(String jobName, String jobURL, String instanceUrl,
			Map<ObjectId, Set<Build>> result, ProcessorToolConnection teamcityServer) {
		log.debug("recursiveGetJobDetails: jobName {} jobURL: {}", jobName, jobURL);

		String url = ProcessorUtils.joinURL(teamcityServer.getUrl(), jobURL);
		ResponseEntity<String> responseEntity = doRestCall(url, teamcityServer);
		if (responseEntity == null || StringUtils.isEmpty(responseEntity.getBody())) {
			return;
		}

		JSONObject projectObject = new JSONObject();
		String projectDetails = responseEntity.getBody();
		JSONParser parser = new JSONParser();

		try {
			projectObject = (JSONObject) parser.parse(projectDetails);
		} catch (ParseException e) {
			log.error(String.format(PARSING_ERROR, url), e);
		}

		JSONObject jsonBuildRoot = (JSONObject) projectObject.get("buildTypes");

		JSONArray jsonBuilds = ProcessorUtils.getJsonArray(jsonBuildRoot, "buildType");
		if (!jsonBuilds.isEmpty()) {

			Set<Build> builds = new LinkedHashSet<>();
			for (Object build : jsonBuilds) {
				JSONObject jsonBuild = (JSONObject) build;

				// A basic Build object. This will be fleshed out later if this
				// is a new Build.
				JSONObject buildDetails = getBuildInfo(jsonBuild.get("href").toString() + Constants.BUILD_URL_END_POINT,
						instanceUrl, teamcityServer);
				String buildNumber = getBuildNumberFromLatestBuild(buildDetails);

				if (null != buildDetails) {
					Build tcbuild = createBuildObject(buildNumber, buildDetails, instanceUrl);
					if (null != tcbuild)
						builds.add(tcbuild);
				}
			}
			// add the builds to the job
			result.put(teamcityServer.getId(), builds);
		}

		JSONObject childJobs = (JSONObject) projectObject.get("projects");
		JSONArray childJobsArray = ProcessorUtils.getJsonArray(childJobs, PROJECT);

		for (Object childJob : childJobsArray) {
			final String name = ProcessorUtils.getString((JSONObject) childJob, NAME);
			final String childJobUrl = ProcessorUtils.getString((JSONObject) childJob, Constants.URL);

			JSONObject jsonSubJob = (JSONObject) childJob;
			recursiveGetJobDetails(name, childJobUrl, instanceUrl, jsonSubJob, teamcityServer);
		}

	}

	private Build createBuildObject(String buildNumber, JSONObject buildDetails, String instanceUrl) {
		String hostIp = teamcityConfig.getDockerHostIp();
		Build teamcityBuild = null;
		if (!ZERO_AS_STR.equals(buildNumber)) {
			teamcityBuild = new Build();
			teamcityBuild.setNumber(buildNumber);
			String buildURL = ProcessorUtils.joinURL(instanceUrl, buildDetails.get("href").toString());
			if (StringUtils.isNotEmpty(hostIp)) {
				buildURL = buildURL.replace("localhost", hostIp);
				log.debug("Adding build & Updated URL to map LocalHost for Docker: {}", buildURL);
			}
			teamcityBuild.setBuildUrl(buildURL);
		}
		return teamcityBuild;
	}

	private String getBuildNumberFromLatestBuild(JSONObject buildDetails) {
		String buildNumber = ZERO_AS_STR;
		if (null != buildDetails) {
			JSONArray buildProperties = ProcessorUtils.getJsonArray(buildDetails, "build");
			if (!buildProperties.isEmpty()) {
				JSONObject property = (JSONObject) buildProperties.get(0);
				if (property.get(NUMBER) != null && !property.get(NUMBER).toString().isEmpty()) {
					buildNumber = property.get(NUMBER).toString();
				}
			}

			log.debug(" buildNumber: {}", buildNumber);
		}
		return buildNumber;

	}

	private JSONObject getBuildInfo(String buildUrl, String hostName, ProcessorToolConnection teamcityServer) {
		String url = ProcessorUtils.joinURL(hostName, buildUrl);
		ResponseEntity<String> result = doRestCall(url, teamcityServer);
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
	 * Provides Build Details.
	 *
	 * @param buildUrl
	 *            the build URL
	 * @param instanceUrl
	 *            the Teamcity instance URL
	 * @param teamcityServer
	 *            the connection properties for Teamcity server
	 * @param proBasicConfig
	 * @return the Build details
	 */
	@SuppressWarnings("PMD.AvoidCatchingGenericException")
	@Override
	public Build getBuildDetails(String buildUrl, String instanceUrl, ProcessorToolConnection teamcityServer,
			ProjectBasicConfig proBasicConfig) {
		try {
			String url = rebuildJobUrl(buildUrl, instanceUrl);
			ResponseEntity<String> result = doRestCall(url, teamcityServer);
			String resultJSON = result.getBody();
			if (StringUtils.isEmpty(resultJSON)) {
				log.error("Error getting build details for. URL = {}", url);
				return null;
			}

			return parseBuildDetailsResponse(resultJSON, buildUrl, teamcityServer, proBasicConfig);
		} catch (UnsupportedEncodingException e) {
			log.error(String.format("Unsupported Encoding Exception in getting build details. URL=%s", buildUrl), e);
		} catch (URISyntaxException e) {
			log.error(String.format("Uri syntax exception for loading build details %s. URL = %s", e.getMessage(),
					buildUrl), e);
		} catch (MalformedURLException e) {
			log.error(String.format("Malformed url for loading build details %s. URL = %s", e.getMessage(), buildUrl),
					e);
		}
		return null;
	}

	private Build createBuild(String buildUrl, ProcessorToolConnection teamcityServer, JSONObject buildJson,
			ProjectBasicConfig proBasicConfig) {
		Build build = new Build();
		if (proBasicConfig.isSaveAssigneeDetails()) {
			build.setStartedBy(ProcessorUtils.firstCulprit(buildJson));
		}
		build.setBuildUrl(buildUrl);
		build.setNumber(buildJson.get(NUMBER).toString());
		build.setStartTime(ProcessorUtils.getCommitTimestamp(buildJson.get("startDate").toString()));
		build.setTimestamp(System.currentTimeMillis());
		build.setEndTime(ProcessorUtils.getCommitTimestamp(buildJson.get("finishDate").toString()));

		JSONObject stats = (JSONObject) buildJson.get("statistics");
		String statsUrl = stats.get(Constants.URL).toString();

		build.setDuration(Long.parseLong(getBuildDuration(statsUrl, teamcityServer)));

		build.setBuildStatus(getBuildStatus(buildJson.get("status").toString()));

		if (teamcityConfig.isIncludeLogs()) {
			build.setLog(getLog(buildUrl, teamcityServer));
		}
		return build;
	}

	private String getBuildDuration(String latestBuildUrl, ProcessorToolConnection teamcityServer) {
		String url = ProcessorUtils.joinURL(teamcityServer.getUrl(), latestBuildUrl);
		ResponseEntity<String> responseEntity = doRestCall(url, teamcityServer);
		JSONObject object = new JSONObject();

		try {

			if (responseEntity == null) {
				return null;
			}
			String returnJSON = responseEntity.getBody();
			if (StringUtils.isEmpty(returnJSON)) {
				return null;
			}

			JSONParser parser = new JSONParser();
			object = (JSONObject) parser.parse(returnJSON);
		} catch (ParseException e) {
			log.error(String.format(PARSING_ERROR, url), e);
		}
		JSONArray property = ProcessorUtils.getJsonArray(object, "property");
		for (Object props : property) {
			JSONObject prop = (JSONObject) props;
			if (prop.get(NAME).equals("BuildDuration")) {
				return prop.get("value").toString();
			}
		}
		return StringUtils.EMPTY;
	}

	private Build parseBuildDetailsResponse(String resultJSON, String buildUrl, ProcessorToolConnection teamcityServer,
			ProjectBasicConfig proBasicConfig) {
		JSONParser parser = new JSONParser();
		try {
			JSONObject buildJson = (JSONObject) parser.parse(resultJSON);

			JSONArray buildList = ProcessorUtils.getJsonArray(buildJson, "build");
			if (CollectionUtils.isEmpty(buildList)) {
				return null;
			}

			JSONObject latestBuild = (JSONObject) buildList.get(0);

			String latestbuildUrl = latestBuild.get(Constants.URL).toString();

			JSONObject latestBuildDetails = getBuildInfo(latestbuildUrl, teamcityServer.getUrl(), teamcityServer);

			if (null != latestBuildDetails)
				return createBuild(latestbuildUrl, teamcityServer, latestBuildDetails, proBasicConfig);
		} catch (ParseException parseException) {
			log.error(String.format("Error in parsing build response: %s", buildUrl), parseException);
		}
		return null;
	}

	/**
	 * Provides Build Status.
	 *
	 * @param status
	 *            the Status of the build
	 * @return the build status
	 */
	private BuildStatus getBuildStatus(String status) {
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
	 * @param teamcityServer
	 *            the connection properties for Teamcity server
	 * @return the response entity
	 * 
	 */
	public ResponseEntity<String> doRestCall(String sUrl, ProcessorToolConnection teamcityServer) {
		log.info("Enter makeRestCall {}", sUrl);
		URI theUri = URI.create(sUrl);
		String userInfo = theUri.getUserInfo();

		if (StringUtils.isEmpty(userInfo)) {
			userInfo = getUserInfo(sUrl, teamcityServer);
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
	 * @param teamcityServer
	 *            teamcity server url
	 * @return user info eg. usernaem:passkey
	 */
	private String getUserInfo(String sUrl, ProcessorToolConnection teamcityServer) {
		String userInfo = "";

		if (ProcessorUtils.isSameServerInfo(sUrl, teamcityServer.getUrl())) {

			if (StringUtils.isNotEmpty(teamcityServer.getUsername())
					&& StringUtils.isNotEmpty(teamcityServer.getPassword())) {
				userInfo = teamcityServer.getUsername() + ":" + teamcityServer.getPassword();
			} else {
				log.warn(
						"Credentials for the following url was not found. This could happen if the domain/subdomain/IP address in the build url returned by Teamcity and the Teamcity instance url in your configuration do not match: {} ",
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
	 * @param teamcityServer
	 *            the connection properties for Teamcity server
	 * @return the log
	 */
	public String getLog(String buildUrl, ProcessorToolConnection teamcityServer) {

		return doRestCall(ProcessorUtils.joinURL(buildUrl, "consoleText"), teamcityServer).getBody();

	}

}
