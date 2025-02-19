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

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import com.publicissapient.kpidashboard.common.constant.SonarAnalysisType;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.sonar.SonarDetails;
import com.publicissapient.kpidashboard.common.model.sonar.SonarHistory;
import com.publicissapient.kpidashboard.common.model.sonar.SonarMetric;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.common.util.RestOperationsFactory;
import com.publicissapient.kpidashboard.sonar.config.SonarConfig;
import com.publicissapient.kpidashboard.sonar.model.SonarProcessorItem;
import com.publicissapient.kpidashboard.sonar.processor.adapter.SonarClient;
import com.publicissapient.kpidashboard.sonar.util.SonarDashboardUrl;
import com.publicissapient.kpidashboard.sonar.util.SonarProcessorUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Provide SonarQube 8 Implementation. Tested with SonarQube 8.0 and SonarQube
 * 8.1
 *
 * @author vijkumar18
 */
@Component
@Slf4j
public class Sonar8Client implements SonarClient {

	private static final String RESOURCE_DETAILS_ENDPOINT = "/api/measures/component?format=json&component=%s&metricKeys=%s&includealerts=true";

	private final RestOperations restOperations;
	private final SonarConfig sonarConfig;

	private ToolCredentialProvider toolCredentialProvider;

	/**
	 * Instantiates a new Sonar 8 client.
	 *
	 * @param restOperationsFactory
	 *          the rest operations supplier
	 * @param sonarConfig
	 *          the sonar settings
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
	 *          the Sonar server connection details
	 * @return the list of Sonar project
	 */
	@Override
	public List<SonarProcessorItem> getSonarProjectList(ProcessorToolConnection sonarServer) {
		return SonarClientCommonUtils.getProcessorItemList(sonarServer, sonarConfig, toolCredentialProvider,
				restOperations);
	}

	/**
	 * Provides Current Sonar snapshot.
	 *
	 * @param project
	 *          the Sonar project setup properties
	 * @param httpHeaders
	 *          the list of http header
	 * @param metrics
	 *          the metrics
	 * @return the current sonar data
	 */
	@Override
	public SonarDetails getLatestSonarDetails(SonarProcessorItem project, HttpEntity<String> httpHeaders,
			String metrics) {
		String url;
		if (!project.getToolDetailsMap().containsKey(SonarClientCommonUtils.BRANCH)) {
			url = String.format(new StringBuilder(project.getInstanceUrl()).append(RESOURCE_DETAILS_ENDPOINT).toString(),
					project.getKey(), metrics);
			log.info("getting sonar details for url = {}", url);
		} else {
			url = String.format(
					new StringBuilder(project.getInstanceUrl()).append(RESOURCE_DETAILS_ENDPOINT)
							.append(SonarClientCommonUtils.BRANCH_ENDPOINT).toString(),
					project.getKey(), metrics, project.getBranch());
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
				sonarDetail.setName(SonarProcessorUtils.convertToString(resources, SonarClientCommonUtils.PROJECT_NAME));
				sonarDetail.setUrl(new SonarDashboardUrl(project.getInstanceUrl(),
						SonarProcessorUtils.convertToString(resources, SonarClientCommonUtils.PROJECT_KEY)).toString());
				sonarDetail.setBranch(project.getBranch());

				if (!project.getToolDetailsMap().containsKey(SonarClientCommonUtils.BRANCH)) {
					url = String.format(new StringBuilder(project.getInstanceUrl())
							.append(SonarClientCommonUtils.PROJECT_ANALYSES_ENDPOINT).toString(),
							SonarProcessorUtils.convertToString(resources, SonarClientCommonUtils.PROJECT_KEY));
				} else {
					url = String.format(
							new StringBuilder(project.getInstanceUrl()).append(SonarClientCommonUtils.PROJECT_ANALYSES_ENDPOINT)
									.append(SonarClientCommonUtils.BRANCH_ENDPOINT).toString(),
							SonarProcessorUtils.convertToString(resources, SonarClientCommonUtils.PROJECT_KEY), project.getBranch());
				}
				key = "analyses";
				JSONArray jsonResources = SonarProcessorUtils.parseData(url, restOperations, key, httpHeaders);
				if (!jsonResources.isEmpty()) {
					JSONObject resourcesLatestData = (JSONObject) jsonResources.get(0);
					sonarDetail
							.setTimestamp(SonarProcessorUtils.getTimestamp(resourcesLatestData, SonarClientCommonUtils.PROJECT_DATE));
					for (Object eventObj : (JSONArray) resourcesLatestData.get(SonarClientCommonUtils.PROJECT_EVENTS)) {
						JSONObject eventJson = (JSONObject) eventObj;

						SonarClientCommonUtils.setVersionToSonarDetails(sonarDetail, eventJson);
					}
				}

				for (Object metricObj : (JSONArray) resources.get(SonarClientCommonUtils.PROJECT_MSR)) {
					JSONObject codeMetrics = (JSONObject) metricObj;

					SonarMetric metric = SonarClientCommonUtils.getSonarMetric(codeMetrics);
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
	 * Provides Past sonar data.
	 *
	 * @param project
	 *          the Sonar server connection details
	 * @param httpHeaders
	 *          the list of http header
	 * @param metrics
	 *          the metrics
	 * @return the list of code quality history
	 */
	@Override
	public List<SonarHistory> getPastSonarDetails(SonarProcessorItem project, HttpEntity<String> httpHeaders,
			String metrics) {
		return SonarClientCommonUtils.getSonarHistories(project, httpHeaders, metrics, restOperations);
	}
}
