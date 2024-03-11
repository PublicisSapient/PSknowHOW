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

package com.publicissapient.kpidashboard.apis.jira.scrum.service.release;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.releasedashboard.JiraReleaseKPIService;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;

import lombok.extern.slf4j.Slf4j;

// Defect count by RCA kpi on release tab
@Slf4j
@Component
public class ReleaseDefectCountByRCAServiceImpl extends JiraReleaseKPIService {

	private static final String TOTAL_DEFECT = "Total Defects";
	private static final String OPEN_DEFECT = "Open Defects";
	private static final String UNDEFINED = "Undefined";
	private static final String NONE = "-";
	@Autowired
	private ConfigHelperService configHelperService;

	@Override
	public String getQualifierType() {
		return KPICode.DEFECT_COUNT_BY_RCA_RELEASE.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node releaseNode)
			throws ApplicationException {
		releaseWiseLeafNodeValue(releaseNode, kpiElement, kpiRequest);
		log.info("ReleaseDefectCountByRCAServiceImpl -> getKpiData ->  : {}", kpiElement);
		return kpiElement;
	}

	/**
	 * @param latestRelease
	 * @param kpiElement
	 * @param kpiRequest
	 */
	private void releaseWiseLeafNodeValue(Node latestRelease, KpiElement kpiElement, KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();
		List<KPIExcelData> excelData = new ArrayList<>();
		List<IterationKpiValue> overAllIterationKpiValue = new ArrayList<>();
		if (latestRelease != null) {
			Map<String, Object> resultMap = fetchKPIDataFromDb(latestRelease, null, null, kpiRequest);
			Object basicProjectConfigId = latestRelease.getProjectFilter().getBasicProjectConfigId();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			List<JiraIssue> totalDefects = (List<JiraIssue>) resultMap.get(TOTAL_DEFECT);
			Set<String> jiraDodKPI142LowerCase = new HashSet<>();
			if (fieldMapping.getJiraDodKPI142() != null) {
				jiraDodKPI142LowerCase = fieldMapping.getJiraDodKPI142().stream().map(String::toLowerCase)
						.collect(Collectors.toSet());
			}

			Set<String> finalJiraDodKPI142LowerCase = jiraDodKPI142LowerCase;
			List<JiraIssue> openDefects = totalDefects.stream()
					.filter(jiraIssue -> fieldMapping.getStoryFirstStatus().contains(jiraIssue.getStatus())
							&& (finalJiraDodKPI142LowerCase.isEmpty()
									|| !finalJiraDodKPI142LowerCase.contains(jiraIssue.getStatus().toLowerCase())))
					.collect(Collectors.toList());
			IterationKpiValue openDefectsIterationKpiValue = new IterationKpiValue();
			openDefectsIterationKpiValue.setFilter1(OPEN_DEFECT);
			openDefectsIterationKpiValue.setValue(getDefectsDataCountList(openDefects, fieldMapping));

			IterationKpiValue totalDefectsIterationKpiValue = new IterationKpiValue();
			totalDefectsIterationKpiValue.setFilter1(TOTAL_DEFECT);
			totalDefectsIterationKpiValue.setValue(getDefectsDataCountList(totalDefects, fieldMapping));

			overAllIterationKpiValue.add(openDefectsIterationKpiValue);
			overAllIterationKpiValue.add(totalDefectsIterationKpiValue);
			populateExcelDataObject(requestTrackerId, excelData, totalDefects, fieldMapping);
			kpiElement.setSprint(latestRelease.getName());
			kpiElement.setModalHeads(KPIExcelColumn.DEFECT_COUNT_BY_RCA_RELEASE.getColumns());
			kpiElement.setExcelColumns(KPIExcelColumn.DEFECT_COUNT_BY_RCA_RELEASE.getColumns());
			kpiElement.setExcelData(excelData);
		}
		kpiElement.setTrendValueList(overAllIterationKpiValue);
	}

