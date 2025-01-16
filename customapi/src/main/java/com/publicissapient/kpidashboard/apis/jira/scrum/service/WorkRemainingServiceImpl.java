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

import static com.publicissapient.kpidashboard.apis.util.IterationKpiHelper.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.CalculatePCDHelper;
import com.publicissapient.kpidashboard.apis.jira.service.iterationdashboard.JiraIterationKPIService;
import com.publicissapient.kpidashboard.apis.model.*;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.IterationPotentialDelay;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WorkRemainingServiceImpl extends JiraIterationKPIService {

	public static final String UNCHECKED = "unchecked";
	public static final String ISSUE_CUSTOM_HISTORY = "issues custom history";
	private static final String FILTER_BY_ISSUE_TYPE = "Filter by issue type";
	private static final String FILTER_BY_STATUS = "Filter by status";
	private static final String ISSUES = "issues";
	private static final String ISSUE_COUNT = "Issue count";
	private static final String REMAINING_WORK = "Remaining Work";
	private static final String POTENTIAL_DELAY = "Potential Delay";
	private static final String SPRINT_DETAILS = "sprint details";
	private static final String FILTER_TYPE = "Multi";
	private static final String SUM = "sum";

	@Autowired
	private ConfigHelperService configHelperService;

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node sprintNode)
			throws ApplicationException {
		projectWiseLeafNodeValue(sprintNode, kpiElement, kpiRequest);
		return kpiElement;
	}

	@Override
	public String getQualifierType() {
		return KPICode.WORK_REMAINING.name();
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(Node leafNode, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		if (null != leafNode) {
			log.info("Work Remaining -> Requested sprint : {}", leafNode.getName());
			SprintDetails sprintDetails;
			SprintDetails dbSprintDetail = getSprintDetailsFromBaseClass();
			if (null != dbSprintDetail) {
				FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
						.get(leafNode.getProjectFilter().getBasicProjectConfigId());
				// to modify sprint details on the basis of configuration for the project
				List<JiraIssueCustomHistory> totalHistoryList = getJiraIssuesCustomHistoryFromBaseClass();
				List<JiraIssue> totalJiraIssueList = getJiraIssuesFromBaseClass();
				Set<String> issueList = totalJiraIssueList.stream().map(JiraIssue::getNumber)
						.collect(Collectors.toSet());

				sprintDetails = transformIterSprintdetail(totalHistoryList, issueList, dbSprintDetail,
						fieldMapping.getJiraIterationIssuetypeKPI119(),
						fieldMapping.getJiraIterationCompletionStatusKPI119(),
						leafNode.getProjectFilter().getBasicProjectConfigId());

				List<String> notCompletedIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(
						sprintDetails, CommonConstant.NOT_COMPLETED_ISSUES);
				if (CollectionUtils.isNotEmpty(notCompletedIssues)) {
					List<JiraIssue> notCompletedJiraIssueList = getFilteredJiraIssue(notCompletedIssues,
							totalJiraIssueList);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getNotCompletedIssues(), notCompletedJiraIssueList);
					List<JiraIssueCustomHistory> issueHistoryList = getFilteredJiraIssueHistory(
							notCompletedJiraIssueList.stream().map(JiraIssue::getNumber).toList(), totalHistoryList);
					resultListMap.put(ISSUES, new ArrayList<>(filtersIssuesList));
					resultListMap.put(SPRINT_DETAILS, sprintDetails);
					resultListMap.put(ISSUE_CUSTOM_HISTORY, issueHistoryList);
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
	 *            sprintLeafNode
	 * @param kpiElement
	 *            kpiElement
	 * @param kpiRequest
	 *            kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(Node sprintLeafNode, KpiElement kpiElement, KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();

		Object basicProjectConfigId = Objects.requireNonNull(sprintLeafNode).getProjectFilter()
				.getBasicProjectConfigId();
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

		Map<String, Object> resultMap = fetchKPIDataFromDb(sprintLeafNode, null, null, kpiRequest);
		List<JiraIssue> allIssues = (List<JiraIssue>) resultMap.get(ISSUES);
		SprintDetails sprintDetails = (SprintDetails) resultMap.get(SPRINT_DETAILS);
		List<JiraIssueCustomHistory> allIssueHistories = (List<JiraIssueCustomHistory>) resultMap
				.get(ISSUE_CUSTOM_HISTORY);
		if (CollectionUtils.isNotEmpty(allIssues)) {
			log.info("Work Remaining -> request id : {} total jira Issues : {}", requestTrackerId, allIssues.size());

			List<IterationPotentialDelay> iterationPotentialDelayList = CalculatePCDHelper
					.calculatePotentialDelay(sprintDetails, allIssues, fieldMapping.getJiraStatusForInProgressKPI119());
			Map<String, IterationPotentialDelay> issueWiseDelay = CalculatePCDHelper.checkMaxDelayAssigneeWise(
					iterationPotentialDelayList, fieldMapping.getJiraStatusForInProgressKPI119());

			// Creating map of modal Objects
			Map<String, IssueKpiModalValue> issueKpiModalObject = KpiDataHelper.createMapOfIssueModal(allIssues);
			allIssues.forEach(issue -> {
				KPIExcelUtility.populateIssueModal(issue, fieldMapping, issueKpiModalObject);
				IssueKpiModalValue data = issueKpiModalObject.get(issue.getNumber());

				JiraIssueCustomHistory issueCustomHistory = allIssueHistories.stream()
						.filter(jiraIssueCustomHistory -> jiraIssueCustomHistory.getStoryID().equals(issue.getNumber()))
						.findFirst().orElse(new JiraIssueCustomHistory());
				String devCompletionDate = getDevCompletionDate(issueCustomHistory,
						fieldMapping.getJiraDevDoneStatusKPI119());

				IterationPotentialDelay iterationPotentialDelay = issueWiseDelay.get(issue.getNumber());
				setKpiSpecificData(sprintDetails, data, iterationPotentialDelay, issue, devCompletionDate,
						fieldMapping);

			});

			kpiElement.setSprint(sprintLeafNode.getName());
			kpiElement.setModalHeads(KPIExcelColumn.WORK_REMAINING.getColumns());
			kpiElement.setIssueData(new HashSet<>(issueKpiModalObject.values()));
			kpiElement.setFilterGroup(createFilterGroup());
			kpiElement.setDataGroup(createDataGroup(fieldMapping));
		}
	}

	private int getDelayInMinutes(int delay) {
		return delay * 60 * 8;
	}

	private void setKpiSpecificData(SprintDetails sprintDetails, IssueKpiModalValue jiraIssueModalObject,
			IterationPotentialDelay iterationPotentialDelay, JiraIssue jiraIssue, String devCompletionDate,
			FieldMapping fieldMapping) {
		jiraIssueModalObject.setDevCompletionDate(
				DateUtil.dateTimeConverter(devCompletionDate, DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT));
		String markerValue = Constant.BLANK;
		if (null != iterationPotentialDelay && StringUtils.isNotEmpty(jiraIssue.getDueDate())) {
			jiraIssueModalObject.setPotentialDelay(iterationPotentialDelay.getPotentialDelay() + "d");
			markerValue = getMarkerValue(sprintDetails, iterationPotentialDelay, markerValue);
			jiraIssueModalObject.setPredictedCompletionDate(
					DateUtil.dateTimeConverter(iterationPotentialDelay.getPredictedCompletedDate(),
							DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT));

		} else {
			jiraIssueModalObject.setPotentialOverallDelay(Constant.BLANK);
			jiraIssueModalObject.setPredictedCompletionDate(Constant.BLANK);
		}

		int delay = 0;
		if (null != iterationPotentialDelay && iterationPotentialDelay.isMaxMarker()) {
			delay = getDelayInMinutes(iterationPotentialDelay.getPotentialDelay());
		}

		jiraIssueModalObject.setValue(0d);
		if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
				&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
			if (null != jiraIssue.getStoryPoints()) {
				jiraIssueModalObject.setValue(jiraIssue.getStoryPoints());
			}
		} else if (null != jiraIssue.getOriginalEstimateMinutes()) {
			jiraIssueModalObject.setValue(Double.valueOf(jiraIssue.getOriginalEstimateMinutes()));
		}
		jiraIssueModalObject.setDelay(delay);
		jiraIssueModalObject.setMarker(markerValue);
	}

	private static String getMarkerValue(SprintDetails sprintDetails, IterationPotentialDelay iterationPotentialDelay, String markerValue) {
		final LocalDate sprintEndDate = DateUtil.stringToLocalDate(sprintDetails.getEndDate(),
				DateUtil.TIME_FORMAT_WITH_SEC);
		final LocalDate predictCompletionDate = LocalDate
				.parse(iterationPotentialDelay.getPredictedCompletedDate());
		if (!sprintEndDate.isBefore(predictCompletionDate)) {
			if (ChronoUnit.DAYS.between(predictCompletionDate, sprintEndDate) < 2) {
				markerValue = Constant.AMBER;
			}
		} else {
			markerValue = Constant.RED;
		}
		return markerValue;
	}

	/**
	 * Creates filter group.
	 * 
	 * @return
	 */
	private FilterGroup createFilterGroup() {
		FilterGroup filterGroup = new FilterGroup();
		// for the group by selection
		List<Filter> filterList = new ArrayList<>();
		filterList.add(createFilter(FILTER_TYPE, FILTER_BY_ISSUE_TYPE, "Issue Type", 1));
		filterList.add(createFilter(FILTER_TYPE, FILTER_BY_STATUS, "Issue Status", 2));
		filterGroup.setFilterGroup1(filterList);

		return filterGroup;
	}

	/**
	 * Creates individual filter object.
	 * 
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
	 * Creates data group that tells what kind of data will be shown on chart.
	 * 
	 * @param fieldMapping
	 * @return
	 */
	private KpiDataGroup createDataGroup(FieldMapping fieldMapping) {
		KpiDataGroup dataGroup = new KpiDataGroup();

		List<KpiData> dataGroup1 = new ArrayList<>();
		String unit;
		String name;
		if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
				&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
			unit = CommonConstant.SP;
			name = CommonConstant.STORY_POINT;
		} else {
			unit = CommonConstant.DAY;
			name = CommonConstant.ORIGINAL_ESTIMATE;
		}

		dataGroup1.add(createKpiData("value", name, 1, SUM, unit));
		dataGroup1.add(createKpiData("", ISSUE_COUNT, 2, "count", ""));

		List<KpiData> dataGroup2 = new ArrayList<>();
		dataGroup2.add(createKpiData("Remaining Hours", REMAINING_WORK, 1, SUM, CommonConstant.DAY));
		dataGroup2.add(createKpiData("Delay", POTENTIAL_DELAY, 2, SUM, CommonConstant.DAY));

		// For markerInfo
		Map<String, String> markerInfo = new HashMap<>();
		markerInfo.put(Constant.AMBER, "Issue finishing in the last two days of the iteration are marked in AMBER");
		markerInfo.put(Constant.RED, "Issues finishing post issue due date are marked in RED");

		dataGroup.setDataGroup1(dataGroup1);
		dataGroup.setDataGroup2(dataGroup2);
		dataGroup.setMarkerInfo(markerInfo);
		dataGroup.setMetaDataColumns(List.of("marker"));
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
}
