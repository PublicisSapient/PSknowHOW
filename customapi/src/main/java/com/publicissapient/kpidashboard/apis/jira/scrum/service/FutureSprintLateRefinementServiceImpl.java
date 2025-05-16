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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.iterationdashboard.JiraIterationKPIService;
import com.publicissapient.kpidashboard.apis.jira.service.iterationdashboard.JiraIterationServiceR;
import com.publicissapient.kpidashboard.apis.model.IterationKpiData;
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.util.IterationKpiHelper;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FutureSprintLateRefinementServiceImpl extends JiraIterationKPIService {
	private static final String INCLUDED_ISSUES = "included issues";
	private static final String OVERALL = "Overall";
	private static final String TOTAL_STORIES = "Total Stories";
	private static final String UNREFINED_STORIES = "Un-Refined Stories";
	private static final String LATE_REFINEMENT = "Late Refinement";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private SprintRepository sprintRepository;
	@Autowired
	private JiraIssueRepository jiraIssueRepository;
	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	@Autowired
	private JiraIterationServiceR jiraIterationServiceR;

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node sprintNode)
			throws ApplicationException {
		DataCount trendValue = new DataCount();
		projectWiseLeafNodeValue(sprintNode, trendValue, kpiElement, kpiRequest);
		return kpiElement;
	}

	@Override
	public String getQualifierType() {
		return KPICode.FUTURE_SPRINT_LATE_REFINEMENT.name();
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(Node leafNode, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		if (null != leafNode) {
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(leafNode.getProjectFilter().getBasicProjectConfigId());
			// to modify sprintdetails on the basis of configuration for the project
			if (CollectionUtils.isNotEmpty(fieldMapping.getJiraIssueTypeNamesKPI188())) {
				log.info("Future Late Refinement -> Requested sprint : {}", leafNode.getName());
				SprintDetails activeSprint = getSprintDetailsFromBaseClass();
				// Future Sprint
				ObjectId basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId();
				List<SprintDetails> futureSprintList = sprintRepository
						.findByBasicProjectConfigIdAndStateIgnoreCaseOrderByStartDateASC(basicProjectConfigId,
								SprintDetails.SPRINT_STATE_FUTURE);

				// Find the next sprint
				SprintDetails sprintDetails = futureSprintList.stream()
						.filter(sprint -> sprint.getStartDate() != null
								&& DateUtil.stringToLocalDateTime(sprint.getStartDate(),DateUtil.TIME_FORMAT_WITH_SEC).isAfter(
								DateUtil.stringToLocalDateTime(activeSprint.getEndDate(),DateUtil.TIME_FORMAT_WITH_SEC)))
						.findFirst().orElse(null);

				if (sprintDetails == null) {
					return new HashMap<>();
				}

				Set<String> totalIssues = jiraIssueRepository.findBySprintID(sprintDetails.getSprintID()).stream()
						.filter(a -> getTypeNames(fieldMapping).contains(a.getTypeName().toLowerCase())).map(JiraIssue::getNumber).collect(Collectors.toSet());
				Map<String, Object> mapOfFilter = new HashMap<>();
				jiraIterationServiceR.createAdditionalFilterMap(kpiRequest, mapOfFilter);
				Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
				uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfFilter);

				List<JiraIssue> totalJiraIssueList = jiraIssueRepository
						.findIssueByNumberWithAdditionalFilter(totalIssues, uniqueProjectMap);
				// no need to transform sprintdetails as it already has a fieldmapping to filter
				// out the issues
				List<String> completeAndIncompleteIssues = KpiDataHelper
						.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails, CommonConstant.TOTAL_ISSUES);

				if (CollectionUtils.isNotEmpty(completeAndIncompleteIssues)) {
					List<JiraIssue> filteredJiraIssue = IterationKpiHelper
							.getFilteredJiraIssue(new ArrayList<>(completeAndIncompleteIssues), totalJiraIssueList);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails, new HashSet<>(),
									filteredJiraIssue);
					resultListMap.put(INCLUDED_ISSUES, new ArrayList<>(filtersIssuesList));
				}
			}
		}

		return resultListMap;
	}

	private static Set<String> getTypeNames(FieldMapping fieldMapping) {
		return fieldMapping.getJiraIssueTypeNamesKPI188().stream()
				.flatMap(name -> "Defect".equalsIgnoreCase(name)
						? Stream.of("defect", NormalizedJira.DEFECT_TYPE.getValue().toLowerCase())
						: Stream.of(name.trim().toLowerCase()))
				.collect(Collectors.toSet());
	}

	/**
	 * Populates KPI value to sprint leaf nodes and gives the trend analysis at
	 * sprint level.
	 * 
	 * @param sprintLeafNode
	 * @param trendValue
	 * @param kpiElement
	 * @param kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(Node sprintLeafNode, DataCount trendValue, KpiElement kpiElement,
			KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();

		Object basicProjectConfigId = sprintLeafNode.getProjectFilter().getBasicProjectConfigId();
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

		Map<String, Object> resultMap = fetchKPIDataFromDb(sprintLeafNode, null, null, kpiRequest);
		List<JiraIssue> totalIssues = (List<JiraIssue>) resultMap.get(INCLUDED_ISSUES);

		List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
		List<IterationKpiData> data = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(totalIssues)) {
			log.info("Scope Change -> request id : {} total jira Issues : {}", requestTrackerId, totalIssues.size());
			List<Integer> overAllStory = Arrays.asList(0);
			List<Integer> overallUnRefined = Arrays.asList(0);
			List<IterationKpiModalValue> overAllmodalValues = new ArrayList<>();
			List<IterationKpiModalValue> modalValues = new ArrayList<>();
			Map<String, IterationKpiModalValue> modalObjectMap = KpiDataHelper.createMapOfModalObject(totalIssues);
			for (JiraIssue jiraIssue : totalIssues) {
				KPIExcelUtility.populateIterationKPI(overAllmodalValues, modalValues, jiraIssue, fieldMapping,
						modalObjectMap);
				overAllStory.set(0, overAllStory.get(0) + 1);
				IterationKpiModalValue jiraIssueModalObject = modalObjectMap.get(jiraIssue.getNumber());
				jiraIssueModalObject.setUnRefined("N");
				if (CollectionUtils.isNotEmpty(jiraIssue.getUnRefinedValue188())) {
					overallUnRefined.set(0, overallUnRefined.get(0) + 1);
					jiraIssueModalObject.setUnRefined("Y");
				}
			}
			IterationKpiData overAllStories = new IterationKpiData(TOTAL_STORIES, Double.valueOf(overAllStory.get(0)),
					null, null, null, overAllmodalValues);

			IterationKpiData overallUnRefinedStories = new IterationKpiData(UNREFINED_STORIES,
					Double.valueOf(overallUnRefined.get(0)), null, null, null, null);

			IterationKpiData overallUnRefinedData = new IterationKpiData(LATE_REFINEMENT,
					calculateUnrefined(overallUnRefined.get(0), overAllStory.get(0)), null, null, Constant.PERCENTAGE,
					null);

			data.add(overAllStories);
			data.add(overallUnRefinedStories);
			data.add(overallUnRefinedData);
			IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, null, data);
			iterationKpiValues.add(overAllIterationKpiValue);

			trendValue.setValue(iterationKpiValues);
			kpiElement.setSprint(sprintLeafNode.getName());
			kpiElement.setModalHeads(KPIExcelColumn.FUTURE_SPRINT_LATE_REFINEMENT.getColumns());
		}

		kpiElement.setTrendValueList(trendValue);
	}

	private Double calculateUnrefined(Integer unRefined, Integer total) {
		return roundingOff((double) (unRefined * 100) / total);
	}

}
