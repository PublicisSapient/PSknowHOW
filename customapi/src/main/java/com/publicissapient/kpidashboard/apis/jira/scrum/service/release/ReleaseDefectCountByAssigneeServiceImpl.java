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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;

import lombok.extern.slf4j.Slf4j;

// Defect count by Assignee kpi on release tab
@Slf4j
@Component
public class ReleaseDefectCountByAssigneeServiceImpl
		extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	private static final String TOTAL_DEFECT = "Total Defects";
	private static final String OPEN_DEFECT = "Open Defects";

	@Autowired
	private ConfigHelperService configHelperService;

	@Override
	public Integer calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

	@Override
	public String getQualifierType() {
		return KPICode.DEFECT_COUNT_BY_ASSIGNEE_RELEASE.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.RELEASE) {
				releaseWiseLeafNodeValue(v, kpiElement, kpiRequest);
			}
		});
		log.info("ReleaseDefectCountByAssigneeServiceImpl -> getKpiData ->  : {}", kpiElement);
		return kpiElement;
	}

	/**
	 * @param releaseLeafNodeList
	 * @param kpiElement
	 * @param kpiRequest
	 */
	private void releaseWiseLeafNodeValue(List<Node> releaseLeafNodeList, KpiElement kpiElement,
			KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();
		List<KPIExcelData> excelData = new ArrayList<>();
		List<Node> latestReleaseNode = new ArrayList<>();
		Node latestRelease = releaseLeafNodeList.get(0);
		Optional.ofNullable(latestRelease).ifPresent(latestReleaseNode::add);
		if (latestRelease != null) {
			Map<String, Object> resultMap = fetchKPIDataFromDb(latestReleaseNode, null, null, kpiRequest);
			List<JiraIssue> totalDefects = (List<JiraIssue>) resultMap.get(TOTAL_DEFECT);
			List<IterationKpiValue> filterDataList = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(totalDefects)) {
				Object basicProjectConfigId = latestRelease.getProjectFilter().getBasicProjectConfigId();
				FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
				List<JiraIssue> openDefects = totalDefects.stream()
						.filter(jiraIssue -> fieldMapping.getStoryFirstStatus().contains(jiraIssue.getStatus()))
						.collect(Collectors.toList());
				Map<String, Map<String, List<JiraIssue>>> assigneeWiseList = getAssigneeWiseList(totalDefects,
						openDefects);
				log.info("ReleaseDefectCountByAssigneeServiceImpl -> assigneeWiseList ->  : {}", assigneeWiseList);
				List<IterationKpiValue> sortedFilterDataList = new ArrayList<>();
				List<DataCount> dataCountListForAllAssignee = new ArrayList<>();
				for (Map.Entry<String, Map<String, List<JiraIssue>>> entry : assigneeWiseList.entrySet()) {
					Map<String, Integer> assigneeWiseCountMap = new HashMap<>();
					Map<String, List<JiraIssue>> assigneeData = entry.getValue();
					getAssigneeWiseCount(assigneeData, assigneeWiseCountMap);
					int issueCount = assigneeData.values().stream().mapToInt(List::size).sum();
					DataCount assigneeDataCount = new DataCount();
					assigneeDataCount.setData(String.valueOf(issueCount));
					assigneeDataCount.setValue(assigneeWiseCountMap);
					List<DataCount> dataCountList = new ArrayList<>();
					dataCountList.add(assigneeDataCount);
					dataCountListForAllAssignee.add(assigneeDataCount);

					List<DataCount> middleTrendValueListForAssignee = new ArrayList<>();
					DataCount middleOverallData = new DataCount();
					middleOverallData.setData(latestRelease.getProjectFilter().getName());
					middleOverallData.setValue(dataCountList);
					middleTrendValueListForAssignee.add(middleOverallData);

					IterationKpiValue filterData = new IterationKpiValue(entry.getKey(),
							middleTrendValueListForAssignee);
					filterDataList.add(filterData);
				}

				Map<String, Integer> overallAssigneeCountMapAggregate = new HashMap<>();
				overallAssigneeCountMap(dataCountListForAllAssignee, overallAssigneeCountMapAggregate);
				if (MapUtils.isNotEmpty(overallAssigneeCountMapAggregate)) {
					populateExcelDataObject(requestTrackerId, excelData, totalDefects, fieldMapping);

					// filterDataList
					kpiElement.setSprint(latestRelease.getName());
					kpiElement.setModalHeads(KPIExcelColumn.DEFECT_COUNT_BY_ASSIGNEE_RELEASE.getColumns());
					kpiElement.setExcelColumns(KPIExcelColumn.DEFECT_COUNT_BY_ASSIGNEE_RELEASE.getColumns());
					kpiElement.setExcelData(excelData);
					sortedFilterDataList.add(filterDataList.stream()
							.filter(iterationKpiValue -> iterationKpiValue.getFilter1().equalsIgnoreCase(OPEN_DEFECT))
							.findFirst().orElse(new IterationKpiValue()));
					filterDataList.removeIf(
							iterationKpiValue -> iterationKpiValue.getFilter1().equalsIgnoreCase(OPEN_DEFECT));
					sortListByKey(filterDataList);
					sortedFilterDataList.addAll(filterDataList);
					kpiElement.setTrendValueList(sortedFilterDataList);
					log.info("ReleaseDefectCountByAssigneeServiceImpl -> request id : {} total jira Issues : {}",
							requestTrackerId, filterDataList.get(0));
				}
			}
		}
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);
		if (null != leafNode) {
			log.info("Defect count by Assignee Release -> Requested sprint : {}", leafNode.getName());
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
	 * create assignee wise map of total and open defects
	 * 
	 * @param defectJiraIssueList
	 * @param openIssues
	 * @return
	 */
	private Map<String, Map<String, List<JiraIssue>>> getAssigneeWiseList(List<JiraIssue> defectJiraIssueList,
			List<JiraIssue> openIssues) {
		Map<String, Map<String, List<JiraIssue>>> scopeWiseDefectsMap = new HashMap<>();
		Collector<JiraIssue, ?, Map<String, List<JiraIssue>>> groupingByAssignee = Collectors
				.groupingBy(JiraIssue::getAssigneeName);
		Predicate<JiraIssue> hasNonEmptyRootCauseList = jiraIssue -> {
			if (StringUtils.isEmpty(jiraIssue.getAssigneeName())) {
				jiraIssue.setAssigneeName("-");
			}
			return true;
		};
		scopeWiseDefectsMap.put(TOTAL_DEFECT,
				defectJiraIssueList.stream().filter(hasNonEmptyRootCauseList).collect(groupingByAssignee));
		scopeWiseDefectsMap.put(OPEN_DEFECT,
				openIssues.stream().filter(hasNonEmptyRootCauseList).collect(groupingByAssignee));
		return scopeWiseDefectsMap;
	}

	/**
	 * create assignee wise count map of total and open defects
	 * 
	 * @param assigneeData
	 * @param assigneeCountMap
	 * @return
	 */
	private static void getAssigneeWiseCount(Map<String, List<JiraIssue>> assigneeData,
			Map<String, Integer> assigneeCountMap) {
		for (Map.Entry<String, List<JiraIssue>> assigneeEntry : assigneeData.entrySet()) {
			String assignee = assigneeEntry.getKey();
			List<JiraIssue> issues = assigneeEntry.getValue();
			assigneeCountMap.put(assignee, issues.size());
		}
	}

	/**
	 * create map of data count by filter
	 * 
	 * @param dataCountListForAllAssignees
	 * @param overallAssigneeCountMapAggregate
	 */
	private static void overallAssigneeCountMap(List<DataCount> dataCountListForAllAssignees,
			Map<String, Integer> overallAssigneeCountMapAggregate) {
		for (DataCount dataCount : dataCountListForAllAssignees) {
			Map<String, Integer> statusCountMap = (Map<String, Integer>) dataCount.getValue();
			statusCountMap.forEach((assignee, assigneeCountValue) -> overallAssigneeCountMapAggregate.merge(assignee,
					assigneeCountValue, Integer::sum));
		}
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

	private void sortListByKey(List<IterationKpiValue> list) {
		list.sort(Comparator.comparing(IterationKpiValue::getFilter1));
	}
}
