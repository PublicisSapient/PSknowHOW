/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ValidationData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anisingh4
 */

@Slf4j
@Component
public class StoriesWithoutEstimateImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	private static final String STORY_LIST = "Total Issues";
	private static final String STORIES_WITHOUT_ESTIMATE_KEY = "Issues Without Estimate";
	private static final String DEV = "DeveloperKpi";
	@Autowired
	private JiraIssueRepository jiraIssueRepository;
	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private FilterHelperService flterHelperService;

	/**
	 * Gets Qualifier Type
	 *
	 * @return KPICode's <tt>STORY_COUNT</tt> enum
	 */
	@Override
	public String getQualifierType() {
		return KPICode.STORIES_WITHOUT_ESTIMATE.name();
	}

	/**
	 * Gets KPI Data
	 *
	 * @param kpiRequest
	 * @param kpiElement
	 * @param treeAggregatorDetail
	 * @return KpiElement
	 * @throws ApplicationException
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		List<DataCount> trendValueList = new ArrayList<>();
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();

		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {

			Filters filters = Filters.getFilter(k);
			if (Filters.SPRINT == filters) {
				sprintWiseLeafNodeValue(mapTmp, v, trendValueList, kpiElement, kpiRequest);
			}

		});

		log.debug("[STORIES_WITHOUT_ESTIMATE-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<String, List<DataCount>> trendAnalysisMap = trendValueList.stream()
				.collect(Collectors.groupingBy(DataCount::getSProjectName, Collectors.toList()));
		List<DataCount> dataList = new ArrayList<>();
		trendAnalysisMap.entrySet().stream().forEach(trend -> dataList.add(new DataCount(trend.getKey(), Lists.reverse(
				Lists.reverse(trend.getValue()).stream().limit(Constant.TREND_LIMIT).collect(Collectors.toList())))));
		kpiElement.setTrendValueList(dataList);

		if (CollectionUtils.isNotEmpty(dataList)) {
			Map<String, Object> howerMap = trendValueList.get(0).getHoverValue();
			List<DataCount> dataCountList = new ArrayList<>();
			howerMap.forEach((k, v) -> {
				DataCount dataCount = new DataCount();
				dataCount.setData(k);
				dataCount.setCount((Integer) v);
				dataCountList.add(dataCount);

			});

			kpiElement.setValue(dataCountList);
		}

		log.debug("[STORIES_WITHOUT_ESTIMATE-AGGREGATED-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), root);
		return kpiElement;
	}

	/**
	 * Fetches KPI Data from DB
	 *
	 * @param leafNodeList
	 * @param startDate
	 * @param endDate
	 * @param kpiRequest
	 * @return {@code Map<String, Object>}
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {

		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Object> resultListMap = new HashMap<>();
		List<String> sprintList = new ArrayList<>();
		List<String> basicProjectConfigIds = new ArrayList<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
			sprintList.add(leaf.getSprintFilter().getId());
			basicProjectConfigIds.add(basicProjectConfigId.toString());

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			if (Optional.ofNullable(fieldMapping.getJiraStoryIdentification()).isPresent()) {
				KpiDataHelper.prepareFieldMappingDefectTypeTransformation(mapOfProjectFilters, fieldMapping.getJiradefecttype(),
						fieldMapping.getJiraStoryIdentification(), JiraFeature.ISSUE_TYPE.getFieldValueInFeature());
			}
			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

		});

		/** additional filter **/
		KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.SCRUM, DEV, flterHelperService);

		mapOfFilters.put(JiraFeature.SPRINT_ID.getFieldValueInFeature(),
				sprintList.stream().distinct().collect(Collectors.toList()));
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		List<JiraIssue> totalIssues = jiraIssueRepository.findIssuesBySprintAndType(mapOfFilters, uniqueProjectMap);
		resultListMap.put(STORY_LIST, totalIssues);
		return resultListMap;

	}

	private List<JiraIssue> findIssuesWithoutEstimate(List<JiraIssue> totalIssues) {
		List<JiraIssue> issuesWithoutEstimate = null;
		if (CollectionUtils.isNotEmpty(totalIssues)) {
			issuesWithoutEstimate = totalIssues.stream().filter(
					jiraIssue -> jiraIssue.getEstimate() == null || Double.valueOf(jiraIssue.getEstimate()).equals(0.0))
					.collect(Collectors.toList());
		}

		return issuesWithoutEstimate;
	}

	/**
	 * Calculates KPI Metrics
	 *
	 * @param subCategoryMap
	 * @return Integer
	 */
	@Override
	public Integer calculateKPIMetrics(Map<String, Object> subCategoryMap) {
		String requestTrackerId = getRequestTrackerId();

		log.debug("[STORIES_WITHOUT_ESTIMATE][{}]. Total Story Count: {}", requestTrackerId, subCategoryMap);
		return subCategoryMap == null ? 0 : subCategoryMap.size();
	}

	/**
	 * Populates KPI value to sprint leaf nodes andgives the trend analysis at
	 * sprint wise.
	 *
	 * @param mapTmp
	 * @param sprintLeafNodeList
	 * @param trendValueList
	 * @param kpiElement
	 * @param kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();

		String startDate;
		String endDate;

		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));

		startDate = sprintLeafNodeList.get(0).getSprintFilter().getStartDate();
		endDate = sprintLeafNodeList.get(sprintLeafNodeList.size() - 1).getSprintFilter().getEndDate();
		Map<String, Object> resultMap = fetchKPIDataFromDb(sprintLeafNodeList, startDate, endDate, kpiRequest);

		List<JiraIssue> sprintWiseStoryList = (List<JiraIssue>) resultMap.get(STORY_LIST);

		Map<Pair<String, String>, List<JiraIssue>> sprintWiseStoryMap = sprintWiseStoryList.stream().collect(Collectors
				.groupingBy(sws -> Pair.of(sws.getBasicProjectConfigId(), sws.getSprintID()), Collectors.toList()));

		Map<String, ValidationData> validationDataMap = new HashMap<>();

		for (Node node : sprintLeafNodeList) {
			// Leaf node wise data
			String trendLineName = node.getProjectFilter().getName();
			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), node.getSprintFilter().getId());

			List<JiraIssue> totalIssuesOfSprint = sprintWiseStoryMap.get(currentNodeIdentifier);

			List<JiraIssue> issuesWithoutEstimate = new ArrayList<>();

			if (CollectionUtils.isNotEmpty(totalIssuesOfSprint)) {
				issuesWithoutEstimate.addAll(findIssuesWithoutEstimate(totalIssuesOfSprint));
			}

			Map<String, Object> howerValues = new HashMap<>();
			howerValues.put(STORY_LIST, totalIssuesOfSprint.size());
			howerValues.put(STORIES_WITHOUT_ESTIMATE_KEY, issuesWithoutEstimate.size());

			double value = calculatePercentage(issuesWithoutEstimate.size(), totalIssuesOfSprint.size());

			mapTmp.get(node.getId()).setValue(value);

			if (CollectionUtils.isNotEmpty(totalIssuesOfSprint)) {
				populateValidationDataObject(kpiElement, requestTrackerId, totalIssuesOfSprint, validationDataMap,
						kpiRequest.getFilterToShowOnTrend(), node);
			}

			log.debug("[STORYCOUNT-SPRINT-WISE][{}]. Total Stories Count for sprint {}  is {}", requestTrackerId,
					node.getSprintFilter().getName(), value);

			DataCount dataCount = new DataCount();
			dataCount.setData(String.valueOf(Math.round(value)));
			dataCount.setSProjectName(trendLineName);
			dataCount.setSSprintID(node.getSprintFilter().getId());
			dataCount.setSSprintName(node.getSprintFilter().getName());
			dataCount.setStartDate(formatDate(startDate));
			dataCount.setEndDate(formatDate(endDate));
			dataCount.setHoverValue(howerValues);
			trendValueList.add(dataCount);

		}
	}

	private double calculatePercentage(double obtained, double total) {
		if (total == 0) {
			return 0;
		}
		return obtained * 100 / total;
	}

	private String formatDate(String dateStr) {
		if (StringUtils.isEmpty(dateStr)) {
			return dateStr;
		}
		LocalDateTime localDate = LocalDateTime.parse(dateStr);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
		return localDate.format(formatter);
	}

	/**
	 * Populates Validation Data Object
	 *
	 * @param kpiElement
	 * @param requestTrackerId
	 * @param sprintWiseStoriesList
	 * @param validationDataMap
	 * @param filterToShowOnTrend
	 * @param node
	 */
	private void populateValidationDataObject(KpiElement kpiElement, String requestTrackerId,
			List<JiraIssue> sprintWiseStoriesList, Map<String, ValidationData> validationDataMap,
			String filterToShowOnTrend, Node node) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			String keyForValidation = sprintWiseStoriesList.get(0).getSprintName();
			List<String> storyKeyList = new ArrayList<>();
			List<String> estimates = new ArrayList<>();

			sprintWiseStoriesList.stream().forEach(jiraIssue -> {
				storyKeyList.add(jiraIssue.getNumber());
				estimates.add(jiraIssue.getEstimate());
			});

			ValidationData validationData = new ValidationData();
			validationData.setStoryKeyList(storyKeyList);
			if (isEstimatesAreInStoryPoint(node)) {
				validationData.setStoryPointList(estimates);
			} else {
				validationData.setEstimateTimeList(estimates);
			}

			validationDataMap.put(keyForValidation, validationData);
			kpiElement.setMapOfSprintAndData(validationDataMap);
		}
	}

	private boolean isEstimatesAreInStoryPoint(Node node) {

		FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
				.get(node.getProjectFilter().getBasicProjectConfigId());
		return "Story Point".equals(fieldMapping.getEstimationCriteria());
	}

}
