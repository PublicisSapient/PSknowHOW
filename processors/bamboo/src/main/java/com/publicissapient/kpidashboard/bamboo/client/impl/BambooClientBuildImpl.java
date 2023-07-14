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

package com.publicissapient.kpidashboard.bamboo.client.impl;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.bamboo.client.BambooClient;
import com.publicissapient.kpidashboard.bamboo.config.BambooConfig;
import com.publicissapient.kpidashboard.common.constant.BuildStatus;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.Deployment;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;

import lombok.extern.slf4j.Slf4j;

/**
 * This class is the implementation of {@link BambooClient} which could be used
 * to retrieve data from Bamboo server intsances.
 * 
 * @see BambooClient
 */
@Component
@Slf4j
public class BambooClientBuildImpl implements BambooClient {

	private static final String ZERO = "0";

	private static final String BUILD_NUMBER = "buildNumber";
	private static final String FINISHED = "finished";
	/*
	 * rest call used to list all plans on Bamboo service that user is allowed to
	 * see (READ permission). Should be invoked as /rest/api/latest/plan. Possible
	 * expand parameters plans - list of plans plans.plan - list of plans with plan
	 * details plans.plan.actions
	 */
	private static final String PLAN_URL_SUFFIX = "/rest/api/latest/plan/";
	private static final String JOBS_RESULT_SUFFIX = "rest/api/latest/result/";
	private static final String BUILD_DETAILS_URL_SUFFIX = "?expand=results.result.artifacts&expand=changes.change.files";
	private static final String BRANCH_URL_SUFFIX = "/branch.json?max-result=2000";
	@Autowired
	private RestTemplate restClient;
	@Autowired
	private BambooConfig settings;

	/**
	 * fetch jobs based on job key and branch key
	 *
	 * @param bambooServer
	 *            {@link ProcessorToolConnection}
	 * @param proBasicConfig
	 * @return
	 * @throws ParseException
	 */
	@Override
	public Map<ObjectId, Set<Build>> getJobsFromServer(ProcessorToolConnection bambooServer,
			ProjectBasicConfig proBasicConfig) throws ParseException {
		Map<ObjectId, Set<Build>> bambooJobs = new LinkedHashMap<>();
		try {
			final String planKey = bambooServer.getJobName();
			final String planURL = BambooClient.appendToURL(bambooServer.getUrl(), PLAN_URL_SUFFIX, planKey);
			final String branchKey = bambooServer.getBranch();
			JSONParser parser = new JSONParser();
			if (branchKey == null) {
				setPlanBuilds(bambooServer, bambooJobs, parser, planKey);
			} else {
				// There might be many branches and sub-plans
				setBranchBuilds(bambooServer, bambooJobs, parser, branchKey, planURL);
			}
		} catch (ParseException | RestClientException e) {
			log.error("Error Fetching the jobs on instance: {}, {}", bambooServer.getUrl(), e);
			throw e;
		}
		return bambooJobs;
	}

	private void setBranchBuilds(ProcessorToolConnection bambooServer, Map<ObjectId, Set<Build>> bambooJobs,
			JSONParser parser, String branchKey, String planURL) throws ParseException {
		String returnJSON;
		String resultUrl;
		String branchesUrl = BambooClient.appendToURL(planURL, BRANCH_URL_SUFFIX);
		returnJSON = makeBambooServerCall(branchesUrl, bambooServer);
		if (StringUtils.isNotEmpty(returnJSON)) {
			JSONObject branchesData = (JSONObject) parser.parse(returnJSON);
			if (branchesData.get("branches") != null) {
				for (Object branch : getJsonArray((JSONObject) branchesData.get("branches"), "branch")) {
					JSONObject branchObject = (JSONObject) branch;
					String subPlan = branchObject.get("key").toString();
					if (!branchKey.equals(subPlan)) {
						continue;
					}
					// Figure out nested jobs under the branches
					resultUrl = BambooClient.appendToURL(bambooServer.getUrl(), JOBS_RESULT_SUFFIX, subPlan);
					log.info("Found sub Plan:{}; URL: {} ", subPlan, resultUrl);
					returnJSON = makeBambooServerCall(resultUrl, bambooServer);
					Set<Build> builds = getBuilds((JSONObject) parser.parse(returnJSON), resultUrl, branchKey);
					bambooJobs.put(bambooServer.getId(), builds);
					// Ended with nested branches
				}
			}
		}
	}

