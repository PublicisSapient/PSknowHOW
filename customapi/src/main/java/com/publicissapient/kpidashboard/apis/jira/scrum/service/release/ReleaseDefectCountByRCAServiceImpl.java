/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.jira.scrum.service.release;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
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
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;

import lombok.extern.slf4j.Slf4j;

// Defect count by RCA kpi on release tab
@Slf4j
@Component
public class ReleaseDefectCountByRCAServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	private static final String TOTAL_DEFECT = "Total Defects";
	private static final String OPEN_DEFECT = "Open Defects";

	@Autowired
	private ConfigHelperService configHelperService;

	@Override
	public Integer calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

	@Override
	public String getQualifierType() {
		return KPICode.DEFECT_COUNT_BY_RCA_RELEASE.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.RELEASE) {
				releaseWiseLeafNodeValue(v, kpiElement, kpiRequest);
			}
		});
		log.info("ReleaseDefectCountByRCAServiceImpl -> getKpiData ->  : {}", kpiElement);
		return kpiElement;
	}

	/**
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
			Object basicProjectConfigId = latestRelease.getProjectFilter().getBasicProjectConfigId();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			List<JiraIssue> totalDefects = (List<JiraIssue>) resultMap.get(TOTAL_DEFECT);
			if (CollectionUtils.isNotEmpty(totalDefects)) {
				List<JiraIssue> openDefects = totalDefects.stream()
						.filter(jiraIssue -> fieldMapping.getStoryFirstStatus().contains(jiraIssue.getStatus()))
						.collect(Collectors.toList());
				Map<String, Map<String, List<JiraIssue>>> rcaWiseList = getRCAWiseList(totalDefects, openDefects);
				List<IterationKpiValue> filterDataList = new ArrayList<>();
				log.info("ReleaseDefectCountByRCAServiceImpl -> rcaDataList ->  : {}", rcaWiseList);
				List<IterationKpiValue> sortedFilterDataList = new ArrayList<>();
				List<DataCount> dataCountListForAllRCA = new ArrayList<>();
				for (Map.Entry<String, Map<String, List<JiraIssue>>> entry : rcaWiseList.entrySet()) {
					Map<String, Integer> rcaWiseCountMap = new HashMap<>();
					Map<String, List<JiraIssue>> rcaData = entry.getValue();
					getRCAWiseCount(rcaData, rcaWiseCountMap);
					DataCount rcaDataCount = new DataCount();
					int rcaCount = rcaData.values().stream().mapToInt(List::size).sum();
					rcaDataCount.setData(String.valueOf(rcaCount));
					rcaDataCount.setValue(rcaWiseCountMap);
					List<DataCount> dataCountList = new ArrayList<>();
					dataCountList.add(rcaDataCount);
					dataCountListForAllRCA.add(rcaDataCount);

					List<DataCount> middleTrendValueListForRCA = new ArrayList<>();
					DataCount middleOverallData = new DataCount();
					middleOverallData.setData(latestRelease.getProjectFilter().getName());
					middleOverallData.setValue(dataCountList);
					middleTrendValueListForRCA.add(middleOverallData);

					IterationKpiValue filterData = new IterationKpiValue(entry.getKey(), middleTrendValueListForRCA);
					filterDataList.add(filterData);
				}

				Map<String, Integer> overallRCACountMapAggregate = new HashMap<>();
				overallRCACountMap(dataCountListForAllRCA, overallRCACountMapAggregate);
				if (MapUtils.isNotEmpty(overallRCACountMapAggregate)) {
					populateExcelDataObject(requestTrackerId, excelData, totalDefects, fieldMapping);
					// filterDataList
					kpiElement.setSprint(latestRelease.getName());
					kpiElement.setModalHeads(KPIExcelColumn.DEFECT_COUNT_BY_RCA_RELEASE.getColumns());
					kpiElement.setExcelColumns(KPIExcelColumn.DEFECT_COUNT_BY_RCA_RELEASE.getColumns());
					kpiElement.setExcelData(excelData);
					sortedFilterDataList.add(filterDataList.stream()
							.filter(iterationKpiValue -> iterationKpiValue.getFilter1().equalsIgnoreCase(OPEN_DEFECT))
							.findFirst().orElse(new IterationKpiValue()));
					filterDataList.removeIf(
							iterationKpiValue -> iterationKpiValue.getFilter1().equalsIgnoreCase(OPEN_DEFECT));
					sortListByKey(filterDataList);
					sortedFilterDataList.addAll(filterDataList);
					kpiElement.setTrendValueList(sortedFilterDataList);

					log.info("ReleaseDefectCountByRCAServiceImpl -> request id : {} total jira Issues : {}",
							requestTrackerId, filterDataList.get(0));
				}
			}
		}
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);
		if (null != leafNode) {
			log.info("Defect count by RCA Release -> Requested sprint : {}", leafNode.getName());
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(leafNode.getProjectFilter().getBasicProjectConfigId());

			if (null != fieldMapping) {
				List<JiraIssue> releaseDefects = getFilteredReleaseJiraIssuesFromBaseClass(fieldMapping);
				resultListMap.put(TOTAL_DEFECT, releaseDefects);
			}
		}
		return resultListMap;
	}

	/**
	 * create root cause wise map of total and open defects
	 * 
	 * @param defectJiraIssueList
	 * @param openIssues
	 * @return
	 */
	private Map<String, Map<String, List<JiraIssue>>> getRCAWiseList(List<JiraIssue> defectJiraIssueList,
			List<JiraIssue> openIssues) {
		Map<String, Map<String, List<JiraIssue>>> scopeWiseDefectsMap = new HashMap<>();
		Collector<JiraIssue, ?, Map<String, List<JiraIssue>>> groupingByRootCause = Collectors
				.groupingBy(jiraIssue -> jiraIssue.getRootCauseList().get(0));
		Predicate<JiraIssue> hasNonEmptyRootCauseList = jiraIssue -> {
			if (CollectionUtils.isEmpty(jiraIssue.getRootCauseList())) {
				jiraIssue.setRootCauseList(Arrays.asList("-"));
			}
			return true;
		};
		scopeWiseDefectsMap.put(TOTAL_DEFECT,
				defectJiraIssueList.stream().filter(hasNonEmptyRootCauseList).collect(groupingByRootCause));
		scopeWiseDefectsMap.put(OPEN_DEFECT,
				openIssues.stream().filter(hasNonEmptyRootCauseList).collect(groupingByRootCause));
		return scopeWiseDefectsMap;
	}

	/**
	 * create rca wise jira issue map
	 * 
	 * @param rcaData
	 * @param rcaCountMap
	 */
	private static void getRCAWiseCount(Map<String, List<JiraIssue>> rcaData, Map<String, Integer> rcaCountMap) {
		for (Map.Entry<String, List<JiraIssue>> rcaEntry : rcaData.entrySet()) {
			String rca = rcaEntry.getKey();
			List<JiraIssue> issues = rcaEntry.getValue();
			rcaCountMap.put(rca, issues.size());
		}
	}

	/**
	 * create map of data count by filter
	 * 
	 * @param dataCountListForAllRCA
	 * @param overallRCACountMapAggregate
	 */
	private static void overallRCACountMap(List<DataCount> dataCountListForAllRCA,
			Map<String, Integer> overallRCACountMapAggregate) {
		for (DataCount dataCount : dataCountListForAllRCA) {
			Map<String, Integer> statusCountMap = (Map<String, Integer>) dataCount.getValue();
			statusCountMap.forEach(
					(rca, rcaCountValue) -> overallRCACountMapAggregate.merge(rca, rcaCountValue, Integer::sum));
		}
	}

	/**
	 * populate excel data
	 * 
	 * @param requestTrackerId
	 * @param excelData
	 * @param jiraIssueList
	 * @param fieldMapping
	 */
	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			List<JiraIssue> jiraIssueList, FieldMapping fieldMapping) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& CollectionUtils.isNotEmpty(jiraIssueList)) {
			KPIExcelUtility.populateReleaseDefectRelatedExcelData(jiraIssueList, excelData, fieldMapping);
		}
	}

	private void sortListByKey(List<IterationKpiValue> list) {
		list.sort(Comparator.comparing(IterationKpiValue::getFilter1));
	}
}