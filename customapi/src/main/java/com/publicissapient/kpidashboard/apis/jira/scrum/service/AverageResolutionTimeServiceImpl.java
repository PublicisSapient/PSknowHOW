package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ResolutionTimeValidation;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AverageResolutionTimeServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {
	private static final String STORY_LIST = "stories";
	private static final String JIRA_ISSUE_LIST = "jiraIssuesBySprintAndType";
	private static final String DEV = "DeveloperKpi";
	private static final String STORY_HISTORY_DATA = "storyHistoryData";
	private static final String PROJECT_FIELDMAPPING = "projectFieldMapping";
	private static final String AGGREGATED = "Overall";
	@Autowired
	private JiraIssueRepository jiraIssueRepository;
	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private FilterHelperService flterHelperService;

	@Override
	public Double calculateKPIMetrics(Map<String, Object> t) {
		return null;
	}

	@Override
	public String getQualifierType() {
		return KPICode.AVERAGE_RESOLUTION_TIME.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		List<DataCount> trendValueList = new ArrayList<>();
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		Set<String> absentIssueTypes = new HashSet<>();

		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {

			Filters filters = Filters.getFilter(k);
			if (Filters.SPRINT == filters) {
				sprintWiseLeafNodeValue(mapTmp, v, trendValueList, kpiElement, kpiRequest, absentIssueTypes);
			}

		});

		log.debug("[AVERAGE RESOLUTION TIME -LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.AVERAGE_RESOLUTION_TIME);

		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.AVERAGE_RESOLUTION_TIME);
		Map<String, Map<String, List<DataCount>>> issueTypeProjectWiseDc = new LinkedHashMap<>();
		trendValuesMap.forEach((issueType, dataCounts) -> {
			Map<String, List<DataCount>> projectWiseDc = dataCounts.stream()
					.collect(Collectors.groupingBy(DataCount::getData));
			issueTypeProjectWiseDc.put(issueType, projectWiseDc);
		});

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		issueTypeProjectWiseDc.forEach((issueType, projectWiseDc) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			List<DataCount> dataList = new ArrayList<>();
			projectWiseDc.entrySet().stream().forEach(trend -> dataList.addAll(trend.getValue()));
			dataCountGroup.setFilter(issueType);
			dataCountGroup.setValue(dataList);
			dataCountGroups.add(dataCountGroup);
		});
		kpiElement.setTrendValueList(dataCountGroups);

		return kpiElement;
	}

	/**
	 * @param mapTmp
	 * @param sprintLeafNodeList
	 * @param trendValueList
	 * @param kpiElement
	 * @param kpiRequest
	 */

	@SuppressWarnings("unchecked")
	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest,
			Set<String> absentIssueTypesRoot) {

		String requestTrackerId = getRequestTrackerId();
		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));

		String startDate = sprintLeafNodeList.get(0).getSprintFilter().getStartDate();
		String endDate = sprintLeafNodeList.get(sprintLeafNodeList.size() - 1).getSprintFilter().getEndDate();

		Map<String, Object> resultMap = fetchKPIDataFromDb(sprintLeafNodeList, startDate, endDate, kpiRequest);

		// Project wise Field Mapping Map
		Map<String, FieldMapping> fieldMappingMap = (Map<String, FieldMapping>) resultMap.get(PROJECT_FIELDMAPPING);
		// History Data
		List<JiraIssueCustomHistory> storiesHistory = (List<JiraIssueCustomHistory>) resultMap.get(STORY_HISTORY_DATA);
		// create map of issue_id and time difference between development status
		// and
		// done status
		Map<String, Double> resolutionTimeIssueIdWise = getResolutionTime(storiesHistory, fieldMappingMap);

		// Jira Issues
		List<JiraIssue> jiraIssues = (List<JiraIssue>) resultMap.get(JIRA_ISSUE_LIST);

		List<KPIExcelData> excelData = new ArrayList<>();
		// create sprint wise map of resolution time with issue type
		Map<String, List<ResolutionTimeValidation>> sprintWiseResolution = groupSprintWiseIssues(jiraIssues,
				resolutionTimeIssueIdWise);

		Map<String, Map<String, Double>> sprintIssueTypeWiseTime = new HashMap<>();
		sprintWiseResolution.forEach((sprint, issueWiseTimeList) -> {
			Map<String, Double> issueTypeAvgTime = new HashMap<>();
			Map<String, List<ResolutionTimeValidation>> issueWiseTime = issueWiseTimeList.stream()
					.collect(Collectors.groupingBy(ResolutionTimeValidation::getIssueType));
			List<Double> sprintTime = issueWiseTimeList.stream()
					.collect(Collectors.mapping(ResolutionTimeValidation::getResolutionTime, Collectors.toList()));
			issueWiseTime.forEach((issueType, timeList) -> {
				List<Double> sprintIssueTypeTime = timeList.stream()
						.collect(Collectors.mapping(ResolutionTimeValidation::getResolutionTime, Collectors.toList()));
				if (CollectionUtils.isNotEmpty(sprintTime)) {
					Double avgTime = sprintIssueTypeTime.stream().mapToDouble(a -> a).average().orElse(0.0);
					issueTypeAvgTime.put(issueType, avgTime);
				}
			});
			Double aggregateAvgTime = sprintTime.stream().mapToDouble(a -> a).average().orElse(0.0);
			issueTypeAvgTime.put(AGGREGATED, aggregateAvgTime);
			sprintIssueTypeWiseTime.put(sprint, issueTypeAvgTime);
		});

		sprintLeafNodeList.forEach(node -> {
			String trendLineName = node.getProjectFilter().getName();
			String basicProjectConfigId = node.getProjectFilter().getBasicProjectConfigId().toString();
			Set<String> issueTypes = new HashSet<>();
			FieldMapping fieldMapping = fieldMappingMap.get(basicProjectConfigId);
			if (null != fieldMapping && null != fieldMapping.getJiraIssueTypeNamesAVR()) {
				issueTypes = Arrays.stream(fieldMapping.getJiraIssueTypeNamesAVR()).collect(Collectors.toSet());
				if (CollectionUtils.containsAny(issueTypes, fieldMapping.getJiradefecttype())) {
					issueTypes.removeIf(x -> fieldMapping.getJiradefecttype().contains(x));
					issueTypes.add(NormalizedJira.DEFECT_TYPE.getValue());
				}
				issueTypes.add(AGGREGATED);

			}
			String currentSprintComponentId = node.getSprintFilter().getId();
			Map<String, Double> issueTypeAvgTime = new HashMap<>();
			if (sprintIssueTypeWiseTime.containsKey(currentSprintComponentId)) {
				issueTypeAvgTime = sprintIssueTypeWiseTime.get(currentSprintComponentId);
				List<ResolutionTimeValidation> resolutionTimeValidations = sprintWiseResolution
						.get(currentSprintComponentId);
				if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
					KPIExcelUtility.populateAverageResolutionTime(node.getSprintFilter().getName(),
							resolutionTimeValidations, excelData);
				}
			}
			Set<String> issueTypesFound = issueTypeAvgTime.keySet();
			issueTypes.removeAll(issueTypesFound);
			absentIssueTypesRoot.addAll(issueTypes);
			Map<String, Double> absentIssueTypes = issueTypes.stream().distinct()
					.collect(Collectors.toMap(s -> s, s -> 0.0));
			// add issue types
			Map<String, Double> allIssueTypes = new HashMap<>();
			allIssueTypes.putAll(issueTypeAvgTime);
			allIssueTypes.putAll(absentIssueTypes);

			Map<String, List<DataCount>> dataCountMap = new HashMap<>();
			allIssueTypes.forEach((issueType, avgTime) -> {
				DataCount dataCount = new DataCount();
				dataCount.setData(String.valueOf(Math.round(avgTime)));
				dataCount.setSProjectName(trendLineName);
				dataCount.setSSprintID(node.getSprintFilter().getId());
				dataCount.setSSprintName(node.getSprintFilter().getName());
				dataCount.setValue(round(avgTime));
				dataCount.setKpiGroup(issueType);
				dataCount.setHoverValue(new HashMap<>());
				trendValueList.add(dataCount);
				dataCountMap.put(issueType, new ArrayList<>(Arrays.asList(dataCount)));
			});
			mapTmp.get(node.getId()).setValue(dataCountMap);
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.AVERAGE_RESOLUTION_TIME.getColumns());
	}

	private Map<String, Double> getResolutionTime(List<JiraIssueCustomHistory> jiraIssueCustomHistories,
			Map<String, FieldMapping> fieldMappingMap) {
		Map<String, Double> sprintWiseResult = new HashMap<>();
		if (CollectionUtils.isNotEmpty(jiraIssueCustomHistories)) {
			for (JiraIssueCustomHistory jiraIssueCustomHistory : jiraIssueCustomHistories) {

				jiraIssueCustomHistory.getStatusUpdationLog()
						.sort(Comparator.comparing(JiraHistoryChangeLog::getUpdatedOn));
				FieldMapping fieldMapping = fieldMappingMap.get(jiraIssueCustomHistory.getBasicProjectConfigId());
				List<JiraHistoryChangeLog> statusUpdationLogs = jiraIssueCustomHistory.getStatusUpdationLog();
				sprintWiseResult.put(jiraIssueCustomHistory.getStoryID(),
						getStoryCompletionDays(fieldMapping, statusUpdationLogs));
			}

		}

		return sprintWiseResult;
	}

	/**
	 * this method get story completion days
	 *
	 * @param fieldMapping
	 *            fieldMapping
	 * @param statusUpdationLog
	 *            statusUpdationLog
	 * @return days
	 */
	private Double getStoryCompletionDays(FieldMapping fieldMapping, List<JiraHistoryChangeLog> statusUpdationLog) {
		Double storyCompletionDays = 0.0;
		long developmentTime = 0L;
		long lastClosedStatusTime = 0L;

		boolean devStatusFound = false;
		boolean closedStatusFound = false;
		List<String> storyDeliveredStatuses = (List<String>) CollectionUtils
				.emptyIfNull(fieldMapping.getJiraIssueDeliverdStatusAVR());

		List<String> storyDevelopmentStatuses = (List<String>) CollectionUtils
				.emptyIfNull(fieldMapping.getJiraStatusForDevelopmentAVR());
		for (int i = 0; i < statusUpdationLog.size(); i++) {
			if (storyDevelopmentStatuses.contains(statusUpdationLog.get(i).getChangedTo()) && developmentTime == 0L) {
				devStatusFound = true;
				developmentTime = statusUpdationLog.get(i).getUpdatedOn().toInstant(ZoneOffset.UTC).toEpochMilli();
			}
			if (storyDeliveredStatuses.contains(statusUpdationLog.get(i).getChangedTo())) {
				closedStatusFound = true;
				lastClosedStatusTime = statusUpdationLog.get(i).getUpdatedOn().toInstant(ZoneOffset.UTC).toEpochMilli();
			}

		}
		if (devStatusFound && closedStatusFound) {
			storyCompletionDays = (double) TimeUnit.DAYS.convert(lastClosedStatusTime - developmentTime,
					TimeUnit.MILLISECONDS);
			if (storyCompletionDays == 0.0) {
				storyCompletionDays = 1.0;
			}
		}
		return storyCompletionDays;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Object> resultListMap = new HashMap<>();

		List<String> sprintList = new ArrayList<>();
		List<String> basicProjectConfigIds = new ArrayList<>();

		Map<String, Pair<String, String>> sprintWithDateMap = new HashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		Map<String, Map<String, List<String>>> statusConfigsOfRejectedStoriesByProject = new HashMap<>();
		Map<String, FieldMapping> projectFieldMapping = new HashMap<>();

		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
			sprintList.add(leaf.getSprintFilter().getId());
			basicProjectConfigIds.add(basicProjectConfigId.toString());
			sprintList.add(leaf.getSprintFilter().getId());
			sprintWithDateMap.put(leaf.getSprintFilter().getId(),
					Pair.of(leaf.getSprintFilter().getStartDate(), leaf.getSprintFilter().getEndDate()));

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			if (null != fieldMapping) {
				projectFieldMapping.put(basicProjectConfigId.toString(), fieldMapping);
				KpiHelperService.getDroppedDefectsFilters(statusConfigsOfRejectedStoriesByProject, basicProjectConfigId,
						fieldMapping.getResolutionTypeForRejectionAVR(),fieldMapping.getJiraDefectRejectionStatusAVR());
				List<String> jiraIssueTypes = new ArrayList<>(Arrays.asList(fieldMapping.getJiraIssueTypeNamesAVR()));
				if (CollectionUtils.containsAny(jiraIssueTypes, fieldMapping.getJiradefecttype())) {
					jiraIssueTypes.add(NormalizedJira.DEFECT_TYPE.getValue());
				}
				mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
						CommonUtils.convertToPatternList(jiraIssueTypes));
				mapOfProjectFilters.put(JiraFeature.JIRA_ISSUE_STATUS.getFieldValueInFeature(),
						fieldMapping.getJiraIssueDeliverdStatusAVR());
				uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);
			}

		});
		/** additional filter **/
		KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.SCRUM, DEV, flterHelperService);

		mapOfFilters.put(JiraFeature.SPRINT_ID.getFieldValueInFeature(),
				sprintList.stream().distinct().collect(Collectors.toList()));
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		List<JiraIssue> jiraIssuesBySprintAndType = jiraIssueRepository.findIssuesBySprintAndType(mapOfFilters,
				uniqueProjectMap);

		// do not change the order of remove methods
		List<JiraIssue> defectListWoDrop = new ArrayList<>();
		KpiHelperService.getDefectsWithoutDrop(statusConfigsOfRejectedStoriesByProject, jiraIssuesBySprintAndType,
				defectListWoDrop);

		List<String> storyIds = getIssueIds(defectListWoDrop);

		List<JiraIssueCustomHistory> storiesHistory = jiraIssueCustomHistoryRepository
				.findByStoryIDInAndBasicProjectConfigIdIn(storyIds, basicProjectConfigIds);

		resultListMap.put(STORY_HISTORY_DATA, storiesHistory);
		resultListMap.put(STORY_LIST, storyIds);
		resultListMap.put(JIRA_ISSUE_LIST, defectListWoDrop);
		resultListMap.put(PROJECT_FIELDMAPPING, projectFieldMapping);
		return resultListMap;
	}

	@NotNull
	private List<String> getIssueIds(List<JiraIssue> issuesBySprintAndType) {
		List<String> storyIds = new ArrayList<>();
		CollectionUtils.emptyIfNull(issuesBySprintAndType).forEach(story -> storyIds.add(story.getNumber()));
		return storyIds;
	}

	private Map<String, List<ResolutionTimeValidation>> groupSprintWiseIssues(List<JiraIssue> jiraIssues,
			Map<String, Double> resolutionTimeIssueIdWise) {

		Map<String, List<ResolutionTimeValidation>> sprintSubCatIssueTypeStoryMap = new HashMap<>();

		Map<String, List<JiraIssue>> sprintAndFilterDataMap = jiraIssues.stream()
				.collect(Collectors.groupingBy(JiraIssue::getSprintID, Collectors.toList()));
		sprintAndFilterDataMap.forEach((sprint, sprintWiseIssue) -> {

			List<ResolutionTimeValidation> resolutionTimes = new ArrayList<>();
			sprintWiseIssue.forEach(issue -> {
				ResolutionTimeValidation resolutionTimeValidation = new ResolutionTimeValidation();
				Double time = resolutionTimeIssueIdWise.get(issue.getNumber());
				if (null != time) {
					resolutionTimeValidation.setIssueNumber(issue.getNumber());
					resolutionTimeValidation.setUrl(issue.getUrl());
					resolutionTimeValidation.setIssueDescription(issue.getName());
					resolutionTimeValidation.setIssueType(issue.getTypeName());
					resolutionTimeValidation.setResolutionTime(time);
					resolutionTimes.add(resolutionTimeValidation);
				}
			});

			sprintSubCatIssueTypeStoryMap.put(sprint, resolutionTimes);

		});

		return sprintSubCatIssueTypeStoryMap;
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiId) {
		return calculateKpiValueForDouble(valueList, kpiId);
	}
}
