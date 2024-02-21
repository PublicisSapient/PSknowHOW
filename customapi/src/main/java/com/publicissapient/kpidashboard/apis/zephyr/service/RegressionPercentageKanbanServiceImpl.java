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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
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
import com.publicissapient.kpidashboard.common.model.zephyr.TestCaseDetails;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Qualifier("RegressionPercentageKanban")
@Slf4j
public class RegressionPercentageKanbanServiceImpl extends ZephyrKPIService<Double, List<Object>, Map<String, Object>> {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
	private static final String TESTCASEKEY = "testCaseData";
	private static final String AUTOMATED_TESTCASE_KEY = "automatedTestCaseData";
	private static final String AUTOMATED = "Regression test cases automated";
	private static final String TOTAL = "Total Regression test cases";
	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private KpiHelperService kpiHelperService;

	/**
	 *
	 */
	@Override
	public Double calculateKPIMetrics(Map<String, Object> filterComponentIdWiseDefectMap) {

		return null;
	}

	/**
	 * Gets Qualifier Type from KPICode enum
	 *
	 * @return String type of <tt>KANBAN_REGRESSION_PASS_PERCENTAGE</tt> enum
	 */
	@Override
	public String getQualifierType() {
		return KPICode.KANBAN_REGRESSION_PASS_PERCENTAGE.toString();
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

		log.info("[REGRESSION PASS PERCENTAGE-KANBAN-LEAF-NODE-VALUE][{}]", kpiRequest.getRequestTrackerId());
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes()
				.get(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT);

		dateWiseLeafNodeValue(mapTmp, projectList, kpiElement, kpiRequest);

		log.debug(
				"[REGRESSION PASS PERCENTAGE-KANBAN-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();

		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.KANBAN_REGRESSION_PASS_PERCENTAGE);
		List<DataCount> trendValues = getTrendValues(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.KANBAN_REGRESSION_PASS_PERCENTAGE);

		kpiElement.setTrendValueList(trendValues);

		kpiElement.setNodeWiseKPIValue(nodeWiseKPIValue);

		log.debug("[REGRESSION-AUTOMATION-KANBAN-AGGREGATED-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), root);
		return kpiElement;
	}

	/**
	 * Populates KPI value to sprint leaf nodes and gives the trend analysis at
	 * sprint wise.
	 *
	 * @param mapTmp
	 * @param leafNodeList
	 * @param kpiElement
	 * @param kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void dateWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> leafNodeList, KpiElement kpiElement,
			KpiRequest kpiRequest) {

		CustomDateRange dateRange = KpiDataHelper.getStartAndEndDate(kpiRequest);// cs

		String startDate = dateRange.getStartDate().format(DATE_FORMATTER);
		String endDate = dateRange.getEndDate().format(DATE_FORMATTER);

		Map<String, Object> resultMap = fetchKPIDataFromDb(leafNodeList, startDate, endDate, kpiRequest);

		kpiWithoutFilter(resultMap, mapTmp, leafNodeList, kpiElement, kpiRequest);

	}

	private void kpiWithoutFilter(Map<String, Object> projectWiseJiraIssue, Map<String, Node> mapTmp,
			List<Node> leafNodeList, KpiElement kpiElement, KpiRequest kpiRequest) {
		List<KPIExcelData> excelData = new ArrayList<>();
		String requestTrackerId = getKanbanRequestTrackerId();
		Map<String, List<TestCaseDetails>> total = (Map<String, List<TestCaseDetails>>) projectWiseJiraIssue
				.get(TESTCASEKEY);
		Map<String, List<TestCaseDetails>> automated = (Map<String, List<TestCaseDetails>>) projectWiseJiraIssue
				.get(AUTOMATED_TESTCASE_KEY);
		leafNodeList.forEach(node -> {

			String projectNodeId = node.getId();
			String projectName = projectNodeId.substring(0, projectNodeId.lastIndexOf(CommonConstant.UNDERSCORE));
			String basicProjectConfId = node.getProjectFilter().getBasicProjectConfigId().toString();

			List<TestCaseDetails> totalTest = total.get(basicProjectConfId);
			List<TestCaseDetails> automatedTest = automated.get(basicProjectConfId);

			if (CollectionUtils.isNotEmpty(automatedTest) || CollectionUtils.isNotEmpty(totalTest)) {
				LocalDate currentDate = LocalDate.now();
				List<DataCount> dc = new ArrayList<>();

				for (int i = 0; i < kpiRequest.getKanbanXaxisDataPoints(); i++) {
					Map<String, Object> hoverMap = new LinkedHashMap<>();
					// fetch date range based on period for which request came
					CustomDateRange dateRange = KpiDataHelper.getStartAndEndDateForDataFiltering(currentDate,
							kpiRequest.getDuration());

					List<TestCaseDetails> totalTestList = filterKanbanTotalDataBasedOnStartAndEndDate(totalTest,
							dateRange.getEndDate());

					List<TestCaseDetails> automatedTestList = filterKanbanAutomatedDataBasedOnStartAndEndDate(
							automatedTest, dateRange.getEndDate());

					setHoverMap(automatedTestList, totalTestList, hoverMap, AUTOMATED, TOTAL);

					double automation = (double) Math
							.round((100.0 * automatedTestList.size()) / (totalTestList.size()));

					String date = getRange(dateRange, kpiRequest);
					DataCount dcObj = getDataCountObject(automation, projectName, date, projectNodeId, hoverMap);
					dc.add(dcObj);

					populateExcelDataObject(requestTrackerId, excelData, totalTestList, automatedTestList, projectName,
							date);

					if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.WEEK)) {
						currentDate = currentDate.minusWeeks(1);
					} else if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.MONTH)) {
						currentDate = currentDate.minusMonths(1);
					} else {
						currentDate = currentDate.minusDays(1);
					}
				}
				mapTmp.get(node.getId()).setValue(dc);
			}
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.KANBAN_REGRESSION_PASS_PERCENTAGE.getColumns());
	}

	private DataCount getDataCountObject(double automation, String projectName, String date, String projectNodeId,
			Map<String, Object> hoverMap) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(automation));
		dataCount.setSProjectName(projectName);
		dataCount.setDate(date);
		dataCount.setSSprintID(projectNodeId);
		dataCount.setSSprintName(projectName);
		dataCount.setSprintIds(new ArrayList<>(Arrays.asList(projectNodeId)));
		dataCount.setSprintNames(new ArrayList<>(Arrays.asList(projectName)));
		dataCount.setHoverValue(hoverMap);
		dataCount.setValue(automation);
		return dataCount;
	}

	private String getRange(CustomDateRange dateRange, KpiRequest kpiRequest) {
		String range = null;
		if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.WEEK)) {
			range = DateUtil.dateTimeConverter(dateRange.getStartDate().toString(), DateUtil.DATE_FORMAT,
					DateUtil.DISPLAY_DATE_FORMAT) + " to "
					+ DateUtil.dateTimeConverter(dateRange.getEndDate().toString(), DateUtil.DATE_FORMAT,
							DateUtil.DISPLAY_DATE_FORMAT);
		} else if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.MONTH)) {
			range = dateRange.getStartDate().getMonth().toString();
		} else {
			range = dateRange.getStartDate().toString();
		}
		return range;
	}

	private List<TestCaseDetails> filterKanbanTotalDataBasedOnStartAndEndDate(List<TestCaseDetails> tests,
			LocalDate endDate) {
		Predicate<TestCaseDetails> predicate = issue -> LocalDateTime
				.parse(issue.getCreatedDate().split("\\.")[0], DATE_TIME_FORMATTER)
				.isBefore(endDate.atTime(23, 59, 59));
		List<TestCaseDetails> filteredTests = tests.stream().filter(predicate).collect(Collectors.toList());
		return filteredTests;
	}

	private List<TestCaseDetails> filterKanbanAutomatedDataBasedOnStartAndEndDate(List<TestCaseDetails> tests,
			LocalDate endDate) {
		Predicate<TestCaseDetails> predicate = issue -> StringUtils.isNotEmpty(issue.getTestAutomatedDate())
				&& LocalDateTime.parse(issue.getTestAutomatedDate().split("\\.")[0], DATE_TIME_FORMATTER)
						.isBefore(endDate.atTime(23, 59, 59));
		List<TestCaseDetails> filteredTests = Optional.ofNullable(tests).orElse(Collections.emptyList()).stream()
				.filter(predicate).collect(Collectors.toList());
		return filteredTests;
	}

	/**
	 * Fetches KPI data from DB
	 *
	 * @param leafNodeList
	 * @param startDate
	 * @param endDate
	 * @param kpiRequest
	 * @return resultListMap
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		return fetchRegressionKPIDataFromDb(leafNodeList, true);
	}

	/**
	 *
	 * @param mapTmp
	 * @param leafNodeList
	 * @param trendValueList
	 * @param kpiElement
	 * @param kpiRequest
	 */

	/**
	 * populates the validation data node of the KPI element.
	 *
	 *
	 * @param requestTrackerId
	 *
	 * @param totalTest
	 * @param automatedTest
	 * @param dateProjectKey
	 */
	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			List<TestCaseDetails> totalTest, List<TestCaseDetails> automatedTest, String dateProjectKey, String date) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {

			Map<String, TestCaseDetails> totalTestCaseMap = new HashMap<>();
			if (CollectionUtils.isNotEmpty(totalTest)) {
				totalTest.stream().forEach(test -> totalTestCaseMap.putIfAbsent(test.getNumber(), test));
			}

			KPIExcelUtility.populateRegressionAutomationExcelData(dateProjectKey, totalTestCaseMap, automatedTest,
					excelData, KPICode.KANBAN_REGRESSION_PASS_PERCENTAGE.getKpiId(), date);

		}
	}

	/**
	 * @param automated
	 * @param total
	 * @param hoverMap
	 */
	private void setHoverMap(List automated, List<TestCaseDetails> total, Map<String, Object> hoverMap, String key1,
			String key2) {
		if (CollectionUtils.isNotEmpty(automated)) {
			hoverMap.put(key1, automated.size());
		} else {
			hoverMap.put(key1, 0);
		}
		if (CollectionUtils.isNotEmpty(total)) {
			hoverMap.put(key2, total.size());
		} else {
			hoverMap.put(key2, 0);
		}
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}

}
