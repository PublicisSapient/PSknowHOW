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
import java.util.Set;
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
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
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
 * This class fetches the defect count KPI along with trend analysis. Trend
 * analysis for Defect Count KPI has total defect count at y-axis and sprint id
 * at x-axis. {@link JiraKPIService}
 * 
 * @author tauakram
 *
 */
@Component
@Slf4j
public class DCServiceImpl extends JiraKPIService<Long, List<Object>, Map<String, Object>> {

	public static final String UNCHECKED = "unchecked";
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
	 * {@inheritDoc}
	 */
	@Override
	public String getQualifierType() {
		return KPICode.DEFECT_COUNT_BY_PRIORITY.name();
	}

	/**
	 * {@inheritDoc}
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

		log.debug("[DC-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.DEFECT_COUNT_BY_PRIORITY);
		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.DEFECT_COUNT_BY_PRIORITY);

		trendValuesMap = sortTrendValueMap(trendValuesMap, priorityTypes(true));
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long calculateKPIMetrics(Map<String, Object> objectMap) {
		return 0L;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(final List<Node> leafNodeList, final String startDate,
			final String endDate, final KpiRequest kpiRequest) {

		List<String> sprintList = new ArrayList<>();
		List<String> basicProjectConfigIds = new ArrayList<>();

		final Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		final Map<String, Map<String, List<String>>> droppedDefects = new HashMap<>();
		final List<String> defectType = new ArrayList<>();

		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			sprintList.add(leaf.getSprintFilter().getId());
			basicProjectConfigIds.add(basicProjectConfigId.toString());

			final Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();

			final FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

			mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
					CommonUtils.convertToPatternList(fieldMapping.getJiraDefectCountlIssueTypeKPI28()));
			KpiHelperService.getDroppedDefectsFilters(droppedDefects, basicProjectConfigId, fieldMapping.getResolutionTypeForRejectionKPI28(),fieldMapping.getJiraDefectRejectionStatusKPI28());
			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

		});

		final Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		final Map<String, Object> resultListMap = new HashMap<>();

		/** additional filter **/
		KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.SCRUM, DEV, flterHelperService);

		mapOfFilters.put(JiraFeature.SPRINT_ID.getFieldValueInFeature(),
				sprintList.stream().distinct().collect(Collectors.toList()));
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		// Fetch Story ID List grouped by Sprint
		List<SprintWiseStory> sprintWiseStoryList = jiraIssueRepository.findIssuesGroupBySprint(mapOfFilters,
				uniqueProjectMap, kpiRequest.getFilterToShowOnTrend(), DEV);

		final List<String> storyIdList = new ArrayList<>();
		sprintWiseStoryList.forEach(s -> storyIdList.addAll(s.getStoryList()));

		defectType.add(NormalizedJira.DEFECT_TYPE.getValue());
		mapOfFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(), defectType);

		// remove keys when search defects based on stories
		mapOfFilters.remove(JiraFeature.SPRINT_ID.getFieldValueInFeature());

		// Fetch Defects linked with story ID's
		mapOfFilters.put(JiraFeature.DEFECT_STORY_ID.getFieldValueInFeature(), storyIdList);

		List<JiraIssue> defectListWoDrop = getDefectListWoDrop(jiraIssueRepository.findIssuesByType(mapOfFilters),
				droppedDefects);
		setDbQueryLogger(storyIdList, defectListWoDrop);
		resultListMap.put(SPRINT_WISE_STORY_DATA, sprintWiseStoryList);
		resultListMap.put(TOTAL_DEFECT_DATA, defectListWoDrop);
		return resultListMap;

	}

	/**
	 * Get DefectList without Drop
	 * 
	 * @param defectLinkedWithStory
	 * @param droppedDefects
	 * @return
	 */
	private List<JiraIssue> getDefectListWoDrop(List<JiraIssue> defectLinkedWithStory,
			Map<String, Map<String, List<String>>> droppedDefects) {
		List<JiraIssue> defectListWoDrop = new ArrayList<>();
		KpiHelperService.getDefectsWithoutDrop(droppedDefects, defectLinkedWithStory, defectListWoDrop);

		return defectListWoDrop;
	}

	/**
	 * This method populates KPI value to sprint leaf nodes. It also gives the trend
	 * analysis at sprint wise.
	 * 
	 * @param mapTmp
	 *            node id map
	 * @param sprintLeafNodeList
	 *            sprint nodes list
	 * @param trendValueList
	 *            list to hold trend nodes data
	 * @param kpiElement
	 *            the KpiElement
	 * @param kpiRequest
	 *            the KpiRequest
	 */
	@SuppressWarnings(UNCHECKED)
	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();

		String startDate;
		String endDate;

		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));

		startDate = sprintLeafNodeList.get(0).getSprintFilter().getStartDate();
		endDate = sprintLeafNodeList.get(sprintLeafNodeList.size() - 1).getSprintFilter().getEndDate();

		Map<String, Object> storyDefectDataListMap = fetchKPIDataFromDb(sprintLeafNodeList, startDate, endDate,
				kpiRequest);

		List<SprintWiseStory> sprintWiseStoryList = (List<SprintWiseStory>) storyDefectDataListMap
				.get(SPRINT_WISE_STORY_DATA);

		Map<Pair<String, String>, List<SprintWiseStory>> sprintWiseMap = sprintWiseStoryList.stream().collect(Collectors
				.groupingBy(sws -> Pair.of(sws.getBasicProjectConfigId(), sws.getSprint()), Collectors.toList()));

		Map<Pair<String, String>, Map<String, Long>> sprintWiseDCPriorityMap = new HashMap<>();
		Map<Pair<String, String>, Integer> sprintWiseTDCMap = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseDefectDataListMap = new HashMap<>();

		List<KPIExcelData> excelData = new ArrayList<>();

		Set<String> projectWisePriorityList = new HashSet<>();
		sprintWiseMap.forEach((sprintFilter, sprintWiseStories) -> {

			List<String> storyIdList = new ArrayList<>();
			sprintWiseStories.stream().map(SprintWiseStory::getStoryList).collect(Collectors.toList())
					.forEach(storyIdList::addAll);

			List<JiraIssue> sprintWiseDefectDataList = ((List<JiraIssue>) storyDefectDataListMap.get(TOTAL_DEFECT_DATA))
					.stream().filter(f -> CollectionUtils.containsAny(f.getDefectStoryID(), storyIdList))
					.collect(Collectors.toList());
			// Below code is needed if defect->sprint linkage is considered

			Map<String, Long> priorityCountMap = KPIHelperUtil.setpriorityScrum(sprintWiseDefectDataList,
					customApiConfig);
			projectWisePriorityList.addAll(priorityCountMap.keySet());
			sprintWiseDefectDataListMap.put(sprintFilter, sprintWiseDefectDataList);

			setSprintWiseLogger(sprintFilter, storyIdList, sprintWiseDefectDataList, priorityCountMap);

			sprintWiseDCPriorityMap.put(sprintFilter, priorityCountMap);
			sprintWiseTDCMap.put(sprintFilter, sprintWiseDefectDataList.size());
		});

		sprintLeafNodeList.forEach(node -> {
			String trendLineName = node.getProjectFilter().getName();
			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), node.getSprintFilter().getId());

			Map<String, List<DataCount>> dataCountMap = new HashMap<>();
			Map<String, Long> priorityMap = sprintWiseDCPriorityMap.getOrDefault(currentNodeIdentifier,
					new HashMap<>());
			Map<String, Long> finalMap = new HashMap<>();
			Map<String, Object> overAllHoverValueMap = new HashMap<>();
			if (CollectionUtils.isNotEmpty(projectWisePriorityList)) {
				projectWisePriorityList.forEach(priority -> {
					Long rcaWiseCount = priorityMap.getOrDefault(priority, 0L);
					finalMap.put(StringUtils.capitalize(priority), rcaWiseCount);
					overAllHoverValueMap.put(StringUtils.capitalize(priority), rcaWiseCount.intValue());
				});
				projectWisePriorityList.forEach(priority -> finalMap.computeIfAbsent(priority, val -> 0L));
				Long overAllCount = finalMap.values().stream().mapToLong(val -> val).sum();
				finalMap.put(CommonConstant.OVERALL, overAllCount);

				String finalTrendLineName = trendLineName;
				finalMap.forEach((priority, value) -> {
					DataCount dataCount = getDataCountObject(node, finalTrendLineName, overAllHoverValueMap, priority,
							value);
					trendValueList.add(dataCount);
					dataCountMap.computeIfAbsent(priority, k -> new ArrayList<>()).add(dataCount);
				});

				populateExcelDataObject(requestTrackerId, node.getSprintFilter().getName(), excelData,
						sprintWiseDefectDataListMap.get(currentNodeIdentifier));
			}
			log.debug("[DC-SPRINT-WISE][{}]. DC for sprint {}  is {} and trend value is {}", requestTrackerId,
					node.getSprintFilter().getName(), sprintWiseDCPriorityMap.get(currentNodeIdentifier),
					sprintWiseTDCMap.get(currentNodeIdentifier));
			mapTmp.get(node.getId()).setValue(dataCountMap);

		});

		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.DEFECT_COUNT_BY_PRIORITY.getColumns());

	}

	/**
	 *
	 * @param node
	 * @param trendLineName
	 * @param overAllHoverValueMap
	 * @param key
	 * @param value
	 * @return
	 */
	private DataCount getDataCountObject(Node node, String trendLineName, Map<String, Object> overAllHoverValueMap,
			String key, Long value) {
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
		return dataCount;
	}

	private void populateExcelDataObject(String requestTrackerId, String sprintName, List<KPIExcelData> excelData,
			List<JiraIssue> sprintWiseDefectDataList) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			KPIExcelUtility.populateDefectRelatedExcelData(sprintName, sprintWiseDefectDataList, excelData,
					KPICode.DEFECT_COUNT_BY_PRIORITY.getKpiId());

		}
	}

	/**
	 * Sets logging data.
	 * 
	 * @param storyIdList
	 *            list of story ids
	 * @param defectLinkedWithStory
	 *            defects linked with story
	 */
	private void setDbQueryLogger(List<String> storyIdList, List<JiraIssue> defectLinkedWithStory) {

		if (customApiConfig.getApplicationDetailedLogger().equalsIgnoreCase("on")) {
			log.info(SEPARATOR_ASTERISK);
			log.info("************* DC (dB) *******************");
			log.info("Story[{}]: {}", storyIdList.size(), storyIdList);
			log.info("DefectLinkedWith -> story[{}]: {}", defectLinkedWithStory.size(),
					defectLinkedWithStory.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));
			log.info(SEPARATOR_ASTERISK);
			log.info("******************X----X*******************");
		}
	}

	/**
	 * Sets logger to log sprint level details.
	 * 
	 * @param sprint
	 *            unique key to identify sprint
	 * @param storyIdList
	 *            story id list
	 * @param sprintWiseDefectDataList
	 *            defects for current sprint
	 * @param priorityCountMap
	 *            priority map of defects
	 */
	private void setSprintWiseLogger(Pair<String, String> sprint, List<String> storyIdList,
			List<JiraIssue> sprintWiseDefectDataList, Map<String, Long> priorityCountMap) {

		if (customApiConfig.getApplicationDetailedLogger().equalsIgnoreCase("on")) {
			log.info(SEPARATOR_ASTERISK);
			log.info("************* SPRINT WISE DC *******************");
			log.info("Sprint: {}", sprint.getValue());
			log.info("Story[{}]: {}", storyIdList.size(), storyIdList);
			log.info("DefectDataList[{}]: {}", sprintWiseDefectDataList.size(),
					sprintWiseDefectDataList.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));
			log.info("DefectPriorityCountMap: {}", priorityCountMap);
			log.info(SEPARATOR_ASTERISK);
			log.info(SEPARATOR_ASTERISK);
		}
	}

	@Override
	public Long calculateKpiValue(List<Long> valueList, String kpiName) {
		return calculateKpiValueForLong(valueList, kpiName);
	}

	private Map<String, List<DataCount>> sortTrendValueMap(Map<String, List<DataCount>> trendMap,
			List<String> keyOrder) {
		Map<String, List<DataCount>> sortedMap = new LinkedHashMap<>();
		keyOrder.forEach(order -> {
			if (null != trendMap.get(order)) {
				sortedMap.put(order, trendMap.get(order));
			}
		});
		return sortedMap;
	}

	private List<String> priorityTypes(boolean addOverall) {
		if (addOverall) {
			return Arrays.asList(CommonConstant.OVERALL, Constant.P1, Constant.P2, Constant.P3, Constant.P4,
					Constant.MISC);
		} else {
			return Arrays.asList(Constant.P1, Constant.P2, Constant.P3, Constant.P4, Constant.MISC);
		}
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping){
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI28(),KPICode.DEFECT_COUNT_BY_PRIORITY.getKpiId());
	}
}