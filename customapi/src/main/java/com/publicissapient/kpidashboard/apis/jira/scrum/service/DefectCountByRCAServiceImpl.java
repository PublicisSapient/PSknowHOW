package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
	private static final String SEARCH_BY_PRIORITY = "filter1";
	private static final String RCA_SIZE = "RCACount";
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
			DataCount trendValueList = new DataCount();
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

	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList, DataCount trendValue,
										 KpiElement kpiElement, KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();
		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));
		List<Node> latestSprintNode = new ArrayList<>();
		Node latestSprint = sprintLeafNodeList.get(0);
		Optional.ofNullable(latestSprint).ifPresent(latestSprintNode::add);
		if(latestSprint !=null){
		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(latestSprint.getProjectFilter().getBasicProjectConfigId());
		if (fieldMapping != null) {
			List<JiraIssue> allCompletedIssuesExcludeStory = filterCompletedIssues(resultMap, fieldMapping);
			Map<String, Map<String, List<JiraIssue>>> priorityWiseRCAList = getPriorityWiseRCAList(allCompletedIssuesExcludeStory);
			Set<String> issueTypes = new HashSet<>();
			List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
			List<Integer> overAllRCAIssueCount = Arrays.asList(0);
			List<IterationKpiModalValue> overAllmodalValues = new ArrayList<>();
			List<IterationKpiData> overAllRCAdata = new ArrayList<>();
			LOGGER.info("DefectCountByRCAServiceImpl -> priorityWiseRCAList ->  : {}", priorityWiseRCAList);
			priorityWiseRCAList.forEach((issueType, issues) -> {
				issueTypes.add(issueType);
				List<IterationKpiData> data = new ArrayList<>();
				issues.forEach((key, values) -> {
					List<IterationKpiModalValue> modalValues = new ArrayList<>();
					IterationKpiData issueCounts = new IterationKpiData(key, Double.valueOf(values.size()), null, key, "", modalValues);
					if (values instanceof ArrayList) {
						for (JiraIssue jiraIssue : values) {
							{
								overAllRCAIssueCount.set(0, overAllRCAIssueCount.get(0) + 1);
								populateIterationData(overAllmodalValues, modalValues, jiraIssue);
							}
						}
						data.add(issueCounts);
						overAllRCAdata.add(issueCounts);
					}
				});
				IterationKpiValue iterationKpiValue = new IterationKpiValue(issueType, null, data);
				iterationKpiValues.add(iterationKpiValue);
			});
			List<IterationKpiData> data = new ArrayList<>();
			IterationKpiData overAllCount = new IterationKpiData(RCA_SIZE, Double.valueOf(overAllRCAIssueCount.get(0)),
					null, null, "", overAllmodalValues);
			data.add(overAllCount);
			//filter and group the overall modal
			Map<String, IterationKpiData> groupedRCAData = groupIterationKpiData(overAllRCAdata);
			List<IterationKpiData> groupedData = new ArrayList<>(groupedRCAData.values());
			IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, null, groupedData);
			iterationKpiValues.add(overAllIterationKpiValue);

			// Create kpi level filters
			IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_PRIORITY, issueTypes);
			IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, null);
			trendValue.setValue(iterationKpiValues);
			kpiElement.setFilters(iterationKpiFilters);
			kpiElement.setSprint(latestSprint.getName());
			kpiElement.setModalHeads(KPIExcelColumn.DEFECT_COUNT_BY_ROOT_CAUSE.getColumns());
			kpiElement.setTrendValueList(trendValue);
			LOGGER.info("DefectCountByRCAServiceImpl -> request id : {} total jira Issues : {}", requestTrackerId, overAllRCAIssueCount.get(0));
		}
	}
}
	private Map<String, IterationKpiData> groupIterationKpiData(List<IterationKpiData> overAllRCAdata) {
		Map<String, IterationKpiData> groupedDataMap = new HashMap<>();
		for (IterationKpiData item : overAllRCAdata) {
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