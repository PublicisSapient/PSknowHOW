package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.CommonServiceImpl;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.IterationKpiHelper;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DefectCountByRCAServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	public static final String UNCHECKED = "unchecked";
	private static final String TOTAL_ISSUES = "Total Issues";
	private static final String SPRINT_DETAILS = "SprintDetails";
	private static final String CREATED_DURING_ITERATION = "Created during Iteration";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private CommonServiceImpl commonService;

	private static void overallRCACountMap(List<DataCount> dataCountListForAllPriorities,
			Map<String, Integer> overallRCACountMapAggregate) {
		for (DataCount dataCount : dataCountListForAllPriorities) {
			Map<String, Integer> rcaCountMap = (Map<String, Integer>) dataCount.getValue();
			rcaCountMap.forEach((rcaName, rcaCountValue) -> overallRCACountMapAggregate.merge(rcaName, rcaCountValue,
					Integer::sum));
		}
	}

	private static int getPriorityRCACount(Map<String, Integer> overallRCACountMap,
			Map<String, List<JiraIssue>> rcaData, int priorityRCACount, Map<String, Integer> rcaCountMap) {
		for (Map.Entry<String, List<JiraIssue>> rcaEntry : rcaData.entrySet()) {
			String rcaName = rcaEntry.getKey();
			List<JiraIssue> issues = rcaEntry.getValue();

			priorityRCACount += issues.size();
			rcaCountMap.put(rcaName, issues.size());
			overallRCACountMap.merge(rcaName, issues.size(), Integer::sum);
		}
		return priorityRCACount;
	}

	@Override
	public Integer calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);
		if (null != leafNode) {
			log.info("Defect count by RCA -> Requested sprint : {}", leafNode.getName());
			String basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId().toString();
			String sprintId = leafNode.getSprintFilter().getId();
			List<String> defectType = new ArrayList<>();
			SprintDetails dbSprintDetail = getSprintDetailsFromBaseClass();
			SprintDetails sprintDetails;
			if (null != dbSprintDetail) {
				FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
						.get(leafNode.getProjectFilter().getBasicProjectConfigId());
				// to modify sprintdetails on the basis of configuration for the project
				List<JiraIssueCustomHistory> totalHistoryList = getJiraIssuesCustomHistoryFromBaseClass();
				List<JiraIssue> totalJiraIssueList = getJiraIssuesFromBaseClass();
				Set<String> issueList = totalJiraIssueList.stream().map(JiraIssue::getNumber)
						.collect(Collectors.toSet());

				sprintDetails = IterationKpiHelper.transformIterSprintdetail(totalHistoryList, issueList,
						dbSprintDetail, new ArrayList<>(), fieldMapping.getJiraIterationCompletionStatusKPI132(),
						leafNode.getProjectFilter().getBasicProjectConfigId());

				List<String> totalIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.TOTAL_ISSUES);
				List<String> defectTypes = Optional.ofNullable(fieldMapping).map(FieldMapping::getJiradefecttype)
						.orElse(Collections.emptyList());
				Set<String> totalSprintReportDefects = new HashSet<>();
				Set<String> totalSprintReportStories = new HashSet<>();
				sprintDetails.getTotalIssues().stream().forEach(sprintIssue -> {
					if (defectTypes.contains(sprintIssue.getTypeName())) {
						totalSprintReportDefects.add(sprintIssue.getNumber());
					} else {
						totalSprintReportStories.add(sprintIssue.getNumber());
					}
				});

				Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
				Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
				Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
				defectType.add(NormalizedJira.DEFECT_TYPE.getValue());
				mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
						CommonUtils.convertToPatternList(defectType));
				uniqueProjectMap.put(basicProjectConfigId, mapOfProjectFilters);
				mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
						Collections.singletonList(basicProjectConfigId));

				if (CollectionUtils.isNotEmpty(totalIssues)) {
					List<JiraIssue> issueListCompleted = IterationKpiHelper.getFilteredJiraIssue(totalIssues,
							totalJiraIssueList);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getTotalIssues(), issueListCompleted);

					// fetched all defects which is linked to current sprint report stories
					List<JiraIssue> linkedDefects = jiraIssueRepository.findLinkedDefects(mapOfFilters,
							totalSprintReportStories, uniqueProjectMap);

					// filter defects which is issue type not coming in sprint report
					List<JiraIssue> subTaskDefects = linkedDefects.stream()
							.filter(jiraIssue -> !totalSprintReportDefects.contains(jiraIssue.getNumber()))
							.collect(Collectors.toList());

					List<JiraIssue> totalSubTaskTaggedToSprint = subTaskDefects.stream()
							.filter(jiraIssue -> CollectionUtils.isNotEmpty(jiraIssue.getSprintIdList())
									&& jiraIssue.getSprintIdList().contains(sprintId.split("_")[0]))
							.collect(Collectors.toList());

					List<JiraIssue> allIssues = new ArrayList<>();
					allIssues.addAll(filtersIssuesList);
					allIssues.addAll(totalSubTaskTaggedToSprint);

					resultListMap.put(CommonConstant.TOTAL_ISSUES, new ArrayList<>(allIssues));
				}
				resultListMap.put(SPRINT_DETAILS, sprintDetails);
			}
		}
		return resultListMap;
	}

	@Override
	public String getQualifierType() {
		return KPICode.DEFECT_COUNT_BY_RCA_PIE_CHART.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.SPRINT) {
				sprintWiseLeafNodeValue(v, kpiElement, kpiRequest);
			}
		});
		log.info("DefectCountByRCAServiceImpl -> getKpiData ->  : {}", kpiElement);
		return kpiElement;
	}

	/**
	 * This method will set trendValueList information to the RCA KPI. It consists
	 * of logic to show data for P1, P2, P3, P4 and "Overall" Priorities as per the
	 * accepted JSON structure.
	 *
	 * @param sprintLeafNodeList
	 * @param kpiElement
	 * @param kpiRequest
	 */
	private void sprintWiseLeafNodeValue(List<Node> sprintLeafNodeList, KpiElement kpiElement, KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();
		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));
		List<KPIExcelData> excelData = new ArrayList<>();
		List<Node> latestSprintNode = new ArrayList<>();
		Node latestSprint = sprintLeafNodeList.get(0);
		Optional.ofNullable(latestSprint).ifPresent(latestSprintNode::add);
		if (latestSprint != null) {
			Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(latestSprint.getProjectFilter().getBasicProjectConfigId());
			if (fieldMapping != null) {
				SprintDetails sprintDetails = (SprintDetails) resultMap.get(SPRINT_DETAILS);
				List<JiraIssue> allCompletedDefects = filterDefects(resultMap, fieldMapping);
				List<JiraIssue> createDuringIteration = allCompletedDefects.stream()
						.filter(jiraIssue -> DateUtil.isWithinDateRange(
								LocalDate.parse(jiraIssue.getCreatedDate().split("\\.")[0], DATE_TIME_FORMATTER),
								LocalDate.parse(sprintDetails.getStartDate().split("\\.")[0], DATE_TIME_FORMATTER),
								LocalDate.parse(sprintDetails.getEndDate().split("\\.")[0], DATE_TIME_FORMATTER)))
						.collect(Collectors.toList());
				Map<String, Map<String, List<JiraIssue>>> priorityWiseRCAList = getPriorityWiseRCAList(
						allCompletedDefects, createDuringIteration);
				List<Integer> overAllRCAIssueCount = Arrays.asList(0);
				log.info("DefectCountByRCAServiceImpl -> priorityWiseRCAList ->  : {}", priorityWiseRCAList);
				// filterDataList will consist of IterationKpiValue which will be set for all
				// priorities
				List<IterationKpiValue> filterDataList = new ArrayList<>();
				List<IterationKpiValue> sortedFilterDataList = new ArrayList<>();
				List<DataCount> dataCountListForAllPriorities = new ArrayList<>();
				Map<String, Integer> overallRCACountMap = new HashMap<>();
				for (Map.Entry<String, Map<String, List<JiraIssue>>> entry : priorityWiseRCAList.entrySet()) {
					String priority = entry.getKey();
					Map<String, List<JiraIssue>> rcaData = entry.getValue();

					DataCount priorityData = new DataCount();
					priorityData.setData(priority);
					priorityData.setValue(new ArrayList<>());

					int priorityRCACount = 0;
					Map<String, Integer> rcaCountMap = new HashMap<>();
					// update and set the overall data
					priorityRCACount = getPriorityRCACount(overallRCACountMap, rcaData, priorityRCACount, rcaCountMap);
					DataCount priorityRCAData = new DataCount();
					priorityRCAData.setData(String.valueOf(priorityRCACount));
					priorityRCAData.setValue(rcaCountMap);
					priorityRCAData.setSSprintID(latestSprint.getSprintFilter().getId());
					priorityRCAData.setSSprintName(latestSprint.getSprintFilter().getName());
					priorityRCAData.setKpiGroup("Priority");
					priorityRCAData.setSProjectName(latestSprint.getProjectFilter().getName());
					// dataCountList will store data for P1,P2,P3 and P4 priorities pertaining to
					// child level structure
					List<DataCount> dataCountList = (List<DataCount>) priorityData.getValue();

					// add dataCount for middle level structure to store P1,P2,P3 and P4 Priorities,
					// set dataCountList
					// as value for child level structure
					List<DataCount> middleTrendValueListForPriorities = new ArrayList<>();
					DataCount middleOverallData = new DataCount();
					middleOverallData.setData(latestSprint.getProjectFilter().getName());
					middleOverallData.setValue(dataCountList);
					middleTrendValueListForPriorities.add(middleOverallData);

					IterationKpiValue filterData = new IterationKpiValue(priority, middleTrendValueListForPriorities);

					filterDataList.add(filterData);
					dataCountList.add(priorityRCAData);
					priorityData.setValue(dataCountList);
					dataCountListForAllPriorities.add(priorityRCAData);
				}
				// logic to create "Overall" Priority which will contain aggregate of all the
				// priorities such as P1, P2, P3 and P4
				Map<String, Integer> overallRCACountMapAggregate = new HashMap<>();
				overallRCACountMap(dataCountListForAllPriorities, overallRCACountMapAggregate);

				if (MapUtils.isNotEmpty(overallRCACountMapAggregate)) {
					populateExcelDataObject(requestTrackerId, excelData, allCompletedDefects,
							latestSprint.getSprintFilter().getName(), fieldMapping, createDuringIteration);

					kpiElement.setSprint(latestSprint.getName());
					kpiElement.setModalHeads(KPIExcelColumn.DEFECT_COUNT_BY_RCA_PIE_CHART.getColumns());
					kpiElement.setExcelColumns(KPIExcelColumn.DEFECT_COUNT_BY_RCA_PIE_CHART.getColumns());
					kpiElement.setExcelData(excelData);
					sortedFilterDataList.add(filterDataList.stream()
							.filter(iterationKpiValue -> iterationKpiValue.getFilter1()
									.equalsIgnoreCase(CREATED_DURING_ITERATION))
							.findFirst().orElse(new IterationKpiValue()));
					filterDataList.removeIf(iterationKpiValue -> iterationKpiValue.getFilter1()
							.equalsIgnoreCase(CREATED_DURING_ITERATION));
					sortListByKey(filterDataList);
					sortedFilterDataList.addAll(filterDataList);
					// filterDataList will consist of iterationKpiValue for all the available
					// priorities such as P1, P2, P3, P4, Overall etc.
					kpiElement.setTrendValueList(sortedFilterDataList);
					log.info("DefectCountByRCAServiceImpl -> request id : {} total jira Issues : {}", requestTrackerId,
							overAllRCAIssueCount.get(0));
				}
			}
		}
	}

	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			List<JiraIssue> sprintWiseDefectDataList, String name, FieldMapping fieldMapping,
			List<JiraIssue> createdDuringIteration) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& !Objects.isNull(sprintWiseDefectDataList) && !sprintWiseDefectDataList.isEmpty()) {
			KPIExcelUtility.populateDefectRCAandStatusRelatedExcelData(name, sprintWiseDefectDataList,
					createdDuringIteration, excelData, fieldMapping);
		}

	}

	private List<JiraIssue> filterDefects(Map<String, Object> resultMap, FieldMapping fieldMapping) {
		List<String> defectStatuses = fieldMapping.getJiradefecttype();
		// subtask defects consider as BUG type in jira_issue
		defectStatuses.add(NormalizedJira.DEFECT_TYPE.getValue());
		if (CollectionUtils.isNotEmpty((List<JiraIssue>) resultMap.get(CommonConstant.TOTAL_ISSUES))) {
			return ((List<JiraIssue>) resultMap.get(CommonConstant.TOTAL_ISSUES)).stream()
					.filter(issue -> defectStatuses.contains(issue.getTypeName())).collect(Collectors.toList());
		}
		return new ArrayList<>();
	}

	private Map<String, Map<String, List<JiraIssue>>> getPriorityWiseRCAList(
			List<JiraIssue> allCompletedIssuesExcludeStory, List<JiraIssue> createdDuringIteration) {

		Map<String, Map<String, List<JiraIssue>>> scopeWiseDefectsMap = new HashMap<>();
		scopeWiseDefectsMap.put(TOTAL_ISSUES, allCompletedIssuesExcludeStory.stream()
				.collect(Collectors.groupingBy(jiraIssue -> jiraIssue.getRootCauseList().get(0))));
		scopeWiseDefectsMap.put(CREATED_DURING_ITERATION, createdDuringIteration.stream()
				.collect(Collectors.groupingBy(jiraIssue -> jiraIssue.getRootCauseList().get(0))));
		return scopeWiseDefectsMap;

	}

	private void sortListByKey(List<IterationKpiValue> list) {
		list.sort(Comparator.comparing(IterationKpiValue::getFilter1));
	}
}