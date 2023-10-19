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

package com.publicissapient.kpidashboard.apis.jira.kanban.service;

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
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.ProjectRelease;
import com.publicissapient.kpidashboard.common.model.application.ProjectVersion;
import com.publicissapient.kpidashboard.common.repository.application.ProjectReleaseRepo;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProjectVersionKanbanServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String PROJECT_RELEASE_DETAIL = "projectReleaseDetail";
	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private ProjectReleaseRepo projectReleaseRepo;

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
		return KPICode.PROJECT_RELEASES_KANBAN.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		log.info("[PROJECT-RELEASE-KANBAN-LEAF-NODE-VALUE][{}]", kpiRequest.getRequestTrackerId());
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		List<KPIExcelData> excelData = new ArrayList<>();
		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes()
				.get(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT);

		projectWiseLeafNodeValue(mapTmp, projectList, kpiElement, kpiRequest, excelData);
		log.debug("[PROJECT-RELEASE-KANBAN-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.PROJECT_RELEASES_KANBAN);
		// 3rd change : remove code to set trendValuelist and call getTrendValues method
		List<DataCount> trendValues = getTrendValues(kpiRequest, kpiElement, nodeWiseKPIValue, KPICode.PROJECT_RELEASES_KANBAN);
		kpiElement.setTrendValueList(trendValues);
		return kpiElement;
	}

	/**
	 * Calculate KPI value for selected project nodes.
	 *
	 * @param leafNodeList
	 *            list of sprint leaf nodes
	 * @param mapTmp
	 *            map containing data to show on KPI
	 * @param kpiElement
	 *            kpiElement
	 * @param kpiRequest
	 *            KpiRequest
	 */
	private void projectWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> leafNodeList, KpiElement kpiElement,
			KpiRequest kpiRequest, List<KPIExcelData> excelData) {

		Map<String, Object> resultMap = fetchKPIDataFromDb(leafNodeList, null, null, kpiRequest);
		String requestTrackerId = getKanbanRequestTrackerId();

		Map<String, ProjectRelease> filterWiseDataMap = createProjectWiseRelease(
				(List<ProjectRelease>) resultMap.get(PROJECT_RELEASE_DETAIL));
		leafNodeList.forEach(node -> {
			String projectNodeId = node.getProjectFilter().getId();
			ProjectRelease projectRelease = filterWiseDataMap.get(projectNodeId);
			if (projectRelease != null) {
				String projectName = projectNodeId.substring(0, projectNodeId.lastIndexOf(CommonConstant.UNDERSCORE));
				Map<String, Double> dateCount = getLastNMonth(customApiConfig.getJiraXaxisMonthCount());
				List<DataCount> dc = new ArrayList<>();
				List<ProjectVersion> projectVersionList = Lists.newArrayList();
				for (ProjectVersion pv : projectRelease.getListProjectVersion()) {
					if (pv.getReleaseDate() != null && dateCount.keySet().contains(
							pv.getReleaseDate().getYear() + Constant.DASH + pv.getReleaseDate().getMonthOfYear())) {
						String yearMonth = pv.getReleaseDate().getYear() + Constant.DASH
								+ pv.getReleaseDate().getMonthOfYear();

						projectVersionList.add(pv);
						dateCount.put(yearMonth, dateCount.get(yearMonth) + 1);

					}
				}
				dateCount.forEach((k, v) -> {
					DataCount dataCount = new DataCount();
					dataCount.setDate(k);
					dataCount.setValue(v);
					dataCount.setData(v.toString());
					dataCount.setHoverValue(new HashMap<>());
					dataCount.setSProjectName(projectName);
					dc.add(dataCount);
				});
				if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
					KPIExcelUtility.populateReleaseFreqExcelData(projectVersionList, projectName, excelData);
				}
				mapTmp.get(node.getId()).setValue(dc);
			}
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.RELEASE_FREQUENCY.getColumns());

	}

	/**
	 * Group list of data by project.
	 *
	 * @param resultList
	 * @return
	 */
	private Map<String, ProjectRelease> createProjectWiseRelease(List<ProjectRelease> resultList) {
		return resultList.stream().collect(Collectors.toMap(ProjectRelease::getProjectId, data -> data));
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}
}
