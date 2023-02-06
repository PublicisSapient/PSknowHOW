package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.*;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefectCountByRCAServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefectCountByRCAServiceImpl.class);

	public static final String UNCHECKED = "unchecked";
	private static final String OVERALL = "Overall";
	private static final String TOTAL_ISSUES = "totalIssues";
	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private SprintRepository sprintRepository;

	@Autowired
	private ConfigHelperService configHelperService;

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
			LOGGER.info("Closure Possible Today -> Requested sprint : {}", leafNode.getName());
			String basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId().toString();
			String sprintId = leafNode.getSprintFilter().getId();
			SprintDetails sprintDetails = sprintRepository.findBySprintID(sprintId);
			if (null != sprintDetails) {
				List<String> totalIssues = KpiDataHelper
						.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails, CommonConstant.TOTAL_ISSUES);
				if (CollectionUtils.isNotEmpty(totalIssues)) {
					List<JiraIssue> issueListCompleted = jiraIssueRepository
							.findByNumberInAndBasicProjectConfigId(totalIssues, basicProjectConfigId);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getTotalIssues(), issueListCompleted);
					resultListMap.put(TOTAL_ISSUES, new ArrayList<>(filtersIssuesList));
				}
			}
		}
		return resultListMap;
	}

	@Override
	public String getQualifierType() {
		return KPICode.DEFECT_COUNT_BY_RCA_PIECHART.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
								 TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		{
			DataCount trendValueList =new DataCount();
			Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
			treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
				if (Filters.getFilter(k) == Filters.SPRINT) {
					sprintWiseLeafNodeValue(mapTmp, v, trendValueList, kpiElement, kpiRequest);
				}
			});
			LOGGER.info("DefectCountByRCAServiceImpl -> getKpiData ->  : {}", kpiElement);

			return kpiElement;
		}
	}

	/**
	 * This method will set trendValueList information to the RCA KPI. It consists of logic to show data for P1, P2, P3, P4
	 * and "Overall" Priorities as per the accepted JSON structure.
	 * @param mapTmp
	 * @param sprintLeafNodeList
	 * @param trendValue
	 * @param kpiElement
	 * @param kpiRequest
	 */
	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList, DataCount trendValue,
										 KpiElement kpiElement, KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();
		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));
		List<Node> latestSprintNode = new ArrayList<>();
		Node latestSprint = sprintLeafNodeList.get(0);
		Optional.ofNullable(latestSprint).ifPresent(latestSprintNode::add);
		if(latestSprint !=null) {
			Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(latestSprint.getProjectFilter().getBasicProjectConfigId());
			if (fieldMapping != null) {
				List<JiraIssue> allCompletedIssuesExcludeStory = filterCompletedIssues(resultMap, fieldMapping);
				Map<String, Map<String, List<JiraIssue>>> priorityWiseRCAList = getPriorityWiseRCAList(allCompletedIssuesExcludeStory);
				List<Integer> overAllRCAIssueCount = Arrays.asList(0);
				String trendLineName = latestSprint.getProjectFilter().getName();
				LOGGER.info("DefectCountByRCAServiceImpl -> priorityWiseRCAList ->  : {}", priorityWiseRCAList);
				// filterDataList will consist of DataCountGroup which will be set for all priorities
				List<DataCountGroup> filterDataList = new ArrayList<>();
				List<DataCount> dataCountListForAllPriorities = new ArrayList<>();
				Map<String, Integer> overallRCACountMap = new HashMap<>();
				int overallRCACount = 0;
				for (Map.Entry<String, Map<String, List<JiraIssue>>> entry : priorityWiseRCAList.entrySet()) {
					String priority = entry.getKey();
					Map<String, List<JiraIssue>> rcaData = entry.getValue();

					DataCount priorityData = new DataCount();
					priorityData.setData(priority);
					priorityData.setValue(new ArrayList<>());

					int priorityRCACount = 0;
					Map<String, Integer> rcaCountMap = new HashMap<>();
					for (Map.Entry<String, List<JiraIssue>> rcaEntry : rcaData.entrySet()) {
						String rcaName = rcaEntry.getKey();
						List<JiraIssue> issues = rcaEntry.getValue();

						priorityRCACount += issues.size();
						rcaCountMap.put(rcaName, issues.size());
						overallRCACount += issues.size();
						if (overallRCACountMap.containsKey(rcaName)) {
							overallRCACountMap.put(rcaName, overallRCACountMap.get(rcaName) + issues.size());
						} else {
							overallRCACountMap.put(rcaName, issues.size());
						}
					}

					DataCount priorityRCAData = new DataCount();
					priorityRCAData.setData(String.valueOf(priorityRCACount));
					priorityRCAData.setValue(rcaCountMap);
					priorityRCAData.setSSprintID(latestSprint.getSprintFilter().getId());
					priorityRCAData.setSSprintName(latestSprint.getSprintFilter().getName());
					priorityRCAData.setKpiGroup("Priority");
					priorityRCAData.setSProjectName(latestSprint.getProjectFilter().getName());
					// dataCountList will store data for P1,P2,P3 and P4 priorities pertaining to child level structure
					List<DataCount> dataCountList = (List<DataCount>) priorityData.getValue();

					// add dataCount for middle level structure to store P1,P2,P3 and P4 Priorities, set dataCountList as value for child level structure
					List<DataCount> middleTrendValueListForPriorities = new ArrayList<>();
					DataCount middleOverallData = new DataCount();
					middleOverallData.setData(latestSprint.getProjectFilter().getName());
					middleOverallData.setValue(dataCountList);
					middleTrendValueListForPriorities.add(middleOverallData);

					DataCountGroup filterData = new DataCountGroup(priority, middleTrendValueListForPriorities);
					filterDataList.add(filterData);
					dataCountList.add(priorityRCAData);
					priorityData.setValue(dataCountList);
					dataCountListForAllPriorities.add(priorityRCAData);
				}

				// logic to create "Overall" Priority which will contain aggregate of all the priorities such as P1, P2, P3 and P4
				List<DataCount> overallRCADataList = new ArrayList<>();
				for (Map.Entry<String, Integer> entry : overallRCACountMap.entrySet()) {
					DataCount overallRCAData = new DataCount();
					overallRCAData.setData(entry.getKey());
					overallRCAData.setValue(entry.getValue());
					overallRCADataList.add(overallRCAData);
				}
				Map<String, Integer> overallRCACountMapAggregate = new HashMap<>();
				for (DataCount dataCount : dataCountListForAllPriorities) {
					Map<String, Integer> rcaCountMap = (Map<String, Integer>) dataCount.getValue();
					for (Map.Entry<String, Integer> rcaCount : rcaCountMap.entrySet()) {
						String rcaName = rcaCount.getKey();
						int rcaCountValue = rcaCount.getValue();
						if (overallRCACountMapAggregate.containsKey(rcaName)) {
							int currentRcaCount = overallRCACountMapAggregate.get(rcaName);
							overallRCACountMapAggregate.put(rcaName, currentRcaCount + rcaCountValue);
						} else {
							overallRCACountMapAggregate.put(rcaName, rcaCountValue);
						}
					}
				}

				// trendValueListOverAll will consist of data only pertaining to "Overall" Priority Filter
				List<DataCount> trendValueListOverAll = new ArrayList<>();
				DataCount overallData = new DataCount();
				int sumOfDefectsCount = overallRCACountMapAggregate.values().stream().mapToInt(Integer::intValue).sum();
				overallData.setData(String.valueOf(sumOfDefectsCount));
				overallData.setValue(overallRCACountMapAggregate);
				overallData.setSSprintID(latestSprint.getSprintFilter().getId());
				overallData.setSSprintName(latestSprint.getSprintFilter().getName());
				overallData.setKpiGroup(OVERALL);
				overallData.setSProjectName(latestSprint.getProjectFilter().getName());
				trendValueListOverAll.add(overallData);

				// add one more data count group and data count for middle level structure to store "Overall" Priority
				List<DataCount> middleTrendValueListOverAll = new ArrayList<>();
				DataCount middleOverallData = new DataCount();
				middleOverallData.setData(latestSprint.getProjectFilter().getName());
				middleOverallData.setValue(trendValueListOverAll);
				middleTrendValueListOverAll.add(middleOverallData);

				// "Overall" dataCountGroup added to filterDataList and added in the final filterDataList
				DataCountGroup filterDataOverall = new DataCountGroup(OVERALL, middleTrendValueListOverAll);
				filterDataList.add(filterDataOverall);
				kpiElement.setSprint(latestSprint.getName());
				kpiElement.setModalHeads(KPIExcelColumn.DEFECT_COUNT_BY_RCA_PIECHART.getColumns());
				// filterDataList will consist of dataCountGroup for all the available priorities such as P1, P2, P3, P4, Overall etc.
				kpiElement.setTrendValueList(filterDataList);
				LOGGER.info("DefectCountByRCAServiceImpl -> request id : {} total jira Issues : {}", requestTrackerId, overAllRCAIssueCount.get(0));
			}
		}
	}

	private DataCount getDataCountObject(Node node, String trendLineName, Map<String, Integer> overAllHoverValueMap,
										 String key, Long value) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(value));
		dataCount.setSProjectName(trendLineName);
		dataCount.setSSprintID(node.getSprintFilter().getId());
		dataCount.setSSprintName(node.getSprintFilter().getName());
		dataCount.setValue(value);
		dataCount.setKpiGroup(key);
		Map<String, Integer> hoverValueMap = new HashMap<>();
		if (key.equalsIgnoreCase(CommonConstant.OVERALL)) {
			dataCount.setHoverValue(overAllHoverValueMap);
		} else {
			hoverValueMap.put(key, value.intValue());
			dataCount.setHoverValue(hoverValueMap);
		}
		return dataCount;
	}
	private Map<String, IterationKpiData> groupIterationKpiData(List<IterationKpiData> overAllRCAData) {
		Map<String, IterationKpiData> groupedDataMap = new HashMap<>();
		for (IterationKpiData item : overAllRCAData) {
			IterationKpiData existingData = groupedDataMap.get(item.getLabel());
			if (existingData == null) {
				groupedDataMap.put(item.getLabel(), item);
			} else {
				existingData.setValue(existingData.getValue() + item.getValue());
				existingData.getModalValues().addAll(item.getModalValues());
			}
		}
		LOGGER.info("DefectCountByRCAServiceImpl -> groupedIterationKpiData ->  : {}", groupedDataMap);
		return groupedDataMap;
	}

	private List<JiraIssue> filterCompletedIssues(Map<String, Object> resultMap, FieldMapping fieldMapping) {
		List<String> defectStatuses = fieldMapping.getJiradefecttype();
		if (CollectionUtils.isNotEmpty((List<JiraIssue>) resultMap.get(TOTAL_ISSUES))) {
			return ((List<JiraIssue>) resultMap.get(TOTAL_ISSUES)).stream()
					.filter(issue -> defectStatuses.contains(issue.getTypeName())).collect(Collectors.toList());
		}
		return new ArrayList<>();
	}

	private Map<String, Map<String, List<JiraIssue>>> getPriorityWiseRCAList(List<JiraIssue> allCompletedIssuesExcludeStory) {
		return allCompletedIssuesExcludeStory.stream()
				.collect(Collectors.groupingBy(JiraIssue::getPriority,
						Collectors.groupingBy(jiraIssue -> jiraIssue.getRootCauseList().get(0))));
	}
}