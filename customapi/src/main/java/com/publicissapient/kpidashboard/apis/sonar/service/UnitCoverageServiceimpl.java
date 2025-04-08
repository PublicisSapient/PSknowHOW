/**
 * Copyright 2014 CapitalOne, LLC. Further development Copyright 2022 Sapient Corporation.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.publicissapient.kpidashboard.apis.sonar.service;

import static com.publicissapient.kpidashboard.common.constant.CommonConstant.HIERARCHY_LEVEL_ID_PROJECT;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.enums.Filters;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
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
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.sonar.SonarDetails;
import com.publicissapient.kpidashboard.common.model.sonar.SonarHistory;
import com.publicissapient.kpidashboard.common.model.sonar.SonarMetric;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * This class is a service to compute unit coverage.
 *
 * @author prigupta8
 */
@Component
@Slf4j
public class UnitCoverageServiceimpl extends SonarKPIService<Double, List<Object>, Map<ObjectId, List<SonarDetails>>> {
	private static final String TEST_UNIT_COVERAGE = "coverage";

	private static final String AVERAGE_COVERAGE = "Average Coverage";

	@Autowired
	private CustomApiConfig customApiConfig;

	@Override
	public String getQualifierType() {
		return KPICode.UNIT_TEST_COVERAGE.name();
	}

