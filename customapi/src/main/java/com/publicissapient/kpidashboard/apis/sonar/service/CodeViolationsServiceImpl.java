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

package com.publicissapient.kpidashboard.apis.sonar.service;

import static com.publicissapient.kpidashboard.common.constant.CommonConstant.HIERARCHY_LEVEL_ID_PROJECT;

import java.time.LocalDate;
import java.time.ZoneId;
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

import org.apache.commons.collections4.CollectionUtils;
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
import com.publicissapient.kpidashboard.apis.model.IterationKpiFilters;
import com.publicissapient.kpidashboard.apis.model.IterationKpiFiltersOptions;
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
 * @author prigupta8
 */
@Component
@Slf4j
public class CodeViolationsServiceImpl extends SonarKPIService<Long, List<Object>, Map<ObjectId, List<SonarDetails>>> {

	private static final Map<String, String> SEVERITY_MAP = Map.of(Constant.CRITICAL_VIOLATIONS, "critical",
			Constant.BLOCKER_VIOLATIONS, "blocker", Constant.MAJOR_VIOLATIONS, "major", Constant.MINOR_VIOLATIONS,
			"minor", Constant.INFO_VIOLATIONS, "info");

	private static final Map<String, String> TYPE_MAP = Map.of(Constant.BUGS, "bugs", Constant.VULNERABILITIES,
			"vulnerabilities", Constant.CODE_SMELL, "code smells");

	private static final String VIOLATION_TYPES = "RadioBtn";
	private static final String JOB_FILTER = "Select a filter";
	private static final String SEVERITY = "Severity";
	private static final String TYPE = "Type";

	public void setCustomApiConfig(CustomApiConfig customApiConfig) {
		this.customApiConfig = customApiConfig;
	}

	@Autowired
	protected CustomApiConfig customApiConfig;

	/**
	 * Gets KPI Data
	 *
	 * @param kpiRequest
	 *            kpiRequest
	 * @param kpiElement
	 *            kpiElement
	 * @param treeAggregatorDetail
	 *            treeAggregatorDetail
	 * @return KpiElement KpiElement
	 * @throws ApplicationException
	 *             throw error
	 */
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes().get(HIERARCHY_LEVEL_ID_PROJECT);

		getSonarKpiData(projectList, treeAggregatorDetail.getMapTmp(), kpiElement);

