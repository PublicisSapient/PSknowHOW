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

package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.text.DecimalFormat;
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
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ValidationData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("javadoc")
@Slf4j
@Component
public class MissingWorkLogsServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	private static final String STORY_LIST = "Total Issues(excluding open and dropped)";
	private static final String TOTAL_STORY_LIST = "Total Issues(including open and dropped)";
	private static final String DEV = "DeveloperKpi";
	private static final String UNLOGGED_STORIES = "Issues Without Worklog";
	private static final int UNLOGGED = 0;
	private final DecimalFormat df2 = new DecimalFormat(".##");
	@Autowired
	private JiraIssueRepository jiraIssueRepository;
	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private FilterHelperService flterHelperService;

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		List<DataCount> trendValueList = new ArrayList<>();
		Node root = treeAggregatorDetail.getRoot();

		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			Filters filters = Filters.getFilter(k);
			if (Filters.SPRINT == filters) {
				sprintWiseLeafNodeValue(v, trendValueList, kpiElement, kpiRequest);
			}
		});

		log.debug("[MISSING-WORK-LOGS-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<String, List<DataCount>> trendAnalysisMap = trendValueList.stream()
				.collect(Collectors.groupingBy(DataCount::getSProjectName, Collectors.toList()));
		List<DataCount> dataList = new ArrayList<>();
		trendAnalysisMap.entrySet().stream().forEach(trend -> dataList.add(new DataCount(trend.getKey(), Lists.reverse(
				Lists.reverse(trend.getValue()).stream().limit(Constant.TREND_LIMIT).collect(Collectors.toList())))));
		kpiElement.setTrendValueList(dataList);

		if (null != trendValueList.get(0)) {
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

		return kpiElement;
	}

	@Override
	public Integer calculateKPIMetrics(Map<String, Object> subCategoryMap) {
		return null;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {

		Map<String, Object> resultListMap = new HashMap<>();
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		List<String> sprintList = new ArrayList<>();
		List<String> basicProjectConfigIds = new ArrayList<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		Map<String, Map<String, Object>> uniqueProjMap = new HashMap<>();
		Map<String, Map<String, Object>> uniqueProjectIssueTypeNotIn = new HashMap<>();
		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> uniqueProjectIssueStatusMap = new HashMap<>();
			Map<String, Object> mapOfProjFilters = new LinkedHashMap<>();
			sprintList.add(leaf.getSprintFilter().getId());
			basicProjectConfigIds.add(basicProjectConfigId.toString());

			List<String> ignoreStatusList = new ArrayList<>();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(leaf.getProjectFilter().getBasicProjectConfigId());
			if (null != fieldMapping) {
				if (Optional.ofNullable(fieldMapping.getJiraStoryIdentification()).isPresent()) {
					KpiDataHelper.prepareFieldMappingDefectTypeTransformation(mapOfProjFilters, fieldMapping.getJiradefecttype(),
							fieldMapping.getJiraStoryIdentification(), JiraFeature.ISSUE_TYPE.getFieldValueInFeature());
				}
				uniqueProjMap.put(basicProjectConfigId.toString(), mapOfProjFilters);
				ignoreStatusList.add(StringUtils.isEmpty(fieldMapping.getStoryFirstStatus()) ? ""
						: fieldMapping.getStoryFirstStatus());
				ignoreStatusList.addAll(
						CollectionUtils.isEmpty(fieldMapping.getJiraDefectDroppedStatus()) ? Lists.newArrayList()
								: fieldMapping.getJiraDefectDroppedStatus());
				uniqueProjectIssueStatusMap.put(JiraFeature.JIRA_ISSUE_STATUS.getFieldValueInFeature(),
						CommonUtils.convertToPatternList(ignoreStatusList));
				uniqueProjectIssueTypeNotIn.put(basicProjectConfigId.toString(), uniqueProjectIssueStatusMap);
			}
		});

		/** additional filter **/
		KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.SCRUM, DEV, flterHelperService);
		mapOfFilters.put(JiraFeature.SPRINT_ID.getFieldValueInFeature(),
				sprintList.stream().distinct().collect(Collectors.toList()));
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		resultListMap.put(TOTAL_STORY_LIST, jiraIssueRepository.findIssuesBySprintAndType(mapOfFilters, uniqueProjMap));
		resultListMap.put(STORY_LIST, jiraIssueRepository.findIssuesBySprintAndType(mapOfFilters, uniqueProjectMap,
				uniqueProjectIssueTypeNotIn));
		return resultListMap;
	}

	@Override
	public String getQualifierType() {
		return KPICode.MISSING_WORK_LOGS.name();
	}

	/**
	 * Populates KPI value to sprint leaf nodes and gives the trend analysis at
	 * sprint level.
	 * 
	 * @param sprintLeafNodeList
	 * @param trendValueList
	 * @param kpiElement
	 * @param kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void sprintWiseLeafNodeValue(List<Node> sprintLeafNodeList, List<DataCount> trendValueList,
			KpiElement kpiElement, KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();

		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));

		String startDate = sprintLeafNodeList.get(0).getSprintFilter().getStartDate();
		String endDate = sprintLeafNodeList.get(sprintLeafNodeList.size() - 1).getSprintFilter().getEndDate();
		Map<String, Object> resultMap = fetchKPIDataFromDb(sprintLeafNodeList, startDate, endDate, kpiRequest);

		List<JiraIssue> sprintWiseStoryList = (List<JiraIssue>) resultMap.get(STORY_LIST);
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseStoryMap = sprintWiseStoryList.stream().collect(Collectors
				.groupingBy(ji -> Pair.of(ji.getBasicProjectConfigId(), ji.getSprintID()), Collectors.toList()));

		List<JiraIssue> sprintWiseTotalStoryList = (List<JiraIssue>) resultMap.get(TOTAL_STORY_LIST);
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseTotalStoryMap = sprintWiseTotalStoryList.stream()
				.collect(Collectors.groupingBy(ji -> Pair.of(ji.getBasicProjectConfigId(), ji.getSprintID()),
						Collectors.toList()));

		Map<String, ValidationData> validationDataMap = new HashMap<>();

		for (Node node : sprintLeafNodeList) {
			// Leaf node wise data
			String trendLineName = node.getProjectFilter().getName();
			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), node.getSprintFilter().getId());

			List<JiraIssue> stories = sprintWiseStoryMap.get(currentNodeIdentifier);
			List<JiraIssue> totalStories = sprintWiseTotalStoryMap.get(currentNodeIdentifier);

			Double sumTotalStories = ((Integer) totalStories.size()).doubleValue();

			List<JiraIssue> totalStory = new ArrayList<>();
			List<JiraIssue> unloggedStory = new ArrayList<>();

			setSprintData(stories, totalStory, unloggedStory);

			Map<String, Object> howerMap = new LinkedHashMap<>();
			howerMap.put(STORY_LIST, totalStory.size());
			howerMap.put(UNLOGGED_STORIES, unloggedStory.size());
			howerMap.put(TOTAL_STORY_LIST, sumTotalStories.intValue());

			Double value = (double) (100 * ((Integer) howerMap.get(UNLOGGED_STORIES))) / (Integer) howerMap.get(STORY_LIST);

			if (CollectionUtils.isNotEmpty(totalStory)) {
				populateValidationDataObject(kpiElement, requestTrackerId, totalStory, validationDataMap,
						kpiRequest.getFilterToShowOnTrend(), node);
			}

			log.debug("[MISSING-WORK-LOGS-SPRINT-WISE][{}]. Total Stories Count for sprint {}  is {}", requestTrackerId,
					node.getSprintFilter().getName(), value);
			LocalDateTime localStartDate = LocalDateTime.parse(startDate);
			LocalDateTime localEndDate = LocalDateTime.parse(endDate);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
			String formatStartDate = localStartDate.format(formatter);
			String formatEndDate = localEndDate.format(formatter);

			DataCount dataCount = new DataCount();
			dataCount.setData(String.valueOf(value.intValue()));
			dataCount.setSProjectName(trendLineName);
			dataCount.setSSprintID(node.getSprintFilter().getId());
			dataCount.setSSprintName(node.getSprintFilter().getName());
			dataCount.setHoverValue(howerMap);
			dataCount.setValue(value);
			dataCount.setStartDate(formatStartDate);
			dataCount.setEndDate(formatEndDate);
			trendValueList.add(dataCount);

		}
	}

	/**
	 * Sets the information at sprint level.
	 * 
	 * @param stories
	 * @param totalStory
	 * @param unloggedStory
	 * 
	 */
	private void setSprintData(List<JiraIssue> stories, List<JiraIssue> totalStory, List<JiraIssue> unloggedStory) {
		if (CollectionUtils.isNotEmpty(stories)) {
			totalStory.addAll(stories.stream().filter(issue -> Double.parseDouble(issue.getEstimate()) > 0.0)
					.collect(Collectors.toList()));

			unloggedStory.addAll(totalStory.stream().filter(
					issue -> (issue.getTimeSpentInMinutes() == null || issue.getTimeSpentInMinutes() == UNLOGGED))
					.collect(Collectors.toList()));
		}
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
			List<String> loggedTime = new ArrayList<>();

			sprintWiseStoriesList.stream().forEach(jiraIssue -> {
				storyKeyList.add(jiraIssue.getNumber());
				Double daysLogged = 0.0d;
				if (jiraIssue.getTimeSpentInMinutes() != null) {
					daysLogged = Double.valueOf(jiraIssue.getTimeSpentInMinutes()) / 60;
				}
				loggedTime.add(df2.format(daysLogged));
			});

			ValidationData validationData = new ValidationData();
			validationData.setTotalStoryKeyList(storyKeyList);
			validationData.setLoggedTimeList(loggedTime);
			validationDataMap.put(keyForValidation, validationData);
			kpiElement.setMapOfSprintAndData(validationDataMap);
		}
	}

}
