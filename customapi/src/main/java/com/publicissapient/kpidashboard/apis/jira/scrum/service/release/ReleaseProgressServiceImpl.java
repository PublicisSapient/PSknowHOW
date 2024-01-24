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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.CommonServiceImpl;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.IterationKpiFilters;
import com.publicissapient.kpidashboard.apis.model.IterationKpiFiltersOptions;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.apis.util.ReleaseKpiHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ReleaseProgressServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	private static final String TOTAL_ISSUES = "totalIssues";
	private static final String RELEASE_JIRA_ISSUE_STATUS = "releaseJiraIssueStatus";
	private static final String TO_DO = "To Do";
	private static final String IN_PROGRESS = "In Progress";
	private static final String DONE = "Done";
	private static final String ISSUE_COUNT = "Issue Count";
	private static final String STORY_POINT = "Story Point";
	private static final String OVERALL = "Overall";
	private static final String SEARCH_BY_ISSUE_TYPE = "Filter by issue type";
	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private CommonServiceImpl commonService;

	@Override
	public Integer calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);
		if (null != leafNode) {
			log.info("Release Progress -> Requested sprint : {}", leafNode.getName());
			List<JiraIssue> releaseIssues = getFilteredReleaseJiraIssuesFromBaseClass(null);
			resultListMap.put(TOTAL_ISSUES, releaseIssues);
			JiraIssueReleaseStatus jiraIssueReleaseStatus = getJiraIssueReleaseStatus();
			resultListMap.put(RELEASE_JIRA_ISSUE_STATUS, jiraIssueReleaseStatus);
		}
		return resultListMap;
	}

	@Override
	public String getQualifierType() {
		return KPICode.RELEASE_PROGRESS.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.RELEASE) {
				releaseWiseLeafNodeValue(v, kpiElement, kpiRequest);
			}
		});
		log.info("ReleaseProgressServiceImpl -> getKpiData ->  : {}", kpiElement);
		return kpiElement;
	}

	private void releaseWiseLeafNodeValue(List<Node> releaseLeafNodeList, KpiElement kpiElement,
			KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();
		List<KPIExcelData> excelData = new ArrayList<>();
		List<Node> latestReleaseNode = new ArrayList<>();
		Node latestRelease = releaseLeafNodeList.get(0);

		if (latestRelease != null) {
			Object basicProjectConfigId = latestRelease.getProjectFilter().getBasicProjectConfigId();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			Optional.ofNullable(latestRelease).ifPresent(latestReleaseNode::add);
			Map<String, Object> resultMap = fetchKPIDataFromDb(latestReleaseNode, null, null, kpiRequest);
			List<JiraIssue> releaseIssues = (List<JiraIssue>) resultMap.get(TOTAL_ISSUES);
			JiraIssueReleaseStatus jiraIssueReleaseStatus = (JiraIssueReleaseStatus) resultMap
					.get(RELEASE_JIRA_ISSUE_STATUS);
			List<IterationKpiValue> filterDataList = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(releaseIssues) && jiraIssueReleaseStatus != null) {
				Set<String> issueTypes = new LinkedHashSet<>();
				issueTypes.add(CommonConstant.OVERALL);
				createDataCountGroupMap(releaseIssues, jiraIssueReleaseStatus, issueTypes, fieldMapping,
						filterDataList);
				populateExcelDataObject(requestTrackerId, excelData, releaseIssues, fieldMapping);
				List<DataCount> dataCountList = new ArrayList<>();
				dataCountList.add(getStatusWiseCountList(releaseIssues, jiraIssueReleaseStatus));
				dataCountList.add(getStatusWiseStoryPointList(releaseIssues, fieldMapping, jiraIssueReleaseStatus));
				IterationKpiValue overAllIterationKpiValue = new IterationKpiValue();
				overAllIterationKpiValue.setFilter1(OVERALL);
				overAllIterationKpiValue.setFilter2(OVERALL);
				overAllIterationKpiValue.setValue(dataCountList);
				filterDataList.add(overAllIterationKpiValue);
				IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_ISSUE_TYPE, issueTypes);
				IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, null);
				kpiElement.setFilters(iterationKpiFilters);
				kpiElement.setSprint(latestRelease.getName());
				kpiElement.setModalHeads(KPIExcelColumn.RELEASE_PROGRESS.getColumns());
				kpiElement.setExcelColumns(KPIExcelColumn.RELEASE_PROGRESS.getColumns());
				kpiElement.setExcelData(excelData);
			}
			kpiElement.setTrendValueList(filterDataList);
		}
	}

	public void createDataCountGroupMap(List<JiraIssue> jiraIssueList, JiraIssueReleaseStatus jiraIssueReleaseStatus,
			Set<String> issueTypes, FieldMapping fieldMapping, List<IterationKpiValue> iterationKpiValues) {
		Map<String, List<JiraIssue>> typeWiseIssues = jiraIssueList.stream()
				.collect(Collectors.groupingBy(JiraIssue::getTypeName));

		typeWiseIssues.forEach((issueType, issues) -> {
			List<DataCount> dataCountList = new ArrayList<>();
			issueTypes.add(issueType);
			dataCountList.add(getStatusWiseCountList(issues, jiraIssueReleaseStatus));
			dataCountList.add(getStatusWiseStoryPointList(issues, fieldMapping, jiraIssueReleaseStatus));
			IterationKpiValue iterationKpiValue = new IterationKpiValue();
			iterationKpiValue.setFilter1(issueType);
			iterationKpiValue.setValue(dataCountList);
			iterationKpiValues.add(iterationKpiValue);
		});

	}

	/**
	 * Create statusWiseIssueCountList
	 * 
	 * @param jiraIssueList
	 * @param jiraIssueReleaseStatus
	 * @return
	 */
	private DataCount getStatusWiseCountList(List<JiraIssue> jiraIssueList,
			JiraIssueReleaseStatus jiraIssueReleaseStatus) {
		DataCount issueCountDc = new DataCount();
		List<DataCount> issueCountDcList = new ArrayList<>();
		List<JiraIssue> toDoJiraIssue = ReleaseKpiHelper.filterIssuesByStatus(jiraIssueList,
				jiraIssueReleaseStatus.getToDoList());
		List<JiraIssue> inProgressJiraIssue = ReleaseKpiHelper.filterIssuesByStatus(jiraIssueList,
				jiraIssueReleaseStatus.getInProgressList());
		List<JiraIssue> doneJiraIssue = ReleaseKpiHelper.filterIssuesByStatus(jiraIssueList,
				jiraIssueReleaseStatus.getClosedList());

		long toDoCount = toDoJiraIssue.size();
		Map<String, Integer> toDoStatusMap = toDoJiraIssue.stream()
				.collect(Collectors.groupingBy(JiraIssue::getStatus, Collectors.summingInt(issue -> 1)));
		createIssueCountDrillDown(toDoStatusMap, TO_DO, toDoCount, issueCountDcList);

		long inProgressCount = inProgressJiraIssue.size();
		Map<String, Integer> inProgressStatusMap = inProgressJiraIssue.stream()
				.collect(Collectors.groupingBy(JiraIssue::getStatus, Collectors.summingInt(issue -> 1)));
		createIssueCountDrillDown(inProgressStatusMap, IN_PROGRESS, inProgressCount, issueCountDcList);

		long doneCount = doneJiraIssue.size();
		Map<String, Integer> doneStatusMap = (doneJiraIssue.stream()
				.collect(Collectors.groupingBy(JiraIssue::getStatus, Collectors.summingInt(issue -> 1))));
		createIssueCountDrillDown(doneStatusMap, DONE, doneCount, issueCountDcList);

		issueCountDc.setData(String.valueOf(toDoCount + inProgressCount + doneCount));
		issueCountDc.setValue(issueCountDcList);
		issueCountDc.setKpiGroup(ISSUE_COUNT);
		return issueCountDc;
	}

	/**
	 * Create StatusWiseStoryPointList
	 * 
	 * @param jiraIssueList
	 * @param fieldMapping
	 * @param jiraIssueReleaseStatus
	 * @return
	 */
	private DataCount getStatusWiseStoryPointList(List<JiraIssue> jiraIssueList, FieldMapping fieldMapping,
			JiraIssueReleaseStatus jiraIssueReleaseStatus) {
		DataCount storyPointDc = new DataCount();
		List<DataCount> storyPointDcList = new ArrayList<>();
		List<JiraIssue> toDoJiraIssue = ReleaseKpiHelper.filterIssuesByStatus(jiraIssueList,
				jiraIssueReleaseStatus.getToDoList());
		List<JiraIssue> inProgressJiraIssue = ReleaseKpiHelper.filterIssuesByStatus(jiraIssueList,
				jiraIssueReleaseStatus.getInProgressList());
		List<JiraIssue> doneJiraIssue = ReleaseKpiHelper.filterIssuesByStatus(jiraIssueList,
				jiraIssueReleaseStatus.getClosedList());

		double toDoSp = KpiDataHelper.calculateStoryPoints(toDoJiraIssue, fieldMapping);
		Map<String, Double> toDoSpMap = createStatusWiseSpMap(toDoJiraIssue, fieldMapping);
		createSpDrillDown(toDoSpMap, TO_DO, toDoSp, storyPointDcList);

		double inProgressSp = KpiDataHelper.calculateStoryPoints(inProgressJiraIssue, fieldMapping);
		Map<String, Double> inProgressSpMap = createStatusWiseSpMap(inProgressJiraIssue, fieldMapping);
		createSpDrillDown(inProgressSpMap, IN_PROGRESS, inProgressSp, storyPointDcList);

		double doneSp = KpiDataHelper.calculateStoryPoints(doneJiraIssue, fieldMapping);
		Map<String, Double> doneSpMap = createStatusWiseSpMap(doneJiraIssue, fieldMapping);
		createSpDrillDown(doneSpMap, DONE, doneSp, storyPointDcList);

		storyPointDc.setData(String.valueOf(inProgressSp + toDoSp + doneSp));
		storyPointDc.setValue(storyPointDcList);
		storyPointDc.setKpiGroup(STORY_POINT);
		return storyPointDc;
	}

	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			List<JiraIssue> jiraIssueList, FieldMapping fieldMapping) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& CollectionUtils.isNotEmpty(jiraIssueList)) {
			KPIExcelUtility.populateReleaseDefectRelatedExcelData(jiraIssueList, excelData, fieldMapping);
		}
	}

	/**
	 * Create statusWise storyPoint/OriginalEstimate map
	 * 
	 * @param jiraIssueList
	 * @param fieldMapping
	 * @return
	 */
	private Map<String, Double> createStatusWiseSpMap(List<JiraIssue> jiraIssueList, FieldMapping fieldMapping) {
		return jiraIssueList.stream().collect(Collectors.groupingBy(JiraIssue::getStatus, Collectors.summingDouble(
				jiraIssue -> KpiDataHelper.calculateStoryPoints(Collections.singletonList(jiraIssue), fieldMapping))));
	}

	/**
	 * Create issueCountDrillDown
	 * 
	 * @param issueCountStatusMap
	 * @param releaseStatus
	 * @param releaseStatusCount
	 * @param issueCountDcList
	 */
	private static void createIssueCountDrillDown(Map<String, Integer> issueCountStatusMap, String releaseStatus,
			long releaseStatusCount, List<DataCount> issueCountDcList) {
		List<DataCount> drillDownList = new ArrayList<>();
		issueCountStatusMap.forEach((status, count) -> drillDownList.add(new DataCount(status, count, null)));
		DataCount releaseStatusDc = new DataCount(releaseStatus, releaseStatusCount, drillDownList);
		issueCountDcList.add(releaseStatusDc);
	}

	/**
	 * Create storyPointDrillDown
	 * 
	 * @param spStatusMap
	 * @param releaseStatus
	 * @param releaseStatusSp
	 * @param storyPointDcList
	 */
	private static void createSpDrillDown(Map<String, Double> spStatusMap, String releaseStatus, double releaseStatusSp,
			List<DataCount> storyPointDcList) {
		List<DataCount> spDrillDownList = new ArrayList<>();
		spStatusMap.forEach((status, spVal) -> spDrillDownList.add(new DataCount(status, spVal, null)));
		DataCount spDc = new DataCount(releaseStatus, releaseStatusSp, spDrillDownList);
		storyPointDcList.add(spDc);
	}

}
