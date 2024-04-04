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
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
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
 * This class proces the KPI request for Root Cause Analysis
 * 
 * @author tauakram
 *
 */
@Component
@Slf4j
public class RCAServiceImpl extends JiraKPIService<Long, List<Object>, Map<String, Object>> {

	public static final String UNCHECKED = "unchecked";
	public static final String CODE_ISSUE = "code issue";
	public static final String MISC = "Misc";
	private static final String SEPARATOR_ASTERISK = "*************************************";
	private static final String TOTAL_DEFECT_DATA = "totalBugKey";
	private static final String SPRINT_WISE_STORY_DATA = "storyData";
	private static final String DEV = "DeveloperKpi";
	@Autowired
	private JiraIssueRepository jiraIssueRepository;
	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private FilterHelperService flterHelperService;

	/**
	 * Gets Qualifier Type from KPICode enum
	 */
	@Override
	public String getQualifierType() {
		return KPICode.DEFECT_COUNT_BY_RCA.name();
	}

	/**
	 * Gets Kpi data based on kpi request
	 * 
	 * @param kpiRequest
	 * @param kpiElement
	 * @param treeAggregatorDetail
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

		log.debug("[RCA-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.DEFECT_COUNT_BY_RCA);

		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.DEFECT_COUNT_BY_RCA);

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
		log.debug("[RCA-AGGREGATED-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), root);

		return kpiElement;
	}

	/**
	 * This method is not implemented yet
	 * 
	 * @param objectMap
	 * @return
	 */
	@Override
	public Long calculateKPIMetrics(Map<String, Object> objectMap) {
		return 0L;
	}

