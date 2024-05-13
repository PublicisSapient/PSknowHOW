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
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.releasedashboard.JiraReleaseKPIService;
import com.publicissapient.kpidashboard.apis.model.Filter;
import com.publicissapient.kpidashboard.apis.model.IssueKpiModalValue;
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
	 *            latestRelease
	 * @param kpiElement
	 *            kpiElement
	 * @param kpiRequest
	 *            kpiRequest
	 */
	private void releaseWiseLeafNodeValue(Node latestRelease, KpiElement kpiElement, KpiRequest kpiRequest) {
		if (latestRelease != null) {
			Map<String, Object> resultMap = fetchKPIDataFromDb(latestRelease, null, null, kpiRequest);
			List<JiraIssue> totalDefects = (List<JiraIssue>) resultMap.get(TOTAL_DEFECT);

			if (CollectionUtils.isNotEmpty(totalDefects)) {
				Object basicProjectConfigId = latestRelease.getProjectFilter().getBasicProjectConfigId();
				FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
				Map<String, IssueKpiModalValue> issueKpiModalObject = KpiDataHelper.createMapOfIssueModal(totalDefects);
				totalDefects.forEach(
						defect -> KPIExcelUtility.populateIssueModal(defect, fieldMapping, issueKpiModalObject));

				log.info("ReleaseDefectCountBy {}", totalDefects);
				kpiElement.setSprint(latestRelease.getName());
				kpiElement.setModalHeads(KPIExcelColumn.DEFECT_COUNT_BY.getColumns());
				kpiElement.setIssueData(new HashSet<>(issueKpiModalObject.values()));
				kpiElement.setPieChartFilter(createFilter());

			}
		}

	}

	private Map<String, List<Filter>> createFilter() {
		Map<String, List<Filter>> mainFilter = new HashMap<>();
		List<Filter> filterList = new ArrayList<>();
		Filter status = new Filter();
		status.setFilterType("Single");
		status.setFilterName("Status");
		status.setFilterKey("Issue Status");
		status.setOrder(1);
		filterList.add(status);

		Filter priority = new Filter();
		priority.setFilterType("Single");
		priority.setFilterName("Priority");
		priority.setFilterKey("Priority");
		priority.setOrder(2);
		filterList.add(priority);

		Filter rca = new Filter();
		rca.setFilterType("Multi");
		rca.setFilterName("RCA");
		rca.setFilterKey("Root Cause List");
		rca.setOrder(3);
		filterList.add(rca);
		mainFilter.put("mainFilter", filterList);
		return mainFilter;
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

}