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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
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
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("javadoc")
@Service
@Slf4j
public class CostOfDelayServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String COD_DATA = "costOfDelayData";
	@Autowired
	private JiraIssueRepository jiraIssueRepository;
	@Autowired
	private CustomApiConfig customApiConfig;

	@Override
	public Double calculateKPIMetrics(Map<String, Object> subCategoryMap) {
		return null;
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		List<DataCount> trendValueList = new ArrayList<>();
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		treeAggregatorDetail.getMapOfListOfProjectNodes().forEach((k, v) -> {

			Filters filters = Filters.getFilter(k);
			if (Filters.PROJECT == filters) {
				projectWiseLeafNodeValue(mapTmp, v, trendValueList, kpiElement, getRequestTrackerId(), kpiRequest);
			}

		});

		log.debug("[PROJECT-WISE][{}]. Values of leaf node after KPI calculation {}", kpiRequest.getRequestTrackerId(),
				root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.COST_OF_DELAY);
		// 3rd change : remove code to set trendValuelist and call
		// getTrendValues method
		List<DataCount> trendValues = getTrendValues(kpiRequest, kpiElement, nodeWiseKPIValue, KPICode.COST_OF_DELAY);
		kpiElement.setTrendValueList(trendValues);

		return kpiElement;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {

		Map<String, Object> resultListMap = new HashMap<>();
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		List<String> basicProjectConfigIds = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(leafNodeList)) {

			leafNodeList.forEach(
					leaf -> basicProjectConfigIds.add(leaf.getProjectFilter().getBasicProjectConfigId().toString()));
		}

		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		mapOfFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
				Arrays.asList(NormalizedJira.ISSUE_TYPE.getValue()));
		mapOfFilters.put(JiraFeature.STATUS.getFieldValueInFeature(), Arrays.asList(NormalizedJira.STATUS.getValue()));
		List<JiraIssue> codList = jiraIssueRepository.findCostOfDelayByType(mapOfFilters);
		resultListMap.put(COD_DATA, codList);
		return resultListMap;
	}

	@Override
	public String getQualifierType() {
		return KPICode.COST_OF_DELAY.name();
	}

	/**
	 * Calculate KPI value for selected project nodes.
	 *
	 * @param projectLeafNodeList
	 *            list of sprint leaf nodes
	 * @param trendValueList
	 *            list containing data to show on KPI
	 * @param kpiElement
	 *            kpiElement
	 * @param kpiRequest
	 *            KpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> projectLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, String requestTrackerId, KpiRequest kpiRequest) {

		Map<String, Object> resultMap = fetchKPIDataFromDb(projectLeafNodeList, null, null, kpiRequest);
		Map<String, List<JiraIssue>> filterWiseDataMap = createProjectWiseDelay(
				(List<JiraIssue>) resultMap.get(COD_DATA));
		List<KPIExcelData> excelData = new ArrayList<>();

		projectLeafNodeList.forEach(node -> {
			String currentProjectId = node.getProjectFilter().getBasicProjectConfigId().toString();
			List<JiraIssue> delayDetail = filterWiseDataMap.get(currentProjectId);
			if (CollectionUtils.isNotEmpty(delayDetail)) {
				setProjectNodeValue(mapTmp, node, delayDetail, trendValueList, requestTrackerId, excelData);
			}

		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.COST_OF_DELAY.getColumns());
	}

	/**
	 * Gets the KPI value for project node.
	 *
	 * @param kpiElement
	 * @param jiraIssues
	 * @param trendValueList
	 * @return
	 */
	private void setProjectNodeValue(Map<String, Node> mapTmp, Node node, List<JiraIssue> jiraIssues,
			List<DataCount> trendValueList, String requestTrackerId, List<KPIExcelData> excelData) {
		Map<String, Double> lastNMonthMap = getLastNMonth(customApiConfig.getJiraXaxisMonthCount());
		String projectName = node.getProjectFilter().getName();
		List<JiraIssue> epicList = new ArrayList<>();
		Map<String, Map<String, Integer>> howerMap = new HashMap<>();

		for (JiraIssue js : jiraIssues) {
			String number = js.getNumber();
			String dateTime = js.getChangeDate() == null ? js.getUpdateDate() : js.getChangeDate();
			if (dateTime != null) {
				DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern(DateUtil.TIME_FORMAT)
						.optionalStart().appendPattern(".").appendFraction(ChronoField.MICRO_OF_SECOND, 1, 9, false)
						.optionalEnd().toFormatter();
				LocalDateTime dateValue = LocalDateTime.parse(dateTime, formatter);
				String date = dateValue.getYear() + Constant.DASH + dateValue.getMonthValue();
				lastNMonthMap.computeIfPresent(date, (key, value) -> {
					epicList.add(js);
					Integer costOfDelay = (int) js.getCostOfDelay();
					Map<String, Integer> epicWiseCost = new HashMap<>();
					epicWiseCost.put(number, costOfDelay);
					if (howerMap.containsKey(date)) {
						epicWiseCost.putAll(howerMap.get(date));
						howerMap.put(date, epicWiseCost);
					} else {
						howerMap.put(date, epicWiseCost);
					}
					return value + costOfDelay;
				});

			}

		}

		List<DataCount> dcList = new ArrayList<>();
		lastNMonthMap.forEach((k, v) -> {
			DataCount dataCount = new DataCount();
			dataCount.setDate(k);
			dataCount.setValue(v);
			dataCount.setData(v.toString());
			dataCount.setSProjectName(projectName);
			dataCount.setHoverValue(new HashMap<>());
			dcList.add(dataCount);
			trendValueList.add(dataCount);

		});
		mapTmp.get(node.getId()).setValue(dcList);

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			KPIExcelUtility.populateCODExcelData(projectName, epicList, excelData);
		}

	}

	/**
	 * Group list of data by project.
	 *
	 * @param resultList
	 * @return
	 */

	private Map<String, List<JiraIssue>> createProjectWiseDelay(List<JiraIssue> resultList) {
		return resultList.stream().filter(p -> p.getBasicProjectConfigId() != null)
				.collect(Collectors.groupingBy(JiraIssue::getBasicProjectConfigId));
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI113(), KPICode.COST_OF_DELAY.getKpiId());
	}

}
