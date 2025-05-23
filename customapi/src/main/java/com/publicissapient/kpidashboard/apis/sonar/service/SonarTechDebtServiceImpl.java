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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
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
 * @author prigupta8
 */
@Component
@Slf4j
public class SonarTechDebtServiceImpl extends SonarKPIService<Long, List<Object>, Map<ObjectId, List<SonarDetails>>> {

	private static final String SQALE_INDEX = "sqale_index";

	@Autowired
	private CustomApiConfig customApiConfig;

	@Override
	public String getQualifierType() {
		return KPICode.SONAR_TECH_DEBT.name();
	}

	/**
	 * @param sonarDetailsMap
	 */
	@Override
	public Long calculateKPIMetrics(Map<ObjectId, List<SonarDetails>> sonarDetailsMap) {
		return null;
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, TreeAggregatorDetail treeAggregatorDetail)
			throws ApplicationException {
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

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(treeAggregatorDetail.getRoot(), nodeWiseKPIValue, KPICode.SONAR_TECH_DEBT);

		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.SONAR_TECH_DEBT);

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		trendValuesMap.forEach((key, datewiseDataCount) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			dataCountGroup.setFilter(key);
			dataCountGroup.setValue(datewiseDataCount);
			dataCountGroups.add(dataCountGroup);
		});
		kpiElement.setTrendValueList(dataCountGroups);
		log.debug("[SONAR-TECH-DEBT-AGGREGATED-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), treeAggregatorDetail.getRoot());
		return kpiElement;
	}

	public void getSonarKpiData(List<Node> pList, Map<String, Node> tempMap, KpiElement kpiElement,
			List<Node> sprintLeafNodeList) {
		List<KPIExcelData> excelData = new ArrayList<>();
		Map<String, SprintDetails> sprintDetailsList = getSprintDetailsByIds(sprintLeafNodeList);

		getSonarHistoryForAllProjects(pList,
				getScrumCurrentDateToFetchFromDb(CommonConstant.WEEK, (long) customApiConfig.getSonarWeekCount()))
				.forEach((projectNodePair, projectData) -> {
					if (CollectionUtils.isNotEmpty(projectData)) {
						String projectId = projectNodePair.getKey();
						SprintDetails sprintDetails = sprintDetailsList.get(projectId) != null
								? sprintDetailsList.get(projectId)
								: null;
						processProjectData(projectNodePair, projectData, sprintDetails, tempMap,
								excelData);
					}
				});

		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.SONAR_TECH_DEBT.getColumns());
	}

	private void processProjectData(Pair<String, String> projectNodePair, List<SonarHistory> projectData,
			SprintDetails sprintDetails, Map<String, Node> tempMap, List<KPIExcelData> excelData) {
		List<String> projectList = new ArrayList<>();
		List<String> debtList = new ArrayList<>();
		List<String> versionDate = new ArrayList<>();
		Map<String, List<DataCount>> projectWiseDataMap = new HashMap<>();
		LocalDate endDateTime = getEndDate(sprintDetails);

		for (int i = 0; i < customApiConfig.getSonarWeekCount(); i++) {
			LocalDate monday = sprintDetails != null ? endDateTime.minusDays(6) : getWeeks(endDateTime)[0];
			LocalDate sunday = sprintDetails != null ? endDateTime : getWeeks(endDateTime)[1];
			String date = DateUtil.dateTimeConverter(monday.toString(), DateUtil.DATE_FORMAT,
					DateUtil.DISPLAY_DATE_FORMAT) + " to "
					+ DateUtil.dateTimeConverter(sunday.toString(), DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT);

			Long startms = monday.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
			Long endms = sunday.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

			Map<String, SonarHistory> history = prepareJobwiseHistoryMap(projectData, startms, endms);
			if (MapUtils.isEmpty(history)) {
				history = prepareEmptyJobWiseHistoryMap(projectData, endms);
			}

			prepareSqualeList(history, date, projectNodePair.getValue(), projectList, debtList, projectWiseDataMap,
					versionDate);
			endDateTime = endDateTime.minusWeeks(1);
		}

		tempMap.get(projectNodePair.getLeft()).setValue(projectWiseDataMap);

		if (getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			KPIExcelUtility.populateSonarKpisExcelData(
					tempMap.get(projectNodePair.getKey()).getProjectFilter().getName(), projectList, debtList,
					versionDate, excelData, KPICode.SONAR_TECH_DEBT.getKpiId());
		}
	}
	
	private Map<String, Object> prepareSqualeList(Map<String, SonarHistory> history, String date, String projectName,
			List<String> projectList, List<String> debtList, Map<String, List<DataCount>> projectWiseDataMap,
			List<String> versionDate) {
		Map<String, Object> key = new HashMap<>();
		List<Long> dateWiseDebtList = new ArrayList<>();
		history.values().stream().forEach(sonarDetails -> {
			Map<String, Object> metricMap = sonarDetails.getMetrics().stream()
					.filter(metricValue -> metricValue.getMetricValue() != null)
					.collect(Collectors.toMap(SonarMetric::getMetricName, SonarMetric::getMetricValue));
			final Long techDebtValue = getTechDebtValue(metricMap.get(SQALE_INDEX));
			if (techDebtValue != -1l) {
				// sqale index is in minutes in a 8 hr day so dividing it by 480
				long techDebtValueInDays = Math.round(techDebtValue / 480.0);
				String keyName = prepareSonarKeyName(projectName, sonarDetails.getName(), sonarDetails.getBranch());
				DataCount dcObj = getDataCountObject(techDebtValueInDays, new HashMap<>(), projectName, date);
				projectWiseDataMap.computeIfAbsent(keyName, k -> new ArrayList<>()).add(dcObj);
				projectList.add(keyName);
				versionDate.add(date);
				dateWiseDebtList.add(techDebtValueInDays);
				debtList.add(String.valueOf(techDebtValueInDays));
			}
		});
		DataCount dcObj = getDataCountObject(calculateKpiValue(dateWiseDebtList, KPICode.SONAR_TECH_DEBT.getKpiId()),
				new HashMap<>(), projectName, date);
		projectWiseDataMap.computeIfAbsent(CommonConstant.OVERALL, k -> new ArrayList<>()).add(dcObj);
		return key;
	}

	private Map<String, SonarHistory> prepareEmptyJobWiseHistoryMap(List<SonarHistory> sonarHistoryList, Long end) {

		List<SonarMetric> metricsList = new ArrayList<>();
		Map<String, SonarHistory> historyMap = new HashMap<>();
		SonarHistory refHistory = sonarHistoryList.get(0);

		SonarMetric sonarMetric = new SonarMetric();
		sonarMetric.setMetricName(SQALE_INDEX);
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

	/**
	 * @param sqlIndex
	 * @return tech Debt value
	 */
	public Long getTechDebtValue(Object sqlIndex) {

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
	 * Not used as data is not being calculated sprintwise
	 *
	 * @param leafNodeList
	 * @param startDate
	 * @param endDate
	 * @param kpiRequest
	 * @return {@code Map<ObjectId, List<SonarDetails>>}
	 */
	@Override
	public Map<ObjectId, List<SonarDetails>> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		return new HashMap<>();
	}

	@Override
	public Long calculateKpiValue(List<Long> valueList, String kpiId) {
		return calculateKpiValueForLong(valueList, kpiId);
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI27(), KPICode.SONAR_TECH_DEBT.getKpiId());
	}
}
