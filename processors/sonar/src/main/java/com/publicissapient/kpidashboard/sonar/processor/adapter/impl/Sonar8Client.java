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

package com.publicissapient.kpidashboard.sonar.processor.adapter.impl;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.publicissapient.kpidashboard.common.constant.SonarAnalysisType;
import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.sonar.SonarDetails;
import com.publicissapient.kpidashboard.common.model.sonar.SonarHistory;
import com.publicissapient.kpidashboard.common.model.sonar.SonarMeasureData;
import com.publicissapient.kpidashboard.common.model.sonar.SonarMetric;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.common.util.RestOperationsFactory;
import com.publicissapient.kpidashboard.sonar.config.SonarConfig;
import com.publicissapient.kpidashboard.sonar.model.Paging;
import com.publicissapient.kpidashboard.sonar.model.SearchProjectsResponse;
import com.publicissapient.kpidashboard.sonar.model.SonarComponent;
import com.publicissapient.kpidashboard.sonar.model.SonarProcessorItem;
import com.publicissapient.kpidashboard.sonar.processor.adapter.SonarClient;
import com.publicissapient.kpidashboard.sonar.util.SonarDashboardUrl;
import com.publicissapient.kpidashboard.sonar.util.SonarProcessorUtils;
import com.publicissapient.kpidashboard.sonar.util.SonarUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Provide SonarQube 8 Implementation. Tested with SonarQube 8.0 and SonarQube
 * 8.1
 *
 * @author vijkumar18
 *
 */
@Component
@Slf4j
public class Sonar8Client implements SonarClient {

	private static final String RESOURCE_ENDPOINT = "/api/components/search?qualifiers=TRK&p=%d&ps=%d";
	private static final String RESOURCE_ENDPOINT_CLOUD = "/api/components/search?qualifier=TRK&organization=%s&p=%d&ps=%d";
	private static final String RESOURCE_DETAILS_ENDPOINT = "/api/measures/component?format=json&component=%s&metricKeys=%s&includealerts=true";
	private static final String PROJECT_ANALYSES_ENDPOINT = "/api/project_analyses/search?project=%s";
	private static final String MEASURE_HISTORY_ENDPOINT = "/api/measures/search_history?component=%s&metrics=%s&includealerts=true&from=%s";
	private static final String BRANCH_ENDPOINT = "&branch=%s";
	private static final String PAGE = "&p=%s";

	private static final String PROJECT_NAME = "name";
	private static final String PROJECT_KEY = "key";
	private static final String PROJECT_METRIC = "metric";
	private static final String PROJECT_MSR = "measures";
	private static final String PROJECT_MSR_VALUE = "value";
	private static final String PROJECT_DATE = "date";
	private static final String PROJECT_EVENTS = "events";
	private static final String DEFAULT_DATE = "2018-01-01";
	private static final String DOT = ".";
	private static final String BRANCH = "branch";

	private final RestOperations restOperations;
	private final SonarConfig sonarConfig;

	private ToolCredentialProvider toolCredentialProvider;

	/**
	 * Instantiates a new Sonar 8 client.
	 *
	 * @param restOperationsFactory
	 *            the rest operations supplier
	 * @param sonarConfig
	 *            the sonar settings
	 */
	@Autowired
	public Sonar8Client(RestOperationsFactory<RestOperations> restOperationsFactory, SonarConfig sonarConfig,
			ToolCredentialProvider toolCredentialProvider) {
		this.restOperations = restOperationsFactory.getTypeInstance();
		this.sonarConfig = sonarConfig;
		this.toolCredentialProvider = toolCredentialProvider;
	}

