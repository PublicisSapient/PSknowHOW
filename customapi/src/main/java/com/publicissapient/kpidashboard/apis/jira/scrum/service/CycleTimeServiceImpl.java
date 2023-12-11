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
import com.publicissapient.kpidashboard.apis.util.BacklogKpiHelper;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.CycleTime;
import com.publicissapient.kpidashboard.common.model.application.CycleTimeValidationData;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CycleTimeServiceImpl extends JiraKPIService<Long, List<Object>, Map<String, Object>> {
	private static final String STORY_HISTORY_DATA = "storyHistoryData";
	private static final String INTAKE_TO_DOR = "Intake - DOR";
	private static final String DOR_TO_DOD = "DOR - DOD";
	private static final String DOD_TO_LIVE = "DOD - Live";
	private static final String INTAKE_TO_DOR_KPI = "Intake to DOR";
	private static final String DOR_TO_DOD_KPI = "DOR to DOD";
	private static final String DOD_TO_LIVE_KPI = "DOD to Live";
	private static final String LEAD_TIME_KPI = "LEAD TIME";
	private static final String PROJECT = "project";
	private static final String SEARCH_BY_ISSUE_TYPE = "Issue Type";
	private static final String SEARCH_BY_DURATION = "Duration";
	public static final String DAYS = "d";
	public static final String ISSUES = "issues";
	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Override
	public Long calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
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
		KpiElement leadTimeReq = kpiRequest.getKpiList().stream().filter(k -> k.getKpiId().equalsIgnoreCase("kpi171"))
				.findFirst().orElse(new KpiElement());

		LinkedHashMap<String, Object> filterDuration = (LinkedHashMap<String, Object>) leadTimeReq.getFilterDuration();
		int value = 2; // Default value for 'value'
		String duration = CommonConstant.WEEK; // Default value for 'duration'
		String startDate = null;
		String endDate = LocalDate.now().toString();

		if (filterDuration != null) {
			value = (int) filterDuration.getOrDefault("value", 6);
			duration = (String) filterDuration.getOrDefault("duration", CommonConstant.MONTH);
		}
		if (duration.equalsIgnoreCase(CommonConstant.WEEK)) {
			startDate = LocalDate.now().minusWeeks(value).toString();
		} else if (duration.equalsIgnoreCase(CommonConstant.MONTH)) {
			startDate = LocalDate.now().minusMonths(value).toString();
		}
		Map<String, Object> resultMap = fetchKPIDataFromDb(leafNodeList, startDate, endDate, kpiRequest);

		List<JiraIssueCustomHistory> ticketList = (List<JiraIssueCustomHistory>) resultMap.get(STORY_HISTORY_DATA);

		Map<String, List<JiraIssueCustomHistory>> projectWiseJiraIssue = ticketList.stream()
				.collect(Collectors.groupingBy(JiraIssueCustomHistory::getBasicProjectConfigId));

		kpiWithFilter(projectWiseJiraIssue, leafNodeList, kpiElement, dataCount);

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
			if (Optional.ofNullable(fieldMapping.getJiraDodKPI171()).isPresent()) {
				status.addAll(fieldMapping.getJiraDodKPI171());
			}

			if (Optional.ofNullable(fieldMapping.getJiraDorKPI171()).isPresent()) {
				status.addAll(fieldMapping.getJiraDorKPI171());
			}

			if (Optional.ofNullable(fieldMapping.getJiraLiveStatusKPI171()).isPresent()) {
				status.addAll(fieldMapping.getJiraLiveStatusKPI171());
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

	@Override
	public String getQualifierType() {
		return KPICode.CYCLE_TIME.name();
	}

	private void kpiWithFilter(Map<String, List<JiraIssueCustomHistory>> projectWiseJiraIssue, List<Node> leafNodeList,
			KpiElement kpiElement, DataCount dataCount) {

		leafNodeList.forEach(node -> {
			List<JiraIssueCustomHistory> issueCustomHistoryList = projectWiseJiraIssue
					.get(node.getProjectFilter().getBasicProjectConfigId().toString());

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(node.getProjectFilter().getBasicProjectConfigId());
			List<CycleTimeValidationData> cycleTimeList = new ArrayList<>();
			getCycleTime(issueCustomHistoryList, fieldMapping, cycleTimeList, kpiElement, dataCount);
		});
		kpiElement.setModalHeads(KPIExcelColumn.CYCLE_TIME.getColumns());
	}

	private void getCycleTime(List<JiraIssueCustomHistory> jiraIssueCustomHistoriesList, FieldMapping fieldMapping,
			List<CycleTimeValidationData> cycleTimeList, KpiElement kpiElement, DataCount trendValue) {

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
						List<String> liveStatus = fieldMapping.getJiraLiveStatusKPI171().stream()
								.filter(Objects::nonNull).map(String::toLowerCase).collect(Collectors.toList());
						List<String> dodStatus = fieldMapping.getJiraDodKPI171().stream().filter(Objects::nonNull)
								.map(String::toLowerCase).collect(Collectors.toList());
						String storyFirstStatus = fieldMapping.getStoryFirstStatusKPI171();
						List<String> dor = fieldMapping.getJiraDorKPI171().stream().filter(Objects::nonNull)
								.map(String::toLowerCase).collect(Collectors.toList());

						jiraIssueCustomHistory.getStatusUpdationLog().forEach(statusUpdateLog -> {
							DateTime updateTime = DateTime.parse(statusUpdateLog.getUpdatedOn().toString());
							BacklogKpiHelper.setLiveTime(cycleTimeValidationData, cycleTime, statusUpdateLog,
									updateTime, liveStatus);
							BacklogKpiHelper.setReadyTime(cycleTimeValidationData, cycleTime, statusUpdateLog,
									updateTime, dor);
							BacklogKpiHelper.setDODTime(statusUpdateLog, updateTime, dodStatus, storyFirstStatus,
									dodStatusDateMap);
						});
						DateTime minUpdatedOn = CollectionUtils.isNotEmpty(dodStatusDateMap.values())
								? Collections.min(dodStatusDateMap.values())
								: null;
						cycleTime.setDeliveryTime(minUpdatedOn);
						cycleTime.setDeliveryLocalDateTime(DateUtil.convertDateTimeToLocalDateTime(minUpdatedOn));
						cycleTimeValidationData.setDodDate(minUpdatedOn);

						String intakeToReady = BacklogKpiHelper.setValueInCycleTime(cycleTime.getIntakeTime(),
								cycleTime.getReadyTime(), INTAKE_TO_DOR_KPI, cycleTimeValidationData, null);
						String readyToDeliver = BacklogKpiHelper.setValueInCycleTime(cycleTime.getReadyTime(),
								cycleTime.getDeliveryTime(), DOR_TO_DOD_KPI, cycleTimeValidationData, null);
						String deliverToLive = BacklogKpiHelper.setValueInCycleTime(cycleTime.getDeliveryTime(),
								cycleTime.getLiveTime(), DOD_TO_LIVE_KPI, cycleTimeValidationData, null);
						String leadTime = BacklogKpiHelper.setValueInCycleTime(cycleTime.getIntakeTime(),
								cycleTime.getLiveTime(), LEAD_TIME_KPI, cycleTimeValidationData, null);
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

					List<IterationKpiData> data = new ArrayList<>();
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

			List<IterationKpiData> data = new ArrayList<>();
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
		trendValue.setValue(dataList);
		kpiElement.setTrendValueList(trendValue);
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

	@Override
	public Long calculateKpiValue(List<Long> valueList, String kpiName) {
		return calculateKpiValueForLong(valueList, kpiName);
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