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

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.CalculatePCDHelper;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.IterationKpiData;
import com.publicissapient.kpidashboard.apis.model.IterationKpiFilters;
import com.publicissapient.kpidashboard.apis.model.IterationKpiFiltersOptions;
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.IterationPotentialDelay;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class displays the stories, linked defects and unlinked defects that are
 * tagged to release, but have not yet been completed.
 *
 * @author eswbogol
 *
 */

@Slf4j
@Component
public class PendingReleaseScopeImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	public static final String UNCHECKED = "unchecked";
	public static final String STORIES = "Stories";
    public static final String LINKED_DEFECTS = "Linked Defects";
	private static final String SEARCH_BY_ISSUE_TYPE = "Filter by issue type";
	private static final String OVERALL = "Overall";
    private static final String ISSUES = "Issues";
	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private SprintRepository sprintRepository;

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		DataCount trendValue = new DataCount();
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {

			Filters filters = Filters.getFilter(k);
			if (Filters.SPRINT == filters) {
				projectWiseLeafNodeValue(v, trendValue, kpiElement, kpiRequest);
			}
		});
		return kpiElement;
	}

	@Override
	public String getQualifierType() {
		return KPICode.PENDING_RELEASE_SCOPE.name();
	}

	@Override
	public Integer calculateKPIMetrics(Map<String, Object> subCategoryMap) {
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
				List<JiraIssue> releaseIssues = getFilteredReleaseJiraIssuesFromBaseClass(fieldMapping);
				resultListMap.put(ISSUES, releaseIssues);
			}
		}
		return resultListMap;
	}

	/**
	 * Populates KPI value to sprint leaf nodes and gives the trend analysis at
	 * sprint level.
	 *
	 * @param sprintLeafNodeList
	 * @param trendValue
	 * @param kpiElement
	 * @param kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(List<Node> sprintLeafNodeList, DataCount trendValue, KpiElement kpiElement,
			KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();

		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));
		List<Node> latestSprintNode = new ArrayList<>();
		Node latestSprint = sprintLeafNodeList.get(0);
		Optional.ofNullable(latestSprint).ifPresent(latestSprintNode::add);

		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);
		List<JiraIssue> allIssues = (List<JiraIssue>) resultMap.get(ISSUES);
		if (CollectionUtils.isNotEmpty(allIssues)) {
			log.info("Estimate Vs Actual -> request id : {} total jira Issues : {}", requestTrackerId,
					allIssues.size());
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(latestSprint.getProjectFilter().getBasicProjectConfigId());
			// Creating map of modal Objects
			Map<String, IterationKpiModalValue> modalObjectMap = KpiDataHelper.createMapOfModalObject(allIssues);
			Map<String, List<JiraIssue>> typeWiseIssues = allIssues.stream()
					.collect(Collectors.groupingBy(JiraIssue::getTypeName));

			Set<String> issueTypes = new HashSet<>();
			List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
			List<Integer> overAllStoriesList = Collections.singletonList(0);
			List<Integer> overLinkedDefectsList = Collections.singletonList(0);
			List<IterationKpiModalValue> overAllmodalValues = new ArrayList<>();
			typeWiseIssues.forEach((issueType, issues) -> {
				issueTypes.add(issueType);
				List<IterationKpiModalValue> modalValues = new ArrayList<>();
				int origEstData = 0;
				int logWorkData = 0;
				List<IterationKpiData> data = new ArrayList<>();
                IterationKpiData stories = null;
                IterationKpiData linkedDefects = null;
                if (CollectionUtils.isNotEmpty(fieldMapping.getJiraReleaseIssueTypeKPI167())
						&& fieldMapping.getJiraReleaseIssueTypeKPI167().contains(issueType)) {
                    stories = new IterationKpiData(STORIES, (double) issues.size(), null, null, "",
                            modalValues);
                    overAllStoriesList.add(issues.size());
				}
                if (CollectionUtils.isNotEmpty(fieldMapping.getJiradefecttype())
                        && fieldMapping.getJiradefecttype().contains(issueType)) {
                    linkedDefects = new IterationKpiData(STORIES, (double) issues.size(), null, null,
                            "", modalValues);
                    overLinkedDefectsList.add(issues.size());
                }
				IterationKpiData unlinkedDefects = new IterationKpiData(STORIES, Double.valueOf(origEstData), null,
						null, "", modalValues);
				data.add(stories);
				data.add(linkedDefects);
				IterationKpiValue iterationKpiValue = new IterationKpiValue(issueType, null, data);
				iterationKpiValues.add(iterationKpiValue);

			});
			List<IterationKpiData> data = new ArrayList<>();

			IterationKpiData overAllorigEstimates = new IterationKpiData(STORIES,
					Double.valueOf(overAllStoriesList.get(0)), null, null, "", overAllmodalValues);
			IterationKpiData overAllloggedWork = new IterationKpiData(LINKED_DEFECTS,
					Double.valueOf(overLinkedDefectsList.get(0)), null, null, "", null);
			data.add(overAllorigEstimates);
			data.add(overAllloggedWork);
			IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, OVERALL, data);
			iterationKpiValues.add(overAllIterationKpiValue);

			// Create kpi level filters
			IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_ISSUE_TYPE, issueTypes);
			IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, null);
			trendValue.setValue(iterationKpiValues);
			kpiElement.setFilters(iterationKpiFilters);
			kpiElement.setSprint(latestSprint.getName());
			kpiElement.setModalHeads(KPIExcelColumn.ESTIMATE_VS_ACTUAL.getColumns());
			kpiElement.setTrendValueList(trendValue);
		}
	}
}
