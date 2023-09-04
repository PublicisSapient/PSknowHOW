package com.publicissapient.kpidashboard.apis.jira.scrum.service.release;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.publicissapient.kpidashboard.apis.model.IterationKpiFilters;
import com.publicissapient.kpidashboard.apis.model.IterationKpiFiltersOptions;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ReleaseProgressServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	private static final String TOTAL_ISSUES = "totalIssues";
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
			log.info("Release Progress -> Requested sprint : {}", leafNode.getName());
			List<JiraIssue> releaseIssues = getFilteredReleaseJiraIssuesFromBaseClass(null);
			resultListMap.put(TOTAL_ISSUES, releaseIssues);
			JiraIssueReleaseStatus jiraIssueReleaseStatus = getJiraIssueReleaseStatus();
			resultListMap.put(RELEASE_JIRA_ISSUE_STATUS, jiraIssueReleaseStatus);
		}
		return resultListMap;
	}

	@Override
	public String getQualifierType() {
		return KPICode.RELEASE_PROGRESS.name();
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
			JiraIssueReleaseStatus jiraIssueReleaseStatus = (JiraIssueReleaseStatus) resultMap
					.get(RELEASE_JIRA_ISSUE_STATUS);
			List<IterationKpiValue> filterDataList = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(releaseIssues) && jiraIssueReleaseStatus != null) {
				Set<String> assignees = new HashSet<>();
				Set<String> priorities = new HashSet<>();
				createDataCountGroupMap(releaseIssues, jiraIssueReleaseStatus, assignees, priorities, fieldMapping,
						filterDataList);
				populateExcelDataObject(requestTrackerId, excelData, releaseIssues, fieldMapping);
				List<DataCount> dataCountList = new ArrayList<>();
				dataCountList.add(getStatusWiseCountList(releaseIssues, jiraIssueReleaseStatus));
				dataCountList.add(getStatusWiseStoryPointList(releaseIssues, fieldMapping, jiraIssueReleaseStatus));
				IterationKpiValue overAllIterationKpiValue = new IterationKpiValue();
				overAllIterationKpiValue.setFilter1(OVERALL);
				overAllIterationKpiValue.setFilter2(OVERALL);
				overAllIterationKpiValue.setValue(dataCountList);
				filterDataList.add(overAllIterationKpiValue);
				IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_ASSIGNEE, assignees);
				IterationKpiFiltersOptions filter2 = new IterationKpiFiltersOptions(SEARCH_BY_PRIORITY, priorities);
				IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, filter2);
				kpiElement.setFilters(iterationKpiFilters);
				kpiElement.setSprint(latestRelease.getName());
				kpiElement.setModalHeads(KPIExcelColumn.RELEASE_PROGRESS.getColumns());
				kpiElement.setExcelColumns(KPIExcelColumn.RELEASE_PROGRESS.getColumns());
				kpiElement.setExcelData(excelData);
			}
			kpiElement.setTrendValueList(filterDataList);
		}
	}

	public void createDataCountGroupMap(List<JiraIssue> jiraIssueList, JiraIssueReleaseStatus jiraIssueReleaseStatus,
			Set<String> assigneeNames, Set<String> priorities, FieldMapping fieldMapping,
			List<IterationKpiValue> iterationKpiValues) {
		Map<String, Map<String, List<JiraIssue>>> typeAndStatusWiseIssues = jiraIssueList.stream().collect(
				Collectors.groupingBy(jiraIssue -> Optional.ofNullable(jiraIssue.getAssigneeName()).orElse("-"),
						Collectors.groupingBy(JiraIssue::getPriority)));
		typeAndStatusWiseIssues
				.forEach((assigneeName, priorityWiseIssue) -> priorityWiseIssue.forEach((priority, issues) -> {
					List<DataCount> dataCountList = new ArrayList<>();
					assigneeNames.add(assigneeName);
					priorities.add(priority);
					dataCountList.add(getStatusWiseCountList(issues, jiraIssueReleaseStatus));
					dataCountList.add(getStatusWiseStoryPointList(issues, fieldMapping, jiraIssueReleaseStatus));
					IterationKpiValue matchingObject = iterationKpiValues.stream()
							.filter(p -> p.getFilter1().equals(assigneeName) && p.getFilter2().equals(priority))
							.findAny().orElse(null);
					if (matchingObject == null) {
						IterationKpiValue iterationKpiValue = new IterationKpiValue();
						iterationKpiValue.setFilter1(assigneeName);
						iterationKpiValue.setFilter2(priority);
						iterationKpiValue.setValue(dataCountList);
						iterationKpiValues.add(iterationKpiValue);
					} else {
						matchingObject.getValue().addAll(dataCountList);
					}
				}));

	}

