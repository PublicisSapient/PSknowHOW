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

/**
 * 
 */
package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
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
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
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
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.DEFECT_SEEPAGE_RATE);
		List<DataCount> trendValues = getTrendValues(kpiRequest, kpiElement, nodeWiseKPIValue, KPICode.DEFECT_SEEPAGE_RATE);
		kpiElement.setTrendValueList(trendValues);

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
		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			projFieldMapping.put(leaf.getProjectFilter().getBasicProjectConfigId().toString(), fieldMapping);

			sprintList.add(leaf.getSprintFilter().getId());
			basicProjectConfigIds.add(basicProjectConfigId.toString());

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

		List<JiraIssue> totalDefectList = jiraIssueRepository.findIssueByStoryNumber(mapOfFiltersWithStoryIds,
				storyNumberList, null);
		List<JiraIssue> defectListWoDrop = new ArrayList<>();

		KpiHelperService.getDefectsWithoutDrop(droppedDefects, totalDefectList, defectListWoDrop);

		resultListMap.put(SPRINTSTORIES, sprintWiseStoryList);
		resultListMap.put(TOTALBUGKEY, defectListWoDrop);
		resultListMap.put(PROJFMAPPING, projFieldMapping);
		return resultListMap;

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
	 * @param kpiElement
	 * @param sprintLeafNodeList
	 * @param trendValueList
	 */
	@SuppressWarnings("unchecked")
	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();

		Collections.sort(sprintLeafNodeList, (Node o1, Node o2) -> o1.getSprintFilter().getStartDate()
				.compareTo(o2.getSprintFilter().getStartDate()));
		long time = System.currentTimeMillis();
		Map<String, Object> defectDataListMap = fetchKPIDataFromDb(sprintLeafNodeList, null, null, kpiRequest);
		log.info("DSR taking fetchKPIDataFromDb {}", String.valueOf(System.currentTimeMillis() - time));

		List<SprintWiseStory> sprintWiseStoryList = (List<SprintWiseStory>) defectDataListMap.get(SPRINTSTORIES);
		Map<String, FieldMapping> projFieldMapping = (Map<String, FieldMapping>) defectDataListMap.get(PROJFMAPPING);

		Map<Pair<String, String>, List<SprintWiseStory>> sprintWiseMap = sprintWiseStoryList.stream().collect(Collectors
				.groupingBy(sws -> Pair.of(sws.getBasicProjectConfigId(), sws.getSprint()), Collectors.toList()));

		Map<String, String> sprintIdSprintNameMap = sprintWiseStoryList.stream().collect(
				Collectors.toMap(SprintWiseStory::getSprint, SprintWiseStory::getSprintName, (name1, name2) -> name1));
		List<JiraIssue> totalDefects = (List<JiraIssue>) defectDataListMap.get(TOTALBUGKEY);
		Map<Pair<String, String>, Double> sprintWiseDsrMap = new HashMap<>();
		Map<Pair<String, String>, Map<String, Object>> sprintWiseHowerMap = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseSubCategoryWiseTotalBugListMap = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseSubCategoryWiseUATBugListMap = new HashMap<>();
		List<KPIExcelData> excelData = new ArrayList<>();

		sprintWiseMap.forEach((sprint, sprintWiseStories) -> {

			List<JiraIssue> sprintWiseUatDefectList = new ArrayList<>();
			List<JiraIssue> sprintWiseTotaldDefectList = new ArrayList<>();
			List<Double> subCategoryWiseDSRList = new ArrayList<>();
			List<String> totalStoryIdList = new ArrayList<>();

			sprintWiseStories.stream().map(SprintWiseStory::getStoryList).collect(Collectors.toList())
					.forEach(totalStoryIdList::addAll);

			List<JiraIssue> subCategoryWiseTotalBugList = totalDefects.stream()
					.filter(f -> CollectionUtils.containsAny(f.getDefectStoryID(), totalStoryIdList))
					.collect(Collectors.toList());
			List<JiraIssue> subCategoryWiseUatBugList = new ArrayList<>();

			if (CollectionUtils.isNotEmpty(subCategoryWiseTotalBugList)) {
				sprintWiseSubCategoryWiseTotalBugListMap.put(sprint, subCategoryWiseTotalBugList);
				subCategoryWiseUatBugList = checkUATDefect(subCategoryWiseTotalBugList, projFieldMapping);
				sprintWiseSubCategoryWiseUATBugListMap.put(sprint, subCategoryWiseUatBugList);
			}

			Map<String, Object> currentSprintLeafNodeDefectDataMap = new HashMap<>();
			currentSprintLeafNodeDefectDataMap.put(UATBUGKEY, subCategoryWiseUatBugList);
			currentSprintLeafNodeDefectDataMap.put(TOTALBUGKEY, subCategoryWiseTotalBugList);
			double dSRForCurrentLeaf = calculateKPIMetrics(currentSprintLeafNodeDefectDataMap);
			subCategoryWiseDSRList.add(dSRForCurrentLeaf);
			sprintWiseUatDefectList.addAll(subCategoryWiseUatBugList);
			sprintWiseTotaldDefectList.addAll(subCategoryWiseTotalBugList);
			sprintWiseDsrMap.put(sprint, dSRForCurrentLeaf);
			setHowerMap(sprintWiseHowerMap, sprint, sprintWiseUatDefectList, sprintWiseTotaldDefectList);
		});

		sprintLeafNodeList.forEach(node -> {
			// Leaf node wise data
			String trendLineName = node.getProjectFilter().getName();
			String currentSprintComponentId = node.getSprintFilter().getId();
			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), currentSprintComponentId);
			double dSRForCurrentLeaf;

			if (sprintWiseDsrMap.containsKey(currentNodeIdentifier)) {
				dSRForCurrentLeaf = sprintWiseDsrMap.get(currentNodeIdentifier);
				// if for populating excel data
				if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
					Map<String, JiraIssue> totalBugList = new HashMap<>();
					sprintWiseSubCategoryWiseTotalBugListMap.getOrDefault(currentNodeIdentifier, new ArrayList<>())
							.stream().forEach(bugs -> totalBugList.putIfAbsent(bugs.getNumber(), bugs));
					List<JiraIssue> subCategoryWiseUatBugList = sprintWiseSubCategoryWiseUATBugListMap
							.getOrDefault(currentNodeIdentifier, new ArrayList<>());
					KPIExcelUtility.populateDefectRelatedExcelData(node.getSprintFilter().getName(), totalBugList,
							subCategoryWiseUatBugList, excelData, KPICode.DEFECT_SEEPAGE_RATE.getKpiId());
				}
			} else {
				dSRForCurrentLeaf = 0.0d;
			}

			log.debug("[DSR-SPRINT-WISE][{}]. DSR for sprint {}  is {}", requestTrackerId,
					node.getSprintFilter().getName(), dSRForCurrentLeaf);

			DataCount dataCount = new DataCount();
			dataCount.setData(String.valueOf(Math.round(dSRForCurrentLeaf)));
			dataCount.setSProjectName(trendLineName);
			dataCount.setSSprintID(node.getSprintFilter().getId());
			dataCount.setSSprintName(node.getSprintFilter().getName());
			dataCount.setSprintIds(new ArrayList<>(Arrays.asList(node.getSprintFilter().getId())));
			dataCount.setSprintNames(new ArrayList<>(Arrays.asList(node.getSprintFilter().getName())));
			dataCount.setValue(dSRForCurrentLeaf);
			dataCount.setHoverValue(sprintWiseHowerMap.get(currentNodeIdentifier));
			mapTmp.get(node.getId()).setValue(new ArrayList<DataCount>(Arrays.asList(dataCount)));
			trendValueList.add(dataCount);

		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.DEFECT_SEEPAGE_RATE.getColumns());
	}

	private List<JiraIssue> checkUATDefect(List<JiraIssue> testCaseList, Map<String, FieldMapping> projFieldMapping) {
		List<JiraIssue> subCategoryWiseUatBugList = new ArrayList<>();
		String basicProjectId = testCaseList.get(0).getBasicProjectConfigId();
		FieldMapping fieldMapping = projFieldMapping.get(basicProjectId);
		if (null != fieldMapping && StringUtils.isNotEmpty(fieldMapping.getJiraBugRaisedByIdentification())) {
			if (fieldMapping.getJiraBugRaisedByIdentification().trim().equalsIgnoreCase(Constant.LABELS)) {
				for (JiraIssue jIssue : testCaseList) {
					List<String> commonLabel = jIssue.getLabels().stream()
							.filter(x -> fieldMapping.getJiraBugRaisedByValue().contains(x))
							.collect(Collectors.toList());
					if (CollectionUtils.isNotEmpty(commonLabel)) {
						subCategoryWiseUatBugList.add(jIssue);
					}
				}
			} else {
				subCategoryWiseUatBugList = testCaseList.stream().filter(
						f -> NormalizedJira.THIRD_PARTY_DEFECT_VALUE.getValue().equalsIgnoreCase(f.getDefectRaisedBy()))
						.collect(Collectors.toList());
			}
		}
		return subCategoryWiseUatBugList;
	}

	/**
	 * Sets map to show on hover of sprint node.
	 * 
	 * @param sprintWiseHowerMap
	 * @param sprint
	 * @param uat
	 * @param total
	 */
	private void setHowerMap(Map<Pair<String, String>, Map<String, Object>> sprintWiseHowerMap,
			Pair<String, String> sprint, List<JiraIssue> uat, List<JiraIssue> total) {
		Map<String, Object> howerMap = new LinkedHashMap<>();
		if (CollectionUtils.isNotEmpty(uat)) {
			howerMap.put(UAT, uat.size());
		} else {
			howerMap.put(UAT, 0);
		}
		if (CollectionUtils.isNotEmpty(total)) {
			howerMap.put(TOTAL, total.size());
		} else {
			howerMap.put(TOTAL, 0);
		}
		sprintWiseHowerMap.put(sprint, howerMap);
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping){
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI35(),KPICode.DEFECT_SEEPAGE_RATE.getKpiId());
	}

}
