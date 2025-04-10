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
	
	private static final String DATE_TIME_FORMAT_REGEX = "Z|\\.\\d+";

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
//      in case if only projects or sprint filters are applied
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
		getSonarHistoryForAllProjects(pList,
				getScrumCurrentDateToFetchFromDb(CommonConstant.WEEK, (long) customApiConfig.getSonarWeekCount()))
				.forEach((projectNodePair, projectData) -> {
					if (CollectionUtils.isNotEmpty(projectData)) {
						processProjectData(projectNodePair, projectData,
								sprintLeafNodeList.stream()
										.filter(node -> node.getProjectFilter().getId()
												.equalsIgnoreCase(projectNodePair.getLeft()))
										.toList(),
								tempMap, excelData);
					}
				});

		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.UNIT_TEST_COVERAGE.getColumns());
	}

	private void processProjectData(Pair<String, String> projectNodePair, List<SonarHistory> projectData,
			List<Node> sprintLeafNodeList, Map<String, Node> tempMap, List<KPIExcelData> excelData) {
		List<String> projectList = new ArrayList<>();
		List<String> coverageList = new ArrayList<>();
		List<String> versionDate = new ArrayList<>();
		Map<String, List<DataCount>> projectWiseDataMap = new HashMap<>();

		boolean isBacklogProject = CollectionUtils.isNotEmpty(sprintLeafNodeList)
				&& sprintLeafNodeList.get(0).getProjectFilter().getName().equalsIgnoreCase(projectNodePair.getValue());
		LocalDate endDateTime = isBacklogProject ? DateUtil.stringToLocalDate(
				sprintLeafNodeList.get(0).getSprintFilter().getEndDate().replaceAll(DATE_TIME_FORMAT_REGEX, ""),
				DateUtil.TIME_FORMAT) : LocalDate.now().minusWeeks(1);

		for (int i = 0; i < customApiConfig.getSonarWeekCount(); i++) {
			LocalDate monday = isBacklogProject ? endDateTime.minusDays(6) : getWeeks(endDateTime)[0];
			LocalDate sunday = isBacklogProject ? endDateTime : getWeeks(endDateTime)[1];
			String date = DateUtil.dateTimeConverter(monday.toString(), DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT)
					+ " to "
					+ DateUtil.dateTimeConverter(sunday.toString(), DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT);

			Long startms = monday.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
			Long endms = sunday.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

			Map<String, SonarHistory> history = prepareJobwiseHistoryMap(projectData, startms, endms);
			if (MapUtils.isEmpty(history)) {
				history = prepareEmptyJobWiseHistoryMap(projectData, endms);
			}

			prepareCoverageList(history, date, projectNodePair, projectList, coverageList, projectWiseDataMap,
					versionDate);
			endDateTime = endDateTime.minusWeeks(1);
		}

		tempMap.get(projectNodePair.getLeft()).setValue(projectWiseDataMap);
		if (getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			KPIExcelUtility.populateSonarKpisExcelData(
					tempMap.get(projectNodePair.getKey()).getProjectFilter().getName(), projectList, versionDate,
					versionDate, excelData, KPICode.SONAR_TECH_DEBT.getKpiId());
		}
	}

	private void prepareCoverageList(Map<String, SonarHistory> history, String date, Pair<String, String> projectNodePair,
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
			String projectDisplayName = projectNodePair.getRight();
			String keyName = prepareSonarKeyName(projectDisplayName, sonarDetails.getName(), sonarDetails.getBranch());
			DataCount dcObj = getDataCountObject(coverage, projectDisplayName, date, keyName);
			projectWiseDataMap.computeIfAbsent(keyName, k -> new ArrayList<>()).add(dcObj);
			projectList.add(keyName);
			versionDate.add(date);
			dateWiseCoverageList.add(coverage);
			coverageList.add(metricMap.get(TEST_UNIT_COVERAGE) == null
					? Constant.NOT_AVAILABLE
					: metricMap.get(TEST_UNIT_COVERAGE).toString());
		});
		DataCount dcObj = getDataCountObject(calculateKpiValue(dateWiseCoverageList, KPICode.UNIT_TEST_COVERAGE.getKpiId()),
				projectNodePair.getRight(), date, AVERAGE_COVERAGE);
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

		List<String> uniqueKeys = sonarHistoryList.stream().map(SonarHistory::getKey).distinct().toList();
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
