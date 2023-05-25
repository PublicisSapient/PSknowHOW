package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.common.model.jira.IssueBacklogCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.repository.jira.IssueBacklogCustomHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.common.model.application.DataCount;

@Slf4j
@Component
public class FlowLoadServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {
	private static final String ISSUE_BACKLOG_HISTORY = "Issue Backlog History";

	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private IssueBacklogCustomHistoryRepository issueBacklogCustomHistoryRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(FlowLoadServiceImpl.class);

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
			LOGGER.info("Flow Load kpi -> Requested project : {}", leafNode.getProjectFilter().getName());
			String basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId().toString();
			List<IssueBacklogCustomHistory> typeCountByDateRange = issueBacklogCustomHistoryRepository
					.findByBasicProjectConfigId(basicProjectConfigId);
			resultListMap.put(ISSUE_BACKLOG_HISTORY, typeCountByDateRange);
		}

		return resultListMap;
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		List<DataCount> trendValueList = new ArrayList<>();
		treeAggregatorDetail.getMapOfListOfProjectNodes().forEach((k, v) -> {
			Filters filters = Filters.getFilter(k);
			if (Filters.PROJECT == filters) {
				projectWiseLeafNodeValue(v, trendValueList, kpiElement, kpiRequest, mapTmp);
			}
		});
		return kpiElement;
	}

	private void projectWiseLeafNodeValue(List<Node> leafNode, List<DataCount> trendValueList, KpiElement kpiElement,
			KpiRequest kpiRequest, Map<String, Node> mapTmp) {

		int monthToSubtract = customApiConfig.getFlowKpiMonthCount();
		LocalDate endDate = LocalDate.now();
		LocalDate startDate = endDate.minusMonths(monthToSubtract);

		String requestTrackerId = getRequestTrackerId();
		List<KPIExcelData> excelData = new ArrayList<>();
		Map<String, Object> resultMap = fetchKPIDataFromDb(leafNode, "", "", kpiRequest);

		List<IssueBacklogCustomHistory> IssueBacklogCustomHistories = (List<IssueBacklogCustomHistory>) resultMap
				.get(ISSUE_BACKLOG_HISTORY);

		Map<String, List<Pair<LocalDate, LocalDate>>> statusesWithStartAndEndDate = new HashMap<>();
		// segregation all different status present in issue with time range
		IssueBacklogCustomHistories.forEach(issueBacklogCustomHistory -> {
			List<JiraHistoryChangeLog> statusChangeLog = issueBacklogCustomHistory.getStatusUpdationLog();
			for (int index = 0; index + 1 < statusChangeLog.size(); index++) {
				JiraHistoryChangeLog changeLog = statusChangeLog.get(index);
				JiraHistoryChangeLog nextChangeLog = statusChangeLog.get(index + 1);
				String status = changeLog.getChangedTo();
				LocalDate intervalStartDate = changeLog.getUpdatedOn().toLocalDate();
				LocalDate intervalEndDate = nextChangeLog.getUpdatedOn().toLocalDate();
				savingDateRangeInMap(endDate, startDate, statusesWithStartAndEndDate, status, intervalStartDate,
						intervalEndDate);
			}
			JiraHistoryChangeLog lastChangeLog = statusChangeLog.get(statusChangeLog.size() - 1);
			String status = lastChangeLog.getChangedTo();
			LocalDate intervalStartDate = lastChangeLog.getUpdatedOn().toLocalDate();
			LocalDate intervalEndDate = endDate;
			savingDateRangeInMap(endDate, startDate, statusesWithStartAndEndDate, status, intervalStartDate,
					intervalEndDate);
		});

		Map<String, Map<String, Integer>> dateWithStatusCount = new HashMap<>();
		LocalDate tempStartDate = startDate;
		while (tempStartDate.compareTo(endDate) <= 0) {
			dateWithStatusCount.put(tempStartDate.toString(), new HashMap<>());
			tempStartDate = tempStartDate.plusDays(1);
		}
		long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 2;
		Map<String, Map<String, Integer>> finalDateWithStatusCount = dateWithStatusCount;
		statusesWithStartAndEndDate.forEach((status, listOfStartAndEndDate) -> {
			Integer[] data = new Integer[(int)totalDays];
			Arrays.fill(data, Integer.valueOf(0));
			List<Integer> list = Arrays.asList(data);
			List<Integer> statusCountPresentInEachDay = list;
			listOfStartAndEndDate.forEach(intervalRange -> {
				int startIndex = (int) ChronoUnit.DAYS.between(startDate, intervalRange.getKey());
				int endIndex = (int) ChronoUnit.DAYS.between(startDate, intervalRange.getValue());
				statusCountPresentInEachDay.set(startIndex, statusCountPresentInEachDay.get(startIndex) + 1);
				statusCountPresentInEachDay.set(endIndex + 1, statusCountPresentInEachDay.get(endIndex + 1) - 1);
			});
			if(statusCountPresentInEachDay.get(0)!=0)
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
		dateWithStatusCount = dateWithStatusCount.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
				 (oldValue, newValue) -> oldValue, LinkedHashMap::new));

		if (!Objects.isNull(dateWithStatusCount)) {
			populateTrendValueList(trendValueList, dateWithStatusCount);
			populateExcelDataObject(requestTrackerId, excelData, dateWithStatusCount);
			LOGGER.info("FlowLoadServiceImpl -> request id : {} dateWithStatusCount : {}", requestTrackerId,
					dateWithStatusCount);
		}
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.FLOW_LOAD.getColumns());
		kpiElement.setTrendValueList(trendValueList);

	}

	private void savingDateRangeInMap(LocalDate endDate, LocalDate startDate,
			Map<String, List<Pair<LocalDate, LocalDate>>> statusesWithStartAndEndDate, String status,
			LocalDate intervalStartDate, LocalDate intervalEndDate) {
		intervalStartDate = intervalStartDate.compareTo(startDate) < 0 ? startDate : intervalStartDate;
		intervalEndDate = intervalEndDate.compareTo(endDate) > 0 ? endDate : intervalEndDate;
		Pair<LocalDate, LocalDate> intervalRange = Pair.of(intervalStartDate, intervalEndDate);
		status = status.replaceAll(" ","-");
		if (!statusesWithStartAndEndDate.containsKey(status))
			statusesWithStartAndEndDate.put(status, new ArrayList<>());
		statusesWithStartAndEndDate.get(status).add(intervalRange);
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