	/**
	 * Provides the list of Sonar Projects.
	 *
	 * @param sonarServer
	 *            the Sonar server connection details
	 * @return the list of Sonar project
	 */
	@Override
	public List<SonarProcessorItem> getSonarProjectList(ProcessorToolConnection sonarServer) {
		List<SonarProcessorItem> projectList = new ArrayList<>();

		int defaultPageSize = sonarConfig.getPageSize();
		Paging paging = new Paging(1, defaultPageSize, 0);

		int nextPageIndex = paging.getPageIndex();
		do {

			SearchProjectsResponse response = getProjects(sonarServer, paging, nextPageIndex);
			List<SonarProcessorItem> theProjectsList = getProjectsListFromResponse(response, sonarServer);
			if (CollectionUtils.isNotEmpty(theProjectsList)) {
				projectList.addAll(theProjectsList);
			}

			paging = response == null ? null : response.getPaging();
			nextPageIndex++;

		} while (hasNextPage(paging));

		return projectList;
	}

	/**
	 * Provides List of Sonar Project setup properties.
	 *
	 * @param response
	 *            the SearchProjectsResponse
	 * @param sonarServer
	 *            the Sonar server connection details
	 * @return the list of Sonar Project
	 */
	private List<SonarProcessorItem> getProjectsListFromResponse(SearchProjectsResponse response,
			ProcessorToolConnection sonarServer) {
		List<SonarProcessorItem> projectList = new ArrayList<>();
		if (response != null) {
			List<SonarComponent> sonarComponents = response.getComponents();
			if (sonarComponents != null) {
				sonarComponents.stream().forEach(sonarComponent -> {
					SonarProcessorItem sonarItem = new SonarProcessorItem();
					sonarItem.setInstanceUrl(sonarServer.getUrl());
					sonarItem.setProjectId(sonarComponent.getId());
					sonarItem.setProjectName(sonarComponent.getName());
					sonarItem.setKey(sonarComponent.getKey());
					if (sonarServer.getBranch() != null) {
						sonarItem.setBranch(sonarServer.getBranch());
					}
					projectList.add(sonarItem);
				});
			}
		}

		return projectList;
	}

	/**
	 * Returns true if next page is available.
	 *
	 * @param paging
	 *            the pagination properties
	 * @return true if next page is available
	 */
	private boolean hasNextPage(Paging paging) {
		if (paging == null) {
			return false;
		}

		int pages = getTotalPages(paging);

		return paging.getPageIndex() < pages;
	}

	/**
	 * Count total pages.
	 *
	 * @param paging
	 *            the pagination properties
	 * @return total number of pages
	 */
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

	private SearchProjectsResponse getProjects(ProcessorToolConnection sonarServer, Paging paging, int nextPageIndex) {

		ToolCredential toolCredentials = SonarUtils.getToolCredentials(toolCredentialProvider, sonarServer);

		String baseUrl = sonarServer.getUrl() == null ? null : sonarServer.getUrl().trim();
		String username = toolCredentials.getUsername();
		String password = toolCredentials.getPassword();

		SearchProjectsResponse response;

		if (sonarServer.isCloudEnv()) {
			response = searchProjectsSonarCloud(baseUrl, password, nextPageIndex, paging.getPageSize(),
					sonarServer.getOrganizationKey());
		} else if (!sonarServer.isCloudEnv() && sonarServer.isAccessTokenEnabled()) {
			response = searchProjectsWithAccessToken(baseUrl, password, nextPageIndex, paging.getPageSize());
		} else {
			response = searchProjects(baseUrl, username, password, nextPageIndex, paging.getPageSize());
		}
		return response;
	}

	/**
	 * Rest call to get the projects of one page.
	 *
	 * @param baseUrl
	 *            the base url
	 * @param username
	 *            the username
	 * @param password
	 *            the password
	 * @param pageIndex
	 *            the page index
	 * @param pageSize
	 *            the page size
	 * @return SearchProjectsResponse containing projects and paging info
	 */
	private SearchProjectsResponse searchProjects(String baseUrl, String username, String password, int pageIndex,
			int pageSize) {

		String resUrl = String.format(RESOURCE_ENDPOINT, pageIndex, pageSize);
		String url = baseUrl + resUrl;

		HttpEntity<?> httpEntity = new HttpEntity<>(SonarProcessorUtils.getHeaders(username, password));

		return getSearchProjectsResponse(restOperations.exchange(url, HttpMethod.GET, httpEntity, String.class), url);

	}

