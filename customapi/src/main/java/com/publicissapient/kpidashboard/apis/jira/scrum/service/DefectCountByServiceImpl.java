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
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.service.iterationdashboard.JiraIterationKPIService;
import com.publicissapient.kpidashboard.apis.model.*;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.IterationKpiHelper;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DefectCountByServiceImpl extends JiraIterationKPIService {

	public static final String UNCHECKED = "unchecked";
	private static final String TOTAL_ISSUES = "Total Issues";
	private static final String SPRINT_DETAILS = "SprintDetails";
	private static final String CREATED_DURING_ITERATION = "Created during Iteration";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
	private static final String FILTER_TYPE = "Multi";

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private FilterHelperService filterHelperService;

	@Override
	public Map<String, Object> fetchKPIDataFromDb(Node leafNode, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		if (null != leafNode) {
			log.info("Defect count by Status -> Requested sprint : {}", leafNode.getName());
			String basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId().toString();
			String sprintId = leafNode.getSprintFilter().getId();
			List<String> defectType = new ArrayList<>();
			SprintDetails sprintDetails = getSprintDetailsFromBaseClass();
			if (null != sprintDetails) {
				FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
						.get(leafNode.getProjectFilter().getBasicProjectConfigId());
				List<JiraIssue> totalJiraIssueList = getJiraIssuesFromBaseClass();

				List<String> totalIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.TOTAL_ISSUES);
				List<String> defectTypes = new ArrayList<>(
						Optional.ofNullable(fieldMapping.getJiradefecttype()).orElse(Collections.emptyList()));
				Set<String> totalSprintReportDefects = new HashSet<>();
				Set<String> totalSprintReportStories = new HashSet<>();
				sprintDetails.getTotalIssues().forEach(sprintIssue -> {
					if (defectTypes.contains(sprintIssue.getTypeName())) {
						totalSprintReportDefects.add(sprintIssue.getNumber());
					} else {
						totalSprintReportStories.add(sprintIssue.getNumber());
					}
				});

				Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
				Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
				Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
				defectType.add(NormalizedJira.DEFECT_TYPE.getValue());
				mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
						CommonUtils.convertToPatternList(defectType));
				uniqueProjectMap.put(basicProjectConfigId, mapOfProjectFilters);
				mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
						Collections.singletonList(basicProjectConfigId));

				if (CollectionUtils.isNotEmpty(totalIssues)) {
					List<JiraIssue> totalIssueList = IterationKpiHelper.getFilteredJiraIssue(totalIssues,
							totalJiraIssueList);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getTotalIssues(), totalIssueList);

					// fetched all defects which is linked to current sprint report stories
					List<JiraIssue> linkedDefects = jiraIssueRepository.findLinkedDefects(mapOfFilters,
							totalSprintReportStories, uniqueProjectMap);

					// filter defects which is issue type not coming in sprint report
					List<JiraIssue> subTaskDefects = linkedDefects.stream()
							.filter(jiraIssue -> !totalSprintReportDefects.contains(jiraIssue.getNumber()))
							.toList();

					List<JiraIssue> totalSubTaskTaggedToSprint = subTaskDefects.stream()
							.filter(jiraIssue -> CollectionUtils.isNotEmpty(jiraIssue.getSprintIdList())
									&& jiraIssue.getSprintIdList().contains(sprintId.split("_")[0]))
							.toList();

					List<JiraIssue> allIssues = new ArrayList<>();
					allIssues.addAll(filtersIssuesList);
					allIssues.addAll(totalSubTaskTaggedToSprint);

					resultListMap.put(CommonConstant.TOTAL_ISSUES, new ArrayList<>(allIssues));
				}
				resultListMap.put(SPRINT_DETAILS, sprintDetails);
			}
		}
		return resultListMap;
	}

	@Override
	public String getQualifierType() {
		return KPICode.DEFECT_COUNT_BY_ITERATION.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node sprintNode)
			throws ApplicationException {
		sprintWiseLeafNodeValue(sprintNode, kpiElement, kpiRequest);
		log.info("DefectCountByServiceImpl -> getKpiData ->  : {}", kpiElement);
		return kpiElement;
	}

	private void sprintWiseLeafNodeValue(Node latestSprint, KpiElement kpiElement, KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();
		List<KPIExcelData> excelData = new ArrayList<>();
		if (latestSprint != null) {
			Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprint, null, null, kpiRequest);
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(latestSprint.getProjectFilter().getBasicProjectConfigId());
			if (fieldMapping != null) {
				SprintDetails sprintDetails = (SprintDetails) resultMap.get(SPRINT_DETAILS);
				List<JiraIssue> allCompletedDefects = filterDefects(resultMap, fieldMapping);
				List<JiraIssue> createDuringIteration = allCompletedDefects.stream()
						.filter(jiraIssue -> DateUtil.isWithinDateRange(
								LocalDate.parse(jiraIssue.getCreatedDate().split("\\.")[0], DATE_TIME_FORMATTER),
								LocalDate.parse(sprintDetails.getStartDate().split("\\.")[0], DATE_TIME_FORMATTER),
								LocalDate.parse(sprintDetails.getEndDate().split("\\.")[0], DATE_TIME_FORMATTER)))
						.collect(Collectors.toList());
				log.info("DefectCountByServiceImpl -> allCompletedDefects ->  : {}", allCompletedDefects);
				// Creating map of modal Objects
				Map<String, IssueKpiModalValue> issueKpiModalObject = KpiDataHelper
						.createMapOfIssueModal(allCompletedDefects);
				allCompletedDefects.forEach(defect -> {
					KPIExcelUtility.populateIssueModal(defect, fieldMapping, issueKpiModalObject);
					IssueKpiModalValue data = issueKpiModalObject.get(defect.getNumber());
					List<String> category = new ArrayList<>();
					category.add(TOTAL_ISSUES);
					if (createDuringIteration.contains(defect)) {
						category.add(CREATED_DURING_ITERATION);
					}
					data.setCategory(category);
				});

				populateExcelDataObject(requestTrackerId, excelData, allCompletedDefects, createDuringIteration,
						latestSprint.getSprintFilter().getName(), fieldMapping);

				kpiElement.setSprint(latestSprint.getName());
				kpiElement.setModalHeads(KPIExcelColumn.DEFECT_COUNT_BY_STATUS_PIE_CHART
						.getColumns(List.of(latestSprint), cacheService, filterHelperService));
				kpiElement.setExcelColumns(KPIExcelColumn.DEFECT_COUNT_BY_STATUS_PIE_CHART
						.getColumns(List.of(latestSprint), cacheService, filterHelperService));
				kpiElement.setExcelData(excelData);

				kpiElement.setIssueData(new HashSet<>(issueKpiModalObject.values()));
				kpiElement.setFilterGroup(createFilterGroup());
				kpiElement.setDataGroup(createDataGroup());
				kpiElement.setCategoryData(createCategoryData());
			}
		}
	}

	private FilterGroup createFilterGroup() {
		FilterGroup filterGroup = new FilterGroup();
		// for the first group by selection
		List<Filter> filterList = new ArrayList<>();
		filterList.add(createFilter(FILTER_TYPE, "Status", "Issue Status", 1));
		filterList.add(createFilter(FILTER_TYPE, "Priority", "Priority", 2));
		filterList.add(createFilter(FILTER_TYPE, "RCA", "Root Cause List", 3));
		filterGroup.setFilterGroup1(filterList);

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

	/**
	 * Creates data group that tells what kind of data will be shown on chart.
	 *
	 * @return
	 */
	private KpiDataGroup createDataGroup() {
		KpiDataGroup dataGroup = new KpiDataGroup();

		List<KpiData> dataGroup1 = new ArrayList<>();
		dataGroup1.add(createKpiData("", "", 1, "count", ""));
		dataGroup.setDataGroup1(dataGroup1);
		return dataGroup;
	}

	/**
	 * Creates kpi data object.
	 *
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
		data.setShowAsLegend(false);
		return data;
	}

	/**
	 * Creates object to hold category related info.
	 * 
	 * @return
	 */
	private CategoryData createCategoryData() {
		CategoryData categoryData = new CategoryData();
		categoryData.setCategoryKey("Category");

		List<KpiDataCategory> categoryGroup = new ArrayList<>();
		categoryGroup.add(createKpiDataCategory(CREATED_DURING_ITERATION, "+", 1));
		categoryGroup.add(createKpiDataCategory(TOTAL_ISSUES, "+", 2));
		categoryData.setCategoryGroup(categoryGroup);
		return categoryData;
	}

	/**
	 * Creates kpi data category object.
	 * 
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

	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			List<JiraIssue> sprintWiseDefectDataList, List<JiraIssue> createDuringIteration, String name,
			FieldMapping fieldMapping) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& !Objects.isNull(sprintWiseDefectDataList) && !sprintWiseDefectDataList.isEmpty()) {
			KPIExcelUtility.populateDefectRCAandStatusRelatedExcelData(name, sprintWiseDefectDataList,
					createDuringIteration, excelData, fieldMapping);
		}

	}

	private List<JiraIssue> filterDefects(Map<String, Object> resultMap, FieldMapping fieldMapping) {
		List<String> defectStatuses = new ArrayList<>(
				Optional.ofNullable(fieldMapping.getJiradefecttype()).orElse(Collections.emptyList()));
		// subtask defects consider as BUG type in jira_issue
		defectStatuses.add(NormalizedJira.DEFECT_TYPE.getValue());
		if (CollectionUtils.isNotEmpty((List<JiraIssue>) resultMap.get(CommonConstant.TOTAL_ISSUES))) {
			return ((List<JiraIssue>) resultMap.get(CommonConstant.TOTAL_ISSUES)).stream()
					.filter(issue -> defectStatuses.contains(issue.getTypeName())).collect(Collectors.toList());
		}
		return new ArrayList<>();
	}
}