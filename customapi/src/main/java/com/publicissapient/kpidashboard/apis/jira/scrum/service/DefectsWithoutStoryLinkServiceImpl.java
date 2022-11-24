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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ValidationData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

@Component
public class DefectsWithoutStoryLinkServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	public static final String UNCHECKED = "unchecked";
	private static final Logger LOGGER = LoggerFactory.getLogger(DefectsWithoutStoryLinkServiceImpl.class);
	private static final String DEFECT_WITHOUT_STORY_LINK = "Defects Without Story Link";
	private static final String DEFECT_LIST = "Total Defects";
	private static final String STORY_LIST = "stories";

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
								 TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();

		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {

			Filters filters = Filters.getFilter(k);
			if (Filters.SPRINT == filters) {
				projectWiseLeafNodeValue(mapTmp, v, kpiElement, kpiRequest);
			}
		});

		LOGGER.debug("[DEFECT-WITHOUT-STORYLINK]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId());
		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		// no aggregation required
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.DEFECTS_WITHOUT_STORY_LINK);

		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, nodeWiseKPIValue, KPICode.DEFECTS_WITHOUT_STORY_LINK);
		Map<String, Map<String, List<DataCount>>> prioriryWiseProjectWiseDC = new LinkedHashMap<>();

		trendValuesMap.forEach((priorityWise, dataCounts) -> {
			Map<String, List<DataCount>> projectWiseDc = dataCounts.stream()
					.collect(Collectors.groupingBy(DataCount::getData));
			prioriryWiseProjectWiseDC.put(priorityWise, projectWiseDc);
		});

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		prioriryWiseProjectWiseDC.forEach((issueType, projectWiseDc) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			List<DataCount> dataList = new ArrayList<>();
			projectWiseDc.entrySet().stream().forEach(trend -> dataList.addAll(trend.getValue()));
			dataCountGroup.setFilter(issueType);
			dataCountGroup.setValue(dataList);
			dataCountGroups.add(dataCountGroup);
		});

		kpiElement.setTrendValueList(dataCountGroups);
		kpiElement.setNodeWiseKPIValue(nodeWiseKPIValue);
		return kpiElement;
	}

	@Override
	public Integer calculateKPIMetrics(Map<String, Object> subCategoryMap) {
		return null;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
												  KpiRequest kpiRequest) {

		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Object> resultListMap = new HashMap<>();
		List<String> basicProjectConfigIds = new ArrayList<>();
		List<String> defectType = new ArrayList<>();
		Map<String, Map<String, Object>> uniqueProjectIssueTypeNotIn = new HashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();

		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> uniqueProjectIssueStatusMap = new HashMap<>();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
			basicProjectConfigIds.add(basicProjectConfigId.toString());

			List<String> ignoreStatusList = new ArrayList<>();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

			if (null != fieldMapping) {
				mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
						CommonUtils.convertToPatternList(fieldMapping.getJiraStoryIdentification()));
				ignoreStatusList.addAll(
						CollectionUtils.isEmpty(fieldMapping.getJiraDefectDroppedStatus()) ? Lists.newArrayList()
								: fieldMapping.getJiraDefectDroppedStatus());
				uniqueProjectIssueStatusMap.put(JiraFeature.JIRA_ISSUE_STATUS.getFieldValueInFeature(),
						CommonUtils.convertToPatternList(ignoreStatusList));
				uniqueProjectIssueTypeNotIn.put(basicProjectConfigId.toString(), uniqueProjectIssueStatusMap);
				uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

			}
		});

		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));
		List<JiraIssue> storyList = jiraIssueRepository.findIssuesBySprintAndType(mapOfFilters, uniqueProjectMap);

		List<String> storyIssueNumberList = storyList.stream().map(JiraIssue::getNumber).collect(Collectors.toList());

		resultListMap.put(STORY_LIST, storyIssueNumberList);
		defectType.add(NormalizedJira.DEFECT_TYPE.getValue());
		mapOfFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(), defectType);

		resultListMap.put(DEFECT_LIST,
				jiraIssueRepository.findDefectsWithoutStoryLink(mapOfFilters, uniqueProjectIssueTypeNotIn));
		return resultListMap;

	}

	@Override
	public String getQualifierType() {
		return KPICode.DEFECTS_WITHOUT_STORY_LINK.name();
	}

	/**
	 * Populates KPI value to sprint leaf nodes and gives the trend analysis at
	 * sprint level.
	 *
	 * @param mapTmp
	 * @param sprintLeafNodeList
	 * @param kpiElement
	 * @param kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
										  KpiElement kpiElement, KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();

		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));

		List<Node> latestSprintNode = new ArrayList<>();
		Node latestSprint = sprintLeafNodeList.get(0);
		Optional.ofNullable(latestSprint).ifPresent(latestSprintNode::add);

		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);
		List<KPIExcelData> excelData = new ArrayList<>();
		List<JiraIssue> totalDefects = checkPriority((List<JiraIssue>) resultMap.get(DEFECT_LIST));
		List<JiraIssue> totalStories = (List<JiraIssue>) resultMap.get(STORY_LIST);
		List<JiraIssue> defectWithoutStory = new ArrayList<>();
		defectWithoutStory.addAll(
				totalDefects.stream().filter(f -> !CollectionUtils.containsAny(f.getDefectStoryID(), totalStories))
						.collect(Collectors.toList()));

		if (CollectionUtils.isNotEmpty(totalDefects)) {
			Map<String, List<JiraIssue>> priorityWiseWSIssues = defectWithoutStory.stream()
					.collect(Collectors.groupingBy(JiraIssue::getPriority));
			Map<String, List<JiraIssue>> priorityWiseTDIssues = totalDefects.stream()
					.collect(Collectors.groupingBy(JiraIssue::getPriority));

			Map<String, Long> priorityWiseWSMap = new HashMap<>();
			Map<String, Long> priorityWiseTDMap = new HashMap<>();
			priorityWiseTDIssues.entrySet().stream().forEach(priority -> {
				priorityWiseWSMap.put(priority.getKey(),
						Long.valueOf(priorityWiseWSIssues.getOrDefault(priority.getKey(), new ArrayList<>()).size()));
				priorityWiseTDMap.put(priority.getKey(), Long.valueOf(priority.getValue().size()));
			});

			if (CollectionUtils.isNotEmpty(defectWithoutStory) && requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
				KPIExcelUtility.populateDefectWithoutIssueLinkExcelData(defectWithoutStory, excelData, latestSprint.getProjectFilter().getName());
			}

			kpiElement.setExcelData(excelData);
			kpiElement.setExcelColumns(KPIExcelColumn.DEFECTS_WITHOUT_STORY_LINK.getColumns());

			LOGGER.debug(
					"[DEFECTS-WITHOUT-STORYLINK-PROJECT-WISE][{}]. Total defect without story link for project {}  is {}",
					requestTrackerId, latestSprint.getProjectFilter().getName(), defectWithoutStory.size());

			Map<String, List<DataCount>> dataCountMap = new HashMap<>();
			populateDataCountMap(priorityWiseWSMap, priorityWiseTDMap, latestSprint, dataCountMap);
			mapTmp.get(latestSprint.getId()).setValue(dataCountMap);

		}
	}

	private List<JiraIssue> checkPriority(List<JiraIssue> jiraIssues) {
		for (JiraIssue issue : jiraIssues) {
			if (StringUtils.isBlank(issue.getPriority())) {
				issue.setPriority(Constant.MISC);
			}
		}
		return jiraIssues;
	}

	private void populateDataCountMap(Map<String, Long> priorityWiseWSMap, Map<String, Long> priorityWiseTDMap,
									  Node latestSprint, Map<String, List<DataCount>> dataCountMap) {

		priorityWiseTDMap.forEach((key, value) -> {
			DataCount dcObj = getDataCountObject(value, priorityWiseWSMap.getOrDefault(key, 0L), latestSprint, key);
			dataCountMap.computeIfAbsent(key, k -> new ArrayList<>()).add(dcObj);
		});

		Long aggTotalDefectValue = priorityWiseTDMap.values().stream().mapToLong(p -> p).sum();
		Long aggDefectWithoutStory = priorityWiseWSMap.values().stream().mapToLong(p -> p).sum();

		dataCountMap.computeIfAbsent(CommonConstant.OVERALL, k -> new ArrayList<>()).add(
				getDataCountObject(aggTotalDefectValue, aggDefectWithoutStory, latestSprint, CommonConstant.OVERALL));

	}

	private DataCount getDataCountObject(Long totalDefect, Long withoutStoryDefect, Node latestSprint, String group) {

		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(withoutStoryDefect));
		dataCount.setSProjectName(latestSprint.getProjectFilter().getName());
		dataCount.setKpiGroup(group);
		dataCount.setValue(withoutStoryDefect);
		Map<String, Integer> howerMap = new HashMap<>();
		howerMap.put(DEFECT_LIST, totalDefect.intValue());
		howerMap.put(DEFECT_WITHOUT_STORY_LINK, withoutStoryDefect.intValue());
		dataCount.setHoverValue(howerMap);
		return dataCount;
	}

}