	/**
	 * Rest call to get the projects of one page.
	 *
	 * @param baseUrl
	 *            the base url
	 * @param password
	 *            the password
	 * @param pageIndex
	 *            the page index
	 * @param pageSize
	 *            the page size
	 * @return SearchProjectsResponse containing projects and paging info
	 */
	private SearchProjectsResponse searchProjectsWithAccessToken(String baseUrl, String password, int pageIndex,
			int pageSize) {

		String resUrl = String.format(RESOURCE_ENDPOINT, pageIndex, pageSize);
		String url = baseUrl + resUrl;

		HttpEntity<?> httpEntity = new HttpEntity<>(SonarProcessorUtils.getHeaders(password, true));

		return getSearchProjectsResponse(restOperations.exchange(url, HttpMethod.GET, httpEntity, String.class), url);

	}

	@Nullable
	private SearchProjectsResponse getSearchProjectsResponse(ResponseEntity<String> restOperations, String url) {
		SearchProjectsResponse searchProjectsResponse = null;
		try {

			ResponseEntity<String> response = restOperations;

			if (response.getStatusCode() == HttpStatus.OK) {
				Gson gson = new Gson();
				searchProjectsResponse = gson.fromJson(response.getBody(), SearchProjectsResponse.class);

			} else {
				String statusCode = response.getStatusCode().toString();
				log.error("Error while fetching projects from {}. with status {}", url, statusCode);
			}
		} catch (RestClientException exception) {
			log.error("Error while fetching projects from {}:  {}", url, exception.getMessage());
		}

		return searchProjectsResponse;
	}

	/**
	 * Rest call to get the projects of one page.
	 *
	 * @param baseUrl
	 *            the base url
	 * @param accessToken
	 *
	 * @param pageIndex
	 *            the page index
	 * @param pageSize
	 *            the page size
	 * @return SearchProjectsResponse containing projects and paging info
	 */

	private SearchProjectsResponse searchProjectsSonarCloud(String baseUrl, String accessToken, int pageIndex,
			int pageSize, String organizationKey) {

		String resUrl = String.format(new StringBuilder(baseUrl).append(RESOURCE_ENDPOINT_CLOUD).toString(),
				organizationKey, pageIndex, pageSize);

		HttpEntity<?> httpEntity = new HttpEntity<>(SonarProcessorUtils.getHeaders(accessToken));

		return getSearchProjectsResponse(restOperations.exchange(resUrl, HttpMethod.GET, httpEntity, String.class),
				resUrl);

	}

