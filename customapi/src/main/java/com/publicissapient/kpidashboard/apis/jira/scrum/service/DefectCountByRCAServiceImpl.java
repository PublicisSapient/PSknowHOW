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

	private static final String SEARCH_BY_PRIORITY = "filter1";
	public static final String UNCHECKED = "unchecked";
	private static final String COMPLETED_ISSUES = "completedIssues";
	private static final String RCA_SIZE = "RCACount";
	private static final String OVERALL = "Overall";
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
				List<String> totalCompletedIssues = KpiDataHelper
						.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails, CommonConstant.TOTAL_ISSUES);
				if (CollectionUtils.isNotEmpty(totalCompletedIssues)) {
					List<JiraIssue> issueList = jiraIssueRepository
							.findByNumberInAndBasicProjectConfigId(totalCompletedIssues, basicProjectConfigId);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getCompletedIssues(), issueList);
					resultListMap.put(COMPLETED_ISSUES, new ArrayList<>(filtersIssuesList));
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
			Node root = treeAggregatorDetail.getRoot();
			Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();

			treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {

				if (Filters.getFilter(k) == Filters.SPRINT) {
					sprintWiseLeafNodeValue(mapTmp, v, trendValueList, kpiElement, kpiRequest);
				}
			});
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

		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
				.get(latestSprint.getProjectFilter().getBasicProjectConfigId());
		List<String> testingStatuses = fieldMapping.getJiradefecttype();
		Double minutesInDay = fieldMapping.getWorkingHoursDayCPT() * 60;
		if (CollectionUtils.isNotEmpty((List<JiraIssue>) resultMap.get(COMPLETED_ISSUES))) {
			List<JiraIssue> allIssues = ((List<JiraIssue>) resultMap.get(COMPLETED_ISSUES)).stream()
					.filter(issue -> testingStatuses.contains(issue.getTypeName())).collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(allIssues)) {
				LOGGER.info("RCA count  -> request id : {} total jira Issues : {}", requestTrackerId, allIssues.size());

				Map<String, Map<String, List<JiraIssue>>> typeWiseIssues = allIssues.stream()
						.collect(Collectors.groupingBy(JiraIssue::getPriority,
								Collectors.groupingBy(jiraIssue -> jiraIssue.getRootCauseList().get(0))));

				Set<String> issueTypes = new HashSet<>();
				List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
				List<Integer> overAllIssueCount = Arrays.asList(0);
				List<IterationKpiModalValue> overAllmodalValues = new ArrayList<>();
				List<IterationKpiData> overAllRCAdata = new ArrayList<>();
				typeWiseIssues.forEach((issueType, issues) -> {
					issueTypes.add(issueType);
					List<IterationKpiData> data = new ArrayList<>();
					issues.forEach((key, values) -> {
						List<IterationKpiModalValue> modalValues = new ArrayList<>();
						IterationKpiData issueCounts = new IterationKpiData(key, Double.valueOf(values.size()), null, key, "", modalValues);
						if (values instanceof ArrayList) {
							for (JiraIssue jiraIssue : values) {
								overAllIssueCount.set(0, overAllIssueCount.get(0) + 1);
								populateIterationData(overAllmodalValues, modalValues, jiraIssue);
							}
						}
						data.add(issueCounts);
						overAllRCAdata.add(issueCounts);
					});
					IterationKpiValue iterationKpiValue = new IterationKpiValue(issueType, null, data);
					iterationKpiValues.add(iterationKpiValue);
				});
				List<IterationKpiData> data = new ArrayList<>();
				IterationKpiData overAllCount = new IterationKpiData(RCA_SIZE, Double.valueOf(overAllIssueCount.get(0)),
						null, null, "", overAllmodalValues);
				data.add(overAllCount);
				IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, null, overAllRCAdata);
				iterationKpiValues.add(overAllIterationKpiValue);

				// Create kpi level filters
				IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_PRIORITY, issueTypes);
				IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, null);
				trendValue.setValue(iterationKpiValues);
				kpiElement.setFilters(iterationKpiFilters);
				kpiElement.setSprint(latestSprint.getName());
				kpiElement.setModalHeads(KPIExcelColumn.DEFECT_COUNT_BY_ROOT_CAUSE.getColumns());
				kpiElement.setTrendValueList(trendValue);
			}
		}
	}
}
