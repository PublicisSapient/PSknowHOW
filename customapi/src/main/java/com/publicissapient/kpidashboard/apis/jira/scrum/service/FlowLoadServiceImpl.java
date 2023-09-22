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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.Filters;
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
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FlowLoadServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {
	private static final String ISSUE_HISTORY = "Issue History";

	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Override
	public String getQualifierType() {
		return KPICode.FLOW_LOAD.name();
	}

	@Override
	public Double calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);

		if (leafNode != null) {
			log.info("Flow Load kpi -> Requested project : {}", leafNode.getProjectFilter().getName());
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(leafNode.getProjectFilter().getBasicProjectConfigId());

			List<JiraIssueCustomHistory> issuesHistory = new ArrayList<>();

			if (CollectionUtils.isNotEmpty(fieldMapping.getJiraIssueTypeNamesKPI148())) {
				issuesHistory = getJiraIssuesCustomHistoryFromBaseClass();
				issuesHistory = issuesHistory.stream().filter(
						jiraIssue -> fieldMapping.getJiraIssueTypeNamesKPI148().contains(jiraIssue.getStoryType()))
						.collect(Collectors.toList());
			}

			resultListMap.put(ISSUE_HISTORY, issuesHistory);
		}

		return resultListMap;
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		List<DataCount> trendValueList = new ArrayList<>();
		treeAggregatorDetail.getMapOfListOfProjectNodes().forEach((k, v) -> {
			Filters filters = Filters.getFilter(k);
			if (Filters.PROJECT == filters) {
				projectWiseLeafNodeValue(v, trendValueList, kpiElement, kpiRequest);
			}
		});
		return kpiElement;
	}

	private void projectWiseLeafNodeValue(List<Node> leafNode, List<DataCount> trendValueList, KpiElement kpiElement,
			KpiRequest kpiRequest) {

		int monthToSubtract = customApiConfig.getFlowKpiMonthCount();

		LocalDate endDate = LocalDate.now();
		LocalDate startDate = endDate.minusMonths(monthToSubtract);

		String requestTrackerId = getRequestTrackerId();
		List<KPIExcelData> excelData = new ArrayList<>();

		Map<String, Object> resultMap = fetchKPIDataFromDb(leafNode, "", "", kpiRequest);

		List<JiraIssueCustomHistory> jiraIssueCustomHistories = (List<JiraIssueCustomHistory>) resultMap
				.get(ISSUE_HISTORY);

		leafNode.forEach(node -> {
			Map<String, List<Pair<LocalDate, LocalDate>>> statusesWithStartAndEndDate = new HashMap<>();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(node.getProjectFilter().getBasicProjectConfigId());
			// Iterating Over All issues history's statusUpdationLog and saving start and
			// end date for each status
			jiraIssueCustomHistories.forEach(jiraIssueCustomHistory -> createDateRangeForStatuses(endDate, startDate,
					statusesWithStartAndEndDate, jiraIssueCustomHistory, fieldMapping));

			Map<String, Map<String, Integer>> dateWithStatusCount = new HashMap<>();
			LocalDate tempStartDate = startDate;
			while (tempStartDate.compareTo(endDate) <= 0) {
				dateWithStatusCount.put(tempStartDate.toString(), new HashMap<>());
				tempStartDate = tempStartDate.plusDays(1);
			}
			long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 2;
			Map<String, Map<String, Integer>> finalDateWithStatusCount = dateWithStatusCount;
			// Marking startDate index with plus 1 and end date next index with -1
			// StartDate and EndDate index are calculated with respect to startDate
			// Now compute the prefix sum, Since the beginning is marked with one, all the
			// values after beginning will be incremented by one. Now as increment is only
			// targeted only till the end of the range, the decrement on index endDate+1
			// prevents that for every range present after endDate.
			calculateStatusCountForEachDay(startDate, statusesWithStartAndEndDate, totalDays, finalDateWithStatusCount);
			dateWithStatusCount = dateWithStatusCount.entrySet().stream().sorted(Map.Entry.comparingByKey())
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue,
							LinkedHashMap::new));

			if (MapUtils.isNotEmpty(dateWithStatusCount)) {
				populateTrendValueList(trendValueList, dateWithStatusCount);
				populateExcelDataObject(requestTrackerId, excelData, dateWithStatusCount);
				log.debug("FlowLoadServiceImpl -> request id : {} dateWithStatusCount : {}", requestTrackerId,
						dateWithStatusCount);
			}
			kpiElement.setExcelData(excelData);
			kpiElement.setExcelColumns(KPIExcelColumn.FLOW_LOAD.getColumns());
			kpiElement.setTrendValueList(trendValueList);
		});

	}

	private void calculateStatusCountForEachDay(LocalDate startDate,
			Map<String, List<Pair<LocalDate, LocalDate>>> statusesWithStartAndEndDate, long totalDays,
			Map<String, Map<String, Integer>> finalDateWithStatusCount) {
		statusesWithStartAndEndDate.forEach((status, listOfStartAndEndDate) -> {
			Integer[] data = new Integer[(int) totalDays];
			Arrays.fill(data, Integer.valueOf(0));
			List<Integer> list = Arrays.asList(data);
			List<Integer> statusCountPresentInEachDay = list;
			listOfStartAndEndDate.forEach(intervalRange -> {
				int startIndex = (int) ChronoUnit.DAYS.between(startDate, intervalRange.getKey());
				int endIndex = (int) ChronoUnit.DAYS.between(startDate, intervalRange.getValue());
				statusCountPresentInEachDay.set(startIndex, statusCountPresentInEachDay.get(startIndex) + 1);
				statusCountPresentInEachDay.set(endIndex + 1, statusCountPresentInEachDay.get(endIndex + 1) - 1);
			});
			if (statusCountPresentInEachDay.get(0) != 0)
				finalDateWithStatusCount.get(startDate.toString()).put(status, statusCountPresentInEachDay.get(0));
			int prevValue = statusCountPresentInEachDay.get(0);
			for (int i = 1; i < totalDays - 1; i++) {
				statusCountPresentInEachDay.set(i, statusCountPresentInEachDay.get(i) + prevValue);
				prevValue = statusCountPresentInEachDay.get(i);
				if (statusCountPresentInEachDay.get(i) != 0) {
					finalDateWithStatusCount.get(startDate.plusDays(i).toString()).put(status,
							statusCountPresentInEachDay.get(i));
				}
			}
		});
	}

	private void createDateRangeForStatuses(LocalDate endDate, LocalDate startDate,
			Map<String, List<Pair<LocalDate, LocalDate>>> statusesWithStartAndEndDate,
			JiraIssueCustomHistory jiraIssueCustomHistory, FieldMapping fieldMapping) {
		List<JiraHistoryChangeLog> statusChangeLog = jiraIssueCustomHistory.getStatusUpdationLog();
		int size = statusChangeLog.size();
		String status = "";

		// If Issue is processed before startDate
		if (size > 0 && statusChangeLog.get(size - 1).getUpdatedOn().toLocalDate().isBefore(startDate)) {
			status = statusChangeLog.get(statusChangeLog.size() - 1).getChangedTo();
			savingDateRangeInMap(startDate, endDate, statusesWithStartAndEndDate, status, startDate, endDate,
					fieldMapping);
		}

		// When issue is created after end date
		else if (LocalDate.parse(jiraIssueCustomHistory.getCreatedDate().toString().split("T")[0]).isAfter(endDate))
			return;
		else {
			for (int index = 0; index + 1 < statusChangeLog.size(); index++) {
				JiraHistoryChangeLog changeLog = statusChangeLog.get(index);
				JiraHistoryChangeLog nextChangeLog = statusChangeLog.get(index + 1);
				status = changeLog.getChangedTo();
				LocalDate intervalStartDate = changeLog.getUpdatedOn().toLocalDate();
				LocalDate intervalEndDate = nextChangeLog.getUpdatedOn().toLocalDate();
				savingDateRangeInMap(startDate, endDate, statusesWithStartAndEndDate, status, intervalStartDate,
						intervalEndDate, fieldMapping);
			}
			JiraHistoryChangeLog lastChangeLog = statusChangeLog.get(statusChangeLog.size() - 1);
			status = lastChangeLog.getChangedTo();
			LocalDate intervalStartDate = lastChangeLog.getUpdatedOn().toLocalDate();
			if (intervalStartDate.isAfter(endDate))
				return;
			LocalDate intervalEndDate = endDate;
			savingDateRangeInMap(startDate, endDate, statusesWithStartAndEndDate, status, intervalStartDate,
					intervalEndDate, fieldMapping);
		}
	}

	private void savingDateRangeInMap(LocalDate startDate, LocalDate endDate,
			Map<String, List<Pair<LocalDate, LocalDate>>> statusesWithStartAndEndDate, String status,
			LocalDate intervalStartDate, LocalDate intervalEndDate, FieldMapping fieldMapping) {
		if (isStatusValid(fieldMapping, status)) {
			if (intervalEndDate.isBefore(startDate) || intervalStartDate.isAfter(endDate))
				return;
			if (intervalStartDate.isBefore(startDate))
				intervalStartDate = startDate;
			if (intervalEndDate.isAfter(endDate))
				intervalEndDate = endDate;
			Pair<LocalDate, LocalDate> intervalRange = Pair.of(intervalStartDate, intervalEndDate);
			status = status.replace(" ", "-");
			if (!statusesWithStartAndEndDate.containsKey(status)) {
				statusesWithStartAndEndDate.put(status, new ArrayList<>());
			}
			statusesWithStartAndEndDate.get(status).add(intervalRange);
		}
	}

	private boolean isStatusValid(FieldMapping fieldMapping, String status) {
		Map<Long, String> doneStatusMap = getJiraIssueReleaseStatus().getClosedList();
		List<String> doneStatus = new ArrayList<>();
		if (doneStatusMap != null)
			doneStatus = doneStatusMap.values().stream().map(String::toLowerCase).collect(Collectors.toList());
		return !doneStatus.contains(status.toLowerCase())
				&& (fieldMapping.getStoryFirstStatusKPI148().equalsIgnoreCase(status)
						|| (CollectionUtils.isNotEmpty(fieldMapping.getJiraStatusForInProgressKPI148())
								&& fieldMapping.getJiraStatusForInProgressKPI148().contains(status))
						|| (CollectionUtils.isNotEmpty(fieldMapping.getJiraStatusForQaKPI148())
								&& fieldMapping.getJiraStatusForQaKPI148().contains(status)));

	}

	private void populateTrendValueList(List<DataCount> dataList,
			Map<String, Map<String, Integer>> dateWithStatusCount) {
		for (Map.Entry<String, Map<String, Integer>> entry : dateWithStatusCount.entrySet()) {
			String date = entry.getKey();
			Map<String, Integer> typeCountMap = entry.getValue();
			DataCount dc = new DataCount();
			dc.setDate(date);
			dc.setValue(typeCountMap);
			dataList.add(dc);
		}
	}

	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			Map<String, Map<String, Integer>> dateWithStatusCount) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& !Objects.isNull(dateWithStatusCount)) {
			KPIExcelUtility.populateFlowKPI(dateWithStatusCount, excelData);
		}
	}
}