	/**
	 * Provides Current Sonar snapshot.
	 *
	 * @param project
	 *            the Sonar project setup properties
	 * @param httpHeaders
	 *            the list of http header
	 * @param metrics
	 *            the metrics
	 * @return the current sonar data
	 */
	@Override
	public SonarDetails getLatestSonarDetails(SonarProcessorItem project, HttpEntity<String> httpHeaders,
			String metrics) {
		String url;
		if (!project.getToolDetailsMap().containsKey(BRANCH)) {
			url = String.format(
					new StringBuilder(project.getInstanceUrl()).append(RESOURCE_DETAILS_ENDPOINT).toString(),
					project.getKey(), metrics);
			log.info("getting sonar details for url = {}", url);
		} else {
			url = String.format(new StringBuilder(project.getInstanceUrl()).append(RESOURCE_DETAILS_ENDPOINT)
					.append(BRANCH_ENDPOINT).toString(), project.getKey(), metrics, project.getBranch());
			log.info("getting sonar details for url = {}", url);
		}
		try {
			ResponseEntity<String> response = restOperations.exchange(url, HttpMethod.GET, httpHeaders, String.class);
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(response.getBody());
			String key = "component";

			if (jsonObject != null) {
				JSONObject resources = (JSONObject) jsonObject.get(key);

				SonarDetails sonarDetail = new SonarDetails();
				sonarDetail.setType(SonarAnalysisType.STATIC_ANALYSIS);
				sonarDetail.setName(SonarProcessorUtils.convertToString(resources, PROJECT_NAME));
				sonarDetail.setUrl(new SonarDashboardUrl(project.getInstanceUrl(),
						SonarProcessorUtils.convertToString(resources, PROJECT_KEY)).toString());
				sonarDetail.setBranch(project.getBranch());

				if (!project.getToolDetailsMap().containsKey(BRANCH)) {
					url = String.format(
							new StringBuilder(project.getInstanceUrl()).append(PROJECT_ANALYSES_ENDPOINT).toString(),
							SonarProcessorUtils.convertToString(resources, PROJECT_KEY));
				} else {
					url = String.format(
							new StringBuilder(project.getInstanceUrl()).append(PROJECT_ANALYSES_ENDPOINT)
									.append(BRANCH_ENDPOINT).toString(),
							SonarProcessorUtils.convertToString(resources, PROJECT_KEY), project.getBranch());
				}
				key = "analyses";
				JSONArray jsonResources = SonarProcessorUtils.parseData(url, restOperations, key, httpHeaders);
				if (!jsonResources.isEmpty()) {
					JSONObject resourcesLatestData = (JSONObject) jsonResources.get(0);
					sonarDetail.setTimestamp(SonarProcessorUtils.getTimestamp(resourcesLatestData, PROJECT_DATE));
					for (Object eventObj : (JSONArray) resourcesLatestData.get(PROJECT_EVENTS)) {
						JSONObject eventJson = (JSONObject) eventObj;

						setVersionToSonarDetails(sonarDetail, eventJson);
					}
				}

				for (Object metricObj : (JSONArray) resources.get(PROJECT_MSR)) {
					JSONObject codeMetrics = (JSONObject) metricObj;

					SonarMetric metric = getSonarMetric(codeMetrics);
					sonarDetail.getMetrics().add(metric);
				}

				return sonarDetail;
			}

		} catch (ParseException | RestClientException ex) {
			log.error("Unable to Parse Response for url: {}", url);
			log.error(ex.getMessage(), ex);
		}

		return null;
	}

	/**
	 * Provides code quality metrics.
	 *
	 * @param metricJson
	 *            the metrics as json
	 * @return the code quality metric
	 */
	private SonarMetric getSonarMetric(JSONObject metricJson) {
		SonarMetric metric = new SonarMetric(SonarProcessorUtils.convertToString(metricJson, PROJECT_METRIC));
		metric.setMetricValue(metricJson.get(PROJECT_MSR_VALUE));
		if (metric.getMetricName().equals("sqale_index")) {
			metric.setFormattedValue(SonarProcessorUtils
					.dateFormatter(SonarProcessorUtils.convertToString(metricJson, PROJECT_MSR_VALUE)));
		} else if (SonarProcessorUtils.convertToStringSafe(metricJson, PROJECT_MSR_VALUE).contains(DOT)) {
			metric.setFormattedValue(SonarProcessorUtils.convertToString(metricJson, PROJECT_MSR_VALUE) + "%");
		} else if (SonarProcessorUtils.convertToStringSafe(metricJson, PROJECT_MSR_VALUE).matches("\\d+")) {
			metric.setFormattedValue(String.format("%,d",
					Integer.parseInt(SonarProcessorUtils.convertToString(metricJson, PROJECT_MSR_VALUE))));
		} else {
			metric.setFormattedValue(SonarProcessorUtils.convertToString(metricJson, PROJECT_MSR_VALUE));
		}
		return metric;
	}

	/**
	 * Set version to Sonar data.
	 *
	 * @param sonarDetail
	 *            the sonar detail
	 * @param eventJson
	 *            the event json
	 */
	private void setVersionToSonarDetails(SonarDetails sonarDetail, JSONObject eventJson) {
		if (SonarProcessorUtils.convertToStringSafe(eventJson, "category").equals("VERSION")) {
			sonarDetail.setVersion(SonarProcessorUtils.convertToString(eventJson, PROJECT_NAME));
		}
	}

	/**
	 * Provides Past sonar data.
	 *
	 * @param project
	 *            the Sonar server connection details
	 * @param httpHeaders
	 *            the list of http header
	 * @param metrics
	 *            the metrics
	 * @return the list of code quality history
	 */
	@Override
	public List<SonarHistory> getPastSonarDetails(SonarProcessorItem project, HttpEntity<String> httpHeaders,
			String metrics) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

