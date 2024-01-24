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

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.CustomDateRange;
import com.publicissapient.kpidashboard.apis.model.IterationKpiData;
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.zephyr.TestCaseDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.zephyr.TestCaseDetailsRepository;

@Component
public class IssuesWithoutStoryLinkImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	private static final String TESTCASES_WITHOUT_STORY_LINK = "Test Cases Without Story Link";
	private static final String DEFECTS_WITHOUT_STORY_LINK = "Defects Without Story Link";
	private static final String TOOL_ZEPHYR = ProcessorConstants.ZEPHYR;
	private static final String TOOL_JIRA_TEST = ProcessorConstants.JIRA_TEST;
	private static final String STORY_LIST = "stories";
	private static final String TOTAL_TEST_CASES = "Total Test Cases";
	private static final String TEST_WITHOUT_STORY_LIST = "Test Without Story list";
	private static final String TEST_WITHOUT_STORY_TEST_CASES = "Test Without Story Test Cases";
	private static final String DEFECTS_WITHOUT_STORY_LIST = "Defects Without Story List";
	private static final String DEFECTS_WITHOUT_STORY_DEFECTS_LIST = "Defects Without Story Defects List";
	private static final String NIN = "nin";
	private static final String DEFECT_LIST = "Total Defects";
	private static final String OVERALL = "Overall";
	private static final String PROJECT = "project";
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private CacheService cacheService;
	@Autowired
	private JiraIssueRepository jiraIssueRepository;
	@Autowired
	private TestCaseDetailsRepository testCaseDetailsRepository;

	@Autowired
	private KpiHelperService kpiHelperService;

	@Override
	public Integer calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		DataCount trendValue = new DataCount();
		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes().get(PROJECT);
		projectWiseLeafNodeValue(trendValue, projectList, kpiElement, kpiRequest);
		return kpiElement;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> resultMapForTestWithoutStory = fetchKPIDataFromDbForTestWithoutStory(leafNodeList);
		map.put(TEST_WITHOUT_STORY_LIST, resultMapForTestWithoutStory.get(STORY_LIST));
		map.put(TEST_WITHOUT_STORY_TEST_CASES, resultMapForTestWithoutStory.get(TOTAL_TEST_CASES));
		Map<String, Object> resultMapDefectsWithoutStoryLink = fetchKPIDataFromDbForDefectsWithoutStoryLink(
				leafNodeList);
		map.put(DEFECTS_WITHOUT_STORY_LIST, resultMapDefectsWithoutStoryLink.get(STORY_LIST));
		map.put(DEFECTS_WITHOUT_STORY_DEFECTS_LIST, resultMapDefectsWithoutStoryLink.get(DEFECT_LIST));
		return map;
	}

	public Map<String, Object> fetchKPIDataFromDbForTestWithoutStory(List<Node> leafNodeList) {

		Map<String, Object> resultListMap = new HashMap<>();
		Map<String, List<String>> mapOfFilters = Maps.newLinkedHashMap();
		List<String> basicProjectConfigIds = new ArrayList<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMapForStories = new HashMap<>();
		List<String> storyType = new ArrayList<>();
		Map<ObjectId, Map<String, List<ProjectToolConfig>>> toolMap = (Map<ObjectId, Map<String, List<ProjectToolConfig>>>) cacheService
				.cacheProjectToolConfigMapData();
		Map<ObjectId, FieldMapping> basicProjetWiseConfig = configHelperService.getFieldMappingMap();
		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			List<String> regressionLabels = new ArrayList<>();
			List<String> sprintAutomationFolderPath = new ArrayList<>();
			basicProjectConfigIds.add(basicProjectConfigId.toString());
			List<ProjectToolConfig> zephyrTools = getToolConfigBasedOnProcessors(toolMap, basicProjectConfigId,
					TOOL_ZEPHYR);

			List<ProjectToolConfig> jiraTestTools = getToolConfigBasedOnProcessors(toolMap, basicProjectConfigId,
					TOOL_JIRA_TEST);

			FieldMapping fieldMapping = basicProjetWiseConfig.get(basicProjectConfigId);
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
			Map<String, Object> mapOfStoriesFilter = new LinkedHashMap<>();

			if (CollectionUtils.isNotEmpty(zephyrTools)) {
				setZephyrScaleConfig(zephyrTools, regressionLabels, sprintAutomationFolderPath);
			}
			if (CollectionUtils.isNotEmpty(jiraTestTools)) {
				setZephyrSquadConfig(jiraTestTools, regressionLabels, mapOfProjectFilters);
			}
			mapOfProjectFilters.put(JiraFeature.LABELS.getFieldValueInFeature(), Arrays.asList("Regression"));
			if (CollectionUtils.isNotEmpty(regressionLabels)) {
				mapOfProjectFilters.put(JiraFeature.LABELS.getFieldValueInFeature(),
						CommonUtils.convertToPatternList(regressionLabels));
			}

			if (CollectionUtils.isNotEmpty(sprintAutomationFolderPath)) {
				mapOfProjectFilters.put(JiraFeature.ATM_TEST_FOLDER.getFieldValueInFeature(),
						CommonUtils.convertTestFolderToPatternList(sprintAutomationFolderPath));
			}

			if (MapUtils.isNotEmpty(mapOfProjectFilters)) {
				uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);
			}
			if (Optional.ofNullable(fieldMapping.getJiraStoryIdentificationKPI129()).isPresent()) {

				if (Optional.ofNullable(fieldMapping.getJiraStoryIdentificationKPI129()).isPresent()) {
					KpiDataHelper.prepareFieldMappingDefectTypeTransformation(mapOfStoriesFilter, fieldMapping.getJiradefecttype(),
							fieldMapping.getJiraStoryIdentificationKPI129(), JiraFeature.ISSUE_TYPE.getFieldValueInFeature());
				}

				uniqueProjectMapForStories.put(basicProjectConfigId.toString(), mapOfStoriesFilter);
			}
			storyType.addAll(fieldMapping.getJiraStoryIdentificationKPI129());
		});

		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		List<JiraIssue> storyList = jiraIssueRepository.findIssuesBySprintAndType(mapOfFilters,
				uniqueProjectMapForStories);
		List<String> storyIssueNumberList = storyList.stream().map(JiraIssue::getNumber).collect(Collectors.toList());
		resultListMap.put(STORY_LIST, storyIssueNumberList);

		mapOfFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
				Arrays.asList(NormalizedJira.TEST_TYPE.getValue()));
		resultListMap.put(TOTAL_TEST_CASES,
				testCaseDetailsRepository.findNonRegressionTestDetails(mapOfFilters, uniqueProjectMap, NIN));
		return resultListMap;
	}

	/**
	 * Returns list of Zephyr/Jira Test tool associated with project
	 *
	 * @param toolMap
	 * @param basicProjectConfId
	 * @return List of ProjectToolConfig
	 */

	public List<ProjectToolConfig> getToolConfigBasedOnProcessors(
			Map<ObjectId, Map<String, List<ProjectToolConfig>>> toolMap, ObjectId basicProjectConfId, String toolName) {
		List<ProjectToolConfig> tools = new ArrayList<>();
		if (MapUtils.isNotEmpty(toolMap) && toolMap.get(basicProjectConfId) != null
				&& toolMap.get(basicProjectConfId).containsKey(toolName)) {
			List<ProjectToolConfig> tool = toolMap.get(basicProjectConfId).get(toolName);
			tools.addAll(tool);
		}
		return tools;
	}

	/**
	 * @param tools
	 * @param regressionLabels
	 */
	public void setZephyrScaleConfig(List<ProjectToolConfig> tools, List<String> regressionLabels,
			List<String> automationFolderPath) {
		tools.forEach(tool -> {
			if (CollectionUtils.isNotEmpty(tool.getRegressionAutomationLabels())) {
				regressionLabels.addAll(tool.getRegressionAutomationLabels());
			}
			if (CollectionUtils.isNotEmpty(tool.getTestRegressionValue())) {
				regressionLabels.addAll(tool.getTestRegressionValue());
			}
			if (CollectionUtils.isNotEmpty(tool.getRegressionAutomationFolderPath())) {
				automationFolderPath.addAll(tool.getRegressionAutomationFolderPath());
			}
		});
	}

	/**
	 * @param jiraTestTools
	 * @param regressionLabels
	 * @param mapOfProjectFiltersNotIn
	 */
	public void setZephyrSquadConfig(List<ProjectToolConfig> jiraTestTools, List<String> regressionLabels,
			Map<String, Object> mapOfProjectFiltersNotIn) {
		jiraTestTools.forEach(tool -> {
			if (CollectionUtils.isNotEmpty(tool.getJiraRegressionTestValue())) {
				regressionLabels.addAll(tool.getJiraRegressionTestValue());
			}
			if (CollectionUtils.isNotEmpty(tool.getTestCaseStatus())) {
				mapOfProjectFiltersNotIn.put(JiraFeature.TEST_CASE_STATUS.getFieldValueInFeature(),
						CommonUtils.convertTestFolderToPatternList(tool.getTestCaseStatus()));
			}
		});
	}

	public Map<String, Object> fetchKPIDataFromDbForDefectsWithoutStoryLink(List<Node> leafNodeList) {

		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Object> resultListMap = new HashMap<>();
		List<String> basicProjectConfigIds = new ArrayList<>();
		List<String> defectType = new ArrayList<>();
		Map<String, Map<String, Object>> uniqueProjectIssueTypeNotIn = new HashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();

		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> uniqueProjectIssueStatusMap = new HashMap<>();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
			basicProjectConfigIds.add(basicProjectConfigId.toString());

			List<String> excludeStatusList = new ArrayList<>();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

			if (null != fieldMapping) {
				if (Optional.ofNullable(fieldMapping.getJiraStoryIdentificationKPI129()).isPresent()) {
					KpiDataHelper.prepareFieldMappingDefectTypeTransformation(mapOfProjectFilters, fieldMapping.getJiradefecttype(),
							fieldMapping.getJiraStoryIdentificationKPI129(), JiraFeature.ISSUE_TYPE.getFieldValueInFeature());
				}
				excludeStatusList.addAll(
						CollectionUtils.isEmpty(fieldMapping.getExcludeStatusKpi129()) ? Lists.newArrayList()
								: fieldMapping.getExcludeStatusKpi129());
				uniqueProjectIssueStatusMap.put(JiraFeature.JIRA_ISSUE_STATUS.getFieldValueInFeature(),
						CommonUtils.convertToPatternList(excludeStatusList));
				uniqueProjectIssueTypeNotIn.put(basicProjectConfigId.toString(), uniqueProjectIssueStatusMap);
				uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

			}
		});

		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));
		List<JiraIssue> jiraStoryList = jiraIssueRepository.findIssuesBySprintAndType(mapOfFilters, uniqueProjectMap);
		List<String> storyIssueNumberList = jiraStoryList.stream().map(JiraIssue::getNumber)
				.collect(Collectors.toList());

		resultListMap.put(STORY_LIST, storyIssueNumberList);
		defectType.add(NormalizedJira.DEFECT_TYPE.getValue());
		mapOfFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(), defectType);
		resultListMap.put(DEFECT_LIST,
				jiraIssueRepository.findDefectsWithoutStoryLink(mapOfFilters, uniqueProjectIssueTypeNotIn));
		return resultListMap;

	}

	@Override
	public String getQualifierType() {
		return KPICode.ISSUES_WITHOUT_STORY_LINK.name();
	}

	private void projectWiseLeafNodeValue(DataCount trendValue, List<Node> leafNodeList, KpiElement kpiElement,
			KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();
		List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
		List<KPIExcelData> excelDataForTestWithoutStory = new ArrayList<>();
		List<KPIExcelData> excelDataDefectsWithoutStoryLink = new ArrayList<>();
		List<IterationKpiModalValue> testCasesWithoutStoryLinkModals = new ArrayList<>();
		List<IterationKpiModalValue> defectWithoutStoryLinkModals = new ArrayList<>();
		CustomDateRange dateRange = KpiDataHelper.getMonthsForPastDataHistory(15);
		String startDate = dateRange.getStartDate().format(DATE_FORMATTER);
		String endDate = dateRange.getEndDate().format(DATE_FORMATTER);
		Node latestNode = leafNodeList.get(0);
		Map<String, Object> returnMap = fetchKPIDataFromDb(leafNodeList, startDate, endDate, kpiRequest);

		List<String> storiesInProject = (List<String>) returnMap.get(TEST_WITHOUT_STORY_LIST);
		List<TestCaseDetails> totalTestNonRegression = (List<TestCaseDetails>) returnMap
				.get(TEST_WITHOUT_STORY_TEST_CASES);
		List<TestCaseDetails> testWithoutStory = totalTestNonRegression.stream()
				.filter(t -> (t.getDefectStoryID() == null
						|| !CollectionUtils.containsAny(t.getDefectStoryID(), storiesInProject)))
				.collect(Collectors.toList());

		List<JiraIssue> totalDefects = checkPriority(
				(List<JiraIssue>) returnMap.get(DEFECTS_WITHOUT_STORY_DEFECTS_LIST));
		List<JiraIssue> totalStories = (List<JiraIssue>) returnMap.get(DEFECTS_WITHOUT_STORY_LIST);
		List<JiraIssue> defectWithoutStory = new ArrayList<>();
		defectWithoutStory.addAll(
				totalDefects.stream().filter(f -> !CollectionUtils.containsAny(f.getDefectStoryID(), totalStories))
						.collect(Collectors.toList()));
		if (CollectionUtils.isNotEmpty(totalTestNonRegression)) {
			populateExcelDataObject(requestTrackerId, totalTestNonRegression, testWithoutStory,
					latestNode.getProjectFilter().getName(), excelDataDefectsWithoutStoryLink);
		}
		kpiElement.setExcelData(excelDataForTestWithoutStory);
		kpiElement.setExcelColumns(KPIExcelColumn.TEST_WITHOUT_STORY_LINK.getColumns());

		if (CollectionUtils.isNotEmpty(defectWithoutStory)
				&& requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			KPIExcelUtility.populateDefectWithoutIssueLinkExcelData(defectWithoutStory,
					excelDataDefectsWithoutStoryLink, latestNode.getProjectFilter().getName());
		}

		kpiElement.setExcelData(excelDataDefectsWithoutStoryLink);
		kpiElement.setExcelColumns(KPIExcelColumn.DEFECTS_WITHOUT_STORY_LINK.getColumns());

		for (TestCaseDetails testCaseDetails : testWithoutStory) {
			populateIterationDataForTestWithoutStory(testCasesWithoutStoryLinkModals, testCaseDetails);
		}

		for (JiraIssue jiraIssue : defectWithoutStory) {
			populateIterationDataForDefectWithoutStory(defectWithoutStoryLinkModals, jiraIssue);
		}

		List<IterationKpiData> data = new ArrayList<>();
		IterationKpiData testCasesWithoutStoryLink = new IterationKpiData(TESTCASES_WITHOUT_STORY_LINK,
				(double) testWithoutStory.size(), (double) totalTestNonRegression.size(), null, "",
				testCasesWithoutStoryLinkModals);
		IterationKpiData defectWithoutStoryLink = new IterationKpiData(DEFECTS_WITHOUT_STORY_LINK,
				(double) defectWithoutStory.size(), (double) totalDefects.size(), null, "",
				defectWithoutStoryLinkModals);
		data.add(testCasesWithoutStoryLink);
		data.add(defectWithoutStoryLink);
		IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, OVERALL, data);
		iterationKpiValues.add(overAllIterationKpiValue);
		trendValue.setValue(iterationKpiValues);
		kpiElement.setModalHeads(KPIExcelColumn.ISSUES_WITHOUT_STORY_LINK.getColumns());
		kpiElement.setTrendValueList(trendValue);

	}

	private List<JiraIssue> checkPriority(List<JiraIssue> jiraIssues) {
		for (JiraIssue issue : jiraIssues) {
			if (StringUtils.isBlank(issue.getPriority())) {
				issue.setPriority(Constant.MISC);
			}
		}
		return jiraIssues;
	}

	private void populateExcelDataObject(String requestTrackerId, List<TestCaseDetails> totalTests,
			List<TestCaseDetails> testWithoutStory, String projectName, List<KPIExcelData> excelData) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			Map<String, TestCaseDetails> totalTestMap = new HashMap<>();
			totalTests.stream()
					.forEach(testCaseDetails -> totalTestMap.putIfAbsent(testCaseDetails.getNumber(), testCaseDetails));
			KPIExcelUtility.populateTestWithoutStoryExcelData(projectName, totalTestMap, testWithoutStory, excelData);
		}
	}
}