	@Override
	public Double calculateKPIMetrics(Map<ObjectId, List<SonarDetails>> sonarDetailsMap) {
		return null;
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, TreeAggregatorDetail treeAggregatorDetail)
			throws ApplicationException {
		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes().get(HIERARCHY_LEVEL_ID_PROJECT);

		Filters filter = Filters.getFilter(kpiRequest.getLabel());
		if (filter == Filters.SPRINT || filter == Filters.PROJECT) {
			List<Node> leafNodes = treeAggregatorDetail.getMapOfListOfLeafNodes().entrySet().stream()
					.filter(k -> Filters.getFilter(k.getKey()) == Filters.SPRINT).map(Map.Entry::getValue).findFirst()
					.orElse(Collections.emptyList());
			getSonarKpiData(projectList, treeAggregatorDetail.getMapTmp(), kpiElement, leafNodes);

		} else {
			getSonarKpiData(projectList, treeAggregatorDetail.getMapTmp(), kpiElement, Collections.emptyList());
		}

		log.debug("[UNIT-TEST-COVERAGE-LEAF-NODE-VALUE][{}]. Values of project size for KPI calculation {}",
				kpiRequest.getRequestTrackerId(),
				treeAggregatorDetail.getMapOfListOfProjectNodes().get(HIERARCHY_LEVEL_ID_PROJECT).size());

		// KPI value null represent no data and hence the maturity value should
		// be zero.UI should show no data for the KPI and nothing on the
		// maturity radar.

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(treeAggregatorDetail.getRoot(), nodeWiseKPIValue, KPICode.UNIT_TEST_COVERAGE);
		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.UNIT_TEST_COVERAGE);

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		trendValuesMap.forEach((key, datewiseDataCount) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			dataCountGroup.setFilter(key);
			dataCountGroup.setValue(datewiseDataCount);
			dataCountGroups.add(dataCountGroup);
		});

		kpiElement.setTrendValueList(dataCountGroups);
		log.debug("[CODE-BUILD-TIME-AGGREGATED-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(),
				treeAggregatorDetail.getMapOfListOfProjectNodes().get(HIERARCHY_LEVEL_ID_PROJECT));
		return kpiElement;
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiId) {
		return calculateKpiValueForDouble(valueList, kpiId);
	}

	@Override
	public Map<ObjectId, List<SonarDetails>> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		return new HashMap<>();
	}

	public void getSonarKpiData(List<Node> pList, Map<String, Node> tempMap, KpiElement kpiElement,
			List<Node> sprintLeafNodeList) {
		List<KPIExcelData> excelData = new ArrayList<>();
		LocalDate dateToFetch = CollectionUtils.isNotEmpty(sprintLeafNodeList)
				? DateUtil.stringToLocalDate(
				sprintLeafNodeList.get(sprintLeafNodeList.size() - 1).getSprintFilter().getStartDate().replaceAll("Z|\\.\\d+", ""),
				DateUtil.TIME_FORMAT)
				: getScrumCurrentDateToFetchFromDb(CommonConstant.WEEK, (long) customApiConfig.getSonarWeekCount());

		getSonarHistoryForAllProjects(pList, dateToFetch).forEach((projectNodePair, projectData) -> {
			if (CollectionUtils.isEmpty(projectData)) {
				return;
			}

			List<String> projectList = new ArrayList<>();
			List<String> coverage = new ArrayList<>();
			List<String> versionDate = new ArrayList<>();
			Map<String, List<DataCount>> projectWiseDataMap = new HashMap<>();

			if (CollectionUtils.isNotEmpty(sprintLeafNodeList)) {
				processSprintData(sprintLeafNodeList.stream()
						.filter(node -> node.getProjectFilter().getId().equalsIgnoreCase(projectNodePair.getLeft()))
						.toList(), projectData, projectList, coverage, projectWiseDataMap, versionDate);
			} else {
				processWeeklyData(projectData, projectNodePair, projectList, coverage, projectWiseDataMap, versionDate);
			}

			updateTempMapAndExcelData(tempMap, excelData, projectNodePair, projectList, coverage, versionDate,
					projectWiseDataMap);
		});

		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.UNIT_TEST_COVERAGE.getColumns());
	}

	private void processSprintData(List<Node> sprintLeafNodeList, List<SonarHistory> projectData,
			List<String> projectList, List<String> coverage, Map<String, List<DataCount>> projectWiseDataMap,
			List<String> versionDate) {
		sprintLeafNodeList.forEach(sprintDetail -> {

			Long startDate = DateUtil.convertStringToLong(sprintDetail.getSprintFilter().getStartDate().replaceAll("Z|\\.\\d+", ""));
			Long endDate = DateUtil.convertStringToLong(sprintDetail.getSprintFilter().getEndDate().replaceAll("Z|\\.\\d+", ""));

			Map<String, SonarHistory> history = prepareJobwiseHistoryMap(projectData, startDate, endDate);
			if (MapUtils.isEmpty(history)) {
				history = prepareEmptyJobWiseHistoryMap(projectData, endDate);
			}
			prepareCoverageList(history, sprintDetail.getSprintFilter().getName(),
					sprintDetail.getProjectFilter().getName(), projectList, coverage, projectWiseDataMap, versionDate);
		});
	}

	private void processWeeklyData(List<SonarHistory> projectData, Pair<String, String> projectNodePair,
			List<String> projectList, List<String> coverage, Map<String, List<DataCount>> projectWiseDataMap,
			List<String> versionDate) {
		LocalDate endDateTime = LocalDate.now().minusWeeks(1);
		for (int i = 0; i < customApiConfig.getSonarWeekCount(); i++) {
			LocalDate[] weeks = getWeeks(endDateTime);
			String dateRange = formatDateRange(weeks[0], weeks[1]);

			Long startMs = weeks[0].atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
			Long endMs = weeks[1].atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

			Map<String, SonarHistory> history = prepareJobwiseHistoryMap(projectData, startMs, endMs);
			if (MapUtils.isEmpty(history)) {
				history = prepareEmptyJobWiseHistoryMap(projectData, endMs);
			}

			prepareCoverageList(history, dateRange, projectNodePair.getRight(), projectList, coverage, projectWiseDataMap,
					versionDate);
			endDateTime = endDateTime.minusWeeks(1);
		}
	}

	private void updateTempMapAndExcelData(Map<String, Node> tempMap, List<KPIExcelData> excelData,
			Pair<String, String> projectNodePair, List<String> projectList, List<String> coverage,
			List<String> versionDate, Map<String, List<DataCount>> projectWiseDataMap) {
		tempMap.get(projectNodePair.getLeft()).setValue(projectWiseDataMap);
		if (getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			KPIExcelUtility.populateSonarKpisExcelData(
					tempMap.get(projectNodePair.getLeft()).getProjectFilter().getName(), projectList, coverage,
					versionDate, excelData, KPICode.UNIT_TEST_COVERAGE.getKpiId());
		}
	}

	private String formatDateRange(LocalDate monday, LocalDate sunday) {
		return DateUtil.dateTimeConverter(monday.toString(), DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT)
				+ " to "
				+ DateUtil.dateTimeConverter(sunday.toString(), DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT);
	}

	private void prepareCoverageList(Map<String, SonarHistory> history, String date, String projectName,
			List<String> projectList, List<String> coverageList, Map<String, List<DataCount>> projectWiseDataMap,
			List<String> versionDate) {
		List<Double> dateWiseCoverageList = new ArrayList<>();
		history.values().forEach(sonarDetails -> {
			Map<String, Object> metricMap = sonarDetails.getMetrics().stream()
					.filter(metricValue -> metricValue.getMetricValue() != null)
					.collect(Collectors.toMap(SonarMetric::getMetricName, SonarMetric::getMetricValue));
			Double coverage = metricMap.get(TEST_UNIT_COVERAGE) == null
					? 0d
					: Double.parseDouble(metricMap.get(TEST_UNIT_COVERAGE).toString());
			String keyName = prepareSonarKeyName(projectName, sonarDetails.getName(), sonarDetails.getBranch());
			DataCount dcObj = getDataCountObject(coverage, projectName, date, keyName);
			projectWiseDataMap.computeIfAbsent(keyName, k -> new ArrayList<>()).add(dcObj);
			projectList.add(keyName);
			versionDate.add(date);
			dateWiseCoverageList.add(coverage);
			coverageList.add(metricMap.get(TEST_UNIT_COVERAGE) == null
					? Constant.NOT_AVAILABLE
					: metricMap.get(TEST_UNIT_COVERAGE).toString());
		});
		DataCount dcObj = getDataCountObject(calculateKpiValue(dateWiseCoverageList, KPICode.UNIT_TEST_COVERAGE.getKpiId()),
				projectName, date, AVERAGE_COVERAGE);
		projectWiseDataMap.computeIfAbsent(AVERAGE_COVERAGE, k -> new ArrayList<>()).add(dcObj);
	}

	private Map<String, SonarHistory> prepareEmptyJobWiseHistoryMap(List<SonarHistory> sonarHistoryList, Long end) {

		List<SonarMetric> metricsList = new ArrayList<>();
		Map<String, SonarHistory> historyMap = new HashMap<>();
		SonarHistory refHistory = sonarHistoryList.get(0);

		SonarMetric sonarMetric = new SonarMetric();
		sonarMetric.setMetricName(TEST_UNIT_COVERAGE);
		sonarMetric.setMetricValue("0");
		metricsList.add(sonarMetric);

		List<String> uniqueKeys = sonarHistoryList.stream().map(SonarHistory::getKey).distinct()
				.toList();
		uniqueKeys.forEach(keys -> {
			SonarHistory sonarHistory = SonarHistory.builder().processorItemId(refHistory.getProcessorItemId()).date(end)
					.timestamp(end).key(keys).name(keys).branch(refHistory.getBranch()).metrics(metricsList).build();
			historyMap.put(keys, sonarHistory);
		});

		return historyMap;
	}

	private DataCount getDataCountObject(Double value, String projectName, String date, String keyName) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(value));
		dataCount.setSSprintID(date);
		dataCount.setSSprintName(date);
		dataCount.setSProjectName(projectName);
		dataCount.setDate(date);
		Map<String, Object> hoverValueMap = new HashMap<>();
		hoverValueMap.put(keyName, value);
		dataCount.setHoverValue(hoverValueMap);
		dataCount.setValue(value);
		return dataCount;
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI17(), KPICode.UNIT_TEST_COVERAGE.getKpiId());
	}
}