		log.debug("[UNIT-TEST-COVERAGE-LEAF-NODE-VALUE][{}]. Values of project size for KPI calculation {}",
				kpiRequest.getRequestTrackerId(),
				treeAggregatorDetail.getMapOfListOfProjectNodes().get(HIERARCHY_LEVEL_ID_PROJECT).size());

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(treeAggregatorDetail.getRoot(), nodeWiseKPIValue, KPICode.CODE_VIOLATIONS);

		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.CODE_VIOLATIONS);

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

		return kpiElement;
	}

	public void getSonarKpiData(List<Node> pList, Map<String, Node> tempMap, KpiElement kpiElement) {
		List<KPIExcelData> excelData = new ArrayList<>();
		Set<String> overAllJoblist = new HashSet<>();
		getSonarHistoryForAllProjects(pList,
				getScrumCurrentDateToFetchFromDb(CommonConstant.WEEK, (long) customApiConfig.getSonarWeekCount()))
				.forEach((projectNodePair, projectData) -> {
					List<String> projectList = new ArrayList<>();
					List<List<String>> violations = new ArrayList<>();
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
							Long startms = monday.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
							Long endms = sunday.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()
									.toEpochMilli();
							Map<String, SonarHistory> history = prepareJobwiseHistoryMap(projectData, startms, endms,
									projectNodePair.getValue());
							prepareViolationsList(history, date, projectNodePair.getValue(), projectList, violations,
									projectWiseDataMap, versionDate);

							endDateTime = endDateTime.minusWeeks(1);
						}
						overAllJoblist.addAll(projectList);
						tempMap.get(projectNodePair.getKey()).setValue(projectWiseDataMap);
						if (getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
							KPIExcelUtility.populateSonarViolationsExcelData(
									tempMap.get(projectNodePair.getKey()).getProjectFilter().getName(), projectList,
									violations, versionDate, excelData, KPICode.CODE_VIOLATIONS.getKpiId());
						}
					}
				});
		IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(JOB_FILTER, overAllJoblist);
		IterationKpiFiltersOptions filter2 = new IterationKpiFiltersOptions(VIOLATION_TYPES,
				new HashSet<>(Arrays.asList(SEVERITY, TYPE)));
		IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, filter2);
		kpiElement.setFilters(iterationKpiFilters);
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.SONAR_VIOLATIONS.getColumns());
	}

	/**
	 * Segregate data week wise
	 *
	 * @param sonarHistoryList
	 *            sonarHistoryList
	 * @param start
	 *            startdate
	 * @param end
	 *            enddate
	 * @param projectNodeId
	 *            projectNodeId
	 * @return map
	 */
	private Map<String, SonarHistory> prepareJobwiseHistoryMap(List<SonarHistory> sonarHistoryList, Long start,
			Long end, String projectNodeId) {
		Map<String, SonarHistory> map = new HashMap<>();
		Map<ObjectId, String> keyNameProcessorMap = new HashMap<>();
		List<SonarMetric> metricsList = new ArrayList<>();

		metricsList.add(SonarMetric.builder().metricName(Constant.CRITICAL_VIOLATIONS).metricValue(Double.NaN).build());
		metricsList.add(SonarMetric.builder().metricName(Constant.BLOCKER_VIOLATIONS).metricValue(Double.NaN).build());
		metricsList.add(SonarMetric.builder().metricName(Constant.MAJOR_VIOLATIONS).metricValue(Double.NaN).build());
		metricsList.add(SonarMetric.builder().metricName(Constant.MINOR_VIOLATIONS).metricValue(Double.NaN).build());
		metricsList.add(SonarMetric.builder().metricName(Constant.INFO_VIOLATIONS).metricValue(Double.NaN).build());
		metricsList.add(SonarMetric.builder().metricName(Constant.BUGS).metricValue(Double.NaN).build());
		metricsList.add(SonarMetric.builder().metricName(Constant.VULNERABILITIES).metricValue(Double.NaN).build());
		metricsList.add(SonarMetric.builder().metricName(Constant.CODE_SMELL).metricValue(Double.NaN).build());

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
		});
		return map;
	}

	/**
	 * Prepares the list of violations for a given project and date range.
	 *
	 * @param history
	 *            A map containing the Sonar history data.
	 * @param date
	 *            The date range for which the violations are being prepared.
	 * @param projectName
	 *            The nodeDisplayName of the project node.
	 * @param projectList
	 *            A list to store the project names.
	 * @param violations
	 *            A list to store the violations.
	 * @param projectWiseDataMap
	 *            A map to store the data counts for each project.
	 * @param versionDate
	 *            A list to store the version dates.
	 */
	private void prepareViolationsList(Map<String, SonarHistory> history, String date, String projectName,
			List<String> projectList, List<List<String>> violations, Map<String, List<DataCount>> projectWiseDataMap,
			List<String> versionDate) {
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
					.filter(entry -> SEVERITY_MAP.containsValue(entry.getKey()) && 0 <= (Integer) entry.getValue())
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
			globalSonarViolationsHoverMapByType.add(sonarViolationsHoverMapByType.entrySet().stream()
					.filter(entry -> TYPE_MAP.containsValue(entry.getKey()) && 0 <= (Integer) entry.getValue())
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

			Long sonarViolations = getSonarViolations(sonarViolationsHoverMapBySeverity);

			String keyName = prepareSonarKeyName(projectName, sonarDetails.getName(), sonarDetails.getBranch());
			String kpiGroup = keyName + "#" + SEVERITY;
			DataCount dcObjSeverety = getDataCountObject(sonarViolations, sonarViolationsHoverMapBySeverity,
					projectName, date, kpiGroup);
			projectWiseDataMap.computeIfAbsent(kpiGroup, k -> new ArrayList<>()).add(dcObjSeverety);
			sonarViolations = getSonarViolations(sonarViolationsHoverMapByType);
			kpiGroup = keyName + "#" + TYPE;
			DataCount dcObjType = getDataCountObject(sonarViolations, sonarViolationsHoverMapByType, projectName, date,
					kpiGroup);
			projectWiseDataMap.computeIfAbsent(kpiGroup, k -> new ArrayList<>()).add(dcObjType);
			projectList.add(keyName);
			versionDate.add(date);
			if(sonarViolations>=0) {
				dateWiseViolationsList.add(sonarViolations);
			}
			Function<Map<String, Object>, String> mapToString = map -> map.entrySet().stream()
					.map(entry -> entry.getValue() + " " + entry.getKey()).collect(Collectors.joining(", "));
			violations.add(Arrays.asList(mapToString.apply(sonarViolationsHoverMapBySeverity),
					mapToString.apply(sonarViolationsHoverMapByType)));
		});
		DataCount dcObj = getDataCountObject(
				calculateKpiValue(dateWiseViolationsList, KPICode.CODE_VIOLATIONS.getKpiId()),
				calculateKpiValueForIntMap(globalSonarViolationsHoverMapBySeverity, KPICode.CODE_VIOLATIONS.getKpiId()),
				projectName, date);
		projectWiseDataMap.computeIfAbsent(CommonConstant.OVERALL + "#" + SEVERITY, k -> new ArrayList<>()).add(dcObj);

		dcObj = getDataCountObject(calculateKpiValue(dateWiseViolationsList, KPICode.CODE_VIOLATIONS.getKpiId()),
				calculateKpiValueForIntMap(globalSonarViolationsHoverMapByType, KPICode.CODE_VIOLATIONS.getKpiId()),
				projectName, date);
		projectWiseDataMap.computeIfAbsent(CommonConstant.OVERALL + "#" + TYPE, k -> new ArrayList<>()).add(dcObj);
	}

	private static Long getSonarViolations(Map<String, Object> sonarViolationsHoverMapBySeverity) {
		List<Object> filteredList = sonarViolationsHoverMapBySeverity.values().stream()
				.filter(a -> 0 <= (Integer) a).toList();
		Long sonarViolations = -1L;
		if (CollectionUtils.isNotEmpty(filteredList)) {
			sonarViolations = filteredList.stream().map(Integer.class::cast).mapToLong(val -> val).sum();
		}
		return sonarViolations;
	}

	/**
	 * Creates and sorts a map of violations based on the provided reference map and
	 * metric map.
	 *
	 * @param referenceMap
	 *            A map containing the reference values for sorting.
	 * @param metricMap
	 *            A map containing the metric values to be evaluated and sorted.
	 * @return A sorted map of violations.
	 */
	private Map<String, Object> createAndSortViolationsMap(Map<String, String> referenceMap,
			Map<String, Object> metricMap) {
		Map<String, Object> violationsMap = new LinkedHashMap<>();
		referenceMap.forEach((key, value) -> evaluateViolations(metricMap.get(key), violationsMap, value));

		return violationsMap.entrySet().stream().filter(entry -> entry.getValue() != null) // Exclude entries with null
				// values
				.sorted((i1, i2) -> ((Integer) i2.getValue()).compareTo((Integer) i1.getValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	/**
	 * Creates a DataCount object with the provided values.
	 *
	 * @param value
	 *            The value to be set in the DataCount object.
	 * @param hoverValues
	 *            A map containing hover values to be set in the DataCount object.
	 * @param projectName
	 *            The name of the project to be set in the DataCount object.
	 * @param date
	 *            The date to be set in the DataCount object.
	 * @param kpiGroup
	 *            The KPI group to be set in the DataCount object.
	 * @return A DataCount object populated with the provided values.
	 */
	public DataCount getDataCountObject(Long value, Map<String, Object> hoverValues, String projectName, String date,
			String kpiGroup) {
		DataCount dataCount = new DataCount();
		if (value >= 0) {
			dataCount.setData(String.valueOf(value));
			dataCount.setValue(value);
			dataCount.setHoverValue(hoverValues);
		}
		dataCount.setSProjectName(projectName);
		dataCount.setKpiGroup(kpiGroup);
		dataCount.setDate(date);

		return dataCount;
	}

	/** Gets KPICode's <tt>SONAR_VIOLATIONS</tt> enum */
	@Override
	public String getQualifierType() {
		return KPICode.CODE_VIOLATIONS.name();
	}

	/**
	 * @param sonarDetailsMap
	 *            sonarDetailsMap
	 */
	@Override
	public Long calculateKPIMetrics(Map<ObjectId, List<SonarDetails>> sonarDetailsMap) {
		return 0L;
	}

	/**
	 * @param violations
	 *            violations
	 * @param valueMap
	 *            valueMap
	 * @param key
	 *            key
	 */
	private void evaluateViolations(Object violations, Map<String, Object> valueMap, String key) {

		if (violations instanceof Double) {
			if (!Double.isNaN((Double) violations)) {
				valueMap.put(key, ((Double) violations).intValue());
			} else {
				valueMap.put(key, -1);
			}
		} else if (violations instanceof String) {
			valueMap.put(key, (Integer.parseInt(violations.toString())));
		} else {
			valueMap.put(key, violations);
		}
	}

	/** Not used */
	@Override
	public Map<ObjectId, List<SonarDetails>> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate,
			String endDate, KpiRequest kpiRequest) {
		return new HashMap<>();
	}

	@Override
	public Long calculateKpiValue(List<Long> valueList, String kpiId) {
		if (CollectionUtils.isNotEmpty(valueList)) {
			return calculateKpiValueForLong(valueList, kpiId);
		} else {
			return -1L;
		}

	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI38(), KPICode.CODE_VIOLATIONS.getKpiId());
	}
}