		String lastUpdated = DEFAULT_DATE;
		if (project.getUpdatedTime() != 0) {
			lastUpdated = sdf.format(new Date(project.getUpdatedTime()));
			log.info("FormattedTime: {}", lastUpdated);
		}
		log.info("Last UpdatedTime: {}", lastUpdated);

		String url = "";

		List<SonarHistory> codeList = new ArrayList<>();
		try {
			int pageIndex = 1;
			do {
				url = createHistoryUrl(project, metrics, lastUpdated, pageIndex);
				ResponseEntity<String> response = restOperations.exchange(url, HttpMethod.GET, httpHeaders,
						String.class);
				JSONParser jsonParser = new JSONParser();
				JSONObject jsonObject = (JSONObject) jsonParser.parse(response.getBody());
				if (jsonObject != null) {
					Gson gson = new Gson();
					Type listType = new TypeToken<ArrayList<SonarMeasureData>>() {
					}.getType();
					List<SonarMeasureData> qualityList = gson.fromJson(jsonObject.get(PROJECT_MSR).toString(),
							listType);
					if (CollectionUtils.isEmpty(qualityList)) {
						return codeList;
					}
					if (qualityList.get(0).getHistory().size() <= 1) {
						return codeList;
					}

					for (int singleHistory = 0; singleHistory < qualityList.get(0).getHistory()
							.size(); singleHistory++) {
						SonarHistory sonarHistory = new SonarHistory();
						sonarHistory.setKey(project.getKey());
						sonarHistory.setName(project.getProjectName());
						sonarHistory.setProcessorItemId(project.getId());
						sonarHistory.setBranch(project.getBranch());

						populateCodeQualityHistory(qualityList, singleHistory, sonarHistory);
						project.setTimestamp(sonarHistory.getTimestamp());
						codeList.add(sonarHistory);
					}
					pageIndex++;
				} else {
					break;
				}
			} while (pageIndex < 100);

		} catch (ParseException | RestClientException | NullPointerException ex) {
			log.error("Unable to Parse Response for url: {}", url);
			log.error(ex.getMessage(), ex);
		}

		return codeList;
	}

	private String createHistoryUrl(SonarProcessorItem project, String metrics, String lastUpdated, int pageIndex) {
		String url = "";
		if (!project.getToolDetailsMap().containsKey(BRANCH)) {
			url = String.format(new StringBuilder(project.getInstanceUrl()).append(MEASURE_HISTORY_ENDPOINT)
					.append(PAGE).toString(), project.getKey(), metrics, lastUpdated, pageIndex);
		} else {
			url = String.format(
					new StringBuilder(project.getInstanceUrl()).append(MEASURE_HISTORY_ENDPOINT).append(BRANCH_ENDPOINT)
							.append(PAGE).toString(),
					project.getKey(), metrics, lastUpdated, project.getBranch(), pageIndex);
		}
		return url;
	}

	/**
	 * Populates sonar history.
	 *
	 * @param qualityList
	 *            the list of Sonar measure data
	 * @param singleHistory
	 *            the single history
	 * @param sonarHistory
	 *            the sonar history
	 */
	private void populateCodeQualityHistory(List<SonarMeasureData> qualityList, int singleHistory,
			SonarHistory sonarHistory) {
		for (SonarMeasureData sonarMeasureData : qualityList) {
			SonarMetric metric = new SonarMetric(sonarMeasureData.getMetric());
			if (!CollectionUtils.isEmpty(sonarMeasureData.getHistory())
					&& sonarMeasureData.getHistory().size() > singleHistory) {
				metric.setMetricValue(sonarMeasureData.getHistory().get(singleHistory).getValue());
				sonarHistory
						.setDate(new DateTime(sonarMeasureData.getHistory().get(singleHistory).getDate()).getMillis());
				sonarHistory.setTimestamp(
						new DateTime(sonarMeasureData.getHistory().get(singleHistory).getDate()).getMillis());
			}
			sonarHistory.getMetrics().add(metric);
		}
	}

}
