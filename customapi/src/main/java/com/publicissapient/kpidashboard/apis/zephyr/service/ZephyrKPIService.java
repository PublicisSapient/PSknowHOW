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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.common.repository.application.TestExecutionRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.ApplicationKPIService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.ToolsKPIService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.AggregationUtils;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.zephyr.TestCaseDetails;
import com.publicissapient.kpidashboard.common.repository.zephyr.TestCaseDetailsRepository;

/**
 * This is an abstract class at source(Zephyr) level. Any common implementation
 * for group of KPI's based on a source has to be implemented here.
 *
 * @param <R>
 *            type of kpi value
 * @param <S>
 *            type of kpi trend object
 * @param <T>
 *            type of db object
 *
 * @author tauakram
 */

@Service
public abstract class ZephyrKPIService<R, S, T> extends ToolsKPIService<R, S>
		implements ApplicationKPIService<R, S, T> {

	private static final String TOOL_ZEPHYR = ProcessorConstants.ZEPHYR;
	private static final String TOOL_JIRA_TEST = ProcessorConstants.JIRA_TEST;
	private static final String TESTCASEKEY = "testCaseData";
	private static final String NIN = "nin";
	private static final String AUTOMATED_TESTCASE_KEY = "automatedTestCaseData";
	private static final String DEV = "DeveloperKpi";
	public static final String SPRINT_ID = "sprintId";
	public static final String UPLOADED_DATA = "uploadedData";
	@Autowired
	private CacheService cacheService;
	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private TestCaseDetailsRepository testCaseDetailsRepository;
	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private FilterHelperService flterHelperService;
	@Autowired
	private TestExecutionRepository testExecutionRepository;

	public abstract String getQualifierType();

	/**
	 * Returns API Request tracker Id to be used for logging/debugging and using it
	 * for maintaining any sort of cache.
	 *
	 * @return request tracker id
	 */
	protected String getRequestTrackerId() {
		return cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.ZEPHYR.name());
	}

	/**
	 * Returns API Request tracker Id to be used for logging/debugging and using it
	 * for maintaining any sort of cache.
	 *
	 * @return request tracker id for kanban
	 */
	protected String getKanbanRequestTrackerId() {
		return cacheService
				.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.ZEPHYRKANBAN.name());
	}

	/**
	 * Gets Kpi data.
	 *
	 * @param kpiRequest
	 * @param kpiElement
	 * @param treeAggregatorDetail
	 * @return {@link KpiElement}
	 * @throws ApplicationException
	 */
	public abstract KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException;

	/**
	 * Creates condition map for additional filters
	 *
	 * @param kpiRequest
	 * @param mapOfFilters
	 */
	public void createAdditionalFilterMap(KpiRequest kpiRequest, Map<String, List<String>> mapOfFilters,
			String individualDevOrQa, FilterHelperService flterHelperService) {
		Map<String, AdditionalFilterCategory> addFilterCat = flterHelperService.getAdditionalFilterHierarchyLevel();
		Map<String, AdditionalFilterCategory> addFilterCategory = addFilterCat.entrySet().stream()
				.collect(Collectors.toMap(entry -> entry.getKey().toUpperCase(), Map.Entry::getValue));

		if (MapUtils.isNotEmpty(kpiRequest.getSelectedMap())) {
			for (Map.Entry<String, List<String>> entry : kpiRequest.getSelectedMap().entrySet()) {
				if (CollectionUtils.isNotEmpty(entry.getValue()) && null != addFilterCategory.get(entry.getKey())) {
					mapOfFilters.put(JiraFeature.ADDITIONAL_FILTERS_FILTERID.getFieldValueInFeature(),
							Arrays.asList(entry.getKey()));
					mapOfFilters.put(JiraFeature.ADDITIONAL_FILTERS_FILTERVALUES_VALUEID.getFieldValueInFeature(),
							entry.getValue());
				}
			}
		}
	}

	/**
	 * Aggregates values at leaf nodes.
	 *
	 * @param node
	 *            root node
	 * @param nodeWiseKPIValue
	 *            node value map
	 * @param kpiName
	 *            kpi name to get aggregation logic
	 * @return aggregated data
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Double> calculateAggValueForMapObject(Node node,
			Map<Pair<String, String>, Node> nodeWiseKPIValue, String kpiName) {

		if (node == null) {
			return new HashMap<>();
		}

		if (!(node.getValue() instanceof HashMap) && (int) node.getValue() == 0) {
			Map<String, Double> dataMap = new HashMap<>();
			dataMap.put("Default", 0.0d);
			node.setValue(dataMap);
		}

		List<Node> children = node.getChildren();
		if (CollectionUtils.isEmpty(children)) {
			nodeWiseKPIValue.put(Pair.of(node.getGroupName(), node.getId()), node);
			return (Map<String, Double>) node.getValue();
		}
		List<Map<String, Double>> aggregatedValueList = new ArrayList<>();
		for (Node child : children) {
			aggregatedValueList.add(calculateAggValueForMapObject(child, nodeWiseKPIValue, kpiName));
		}
		node.setValue(aggregateObject(aggregatedValueList, kpiName));
		nodeWiseKPIValue.put(Pair.of(node.getGroupName(), node.getId()), node);
		return (Map<String, Double>) node.getValue();

	}

	/**
	 * Returns aggregated value based on criteria selected.
	 *
	 * @param aggregatedValueList
	 * @param kpiName
	 * @return
	 */
	private Map<String, Double> aggregateObject(List<Map<String, Double>> aggregatedValueList, String kpiName) {

		Map<String, Double> resultMap = new HashMap<>();
		Map<String, List<Double>> aggMap = aggregatedValueList.stream().flatMap(m -> m.entrySet().stream()).collect(
				Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

		aggMap.forEach((key, value) -> {
			if (Constant.PERCENTILE.equalsIgnoreCase(configHelperService.calculateCriteria().get(kpiName))) {
				if (null == customApiConfig.getPercentileValue()) {
					resultMap.put(key, AggregationUtils.percentiles(value, 90.0D));
				} else {
					resultMap.put(key, AggregationUtils.percentiles(value, customApiConfig.getPercentileValue()));
				}
			} else if (Constant.MEDIAN.equalsIgnoreCase(configHelperService.calculateCriteria().get(kpiName))) {
				resultMap.put(key, AggregationUtils.median(value));
			} else if (Constant.AVERAGE.equalsIgnoreCase(configHelperService.calculateCriteria().get(kpiName))) {
				resultMap.put(key, AggregationUtils.average(value));
			} else if (Constant.SUM.equalsIgnoreCase(configHelperService.calculateCriteria().get(kpiName))) {
				resultMap.put(key, AggregationUtils.sum(value));
			}
		});
		resultMap.remove("Default");

		return resultMap;
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
	 * Returns regression kpi data
	 *
	 * @param leafNodeList
	 * @param
	 * @return Map of automated and all regression test cases
	 */
	public Map<String, Object> fetchRegressionKPIDataFromDb(List<Node> leafNodeList, boolean isKanban) {
		Map<String, Object> resultListMap = new HashMap<>();
		if (CollectionUtils.isNotEmpty(leafNodeList)) {
			Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
			Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
			List<String> basicProjectConfigIds = new ArrayList<>();
			Map<ObjectId, Map<String, List<ProjectToolConfig>>> toolMap = (Map<ObjectId, Map<String, List<ProjectToolConfig>>>) cacheService
					.cacheProjectToolConfigMapData();
			leafNodeList.forEach(leaf -> {
				ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
				Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
				basicProjectConfigIds.add(basicProjectConfigId.toString());

				List<ProjectToolConfig> zephyrTools = getToolConfigBasedOnProcessors(toolMap, basicProjectConfigId,
						TOOL_ZEPHYR);

				List<ProjectToolConfig> jiraTestTools = getToolConfigBasedOnProcessors(toolMap, basicProjectConfigId,
						TOOL_JIRA_TEST);

				List<String> regressionLabels = new ArrayList<>();
				List<String> regressionAutomationFolderPath = new ArrayList<>();
				// if Zephyr scale as a tool is setup with project
				if (CollectionUtils.isNotEmpty(zephyrTools)) {
					setZephyrScaleConfig(zephyrTools, regressionLabels, regressionAutomationFolderPath);
				}
				if (CollectionUtils.isNotEmpty(jiraTestTools)) {
					setZephyrSquadConfig(jiraTestTools, regressionLabels, mapOfProjectFilters);
				}

				if (CollectionUtils.isNotEmpty(regressionLabels)) {
					mapOfProjectFilters.put(JiraFeature.LABELS.getFieldValueInFeature(),
							CommonUtils.convertToPatternList(regressionLabels));
				}
				if (CollectionUtils.isNotEmpty(regressionAutomationFolderPath)) {
					mapOfProjectFilters.put(JiraFeature.ATM_TEST_FOLDER.getFieldValueInFeature(),
							CommonUtils.convertTestFolderToPatternList(regressionAutomationFolderPath));
				}

				uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

			});

			mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
					basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));
			mapOfFilters.put(JiraFeature.CAN_TEST_AUTOMATED.getFieldValueInFeature(),
					Arrays.asList(NormalizedJira.YES_VALUE.getValue()));
			mapOfFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
					Arrays.asList(NormalizedJira.TEST_TYPE.getValue()));

			List<TestCaseDetails> testCasesList = testCaseDetailsRepository.findTestDetails(mapOfFilters,
					uniqueProjectMap, NIN);

			Map<String, List<TestCaseDetails>> towerWiseTotalMap = testCasesList.stream()
					.collect(Collectors.groupingBy(TestCaseDetails::getBasicProjectConfigId, Collectors.toList()));

			Map<String, List<TestCaseDetails>> towerWiseAutomatedMap = testCasesList.stream()
					.filter(feature -> NormalizedJira.YES_VALUE.getValue().equals(feature.getIsTestAutomated()))
					.collect(Collectors.groupingBy(TestCaseDetails::getBasicProjectConfigId, Collectors.toList()));

			resultListMap.put(TESTCASEKEY, towerWiseTotalMap);
			resultListMap.put(AUTOMATED_TESTCASE_KEY, towerWiseAutomatedMap);
		}
		return resultListMap;
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

	/**
	 * fetch test execution uploaded data
	 * 
	 * @param leafNodeList
	 * @param kpiRequest
	 * @return
	 */
	public Map<String, Object> fetchTestExecutionUploadDataFromDb(List<Node> leafNodeList, KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		if (CollectionUtils.isNotEmpty(leafNodeList)) {
			Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
			List<String> sprintList = new ArrayList<>();
			List<String> basicProjectConfigIds = new ArrayList<>();
			Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();

			leafNodeList.forEach(leaf -> {
				ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
				sprintList.add(leaf.getSprintFilter().getId());
				basicProjectConfigIds.add(basicProjectConfigId.toString());
			});
			/** additional filter **/
			createAdditionalFilterMap(kpiRequest, mapOfFilters, DEV, flterHelperService);

			mapOfFilters.put(SPRINT_ID, sprintList.stream().distinct().collect(Collectors.toList()));
			mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
					basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

			resultListMap.put(UPLOADED_DATA,
					testExecutionRepository.findTestExecutionDetailByFilters(mapOfFilters, uniqueProjectMap));
		}
		return resultListMap;
	}

}
