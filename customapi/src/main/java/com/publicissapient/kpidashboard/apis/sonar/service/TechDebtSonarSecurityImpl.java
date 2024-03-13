/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
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
import com.publicissapient.kpidashboard.apis.sonar.utiils.SonarQualityMetric;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.sonar.SonarDetails;
import com.publicissapient.kpidashboard.common.model.sonar.SonarHistory;

import lombok.extern.slf4j.Slf4j;

/**
 * Sonar Kpi for analysis of security_remediation_effort,measures the effort
 * required to fix all Vulnerabilities detected with Sonar in the code.
 *
 * @author shunaray
 *
 */
@Component
@Slf4j
public class TechDebtSonarSecurityImpl
		extends SonarKPIService<Double, List<Object>, Map<ObjectId, List<SonarDetails>>> {

	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private ConfigHelperService configHelperService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes().get(HIERARCHY_LEVEL_ID_PROJECT);

		getSonarKpiData(projectList, treeAggregatorDetail.getMapTmp(), kpiElement);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(treeAggregatorDetail.getRoot(), nodeWiseKPIValue, KPICode.TECH_DEBT_SONAR_SECURITY);

		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.TECH_DEBT_SONAR_SECURITY);

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		trendValuesMap.forEach((key, dateWiseDataCount) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			dataCountGroup.setFilter(key);
			dataCountGroup.setValue(dateWiseDataCount);
			dataCountGroups.add(dataCountGroup);
		});
		kpiElement.setTrendValueList(dataCountGroups);

		log.debug("[TECH-DEBT-SONAR-SECURITY-AGGREGATED-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), treeAggregatorDetail.getRoot());
		return kpiElement;
	}

	/**
	 * get Sonar kpi data
	 *
	 * @param pList
	 *            projectList
	 * @param tempMap
	 *            tempMap
	 * @param kpiElement
	 *            kpiElement
	 */
	public void getSonarKpiData(List<Node> pList, Map<String, Node> tempMap, KpiElement kpiElement) {
		List<KPIExcelData> excelData = new ArrayList<>();

		getSonarHistoryForAllProjects(pList,
				getScrumCurrentDateToFetchFromDb(CommonConstant.WEEK, (long) customApiConfig.getSonarWeekCount()))
				.forEach((projectNodeId, projectData) -> {
					List<String> projectList = new ArrayList<>();
					List<String> securityRemidiationList = new ArrayList<>();
					List<String> versionDate = new ArrayList<>();
					Map<String, List<DataCount>> projectWiseDataMap = new HashMap<>();
					if (CollectionUtils.isNotEmpty(projectData)) {
						FieldMapping fieldMapping = configHelperService.getFieldMapping(
								tempMap.get(projectNodeId).getProjectFilter().getBasicProjectConfigId());
						// get previous week details as the start date
						LocalDate endDateTime = LocalDate.now().minusWeeks(1);

						for (int i = 0; i < customApiConfig.getSonarWeekCount(); i++) {
							CustomDateRange dateRange = KpiDataHelper.getStartAndEndDateForDataFiltering(endDateTime,
									CommonConstant.WEEK);
							LocalDate weekStartDate = dateRange.getStartDate();
							LocalDate weekEndDate = dateRange.getEndDate();
							String date = getFormattedDate(weekStartDate, weekEndDate);

							Long startMs = weekStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
									.toEpochMilli();
							Long endMs = weekEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

							// find sonar history in the start & end date interval boundary
							Map<String, Pair<SonarHistory, SonarHistory>> history = prepareJobWiseHistoryMapPair(
									projectData, startMs, endMs);
							if (MapUtils.isEmpty(history)) {
								history = prepareEmptyJobWiseHistoryMapPair(projectData,
										Arrays.asList(Constant.SECURITY_REMEDIATION, Constant.N_CLOC), endMs);
							}

							String projectName = projectNodeId.substring(0,
									projectNodeId.lastIndexOf(CommonConstant.UNDERSCORE));

							SonarQualityMetric projOverallMetric = new SonarQualityMetric();
							history.values().forEach(sonarDetailPair -> {
								final double securityRemediationEffort = calculateQualityMetric(sonarDetailPair,
										fieldMapping, projOverallMetric, Constant.SECURITY_REMEDIATION,
										Constant.N_CLOC);

								DataCount dcObj = getDataCount(securityRemediationEffort, projectName, date);
								String keyName = prepareSonarKeyName(projectNodeId, sonarDetailPair.getLeft().getName(),
										sonarDetailPair.getRight().getBranch());
								projectWiseDataMap.computeIfAbsent(keyName, k -> new ArrayList<>()).add(dcObj);
								projectList.add(keyName);
								versionDate.add(date);
								securityRemidiationList.add(String.valueOf(securityRemediationEffort));

							});
							final double overAllVal = roundingOff(projOverallMetric.getTEngineData());

							DataCount dcObj = getDataCount(overAllVal, projectName, date);
							projectWiseDataMap.computeIfAbsent(CommonConstant.OVERALL, k -> new ArrayList<>())
									.add(dcObj);

							endDateTime = endDateTime.minusWeeks(1);
						}
						tempMap.get(projectNodeId).setValue(projectWiseDataMap);
						if (getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
							KPIExcelUtility.populateSonarKpisExcelData(
									tempMap.get(projectNodeId).getProjectFilter().getName(), projectList,
									securityRemidiationList, versionDate, excelData,
									KPICode.TECH_DEBT_SONAR_SECURITY.getKpiId());
						}
					}
				});

		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.TECH_DEBT_SONAR_SECURITY.getColumns());
	}

	/**
	 * Not used as data is not being calculated sprintWise
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiId) {
		return calculateKpiValueForDouble(valueList, kpiId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getQualifierType() {
		return KPICode.TECH_DEBT_SONAR_SECURITY.name();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Double calculateKPIMetrics(Map<ObjectId, List<SonarDetails>> sonarDetailsMap) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI174(),
				KPICode.TECH_DEBT_SONAR_SECURITY.getKpiId());
	}

}
