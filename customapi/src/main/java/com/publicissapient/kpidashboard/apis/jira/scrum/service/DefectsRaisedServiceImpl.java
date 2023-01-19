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

import java.util.*;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.model.*;

import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import org.apache.commons.collections.CollectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;

import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

@Component
public class DefectsRaisedServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefectsRaisedServiceImpl.class);

	private static final String SEARCH_BY_PRIORITY = "Filter by priority";
	public static final String UNCHECKED = "unchecked";
	private static final String OVERALL = "Overall";

	private static final String LINKED_DEFECTS = "Linked defects";
	private static final String UNLINKED_DEFECTS = "Unlinked defects";
	private static final String DEFECT_DENSITY = "Defect density";
	private static final String SEARCH_BY_STATUS = "Filter by Status";
	private static final String ADDED_ISSUES = "Added issue";

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private SprintRepository sprintRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private KpiHelperService kpiHelperService;

	@Autowired
	private CustomApiConfig customApiConfig;

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
		return KPICode.DEFECT_RAISED.name();
	}

	@Override
	public Double calculateKPIMetrics(Map<String, Object> subCategoryMap) {
		return null;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);
		if (null != leafNode) {
			LOGGER.info("Defect raised -> Requested sprint : {}", leafNode.getName());
			String basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId().toString();
			String sprintId = leafNode.getSprintFilter().getId();
			SprintDetails sprintDetails = sprintRepository.findBySprintID(sprintId);
			if (null != sprintDetails && null != sprintDetails.getAddedIssues()) {
				List<String> addedIssues = new ArrayList<>(sprintDetails.getAddedIssues());
				if (CollectionUtils.isNotEmpty(addedIssues)) {
					List<JiraIssue> issueList = jiraIssueRepository
							.findByNumberInAndBasicProjectConfigId(new ArrayList<>(addedIssues), basicProjectConfigId);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails, new HashSet<>(),
									issueList);
					resultListMap.put(ADDED_ISSUES, new ArrayList<>(filtersIssuesList));
				}
			}
		}
		return resultListMap;
	}

	/**
	 * Populates KPI value to sprint leaf nodes and gives the trend analysis at
	 * sprint level.
	 *
	 * @param trendValue
	 * @param sprintLeafNodeList
	 * @param kpiElement
	 * @param kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(List<Node> sprintLeafNodeList, DataCount trendValue, KpiElement kpiElement,
			KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();

		sprintLeafNodeList.sort(Comparator.comparing(node -> node.getSprintFilter().getStartDate()));
		List<Node> latestSprintNode = new ArrayList<>();
		Node latestSprint = sprintLeafNodeList.get(0);

		Optional.ofNullable(latestSprint).ifPresent(latestSprintNode::add);

		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);
		List<JiraIssue> allIssues = (List<JiraIssue>) resultMap.get(ADDED_ISSUES);
		if (CollectionUtils.isNotEmpty(allIssues)) {
			LOGGER.info("Defect raised -> request id : {} total jira Issues : {}", requestTrackerId, allIssues.size());

			Map<String, Map<String, List<JiraIssue>>> priorityAndStatusWiseIssues = allIssues.stream().collect(
					Collectors.groupingBy(JiraIssue::getPriority, Collectors.groupingBy(JiraIssue::getStatus)));

			Set<String> priorities = new HashSet<>();
			Set<String> statuses = new HashSet<>();
			List<JiraIssue> tempLinkedDefect = allIssues.stream()
					.filter(ai -> CollectionUtils.isNotEmpty(ai.getDefectStoryID())).collect(Collectors.toList());

			double storyPointsSum = tempLinkedDefect.stream().filter(tld -> tld.getStoryPoints() != null)
					.mapToDouble(JiraIssue::getStoryPoints).sum();
			double overAllDefectDensity = storyPointsSum == 0 ? 0 : tempLinkedDefect.size() / storyPointsSum;

			List<Double> overAllLinkedDefects = Arrays.asList(0.0);
			List<Double> overAllUnlinkedDefects = Arrays.asList(0.0);
			List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
			List<IterationKpiModalValue> overAllUnlinkedmodalValues = new ArrayList<>();
			List<IterationKpiModalValue> overAlllinkedmodalValues = new ArrayList<>();
			List<IterationKpiModalValue> overAllModalValues = new ArrayList<>();

			priorityAndStatusWiseIssues
					.forEach((priority, statusWiseIssue) -> statusWiseIssue.forEach((status, issues) -> {
						priorities.add(priority);
						statuses.add(status);

						List<IterationKpiModalValue> linkedModalValues = new ArrayList<>();
						List<IterationKpiModalValue> unLinkedModalValues = new ArrayList<>();
						List<IterationKpiModalValue> modalValues = new ArrayList<>();

						List<JiraIssue> linkedDefect = new ArrayList<>();

						List<JiraIssue> unlinkedDefect = new ArrayList<>();

						for (JiraIssue jiraIssue : issues) {

							checkDefectType(overAllUnlinkedmodalValues, overAlllinkedmodalValues, linkedModalValues,
									unLinkedModalValues, linkedDefect, unlinkedDefect, jiraIssue);
							populateIterationData(overAllModalValues, modalValues, jiraIssue);

						}
						double storyPoints = linkedDefect.stream().filter(tld -> tld.getStoryPoints() != null)
								.mapToDouble(JiraIssue::getStoryPoints).sum();
						double defectDensity = storyPoints == 0 ? 0 : linkedDefect.size() / storyPoints;

						overAllLinkedDefects.set(0, overAllLinkedDefects.get(0) + linkedDefect.size());
						overAllUnlinkedDefects.set(0, overAllUnlinkedDefects.get(0) + unlinkedDefect.size());
						List<IterationKpiData> data = new ArrayList<>();
						IterationKpiData dd = new IterationKpiData(DEFECT_DENSITY, defectDensity, null, null, "",
								modalValues);
						IterationKpiData ud = new IterationKpiData(UNLINKED_DEFECTS, (double) unlinkedDefect.size(),
								null, null, null, unLinkedModalValues);

						IterationKpiData ld = new IterationKpiData(LINKED_DEFECTS, (double) linkedDefect.size(), null,
								null, null, linkedModalValues);
						data.add(dd);
						data.add(ud);
						data.add(ld);

						IterationKpiValue iterationKpiValue = new IterationKpiValue(priority, status, data);
						iterationKpiValues.add(iterationKpiValue);
					}));

			List<IterationKpiData> data = new ArrayList<>();
			IterationKpiData overAllDD = new IterationKpiData(DEFECT_DENSITY, overAllDefectDensity, null, null, "",
					overAllModalValues);
			IterationKpiData overAllLD = new IterationKpiData(LINKED_DEFECTS, overAllLinkedDefects.get(0), null, null,
					null, overAlllinkedmodalValues);
			IterationKpiData overAllUD = new IterationKpiData(UNLINKED_DEFECTS, overAllUnlinkedDefects.get(0), null,
					null, null, overAllUnlinkedmodalValues);
			data.add(overAllDD);
			data.add(overAllUD);
			data.add(overAllLD);
			IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, OVERALL, data);
			iterationKpiValues.add(overAllIterationKpiValue);

			// Create kpi level filters
			IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_PRIORITY, priorities);
			IterationKpiFiltersOptions filter2 = new IterationKpiFiltersOptions(SEARCH_BY_STATUS, statuses);
			IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, filter2);

			trendValue.setValue(iterationKpiValues);
			kpiElement.setFilters(iterationKpiFilters);
			if (latestSprint != null) {
				kpiElement.setSprint(latestSprint.getName());
			}
			kpiElement.setModalHeads(KPIExcelColumn.DEFECT_RAISED.getColumns());
			kpiElement.setTrendValueList(trendValue);
		}
	}

	private void checkDefectType(List<IterationKpiModalValue> overAllUnlinkedmodalValues,
			List<IterationKpiModalValue> overAlllinkedmodalValues, List<IterationKpiModalValue> linkedModalValues,
			List<IterationKpiModalValue> unLinkedModalValues, List<JiraIssue> linkedDefect,
			List<JiraIssue> unlinkedDefect, JiraIssue jiraIssue) {
		if (CollectionUtils.isNotEmpty(jiraIssue.getDefectStoryID())) {
			linkedDefect.add(jiraIssue);
			populateIterationData(overAlllinkedmodalValues, linkedModalValues, jiraIssue);

		} else {
			unlinkedDefect.add(jiraIssue);
			populateIterationData(overAllUnlinkedmodalValues, unLinkedModalValues, jiraIssue);

		}
	}
}