/*
	private DataCount getStatusWiseCountList(List<JiraIssue> jiraIssueList,
			JiraIssueReleaseStatus jiraIssueReleaseStatus) {
		DataCount dataCount = new DataCount();
		Map<String, Double> releaseProgressCount = new LinkedHashMap<>();
		releaseProgressCount.put(TO_DO,
				(double) jiraIssueList.stream().filter(
						jiraIssue -> jiraIssueReleaseStatus.getToDoList().values().contains(jiraIssue.getStatus()))
						.count());
		releaseProgressCount.put(IN_PROGRESS, (double) jiraIssueList.stream().filter(
				jiraIssue -> jiraIssueReleaseStatus.getInProgressList().values().contains(jiraIssue.getStatus()))
				.count());
		releaseProgressCount.put(DONE,
				(double) jiraIssueList.stream().filter(
						jiraIssue -> jiraIssueReleaseStatus.getClosedList().values().contains(jiraIssue.getStatus()))
						.count());
		dataCount
				.setData(String.valueOf(releaseProgressCount.values().stream().mapToDouble(Double::doubleValue).sum()));
		dataCount.setValue(releaseProgressCount);
		dataCount.setKpiGroup(ISSUE_COUNT);
		return dataCount;
	}
*/
/*
	private DataCount getStatusWiseStoryPointList(List<JiraIssue> jiraIssueList, FieldMapping fieldMapping,
			JiraIssueReleaseStatus jiraIssueReleaseStatus) {
		DataCount dataCount = new DataCount();
		Map<String, Double> releaseProgressStoryPoint = new HashMap<>();

		releaseProgressStoryPoint.put(TO_DO, jiraIssueList.stream()
				.filter(jiraIssue -> jiraIssueReleaseStatus.getToDoList().values().contains(jiraIssue.getStatus()))
				.mapToDouble(jiraIssue -> {
					if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
							&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
						return Optional.ofNullable(jiraIssue.getStoryPoints()).orElse(0.0d);
					} else {
						Integer integer = Optional.ofNullable(jiraIssue.getOriginalEstimateMinutes()).orElse(0);
						int inHours = integer / 60;
						return inHours / fieldMapping.getStoryPointToHourMapping();
					}
				}).sum());
		releaseProgressStoryPoint.put(IN_PROGRESS, jiraIssueList.stream().filter(
				jiraIssue -> jiraIssueReleaseStatus.getInProgressList().values().contains(jiraIssue.getStatus()))
				.mapToDouble(jiraIssue -> {
					if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
							&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
						return Optional.ofNullable(jiraIssue.getStoryPoints()).orElse(0.0d);
					} else {
						Integer integer = Optional.ofNullable(jiraIssue.getOriginalEstimateMinutes()).orElse(0);
						int inHours = integer / 60;
						return inHours / fieldMapping.getStoryPointToHourMapping();
					}
				}).sum());
		releaseProgressStoryPoint.put(DONE, jiraIssueList.stream()
				.filter(jiraIssue -> jiraIssueReleaseStatus.getClosedList().values().contains(jiraIssue.getStatus()))
				.mapToDouble(jiraIssue -> {
					if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
							&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
						return Optional.ofNullable(jiraIssue.getStoryPoints()).orElse(0.0d);
					} else {
						Integer integer = Optional.ofNullable(jiraIssue.getOriginalEstimateMinutes()).orElse(0);
						int inHours = integer / 60;
						return inHours / fieldMapping.getStoryPointToHourMapping();
					}
				}).sum());
		dataCount.setData(
				String.valueOf(releaseProgressStoryPoint.values().stream().mapToDouble(Double::doubleValue).sum()));
		dataCount.setValue(releaseProgressStoryPoint);
		dataCount.setKpiGroup(STORY_POINT);
		return dataCount;
	}
*/

