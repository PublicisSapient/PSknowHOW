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
import java.time.LocalDateTime;
import java.util.Date;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import com.publicissapient.kpidashboard.common.constant.DeploymentStatus;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.Deployment;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import com.publicissapient.kpidashboard.common.util.RestOperationsFactory;
import com.publicissapient.kpidashboard.jenkins.config.Constants;
import com.publicissapient.kpidashboard.jenkins.model.JenkinsProcessor;
import com.publicissapient.kpidashboard.jenkins.processor.adapter.JenkinsClient;
import com.publicissapient.kpidashboard.jenkins.util.ProcessorUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JenkinsDeployClient implements JenkinsClient {

	private static final String DEPLOYMENT_URL = "/job/%s/api/json?tree=builds[number,status,timestamp,id,result,duration,actions[parameters[name,value]]]";
	private static final String ID = "id";
	private static final String ACTIONS = "actions";
	private static final String PARAMETERS = "parameters";
	private static final String VALUE = "value";
	private final RestOperations restOperations;

	public JenkinsDeployClient(RestOperationsFactory<RestOperations> restOperationsFactory) {
		this.restOperations = restOperationsFactory.getTypeInstance();
	}

	@Override
	public Map<String, Set<Deployment>> getDeployJobsFromServer(ProcessorToolConnection jenkinsServer,
			JenkinsProcessor processor) {
		Map<String, Set<Deployment>> deployMap = new LinkedHashMap<>();
		try {
			String jobName = jenkinsServer.getJobName().replace("/", "/job/");
			String url = String.format(new StringBuilder(jenkinsServer.getUrl()).append(DEPLOYMENT_URL).toString(),
					jobName);
			ResponseEntity<String> responseEntity = doRestCall(url, jenkinsServer);
			if (StringUtils.isNotEmpty(responseEntity.getBody())) {
				processResponse(jenkinsServer, deployMap, responseEntity.getBody(), processor);
			}
		} catch (RestClientException e) {
			log.error(String.format("Error getting for instance : %s, job : %s", jenkinsServer.getUrl(),
					jenkinsServer.getJobName()), e);
		}
		return deployMap;
	}

	private void processResponse(ProcessorToolConnection jenkinsServer, Map<String, Set<Deployment>> deployMap,
			String returnJSON, JenkinsProcessor processor) {
		Set<Deployment> deployments = new LinkedHashSet<>();
		try {
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(returnJSON);
			JSONArray builds = ProcessorUtils.getJsonArray(jsonObject, Constants.BUILDS);
			if (!builds.isEmpty()) {
				builds.forEach(build -> {
					Deployment deployment = new Deployment();
					deployment = prepareDeploymentObject(build, deployment, jenkinsServer, processor);
					if (checkDeploymentConditionsNotNull(deployment)) {
						deployments.add(deployment);
					}
				});
			}
			// added deployments to the job
			deployMap.put(jenkinsServer.getJobName(), deployments);

		} catch (ParseException e) {
			log.error(String.format("Parsing jobs details on instance : %s ", jenkinsServer.getUrl()), e);
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

	private Deployment prepareDeploymentObject(Object build, Deployment deployment,
			ProcessorToolConnection jenkinsServer, JenkinsProcessor processor) {
		JSONObject buildJsonObj = (JSONObject) build;
		deployment.setProcessorId(processor.getId());
		deployment.setBasicProjectConfigId(jenkinsServer.getBasicProjectConfigId());
		deployment.setProjectToolConfigId(jenkinsServer.getId());
		String serverJobName = jenkinsServer.getJobName();
		int indexJ = serverJobName.lastIndexOf('/');
		String jobName = serverJobName.substring(indexJ + 1);
		deployment.setJobName(jobName);
		deployment.setJobFolderName(jenkinsServer.getJobName());
		deployment.setNumber((String) buildJsonObj.get(ID));
		long duration = (long) buildJsonObj.get(Constants.DURATION);
		long timestamp = (long) buildJsonObj.get(Constants.TIMESTAMP);
		long endTime = duration + timestamp;
		String status = (String) buildJsonObj.get(Constants.RESULT);
		if (status != null) {
			deployment.setDeploymentStatus((DeploymentStatus.fromString(status)));
			deployment.setEndTime(DateUtil.dateTimeFormatter(new Date(endTime), DateUtil.TIME_FORMAT));
		} else {
			deployment.setDeploymentStatus(DeploymentStatus.IN_PROGRESS);
			deployment.setEndTime("");
		}
		deployment.setDuration(duration);
		deployment.setStartTime(DateUtil.dateTimeFormatter(new Date(timestamp), DateUtil.TIME_FORMAT));
		deployment.setCreatedAt(DateUtil.dateTimeFormatter(LocalDateTime.now(), DateUtil.TIME_FORMAT));
		JSONArray actions = ProcessorUtils.getJsonArray(buildJsonObj, ACTIONS);
		getEnvFromDeployJob(actions, jenkinsServer, deployment);
		return deployment;
	}

	private Deployment getEnvFromDeployJob(JSONArray actions, ProcessorToolConnection jenkinsServer,
			Deployment deployment) {
		actions.forEach(action -> {
			JSONArray parameters = ProcessorUtils.getJsonArray((JSONObject) action, PARAMETERS);
			for (Object parameter : parameters) {
				JSONObject parameterJsonObj = (JSONObject) parameter;
				String name = ProcessorUtils.getString(parameterJsonObj, Constants.NAME);
				if (name.equalsIgnoreCase(jenkinsServer.getParameterNameForEnvironment())
						&& parameterJsonObj.get(VALUE) != null && parameterJsonObj.get(VALUE) != "") {
					String value = ProcessorUtils.getString(parameterJsonObj, VALUE);
					deployment.setEnvName(value);
					break;
				}
			}
		});
		return deployment;
	}

	private ResponseEntity<String> doRestCall(String url, ProcessorToolConnection jenkinsServer) {
		URI uri = URI.create(url);
		String userInfo = uri.getUserInfo();
		if (StringUtils.isEmpty(userInfo)) {
			userInfo = getUserInfo(url, jenkinsServer);
		}
		if (StringUtils.isNotEmpty(userInfo)) {
			return restOperations.exchange(uri, HttpMethod.GET,
					new HttpEntity<>(ProcessorUtils.createHeaders(userInfo)), String.class);
		} else {
			return restOperations.exchange(uri, HttpMethod.GET, null, String.class);
		}
	}

	private String getUserInfo(String url, ProcessorToolConnection jenkinsServer) {
		String userInfo = "";
		if (ProcessorUtils.isSameServerInfo(url, jenkinsServer.getUrl())) {
			if (StringUtils.isNotEmpty(jenkinsServer.getUsername())
					&& StringUtils.isNotEmpty(jenkinsServer.getApiKey())) {
				userInfo = jenkinsServer.getUsername() + ":" + jenkinsServer.getApiKey();
			} else {
				log.warn(
						"Credentials for the following url was not found. This could happen if the domain/subdomain/IP address in the deployment url returned by Jenkins and the Jenkins instance url in your KnowHOW configuration do not match: {} ",
						url);
			}
		}
		return userInfo;
	}

	@Override
	public Map<ObjectId, Set<Build>> getBuildJobsFromServer(ProcessorToolConnection jenkinsServer,
			ProjectBasicConfig proBasicConfig) {
		return new HashMap<>();
	}

}
