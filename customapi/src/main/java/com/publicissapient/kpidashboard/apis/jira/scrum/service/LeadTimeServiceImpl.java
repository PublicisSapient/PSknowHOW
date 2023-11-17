package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import com.publicissapient.kpidashboard.apis.jira.service.backlogdashboard.JiraBacklogKPIService;
import com.publicissapient.kpidashboard.apis.model.IterationKpiData;
import com.publicissapient.kpidashboard.apis.model.IterationKpiFilters;
import com.publicissapient.kpidashboard.apis.model.IterationKpiFiltersOptions;
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.util.AggregationUtils;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.CycleTime;
import com.publicissapient.kpidashboard.common.model.application.CycleTimeValidationData;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LeadTimeServiceImpl extends JiraBacklogKPIService {
	private static final String STORY_HISTORY_DATA = "storyHistoryData";
	private static final String INTAKE_TO_DOR = "Intake - DOR";
	private static final String DOR_TO_DOD = "DOR - DOD";
	private static final String DOD_TO_LIVE = "DOD - Live";
	private static final String LEAD_TIME = "Lead Time";
	private static final String SEARCH_BY_ISSUE_TYPE = "Issue Type";
	private static final String SEARCH_BY_DURATION = "Duration";
	public static final String DAYS = "days";
	public static final String ISSUES = "issues";
	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Override
	public Map<String, Object> fetchKPIDataFromDb(Node leafNode, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();

		List<String> basicProjectConfigIds = new ArrayList<>();
		ObjectId basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId();
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
		if (Optional.ofNullable(fieldMapping.getJiraDodKPI3()).isPresent()) {
			status.addAll(fieldMapping.getJiraDodKPI3());
		}

		if (Optional.ofNullable(fieldMapping.getJiraDorKPI3()).isPresent()) {
			status.addAll(fieldMapping.getJiraDorKPI3());
		}

		if (Optional.ofNullable(fieldMapping.getJiraLiveStatusKPI3()).isPresent()) {
			status.addAll(fieldMapping.getJiraLiveStatusKPI3());
		}
		mapOfProjectFilters.put("statusUpdationLog.story.changedTo", CommonUtils.convertToPatternList(status));
		uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		resultListMap.put(STORY_HISTORY_DATA, jiraIssueCustomHistoryRepository
				.findByFilterAndFromStatusMapWithDateFilter(mapOfFilters, uniqueProjectMap, startDate, endDate));

		return resultListMap;
	}

	@Override
	public String getQualifierType() {
		return KPICode.LEAD_TIME.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node projectNode)
			throws ApplicationException {

		log.info("LEAD-TIME -> requestTrackerId[{}]", kpiRequest.getRequestTrackerId());
		projectWiseLeafNodeValue(projectNode, kpiElement, kpiRequest);

		log.debug("[LEAD-TIME-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), projectNode);
		return kpiElement;
	}

	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(Node leafNode, KpiElement kpiElement, KpiRequest kpiRequest) {
		KpiElement leadTimeReq = kpiRequest.getKpiList().stream().filter(k -> k.getKpiId().equalsIgnoreCase("kpi3"))
				.findFirst().orElse(new KpiElement());

		LinkedHashMap<String, Object> filterDuration = (LinkedHashMap<String, Object>) leadTimeReq.getFilterDuration();
		int value = 2; // Default value for 'value'
		String duration = CommonConstant.WEEK; // Default value for 'duration'
		String startDate = null;
		String endDate = LocalDate.now().toString();

		if (filterDuration != null) {
			value = (int) filterDuration.getOrDefault("value", 2);
			duration = (String) filterDuration.getOrDefault("duration", CommonConstant.WEEK);
		}
		if (duration.equalsIgnoreCase(CommonConstant.WEEK)) {
			startDate = LocalDate.now().minusWeeks(value).toString();
		} else if (duration.equalsIgnoreCase(CommonConstant.MONTH)) {
			startDate = LocalDate.now().minusMonths(value).toString();
		}
		Map<String, Object> resultMap = fetchKPIDataFromDb(leafNode, startDate, endDate, kpiRequest);

		List<JiraIssueCustomHistory> ticketList = (List<JiraIssueCustomHistory>) resultMap.get(STORY_HISTORY_DATA);

		Map<String, List<JiraIssueCustomHistory>> projectWiseJiraIssue = ticketList.stream()
				.collect(Collectors.groupingBy(JiraIssueCustomHistory::getBasicProjectConfigId));

		kpiWithFilter(projectWiseJiraIssue, leafNode, kpiElement);

	}

	private void kpiWithFilter(Map<String, List<JiraIssueCustomHistory>> projectWiseJiraIssue, Node node,
			KpiElement kpiElement) {

		List<JiraIssueCustomHistory> issueCustomHistoryList = projectWiseJiraIssue
				.get(node.getProjectFilter().getBasicProjectConfigId().toString());

		FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
				.get(node.getProjectFilter().getBasicProjectConfigId());
		List<CycleTimeValidationData> cycleTimeList = new ArrayList<>();
		getCycleTime(issueCustomHistoryList, fieldMapping, cycleTimeList, kpiElement);
		kpiElement.setModalHeads(KPIExcelColumn.LEAD_TIME.getColumns());
	}

	private void getCycleTime(List<JiraIssueCustomHistory> jiraIssueCustomHistoriesList, FieldMapping fieldMapping,
			List<CycleTimeValidationData> cycleTimeList, KpiElement kpiElement) {

		Set<String> issueTypeFilter = new LinkedHashSet<>();

		List<IterationKpiValue> dataList = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(jiraIssueCustomHistoriesList)) {
			List<Long> overAllIntakeDorTime = new ArrayList<>();
			List<Long> overAllDorDodTime = new ArrayList<>();
			List<Long> overAllDodLiveTime = new ArrayList<>();
			List<Long> overAllLeadTimeList = new ArrayList<>();

			List<JiraIssueCustomHistory> overAllIntakeDorModalValues = new ArrayList<>();
			List<JiraIssueCustomHistory> overAllDorDodModalValues = new ArrayList<>();
			List<JiraIssueCustomHistory> overAllDodLiveModalValues = new ArrayList<>();
			List<JiraIssueCustomHistory> overAllLeadTimeModalValues = new ArrayList<>();

			Long overAllIntakeDor;
			Long overAllDorDod;
			Long overAllDodLive;
			Long overAllLeadTimeAvg;

			Map<String, List<JiraIssueCustomHistory>> typeWiseIssues = jiraIssueCustomHistoriesList.stream()
					.collect(Collectors.groupingBy(JiraIssueCustomHistory::getStoryType));
			typeWiseIssues.forEach((type, jiraIssueCustomHistories) -> {

				List<Long> intakeDorTime = new ArrayList<>();
				List<Long> dorDodTime = new ArrayList<>();
				List<Long> dodLiveTime = new ArrayList<>();
				List<Long> leadTimeList = new ArrayList<>();

				Long intakeDor = 0L;
				Long dorDod = 0L;
				Long dodLive = 0L;
				Long leadTimeAvg = 0L;

				List<JiraIssueCustomHistory> intakeDorModalValues = new ArrayList<>();
				List<JiraIssueCustomHistory> dorDodModalValues = new ArrayList<>();
				List<JiraIssueCustomHistory> dodLiveModalValues = new ArrayList<>();
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

						String intakeToReady = KpiDataHelper.calWeekHours(cycleTime.getIntakeTime(),
								cycleTime.getReadyTime());
						String readyToDeliver = KpiDataHelper.calWeekHours(cycleTime.getReadyTime(),
								cycleTime.getDeliveryTime());
						String deliverToLive = KpiDataHelper.calWeekHours(cycleTime.getDeliveryTime(),
								cycleTime.getLiveTime());
						String leadTime = KpiDataHelper.calWeekHours(cycleTime.getIntakeTime(),
								cycleTime.getLiveTime());

						transitionExist(overAllIntakeDorTime, overAllIntakeDorModalValues, intakeDorTime,
								intakeDorModalValues, jiraIssueCustomHistory, intakeToReady);
						transitionExist(overAllDorDodTime, overAllDorDodModalValues, dorDodTime, dorDodModalValues,
								jiraIssueCustomHistory, readyToDeliver);
						transitionExist(overAllDodLiveTime, overAllDodLiveModalValues, dodLiveTime, dodLiveModalValues,
								jiraIssueCustomHistory, deliverToLive);
						transitionExist(overAllLeadTimeList, overAllLeadTimeModalValues, leadTimeList,
								leadTimeModalValues, jiraIssueCustomHistory, leadTime);

						cycleTimeList.add(cycleTimeValidationData);
					}

					intakeDor = AggregationUtils.averageLong(intakeDorTime);
					dorDod = AggregationUtils.averageLong(dorDodTime);
					dodLive = AggregationUtils.averageLong(dodLiveTime);
					leadTimeAvg = AggregationUtils.averageLong(leadTimeList);

					List<IterationKpiData> data = new ArrayList<>();
					data.add(new IterationKpiData(LEAD_TIME,
							(double) Math.round(ObjectUtils.defaultIfNull(leadTimeAvg, 0L).doubleValue() / 480),
							ObjectUtils.defaultIfNull(leadTimeList.size(), 0L).doubleValue(), null, DAYS, ISSUES,
							getIterationKpiModalValue(leadTimeModalValues, cycleTimeList)));
					data.add(new IterationKpiData(INTAKE_TO_DOR,
							(double) Math.round(ObjectUtils.defaultIfNull(intakeDor, 0L).doubleValue() / 480),
							ObjectUtils.defaultIfNull(intakeDorTime.size(), 0L).doubleValue(), null, DAYS, ISSUES,
							getIterationKpiModalValue(intakeDorModalValues, cycleTimeList)));
					data.add(new IterationKpiData(DOR_TO_DOD,
							(double) Math.round(ObjectUtils.defaultIfNull(dorDod, 0L).doubleValue() / 480),
							ObjectUtils.defaultIfNull(dorDodTime.size(), 0L).doubleValue(), null, DAYS, ISSUES,
							getIterationKpiModalValue(dorDodModalValues, cycleTimeList)));
					data.add(new IterationKpiData(DOD_TO_LIVE,
							(double) Math.round(ObjectUtils.defaultIfNull(dodLive, 0L).doubleValue() / 480),
							ObjectUtils.defaultIfNull(dodLiveTime.size(), 0L).doubleValue(), null, DAYS, ISSUES,
							getIterationKpiModalValue(dodLiveModalValues, cycleTimeList)));
					IterationKpiValue iterationKpiValue = new IterationKpiValue(type, null, data);
					dataList.add(iterationKpiValue);
				}
			});
			overAllIntakeDor = AggregationUtils.averageLong(overAllIntakeDorTime);
			overAllDorDod = AggregationUtils.averageLong(overAllDorDodTime);
			overAllDodLive = AggregationUtils.averageLong(overAllDodLiveTime);
			overAllLeadTimeAvg = AggregationUtils.averageLong(overAllLeadTimeList);

			List<IterationKpiData> data = new ArrayList<>();
			data.add(new IterationKpiData(LEAD_TIME,
					(double) Math.round(ObjectUtils.defaultIfNull(overAllLeadTimeAvg, 0L).doubleValue() / 480),
					ObjectUtils.defaultIfNull(overAllLeadTimeList.size(), 0L).doubleValue(), null, DAYS, ISSUES,
					getIterationKpiModalValue(overAllLeadTimeModalValues, cycleTimeList)));
			data.add(new IterationKpiData(INTAKE_TO_DOR,
					(double) Math.round(ObjectUtils.defaultIfNull(overAllIntakeDor, 0L).doubleValue() / 480),
					ObjectUtils.defaultIfNull(overAllIntakeDorTime.size(), 0L).doubleValue(), null, DAYS, ISSUES,
					getIterationKpiModalValue(overAllIntakeDorModalValues, cycleTimeList)));
			data.add(new IterationKpiData(DOR_TO_DOD,
					(double) Math.round(ObjectUtils.defaultIfNull(overAllDorDod, 0L).doubleValue() / 480),
					ObjectUtils.defaultIfNull(overAllDorDodTime.size(), 0L).doubleValue(), null, DAYS, ISSUES,
					getIterationKpiModalValue(overAllDorDodModalValues, cycleTimeList)));
			data.add(new IterationKpiData(DOD_TO_LIVE,
					(double) Math.round(ObjectUtils.defaultIfNull(overAllDodLive, 0L).doubleValue() / 480),
					ObjectUtils.defaultIfNull(overAllDodLiveTime.size(), 0L).doubleValue(), null, DAYS, ISSUES,
					getIterationKpiModalValue(overAllDodLiveModalValues, cycleTimeList)));
			IterationKpiValue iterationKpiValue = new IterationKpiValue(CommonConstant.OVERALL, CommonConstant.OVERALL,
					data);
			dataList.add(iterationKpiValue);
		}
		Set<String> duration = new LinkedHashSet<>(
				Arrays.asList("Past Week", "Past 2 Weeks", "Past Month", "Past 3 Months", "Past 6 Months"));
		IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_DURATION, duration);
		IterationKpiFiltersOptions filter2 = new IterationKpiFiltersOptions(SEARCH_BY_ISSUE_TYPE, issueTypeFilter);
		IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, filter2);
		// Modal Heads Options
		kpiElement.setFilters(iterationKpiFilters);
		kpiElement.setTrendValueList(dataList);
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
	 * 
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
		List<String> dor = fieldMapping.getJiraDorKPI3().stream().filter(Objects::nonNull).map(String::toLowerCase)
				.collect(Collectors.toList());
		List<String> dod = fieldMapping.getJiraDodKPI3().stream().map(String::toLowerCase).collect(Collectors.toList());
		List<String> live = fieldMapping.getJiraLiveStatusKPI3().stream().filter(Objects::nonNull)
				.map(String::toLowerCase).collect(Collectors.toList());
		String storyFirstStatus = fieldMapping.getStoryFirstStatusKPI3();
		if (cycleTime.getReadyTime() == null && CollectionUtils.isNotEmpty(dor)
				&& dor.contains(statusUpdateLog.getChangedTo().toLowerCase())) {
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
		if (cycleTime.getLiveTime() == null && CollectionUtils.isNotEmpty(live)
				&& live.contains(statusUpdateLog.getChangedTo().toLowerCase())) {
			cycleTime.setLiveTime(updatedOn);
			cycleTimeValidationData.setLiveDate(updatedOn);
		}
	}

	private void transitionExist(List<Long> overAllTimeList, List<JiraIssueCustomHistory> overAllTransitionModalValues,
			List<Long> filterTimeList, List<JiraIssueCustomHistory> transitionModalValues,
			JiraIssueCustomHistory jiraIssueCustomHistory, String transitionTime) {
		if (!transitionTime.equalsIgnoreCase(Constant.NOT_AVAILABLE)) {
			long time = KpiDataHelper.calculateTimeInDays(Long.parseLong(transitionTime));
			filterTimeList.add(time);
			overAllTimeList.add(time);
			transitionModalValues.add(jiraIssueCustomHistory);
			overAllTransitionModalValues.add(jiraIssueCustomHistory);
		}
	}

}