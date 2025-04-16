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
import java.time.YearMonth;
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
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.sonar.SonarDetails;
import com.publicissapient.kpidashboard.common.model.sonar.SonarHistory;
import com.publicissapient.kpidashboard.common.model.sonar.SonarMetric;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * this kpi is used to find out the code quality of any branch you configure on
 * sonar
 *
 * @author shi6
 */
@Component
@Slf4j
public class CodeQualityServiceImpl extends SonarKPIService<Long, List<Object>, Map<ObjectId, List<SonarDetails>>> {

	private static final String SQALE_RATING = "sqale_rating";
	private static final String DATE_TIME_FORMAT_REGEX = "Z|\\.\\d+";

	@Autowired
	private CustomApiConfig customApiConfig;

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
		calculateAggregatedValueMap(treeAggregatorDetail.getRoot(), nodeWiseKPIValue, KPICode.SONAR_CODE_QUALITY);

		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.SONAR_CODE_QUALITY);

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

	/**
	 * get Sonar kpi data
	 *
	 * @param pList
	 *          prpjectlIST
	 * @param tempMap
	 *          tempMap
	 * @param kpiElement
	 *          kpiElement
	 */
	public void getSonarKpiData(List<Node> pList, Map<String, Node> tempMap, KpiElement kpiElement, List<Node> sprintLeafNodeList) {
		List<KPIExcelData> excelData = new ArrayList<>();
		Map<String, SprintDetails> sprintDetailsList = getSprintDetailsByIds(sprintLeafNodeList);

		getSonarHistoryForAllProjects(pList,
				getScrumCurrentDateToFetchFromDb(CommonConstant.MONTH, Long.valueOf(customApiConfig.getSonarMonthCount())))
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
		kpiElement.setExcelColumns(KPIExcelColumn.CODE_QUALITY.getColumns());
	}

	private void processProjectData(Pair<String, String> projectNodePair, List<SonarHistory> projectData,
			SprintDetails sprintDetails, Map<String, Node> tempMap, List<KPIExcelData> excelData) {
		List<String> projectList = new ArrayList<>();
		List<String> debtList = new ArrayList<>();
		List<String> versionDate = new ArrayList<>();
		Map<String, List<DataCount>> projectWiseDataMap = new HashMap<>();
		// get previous month details as the start date
		LocalDate endDateTime = LocalDate.now().minusWeeks(1);
		if (sprintDetails != null) {
			endDateTime = sprintDetails.getCompleteDate() != null
					? DateUtil.stringToLocalDate(sprintDetails.getCompleteDate().replaceAll(DATE_TIME_FORMAT_REGEX, ""),
							DateUtil.TIME_FORMAT)
					: DateUtil.stringToLocalDate(sprintDetails.getEndDate().replaceAll(DATE_TIME_FORMAT_REGEX, ""),
							DateUtil.TIME_FORMAT);
		}

		for (int i = 0; i < customApiConfig.getSonarMonthCount(); i++) {
			CustomDateRange dateRange = KpiDataHelper.getStartAndEndDateForDataFiltering(endDateTime,
					CommonConstant.MONTH);
			LocalDate monthStartDate = dateRange.getStartDate();
			LocalDate monthEndDate = dateRange.getEndDate();
			if (sprintDetails != null) {
				monthStartDate = endDateTime
						.minusDays(YearMonth.of(endDateTime.getYear(), endDateTime.getMonth()).lengthOfMonth() - 1L);
				monthEndDate = endDateTime;
			}

			String date = DateUtil.dateTimeConverter(monthStartDate.toString(), DateUtil.DATE_FORMAT,
					DateUtil.DISPLAY_DATE_FORMAT) + " to "
					+ DateUtil.dateTimeConverter(monthEndDate.toString(), DateUtil.DATE_FORMAT,
							DateUtil.DISPLAY_DATE_FORMAT);

			Long startms = monthStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
			Long endms = monthEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

			// create sonarhistory map for all the x-axis points
			Map<String, SonarHistory> history = prepareJobwiseHistoryMap(projectData, startms, endms);
			if (MapUtils.isEmpty(history)) {
				history = prepareEmptyJobWiseHistoryMap(projectData, endms);
			}

			prepareSqualeList(history, date, projectNodePair, projectList, debtList, projectWiseDataMap, versionDate);

			endDateTime = endDateTime.minusMonths(1);
		}
		tempMap.get(projectNodePair.getKey()).setValue(projectWiseDataMap);
		if (getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			KPIExcelUtility.populateSonarKpisExcelData(
					tempMap.get(projectNodePair.getKey()).getProjectFilter().getName(), projectList, debtList,
					versionDate, excelData, KPICode.SONAR_CODE_QUALITY.getKpiId());
		}
	}

	/**
	 * prepare dummy data for empty responses
	 *
	 * @param sonarHistoryList
	 *          sonarHistoryList
	 * @param end
	 *          endDate
	 * @return map
	 */
	public Map<String, SonarHistory> prepareEmptyJobWiseHistoryMap(List<SonarHistory> sonarHistoryList, Long end) {
		Map<String, SonarHistory> historyMap = new HashMap<>();
		if (CollectionUtils.isNotEmpty(sonarHistoryList)) {
			List<SonarMetric> metricsList = new ArrayList<>();
			SonarHistory refHistory = sonarHistoryList.get(0);

			SonarMetric sonarMetric = new SonarMetric();
			sonarMetric.setMetricName(SQALE_RATING);
			sonarMetric.setMetricValue("0.0");
			metricsList.add(sonarMetric);

			List<String> uniqueKeys = sonarHistoryList.stream().map(SonarHistory::getKey).distinct().toList();
			uniqueKeys.forEach(keys -> {
				SonarHistory sonarHistory = SonarHistory.builder().processorItemId(refHistory.getProcessorItemId()).date(end)
						.timestamp(end).key(keys).name(keys).branch(refHistory.getBranch()).metrics(metricsList).build();
				historyMap.put(keys, sonarHistory);
			});
		}

		return historyMap;
	}

	/**
	 * create data count values
	 *
	 * @param history
	 *          sonarhistory
	 * @param date
	 *          node date
	 * @param projectNodePair
	 *          projectNodePair
	 * @param projectList
	 *          projectList
	 * @param debtList
	 *          db debtList
	 * @param projectWiseDataMap
	 *          projectWiseDataMap
	 * @param versionDate
	 *          versionDate
	 */
	private void prepareSqualeList(Map<String, SonarHistory> history, String date, Pair<String, String> projectNodePair,
			List<String> projectList, List<String> debtList, Map<String, List<DataCount>> projectWiseDataMap,
			List<String> versionDate) {
		List<Long> dateWiseDebtList = new ArrayList<>();
		history.values().forEach(sonarDetails -> {
			Map<String, Object> metricMap = sonarDetails.getMetrics().stream()
					.filter(metricValue -> metricValue.getMetricValue() != null)
					.collect(Collectors.toMap(SonarMetric::getMetricName, SonarMetric::getMetricValue));

			final Long squaleRatingValue = getSqualeRatingValue(metricMap.get(SQALE_RATING));
			String keyName = prepareSonarKeyName(projectNodePair.getValue(), sonarDetails.getName(),
					sonarDetails.getBranch());
			DataCount dcObj = getDataCount(squaleRatingValue, projectNodePair.getValue(), date);
			projectWiseDataMap.computeIfAbsent(keyName, k -> new ArrayList<>()).add(dcObj);
			projectList.add(keyName);
			versionDate.add(date);
			dateWiseDebtList.add(squaleRatingValue);
			debtList.add(String.valueOf(squaleRatingValue));
		});
		DataCount dcObj = getDataCount(calculateKpiValue(dateWiseDebtList, KPICode.SONAR_CODE_QUALITY.getKpiId()),
				projectNodePair.getValue(), date);
		projectWiseDataMap.computeIfAbsent(CommonConstant.OVERALL, k -> new ArrayList<>()).add(dcObj);
	}

	/**
	 * @param sqlIndex
	 *          sqlIndex
	 * @return squale Value
	 */
	public Long getSqualeRatingValue(Object sqlIndex) {
		long squaleValue = 0L;
		if (sqlIndex != null) {
			if (sqlIndex instanceof Double) {
				squaleValue = ((Double) sqlIndex).longValue();
			} else if (sqlIndex instanceof String) {
				squaleValue = Double.valueOf(sqlIndex.toString()).longValue();
			} else {
				squaleValue = (Long) sqlIndex;
			}
		}
		return squaleValue;
	}

	/**
	 * create sonar kpis data count obj
	 *
	 * @param value
	 *          value
	 * @param projectName
	 *          projectName
	 * @param date
	 *          date
	 * @return datacpunt
	 */
	public DataCount getDataCount(Long value, String projectName, String date) {
		DataCount dataCount = new DataCount();
		dataCount.setData(refineQuality(value));
		dataCount.setSProjectName(projectName);
		dataCount.setDate(date);
		dataCount.setValue(value);
		dataCount.setHoverValue(new HashMap<>());
		return dataCount;
	}

	/**
	 * Not used as data is not being calculated sprintwise
	 *
	 * @param leafNodeList
	 *          leafNodeList
	 * @param startDate
	 *          startDate
	 * @param endDate
	 *          endDate
	 * @param kpiRequest
	 *          kpiRequest
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
	public String getQualifierType() {
		return KPICode.SONAR_CODE_QUALITY.name();
	}

	@Override
	public Long calculateKPIMetrics(Map<ObjectId, List<SonarDetails>> sonarDetailsMap) {
		return null;
	}

	/**
	 * Get all the code quality debt data in form of key as sonar-project and value
	 * as techDebt sqale_index value
	 *
	 * @param pList
	 *          : project list of nodes
	 * @param tempMap
	 *          : containing all nodes of the hierarchy with key as node id and
	 *          value as node
	 * @param kpiElement
	 *          : request info
	 * @return map having key as sonar-project and value as techDebt sqale_index
	 *         value
	 */
	public Map<String, Object> getSonarJobWiseKpiData(final List<Node> pList, Map<String, Node> tempMap,
			final KpiElement kpiElement) {
		return new HashMap<>();
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI168(), KPICode.SONAR_CODE_QUALITY.getKpiId());
	}
}
