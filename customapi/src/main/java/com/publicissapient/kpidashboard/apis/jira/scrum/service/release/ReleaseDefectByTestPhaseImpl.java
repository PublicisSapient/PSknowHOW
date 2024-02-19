/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.jira.scrum.service.release;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
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

/**
 * This class displays the defects that are part of the release and the phase in
 * which the defect is raised
 *
 * @author eswbogol
 */
@Slf4j
@Component
public class ReleaseDefectByTestPhaseImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	private static final String TOTAL_ISSUES = "totalIssues";
	private static final String DEFECTS_COUNT = "Defects Count";
	private static final String OPEN_DEFECTS = "Open Defects";
	private static final String TOTAL_DEFECTS = "Total Defects";
	private static final String UNDEFINED = "Undefined";

	@Autowired
	ConfigHelperService configHelperService;

	/**
	 *
	 * @param kpiRequest
	 *            kpiRequest with request details
	 * @param kpiElement
	 *            basic details of KPI
	 * @param treeAggregatorDetail
	 *            details of project nodes
	 * @return KpiElement with data
	 * @throws ApplicationException
	 *             exception while processing request
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.RELEASE) {
				releaseWiseLeafNodeValue(v, kpiElement, kpiRequest);
			}
		});
		log.info("ReleaseDefectByTestPhaseImpl -> getKpiData ->  : {}", kpiElement);
		return kpiElement;
	}

	@SuppressWarnings("unchecked")
	private void releaseWiseLeafNodeValue(List<Node> releaseLeafNodeList, KpiElement kpiElement,
			KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();
		List<KPIExcelData> excelData = new ArrayList<>();
		List<Node> latestReleaseNode = new ArrayList<>();
		Node latestRelease = releaseLeafNodeList.get(0);

		if (latestRelease != null) {
			Optional.of(latestRelease).ifPresent(latestReleaseNode::add);
			Map<String, Object> resultMap = fetchKPIDataFromDb(latestReleaseNode, null, null, kpiRequest);
			List<JiraIssue> releaseIssues = (List<JiraIssue>) resultMap.get(TOTAL_ISSUES);
			List<IterationKpiValue> filterDataList = new ArrayList<>();
			Node leafNode = latestReleaseNode.stream().findFirst().orElse(null);
			if (CollectionUtils.isNotEmpty(releaseIssues) && leafNode != null) {
				FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
						.get(leafNode.getProjectFilter().getBasicProjectConfigId());
				populateExcelDataObject(requestTrackerId, excelData, releaseIssues);
				createExcelDataAndTrendValueList(kpiElement, filterDataList, releaseIssues, fieldMapping, excelData,
						latestRelease);
			}
			kpiElement.setTrendValueList(filterDataList);
		}
	}

	/**
	 *
	 * @param leafNodeList
	 *            project node details
	 * @param startDate
	 *            startDate
	 * @param endDate
	 *            endDate
	 * @param kpiRequest
	 *            kpiRequest with request details
	 * @return JiraIssues with test phases
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);
		if (null != leafNode) {
			log.info("ReleaseDefectByTestPhaseImpl -> Requested sprint : {}", leafNode.getName());
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(leafNode.getProjectFilter().getBasicProjectConfigId());
			List<JiraIssue> releaseIssues = getFilteredReleaseJiraIssuesFromBaseClass(fieldMapping);
			resultListMap.put(TOTAL_ISSUES, releaseIssues);
		}
		return resultListMap;
	}

	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			List<JiraIssue> jiraIssueList) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& CollectionUtils.isNotEmpty(jiraIssueList)) {
			KPIExcelUtility.populateReleaseDefectWithTestPhasesRelatedExcelData(jiraIssueList, excelData);
		}
	}

	private void createExcelDataAndTrendValueList(KpiElement kpiElement, List<IterationKpiValue> iterationKpiValueList,
			List<JiraIssue> releaseIssues, FieldMapping fieldMapping, List<KPIExcelData> excelData,
			Node latestRelease) {
		if (CollectionUtils.isNotEmpty(releaseIssues)) {
			List<DataCount> openDefectsDataCountList = new ArrayList<>();
			List<DataCount> totalDefectsDataCountList = new ArrayList<>();
			List<JiraIssue> openDefectsList;
			openDefectsList = releaseIssues.stream()
					.filter(jiraIssue -> StringUtils.isNotEmpty(jiraIssue.getStatus())
							&& CollectionUtils.isNotEmpty(fieldMapping.getJiraDodKPI163())
							&& !fieldMapping.getJiraDodKPI163().contains(jiraIssue.getStatus()))
					.collect(Collectors.toList());
			setDefectList(OPEN_DEFECTS, openDefectsDataCountList, openDefectsList, iterationKpiValueList);
			setDefectList(TOTAL_DEFECTS, totalDefectsDataCountList, releaseIssues, iterationKpiValueList);
			kpiElement.setSprint(latestRelease.getName());
			kpiElement.setModalHeads(KPIExcelColumn.RELEASE_DEFECT_BY_TEST_PHASE.getColumns());
			kpiElement.setExcelColumns(KPIExcelColumn.RELEASE_DEFECT_BY_TEST_PHASE.getColumns());
			kpiElement.setExcelData(excelData);
		}
	}

	private void setDefectList(String defects, List<DataCount> defectsDataCountList, List<JiraIssue> defectsList,
			List<IterationKpiValue> iterationKpiValueList) {
		IterationKpiValue kpiValueIssueCount = new IterationKpiValue();
		kpiValueIssueCount.setFilter1(defects);
		if (CollectionUtils.isNotEmpty(defectsList)) {
			defectsDataCountList.add(getStatusWiseCountList(defectsList));
			kpiValueIssueCount.setValue(defectsDataCountList);
		}
		iterationKpiValueList.add(kpiValueIssueCount);
	}

	private DataCount getStatusWiseCountList(List<JiraIssue> jiraIssueList) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(jiraIssueList.size()));
		dataCount.setKpiGroup(DEFECTS_COUNT);
		dataCount.setDrillDown(null);
		Map<String, Long> countByTestPhase = jiraIssueList.stream().collect(
				Collectors.groupingBy(jiraIssue -> CollectionUtils.isNotEmpty(jiraIssue.getEscapedDefectGroup())
						? jiraIssue.getEscapedDefectGroup().stream().findFirst().orElse(UNDEFINED)
						: UNDEFINED, Collectors.counting()));
		Set<DataCount> dataCountList = countByTestPhase.entrySet().stream()
				.map(entry -> new DataCount(entry.getKey(), entry.getValue().doubleValue(), null))
				.collect(Collectors.toSet());
		dataCount.setValue(dataCountList);
		return dataCount;
	}

	@Override
	public Integer calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

	@Override
	public String getQualifierType() {
		return KPICode.RELEASE_DEFECT_BY_TEST_PHASE.name();
	}
}