	/**
	 * Fetches KPI data from DB
	 * 
	 * @param leafNodeList
	 * @param startDate
	 * @param endDate
	 * @return Fetch data from dB on date range and AHD filters.
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {

		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Object> resultListMap = new HashMap<>();
		List<String> sprintList = new ArrayList<>();
		List<String> basicProjectConfigIds = new ArrayList<>();
		List<String> defectType = new ArrayList<>();

		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		Map<String, Map<String, List<String>>> droppedDefects = new HashMap<>();
		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			sprintList.add(leaf.getSprintFilter().getId());
			basicProjectConfigIds.add(basicProjectConfigId.toString());

			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			// defectCountIssueType is same as rcaIssueType. Hence did not
			// created a
			// separate field mapping.
			if (null != fieldMapping && CollectionUtils.isNotEmpty(fieldMapping.getJiraDefectCountlIssueTypeKPI36())) {
				mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
						CommonUtils.convertToPatternList(fieldMapping.getJiraDefectCountlIssueTypeKPI36()));
			}
			KpiHelperService.getDroppedDefectsFilters(droppedDefects, basicProjectConfigId,fieldMapping.getResolutionTypeForRejectionRCAKPI36(), fieldMapping.getJiraDefectRejectionStatusRCAKPI36());
			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

		});
		/** additional filter **/
		KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.SCRUM, DEV, flterHelperService);

		mapOfFilters.put(JiraFeature.SPRINT_ID.getFieldValueInFeature(),
				sprintList.stream().distinct().collect(Collectors.toList()));
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		// Fetch Story ID List grouped by Sprint
		List<SprintWiseStory> sprintWiseStoryList = jiraIssueRepository.findIssuesGroupBySprint(mapOfFilters,
				uniqueProjectMap, kpiRequest.getFilterToShowOnTrend(), DEV);

		List<String> storyIdList = new ArrayList<>();
		sprintWiseStoryList.forEach(s -> storyIdList.addAll(s.getStoryList()));

		defectType.add(NormalizedJira.DEFECT_TYPE.getValue());
		mapOfFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(), defectType);

		// Fetch Defects linked with story ID's
		mapOfFilters.put(JiraFeature.DEFECT_STORY_ID.getFieldValueInFeature(), storyIdList);
		// remove keys when search defects based on stories
		mapOfFilters.remove(JiraFeature.SPRINT_ID.getFieldValueInFeature());

		List<JiraIssue> defectLinkedWithStory = jiraIssueRepository.findDefectCountByRCA(mapOfFilters);
		List<JiraIssue> defectListWoDrop = new ArrayList<>();
		addJiraIssueTodefectListWoDrop(defectLinkedWithStory, defectListWoDrop, droppedDefects);

		setDbQueryLogger(storyIdList, null, null, defectListWoDrop);
		resultListMap.put(SPRINT_WISE_STORY_DATA, sprintWiseStoryList);
		resultListMap.put(TOTAL_DEFECT_DATA, defectListWoDrop);

		return resultListMap;

	}

	/**
	 * This method populates KPI value to sprint leaf nodes. It also gives the trend
	 * analysis at sprint wise.
	 *
	 * @param mapTmp
	 * @param sprintLeafNodeList
	 * @param trendValueList
	 * @param kpiElement
	 * @param kpiRequest
	 */
	@SuppressWarnings(UNCHECKED)
	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();

		String startDate;
		String endDate;

		Collections.sort(sprintLeafNodeList, (Node o1, Node o2) -> o1.getSprintFilter().getStartDate()
				.compareTo(o2.getSprintFilter().getStartDate()));

		startDate = sprintLeafNodeList.get(0).getSprintFilter().getStartDate();
		endDate = sprintLeafNodeList.get(sprintLeafNodeList.size() - 1).getSprintFilter().getEndDate();

		Map<String, Object> storyDefectDataListMap = fetchKPIDataFromDb(sprintLeafNodeList, startDate, endDate,
				kpiRequest);

		List<SprintWiseStory> sprintWiseStoryList = (List<SprintWiseStory>) storyDefectDataListMap
				.get(SPRINT_WISE_STORY_DATA);

		Map<Pair<String, String>, List<SprintWiseStory>> sprintWiseMap = sprintWiseStoryList.stream().collect(Collectors
				.groupingBy(sws -> Pair.of(sws.getBasicProjectConfigId(), sws.getSprint()), Collectors.toList()));

		Map<Pair<String, String>, Map<String, Long>> sprintWiseRCAMap = new HashMap<>();
		List<KPIExcelData> excelData = new ArrayList<>();
		// Assumption: There will be no sprint without any story. If yes, then a
		// sprint will contain all defects.It is assumed that those defects will
		// be linked with story. Otherwise if there will be a case where a
		// sprint
		// is made of only defects and none of defects are linked to story, all
		// linked to sprint will create a bug.sprintWiseStoryMap will be empty.
		Map<Pair<String, String>, Long> sprintIssueRCACountMap = new HashMap<>();
		Map<String, Set<String>> projectWiseRCA = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseDefectDataListMap = new HashMap<>();
		sprintWiseMap.forEach((sprint, sprintWiseStories) -> {

			List<String> storyIdList = new ArrayList<>();
			sprintWiseStories.stream().map(SprintWiseStory::getStoryList).collect(Collectors.toList())
					.forEach(storyIdList::addAll);

			// get story linked defects
			List<JiraIssue> sprintWiseDefectDataList = ((List<JiraIssue>) storyDefectDataListMap.get(TOTAL_DEFECT_DATA))
					.stream().filter(f -> CollectionUtils.containsAny(f.getDefectStoryID(), storyIdList))
					.collect(Collectors.toList());

			Map<String, Long> rcaCountMap = sprintWiseDefectDataList.stream()
					.flatMap(f -> f.getRootCauseList().stream())
					.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

			setSprintWiseLogger(sprint, storyIdList, sprintWiseDefectDataList, rcaCountMap);

			sprintWiseRCAMap.put(sprint, rcaCountMap);
			sprintIssueRCACountMap.put(sprint, rcaCountMap.values().stream().reduce(0L, Long::sum));
			projectWiseRCA.computeIfAbsent(sprint.getLeft(), k -> new HashSet<>()).addAll(rcaCountMap.keySet());
			sprintWiseDefectDataListMap.put(sprint, sprintWiseDefectDataList);
		});

		sprintLeafNodeList.forEach(node -> {
			String trendLineName = node.getProjectFilter().getName();
			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), node.getSprintFilter().getId());

			Set<String> allRCA = projectWiseRCA
					.getOrDefault(node.getProjectFilter().getBasicProjectConfigId().toString(), new HashSet<>());
			Map<String, Long> rcaMap = sprintWiseRCAMap.getOrDefault(currentNodeIdentifier, new HashMap<>());
			List<JiraIssue> jiraIssueList = sprintWiseDefectDataListMap.get(currentNodeIdentifier);
			Map<String, Long> finalMap = new HashMap<>();
			Map<String, Object> overAllHoverValueMap = new HashMap<>();
			if (allRCA.size() > 1) {
				allRCA.forEach(rca -> {
					finalMap.put(StringUtils.capitalize(rca), rcaMap.getOrDefault(rca, 0L));
					Long rcaCount = rcaMap.values().stream().mapToLong(val -> val).sum();
					finalMap.put(CommonConstant.OVERALL, rcaCount);
					Integer rcaCountHover = rcaMap.getOrDefault(rca, 0L).intValue();
					overAllHoverValueMap.put(StringUtils.capitalize(rca), rcaCountHover);
				});
				populateExcelDataObject(requestTrackerId, excelData, jiraIssueList, node.getSprintFilter().getName());
			}
			Map<String, List<DataCount>> dataCountMap = new HashMap<>();

			finalMap.forEach((key, value) -> {
				DataCount dataCount = new DataCount();
				dataCount.setData(String.valueOf(value));
				dataCount.setSProjectName(trendLineName);
				dataCount.setSSprintID(node.getSprintFilter().getId());
				dataCount.setSSprintName(node.getSprintFilter().getName());
				dataCount.setValue(value);
				dataCount.setKpiGroup(key);
				Map<String, Object> hoverValueMap = new HashMap<>();
				if (key.equalsIgnoreCase(CommonConstant.OVERALL)) {
					dataCount.setHoverValue(overAllHoverValueMap);
				} else {
					hoverValueMap.put(key, value.intValue());
					dataCount.setHoverValue(hoverValueMap);
				}
				trendValueList.add(dataCount);
				dataCountMap.put(key, new ArrayList<>(Arrays.asList(dataCount)));
			});
			mapTmp.get(node.getId()).setValue(dataCountMap);
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.DEFECT_COUNT_BY_RCA.getColumns());
	}

	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			List<JiraIssue> sprintWiseDefectDataList, String name) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& !Objects.isNull(sprintWiseDefectDataList) && !sprintWiseDefectDataList.isEmpty()) {
			KPIExcelUtility.populateDefectRelatedExcelData(name, sprintWiseDefectDataList, excelData,
					KPICode.DEFECT_COUNT_BY_RCA.getKpiId());
		}

	}

	/**
	 * Sets DB Query Logger
	 *
	 * @param storyIdList
	 * @param defectLinkedWithSprint
	 * @param removeStoryLinkedWithDefectFoundFromSprintLinkage
	 * @param defectLinkedWithStory
	 */
	private void setDbQueryLogger(List<String> storyIdList, List<JiraIssue> defectLinkedWithSprint,
			List<JiraIssue> removeStoryLinkedWithDefectFoundFromSprintLinkage, List<JiraIssue> defectLinkedWithStory) {

		if (customApiConfig.getApplicationDetailedLogger().equalsIgnoreCase("on")) {
			log.info(SEPARATOR_ASTERISK);
			log.info("************* RCA (dB) *******************");
			log.info("Story: {}", storyIdList);
			log.info("DefectLinkedWith -> story[{}]: {}", defectLinkedWithStory.size(),
					defectLinkedWithStory.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));
			if (null != defectLinkedWithSprint) {
				log.info("DefectLinkedWith->SPRINT[{}]: {}", defectLinkedWithSprint.size(),
						defectLinkedWithSprint.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));
			}
			if (null != removeStoryLinkedWithDefectFoundFromSprintLinkage) {
				log.info("FilteredDefectLinkedWith->SPRINT[{}]: {}",
						removeStoryLinkedWithDefectFoundFromSprintLinkage.size(),
						removeStoryLinkedWithDefectFoundFromSprintLinkage.stream().map(JiraIssue::getNumber)
								.collect(Collectors.toList()));
			}
			log.info(SEPARATOR_ASTERISK);
			log.info("******************X----X*******************");
		}
	}

	/**
	 * Sets Sprint Wise Logger
	 *
	 * @param sprint
	 * @param storyIdList
	 * @param sprintWiseDefectDataList
	 * @param rcaCountMap
	 */
	private void setSprintWiseLogger(Pair<String, String> sprint, List<String> storyIdList,
			List<JiraIssue> sprintWiseDefectDataList, Map<String, Long> rcaCountMap) {

		if (customApiConfig.getApplicationDetailedLogger().equalsIgnoreCase("on")) {
			log.debug(SEPARATOR_ASTERISK);
			log.debug("************* SPRINT WISE RCA *******************");
			log.debug("Sprint: {}", sprint.getValue());
			log.debug("Story[{}]: {}", storyIdList.size(), storyIdList);
			log.debug("DefectDataList[{}]: {}", sprintWiseDefectDataList.size(),
					sprintWiseDefectDataList.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));
			log.debug("DefectRCAMap: {}", rcaCountMap);
			log.debug(SEPARATOR_ASTERISK);
			log.debug(SEPARATOR_ASTERISK);
		}
	}

	private void addJiraIssueTodefectListWoDrop(List<JiraIssue> defectLinkedWithStory, List<JiraIssue> defectListWoDrop,
			Map<String, Map<String, List<String>>> droppedDefects) {
		KpiHelperService.getDefectsWithoutDrop(droppedDefects, defectLinkedWithStory, defectListWoDrop);

	}

	@Override
	public Long calculateKpiValue(List<Long> valueList, String kpiName) {
		return calculateKpiValueForLong(valueList, kpiName);
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping){
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI36(),KPICode.DEFECT_COUNT_BY_RCA.getKpiId());
	}
}