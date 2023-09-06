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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
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
 * @author prigupta8
 *
 */
@Component
@Slf4j
public class SonarViolationsServiceImpl extends SonarKPIService<Long, List<Object>, Map<ObjectId, List<SonarDetails>>> {

	private static final String CRITICAL = "critical";
	private static final String MAJOR = "major";
	private static final String MINOR = "minor";
	private static final String BLOCKER = "blocker";
	private static final String INFO = "info";

	@Autowired
	private CustomApiConfig customApiConfig;

	/**
	 * Gets KPI Data
	 * 
	 * @param kpiRequest
	 * @param kpiElement
	 * @param treeAggregatorDetail
	 * @return KpiElement
	 * @throws ApplicationException
	 */
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes().get(HIERARCHY_LEVEL_ID_PROJECT);

		getSonarKpiData(projectList, treeAggregatorDetail.getMapTmp(), kpiElement);

		log.debug("[UNIT-TEST-COVERAGE-LEAF-NODE-VALUE][{}]. Values of project size for KPI calculation {}",
				kpiRequest.getRequestTrackerId(),
				treeAggregatorDetail.getMapOfListOfProjectNodes().get(HIERARCHY_LEVEL_ID_PROJECT).size());

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(treeAggregatorDetail.getRoot(), nodeWiseKPIValue, KPICode.SONAR_VIOLATIONS);

		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, nodeWiseKPIValue,
				KPICode.SONAR_VIOLATIONS);

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		trendValuesMap.forEach((key, datewiseDataCount) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			dataCountGroup.setFilter(key);
			dataCountGroup.setValue(datewiseDataCount);
			dataCountGroups.add(dataCountGroup);
		});
		kpiElement.setTrendValueList(dataCountGroups);

		return kpiElement;
	}

	/**
	 * @param projectList
	 * @param kpiElement
	 * @return
	 */
	@Override
	public Map<String, Object> getSonarJobWiseKpiData(final List<Node> projectList, Map<String, Node> tempMap,
			KpiElement kpiElement) {

		return new HashMap<>();
	}

	public void getSonarKpiData(List<Node> pList, Map<String, Node> tempMap, KpiElement kpiElement) {
		List<KPIExcelData> excelData = new ArrayList<>();

		getSonarHistoryForAllProjects(pList, null, false).forEach((projectNodeId, projectData) -> {
			List<String> projectList = new ArrayList<>();
			List<String> violations = new ArrayList<>();
			List<String> versionDate = new ArrayList<>();
			Map<String, List<DataCount>> projectWiseDataMap = new HashMap<>();
			if (CollectionUtils.isNotEmpty(projectData)) {
				LocalDate endDateTime = LocalDate.now();
				for (int i = 0; i < customApiConfig.getSonarWeekCount(); i++) {
					LocalDate[] weeks = getWeeks(endDateTime);
					LocalDate monday = weeks[0];
					LocalDate sunday = weeks[1];

					String date = DateUtil.dateTimeConverter(monday.toString(), DateUtil.DATE_FORMAT,
							DateUtil.DISPLAY_DATE_FORMAT) + " to "
							+ DateUtil.dateTimeConverter(sunday.toString(), DateUtil.DATE_FORMAT,
									DateUtil.DISPLAY_DATE_FORMAT);
					Long startms = monday.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
					Long endms = sunday.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
					Map<String, SonarHistory> history = prepareJobwiseHistoryMap(projectData, startms, endms,
							projectNodeId);
					prepareViolationsList(history, date, projectNodeId, projectList, violations, projectWiseDataMap,
							versionDate);

					endDateTime = endDateTime.minusWeeks(1);
				}
				tempMap.get(projectNodeId).setValue(projectWiseDataMap);
				if (getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
					KPIExcelUtility.populateSonarKpisExcelData(tempMap.get(projectNodeId).getProjectFilter().getName(),
							projectList, violations, versionDate, excelData, KPICode.SONAR_VIOLATIONS.getKpiId());
				}
			}
		});

		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.SONAR_VIOLATIONS.getColumns());
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

	private void prepareViolationsList(Map<String, SonarHistory> history, String date, String projectNodeId,
			List<String> projectList, List<String> violations, Map<String, List<DataCount>> projectWiseDataMap,
			List<String> versionDate) {
		String projectName = projectNodeId.substring(0, projectNodeId.lastIndexOf(CommonConstant.UNDERSCORE));
		List<Long> dateWiseViolationsList = new ArrayList<>();
		List<Map<String, Object>> globalSonarViolationsHowerMap = new ArrayList<>();
		history.values().stream().forEach(sonarDetails -> {
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

			Long sonarViolations = sonarViolationsHowerMap.values().stream().map(Integer.class::cast)
					.mapToLong(val -> val).sum();

			String keyName = prepareSonarKeyName(projectNodeId, sonarDetails.getName(), sonarDetails.getBranch());
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

	private Map<String, SonarHistory> prepareEmptyJobWiseHistoryMap(List<SonarHistory> sonarHistoryList, Long end) {

		List<SonarMetric> metricsList = new ArrayList<>();
		Map<String, SonarHistory> historyMap = new HashMap<>();
		SonarHistory refHistory = sonarHistoryList.get(0);

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

	private DataCount getDataCountObject(Long sonarViolations, Map<String, Object> sonarViolationsHowerMap,
			String projectName, String date) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(sonarViolations));
		dataCount.setSSprintID(date);
		dataCount.setSSprintName(date);
		dataCount.setSProjectName(projectName);
		dataCount.setDate(date);
		dataCount.setValue(sonarViolations);
		dataCount.setHoverValue(sonarViolationsHowerMap);
		return dataCount;
	}

	/**
	 * Gets KPICode's <tt>SONAR_VIOLATIONS</tt> enum
	 */
	@Override
	public String getQualifierType() {
		return KPICode.SONAR_VIOLATIONS.name();
	}

	/**
	 * @param sonarDetailsMap
	 */
	@Override
	public Long calculateKPIMetrics(Map<ObjectId, List<SonarDetails>> sonarDetailsMap) {
		return 0L;
	}

	/**
	 * @param violations
	 * @param valueMap
	 * @param key
	 */
	private void evaluateViolations(Object violations, Map<String, Object> valueMap, String key) {
		if (violations instanceof Double) {
			valueMap.put(key, ((Double) violations).intValue());
		} else if (violations instanceof String) {
			valueMap.put(key, (Integer.parseInt(violations.toString())));
		} else {
			valueMap.put(key, (Integer) violations);
		}
	}

	/**
	 * Not used
	 */
	@Override
	public Map<ObjectId, List<SonarDetails>> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate,
			String endDate, KpiRequest kpiRequest) {
		return new HashMap<>();
	}

	@Override
	public Long calculateKpiValue(List<Long> valueList, String kpiId) {
		return calculateKpiValueForLong(valueList, kpiId);
	}

}
