package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.JiraFeatureHistory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.IterationKpiData;
import com.publicissapient.kpidashboard.apis.model.IterationKpiFilters;
import com.publicissapient.kpidashboard.apis.model.IterationKpiFiltersOptions;
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.AggregationUtils;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.model.application.CycleTime;
import com.publicissapient.kpidashboard.common.model.application.CycleTimeValidationData;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LeadTimeServiceImpl extends JiraKPIService<Long, List<Object>, Map<String, Object>> {
	private static final String STORY_HISTORY_DATA = "storyHistoryData";
	private static final String INTAKE_TO_DOR = "Intake - DoR";
	private static final String DOR_TO_DOD = "DoR - DoD";
	private static final String DOD_TO_LIVE = "DoD - Live";
	private static final String INTAKE_TO_DOD = "Intake - DoD";
	private static final String DOR_TO_LIVE = "DoR - Live";
	private static final String PROJECT = "project";
	private static final String LEAD_TIME = "Lead Time";
	private static final String SEARCH_BY_ISSUE_TYPE = "Issue Type";
	private static final String ISSUE_COUNT = "Issue Count";
	public static final String DAYS = "Days";
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

			if (Optional.ofNullable(fieldMapping.getJiraIssueTypeLT()).isPresent()) {

				KpiDataHelper.prepareFieldMappingDefectTypeTransformation(mapOfProjectFilters, fieldMapping.getJiradefecttypeLT(),
						fieldMapping.getJiraIssueTypeLT(),
						JiraFeatureHistory.STORY_TYPE.getFieldValueInFeature());
				uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

			}
			List<String> status = new ArrayList<>();
			if (Optional.ofNullable(fieldMapping.getJiraDodLT()).isPresent()) {
				status.addAll(fieldMapping.getJiraDodLT());
			}

			if (Optional.ofNullable(fieldMapping.getJiraDorLT()).isPresent()) {
				status.add(fieldMapping.getJiraDorLT());
			}

			if (Optional.ofNullable(fieldMapping.getJiraLiveStatusLT()).isPresent()) {
				status.add(fieldMapping.getJiraLiveStatusLT());
			}
			mapOfProjectFilters.put("statusUpdationLog.story.changedTo", CommonUtils.convertToPatternList(status));
			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

		});
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		resultListMap.put(STORY_HISTORY_DATA, jiraIssueCustomHistoryRepository
				.findByFilterAndFromStatusMap(mapOfFilters, uniqueProjectMap));

		return resultListMap;
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
		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes().get(PROJECT);
		DataCount dataCount = new DataCount();
		projectWiseLeafNodeValue(projectList, kpiElement, kpiRequest, dataCount);

		log.debug("[LEAD-TIME-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);
		return kpiElement;
	}

	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(List<Node> leafNodeList, KpiElement kpiElement, KpiRequest kpiRequest,
			DataCount dataCount) {

		Map<String, Object> resultMap = fetchKPIDataFromDb(leafNodeList, null, null, kpiRequest);

		List<JiraIssueCustomHistory> ticketList = (List<JiraIssueCustomHistory>) resultMap.get(STORY_HISTORY_DATA);

		Map<String, List<JiraIssueCustomHistory>> projectWiseJiraIssue = ticketList.stream()
				.collect(Collectors.groupingBy(JiraIssueCustomHistory::getBasicProjectConfigId));

		kpiWithFilter(projectWiseJiraIssue, leafNodeList, kpiElement, dataCount);

	}

	private void kpiWithFilter(Map<String, List<JiraIssueCustomHistory>> projectWiseJiraIssue, List<Node> leafNodeList,
			KpiElement kpiElement, DataCount dataCount) {

		leafNodeList.forEach(node -> {
			List<JiraIssueCustomHistory> issueCustomHistoryList = projectWiseJiraIssue
					.get(node.getProjectFilter().getBasicProjectConfigId().toString());
			if (CollectionUtils.isNotEmpty(issueCustomHistoryList)) {
				FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
						.get(node.getProjectFilter().getBasicProjectConfigId());
				List<CycleTimeValidationData> cycleTimeList = new ArrayList<>();
				getCycleTime(issueCustomHistoryList, fieldMapping, cycleTimeList, kpiElement, dataCount);
			}
		});
		kpiElement.setModalHeads(KPIExcelColumn.LEAD_TIME.getColumns());
	}

	private void getCycleTime(List<JiraIssueCustomHistory> jiraIssueCustomHistoriesList, FieldMapping fieldMapping,
			List<CycleTimeValidationData> cycleTimeList, KpiElement kpiElement, DataCount trendValue) {

		Set<String> leadTimeFilter = new LinkedHashSet<>();
		leadTimeFilter.add(LEAD_TIME);
		leadTimeFilter.add(INTAKE_TO_DOR);
		leadTimeFilter.add(DOR_TO_DOD);
		leadTimeFilter.add(DOD_TO_LIVE);
		leadTimeFilter.add(INTAKE_TO_DOD);
		leadTimeFilter.add(DOR_TO_LIVE);
		Set<String> issueTypeFilter = new LinkedHashSet<>();

		List<IterationKpiValue> dataList = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(jiraIssueCustomHistoriesList)) {
			Map<String, List<JiraIssueCustomHistory>> typeWiseIssues = jiraIssueCustomHistoriesList.stream()
					.collect(Collectors.groupingBy(JiraIssueCustomHistory::getStoryType));
			typeWiseIssues.forEach((type, jiraIssueCustomHistories) -> {

				List<Long> intakeDorTime = new ArrayList<>();
				List<Long> dorDodTime = new ArrayList<>();
				List<Long> dodLiveTime = new ArrayList<>();
				List<Long> intakeDodTime = new ArrayList<>();
				List<Long> dorLiveTime = new ArrayList<>();
				List<Long> leadTimeList = new ArrayList<>();

				Long intakeDor = 0L;
				Long dorDod = 0L;
				Long dodLive = 0L;
				Long intakeDod = 0L;
				Long dorLive = 0L;
				Long leadTimeAvg = 0L;

				List<JiraIssueCustomHistory> intakeDorModalValues = new ArrayList<>();
				List<JiraIssueCustomHistory> dorDodModalValues = new ArrayList<>();
				List<JiraIssueCustomHistory> dodLiveModalValues = new ArrayList<>();
				List<JiraIssueCustomHistory> intakeDodModalValues = new ArrayList<>();
				List<JiraIssueCustomHistory> dorLiveModalValues = new ArrayList<>();
				List<JiraIssueCustomHistory> leadTimeModalValues = new ArrayList<>();

				if (CollectionUtils.isNotEmpty(jiraIssueCustomHistories)) {
					// in below loop create list of day difference between Intake and
					// DOR. Here Intake is created date of issue.
					issueTypeFilter.add(type);
					for (JiraIssueCustomHistory jiraIssueCustomHistory : jiraIssueCustomHistories) {
						CycleTimeValidationData cycleTimeValidationData = new CycleTimeValidationData();
						cycleTimeValidationData.setIssueNumber(jiraIssueCustomHistory.getStoryID());
						cycleTimeValidationData.setUrl(jiraIssueCustomHistory.getUrl());
						cycleTimeValidationData.setIssueDesc(jiraIssueCustomHistory.getDescription());
						CycleTime cycleTime = new CycleTime();
						cycleTime.setIntakeTime(jiraIssueCustomHistory.getCreatedDate());
						cycleTimeValidationData.setIntakeDate(jiraIssueCustomHistory.getCreatedDate());
						Map<String, DateTime> dodStatusDateMap = new HashMap<>();
						jiraIssueCustomHistory.getStatusUpdationLog()
								.forEach(statusUpdateLog -> updateCycleTimeValidationData(fieldMapping,
										cycleTimeValidationData, cycleTime, dodStatusDateMap, statusUpdateLog));

						String readyToIntake = DateUtil.calWeekHours(cycleTime.getIntakeTime(),
								cycleTime.getReadyTime());
						String readyToDeliver = DateUtil.calWeekHours(cycleTime.getReadyTime(),
								cycleTime.getDeliveryTime());
						String deliverToLive = DateUtil.calWeekHours(cycleTime.getDeliveryTime(),
								cycleTime.getLiveTime());
						String leadTime = DateUtil.calWeekHours(cycleTime.getIntakeTime(),
								cycleTime.getLiveTime());
						if (!readyToIntake.equalsIgnoreCase(Constant.NOT_AVAILABLE)) {
							intakeDorTime.add(DateUtil.calculateTimeInDays(Long.parseLong(readyToIntake)));
							intakeDorModalValues.add(jiraIssueCustomHistory);
						}
						if (!readyToDeliver.equalsIgnoreCase(Constant.NOT_AVAILABLE)) {
							dorDodTime.add(DateUtil.calculateTimeInDays(Long.parseLong(readyToDeliver)));
							dorDodModalValues.add(jiraIssueCustomHistory);
						}
						if (!deliverToLive.equalsIgnoreCase(Constant.NOT_AVAILABLE)) {
							dodLiveTime.add(DateUtil.calculateTimeInDays(Long.parseLong(deliverToLive)));
							dodLiveModalValues.add(jiraIssueCustomHistory);
						}
						String intakeToDod = DateUtil.calWeekHours(cycleTime.getIntakeTime(),
								cycleTime.getDeliveryTime());
						if (cycleTime.getReadyTime() != null && !intakeToDod.equalsIgnoreCase(Constant.NOT_AVAILABLE)) {
							intakeDodTime.add(DateUtil.calculateTimeInDays(Long.parseLong(intakeToDod)));
							intakeDodModalValues.add(jiraIssueCustomHistory);
						}
						String dorToLive = DateUtil.calWeekHours(cycleTime.getReadyTime(),
								cycleTime.getLiveTime());
						if (cycleTime.getDeliveryTime() != null
								&& !dorToLive.equalsIgnoreCase(Constant.NOT_AVAILABLE)) {
							dorLiveTime.add(DateUtil.calculateTimeInDays(Long.parseLong(dorToLive)));
							dorLiveModalValues.add(jiraIssueCustomHistory);
						}
						if (!leadTime.equalsIgnoreCase(Constant.NOT_AVAILABLE)) {
							leadTimeList.add(DateUtil.calculateTimeInDays(Long.parseLong(leadTime)));
							leadTimeModalValues.add(jiraIssueCustomHistory);
						}

						cycleTimeList.add(cycleTimeValidationData);
					}

					intakeDor = AggregationUtils.averageLong(intakeDorTime);
					dorDod = AggregationUtils.averageLong(dorDodTime);
					dodLive = AggregationUtils.averageLong(dodLiveTime);
					intakeDod = AggregationUtils.averageLong(intakeDodTime);
					dorLive = AggregationUtils.averageLong(dorLiveTime);
					leadTimeAvg = AggregationUtils.averageLong(leadTimeList);
				}
				prepareIterationKpiValue(type, INTAKE_TO_DOR, intakeDor, intakeDorTime, intakeDorModalValues,
						cycleTimeList, dataList);
				prepareIterationKpiValue(type, DOR_TO_DOD, dorDod, dorDodTime, dorDodModalValues, cycleTimeList,
						dataList);
				prepareIterationKpiValue(type, DOD_TO_LIVE, dodLive, dodLiveTime, dodLiveModalValues, cycleTimeList,
						dataList);
				prepareIterationKpiValue(type, INTAKE_TO_DOD, intakeDod, intakeDodTime, intakeDodModalValues,
						cycleTimeList, dataList);
				prepareIterationKpiValue(type, DOR_TO_LIVE, dorLive, dorLiveTime, dorLiveModalValues, cycleTimeList,
						dataList);
				prepareIterationKpiValue(type, LEAD_TIME, leadTimeAvg, leadTimeList, leadTimeModalValues, cycleTimeList,
						dataList);

			});
		}

		IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(LEAD_TIME, leadTimeFilter);
		IterationKpiFiltersOptions filter2 = new IterationKpiFiltersOptions(SEARCH_BY_ISSUE_TYPE, issueTypeFilter);
		IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, filter2);
		// Modal Heads Options
		kpiElement.setFilters(iterationKpiFilters);
		trendValue.setValue(dataList);
		kpiElement.setTrendValueList(trendValue);
	}

	private void prepareIterationKpiValue(String issueTypeFilterName, String leadTimeFilterName, Long transitionTime,
			List<Long> transitionTimeList, List<JiraIssueCustomHistory> transitionModalValues,
			List<CycleTimeValidationData> cycleTimeList, List<IterationKpiValue> dataList) {
		IterationKpiValue intakeToDorIterationKpiValue = new IterationKpiValue();
		intakeToDorIterationKpiValue.setFilter2(issueTypeFilterName);
		intakeToDorIterationKpiValue.setFilter1(leadTimeFilterName);
		List<IterationKpiData> intakeToDorKpiDataList = new ArrayList<>();
		intakeToDorKpiDataList.add(new IterationKpiData(leadTimeFilterName,
				(double) Math.round(ObjectUtils.defaultIfNull(transitionTime, 0L).doubleValue()/480), null, null, DAYS, null));
		intakeToDorKpiDataList.add(new IterationKpiData(ISSUE_COUNT,
				ObjectUtils.defaultIfNull(transitionTimeList.size(), 0L).doubleValue(), null, null, null,
				getIterationKpiModalValue(transitionModalValues, cycleTimeList)));
		intakeToDorIterationKpiValue.setData(intakeToDorKpiDataList);
		dataList.add(intakeToDorIterationKpiValue);
	}

	private List<IterationKpiModalValue> getIterationKpiModalValue(List<JiraIssueCustomHistory> modalJiraIssues,
			List<CycleTimeValidationData> cycleTimeList) {
		List<IterationKpiModalValue> iterationKpiDataList = new ArrayList<>();
		Map<String, IterationKpiModalValue> dataMap = KpiDataHelper
				.createMapOfModalObjectFromJiraHistory(modalJiraIssues, cycleTimeList);
		for (Map.Entry<String, IterationKpiModalValue> entry : dataMap.entrySet()) {
			iterationKpiDataList.add(dataMap.get(entry.getKey()));
		}
		return iterationKpiDataList;
	}

	/**
	 * Updates cycle time data for validation.
	 * @param fieldMapping
	 * @param cycleTimeValidationData
	 * @param cycleTime
	 * @param dodStatusDateMap
	 * @param statusUpdateLog
	 */
	private void updateCycleTimeValidationData(FieldMapping fieldMapping,
			CycleTimeValidationData cycleTimeValidationData, CycleTime cycleTime,
			Map<String, DateTime> dodStatusDateMap, JiraHistoryChangeLog statusUpdateLog) {
		DateTime updatedOn = DateTime.parse(statusUpdateLog.getUpdatedOn().toString());
		String dor = fieldMapping.getJiraDorLT();
		List<String> dod = fieldMapping.getJiraDodLT().stream().map(String::toLowerCase).collect(Collectors.toList());
		String live = fieldMapping.getJiraLiveStatusLT();
		String storyFirstStatus = fieldMapping.getStoryFirstStatusLT();
		if (cycleTime.getReadyTime() == null && null != dor && dor.equalsIgnoreCase(statusUpdateLog.getChangedTo())) {
			cycleTime.setReadyTime(updatedOn);
			cycleTimeValidationData.setDorDate(updatedOn);
		} // case of reopening the ticket
		if (CollectionUtils.isNotEmpty(dod) && statusUpdateLog.getChangedFrom() != null
				&& dod.contains(statusUpdateLog.getChangedFrom().toLowerCase())
				&& storyFirstStatus.equalsIgnoreCase(statusUpdateLog.getChangedTo())) {
			dodStatusDateMap.clear();
			cycleTime.setDeliveryTime(null);
			cycleTimeValidationData.setDodDate(null);
		} // taking the delivery date of first closed status date of last closed cycle
		if (CollectionUtils.isNotEmpty(dod) && dod.contains(statusUpdateLog.getChangedTo().toLowerCase())) {
			if (dodStatusDateMap.containsKey(statusUpdateLog.getChangedTo().toLowerCase())) {
				dodStatusDateMap.clear();
			}
			dodStatusDateMap.put(statusUpdateLog.getChangedTo(), updatedOn);
			DateTime minUpdatedOn = Collections.min(dodStatusDateMap.values());
			cycleTime.setDeliveryTime(minUpdatedOn);
			cycleTimeValidationData.setDodDate(minUpdatedOn);
		}
		if (Optional.ofNullable(live).isPresent() && live.equalsIgnoreCase(statusUpdateLog.getChangedTo())) {
			cycleTime.setLiveTime(updatedOn);
			cycleTimeValidationData.setLiveDate(updatedOn);
		}
	}

	@Override
	public Long calculateKpiValue(List<Long> valueList, String kpiName) {
		return calculateKpiValueForLong(valueList, kpiName);
	}
}