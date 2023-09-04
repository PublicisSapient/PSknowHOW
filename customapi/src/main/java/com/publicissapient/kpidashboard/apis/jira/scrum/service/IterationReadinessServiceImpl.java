package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
		List<IterationKpiValue> filterDataList = new ArrayList<>();
		if (leafNode != null) {
			Object basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			Map<String, Object> resultMap = fetchKPIDataFromDb(leafNodeList, "", "", kpiRequest);
			List<JiraIssue> jiraIssues = (List<JiraIssue>) resultMap.get(PROJECT_WISE_JIRA_ISSUE);
			if (CollectionUtils.isNotEmpty(jiraIssues)) {
				List<DataCount> dataCountList = new ArrayList<>();
				Map<String, Map<String, List<JiraIssue>>> sprintStatusJiraIssueGroups = jiraIssues.stream().collect(
						Collectors.groupingBy(JiraIssue::getSprintName, Collectors.groupingBy(JiraIssue::getStatus)));
				sprintStatusJiraIssueGroups.forEach((sprintName, statusJiraMap) -> statusJiraMap
						.forEach((status, jiraIssue) -> dataCountList.add(getStatusWiseStoryCountAndPointList(leafNode,
								jiraIssue, fieldMapping, sprintName, status))));
				IterationKpiValue overAllIterationKpiValue = new IterationKpiValue();
				overAllIterationKpiValue.setValue(dataCountList);
				filterDataList.add(overAllIterationKpiValue);
				populateExcelDataObject(requestTrackerId, excelData, jiraIssues, fieldMapping);
				kpiElement.setModalHeads(KPIExcelColumn.ITERATION_READINESS.getColumns());
				kpiElement.setExcelColumns(KPIExcelColumn.ITERATION_READINESS.getColumns());
				kpiElement.setExcelData(excelData);
				log.info("Iteration Readiness Service Impl -> request id : {} total jira Issues : {}", requestTrackerId,
						filterDataList.get(0));
			}

		}
		kpiElement.setTrendValueList(filterDataList);
	}

	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			List<JiraIssue> jiraIssueList, FieldMapping fieldMapping) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& CollectionUtils.isNotEmpty(jiraIssueList)) {
			KPIExcelUtility.populateIterationReadinessExcelData(jiraIssueList, excelData, fieldMapping);
		}
	}

	private DataCount getStatusWiseStoryCountAndPointList(Node leafNode, List<JiraIssue> jiraIssueList,
			FieldMapping fieldMapping, String sprintName, String status) {
		DataCount dataCount = new DataCount();
		Map<String, List<StatusWiseIssue>> statusWiseCountAndPoint = new LinkedHashMap<>();
		List<StatusWiseIssue> statusWiseIssueList = new ArrayList<>();
		StatusWiseIssue swi = new StatusWiseIssue();
		swi.setIssueCount((double) jiraIssueList.size());
		swi.setIssueStoryPoint(jiraIssueList.stream().mapToDouble(jiraIssue -> {
			if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
					&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
				return Optional.ofNullable(jiraIssue.getStoryPoints()).orElse(0.0d);
			} else {
				Integer integer = Optional.ofNullable(jiraIssue.getOriginalEstimateMinutes()).orElse(0);
				int inHours = integer / 60;
				return inHours / fieldMapping.getStoryPointToHourMapping();
			}
		}).sum());
		statusWiseIssueList.add(swi);
		statusWiseCountAndPoint.put(status, statusWiseIssueList);
		dataCount.setSSprintName(sprintName);
		dataCount.setValue(statusWiseCountAndPoint);
		dataCount.setKpiGroup(CommonConstant.FUTURE_SPRINTS);
		dataCount.setSProjectName(leafNode.getProjectFilter().getName());
		return dataCount;
	}
}
