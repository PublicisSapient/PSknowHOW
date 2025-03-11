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
package com.publicissapient.kpidashboard.rally.service;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.exceptions.ClientErrorMessageEnum;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.model.application.ErrorDetail;
import com.publicissapient.kpidashboard.common.model.application.ProjectVersion;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import com.publicissapient.kpidashboard.rally.config.RallyProcessorConfig;
import com.publicissapient.kpidashboard.rally.constant.RallyConstants;
import com.publicissapient.kpidashboard.rally.helper.RallyHelper;
import com.publicissapient.kpidashboard.rally.model.HierarchicalRequirement;
import com.publicissapient.kpidashboard.rally.model.Iteration;
import com.publicissapient.kpidashboard.rally.model.IterationResponse;
import com.publicissapient.kpidashboard.rally.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.rally.model.QueryResult;
import com.publicissapient.kpidashboard.rally.model.RallyResponse;
import com.publicissapient.kpidashboard.rally.model.RallyToolConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RallyCommonService {

	private static final String RALLY_URL = "https://rally1.rallydev.com/slm/webservice/v2.0";
	private static final String API_KEY = "_8BogJQcTuGwVjEemJiAjV0z5SgR2UCSsSnBUu55Y5U";
	private static final String PROJECT_NAME = "Core Team";
	private static final int PAGE_SIZE = 200; // Number of artifacts per page

	@Autowired
	private RallyProcessorConfig rallyProcessorConfig;

	@Autowired
	private ToolCredentialProvider toolCredentialProvider;

	@Autowired
	private AesEncryptionService aesEncryptionService;
	@Autowired
	private ProcessorToolConnectionService processorToolConnectionService;
	@Autowired
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

	@Autowired
	private RestTemplate restTemplate;

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
		ObjectId projectConfigId = projectConfig.getBasicProjectConfigId();
		boolean spenagoClient = connectionOptional.map(Connection::isJaasKrbAuth).orElse(false);
		if (spenagoClient) {
			HttpUriRequest request = RequestBuilder.get().setUri(url.toString())
					.setHeader(org.apache.http.HttpHeaders.ACCEPT, "application/json")
					.setHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, "application/json").build();
			String responce = krb5Client.getResponse(request);
			return responce;
		} else {
			return getDataFromServer(url, connectionOptional, projectConfigId);
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
	public String getDataFromServer(URL url, Optional<Connection> connectionOptional, ObjectId projectConfigId)
			throws IOException {
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
		// process the client error
		processClientError(connectionOptional, request, projectConfigId);
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
			String errorMessage = ie.getMessage();
			// Regular expression pattern to extract the status code
			Pattern pattern = Pattern.compile("\\b(\\d{3})\\b");
			Matcher matcher = pattern.matcher(errorMessage);
			isClientException(connectionOptional, matcher);
			request.disconnect();
		}
		return sb.toString();
	}

	/**
	 * Method to process client error and update the connection broken flag
	 *
	 * @param connectionOptional
	 *            connectionOptional
	 * @param request
	 *            request
	 * @throws IOException
	 *             throw IO Error
	 */
	private void processClientError(Optional<Connection> connectionOptional, HttpURLConnection request,
			ObjectId basicProjectConfigId) throws IOException {
		int responseCode = request.getResponseCode();
		if (responseCode >= 400 && responseCode < 500) {
			// Read error message from the server
			String errorMessage = readErrorStream(request.getErrorStream());
			if (responseCode == 404) {
				ErrorDetail errorDetail = new ErrorDetail(responseCode, request.getURL().toString(), errorMessage,
						determineImpactBasedOnUrl(request.getURL().toString()));
				Optional<ProcessorExecutionTraceLog> existingTraceLog = processorExecutionTraceLogRepository
						.findByProcessorNameAndBasicProjectConfigIdAndProgressStatsTrue(RallyConstants.RALLY,
								basicProjectConfigId.toString());
				existingTraceLog.ifPresent(traceLog -> {
					List<ErrorDetail> errorDetailList = Optional.ofNullable(traceLog.getErrorDetailList())
							.orElseGet(ArrayList::new);
					errorDetailList.add(errorDetail);
					traceLog.setErrorDetailList(errorDetailList);
					processorExecutionTraceLogRepository.save(traceLog);
				});
			}
			// flagging the connection flag w.r.t error code.
			connectionOptional.ifPresent(connection -> {
				String errMsg = ClientErrorMessageEnum.fromValue(responseCode).getReasonPhrase();
				processorToolConnectionService.updateBreakingConnection(connection.getId(), errMsg);
			});
			log.error("Exception when reading from server {} - {}", responseCode, errorMessage);
			// Throw exception for non-404 errors, as 404 indicates the resource mightn't
			// exist
			if (responseCode != 404) {
				request.disconnect();
				throw new IOException(String.format("Error: %d - %s", responseCode, errorMessage));
			}
		}
	}

	private String readErrorStream(InputStream errorStream) throws IOException {
		StringBuilder response = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
		}
		return response.toString();
	}

	private String determineImpactBasedOnUrl(String url) {
		if (url.contains("sprint")) {
			return "Sprint KPI's";
		} else if (url.contains("versions")) {
			return "Release KPI's";
		} else if (url.contains("epic")) {
			return "Epic KPI's";
		}
		return ""; // Default or unknown impact
	}

	/**
	 * @param connectionOptional
	 *            connectionOptional
	 * @param matcher
	 *            matcher
	 */
	private void isClientException(Optional<Connection> connectionOptional, Matcher matcher) {
		if (matcher.find()) {
			String statusCodeString = matcher.group(1);
			int statusCode = Integer.parseInt(statusCodeString);
			if (statusCode >= 400 && statusCode < 500 && connectionOptional.isPresent()) {
				String errMsg = ClientErrorMessageEnum.fromValue(statusCode).getReasonPhrase();
				processorToolConnectionService.updateBreakingConnection(connectionOptional.get().getId(), errMsg);
			}
		}
	}

	/**
	 * @param encryptedPassword
	 *            encryptedPassword
	 * @return String
	 */
	public String decryptJiraPassword(String encryptedPassword) {
		return aesEncryptionService.decrypt(encryptedPassword, rallyProcessorConfig.getAesEncryptionKey());
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
	 * @param pageNumber
	 *            pageNumber
	 * @param deltaDate
	 *            deltaDate
	 * @return List of Issue
	 */
	public List<HierarchicalRequirement> fetchIssuesBasedOnJql(ProjectConfFieldMapping projectConfig, int pageNumber,
			String deltaDate) throws InterruptedException {
		String queryDate = DateUtil
				.dateTimeFormatter(DateUtil.stringToLocalDateTime(deltaDate, RallyConstants.QUERYDATEFORMAT)
						.minusDays(rallyProcessorConfig.getDaysToReduce()), RallyConstants.QUERYDATEFORMAT);
		RallyResponse rallyResponse = getRqlIssues(projectConfig, queryDate, pageNumber);
		List<HierarchicalRequirement> hierarchicalRequirements = RallyHelper.getIssuesFromResult(rallyResponse);
		return hierarchicalRequirements;
	}
	/**
	 * @param projectConfig
	 *            projectConfig
	 * @param deltaDate
	 *            deltaDate
	 * @param pageStart
	 *            pageStart
	 * @return SearchResult
	 */
	public RallyResponse getRqlIssues(ProjectConfFieldMapping projectConfig, String deltaDate, int pageStart) throws InterruptedException {
		RallyResponse rallyResponse = null;
		// String[] rallyIssueTypeNames =
		// projectConfig.getFieldMapping().getRallyIssueTypeNames();
//		List<RallyArtifact> queryResponse = getRallyIssues(projectConfig, deltaDate, pageStart);
//		queryResponse = queryResponse.stream().filter(Objects::nonNull).collect(Collectors.toList());
		try {
			List<HierarchicalRequirement> allArtifacts = getHierarchicalRequirements(pageStart);
			// Create a RallyResponse object and populate it with the combined results
			QueryResult queryResult = new QueryResult();
			queryResult.setResults(allArtifacts);
			queryResult.setTotalResultCount(allArtifacts.size());
			queryResult.setStartIndex(pageStart);
			queryResult.setPageSize(PAGE_SIZE);

			rallyResponse = new RallyResponse();
			rallyResponse.setQueryResult(queryResult);

			if (rallyResponse != null) {
				saveSearchDetailsInContext(rallyResponse, pageStart, null, StepSynchronizationManager.getContext());
				// log.info(String.format(PROCESSING_ISSUES_PRINT_LOG, pageStart,
				// Math.min(pageStart + rallyProcessorConfig.getPageSize() - 1,
				// rallyResponse.getQueryResult().getTotalResultCount()),
				// rallyResponse.getQueryResult().getTotalResultCount());
			}
		} catch (RestClientException e) {
			if (e.getStatusCode().isPresent() && e.getStatusCode().get() >= 400 && e.getStatusCode().get() < 500) {
				String errMsg = ClientErrorMessageEnum.fromValue(e.getStatusCode().get()).getReasonPhrase();
				processorToolConnectionService
						.updateBreakingConnection(projectConfig.getProjectToolConfig().getConnectionId(), errMsg);
			}
			throw e;
		}
		return rallyResponse;
	}

	public List<HierarchicalRequirement> getHierarchicalRequirements(int pageStart) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("ZSESSIONID", API_KEY);
		HttpEntity<String> entity = new HttpEntity<>(headers);

		// List of artifact types to query
		List<String> artifactTypes = Arrays.asList("hierarchicalrequirement", "defect", "task");

		// Fetch fields for each artifact type
		String fetchFields = "FormattedID,Name,Owner,PlanEstimate,ScheduleState,Iteration,CreationDate,LastUpdateDate";
		List<HierarchicalRequirement> allArtifacts = new ArrayList<>();

		// Query each artifact type
		for (String artifactType : artifactTypes) {
			int start = pageStart; // Start index for pagination
			boolean hasMoreResults = true;

			while (hasMoreResults) {
				String url = String.format("%s/%s?query = (Project.Name = \"%s\")&fetch=%s&start=%d&pagesize=%d",
						RALLY_URL, artifactType, PROJECT_NAME, fetchFields, start, PAGE_SIZE);
				ResponseEntity<RallyResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity,
						RallyResponse.class);

				if (response.getStatusCode() == HttpStatus.OK) {
					RallyResponse responseBody = response.getBody();
					if (responseBody != null && responseBody.getQueryResult() != null) {
						List<HierarchicalRequirement> artifacts = responseBody.getQueryResult().getResults();
						if (artifacts != null && !artifacts.isEmpty()) {
							for (HierarchicalRequirement artifact : artifacts) {
								// Fetch full iteration details if it exists
								if (artifact.getIteration() != null && artifact.getIteration().getRef() != null) {
									artifact.setIteration(fetchIterationDetails(artifact.getIteration().getRef(), entity));
								}
								allArtifacts.add(artifact);
							}
							start += PAGE_SIZE; // Move to the next page
						} else {
							hasMoreResults = false;
						}
					} else {
						hasMoreResults = false; // No response body
					}
				} else {
					log.error("Failed to fetch data for {}: {}", artifactType, response.getStatusCode());
					hasMoreResults = false; // Stop on error
				}
			}
		}
		return allArtifacts;
	}



	public List<HierarchicalRequirement> getHierarchicalRequirementsByIteration(Iteration iteration,HierarchicalRequirement hierarchicalRequirement) {
		List<HierarchicalRequirement> results = new ArrayList<>();
		if(iteration != null){
			HttpHeaders headers = new HttpHeaders();
			headers.set("ZSESSIONID", API_KEY);
			HttpEntity<String> entity = new HttpEntity<>(headers);
			String RALLY_API_URL = "https://rally1.rallydev.com/slm/webservice/v2.0/+\""+hierarchicalRequirement.getType()+"\"?" +
					"query=(Iteration.Name = \"" + iteration.getName() + "\")&fetch=FormattedID,Name,Owner,PlanEstimate,ScheduleState,Iteration";
			results = restTemplate.exchange(RALLY_API_URL, HttpMethod.GET, entity,
					RallyResponse.class).getBody().getQueryResult().getResults();
		}
		return results;
	}

	/**
	 * Method to save the search details in context.
	 *
	 * @param rallyResponse
	 *            rallyResponse
	 * @param pageStart
	 *            pageStart
	 * @param stepContext
	 *            stepContext
	 */
	public void saveSearchDetailsInContext(RallyResponse rallyResponse, int pageStart, String boardId,
			StepContext stepContext) {
		if (stepContext == null) {
			log.error("StepContext is null");
			return;
		}
		JobExecution jobExecution = stepContext.getStepExecution().getJobExecution();
		int total = rallyResponse.getQueryResult().getTotalResultCount();
		int processed = Math.min(pageStart + rallyProcessorConfig.getPageSize() - 1, total);

		// Saving Progress details in context
		jobExecution.getExecutionContext().putInt(RallyConstants.TOTAL_ISSUES, total);
		jobExecution.getExecutionContext().putInt(RallyConstants.PROCESSED_ISSUES, processed);
		jobExecution.getExecutionContext().putInt(RallyConstants.PAGE_START, pageStart);
		jobExecution.getExecutionContext().putString(RallyConstants.BOARD_ID, boardId);
	}

	/**
	 * @param projectConfig
	 *            projectConfig
	 * @param krb5Client
	 *            krb5Client
	 * @return List of ProjectVersion
	 * @throws IOException
	 *             IOException
	 * @throws ParseException
	 *             ParseException
	 */
	public List<ProjectVersion> getVersion(ProjectConfFieldMapping projectConfig, KerberosClient krb5Client)
			throws IOException, ParseException {
		List<ProjectVersion> projectVersionList = new ArrayList<>();
		try {
			RallyToolConfig rallyToolConfig = projectConfig.getJira();
			if (null != rallyToolConfig) {
				URL url = getVersionUrl(projectConfig);
				parseVersionData(getDataFromClient(projectConfig, url, krb5Client), projectVersionList);
			}
		} catch (RestClientException rce) {
			if (rce.getStatusCode().isPresent() && rce.getStatusCode().get() >= 400
					&& rce.getStatusCode().get() < 500) {
				String errMsg = ClientErrorMessageEnum.fromValue(rce.getStatusCode().get()).getReasonPhrase();
				processorToolConnectionService
						.updateBreakingConnection(projectConfig.getProjectToolConfig().getConnectionId(), errMsg);
			}
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
		String serverURL = rallyProcessorConfig.getJiraVersionApi();
		if (isCloudEnv) {
			serverURL = rallyProcessorConfig.getJiraCloudVersionApi();
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
	 * * Gets api host
	 *
	 * @return apiHost
	 * @throws UnknownHostException
	 *             UnknownHostException
	 */
	public String getApiHost() throws UnknownHostException {

		StringBuilder urlPath = new StringBuilder();
		if (StringUtils.isNotEmpty(rallyProcessorConfig.getUiHost())) {
			urlPath.append("https").append(':').append(File.separator + File.separator)
					.append(rallyProcessorConfig.getUiHost().trim());
		} else {
			throw new UnknownHostException("Api host not found in properties.");
		}

		return urlPath.toString();
	}

	private Iteration fetchIterationDetails(String iterationUrl, HttpEntity<String> entity) {
		try {
			ResponseEntity<IterationResponse> response = restTemplate.exchange(iterationUrl, HttpMethod.GET, entity, IterationResponse.class);

			if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && response.getBody().getIteration() != null) {
				Iteration iteration = response.getBody().getIteration();
				log.info("Fetched Iteration: {}", iteration.getName());
				return iteration;
			} else {
				log.warn("Iteration details not found in response for URL: {}", iterationUrl);
			}
		} catch (RestClientException e) {
			log.error("Failed to fetch iteration details from URL: {}. Error: {}", iterationUrl, e.getMessage(), e);
		}
		// Return an empty Iteration object instead of null
		return new Iteration();
	}

}
