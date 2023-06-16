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

package com.publicissapient.kpidashboard.azurepipeline.processor.adapter.impl;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import com.publicissapient.kpidashboard.azurepipeline.config.AzurePipelineConfig;
import com.publicissapient.kpidashboard.azurepipeline.processor.adapter.AzurePipelineClient;
import com.publicissapient.kpidashboard.azurepipeline.util.AzurePipelineUtils;
import com.publicissapient.kpidashboard.common.constant.BuildStatus;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.Deployment;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.util.RestOperationsFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * AzurePipelineClient implementation that uses RestTemplate and JSONSimple to
 * fetch information from AzurePipeline instances.
 */
@Component
@Slf4j
@Primary
public class DefaultAzurePipelineClient implements AzurePipelineClient {

	/**
	 * Instantiate DefaultAzurePipelineClient.
	 *
	 * @param restOperationsFactory
	 *            the object supplier for RestOperations
	 * @param azurePipelineConfig
	 *            the AzurePipeline configuration details
	 */
	@Autowired
	RestOperationsFactory<RestOperations> restOperationsFactory;
	@Autowired
	private AzurePipelineConfig azurePipelineConfig;

	/**
	 * Provides Instance Jobs.
	 *
	 * @param azurePipelineServer
	 *            the connection properties for AzurePipeline server
	 * @param lastStartTimeOfBuilds
	 *            the last updated time of the processor which is used for delta
	 *            import
	 * @param proBasicConfig
	 * @return the map of azurePipeline jobs and set of builds
	 */
	@Override
	public Map<ObjectId, Set<Build>> getInstanceJobs(ProcessorToolConnection azurePipelineServer,
			long lastStartTimeOfBuilds, ProjectBasicConfig proBasicConfig) {
		log.debug("Enter getInstanceJobs");
		Map<ObjectId, Set<Build>> result = new LinkedHashMap<>();

		try {
			String minTime = AzurePipelineUtils.getDateFromTimeInMili(lastStartTimeOfBuilds);
			StringBuilder url = new StringBuilder(
					AzurePipelineUtils.joinURL(azurePipelineServer.getUrl(), azurePipelineConfig.getApiEndPoint()));
			url = AzurePipelineUtils.addParam(url, "api-version", azurePipelineServer.getApiVersion());
			url = AzurePipelineUtils.addParam(url, "definitions", azurePipelineServer.getJobName());
			if (!minTime.equals("1970-01-01T00:00:00.000Z")) {
				url = AzurePipelineUtils.addParam(url, "minTime", minTime);
			}
			ResponseEntity<String> responseEntity = doRestCall(url.toString(), azurePipelineServer);
			processResponse(azurePipelineServer, result, responseEntity.getBody(), proBasicConfig);
		} catch (RestClientException rce) {
			log.error("client exception loading jobs details", rce);
			throw rce;
		}
		return result;
	}

	@Override
	public Map<Deployment, Set<Deployment>> getDeploymentJobs(ProcessorToolConnection azurePipelineServer,
			long lastStartTimeOfJobs, ProjectBasicConfig proBasicConfig) {
		return new HashMap<>();
	}

	/**
	 * Processes response of the api call. In response we get array of builds. We
	 * iterate over each build object and create a azurepipeline job and check
	 * whether the job is present in the result map. If the job is present we add
	 * the build to the build set of that job else we create a new job and then add
	 * the build to its build set. Current implementation covers the case of having
	 * more than one azurepipeline job but ideally we would have 1 job only. This
	 * was done if in future we change the implementation to include more than 1 job
	 * only the rest api call would change.
	 *
	 * @param azurePipelineServer
	 *            the connection properties for AzurePipeline server
	 * @param result
	 *            the map of azurePipeline jobs and set of builds
	 * @param resJSON
	 *            response body of rest api call
	 * @param proBasicConfig
	 */
	private void processResponse(ProcessorToolConnection azurePipelineServer, Map<ObjectId, Set<Build>> result,
			String resJSON, ProjectBasicConfig proBasicConfig) {
		try {
			JSONParser parser = new JSONParser();
			JSONObject resObject = (JSONObject) parser.parse(resJSON);
			JSONArray builds = AzurePipelineUtils.getJsonArray(resObject, "value");
			Set<Build> buildSet = new HashSet<>();
			for (Object buildObject : builds) {
				JSONObject jsonBuild = (JSONObject) buildObject;

				Build build = createBuild(jsonBuild, proBasicConfig);
				buildSet.add(build);
				result.put(azurePipelineServer.getId(), buildSet);
			}

		} catch (ParseException e) {
			log.error(String.format("Parsing jobs details on instance: %s", azurePipelineServer.getUrl()), e);
		}
	}

