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
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.iterationdashboard.JiraIterationKPIService;
import com.publicissapient.kpidashboard.apis.model.*;
import com.publicissapient.kpidashboard.apis.util.IterationKpiHelper;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IterationCommitmentV2ServiceImpl extends JiraIterationKPIService {

	public static final String UNCHECKED = "unchecked";
	public static final String OVERALL_COMMITMENT = "Overall Commitment";
	private static final String PUNTED_ISSUES = "puntedIssues";
	private static final String ADDED_ISSUES = "addedIssues";
	private static final String EXCLUDE_ADDED_ISSUES = "excludeAddedIssues";
	private static final String SCOPE_ADDED = "Scope added";
	private static final String SCOPE_REMOVED = "Scope removed";
	private static final String INITIAL_COMMITMENT = "Initial Commitment";
	private static final String SINGLE = "Single";

	@Autowired
	private ConfigHelperService configHelperService;

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node sprintNode)
			throws ApplicationException {
		DataCount trendValue = new DataCount();
		projectWiseLeafNodeValue(sprintNode, kpiElement, kpiRequest);
		return kpiElement;
	}

	@Override
	public String getQualifierType() {
		return KPICode.ITERATION_COMMITMENT_V2.name();
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(Node leafNode, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		if (null != leafNode) {
			log.info("Scope Change -> Requested sprint : {}", leafNode.getName());
			SprintDetails dbSprintDetail = getSprintDetailsFromBaseClass();
			SprintDetails sprintDetails;
			if (null != dbSprintDetail) {
				FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
						.get(leafNode.getProjectFilter().getBasicProjectConfigId());
				// to modify sprintdetails on the basis of configuration for the project
				List<JiraIssueCustomHistory> totalHistoryList = getJiraIssuesCustomHistoryFromBaseClass();
				List<JiraIssue> totalJiraIssueList = getJiraIssuesFromBaseClass();
				Set<String> issueList = totalJiraIssueList.stream().map(JiraIssue::getNumber)
						.collect(Collectors.toSet());

				sprintDetails = IterationKpiHelper.transformIterSprintdetail(totalHistoryList, issueList, dbSprintDetail,
						fieldMapping.getJiraIterationIssuetypeKPI120(),
						fieldMapping.getJiraIterationCompletionStatusKPI120(),
						leafNode.getProjectFilter().getBasicProjectConfigId());

				List<String> puntedIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.PUNTED_ISSUES);
				Set<String> addedIssues = sprintDetails.getAddedIssues();
				List<String> completeAndIncompleteIssues = Stream
						.of(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
								CommonConstant.COMPLETED_ISSUES),
								KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
										CommonConstant.NOT_COMPLETED_ISSUES))
						.flatMap(Collection::stream).collect(Collectors.toList());
				// Adding issues which were added before sprint start and later removed form
				// sprint or dropped.
				completeAndIncompleteIssues.addAll(puntedIssues);
				if (CollectionUtils.isNotEmpty(puntedIssues)) {
					List<JiraIssue> filteredPuntedIssueList = IterationKpiHelper.getFilteredJiraIssue(puntedIssues, totalJiraIssueList);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getPuntedIssues(), filteredPuntedIssueList);
					resultListMap.put(PUNTED_ISSUES, new ArrayList<>(filtersIssuesList));
				}
				if (CollectionUtils.isNotEmpty(addedIssues)) {
					List<JiraIssue> filterAddedIssueList = IterationKpiHelper.getFilteredJiraIssue(new ArrayList<>(addedIssues),
							totalJiraIssueList);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails, new HashSet<>(),
									filterAddedIssueList);
					resultListMap.put(ADDED_ISSUES, new ArrayList<>(filtersIssuesList));
					completeAndIncompleteIssues.removeAll(new ArrayList<>(addedIssues));
				}
				if (CollectionUtils.isNotEmpty(completeAndIncompleteIssues)) {
					List<JiraIssue> filteredJiraIssue = IterationKpiHelper.getFilteredJiraIssue(
							new ArrayList<>(completeAndIncompleteIssues), totalJiraIssueList);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails, new HashSet<>(),
									filteredJiraIssue);
					resultListMap.put(EXCLUDE_ADDED_ISSUES, new ArrayList<>(filtersIssuesList));
				}
			}
		}
		return resultListMap;
	}

	/**
	 * Populates KPI value to sprint leaf nodes and gives the trend analysis at
	 * sprint level.
	 * 
	 * @param sprintLeafNode
	 * @param kpiElement
	 * @param kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(Node sprintLeafNode, KpiElement kpiElement, KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();

		Object basicProjectConfigId = sprintLeafNode.getProjectFilter().getBasicProjectConfigId();
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

		Map<String, Object> resultMap = fetchKPIDataFromDb(sprintLeafNode, null, null, kpiRequest);
		List<JiraIssue> puntedIssues = (List<JiraIssue>) resultMap.get(PUNTED_ISSUES);
		List<JiraIssue> addedIssues = (List<JiraIssue>) resultMap.get(ADDED_ISSUES);
		List<JiraIssue> initialIssues = (List<JiraIssue>) resultMap.get(EXCLUDE_ADDED_ISSUES);
		Set<IssueKpiModalValue> issueData = new HashSet<>();

		if (CollectionUtils.isNotEmpty(initialIssues)) {
			Map<String, IssueKpiModalValue> issueKpiModalObject = KpiDataHelper.createMapOfIssueModal(initialIssues);
			initialIssues.forEach(issue -> {
				KPIExcelUtility.populateIssueModal(issue, fieldMapping, issueKpiModalObject);
				IssueKpiModalValue data = issueKpiModalObject.get(issue.getNumber());
				setCategoryAndDataValue(issue, data, fieldMapping, INITIAL_COMMITMENT);
			});
			issueData.addAll(new HashSet<>(issueKpiModalObject.values()));
		}
		if (CollectionUtils.isNotEmpty(addedIssues)) {
			Map<String, IssueKpiModalValue> issueKpiModalObject = KpiDataHelper.createMapOfIssueModal(addedIssues);
			addedIssues.forEach(issue -> {
				KPIExcelUtility.populateIssueModal(issue, fieldMapping, issueKpiModalObject);
				IssueKpiModalValue data = issueKpiModalObject.get(issue.getNumber());
				setCategoryAndDataValue(issue, data, fieldMapping, SCOPE_ADDED);
			});
			issueData.addAll(new HashSet<>(issueKpiModalObject.values()));
		}
		if (CollectionUtils.isNotEmpty(puntedIssues)) {
			Map<String, IssueKpiModalValue> issueKpiModalObject = KpiDataHelper.createMapOfIssueModal(puntedIssues);
			puntedIssues.forEach(issue -> {
				KPIExcelUtility.populateIssueModal(issue, fieldMapping, issueKpiModalObject);
				IssueKpiModalValue data = issueKpiModalObject.get(issue.getNumber());
				setCategoryAndDataValue(issue, data, fieldMapping, SCOPE_REMOVED);
			});
			issueData.addAll(new HashSet<>(issueKpiModalObject.values()));
		}

		if (CollectionUtils.isNotEmpty(issueData)) {

			kpiElement.setSprint(sprintLeafNode.getName());
			kpiElement.setModalHeads(KPIExcelColumn.ITERATION_COMMITMENT.getColumns());
			kpiElement.setIssueData(issueData);
			kpiElement.setFilterGroup(createFilterGroup());
			kpiElement.setDataGroup(createDataGroup(fieldMapping));
			kpiElement.setCategoryData(createCategoryData());
		}
	}

	/**
	 * Sets kpi data category and value field.
	 * @param issue
	 * @param data
	 * @param fieldMapping
	 * @param category
	 */
	private static void setCategoryAndDataValue(JiraIssue issue, IssueKpiModalValue data, FieldMapping fieldMapping, String category) {
		if (null == data.getCategory()) {
			data.setCategory(List.of(category));
		} else {
			data.getCategory().add(category);
		}

		if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
				&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
			if(null != issue.getStoryPoints()) {
				data.setValue(issue.getStoryPoints());
			}
		} else if(null != issue.getOriginalEstimateMinutes()){
			data.setValue(Double.valueOf(issue.getOriginalEstimateMinutes()));
		} else {
			data.setValue(0.0);
		}
	}

	/**
	 * Creates filter group.
	 * @return
	 */
	private FilterGroup createFilterGroup() {
		FilterGroup filterGroup = new FilterGroup();
		// for the group by selection
		List<Filter> filterList = new ArrayList<>();
		filterList.add(createFilter(SINGLE, "Issue Type", "Issue Type", 1));
		filterList.add(createFilter(SINGLE, "Status", "Issue Status", 2));
		filterGroup.setFilterGroup1(filterList);

		return filterGroup;
	}

	/**
	 * Creates individual filter object.
	 * @param type
	 * @param name
	 * @param key
	 * @param order
	 * @return
	 */
	private Filter createFilter(String type, String name, String key, Integer order) {
		Filter filter = new Filter();
		filter.setFilterType(type);
		filter.setFilterName(name);
		filter.setFilterKey(key);
		filter.setOrder(order);
		return filter;
	}

	/**
	 * Cretaes data group that tells what kind of data will be shown on chart.
	 * @param fieldMapping
	 * @return
	 */
	private List<KpiData> createDataGroup(FieldMapping fieldMapping) {
		List<KpiData> dataGroup = new ArrayList<>();
		String unit;
		if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
				&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
			unit = CommonConstant.SP;
		} else {
			unit = CommonConstant.DAY;
		}

		dataGroup.add(createKpiData("", "Issues", 1, "count", ""));
		dataGroup.add(createKpiData("Value", "Story Point", 2, "sum", unit));
		return dataGroup;
	}

	/**
	 * Creates kpi data object.
	 * @param key
	 * @param name
	 * @param order
	 * @param aggregation
	 * @param unit
	 * @return
	 */
	private KpiData createKpiData(String key, String name, Integer order, String aggregation, String unit) {
		KpiData data = new KpiData();
		data.setKey(key);
		data.setName(name);
		data.setOrder(order);
		data.setAggregation(aggregation);
		data.setUnit(unit);
		return data;
	}

	/**
	 * Creates object to hold category related info.
	 * @return
	 */
	private CategoryData createCategoryData() {
		CategoryData categoryData = new CategoryData();
		categoryData.setCategoryKey("Category");

		List<KpiDataCategory> categoryGroup = new ArrayList<>();
		categoryGroup.add(createKpiDataCategory(INITIAL_COMMITMENT, "positive", 1));
		categoryGroup.add(createKpiDataCategory(SCOPE_ADDED, "positive", 2));
		categoryGroup.add(createKpiDataCategory(SCOPE_REMOVED, "negative", -1));
		categoryData.setCategoryGroup(categoryGroup);
		return categoryData;
	}

	/**
	 * Creates kpi data category object.
	 * @param categoryName
	 * @param categoryValue
	 * @param order
	 * @return
	 */
	private KpiDataCategory createKpiDataCategory(String categoryName, String categoryValue, Integer order) {
		KpiDataCategory category = new KpiDataCategory();
		category.setCategoryName(categoryName);
		category.setCategoryValue(categoryValue);
		category.setOrder(order);
		return category;
	}
}