	@NotNull
	private void setPlanBuilds(ProcessorToolConnection bambooServer, Map<ObjectId, Set<Build>> bambooJobs,
			JSONParser parser, String planName) throws ParseException {
		String resultUrl = BambooClient.appendToURL(bambooServer.getUrl(), JOBS_RESULT_SUFFIX, planName);
		// Finding out the results of the top-level plan
		String returnJSON = makeBambooServerCall(resultUrl, bambooServer);
		Set<Build> builds = getBuilds((JSONObject) parser.parse(returnJSON), resultUrl, planName);
		bambooJobs.put(bambooServer.getId(), builds);
	}

	/**
	 * Getting build list for the sub plans
	 *
	 * @param jsonJob
	 * @param resultUrl
	 * @return
	 */
	private Set<Build> getBuilds(JSONObject jsonJob, String resultUrl, String jobName) {
		Set<Build> buildSet = new HashSet<>();
		getJsonArray((JSONObject) jsonJob.get("results"), "result").forEach(buildDetail -> {
			JSONObject buildObj = (JSONObject) buildDetail;
			// A basic Build object. This will be fleshed out later if this is a new Build.
			if (buildObj.containsKey(BUILD_NUMBER)) {
				String buildNumber = buildObj.get(BUILD_NUMBER).toString();
				if (!ZERO.equals(buildNumber)) {
					Build build = new Build();
					build.setBuildJob(jobName);
					build.setNumber(buildNumber);
					String bUrl = BambooClient.appendToURL(resultUrl, buildNumber);
					// Modify local host if Docker Natting is being done
					String dockerLocalHostIP = settings.getDockerLocalHostIP();
					if (StringUtils.isNotBlank(dockerLocalHostIP)) {
						bUrl = bUrl.replace("localhost", dockerLocalHostIP);
						log.debug("Bamboo Build being added & updating URL to map localhost for Docker: {}", bUrl);
					} else {
						log.debug(" Bamboo Build being added: {}", bUrl);
					}
					build.setBuildUrl(bUrl);
					buildSet.add(build);
				}
			}
		});
		return buildSet;
	}

	@Override
	public Build getBuildDetailsFromServer(String bUrl, String iUrl, ProcessorToolConnection bambooServer) {
		log.debug("Fetching the build details from buildUrl : {}, instanceUrl : {}", bUrl, iUrl);
		Build buildInfo = null;
		try {
			String newUrl = getFinalURL(bUrl, iUrl);
			log.debug("Rebuilt URL : {}", newUrl);
			String resultJSON = makeBambooServerCall(newUrl, bambooServer);
			JSONObject buildData = (JSONObject) new JSONParser().parse(resultJSON);

			// get the build data of the completed jobs
			if (null != buildData && buildData.containsKey(FINISHED) && (boolean) buildData.get(FINISHED)) {
				buildInfo = new Build();
				buildInfo.setTimestamp(System.currentTimeMillis());
				buildInfo.setBuildUrl(bUrl);
				buildInfo.setBuildStatus(getStateOfBuild(buildData.get("buildState").toString()));
				// "2020-01-23T09:13:29.961+07:00"
				if (buildData.get("buildStartedTime") != null) {
					Date startDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
							.parse(buildData.get("buildStartedTime").toString());
					buildInfo.setStartTime(startDate.getTime());
				}
				buildInfo.setDuration((Long) buildData.get("buildDuration"));
				buildInfo.setEndTime(buildInfo.getStartTime() + buildInfo.getDuration());
				buildInfo.setNumber(buildData.get(BUILD_NUMBER).toString());
				buildInfo.setLog(getLog(bUrl, bambooServer, settings.isSaveLog()));
				log.debug("Successfully got build details for: {} from server", buildInfo.getNumber());
			}

		} catch (MalformedURLException | UnsupportedEncodingException | URISyntaxException | ParseException
				| java.text.ParseException e) {
			log.error("Could not get build details", e);
		}
		return buildInfo;
	}

	@Override
	public Map<Pair<ObjectId, String>, Set<Deployment>> getDeployJobsFromServer(ProcessorToolConnection bambooServer,
			ProjectBasicConfig proBasicConfig) throws ParseException, MalformedURLException {
		return new HashMap<>();
	}

