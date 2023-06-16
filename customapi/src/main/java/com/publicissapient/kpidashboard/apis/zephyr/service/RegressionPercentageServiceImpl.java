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
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
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
import com.publicissapient.kpidashboard.common.model.zephyr.TestCaseDetails;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RegressionPercentageServiceImpl extends ZephyrKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String TESTCASEKEY = "testCaseData";
	private static final String AUTOMATED_TESTCASE_KEY = "automatedTestCaseData";
	private static final String AUTOMATED = "Regression test cases automated";
	private static final String TOTAL = "Total Regression test cases";
	@Autowired
	private CustomApiConfig customApiConfig;

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
		List<DataCount> trendValues = getTrendValues(kpiRequest, nodeWiseKPIValue,
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
		DateTimeFormatter parser = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS");
		Collections.sort(sprintLeafNodeList, (Node o1, Node o2) -> o1.getSprintFilter().getStartDate()
				.compareTo(o2.getSprintFilter().getStartDate()));
		Map<String, Object> testDataListMap = fetchKPIDataFromDb(sprintLeafNodeList, null, null, kpiRequest);
		Map<String, List<TestCaseDetails>> total = (Map<String, List<TestCaseDetails>>) testDataListMap
				.get(TESTCASEKEY);
		Map<String, List<TestCaseDetails>> automated = (Map<String, List<TestCaseDetails>>) testDataListMap
				.get(AUTOMATED_TESTCASE_KEY);
		List<KPIExcelData> excelData = new ArrayList<>();
		sprintLeafNodeList.forEach(node -> {
			String trendLineName = node.getProjectFilter().getName();

			Map<String, Object> howerMap = new LinkedHashMap<>();
			String basicProjectConfId = node.getProjectFilter().getBasicProjectConfigId().toString();
			List<TestCaseDetails> totalTest = total.get(basicProjectConfId);
			List<TestCaseDetails> automatedTest = automated.get(basicProjectConfId);
			// Automation Percentage
			double automationForCurrentLeaf = getKPI(totalTest, automatedTest);

			String sprintEndDate = node.getSprintFilter().getEndDate();
			double sprintWiseAutomation = 0;
			if (StringUtils.isNotEmpty(sprintEndDate) && CollectionUtils.isNotEmpty(totalTest)
					&& CollectionUtils.isNotEmpty(automatedTest)) {
				DateTime endDate = parser.parseDateTime(sprintEndDate);
				List<TestCaseDetails> sprintWiseTotalTest = totalTest.stream()
						.filter(test -> StringUtils.isNotBlank(test.getCreatedDate())
								&& parser.parseDateTime(test.getCreatedDate()).isBefore(endDate))
						.collect(Collectors.toList());
				List<TestCaseDetails> sprintWiseAutomatedTest = automatedTest.stream()
						.filter(test -> StringUtils.isNotBlank(test.getTestAutomatedDate())
								&& parser.parseDateTime(test.getTestAutomatedDate()).isBefore(endDate))
						.collect(Collectors.toList());
				setHowerMap(sprintWiseAutomatedTest, sprintWiseTotalTest, howerMap, AUTOMATED, TOTAL);
				populateExcelDataObject(requestTrackerId, excelData, node.getSprintFilter().getName(),
						sprintWiseAutomatedTest, sprintWiseTotalTest);
				sprintWiseAutomation = (double) Math
						.round((100.0 * sprintWiseAutomatedTest.size()) / (sprintWiseTotalTest.size()));
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
}