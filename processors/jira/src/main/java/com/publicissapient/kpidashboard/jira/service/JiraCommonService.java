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
package com.publicissapient.kpidashboard.jira.service;

import static com.publicissapient.kpidashboard.jira.constant.JiraConstants.ERROR_MSG_401;
import static com.publicissapient.kpidashboard.jira.constant.JiraConstants.ERROR_MSG_NO_RESULT_WAS_AVAILABLE;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.model.application.ProjectVersion;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import com.publicissapient.kpidashboard.jira.client.CustomAsynchronousIssueRestClient;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.helper.JiraHelper;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

import io.atlassian.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JiraCommonService {

	public static final String PROCESSING_ISSUES_PRINT_LOG = "Processing issues %d - %d out of %d";
	private static final String MSG_JIRA_CLIENT_SETUP_FAILED = "Jira client setup failed. No results obtained. Check your jira setup.";

	@Autowired
	private JiraProcessorConfig jiraProcessorConfig;

	private ProcessorJiraRestClient client;

	@Autowired
	private ToolCredentialProvider toolCredentialProvider;

	@Autowired
	private AesEncryptionService aesEncryptionService;

	/**
	 * @param projectConfig
	 *            projectConfig
	 * @param url
	 *            url
	 * @param krb5Client
	 *            krb5Client
	 * @return String
	 * @throws IOException
	 *             IOException
	 */
	public String getDataFromClient(ProjectConfFieldMapping projectConfig, URL url, KerberosClient krb5Client)
			throws IOException {
		Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();
		boolean spenagoClient = connectionOptional.map(Connection::isJaasKrbAuth).orElse(false);
		if (spenagoClient) {
			HttpUriRequest request = RequestBuilder.get().setUri(url.toString())
					.setHeader(org.apache.http.HttpHeaders.ACCEPT, "application/json")
					.setHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, "application/json").build();
			String responce = krb5Client.getResponse(request);
			return responce;
		} else {
			return getDataFromServer(url, connectionOptional);
		}
	}

	/**
	 * @param url
	 *            url
	 * @param connectionOptional
	 *            connectionOptional
	 * @return String
	 * @throws IOException
	 *             IOException
	 */
	public String getDataFromServer(URL url, Optional<Connection> connectionOptional) throws IOException {
		HttpURLConnection request = (HttpURLConnection) url.openConnection();

		String username = null;
		String password = null;

		if (connectionOptional.isPresent()) {
			Connection conn = connectionOptional.get();
			if (conn.isVault()) {
				ToolCredential toolCredential = toolCredentialProvider.findCredential(conn.getUsername());
				if (toolCredential != null) {
					username = toolCredential.getUsername();
					password = toolCredential.getPassword();
				}

			} else {
				username = connectionOptional.map(Connection::getUsername).orElse(null);
				password = decryptJiraPassword(connectionOptional.map(Connection::getPassword).orElse(null));
			}
		}
		if (connectionOptional.isPresent() && connectionOptional.get().isBearerToken()) {
			String patOAuthToken = decryptJiraPassword(connectionOptional.get().getPatOAuthToken());
			request.setRequestProperty("Authorization", "Bearer " + patOAuthToken); // NOSONAR
		} else {
			request.setRequestProperty("Authorization", "Basic " + encodeCredentialsToBase64(username, password)); // NOSONAR
		}
		request.connect();
		StringBuilder sb = new StringBuilder();
		try (InputStream in = (InputStream) request.getContent();
			 BufferedReader inReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			int cp;
			while ((cp = inReader.read()) != -1) {
				sb.append((char) cp);
			}
			request.disconnect();
		} catch (IOException ie) {
			log.error("Read exception when connecting to server {}", ie);
			request.disconnect();
		}
		return sb.toString();
	}

	/**
	 * @param encryptedPassword
	 *            encryptedPassword
	 * @return String
	 */
	public String decryptJiraPassword(String encryptedPassword) {
		return aesEncryptionService.decrypt(encryptedPassword, jiraProcessorConfig.getAesEncryptionKey());
	}

	/**
	 * @param username
	 *            username
	 * @param password
	 *            password
	 * @return String
	 */
	public String encodeCredentialsToBase64(String username, String password) {
		String cred = username + ":" + password;
		return Base64.getEncoder().encodeToString(cred.getBytes());
	}

	/**
	 * @param projectConfig
	 *            projectConfig
	 * @param clientIncoming
	 *            clientIncoming
	 * @param pageNumber
	 *            pageNumber
	 * @param deltaDate
	 *            deltaDate
	 * @return List of Issue
	 * @throws InterruptedException
	 *             InterruptedException
	 */
	public List<Issue> fetchIssuesBasedOnJql(ProjectConfFieldMapping projectConfig,
			ProcessorJiraRestClient clientIncoming, int pageNumber, String deltaDate) throws InterruptedException {

		client = clientIncoming;
		List<Issue> issues = new ArrayList<>();
		if (client == null) {
			log.error(MSG_JIRA_CLIENT_SETUP_FAILED);
		} else {

			String queryDate = DateUtil
					.dateTimeFormatter(DateUtil.stringToLocalDateTime(deltaDate, JiraConstants.QUERYDATEFORMAT)
							.minusDays(jiraProcessorConfig.getDaysToReduce()), JiraConstants.QUERYDATEFORMAT);

			SearchResult searchResult = getJqlIssues(projectConfig, queryDate, pageNumber);
			issues = JiraHelper.getIssuesFromResult(searchResult);

		}
		return issues;
	}

	/**
	 * @param projectConfig
	 *            projectConfig
	 * @param deltaDate
	 *            deltaDate
	 * @param pageStart
	 *            pageStart
	 * @return SearchResult
	 * @throws InterruptedException
	 *             InterruptedException
	 */
	private SearchResult getJqlIssues(ProjectConfFieldMapping projectConfig, String deltaDate, int pageStart)
			throws InterruptedException {
		SearchResult searchResult = null;
		String[] jiraIssueTypeNames = projectConfig.getFieldMapping().getJiraIssueTypeNames();
		if (client == null) {
			log.warn(MSG_JIRA_CLIENT_SETUP_FAILED);
		} else if (StringUtils.isEmpty(projectConfig.getProjectToolConfig().getProjectKey())
				|| StringUtils.isEmpty(projectConfig.getProjectToolConfig().getBoardQuery())
				|| null == jiraIssueTypeNames) {
			log.error(
					"Either Project key is empty or Jql Query not provided or jiraIssueTypeNames not configured in fieldmapping . key {} jql query {} ",
					projectConfig.getProjectToolConfig().getProjectKey(),
					projectConfig.getProjectToolConfig().getBoardQuery());
		} else {
			try {
				String issueTypes = Arrays.stream(jiraIssueTypeNames)
						.map(array -> "\"" + String.join("\", \"", array) + "\"").collect(Collectors.joining(", "));
				StringBuilder query = new StringBuilder("project in (")
						.append(projectConfig.getProjectToolConfig().getProjectKey()).append(") and ");

				String userQuery = projectConfig.getJira().getBoardQuery().toLowerCase()
						.split(JiraConstants.ORDERBY)[0];
				query.append(userQuery);
				query.append(" and issuetype in (" + issueTypes + " ) and updatedDate>='" + deltaDate + "' ");
				query.append(" order BY updatedDate asc");
				log.info("jql query :{}", query);
				Promise<SearchResult> promisedRs = client.getProcessorSearchClient().searchJql(query.toString(),
						jiraProcessorConfig.getPageSize(), pageStart, JiraConstants.ISSUE_FIELD_SET);
				searchResult = promisedRs.claim();
				if (searchResult != null) {
					log.info(String.format(PROCESSING_ISSUES_PRINT_LOG, pageStart,
							Math.min(pageStart + jiraProcessorConfig.getPageSize() - 1, searchResult.getTotal()),
							searchResult.getTotal()));
				}
			} catch (RestClientException e) {
				if (e.getStatusCode().isPresent() && e.getStatusCode().get() == 401) {
					log.error(ERROR_MSG_401);
				} else {
					log.error(ERROR_MSG_NO_RESULT_WAS_AVAILABLE, e);
				}
				throw e;
			}

		}
		return searchResult;
	}

	/**
	 * @param projectConfig
	 *            projectConfig
	 * @param clientIncoming
	 *            clientIncoming
	 * @param pageNumber
	 *            pageNumber
	 * @param boardId
	 *            boardId
	 * @param deltaDate
	 *            deltaDate
	 * @return List of Issue
	 * @throws InterruptedException
	 *             InterruptedException
	 */
	public List<Issue> fetchIssueBasedOnBoard(ProjectConfFieldMapping projectConfig,
			ProcessorJiraRestClient clientIncoming, int pageNumber, String boardId, String deltaDate)
			throws InterruptedException, IOException {

		client = clientIncoming;
		List<Issue> issues = new ArrayList<>();
		if (client == null) {
			log.error(MSG_JIRA_CLIENT_SETUP_FAILED);
		} else {
			String queryDate = DateUtil
					.dateTimeFormatter(DateUtil.stringToLocalDateTime(deltaDate, JiraConstants.QUERYDATEFORMAT)
							.minusDays(jiraProcessorConfig.getDaysToReduce()), JiraConstants.QUERYDATEFORMAT);

			SearchResult searchResult = getBoardIssues(boardId, projectConfig, queryDate, pageNumber);
			issues = JiraHelper.getIssuesFromResult(searchResult);
		}
		return issues;
	}

	/**
	 * @param boardId
	 *            boardId
	 * @param projectConfig
	 *            projectConfig
	 * @param deltaDate
	 *            deltaDate
	 * @param pageStart
	 *            pageStart
	 * @return SearchResult
	 * @throws InterruptedException
	 *             InterruptedException
	 */
	public SearchResult getBoardIssues(String boardId, ProjectConfFieldMapping projectConfig, String deltaDate,
			int pageStart) throws InterruptedException {
		SearchResult searchResult = null;
		String[] jiraIssueTypeNames = projectConfig.getFieldMapping().getJiraIssueTypeNames();
		if (client == null) {
			log.warn(MSG_JIRA_CLIENT_SETUP_FAILED);
		} else if (StringUtils.isEmpty(projectConfig.getProjectToolConfig().getProjectKey())
				|| null == jiraIssueTypeNames) {
			log.error("Either Project key is empty or jiraIssueTypeNames not provided. key {} ",
					projectConfig.getProjectToolConfig().getProjectKey());
		} else {
			try {
				String query = "updatedDate>='" + deltaDate + "' order by updatedDate asc";
				Promise<SearchResult> promisedRs = client.getCustomIssueClient().searchBoardIssue(boardId, query,
						jiraProcessorConfig.getPageSize(), pageStart, JiraConstants.ISSUE_FIELD_SET);
				searchResult = promisedRs.claim();
				if (searchResult != null) {
					log.info(String.format(PROCESSING_ISSUES_PRINT_LOG, pageStart,
							Math.min(pageStart + jiraProcessorConfig.getPageSize() - 1, searchResult.getTotal()),
							searchResult.getTotal()));
				}
			} catch (RestClientException e) {
				if (e.getStatusCode().isPresent() && e.getStatusCode().get() == 401) {
					log.error(ERROR_MSG_401);
				} else {
					log.error(ERROR_MSG_NO_RESULT_WAS_AVAILABLE, e);
				}
				throw e;
			}
		}

		return searchResult;
	}

	/**
	 * @param projectConfig
	 *            projectConfig
	 * @param krb5Client
	 *            krb5Client
	 * @return List of ProjectVersion
	 */
	public List<ProjectVersion> getVersion(ProjectConfFieldMapping projectConfig, KerberosClient krb5Client)
			throws IOException, ParseException {
		List<ProjectVersion> projectVersionList = new ArrayList<>();
		try {
			JiraToolConfig jiraToolConfig = projectConfig.getJira();
			if (null != jiraToolConfig) {
				URL url = getVersionUrl(projectConfig);
				parseVersionData(getDataFromClient(projectConfig, url, krb5Client), projectVersionList);
			}
		} catch (RestClientException rce) {
			log.error("Client exception when fetching versions " + rce);
			throw rce;
		} catch (MalformedURLException mfe) {
			log.error("Malformed url for fetching versions", mfe);
			throw mfe;
		}
		return projectVersionList;
	}

	private URL getVersionUrl(ProjectConfFieldMapping projectConfig) throws MalformedURLException {

		Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();
		boolean isCloudEnv = connectionOptional.map(Connection::isCloudEnv).orElse(false);
		String serverURL = jiraProcessorConfig.getJiraVersionApi();
		if (isCloudEnv) {
			serverURL = jiraProcessorConfig.getJiraCloudVersionApi();
		}
		serverURL = serverURL.replace("{projectKey}", projectConfig.getJira().getProjectKey());
		String baseUrl = connectionOptional.map(Connection::getBaseUrl).orElse("");
		return new URL(baseUrl + (baseUrl.endsWith("/") ? "" : "/") + serverURL);

	}

	private void parseVersionData(String dataFromServer, List<ProjectVersion> projectVersionDetailList)
			throws ParseException {
		if (StringUtils.isNotBlank(dataFromServer)) {
			try {
				JSONArray obj = (JSONArray) new JSONParser().parse(dataFromServer);
				if (null != obj) {
					((JSONArray) new JSONParser().parse(dataFromServer)).forEach(values -> {
						ProjectVersion projectVersion = new ProjectVersion();
						projectVersion.setId(
								Long.valueOf(Objects.requireNonNull(getOptionalString((JSONObject) values, "id"))));
						projectVersion.setName(getOptionalString((JSONObject) values, "name"));
						projectVersion
								.setArchived(Boolean.parseBoolean(getOptionalString((JSONObject) values, "archived")));
						projectVersion
								.setReleased(Boolean.parseBoolean(getOptionalString((JSONObject) values, "released")));
						if (getOptionalString((JSONObject) values, "startDate") != null) {
							projectVersion.setStartDate(DateUtil.stringToDateTime(
									Objects.requireNonNull(getOptionalString((JSONObject) values, "startDate")),
									"yyyy-MM-dd"));
						}
						if (getOptionalString((JSONObject) values, "releaseDate") != null) {
							projectVersion.setReleaseDate(DateUtil.stringToDateTime(
									Objects.requireNonNull(getOptionalString((JSONObject) values, "releaseDate")),
									"yyyy-MM-dd"));
						}
						projectVersionDetailList.add(projectVersion);
					});
				}
			} catch (Exception pe) {
				log.error("Parser exception when parsing versions", pe);
				throw pe;
			}

		}
	}

	private String getOptionalString(final JSONObject jsonObject, final String attributeName) {
		final Object res = jsonObject.get(attributeName);
		if (res == null) {
			return null;
		}
		return res.toString();
	}

	/**
	 * Gets api host
	 **/
	public String getApiHost() throws UnknownHostException {

		StringBuilder urlPath = new StringBuilder();
		if (StringUtils.isNotEmpty(jiraProcessorConfig.getUiHost())) {
			urlPath.append("https").append(':').append(File.separator + File.separator)
					.append(jiraProcessorConfig.getUiHost().trim());
		} else {
			throw new UnknownHostException("Api host not found in properties.");
		}

		return urlPath.toString();
	}

}