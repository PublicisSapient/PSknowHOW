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

package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.backlogdashboard.JiraBacklogKPIService;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * This class displays the defects that are presently in the backlog and the
 * active sprint, but have not yet been completed.
 *
 * @author eswbogol
 */
@Slf4j
@Component
public class DefectCountByTypeImpl extends JiraBacklogKPIService<Integer, List<Object>> {

	private static final String PROJECT_WISE_JIRA_ISSUE = "Jira Issue";

	@Autowired
	ConfigHelperService configHelperService;

	@Autowired
	JiraIssueRepository jiraIssueRepository;

	/**
	 * @param kpiRequest
	 *          kpiRequest with request details
	 * @param kpiElement
	 *          basic details of KPI
	 * @param projectNode
	 *          details of project nodes
	 * @return KpiElement with data
	 * @throws ApplicationException
	 *           exception while processing request
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node projectNode)
			throws ApplicationException {
		projectWiseLeafNodeValue(projectNode, kpiElement, kpiRequest);
		log.info("DefectCountByType -> getKpiData ->  : {}", kpiElement);
		return kpiElement;
	}

	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(Node leafNode, KpiElement kpiElement, KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();
		List<KPIExcelData> excelData = new ArrayList<>();
		if (leafNode != null) {
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(leafNode.getProjectFilter().getBasicProjectConfigId());
			Map<String, Object> resultMap = fetchKPIDataFromDb(leafNode, "", "", kpiRequest);
			List<JiraIssue> jiraIssues = (List<JiraIssue>) resultMap.get(PROJECT_WISE_JIRA_ISSUE);
			List<IterationKpiValue> filterDataList = new ArrayList<>();
			Set<String> excludeStatuses = getExcludeStatuses(fieldMapping);
			// method to exclude the issues with status
			jiraIssues = getJiraIssueListAfterDefectsWithStatusExcluded(jiraIssues, excludeStatuses);
			Map<String, List<JiraIssue>> statusWiseIssuesList = new HashMap<>(CollectionUtils.isNotEmpty(jiraIssues)
					? jiraIssues.stream()
							.filter(jiraIssue -> StringUtils.isNotEmpty(jiraIssue.getOriginalType()) &&
									fieldMapping.getJiradefecttype().contains(jiraIssue.getOriginalType()))
							.collect(Collectors.groupingBy(JiraIssue::getOriginalType))
					: new HashMap<>());
			log.info("Defect Count By Type -> request id : {} total jira Issues : {}", requestTrackerId, jiraIssues.size());
			log.info("statusWiseIssuesList ->  : {}", statusWiseIssuesList);
			Map<String, Integer> statusWiseCountMap = new HashMap<>();
			getIssuesStatusCount(statusWiseIssuesList, statusWiseCountMap);
			if (MapUtils.isNotEmpty(statusWiseCountMap)) {
				constructData(kpiElement, requestTrackerId, excelData, leafNode, jiraIssues, filterDataList,
						statusWiseCountMap);
			}
			kpiElement.setTrendValueList(filterDataList);
		}
	}

	/**
	 * @param leafNode
	 *          project node details
	 * @param startDate
	 *          startDate
	 * @param endDate
	 *          endDate
	 * @param kpiRequest
	 *          kpiRequest with request details
	 * @return JiraIssues with Original Types
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(Node leafNode, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		if (leafNode != null) {
			log.info("Defect Count By Type kpi -> Requested project : {}", leafNode.getProjectFilter().getName());
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(leafNode.getProjectFilter().getBasicProjectConfigId());
			List<JiraIssue> totalJiraIssue = getBackLogJiraIssuesFromBaseClass().stream()
					.filter(j -> fieldMapping.getJiradefecttype().contains(j.getOriginalType())).toList();
			resultListMap.put(PROJECT_WISE_JIRA_ISSUE, totalJiraIssue);
		}
		return resultListMap;
	}

	private static Set<String> getExcludeStatuses(FieldMapping fieldMapping) {
		Set<String> excludeStatuses = new HashSet<>();
		excludeStatuses.add(Optional.ofNullable(fieldMapping.getJiraDefectRejectionStatusKPI155()).orElse(""));
		excludeStatuses.add(Optional.ofNullable(fieldMapping.getJiraLiveStatusKPI155()).orElse(""));
		excludeStatuses.addAll(Optional.ofNullable(fieldMapping.getJiraDodKPI155()).isPresent()
				? fieldMapping.getJiraDodKPI155()
				: new HashSet<>());
		return excludeStatuses;
	}

	private static List<JiraIssue> getJiraIssueListAfterDefectsWithStatusExcluded(List<JiraIssue> jiraIssues,
			Set<String> excludeStatuses) {
		Set<String> excludeStatus = excludeStatuses.stream().map(String::toUpperCase).collect(Collectors.toSet());
		jiraIssues = jiraIssues.stream()
				.filter(jiraIssue -> !excludeStatus.contains(jiraIssue.getJiraStatus().toUpperCase()))
				.collect(Collectors.toList());
		return jiraIssues;
	}

	private static void getIssuesStatusCount(Map<String, List<JiraIssue>> statusData,
			Map<String, Integer> statusWiseCountMap) {
		for (Map.Entry<String, List<JiraIssue>> statusEntry : statusData.entrySet()) {
			statusWiseCountMap.put(statusEntry.getKey(), statusEntry.getValue().size());
		}
	}

	private void constructData(KpiElement kpiElement, String requestTrackerId, List<KPIExcelData> excelData,
			Node leafNode, List<JiraIssue> jiraIssues, List<IterationKpiValue> filterDataList,
			Map<String, Integer> statusWiseCountMap) {
		List<DataCount> trendValueListOverAll = new ArrayList<>();
		DataCount overallData = new DataCount();
		int sumOfDefectsCount = statusWiseCountMap.values().stream().mapToInt(Integer::intValue).sum();
		overallData.setData(String.valueOf(sumOfDefectsCount));
		overallData.setValue(statusWiseCountMap);
		overallData.setKpiGroup(CommonConstant.OVERALL);
		overallData.setSProjectName(leafNode.getProjectFilter().getName());
		trendValueListOverAll.add(overallData);
		List<DataCount> middleTrendValueListOverAll = new ArrayList<>();
		DataCount middleOverallData = new DataCount();
		middleOverallData.setData(leafNode.getProjectFilter().getName());
		middleOverallData.setValue(trendValueListOverAll);
		middleTrendValueListOverAll.add(middleOverallData);
		populateExcelDataObject(requestTrackerId, excelData, jiraIssues);
		IterationKpiValue filterDataOverall = new IterationKpiValue(CommonConstant.OVERALL, middleTrendValueListOverAll);
		filterDataList.add(filterDataOverall);
		kpiElement.setModalHeads(KPIExcelColumn.DEFECT_COUNT_BY_TYPE.getColumns());
		kpiElement.setExcelColumns(KPIExcelColumn.DEFECT_COUNT_BY_TYPE.getColumns());
		kpiElement.setExcelData(excelData);
		log.info("DefectCountByTypeImpl -> request id : {} total jira Issues : {}", requestTrackerId,
				filterDataList.get(0));
	}

	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			List<JiraIssue> jiraIssueList) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase()) &&
				CollectionUtils.isNotEmpty(jiraIssueList)) {
			KPIExcelUtility.populateBacklogDefectCountExcelData(jiraIssueList, excelData);
		}
	}

	@Override
	public String getQualifierType() {
		return KPICode.DEFECT_COUNT_BY_TYPE.name();
	}
}
