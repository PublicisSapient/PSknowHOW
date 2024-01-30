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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.publicissapient.kpidashboard.apis.model.DSRValidationData;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * This class calculates the DSR and trend analysis of the DSR.
 * 
 * @author pkum34
 *
 */
@Component
@Slf4j
public class DSRServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String UATBUGKEY = "uatBugData";
	private static final String TOTALBUGKEY = "totalBugData";
	private static final String SPRINTSTORIES = "storyData";
	private static final String UAT = "Escaped Defects";
	private static final String TOTAL = "Total Defects";
	private static final String QA = "QaKpi";
	private static final String PROJFMAPPING = "projectFieldMapping";
	@Autowired
	private JiraIssueRepository jiraIssueRepository;
	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private FilterHelperService flterHelperService;
	@Autowired
	private CustomApiConfig customApiConfig;

	@Override
	public String getQualifierType() {
		return KPICode.DEFECT_SEEPAGE_RATE.name();
	}

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

		log.debug("[DSR-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.DEFECT_SEEPAGE_RATE);
		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.DEFECT_SEEPAGE_RATE);

		Map<String, Map<String, List<DataCount>>> issueTypeProjectWiseDc = new LinkedHashMap<>();
		trendValuesMap.forEach((issueType, dataCounts) -> {
			Map<String, List<DataCount>> projectWiseDc = dataCounts.stream()
					.collect(Collectors.groupingBy(DataCount::getData));
			issueTypeProjectWiseDc.put(issueType, projectWiseDc);
		});

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		issueTypeProjectWiseDc.forEach((issueType, projectWiseDc) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			List<DataCount> dataList = new ArrayList<>();
			projectWiseDc.entrySet().stream().forEach(trend -> dataList.addAll(trend.getValue()));
			dataCountGroup.setFilter(issueType);
			dataCountGroup.setValue(dataList);
			dataCountGroups.add(dataCountGroup);
		});

		kpiElement.setTrendValueList(dataCountGroups);
		return kpiElement;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {

		Map<String, Object> resultListMap = new HashMap<>();
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		List<String> sprintList = new ArrayList<>();
		List<String> basicProjectConfigIds = new ArrayList<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		Map<String, FieldMapping> projFieldMapping = new HashMap<>();
		Map<String, Map<String, List<String>>> droppedDefects = new HashMap<>();
		Map<String, List<String>> projectWisePriority = new HashMap<>();
		Map<String, List<String>> configPriority = customApiConfig.getPriority();
		Map<String, Set<String>> projectWiseRCA = new HashMap<>();
		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			projFieldMapping.put(leaf.getProjectFilter().getBasicProjectConfigId().toString(), fieldMapping);
			sprintList.add(leaf.getSprintFilter().getId());
			basicProjectConfigIds.add(basicProjectConfigId.toString());

			KpiHelperService.addPriorityProjectWise(projectWisePriority, configPriority, leaf,
					fieldMapping.getDefectPriorityKPI35());
			KpiHelperService.addRCAProjectWise(projectWiseRCA, leaf, fieldMapping.getIncludeRCAForKPI35());

			mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
					CommonUtils.convertToPatternList(fieldMapping.getJiraIssueTypeKPI35()));
			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);
			KpiHelperService.getDroppedDefectsFilters(droppedDefects, basicProjectConfigId,
					fieldMapping.getResolutionTypeForRejectionKPI35(),
					fieldMapping.getJiraDefectRejectionStatusKPI35());
		});
		/** additional filter **/
		KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.SCRUM, QA, flterHelperService);

		mapOfFilters.put(JiraFeature.SPRINT_ID.getFieldValueInFeature(),
				sprintList.stream().distinct().collect(Collectors.toList()));
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		List<SprintWiseStory> sprintWiseStoryList = jiraIssueRepository.findIssuesGroupBySprint(mapOfFilters,
				uniqueProjectMap, kpiRequest.getFilterToShowOnTrend(), QA);

		List<JiraIssue> issuesBySprintAndType = jiraIssueRepository.findIssuesBySprintAndType(mapOfFilters,
				uniqueProjectMap);
		List<JiraIssue> storyListWoDrop = new ArrayList<>();
		KpiHelperService.getDefectsWithoutDrop(droppedDefects, issuesBySprintAndType, storyListWoDrop);
		KpiHelperService.removeRejectedStoriesFromSprint(sprintWiseStoryList, storyListWoDrop);
		List<String> storyNumberList = new ArrayList<>();
		sprintWiseStoryList.forEach(s -> storyNumberList.addAll(s.getStoryList()));

		Map<String, List<String>> mapOfFiltersWithStoryIds = new LinkedHashMap<>();
		mapOfFiltersWithStoryIds.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));
		mapOfFiltersWithStoryIds.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
				Arrays.asList(NormalizedJira.DEFECT_TYPE.getValue()));

		List<JiraIssue> totalDefectList = jiraIssueRepository.findIssuesByType(mapOfFiltersWithStoryIds);

		List<JiraIssue> defectListWoDrop = new ArrayList<>();
		List<JiraIssue> remainingDefect = new ArrayList<>();

		KpiHelperService.getDefectsWithoutDrop(droppedDefects, totalDefectList, defectListWoDrop);
		exludePriorityDefect(projectWisePriority, projectWiseRCA, new HashSet<>(defectListWoDrop), remainingDefect);

		resultListMap.put(SPRINTSTORIES, sprintWiseStoryList);
		resultListMap.put(TOTALBUGKEY, remainingDefect);
		resultListMap.put(PROJFMAPPING, projFieldMapping);
		return resultListMap;

	}

	private static void exludePriorityDefect(Map<String, List<String>> projectWisePriority,
			Map<String, Set<String>> projectWiseRCA, Set<JiraIssue> defects, List<JiraIssue> remainingDefect) {
		List<JiraIssue> remainingDefects = new ArrayList<>();
		List<JiraIssue> rcaDefects = new ArrayList<>();

		for (JiraIssue jiraIssue : defects) {
			excludeSelectedPriorities(projectWisePriority, remainingDefects, jiraIssue);

			// Filter priorityRemaining based on configured Root Causes (RCA) for the
			// project, or include if no RCA is configured.
			excludeSelectRCAJiraIssue(projectWiseRCA, rcaDefects, jiraIssue);
		}

		remainingDefects.stream().forEach(pi -> rcaDefects.stream().forEach(ri -> {
			if (pi.getNumber().equalsIgnoreCase(ri.getNumber())) {
				remainingDefect.add(pi);
			}
		}));

	}

	private static void excludeSelectedPriorities(Map<String, List<String>> projectWisePriority,
			List<JiraIssue> remainingDefects, JiraIssue jiraIssue) {
		if (CollectionUtils.isNotEmpty(projectWisePriority.get(jiraIssue.getBasicProjectConfigId()))) {
			if (!(projectWisePriority.get(jiraIssue.getBasicProjectConfigId())
					.contains(jiraIssue.getPriority().toLowerCase()))) {
				remainingDefects.add(jiraIssue);
			}
		} else {
			remainingDefects.add(jiraIssue);
		}
	}

	private static void excludeSelectRCAJiraIssue(Map<String, Set<String>> projectWiseRCA, List<JiraIssue> rcaDefects,
			JiraIssue jiraIssue) {
		if (CollectionUtils.isNotEmpty(projectWiseRCA.get(jiraIssue.getBasicProjectConfigId()))) {
			for (String toFindRca : jiraIssue.getRootCauseList()) {
				if ((projectWiseRCA.get(jiraIssue.getBasicProjectConfigId()).contains(toFindRca.toLowerCase()))) {
					rcaDefects.add(jiraIssue);
				}
			}
		} else {
			rcaDefects.add(jiraIssue);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Double calculateKPIMetrics(Map<String, Object> filterComponentIdWiseDefectMap) {
		String requestTrackerId = getRequestTrackerId();
		Double dsrPercentage = 0d;
		List<JiraIssue> uatBugList = (List<JiraIssue>) filterComponentIdWiseDefectMap.get(UATBUGKEY);
		List<JiraIssue> totalBugList = (List<JiraIssue>) filterComponentIdWiseDefectMap.get(TOTALBUGKEY);
		if (CollectionUtils.isNotEmpty(uatBugList) && CollectionUtils.isNotEmpty(totalBugList)) {
			int uatDefectCount = uatBugList.size();
			int totalDefectCount = totalBugList.size();

			log.debug("[JIRA-DSR][{}]. UAT Defect count: {}  Total Defect Count: {}", requestTrackerId, uatDefectCount,
					totalDefectCount);
			dsrPercentage = (double) Math.round((100.0 * uatDefectCount) / (totalDefectCount));
		}
		return dsrPercentage;
	}

	/**
	 * This method populates KPI value to sprint leaf nodes. It also gives the trend
	 * analysis at sprint wise.
	 * 
	 * @param mapTmp
	 *            mapTmp
	 * @param kpiElement
	 *            kpiElement
	 * @param sprintLeafNodeList
	 *            sprintLeafNodeList
	 * @param trendValueList
	 *            trendValueList
	 */
	@SuppressWarnings("unchecked")
	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();

		Collections.sort(sprintLeafNodeList, (Node o1, Node o2) -> o1.getSprintFilter().getStartDate()
				.compareTo(o2.getSprintFilter().getStartDate()));
		long time = System.currentTimeMillis();
		Map<String, Object> defectDataListMap = fetchKPIDataFromDb(sprintLeafNodeList, null, null, kpiRequest);
		log.info("DSR taking fetchKPIDataFromDb {}", System.currentTimeMillis() - time);

		List<SprintWiseStory> sprintWiseStoryList = (List<SprintWiseStory>) defectDataListMap.get(SPRINTSTORIES);
		Map<String, FieldMapping> projFieldMapping = (Map<String, FieldMapping>) defectDataListMap.get(PROJFMAPPING);

		Map<Pair<String, String>, List<SprintWiseStory>> sprintWiseMap = sprintWiseStoryList.stream().collect(Collectors
				.groupingBy(sws -> Pair.of(sws.getBasicProjectConfigId(), sws.getSprint()), Collectors.toList()));

		List<JiraIssue> totalDefects = (List<JiraIssue>) defectDataListMap.get(TOTALBUGKEY);
		Map<Pair<String, String>, List<JiraIssue>> unlinkedDefect = totalDefects.stream()
				.filter(issue -> CollectionUtils.isEmpty(issue.getDefectStoryID())).collect(Collectors.groupingBy(
						sws -> Pair.of(sws.getBasicProjectConfigId(), sws.getSprintID()), Collectors.toList()));

		List<KPIExcelData> excelData = new ArrayList<>();

		sprintLeafNodeList.forEach(node -> {
			// Leaf node wise data
			Set<String> uatLabels = new HashSet<>();
			Map<String, List<DataCount>> dataCountMap = new HashMap<>();
			String trendLineName = node.getProjectFilter().getName();
			String currentSprintComponentId = node.getSprintFilter().getId();
			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), currentSprintComponentId);
			FieldMapping fieldMapping = projFieldMapping
					.get(node.getProjectFilter().getBasicProjectConfigId().toString());

			List<SprintWiseStory> sprintWiseStories = sprintWiseMap.getOrDefault(currentNodeIdentifier,
					new ArrayList<>());
			List<String> totalStoryIdList = new ArrayList<>();// totalstories
			sprintWiseStories.stream().map(SprintWiseStory::getStoryList).collect(Collectors.toList())
					.forEach(totalStoryIdList::addAll);
			List<JiraIssue> subCategoryWiseTotalBugList = totalDefects.stream()
					.filter(f -> CollectionUtils.containsAny(f.getDefectStoryID(), totalStoryIdList))
					.collect(Collectors.toList());

			if (fieldMapping != null && !fieldMapping.isExcludeUnlinkedDefects()) {
				subCategoryWiseTotalBugList
						.addAll(unlinkedDefect.getOrDefault(currentNodeIdentifier, new ArrayList<>()));
			}
			Map<String, List<JiraIssue>> uatDefect = checkUATDefect(subCategoryWiseTotalBugList, fieldMapping,
					uatLabels);

			Map<String, Double> finalMap = new HashMap<>();
			Map<String, Object> overallHowerMap = new HashMap<>();
			List<DSRValidationData> validationDataList = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(uatLabels)) {
				for (String label : uatLabels) {
					List<JiraIssue> issueList = uatDefect.getOrDefault(label, new ArrayList<>());
					int totalDefectCount = subCategoryWiseTotalBugList.size();
					Map<String, Object> howerMap = new HashMap<>();
					Double dsrPercentage = 0d;
					if (CollectionUtils.isNotEmpty(issueList)
							&& CollectionUtils.isNotEmpty(subCategoryWiseTotalBugList)) {
						createDSRValidation(issueList, label, validationDataList);
						int uatDefectCount = issueList.size();
						howerMap.put(UAT, uatDefectCount);
						dsrPercentage = (double) Math.round((100.0 * uatDefectCount) / (totalDefectCount));
					} else {
						howerMap.put(UAT, 0);
					}
					howerMap.put(TOTAL, totalDefectCount);
					finalMap.put(StringUtils.capitalize(label), dsrPercentage);
					overallHowerMap.put(StringUtils.capitalize(label), howerMap);
				}

				uatLabels.forEach(label -> finalMap.computeIfAbsent(label, val -> 0D));
				Double overAllCount = finalMap.values().stream().mapToDouble(val -> val).sum();
				finalMap.put(CommonConstant.OVERALL, overAllCount);

				Map<String, Object> overHowerMap = new HashMap<>();
				overHowerMap.put(UAT, uatDefect.values().stream().mapToInt(List::size).sum());
				overHowerMap.put(TOTAL, subCategoryWiseTotalBugList.size());
				overallHowerMap.put(CommonConstant.OVERALL, overHowerMap);
			}

			createDataCount(trendValueList, node, dataCountMap, trendLineName, finalMap, overallHowerMap,
					subCategoryWiseTotalBugList.size());

			populateExcel(requestTrackerId, subCategoryWiseTotalBugList, validationDataList, excelData, node);
			mapTmp.get(node.getId()).setValue(dataCountMap);
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.DEFECT_SEEPAGE_RATE.getColumns());

	}

	private void createDSRValidation(List<JiraIssue> issueList, String label,
			List<DSRValidationData> validationDataList) {
		issueList.forEach(issue -> validationDataList.add(new DSRValidationData(issue.getNumber(), label)));
	}

	private void populateExcel(String requestTrackerId, List<JiraIssue> sprintWiseSubCategoryWiseTotalBugListMap,
			List<DSRValidationData> subCategoryWiseUatBugList, List<KPIExcelData> excelData, Node node) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			Map<String, JiraIssue> totalBugList = new HashMap<>();
			sprintWiseSubCategoryWiseTotalBugListMap.stream()
					.forEach(bugs -> totalBugList.putIfAbsent(bugs.getNumber(), bugs));

			KPIExcelUtility.populateDefectSeepageRateExcelData(node.getSprintFilter().getName(), totalBugList,
					subCategoryWiseUatBugList, excelData);
		}
	}

	private void createDataCount(List<DataCount> trendValueList, Node node, Map<String, List<DataCount>> dataCountMap,
			String trendLineName, Map<String, Double> finalMap, Map<String, Object> overallHowerMap, int totalBug) {
		if (MapUtils.isNotEmpty(finalMap)) {
			finalMap.forEach((label, value) -> {
				DataCount dataCount = getDataCountObject(node, trendLineName, overallHowerMap, label, value);
				trendValueList.add(dataCount);
				dataCountMap.computeIfAbsent(label, k -> new ArrayList<>()).add(dataCount);
			});
		} else {
			Map<String, Object> overHowerMap = new HashMap<>();
			overHowerMap.put(UAT, 0);
			overHowerMap.put(TOTAL, totalBug);
			Map<String, Object> defaultMap = new HashMap<>();
			defaultMap.put(CommonConstant.OVERALL, overHowerMap);

			DataCount dataCount = getDataCountObject(node, trendLineName, defaultMap, CommonConstant.OVERALL, 0D);
			trendValueList.add(dataCount);
			dataCountMap.computeIfAbsent(CommonConstant.OVERALL, k -> new ArrayList<>()).add(dataCount);
		}
	}

	private DataCount getDataCountObject(Node node, String trendLineName, Map<String, Object> overAllHoverValueMap,
			String key, Double value) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(value));
		dataCount.setSProjectName(trendLineName);
		dataCount.setSSprintID(node.getSprintFilter().getId());
		dataCount.setSSprintName(node.getSprintFilter().getName());
		dataCount.setValue(value);
		dataCount.setKpiGroup(key);
		dataCount.setHoverValue((Map<String, Object>) overAllHoverValueMap.get(key));

		return dataCount;
	}

	private Map<String, List<JiraIssue>> checkUATDefect(List<JiraIssue> testCaseList, FieldMapping fieldMapping,
			Set<String> labels) {
		Map<String, List<JiraIssue>> uatMap = new HashMap<>();
		if (null != fieldMapping && StringUtils.isNotEmpty(fieldMapping.getJiraBugRaisedByIdentification())
				&& CollectionUtils.isNotEmpty(fieldMapping.getJiraBugRaisedByValue())) {
			List<String> jiraBugRaisedByValue = fieldMapping.getJiraBugRaisedByValue();
			labels.addAll(new HashSet<>(jiraBugRaisedByValue));
			if (fieldMapping.getJiraBugRaisedByIdentification().trim().equalsIgnoreCase(Constant.LABELS)) {
				testCaseList.stream()
						.filter(jIssue -> CollectionUtils.isNotEmpty(jIssue.getLabels())
								&& jIssue.getLabels().stream().anyMatch(jiraBugRaisedByValue::contains))
						.forEach(jIssue -> jIssue.getLabels()
								.forEach(label -> uatMap.computeIfAbsent(label, k -> new ArrayList<>()).add(jIssue)));

			} else {
				testCaseList.stream()
						.filter(f -> NormalizedJira.THIRD_PARTY_DEFECT_VALUE.getValue()
								.equalsIgnoreCase(f.getDefectRaisedBy()))
						.collect(Collectors.toList()).stream()
						.filter(issue -> CollectionUtils.isNotEmpty(issue.getUatDefectGroup()))
						.forEach(issue -> issue.getUatDefectGroup()
								.forEach(label -> uatMap.computeIfAbsent(label, k -> new ArrayList<>()).add(issue)));
			}
		}

		return uatMap;
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI35(), KPICode.DEFECT_SEEPAGE_RATE.getKpiId());
	}

}
