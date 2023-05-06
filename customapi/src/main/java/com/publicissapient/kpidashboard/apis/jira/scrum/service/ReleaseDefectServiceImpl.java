package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

@Component
public class ReleaseDefectServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseDefectServiceImpl.class);
	private static final String RELEASE = "releaseName";
	private static final String TOTAL_DEFECT = "totalDefects";
	private static final String PRIORITY = "Priority";
	private static final String STATUS = "Status";
	private static final String Assignee = "Assignee";
	private static final String RCA = "RCA";
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
			LOGGER.info("Defect count by RCA -> Requested sprint : {}", leafNode.getName());
			String basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId().toString();
			String releaseId = leafNode.getReleaseFilter().getName().split("_")[0];
			List<String> defectType = new ArrayList<>();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(leafNode.getProjectFilter().getBasicProjectConfigId());

			if (null != fieldMapping) {
				Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
				Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
				Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
				if (fieldMapping.getJiradefecttype() != null) {
					defectType.addAll(fieldMapping.getJiradefecttype());
				}
				defectType.add(NormalizedJira.DEFECT_TYPE.getValue());
				mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
						CommonUtils.convertToPatternList(defectType));
				mapOfProjectFilters.put(RELEASE, Arrays.asList(releaseId));
				uniqueProjectMap.put(basicProjectConfigId, mapOfProjectFilters);
				mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
						Collections.singletonList(basicProjectConfigId));

				List<JiraIssue> releaseDefects = jiraIssueRepository.findByRelease(mapOfFilters, uniqueProjectMap);
				resultListMap.put(TOTAL_DEFECT, releaseDefects);
			}
		}
		return resultListMap;
	}

	@Override
	public String getQualifierType() {
		return KPICode.RELEASE_DEFECT.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.RELEASE) {
				releaseWiseLeafNodeValue(v, kpiElement, kpiRequest);
			}
		});
		LOGGER.info("DefectCountByRCAServiceImpl -> getKpiData ->  : {}", kpiElement);
		return kpiElement;
	}

	/**
	 * This method will set trendValueList information to the RCA KPI. It consists
	 * of logic to show data for P1, P2, P3, P4 and "Overall" Priorities as per the
	 * accepted JSON structure.
	 * 
	 * @param releaseLeafNodeList
	 * @param kpiElement
	 * @param kpiRequest
	 */
	private void releaseWiseLeafNodeValue(List<Node> releaseLeafNodeList, KpiElement kpiElement,
			KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();
		List<KPIExcelData> excelData = new ArrayList<>();
		List<Node> latestReleaseNode = new ArrayList<>();
		Node latestRelease = releaseLeafNodeList.get(0);
		Optional.ofNullable(latestRelease).ifPresent(latestReleaseNode::add);
		if (latestRelease != null) {
			Map<String, Object> resultMap = fetchKPIDataFromDb(latestReleaseNode, null, null, kpiRequest);
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(latestRelease.getProjectFilter().getBasicProjectConfigId());
			if (fieldMapping != null) {

				List<JiraIssue> totalDefects = (List<JiraIssue>) resultMap.get(TOTAL_DEFECT);

				Map<String, Map<String, List<JiraIssue>>> priorityWiseRCAList = getPriorityWiseRCAList(totalDefects);
				List<Integer> overAllRCAIssueCount = Arrays.asList(0);
				LOGGER.info("DefectCountByRCAServiceImpl -> priorityWiseRCAList ->  : {}", priorityWiseRCAList);
				// filterDataList will consist of IterationKpiValue which will be set for all
				// priorities
				List<IterationKpiValue> filterDataList = new ArrayList<>();
				for (Map.Entry<String, Map<String, List<JiraIssue>>> entry : priorityWiseRCAList.entrySet()) {
					String filterKey = entry.getKey();
					Map<String, List<JiraIssue>> filterIssueData = entry.getValue();

					DataCount priorityData = new DataCount();
					priorityData.setData(filterKey);
					priorityData.setValue(new ArrayList<>());

					int filteredCount = 0;
					Map<String, Integer> filteredCountMap = new HashMap<>();
					// update and set the overall data
					filteredCount = getFilteredCount(filterIssueData, filteredCount, filteredCountMap);
					DataCount filterDataCount = new DataCount();
					filterDataCount.setData(String.valueOf(filteredCount));
					filterDataCount.setValue(filteredCountMap);
					filterDataCount.setKpiGroup(filterKey);
					filterDataCount.setSProjectName(latestRelease.getProjectFilter().getName());
					// dataCountList will store data for P1,P2,P3 and P4 priorities pertaining to
					// child level structure
					List<DataCount> dataCountList = (List<DataCount>) priorityData.getValue();

					// add dataCount for middle level structure to store P1,P2,P3 and P4 Priorities,
					// set dataCountList
					// as value for child level structure
					List<DataCount> middleTrendValueListForPriorities = new ArrayList<>();
					DataCount middleOverallData = new DataCount();
					middleOverallData.setData(latestRelease.getProjectFilter().getName());
					middleOverallData.setValue(dataCountList);
					middleTrendValueListForPriorities.add(middleOverallData);

					IterationKpiValue filterData = new IterationKpiValue(filterKey, middleTrendValueListForPriorities);
					filterDataList.add(filterData);
					dataCountList.add(filterDataCount);
					priorityData.setValue(dataCountList);
				}
				populateExcelDataObject(requestTrackerId, excelData, totalDefects,
						latestRelease.getReleaseFilter().getName(), fieldMapping);
				kpiElement.setModalHeads(KPIExcelColumn.DEFECT_COUNT_BY_RCA_PIE_CHART.getColumns());
				kpiElement.setExcelColumns(KPIExcelColumn.DEFECT_COUNT_BY_RCA_PIE_CHART.getColumns());
				kpiElement.setExcelData(excelData);
				// filterDataList will consist of iterationKpiValue for all the available
				// priorities such as P1, P2, P3, P4, Overall etc.
				kpiElement.setTrendValueList(filterDataList);
				LOGGER.info("DefectCountByRCAServiceImpl -> request id : {} total jira Issues : {}", requestTrackerId,
						overAllRCAIssueCount.get(0));

			}
		}
	}

	private static int getFilteredCount(Map<String, List<JiraIssue>> rcaData, int priorityRCACount,
			Map<String, Integer> rcaCountMap) {
		for (Map.Entry<String, List<JiraIssue>> rcaEntry : rcaData.entrySet()) {
			String rcaName = rcaEntry.getKey();
			List<JiraIssue> issues = rcaEntry.getValue();

			priorityRCACount += issues.size();
			rcaCountMap.put(rcaName, issues.size());
		}
		return priorityRCACount;
	}

	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			List<JiraIssue> jiraIssueList, String name, FieldMapping fieldMapping) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& !Objects.isNull(jiraIssueList) && !jiraIssueList.isEmpty()) {
			KPIExcelUtility.populateDefectRCAandStatusRelatedExcelData(name, jiraIssueList, excelData, fieldMapping);
		}
	}

	private Map<String, Map<String, List<JiraIssue>>> getPriorityWiseRCAList(
			List<JiraIssue> allCompletedIssuesExcludeStory) {
		Map<String, Map<String, List<JiraIssue>>> returnMap = new HashMap<>();
		returnMap.put(PRIORITY,
				allCompletedIssuesExcludeStory.stream().collect(Collectors.groupingBy(JiraIssue::getPriority)));
		returnMap.put(Assignee,
				allCompletedIssuesExcludeStory.stream().collect(Collectors.groupingBy(JiraIssue::getAssigneeName)));
		returnMap.put(RCA, allCompletedIssuesExcludeStory.stream()
				.collect(Collectors.groupingBy(jiraIssue -> jiraIssue.getRootCauseList().get(0))));
		returnMap.put(STATUS,
				allCompletedIssuesExcludeStory.stream().collect(Collectors.groupingBy(JiraIssue::getStatus)));
		return returnMap;
	}

}