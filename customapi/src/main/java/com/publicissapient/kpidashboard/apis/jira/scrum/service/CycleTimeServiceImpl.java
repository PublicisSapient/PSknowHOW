package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
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
public class CycleTimeServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {
	private static final String STORY_HISTORY_DATA = "storyHistoryData";
	private static final String INTAKE_TO_DOR = "Intake to DOR";
	private static final String DOR_TO_DOD = "DOR to DOD";
	private static final String DOD_TO_LIVE = "DOD to Live";
	private static final String LEAD_TIME = "LEAD TIME";
	private static final String PROJECT = "project";
	private static final String SEARCH_BY_ISSUE_TYPE = "Issue Type";
	private static final String SEARCH_BY_DURATION = "Duration";
	public static final String DAYS = "days";
	public static final String DAY = "d";
	public static final String ISSUES = "issues";
	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		log.info("CycleTimeServiceImpl-> requestTrackerId[{}]", kpiRequest.getRequestTrackerId());
		Node root = treeAggregatorDetail.getRoot();
		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes().get(PROJECT);
		DataCount dataCount = new DataCount();
		projectWiseLeafNodeValue(projectList, kpiElement, kpiRequest, dataCount);

		log.debug("[CycleTimeServiceImpl-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);
		return kpiElement;
	}

	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(List<Node> leafNodeList, KpiElement kpiElement, KpiRequest kpiRequest,
			DataCount dataCount) {
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);
		if (leafNode != null) {
			Object basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			List<String> rangeList = new LinkedList<>(customApiConfig.getCycleTimeRange());
			Map<String, Object> resultMap = fetchKPIDataFromDb(leafNodeList, LocalDate.now().minusMonths(6).toString(),
					LocalDate.now().toString(), kpiRequest);

			List<JiraIssueCustomHistory> jiraIssueCustomHistoriesList = (List<JiraIssueCustomHistory>) resultMap
					.get(STORY_HISTORY_DATA);
			if (CollectionUtils.isNotEmpty(jiraIssueCustomHistoriesList)) {
				List<CycleTimeValidationData> cycleTimeValidationDataList = new ArrayList<>();
				Set<String> allIssueTypes = jiraIssueCustomHistoriesList.stream()
						.map(JiraIssueCustomHistory::getStoryType).collect(Collectors.toSet());
				List<IterationKpiValue> cycleTime = getCycleTime(jiraIssueCustomHistoriesList, fieldMapping,
						cycleTimeValidationDataList, rangeList, allIssueTypes);
				IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_DURATION,
						new LinkedHashSet<>(rangeList));
				IterationKpiFiltersOptions filter2 = new IterationKpiFiltersOptions(SEARCH_BY_ISSUE_TYPE,
						allIssueTypes);
				IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, filter2);
				// Modal Heads Options
				kpiElement.setFilters(iterationKpiFilters);
				dataCount.setValue(cycleTime);
				kpiElement.setTrendValueList(dataCount);
			}

			kpiElement.setModalHeads(KPIExcelColumn.CYCLE_TIME.getColumns());
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();

		List<String> basicProjectConfigIds = new ArrayList<>();
		leafNodeList.forEach(leaf -> {
			Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
			Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();

			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

			basicProjectConfigIds.add(basicProjectConfigId.toString());

			if (Optional.ofNullable(fieldMapping.getJiraIssueTypeKPI171()).isPresent()) {

				KpiDataHelper.prepareFieldMappingDefectTypeTransformation(mapOfProjectFilters,
						fieldMapping.getJiradefecttype(), fieldMapping.getJiraIssueTypeKPI171(),
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

			mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
					basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

			resultListMap.put(STORY_HISTORY_DATA, jiraIssueCustomHistoryRepository
					.findByFilterAndFromStatusMapWithDateFilter(mapOfFilters, uniqueProjectMap, startDate, endDate));
		});

		return resultListMap;
	}

	protected List<IterationKpiValue> getCycleTime(List<JiraIssueCustomHistory> jiraIssueCustomHistoriesList,
			FieldMapping fieldMapping, List<CycleTimeValidationData> cycleTimeValidationDataList,
			List<String> rangeList, Set<String> allIssueTypes) {
		Map<Long, String> monthRangeMap = new HashMap<>();
		Map<String, Map<String, List<JiraIssueCustomHistory>>> dodToLiveRangeMap = new LinkedHashMap<>();
		BacklogKpiHelper.initializeRangeMapForProjects(dodToLiveRangeMap, rangeList, monthRangeMap);
		Map<String, Map<String, List<JiraIssueCustomHistory>>> dorToDODRangeMap = new LinkedHashMap<>();
		BacklogKpiHelper.initializeRangeMapForProjects(dorToDODRangeMap, rangeList, monthRangeMap);
		Map<String, Map<String, List<JiraIssueCustomHistory>>> intakeToDORRangeMap = new LinkedHashMap<>();
		BacklogKpiHelper.initializeRangeMapForProjects(intakeToDORRangeMap, rangeList, monthRangeMap);
		Set<String> issueTypes = new HashSet<>();
		if (CollectionUtils.isNotEmpty(jiraIssueCustomHistoriesList) && fieldMapping != null) {
			for (JiraIssueCustomHistory jiraIssueCustomHistory : jiraIssueCustomHistoriesList) {
				CycleTimeValidationData cycleTimeValidationData = new CycleTimeValidationData();
				cycleTimeValidationData.setIssueNumber(jiraIssueCustomHistory.getStoryID());
				cycleTimeValidationData.setUrl(jiraIssueCustomHistory.getUrl());
				cycleTimeValidationData.setIssueDesc(jiraIssueCustomHistory.getDescription());
				cycleTimeValidationData.setIssueType(jiraIssueCustomHistory.getStoryType());
				CycleTime cycleTime = new CycleTime();
				cycleTime.setIntakeTime(jiraIssueCustomHistory.getCreatedDate());
				cycleTimeValidationData.setIntakeDate(jiraIssueCustomHistory.getCreatedDate());

				List<String> liveStatus = fieldMapping.getJiraLiveStatusKPI171().stream().filter(Objects::nonNull)
						.map(String::toLowerCase).collect(Collectors.toList());
				List<String> dodStatus = fieldMapping.getJiraDodKPI171().stream().filter(Objects::nonNull)
						.map(String::toLowerCase).collect(Collectors.toList());
				String storyFirstStatus = fieldMapping.getStoryFirstStatusKPI171();
				List<String> dor = fieldMapping.getJiraDorKPI171().stream().filter(Objects::nonNull)
						.map(String::toLowerCase).collect(Collectors.toList());

				Map<String, DateTime> dodStatusDateMap = new HashMap<>();
				jiraIssueCustomHistory.getStatusUpdationLog().forEach(statusUpdateLog -> {
					DateTime updateTime = DateTime.parse(statusUpdateLog.getUpdatedOn().toString());
					BacklogKpiHelper.setLiveTime(cycleTimeValidationData, cycleTime, statusUpdateLog, updateTime,
							liveStatus);
					BacklogKpiHelper.setReadyTime(cycleTimeValidationData, cycleTime, statusUpdateLog, updateTime, dor);
					BacklogKpiHelper.setDODTime(statusUpdateLog, updateTime, dodStatus, storyFirstStatus,
							dodStatusDateMap);
				});

				DateTime minUpdatedOn = CollectionUtils.isNotEmpty(dodStatusDateMap.values())
						? Collections.min(dodStatusDateMap.values())
						: null;
				cycleTime.setDeliveryTime(minUpdatedOn);
				cycleTime.setDeliveryLocalDateTime(DateUtil.convertDateTimeToLocalDateTime(minUpdatedOn));
				cycleTimeValidationData.setDodDate(minUpdatedOn);
				BacklogKpiHelper.setRangeWiseJiraIssuesMap(intakeToDORRangeMap, jiraIssueCustomHistory,
						cycleTime.getReadyLocalDateTime(), monthRangeMap);
				BacklogKpiHelper.setRangeWiseJiraIssuesMap(dorToDODRangeMap, jiraIssueCustomHistory,
						cycleTime.getDeliveryLocalDateTime(), monthRangeMap);
				BacklogKpiHelper.setRangeWiseJiraIssuesMap(dodToLiveRangeMap, jiraIssueCustomHistory,
						cycleTime.getLiveLocalDateTime(), monthRangeMap);

				BacklogKpiHelper.setValueInCycleTime(cycleTime.getIntakeTime(), cycleTime.getReadyTime(), INTAKE_TO_DOR,
						cycleTimeValidationData, issueTypes);
				BacklogKpiHelper.setValueInCycleTime(cycleTime.getReadyTime(), cycleTime.getDeliveryTime(), DOR_TO_DOD,
						cycleTimeValidationData, issueTypes);
				BacklogKpiHelper.setValueInCycleTime(cycleTime.getDeliveryTime(), cycleTime.getLiveTime(), DOD_TO_LIVE,
						cycleTimeValidationData, issueTypes);
				BacklogKpiHelper.setValueInCycleTime(jiraIssueCustomHistory.getCreatedDate(), cycleTime.getLiveTime(),
						LEAD_TIME, cycleTimeValidationData, issueTypes);
				cycleTimeValidationDataList.add(cycleTimeValidationData);
			}
		}
		return setDataCountMap(intakeToDORRangeMap, dorToDODRangeMap, dodToLiveRangeMap, cycleTimeValidationDataList,
				rangeList, allIssueTypes);

	}

	private List<IterationKpiValue> setDataCountMap(
			Map<String, Map<String, List<JiraIssueCustomHistory>>> intakeToDORRangeMap,
			Map<String, Map<String, List<JiraIssueCustomHistory>>> dorToDODRangeMap,
			Map<String, Map<String, List<JiraIssueCustomHistory>>> dodToLiveRangeMap,
			List<CycleTimeValidationData> cycleTimeValidationDataList, List<String> rangeList,
			Set<String> allIssueTypes) {
		List<IterationKpiValue> dataList = new ArrayList<>();
		Map<String, Long> issueWiseIntakeToDOR = cycleTimeValidationDataList.stream()
				.filter(data -> ObjectUtils.isNotEmpty(data.getIntakeTime()))
				.collect(Collectors.toMap(CycleTimeValidationData::getIssueNumber,
						CycleTimeValidationData::getIntakeTime, (existing, replacement) -> existing));

		Map<String, Long> issueWiseDORToDOD = cycleTimeValidationDataList.stream()
				.filter(data -> ObjectUtils.isNotEmpty(data.getDorTime()))
				.collect(Collectors.toMap(CycleTimeValidationData::getIssueNumber, CycleTimeValidationData::getDorTime,
						(existing, replacement) -> existing));

		Map<String, Long> issueWiseDODToLive = cycleTimeValidationDataList.stream()
				.filter(data -> ObjectUtils.isNotEmpty(data.getDodTime()))
				.collect(Collectors.toMap(CycleTimeValidationData::getIssueNumber, CycleTimeValidationData::getDodTime,
						(existing, replacement) -> existing));

		for (String range : rangeList) {
			for (String issueType : allIssueTypes) {
				List<IterationKpiData> kpiDataList = new ArrayList<>();

				populateDataCountList(intakeToDORRangeMap, issueWiseIntakeToDOR, range, issueType, kpiDataList,
						cycleTimeValidationDataList, INTAKE_TO_DOR);
				populateDataCountList(dorToDODRangeMap, issueWiseDORToDOD, range, issueType, kpiDataList,
						cycleTimeValidationDataList, DOR_TO_DOD);
				populateDataCountList(dodToLiveRangeMap, issueWiseDODToLive, range, issueType, kpiDataList,
						cycleTimeValidationDataList, DOD_TO_LIVE);
				IterationKpiValue iterationKpiValue = new IterationKpiValue(range, issueType, kpiDataList);
				dataList.add(iterationKpiValue);
			}
		}
		List<IterationKpiData> data = new ArrayList<>();
		List<Long> overAllIntakeDor = new ArrayList<>(issueWiseIntakeToDOR.values());
		List<Long> overAllDorDod = new ArrayList<>(issueWiseDORToDOD.values());
		List<Long> overAllDodLive = new ArrayList<>(issueWiseDODToLive.values());

		List<JiraIssueCustomHistory> overAllIntakeDorModalValues = intakeToDORRangeMap.values().stream()
				.flatMap(innerMap -> innerMap.values().stream()).flatMap(List::stream).collect(Collectors.toList());

		List<JiraIssueCustomHistory> overAllDorDodModalValues = dorToDODRangeMap.values().stream()
				.flatMap(innerMap -> innerMap.values().stream()).flatMap(List::stream).collect(Collectors.toList());

		List<JiraIssueCustomHistory> overAllDodLiveModalValues = dodToLiveRangeMap.values().stream()
				.flatMap(innerMap -> innerMap.values().stream()).flatMap(List::stream).collect(Collectors.toList());

		data.add(new IterationKpiData(INTAKE_TO_DOR, (double) Math.round(
				ObjectUtils.defaultIfNull(AggregationUtils.averageLong(overAllIntakeDor), 0L).doubleValue() / 480),
				ObjectUtils.defaultIfNull(overAllIntakeDor.size(), 0L).doubleValue(), null, DAY, ISSUES,
				getIterationKpiModalValue(overAllIntakeDorModalValues, cycleTimeValidationDataList)));
		data.add(new IterationKpiData(DOR_TO_DOD,
				(double) Math.round(
						ObjectUtils.defaultIfNull(AggregationUtils.averageLong(overAllDorDod), 0L).doubleValue() / 480),
				ObjectUtils.defaultIfNull(overAllDorDod.size(), 0L).doubleValue(), null, DAY, ISSUES,
				getIterationKpiModalValue(overAllDorDodModalValues, cycleTimeValidationDataList)));
		data.add(new IterationKpiData(DOD_TO_LIVE, (double) Math
				.round(ObjectUtils.defaultIfNull(AggregationUtils.averageLong(overAllDodLive), 0L).doubleValue() / 480),
				ObjectUtils.defaultIfNull(overAllDodLive.size(), 0L).doubleValue(), null, DAY, ISSUES,
				getIterationKpiModalValue(overAllDodLiveModalValues, cycleTimeValidationDataList)));
		IterationKpiValue iterationKpiValue = new IterationKpiValue(CommonConstant.OVERALL, CommonConstant.OVERALL,
				data);
		dataList.add(iterationKpiValue);
		return dataList;
	}

	private void populateDataCountList(Map<String, Map<String, List<JiraIssueCustomHistory>>> rangeWiseTypeWiseIssue,
			Map<String, Long> issueWiseGroupValue, String range, String issueType,
			List<IterationKpiData> iterationKpiDataList, List<CycleTimeValidationData> cycleTimeValidationDataList,
			String kpiGroup) {
		List<JiraIssueCustomHistory> jiraIssueCustomHistories = rangeWiseTypeWiseIssue.get(range).get(issueType);
		if (CollectionUtils.isNotEmpty(jiraIssueCustomHistories)) {
			List<Long> valueList = jiraIssueCustomHistories.stream().map(JiraIssueCustomHistory::getStoryID)
					.filter(issueWiseGroupValue::containsKey).map(issueWiseGroupValue::get)
					.collect(Collectors.toList());

			iterationKpiDataList.add(new IterationKpiData(kpiGroup,
					(double) Math.round(
							ObjectUtils.defaultIfNull(AggregationUtils.averageLong(valueList), 0L).doubleValue() / 480),
					ObjectUtils.defaultIfNull(valueList.size(), 0L).doubleValue(), null, DAY, ISSUES,
					getIterationKpiModalValue(jiraIssueCustomHistories, cycleTimeValidationDataList)));

		} else {
			iterationKpiDataList.add(new IterationKpiData(kpiGroup, 0D, 0D, null, DAY, ISSUES, null));
		}
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
	public Integer calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

	@Override
	public String getQualifierType() {
		return KPICode.CYCLE_TIME.name();
	}

}