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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.testexecution.TestExecution;
import com.publicissapient.kpidashboard.common.model.zephyr.TestCaseDetails;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RegressionPercentageServiceImpl extends ZephyrKPIService<Double, List<Object>, Map<String, Object>> {

	public static final DateTimeFormatter PARSER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS");
	private static final String TESTCASEKEY = "testCaseData";
	private static final String AUTOMATED_TESTCASE_KEY = "automatedTestCaseData";
	private static final String AUTOMATED = "Regression test cases automated";
	private static final String TOTAL = "Total Regression test cases";
	public static final String TEST_EXECUTION_FROM_UPLOAD = "uploadedData";
	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private KpiHelperService kpiHelperService;

	/**
	 * Gets Qualifier Type from KPICode enum
	 * 
	 * @return String type of <tt>REGRESSION_PASS_PERCENTAGE</tt> enum
	 */
	@Override
	public String getQualifierType() {
		return KPICode.REGRESSION_AUTOMATION_COVERAGE.toString();
	}

	/**
	 * Gets KPI Data
	 * 
	 * @param kpiRequest
	 * @param kpiElement
	 * @param treeAggregatorDetail
	 * @return KpiElement
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		List<DataCount> trendValueList = new ArrayList<>();
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.SPRINT) {
				leafNodeValueByTower(mapTmp, v, trendValueList, kpiElement, kpiRequest);
			}
		});

		log.debug("[TEST-AUTOMATION-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.REGRESSION_AUTOMATION_COVERAGE);
		// 3rd change : remove code to set trendValuelist and call getTrendValues method
		List<DataCount> trendValues = getTrendValues(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.REGRESSION_AUTOMATION_COVERAGE);
		kpiElement.setTrendValueList(trendValues);

		return kpiElement;
	}

	@Override
	public Double calculateKPIMetrics(Map<String, Object> filterComponentIdWiseDefectMap) {

		return null;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		return fetchRegressionKPIDataFromDb(leafNodeList, false);
	}

	/**
	 * 
	 * @param mapTmp
	 * @param sprintLeafNodeList
	 * @param trendValueList
	 * @param kpiElement
	 * @param kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void leafNodeValueByTower(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();
		Collections.sort(sprintLeafNodeList, (Node o1, Node o2) -> o1.getSprintFilter().getStartDate()
				.compareTo(o2.getSprintFilter().getStartDate()));

		// partitioning the sprintLeafNodeList by data upload enable
		Map<Boolean, List<Node>> nodePartitionedMap = sprintLeafNodeList.stream()
				.collect(Collectors.partitioningBy(leaf -> configHelperService.getFieldMappingMap()
						.get(leaf.getProjectFilter().getBasicProjectConfigId()).isUploadDataKPI42()));

		List<Node> uploadDataEnableNodes = nodePartitionedMap.get(true);
		List<Node> uploadDataDisableNodes = nodePartitionedMap.get(false);

		// flow 1 : fetching the uploaded data for uploadEnableNode
		Map<String, Object> uploadedDataMap = fetchTestExecutionUploadDataFromDb(uploadDataEnableNodes, kpiRequest);
		// Grouping of uploaded data by sprint
		Map<String, TestExecution> sprintWiseUploadedDataMap = createSprintWiseTestExecutionMap(
				(List<TestExecution>) uploadedDataMap.getOrDefault(TEST_EXECUTION_FROM_UPLOAD, new ArrayList<>()));
		// flow 2 : fetching the data from configured tool for uploadDisableNode
		Map<String, Object> testDataListMap = fetchKPIDataFromDb(uploadDataDisableNodes, null, null, kpiRequest);

		List<KPIExcelData> excelData = new ArrayList<>();
		sprintLeafNodeList.forEach(node -> {
			List<DataCount> resultList = new ArrayList<>();
			String sprintId = node.getSprintFilter().getId();
			// Leaf node wise data
			String trendLineName = node.getProjectFilter().getName();
			if (uploadDataEnableNodes.contains(node)) {
				// flow 1 : populating by uploaded data
				populatingForUploadedData(mapTmp, trendValueList, node, sprintWiseUploadedDataMap, sprintId, resultList,
						trendLineName);
			} else {
				// flow 2 : populating by configured tool
				populateForToolConfigured(mapTmp, trendValueList, node, testDataListMap, requestTrackerId, excelData,
						trendLineName);
			}
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.REGRESSION_AUTOMATION_COVERAGE.getColumns());
	}

	/**
	 * 
	 * @param totalTest
	 * @param automatedTest
	 * @return automatedPercentage
	 */
	private Double getKPI(List<TestCaseDetails> totalTest, List<TestCaseDetails> automatedTest) {
		Double automatedPercentage = 0d;
		if (CollectionUtils.isNotEmpty(automatedTest) && CollectionUtils.isNotEmpty(totalTest)) {
			int automatedCount = automatedTest.size();
			int totalCount = totalTest.size();
			automatedPercentage = (double) Math.round((100.0 * automatedCount) / (totalCount));
		}

		return automatedPercentage;
	}

	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData, String sprintName,
			List<TestCaseDetails> automatedTest, List<TestCaseDetails> totalTest) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {

			Map<String, TestCaseDetails> totalTestCaseMap = new HashMap<>();
			if (CollectionUtils.isNotEmpty(totalTest)) {
				totalTest.stream().forEach(test -> totalTestCaseMap.putIfAbsent(test.getNumber(), test));
			}

			KPIExcelUtility.populateRegressionAutomationExcelData(sprintName, totalTestCaseMap, automatedTest,
					excelData, KPICode.REGRESSION_AUTOMATION_COVERAGE.getKpiId(), "");

		}
	}

	/**
	 *
	 * @param sprintWiseTotal
	 * @param sprintWiseAutomated
	 * @param howerMap
	 */
	private void setHowerMap(List<TestCaseDetails> sprintWiseAutomated, List<TestCaseDetails> sprintWiseTotal,
			Map<String, Object> howerMap, String key1, String key2) {
		if (CollectionUtils.isNotEmpty(sprintWiseAutomated)) {
			howerMap.put(key1, sprintWiseAutomated.size());
		} else {
			howerMap.put(key1, 0);
		}
		if (CollectionUtils.isNotEmpty(sprintWiseTotal)) {
			howerMap.put(key2, sprintWiseTotal.size());
		} else {
			howerMap.put(key2, 0);
		}
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}

	/**
	 * populate by tool configured
	 * 
	 * @param mapTmp
	 * @param trendValueList
	 * @param node
	 * @param testDataListMap
	 * @param requestTrackerId
	 * @param excelData
	 * @param trendLineName
	 */
	@SuppressWarnings("unchecked")
	private void populateForToolConfigured(Map<String, Node> mapTmp, List<DataCount> trendValueList, Node node,
			Map<String, Object> testDataListMap, String requestTrackerId, List<KPIExcelData> excelData,
			String trendLineName) {
		Map<String, Object> howerMap = new LinkedHashMap<>();
		String basicProjectConfId = node.getProjectFilter().getBasicProjectConfigId().toString();
		Map<String, List<TestCaseDetails>> total = (Map<String, List<TestCaseDetails>>) testDataListMap
				.getOrDefault(TESTCASEKEY, new HashMap<>());
		Map<String, List<TestCaseDetails>> automated = (Map<String, List<TestCaseDetails>>) testDataListMap
				.getOrDefault(AUTOMATED_TESTCASE_KEY, new HashMap<>());
		List<TestCaseDetails> totalTest = total.get(basicProjectConfId);
		List<TestCaseDetails> automatedTest = automated.get(basicProjectConfId);
		// Automation Percentage
		double automationForCurrentLeaf = getKPI(totalTest, automatedTest);

		String sprintEndDate = node.getSprintFilter().getEndDate();
		double sprintWiseAutomation = 0;
		if (StringUtils.isNotEmpty(sprintEndDate) && CollectionUtils.isNotEmpty(totalTest)
				&& CollectionUtils.isNotEmpty(automatedTest)) {
			String formatDate = sprintEndDate.split("\\.")[0];
			DateTime endDate = DateUtil.stringToDateTime(formatDate, DateUtil.TIME_FORMAT);
			List<TestCaseDetails> sprintWiseTotalTest = totalTest.stream()
					.filter(test -> StringUtils.isNotBlank(test.getCreatedDate())
							&& PARSER.parseDateTime(test.getCreatedDate()).isBefore(endDate))
					.collect(Collectors.toList());
			List<TestCaseDetails> sprintWiseAutomatedTest = automatedTest.stream()
					.filter(test -> StringUtils.isNotBlank(test.getTestAutomatedDate())
							&& PARSER.parseDateTime(test.getTestAutomatedDate()).isBefore(endDate))
					.collect(Collectors.toList());
			setHowerMap(sprintWiseAutomatedTest, sprintWiseTotalTest, howerMap, AUTOMATED, TOTAL);
			populateExcelDataObject(requestTrackerId, excelData, node.getSprintFilter().getName(),
					sprintWiseAutomatedTest, sprintWiseTotalTest);
			sprintWiseAutomation = Math.round((100.0 * sprintWiseAutomatedTest.size()) / (sprintWiseTotalTest.size()));
		}

		log.debug("[REGRESSION-AUTOMATION-SPRINT-WISE][{}]. REGRESSION-AUTOMATION for sprint {}  is {}",
				requestTrackerId, node.getSprintFilter().getName(), automationForCurrentLeaf);

		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(sprintWiseAutomation));
		dataCount.setSProjectName(trendLineName);
		dataCount.setSSprintID(node.getSprintFilter().getId());
		dataCount.setSSprintName(node.getSprintFilter().getName());
		dataCount.setSprintIds(new ArrayList<>(Arrays.asList(node.getSprintFilter().getId())));
		dataCount.setSprintNames(new ArrayList<>(Arrays.asList(node.getSprintFilter().getName())));
		dataCount.setHoverValue(howerMap);
		dataCount.setValue(sprintWiseAutomation);
		mapTmp.get(node.getId()).setValue(new ArrayList<>(Arrays.asList(dataCount)));
		trendValueList.add(dataCount);
	}

	/**
	 * populate by uploaded data
	 * 
	 * @param mapTmp
	 * @param trendValueList
	 * @param node
	 * @param sprintWiseUploadedDataMap
	 * @param sprintId
	 * @param resultList
	 * @param trendLineName
	 */
	private void populatingForUploadedData(Map<String, Node> mapTmp, List<DataCount> trendValueList, Node node,
			Map<String, TestExecution> sprintWiseUploadedDataMap, String sprintId, List<DataCount> resultList,
			String trendLineName) {
		if (null != sprintWiseUploadedDataMap.get(sprintId)) {
			setSprintNodeValueRegression(sprintWiseUploadedDataMap.get(sprintId), resultList, trendLineName, node);
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
	}

	private void setSprintNodeValueRegression(TestExecution executionDetail, List<DataCount> trendValueList,
			String trendLineName, Node node) {

		// aggregated value of all sub-filters of a project for given sprint
		double regressionPerc = Math.round((100.0 * executionDetail.getAutomatedRegressionTestCases())
				/ executionDetail.getTotalRegressionTestCases());
		Map<String, Object> howerMap = new LinkedHashMap<>();
		howerMap.put(AUTOMATED, executionDetail.getAutomatedRegressionTestCases());
		howerMap.put(TOTAL, executionDetail.getTotalRegressionTestCases());
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(Math.round(regressionPerc)));
		dataCount.setSProjectName(trendLineName);
		dataCount.setSubFilter(trendLineName);
		dataCount.setValue(regressionPerc);
		dataCount.setHoverValue(howerMap);
		dataCount.setSSprintID(node.getSprintFilter().getId());
		dataCount.setSSprintName(node.getSprintFilter().getName());
		trendValueList.add(dataCount);
	}

	/**
	 * * Checking if data exist in that sprint & grouping it by sprint
	 *
	 * @param resultList
	 * @return
	 */
	public Map<String, TestExecution> createSprintWiseTestExecutionMap(List<TestExecution> resultList) {
		return resultList.stream()
				.filter(testExecution -> testExecution.getAutomatedRegressionTestCases() != null
						&& testExecution.getTotalRegressionTestCases() != null)
				.collect(Collectors.toMap(TestExecution::getSprintId, Function.identity()));
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI42(),
				KPICode.REGRESSION_AUTOMATION_COVERAGE.getKpiId());
	}
}