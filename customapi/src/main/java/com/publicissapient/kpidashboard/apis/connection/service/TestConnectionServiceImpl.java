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

package com.publicissapient.kpidashboard.apis.connection.service;

import java.io.IOException;
import java.net.URI;
import java.util.Base64;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.repotools.repository.RepoToolsProviderRepository;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.connection.Connection;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TestConnectionServiceImpl implements TestConnectionService {

	private static final String SPACE = " ";
	private static final String ENCODED_SPACE = "%20";
	private static final String URL_SAPERATOR = "/";
	private static final int GITHUB_RATE_LIMIT_PER_HOUR = 60;
	private static final String VALID_MSG = "Valid Credentials ";
	private static final String INVALID_MSG = "Invalid Credentials ";
	private static final String WRONG_JIRA_BEARER = "{\"expand\":\"projects\",\"projects\":[]}";
	private static final String APPICATION_JSON = "application/json";
	private static final String CLOUD_BITBUCKET_IDENTIFIER = "bitbucket.org";
	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private RepoToolsProviderRepository repoToolsProviderRepository;

	@Override
	public ServiceResponse validateConnection(Connection connection, String toolName) {
		String apiUrl = getApiUrl(connection, toolName);
		if (apiUrl == null) {
			return new ServiceResponse(false, "Invalid Tool name", HttpStatus.NOT_FOUND);
		}

		String password = getPassword(connection, toolName);
		int statusCode = testConnectionDetails(connection, apiUrl, password, toolName);

		if (statusCode == HttpStatus.OK.value()) {
			return new ServiceResponse(true, VALID_MSG, statusCode);
		}

		if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
			return new ServiceResponse(false, INVALID_MSG, statusCode);
		}

		return new ServiceResponse(false, "Password/API token missing", HttpStatus.NOT_FOUND);
	}

	private String getApiUrl(Connection connection, String toolName) {
		return switch (toolName) {
			case Constant.TOOL_BITBUCKET -> createBitBucketUrl(connection);
			case Constant.TOOL_AZURE, Constant.TOOL_AZUREREPO, Constant.TOOL_AZUREPIPELINE ->
				createAzureApiUrl(connection.getBaseUrl(), toolName);
			case Constant.TOOL_GITHUB -> createGitHubTestConnectionUrl(connection);
			case Constant.TOOL_SONAR,
					Constant.TOOL_ZEPHYR ->
				connection.isCloudEnv()
						? createCloudApiUrl(connection.getBaseUrl(), toolName)
						: createApiUrl(connection.getBaseUrl(), toolName);
			case Constant.TOOL_JIRA, Constant.TOOL_TEAMCITY, Constant.TOOL_BAMBOO, Constant.TOOL_JENKINS,
                 Constant.TOOL_ARGOCD, Constant.TOOL_GITLAB, Constant.TOOL_RALLY ->
				createApiUrl(connection.getBaseUrl(), toolName);
			case Constant.REPO_TOOLS -> getApiForRepoTool(connection);
            default -> null;
		};
	}

	private String getApiForRepoTool(Connection connection) {
		String apiUrl = "";
		if (connection.getRepoToolProvider().equalsIgnoreCase(Constant.TOOL_GITHUB)) {
			apiUrl = createGitHubTestConnectionUrl(connection);
		} else if (connection.getRepoToolProvider().equalsIgnoreCase(Constant.TOOL_GITLAB)) {
			apiUrl = createApiUrl(connection.getBaseUrl(), Constant.TOOL_GITLAB);
		} else if (connection.getRepoToolProvider().equalsIgnoreCase(Constant.TOOL_BITBUCKET)) {
			if (connection.getBaseUrl().contains(CLOUD_BITBUCKET_IDENTIFIER))
				connection.setCloudEnv(true);
			apiUrl = createBitBucketUrl(connection);
		} else if (connection.getRepoToolProvider().equalsIgnoreCase(Constant.TOOL_AZUREREPO)) {
			apiUrl = createAzureApiUrl(connection.getBaseUrl(), Constant.TOOL_AZUREREPO);
		} else if (connection.getType().equalsIgnoreCase(Constant.TOOL_RALLY)) {
			apiUrl = createApiUrl(connection.getBaseUrl(), Constant.TOOL_RALLY);
		}
		return apiUrl != null ? apiUrl.trim() : "";
	}

	private boolean testConnection(Connection connection, String toolName, String apiUrl, String password,
			boolean isSonarWithAccessToken) {
		boolean isValidConnection = false;
		if (connection.isJaasKrbAuth()) {
			try {
				KerberosClient client = new KerberosClient(connection.getJaasConfigFilePath(),
						connection.getKrb5ConfigFilePath(), connection.getJaasUser(), connection.getSamlEndPoint(),
						connection.getBaseUrl());
				client.login(customApiConfig.getSamlTokenStartString(), customApiConfig.getSamlTokenEndString(),
						customApiConfig.getSamlUrlStartString(), customApiConfig.getSamlUrlEndString());
				HttpResponse response = getApiResponseWithKerbAuth(client, apiUrl);
				if (null != response && response.getStatusLine().getStatusCode() == 200) {
					isValidConnection = true;
				}
			} catch (RestClientException ex) {
				log.error("exception occured while trying to hit api.");
			}
		} else {
			HttpStatusCode status = getApiResponseWithBasicAuth(connection.getUsername(), password, apiUrl, toolName,
					isSonarWithAccessToken);
			isValidConnection = status.is2xxSuccessful();
		}
		return isValidConnection;
	}

	/**
	 * @param apiUrl
	 * @param pat
	 * @return
	 */
	private boolean testConnectionWithBearerToken(String apiUrl, String pat) {
		boolean isValidConnection;
		HttpStatusCode status = null;
		status = getApiResponseWithBearer(pat, apiUrl);
		isValidConnection = status.is2xxSuccessful();
		return isValidConnection;
	}

	private boolean checkDetails(String apiUrl, String password, Connection connection, String toolName) {
		if (toolName.equals(Constant.TOOL_SONAR) || toolName.equals(Constant.TOOL_ZEPHYR) ||
				toolName.equals(Constant.TOOL_GITLAB)) {
			return checkDetailsForTool(apiUrl, password);
		} else if(toolName.equals(Constant.TOOL_RALLY)){
			return true;
		}
		else {
			return apiUrl != null && isUrlValid(apiUrl) && (StringUtils.isNotEmpty(password) || connection.isJaasKrbAuth()) &&
					StringUtils.isNotEmpty(connection.getUsername());
		}
	}

	private int testConnectionDetails(Connection connection, String apiUrl, String password, String toolName) {
		int status = 0;
		if (checkDetails(apiUrl, password, connection, toolName)) {
			status = validateTestConn(connection, apiUrl, password, toolName);
		}
		return status;
	}

	private int validateTestConn(Connection connection, String apiUrl, String password, String toolName) {
		boolean isValid;

		return switch (toolName) {
			case Constant.TOOL_GITHUB -> {
				isValid = testConnectionForGitHub(apiUrl, connection.getUsername(), password);
				yield getStatusCode(isValid);
			}
			case Constant.TOOL_ZEPHYR, Constant.TOOL_GITLAB, Constant.TOOL_ARGOCD -> {
				isValid = testConnectionForTools(apiUrl, password);
				yield getStatusCode(isValid);
			}
			case Constant.TOOL_SONAR -> {
				isValid = validateSonarConnection(connection, apiUrl, password);
				yield getStatusCode(isValid);
			}
			case Constant.TOOL_RALLY -> {
				isValid = validateRallyConnection(connection, apiUrl, password);
				yield getStatusCode(isValid);
			}
			default -> {
				isValid = connection.isBearerToken()
						? testConnectionWithBearerToken(apiUrl, password)
						: testConnection(connection, toolName, apiUrl, password, false);
				yield getStatusCode(isValid);
			}
		};
	}

	private int getStatusCode(boolean isValid) {
		return isValid ? HttpStatus.OK.value() : HttpStatus.UNAUTHORIZED.value();
	}

	private boolean validateSonarConnection(Connection connection, String apiUrl, String password) {
		if (connection.isCloudEnv()) {
			return testConnectionForTools(apiUrl, password);
		} else if (connection.isAccessTokenEnabled()) {
			return testConnection(connection, Constant.TOOL_SONAR, apiUrl, password, true);
		} else {
			return testConnection(connection, Constant.TOOL_SONAR, apiUrl, password, false);
		}
	}

	private boolean validateRallyConnection(Connection connection, String apiUrl, String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("ZSESSIONID", connection.getAccessToken());
		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);
		return response.getStatusCode().is2xxSuccessful();
	}

	private boolean testConnectionForGitHub(String apiUrl, String username, String password) {

		HttpHeaders httpHeaders = createHeadersWithAuthentication(username, password, false);
		HttpEntity<?> requestEntity = new HttpEntity<>(httpHeaders);
		ResponseEntity<String> result = restTemplate.exchange(URI.create(apiUrl), HttpMethod.GET, requestEntity,
				String.class);
		if (result.getStatusCode().is2xxSuccessful()) {
			HttpHeaders headers = result.getHeaders();
			List<String> rateLimits = headers.get("X-RateLimit-Limit");
			return CollectionUtils.isNotEmpty(rateLimits) && Integer.valueOf(rateLimits.get(0)) > GITHUB_RATE_LIMIT_PER_HOUR;
		} else {
			return false;
		}
	}

	private boolean testConnectionForTools(String apiUrl, String accessToken) {
		HttpHeaders headers = createHeadersWithBearer(accessToken);
		HttpEntity<?> requestEntity = new HttpEntity<>(headers);
		try {
			ResponseEntity<String> result = restTemplate.exchange(URI.create(apiUrl), HttpMethod.GET, requestEntity,
					String.class);
			return result.getStatusCode().is2xxSuccessful();
		} catch (HttpClientErrorException e) {
			return e.getStatusCode().is5xxServerError();
		}
	}

	private String createGitHubTestConnectionUrl(Connection connection) {

		return connection.getBaseUrl() + URL_SAPERATOR + "users" + URL_SAPERATOR + connection.getUsername();
	}

	private String createAzureApiUrl(String baseUrl, String toolName) {
		String resultUrl = "";

		String baseUrlWithoutTrailingSlash = StringUtils.removeEnd(baseUrl, URL_SAPERATOR);
		String baseUrlWithEncodedSpace = StringUtils.replace(baseUrlWithoutTrailingSlash, SPACE, ENCODED_SPACE);
		String apiEndPoint = "";
		switch (toolName) {
			case Constant.TOOL_AZURE :
				apiEndPoint = customApiConfig.getAzureBoardApi();
				break;
			case Constant.TOOL_AZUREREPO :
				apiEndPoint = customApiConfig.getAzureRepoApi();
				break;
			case Constant.TOOL_AZUREPIPELINE :
				apiEndPoint = customApiConfig.getAzurePipelineApi();
				break;

			default :
				log.info("Tool name is invalid or empty");
				break;
		}

		if (StringUtils.isNotEmpty(apiEndPoint)) {
			resultUrl = baseUrlWithEncodedSpace + URL_SAPERATOR + apiEndPoint;
		}

		return resultUrl;
	}

	/**
	 * Create API URL using base URL and API path for bitbucket
	 *
	 * @param connection
	 *          connection
	 * @param connection
	 * @return apiURL
	 */
	private String createBitBucketUrl(Connection connection) {
		URI uri = URI.create(connection.getBaseUrl().replace(SPACE, ENCODED_SPACE));
		if (connection.isCloudEnv()) {
			return uri.getScheme() + "://" + uri.getHost() + StringUtils.removeEnd(connection.getApiEndPoint(), "/") +
					"/workspaces/";
		} else {
			return uri.getScheme() + "://" + uri.getHost() + StringUtils.removeEnd(connection.getApiEndPoint(), "/") +
					"/projects/";
		}
	}

	/**
	 * Create HTTP header with basic Authentication
	 *
	 * @param username
	 * @param password
	 * @return
	 */
	private HttpHeaders createHeadersWithAuthentication(String username, String password,
			boolean isSonarWithAccessToken) {
		String plainCreds = null;

		if (isSonarWithAccessToken) {
			plainCreds = password + ":";
		} else {
			plainCreds = username + ":" + password;
		}
		byte[] base64CredsBytes = Base64.getEncoder().encode(plainCreds.getBytes());
		String base64Creds = new String(base64CredsBytes);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + base64Creds);
		return headers;
	}

	private HttpHeaders createHeadersWithBearer(String pat) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + pat);
		return headers;
	}

	/**
	 * Make API call to validate Credentials
	 *
	 * @param username
	 * @param password
	 * @param apiUrl
	 * @return API response
	 */
	private HttpStatusCode getApiResponseWithBasicAuth(String username, String password, String apiUrl, String toolName,
			boolean isSonarWithAccessToken) {
		HttpHeaders httpHeaders;
		ResponseEntity<?> responseEntity;
		httpHeaders = createHeadersWithAuthentication(username, password, isSonarWithAccessToken);
		HttpEntity<?> requestEntity = new HttpEntity<>(httpHeaders);
		try {
			responseEntity = restTemplate.exchange(URI.create(apiUrl), HttpMethod.GET, requestEntity, String.class);
		} catch (HttpClientErrorException e) {
			log.error("Invalid login credentials");
			return e.getStatusCode();
		}

		Object responseBody = responseEntity.getBody();
		if (toolName.equalsIgnoreCase(Constant.TOOL_SONAR) && (responseBody != null &&
				(responseBody.toString().contains("false") || responseBody.toString().contains("</html>")))) {
			return HttpStatus.UNAUTHORIZED;
		}
		if (toolName.equalsIgnoreCase(Constant.TOOL_BITBUCKET) && responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			return HttpStatus.OK;
		}

		return responseEntity.getStatusCode();
	}

	private HttpStatusCode getApiResponseWithBearer(String pat, String apiUrl) {
		HttpHeaders httpHeaders;
		ResponseEntity<?> responseEntity;
		httpHeaders = createHeadersWithBearer(pat);
		HttpEntity<?> requestEntity = new HttpEntity<>(httpHeaders);
		try {
			responseEntity = restTemplate.exchange(URI.create(apiUrl), HttpMethod.GET, requestEntity, String.class);
		} catch (HttpClientErrorException e) {
			log.error("Invalid login credentials");
			return e.getStatusCode();
		}
		HttpStatusCode responseCode = responseEntity.getStatusCode();

		Object responseBody = responseEntity.getBody();
		if (responseCode.is2xxSuccessful() && responseBody != null &&
				WRONG_JIRA_BEARER.equalsIgnoreCase(responseBody.toString())) {
			responseCode = HttpStatus.UNAUTHORIZED;
		}

		return responseCode;
	}

	/**
	 * Create API URL using base URL and API path
	 *
	 * @param client
	 * @param apiUrl
	 * @return apiURL
	 */
	private HttpResponse getApiResponseWithKerbAuth(KerberosClient client, String apiUrl) {
		HttpUriRequest request = RequestBuilder.get().setUri(apiUrl)
				.setHeader(org.apache.http.HttpHeaders.ACCEPT, APPICATION_JSON)
				.setHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, APPICATION_JSON).build();
		try {
			return client.getHttpResponse(request);
		} catch (IOException e) {
			log.error("error occured while executing kerberos client request." + e.getMessage());
			return null;
		}
	}

	private String createApiUrl(String baseUrl, String toolName) {
		String apiPath = getApiPath(toolName);
		if (StringUtils.isNotEmpty(baseUrl) && StringUtils.isNotEmpty(apiPath)) {
			return baseUrl.endsWith("/") ? baseUrl.concat(apiPath) : baseUrl.concat("/").concat(apiPath);
		}
		return null;
	}

	private String createCloudApiUrl(String baseUrl, String toolName) {
		String apiPath = "healthcheck";
		String endpoint = "api/favorites/search";
		if (Constant.TOOL_ZEPHYR.equalsIgnoreCase(toolName) && StringUtils.isNotEmpty(baseUrl) &&
				StringUtils.isNotEmpty(apiPath)) {
			return baseUrl.endsWith("/") ? baseUrl.concat(apiPath) : baseUrl.concat("/").concat(apiPath);
		}
		if (Constant.TOOL_SONAR.equalsIgnoreCase(toolName) && StringUtils.isNotEmpty(baseUrl) &&
				StringUtils.isNotEmpty(endpoint)) {
			return baseUrl.endsWith("/") ? baseUrl.concat(endpoint) : baseUrl.concat("/").concat(endpoint);
		}
		return null;
	}

	/**
	 * this method returns API path from configuration
	 *
	 * @param toolName
	 * @return apiPath
	 */
	private String getApiPath(String toolName) {
		switch (toolName) {
			case Constant.TOOL_JIRA :
				return customApiConfig.getJiraTestConnection();
			case Constant.TOOL_SONAR :
				return customApiConfig.getSonarTestConnection();
			case Constant.TOOL_TEAMCITY :
				return customApiConfig.getTeamcityTestConnection();
			case Constant.TOOL_BAMBOO :
				return customApiConfig.getBambooTestConnection();
			case Constant.TOOL_JENKINS :
				return customApiConfig.getJenkinsTestConnection();
			case Constant.TOOL_GITLAB :
				return customApiConfig.getGitlabTestConnection();
			case Constant.TOOL_BITBUCKET :
				return customApiConfig.getBitbucketTestConnection();
			case Constant.TOOL_ZEPHYR :
				return customApiConfig.getZephyrTestConnection();
			case Constant.TOOL_ARGOCD :
				return customApiConfig.getArgoCDTestConnection();
			case Constant.TOOL_RALLY:
				return customApiConfig.getRallyTestConnection();
			default :
				return null;
		}
	}

	/**
	 * checks if input URL is valid or not
	 *
	 * @param url
	 * @return
	 */
	private boolean isUrlValid(String url) {
		UrlValidator urlValidator = new UrlValidator();
		return urlValidator.isValid(url);
	}

	private String getPassword(Connection connection, String toolName) {
		if (connection == null || toolName == null) {
			return null;
		}
		return switch (toolName) {
			case Constant.TOOL_GITHUB, Constant.TOOL_GITLAB, Constant.TOOL_ARGOCD, Constant.REPO_TOOLS ->
				connection.getAccessToken();
			case Constant.TOOL_ZEPHYR -> {
				if (connection.isCloudEnv() || connection.isBearerToken()) {
					yield connection.getAccessToken();
				}
				yield connection.getApiKey();
			}
			case Constant.TOOL_SONAR -> {
				if (connection.isCloudEnv() || StringUtils.isNotEmpty(connection.getAccessToken())) {
					yield connection.getAccessToken();
				}
				yield connection.getPassword();
			}
			case Constant.TOOL_JIRA -> {
				if (connection.isBearerToken()) {
					yield connection.getPatOAuthToken();
				}
				yield connection.getPassword();
			}
			default -> connection.getPassword() != null ? connection.getPassword() : connection.getApiKey();
		};
	}

	@Override
	public ServiceResponse getZephyrCloudUrlDetails() {
		boolean success = false;
		String zephyrCloudUrl = customApiConfig.getZephyrCloudBaseUrl();
		if (zephyrCloudUrl != null) {
			success = true;
		}

		return new ServiceResponse(success, "Fetched Zephyr Cloud Base Url successfully", zephyrCloudUrl);
	}

	private boolean checkDetailsForTool(String apiUrl, String password) {
		boolean b = false;
		if (apiUrl != null && isUrlValid(apiUrl) && StringUtils.isNotEmpty(password)) {
			b = true;
		}
		return b;
	}
}
