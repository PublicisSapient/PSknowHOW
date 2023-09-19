package com.publicissapient.kpidashboard.apis.jira.scrum.service.release;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.CommonServiceImpl;
import com.publicissapient.kpidashboard.apis.enums.Filters;
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
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.ReleaseKpiHelper;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EpicProgressServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	private static final String TOTAL_ISSUES = "totalIssues";
	private static final String EPIC_LINKED = "epicLinked";
	private static final String RELEASE_JIRA_ISSUE_STATUS = "releaseJiraIssueStatus";
	private static final String TO_DO = "To Do";
	private static final String IN_PROGRESS = "In Progress";
	private static final String DONE = "Done";
	private static final String ISSUE_COUNT = "Issue Count";
	private static final String STORY_POINT = "Story Point";
	private static final String OVERALL = "Overall";
	private static final String SEARCH_BY_ASSIGNEE = "Filter by Assignee";
	private static final String SEARCH_BY_PRIORITY = "Filter by Priority";
	@Autowired
	private JiraIssueRepository jiraIssueRepository;

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
			log.info("Epic Progress -> Requested sprint : {}", leafNode.getName());
			List<JiraIssue> releaseIssues = ReleaseKpiHelper
					.getFilteredReleaseJiraIssuesFromBaseClass(getBaseReleaseJiraIssues(), getBaseReleaseSubTask());

			resultListMap.put(TOTAL_ISSUES, releaseIssues);
			resultListMap.put(EPIC_LINKED,
					jiraIssueRepository.findEpicByNumberInAndBasicProjectConfigIdAndTypeName(
							releaseIssues.stream().map(JiraIssue::getEpicLinked).collect(Collectors.toList()),
							leafNode.getProjectFilter().getBasicProjectConfigId().toString(),
							NormalizedJira.ISSUE_TYPE.getValue()));
			resultListMap.put(RELEASE_JIRA_ISSUE_STATUS, getJiraIssueReleaseStatus());

		}
		return resultListMap;
	}

	@Override
	public String getQualifierType() {
		return KPICode.EPIC_PROGRESS.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.RELEASE) {
				releaseWiseLeafNodeValue(v, kpiElement, kpiRequest);
			}
		});
		log.info("ReleaseProgressServiceImpl -> getKpiData ->  : {}", kpiElement);
		return kpiElement;
	}

	private void releaseWiseLeafNodeValue(List<Node> releaseLeafNodeList, KpiElement kpiElement,
			KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();
		List<KPIExcelData> excelData = new ArrayList<>();
		List<Node> latestReleaseNode = new ArrayList<>();
		Node latestRelease = releaseLeafNodeList.get(0);

		if (latestRelease != null) {
			Object basicProjectConfigId = latestRelease.getProjectFilter().getBasicProjectConfigId();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			Optional.ofNullable(latestRelease).ifPresent(latestReleaseNode::add);
			Map<String, Object> resultMap = fetchKPIDataFromDb(latestReleaseNode, null, null, kpiRequest);
			List<JiraIssue> releaseIssues = (List<JiraIssue>) resultMap.get(TOTAL_ISSUES);
			Set<JiraIssue> epicIssues = (Set<JiraIssue>) resultMap.get(EPIC_LINKED);
			JiraIssueReleaseStatus jiraIssueReleaseStatus = (JiraIssueReleaseStatus) resultMap
					.get(RELEASE_JIRA_ISSUE_STATUS);
			List<IterationKpiValue> filterDataList = new ArrayList<>();

			if (CollectionUtils.isNotEmpty(releaseIssues) && jiraIssueReleaseStatus != null) {
				createDataCountGroupMap(releaseIssues, jiraIssueReleaseStatus, epicIssues, fieldMapping,
						filterDataList);
				populateExcelDataObject(requestTrackerId, excelData, releaseIssues, fieldMapping);
				/*
				 * List<DataCount> dataCountList = new ArrayList<>();
				 * 
				 * dataCountList.add(getStatusWiseCountList(releaseIssues,
				 * jiraIssueReleaseStatus, epic));
				 * 
				 * 
				 * IterationKpiValue overAllIterationKpiValue = new IterationKpiValue();
				 * overAllIterationKpiValue.setFilter1(OVERALL);
				 * overAllIterationKpiValue.setFilter2(OVERALL);
				 * overAllIterationKpiValue.setValue(dataCountList);
				 * filterDataList.add(overAllIterationKpiValue);
				 */

				kpiElement.setSprint(latestRelease.getName());
				kpiElement.setModalHeads(KPIExcelColumn.RELEASE_PROGRESS.getColumns());
				kpiElement.setExcelColumns(KPIExcelColumn.RELEASE_PROGRESS.getColumns());
				kpiElement.setExcelData(excelData);
			}
			kpiElement.setTrendValueList(filterDataList);
		}
	}

	public void createDataCountGroupMap(List<JiraIssue> jiraIssueList, JiraIssueReleaseStatus jiraIssueReleaseStatus,
			Set<JiraIssue> epicIssues, FieldMapping fieldMapping, List<IterationKpiValue> iterationKpiValues) {

		Map<String, List<JiraIssue>> epicWiseJiraIssues = jiraIssueList.stream().collect(
				Collectors.groupingBy(jiraIssue -> Optional.ofNullable(jiraIssue.getEpicLinked()).orElse("None")));
		Map<String, String> epicIssue = epicIssues.stream()
				.collect(Collectors.toMap(JiraIssue::getNumber, JiraIssue::getName));
		List<DataCount> dataCountList = new ArrayList<>();
		epicWiseJiraIssues.forEach((epic, issues) -> {
			String epicName = epicIssue.getOrDefault(epic, "None");
			dataCountList.add(getStatusWiseCountList(issues, jiraIssueReleaseStatus, epicName));
		});
		IterationKpiValue iterationKpiValue = new IterationKpiValue();
		iterationKpiValue.setValue(dataCountList);
		iterationKpiValue.setFilter1(OVERALL);
		iterationKpiValue.setFilter2(OVERALL);
		iterationKpiValues.add(iterationKpiValue);
	}

	private DataCount getStatusWiseCountList(List<JiraIssue> jiraIssueList,
			JiraIssueReleaseStatus jiraIssueReleaseStatus, String epic) {
		DataCount issueCountDc = new DataCount();
		List<DataCount> issueCountDcList = new ArrayList<>();
		List<JiraIssue> toDoJiraIssue = filterIssuesByStatus(jiraIssueList, jiraIssueReleaseStatus.getToDoList());
		List<JiraIssue> inProgressJiraIssue = filterIssuesByStatus(jiraIssueList,
				jiraIssueReleaseStatus.getInProgressList());
		List<JiraIssue> doneJiraIssue = filterIssuesByStatus(jiraIssueList, jiraIssueReleaseStatus.getClosedList());

		long toDoCount = toDoJiraIssue.size();
		Map<String, Integer> toDoStatusMap = toDoJiraIssue.stream()
				.collect(Collectors.groupingBy(JiraIssue::getStatus, Collectors.summingInt(issue -> 1)));
		createIssueCountDrillDown(toDoStatusMap, TO_DO, toDoCount, issueCountDcList);

		long inProgressCount = inProgressJiraIssue.size();
		Map<String, Integer> inProgressStatusMap = inProgressJiraIssue.stream()
				.collect(Collectors.groupingBy(JiraIssue::getStatus, Collectors.summingInt(issue -> 1)));
		createIssueCountDrillDown(inProgressStatusMap, IN_PROGRESS, inProgressCount, issueCountDcList);

		long doneCount = doneJiraIssue.size();
		Map<String, Integer> doneStatusMap = (doneJiraIssue.stream()
				.collect(Collectors.groupingBy(JiraIssue::getStatus, Collectors.summingInt(issue -> 1))));
		createIssueCountDrillDown(doneStatusMap, DONE, doneCount, issueCountDcList);

		issueCountDc.setData(String.valueOf(toDoCount + inProgressCount + doneCount));
		issueCountDc.setValue(issueCountDcList);
		issueCountDc.setKpiGroup(epic);
		return issueCountDc;
	}

	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			List<JiraIssue> jiraIssueList, FieldMapping fieldMapping) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& CollectionUtils.isNotEmpty(jiraIssueList)) {
			KPIExcelUtility.populateReleaseDefectRelatedExcelData(jiraIssueList, excelData, fieldMapping);
		}
	}

	/**
	 * Filtering the jiraIssue based on releaseStatus
	 *
	 * @param jiraIssueList
	 * @param statusMap
	 * @return
	 */
	private List<JiraIssue> filterIssuesByStatus(List<JiraIssue> jiraIssueList, Map<Long, String> statusMap) {
		return jiraIssueList.stream().filter(jiraIssue -> statusMap.containsValue(jiraIssue.getStatus()))
				.collect(Collectors.toList());
	}

	/**
	 * Create issueCountDrillDown
	 *
	 * @param issueCountStatusMap
	 * @param releaseStatus
	 * @param releaseStatusCount
	 * @param issueCountDcList
	 */
	private static void createIssueCountDrillDown(Map<String, Integer> issueCountStatusMap, String releaseStatus,
			long releaseStatusCount, List<DataCount> issueCountDcList) {
		List<DataCount> drillDownList = new ArrayList<>();
		issueCountStatusMap.forEach((status, count) -> drillDownList.add(new DataCount(status, count, null)));
		DataCount releaseStatusDc = new DataCount(releaseStatus, releaseStatusCount, drillDownList);
		issueCountDcList.add(releaseStatusDc);
	}

}
