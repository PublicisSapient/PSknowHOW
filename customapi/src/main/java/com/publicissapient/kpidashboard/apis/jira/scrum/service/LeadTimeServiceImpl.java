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

package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.JiraFeatureHistory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.AggregationUtils;
import com.publicissapient.kpidashboard.apis.util.BacklogKpiHelper;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.model.application.CycleTime;
import com.publicissapient.kpidashboard.common.model.application.CycleTimeValidationData;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * lead time kpi is a line graph to depict the lead time of issues in past 6
 * months with trends
 * author @shi6
 */
@Component
@Slf4j
public class LeadTimeServiceImpl extends JiraKPIService<Long, List<Object>, Map<String, Object>> {
	private static final String STORY_HISTORY_DATA = "storyHistoryData";
	public static final String ISSUES = "issues";
	private static final String ISSUE_COUNT = "Issue Count";
	private static final String LEAD_TIME = "LEAD TIME";

	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Override
	public Long calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return 0L;
	}

	@Override
	public String getQualifierType() {
		return KPICode.LEAD_TIME.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		log.info("LEAD-TIME -> requestTrackerId[{}]", kpiRequest.getRequestTrackerId());
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		treeAggregatorDetail.getMapOfListOfProjectNodes().forEach((k, v) -> {

			Filters filters = Filters.getFilter(k);
			if (Filters.PROJECT == filters) {
				projectWiseLeafNodeValue(kpiElement, mapTmp, v, kpiRequest);
			}
		});

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.LEAD_TIME);
		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.LEAD_TIME);

		Map<String, Map<String, List<DataCount>>> priorityTypeProjectWiseDc = new LinkedHashMap<>();
		trendValuesMap.forEach((priority, dataCounts) -> {
			Map<String, List<DataCount>> projectWiseDc = dataCounts.stream()
					.collect(Collectors.groupingBy(DataCount::getData));
			priorityTypeProjectWiseDc.put(priority, projectWiseDc);
		});

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		priorityTypeProjectWiseDc.forEach((filter, projectWiseDc) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			List<DataCount> dataList = new ArrayList<>();
			projectWiseDc.forEach((key, value) -> dataList.addAll(value));
			dataCountGroup.setFilter(filter);
			dataCountGroup.setValue(dataList);
			dataCountGroups.add(dataCountGroup);
		});
		kpiElement.setTrendValueList(dataCountGroups);
		kpiElement.setNodeWiseKPIValue(nodeWiseKPIValue);

		log.debug("[LEAD-TIME-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);
		return kpiElement;
	}

	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(KpiElement kpiElement, Map<String, Node> mapTmp, List<Node> leafNodeList,
			KpiRequest kpiRequest) {

		List<KPIExcelData> excelData = new ArrayList<>();
		List<String> rangeList = customApiConfig.getLeadTimeRange();
		Map<String, Object> resultMap = fetchKPIDataFromDb(leafNodeList, LocalDate.now().minusMonths(6).toString(),
				LocalDate.now().toString(), kpiRequest);
		List<JiraIssueCustomHistory> ticketList = (List<JiraIssueCustomHistory>) resultMap.get(STORY_HISTORY_DATA);

		Map<String, List<JiraIssueCustomHistory>> projectWiseJiraIssue = ticketList.stream()
				.collect(Collectors.groupingBy(JiraIssueCustomHistory::getBasicProjectConfigId));

		for (Node node : leafNodeList) {
			List<JiraIssueCustomHistory> issueCustomHistoryList = projectWiseJiraIssue
					.get(node.getProjectFilter().getBasicProjectConfigId().toString());

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(node.getProjectFilter().getBasicProjectConfigId());
			List<CycleTimeValidationData> cycleTimeList = new ArrayList<>();
			LinkedHashMap<String, List<DataCount>> leadTime = getLeadTime(issueCustomHistoryList, fieldMapping,
					cycleTimeList, node, rangeList);
			populateExcelDataObject(getRequestTrackerId(), cycleTimeList, excelData);
			mapTmp.get(node.getId()).setValue(leadTime);
		}
		List<String> xAxisRange = new ArrayList<>(rangeList);
		Collections.reverse(xAxisRange);
		kpiElement.setxAxisValues(xAxisRange);
		kpiElement.setExcelColumns(KPIExcelColumn.LEAD_TIME.getColumns());
		kpiElement.setExcelData(excelData);

	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();

		List<String> basicProjectConfigIds = new ArrayList<>();
		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			basicProjectConfigIds.add(basicProjectConfigId.toString());
			if (Optional.ofNullable(fieldMapping.getJiraIssueTypeKPI3()).isPresent()) {
				KpiDataHelper.prepareFieldMappingDefectTypeTransformation(mapOfProjectFilters,
						fieldMapping.getJiradefecttype(), fieldMapping.getJiraIssueTypeKPI3(),
						JiraFeatureHistory.STORY_TYPE.getFieldValueInFeature());
				uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

			}
			List<String> status = new ArrayList<>();
			if (Optional.ofNullable(fieldMapping.getJiraLiveStatusKPI3()).isPresent()) {
				status.addAll(fieldMapping.getJiraLiveStatusKPI3());
			}
			mapOfProjectFilters.put("statusUpdationLog.story.changedTo", CommonUtils.convertToPatternList(status));
			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

		});
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		resultListMap.put(STORY_HISTORY_DATA, jiraIssueCustomHistoryRepository
				.findByFilterAndFromStatusMapWithDateFilter(mapOfFilters, uniqueProjectMap, startDate, endDate));

		return resultListMap;
	}

	/**
	 * prepareLeadtime list
	 * 
	 * @param jiraIssueCustomHistoriesList
	 *            historylist
	 * @param fieldMapping
	 *            fieldMapping
	 * @param leadTimeList
	 *            leadTimeList
	 * @param node
	 *            projectNode
	 * @param rangeList
	 * 			rangeList
	 * @return dataCountMap
	 */
	private LinkedHashMap<String, List<DataCount>> getLeadTime(
			List<JiraIssueCustomHistory> jiraIssueCustomHistoriesList, FieldMapping fieldMapping,
			List<CycleTimeValidationData> leadTimeList, Node node, List<String> rangeList) {

		Map<Long, String> monthRangeMap = new HashMap<>();
		Map<String, Map<String, List<JiraIssueCustomHistory>>> rangeWiseJiraIssuesMap = new LinkedHashMap<>();
		BacklogKpiHelper.initializeRangeMapForProjects(rangeWiseJiraIssuesMap, rangeList, monthRangeMap);
		Set<String> issueTypes= new HashSet<>();

		if (CollectionUtils.isNotEmpty(jiraIssueCustomHistoriesList)) {
			for (JiraIssueCustomHistory jiraIssueCustomHistory : jiraIssueCustomHistoriesList) {
				CycleTimeValidationData cycleTimeValidationData = new CycleTimeValidationData();
				cycleTimeValidationData.setIssueNumber(jiraIssueCustomHistory.getStoryID());
				cycleTimeValidationData.setUrl(jiraIssueCustomHistory.getUrl());
				cycleTimeValidationData.setIssueDesc(jiraIssueCustomHistory.getDescription());
				cycleTimeValidationData.setIssueType(jiraIssueCustomHistory.getStoryType());
				CycleTime cycleTime = new CycleTime();
				cycleTime.setIntakeTime(jiraIssueCustomHistory.getCreatedDate());
				cycleTimeValidationData.setIntakeDate(jiraIssueCustomHistory.getCreatedDate());

				List<String> liveStatus = fieldMapping.getJiraLiveStatusKPI3().stream().filter(Objects::nonNull)
						.map(String::toLowerCase).collect(Collectors.toList());
				jiraIssueCustomHistory.getStatusUpdationLog()
						.forEach(statusUpdateLog -> BacklogKpiHelper.setLiveTime(cycleTimeValidationData, cycleTime,
								statusUpdateLog, DateTime.parse(statusUpdateLog.getUpdatedOn().toString()),
								liveStatus));

				if(BacklogKpiHelper.setRangeWiseJiraIssuesMap(rangeWiseJiraIssuesMap, jiraIssueCustomHistory,
						cycleTime.getLiveLocalDateTime(), monthRangeMap)){
					BacklogKpiHelper.setValueInCycleTime(jiraIssueCustomHistory.getCreatedDate(), cycleTime.getLiveTime(),
							LEAD_TIME, cycleTimeValidationData, issueTypes);
					leadTimeList.add(cycleTimeValidationData);
				}
			}
			return setDataCountMap(rangeWiseJiraIssuesMap, leadTimeList, node, issueTypes);
		}
		return new LinkedHashMap<>();
	}

	/**
	 * create trendValueMap of DataCounts
	 *
	 * @param rangeAndStatusWiseJiraIssueMap
	 *            map of jira issue by range
	 * @param leadTimeFullList
	 *            map of flow efficiency by issue
	 * @param leafNode
	 *            project node
	 * @param totalIssueTypeString
	 * @return data count map by filter
	 */
	private LinkedHashMap<String, List<DataCount>> setDataCountMap(
			Map<String, Map<String, List<JiraIssueCustomHistory>>> rangeAndStatusWiseJiraIssueMap,
			List<CycleTimeValidationData> leadTimeFullList, Node leafNode, Set<String> totalIssueTypeString) {
		Map<String, Long> map = leadTimeFullList.stream().collect(
				Collectors.toMap(CycleTimeValidationData::getIssueNumber, CycleTimeValidationData::getLeadTime,  (existing, replacement) -> existing));
		LinkedHashMap<String, List<DataCount>> dataCountMap = new LinkedHashMap<>();

		rangeAndStatusWiseJiraIssueMap.forEach((dateRange, statusWiseJiraIssues) -> {
			totalIssueTypeString.forEach(issueType -> {
				List<JiraIssueCustomHistory> typeWiseIssues = statusWiseJiraIssues.getOrDefault(issueType,
						new ArrayList<>());
				List<Long> leadTimeList = typeWiseIssues.stream().map(JiraIssueCustomHistory::getStoryID)
						.filter(map::containsKey).map(map::get).collect(Collectors.toList());

				setDataCount(leafNode.getProjectFilter().getName(), issueType, dateRange,
						AggregationUtils.averageLong(leadTimeList), dataCountMap, typeWiseIssues.size());
			});
			List<JiraIssueCustomHistory> totalRangeWiseIssues = statusWiseJiraIssues.values().stream()
					.flatMap(List::stream).collect(Collectors.toList());
			List<Long> leadTimeList = totalRangeWiseIssues.stream().map(JiraIssueCustomHistory::getStoryID)
					.filter(map::containsKey).map(map::get).collect(Collectors.toList());
			setDataCount(leafNode.getProjectFilter().getName(), "Overall", dateRange,
					AggregationUtils.averageLong(leadTimeList), dataCountMap, totalRangeWiseIssues.size());
		});

		return dataCountMap;
	}

	/**
	 * create DataCount by Filter
	 *
	 * @param projectName
	 *            project name
	 * @param filter
	 *            filter
	 * @param dateRange
	 *            date range
	 * @param value
	 *            average flow efficiency
	 * @param dataCountMap
	 *            data count by filter
	 * @param count
	 *            no of issues for range
	 */
	public void setDataCount(String projectName, String filter, String dateRange, Long value,
			Map<String, List<DataCount>> dataCountMap, int count) {
		double leadTime = Math.round(ObjectUtils.defaultIfNull(value, 0L).doubleValue() / 480);
		Map<String, Object> hoverMap = new HashMap<>();
		hoverMap.put(ISSUE_COUNT, count);
		DataCount dataCount = new DataCount();
		dataCount.setValue(leadTime);
		dataCount.setData(String.valueOf(leadTime));
		dataCount.setSSprintName(dateRange);
		dataCount.setSSprintID(dateRange);
		dataCount.setDate(dateRange);
		dataCount.setSProjectName(projectName);
		dataCount.setKpiGroup(filter);
		dataCount.setHoverValue(hoverMap);
		dataCountMap.computeIfAbsent(filter, k -> new ArrayList<>()).add(dataCount);
	}

	/**
	 * 
	 * @param requestTrackerId
	 *            requestTrackerId
	 * @param leadTimeList
	 *            leadTimeList
	 * @param excelData
	 *            excelData
	 */
	private void populateExcelDataObject(String requestTrackerId, List<CycleTimeValidationData> leadTimeList,
			List<KPIExcelData> excelData) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			KPIExcelUtility.populateLeadTime(leadTimeList, excelData);
		}

	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping){
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI3(),KPICode.LEAD_TIME.getKpiId());
	}

}