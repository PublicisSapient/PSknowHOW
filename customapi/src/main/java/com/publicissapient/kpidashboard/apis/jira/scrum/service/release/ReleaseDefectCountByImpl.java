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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.CommonServiceImpl;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.releasedashboard.JiraReleaseKPIService;
import com.publicissapient.kpidashboard.apis.model.Filter;
import com.publicissapient.kpidashboard.apis.model.FilterGroup;
import com.publicissapient.kpidashboard.apis.model.IssueKpiModalValue;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ReleaseDefectCountByImpl extends JiraReleaseKPIService {

	private static final String TOTAL_DEFECT = "totalDefects";
	private static final String SINGLE = "Single";

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private CommonServiceImpl commonService;

	@Override
	public String getQualifierType() {
		return KPICode.DEFECT_COUNT_BY.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node releaseNode)
			throws ApplicationException {
		releaseWiseLeafNodeValue(releaseNode, kpiElement, kpiRequest);
		log.info("DefectCountBy -> getKpiData ->  : {}", kpiElement);
		return kpiElement;
	}

	/**
	 * @param latestRelease
	 *          latestRelease
	 * @param kpiElement
	 *          kpiElement
	 * @param kpiRequest
	 *          kpiRequest
	 */
	private void releaseWiseLeafNodeValue(Node latestRelease, KpiElement kpiElement, KpiRequest kpiRequest) {
		if (latestRelease != null) {
			String requestTrackerId = getRequestTrackerId();
			Map<String, Object> resultMap = fetchKPIDataFromDb(latestRelease, null, null, kpiRequest);
			List<JiraIssue> totalDefects = (List<JiraIssue>) resultMap.get(TOTAL_DEFECT);
			List<KPIExcelData> excelData = new ArrayList<>();

			if (CollectionUtils.isNotEmpty(totalDefects)) {
				Object basicProjectConfigId = latestRelease.getProjectFilter().getBasicProjectConfigId();
				FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
				Map<String, IssueKpiModalValue> issueKpiModalObject = KpiDataHelper.createMapOfIssueModal(totalDefects);
				totalDefects.forEach(defect -> KPIExcelUtility.populateIssueModal(defect, fieldMapping, issueKpiModalObject));

				if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
					KPIExcelUtility.populateReleaseDefectRelatedExcelData(totalDefects, excelData, fieldMapping);
				}

				log.info("ReleaseDefectCountBy {}", totalDefects);
				kpiElement.setSprint(latestRelease.getName());
				kpiElement.setModalHeads(KPIExcelColumn.DEFECT_COUNT_BY.getColumns());
				kpiElement.setExcelColumns(KPIExcelColumn.DEFECT_COUNT_BY_EXPORT.getColumns());
				kpiElement.setExcelData(excelData);
				kpiElement.setIssueData(new HashSet<>(issueKpiModalObject.values()));
				kpiElement.setFilterGroup(createFilterGroup());
			}
		}
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(Node leafNode, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		if (null != leafNode) {
			log.info("Defect count by -> Requested release : {}", leafNode.getName());
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(leafNode.getProjectFilter().getBasicProjectConfigId());
			if (null != fieldMapping) {
				List<JiraIssue> releaseDefects = getFilteredReleaseJiraIssuesFromBaseClass(fieldMapping);
				resultListMap.put(TOTAL_DEFECT, releaseDefects);
			}
		}
		return resultListMap;
	}

	private FilterGroup createFilterGroup() {
		FilterGroup filterGroup = new FilterGroup();
		// for the first group by selection
		List<Filter> filterList = new ArrayList<>();
		filterList.add(createFilter(SINGLE, "Status", "Issue Status", 1));
		filterList.add(createFilter(SINGLE, "Priority", "Priority", 2));
		filterList.add(createFilter("Multi", "RCA", "Root Cause List", 3));
		filterGroup.setFilterGroup1(filterList);
		// for the additional filter selection
		List<Filter> filterGroupList2 = new ArrayList<>();
		filterGroupList2.add(createFilter(SINGLE, "Assignee", "Assignee", 1));
		filterGroupList2.add(createFilter("Multi", "Testing Phase", "Testing Phase", 2));
		filterGroup.setFilterGroup2(filterGroupList2);
		return filterGroup;
	}

	private Filter createFilter(String type, String name, String key, Integer order) {
		Filter filter = new Filter();
		filter.setFilterType(type);
		filter.setFilterName(name);
		filter.setFilterKey(key);
		filter.setOrder(order);
		return filter;
	}
}
