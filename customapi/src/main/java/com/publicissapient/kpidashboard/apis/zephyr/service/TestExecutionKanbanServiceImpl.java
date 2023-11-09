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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.CustomDateRange;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.testexecution.KanbanTestExecution;
import com.publicissapient.kpidashboard.common.repository.application.KanbanTestExecutionRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for Test Execution percentage kpi for kanban.
 *
 * @author anisingh4
 */
@Service
@Slf4j
public class TestExecutionKanbanServiceImpl extends ZephyrKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String QA = "QaKpi";
	private static final String SUBGROUPCATEGORY = "subGroupCategory";
	private static final String TEST_EXECUTION_DETAIL = "testExecutionDetail";
	private static final String TOTAL = "Total Test Cases";
	private static final String EXECUTED = "Executed Test Cases";
	private static final String PASSED = "Passed Test Cases";
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private FilterHelperService flterHelperService;
	@Autowired
	private KanbanTestExecutionRepository kanbanTestExecutionRepository;

	@Override
	public String getQualifierType() {
		return KPICode.TEST_EXECUTION_KANBAN.name();
	}

	@SuppressWarnings("unchecked")
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes()
				.get(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT);

		dateWiseLeafNodeValue(mapTmp, projectList, kpiElement, kpiRequest);

		log.debug("[TEST-EXECUTION-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();

		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.TEST_EXECUTION_KANBAN);
		List<DataCount> trendValues = getTrendValues(kpiRequest, kpiElement, nodeWiseKPIValue, KPICode.TEST_EXECUTION_KANBAN);

		kpiElement.setTrendValueList(trendValues);
		kpiElement.setNodeWiseKPIValue(nodeWiseKPIValue);

		log.debug("[TEST-EXECUTION-LEAF-NODE-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), root);

		return kpiElement;
	}

	@Override
	public Double calculateKPIMetrics(Map<String, Object> objectMap) {
		return null;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		List<String> projectList = new ArrayList<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();

		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			projectList.add(basicProjectConfigId.toString());
		});
		/** additional filter **/
		String subGroupCategory = KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.KANBAN, QA,
				flterHelperService);

		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				projectList.stream().distinct().collect(Collectors.toList()));
		resultListMap.put(TEST_EXECUTION_DETAIL, kanbanTestExecutionRepository
				.findTestExecutionDetailByFilters(mapOfFilters, uniqueProjectMap, startDate, endDate));
		resultListMap.put(SUBGROUPCATEGORY, subGroupCategory);
		return resultListMap;
	}

	private void dateWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> leafNodeList, KpiElement kpiElement,
			KpiRequest kpiRequest) {

		CustomDateRange dateRange = KpiDataHelper.getStartAndEndDate(kpiRequest);

		String startDate = dateRange.getStartDate().format(DATE_FORMATTER);
		String endDate = dateRange.getEndDate().format(DATE_FORMATTER);

		Map<String, Object> resultMap = fetchKPIDataFromDb(leafNodeList, startDate, endDate, kpiRequest);

		Map<String, Map<String, KanbanTestExecution>> projectAndDateWiseCapacityMap = groupByProjectsAndDate(resultMap);

		kpiWithoutFilter(projectAndDateWiseCapacityMap, mapTmp, leafNodeList, kpiElement, kpiRequest);

	}

	private Map<String, Map<String, KanbanTestExecution>> groupByProjectsAndDate(Map<String, Object> dataFromDb) {
		List<KanbanTestExecution> testExecutionDetails = (List<KanbanTestExecution>) dataFromDb
				.get(TEST_EXECUTION_DETAIL);
		Map<String, List<KanbanTestExecution>> groupByProjects = CollectionUtils.emptyIfNull(testExecutionDetails)
				.stream().collect(Collectors.groupingBy(KanbanTestExecution::getBasicProjectConfigId));

		Map<String, Map<String, KanbanTestExecution>> resultMap = new HashMap<>();
		groupByProjects.forEach((project, testExecutions) -> resultMap.put(project, testExecutions.stream()
				.collect(Collectors.toMap(KanbanTestExecution::getExecutionDate, (testExecution -> testExecution)))));

		return resultMap;
	}

	private void kpiWithoutFilter(Map<String, Map<String, KanbanTestExecution>> projectWiseTestExecutions,
			Map<String, Node> mapTmp, List<Node> leafNodeList, KpiElement kpiElement, KpiRequest kpiRequest) {
		List<KPIExcelData> excelData = new ArrayList<>();
		String requestTrackerId = getKanbanRequestTrackerId();

		leafNodeList.forEach(node -> {

			String projectNodeId = node.getProjectFilter().getBasicProjectConfigId().toString();
			String projectName = node.getId().substring(0, node.getId().lastIndexOf(CommonConstant.UNDERSCORE));
			Map<String, KanbanTestExecution> existingTestExecutionsByDates = projectWiseTestExecutions
					.get(projectNodeId);

			if (MapUtils.isNotEmpty(existingTestExecutionsByDates)) {

				LocalDate currentDate = LocalDate.now();
				List<DataCount> dataCounts = new ArrayList<>();

				for (int i = 0; i < kpiRequest.getKanbanXaxisDataPoints(); i++) {
					// fetch date range based on period for which request came
					CustomDateRange dateRange = KpiDataHelper.getStartAndEndDateForDataFiltering(currentDate,
							kpiRequest.getDuration());

					String xAxisDataPointName = getXAxisDataPointName(dateRange, kpiRequest);
					// calculation based on your kpi
					Map<String, KanbanTestExecution> dataForTreadList = createDataForDateRange(projectName,
							existingTestExecutionsByDates, dateRange);

					Map<String, Integer> testExecutionAggregatedValuesForDateRange = aggregateValuesForDateRange(
							dataForTreadList);

					DataCount dcObj = getDataCountObject(projectName, xAxisDataPointName,
							testExecutionAggregatedValuesForDateRange);
					dataCounts.add(dcObj);

					populateValidationDataObject(projectName, requestTrackerId, dataForTreadList, excelData);

					if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.WEEK)) {
						currentDate = currentDate.minusWeeks(1);
					} else if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.MONTH)) {
						currentDate = currentDate.minusMonths(1);
					} else {
						currentDate = currentDate.minusDays(1);
					}
				}

				mapTmp.get(node.getId()).setValue(dataCounts);
			}
		});

		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.TEST_EXECUTION_KANBAN.getColumns());

	}

	private Map<String, Integer> aggregateValuesForDateRange(Map<String, KanbanTestExecution> dataForTreadList) {

		Map<String, Integer> resultMap = new HashMap<>();
		resultMap.put(TOTAL, 0);
		resultMap.put(EXECUTED, 0);
		resultMap.put(PASSED, 0);

		dataForTreadList.forEach((date, testExecution) -> {
			resultMap.put(TOTAL, resultMap.get(TOTAL) + testExecution.getTotalTestCases());
			resultMap.put(EXECUTED, resultMap.get(EXECUTED) + testExecution.getExecutedTestCase());
			resultMap.put(PASSED, resultMap.get(PASSED) + testExecution.getPassedTestCase());
		});

		return resultMap;

	}

	private Map<String, KanbanTestExecution> createDataForDateRange(String projectName,
			Map<String, KanbanTestExecution> existingTestExecutionsByDate, CustomDateRange dateRange) {

		Map<String, KanbanTestExecution> resultMap = new HashMap<>();
		LocalDate currentDate = dateRange.getStartDate();
		while (DateUtil.isWithinDateRange(currentDate, dateRange.getStartDate(), dateRange.getEndDate())) {
			String formattedCurrentDate = DateUtil.localDateTimeConverter(currentDate);
			resultMap.put(formattedCurrentDate, existingTestExecutionsByDate.getOrDefault(currentDate.toString(),
					emptyKanbanTestExecution(projectName, currentDate.toString())));
			currentDate = currentDate.plusDays(1);
		}

		return resultMap;
	}

	private KanbanTestExecution emptyKanbanTestExecution(String projectName, String date) {
		KanbanTestExecution testExecution = new KanbanTestExecution();
		testExecution.setExecutionDate(date);
		testExecution.setPassedTestCase(0);
		testExecution.setTotalTestCases(0);
		testExecution.setExecutedTestCase(0);
		testExecution.setProjectName(projectName);

		return testExecution;
	}

	private String getXAxisDataPointName(CustomDateRange dateRange, KpiRequest kpiRequest) {
		String range = null;
		if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.WEEK)) {
			range = DateUtil.localDateTimeConverter(dateRange.getStartDate()) + " to "
					+ DateUtil.localDateTimeConverter(dateRange.getEndDate());
		} else if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.MONTH)) {
			range = dateRange.getStartDate().getMonth().toString();
		} else {
			range = dateRange.getStartDate().toString();
		}
		return range;
	}

	private DataCount getDataCountObject(String projectName, String date,
			Map<String, Integer> testExecutionAggregatedValuesForDateRange) {
		int total = testExecutionAggregatedValuesForDateRange.get(TOTAL);
		int executed = testExecutionAggregatedValuesForDateRange.get(EXECUTED);
		int passed = testExecutionAggregatedValuesForDateRange.get(PASSED);
		double executionPerc = calculatePercentage(executed, total);
		double passedPerc = calculatePercentage(passed, total);

		DataCount dataCount = new DataCount();
		dataCount.setData("" + executionPerc);
		dataCount.setValue(executionPerc);
		dataCount.setLineValue(passedPerc);
		dataCount.setSProjectName(projectName);
		dataCount.setSSprintID(date);
		dataCount.setSSprintName(date);
		dataCount.setDate(date);
		dataCount.setHoverValue(getHoverValue(total, executed, passed));
		return dataCount;
	}

	private long calculatePercentage(double executed, double total) {
		if (total == 0) {
			return 0;
		}
		return Math.round((100.0 * executed) / total);
	}

	private Map<String, Object> getHoverValue(int total, int executed, int passed) {
		Map<String, Object> hoverData = new HashMap<>();
		hoverData.put(TOTAL, total);
		hoverData.put(EXECUTED, executed);
		hoverData.put(PASSED, passed);

		return hoverData;
	}

	private void populateValidationDataObject(String projectName, String requestTrackerId,
			Map<String, KanbanTestExecution> dataForTreadList, List<KPIExcelData> excelData) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {

			dataForTreadList.forEach((date, testExecution) -> {
				double executionPerc = Math
						.round((100.0 * testExecution.getExecutedTestCase()) / testExecution.getTotalTestCases());
				double passedPerc = Math
						.round((100.0 * testExecution.getPassedTestCase()) / (testExecution.getExecutedTestCase()));
				KPIExcelUtility.populateTestExcecutionExcelData(projectName, null, testExecution, executionPerc,
						passedPerc, excelData);

			});

		}
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}
}