	/**
	 * Rebuilds the API endpoint because the buildUrl obtained via Bamboo API does
	 * not save the auth user info and needs to be added
	 *
	 * @param buildURL
	 *            build URL
	 * @param serverURL
	 *            server URL
	 * @return final url with BUILD_DETAILS_URL_SUFFIX
	 * @throws URISyntaxException
	 *             URISyntax Exception
	 * @throws MalformedURLException
	 *             MalformedURL Exception
	 * @throws UnsupportedEncodingException
	 *             UnsupportedEncoding Exception
	 */
	protected String getFinalURL(String buildURL, String serverURL)
			throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {
		URL buildUrl = new URL(URLDecoder.decode(buildURL, "UTF-8"));
		URL serverUrl = new URL(serverURL);
		URI newUri = new URI(serverUrl.getProtocol(), serverUrl.getUserInfo(), buildUrl.getHost(), buildUrl.getPort(),
				buildUrl.getPath(), null, null);
		return newUri.toString() + BUILD_DETAILS_URL_SUFFIX;
	}

	/**
	 * @param obj
	 * @param key
	 * @return
	 */
	private JSONArray getJsonArray(JSONObject obj, String key) {
		Object array = obj.get(key);
		return array == null ? new JSONArray() : (JSONArray) array;
	}

	private BuildStatus getStateOfBuild(String buildState) {
		switch (buildState) {
		case "Successful":
			return BuildStatus.SUCCESS;
		case "ABORTED":
			return BuildStatus.ABORTED;
		case "Failed":
			return BuildStatus.FAILURE;
		case "UNSTABLE":
			return BuildStatus.UNSTABLE;
		default:
			return BuildStatus.UNKNOWN;
		}
	}

	/**
	 *
	 *
	 * @param sUrl
	 *            server url
	 * @param bambooServer
	 *            bambooServer data
	 * @return response body
	 */
	protected String makeBambooServerCall(String sUrl, ProcessorToolConnection bambooServer) {
		log.debug("Making rest call with user: {} to Url: {}", sUrl, bambooServer.getUsername());
		ResponseEntity<String> response = restClient.exchange(URI.create(sUrl), HttpMethod.GET,
				getHttpEntity(bambooServer), String.class);
		if (HttpStatus.OK != response.getStatusCode()) {
			log.error("Got response code: {} from URL call: {} ", response.getStatusCode(), sUrl);
			throw new RestClientException("Got response" + response.toString() + " from URL :" + sUrl);
		}
		return response.getBody();

	}

	/**
	 * @param bambooServer
	 *            ProcessorToolConnection
	 * @return respEntity
	 */
	private HttpEntity<String> getHttpEntity(ProcessorToolConnection bambooServer) {
		HttpEntity<String> respEntity = null;
		String userInfo = getUserInfo(bambooServer);
		// Basic Auth only.
		if (StringUtils.isNotBlank(userInfo)) {
			respEntity = new HttpEntity<>(createHeaders(userInfo));
		}
		return respEntity;

	}

	/**
	 * @param bambooServer
	 *            basic auth data
	 * @return userInfo
	 */
	private String getUserInfo(ProcessorToolConnection bambooServer) {
		String userInfo = null;
		// get userinfo from URI or settings (in spring properties)
		if (null != bambooServer && null != bambooServer.getUsername() && null != bambooServer.getPassword()) {
			userInfo = bambooServer.getUsername() + ":" + bambooServer.getPassword().trim();
		}
		return userInfo;
	}

	/**
	 * creates Headers
	 *
	 * @param user
	 *            username credentials
	 * @return headers
	 */
	protected HttpHeaders createHeaders(String user) {
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION,
				"Basic " + Base64.getEncoder().encodeToString(user.getBytes(StandardCharsets.US_ASCII)));
		headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		return headers;
	}

	/**
	 * Gets log
	 *
	 * @param url
	 *            bamboo service url
	 * @param bambooServer
	 *            bambooServer data
	 * @param shouldGetLogs
	 *            true or false
	 * @return logs
	 * 
	 * 
	 */
	protected String getLog(String url, ProcessorToolConnection bambooServer, boolean shouldGetLogs) {
		String logs = StringUtils.EMPTY;
		if (shouldGetLogs) {
			try {
				logs = makeBambooServerCall(BambooClient.appendToURL(url, "consoleText"), bambooServer);
			} catch (RestClientException rce) {
				log.warn("BuildInfo log message not set for buildURL: {} having exception {}",
						BambooClient.appendToURL(url, "consoleText"), rce);
			}
		}
		return logs;
	}

}
