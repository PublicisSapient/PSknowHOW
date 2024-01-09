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

/**
 * 
 */
package com.publicissapient.kpidashboard.apis.sonar.service;

import static com.publicissapient.kpidashboard.common.constant.CommonConstant.HIERARCHY_LEVEL_ID_PROJECT;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.sonar.SonarHistory;
import com.publicissapient.kpidashboard.common.model.sonar.SonarMetric;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * This class is a service to compute unite coverage for kanban.
 *
 * @author shichand0
 *
 */
@Component
@Slf4j
public class UnitCoverageKanbanServiceimpl
		extends SonarKPIService<Double, List<Object>, Map<String, List<SonarHistory>>> {

	private static final String MATRIC_NAME_COVERAGE = "coverage";
	@Autowired
	private CustomApiConfig customApiConfig;

	@Override
	public String getQualifierType() {
		return KPICode.UNIT_TEST_COVERAGE_KANBAN.name();
	}

	/**
	 * Unit code coverage (non-Javadoc)
	 *
	 * @see com.publicissapient.kpidashboard.apis.sonar.service .SonarKPIService#
	 *      getUnitCoverageKanban(com.sapient.customdashboard.model.KpiRequest,
	 *      com.sapient.customdashboard.model.KpiElement,
	 *      com.sapient.customdashboard.model.TreeAggregatorDetail)
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		log.info("SONAR-UNIT-COVERAGE-KANBAN-", kpiRequest.getRequestTrackerId());
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes().get(HIERARCHY_LEVEL_ID_PROJECT);

		dateWiseLeafNodeValue(mapTmp, projectList, kpiElement, kpiRequest);

		log.debug("[SONAR-UNIT-COVERAGE-KANBAN-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.UNIT_TEST_COVERAGE_KANBAN);

		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.UNIT_TEST_COVERAGE_KANBAN);

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		trendValuesMap.forEach((key, datewiseDataCount) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			dataCountGroup.setFilter(key);
			dataCountGroup.setValue(datewiseDataCount);
			dataCountGroups.add(dataCountGroup);
		});

		kpiElement.setTrendValueList(dataCountGroups);

		// KPI value null represent no data and hence the maturity value should
		// be zero.UI should show no data for the KPI and nothing on the
		// maturity radar.

		kpiElement.setNodeWiseKPIValue(nodeWiseKPIValue);

		log.debug("[SONAR-UNIT-COVERAGE-KANBAN-AGGREGATED-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), root);
		return kpiElement;
	}

	@Override
	public Map<String, Object> getSonarJobWiseKpiData(List<Node> projectList, Map<String, Node> tempMap,
			KpiElement kpiElement) {
		return new HashMap<>();
	}

	@Override
	public Double calculateKPIMetrics(Map<String, List<SonarHistory>> stringListMap) {
		return null;
	}

	@Override
	public Map<String, List<SonarHistory>> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		return getSonarHistoryForAllProjects(leafNodeList, getKanbanCurrentDateToFetchFromDb(startDate));
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiId) {
		return calculateKpiValueForDouble(valueList, kpiId);
	}

	private void dateWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> leafNodeList, KpiElement kpiElement,
			KpiRequest kpiRequest) {

		// this method fetch start and end date to fetch data.
		CustomDateRange dateRange = KpiDataHelper.getStartAndEndDate(kpiRequest);

		// get start and end date in yyyy-mm-dd format
		String startDate = dateRange.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String endDate = dateRange.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

		Map<String, List<SonarHistory>> sonarDetailsForAllProjects = fetchKPIDataFromDb(leafNodeList, startDate,
				endDate, kpiRequest);

		kpiWithFilter(sonarDetailsForAllProjects, mapTmp, kpiElement, kpiRequest);

	}

	private void kpiWithFilter(Map<String, List<SonarHistory>> sonarDetailsForAllProjects, Map<String, Node> mapTmp,
			KpiElement kpiElement, KpiRequest kpiRequest) {
		List<KPIExcelData> excelData = new ArrayList<>();
		sonarDetailsForAllProjects.forEach((projectName, projectData) -> {
			if (CollectionUtils.isNotEmpty(projectData)) {
				List<String> projectList = new ArrayList<>();
				List<String> debtList = new ArrayList<>();
				List<String> versionDate = new ArrayList<>();
				Map<String, List<DataCount>> projectWiseDataMap = new LinkedHashMap<>();

				LocalDate currentDate = LocalDate.now();
				for (int i = 0; i < kpiRequest.getKanbanXaxisDataPoints(); i++) {
					CustomDateRange dateRange = KpiDataHelper.getStartAndEndDateForDataFiltering(currentDate,
							kpiRequest.getDuration());
					Long startms = dateRange.getStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant()
							.toEpochMilli();
					Long endms = dateRange.getEndDate().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()
							.toEpochMilli();
					Map<String, SonarHistory> history = prepareJobwiseHistoryMap(projectData, startms, endms,
							projectName);
					String date = getRange(dateRange, kpiRequest);
					prepareCoverageList(history, date, projectName, projectList, debtList, projectWiseDataMap,
							versionDate);
					currentDate = getNextRangeDate(kpiRequest, currentDate);
				}
				mapTmp.get(projectName).setValue(projectWiseDataMap);
				if (getRequestTrackerIdKanban().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
					KPIExcelUtility.populateSonarKpisExcelData(mapTmp.get(projectName).getProjectFilter().getName(),
							projectList, debtList, versionDate, excelData,
							KPICode.UNIT_TEST_COVERAGE_KANBAN.getKpiId());
				}
			}
		});

		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.UNIT_TEST_COVERAGE_KANBAN.getColumns());

	}

	private LocalDate getNextRangeDate(KpiRequest kpiRequest, LocalDate currentDate) {
		if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.WEEK)) {
			currentDate = currentDate.minusWeeks(1);
		} else if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.MONTH)) {
			currentDate = currentDate.minusMonths(1);
		} else {
			currentDate = currentDate.minusDays(1);
		}
		return currentDate;
	}

	private String getRange(CustomDateRange dateRange, KpiRequest kpiRequest) {
		String range = null;
		if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.WEEK)) {
			range = DateUtil.dateTimeConverter(dateRange.getStartDate().toString(), DateUtil.DATE_FORMAT,
					DateUtil.DISPLAY_DATE_FORMAT) + " to "
					+ DateUtil.dateTimeConverter(dateRange.getEndDate().toString(), DateUtil.DATE_FORMAT,
							DateUtil.DISPLAY_DATE_FORMAT);
		} else if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.MONTH)) {
			range = dateRange.getStartDate().getMonth().toString() + " " + dateRange.getStartDate().getYear();
		} else {
			range = dateRange.getStartDate().toString();
		}
		return range;
	}

	private Map<String, Object> prepareCoverageList(Map<String, SonarHistory> history, String date,
			String projectNodeId, List<String> projectList, List<String> debtList,
			Map<String, List<DataCount>> projectWiseDataMap, List<String> versionDate) {
		Map<String, Object> key = new HashMap<>();
		List<Double> dateWiseCoverageList = new ArrayList<>();
		String projectName = projectNodeId.substring(0, projectNodeId.lastIndexOf(CommonConstant.UNDERSCORE));
		history.forEach((keyName, sonarDetails) -> {
			Map<String, Object> metricMap = sonarDetails.getMetrics().stream()
					.filter(metricValue -> metricValue.getMetricValue() != null)
					.collect(Collectors.toMap(SonarMetric::getMetricName, SonarMetric::getMetricValue));
			final Double coverageValue = getCoverageValue(metricMap.get(MATRIC_NAME_COVERAGE));
			if (coverageValue != -1l) {

				DataCount dcObj = getDataCountObject(coverageValue.longValue(), new HashMap<>(), projectName, date);
				projectWiseDataMap.computeIfAbsent(keyName, k -> new LinkedList<>()).add(dcObj);
				projectList.add(keyName);
				versionDate.add(date);
				dateWiseCoverageList.add(coverageValue);
				debtList.add(String.valueOf(coverageValue));
			}
		});
		DataCount dcObj = getDataCountObject(
				calculateKpiValue(dateWiseCoverageList, KPICode.UNIT_TEST_COVERAGE_KANBAN.getKpiId()).longValue(),
				new HashMap<>(), projectName, date);
		projectWiseDataMap.computeIfAbsent(CommonConstant.OVERALL, k -> new ArrayList<>()).add(dcObj);

		return key;
	}

	private Double getCoverageValue(Object coverage) {
		Double value = -1D;
		if (coverage != null) {
			if (coverage instanceof Double) {
				value = (Double) coverage;
			} else if (coverage instanceof String) {
				value = Double.parseDouble(coverage.toString());
			} else {
				value = (Double) coverage;
			}
		}

		return value;

	}

	public Map<String, SonarHistory> prepareJobwiseHistoryMap(List<SonarHistory> sonarHistoryList, Long start, Long end,
			String projectNodeId) {
		Map<String, SonarHistory> map = new HashMap<>();
		Map<ObjectId, String> keyNameProcessorMap = new HashMap<>();
		List<SonarMetric> metricsList = new ArrayList<>();
		SonarMetric sonarMetric = new SonarMetric();
		sonarMetric.setMetricName(MATRIC_NAME_COVERAGE);
		sonarMetric.setMetricValue("0");
		metricsList.add(sonarMetric);

		for (SonarHistory sonarHistory : sonarHistoryList) {
			String keyName = prepareSonarKeyName(projectNodeId, sonarHistory.getName(), sonarHistory.getBranch());
			ObjectId processorItemId = sonarHistory.getProcessorItemId();
			if (sonarHistory.getTimestamp().compareTo(start) > 0 && sonarHistory.getTimestamp().compareTo(end) < 0) {
				map.putIfAbsent(keyName, sonarHistory);
				if (sonarHistory.getTimestamp().compareTo(map.get(keyName).getTimestamp()) > 0) {
					map.put(keyName, sonarHistory);
				}
			}
			keyNameProcessorMap.put(processorItemId, keyName);
		}

		keyNameProcessorMap.entrySet().stream().filter(key -> !map.containsKey(key.getValue())).forEach(key -> {
			String[] split = key.getValue().split(CommonConstant.ARROW);
			SonarHistory build;
			if (split.length == 3) {
				build = SonarHistory.builder().processorItemId(key.getKey()).date(end).timestamp(end).key(split[0])
						.name(split[0]).branch(split[1]).metrics(metricsList).build();
			} else {
				build = SonarHistory.builder().processorItemId(key.getKey()).date(end).timestamp(end).key(split[0])
						.name(split[0]).metrics(metricsList).build();
			}
			map.put(key.getValue(), build);
		}

		);
		return map;
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping){
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI62(),KPICode.UNIT_TEST_COVERAGE_KANBAN.getKpiId());
	}
}
