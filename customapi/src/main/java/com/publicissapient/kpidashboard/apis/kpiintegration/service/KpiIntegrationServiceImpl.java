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

package com.publicissapient.kpidashboard.apis.kpiintegration.service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.jenkins.service.JenkinsServiceR;
import com.publicissapient.kpidashboard.apis.jira.service.NonTrendServiceFactory;
import com.publicissapient.kpidashboard.apis.model.ProjectWiseKpiRecommendation;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolKpiBulkMetricResponse;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.errors.EntityNotFoundException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraServiceR;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.sonar.service.SonarServiceR;
import com.publicissapient.kpidashboard.apis.zephyr.service.ZephyrService;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.KpiMaster;
import com.publicissapient.kpidashboard.common.repository.application.KpiMasterRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * @author kunkambl
 */
@Slf4j
@Service
public class KpiIntegrationServiceImpl {

	private static final List<String> FILTER_LIST = Arrays.asList("Final Scope (Story Points)", "Average Coverage",
			"Story Points", "Overall");
	private static final String KPI_SOURCE_JIRA = "Jira";
	private static final String KPI_SOURCE_SONAR = "Sonar";
	private static final String KPI_SOURCE_ZEPHYR = "Zypher";
	private static final String KPI_SOURCE_JENKINS = "Jenkins";
	private static final String SPRINT_CLOSED = "CLOSED";

	@Autowired
	KpiMasterRepository kpiMasterRepository;

	@Autowired
	private JiraServiceR jiraService;

	@Autowired
	private SonarServiceR sonarService;

	@Autowired
	private ZephyrService zephyrService;

	@Autowired
	private JenkinsServiceR jenkinsServiceR;

	@Autowired
	private HierarchyLevelService hierarchyLevelService;

	@Autowired
	NonTrendServiceFactory serviceFactory;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private RestTemplate restTemplate;

	/**
	 * get kpi element list with maturity assuming req for hierarchy level 4
	 *
	 * @param kpiRequest
	 * 		kpiRequest to fetch kpi data
	 * @return list of KpiElement
	 */
	public List<KpiElement> getKpiResponses(KpiRequest kpiRequest) {
		List<KpiMaster> kpiMasterList = kpiMasterRepository.findByKpiIdIn(kpiRequest.getKpiIdList());
		Map<String, List<KpiMaster>> sourceWiseKpiList = kpiMasterList.stream()
				.collect(Collectors.groupingBy(KpiMaster::getKpiSource));
		List<KpiElement> kpiElements = new ArrayList<>();
		setKpiRequest(kpiRequest);
		sourceWiseKpiList.forEach((source, kpiList) -> {
			try {
				kpiRequest.setKpiList(sourceWiseKpiList.get(source).stream().map(this::mapKpiMasterToKpiElement)
						.collect(Collectors.toList()));
				switch (source) {
				case KPI_SOURCE_JIRA:
					kpiElements.addAll(getJiraKpiMaturity(kpiRequest));
					break;
				case KPI_SOURCE_SONAR:
					kpiElements.addAll(getSonarKpiMaturity(kpiRequest));
					break;
				case KPI_SOURCE_ZEPHYR:
					kpiElements.addAll(getZephyrKpiMaturity(kpiRequest));
					break;
				case KPI_SOURCE_JENKINS:
					kpiElements.addAll(getJenkinsKpiMaturity(kpiRequest));
					break;
				default:
					log.error("Invalid Kpi");
				}
			} catch (Exception ex) {
				log.error("Error while fetching kpi maturity data", ex);
			}
		});
		kpiElements.forEach(kpiElement -> {
			List<?> trendValueList = (List<?>) kpiElement.getTrendValueList();
			if (CollectionUtils.isNotEmpty(trendValueList)) {
				if (trendValueList.get(0) instanceof DataCountGroup) {
					List<DataCountGroup> dataCountGroups = (List<DataCountGroup>) trendValueList;

					Optional<DataCount> firstMatchingDataCount = dataCountGroups.stream()
							.filter(trend -> FILTER_LIST.contains(trend.getFilter()) || (FILTER_LIST.contains(
									trend.getFilter1()) && FILTER_LIST.contains(trend.getFilter2())))
							.map(DataCountGroup::getValue).flatMap(List::stream).findFirst();

					firstMatchingDataCount.ifPresent(dataCount -> {
						kpiElement.setOverAllMaturityValue((String) dataCount.getMaturityValue());
						kpiElement.setOverallMaturity(dataCount.getMaturity());
					});
				} else {
					List<DataCount> dataCounts = (List<DataCount>) trendValueList;
					DataCount firstDataCount = dataCounts.get(0);

					kpiElement.setOverAllMaturityValue((String) firstDataCount.getMaturityValue());
					kpiElement.setOverallMaturity(firstDataCount.getMaturity());
				}
			}
		});

		return kpiElements;
	}

