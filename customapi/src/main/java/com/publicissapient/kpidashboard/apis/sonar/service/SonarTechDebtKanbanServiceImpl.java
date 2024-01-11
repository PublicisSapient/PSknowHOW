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
import org.springframework.stereotype.Component;

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
 * @author shichand0
 *
 */
@Component
@Slf4j
public class SonarTechDebtKanbanServiceImpl
		extends SonarKPIService<Long, List<Object>, Map<String, List<SonarHistory>>> {

	private static final String SQALE_INDEX = "sqale_index";


	/**
	 * Gets KPICode's <tt>SONAR_TECH_DEBT_KANBAN</tt> enum
	 */
	@Override
	public String getQualifierType() {
		return KPICode.SONAR_TECH_DEBT_KANBAN.name();
	}

	/**
	 * @param sonarDetailsMap
	 */
	@Override
	public Long calculateKPIMetrics(Map<String, List<SonarHistory>> sonarDetailsMap) {
		return null;
	}

	/**
	 * Gets KPI Data for Sonar Tech Debt for Kanban projects
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

		log.info("SONAR-TECH-DEBT-KANBAN-", kpiRequest.getRequestTrackerId());
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes().get(HIERARCHY_LEVEL_ID_PROJECT);

		dateWiseLeafNodeValue(mapTmp, projectList, kpiElement, kpiRequest);

		log.debug("[SONAR-TECH-DEBT-KANBAN-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.SONAR_TECH_DEBT_KANBAN);

		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.SONAR_TECH_DEBT_KANBAN);

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

		log.debug("[SONAR-TECH-DEBT-KANBAN-AGGREGATED-VALUE][{}]. Aggregated Value at each level in the tree {}",
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
	 *
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
					prepareSqualeList(history, date, projectName, projectList, debtList, projectWiseDataMap,
							versionDate);
					currentDate = getNextRangeDate(kpiRequest, currentDate);
				}
				mapTmp.get(projectName).setValue(projectWiseDataMap);
				if (getRequestTrackerIdKanban().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
					KPIExcelUtility.populateSonarKpisExcelData(mapTmp.get(projectName).getProjectFilter().getName(),
							projectList, debtList, versionDate, excelData, KPICode.SONAR_TECH_DEBT_KANBAN.getKpiId());
				}
			}
		});

		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.SONAR_TECH_DEBT_KANBAN.getColumns());

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

	/**
	 * Fetches KPI Data from DB
	 * 
	 * @param leafNodeList
	 * @param startDate
	 * @param endDate
	 * @param kpiRequest
	 * @return {@code Map<ObjectId, List<SonarDetails>>}
	 */
	@Override
	public Map<String, List<SonarHistory>> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		return getSonarHistoryForAllProjects(leafNodeList, getKanbanCurrentDateToFetchFromDb(startDate));
	}

	@Override
	public Map<String, Object> getSonarJobWiseKpiData(List<Node> pList, Map<String, Node> tempMap,
			KpiElement kpiElement) {
		return new HashMap<>();
	}

	private Long getTechDebtValue(Object sqlIndex) {
		Long techDebtValue = -1l;
		if (sqlIndex != null) {
			if (sqlIndex instanceof Double) {
				techDebtValue = ((Double) sqlIndex).longValue();
			} else if (sqlIndex instanceof String) {
				techDebtValue = Long.parseLong(sqlIndex.toString());
			} else {
				techDebtValue = (Long) sqlIndex;
			}
		}

		return techDebtValue;

	}

	/**
	 * prepare Map after calculating squale data for each filter drop down
	 * 
	 * @param history
	 * @param date
	 * @param projectNodeId
	 * @param projectList
	 * @param debtList
	 * @param projectWiseDataMap
	 * @param versionDate
	 * @return
	 */
	private Map<String, Object> prepareSqualeList(Map<String, SonarHistory> history, String date, String projectNodeId,
			List<String> projectList, List<String> debtList, Map<String, List<DataCount>> projectWiseDataMap,
			List<String> versionDate) {
		Map<String, Object> key = new HashMap<>();
		List<Long> dateWiseDebtList = new ArrayList<>();
		String projectName = projectNodeId.substring(0, projectNodeId.lastIndexOf(CommonConstant.UNDERSCORE));
		history.forEach((keyName, sonarDetails) -> {
			Map<String, Object> metricMap = sonarDetails.getMetrics().stream()
					.filter(metricValue -> metricValue.getMetricValue() != null)
					.collect(Collectors.toMap(SonarMetric::getMetricName, SonarMetric::getMetricValue));
			final Long techDebtValue = getTechDebtValue(metricMap.get(SQALE_INDEX));
			if (techDebtValue != -1l) {
				// sqale index is in minutes in a 8 hr day so dividing it by 480
				long techDebtValueInDays = Math.round(techDebtValue / 480.0);
				DataCount dcObj = getDataCountObject(techDebtValueInDays, new HashMap<>(), projectName, date);
				projectWiseDataMap.computeIfAbsent(keyName, k -> new LinkedList<>()).add(dcObj);
				projectList.add(keyName);
				versionDate.add(date);
				dateWiseDebtList.add(techDebtValueInDays);
				debtList.add(String.valueOf(techDebtValueInDays));
			}
		});
		DataCount dcObj = getDataCountObject(
				calculateKpiValue(dateWiseDebtList, KPICode.SONAR_TECH_DEBT_KANBAN.getKpiId()), new HashMap<>(),
				projectName, date);
		projectWiseDataMap.computeIfAbsent(CommonConstant.OVERALL, k -> new ArrayList<>()).add(dcObj);

		return key;
	}

	/**
	 * particulate date format given as per date type
	 *
	 * @param dateRange
	 * @param kpiRequest
	 */
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

	public Map<String, SonarHistory> prepareJobwiseHistoryMap(List<SonarHistory> sonarHistoryList, Long start, Long end,
			String projectNodeId) {
		Map<String, SonarHistory> map = new HashMap<>();
		Map<ObjectId, String> keyNameProcessorMap = new HashMap<>();
		List<SonarMetric> metricsList = new ArrayList<>();
		SonarMetric sonarMetric = new SonarMetric();
		sonarMetric.setMetricName(SQALE_INDEX);
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
	public Long calculateKpiValue(List<Long> valueList, String kpiId) {
		return calculateKpiValueForLong(valueList, kpiId);
	}


	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping){
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI67(),KPICode.SONAR_TECH_DEBT_KANBAN.getKpiId());
	}

}