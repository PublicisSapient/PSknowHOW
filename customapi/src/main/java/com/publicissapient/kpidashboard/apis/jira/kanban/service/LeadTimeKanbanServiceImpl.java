package com.publicissapient.kpidashboard.apis.jira.kanban.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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
import com.publicissapient.kpidashboard.apis.enums.JiraFeatureHistory;
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
import com.publicissapient.kpidashboard.apis.util.AggregationUtils;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.CycleTime;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.LeadTimeData;
import com.publicissapient.kpidashboard.common.model.application.LeadTimeValidationDataForKanban;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueHistory;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueHistoryRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LeadTimeKanbanServiceImpl extends JiraKPIService<Long, List<Object>, Map<String, Object>> {
	private static final String STORY_HISTORY_DATA = "storyHistoryData";
	private static final String LEAD_TIME = "Lead Time";
	private static final String OPEN_TO_TRIAGE = "Open - Triage";
	private static final String TRIAGE_TO_COMPLETE = "Triage - Complete";
	private static final String COMPLETE_TO_LIVE = "Complete - Live";

	@Autowired
	private KanbanJiraIssueHistoryRepository kanbanJiraIssueHistoryRepository;

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
		return KPICode.LEAD_TIME_KANBAN.name();
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		List<String> projectList = new ArrayList<>();
		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			projectList.add(basicProjectConfigId.toString());
			if (Optional.ofNullable(fieldMapping.getKanbanCycleTimeIssueType()).isPresent()) {
				mapOfProjectFilters.put(JiraFeatureHistory.STORY_TYPE.getFieldValueInFeature(),
						CommonUtils.convertToPatternList(fieldMapping.getKanbanCycleTimeIssueType()));
			}

			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);
		});
		mapOfFilters.put(JiraFeatureHistory.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				projectList.stream().distinct().collect(Collectors.toList()));

		resultListMap.put(STORY_HISTORY_DATA, kanbanJiraIssueHistoryRepository
				.findIssuesByCreatedDateAndType(mapOfFilters, uniqueProjectMap, startDate, endDate));

		return resultListMap;
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		log.info("LEAD-TIME-KANBAN", kpiRequest.getRequestTrackerId());
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes()
				.get(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT);

		dateWiseLeafNodeValue(mapTmp, projectList, kpiElement, kpiRequest);
		log.debug("[LEAD-TIME-KANBAN-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.LEAD_TIME_KANBAN);

		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.LEAD_TIME_KANBAN);

		List<DataCountGroup> dataCountGroups = getDataCountGroups(trendValuesMap);

		kpiElement.setTrendValueList(dataCountGroups);
		List<String> maturityRanges = new ArrayList<>(
				configHelperService.calculateMaturity().get(LEAD_TIME.replace(" ", "")));
		maturityRanges.addAll(configHelperService.calculateMaturity().get(OPEN_TO_TRIAGE.replace(" ", "")));
		maturityRanges.addAll(configHelperService.calculateMaturity().get(TRIAGE_TO_COMPLETE.replace(" ", "")));
		maturityRanges.addAll(configHelperService.calculateMaturity().get(COMPLETE_TO_LIVE.replace(" ", "")));
		kpiElement.setMaturityRange(maturityRanges);

		log.debug("[LEAD-TIME-KANBAN-AGGREGATED-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), root);
		return kpiElement;
	}

	/**
	 * 
	 * @param mapTmp
	 * @param leafNodeList
	 * @param kpiElement
	 * @param kpiRequest
	 */
	private void dateWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> leafNodeList, KpiElement kpiElement,
			KpiRequest kpiRequest) {

		CustomDateRange dateRange = KpiDataHelper.getStartAndEndDate(kpiRequest);

		// get start and end date in yyyy-mm-dd format
		String startDate = dateRange.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String endDate = dateRange.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

		Map<String, Object> resultMap = fetchKPIDataFromDb(leafNodeList, startDate, endDate, kpiRequest);

		List<KanbanIssueCustomHistory> ticketList = (List<KanbanIssueCustomHistory>) resultMap.get(STORY_HISTORY_DATA);
		Map<String, List<KanbanIssueCustomHistory>> projectWiseJiraIssue = ticketList.stream()
				.collect(Collectors.groupingBy(KanbanIssueCustomHistory::getBasicProjectConfigId));

		kpiWithFilter(projectWiseJiraIssue, mapTmp, leafNodeList, kpiElement);

	}

	/**
	 * 
	 * @param projectWiseJiraIssue
	 * @param mapTmp
	 * @param leafNodeList
	 * @param kpiElement
	 */

	private void kpiWithFilter(Map<String, List<KanbanIssueCustomHistory>> projectWiseJiraIssue,
			Map<String, Node> mapTmp, List<Node> leafNodeList, KpiElement kpiElement) {

		List<KPIExcelData> excelData = new ArrayList<>();
		String requestTrackerId = getKanbanRequestTrackerId();

		leafNodeList.forEach(node -> {
			String projectNodeId = node.getProjectFilter().getBasicProjectConfigId().toString();
			String trendLineName = node.getProjectFilter().getName();
			List<KanbanIssueCustomHistory> kanbanIssueList = projectWiseJiraIssue.get(projectNodeId);
			if (CollectionUtils.isNotEmpty(kanbanIssueList)) {
				FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
						.get(node.getProjectFilter().getBasicProjectConfigId());
				List<LeadTimeValidationDataForKanban> leadTimeList = new ArrayList<>();
				Map<String, Long> cycleMap = getCycleTime(kanbanIssueList, fieldMapping, leadTimeList);

				Map<String, List<DataCount>> dataCountMap = getDataCountObject(trendLineName, cycleMap);

				mapTmp.get(node.getId()).setValue(dataCountMap);

				LeadTimeData leadTimeData = getLeadTime(leadTimeList);

				if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
					KPIExcelUtility.populateKanbanLeadTime(excelData, trendLineName, leadTimeData);

				}

				log.debug(
						"[LEAD-TIME-KANBAN-FILTER-WISE][{}]. Open to Triage: {} . Triage to Complete: {} . Complete to Live: {}. Open to Live: {}",
						requestTrackerId, cycleMap.get(OPEN_TO_TRIAGE), cycleMap.get(TRIAGE_TO_COMPLETE),
						cycleMap.get(COMPLETE_TO_LIVE), cycleMap.get(LEAD_TIME));
			}
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.LEAD_TIME_KANBAN.getColumns());

	}

	private LeadTimeData getLeadTime(List<LeadTimeValidationDataForKanban> leadTimeValidationDataForKanbanList) {

		List<String> openToTriageDay = new ArrayList<>();
		List<String> triageToCompleteDay = new ArrayList<>();
		List<String> completeToLiveDay = new ArrayList<>();
		List<String> openToLiveDay = new ArrayList<>();
		List<String> issueNumber = new ArrayList<>();
		List<String> issueURL = new ArrayList<>();
		List<String> issueDisc = new ArrayList<>();
		LeadTimeData leadTimeData = new LeadTimeData();

		if (CollectionUtils.isNotEmpty(leadTimeValidationDataForKanbanList)) {

			for (LeadTimeValidationDataForKanban leadTimeValidationDataForKanban : leadTimeValidationDataForKanbanList) {

				issueNumber.add(leadTimeValidationDataForKanban.getIssueNumber());
				issueURL.add(leadTimeValidationDataForKanban.getUrl());
				issueDisc.add(leadTimeValidationDataForKanban.getIssueDesc());

				if (leadTimeValidationDataForKanban.getIntakeDate() != null
						&& leadTimeValidationDataForKanban.getTriageDate() != null) {
					Long diff = leadTimeValidationDataForKanban.getTriageDate().getMillis()
							- leadTimeValidationDataForKanban.getIntakeDate().getMillis();
					openToTriageDay.add(String.valueOf(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)));
				} else {
					openToTriageDay.add("NA");
				}
				if (leadTimeValidationDataForKanban.getTriageDate() != null
						&& leadTimeValidationDataForKanban.getCompletedDate() != null) {
					Long diff = leadTimeValidationDataForKanban.getCompletedDate().getMillis()
							- leadTimeValidationDataForKanban.getTriageDate().getMillis();
					triageToCompleteDay.add(String.valueOf(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)));
				} else {
					triageToCompleteDay.add("NA");
				}
				if (leadTimeValidationDataForKanban.getCompletedDate() != null
						&& leadTimeValidationDataForKanban.getLiveDate() != null) {
					Long diff = leadTimeValidationDataForKanban.getLiveDate().getMillis()
							- leadTimeValidationDataForKanban.getCompletedDate().getMillis();
					completeToLiveDay.add(String.valueOf(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)));
				} else {
					completeToLiveDay.add("NA");
				}
				if (leadTimeValidationDataForKanban.getIntakeDate() != null
						&& leadTimeValidationDataForKanban.getLiveDate() != null) {
					Long diff = leadTimeValidationDataForKanban.getLiveDate().getMillis()
							- leadTimeValidationDataForKanban.getIntakeDate().getMillis();
					openToLiveDay.add(String.valueOf(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)));
				} else {
					openToLiveDay.add("NA");
				}

			}
			leadTimeData.setIssueNumber(issueNumber);
			leadTimeData.setUrlList(issueURL);
			leadTimeData.setIssueDiscList(issueDisc);
			leadTimeData.setOpenToTriage(openToTriageDay);
			leadTimeData.setTriageToComplete(triageToCompleteDay);
			leadTimeData.setCompleteToLive(completeToLiveDay);
			leadTimeData.setLeadTime(openToLiveDay);
		}

		return leadTimeData;
	}

	/**
	 * 
	 * @param trendLineName
	 * @param cycleMap
	 * @return
	 */
	private Map<String, List<DataCount>> getDataCountObject(String trendLineName, Map<String, Long> cycleMap) {
		Map<String, List<DataCount>> dataCountMap = new HashMap<>();
		cycleMap.forEach((key, value) -> {
			DataCount dataCount = new DataCount();
			dataCount.setData(String.valueOf(value));
			dataCount.setSProjectName(trendLineName);
			dataCount.setValue(value);
			dataCount.setKpiGroup(key);
			dataCount.setHoverValue(new HashMap<>());
			dataCountMap.put(key, new ArrayList<>(Arrays.asList(dataCount)));
		});
		return dataCountMap;
	}

	/**
	 * 
	 * @param jiraIssueCustomHistories
	 * @param fieldMapping
	 * @param leadTimeList
	 * @return
	 */
	private Map<String, Long> getCycleTime(List<KanbanIssueCustomHistory> jiraIssueCustomHistories,
			FieldMapping fieldMapping, List<LeadTimeValidationDataForKanban> leadTimeList) {
		List<Long> openToTriageTime = new ArrayList<>();
		List<Long> triageToCompleteTime = new ArrayList<>();
		List<Long> completeToLiveTime = new ArrayList<>();
		List<Long> openToLiveTime = new ArrayList<>();
		Long openToTriage = 0L;
		Long triageToComplete = 0L;
		Long completeToLive = 0L;
		Long openToLive = 0L;
		Map<String, Long> cycleResult = new LinkedHashMap<>();
		if (CollectionUtils.isNotEmpty(jiraIssueCustomHistories)) {
			// in below loop create list of day difference between Intake and
			// DOR. Here Intake is created date of issue.
			for (KanbanIssueCustomHistory jiraIssueCustomHistory : jiraIssueCustomHistories) {
				List<String> triaged = fieldMapping.getJiraTicketTriagedStatus();
				List<String> completed = fieldMapping.getJiraTicketClosedStatus();
				String live = fieldMapping.getJiraLiveStatusLTK();
				LeadTimeValidationDataForKanban leadTimeValidationDataForKanban = new LeadTimeValidationDataForKanban();
				leadTimeValidationDataForKanban.setIssueNumber(jiraIssueCustomHistory.getStoryID());
				leadTimeValidationDataForKanban.setUrl(jiraIssueCustomHistory.getUrl());
				leadTimeValidationDataForKanban.setIssueDesc(jiraIssueCustomHistory.getDescription());
				CycleTime cycleTime = new CycleTime();
				cycleTime.setIntakeTime(new DateTime(jiraIssueCustomHistory.getCreatedDate()));
				leadTimeValidationDataForKanban.setIntakeDate(DateTime.parse(jiraIssueCustomHistory.getCreatedDate()));
				jiraIssueCustomHistory.getHistoryDetails()
						.forEach(kanbanIssueHistory -> updateCycleTimeValidationData(triaged, completed, live,
								leadTimeValidationDataForKanban, cycleTime, kanbanIssueHistory));
				setCycleTimeAsPerFilter(openToTriageTime, triageToCompleteTime, completeToLiveTime, openToLiveTime,
						cycleTime);
				leadTimeList.add(leadTimeValidationDataForKanban);
			}

			openToLive = AggregationUtils.averageLong(openToLiveTime);
			openToTriage = AggregationUtils.averageLong(openToTriageTime);
			triageToComplete = AggregationUtils.averageLong(triageToCompleteTime);
			completeToLive = AggregationUtils.averageLong(completeToLiveTime);
		}

		cycleResult.put(LEAD_TIME, ObjectUtils.defaultIfNull(openToLive, 0L));
		cycleResult.put(OPEN_TO_TRIAGE, ObjectUtils.defaultIfNull(openToTriage, 0L));
		cycleResult.put(TRIAGE_TO_COMPLETE, ObjectUtils.defaultIfNull(triageToComplete, 0L));
		cycleResult.put(COMPLETE_TO_LIVE, ObjectUtils.defaultIfNull(completeToLive, 0L));
		return cycleResult;
	}

	/**
	 * 
	 * @param openToTriageTime
	 * @param triageToCompleteTime
	 * @param completeToLiveTime
	 * @param openToLiveTime
	 * @param cycleTime
	 */

	private void setCycleTimeAsPerFilter(List<Long> openToTriageTime, List<Long> triageToCompleteTime,
			List<Long> completeToLiveTime, List<Long> openToLiveTime, CycleTime cycleTime) {
		if (cycleTime.getReadyTime() != null && cycleTime.getIntakeTime() != null) {
			Long diff = cycleTime.getReadyTime().getMillis() - cycleTime.getIntakeTime().getMillis();
			openToTriageTime.add(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
		}
		if (cycleTime.getDeliveryTime() != null && cycleTime.getReadyTime() != null) {
			Long diff = cycleTime.getDeliveryTime().getMillis() - cycleTime.getReadyTime().getMillis();
			triageToCompleteTime.add(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
		}
		if (cycleTime.getLiveTime() != null && cycleTime.getDeliveryTime() != null) {
			Long diff = cycleTime.getLiveTime().getMillis() - cycleTime.getDeliveryTime().getMillis();
			completeToLiveTime.add(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
		}
		if (cycleTime.getIntakeTime() != null && cycleTime.getLiveTime() != null) {
			Long diff = cycleTime.getLiveTime().getMillis() - cycleTime.getIntakeTime().getMillis();
			openToLiveTime.add(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
		}
	}

	/**
	 * 
	 * @param triaged
	 * @param completed
	 * @param live
	 * @param leadTimeValidationDataForKanban
	 * @param cycleTime
	 * @param history
	 */

	private void updateCycleTimeValidationData(List<String> triaged, List<String> completed, String live,
			LeadTimeValidationDataForKanban leadTimeValidationDataForKanban, CycleTime cycleTime,
			KanbanIssueHistory history) {
		if (cycleTime.getReadyTime() == null && CollectionUtils.emptyIfNull(triaged).contains(history.getStatus())) {
			cycleTime.setReadyTime(new DateTime(history.getActivityDate()));
			leadTimeValidationDataForKanban.setTriageDate(DateTime.parse(history.getActivityDate()));
		}
		if (CollectionUtils.emptyIfNull(completed).contains(history.getStatus())) {
			cycleTime.setDeliveryTime(new DateTime(history.getActivityDate()));
			leadTimeValidationDataForKanban.setCompletedDate(DateTime.parse(history.getActivityDate()));
		}
		if (Optional.ofNullable(live).isPresent() && live.equalsIgnoreCase(history.getStatus())) {
			cycleTime.setLiveTime(new DateTime(history.getActivityDate()));
			leadTimeValidationDataForKanban.setLiveDate(DateTime.parse(history.getActivityDate()));
		}
	}

	/**
	 *
	 * @param trendValuesMap
	 * @return
	 */
	private List<DataCountGroup> getDataCountGroups(Map<String, List<DataCount>> trendValuesMap) {
		trendValuesMap = sortTrendValueMap(trendValuesMap, filterOrder());
		Map<String, Map<String, List<DataCount>>> filterProjectWiseDc = new LinkedHashMap<>();
		trendValuesMap.forEach((filter, dataCounts) -> {
			Map<String, List<DataCount>> projectWiseDc = dataCounts.stream()
					.collect(Collectors.groupingBy(DataCount::getData));
			filterProjectWiseDc.put(filter, projectWiseDc);
		});

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		filterProjectWiseDc.forEach((filter, projectWiseDc) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			List<DataCount> dataList = new ArrayList<>();
			projectWiseDc.entrySet().stream().forEach(trend -> {
				List<DataCount> dclist = trend.getValue();
				dclist.forEach(dc -> {
					List<Long> dcValues = new ArrayList<>();
					List<DataCount> latestDcList = (List<DataCount>) dc.getValue();
					latestDcList.forEach(dataCount -> dcValues.add((Long) dataCount.getValue()));
					Long aggValue = calculateKpiValue(dcValues, KPICode.LEAD_TIME_KANBAN.getKpiId());
					String maturityValue = calculateMaturity(
							configHelperService.calculateMaturity().get(filter.replace(" ", "")),
							KPICode.LEAD_TIME_KANBAN.getKpiId(), String.valueOf(aggValue));
					dc.setMaturity(maturityValue);
					dc.setMaturityValue(aggValue);
				});
				dataList.addAll(dclist);
			});
			dataCountGroup.setFilter(filter);
			dataCountGroup.setValue(dataList);
			dataCountGroups.add(dataCountGroup);
		});
		return dataCountGroups;
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

	private List<String> filterOrder() {
		return Arrays.asList(LEAD_TIME, OPEN_TO_TRIAGE, TRIAGE_TO_COMPLETE, COMPLETE_TO_LIVE);
	}

	@Override
	public Long calculateKpiValue(List<Long> valueList, String kpiName) {
		return calculateKpiValueForLong(valueList, kpiName);
	}
}
