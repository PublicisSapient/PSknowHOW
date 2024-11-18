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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.testexecution.TestExecution;
import com.publicissapient.kpidashboard.common.repository.application.TestExecutionRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TestExecutionServiceImpl extends ZephyrKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String QA = "QaKpi";
	private static final String SPRINT_ID = "sprintId";
	private static final String TEST_EXECUTION_DETAIL = "testExecutionDetail";
	private static final String TOTAL = "Total Test Cases";
	private static final String EXECUTED = "Executed Test Cases";
	private static final String PASSED = "Passed Test Cases";
	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private FilterHelperService flterHelperService;
	@Autowired
	private TestExecutionRepository testExecutionRepository;

	@SuppressWarnings("unchecked")
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		List<DataCount> trendValueList = new ArrayList<>();
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.SPRINT) {
				sprintWiseLeafNodeValue(mapTmp, v, trendValueList, kpiElement, kpiRequest);
			}

		});

		log.debug("[TEST-EXECUTION-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.TEST_EXECUTION_AND_PASS_PERCENTAGE);
		// 3rd change : remove code to set trendValuelist and call
		// getTrendValues method
		List<DataCount> trendValues = getTrendValues(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.TEST_EXECUTION_AND_PASS_PERCENTAGE);
		kpiElement.setTrendValueList(trendValues);

		return kpiElement;
	}

	@Override
	public Double calculateKPIMetrics(Map<String, Object> subCategoryMap) {
		return null;
	}

	@Override
	public String getQualifierType() {
		return KPICode.TEST_EXECUTION_AND_PASS_PERCENTAGE.name();
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {

		Map<String, Object> resultListMap = new HashMap<>();
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
		KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.SCRUM, QA, flterHelperService);

		mapOfFilters.put(SPRINT_ID, sprintList.stream().distinct().collect(Collectors.toList()));
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		resultListMap.put(TEST_EXECUTION_DETAIL,
				testExecutionRepository.findTestExecutionDetailByFilters(mapOfFilters, uniqueProjectMap));

		return resultListMap;
	}

	/**
	 * Calculate KPI value for selected sprint nodes.
	 * 
	 * @param mapTmp
	 *            key-value pair of node id aand node object
	 * @param sprintLeafNodeList
	 *            list of sprint leaf nodes
	 * @param trendValueList
	 *            list containing data to show on KPI
	 * @param kpiElement
	 *            kpiElement
	 * @param kpiRequest
	 *            KpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest) {

		log.info("[TEST-EXECUTION-AGGREGATED-VALUE][{}]. Aggregated Value at each level in the tree {}");
		Collections.sort(sprintLeafNodeList, (Node o1, Node o2) -> o1.getSprintFilter().getStartDate()
				.compareTo(o2.getSprintFilter().getStartDate()));
		String startDate = sprintLeafNodeList.get(0).getSprintFilter().getStartDate();
		String endDate = sprintLeafNodeList.get(sprintLeafNodeList.size() - 1).getSprintFilter().getEndDate();

		Map<String, Object> resultMap = fetchKPIDataFromDb(sprintLeafNodeList, startDate, endDate, kpiRequest);
		Map<String, TestExecution> sprintWiseDataMap = createSprintWiseTestExecutionMap(
				(List<TestExecution>) resultMap.get(TEST_EXECUTION_DETAIL));

		List<KPIExcelData> excelData = new ArrayList<>();
		sprintLeafNodeList.forEach(node -> {
			List<DataCount> resultList = new ArrayList<>();
			String sprintId = node.getSprintFilter().getId();
			String trendLineName = node.getProjectFilter().getName();

			if (null != sprintWiseDataMap.get(sprintId)) {
				setSprintNodeValue(sprintWiseDataMap.get(sprintId), resultList, trendLineName, node, excelData);
			} else {
				DataCount dataCount = new DataCount();
				dataCount.setSubFilter(Constant.EMPTY_STRING);
				dataCount.setSProjectName(trendLineName);
				dataCount.setValue(0.0);
				dataCount.setLineValue(0.0);
				dataCount.setHoverValue(new HashMap<>());
				dataCount.setSSprintID(node.getSprintFilter().getId());
				dataCount.setSSprintName(node.getSprintFilter().getName());
				resultList.add(dataCount);
				trendValueList.add(dataCount);
			}
			mapTmp.get(node.getId()).setValue(resultList);
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.TEST_EXECUTION_AND_PASS_PERCENTAGE.getColumns());

	}

	/**
	 * * Gets the KPI value for sprint node.
	 * 
	 * @param executionDetail
	 * @param trendValueList
	 * @param trendLineName
	 * @param node
	 * @param validationKey
	 * @param excelData
	 */
	private void setSprintNodeValue(TestExecution executionDetail, List<DataCount> trendValueList, String trendLineName,
			Node node, List<KPIExcelData> excelData) {

		// aggregated value of all sub-filters of a project for given sprint
		double executionPerc = Math
				.round((100.0 * executionDetail.getExecutedTestCase()) / executionDetail.getTotalTestCases());
		double passedPerc = Math
				.round((100.0 * executionDetail.getPassedTestCase()) / (executionDetail.getExecutedTestCase()));

		DataCount dataCount = new DataCount();
		dataCount.setSProjectName(trendLineName);
		dataCount.setSubFilter(trendLineName);
		dataCount.setValue(executionPerc);
		dataCount.setLineValue(passedPerc);
		dataCount.setHoverValue(getHoverValue(executionDetail));
		dataCount.setSSprintID(node.getSprintFilter().getId());
		dataCount.setSSprintName(node.getSprintFilter().getName());
		trendValueList.add(dataCount);

		if (getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			KPIExcelUtility.populateTestExcecutionExcelData(node.getSprintFilter().getName(), executionDetail, null,
					executionPerc, passedPerc, excelData);
		}

	}

	/**
	 * return map of data
	 * 
	 * @param detail
	 *            detail
	 * @return
	 */
	private Map<String, Object> getHoverValue(TestExecution detail) {
		Map<String, Object> aggData = new HashMap<>();
		aggData.put(TOTAL, detail.getTotalTestCases());
		aggData.put(EXECUTED, detail.getExecutedTestCase());
		aggData.put(PASSED, detail.getPassedTestCase());

		return aggData;
	}

	/**
	 * * Checking if data exist in that sprint & grouping it by sprint
	 *
	 * @param resultList
	 * @return
	 */
	public Map<String, TestExecution> createSprintWiseTestExecutionMap(List<TestExecution> resultList) {
		return resultList.stream()
				.filter(testExecution -> testExecution.getExecutedTestCase() != null
						&& testExecution.getTotalTestCases() != null && testExecution.getPassedTestCase() != null)
				.collect(Collectors.toMap(TestExecution::getSprintId, Function.identity()));
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI70(),
				KPICode.TEST_EXECUTION_AND_PASS_PERCENTAGE.getKpiId());
	}

}
