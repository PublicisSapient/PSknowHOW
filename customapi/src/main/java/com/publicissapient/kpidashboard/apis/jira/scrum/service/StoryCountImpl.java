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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
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
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

@Component
public class StoryCountImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(StoryCountImpl.class);
	private static final String STORY_LIST = "stories";
	private static final String SPRINTSDETAILS = "sprints";
	private static final String DEV = "DeveloperKpi";
	@Autowired
	private JiraIssueRepository jiraIssueRepository;
	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private FilterHelperService flterHelperService;
	@Autowired
	private SprintRepository sprintRepository;

	/**
	 * Gets Qualifier Type
	 *
	 * @return KPICode's <tt>STORY_COUNT</tt> enum
	 */
	@Override
	public String getQualifierType() {
		return KPICode.STORY_COUNT.name();
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

			Filters filters = Filters.getFilter(k);
			if (Filters.SPRINT == filters) {
				sprintWiseLeafNodeValue(mapTmp, v, trendValueList, kpiElement, kpiRequest);
			}

		});

		LOGGER.debug("[STORYCOUNT-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.STORY_COUNT);
		List<DataCount> trendValues = getTrendValues(kpiRequest, nodeWiseKPIValue, KPICode.STORY_COUNT);
		kpiElement.setTrendValueList(trendValues);

		return kpiElement;
	}

	/**
	 * Fetches KPI Data from DB
	 *
	 * @param leafNodeList
	 * @param startDate
	 * @param endDate
	 * @param kpiRequest
	 * @return {@code Map<String, Object>}
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {

		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Object> resultListMap = new HashMap<>();
		List<String> sprintList = new ArrayList<>();
		List<String> basicProjectConfigIds = new ArrayList<>();

		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();

			sprintList.add(leaf.getSprintFilter().getId());
			basicProjectConfigIds.add(basicProjectConfigId.toString());
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

			if (Optional.ofNullable(fieldMapping.getJiraStoryIdentification()).isPresent()) {
				KpiDataHelper.prepareFieldMappingDefectTypeTransformation(mapOfProjectFilters, fieldMapping,
						fieldMapping.getJiraStoryIdentification(), JiraFeature.ISSUE_TYPE.getFieldValueInFeature());
				uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);
			}

		});

		List<SprintDetails> sprintDetails = sprintRepository.findBySprintIDIn(sprintList);
		getModifiedSprintDetailsFromBaseClass(sprintDetails,configHelperService);
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
			resultListMap.put(STORY_LIST,
					jiraIssueRepository.findIssueByNumber(mapOfFilters, totalIssue, uniqueProjectMap));
			resultListMap.put(SPRINTSDETAILS, sprintDetails);
		} else {
			// start : for azure board sprint details collections put is empty due to we did
			// not have required data of issues.
			resultListMap.put(STORY_LIST,
					jiraIssueRepository.findIssuesBySprintAndType(mapOfFilters, uniqueProjectMap));
			resultListMap.put(SPRINTSDETAILS, null);
		}
		// end : for azure board sprint details collections put is empty due to we did
		// not have required data of issues.
		return resultListMap;

	}

	/**
	 * Calculates KPI Metrics
	 *
	 * @param subCategoryMap
	 * @return Integer
	 */
	@Override
	public Double calculateKPIMetrics(Map<String, Object> subCategoryMap) {
		String requestTrackerId = getRequestTrackerId();

		LOGGER.debug("[JIRA-STORYCOUNT][{}]. Total Story Count: {}", requestTrackerId, subCategoryMap);
		return 0.0d;
	}

	/**
	 * Populates KPI value to sprint leaf nodes andgives the trend analysis at
	 * sprint wise.
	 *
	 * @param mapTmp
	 * @param sprintLeafNodeList
	 * @param trendValueList
	 * @param kpiElement
	 * @param kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();

		String startDate;
		String endDate;

		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));

		startDate = sprintLeafNodeList.get(0).getSprintFilter().getStartDate();
		endDate = sprintLeafNodeList.get(sprintLeafNodeList.size() - 1).getSprintFilter().getEndDate();
		Map<String, Object> resultMap = fetchKPIDataFromDb(sprintLeafNodeList, startDate, endDate, kpiRequest);

		List<JiraIssue> allJiraIssue = (List<JiraIssue>) resultMap.get(STORY_LIST);

		List<SprintDetails> sprintDetails = (List<SprintDetails>) resultMap.get(SPRINTSDETAILS);

		Map<Pair<String, String>, List<String>> sprintWiseIssueNumbers = new HashMap<>();
		if (CollectionUtils.isNotEmpty(allJiraIssue)) {
			if (CollectionUtils.isNotEmpty(sprintDetails)) {
				sprintDetails.forEach(sd -> {
					List<String> totalIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sd,
							CommonConstant.TOTAL_ISSUES);
					totalIssues.retainAll(
							allJiraIssue.stream().distinct().map(JiraIssue::getNumber).collect(Collectors.toList()));
					sprintWiseIssueNumbers.put(Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID()),
							totalIssues);
				});
			} else {
				// start : for azure board sprint details collections empty so that we have to
				// prepare data from jira issue.
				Map<String, List<JiraIssue>> projectWiseJiraIssues = allJiraIssue.stream()
						.collect(Collectors.groupingBy(JiraIssue::getBasicProjectConfigId));
				projectWiseJiraIssues.forEach((basicProjectConfigId, projectWiseIssuesList) -> {
					Map<String, List<JiraIssue>> sprintWiseJiraIssues = projectWiseIssuesList.stream()
							.filter(jiraIssue -> Objects.nonNull(jiraIssue.getSprintID()))
							.collect(Collectors.groupingBy(JiraIssue::getSprintID));
					sprintWiseJiraIssues.forEach((sprintId, sprintWiseJiraIssue) -> {
						List<String> totalIssues = sprintWiseJiraIssue.stream().filter(Objects::nonNull)
								.map(JiraIssue::getNumber).distinct().collect(Collectors.toList());
						sprintWiseIssueNumbers.put(Pair.of(basicProjectConfigId, sprintId), totalIssues);
					});
				});
			}
			// end : for azure board sprint details collections empty so that we have to
			// prepare data from jira issue.
		}


		List<KPIExcelData> excelData = new ArrayList<>();

		for (Node node : sprintLeafNodeList) {
			// Leaf node wise data
			String trendLineName = node.getProjectFilter().getName();
			String currentSprintComponentId = node.getSprintFilter().getId();
			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), currentSprintComponentId);
			Double storyCount = 0.0;

			if (CollectionUtils.isNotEmpty(sprintWiseIssueNumbers.get(currentNodeIdentifier))) {
				List<String> totalPresentJiraIssue = sprintWiseIssueNumbers.get(currentNodeIdentifier);
				storyCount = ((Integer) totalPresentJiraIssue.size()).doubleValue();
				populateExcelData(requestTrackerId, allJiraIssue, excelData, node, totalPresentJiraIssue);

			}

			LOGGER.debug("[STORYCOUNT-SPRINT-WISE][{}]. Total Stories Count for sprint {}  is {}", requestTrackerId,
					node.getSprintFilter().getName(), storyCount);

			DataCount dataCount = new DataCount();
			dataCount.setData(String.valueOf(storyCount));
			dataCount.setSProjectName(trendLineName);
			dataCount.setSSprintID(node.getSprintFilter().getId());
			dataCount.setSSprintName(node.getSprintFilter().getName());
			dataCount.setSprintIds(new ArrayList<>(Arrays.asList(node.getSprintFilter().getId())));
			dataCount.setSprintNames(new ArrayList<>(Arrays.asList(node.getSprintFilter().getName())));
			dataCount.setValue(storyCount);
			dataCount.setHoverValue(new HashMap<>());
			mapTmp.get(node.getId()).setValue(new ArrayList<DataCount>(Arrays.asList(dataCount)));
			trendValueList.add(dataCount);

		}
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.STORY_COUNT.getColumns());

	}

	/**
	 * Populates Validation Data Object
	 *
	 * @param requestTrackerId
	 * @param allJiraIssuesList
	 * @param node
	 * @param totalPresentJiraIssue
	 */
	private void populateExcelData(String requestTrackerId, List<JiraIssue> allJiraIssuesList,
			List<KPIExcelData> excelData, Node node, List<String> totalPresentJiraIssue) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			String sprintName = node.getSprintFilter().getName();
			KPIExcelUtility.populateStoryCountExcelData(sprintName, excelData, allJiraIssuesList,
					totalPresentJiraIssue);

		}
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}

}
