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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.model.IterationKpiFilters;
import com.publicissapient.kpidashboard.apis.model.IterationKpiFiltersOptions;
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
public class CodeViolationsKanbanServiceImpl
		extends SonarKPIService<Long, List<Object>, Map<String, List<SonarHistory>>> {
	private static final Map<String, String> SEVERITY_MAP = Map.of(
			Constant.CRITICAL_VIOLATIONS, "critical",
			Constant.BLOCKER_VIOLATIONS, "blocker",
			Constant.MAJOR_VIOLATIONS, "major",
			Constant.MINOR_VIOLATIONS, "minor",
			Constant.INFO_VIOLATIONS, "info"
	);

	private static final Map<String, String> TYPE_MAP = Map.of(
			Constant.BUGS, "bugs",
			Constant.VULNERABILITIES, "vulnerabilities",
			Constant.CODE_SMELL, "code smells"
	);

	private static final String VIOLATION_TYPES = "RadioBtn";
	private static final String JOB_FILTER = "Select a filter";
	private static final String SEVERITY = "Severity";
	private static final String TYPE = "Type";


	@Override
	public String getQualifierType() {
		return KPICode.CODE_VIOLATIONS_KANBAN.name();
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
		calculateAggregatedValueMap(treeAggregatorDetail.getRoot(), nodeWiseKPIValue, KPICode.CODE_VIOLATIONS_KANBAN);

		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.CODE_VIOLATIONS_KANBAN);

		Map<String, Map<String, List<DataCount>>> statusTypeProjectWiseDc = new LinkedHashMap<>();
		trendValuesMap.forEach((statusType, dataCounts) -> {
			Map<String, List<DataCount>> projectWiseDc = dataCounts.stream()
					.collect(Collectors.groupingBy(DataCount::getData));
			statusTypeProjectWiseDc.put(statusType, projectWiseDc);
		});

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		statusTypeProjectWiseDc.forEach((issueType, projectWiseDc) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			List<DataCount> dataList = new ArrayList<>();
			projectWiseDc.forEach((key, value) -> dataList.addAll(value));
			// split for filters
			String[] issueFilter = issueType.split("#");
			dataCountGroup.setFilter1(issueFilter[0]);
			dataCountGroup.setFilter2(issueFilter[1]);
			dataCountGroup.setValue(dataList);
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
		Set<String> overAllJoblist = new HashSet<>();
		sonarDetailsForAllProjects.forEach((projectName, projectData) -> {
			if (CollectionUtils.isNotEmpty(projectData)) {
				List<String> projectList = new ArrayList<>();
				List<List<String>> violations = new ArrayList<>();
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
					prepareViolationsList(history, date, projectName, projectList, violations,
							projectWiseDataMap, versionDate);

					currentDate = getNextRangeDate(kpiRequest, currentDate);
				}
				overAllJoblist.addAll(projectList);
				mapTmp.get(projectName).setValue(projectWiseDataMap);
				if (getRequestTrackerIdKanban().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
					KPIExcelUtility.populateSonarViolationsExcelData(mapTmp.get(projectName).getName(), projectList,
							violations, versionDate, excelData, KPICode.CODE_VIOLATIONS_KANBAN.getKpiId());
				}
			}
		});
		IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(JOB_FILTER, overAllJoblist);
		IterationKpiFiltersOptions filter2 = new IterationKpiFiltersOptions(VIOLATION_TYPES,
				new HashSet<>(Arrays.asList(SEVERITY, TYPE)));
		IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, filter2);
		kpiElement.setFilters(iterationKpiFilters);
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.SONAR_VIOLATIONS_KANBAN.getColumns());

	}

	private void prepareViolationsList(Map<String, SonarHistory> history, String date, String projectNodeId,
			List<String> projectList, List<List<String>> violations, Map<String, List<DataCount>> projectWiseDataMap,
			List<String> versionDate) {

		String projectName = projectNodeId.substring(0, projectNodeId.lastIndexOf(CommonConstant.UNDERSCORE));
		List<Long> dateWiseViolationsList = new ArrayList<>();
		List<Map<String, Object>> globalSonarViolationsHoverMapBySeverity = new ArrayList<>();
		List<Map<String, Object>> globalSonarViolationsHoverMapByType = new ArrayList<>();
		history.values().forEach(sonarDetails -> {
			Map<String, Object> metricMap = sonarDetails.getMetrics().stream()
					.filter(metricValue -> metricValue.getMetricValue() != null)
					.collect(Collectors.toMap(SonarMetric::getMetricName, SonarMetric::getMetricValue));

			Map<String, Object> sonarViolationsHoverMapBySeverity = createAndSortViolationsMap(SEVERITY_MAP, metricMap);
			Map<String, Object> sonarViolationsHoverMapByType = createAndSortViolationsMap(TYPE_MAP, metricMap);

			globalSonarViolationsHoverMapBySeverity.add(sonarViolationsHoverMapBySeverity.entrySet().stream()
					.filter(entry -> SEVERITY_MAP.containsValue(entry.getKey()))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
			globalSonarViolationsHoverMapByType.add(sonarViolationsHoverMapByType.entrySet().stream()
					.filter(entry -> TYPE_MAP.containsValue(entry.getKey()))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
			Long sonarViolations = sonarViolationsHoverMapBySeverity.values().stream().map(Integer.class::cast)
					.mapToLong(val -> val).sum();

			String keyName = prepareSonarKeyName(projectNodeId, sonarDetails.getName(), sonarDetails.getBranch());
			String kpiGroup = keyName + "#" + SEVERITY;
			DataCount dcObjSeverety = getDataCountObject(sonarViolations, sonarViolationsHoverMapBySeverity,
					projectName, date, kpiGroup);
			projectWiseDataMap.computeIfAbsent(kpiGroup, k -> new ArrayList<>()).add(dcObjSeverety);
			sonarViolations = sonarViolationsHoverMapByType.values().stream().map(Integer.class::cast)
					.mapToLong(val -> val).sum();
			kpiGroup = keyName + "#" + TYPE;
			DataCount dcObjType = getDataCountObject(sonarViolations, sonarViolationsHoverMapByType,
					projectName, date, kpiGroup);
			projectWiseDataMap.computeIfAbsent(kpiGroup, k -> new ArrayList<>()).add(dcObjType);
			projectList.add(keyName);
			versionDate.add(date);
			dateWiseViolationsList.add(sonarViolations);
			Function<Map<String, Object>, String> mapToString = map -> map.entrySet().stream()
					.map(entry -> entry.getValue() + " " + entry.getKey()).collect(Collectors.joining(", "));
			violations.add(Arrays.asList(mapToString.apply(sonarViolationsHoverMapBySeverity),
					mapToString.apply(sonarViolationsHoverMapByType)));
		});
		DataCount dcObj= getDataCountObject(
				calculateKpiValue(dateWiseViolationsList, KPICode.CODE_VIOLATIONS.getKpiId()),
				calculateKpiValueForIntMap(globalSonarViolationsHoverMapBySeverity, KPICode.CODE_VIOLATIONS.getKpiId()),
				projectName, date);
		projectWiseDataMap.computeIfAbsent(CommonConstant.OVERALL + "#" + SEVERITY, k -> new ArrayList<>()).add(dcObj);

		dcObj = getDataCountObject(
				calculateKpiValue(dateWiseViolationsList, KPICode.CODE_VIOLATIONS.getKpiId()),
				calculateKpiValueForIntMap(globalSonarViolationsHoverMapByType, KPICode.CODE_VIOLATIONS.getKpiId()),
				projectName, date);
		projectWiseDataMap.computeIfAbsent(CommonConstant.OVERALL+"#"+TYPE, k -> new ArrayList<>()).add(dcObj);
	}

	/**
	 * Creates and sorts a map of violations based on the provided reference map and metric map.
	 *
	 * @param referenceMap A map containing the reference values for sorting.
	 * @param metricMap    A map containing the metric values to be evaluated and sorted.
	 * @return             A sorted map of violations.
	 */
	private Map<String, Object> createAndSortViolationsMap(Map<String, String> referenceMap,
														   Map<String, Object> metricMap) {
		Map<String, Object> violationsMap = new LinkedHashMap<>();
		referenceMap.forEach((key, value) -> evaluateViolations(metricMap.get(key), violationsMap, value));

		return violationsMap.entrySet().stream()
				.filter(entry -> entry.getValue() != null) // Exclude entries with null values
				.sorted((i1, i2) -> ((Integer) i2.getValue()).compareTo((Integer) i1.getValue()))
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue,
						(e1, e2) -> e1,
						LinkedHashMap::new
				));
	}

	/**
	 * Creates a DataCount object with the provided values.
	 *
	 * @param value        The value to be set in the DataCount object.
	 * @param hoverValues  A map containing hover values to be set in the DataCount object.
	 * @param projectName  The name of the project to be set in the DataCount object.
	 * @param date         The date to be set in the DataCount object.
	 * @param kpiGroup     The KPI group to be set in the DataCount object.
	 * @return             A DataCount object populated with the provided values.
	 */
	public DataCount getDataCountObject(Long value, Map<String, Object> hoverValues, String projectName, String date,
			String kpiGroup) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(value));
		dataCount.setSProjectName(projectName);
		dataCount.setKpiGroup(kpiGroup);
		dataCount.setDate(date);
		dataCount.setValue(value);
		dataCount.setHoverValue(hoverValues);
		return dataCount;
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
		metricsList.add(SonarMetric.builder().metricName(Constant.CRITICAL_VIOLATIONS).metricValue("0").build());
		metricsList.add(SonarMetric.builder().metricName(Constant.BLOCKER_VIOLATIONS).metricValue("0").build());
		metricsList.add(SonarMetric.builder().metricName(Constant.MAJOR_VIOLATIONS).metricValue("0").build());
		metricsList.add(SonarMetric.builder().metricName(Constant.MINOR_VIOLATIONS).metricValue("0").build());
		metricsList.add(SonarMetric.builder().metricName(Constant.INFO_VIOLATIONS).metricValue("0").build());
		metricsList.add(SonarMetric.builder().metricName(Constant.BUGS).metricValue("0").build());
		metricsList.add(SonarMetric.builder().metricName(Constant.VULNERABILITIES).metricValue("0").build());
		metricsList.add(SonarMetric.builder().metricName(Constant.CODE_SMELL).metricValue("0").build());

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
	public Long calculateKpiValue(List<Long> valueList, String kpiId) {
		return calculateKpiValueForLong(valueList, kpiId);
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping){
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI64(),KPICode.CODE_VIOLATIONS_KANBAN.getKpiId());
	}

}