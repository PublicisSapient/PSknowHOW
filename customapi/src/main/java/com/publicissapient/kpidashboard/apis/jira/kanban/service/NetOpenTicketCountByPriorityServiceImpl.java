package com.publicissapient.kpidashboard.apis.jira.kanban.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
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
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.util.DateUtil;

@Component
public class NetOpenTicketCountByPriorityServiceImpl
		extends JiraKPIService<Long, List<Object>, Map<String, Map<String, Map<String, Set<String>>>>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(NetOpenTicketCountByPriorityServiceImpl.class);
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final String FIELD_PRIORITY = "priority";
	private static final String JIRA_ISSUE_HISTORY_DATA = "JiraIssueHistoryData";
	Map<String, Object> resultListMap = new HashMap<>();
	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private KpiHelperService kpiHelperService;
	@Autowired
	private CustomApiConfig customApiConfig;

	/**
	 * Gets Qualifier Type
	 *
	 * @return KPICode's <tt>NET_OPEN_COUNT_BY_PRIORITY</tt> enum
	 */
	@Override
	public String getQualifierType() {
		return KPICode.TICKET_COUNT_BY_PRIORITY.name();
	}

	/**
	 * Gets KPI Data
	 *
	 * @param kpiRequest
	 * @param kpiElement
	 * @param treeAggregatorDetail
	 * @return KpiElement
	 * @throws ApplicationException
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		LOGGER.info("NET-OPEN-TICKET-COUNT-BY-PRIORITY {}", kpiRequest.getRequestTrackerId());
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes()
				.get(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT);

		dateWiseLeafNodeValue(mapTmp, projectList, kpiElement, kpiRequest);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();

		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.TICKET_COUNT_BY_PRIORITY);
		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.TICKET_COUNT_BY_PRIORITY);

		trendValuesMap = sortTrendValueMap(trendValuesMap, priorityTypes(true));
		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		trendValuesMap.forEach((key, dateWiseDataCount) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			dataCountGroup.setFilter(key);
			dataCountGroup.setValue(dateWiseDataCount);
			dataCountGroups.add(dataCountGroup);
		});

		kpiElement.setTrendValueList(dataCountGroups);
		// map aggregation implementation over

		kpiElement.setNodeWiseKPIValue(nodeWiseKPIValue);

		LOGGER.debug(
				"[NET-OPEN-TICKET-COUNT-BY-PRIORITY-KANBAN-AGGREGATED-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), root);
		return kpiElement;
	}

	/**
	 * Fetches KPI Data From Database
	 *
	 * @param leafNodeList
	 * @param startDate
	 * @param endDate
	 * @return resultListMap
	 */
	@Override
	public Map<String, Map<String, Map<String, Set<String>>>> fetchKPIDataFromDb(List<Node> leafNodeList,
			String startDate, String endDate, KpiRequest kpiRequest) {
		Map<ObjectId,Map<String,Object>> projectWiseMapping=new HashMap<>();
		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			Map<String, Object> fieldWise = new HashMap<>();
			fieldWise.put("LiveStatus", fieldMapping.getJiraLiveStatusNOPK());
			fieldWise.put("ClosedStatus", fieldMapping.getJiraTicketClosedStatus());
			fieldWise.put("RejectedStatus", fieldMapping.getJiraTicketRejectedStatus());
			fieldWise.put("RCA_Count_IssueType", fieldMapping.getKanbanRCACountIssueType());
			fieldWise.put("Ticket_Count_IssueType", fieldMapping.getTicketCountIssueType());
			fieldWise.put("StoryFirstStatus", fieldMapping.getStoryFirstStatus());
		 	projectWiseMapping.put(basicProjectConfigId, fieldWise);
		});

		resultListMap = kpiHelperService.fetchJiraCustomHistoryDataFromDbForKanban(leafNodeList, startDate, endDate,
				kpiRequest, FIELD_PRIORITY,projectWiseMapping);

		CustomDateRange dateRangeForCumulative = KpiDataHelper.getStartAndEndDatesForCumulative(kpiRequest);
		String startDateForCumulative = dateRangeForCumulative.getStartDate().format(DATE_FORMATTER);

		Map<String, List<KanbanIssueCustomHistory>> projectWiseNonClosedTickets = kpiHelperService
				.removeClosedTicketsFromHistoryIssuesData(resultListMap, startDateForCumulative);

		return kpiHelperService.computeProjectWiseJiraHistoryByFieldAndDate(projectWiseNonClosedTickets,
				startDateForCumulative, resultListMap, FIELD_PRIORITY);
	}

	private void dateWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> leafNodeList, KpiElement kpiElement,
			KpiRequest kpiRequest) {

		// this method fetch dates for past history data
		CustomDateRange dateRange = KpiDataHelper.getMonthsForPastDataHistory(15);

		// get start and end date in yyyy-mm-dd format
		String startDate = dateRange.getStartDate().format(DATE_FORMATTER);
		String endDate = dateRange.getEndDate().format(DATE_FORMATTER);

		// past all tickets and given range ticket data fetch from db
		Map<String, Map<String, Map<String, Set<String>>>> resultMap = fetchKPIDataFromDb(leafNodeList, startDate,
				endDate, kpiRequest);

		kpiWithFilter(resultMap, mapTmp, leafNodeList, kpiElement, kpiRequest);
	}

	private void kpiWithFilter(Map<String, Map<String, Map<String, Set<String>>>> resultMap, Map<String, Node> mapTmp,
			List<Node> leafNodeList, KpiElement kpiElement, KpiRequest kpiRequest) {
		List<KPIExcelData> excelData = new ArrayList<>();
		String requestTrackerId = getKanbanRequestTrackerId();
		leafNodeList.forEach(node -> {
			Map<String, List<DataCount>> trendValueMap = new HashMap<>();
			String projectNodeId = node.getProjectFilter().getBasicProjectConfigId().toString();
			Map<String, Map<String, Set<String>>> jiraHistoryPriorityAndDateWiseIssueMap = resultMap
					.getOrDefault(projectNodeId, new HashMap<>());
			if (MapUtils.isNotEmpty(jiraHistoryPriorityAndDateWiseIssueMap)) {
				Set<String> projectWisePriorityList = new HashSet<>();
				projectWisePriorityList.addAll(jiraHistoryPriorityAndDateWiseIssueMap.keySet());
				LocalDate currentDate = LocalDate.now();
				for (int i = 0; i < kpiRequest.getKanbanXaxisDataPoints(); i++) {

					CustomDateRange dateRange = KpiDataHelper.getStartAndEndDateForDataFiltering(currentDate,
							kpiRequest.getDuration());

					Map<String, Long> projectWisePriorityCountMap = filterKanbanDataBasedOnDateAndPriorityWise(
							jiraHistoryPriorityAndDateWiseIssueMap, projectWisePriorityList, dateRange.getEndDate());

					String date = getRange(dateRange, kpiRequest);

					populateProjectFilterWiseDataMap(projectWisePriorityCountMap, trendValueMap,
							node.getProjectFilter().getId(), date);

					currentDate = getNextRangeDate(kpiRequest, currentDate);

				}
				// Populates data in Excel for validation for tickets created before
				populateExcelDataObject(requestTrackerId, jiraHistoryPriorityAndDateWiseIssueMap, node,
						projectWisePriorityList,
						new HashSet<>((List<KanbanIssueCustomHistory>) resultListMap.get(JIRA_ISSUE_HISTORY_DATA)),
						excelData, kpiRequest);
				mapTmp.get(node.getId()).setValue(trendValueMap);
			}
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.TICKET_COUNT_BY_PRIORITY.getColumns());
	}

	/**
	 * Calculates KPI Metrics
	 *
	 * @param subCategoryMap
	 * @return Long
	 */
	@Override
	public Long calculateKPIMetrics(Map<String, Map<String, Map<String, Set<String>>>> subCategoryMap) {
		return subCategoryMap == null ? 0L : subCategoryMap.size();
	}

	@Override
	public Long calculateKpiValue(List<Long> valueList, String kpiId) {
		return calculateKpiValueForLong(valueList, kpiId);
	}

	/**
	 * Total tickets data as per given date range and type If range is DAYS then
	 * filter data as consider data is currentDate data. If range Weeks then filter
	 * data as consider sunday data for given week data and If range Month then
	 * Filter data as consider last month data for given month data. If range date
	 * is after than today date then consider as today date for data
	 *
	 * @param jiraHistoryPriorityAndDateWiseIssueMap
	 * @param priorityList
	 * @param currentDate
	 */
	public Map<String, Long> filterKanbanDataBasedOnDateAndPriorityWise(
			Map<String, Map<String, Set<String>>> jiraHistoryPriorityAndDateWiseIssueMap, Set<String> priorityList,
			LocalDate currentDate) {
		String date;
		if (currentDate.isAfter(LocalDate.now())) {
			date = LocalDate.now().toString();
		} else {
			date = currentDate.toString();
		}
		Map<String, Long> projectPriorityMap = new HashMap<>();
		priorityList.forEach(priority -> {
			Set<String> ids = jiraHistoryPriorityAndDateWiseIssueMap.get(priority).getOrDefault(date, new HashSet<>())
					.stream().filter(Objects::nonNull).collect(Collectors.toSet());
			projectPriorityMap.put(priority, Long.valueOf(ids.size()));
		});
		priorityList.forEach(priority -> projectPriorityMap.computeIfAbsent(priority, val -> 0L));
		return projectPriorityMap;
	}

	/**
	 * priority wise prepare data count list and treadValueMap
	 *
	 * @param projectWisePriorityMap
	 * @param projectFilterWiseDataMap
	 * @param projectNodeId
	 * @param date
	 */
	private void populateProjectFilterWiseDataMap(Map<String, Long> projectWisePriorityMap,
			Map<String, List<DataCount>> projectFilterWiseDataMap, String projectNodeId, String date) {
		String projectName = projectNodeId.substring(0, projectNodeId.lastIndexOf(CommonConstant.UNDERSCORE));

		Map<String, Object> hoverValueMap = new HashMap<>();
		projectWisePriorityMap.forEach((key, value) -> {
			hoverValueMap.put(key, value.intValue());
			DataCount dcObj = getDataCountObject(value, projectName, date, projectNodeId, key, hoverValueMap);
			projectFilterWiseDataMap.computeIfAbsent(key, k -> new ArrayList<>()).add(dcObj);
		});

		Long aggLineValue = projectWisePriorityMap.values().stream().mapToLong(p -> p).sum();

		projectFilterWiseDataMap.computeIfAbsent(CommonConstant.OVERALL, k -> new ArrayList<>()).add(getDataCountObject(
				aggLineValue, projectName, date, projectNodeId, CommonConstant.OVERALL, hoverValueMap));
	}

	/**
	 * as per date type given next range date
	 *
	 * @param kpiRequest
	 * @param currentDate
	 */
	@NotNull
	private LocalDate getNextRangeDate(KpiRequest kpiRequest, LocalDate currentDate) {
		if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.WEEK)) {
			currentDate = currentDate.minusWeeks(1);
		} else if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.MONTH)) {
			currentDate = currentDate.minusMonths(1);
		} else {
			currentDate = currentDate.minusDays(1);
		}
		return currentDate;
	}

	/**
	 * particulate date format given as per date type
	 *
	 * @param dateRange
	 * @param kpiRequest
	 */
	private String getRange(CustomDateRange dateRange, KpiRequest kpiRequest) {
		String range = null;
		if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.WEEK)) {
			range = DateUtil.dateTimeConverter(dateRange.getStartDate().toString(), DateUtil.DATE_FORMAT,
					DateUtil.DISPLAY_DATE_FORMAT) + " to "
					+ DateUtil.dateTimeConverter(dateRange.getEndDate().toString(), DateUtil.DATE_FORMAT,
							DateUtil.DISPLAY_DATE_FORMAT);
		} else if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.MONTH)) {
			range = dateRange.getStartDate().getMonth().toString() + " " + dateRange.getStartDate().getYear();
		} else {
			range = dateRange.getStartDate().toString();
		}
		return range;
	}

	/**
	 * particulate date format given as per date type
	 *
	 * @param value
	 * @param projectName
	 * @param date
	 * @param projectNodeId
	 * @param priority
	 * @param
	 */
	private DataCount getDataCountObject(Long value, String projectName, String date, String projectNodeId,
			String priority, Map<String, Object> overAllHoverValueMap) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(value));
		dataCount.setSProjectName(projectName);
		dataCount.setDate(date);
		dataCount.setKpiGroup(priority);
		Map<String, Object> hoverValueMap = new HashMap<>();
		if (priority.equalsIgnoreCase(CommonConstant.OVERALL)) {
			dataCount.setHoverValue(overAllHoverValueMap);
		} else {
			hoverValueMap.put(priority, value.intValue());
			dataCount.setHoverValue(hoverValueMap);
		}
		dataCount.setSprintIds(new ArrayList<>(Arrays.asList(projectNodeId)));
		dataCount.setSprintNames(new ArrayList<>(Arrays.asList(projectName)));
		dataCount.setValue(value);
		return dataCount;
	}

	private void populateExcelDataObject(String requestTrackerId,
			Map<String, Map<String, Set<String>>> jiraHistoryPriorityAndDateWiseIssueMap, Node node,
			Set<String> projectWisePriorityList, Set<KanbanIssueCustomHistory> kanbanJiraIssues,
			List<KPIExcelData> excelData, KpiRequest kpiRequest) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& MapUtils.isNotEmpty(jiraHistoryPriorityAndDateWiseIssueMap)) {
			String dateProjectKey = node.getAccountHierarchyKanban().getNodeName();
			String date = getRange(
					KpiDataHelper.getStartAndEndDateForDataFiltering(LocalDate.now(), kpiRequest.getDuration()),
					kpiRequest);
			KPIExcelUtility.prepareExcelForKanbanCumulativeDataMap(dateProjectKey,
					jiraHistoryPriorityAndDateWiseIssueMap, projectWisePriorityList, kanbanJiraIssues, excelData, date,
					KPICode.TICKET_COUNT_BY_PRIORITY.getKpiId());
		}
	}

	private Map<String, List<DataCount>> sortTrendValueMap(Map<String, List<DataCount>> trendMap,
			List<String> keyOrder) {
		Map<String, List<DataCount>> sortedMap = new LinkedHashMap<>();
		keyOrder.forEach(order -> {
			if (null != trendMap.get(order)) {
				sortedMap.put(order, trendMap.get(order));
			}
		});
		return sortedMap;
	}

	private List<String> priorityTypes(boolean addOverall) {
		if (addOverall) {
			return Arrays.asList(CommonConstant.OVERALL, Constant.P1, Constant.P2, Constant.P3, Constant.P4,
					Constant.MISC);
		} else {
			return Arrays.asList(Constant.P1, Constant.P2, Constant.P3, Constant.P4, Constant.MISC);
		}
	}

}
