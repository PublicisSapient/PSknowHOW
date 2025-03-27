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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.common.service.KpiDataCacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiDataProvider;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.Filters;
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
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;

import lombok.extern.slf4j.Slf4j;

/**
 * This class calculates the DSR and trend analysis of the DSR.
 *
 * @author pkum34
 */
@Component
@Slf4j
public class DSRServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String UATBUGKEY = "uatBugData";
	private static final String TOTALBUGKEY = "totalBugData";
	private static final String SPRINTSTORIES = "storyData";
	private static final String UAT = "Escaped Defects";
	private static final String TOTAL = "Total Defects";
	private static final String PROJFMAPPING = "projectFieldMapping";
	public static final String STORY_LIST_WO_DROP = "storyList";

    @Autowired
	private FilterHelperService filterHelperService;
	@Autowired
	private CacheService cacheService;
	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private KpiDataCacheService kpiDataCacheService;
	@Autowired
	private KpiDataProvider kpiDataProvider;

	private List<String> sprintIdList = Collections.synchronizedList(new ArrayList<>());

	@Override
	public String getQualifierType() {
		return KPICode.DEFECT_SEEPAGE_RATE.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, TreeAggregatorDetail treeAggregatorDetail)
			throws ApplicationException {

		List<DataCount> trendValueList = new ArrayList<>();
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		sprintIdList = treeAggregatorDetail.getMapOfListOfLeafNodes().get(CommonConstant.SPRINT_MASTER).stream()
				.map(node -> node.getSprintFilter().getId()).collect(Collectors.toList());
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
			projectWiseDc.entrySet().forEach(trend -> dataList.addAll(trend.getValue()));
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
		Map<ObjectId, List<String>> projectWiseSprints = new HashMap<>();
		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			String sprint = leaf.getSprintFilter().getId();
			projectWiseSprints.putIfAbsent(basicProjectConfigId, new ArrayList<>());
			projectWiseSprints.get(basicProjectConfigId).add(sprint);
		});
		
		List<SprintWiseStory> sprintWiseStoryList = new ArrayList<>();
		List<JiraIssue> remainingDefect = new ArrayList<>();
		Map<String, FieldMapping> projFieldMapping = new HashMap<>();
		List<JiraIssue> storyListWoDrop = new ArrayList<>();
		boolean fetchCachedData = filterHelperService.isFilterSelectedTillSprintLevel(kpiRequest.getLevel(), false);
		projectWiseSprints.forEach((basicProjectConfigId, sprintList) -> {
			Map<String, Object> result;
			if (fetchCachedData) { // fetch data from cache only if Filter is selected till Sprint
				// level.
				result = kpiDataCacheService.fetchDSRData(kpiRequest, basicProjectConfigId, sprintIdList,
						KPICode.DEFECT_SEEPAGE_RATE.getKpiId());
			} else { // fetch data from DB if filters below Sprint level (i.e. additional filters)
				result = kpiDataProvider.fetchDSRData(kpiRequest, basicProjectConfigId, sprintList);
			}

			sprintWiseStoryList.addAll((List<SprintWiseStory>) result.getOrDefault(SPRINTSTORIES, new ArrayList<>()));
			remainingDefect.addAll((List<JiraIssue>) result.getOrDefault(TOTALBUGKEY, new ArrayList<>()));
			projFieldMapping.put(basicProjectConfigId.toString(),
					((Map<String, FieldMapping>) result.get(PROJFMAPPING)).get(basicProjectConfigId.toString()));
			storyListWoDrop.addAll((List<JiraIssue>) result.getOrDefault(STORY_LIST_WO_DROP, new ArrayList<>()));
		});
		
		resultListMap.put(SPRINTSTORIES, sprintWiseStoryList);
		resultListMap.put(TOTALBUGKEY, remainingDefect);
		resultListMap.put(PROJFMAPPING, projFieldMapping);
		resultListMap.put(STORY_LIST_WO_DROP, storyListWoDrop);
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
	 *          mapTmp
	 * @param kpiElement
	 *          kpiElement
	 * @param sprintLeafNodeList
	 *          sprintLeafNodeList
	 * @param trendValueList
	 *          trendValueList
	 */
	@SuppressWarnings("unchecked")
	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();

		Collections.sort(sprintLeafNodeList,
				(Node o1, Node o2) -> o1.getSprintFilter().getStartDate().compareTo(o2.getSprintFilter().getStartDate()));
		long time = System.currentTimeMillis();
		Map<String, Object> defectDataListMap = fetchKPIDataFromDb(sprintLeafNodeList, null, null, kpiRequest);
		log.info("DSR taking fetchKPIDataFromDb {}", System.currentTimeMillis() - time);

		List<SprintWiseStory> sprintWiseStoryList = (List<SprintWiseStory>) defectDataListMap.get(SPRINTSTORIES);
		Map<String, FieldMapping> projFieldMapping = (Map<String, FieldMapping>) defectDataListMap.get(PROJFMAPPING);

		Map<Pair<String, String>, List<SprintWiseStory>> sprintWiseMap = sprintWiseStoryList.stream().collect(
				Collectors.groupingBy(sws -> Pair.of(sws.getBasicProjectConfigId(), sws.getSprint()), Collectors.toList()));
		List<JiraIssue> totalStoryWoDrop = (List<JiraIssue>) defectDataListMap.get(STORY_LIST_WO_DROP);
		List<JiraIssue> totalDefects = (List<JiraIssue>) defectDataListMap.get(TOTALBUGKEY);
		Map<Pair<String, String>, List<JiraIssue>> unlinkedDefect = totalDefects.stream()
				.filter(issue -> CollectionUtils.isEmpty(issue.getDefectStoryID())).collect(Collectors
						.groupingBy(sws -> Pair.of(sws.getBasicProjectConfigId(), sws.getSprintID()), Collectors.toList()));

		List<KPIExcelData> excelData = new ArrayList<>();

		sprintLeafNodeList.forEach(node -> {
			// Leaf node wise data
			Set<String> uatLabels = new HashSet<>();
			Map<String, List<DataCount>> dataCountMap = new HashMap<>();
			String trendLineName = node.getProjectFilter().getName();
			String currentSprintComponentId = node.getSprintFilter().getId();
			Pair<String, String> currentNodeIdentifier = Pair.of(node.getProjectFilter().getBasicProjectConfigId().toString(),
					currentSprintComponentId);
			FieldMapping fieldMapping = projFieldMapping.get(node.getProjectFilter().getBasicProjectConfigId().toString());

			List<SprintWiseStory> sprintWiseStories = sprintWiseMap.getOrDefault(currentNodeIdentifier, new ArrayList<>());
			List<String> totalStoryIdList = new ArrayList<>(); // totalstories
			sprintWiseStories.stream().map(SprintWiseStory::getStoryList).toList()
					.forEach(totalStoryIdList::addAll);
			List<JiraIssue> subCategoryWiseTotalBugList = totalDefects.stream()
					.filter(f -> CollectionUtils.containsAny(f.getDefectStoryID(), totalStoryIdList))
					.collect(Collectors.toList());

			if (fieldMapping != null && !fieldMapping.isExcludeUnlinkedDefects()) {
				subCategoryWiseTotalBugList.addAll(unlinkedDefect.getOrDefault(currentNodeIdentifier, new ArrayList<>()));
			}
			Map<String, List<JiraIssue>> uatDefect = checkUATDefect(subCategoryWiseTotalBugList, fieldMapping, uatLabels);

			Map<String, Double> finalMap = new HashMap<>();
			Map<String, Object> overallHowerMap = new HashMap<>();
			List<DSRValidationData> validationDataList = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(uatLabels)) {
				for (String lowerCaseLabel : uatLabels) {
					String label = lowerCaseLabel.toLowerCase();
					List<JiraIssue> issueList = uatDefect.getOrDefault(label, new ArrayList<>());
					int totalDefectCount = subCategoryWiseTotalBugList.size();
					Map<String, Object> howerMap = new HashMap<>();
					Double dsrPercentage = Double.NaN;
					if (CollectionUtils.isNotEmpty(subCategoryWiseTotalBugList)) {
						createDSRValidation(issueList, label, validationDataList);
						int uatDefectCount = issueList.size();
						howerMap.put(UAT, uatDefectCount);
						dsrPercentage = (double) Math.round((100.0 * uatDefectCount) / (totalDefectCount));
					} else {
						howerMap.put(UAT, 0);
					}
					howerMap.put(TOTAL, totalDefectCount);
					finalMap.put(label, dsrPercentage);
					overallHowerMap.put(label, howerMap);
				}

				uatLabels.forEach(label -> finalMap.computeIfAbsent(label, val -> Double.NaN));
				Double overAllCount = finalMap.values().stream().filter(val -> !Double.isNaN(val)).mapToDouble(val -> val).sum();
				if (finalMap.values().stream().allMatch(a->Double.isNaN(a))) {
					overAllCount = Double.NaN;
				}
				finalMap.put(CommonConstant.OVERALL, overAllCount);

				Map<String, Object> overHowerMap = new HashMap<>();
				overHowerMap.put(UAT, uatDefect.values().stream().mapToInt(List::size).sum());
				overHowerMap.put(TOTAL, subCategoryWiseTotalBugList.size());
				overallHowerMap.put(CommonConstant.OVERALL, overHowerMap);
			}

			createDataCount(trendValueList, node, dataCountMap, trendLineName, finalMap, overallHowerMap,
					subCategoryWiseTotalBugList.size());

			populateExcel(requestTrackerId, subCategoryWiseTotalBugList, validationDataList, excelData, node,
					totalStoryWoDrop);
			mapTmp.get(node.getId()).setValue(dataCountMap);
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(
				KPIExcelColumn.DEFECT_SEEPAGE_RATE.getColumns(sprintLeafNodeList, cacheService, filterHelperService));
	}

	private void createDSRValidation(List<JiraIssue> issueList, String label,
			List<DSRValidationData> validationDataList) {
		issueList.forEach(issue -> validationDataList.add(new DSRValidationData(issue.getNumber(), label)));
	}

	private void populateExcel(String requestTrackerId, List<JiraIssue> sprintWiseSubCategoryWiseTotalBugListMap,
			List<DSRValidationData> subCategoryWiseUatBugList, List<KPIExcelData> excelData, Node node,
			List<JiraIssue> totalStoryWoDrop) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			Map<String, JiraIssue> totalBugList = new HashMap<>();
			sprintWiseSubCategoryWiseTotalBugListMap.forEach(bugs -> totalBugList.putIfAbsent(bugs.getNumber(), bugs));

			KPIExcelUtility.populateDefectSeepageRateExcelData(node.getSprintFilter().getName(), totalBugList,
					subCategoryWiseUatBugList, excelData, customApiConfig, totalStoryWoDrop);
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

			DataCount dataCount = getDataCountObject(node, trendLineName, defaultMap, CommonConstant.OVERALL, Double.NaN);
			trendValueList.add(dataCount);
			dataCountMap.computeIfAbsent(CommonConstant.OVERALL, k -> new ArrayList<>()).add(dataCount);
		}
	}

	private DataCount getDataCountObject(Node node, String trendLineName, Map<String, Object> overAllHoverValueMap,
			String key, Double value) {
		DataCount dataCount = new DataCount();
		if(!Double.isNaN(value)) {
			dataCount.setData(String.valueOf(value));
			dataCount.setValue(value);
		}
		dataCount.setHoverValue((Map<String, Object>) overAllHoverValueMap.get(key));
		dataCount.setSProjectName(trendLineName);
		dataCount.setSSprintID(node.getSprintFilter().getId());
		dataCount.setSSprintName(node.getSprintFilter().getName());

		dataCount.setKpiGroup(key);


		return dataCount;
	}

	private Map<String, List<JiraIssue>> checkUATDefect(List<JiraIssue> testCaseList, FieldMapping fieldMapping,
			Set<String> labels) {
		Map<String, List<JiraIssue>> uatMap = new HashMap<>();
		if (null != fieldMapping && StringUtils.isNotEmpty(fieldMapping.getJiraBugRaisedByIdentification()) &&
				CollectionUtils.isNotEmpty(fieldMapping.getJiraBugRaisedByValue())) {
			Set<String> jiraBugRaisedByValue = new HashSet<>();
			fieldMapping.getJiraBugRaisedByValue().forEach(value -> jiraBugRaisedByValue.add(value.toLowerCase()));
			labels.addAll(jiraBugRaisedByValue);
			if (fieldMapping.getJiraBugRaisedByIdentification().trim().equalsIgnoreCase(Constant.LABELS)) {
				testCaseList.stream()
						.filter(testCase -> CollectionUtils.isNotEmpty(testCase.getLabels()) &&
								testCase.getLabels().stream().anyMatch(label -> jiraBugRaisedByValue.contains(label.toLowerCase())))
						.forEach(jIssue -> jIssue.getLabels()
								.forEach(label -> uatMap.computeIfAbsent(label.toLowerCase(), k -> new ArrayList<>()).add(jIssue)));

			} else {
				testCaseList.stream()
						.filter(f -> NormalizedJira.THIRD_PARTY_DEFECT_VALUE.getValue()
								.equalsIgnoreCase(f.getDefectRaisedBy()))
						.toList().stream().filter(issue -> CollectionUtils.isNotEmpty(issue.getUatDefectGroup()))
						.forEach(issue -> issue.getUatDefectGroup().forEach(label -> uatMap
								.computeIfAbsent(label.toLowerCase(), k -> new ArrayList<>()).add(issue)));
			}
		}
		// removing for overall filter
		uatMap.keySet().removeIf(key -> !labels.contains(key));
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
