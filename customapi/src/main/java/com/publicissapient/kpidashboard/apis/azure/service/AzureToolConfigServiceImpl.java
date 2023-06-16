package com.publicissapient.kpidashboard.apis.azure.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.apis.azure.model.AzurePipelinesResponseDTO;
import com.publicissapient.kpidashboard.apis.util.RestAPIUtils;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AzureToolConfigServiceImpl {

	private static final String DEFINITIONS_URL_SUFFIX = "/_apis/build/definitions?api-version=%s";
	private static final String RELEASE_DEFINITIONS_URL = "/_apis/release/definitions?api-version=6.0";
	private static final String RELEASE_URL = "vsrm.";
	private static final String VALUE = "value";
	private static final String NAME = "name";
	private static final String ID = "id";

	@Autowired
	private RestAPIUtils restAPIUtils;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ConnectionRepository connectionRepository;

	public List<AzurePipelinesResponseDTO> getAzurePipelineNameAndDefinitionIdList(String connectionId,
			String version) {

		List<AzurePipelinesResponseDTO> responseList = new ArrayList<>();
		Optional<Connection> optConnection = connectionRepository.findById(new ObjectId(connectionId));
		if (optConnection.isPresent()) {
			Connection connection = optConnection.get();
			String baseUrl = connection.getBaseUrl() == null ? null : connection.getBaseUrl().trim();
			String username = "testDummyUser";
			String password = connection.getPat() == null ? null : restAPIUtils.decryptPassword(connection.getPat());

			StringBuilder urlBuilder = new StringBuilder();
			String finalUrl = String.format(urlBuilder.append(baseUrl).append(DEFINITIONS_URL_SUFFIX).toString(),
					version);

			try {
				HttpEntity<?> httpEntity = new HttpEntity<>(restAPIUtils.getHeaders(username, password));
				ResponseEntity<String> response = restTemplate.exchange(finalUrl, HttpMethod.GET, httpEntity,
						String.class);

				if (response.getStatusCode() == HttpStatus.OK) {
					JSONArray jsonArray = restAPIUtils.convertJSONArrayFromResponse(response.getBody(), VALUE);
					for (Object job : jsonArray) {
						AzurePipelinesResponseDTO azurePipelinesResponseDTO = new AzurePipelinesResponseDTO();
						final String pipelineName = restAPIUtils.convertToString((JSONObject) job, NAME);
						final String definitionId = restAPIUtils.convertToString((JSONObject) job, ID);
						azurePipelinesResponseDTO.setPipelineName(pipelineName);
						azurePipelinesResponseDTO.setDefinitions(definitionId);
						responseList.add(azurePipelinesResponseDTO);
					}
				} else {
					String statusCode = response.getStatusCode().toString();
					log.error("Error while fetching ProjectsAndPlanKeyList from {}. with status {}", finalUrl,
							statusCode);
				}

			} catch (Exception exception) {
				log.error("Error while fetching ProjectsAndPlanKeyList from {}:  {}", finalUrl, exception.getMessage());
			}
		}
		return responseList;
	}

	public List<AzurePipelinesResponseDTO> getAzureReleaseNameAndDefinitionIdList(String connectionId) {

		List<AzurePipelinesResponseDTO> responseList = new ArrayList<>();
		Optional<Connection> optConnection = connectionRepository.findById(new ObjectId(connectionId));
		if (optConnection.isPresent()) {
			Connection connection = optConnection.get();
			String baseUrl = connection.getBaseUrl() == null ? null : connection.getBaseUrl().trim();
			String username = "testUser";
			String password = connection.getPat() == null ? null : restAPIUtils.decryptPassword(connection.getPat());

			fetchReleaseDetails(responseList, baseUrl, username, password);
		}
		return responseList;
	}

	private void fetchReleaseDetails(List<AzurePipelinesResponseDTO> responseList, String baseUrl, String username,
			String password) {

		if (baseUrl != null) {
			StringBuilder urlBuilder = new StringBuilder();
			String resultUrl = String.format(urlBuilder.append(baseUrl, 0, 8).append(RELEASE_URL)
					.append(baseUrl, 8, baseUrl.length()).append(RELEASE_DEFINITIONS_URL).toString());

			try {
				HttpEntity<?> httpEntity = new HttpEntity<>(restAPIUtils.getHeaders(username, password));
				ResponseEntity<String> response = restTemplate.exchange(resultUrl, HttpMethod.GET, httpEntity,
						String.class);

				if (response.getStatusCode() == HttpStatus.OK) {
					JSONArray jsonArray = restAPIUtils.convertJSONArrayFromResponse(response.getBody(), VALUE);
					for (Object job : jsonArray) {
						AzurePipelinesResponseDTO azurePipelinesResponseDTO = new AzurePipelinesResponseDTO();
						final String pipelineName = restAPIUtils.convertToString((JSONObject) job, NAME);
						final String definitionId = restAPIUtils.convertToString((JSONObject) job, ID);
						azurePipelinesResponseDTO.setPipelineName(pipelineName);
						azurePipelinesResponseDTO.setDefinitions(definitionId);
						responseList.add(azurePipelinesResponseDTO);
					}
				} else {
					String resultCode = response.getStatusCode().toString();
					log.error("Error while fetching ReleasesAndDefinitionIdList from {}. with status {}", resultUrl,
							resultCode);
				}

			} catch (Exception exception) {
				log.error("Error while fetching ReleasesAndDefinitionIdList from {}:  {}", resultUrl,
						exception.getMessage());
			}
		} else {
			log.error("Connection Base Url cannot be null");
		}
	}
}