	/**
	 * Creates Build Object
	 *
	 * @param buildJson
	 *            the build as JSON object
	 * @param proBasicConfig
	 * @return the build object
	 */
	private Build createBuild(JSONObject buildJson, ProjectBasicConfig proBasicConfig) {
		JSONObject jsonRequestedFor = AzurePipelineUtils.getJsonObject(buildJson, "requestedFor");
		Build build = new Build();
		if (proBasicConfig.isSaveAssigneeDetails()) {
			build.setStartedBy(AzurePipelineUtils.getString(jsonRequestedFor, "displayName"));
		}
		build.setBuildUrl(AzurePipelineUtils.getString(buildJson, "url"));
		build.setNumber(String.valueOf(buildJson.get("id")));
		build.setStartTime(Instant.parse(AzurePipelineUtils.getString(buildJson, "startTime")).toEpochMilli());
		build.setEndTime(Instant.parse(AzurePipelineUtils.getString(buildJson, "finishTime")).toEpochMilli());
		build.setBuildStatus(getBuildStatus(buildJson));
		build.setDuration(build.getEndTime() - build.getStartTime());
		build.setTimestamp(System.currentTimeMillis());
		return build;
	}

	/**
	 * Provides Build Status.
	 *
	 * @param buildJson
	 *            the build as JSON object
	 * @return the build status
	 */
	private BuildStatus getBuildStatus(JSONObject buildJson) {
		String status = AzurePipelineUtils.getString(buildJson, "result");
		switch (status) {
		case "succeeded":
			return BuildStatus.SUCCESS;
		case "partiallySucceeded":
			return BuildStatus.UNSTABLE;
		case "failed":
			return BuildStatus.FAILURE;
		case "canceled":
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
	 * @param azurePipelineServer
	 *            the connection properties for AzurePipeline server
	 * @return the response entity
	 *
	 */
	public ResponseEntity<String> doRestCall(String sUrl, ProcessorToolConnection azurePipelineServer) {
		log.debug("Enter makeRestCall {}", sUrl);
		URI theUri = URI.create(sUrl);
		String pat = getPat(sUrl, azurePipelineServer);

		if (StringUtils.isNotEmpty(pat)) {
			return restOperationsFactory.getTypeInstance().exchange(theUri, HttpMethod.GET,
					new HttpEntity<>(AzurePipelineUtils.createHeaders(pat)), String.class);
		} else {
			return restOperationsFactory.getTypeInstance().exchange(theUri, HttpMethod.GET, null, String.class);
		}
	}

	/**
	 * Gets pat info
	 *
	 * @param sUrl
	 *            the url
	 * @param azurePipelineServer
	 *            azurePipeline server url
	 * @return pat info eg. :pat
	 */
	private String getPat(String sUrl, ProcessorToolConnection azurePipelineServer) {
		String pat = "";

		if (AzurePipelineUtils.isSameServerInfo(sUrl, azurePipelineServer.getUrl())) {

			if (StringUtils.isNotEmpty(azurePipelineServer.getPat())) {
				pat = azurePipelineServer.getPat();
			} else {
				log.warn(
						"Credentials for the following url was not found. This could happen if the domain/subdomain/IP address in the build url returned by AzurePipeline and the AzurePipeline instance url in your configuration do not match: {} ",
						sUrl);
			}

		}

		return pat;
	}
}
