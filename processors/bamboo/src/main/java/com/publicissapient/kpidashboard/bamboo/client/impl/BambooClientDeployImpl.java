package com.publicissapient.kpidashboard.bamboo.client.impl;

import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
import com.publicissapient.kpidashboard.common.constant.DeploymentStatus;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.Deployment;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BambooClientDeployImpl implements BambooClient {
	private static final String DEPLOYMENT_SUFFIX = "/rest/api/latest/deploy/dashboard/";
	private static final String ENVIRONMENT_SUFFIX = "/rest/api/latest/deploy/environment/%s/results";
	private static final String MINIMUM_DATE = "1970-01-01T00:00:00";
	private static final String QUEUED = "queued";

	@Autowired
	private RestTemplate restClient;

	@Override
	public Map<Pair<ObjectId, String>, Set<Deployment>> getDeployJobsFromServer(ProcessorToolConnection bambooServer,
			ProjectBasicConfig proBasicConfig) throws ParseException, MalformedURLException {
		Map<Pair<ObjectId, String>, Set<Deployment>> deploySetMap = new HashMap<>();
		String deploymentProjectId = bambooServer.getDeploymentProjectId();
		String url = BambooClient.appendToURL(bambooServer.getUrl() + DEPLOYMENT_SUFFIX + deploymentProjectId);
		String environemntUrl = BambooClient.appendToURL(bambooServer.getUrl() + ENVIRONMENT_SUFFIX);
		HttpEntity<String> httpAuth = generateAuthentication(bambooServer);
		String deployInformation = connectBamboo(url, bambooServer, httpAuth);
		Set<Deployment> environments = getEnvironments(deployInformation, environemntUrl, httpAuth, bambooServer,
				proBasicConfig);
		deploySetMap.put(Pair.of(bambooServer.getId(), bambooServer.getDeploymentProjectId()), environments);
		return deploySetMap;
	}

	private Set<Deployment> getEnvironments(String deployInformation, String environemntUrl,
			HttpEntity<String> httpAuth, ProcessorToolConnection bambooServer, ProjectBasicConfig proBasicConfig)
			throws ParseException {
		Set<Deployment> deployments = new HashSet<>();
		Map<String, String> environments = parseJsonToFetchEnv(deployInformation);
		for (Map.Entry<String, String> env : environments.entrySet()) {
			String environemntInformation = connectBamboo(
					String.format(new StringBuilder(environemntUrl).toString(), env.getKey()), bambooServer, httpAuth);
			Set<Deployment> deploymentSet = getEnvironmentInformation(environemntInformation, proBasicConfig);
			deploymentSet.forEach(deployment -> {
				deployment.setBasicProjectConfigId(bambooServer.getBasicProjectConfigId());
				deployment.setProjectToolConfigId(bambooServer.getId());
				deployment.setJobId(bambooServer.getDeploymentProjectId());
				deployment.setJobName(bambooServer.getDeploymentProjectName());
				deployment.setEnvId(env.getKey());
				deployment.setEnvName(env.getValue());
			});

			deployments.addAll(deploymentSet);
		}

		return deployments;
	}

	private String convertToString(JSONObject jsonData, String key) {
		Object jsonObj = jsonData.get(key);
		return jsonObj == null ? null : jsonObj.toString();
	}

	private Set<Deployment> getEnvironmentInformation(String environemntInformation, ProjectBasicConfig proBasicConfig)
			throws ParseException {
		JSONParser respParser = new JSONParser();
		Set<Deployment> deploymentSet = new HashSet<>();
		JSONArray results = (JSONArray) ((JSONObject) respParser.parse(environemntInformation)).get("results");
		if (!results.isEmpty()) {
			results.forEach(result -> {
				try {
					Deployment deployment = new Deployment();
					JSONObject resultObject = (JSONObject) result;
					deployment.setDeploymentStatus(
							QUEUED.equalsIgnoreCase(convertToString(resultObject, "lifeCycleState"))
									? getDeploymentStatus(QUEUED)
									: getDeploymentStatus(
											convertToString(resultObject, "deploymentState").toLowerCase()));
					JSONObject deploymentVersion = (JSONObject) (resultObject).get("deploymentVersion");
					deployment.setNumber(convertToString(resultObject, "id"));
					if (proBasicConfig.isSaveAssigneeDetails()) {
						deployment.setDeployedBy(convertToString(deploymentVersion, "creatorUserName"));
					}
					settingTime(resultObject, deployment);
					deploymentSet.add(deployment);
				} catch (DateTimeParseException | NumberFormatException ex) {
					log.error("Could not get deploy Environment details", ex);
				}
			});
		}
		return deploymentSet;
	}

	private void settingTime(JSONObject resultObject, Deployment deployment) {
		try {
			long startedDate = Long.parseLong(convertToString(resultObject, "startedDate"));
			long finishedDate = Long.parseLong(convertToString(resultObject, "finishedDate"));

			if (startedDate > 0) {
				LocalDateTime startDateTime = Instant.ofEpochMilli(startedDate).atZone(ZoneId.systemDefault())
						.toLocalDateTime();
				deployment.setStartTime(DateUtil.dateTimeFormatter(startDateTime, DateUtil.TIME_FORMAT));
				LocalDateTime endDateTime = Instant.ofEpochMilli(finishedDate).atZone(ZoneId.systemDefault())
						.toLocalDateTime();
				deployment.setEndTime(DateUtil.dateTimeFormatter(endDateTime, DateUtil.TIME_FORMAT));
				deployment.setDuration(Duration.between(startDateTime, endDateTime).toMillis());
			}

		} catch (DateTimeParseException | NumberFormatException ex) {
			log.error("Exception while transforming date " + ex);
			if (StringUtils.isEmpty(deployment.getStartTime())) {
				deployment.setStartTime(
						DateUtil.dateTimeFormatter(LocalDateTime.parse(MINIMUM_DATE), DateUtil.TIME_FORMAT));
			}
			deployment.setEndTime(DateUtil.dateTimeFormatter(LocalDateTime.parse(MINIMUM_DATE), DateUtil.TIME_FORMAT));
			deployment.setDuration(0);
		} finally {
			deployment.setCreatedAt(DateUtil.dateTimeFormatter(
					Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDateTime(),
					DateUtil.TIME_FORMAT));
		}
	}

	private Map<String, String> parseJsonToFetchEnv(String deployInformation) throws ParseException {
		Map<String, String> environments = new TreeMap<>();
		JSONParser respParser = new JSONParser();
		Object deployArray = ((JSONArray) respParser.parse(deployInformation)).get(0);
		JSONArray environmentStatuses = (JSONArray) ((JSONObject) deployArray).get("environmentStatuses");

		if (!environmentStatuses.isEmpty()) {
			environmentStatuses.forEach(envStatus -> {
				JSONObject environment = (JSONObject) ((JSONObject) envStatus).get("environment");
				environments.put(convertToString(environment, "id"), convertToString(environment, "name"));
			}

			);

		}
		return environments;
	}

	private HttpEntity<String> generateAuthentication(ProcessorToolConnection bambooServer) {
		String userInfo = null;
		HttpEntity<String> respEntity = null;
		if (null != bambooServer && null != bambooServer.getUsername() && null != bambooServer.getPassword()) {
			userInfo = bambooServer.getUsername() + ":" + bambooServer.getPassword().trim();
		}

		if (StringUtils.isNotBlank(userInfo)) {
			HttpHeaders headers = new HttpHeaders();
			headers.set(HttpHeaders.AUTHORIZATION,
					"Basic " + Base64.getEncoder().encodeToString(userInfo.getBytes(StandardCharsets.US_ASCII)));
			headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
			respEntity = new HttpEntity<>(headers);
		}
		return respEntity;
	}

	public String connectBamboo(String sUrl, ProcessorToolConnection bambooServer, HttpEntity<String> respEntity) {
		log.debug("Making rest call with user: {} to Url: {}", sUrl, bambooServer.getUsername());
		ResponseEntity<String> response = restClient.exchange(URI.create(sUrl), HttpMethod.GET, respEntity,
				String.class);
		if (HttpStatus.OK != response.getStatusCode()) {
			log.error("Got response code: {} from URL call: {} ", response.getStatusCode(), sUrl);
			throw new RestClientException("Got response" + response.toString() + " from URL :" + sUrl);
		}
		return response.getBody();

	}

	@Override
	public Map<ObjectId, Set<Build>> getJobsFromServer(ProcessorToolConnection bambooServer,
			ProjectBasicConfig proBasicConfig) throws ParseException, MalformedURLException {
		return new HashMap<>();
	}

	@Override
	public Build getBuildDetailsFromServer(String buildUrl, String instanceUrl, ProcessorToolConnection bambooServer) {
		return null;
	}

	private DeploymentStatus getDeploymentStatus(String deployStatus) {
		switch (deployStatus) {
		case "success":
			return DeploymentStatus.SUCCESS;
		case "aborted":
			return DeploymentStatus.ABORTED;
		case "failed":
			return DeploymentStatus.FAILURE;
		case "unstable":
			return DeploymentStatus.UNSTABLE;
		case QUEUED:
			return DeploymentStatus.IN_PROGRESS;
		case "in progress":
			return DeploymentStatus.IN_PROGRESS;
		default:
			return DeploymentStatus.UNKNOWN;
		}
	}

}
