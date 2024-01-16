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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.constant.Constant;
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

/**
 * @author shichand0
 *
 */
@Component
public class SonarViolationsKanbanServiceImpl
		extends SonarKPIService<Long, List<Object>, Map<String, List<SonarHistory>>> {
	private static final String CRITICAL = "critical";
	private static final String MAJOR = "major";
	private static final String MINOR = "minor";
	private static final String BLOCKER = "blocker";
	private static final String INFO = "info";


	@Override
	public String getQualifierType() {
		return KPICode.SONAR_VIOLATIONS_KANBAN.name();
	}

	/**
	 * @param sonarDetailsMap
	 */
	@Override
	public Long calculateKPIMetrics(Map<String, List<SonarHistory>> sonarDetailsMap) {
		return 0L;
	}

	/**
	 * Gets KPI Data
	 * 
	 * @param kpiRequest
	 * @param kpiElement
	 * @param treeAggregatorDetail
	 * @return KpiElement
	 * @throws ApplicationException
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes().get(HIERARCHY_LEVEL_ID_PROJECT);

		dateWiseLeafNodeValue(mapTmp, projectList, kpiElement, kpiRequest);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(treeAggregatorDetail.getRoot(), nodeWiseKPIValue, KPICode.SONAR_VIOLATIONS_KANBAN);

		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.SONAR_VIOLATIONS_KANBAN);

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		trendValuesMap.forEach((key, datewiseDataCount) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			dataCountGroup.setFilter(key);
			dataCountGroup.setValue(datewiseDataCount);
			dataCountGroups.add(dataCountGroup);
		});

		kpiElement.setTrendValueList(dataCountGroups);
		kpiElement.setNodeWiseKPIValue(nodeWiseKPIValue);

		return kpiElement;
	}

	/**
	 * fetch data from db.
	 */
	@Override
	public Map<String, List<SonarHistory>> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		return getSonarHistoryForAllProjects(leafNodeList, getKanbanCurrentDateToFetchFromDb(startDate));
	}

	/**
	 * This method mark the start of data processing for sonar violations
	 * 
	 * @param mapTmp
	 *            mapTmp
	 * @param leafNodeList
	 *            leafNodeList
	 * @param kpiElement
	 *            kpiElement
	 * @param kpiRequest
	 *            kpiRequest
	 */
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

	/**
	 * Create data filter wise
	 * 
	 * @param sonarDetailsForAllProjects
	 * @param mapTmp
	 * @param kpiElement
	 * @param kpiRequest
	 */
	private void kpiWithFilter(Map<String, List<SonarHistory>> sonarDetailsForAllProjects, Map<String, Node> mapTmp,
			KpiElement kpiElement, KpiRequest kpiRequest) {
		List<KPIExcelData> excelData = new ArrayList<>();
		sonarDetailsForAllProjects.forEach((projectName, projectData) -> {
			if (CollectionUtils.isNotEmpty(projectData)) {
				List<String> projectList = new ArrayList<>();
				List<String> violations = new ArrayList<>();
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
					prepareViolationsList(history, date, projectName, projectList, violations, projectWiseDataMap,
							versionDate);

					currentDate = getNextRangeDate(kpiRequest, currentDate);
				}
				mapTmp.get(projectName).setValue(projectWiseDataMap);
				if (getRequestTrackerIdKanban().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
					KPIExcelUtility.populateSonarKpisExcelData(mapTmp.get(projectName).getName(), projectList,
							violations, versionDate, excelData, KPICode.SONAR_VIOLATIONS_KANBAN.getKpiId());
				}
			}
		});

		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.SONAR_VIOLATIONS_KANBAN.getColumns());

	}

	private void prepareViolationsList(Map<String, SonarHistory> history, String date, String projectNodeId,
			List<String> projectList, List<String> violations, Map<String, List<DataCount>> projectWiseDataMap,
			List<String> versionDate) {
		String projectName = projectNodeId.substring(0, projectNodeId.lastIndexOf(CommonConstant.UNDERSCORE));
		List<Long> dateWiseViolationsList = new ArrayList<>();
		List<Map<String, Object>> globalSonarViolationsHowerMap = new ArrayList<>();
		history.forEach((keyName, sonarDetails) -> {
			Map<String, Object> metricMap = sonarDetails.getMetrics().stream()
					.filter(metricValue -> metricValue.getMetricValue() != null)
					.collect(Collectors.toMap(SonarMetric::getMetricName, SonarMetric::getMetricValue));

			Map<String, Object> sonarViolationsHowerMap = new LinkedHashMap<>();
			evaluateViolations(metricMap.get(Constant.CRITICAL_VIOLATIONS), sonarViolationsHowerMap, CRITICAL);
			evaluateViolations(metricMap.get(Constant.BLOCKER_VIOLATIONS), sonarViolationsHowerMap, BLOCKER);
			evaluateViolations(metricMap.get(Constant.MAJOR_VIOLATIONS), sonarViolationsHowerMap, MAJOR);
			evaluateViolations(metricMap.get(Constant.MINOR_VIOLATIONS), sonarViolationsHowerMap, MINOR);
			evaluateViolations(metricMap.get(Constant.INFO_VIOLATIONS), sonarViolationsHowerMap, INFO);

			sonarViolationsHowerMap = sonarViolationsHowerMap.entrySet().stream()
					.sorted((i1, i2) -> ((Integer) i2.getValue()).compareTo((Integer) i1.getValue())).collect(Collectors
							.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
			globalSonarViolationsHowerMap.add(sonarViolationsHowerMap);

			Long sonarViolations = sonarViolationsHowerMap.values().stream().map(a -> (Integer) a).mapToLong(val -> val)
					.sum();

			DataCount dcObj = getDataCountObject(sonarViolations, sonarViolationsHowerMap, projectName, date);
			projectWiseDataMap.computeIfAbsent(keyName, k -> new ArrayList<>()).add(dcObj);
			projectList.add(keyName);
			versionDate.add(date);
			dateWiseViolationsList.add(sonarViolations);
			violations.add(sonarViolationsHowerMap.toString());
		});
		DataCount dcObj = getDataCountObject(
				calculateKpiValue(dateWiseViolationsList, KPICode.SONAR_VIOLATIONS.getKpiId()),
				calculateKpiValueForIntMap(globalSonarViolationsHowerMap, KPICode.SONAR_VIOLATIONS.getKpiId()),
				projectName, date);
		projectWiseDataMap.computeIfAbsent(CommonConstant.OVERALL, k -> new ArrayList<>()).add(dcObj);
	}

	/**
	 *
	 * @param violations
	 * @param valueMap
	 * @param key
	 */
	private void evaluateViolations(Object violations, Map<String, Object> valueMap, String key) {

		if (violations != null) {
			if (violations instanceof Double) {
				valueMap.put(key, ((Double) violations).intValue());
			} else if (violations instanceof String) {
				valueMap.put(key, (Integer.parseInt(violations.toString())));
			} else {
				valueMap.put(key, violations);
			}
		}
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

	/**
	 * Segregate data week wise
	 * 
	 * @param sonarHistoryList
	 * @param start
	 * @param end
	 * @param projectNodeId
	 * @return
	 */
	private Map<String, SonarHistory> prepareJobwiseHistoryMap(List<SonarHistory> sonarHistoryList, Long start,
			Long end, String projectNodeId) {
		Map<String, SonarHistory> map = new HashMap<>();
		Map<ObjectId, String> keyNameProcessorMap = new HashMap<>();
		List<SonarMetric> metricsList = new ArrayList<>();
		SonarMetric sonarMetric = SonarMetric.builder().metricName(Constant.CRITICAL_VIOLATIONS).metricValue("0")
				.build();
		SonarMetric sonarMetric1 = SonarMetric.builder().metricName(Constant.BLOCKER_VIOLATIONS).metricValue("0")
				.build();
		SonarMetric sonarMetric2 = SonarMetric.builder().metricName(Constant.MAJOR_VIOLATIONS).metricValue("0").build();
		SonarMetric sonarMetric3 = SonarMetric.builder().metricName(Constant.MINOR_VIOLATIONS).metricValue("0").build();
		SonarMetric sonarMetric4 = SonarMetric.builder().metricName(Constant.INFO_VIOLATIONS).metricValue("0").build();

		metricsList.add(sonarMetric);
		metricsList.add(sonarMetric1);
		metricsList.add(sonarMetric2);
		metricsList.add(sonarMetric3);
		metricsList.add(sonarMetric4);

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
	public Map<String, Object> getSonarJobWiseKpiData(List<Node> projectList, Map<String, Node> tempMap,
			KpiElement kpiElement) {
		return new HashMap<>();
	}

	@Override
	public Long calculateKpiValue(List<Long> valueList, String kpiId) {
		return calculateKpiValueForLong(valueList, kpiId);
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping){
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI64(),KPICode.SONAR_VIOLATIONS_KANBAN.getKpiId());
	}

}