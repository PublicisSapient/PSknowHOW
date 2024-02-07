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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
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
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ReleaseDefectCountByStatusServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	private static final String TOTAL_DEFECT = "totalDefects";

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private CommonServiceImpl commonService;

	private static void getStatusWiseCount(Map<String, List<JiraIssue>> statusData,
			Map<String, Integer> statusCountMap) {
		for (Map.Entry<String, List<JiraIssue>> statusEntry : statusData.entrySet()) {
			statusCountMap.put(statusEntry.getKey(), statusEntry.getValue().size());
		}
	}

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
			log.info("Defect count by Status Release -> Requested sprint : {}", leafNode.getName());
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(leafNode.getProjectFilter().getBasicProjectConfigId());
			if (null != fieldMapping) {
				List<JiraIssue> releaseDefects = getFilteredReleaseJiraIssuesFromBaseClass(fieldMapping);
				resultListMap.put(TOTAL_DEFECT, releaseDefects);
			}
		}
		return resultListMap;
	}

	@Override
	public String getQualifierType() {
		return KPICode.DEFECT_COUNT_BY_STATUS_RELEASE.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.RELEASE) {
				releaseWiseLeafNodeValue(v, kpiElement, kpiRequest);
			}
		});
		log.info("DefectCountByStatusServiceImpl -> getKpiData ->  : {}", kpiElement);
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
			List<JiraIssue> totalDefects = (List<JiraIssue>) resultMap.get(TOTAL_DEFECT);
			List<IterationKpiValue> filterDataList = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(totalDefects)) {
				Map<String, List<JiraIssue>> statusWiseList = getStatusWiseList(totalDefects);
				log.info("ReleaseDefectCountByStatusServiceImpl -> statusWiseList ->  : {}", statusWiseList);
				Map<String, Integer> statusCountMap = new HashMap<>();
				getStatusWiseCount(statusWiseList, statusCountMap);
				if (MapUtils.isNotEmpty(statusCountMap)) {
					Object basicProjectConfigId = latestRelease.getProjectFilter().getBasicProjectConfigId();
					FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

					List<DataCount> trendValueListOverAll = new ArrayList<>();
					DataCount overallData = new DataCount();
					int sumOfDefectsCount = statusCountMap.values().stream().mapToInt(Integer::intValue).sum();
					overallData.setData(String.valueOf(sumOfDefectsCount));
					overallData.setValue(statusCountMap);
					overallData.setKpiGroup(CommonConstant.OVERALL);
					overallData.setSProjectName(latestRelease.getProjectFilter().getName());
					trendValueListOverAll.add(overallData);

					List<DataCount> middleTrendValueListOverAll = new ArrayList<>();
					DataCount middleOverallData = new DataCount();
					middleOverallData.setData(latestRelease.getProjectFilter().getName());
					middleOverallData.setValue(trendValueListOverAll);
					middleTrendValueListOverAll.add(middleOverallData);
					populateExcelDataObject(requestTrackerId, excelData, totalDefects, fieldMapping);
					IterationKpiValue filterDataOverall = new IterationKpiValue(CommonConstant.OVERALL,
							middleTrendValueListOverAll);
					filterDataList.add(filterDataOverall);
					kpiElement.setSprint(latestRelease.getName());
					kpiElement.setModalHeads(KPIExcelColumn.DEFECT_COUNT_BY_STATUS_RELEASE.getColumns());
					kpiElement.setExcelColumns(KPIExcelColumn.DEFECT_COUNT_BY_STATUS_RELEASE.getColumns());
					kpiElement.setExcelData(excelData);
					log.info("ReleaseDefectCountByStatusServiceImpl -> request id : {} total jira Issues : {}",
							requestTrackerId, filterDataList.get(0));
				}
			}
			kpiElement.setTrendValueList(filterDataList);
		}
	}

	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			List<JiraIssue> jiraIssueList, FieldMapping fieldMapping) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& CollectionUtils.isNotEmpty(jiraIssueList)) {
			KPIExcelUtility.populateReleaseDefectRelatedExcelData(jiraIssueList, excelData, fieldMapping);
		}
	}

	private Map<String, List<JiraIssue>> getStatusWiseList(List<JiraIssue> defectJiraIssueList) {
		return defectJiraIssueList.stream().filter(jiraIssue -> {
			if (StringUtils.isEmpty(jiraIssue.getStatus())) {
				jiraIssue.setStatus("-");
			}
			return true;
		}).collect(Collectors.groupingBy(JiraIssue::getStatus));
	}
}