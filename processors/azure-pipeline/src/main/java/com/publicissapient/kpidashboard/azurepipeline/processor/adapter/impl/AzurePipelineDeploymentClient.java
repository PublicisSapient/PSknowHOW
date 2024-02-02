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

import static com.publicissapient.kpidashboard.common.util.DateUtil.TIME_FORMAT;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import com.publicissapient.kpidashboard.azurepipeline.processor.adapter.AzurePipelineClient;
import com.publicissapient.kpidashboard.azurepipeline.util.AzurePipelineUtils;
import com.publicissapient.kpidashboard.common.constant.DeploymentStatus;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.Deployment;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import com.publicissapient.kpidashboard.common.util.RestOperationsFactory;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AzurePipelineDeploymentClient implements AzurePipelineClient {

	private static final String RELEASE_URL = "vsrm.";
	private static final String RELEASE_DEFINITIONS_URL = "/_apis/release/deployments?api-version=%s&definitionId=%s";
	private static final String RELEASE_PARAM_MINTIME = "&minTime=%s";
	private static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	private static final String MIN_DATE = "1970-01-01T00:00:00";

	/**
	 * Instantiate AzurePipelineDeploymentClient .
	 *
	 * @param restOperationsFactory
	 *            the object supplier for RestOperations
	 *
	 */
	@Autowired
	RestOperationsFactory<RestOperations> restOperationsFactory;

	@Override
	public Map<Deployment, Set<Deployment>> getDeploymentJobs(ProcessorToolConnection azurePipelineServer,
			long lastStartTimeOfDeployment, ProjectBasicConfig proBasicConfig) {
		log.debug("Enter getInstanceJobs");
		Map<Deployment, Set<Deployment>> result = new LinkedHashMap<>();

		try {
			String minTime = AzurePipelineUtils.getDateFromTimeInMili(lastStartTimeOfDeployment);
			StringBuilder urlBuilder = new StringBuilder();

			String resultUrl = String.format(
					urlBuilder.append(azurePipelineServer.getUrl(), 0, 8).append(RELEASE_URL)
							.append(azurePipelineServer.getUrl(), 8, azurePipelineServer.getUrl().length())
							.append(RELEASE_DEFINITIONS_URL).toString(),
					azurePipelineServer.getApiVersion(), azurePipelineServer.getJobName());

			if (!minTime.equals("1970-01-01T00:00:00.000Z")) {
				resultUrl = String.format(String.valueOf(urlBuilder.append(RELEASE_PARAM_MINTIME)), minTime);
			}

			ResponseEntity<String> responseEntity = doRestCall(resultUrl, azurePipelineServer);
			processResponse(azurePipelineServer, result, responseEntity.getBody(), proBasicConfig);

		} catch (RestClientException exception) {
			log.error("client exception loading jobs details", exception);
			throw exception;
		}
		return result;
	}

	private void processResponse(ProcessorToolConnection azurePipelineServer, Map<Deployment, Set<Deployment>> result,
			String body, ProjectBasicConfig proBasicConfig) {

		try {
			JSONParser parser = new JSONParser();
			JSONObject resObject = (JSONObject) parser.parse(body);
			JSONArray deployments = AzurePipelineUtils.getJsonArray(resObject, "value");

			for (Object deployObject : deployments) {
				Deployment deploymentJob = new Deployment();

				JSONObject jsonDeploy = (JSONObject) deployObject;
				JSONObject jsonReleaseEnv = AzurePipelineUtils.getJsonObject(jsonDeploy, "releaseEnvironment");
				JSONObject jsonDeployedBy = AzurePipelineUtils.getJsonObject(jsonDeploy, "requestedBy");
				JSONObject jsonDeployRelease = AzurePipelineUtils.getJsonObject(jsonDeploy, "release");

				deploymentJob.setEnvId(String.valueOf(jsonReleaseEnv.get("id")));
				deploymentJob.setEnvName(AzurePipelineUtils.getString(jsonReleaseEnv, "name"));
				deploymentJob.setEnvUrl(AzurePipelineUtils.getString(jsonReleaseEnv, "url"));
				deploymentJob.setProjectToolConfigId(azurePipelineServer.getId());
				deploymentJob.setBasicProjectConfigId(azurePipelineServer.getBasicProjectConfigId());
				deploymentJob.setCreatedAt(String.valueOf(System.currentTimeMillis()));
				if (proBasicConfig.isSaveAssigneeDetails()) {
					deploymentJob.setDeployedBy(AzurePipelineUtils.getString(jsonDeployedBy, "displayName"));
				}
				deploymentJob.setDeploymentStatus(getDeploymentStatus(jsonDeploy));
				deploymentJob.setNumber(String.valueOf(jsonDeployRelease.get("id")));
				deploymentJob.setJobId(azurePipelineServer.getJobName());
				deploymentJob.setJobName(azurePipelineServer.getDeploymentProjectName());
				setTime(jsonDeploy, deploymentJob);

				if (checkDeploymentConditionsNotNull(deploymentJob)) {
					if (result.containsKey(deploymentJob)) {
						Set<Deployment> deploymentSet = result.get(deploymentJob);
						deploymentSet.add(deploymentJob);
					} else {
						Set<Deployment> deploymentSet = new HashSet<>();
						deploymentSet.add(deploymentJob);
						result.put(deploymentJob, deploymentSet);
					}
				}
			}
		} catch (ParseException e) {
			log.error(String.format("Parsing jobs details on instance: %s", azurePipelineServer.getUrl()), e);
		}
	}

	private boolean checkDeploymentConditionsNotNull(Deployment deployment) {
		if (deployment.getEnvName() == null || deployment.getStartTime() == null || deployment.getEndTime() == null
				|| deployment.getDeploymentStatus() == null) {
			log.error("deployments conditions not satisfied so that data is not saved in db {}", deployment);
			return false;
		} else {
			return true;
		}
	}

	public ResponseEntity<String> doRestCall(String sUrl, ProcessorToolConnection azurePipelineServer) {
		log.debug("Enter makeRestCall {}", sUrl);
		URI theUri = URI.create(sUrl);

		if (StringUtils.isNotEmpty(azurePipelineServer.getPat())) {
			return restOperationsFactory.getTypeInstance().exchange(theUri, HttpMethod.GET,
					new HttpEntity<>(AzurePipelineUtils.createHeaders(azurePipelineServer.getPat())), String.class);
		} else {
			return restOperationsFactory.getTypeInstance().exchange(theUri, HttpMethod.GET, null, String.class);
		}
	}

	@Override
	public Map<ObjectId, Set<Build>> getInstanceJobs(ProcessorToolConnection azurePipelineServer,
			long lastStartTimeOfJobs, ProjectBasicConfig proBasicConfig) {
		return new HashMap<>();
	}

	private void setTime(JSONObject jsonDeploy, Deployment deployment) {
		try {
			String startDate = String.valueOf(jsonDeploy.get("startedOn"));
			String endDate = String.valueOf(jsonDeploy.get("completedOn"));

			Long startDateTime = Instant.parse(AzurePipelineUtils.getString(jsonDeploy, "startedOn")).toEpochMilli();
			Long endDateTime = Instant.parse(AzurePipelineUtils.getString(jsonDeploy, "completedOn")).toEpochMilli();

			if (StringUtils.isNotEmpty(startDate)) {

				deployment.setStartTime(DateUtil.dateTimeConverter(startDate, DATETIME_FORMAT, TIME_FORMAT));
				deployment.setEndTime(DateUtil.dateTimeConverter(endDate, DATETIME_FORMAT, TIME_FORMAT));
				deployment.setDuration(endDateTime - startDateTime);
			}

		} catch (DateTimeParseException | NumberFormatException ex) {
			log.error("Exception in changing date " + ex);
			if (StringUtils.isEmpty(deployment.getStartTime())) {
				deployment.setStartTime(DateUtil.dateTimeFormatter(LocalDateTime.parse(MIN_DATE), TIME_FORMAT));
			}
			deployment.setEndTime(DateUtil.dateTimeFormatter(LocalDateTime.parse(MIN_DATE), TIME_FORMAT));
			deployment.setDuration(0);
		} finally {
			deployment.setCreatedAt(DateUtil.dateTimeFormatter(
					Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDateTime(),
					TIME_FORMAT));
		}
	}

	private DeploymentStatus getDeploymentStatus(JSONObject jsonDeploy) {
		String status = AzurePipelineUtils.getString(jsonDeploy, "deploymentStatus");
		switch (status) {
		case "succeeded":
			return DeploymentStatus.SUCCESS;
		case "partiallySucceeded":
		case "notDeployed":
			return DeploymentStatus.UNSTABLE;
		case "failed":
			return DeploymentStatus.FAILURE;
		case "canceled":
			return DeploymentStatus.ABORTED;
		default:
			return DeploymentStatus.UNKNOWN;
		}
	}

}
