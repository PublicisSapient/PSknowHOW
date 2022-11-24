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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * This class calculates the Created vs Resolved Defects.
 * 
 * @author marjesur
 *
 */
@Component
@Slf4j
public class CreatedVsResolvedServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String SEPARATOR_ASTERISK = "*************************************";
	private static final String CREATED_VS_RESOLVED_KEY = "createdVsResolvedKey";
	private static final String SPRINT_WISE_SPRINTDETAILS = "sprintWiseSprintDetailMap";
	private static final String CREATED_DEFECTS = "createdDefects";
	private static final String RESOLVED_DEFECTS = "resolvedDefects";
	private static final String DEV = "DeveloperKpi";
	private static final String PROJECT_WISE_CLOSED_STORY_STATUS = "projectWiseClosedStoryStatus";

	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private SprintRepository sprintRepository;

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private FilterHelperService flterHelperService;

	@Autowired
	private KpiHelperService kpiHelperService;
	@Autowired
	private ConfigHelperService configHelperService;

	/**
	 * Gets Qualifier Type
	 * 
	 * @return KPICode's <tt>CREATED_VS_RESOLVED</tt> enum
	 */
	@Override
	public String getQualifierType() {
		return KPICode.CREATED_VS_RESOLVED_DEFECTS.name();
	}

	/**
	 * Gets KPI Data
	 * 
	 * @param kpiRequest
	 * @param kpiElement
	 * @param treeAggregatorDetail
	 * @return KpiElement
	 * @throws ApplicationException
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		List<DataCount> trendValueList = new ArrayList<>();
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {

			if (Filters.getFilter(k) == Filters.SPRINT) {
				sprintWiseLeafNodeValue(mapTmp, v, trendValueList, kpiElement, kpiRequest);
			}
		});

		log.debug("[CREATED-RESOLVED-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.CREATED_VS_RESOLVED_DEFECTS);
		List<DataCount> trendValues = getTrendValues(kpiRequest, nodeWiseKPIValue, KPICode.CREATED_VS_RESOLVED_DEFECTS);
		kpiElement.setTrendValueList(trendValues);
		return kpiElement;
	}

	/**
	 * Fetches KPI Data from Created Vs Resolved.
	 * 
	 * @param leafNodeList
	 * @param startDate
	 * @param endDate
	 * @param kpiRequest
	 * @return {@code Map<String, Object>}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Object> resultListMap = new HashMap<>();
		List<String> sprintList = new ArrayList<>();
		List<String> basicProjectConfigIds = new ArrayList<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		List<String> defectType = new ArrayList<>();
		Map<String, List<String>> projectWiseClosedStatusMap = new HashMap<>();
		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
			sprintList.add(leaf.getSprintFilter().getId());
			basicProjectConfigIds.add(basicProjectConfigId.toString());

			defectType.add(NormalizedJira.DEFECT_TYPE.getValue());
			mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(), defectType);
			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			if (Optional.ofNullable(fieldMapping.getJiraIssueDeliverdStatus()).isPresent()) {
				projectWiseClosedStatusMap.put(basicProjectConfigId.toString(),
						fieldMapping.getJiraIssueDeliverdStatus().stream().distinct().collect(Collectors.toList()));
			}
		});

		List<SprintDetails> sprintDetails = sprintRepository.findBySprintIDIn(sprintList);
		Set<String> totalIssue = new HashSet<>();
		sprintDetails.stream().forEach(sprintDetail -> {
			if (CollectionUtils.isNotEmpty(sprintDetail.getTotalIssues())) {
				totalIssue.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetail,
						CommonConstant.TOTAL_ISSUES));
			}

		});

		/** additional filter **/
		KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.SCRUM, DEV, flterHelperService);

		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		if (CollectionUtils.isNotEmpty(totalIssue)) {
			resultListMap.put(CREATED_VS_RESOLVED_KEY,
					jiraIssueRepository.findIssueByNumber(mapOfFilters, totalIssue, uniqueProjectMap));
			resultListMap.put(SPRINT_WISE_SPRINTDETAILS, sprintDetails);
		} else {
			// start : for azure board sprint details collections put is empty due to we did
			// not have required data of issues.
			resultListMap.put(CREATED_VS_RESOLVED_KEY,
					jiraIssueRepository.findIssuesBySprintAndType(mapOfFilters, uniqueProjectMap));
			resultListMap.put(SPRINT_WISE_SPRINTDETAILS, new ArrayList<>());
			// end : for azure board sprint details collections put is empty due to we did
			// not have required data of issues.
		}
		resultListMap.put(PROJECT_WISE_CLOSED_STORY_STATUS, projectWiseClosedStatusMap);
		setDbQueryLogger((List<JiraIssue>) resultListMap.get(CREATED_VS_RESOLVED_KEY));
		return resultListMap;

	}

	/**
	 * @param createdVsResolvedMap
	 * @return timeLogged in seconds
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Double calculateKPIMetrics(Map<String, Object> createdVsResolvedMap) {
		String requestTrackerId = getRequestTrackerId();
		Double createdVsResolved = 0.0d;
		List<JiraIssue> createdVsResolvedList = (List<JiraIssue>) createdVsResolvedMap.get(CREATED_VS_RESOLVED_KEY);
		log.debug("[CREATED-RESOLVED][{}]. Stories Count: {}", requestTrackerId, createdVsResolvedList.size());
		for (JiraIssue jiraIssue : createdVsResolvedList) {
			createdVsResolved = createdVsResolved + Double.valueOf(jiraIssue.getCount());
		}
		return createdVsResolved;
	}

	/**
	 * Populates KPI value to sprint leaf nodes and gives the trend analysis at
	 * sprint wise.
	 * 
	 * @param mapTmp
	 * @param kpiElement
	 * @param sprintLeafNodeList
	 * @param trendValueList
	 * @param kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();

		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));

		Map<String, Object> createdVsResolvedMap = fetchKPIDataFromDb(sprintLeafNodeList, null, null, kpiRequest);

		List<JiraIssue> allJiraIssue = (List<JiraIssue>) createdVsResolvedMap.get(CREATED_VS_RESOLVED_KEY);

		List<SprintDetails> sprintDetails = (List<SprintDetails>) createdVsResolvedMap.get(SPRINT_WISE_SPRINTDETAILS);

		Map<Pair<String, String>, List<JiraIssue>> sprintWiseCreatedIssues = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseClosedIssues = new HashMap<>();

		if (CollectionUtils.isNotEmpty(allJiraIssue)) {
			if (CollectionUtils.isNotEmpty(sprintDetails)) {
				sprintDetails.forEach(sd -> {
					List<String> availableIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sd,
							CommonConstant.TOTAL_ISSUES);
					List<String> completedSprintIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sd,
							CommonConstant.COMPLETED_ISSUES);
					List<JiraIssue> totalIssues = allJiraIssue.stream()
							.filter(element -> availableIssues.contains(element.getNumber()))
							.collect(Collectors.toList());
					List<JiraIssue> completedIssues = allJiraIssue.stream()
							.filter(element -> completedSprintIssues.contains(element.getNumber()))
							.collect(Collectors.toList());
					sprintWiseCreatedIssues.put(Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID()),
							totalIssues);
					sprintWiseClosedIssues.put(Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID()),
							completedIssues);
				});
			} else {
				// for azure board sprint details collections empty so that we have to prepare
				// data from jira issue
				Map<String, List<String>> projectWiseClosedStatusMap = (Map<String, List<String>>) createdVsResolvedMap
						.get(PROJECT_WISE_CLOSED_STORY_STATUS);
				Map<String, List<JiraIssue>> projectWiseJiraIssues = allJiraIssue.stream()
						.collect(Collectors.groupingBy(JiraIssue::getBasicProjectConfigId));
				projectWiseJiraIssues.forEach((basicProjectConfigId, projectWiseIssuesList) -> {
					Map<String, List<JiraIssue>> sprintWiseJiraIssues = projectWiseIssuesList.stream()
							.filter(jiraIssue -> Objects.nonNull(jiraIssue.getSprintID()))
							.collect(Collectors.groupingBy(JiraIssue::getSprintID));
					sprintWiseJiraIssues.forEach((sprintId, totalIssues) -> sprintWiseCreatedIssues
							.put(Pair.of(basicProjectConfigId, sprintId), totalIssues));
					List<String> closedStatus = projectWiseClosedStatusMap.get(basicProjectConfigId);
					sprintWiseJiraIssues.forEach((sprintId, sprintWiseIssuesList) -> {
						List<JiraIssue> completedIssues = sprintWiseIssuesList.stream()
								.filter(jiraIssue -> closedStatus.contains(jiraIssue.getStatus()))
								.collect(Collectors.toList());
						sprintWiseClosedIssues.put(Pair.of(basicProjectConfigId, sprintId), completedIssues);
					});
				});
			}
		}

		List<KPIExcelData> excelData = new ArrayList<>();

		sprintLeafNodeList.forEach(node -> {
			// Leaf node wise data
			String trendLineName = node.getProjectFilter().getName();
			String currentSprintComponentId = node.getSprintFilter().getId();
			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), currentSprintComponentId);

			Map<String, Integer> hoverValue = new HashMap<>();

			double createdForCurrentLeaf = 0.0d;
			double resolvedForCurrentLeaf = 0.0d;

			if (CollectionUtils.isNotEmpty(sprintWiseCreatedIssues.get(currentNodeIdentifier))) {
				List<JiraIssue> createdIssues = sprintWiseCreatedIssues.get(currentNodeIdentifier);
				List<JiraIssue> closedIssues = sprintWiseClosedIssues.get(currentNodeIdentifier);
				createdForCurrentLeaf = createdIssues.size();
				resolvedForCurrentLeaf = closedIssues.size();
				hoverValue.put(CREATED_DEFECTS, createdIssues.size());
				hoverValue.put(RESOLVED_DEFECTS, closedIssues.size());
				populateExcelDataObject(requestTrackerId, excelData, node, createdIssues, closedIssues);
			}

			log.debug("[CREATED-VS-RESOLVED-SPRINT-WISE][{}]. Created Vs Resolved for sprint {}  is {} - {}",
					requestTrackerId, node.getSprintFilter().getName(), createdForCurrentLeaf, resolvedForCurrentLeaf);

			DataCount dataCount = new DataCount();
			dataCount.setData(String.valueOf(Math.round(createdForCurrentLeaf)));
			dataCount.setSProjectName(trendLineName);
			dataCount.setSSprintID(node.getSprintFilter().getId());
			dataCount.setSSprintName(node.getSprintFilter().getName());
			dataCount.setSprintIds(new ArrayList<>(Arrays.asList(node.getSprintFilter().getId())));
			dataCount.setSprintNames(new ArrayList<>(Arrays.asList(node.getSprintFilter().getName())));
			dataCount.setValue(createdForCurrentLeaf);
			dataCount.setLineValue(resolvedForCurrentLeaf);
			dataCount.setHoverValue(hoverValue);
			trendValueList.add(dataCount);
			mapTmp.get(node.getId()).setValue(new ArrayList<DataCount>(Arrays.asList(dataCount)));
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.CREATED_VS_RESOLVED_DEFECTS.getColumns());
	}

	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData, Node node,
			List<JiraIssue> totalCreatedTickets, List<JiraIssue> totalResolvedTickets) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {

			Map<String, JiraIssue> createdTicketMap = new HashMap<>();
			totalCreatedTickets.stream()
					.forEach(jiraIssue -> createdTicketMap.putIfAbsent(jiraIssue.getNumber(), jiraIssue));

			KPIExcelUtility.populateCreatedVsResolvedExcelData(node.getSprintFilter().getName(), createdTicketMap, totalResolvedTickets,
					excelData);
		}

	}

	/**
	 * Sets DB Query log
	 * 
	 * @param storyFeatureList
	 */
	private void setDbQueryLogger(List<JiraIssue> storyFeatureList) {

		if (customApiConfig.getApplicationDetailedLogger().equalsIgnoreCase("on")) {
			log.info(SEPARATOR_ASTERISK);
			log.info("************* Sprint Created Vs Resolved (dB) *******************");
			if (null != storyFeatureList && !storyFeatureList.isEmpty()) {
				List<String> storyIdList = storyFeatureList.stream().map(JiraIssue::getNumber)
						.collect(Collectors.toList());
				log.info("Story[{}]: {}", storyIdList.size(), storyIdList);
			}
			log.info(SEPARATOR_ASTERISK);
			log.info("******************X----X*******************");
		}
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}
}
