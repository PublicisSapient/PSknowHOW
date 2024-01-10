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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolsProvider;
import com.publicissapient.kpidashboard.apis.repotools.repository.RepoToolsProviderRepository;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
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
	private static final Pattern URL_PATTERN = Pattern.compile("^(https?://)([^/?#]+)([^?#]*)(\\?[^#]*)?(#.*)?$");
	private static final String APPICATION_JSON = "application/json";
	private static final String CLOUD_BITBUCKET = "bitbucket.org";
	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private RepoToolsProviderRepository repoToolsProviderRepository;


	@Override
	public ServiceResponse validateConnection(Connection connection, String toolName) {
		String apiUrl = "";
		String password = getPassword(connection, toolName);
		int statusCode = 0;
		switch (toolName) {
		case Constant.TOOL_BITBUCKET:
			apiUrl = createBitBucketUrl(connection);
			statusCode = testConnectionDetails(connection, apiUrl, password, toolName);
			break;
		case Constant.TOOL_AZURE:
		case Constant.TOOL_AZUREREPO:
		case Constant.TOOL_AZUREPIPELINE:
			apiUrl = createAzureApiUrl(connection.getBaseUrl(), toolName);
			statusCode = testConnectionDetails(connection, apiUrl, password, toolName);
			break;
		case Constant.TOOL_GITHUB:
			apiUrl = createGitHubTestConnectionUrl(connection);
			statusCode = testConnectionDetails(connection, apiUrl, password, toolName);
			break;
		case Constant.TOOL_SONAR:
			if (connection.isCloudEnv()) {
				apiUrl = createCloudApiUrl(connection.getBaseUrl(), toolName);
				if (checkDetailsForTool(apiUrl, password)) {
					statusCode = validateTestConn(connection, apiUrl, password, toolName);
				}
			} else {
				apiUrl = createApiUrl(connection.getBaseUrl(), toolName);
				if (!connection.isCloudEnv() && connection.isAccessTokenEnabled()) {
					if (checkDetailsForTool(apiUrl, password)) {
						statusCode = validateTestConn(connection, apiUrl, password, toolName);
					}
				} else {
					statusCode = testConnectionDetails(connection, apiUrl, password, toolName);
				}
			}
			break;
		case Constant.TOOL_ZEPHYR:
			if (connection.isCloudEnv()) {
				apiUrl = createCloudApiUrl(connection.getBaseUrl(), toolName);
				if (checkDetailsForTool(apiUrl, password)) {
					statusCode = validateTestConn(connection, apiUrl, password, toolName);
				}
			} else {
				apiUrl = createApiUrl(connection.getBaseUrl(), toolName);
				statusCode = testConnectionDetails(connection, apiUrl, password, toolName);
			}
			break;
		case Constant.TOOL_JIRA:
		case Constant.TOOL_TEAMCITY:
		case Constant.TOOL_BAMBOO:
		case Constant.TOOL_JENKINS:
			apiUrl = createApiUrl(connection.getBaseUrl(), toolName);
			statusCode = testConnectionDetails(connection, apiUrl, password, toolName);
			break;
		case Constant.TOOL_GITLAB:
			apiUrl = createApiUrl(connection.getBaseUrl(), toolName);
			if (checkDetailsForTool(apiUrl, password)) {
				statusCode = validateTestConn(connection, apiUrl, password, toolName);
			}
			break;
			case Constant.REPO_TOOLS:
				apiUrl = getApiForRepoTool(connection);
				statusCode = validateTestConn(connection, apiUrl, password, toolName);
			break;
		default:
			return new ServiceResponse(false, "Invalid Toolname", HttpStatus.NOT_FOUND);
		}

		if (statusCode == HttpStatus.OK.value()) {
			return new ServiceResponse(true, VALID_MSG, statusCode);
		}

		if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
			return new ServiceResponse(false, INVALID_MSG, statusCode);
		}

		return new ServiceResponse(false, "Password/API token missing", HttpStatus.NOT_FOUND);
	}

	private String getApiForRepoTool(Connection connection){
		RepoToolsProvider repoToolsProvider = repoToolsProviderRepository
				.findByToolName(connection.getRepoToolProvider());
		String apiUrl = "";
			Matcher matcher = URL_PATTERN.matcher(connection.getHttpUrl());
			if (connection.getRepoToolProvider().equalsIgnoreCase(Constant.TOOL_GITHUB))
				apiUrl = repoToolsProvider.getTestApiUrl() + connection.getUsername();
			else if (connection.getRepoToolProvider().equalsIgnoreCase(Constant.TOOL_BITBUCKET)) {
				if (connection.getHttpUrl().contains(CLOUD_BITBUCKET)) {
					apiUrl = repoToolsProvider.getTestApiUrl();
				} else if (matcher.find()) {
					apiUrl = matcher.group(1).concat(matcher.group(2)).concat(repoToolsProvider.getTestServerApiUrl());
				}
			} else {
				if (matcher.find()) {
					apiUrl = createApiUrl(matcher.group(1).concat(matcher.group(2)), Constant.TOOL_GITLAB);
				}
			}
		return apiUrl;
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
	 *
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

	private boolean checkDetails(String apiUrl, String password, Connection connection) {
		boolean b = false;
		if (apiUrl != null && isUrlValid(apiUrl) && (StringUtils.isNotEmpty(password) || connection.isJaasKrbAuth())
				&& StringUtils.isNotEmpty(connection.getUsername())) {
			b = true;
		}
		return b;
	}

	private int testConnectionDetails(Connection connection, String apiUrl, String password, String toolName) {
		int status = 0;
		if (checkDetails(apiUrl, password, connection)) {
			status = validateTestConn(connection, apiUrl, password, toolName);
		}
		return status;
	}

	private int validateTestConn(Connection connection, String apiUrl, String password, String toolName) {
		boolean isValid;
		int statusCode;
		if (toolName.equals(Constant.TOOL_GITHUB)) {
			isValid = testConnectionForGitHub(apiUrl, connection.getUsername(), password);
			statusCode = isValid ? HttpStatus.OK.value() : HttpStatus.UNAUTHORIZED.value();
		} else if ((toolName.equals(Constant.TOOL_ZEPHYR) && connection.isCloudEnv())
				|| toolName.equals(Constant.TOOL_GITLAB)) {
			isValid = testConnectionForTools(apiUrl, password);
			statusCode = isValid ? HttpStatus.OK.value() : HttpStatus.UNAUTHORIZED.value();
		} else if (toolName.equals(Constant.TOOL_SONAR)) {
			if (connection.isCloudEnv()) {
				isValid = testConnectionForTools(apiUrl, password);
			} else if (!connection.isCloudEnv() && connection.isAccessTokenEnabled()) {
				isValid = testConnection(connection, toolName, apiUrl, password, true);
			} else {
				isValid = testConnection(connection, toolName, apiUrl, password, false);
			}
			statusCode = isValid ? HttpStatus.OK.value() : HttpStatus.UNAUTHORIZED.value();
		} else {
			if (connection.isBearerToken()) {
				isValid = testConnectionWithBearerToken(apiUrl, password);
				statusCode = isValid ? HttpStatus.OK.value() : HttpStatus.UNAUTHORIZED.value();
		} else if (toolName.equalsIgnoreCase(CommonConstant.REPO_TOOLS)) {
				isValid = testConnectionForRepoTools(apiUrl, password, connection);
				statusCode = isValid ? HttpStatus.OK.value() : HttpStatus.UNAUTHORIZED.value();
			}
		else {
			isValid = testConnection(connection, toolName, apiUrl, password, false);
			statusCode = isValid ? HttpStatus.OK.value() : HttpStatus.UNAUTHORIZED.value();
		}

		}
		return statusCode;
	}

	private boolean testConnectionForRepoTools(String apiUrl, String password, Connection connection) {

		if (connection.getRepoToolProvider().equalsIgnoreCase(Constant.TOOL_GITHUB))
			return testConnectionForGitHub(apiUrl, connection.getUsername(), password);
		else if (connection.getRepoToolProvider().equalsIgnoreCase(Constant.TOOL_BITBUCKET))
			return testConnection(connection, Constant.TOOL_BITBUCKET, apiUrl, password, false);
		else
			return testConnectionForTools(apiUrl, password);
	}

	private boolean testConnectionForGitHub(String apiUrl, String username, String password) {

		HttpHeaders httpHeaders = createHeadersWithAuthentication(username, password, false);
		HttpEntity<?> requestEntity = new HttpEntity<>(httpHeaders);
		ResponseEntity<String> result = restTemplate.exchange(URI.create(apiUrl), HttpMethod.GET, requestEntity,
				String.class);
		if (result.getStatusCode().is2xxSuccessful()) {
			HttpHeaders headers = result.getHeaders();
			List<String> rateLimits = headers.get("X-RateLimit-Limit");
			return CollectionUtils.isNotEmpty(rateLimits)
					&& Integer.valueOf(rateLimits.get(0)) > GITHUB_RATE_LIMIT_PER_HOUR;
		} else {
			return false;
		}

	}

	private boolean testConnectionForTools(String apiUrl, String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + accessToken);
		HttpEntity<?> requestEntity = new HttpEntity<>(headers);
		try {
			ResponseEntity<String> result = restTemplate.exchange(URI.create(apiUrl), HttpMethod.GET, requestEntity,
					String.class);
			return result.getStatusCode().is2xxSuccessful();
		} catch (HttpClientErrorException e) {
			log.error(INVALID_MSG);
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
		case Constant.TOOL_AZURE:
			apiEndPoint = customApiConfig.getAzureBoardApi();
			break;
		case Constant.TOOL_AZUREREPO:
			apiEndPoint = customApiConfig.getAzureRepoApi();
			break;
		case Constant.TOOL_AZUREPIPELINE:
			apiEndPoint = customApiConfig.getAzurePipelineApi();
			break;

		default:
			log.info("Tool name is invalid or empty");
			break;
		}

		if (StringUtils.isNotEmpty(apiEndPoint)) {
			resultUrl = baseUrlWithEncodedSpace + URL_SAPERATOR + apiEndPoint;
		}

		return resultUrl;
	}

	private boolean checkDetailsForTool(String apiUrl, String password) {
		boolean b = false;
		if (apiUrl != null && isUrlValid(apiUrl) && StringUtils.isNotEmpty(password)) {
			b = true;
		}
		return b;
	}

	/**
	 * Create API URL using base URL and API path for bitbucket
	 *
	 * @param connection
	 *            connection
	 *
	 * @param connection
	 * @return apiURL
	 */
	private String createBitBucketUrl(Connection connection) {
		URI uri = URI.create(connection.getBaseUrl().replace(SPACE, ENCODED_SPACE));
		if (connection.isCloudEnv()) {
			return uri.getScheme() + "://" + uri.getHost() + StringUtils.removeEnd(connection.getApiEndPoint(), "/")
					+ "/workspaces/";
		} else {
			return uri.getScheme() + "://" + uri.getHost() + StringUtils.removeEnd(connection.getApiEndPoint(), "/")
					+ "/projects/";
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
		headers.add(HttpHeaders.ACCEPT, "*/*");
		headers.add(HttpHeaders.CONTENT_TYPE, APPICATION_JSON);
		headers.set("Cookie", "");
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
		RestTemplate rest = new RestTemplate();
		HttpHeaders httpHeaders;
		ResponseEntity<?> responseEntity;
		httpHeaders = createHeadersWithAuthentication(username, password, isSonarWithAccessToken);
		HttpEntity<?> requestEntity = new HttpEntity<>(httpHeaders);
		try {
			responseEntity = rest.exchange(URI.create(apiUrl), HttpMethod.GET, requestEntity, String.class);
		} catch (HttpClientErrorException e) {
			log.error("Invalid login credentials");
			return e.getStatusCode();
		}

		Object responseBody = responseEntity.getBody();
		if (toolName.equalsIgnoreCase(Constant.TOOL_SONAR)
				&& ((responseBody != null && responseBody.toString().contains("false"))
						|| responseBody.toString().contains("</html>"))) {
			return HttpStatus.UNAUTHORIZED;
		}
		if (toolName.equalsIgnoreCase(Constant.TOOL_BITBUCKET)
				&& responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			return HttpStatus.OK;
		}

		return responseEntity.getStatusCode();
	}

	private HttpStatusCode getApiResponseWithBearer(String pat, String apiUrl) {
		RestTemplate rest = new RestTemplate();
		HttpHeaders httpHeaders;
		ResponseEntity<?> responseEntity;
		httpHeaders = createHeadersWithBearer(pat);
		HttpEntity<?> requestEntity = new HttpEntity<>(httpHeaders);
		try {
			responseEntity = rest.exchange(URI.create(apiUrl), HttpMethod.GET, requestEntity, String.class);
		} catch (HttpClientErrorException e) {
			log.error("Invalid login credentials");
			return e.getStatusCode();
		}
		HttpStatusCode responseCode = responseEntity.getStatusCode();

		if (responseCode.is2xxSuccessful() && null != responseEntity.getBody()
				&& responseEntity.getBody().toString().equalsIgnoreCase(WRONG_JIRA_BEARER)) {
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
		if (Constant.TOOL_ZEPHYR.equalsIgnoreCase(toolName) && StringUtils.isNotEmpty(baseUrl)
				&& StringUtils.isNotEmpty(apiPath)) {
			return baseUrl.endsWith("/") ? baseUrl.concat(apiPath) : baseUrl.concat("/").concat(apiPath);
		}
		if (Constant.TOOL_SONAR.equalsIgnoreCase(toolName) && StringUtils.isNotEmpty(baseUrl)
				&& StringUtils.isNotEmpty(endpoint)) {
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
		case Constant.TOOL_JIRA:
			return customApiConfig.getJiraTestConnection();
		case Constant.TOOL_SONAR:
			return customApiConfig.getSonarTestConnection();
		case Constant.TOOL_TEAMCITY:
			return customApiConfig.getTeamcityTestConnection();
		case Constant.TOOL_BAMBOO:
			return customApiConfig.getBambooTestConnection();
		case Constant.TOOL_JENKINS:
			return customApiConfig.getJenkinsTestConnection();
		case Constant.TOOL_GITLAB:
			return customApiConfig.getGitlabTestConnection();
		case Constant.TOOL_BITBUCKET:
			return customApiConfig.getBitbucketTestConnection();
		case Constant.TOOL_ZEPHYR:
			return customApiConfig.getZephyrTestConnection();
		default:
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
		if (Constant.TOOL_GITHUB.equalsIgnoreCase(toolName)) {
			return connection.getAccessToken();
		}
		if (Constant.TOOL_ZEPHYR.equalsIgnoreCase(toolName) && connection.isCloudEnv()) {
			return connection.getAccessToken();
		}
		if (Constant.TOOL_GITLAB.equalsIgnoreCase(toolName)) {
			return connection.getAccessToken();
		}
		if (Constant.TOOL_SONAR.equalsIgnoreCase(toolName) && connection.isCloudEnv()) {
			return connection.getAccessToken();
		}
		if (Constant.TOOL_SONAR.equalsIgnoreCase(toolName) && StringUtils.isNotEmpty(connection.getAccessToken())) {
			return connection.getAccessToken();
		}
		if (Constant.TOOL_JIRA.equalsIgnoreCase(toolName) && connection.isBearerToken()) {
			return connection.getPatOAuthToken();
		}
		if (Constant.TOOL_ZEPHYR.equalsIgnoreCase(toolName) && connection.isBearerToken()) {
			return connection.getPatOAuthToken();
		}
		if (Constant.REPO_TOOLS.equalsIgnoreCase(toolName) &&
				StringUtils.isNotEmpty(connection.getAccessToken())) {
			return connection.getAccessToken();
		}
		return connection.getPassword() != null ? connection.getPassword() : connection.getApiKey();
	}

}