private DataCount getStatusWiseCountList(List<JiraIssue> jiraIssueList, JiraIssueReleaseStatus jiraIssueReleaseStatus) {
	DataCount issueCountDc = new DataCount();
	List<DataCount> issueCountDcList = new ArrayList<>();
	List<JiraIssue> toDoJiraIssue = jiraIssueList.stream()
			.filter(jiraIssue -> jiraIssueReleaseStatus.getToDoList().values().contains(jiraIssue.getStatus()))
			.collect(Collectors.toList());
	List<JiraIssue> inProgressJiraIssue = jiraIssueList.stream()
			.filter(jiraIssue -> jiraIssueReleaseStatus.getInProgressList().values().contains(jiraIssue.getStatus()))
			.collect(Collectors.toList());
	List<JiraIssue> doneJiraIssue = jiraIssueList.stream()
			.filter(jiraIssue -> jiraIssueReleaseStatus.getClosedList().values().contains(jiraIssue.getStatus()))
			.collect(Collectors.toList());

	DataCount toDoDc = new DataCount();
	toDoDc.setSubFilter(TO_DO);
	long toDoCount = toDoJiraIssue.stream().count();
	toDoDc.setValue(String.valueOf(toDoCount));
	Map<String, Integer> toDoStatusMap = (toDoJiraIssue.stream()
			.collect(Collectors.groupingBy(JiraIssue::getStatus, Collectors.summingInt(issue -> 1))));
	List<DataCount> toDoDrillDownList = new ArrayList<>();
	toDoStatusMap.forEach((status, count) -> {
		DataCount toDoDrillDown = new DataCount();
		toDoDrillDown.setSubFilter(status);
		toDoDrillDown.setValue(count);
		toDoDrillDownList.add(toDoDrillDown);
	});
	toDoDc.setDrillDown(toDoDrillDownList);
	issueCountDcList.add(toDoDc);

	DataCount inProgressDc = new DataCount();
	inProgressDc.setSubFilter(IN_PROGRESS);
	long inProgressCount = toDoJiraIssue.stream().count();
	inProgressDc.setValue(String.valueOf(inProgressCount));
	Map<String, Integer> inProgressStatusMap = (inProgressJiraIssue.stream()
			.collect(Collectors.groupingBy(JiraIssue::getStatus, Collectors.summingInt(issue -> 1))));
	List<DataCount> inProgressDrillDownList = new ArrayList<>();
	inProgressStatusMap.forEach((status, count) -> {
		DataCount toDoDrillDown = new DataCount();
		toDoDrillDown.setSubFilter(status);
		toDoDrillDown.setValue(count);
		inProgressDrillDownList.add(toDoDrillDown);
	});
	inProgressDc.setDrillDown(inProgressDrillDownList);
	issueCountDcList.add(inProgressDc);

	DataCount doneDc = new DataCount();
	doneDc.setSubFilter(DONE);
	long doneCount = toDoJiraIssue.stream().count();
	doneDc.setValue(String.valueOf(doneCount));
	Map<String, Integer> doneStatusMap = (doneJiraIssue.stream()
			.collect(Collectors.groupingBy(JiraIssue::getStatus, Collectors.summingInt(issue -> 1))));
	List<DataCount> doneDrillDownList = new ArrayList<>();
	doneStatusMap.forEach((status, count) -> {
		DataCount toDoDrillDown = new DataCount();
		toDoDrillDown.setSubFilter(status);
		toDoDrillDown.setValue(count);
		doneDrillDownList.add(toDoDrillDown);
	});
	doneDc.setDrillDown(doneDrillDownList);
	issueCountDcList.add(doneDc);

	issueCountDc.setData(String.valueOf(inProgressCount + doneCount + toDoCount));
	issueCountDc.setValue(issueCountDcList);
	issueCountDc.setKpiGroup(ISSUE_COUNT);
	return issueCountDc;
}

