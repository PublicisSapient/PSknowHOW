package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
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
import com.publicissapient.kpidashboard.common.model.jira.IssueBacklogCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.repository.jira.IssueBacklogCustomHistoryRepositoryImpl;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LeadTimeServiceImpl extends JiraKPIService<Long, List<Object>, Map<String, Object>> {
	private static final String STORY_HISTORY_DATA = "storyHistoryData";
	private static final String INTAKE_TO_DOR = "Intake - DoR";
	private static final String DOR_TO_DOD = "DoR - DoD";
	private static final String DOD_TO_LIVE = "DoD - Live";
	private static final String PROJECT = "project";
	private static final String OVERALL = "Overall";
	private static final String SEARCH_BY_LEAD_TYPE = "Lead Type";
	private static final String SEARCH_BY_ISSUE_TYPE = "Issue Type";
	private static final String SEARCH_BY_ISSUE_TIME = "Lead Time";
	private static final String ISSUE_COUNT = "Issue Count";
	public static final String DAYS = "Days";
	private static final long DAYS_IN_SPRINT = 14;

	@Autowired
	private IssueBacklogCustomHistoryRepositoryImpl issueBacklogCustomHistoryRepository;

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

			if (Optional.ofNullable(fieldMapping.getJiraIntakeToDorIssueType()).isPresent()) {

				KpiDataHelper.prepareFieldMappingDefectTypeTransformation(mapOfProjectFilters, fieldMapping,
						fieldMapping.getJiraIntakeToDorIssueType(),
						JiraFeatureHistory.STORY_TYPE.getFieldValueInFeature());
				uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

			}
			List<String> status = new ArrayList<>();
			if (Optional.ofNullable(fieldMapping.getJiraDod()).isPresent()) {
				status.addAll(fieldMapping.getJiraDod());
			}

			if (Optional.ofNullable(fieldMapping.getJiraDor()).isPresent()) {
				status.add(fieldMapping.getJiraDor());
			}

			if (Optional.ofNullable(fieldMapping.getJiraLiveStatus()).isPresent()) {
				status.add(fieldMapping.getJiraLiveStatus());
			}
			mapOfProjectFilters.put("statusUpdationLog.story.changedTo", CommonUtils.convertToPatternList(status));
			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

		});
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		resultListMap.put(STORY_HISTORY_DATA, issueBacklogCustomHistoryRepository
				.findByFilterAndFromStatusMapWithDateFilter(mapOfFilters, uniqueProjectMap, startDate, endDate));

		return resultListMap;
	}

	@Override
	public String getQualifierType() {
		return KPICode.LEAD_TIME.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
								 TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		log.info("LEAD-TIME", kpiRequest.getRequestTrackerId());
		Node root = treeAggregatorDetail.getRoot();
		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes().get(PROJECT);
		DataCount dataCount = new DataCount();
		projectWiseLeafNodeValue(projectList, kpiElement, kpiRequest, dataCount);

		log.debug("[LEAD-TIME-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);
		log.debug("[LEAD-TIME-AGGREGATED-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), root);
		return kpiElement;
	}

	private void projectWiseLeafNodeValue( List<Node> leafNodeList, KpiElement kpiElement,
										   KpiRequest kpiRequest, DataCount dataCount) {
		// calculate days in based on data points in sprints
		String startDate = LocalDate.now().minusDays(customApiConfig.getSprintCountForFilters() * DAYS_IN_SPRINT)
				.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String endDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

		Map<String, Object> resultMap = fetchKPIDataFromDb(leafNodeList, startDate, endDate, kpiRequest);

		List<IssueBacklogCustomHistory> ticketList = (List<IssueBacklogCustomHistory>) resultMap
				.get(STORY_HISTORY_DATA);
		Map<String, List<IssueBacklogCustomHistory>> projectWiseJiraIssue = ticketList.stream()
				.collect(Collectors.groupingBy(IssueBacklogCustomHistory::getBasicProjectConfigId));

		kpiWithFilter(projectWiseJiraIssue, leafNodeList, kpiElement, dataCount);

	}

	private void kpiWithFilter(Map<String, List<IssueBacklogCustomHistory>> projectWiseJiraIssue,
							   List<Node> leafNodeList, KpiElement kpiElement, DataCount dataCount) {

		leafNodeList.forEach(node -> {
			List<IssueBacklogCustomHistory> issueCustomHistoryList = projectWiseJiraIssue
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

	private void getCycleTime(List<IssueBacklogCustomHistory> jiraIssueCustomHistories, FieldMapping fieldMapping,
							  List<CycleTimeValidationData> cycleTimeList, KpiElement kpiElement, DataCount trendValue) {

		Set<String> leadTimeFilter = new LinkedHashSet<>();
		leadTimeFilter.add(INTAKE_TO_DOR);
		leadTimeFilter.add(DOR_TO_DOD);
		leadTimeFilter.add(DOD_TO_LIVE);
		Set<String> issueTypeFilter = new LinkedHashSet<>();

		List<IterationKpiValue> dataList = new ArrayList<>();
		Set<IssueBacklogCustomHistory> overAllModalValues = new HashSet<>();

		if (CollectionUtils.isNotEmpty(jiraIssueCustomHistories)) {
			Map<String, List<IssueBacklogCustomHistory>> typeWiseIssues = jiraIssueCustomHistories
					.stream().collect(Collectors.groupingBy(IssueBacklogCustomHistory::getStoryType));
			typeWiseIssues.forEach((type, issueBacklogCustomHistories) -> {

				List<Long> intakeDorTime = new ArrayList<>();
				List<Long> dorDodTime = new ArrayList<>();
				List<Long> dodLiveTime = new ArrayList<>();

				Long intakeDor = 0L;
				Long dorDod = 0L;
				Long dodLive = 0L;

				issueTypeFilter.add(type);
				List<IssueBacklogCustomHistory> intakeDorModalValues = new ArrayList<>();
				List<IssueBacklogCustomHistory> dorDodModalValues = new ArrayList<>();
				List<IssueBacklogCustomHistory> dodLiveModalValues = new ArrayList<>();

				if (CollectionUtils.isNotEmpty(issueBacklogCustomHistories)) {
					// in below loop create list of day difference between Intake and
					// DOR. Here Intake is created date of issue.
					for (IssueBacklogCustomHistory issueBacklogCustomHistory : typeWiseIssues.get(type)) {
						String dor = fieldMapping.getJiraDor();
						List<String> dod = fieldMapping.getJiraDod();
						String live = fieldMapping.getJiraLiveStatus();
						CycleTimeValidationData cycleTimeValidationData = new CycleTimeValidationData();
						cycleTimeValidationData.setIssueNumber(issueBacklogCustomHistory.getStoryID());
						cycleTimeValidationData.setUrl(issueBacklogCustomHistory.getUrl());
						cycleTimeValidationData.setIssueDesc(issueBacklogCustomHistory.getDescription());
						CycleTime cycleTime = new CycleTime();
						cycleTime.setIntakeTime(issueBacklogCustomHistory.getCreatedDate());
						cycleTimeValidationData.setIntakeDate(issueBacklogCustomHistory.getCreatedDate());
						issueBacklogCustomHistory.getStatusUpdationLog()
								.forEach(sprintDetail -> updateCycleTimeValidationData(dor, dod, live,
										cycleTimeValidationData, cycleTime, sprintDetail));
						setCycleTimeAsPerFilter(intakeDorTime, dorDodTime, dodLiveTime, cycleTime, intakeDorModalValues,
								dodLiveModalValues, dorDodModalValues, issueBacklogCustomHistory);
						cycleTimeList.add(cycleTimeValidationData);
					}

					intakeDor = AggregationUtils.averageLong(intakeDorTime);
					dorDod = AggregationUtils.averageLong(dorDodTime);
					dodLive = AggregationUtils.averageLong(dodLiveTime);
				}
				IterationKpiValue intakeToDorIterationKpiValue = new IterationKpiValue();
				intakeToDorIterationKpiValue.setFilter2(type);
				intakeToDorIterationKpiValue.setFilter1(INTAKE_TO_DOR);
				List<IterationKpiData> intakeToDorKpiDataList = new ArrayList<>();
				intakeToDorKpiDataList.add(new IterationKpiData(ISSUE_COUNT,
						ObjectUtils.defaultIfNull(intakeDorTime.size(), 0L).doubleValue(), null, null, null,
						getIterationKpiModalValue(intakeDorModalValues, cycleTimeList)));
				intakeToDorKpiDataList
						.add(new IterationKpiData(INTAKE_TO_DOR, ObjectUtils.defaultIfNull(intakeDor, 0L).doubleValue(),
								null, null, DAYS, getIterationKpiModalValue(intakeDorModalValues, cycleTimeList)));
				intakeToDorIterationKpiValue.setData(intakeToDorKpiDataList);
				dataList.add(intakeToDorIterationKpiValue);

				IterationKpiValue dorToDodIterationKpiValue = new IterationKpiValue();
				dorToDodIterationKpiValue.setFilter2(type);
				dorToDodIterationKpiValue.setFilter1(DOR_TO_DOD);
				List<IterationKpiData> dorToDodKpiDataList = new ArrayList<>();
				dorToDodKpiDataList.add(new IterationKpiData(ISSUE_COUNT,
						ObjectUtils.defaultIfNull(dorDodTime.size(), 0L).doubleValue(), null, null, null,
						getIterationKpiModalValue(dorDodModalValues, cycleTimeList)));
				dorToDodKpiDataList
						.add(new IterationKpiData(DOR_TO_DOD, ObjectUtils.defaultIfNull(dorDod, 0L).doubleValue(), null,
								null, DAYS, getIterationKpiModalValue(dorDodModalValues, cycleTimeList)));
				dorToDodIterationKpiValue.setData(dorToDodKpiDataList);
				dataList.add(dorToDodIterationKpiValue);

				IterationKpiValue dodToLiveIterationKpiValue = new IterationKpiValue();
				dodToLiveIterationKpiValue.setFilter2(type);
				dodToLiveIterationKpiValue.setFilter1(DOD_TO_LIVE);
				List<IterationKpiData> dodToLiveKpiDataList = new ArrayList<>();
				dodToLiveKpiDataList.add(new IterationKpiData(ISSUE_COUNT,
						ObjectUtils.defaultIfNull(dodLiveTime.size(), 0L).doubleValue(), null, null, null,
						getIterationKpiModalValue(dodLiveModalValues, cycleTimeList)));
				dodToLiveKpiDataList
						.add(new IterationKpiData(DOD_TO_LIVE, ObjectUtils.defaultIfNull(dodLive, 0L).doubleValue(),
								null, null, DAYS, getIterationKpiModalValue(dodLiveModalValues, cycleTimeList)));
				dodToLiveIterationKpiValue.setData(dodToLiveKpiDataList);
				dataList.add(dodToLiveIterationKpiValue);

				checkAndAddToOverAllModalValueList(overAllModalValues, dodLiveModalValues);
				checkAndAddToOverAllModalValueList(overAllModalValues, dorDodModalValues);
				checkAndAddToOverAllModalValueList(overAllModalValues, intakeDorModalValues);

			});
			List<Long> intakeLiveTime = new ArrayList<>();
			List<IterationKpiModalValue> overallKpiModalValues = new ArrayList<>();
			List<IterationKpiModalValue> iterationKpiModalValues = getIterationKpiModalValue(
					overAllModalValues.stream().collect(Collectors.toList()), cycleTimeList);
			iterationKpiModalValues.stream().forEach(itr -> {
				if (null != itr.getLeadTime() && !"NA".equalsIgnoreCase(itr.getLeadTime()) && null != itr.getDorToDod()
						&& !"NA".equalsIgnoreCase(itr.getDorToDod()) && null != itr.getDodToLive()
						&& !"NA".equalsIgnoreCase(itr.getDodToLive()) && null != itr.getIntakeToDor()
						&& !"NA".equalsIgnoreCase(itr.getIntakeToDor())) {
					intakeLiveTime.add(Long.valueOf(itr.getLeadTime()));
					overallKpiModalValues.add(itr);
				}
			});
			List<IterationKpiData> intakeToLiveKpiDataList = new ArrayList<>();
			intakeToLiveKpiDataList.add(new IterationKpiData(ISSUE_COUNT,
					ObjectUtils.defaultIfNull(intakeLiveTime.size(), 0L).doubleValue(), null, null, null,
					overallKpiModalValues));
			intakeToLiveKpiDataList.add(new IterationKpiData(SEARCH_BY_ISSUE_TIME,
					ObjectUtils.defaultIfNull(AggregationUtils.averageLong(intakeLiveTime), 0L).doubleValue(), null,
					null, DAYS, overallKpiModalValues));
			IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, OVERALL,
					intakeToLiveKpiDataList, null, null);

			dataList.add(overAllIterationKpiValue);
		}

		IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_LEAD_TYPE, leadTimeFilter);
		IterationKpiFiltersOptions filter2 = new IterationKpiFiltersOptions(SEARCH_BY_ISSUE_TYPE, issueTypeFilter);
		IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, filter2);
		// Modal Heads Options
		kpiElement.setFilters(iterationKpiFilters);
		trendValue.setValue(dataList);
		kpiElement.setTrendValueList(trendValue);
	}

	private void checkAndAddToOverAllModalValueList(Set<IssueBacklogCustomHistory> overAllsdModalValues,
													List<IssueBacklogCustomHistory> modalValues) {
		modalValues.stream().forEach(modalVal -> {
			Optional<IssueBacklogCustomHistory> issueBacklog = overAllsdModalValues.stream()
					.filter(f -> f.getStoryID().equalsIgnoreCase(modalVal.getStoryID())).findFirst();
			if (!issueBacklog.isPresent()) {
				overAllsdModalValues.add(modalVal);
			}
		});
	}

	private List<IterationKpiModalValue> getIterationKpiModalValue(List<IssueBacklogCustomHistory> modalJiraIssues,
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
	 * calculate time as per filter based on cycle status time
	 *
	 * @param intakeDorTime
	 * @param dorDodTime
	 * @param dodLiveTime
	 * @param cycleTime
	 * @param intakeDorModalValues
	 * @param dodLiveModalValues
	 * @param dorDodModalValues
	 * @param issueBacklogCustomHistory
	 */
	private void setCycleTimeAsPerFilter(List<Long> intakeDorTime, List<Long> dorDodTime, List<Long> dodLiveTime,
										 CycleTime cycleTime, List<IssueBacklogCustomHistory> intakeDorModalValues,
										 List<IssueBacklogCustomHistory> dodLiveModalValues, List<IssueBacklogCustomHistory> dorDodModalValues,
										 IssueBacklogCustomHistory issueBacklogCustomHistory) {
		if (cycleTime.getReadyTime() != null && cycleTime.getIntakeTime() != null) {
			Long diff = cycleTime.getReadyTime().getMillis() - cycleTime.getIntakeTime().getMillis();
			intakeDorTime.add(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
			intakeDorModalValues.add(issueBacklogCustomHistory);
		}
		if (cycleTime.getDeliveryTime() != null && cycleTime.getReadyTime() != null) {
			Long diff = cycleTime.getDeliveryTime().getMillis() - cycleTime.getReadyTime().getMillis();
			dorDodTime.add(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
			dorDodModalValues.add(issueBacklogCustomHistory);
		}
		if (cycleTime.getLiveTime() != null && cycleTime.getDeliveryTime() != null) {
			Long diff = cycleTime.getLiveTime().getMillis() - cycleTime.getDeliveryTime().getMillis();
			dodLiveTime.add(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
			dodLiveModalValues.add(issueBacklogCustomHistory);
		}
	}

	/**
	 * Updates cycle time data for validation.
	 *
	 * @param dor
	 *            DOR
	 * @param dod
	 *            DOD
	 * @param live
	 *            Live
	 * @param cycleTimeValidationData
	 *            CycleTimeValidationData
	 * @param cycleTime
	 *            CycleTime
	 * @param jiraIssueSprint
	 *            FeatureSprint
	 */
	private void updateCycleTimeValidationData(String dor, List<String> dod, String live,
											   CycleTimeValidationData cycleTimeValidationData, CycleTime cycleTime,
											   JiraHistoryChangeLog jiraIssueSprint) {
		if (cycleTime.getReadyTime() == null && null != dor && dor.equalsIgnoreCase(jiraIssueSprint.getChangedTo())) {
			cycleTime.setReadyTime(DateTime.parse(jiraIssueSprint.getUpdatedOn().toString()));
			cycleTimeValidationData.setDorDate(DateTime.parse(jiraIssueSprint.getUpdatedOn().toString()));
		}
		if (org.apache.commons.collections.CollectionUtils.isNotEmpty(dod)
				&& dod.contains(jiraIssueSprint.getChangedTo())) {
			cycleTime.setDeliveryTime(DateTime.parse(jiraIssueSprint.getUpdatedOn().toString()));
			cycleTimeValidationData.setDodDate(DateTime.parse(jiraIssueSprint.getUpdatedOn().toString()));
		}
		if (Optional.ofNullable(live).isPresent() && live.equalsIgnoreCase(jiraIssueSprint.getChangedTo())) {
			cycleTime.setLiveTime(DateTime.parse(jiraIssueSprint.getUpdatedOn().toString()));
			cycleTimeValidationData.setLiveDate(DateTime.parse(jiraIssueSprint.getUpdatedOn().toString()));
		}
	}

	@Override
	public Long calculateKpiValue(List<Long> valueList, String kpiName) {
		return calculateKpiValueForLong(valueList, kpiName);
	}
}