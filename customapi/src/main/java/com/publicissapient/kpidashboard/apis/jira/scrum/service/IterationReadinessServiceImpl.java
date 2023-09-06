package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.jira.service.JiraServiceR;
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
import com.publicissapient.kpidashboard.common.model.jira.StatusWiseIssue;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IterationReadinessServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	@Autowired
	private JiraServiceR jiraService;
	@Autowired
	private ConfigHelperService configHelperService;

	private static final String PROJECT_WISE_JIRA_ISSUE = "Jira Issue";

	@Override
	public Integer calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DateUtil.DATE_FORMAT);

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);

		if (leafNode != null) {
			log.info("Iteration Readiness kpi -> Requested project : {}", leafNode.getProjectFilter().getName());
			List<JiraIssue> totalJiraIssue = jiraService.getJiraIssuesForCurrentSprint();
			List<JiraIssue> futureSprintJiraIssues = totalJiraIssue.stream()
					.filter(jiraIssue -> jiraIssue.getSprintAssetState().equalsIgnoreCase("FUTURE"))
					.collect(Collectors.toList());
			resultListMap.put(PROJECT_WISE_JIRA_ISSUE, futureSprintJiraIssues);
		}

		return resultListMap;
	}

	@Override
	public String getQualifierType() {
		return KPICode.ITERATION_READINESS_KPI.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		treeAggregatorDetail.getMapOfListOfProjectNodes().forEach((k, v) -> {
			Filters filters = Filters.getFilter(k);
			if (Filters.PROJECT == filters) {
				projectWiseLeafNodeValue(v, kpiElement, kpiRequest);
			}
		});
		log.info("Iteration Readiness Service impl -> getKpiData ->  : {}", kpiElement);
		return kpiElement;

	}

	private void projectWiseLeafNodeValue(List<Node> leafNodeList, KpiElement kpiElement, KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();
		List<KPIExcelData> excelData = new ArrayList<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);
		IterationKpiValue overAllIterationKpiValue = new IterationKpiValue();
		if (leafNode != null) {
			Object basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			Map<String, Object> resultMap = fetchKPIDataFromDb(leafNodeList, "", "", kpiRequest);
			List<JiraIssue> jiraIssues = (List<JiraIssue>) resultMap.get(PROJECT_WISE_JIRA_ISSUE);
			if (CollectionUtils.isNotEmpty(jiraIssues)) {
				List<DataCount> dataCountList = new ArrayList<>();
				Map<String, Map<String, List<JiraIssue>>> sprintStatusJiraIssueGroups = jiraIssues.stream()
						.collect(Collectors.groupingBy(JiraIssue::getSprintName,
								() -> new TreeMap<>(Comparator.comparing(sprintName -> {
									Optional<JiraIssue> earliestIssue = jiraIssues.stream()
											.filter(issue -> issue.getSprintName().equals(sprintName))
											.min(Comparator.comparing(issue -> {
												String sprintBeginDate = issue.getSprintBeginDate();
												return StringUtils.isNotEmpty(sprintBeginDate)
														? LocalDate.parse(sprintBeginDate.split("T")[0],
																DATE_TIME_FORMATTER)
														: LocalDate.MIN;
											}));
									return earliestIssue.map(JiraIssue::getSprintBeginDate).orElse("");
								})), Collectors.groupingBy(JiraIssue::getStatus)));

				sprintStatusJiraIssueGroups.forEach((sprintName, statusJiraMap) -> {
					DataCount dataCount = new DataCount();
					dataCount.setSSprintName(sprintName);
					dataCount.setKpiGroup(CommonConstant.FUTURE_SPRINTS);
					Map<String, StatusWiseIssue> statusWiseStoryCountAndPointMap = new LinkedHashMap<>();
					TreeMap<String, List<JiraIssue>> sortedStatusJiraMap = new TreeMap<>(statusJiraMap);
					sortedStatusJiraMap.forEach((status, jiraIssue) -> {
						StatusWiseIssue statusWiseData = getStatusWiseStoryCountAndPointList(jiraIssue, fieldMapping);
						statusWiseStoryCountAndPointMap.put(status, statusWiseData);
					});

					dataCount.setData(String.valueOf(calculateTotalSize(sortedStatusJiraMap)));
					dataCount.setValue(statusWiseStoryCountAndPointMap);
					dataCountList.add(dataCount);
				});

				overAllIterationKpiValue.setValue(dataCountList);
				populateExcelDataObject(requestTrackerId, excelData, jiraIssues, fieldMapping);
				kpiElement.setModalHeads(KPIExcelColumn.ITERATION_READINESS.getColumns());
				kpiElement.setExcelColumns(KPIExcelColumn.ITERATION_READINESS.getColumns());
				kpiElement.setExcelData(excelData);
				log.info("Iteration Readiness Service Impl -> request id : {} total jira Issues : {}",
						requestTrackerId);
			}

		}
		kpiElement.setTrendValueList(overAllIterationKpiValue);
	}

	public static int calculateTotalSize(Map<String, List<JiraIssue>> map) {
		int totalSize = 0;
		for (List<JiraIssue> list : map.values()) {
			totalSize += list.size();
		}

		return totalSize;
	}

	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			List<JiraIssue> jiraIssueList, FieldMapping fieldMapping) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& CollectionUtils.isNotEmpty(jiraIssueList)) {
			KPIExcelUtility.populateIterationReadinessExcelData(jiraIssueList, excelData, fieldMapping);
		}
	}

	private StatusWiseIssue getStatusWiseStoryCountAndPointList(List<JiraIssue> jiraIssueList,
			FieldMapping fieldMapping) {
		StatusWiseIssue statusWiseCountAndPoints = new StatusWiseIssue();
		statusWiseCountAndPoints.setIssueCount((double) jiraIssueList.size());
		statusWiseCountAndPoints.setIssueStoryPoint(jiraIssueList.stream().mapToDouble(jiraIssue -> {
			if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
					&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
				return Optional.ofNullable(jiraIssue.getStoryPoints()).orElse(0.0d);
			} else {
				Integer integer = Optional.ofNullable(jiraIssue.getOriginalEstimateMinutes()).orElse(0);
				int inHours = integer / 60;
				return inHours / fieldMapping.getStoryPointToHourMapping();
			}
		}).sum());

		return statusWiseCountAndPoints;
	}
}
