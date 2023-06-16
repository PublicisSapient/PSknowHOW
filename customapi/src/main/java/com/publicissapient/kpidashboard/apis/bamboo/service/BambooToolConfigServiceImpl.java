package com.publicissapient.kpidashboard.apis.bamboo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.apis.bamboo.model.BambooBranchesResponseDTO;
import com.publicissapient.kpidashboard.apis.bamboo.model.BambooDeploymentProjectsResponseDTO;
import com.publicissapient.kpidashboard.apis.bamboo.model.BambooPlansResponseDTO;
import com.publicissapient.kpidashboard.apis.util.RestAPIUtils;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BambooToolConfigServiceImpl {

	public static final String PLAN_LIST = "plans";
	public static final String PLAN = "plan";
	public static final String KEY = "key";
	public static final String BRANCH_LIST = "branches";
	public static final String BRANCH = "branch";
	public static final String JOB_NAME = "name";
	public static final String BRANCH_NAME = "shortName";
	public static final String SEARCHKEY = "searchResults";
	public static final String SEARCHLIST = "searchEntity";
	public static final String DEPLOYMENT_PROJECT = "projectName";
	public static final String PROJECT_ID = "key";
	private static final String JOBS_URL_SUFFIX = "/rest/api/latest/plan.json?expand=plans&max-result=2000";
	private static final String DEPLOYMENTPROJECT_URL_SUFFIX = "/rest/api/latest/search/deployments.json?max-result=2000";
	private static final String RESOURCE_BRANCH_ENDPOINT = "/rest/api/latest/plan/%s/branch.json?max-result=2000";
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private RestAPIUtils restAPIUtils;

	@Autowired
	private ConnectionRepository connectionRepository;

	public List<BambooPlansResponseDTO> getProjectsAndPlanKeyList(String connectionId) {

		List<BambooPlansResponseDTO> responseDTOList = new ArrayList<>();
		Optional<Connection> optConnection = connectionRepository.findById(new ObjectId(connectionId));
		if (optConnection.isPresent()) {
			Connection connection = optConnection.get();
			String baseUrl = connection.getBaseUrl() == null ? null : connection.getBaseUrl().trim();
			String username = connection.getUsername() == null ? null : connection.getUsername().trim();
			String password = connection.getPassword() == null ? null
					: restAPIUtils.decryptPassword(connection.getPassword());

			String url = baseUrl + JOBS_URL_SUFFIX;

			HttpEntity<?> httpEntity = new HttpEntity<>(restAPIUtils.getHeaders(username, password));
			try {

				ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);

				if (response.getStatusCode() == HttpStatus.OK) {
					JSONParser respParser = new JSONParser();
					JSONObject object = (JSONObject) respParser.parse(response.getBody());
					for (Object job : restAPIUtils.getJsonArrayFromJSONObj((JSONObject) object.get(PLAN_LIST), PLAN)) {
						BambooPlansResponseDTO bambooPlansResponseDTO = new BambooPlansResponseDTO();
						final String jobPlanKey = restAPIUtils.convertToString((JSONObject) job, KEY);
						final String projectNamePlanName = restAPIUtils.convertToString((JSONObject) job, JOB_NAME);
						bambooPlansResponseDTO.setProjectAndPlanName(projectNamePlanName);
						bambooPlansResponseDTO.setJobNameKey(jobPlanKey);
						responseDTOList.add(bambooPlansResponseDTO);
					}
				} else {
					String statusCode = response.getStatusCode().toString();
					log.error("Error while fetching ProjectsAndPlanKeyList from {}. with status {}", url, statusCode);
				}

			} catch (Exception exception) {
				log.error("Error while fetching ProjectsAndPlanKeyList from {}:  {}", url, exception.getMessage());
			}
			return responseDTOList;
		}
		return responseDTOList;
	}

	public List<BambooBranchesResponseDTO> getBambooBranchesNameAndKeys(String connectionId, String jobNameKey) {
		List<BambooBranchesResponseDTO> responseDTOList = new ArrayList<>();
		Optional<Connection> optConnection = connectionRepository.findById(new ObjectId(connectionId));
		if (optConnection.isPresent()) {
			Connection connection = optConnection.get();
			String baseUrl = connection.getBaseUrl() == null ? null : connection.getBaseUrl().trim();
			String username = connection.getUsername() == null ? null : connection.getUsername().trim();
			String password = connection.getPassword() == null ? null
					: restAPIUtils.decryptPassword(connection.getPassword());

			String url = String.format(new StringBuilder(baseUrl).append(RESOURCE_BRANCH_ENDPOINT).toString(),
					jobNameKey);

			HttpEntity<?> httpEntity = new HttpEntity<>(restAPIUtils.getHeaders(username, password));
			try {

				ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);

				if (response.getStatusCode() == HttpStatus.OK) {
					parseBranchesResponse(responseDTOList, response);
				} else {
					String statusCode = response.getStatusCode().toString();
					log.error("Error while fetching BambooBranchesNameAndKeys from {}. with status {}", url,
							statusCode);
				}

			} catch (Exception exception) {
				log.error("Error while fetching BambooBranchesNameAndKeys from {}:  {}", url, exception.getMessage());
			}
			return responseDTOList;
		}
		return responseDTOList;
	}

	private void parseBranchesResponse(List<BambooBranchesResponseDTO> responseDTOList, ResponseEntity<String> response)
			throws ParseException {
		JSONParser respParser = new JSONParser();
		JSONObject object = (JSONObject) respParser.parse(response.getBody());
		JSONObject branches = (JSONObject) object.get(BRANCH_LIST);
		Long size = (Long) branches.get("size");
		if (size > 0) {
			for (Object job : restAPIUtils.getJsonArrayFromJSONObj(branches, BRANCH)) {
				BambooBranchesResponseDTO bambooBranchesResponseDTO = new BambooBranchesResponseDTO();
				final String jobBranchKey = restAPIUtils.convertToString((JSONObject) job, KEY);
				final String branchName = restAPIUtils.convertToString((JSONObject) job, BRANCH_NAME);
				bambooBranchesResponseDTO.setBranchName(branchName);
				bambooBranchesResponseDTO.setJobBranchKey(jobBranchKey);
				responseDTOList.add(bambooBranchesResponseDTO);
			}
		}
	}

	/**
	 * fetch deployment project list
	 * 
	 * @param connectionId
	 * @return
	 */
	public List<BambooDeploymentProjectsResponseDTO> getDeploymentProjectList(String connectionId) {
		List<BambooDeploymentProjectsResponseDTO> responseDTOList = new ArrayList<>();
		Optional<Connection> optConnection = connectionRepository.findById(new ObjectId(connectionId));
		if (optConnection.isPresent()) {
			Connection connection = optConnection.get();
			String baseUrl = connection.getBaseUrl() == null ? null : connection.getBaseUrl().trim();
			String username = connection.getUsername() == null ? null : connection.getUsername().trim();
			String password = connection.getPassword() == null ? null
					: restAPIUtils.decryptPassword(connection.getPassword());

			String url = baseUrl + DEPLOYMENTPROJECT_URL_SUFFIX;
			HttpEntity<?> httpEntity = new HttpEntity<>(restAPIUtils.getHeaders(username, password));
			try {
				ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);

				if (response.getStatusCode() == HttpStatus.OK) {
					JSONParser respParser = new JSONParser();
					JSONArray searchResults = (JSONArray) ((JSONObject) respParser.parse(response.getBody()))
							.get(SEARCHKEY);

					for (Object job : searchResults) {
						BambooDeploymentProjectsResponseDTO bambooPlansResponseDTO = new BambooDeploymentProjectsResponseDTO();
						Object searchEntity = ((JSONObject) job).get(SEARCHLIST);
						final String deploymentProjectName = restAPIUtils
								.convertToString((JSONObject) searchEntity, DEPLOYMENT_PROJECT).trim();
						final String deploymentProjectId = restAPIUtils
								.convertToString((JSONObject) searchEntity, PROJECT_ID).trim();
						bambooPlansResponseDTO.setDeploymentProjectName(deploymentProjectName);
						bambooPlansResponseDTO.setDeploymentProjectId(deploymentProjectId);
						responseDTOList.add(bambooPlansResponseDTO);
					}
				} else {
					String statusCode = response.getStatusCode().toString();
					log.error("Error while fetching Deployment projects from {}. with status {}", url, statusCode);
				}

			} catch (Exception exception) {
				log.error("Error while fetching Deployment projects from {}:  {}", url, exception.getMessage());
			}
			return responseDTOList;
		}
		return responseDTOList;
	}
}
