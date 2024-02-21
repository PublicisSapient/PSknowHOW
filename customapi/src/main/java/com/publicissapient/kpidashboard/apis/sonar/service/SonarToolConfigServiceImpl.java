package com.publicissapient.kpidashboard.apis.sonar.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.sonar.utiils.SonarAPIUtils;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.sonar.Paging;
import com.publicissapient.kpidashboard.common.model.sonar.SearchProjectsResponse;
import com.publicissapient.kpidashboard.common.model.sonar.SonarComponent;
import com.publicissapient.kpidashboard.common.model.sonar.SonarVersionResponseDTO;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SonarToolConfigServiceImpl {

	public static final String PROJECTS_LIST = "components";
	public static final String PROJECT_KEY = "key";
	public static final String BRANCH_LIST = "branches";
	public static final String BRANCH_NAME = "name";
	private static final String RESOURCE_PROJECT_ENDPOINT = "/api/components/search?qualifiers=TRK&p=%d&ps=%d";

	private static final String RESOURCE_BRANCH_ENDPOINT = "/api/project_branches/list?project=%s";
	private static final String RESOURCE_CLOUD_PROJECT_ENDPOINT = "/api/components/search?qualifiers=TRK&organization=%s&p=%d&ps=%d";
	private static final List<String> SONAR_SERVER_VERSION_BRANCH_NOT_SUPPORTED = Arrays.asList("6.5", "6.4", "6.3",
			"6.2", "6.1", "6.0");
	private static final List<String> SONAR_SERVER_VERSION_BRANCH_SUPPORTED = Arrays.asList("9.x", "8.x", "7.x", "6.7",
			"6.6");
	private static final List<String> SONAR_CLOUD_VERSION_BRANCH_NOT_SUPPORTED = Arrays.asList("7.1", "7.0", "6.x");
	private static final List<String> SONAR_CLOUD_VERSION_BRANCH_SUPPORTED = Arrays.asList("9.x", "8.x", "7.9", "7.8",
			"7.7", "7.6", "7.5", "7.4", "7.3", "7.2");
	private static final String SONAR_SERVER = "Sonar Server";
	private static final String SONAR_CLOUD = "Sonar Cloud";
	@Autowired
	private ConnectionRepository connectionRepository;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private AesEncryptionService aesEncryptionService;
	@Autowired
	private CustomApiConfig customApiConfig;
	private ObjectMapper mapper = new ObjectMapper();

	/**
	 * Provides the list of Sonar version based on branch support and type
	 *
	 * @return the list of Sonar server and sonar cloud version
	 */
	public ServiceResponse getSonarVersionList() {
		List<SonarVersionResponseDTO> sonarVersionResponse = new ArrayList<>();
		SonarVersionResponseDTO sonarServerBranchSupportedList = prepareSonarVersionList(SONAR_SERVER, true,
				SONAR_SERVER_VERSION_BRANCH_SUPPORTED);
		SonarVersionResponseDTO sonarServerBranchNotSupportedList = prepareSonarVersionList(SONAR_SERVER, false,
				SONAR_SERVER_VERSION_BRANCH_NOT_SUPPORTED);
		SonarVersionResponseDTO sonarCloudBranchSupportedList = prepareSonarVersionList(SONAR_CLOUD, true,
				SONAR_CLOUD_VERSION_BRANCH_SUPPORTED);
		SonarVersionResponseDTO sonarCloudBranchNotSupportedList = prepareSonarVersionList(SONAR_CLOUD, false,
				SONAR_CLOUD_VERSION_BRANCH_NOT_SUPPORTED);
		sonarVersionResponse.add(sonarServerBranchSupportedList);
		sonarVersionResponse.add(sonarServerBranchNotSupportedList);
		sonarVersionResponse.add(sonarCloudBranchSupportedList);
		sonarVersionResponse.add(sonarCloudBranchNotSupportedList);
		return new ServiceResponse(true, "Version List Successfully Fetched", sonarVersionResponse);
	}

	private SonarVersionResponseDTO prepareSonarVersionList(String type, boolean branchSupport,
			List<String> versionList) {
		SonarVersionResponseDTO sonarVersionList = new SonarVersionResponseDTO();
		sonarVersionList.setType(type);
		sonarVersionList.setBranchSupport(branchSupport);
		sonarVersionList.setVersions(versionList);
		return sonarVersionList;
	}

	/**
	 * Provides the list of Sonar Project's Key.
	 *
	 * @param connectionId
	 * @param organizationKey
	 * @return the list of Sonar project's key
	 */
	public List<String> getSonarProjectKeyList(String connectionId, String organizationKey) {
		List<String> projectKeyList = null;
		Optional<Connection> optConnection = connectionRepository.findById(new ObjectId(connectionId));
		if (optConnection.isPresent()) {
			projectKeyList = getSonarProjectKeyList(optConnection.get(), organizationKey);
		}
		return projectKeyList;
	}

	/**
	 * Provides the list of Sonar Project's Branch.
	 *
	 * @param connectionId
	 *            the Sonar connection details
	 * @param version
	 *            the Sonar api version
	 * @param projectKey
	 *            the Sonar project's key
	 * @return the list of Sonar project's branch
	 */
	public ServiceResponse getSonarProjectBranchList(String connectionId, String version, String projectKey) {
		List<String> branchList = null;
		if (SONAR_SERVER_VERSION_BRANCH_SUPPORTED.stream().anyMatch(version::equalsIgnoreCase)
				|| SONAR_CLOUD_VERSION_BRANCH_SUPPORTED.stream().anyMatch(version::equalsIgnoreCase)) {
			Optional<Connection> optConnection = connectionRepository.findById(new ObjectId(connectionId));
			if (optConnection.isPresent()) {
				branchList = getSonarProjectBranchList(optConnection.get(), projectKey);
			}
			if (CollectionUtils.isEmpty(branchList)) {
				return new ServiceResponse(false, "no branches found", null);
			} else {
				return new ServiceResponse(true, "branches list fetch successfully", branchList);
			}
		} else {
			return new ServiceResponse(false, "sonar branch support not provided for this version", null);
		}

	}

	/**
	 * search project key list up to last page
	 *
	 * @param connection
	 * @param organizationKey
	 * @return
	 */
	public List<String> getSonarProjectKeyList(Connection connection, String organizationKey) {
		List<String> projectList = new ArrayList<>();

		try {
			int defaultPageSize = 300;
			Paging paging = new Paging(1, defaultPageSize, 0);

			int nextPageIndex = paging.getPageIndex();
			do {
				SearchProjectsResponse response = getSearchProjectsResponse(connection, organizationKey, paging,
						nextPageIndex);

				if (Objects.nonNull(response)) {
					projectList.addAll(
							response.getComponents().stream().map(SonarComponent::getKey).collect(Collectors.toList()));
					paging = response.getPaging();
				} else {
					paging = null;
				}
				nextPageIndex++;

			} while (hasNextPage(paging));

		} catch (Exception exception) {
			log.error("Error while fetching projects {}", exception.getMessage());
		}

		return projectList;
	}

	/**
	 * based on connection prepare rest api
	 * 
	 * @param connection
	 * @param organizationKey
	 * @param paging
	 * @param nextPageIndex
	 * @return
	 */
	private SearchProjectsResponse getSearchProjectsResponse(Connection connection, String organizationKey,
			Paging paging, int nextPageIndex) {
		SearchProjectsResponse response;
		String baseUrl = connection.getBaseUrl() == null ? null : connection.getBaseUrl().trim();
		if (connection.isCloudEnv()) {
			String sonarCloudUrl = String.format(
					new StringBuilder(baseUrl).append(RESOURCE_CLOUD_PROJECT_ENDPOINT).toString(), organizationKey,
					nextPageIndex, paging.getPageSize());
			HttpEntity<?> httpEntity = createHeaders(connection);
			response = searchProjects(sonarCloudUrl, httpEntity);
		} else {
			String sonarUrl = String.format(new StringBuilder(baseUrl).append(RESOURCE_PROJECT_ENDPOINT).toString(),
					nextPageIndex, paging.getPageSize());
			HttpEntity<?> httpEntity = createHeaders(connection);
			response = searchProjects(sonarUrl, httpEntity);
		}
		return response;
	}

	private boolean hasNextPage(Paging paging) {
		if (paging == null) {
			return false;
		}

		int pages = getTotalPages(paging);

		return paging.getPageIndex() < pages;
	}

	private int getTotalPages(Paging paging) {
		if (paging == null) {
			return 0;
		}
		int totalItems = paging.getTotal();
		int pageSize = paging.getPageSize();
		if (pageSize == 0) {
			return 0;
		}
		return (int) Math.ceil(((double) totalItems / pageSize));
	}

	/**
	 * get sonar project keys based on connection
	 *
	 * @param sonarUrl
	 * @param httpEntity
	 * @return
	 */
	private SearchProjectsResponse searchProjects(String sonarUrl, HttpEntity<?> httpEntity) {

		SearchProjectsResponse searchProjectsResponse = null;
		try {
			ResponseEntity<String> response = restTemplate.exchange(sonarUrl, HttpMethod.GET, httpEntity, String.class);
			if (response.getStatusCode() == HttpStatus.OK) {
				searchProjectsResponse = mapper.readValue(response.getBody(), SearchProjectsResponse.class);
			} else {
				String statusCode = response.getStatusCode().toString();
				log.error("Error while fetching projects from {}. with status {}", sonarUrl, statusCode);
			}
		} catch (RestClientException exception) {
			log.error("Error while fetching projects from {}:  {}", sonarUrl, exception.getMessage());
		} catch (JsonProcessingException e) {
			log.error("Error while fetching projects from {}:  {}", sonarUrl, e.getMessage());
		}
		return searchProjectsResponse;
	}

	public List<String> getSonarProjectBranchList(Connection connection, String projectKey) {
		List<String> branchNameList = new ArrayList<>();

		String baseUrl = connection.getBaseUrl() == null ? null : connection.getBaseUrl().trim();
		String url = String.format(new StringBuilder(baseUrl).append(RESOURCE_BRANCH_ENDPOINT).toString(), projectKey);

		HttpEntity<?> httpEntity = createHeaders(connection);
		try {

			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);

			if (response.getStatusCode() == HttpStatus.OK) {
				JSONArray jsonArray = SonarAPIUtils.parseData(response.getBody(), BRANCH_LIST);
				List<String> branches = SonarAPIUtils.convertListFromArray(jsonArray, BRANCH_NAME);
				if (CollectionUtils.isNotEmpty(branches)) {
					branchNameList.addAll(branches);
				}
			} else {
				String statusCode = response.getStatusCode().toString();
				log.error("Error while fetching branches from {}. with status {}", url, statusCode);
			}
		} catch (Exception exception) {
			log.error("Error while fetching branches from {}:  {}", url, exception.getMessage());
		}

		return branchNameList;
	}

	private HttpEntity<?> createHeaders(Connection connection) {
		String accessToken = connection.getAccessToken() == null ? null
				: aesEncryptionService.decrypt(connection.getAccessToken(), customApiConfig.getAesEncryptionKey());
		String username = connection.getUsername() == null ? null : connection.getUsername().trim();
		String password = connection.getPassword() == null ? null
				: aesEncryptionService.decrypt(connection.getPassword(), customApiConfig.getAesEncryptionKey());

		HttpEntity<?> httpEntity;
		if (connection.isCloudEnv()) {
			httpEntity = new HttpEntity<>(SonarAPIUtils.getHeaders(accessToken, false));
		} else if (!connection.isCloudEnv() && connection.isAccessTokenEnabled()) {
			httpEntity = new HttpEntity<>(SonarAPIUtils.getHeaders(accessToken, true));
		} else {
			httpEntity = new HttpEntity<>(SonarAPIUtils.getHeaders(username, password));
		}
		return httpEntity;
	}
}
