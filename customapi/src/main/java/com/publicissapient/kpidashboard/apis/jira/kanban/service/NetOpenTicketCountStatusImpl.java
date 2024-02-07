package com.publicissapient.kpidashboard.apis.jira.kanban.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
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
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

@Component
public class NetOpenTicketCountStatusImpl
		extends JiraKPIService<Long, List<Object>, Map<String, Map<String, Map<String, Set<String>>>>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(NetOpenTicketCountStatusImpl.class);
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private static final String FIELD_STATUS = "status";
	private static final String PROJECT_WISE_CLOSED_STORY_STATUS = "projectWiseClosedStoryStatus";
	private static final String JIRA_ISSUE_HISTORY_DATA = "JiraIssueHistoryData";

	@Autowired
	private KanbanJiraIssueRepository kanbanJiraIssueRepository;
	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private KpiHelperService kpiHelperService;
	@Autowired
	private CustomApiConfig customApiConfig;
	private Map<String, Object> historyDataResultMap;

	/**
	 * Gets Qualifier Type
	 *
	 * @return KPICode's <tt>TICKET_COUNT_BY_PRIORITY</tt> enum
	 */
	@Override
	public String getQualifierType() {
		return KPICode.NET_OPEN_TICKET_COUNT_BY_STATUS.name();
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

		LOGGER.info("NET-OPEN-TICKET-COUNT-BY-STATUS {}", kpiRequest.getRequestTrackerId());
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes()
				.get(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT);

		dateWiseLeafNodeValue(mapTmp, projectList, kpiElement, kpiRequest);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		// for chart with filter,group stack chart
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.NET_OPEN_TICKET_COUNT_BY_STATUS);
		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.NET_OPEN_TICKET_COUNT_BY_STATUS);

		Map<String, Map<String, List<DataCount>>> statusTypeProjectWiseDc = new LinkedHashMap<>();
		trendValuesMap.forEach((statusType, dataCounts) -> {
			Map<String, List<DataCount>> projectWiseDc = dataCounts.stream()
					.collect(Collectors.groupingBy(DataCount::getData));
			statusTypeProjectWiseDc.put(statusType, projectWiseDc);
		});

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		statusTypeProjectWiseDc.forEach((issueType, projectWiseDc) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			List<DataCount> dataList = new ArrayList<>();
			projectWiseDc.entrySet().stream().forEach(trend -> dataList.addAll(trend.getValue()));
			dataCountGroup.setFilter(issueType);
			dataCountGroup.setValue(dataList);
			dataCountGroups.add(dataCountGroup);
		});

		kpiElement.setTrendValueList(dataCountGroups);
		kpiElement.setNodeWiseKPIValue(nodeWiseKPIValue);

		LOGGER.debug(
				"[TOTAL-TICKET-COUNT-BY-STATUS-KANBAN-AGGREGATED-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), root);
		return kpiElement;
	}

	/**
	 * Calculates KPI Metrics
	 *
	 * @param subCategoryMap
	 * @return Long
	 */
	@Override
	public Long calculateKPIMetrics(Map<String, Map<String, Map<String, Set<String>>>> subCategoryMap) {
		return 0L;
	}

	@Override
	public Long calculateKpiValue(List<Long> valueList, String kpiName) {
		return calculateKpiValueForLong(valueList, kpiName);
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
			fieldWise.put("LiveStatus", fieldMapping.getJiraLiveStatusNOSK());
			fieldWise.put("ClosedStatus", fieldMapping.getJiraTicketClosedStatus());
			fieldWise.put("RejectedStatus", fieldMapping.getJiraTicketRejectedStatus());
			fieldWise.put("RCA_Count_IssueType", fieldMapping.getKanbanRCACountIssueType());
			fieldWise.put("Ticket_Count_IssueType", fieldMapping.getTicketCountIssueType());
			fieldWise.put("StoryFirstStatus", fieldMapping.getStoryFirstStatus());
			projectWiseMapping.put(basicProjectConfigId, fieldWise);
		});
		historyDataResultMap = kpiHelperService.fetchJiraCustomHistoryDataFromDbForKanban(leafNodeList, startDate,
				endDate, kpiRequest, FIELD_STATUS, projectWiseMapping);
		CustomDateRange dateRangeForCumulative = KpiDataHelper.getStartAndEndDatesForCumulative(kpiRequest);
		// get start and end date in yyyy-mm-dd format
		String cumulativeStartDate = dateRangeForCumulative.getStartDate().format(DATE_FORMATTER);

		Map<String, List<KanbanIssueCustomHistory>> projectWiseNonClosedTickets = kpiHelperService
				.removeClosedTicketsFromHistoryIssuesData(historyDataResultMap, cumulativeStartDate);

		return kpiHelperService.computeProjectWiseJiraHistoryByStatusAndDate(projectWiseNonClosedTickets,
				cumulativeStartDate, historyDataResultMap);

	}

	/**
	 * Populates KPI value to sprint leaf nodes and gives the trend analysis at
	 * sprint wise.
	 *
	 * @param mapTmp
	 * @param leafNodeList
	 * @param kpiElement
	 * @param kpiRequest
	 *
	 */
	@SuppressWarnings("unchecked")
	private void dateWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> leafNodeList, KpiElement kpiElement,
			KpiRequest kpiRequest) {

		// this method fetch start and end date to fetch data.
		CustomDateRange dateRange = KpiDataHelper.getMonthsForPastDataHistory(15);

		// get start and end date in yyyy-mm-dd format
		String startDate = dateRange.getStartDate().format(DATE_FORMATTER);
		String endDate = dateRange.getEndDate().format(DATE_FORMATTER);

		// past all tickets and given range ticket data fetch from db
		Map<String, Map<String, Map<String, Set<String>>>> projectWiseDbData = fetchKPIDataFromDb(leafNodeList,
				startDate, endDate, kpiRequest);

		kpiWithFilter(projectWiseDbData, mapTmp, leafNodeList, kpiElement, kpiRequest);
	}

	private void kpiWithFilter(Map<String, Map<String, Map<String, Set<String>>>> projectWiseStatusDateData,
			Map<String, Node> mapTmp, List<Node> leafNodeList, KpiElement kpiElement, KpiRequest kpiRequest) {

		List<KPIExcelData> excelData = new ArrayList<>();
		String requestTrackerId = getKanbanRequestTrackerId();
		Map<String, List<String>> projectWiseDoneStatus = (Map<String, List<String>>) historyDataResultMap
				.get(PROJECT_WISE_CLOSED_STORY_STATUS);
		leafNodeList.forEach(node -> {
			Map<String, List<DataCount>> dataCountMap = new HashMap<>();
			String projectNodeId = node.getProjectFilter().getBasicProjectConfigId().toString();
			Map<String, Map<String, Set<String>>> statusWiseDateData = projectWiseStatusDateData
					.getOrDefault(projectNodeId, new HashMap<>());

			Set<String> doneStatus = new HashSet<>(
					projectWiseDoneStatus.getOrDefault(projectNodeId, new ArrayList<>()));

			if (MapUtils.isNotEmpty(statusWiseDateData)) {
				Set<String> projectWiseStatusList = getStatusOtherThanDone(statusWiseDateData, doneStatus);

				LocalDate currentDate = LocalDate.now();
				for (int i = 0; i < kpiRequest.getKanbanXaxisDataPoints(); i++) {
					CustomDateRange dateRange = KpiDataHelper.getStartAndEndDateForDataFiltering(currentDate,
							kpiRequest.getDuration());
					Map<String, Long> projectWiseStatusCountMap;
					if (dateRange.getEndDate().isAfter(LocalDate.now())) {
						projectWiseStatusCountMap = filterKanbanDataBasedOnDateAndStatusWise(statusWiseDateData,
								projectWiseStatusList, LocalDate.now());
					} else {
						projectWiseStatusCountMap = filterKanbanDataBasedOnDateAndStatusWise(statusWiseDateData,
								projectWiseStatusList, dateRange.getEndDate());
					}

					String date = getRange(dateRange, kpiRequest);

					populateProjectFilterWiseDataMap(projectWiseStatusCountMap, projectWiseStatusList, dataCountMap,
							node.getProjectFilter().getId(), date);

					currentDate = getNextRangeDate(kpiRequest, currentDate);

				}
				// Populates data in Excel for validation for tickets created before
				populateExcelDataObject(requestTrackerId, statusWiseDateData, node, projectWiseStatusList,
						new HashSet<>(
								(List<KanbanIssueCustomHistory>) historyDataResultMap.get(JIRA_ISSUE_HISTORY_DATA)),
						excelData, kpiRequest);
				mapTmp.get(node.getId()).setValue(dataCountMap);
			}
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.NET_OPEN_TICKET_COUNT_BY_STATUS.getColumns());
	}

	private Set<String> getStatusOtherThanDone(Map<String, Map<String, Set<String>>> statusWiseDateData,
			Set<String> doneStaus) {
		Set<String> statuses;
		statuses = statusWiseDateData.keySet().stream().filter(status -> !doneStaus.contains(status))
				.collect(Collectors.toSet());
		return statuses;
	}

	/**
	 *
	 * @param statusWiseDateData
	 * @param statusList
	 * @param currentDate
	 * @return
	 */
	public Map<String, Long> filterKanbanDataBasedOnDateAndStatusWise(
			Map<String, Map<String, Set<String>>> statusWiseDateData, Set<String> statusList, LocalDate currentDate) {

		Map<String, Long> projectStatusMap = new HashMap<>();
		statusList.forEach(status -> {
			Set<String> ids = statusWiseDateData.get(status).getOrDefault(currentDate.toString(), new HashSet<>())
					.stream().filter(Objects::nonNull).collect(Collectors.toSet());
			projectStatusMap.put(status, Long.valueOf(ids.size()));
		});

		statusList.forEach(status -> projectStatusMap.computeIfAbsent(status, val -> 0L));

		return projectStatusMap;
	}

	/**
	 * priority wise prepare data count list and treadValueMap
	 *
	 * @param projectWiseStatusList
	 * @param projectFilterWiseDataMap
	 * @param projectNodeId
	 * @param date
	 */
	private void populateProjectFilterWiseDataMap(Map<String, Long> projectWiseStatusCountMap,
			Set<String> projectWiseStatusList, Map<String, List<DataCount>> projectFilterWiseDataMap,
			String projectNodeId, String date) {
		String projectName = projectNodeId.substring(0, projectNodeId.lastIndexOf(CommonConstant.UNDERSCORE));
		Map<String, Long> finalMap = new HashMap<>();
		Map<String, Object> hoverValueMap = new HashMap<>();
		if (CollectionUtils.isNotEmpty(projectWiseStatusList)) {
			projectWiseStatusList.forEach(status -> {
				Long statusWiseCount = projectWiseStatusCountMap.getOrDefault(status, 0L);
				finalMap.put(StringUtils.capitalize(status), statusWiseCount);
				hoverValueMap.put(StringUtils.capitalize(status), statusWiseCount.intValue());
			});
			Long overAllCount = finalMap.values().stream().mapToLong(val -> val).sum();
			finalMap.put(CommonConstant.OVERALL, overAllCount);
		}

		finalMap.forEach((status, value) -> {
			DataCount dcObj = getDataCountObject(value, projectName, date, status, hoverValueMap);
			projectFilterWiseDataMap.computeIfAbsent(status, k -> new ArrayList<>()).add(dcObj);
		});

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
	 * @param status
	 * @param
	 */
	private DataCount getDataCountObject(Long value, String projectName, String date, String status,
			Map<String, Object> overAllHoverValueMap) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(value));
		dataCount.setSProjectName(projectName);
		dataCount.setDate(date);
		dataCount.setKpiGroup(status);
		Map<String, Object> hoverValueMap = new HashMap<>();
		if (status.equalsIgnoreCase(CommonConstant.OVERALL)) {
			dataCount.setHoverValue(overAllHoverValueMap);
		} else {
			hoverValueMap.put(status, value.intValue());
			dataCount.setHoverValue(hoverValueMap);
		}
		dataCount.setSSprintID(date);
		dataCount.setSSprintName(date);
		dataCount.setValue(value);
		return dataCount;
	}

	private void populateExcelDataObject(String requestTrackerId,
			Map<String, Map<String, Set<String>>> projectWiseFeatureList, Node node, Set<String> projectWiseStatusList,
			Set<KanbanIssueCustomHistory> kanbanJiraIssues, List<KPIExcelData> excelData, KpiRequest kpiRequest) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			String projectName = node.getAccountHierarchyKanban().getNodeName();
			String date = getRange(
					KpiDataHelper.getStartAndEndDateForDataFiltering(LocalDate.now(), kpiRequest.getDuration()),
					kpiRequest);
			KPIExcelUtility.prepareExcelForKanbanCumulativeDataMap(projectName, projectWiseFeatureList,
					projectWiseStatusList, kanbanJiraIssues, excelData, date,
					KPICode.NET_OPEN_TICKET_COUNT_BY_STATUS.getKpiId());
		}

	}

}