	/**
	 * set kpi request parameters as per the request
	 * @param kpiRequest received kpi request
	 */
	public void setKpiRequest(KpiRequest kpiRequest) {
		String[] hierarchyIdList = kpiRequest.getIds();
		Optional<HierarchyLevel> optionalHierarchyLevel = hierarchyLevelService.getFullHierarchyLevels(true).stream()
				.filter(hierarchyLevel -> hierarchyLevel.getLevel() == kpiRequest.getLevel()).findFirst();
		if (optionalHierarchyLevel.isPresent()) {
			HierarchyLevel hierarchyLevel = optionalHierarchyLevel.get();
			if (hierarchyIdList  == null) {
				hierarchyIdList = new String[] { kpiRequest.getHierarchyName().concat(Constant.UNDERSCORE).concat(
						hierarchyLevel.getHierarchyLevelId()) };
			}
			Map<String, List<String>> selectedMap = new HashMap<>();
			selectedMap.put(hierarchyLevel.getHierarchyLevelId(),
					Arrays.stream(hierarchyIdList).collect(Collectors.toList()));
			kpiRequest.setIds(hierarchyIdList);
			kpiRequest.setLabel(hierarchyLevel.getHierarchyLevelId());
			kpiRequest.setSelectedMap(selectedMap);
			kpiRequest.setSprintIncluded(Arrays.asList(SPRINT_CLOSED));
		}

	}

	/**
	 * get kpi data for source jira
	 *
	 * @param kpiRequest
	 * 		kpiRequest to fetch kpi data
	 * @return list of jira KpiElement
	 * @throws EntityNotFoundException
	 * 		entity not found exception for jira service method
	 */
	private List<KpiElement> getJiraKpiMaturity(KpiRequest kpiRequest) throws EntityNotFoundException {
		MDC.put("JiraScrumKpiRequest", kpiRequest.getRequestTrackerId());
		log.info("Received Jira KPI request {}", kpiRequest);
		long jiraRequestStartTime = System.currentTimeMillis();
		MDC.put("JiraRequestStartTime", String.valueOf(jiraRequestStartTime));
		HashSet<String> category = new HashSet<>();
		category.add(CommonConstant.ITERATION);
		category.add(CommonConstant.RELEASE);
		category.add(CommonConstant.BACKLOG);
		List<KpiElement> responseList;
		if (category.contains(kpiRequest.getKpiList().get(0).getKpiCategory())) {
			//when request coming from ITERATION/RELEASE/BACKLOG board
			responseList = serviceFactory.getService(kpiRequest.getKpiList().get(0).getKpiCategory())
						.processWithExposedApiToken(kpiRequest);
		} else {
			responseList = jiraService.processWithExposedApiToken(kpiRequest);
		}
		MDC.put("TotalJiraRequestTime", String.valueOf(System.currentTimeMillis() - jiraRequestStartTime));
		MDC.clear();
		return responseList;
	}

	/**
	 * get kpi data for source sonar
	 *
	 * @param kpiRequest
	 * 		kpiRequest to fetch kpi data
	 * @return list of sonar KpiElement
	 */
	private List<KpiElement> getSonarKpiMaturity(KpiRequest kpiRequest) {
		MDC.put("SonarKpiRequest", kpiRequest.getRequestTrackerId());
		log.info("Received Sonar KPI request {}", kpiRequest);
		long sonarRequestStartTime = System.currentTimeMillis();
		MDC.put("SonarRequestStartTime", String.valueOf(sonarRequestStartTime));
		List<KpiElement> responseList = sonarService.processWithExposedApiToken(kpiRequest);
		MDC.put("TotalSonarRequestTime", String.valueOf(System.currentTimeMillis() - sonarRequestStartTime));
		MDC.clear();
		return responseList;
	}

