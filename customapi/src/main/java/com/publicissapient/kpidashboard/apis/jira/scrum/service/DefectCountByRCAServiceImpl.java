package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.util.Objects;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.CommonServiceImpl;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
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
	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private SprintRepository sprintRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private CommonServiceImpl commonService;

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
			LOGGER.info("Defect count by RCA -> Requested sprint : {}", leafNode.getName());
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
					resultListMap.put(CommonConstant.TOTAL_ISSUES, new ArrayList<>(filtersIssuesList));
				}
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
		LOGGER.info("DefectCountByRCAServiceImpl -> getKpiData ->  : {}", kpiElement);
		return kpiElement;
	}

	/**
	 * This method will set trendValueList information to the RCA KPI. It consists of logic to show data for P1, P2, P3, P4
	 * and "Overall" Priorities as per the accepted JSON structure.
	 * @param sprintLeafNodeList
	 * @param kpiElement
	 * @param kpiRequest
	 */
	private void sprintWiseLeafNodeValue(List<Node> sprintLeafNodeList,
										 KpiElement kpiElement, KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();
		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));
		List<KPIExcelData> excelData = new ArrayList<>();
		List<Node> latestSprintNode = new ArrayList<>();
		Node latestSprint = sprintLeafNodeList.get(0);
		Optional.ofNullable(latestSprint).ifPresent(latestSprintNode::add);
		if (latestSprint != null) {
			Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(latestSprint.getProjectFilter()
					.getBasicProjectConfigId());
			if (fieldMapping != null) {
				List<JiraIssue> allCompletedIssuesExcludeStory = KpiDataHelper.filterCompletedIssues(resultMap, fieldMapping);
				Map<String, Map<String, List<JiraIssue>>> priorityWiseRCAList = KpiDataHelper.
						getPriorityWiseRCAList(allCompletedIssuesExcludeStory);
				List<Integer> overAllRCAIssueCount = Arrays.asList(0);
				LOGGER.info("DefectCountByRCAServiceImpl -> priorityWiseRCAList ->  : {}", priorityWiseRCAList);
				// filterDataList will consist of DataCountGroup which will be set for all priorities
				List<DataCountGroup> filterDataList = new ArrayList<>();
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
					// dataCountList will store data for P1,P2,P3 and P4 priorities pertaining to child level structure
					List<DataCount> dataCountList = (List<DataCount>) priorityData.getValue();

					// add dataCount for middle level structure to store P1,P2,P3 and P4 Priorities, set dataCountList
					// as value for child level structure
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
				Map<String, Integer> overallRCACountMapAggregate = new HashMap<>();
				rcaCountMapMethod(dataCountListForAllPriorities, overallRCACountMapAggregate);
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
				populateExcelDataObject(requestTrackerId, excelData, allCompletedIssuesExcludeStory,
						latestSprint.getSprintFilter().getName(), fieldMapping);

				// "Overall" dataCountGroup added to filterDataList and added in the final filterDataList
				DataCountGroup filterDataOverall = new DataCountGroup(OVERALL, middleTrendValueListOverAll);
				filterDataList.add(filterDataOverall);
				kpiElement.setSprint(latestSprint.getName());
				kpiElement.setModalHeads(KPIExcelColumn.DEFECT_COUNT_BY_RCA_PIE_CHART.getColumns());
				kpiElement.setExcelColumns(KPIExcelColumn.DEFECT_COUNT_BY_RCA_PIE_CHART.getColumns());
				kpiElement.setExcelData(excelData);

				// filterDataList will consist of dataCountGroup for all the available priorities such as P1, P2, P3, P4, Overall etc.
				getTrendValueList(kpiElement, filterDataList, sumOfDefectsCount);
				LOGGER.info("DefectCountByRCAServiceImpl -> request id : {} total jira Issues : {}", requestTrackerId,
						overAllRCAIssueCount.get(0));
			}
		}
	}

	private static void getTrendValueList(KpiElement kpiElement, List<DataCountGroup> filterDataList, int sumOfDefectsCount) {
		if (sumOfDefectsCount < 1) {
			kpiElement.setTrendValueList(null);
		} else {
			kpiElement.setTrendValueList(filterDataList);
		}
	}

	private static void rcaCountMapMethod(List<DataCount> dataCountListForAllPriorities, Map<String, Integer> overallRCACountMapAggregate) {
		for (DataCount dataCount : dataCountListForAllPriorities) {
			Map<String, Integer> rcaCountMap = (Map<String, Integer>) dataCount.getValue();
			rcaCountMap.forEach((rcaName, rcaCountValue) ->
					overallRCACountMapAggregate.merge(rcaName, rcaCountValue, Integer::sum));
		}
	}

	private static int getPriorityRCACount(Map<String, Integer> overallRCACountMap, Map<String, List<JiraIssue>> rcaData, int priorityRCACount, Map<String, Integer> rcaCountMap) {
		for (Map.Entry<String, List<JiraIssue>> rcaEntry : rcaData.entrySet()) {
			String rcaName = rcaEntry.getKey();
			List<JiraIssue> issues = rcaEntry.getValue();

			priorityRCACount += issues.size();
			rcaCountMap.put(rcaName, issues.size());
			overallRCACountMap.merge(rcaName, issues.size(), Integer::sum);
		}
		return priorityRCACount;
	}

	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
										 List<JiraIssue> sprintWiseDefectDataList, String name, FieldMapping fieldMapping) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& !Objects.isNull(sprintWiseDefectDataList) && !sprintWiseDefectDataList.isEmpty()) {
			KPIExcelUtility.populateDefectRCARelatedExcelData(name, sprintWiseDefectDataList, excelData, fieldMapping);
		}

	}
}