	private List<DataCount> getDefectsDataCountList(List<JiraIssue> defects, FieldMapping fieldMapping) {
		List<DataCount> defectsDataCountList = new ArrayList<>();
		List<JiraIssue> filteredJiraIssue = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(defects)) {
			updateRCAAndTestingPhase(defects);
			Set<String> testingPhases = findUniqueTestingPhases(defects);
			Set<String> RCAList = findUniqueRCAList(defects);
			testingPhases.forEach(testingPhase -> {
				Map<String, List<JiraIssue>> testingPhaseRCAWiseJiraIssue = getTestingPhaseRCAWiseJiraIssue(defects,
						testingPhase, RCAList);
				DataCount defectsDataCount = new DataCount();
				defectsDataCount.setSSprintName(testingPhase);
				List<DataCount> dataCountRCA = new ArrayList<>();
				if (MapUtils.isNotEmpty(testingPhaseRCAWiseJiraIssue)) {
					testingPhaseRCAWiseJiraIssue.forEach((rca, jiraIssue) -> {
						filteredJiraIssue.addAll(jiraIssue);
						DataCount dataCount = new DataCount();
						dataCount.setSubFilter(rca);
						dataCount.setValue(jiraIssue.size());
						dataCount.setSize(KpiDataHelper.calculateStoryPoints(jiraIssue, fieldMapping));
						dataCountRCA.add(dataCount);
					});
					defectsDataCount.setData(String.valueOf(dataCountRCA.stream()
							.mapToInt(obj -> obj.getValue() != null ? (int) obj.getValue() : 0).sum()));
				} else {
					defectsDataCount.setData(String.valueOf(0));
				}
				defectsDataCount.setValue(dataCountRCA);
				defectsDataCountList.add(defectsDataCount);
			});
		}
		return defectsDataCountList;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(Node leafNode, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		if (null != leafNode) {
			log.info("Defect count by RCA Release -> Requested sprint : {}", leafNode.getName());
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(leafNode.getProjectFilter().getBasicProjectConfigId());

			if (null != fieldMapping) {
				List<JiraIssue> releaseDefects = getFilteredReleaseJiraIssuesFromBaseClass(fieldMapping);
				resultListMap.put(TOTAL_DEFECT, releaseDefects);
			}
		}
		return resultListMap;
	}

	/**
	 * populate excel data
	 *
	 * @param requestTrackerId
	 * @param excelData
	 * @param jiraIssueList
	 * @param fieldMapping
	 */
	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			List<JiraIssue> jiraIssueList, FieldMapping fieldMapping) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& CollectionUtils.isNotEmpty(jiraIssueList)) {
			KPIExcelUtility.populateReleaseDefectRelatedExcelData(jiraIssueList, excelData, fieldMapping);
		}
	}

	private Map<String, List<JiraIssue>> getTestingPhaseRCAWiseJiraIssue(List<JiraIssue> totalDefects,
			String testingPhase, Set<String> RCAList) {
		Map<String, List<JiraIssue>> rcaWiseJiraIssue = new LinkedHashMap<>();
		RCAList.forEach(rca -> rcaWiseJiraIssue.put(rca, filterByStatus(testingPhase, totalDefects, rca)));
		return rcaWiseJiraIssue;
	}

	private Set<String> findUniqueTestingPhases(List<JiraIssue> totalDefects) {
		return totalDefects.stream()
				.collect(
						Collectors.groupingBy(jiraIssue -> CollectionUtils.isNotEmpty(jiraIssue.getEscapedDefectGroup())
								? jiraIssue.getEscapedDefectGroup().stream().findFirst().orElse(UNDEFINED)
								: UNDEFINED))
				.keySet();

	}

	private Set<String> findUniqueRCAList(List<JiraIssue> totalDefects) {
		return totalDefects.stream()
				.collect(Collectors.groupingBy(jiraIssue -> CollectionUtils.isNotEmpty(jiraIssue.getRootCauseList())
						? jiraIssue.getRootCauseList().stream().findFirst().orElse(NONE)
						: NONE))
				.keySet();
	}

	private List<JiraIssue> filterByStatus(String testingPhase, List<JiraIssue> totalDefects, String rca) {
		return totalDefects.stream()
				.filter(jiraIssue -> jiraIssue.getEscapedDefectGroup().stream().findFirst().get()
						.equalsIgnoreCase(testingPhase))
				.filter(jiraIssue -> jiraIssue.getRootCauseList().stream().findFirst().get().equalsIgnoreCase(rca))
				.collect(Collectors.toList());
	}

	private void updateRCAAndTestingPhase(List<JiraIssue> totalDefects) {
		totalDefects.forEach(jiraIssue -> {
			if (CollectionUtils.isEmpty(jiraIssue.getEscapedDefectGroup())) {
				jiraIssue.setEscapedDefectGroup(Arrays.asList(UNDEFINED));
			}
			if (CollectionUtils.isEmpty(jiraIssue.getRootCauseList())) {
				jiraIssue.setEscapedDefectGroup(Arrays.asList(NONE));
			}
		});

	}

}