private DataCount getStatusWiseStoryPointList(List<JiraIssue> jiraIssueList, FieldMapping fieldMapping,
		JiraIssueReleaseStatus jiraIssueReleaseStatus) {
	DataCount storyPointDc = new DataCount();
	List<DataCount> storyPointDcList = new ArrayList<>();
	List<JiraIssue> toDoJiraIssue = jiraIssueList.stream()
			.filter(jiraIssue -> jiraIssueReleaseStatus.getToDoList().values().contains(jiraIssue.getStatus()))
			.collect(Collectors.toList());
	List<JiraIssue> inProgressJiraIssue = jiraIssueList.stream()
			.filter(jiraIssue -> jiraIssueReleaseStatus.getInProgressList().values().contains(jiraIssue.getStatus()))
			.collect(Collectors.toList());
	List<JiraIssue> doneJiraIssue = jiraIssueList.stream()
			.filter(jiraIssue -> jiraIssueReleaseStatus.getClosedList().values().contains(jiraIssue.getStatus()))
			.collect(Collectors.toList());

	DataCount toDoSpDc = new DataCount();
	toDoSpDc.setSubFilter(TO_DO);
	double toDoSp = toDoJiraIssue.stream().mapToDouble(jiraIssue -> {
		if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
				&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
			return Optional.ofNullable(jiraIssue.getStoryPoints()).orElse(0.0d);
		} else {
			Integer integer = Optional.ofNullable(jiraIssue.getOriginalEstimateMinutes()).orElse(0);
			int inHours = integer / 60;
			return inHours / fieldMapping.getStoryPointToHourMapping();
		}
	}).sum();
	toDoSpDc.setValue(toDoSp);
	Map<String, Double> toDoSpMap = toDoJiraIssue.stream()
			.collect(Collectors.groupingBy(JiraIssue::getStatus, Collectors.summingDouble(jiraIssue -> {
				if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
						&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
					return Optional.ofNullable(jiraIssue.getStoryPoints()).orElse(0.0d);
				} else {
					Integer integer = Optional.ofNullable(jiraIssue.getOriginalEstimateMinutes()).orElse(0);
					int inHours = integer / 60;
					return inHours / fieldMapping.getStoryPointToHourMapping();
				}
			})));
	List<DataCount> toDoDrillDownList = new ArrayList<>();
	toDoSpMap.forEach((status, spVal) -> {
		DataCount toDoDrillDown = new DataCount();
		toDoDrillDown.setSubFilter(status);
		toDoDrillDown.setValue(spVal);
		toDoDrillDownList.add(toDoDrillDown);
	});
	toDoSpDc.setDrillDown(toDoDrillDownList);
	storyPointDcList.add(toDoSpDc);

	DataCount inProgressSpDc = new DataCount();
	inProgressSpDc.setSubFilter(IN_PROGRESS);
	double inProgressSp = inProgressJiraIssue.stream()
			.filter(jiraIssue -> jiraIssueReleaseStatus.getInProgressList().values().contains(jiraIssue.getStatus()))
			.mapToDouble(jiraIssue -> {
				if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
						&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
					return Optional.ofNullable(jiraIssue.getStoryPoints()).orElse(0.0d);
				} else {
					Integer integer = Optional.ofNullable(jiraIssue.getOriginalEstimateMinutes()).orElse(0);
					int inHours = integer / 60;
					return inHours / fieldMapping.getStoryPointToHourMapping();
				}
			}).sum();
	inProgressSpDc.setValue(inProgressSp);
	Map<String, Double> inProgressSpMap = inProgressJiraIssue.stream()
			.collect(Collectors.groupingBy(JiraIssue::getStatus, Collectors.summingDouble(jiraIssue -> {
				if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
						&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
					return Optional.ofNullable(jiraIssue.getStoryPoints()).orElse(0.0d);
				} else {
					Integer integer = Optional.ofNullable(jiraIssue.getOriginalEstimateMinutes()).orElse(0);
					int inHours = integer / 60;
					return inHours / fieldMapping.getStoryPointToHourMapping();
				}
			})));
	List<DataCount> inProgressDrillDownList = new ArrayList<>();
	inProgressSpMap.forEach((status, spVal) -> {
		DataCount toDoDrillDown = new DataCount();
		toDoDrillDown.setSubFilter(status);
		toDoDrillDown.setValue(spVal);
		inProgressDrillDownList.add(toDoDrillDown);
	});
	inProgressSpDc.setDrillDown(inProgressDrillDownList);
	storyPointDcList.add(inProgressSpDc);

	DataCount doneSpDc = new DataCount();
	doneSpDc.setSubFilter(DONE);
	double doneSp = doneJiraIssue.stream()
			.filter(jiraIssue -> jiraIssueReleaseStatus.getClosedList().values().contains(jiraIssue.getStatus()))
			.mapToDouble(jiraIssue -> {
				if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
						&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
					return Optional.ofNullable(jiraIssue.getStoryPoints()).orElse(0.0d);
				} else {
					Integer integer = Optional.ofNullable(jiraIssue.getOriginalEstimateMinutes()).orElse(0);
					int inHours = integer / 60;
					return inHours / fieldMapping.getStoryPointToHourMapping();
				}
			}).sum();
	doneSpDc.setValue(doneSp);
	Map<String, Double> doneSpMap = doneJiraIssue.stream()
			.collect(Collectors.groupingBy(JiraIssue::getStatus, Collectors.summingDouble(jiraIssue -> {
				if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
						&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
					return Optional.ofNullable(jiraIssue.getStoryPoints()).orElse(0.0d);
				} else {
					Integer integer = Optional.ofNullable(jiraIssue.getOriginalEstimateMinutes()).orElse(0);
					int inHours = integer / 60;
					return inHours / fieldMapping.getStoryPointToHourMapping();
				}
			})));
	List<DataCount> doneDrillDownList = new ArrayList<>();
	doneSpMap.forEach((status, spVal) -> {
		DataCount doneDrillDown = new DataCount();
		doneDrillDown.setSubFilter(status);
		doneDrillDown.setValue(spVal);
		doneDrillDownList.add(doneDrillDown);
	});
	doneSpDc.setDrillDown(doneDrillDownList);
	storyPointDcList.add(doneSpDc);

	storyPointDc.setData(String.valueOf(inProgressSp + toDoSp + doneSp));
	storyPointDc.setValue(storyPointDcList);
	storyPointDc.setKpiGroup(STORY_POINT);
	return storyPointDc;
}

	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			List<JiraIssue> jiraIssueList, FieldMapping fieldMapping) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& CollectionUtils.isNotEmpty(jiraIssueList)) {
			KPIExcelUtility.populateReleaseDefectRelatedExcelData(jiraIssueList, excelData, fieldMapping);
		}
	}

}
