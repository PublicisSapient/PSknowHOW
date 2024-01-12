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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import com.google.gson.Gson;
import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.sonar.SonarDetails;
import com.publicissapient.kpidashboard.common.model.sonar.SonarHistory;
import com.publicissapient.kpidashboard.common.model.sonar.SonarMeasureData;
import com.publicissapient.kpidashboard.common.model.sonar.SonarMetric;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.sonar.model.Paging;
import com.publicissapient.kpidashboard.sonar.model.SearchProjectsResponse;
import com.publicissapient.kpidashboard.sonar.model.SonarComponent;
import com.publicissapient.kpidashboard.sonar.model.SonarProcessorItem;
import com.publicissapient.kpidashboard.sonar.util.SonarProcessorUtils;
import com.publicissapient.kpidashboard.sonar.util.SonarUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for common clients methods.
 *
 */
@Slf4j
public class SonarClientCommonUtils {

	public static final String RESOURCE_ENDPOINT = "/api/components/search?qualifiers=TRK&p=%d&ps=%d";
	public static final String RESOURCE_ENDPOINT_CLOUD = "/api/components/search?qualifier=TRK&organization=%s&p=%d&ps=%d";
	public static final String PROJECT_ANALYSES_ENDPOINT = "/api/project_analyses/search?project=%s";
	public static final String MEASURE_HISTORY_ENDPOINT = "/api/measures/search_history?component=%s&metrics=%s&includealerts=true&from=%s";
	public static final String BRANCH_ENDPOINT = "&branch=%s";
	public static final String PAGE = "&p=%s";

	public static final String PROJECT_NAME = "name";
	public static final String PROJECT_KEY = "key";
	public static final String PROJECT_METRIC = "metric";
	public static final String PROJECT_MSR = "measures";
	public static final String PROJECT_MSR_VALUE = "value";
	public static final String PROJECT_DATE = "date";
	public static final String PROJECT_EVENTS = "events";
	public static final String DEFAULT_DATE = "2018-01-01";
	public static final String DOT = ".";
	public static final String BRANCH = "branch";

	private SonarClientCommonUtils() {

	}

	/**
	 * Provides Sonar Project .
	 *
	 * @param sonarServer
	 *            the Sonar server connection details
	 * @param toolCredentialProvider
	 * @param restOperations
	 * @return the list of Sonar Project
	 */
	public static SearchProjectsResponse getProjects(ProcessorToolConnection sonarServer, Paging paging,
			int nextPageIndex, ToolCredentialProvider toolCredentialProvider, RestOperations restOperations) {

		ToolCredential toolCredentials = SonarUtils.getToolCredentials(toolCredentialProvider, sonarServer);

		String baseUrl = sonarServer.getUrl() == null ? null : sonarServer.getUrl().trim();
		String username = toolCredentials.getUsername();
		String password = toolCredentials.getPassword();

		SearchProjectsResponse response;

		if (sonarServer.isCloudEnv()) {
			response = searchProjectsSonarCloud(baseUrl, password, nextPageIndex, paging.getPageSize(),
					sonarServer.getOrganizationKey(), restOperations);
		} else if (!sonarServer.isCloudEnv() && sonarServer.isAccessTokenEnabled()) {
			response = searchProjectsWithAccessToken(baseUrl, password, nextPageIndex, paging.getPageSize(),
					restOperations);
		} else {
			response = searchProjects(baseUrl, username, password, nextPageIndex, paging.getPageSize(), restOperations);
		}
		return response;
	}

	/*
	 * Rest call to get the projects of one page.
	 *
	 * @param baseUrl the base url
	 * 
	 * @param password the password
	 * 
	 * @param pageIndex the page index
	 * 
	 * @param pageSize the page size
	 * 
	 * @return SearchProjectsResponse containing projects and paging info
	 */
	private static SearchProjectsResponse searchProjectsWithAccessToken(String baseUrl, String password, int pageIndex,
			int pageSize, RestOperations restOperations) {

		String resUrl = String.format(RESOURCE_ENDPOINT, pageIndex, pageSize);
		String url = baseUrl + resUrl;

		HttpEntity<?> httpEntity = new HttpEntity<>(SonarProcessorUtils.getHeaders(password, true));

		return getSearchProjectsResponse(restOperations.exchange(url, HttpMethod.GET, httpEntity, String.class), url);

	}

	/**
	 * Rest call to get the projects of one page.
	 *
	 * @param baseUrl
	 *            the base url
	 * @param accessToken
	 * @param pageIndex
	 *            the page index
	 * @param pageSize
	 *            the page size
	 * @param restOperations
	 * @return SearchProjectsResponse containing projects and paging info
	 */
	private static SearchProjectsResponse searchProjectsSonarCloud(String baseUrl, String accessToken, int pageIndex,
			int pageSize, String organizationKey, RestOperations restOperations) {

		String resUrl = String.format(new StringBuilder(baseUrl).append(RESOURCE_ENDPOINT_CLOUD).toString(),
				organizationKey, pageIndex, pageSize);

		HttpEntity<?> httpEntity = new HttpEntity<>(SonarProcessorUtils.getHeaders(accessToken));

		return getSearchProjectsResponse(restOperations.exchange(resUrl, HttpMethod.GET, httpEntity, String.class),
				resUrl);

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
	 * @param restOperations
	 * @return SearchProjectsResponse containing projects and paging info
	 */
	private static SearchProjectsResponse searchProjects(String baseUrl, String username, String password,
			int pageIndex, int pageSize, RestOperations restOperations) {

		String resUrl = String.format(RESOURCE_ENDPOINT, pageIndex, pageSize);
		String url = baseUrl + resUrl;

		HttpEntity<?> httpEntity = new HttpEntity<>(SonarProcessorUtils.getHeaders(username, password));

		return getSearchProjectsResponse(restOperations.exchange(url, HttpMethod.GET, httpEntity, String.class), url);

	}

	@Nullable
	private static SearchProjectsResponse getSearchProjectsResponse(ResponseEntity<String> restOperations, String url) {
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
	 * Provides List of Sonar Project setup properties.
	 *
	 * @param response
	 *            the SearchProjectsResponse
	 * @param sonarServer
	 *            the Sonar server connection details
	 * @return the list of Sonar Project
	 */

	public static List<SonarProcessorItem> getProjectsListFromResponse(SearchProjectsResponse response,
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
	public static boolean hasNextPage(Paging paging) {
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
	private static int getTotalPages(Paging paging) {
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
	 * Populates sonar history.
	 *
	 * @param qualityList
	 *            the list of Sonar measure data
	 * @param singleHistory
	 *            the single history
	 * @param sonarHistory
	 *            the sonar history
	 */
	public static void populateCodeQualityHistory(List<SonarMeasureData> qualityList, int singleHistory,
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

	public static String createHistoryUrl(SonarProcessorItem project, String metrics, String lastUpdated,
			int pageIndex) {
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
	 * Provides code quality metrics.
	 *
	 * @param metricJson
	 *            the metrics as json
	 * @return the code quality metric
	 */
	public static SonarMetric getSonarMetric(JSONObject metricJson) {
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
	public static void setVersionToSonarDetails(SonarDetails sonarDetail, JSONObject eventJson) {
		if (SonarProcessorUtils.convertToStringSafe(eventJson, "category").equals("VERSION")) {
			sonarDetail.setVersion(SonarProcessorUtils.convertToString(eventJson, PROJECT_NAME));
		}
	}

}
