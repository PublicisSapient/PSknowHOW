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

package com.publicissapient.kpidashboard.apis.zephyr.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.zephyr.TestCaseDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.zephyr.TestCaseDetailsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Optional;


@Service
@Slf4j
public class TestWithoutStoryServiceImpl extends ZephyrKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String TEST_TITLE = "Test Cases without any story link";
	private static final String STORY_LIST = "stories";
	private static final String TOTAL_TEST_CASES = "Total Test Cases";
	@Autowired
	private JiraIssueRepository jiraIssueRepository;
	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private CacheService cacheService;
	@Autowired
	private TestCaseDetailsRepository testCaseDetailsRepository;

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
								 TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		List<DataCount> trendValueList = Lists.newArrayList();
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			Filters filters = Filters.getFilter(k);
			if (Filters.SPRINT == filters) {
				sprintWiseLeafNodeValue(v, trendValueList, kpiElement, kpiRequest);
			}
		});
		log.debug("[TEST-WITHOUT-STORY][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId());

		Map<String, List<DataCount>> trendAnalysisMap = trendValueList.stream()
				.collect(Collectors.groupingBy(DataCount::getSProjectName, Collectors.toList()));
		List<DataCount> dataList = new ArrayList<>();
		trendAnalysisMap.entrySet().stream().forEach(trend -> dataList.add(new DataCount(trend.getKey(), Lists.reverse(
				Lists.reverse(trend.getValue()).stream().limit(Constant.TREND_LIMIT).collect(Collectors.toList())))));
		kpiElement.setTrendValueList(dataList);

		log.debug("[TEST-WITHOUT-STORY][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId());
		return kpiElement;
	}

	@Override
	public Double calculateKPIMetrics(Map<String, Object> subCategoryMap) {
		return null;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
												  KpiRequest kpiRequest) {

		Map<String, Object> resultListMap = new HashMap<>();
		Map<String, List<String>> mapOfFilters = Maps.newLinkedHashMap();
		List<String> basicProjectConfigIds = new ArrayList<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMapForStories = new HashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMapNotIn = new HashMap<>();
		List<String> storyType = new ArrayList<>();
		Map<ObjectId, Map<String, List<ProjectToolConfig>>> toolMap = (Map<ObjectId, Map<String, List<ProjectToolConfig>>>) cacheService
				.cacheProjectToolConfigMapData();
		Map<ObjectId, FieldMapping> basicProjetWiseConfig = configHelperService.getFieldMappingMap();
		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			List<String> regressionLabels = new ArrayList<>();
			List<String> sprintAutomationFolderPath = new ArrayList<>();
			basicProjectConfigIds.add(basicProjectConfigId.toString());
			List<ProjectToolConfig> tools = getAllZephyrTool(toolMap, basicProjectConfigId);
			FieldMapping fieldMapping = basicProjetWiseConfig.get(basicProjectConfigId);
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
			Map<String, Object> mapOfStoriesFilter = new LinkedHashMap<>();
			Map<String, Object> mapOfProjectFiltersNotIn = new LinkedHashMap<>();

			if (CollectionUtils.isNotEmpty(tools)) {
				setZephyrScaleConfig(tools, regressionLabels, sprintAutomationFolderPath);
			} else {
				setZephyrSquadConfig(fieldMapping, regressionLabels, sprintAutomationFolderPath);
			}
			mapOfProjectFilters.put(JiraFeature.LABELS.getFieldValueInFeature(), Arrays.asList("Regression"));
			if (CollectionUtils.isNotEmpty(regressionLabels)) {
				mapOfProjectFilters.put(JiraFeature.LABELS.getFieldValueInFeature(),
						CommonUtils.convertToPatternList(regressionLabels));
			}

			if (CollectionUtils.isNotEmpty(fieldMapping.getTestCaseStatus())) {
				mapOfProjectFiltersNotIn.put(JiraFeature.TEST_CASE_STATUS.getFieldValueInFeature(),
						CommonUtils.convertTestFolderToPatternList(fieldMapping.getTestCaseStatus()));
			}
			if (MapUtils.isNotEmpty(mapOfProjectFiltersNotIn)) {
				uniqueProjectMapNotIn.put(basicProjectConfigId.toString(), mapOfProjectFiltersNotIn);
			}

			if (CollectionUtils.isNotEmpty(sprintAutomationFolderPath)) {
				mapOfProjectFilters.put(JiraFeature.ATM_TEST_FOLDER.getFieldValueInFeature(),
						CommonUtils.convertTestFolderToPatternList(sprintAutomationFolderPath));
			}

			if (MapUtils.isNotEmpty(mapOfProjectFilters)) {
				uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);
			}
			if (Optional.ofNullable(fieldMapping.getJiraStoryIdentification()).isPresent()) {
				mapOfStoriesFilter.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
						CommonUtils.convertToPatternList(fieldMapping.getJiraStoryIdentification()));
				uniqueProjectMapForStories.put(basicProjectConfigId.toString(), mapOfStoriesFilter);
			}
			storyType.addAll(fieldMapping.getJiraStoryIdentification());
		});

		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		List<JiraIssue> storyList = jiraIssueRepository.findIssuesBySprintAndType(mapOfFilters,
				uniqueProjectMapForStories);
		List<String> storyIssueNumberList = storyList.stream().map(JiraIssue::getNumber).collect(Collectors.toList());

		mapOfFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
				Arrays.asList(NormalizedJira.TEST_TYPE.getValue()));
		resultListMap.put(STORY_LIST, storyIssueNumberList);

		resultListMap.put(TOTAL_TEST_CASES,
				testCaseDetailsRepository.findNonRegressionTestDetails(mapOfFilters, uniqueProjectMap, uniqueProjectMapNotIn));
		return resultListMap;
	}

	@Override
	public String getQualifierType() {
		return KPICode.TEST_WITHOUT_STORY.name();
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
		List<Node> latestSprintNode = new ArrayList<>();
		List<KPIExcelData> excelData = new ArrayList<>();
		Node latestSprint = sprintLeafNodeList.get(0);
		Optional.ofNullable(latestSprint).ifPresent(latestSprintNode::add);
		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);
		List<String> storiesInProject = (List<String>) resultMap.get(STORY_LIST);
		List<TestCaseDetails> totalTestNonRegression = (List<TestCaseDetails>) resultMap.get(TOTAL_TEST_CASES);
		List<TestCaseDetails> testWithoutStory = totalTestNonRegression.stream()
				.filter(t -> (t.getDefectStoryID() == null
						|| !CollectionUtils.containsAny(t.getDefectStoryID(), storiesInProject)))
				.collect(Collectors.toList());
		Map<String, Integer> howerMap = new LinkedHashMap<>();
		howerMap.put(TOTAL_TEST_CASES, totalTestNonRegression.size());
		howerMap.put(TEST_TITLE, testWithoutStory.size());

		long value = testWithoutStory.size();

		if (CollectionUtils.isNotEmpty(totalTestNonRegression)) {
			populateExcelDataObject(requestTrackerId, totalTestNonRegression, testWithoutStory, latestSprint.getProjectFilter().getName(), excelData);
		}

		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.TEST_WITHOUT_STORY_LINK.getColumns());

		log.debug("[MISSING-WORK-LOGS-SPRINT-WISE][{}]. Total Stories Count for sprint {}  is {}", requestTrackerId,
				latestSprint.getProjectFilter().getName(), value);

		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(value));
		dataCount.setSProjectName(latestSprint.getProjectFilter().getName());
		dataCount.setHoverValue(howerMap);
		dataCount.setValue(value);
		trendValueList.add(dataCount);
		kpiElement.setValue(value);

	}


	private void populateExcelDataObject(String requestTrackerId, List<TestCaseDetails> totalTests,
										 List<TestCaseDetails> testWithoutStory, String projectName, List<KPIExcelData> excelData) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {

			Map<String, TestCaseDetails> totalTestMap = new HashMap<>();
			totalTests.stream()
					.forEach(testCaseDetails -> totalTestMap.putIfAbsent(testCaseDetails.getNumber(), testCaseDetails));

			KPIExcelUtility.populateTestWithoutStoryExcelData(projectName, totalTestMap, testWithoutStory,
					excelData);
		}

	}
}