	/**
	 * get kpi data for source zephyr
	 *
	 * @param kpiRequest
	 * 		kpiRequest to fetch kpi data
	 * @return list of sonar KpiElement
	 * @throws EntityNotFoundException
	 * 		entity not found exception for zephyr service method
	 */
	private List<KpiElement> getZephyrKpiMaturity(KpiRequest kpiRequest) throws EntityNotFoundException {
		MDC.put("ZephyrKpiRequest", kpiRequest.getRequestTrackerId());
		log.info("Received Zephyr KPI request {}", kpiRequest);
		long zypherRequestStartTime = System.currentTimeMillis();
		MDC.put("ZephyrRequestStartTime", String.valueOf(zypherRequestStartTime));
		List<KpiElement> responseList = zephyrService.processWithExposedApiToken(kpiRequest);
		MDC.put("TotalZephyrRequestTime", String.valueOf(System.currentTimeMillis() - zypherRequestStartTime));
		MDC.clear();
		return responseList;
	}

	/**
	 * get kpi data for source jenkins
	 *
	 * @param kpiRequest
	 * 		kpiRequest to fetch kpi data
	 * @return list of sonar KpiElement
	 * @throws EntityNotFoundException
	 * 		entity not found exception for jenkins service method
	 */
	private List<KpiElement> getJenkinsKpiMaturity(KpiRequest kpiRequest) throws EntityNotFoundException {
		MDC.put("JenkinsKpiRequest", kpiRequest.getRequestTrackerId());
		log.info("Received Zephyr KPI request {}", kpiRequest);
		long jenkinsRequestStartTime = System.currentTimeMillis();
		MDC.put("JenkinsRequestStartTime", String.valueOf(jenkinsRequestStartTime));
		List<KpiElement> responseList = jenkinsServiceR.processWithExposedApiToken(kpiRequest);
		MDC.put("TotalJenkinsRequestTime", String.valueOf(System.currentTimeMillis() - jenkinsRequestStartTime));
		MDC.clear();
		return responseList;
	}

	/**
	 * Map KpiMaster object to KpiElement
	 *
	 * @param kpiMaster
	 * 		KpiMaster object fetched from db
	 * @return KpiElement
	 */
	public KpiElement mapKpiMasterToKpiElement(KpiMaster kpiMaster) {
		KpiElement kpiElement = new KpiElement();
		kpiElement.setKpiId(kpiMaster.getKpiId());
		kpiElement.setKpiName(kpiMaster.getKpiName());
		kpiElement.setIsDeleted(kpiMaster.getIsDeleted());
		kpiElement.setKpiCategory(kpiMaster.getKpiCategory());
		kpiElement.setKpiInAggregatedFeed(kpiMaster.getKpiInAggregatedFeed());
		kpiElement.setKpiOnDashboard(kpiMaster.getKpiOnDashboard());
		kpiElement.setKpiBaseLine(kpiMaster.getKpiBaseLine());
		kpiElement.setKpiUnit(kpiMaster.getKpiUnit());
		kpiElement.setIsTrendUpOnValIncrease(kpiMaster.getIsTrendUpOnValIncrease());
		kpiElement.setKanban(kpiMaster.getKanban());
		kpiElement.setKpiSource(kpiMaster.getKpiSource());
		kpiElement.setThresholdValue(kpiMaster.getThresholdValue());
		kpiElement.setAggregationType(kpiMaster.getAggregationCriteria());
		kpiElement.setMaturityRange(kpiMaster.getMaturityRange());
		kpiElement.setGroupId(kpiMaster.getGroupId());
		return kpiElement;
	}

	public ProjectWiseKpiRecommendation getProjectWiseKpiRecommendation(KpiRequest kpiRequest) {
		try {
			String recommendationUrl = String.format(customApiConfig.getRnrRecommendationUrl(),
					URLEncoder.encode(kpiRequest.getIds()[0], StandardCharsets.UTF_8),
					URLEncoder.encode(String.join(",", kpiRequest.getKpiIdList()), StandardCharsets.UTF_8));
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.set("X-Custom-Authentication", customApiConfig.getRnrRecommendationApiKey());
			httpHeaders.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
			ResponseEntity<ProjectWiseKpiRecommendation> response = restTemplate.exchange(URI.create(recommendationUrl),
					HttpMethod.GET, entity, ProjectWiseKpiRecommendation.class);
			return response.getBody();
		} catch (Exception ex) {
			log.error("Exception hitting recommendation api ", ex);
			return new ProjectWiseKpiRecommendation();
		}
	}


}
