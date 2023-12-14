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

package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectRelease;
import com.publicissapient.kpidashboard.common.model.application.ProjectVersion;
import com.publicissapient.kpidashboard.common.repository.application.ProjectReleaseRepo;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProjectVersionServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String PROJECT_RELEASE_DETAIL = "projectReleaseDetail";

	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private ProjectReleaseRepo projectReleaseRepo;

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		List<DataCount> trendValueList = Lists.newArrayList();
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		treeAggregatorDetail.getMapOfListOfProjectNodes().forEach((k, v) -> {
			Filters filters = Filters.getFilter(k);
			if (Filters.PROJECT == filters) {
				projectWiseLeafNodeValue(mapTmp, v, trendValueList, kpiElement, getRequestTrackerId(), kpiRequest);
			}

		});

		log.debug("[PROJECT-RELEASE-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.PROJECT_RELEASES);
		// 3rd change : remove code to set trendValuelist and call
		// getTrendValues method
		List<DataCount> trendValues = getTrendValues(kpiRequest, kpiElement, nodeWiseKPIValue, KPICode.PROJECT_RELEASES);
		kpiElement.setTrendValueList(trendValues);
		return kpiElement;
	}

	@Override
	public Double calculateKPIMetrics(Map<String, Object> subCategoryMap) {
		return null;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {

		Map<String, Object> resultListMap = new HashMap<>();
		List<ObjectId> basicProjectConfigIds = new ArrayList<>();

		leafNodeList.forEach(leaf -> basicProjectConfigIds.add(leaf.getProjectFilter().getBasicProjectConfigId()));
		resultListMap.put(PROJECT_RELEASE_DETAIL, projectReleaseRepo.findByConfigIdIn(basicProjectConfigIds));
		return resultListMap;
	}

	@Override
	public String getQualifierType() {
		return KPICode.PROJECT_RELEASES.name();
	}

	/**
	 * Calculate KPI value for selected project nodes.
	 *
	 * @param projectLeafNodeList
	 *            list of sprint leaf nodes
	 * @param trendValueList
	 *            list containing data to show on KPI
	 * @param kpiElement
	 *            kpiElement
	 * @param kpiRequest
	 *            KpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> projectLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, String requestTrackerId, KpiRequest kpiRequest) {

		Map<String, Object> resultMap = fetchKPIDataFromDb(projectLeafNodeList, null, null, kpiRequest);
		Map<String, ProjectRelease> filterWiseDataMap = createProjectWiseRelease(
				(List<ProjectRelease>) resultMap.get(PROJECT_RELEASE_DETAIL));
		List<KPIExcelData> excelData = new ArrayList<>();
		projectLeafNodeList.forEach(node -> {
			String currentProjectId = node.getProjectFilter().getBasicProjectConfigId().toString();
			String projectName = node.getProjectFilter().getName();

			ProjectRelease releaseDetail = filterWiseDataMap.get(currentProjectId);
			if (releaseDetail != null) {
				setProjectNodeValue(mapTmp, node, releaseDetail, trendValueList, projectName, requestTrackerId,
						excelData);
			}

		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.RELEASE_FREQUENCY.getColumns());
	}

	/**
	 * Gets the KPI value for project node.
	 *
	 * @param kpiElement
	 * @param projectRelease
	 * @param trendValueList
	 * @param projectName
	 * @return
	 */
	private void setProjectNodeValue(Map<String, Node> mapTmp, Node node, ProjectRelease projectRelease,
			List<DataCount> trendValueList, String projectName, String requestTrackerId, List<KPIExcelData> excelData) {
		Map<String, Double> dateCount = getLastNMonth(customApiConfig.getSprintCountForFilters());
		List<ProjectVersion> projectVersionList = Lists.newArrayList();
		List<String> dateList = Lists.newArrayList();

		for (ProjectVersion pv : projectRelease.getListProjectVersion()) {
			if (pv.getReleaseDate() != null) {
				String yearMonth = pv.getReleaseDate().getYear() + Constant.DASH + pv.getReleaseDate().getMonthOfYear();
				if (dateCount.keySet().contains(yearMonth)) {
					projectVersionList.add(pv);
					dateList.add(yearMonth);
					dateCount.put(yearMonth, dateCount.get(yearMonth) + 1);
				}
			}
		}
		List<DataCount> dcList = new ArrayList<>();
		dateCount.forEach((k, v) -> setDataCount(trendValueList, projectName, dcList, k, v));
		mapTmp.get(node.getId()).setValue(dcList);

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			KPIExcelUtility.populateReleaseFreqExcelData(projectVersionList, projectName, excelData);
		}

	}

	/**
	 * @param trendValueList
	 * @param projectName
	 * @param dcList
	 * @param k
	 * @param v
	 */
	private void setDataCount(List<DataCount> trendValueList, String projectName, List<DataCount> dcList, String k,
			Double v) {
		DataCount dataCount = new DataCount();
		dataCount.setDate(k);
		dataCount.setValue(v);
		dataCount.setData(v.toString());
		dataCount.setHoverValue(new HashMap<>());
		dataCount.setSProjectName(projectName);
		dcList.add(dataCount);
		trendValueList.add(dataCount);
	}

	/**
	 * Group list of data by project.
	 *
	 * @param resultList
	 * @return
	 */
	private Map<String, ProjectRelease> createProjectWiseRelease(List<ProjectRelease> resultList) {
		return resultList.stream().collect(Collectors.toMap(pr -> pr.getConfigId().toString(), data -> data));
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI73(), KPICode.PROJECT_RELEASES.getKpiId());
	}

}
