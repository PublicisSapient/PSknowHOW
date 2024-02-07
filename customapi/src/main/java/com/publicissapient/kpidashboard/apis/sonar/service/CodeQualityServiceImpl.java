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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
 *
 */
@Component
@Slf4j
public class CodeQualityServiceImpl extends SonarKPIService<Long, List<Object>, Map<ObjectId, List<SonarDetails>>> {

	private static final String SQALE_RATING = "sqale_rating";

	@Autowired
	private CustomApiConfig customApiConfig;

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes().get(HIERARCHY_LEVEL_ID_PROJECT);

		getSonarKpiData(projectList, treeAggregatorDetail.getMapTmp(), kpiElement);

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
	 *            prpjectlIST
	 * @param tempMap
	 *            tempMap
	 * @param kpiElement
	 *            kpiElement
	 */
	public void getSonarKpiData(List<Node> pList, Map<String, Node> tempMap, KpiElement kpiElement) {
		List<KPIExcelData> excelData = new ArrayList<>();

		getSonarHistoryForAllProjects(pList, getScrumCurrentDateToFetchFromDb(CommonConstant.MONTH,
				Long.valueOf(customApiConfig.getSonarMonthCount()))).forEach((projectNodeId, projectData) -> {
					List<String> projectList = new ArrayList<>();
					List<String> debtList = new ArrayList<>();
					List<String> versionDate = new ArrayList<>();
					Map<String, List<DataCount>> projectWiseDataMap = new HashMap<>();
					if (CollectionUtils.isNotEmpty(projectData)) {
						// get previous month details as the start date
						LocalDate endDateTime = LocalDate.now().minusMonths(1);

						for (int i = 0; i < customApiConfig.getSonarMonthCount(); i++) {
							CustomDateRange dateRange = KpiDataHelper.getStartAndEndDateForDataFiltering(endDateTime,
									CommonConstant.MONTH);
							LocalDate monthStartDate = dateRange.getStartDate();
							LocalDate monthEndDate = dateRange.getEndDate();
							String date = DateUtil.dateTimeConverter(monthStartDate.toString(), DateUtil.DATE_FORMAT,
									DateUtil.DISPLAY_DATE_FORMAT) + " to "
									+ DateUtil.dateTimeConverter(monthEndDate.toString(), DateUtil.DATE_FORMAT,
											DateUtil.DISPLAY_DATE_FORMAT);

							Long startms = monthStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
									.toEpochMilli();
							Long endms = monthEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

							// create sonarhistory map for all the x-axis points
							Map<String, SonarHistory> history = prepareJobwiseHistoryMap(projectData, startms, endms);
							if (MapUtils.isEmpty(history)) {
								history = prepareEmptyJobWiseHistoryMap(projectData, endms);
							}

							prepareSqualeList(history, date, projectNodeId, projectList, debtList, projectWiseDataMap,
									versionDate);

							endDateTime = endDateTime.minusMonths(1);
						}
						tempMap.get(projectNodeId).setValue(projectWiseDataMap);
						if (getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
							KPIExcelUtility.populateSonarKpisExcelData(
									tempMap.get(projectNodeId).getProjectFilter().getName(), projectList, debtList,
									versionDate, excelData, KPICode.SONAR_CODE_QUALITY.getKpiId());
						}
					}
				});

		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.CODE_QUALITY.getColumns());
	}

	/**
	 * prepare dummy data for empty responses
	 * 
	 * @param sonarHistoryList
	 *            sonarHistoryList
	 * @param end
	 *            endDate
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

			List<String> uniqueKeys = sonarHistoryList.stream().map(SonarHistory::getKey).distinct()
					.collect(Collectors.toList());
			uniqueKeys.forEach(keys -> {
				SonarHistory sonarHistory = SonarHistory.builder().processorItemId(refHistory.getProcessorItemId())
						.date(end).timestamp(end).key(keys).name(keys).branch(refHistory.getBranch())
						.metrics(metricsList).build();
				historyMap.put(keys, sonarHistory);
			});
		}

		return historyMap;
	}

	/**
	 * create data count values
	 * 
	 * @param history
	 *            sonarhistory
	 * @param date
	 *            node date
	 * @param projectNodeId
	 *            projectNodeId
	 * @param projectList
	 *            projectList
	 * @param debtList
	 *            db debtList
	 * @param projectWiseDataMap
	 *            projectWiseDataMap
	 * @param versionDate
	 *            versionDate
	 */
	private void prepareSqualeList(Map<String, SonarHistory> history, String date, String projectNodeId,
			List<String> projectList, List<String> debtList, Map<String, List<DataCount>> projectWiseDataMap,
			List<String> versionDate) {
		String projectName = projectNodeId.substring(0, projectNodeId.lastIndexOf(CommonConstant.UNDERSCORE));
		List<Long> dateWiseDebtList = new ArrayList<>();
		history.values().forEach(sonarDetails -> {

			Map<String, Object> metricMap = sonarDetails.getMetrics().stream()
					.filter(metricValue -> metricValue.getMetricValue() != null)
					.collect(Collectors.toMap(SonarMetric::getMetricName, SonarMetric::getMetricValue));

			final Long squaleRatingValue = getSqualeRatingValue(metricMap.get(SQALE_RATING));
			String keyName = prepareSonarKeyName(projectNodeId, sonarDetails.getName(), sonarDetails.getBranch());
			DataCount dcObj = getDataCount(squaleRatingValue, projectName, date);
			projectWiseDataMap.computeIfAbsent(keyName, k -> new ArrayList<>()).add(dcObj);
			projectList.add(keyName);
			versionDate.add(date);
			dateWiseDebtList.add(squaleRatingValue);
			debtList.add(String.valueOf(squaleRatingValue));

		});
		DataCount dcObj = getDataCount(calculateKpiValue(dateWiseDebtList, KPICode.SONAR_CODE_QUALITY.getKpiId()),
				projectName, date);
		projectWiseDataMap.computeIfAbsent(CommonConstant.OVERALL, k -> new ArrayList<>()).add(dcObj);
	}

	/**
	 *
	 * @param sqlIndex
	 *            sqlIndex
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
	 *            value
	 * @param projectName
	 *            projectName
	 * @param date
	 *            date
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
	 *            leafNodeList
	 * @param startDate
	 *            startDate
	 * @param endDate
	 *            endDate
	 * @param kpiRequest
	 *            kpiRequest
	 * @return {@code Map<ObjectId, List<SonarDetails>>}
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
	 *            : project list of nodes
	 * @param tempMap
	 *            : containing all nodes of the hierarchy with key as node id and
	 *            value as node
	 * @param kpiElement
	 *            : request info
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