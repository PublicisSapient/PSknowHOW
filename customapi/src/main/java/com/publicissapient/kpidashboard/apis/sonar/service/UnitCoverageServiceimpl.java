/**
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.publicissapient.kpidashboard.apis.sonar.service;

import static com.publicissapient.kpidashboard.common.constant.CommonConstant.HIERARCHY_LEVEL_ID_PROJECT;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
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
import com.publicissapient.kpidashboard.common.model.sonar.SonarDetails;
import com.publicissapient.kpidashboard.common.model.sonar.SonarHistory;
import com.publicissapient.kpidashboard.common.model.sonar.SonarMetric;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * This class is a service to compute unit coverage.
 *
 * @author prigupta8
 *
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
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes().get(HIERARCHY_LEVEL_ID_PROJECT);

		getSonarKpiData(projectList, treeAggregatorDetail.getMapTmp(), kpiElement);

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
	public Map<ObjectId, List<SonarDetails>> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate,
			String endDate, KpiRequest kpiRequest) {
		return new HashMap<>();
	}

	@Override
	public Map<String, Object> getSonarJobWiseKpiData(List<Node> pList, Map<String, Node> tempMap,
			KpiElement kpiElement) {
		return new HashMap<>();
	}

	public void getSonarKpiData(List<Node> pList, Map<String, Node> tempMap, KpiElement kpiElement) {
		List<KPIExcelData> excelData = new ArrayList<>();
		getSonarHistoryForAllProjects(pList,
				getScrumCurrentDateToFetchFromDb(CommonConstant.WEEK, (long) customApiConfig.getSonarWeekCount()))
						.forEach((projectNodeId, projectData) -> {
							List<String> projectList = new ArrayList<>();
							List<String> coverageList = new ArrayList<>();
							List<String> versionDate = new ArrayList<>();
							Map<String, List<DataCount>> projectWiseDataMap = new HashMap<>();
							if (CollectionUtils.isNotEmpty(projectData)) {
								LocalDate endDateTime = LocalDate.now().minusWeeks(1);
								for (int i = 0; i < customApiConfig.getSonarWeekCount(); i++) {
									LocalDate[] weeks = getWeeks(endDateTime);
									LocalDate monday = weeks[0];
									LocalDate sunday = weeks[1];

									String date = DateUtil.dateTimeConverter(monday.toString(), DateUtil.DATE_FORMAT,
											DateUtil.DISPLAY_DATE_FORMAT) + " to "
											+ DateUtil.dateTimeConverter(sunday.toString(), DateUtil.DATE_FORMAT,
													DateUtil.DISPLAY_DATE_FORMAT);
									Long startms = monday.atStartOfDay(ZoneId.systemDefault()).toInstant()
											.toEpochMilli();
									Long endms = sunday.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()
											.toEpochMilli();
									Map<String, SonarHistory> history = prepareJobwiseHistoryMap(projectData, startms,
											endms);

									if (MapUtils.isEmpty(history)) {
										history = prepareEmptyJobWiseHistoryMap(projectData, endms);
									}
									prepareCoverageList(history, date, projectNodeId, projectList, coverageList,
											projectWiseDataMap, versionDate);

									endDateTime = endDateTime.minusWeeks(1);
								}
								tempMap.get(projectNodeId).setValue(projectWiseDataMap);
								if (getRequestTrackerId().toLowerCase()
										.contains(KPISource.EXCEL.name().toLowerCase())) {
									KPIExcelUtility.populateSonarKpisExcelData(
											tempMap.get(projectNodeId).getProjectFilter().getName(), projectList,
											coverageList, versionDate, excelData,
											KPICode.UNIT_TEST_COVERAGE.getKpiId());
								}
							}
						});

		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.UNIT_TEST_COVERAGE.getColumns());
	}

	private void prepareCoverageList(Map<String, SonarHistory> history, String date, String projectNodeId,
			List<String> projectList, List<String> coverageList, Map<String, List<DataCount>> projectWiseDataMap,
			List<String> versionDate) {
		String projectName = projectNodeId.substring(0, projectNodeId.lastIndexOf(CommonConstant.UNDERSCORE));
		List<Double> dateWiseCoverageList = new ArrayList<>();
		history.values().forEach(sonarDetails -> {
			Map<String, Object> metricMap = sonarDetails.getMetrics().stream()
					.filter(metricValue -> metricValue.getMetricValue() != null)
					.collect(Collectors.toMap(SonarMetric::getMetricName, SonarMetric::getMetricValue));
			Double coverage = metricMap.get(TEST_UNIT_COVERAGE) == null ? 0d
					: Double.parseDouble(metricMap.get(TEST_UNIT_COVERAGE).toString());
			String keyName = prepareSonarKeyName(projectNodeId, sonarDetails.getName(), sonarDetails.getBranch());
			DataCount dcObj = getDataCountObject(coverage, projectName, date, keyName);
			projectWiseDataMap.computeIfAbsent(keyName, k -> new ArrayList<>()).add(dcObj);
			projectList.add(keyName);
			versionDate.add(date);
			dateWiseCoverageList.add(coverage);
			coverageList.add(metricMap.get(TEST_UNIT_COVERAGE) == null ? Constant.NOT_AVAILABLE
					: metricMap.get(TEST_UNIT_COVERAGE).toString());
		});
		DataCount dcObj = getDataCountObject(
				calculateKpiValue(dateWiseCoverageList, KPICode.UNIT_TEST_COVERAGE.getKpiId()), projectName, date,
				AVERAGE_COVERAGE);
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
				.collect(Collectors.toList());
		uniqueKeys.forEach(keys -> {
			SonarHistory sonarHistory = SonarHistory.builder().processorItemId(refHistory.getProcessorItemId())
					.date(end).timestamp(end).key(keys).name(keys).branch(refHistory.getBranch()).metrics(metricsList)
					.build();
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
	public Double calculateThresholdValue(FieldMapping fieldMapping){
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI17(),KPICode.UNIT_TEST_COVERAGE.getKpiId());
	}

}
