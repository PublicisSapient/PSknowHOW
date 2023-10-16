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

package com.publicissapient.kpidashboard.azure.adapter.impl.async;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicissapient.kpidashboard.azure.config.AzureProcessorConfig;
import com.publicissapient.kpidashboard.azure.model.AzureServer;
import com.publicissapient.kpidashboard.azure.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.azure.util.AzureConstants;
import com.publicissapient.kpidashboard.azure.util.AzureProcessorUtil;
import com.publicissapient.kpidashboard.common.model.azureboards.AzureBoardsWIModel;
import com.publicissapient.kpidashboard.common.model.azureboards.iterations.AzureIterationsModel;
import com.publicissapient.kpidashboard.common.model.azureboards.updates.AzureUpdatesModel;
import com.publicissapient.kpidashboard.common.model.azureboards.wiql.AzureWiqlModel;
import com.publicissapient.kpidashboard.common.util.RestOperationsFactory;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ProcessorAsyncAzureRestClientImpl implements ProcessorAzureRestClient {

	private static final String ERROR_GET_WIQL_RESPONSE_API = "Error while parsing getWiqlResponse API ";
	private static final String ERROR_GET_WORK_ITEM_INFO_API = "Error while parsing getWorkItemInfo API ";
	private static final String ERROR_WHILE_PARSING = "Error while parsing getIterationsResponse API";
	private static final String API_VERSION = "api-version";
	private static final String NO_RESULT_QUERY = "No result available for query: {}";

	private final RestOperations restOperations;
	private final AzureProcessorConfig azureProcessorConfig;

	ObjectMapper mapper;

	@Autowired
	public ProcessorAsyncAzureRestClientImpl(RestOperationsFactory<RestOperations> restOperationsFactory,
			AzureProcessorConfig azureProcessorConfig, ObjectMapper mapper) {
		this.restOperations = restOperationsFactory.getTypeInstance();
		this.azureProcessorConfig = azureProcessorConfig;
		this.mapper = mapper;
	}

	/**
	 * Creates HTTP Headers.
	 *
	 * @param userInfo
	 *            the user info
	 * @return the HttpHeaders
	 */
	public static HttpHeaders createHeaders(final String userInfo) {
		byte[] encodedAuth = Base64.encodeBase64(userInfo.getBytes(StandardCharsets.US_ASCII));
		String authHeader = "Basic " + new String(encodedAuth);

		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, authHeader);
		return headers;
	}

	/**
	 * Check whether two urls have the same server info
	 *
	 * @param url1
	 *            url1
	 * @param url2
	 *            url2
	 * @return true if they have same server info else false
	 */
	public static boolean isSameServerInfo(String url1, String url2) {
		try {
			String domain1 = extractDomain(url1);
			int port1 = extractPort(url1);
			String domain2 = extractDomain(url2);
			int port2 = extractPort(url2);

			if (StringUtils.isEmpty(domain1) || StringUtils.isEmpty(domain2)) {
				return false;
			}
			if (domain1.equals(domain2) && port1 == port2) {
				return true;
			}

		} catch (URISyntaxException exception) {
			log.error(String.format("uri syntax error url1= %s, url2 = %s", url1, url2), exception);
		}

		return false;
	}

	/**
	 * Provides Domain name.
	 *
	 * @param url
	 *            the URL
	 * @return the domain name
	 * @throws URISyntaxException
	 *             if there is any illegal character in URI
	 */
	public static String extractDomain(String url) throws URISyntaxException {
		URI uri = new URI(url);
		return uri.getHost();
	}

	/**
	 * Provides Port number.
	 *
	 * @param url
	 *            the URL
	 * @return port the port number
	 * @throws URISyntaxException
	 *             if there is any illegal character in URI
	 */
	public static int extractPort(String url) throws URISyntaxException {
		URI uri = new URI(url);
		return uri.getPort();
	}

	private static String getFromattedQueryInput(String field) {
		return "'" + field + "'";
	}

	/**
	 * append pre and post query
	 *
	 * @param preQuery
	 * @param postQuery
	 * @return appended query
	 */
	private static StringBuilder appendDateQuery(String preQuery, String postQuery) {
		StringBuilder sb = new StringBuilder();
		if (StringUtils.containsIgnoreCase(preQuery, AzureConstants.WHERE)) {
			sb.append(preQuery.replace(AzureConstants.WHERE, AzureConstants.WHERE + " (" + postQuery + ") AND "));
		} else {
			sb.append(preQuery);
			sb.append(" " + AzureConstants.WHERE + " ");
			sb.append("(");
			sb.append(postQuery);
			sb.append(")");
		}

		return sb;
	}

	/**
	 * replace changed date
	 *
	 * @param preQuery
	 * @param postQuery
	 * @return replaced query
	 */
	private static StringBuilder replaceDateQuery(String preQuery, String postQuery) {
		StringBuilder sb = new StringBuilder();
		sb.append(preQuery.replace(AzureConstants.CHANGEDDATE, " (" + postQuery + ") "));
		return sb;
	}

	/**
	 * Makes Rest Call.
	 *
	 * @param sUrl
	 *            the rest call URL
	 * @param azureInfo
	 *            azureInfo
	 * @return the response entity
	 *
	 */
	public ResponseEntity<String> doRestCall(String sUrl, AzureServer azureInfo) {
		log.debug("Inside doRestCall {}", sUrl);
		URI theUri = URI.create(sUrl);
		String userInfo = getUserInfo(sUrl, azureInfo);

		if (StringUtils.isNotEmpty(userInfo)) {
			return restOperations.exchange(theUri, HttpMethod.GET, new HttpEntity<>(createHeaders(userInfo)),
					String.class);
		} else {
			return restOperations.exchange(theUri, HttpMethod.GET, null, String.class);
		}
	}

	public ResponseEntity<String> doRestPOSTCall(String sUrl, AzureServer azureServer, Map<String, String> params) {
		log.debug("Inside doRestPOSTCall {}", sUrl);
		URI theUri = URI.create(sUrl);
		String userInfo = getUserInfo(sUrl, azureServer);

		if (StringUtils.isNotEmpty(userInfo)) {

			return restOperations.exchange(theUri, HttpMethod.POST, new HttpEntity<>(params, createHeaders(userInfo)),
					String.class);
		} else {
			return restOperations.exchange(theUri, HttpMethod.POST, new HttpEntity<>(params), String.class);
		}
	}

	/**
	 * Gets user info.
	 *
	 * @param sUrl
	 *            the url
	 * @param azureServer
	 *            the azure server
	 * @return userInfo info
	 */
	private String getUserInfo(String sUrl, AzureServer azureServer) {
		String userInfo = "";

		if (isSameServerInfo(sUrl, azureServer.getUrl())) {

			if (StringUtils.isNotEmpty(azureServer.getPat())) {
				userInfo = azureServer.getUsername() + ":" + azureServer.getPat();
			} else {
				log.warn(
						"Credentials for the following url was not found. This could happen if the domain/subdomain/IP address in the build url returned by AzurePipeline and the AzurePipeline instance url in your configuration do not match: {} ",
						sUrl);
			}

		}

		return userInfo;
	}

	@Override
	public AzureBoardsWIModel getWorkItemInfo(AzureServer azureServer, List<Integer> azureWorkItemIds) {
		AzureBoardsWIModel azureBoardsWIModel = new AzureBoardsWIModel();

		String ids = StringUtils.join(azureWorkItemIds, ",");

		StringBuilder url = new StringBuilder(
				AzureProcessorUtil.joinURL(azureServer.getUrl(), azureProcessorConfig.getApiEndpointWorkItems()));
		url = AzureProcessorUtil.addParam(url, API_VERSION, azureServer.getApiVersion());
		url = AzureProcessorUtil.addParam(url, "ids", ids);
		url = AzureProcessorUtil.addParam(url, "$expand", "all");

		ResponseEntity<String> responseEntity = doRestCall(url.toString(), azureServer);
		String response = responseEntity.getBody();
		try {
			if (StringUtils.isNotEmpty(response)) {
				azureBoardsWIModel = mapper.readValue(response, AzureBoardsWIModel.class);
			}
		} catch (IOException e) {
			log.error(ERROR_GET_WORK_ITEM_INFO_API, e.getMessage());
		}
		return azureBoardsWIModel;
	}

	@Override
	public AzureWiqlModel getWiqlResponse(AzureServer azureServer, Map<String, LocalDateTime> startTimesByIssueType,
			ProjectConfFieldMapping projectConfig, boolean dataExist) {
		AzureWiqlModel azureWiqlModel = new AzureWiqlModel();
		StringBuilder url = new StringBuilder(
				AzureProcessorUtil.joinURL(azureServer.getUrl(), azureProcessorConfig.getApiEndpointWiql()));
		url = AzureProcessorUtil.addParam(url, API_VERSION, azureServer.getApiVersion());

		if (null != projectConfig.getFieldMapping().getJiraIssueTypeNames()
				&& projectConfig.getFieldMapping().getJiraIssueTypeNames().length > 0) {
			azureWiqlModel = prepareWiqlResponse(azureServer, projectConfig, startTimesByIssueType, url, dataExist);
		}

		return azureWiqlModel;

	}

	private AzureWiqlModel prepareWiqlResponse(AzureServer azureServer, ProjectConfFieldMapping projectConfig,
			Map<String, LocalDateTime> startTimesByIssueType, StringBuilder url, boolean dataExist) {
		AzureWiqlModel azureWiqlModel = new AzureWiqlModel();
		String finalQuery = null;

		if (projectConfig.getAzure().isQueryEnabled()) {
			finalQuery = processProvidedQuery(projectConfig.getAzure().getBoardQuery(), startTimesByIssueType,
					dataExist);
		} else {
			finalQuery = prepareDefaultQuery(projectConfig, startTimesByIssueType);
		}

		Map<String, String> params = new HashMap<>();
		params.put("query", finalQuery);
		log.info(finalQuery);
		ResponseEntity<String> responseEntity = doRestPOSTCall(url.toString(), azureServer, params);
		if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			String response = responseEntity.getBody();
			try {
				if (StringUtils.isNotEmpty(response)) {
					azureWiqlModel = mapper.readValue(response, AzureWiqlModel.class);
				} else {
					log.info(NO_RESULT_QUERY, finalQuery);
				}
			} catch (IOException e) {
				log.error(ERROR_GET_WIQL_RESPONSE_API, e.getMessage());
			}

		} else {
			log.error("Response Error for Wiql API call ");
		}
		return azureWiqlModel;
	}

	@Override
	public AzureIterationsModel getIterationsResponse(AzureServer azureServer) {
		AzureIterationsModel azureIterationsModel = new AzureIterationsModel();

		StringBuilder url = new StringBuilder(
				AzureProcessorUtil.joinURL(azureServer.getUrl(), azureProcessorConfig.getApiEndpointIterations()));
		url = AzureProcessorUtil.addParam(url, API_VERSION, azureServer.getApiVersion());

		ResponseEntity<String> responseEntity = doRestCall(url.toString(), azureServer);
		String response = responseEntity.getBody();
		try {
			azureIterationsModel = mapper.readValue(response, AzureIterationsModel.class);
		} catch (IOException e) {
			log.error(ERROR_WHILE_PARSING, e.getMessage());
		}
		return azureIterationsModel;
	}

	/**
	 * @param azureServer
	 *            for connection detail
	 * @param issueId
	 *            for getting particular issue value
	 * @return AzureUpdatesModel response
	 */
	@Override
	public AzureUpdatesModel getUpdatesResponse(AzureServer azureServer, String issueId) {
		AzureUpdatesModel azureUpdatesModel = new AzureUpdatesModel();
		StringBuilder url = new StringBuilder(AzureProcessorUtil.joinURL(azureServer.getUrl(),
				azureProcessorConfig.getApiEndpointWorkItems(), "/" + issueId + "/updates"));
		url = AzureProcessorUtil.addParam(url, API_VERSION, azureServer.getApiVersion());

		ResponseEntity<String> responseEntity = doRestCall(url.toString(), azureServer);
		String response = responseEntity.getBody();
		try {
			azureUpdatesModel = mapper.readValue(response, AzureUpdatesModel.class);
		} catch (IOException e) {
			log.error("Error while parsing getUpdate API", e.getMessage());
		}
		return azureUpdatesModel;
	}

	/**
	 * @param azureServer
	 *            for connection detail
	 * @param metadataUrlPath
	 *            for api endpoints
	 * @param orgLevelApi
	 *            for switching between project level and organisational level
	 *            endpoint
	 * @return jsonObject response
	 */
	public JSONObject getMetadataJson(AzureServer azureServer, String metadataUrlPath, boolean orgLevelApi) {
		JSONObject response = null;
		if (orgLevelApi) {
			azureServer.setUrl(azureServer.getUrl().substring(0, azureServer.getUrl().lastIndexOf('/'))); // for
			// getIssueLinkTypes
		}
		StringBuilder url = new StringBuilder(AzureProcessorUtil.joinURL(azureServer.getUrl(), metadataUrlPath));
		url = AzureProcessorUtil.addParam(url, API_VERSION, azureServer.getApiVersion());

		ResponseEntity<String> responseEntity = doRestCall(url.toString(), azureServer);
		if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			try {
				JSONParser parser = new JSONParser();
				response = (JSONObject) parser.parse(responseEntity.getBody());
			} catch (ParseException e) {
				log.error("error while parsing metadata.", e.getMessage());
			}
		} else {
			log.error("Response Error for metadata API call ");
		}
		return response;

	}

	private String prepareDefaultQuery(ProjectConfFieldMapping projectConfig,
			Map<String, LocalDateTime> startTimesByIssueType) {
		StringBuilder query = new StringBuilder();
		String selectQuery = azureProcessorConfig.getWiqlSelectQuery();
		query.append(selectQuery);
		query.append(" ");
		int size = startTimesByIssueType.entrySet().size();
		int count = 0;
		StringBuilder issueTypeQuery = new StringBuilder("");
		for (Map.Entry<String, LocalDateTime> entry : startTimesByIssueType.entrySet()) {
			count++;
			String type = entry.getKey();
			String date = entry.getValue().toLocalDate().toString();
			issueTypeQuery.append("([System.WorkItemType] ='" + type + "' AND [System.ChangedDate] >="
					+ getFromattedQueryInput(date) + ") ");
			if (count < size) {
				issueTypeQuery.append(" OR ");
			}
		}
		query.append("[System.TeamProject] = '" + projectConfig.getProjectKey() + "' AND (" + issueTypeQuery + ") ");
		query.append(azureProcessorConfig.getWiqlSortQuery());

		return query.toString();
	}

	private String processProvidedQuery(String userProvidedQuery,
			Map<String, LocalDateTime> startDateTimeStrByIssueType, boolean dataExist) {
		StringBuilder finalQuery = new StringBuilder();
		if (StringUtils.isEmpty(userProvidedQuery) || startDateTimeStrByIssueType == null) {
			return finalQuery.toString();
		}
		userProvidedQuery = userProvidedQuery.toLowerCase().split(AzureConstants.ORDERBY)[0];
		int size = startDateTimeStrByIssueType.entrySet().size();
		int count = 0;
		StringBuilder issueTypeQuery = new StringBuilder(" ");
		for (Map.Entry<String, LocalDateTime> entry : startDateTimeStrByIssueType.entrySet()) {
			count++;
			String type = entry.getKey();
			String date = entry.getValue().toLocalDate().toString();
			issueTypeQuery.append("([System.WorkItemType] ='" + type + "' AND [System.ChangedDate] >="
					+ getFromattedQueryInput(date) + ") ");
			if (count < size) {
				issueTypeQuery.append(" OR ");
			}
		}

		if (dataExist) {
			if (StringUtils.containsIgnoreCase(userProvidedQuery, AzureConstants.CHANGEDDATE)) {
				finalQuery = replaceDateQuery(userProvidedQuery, issueTypeQuery.toString());
			} else {
				finalQuery = appendDateQuery(userProvidedQuery, issueTypeQuery.toString());
			}
		} else {
			if (StringUtils.containsIgnoreCase(userProvidedQuery, AzureConstants.CHANGEDDATE)) {
				finalQuery.append(userProvidedQuery + " ");
			} else {
				finalQuery = appendDateQuery(userProvidedQuery, issueTypeQuery.toString());
			}
		}

		finalQuery.append(" " + azureProcessorConfig.getWiqlSortQuery());
		return finalQuery.toString();
	}

	/**
	 * fetched all issues tag to sprint
	 * 
	 * @param azureServer
	 * @param sprintId
	 * @return
	 */
	@Override
	public List<String> getIssuesBySprintResponse(AzureServer azureServer, String sprintId) {
		List<String> sprintWiseItemIdList = new ArrayList<>();
		StringBuilder url = new StringBuilder(AzureProcessorUtil.joinURL(azureServer.getUrl(),
				azureProcessorConfig.getApiEndpointIterations(), "/" + sprintId + "/workitems"));
		url = AzureProcessorUtil.addParam(url, API_VERSION, azureServer.getApiVersion());

		ResponseEntity<String> responseEntity = doRestCall(url.toString(), azureServer);
		String response = responseEntity.getBody();
		try {
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(response);
			JSONArray jsonArray = (JSONArray) jsonObject.get("workItemRelations");
			for (Object workItemObj : jsonArray) {
				JSONObject workItemJson = (JSONObject) workItemObj;
				JSONObject targetJsonValue = (JSONObject) workItemJson.get("target");
				if (targetJsonValue != null) {
					String itemId = convertToString(targetJsonValue, "id");
					sprintWiseItemIdList.add(itemId);
				}
			}
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		return sprintWiseItemIdList;
	}

	public String convertToString(JSONObject jsonData, String key) {
		Object jsonObj = jsonData.get(key);
		return jsonObj == null ? null : jsonObj.toString();
	}
}