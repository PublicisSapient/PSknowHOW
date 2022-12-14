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

package com.publicissapient.kpidashboard.apis.common.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.JiraFeatureHistory;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.KPIFieldMappingResponse;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.MasterResponse;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.KPIFieldMapping;
import com.publicissapient.kpidashboard.common.model.application.KpiMaster;
import com.publicissapient.kpidashboard.common.model.application.ValidationData;
import com.publicissapient.kpidashboard.common.model.excel.CapacityKpiData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;
import com.publicissapient.kpidashboard.common.model.kpivideolink.KPIVideoLink;
import com.publicissapient.kpidashboard.common.repository.excel.CapacityKpiDataRepository;
import com.publicissapient.kpidashboard.common.repository.excel.KanbanCapacityRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.repository.kpivideolink.KPIVideoLinkRepository;

/**
 * Helper class for kpi requests . Utility to process for kpi requests.
 *
 * @author tauakram
 */
@Service
public class KpiHelperService { // NOPMD

	private static final String STORY_DATA = "storyData";
	private static final String PROJECT_WISE_OPEN_STORY_STATUS = "projectWiseOpenStatus";
	private static final String STORY_POINTS_DATA = "storyPoints";
	private static final String DEFECT_DATA = "defectData";
	private static final String SPRINTVELOCITYKEY = "sprintVelocityKey";
	private static final String SUBGROUPCATEGORY = "subGroupCategory";
	private static final String TICKETVELOCITYKEY = "ticketVelocityKey";
	private static final String IN = "in";
	private static final String DEV = "DeveloperKpi";
	private static final String PROJECT_WISE_ISSUE_TYPES = "projectWiseIssueTypes";
	private static final String PROJECT_WISE_CLOSED_STORY_STATUS = "projectWiseClosedStoryStatus";
	private static final String JIRA_ISSUE_HISTORY_DATA = "JiraIssueHistoryData";
	private static final String FIELD_PRIORITY = "priority";
	private static final String FIELD_RCA = "rca";
	private static final String SPRINT_WISE_SPRINTDETAILS = "sprintWiseSprintDetailMap";
	private static final String ISSUE_DATA = "issueData";
	private static final String FIELD_STATUS = "status";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private KanbanJiraIssueHistoryRepository kanbanJiraIssueHistoryRepository;

	@Autowired
	private CapacityKpiDataRepository capacityKpiDataRepository;

	@Autowired
	private KanbanCapacityRepository kanbanCapacityRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private KPIVideoLinkRepository kpiVideoLinkRepository;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private SprintRepository sprintRepository;

	@Autowired
	private FilterHelperService flterHelperService;

	@Autowired
	private KanbanJiraIssueRepository kanbanJiraIssueRepository;

	/**
	 * Prepares Kpi Elemnts on the basis of kpi master data.
	 *
	 * @param kpiList
	 *            the kpi list
	 */
	public void kpiResolution(List<KpiElement> kpiList) {
		Iterable<KpiMaster> kpiIterable = configHelperService.loadKpiMaster();
		Map<String, KpiMaster> kpiMasterMapping = new HashMap<>();
		kpiIterable.forEach(kpiMaster -> kpiMasterMapping.put(kpiMaster.getKpiId(), kpiMaster));
		kpiList.forEach(kpiElement -> {
			KpiMaster kpiMaster = kpiMasterMapping.get(kpiElement.getKpiId());
			if (null != kpiMaster) {
				kpiElement.setKpiSource(kpiMaster.getKpiSource());
				kpiElement.setKpiName(kpiMaster.getKpiName());
				kpiElement.setUnit(kpiMaster.getKpiUnit());
				kpiElement.setMaxValue(kpiMaster.getMaxValue());
				kpiElement.setKpiCategory(kpiMaster.getKpiCategory());
			}

		});
	}

	/**
	 * Fetchs kpi master list master response.
	 *
	 * @return the master response
	 */
	public MasterResponse fetchKpiMasterList() {

		List<KpiMaster> lisOfKpiMaster = (List<KpiMaster>) configHelperService.loadKpiMaster();
		List<KPIVideoLink> videos = kpiVideoLinkRepository.findAll();
		CollectionUtils.emptyIfNull(lisOfKpiMaster)
				.forEach(kpiMaster -> kpiMaster.setVideoLink(findKpiVideoLink(kpiMaster.getKpiId(), videos)));

		MasterResponse masterResponse = new MasterResponse();
		masterResponse.setKpiList(lisOfKpiMaster);

		return masterResponse;
	}

	private KPIVideoLink findKpiVideoLink(String kpiId, List<KPIVideoLink> videos) {
		if (CollectionUtils.isEmpty(videos)) {
			return null;
		}
		return videos.stream().filter(video -> video.getKpiId().equals(kpiId)).findAny().orElse(null);
	}

	/**
	 * Process story data double.
	 *
	 * @param jiraIssueCustomHistory
	 *            the feature custom history
	 * @param status1
	 *            the status 1
	 * @param status2
	 *            the status 2
	 * @return difference of two date as days
	 */
	public double processStoryData(JiraIssueCustomHistory jiraIssueCustomHistory, String status1, String status2) {
		int storyDataSize = jiraIssueCustomHistory.getStorySprintDetails().size();
		double daysDifference = -99d;
		if (storyDataSize >= 2 && null != status1 && null != status2) {
			if (status2.equalsIgnoreCase(jiraIssueCustomHistory.getStorySprintDetails().get(0).getFromStatus())
					&& status1.equalsIgnoreCase(
							jiraIssueCustomHistory.getStorySprintDetails().get(storyDataSize - 1).getFromStatus())) {
				DateTime closeDate = new DateTime(
						jiraIssueCustomHistory.getStorySprintDetails().get(0).getActivityDate(), DateTimeZone.UTC);
				DateTime startDate = new DateTime(
						jiraIssueCustomHistory.getStorySprintDetails().get(storyDataSize - 1).getActivityDate(),
						DateTimeZone.UTC);
				Duration duration = new Duration(startDate, closeDate);
				daysDifference = duration.getStandardDays();
			}
		} else {
			DateTime firstDate = new DateTime(jiraIssueCustomHistory.getCreatedDate(), DateTimeZone.UTC);
			DateTime secondDate = new DateTime(jiraIssueCustomHistory.getStorySprintDetails().get(0).getActivityDate(),
					DateTimeZone.UTC);
			Duration duration = new Duration(firstDate, secondDate);
			daysDifference = duration.getStandardDays();
		}
		return daysDifference;
	}

	/**
	 * This method returns DIR data based upon kpi request and leaf node list.
	 *
	 * @param leafNodeList
	 *            the leaf node list
	 * @param kpiRequest
	 *            the kpi request
	 * @return Map of string and object
	 */
	public Map<String, Object> fetchDIRDataFromDb(List<Node> leafNodeList, KpiRequest kpiRequest) {

		Map<String, Object> resultListMap = new HashMap<>();
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, List<String>> mapOfFiltersFH = new LinkedHashMap<>();
		List<String> sprintList = new ArrayList<>();
		List<String> basicProjectConfigIds = new ArrayList<>();
		Map<String, Map<String, Object>> uniqueProjectMapFH = new HashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		Map<String, Map<String,List<String>>> droppedDefects = new HashMap<>();
		leafNodeList.forEach(leaf -> {
			Map<String, Object> mapOfProjectFiltersFH = new LinkedHashMap<>();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(leaf.getProjectFilter().getBasicProjectConfigId());
			sprintList.add(leaf.getSprintFilter().getId());
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			basicProjectConfigIds.add(basicProjectConfigId.toString());
			mapOfProjectFiltersFH.put(JiraFeatureHistory.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
					leaf.getProjectFilter().getBasicProjectConfigId());
			mapOfProjectFiltersFH.put(JiraFeatureHistory.STORY_TYPE.getFieldValueInFeature(),
					CommonUtils.convertToPatternList(fieldMapping.getJiraDefectInjectionIssueType()));
			mapOfProjectFiltersFH.put("storySprintDetails.story.fromStatus",
					CommonUtils.convertToPatternList(fieldMapping.getJiraDod()));
			mapOfProjectFiltersFH.put("storySprintDetails.defect.fromStatus",
					fieldMapping.getJiraDefectCreatedStatus());
			uniqueProjectMapFH.put(basicProjectConfigId.toString(), mapOfProjectFiltersFH);
			mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
					CommonUtils.convertToPatternList(fieldMapping.getJiraDefectInjectionIssueType()));
			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);
			getDroppedDefectsFilters(droppedDefects, basicProjectConfigId, fieldMapping);
		});

		KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.SCRUM, DEV, flterHelperService);

		mapOfFilters.put(JiraFeature.SPRINT_ID.getFieldValueInFeature(),
				sprintList.stream().distinct().collect(Collectors.toList()));
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		List<SprintWiseStory> sprintWiseStoryList = jiraIssueRepository.findIssuesGroupBySprint(mapOfFilters,
				uniqueProjectMap, kpiRequest.getFilterToShowOnTrend(), DEV);
		List<JiraIssue> issuesBySprintAndType = jiraIssueRepository.findIssuesBySprintAndType(mapOfFilters,
				uniqueProjectMap);
		List<JiraIssue> storyListWoDrop = new ArrayList<>();
		KpiHelperService.getDefectsWithoutDrop(droppedDefects, issuesBySprintAndType, storyListWoDrop);
		removeRejectedStoriesFromSprint(sprintWiseStoryList, storyListWoDrop);
		// Filter stories fetched in above query to get stories that have DOD
		// status
		List<String> storyIdList = new ArrayList<>();
		sprintWiseStoryList.forEach(s -> storyIdList.addAll(s.getStoryList()));
		mapOfFiltersFH.put("storyID", storyIdList);
		List<JiraIssueCustomHistory> storyDataList = jiraIssueCustomHistoryRepository
				.findFeatureCustomHistoryStoryProjectWise(mapOfFiltersFH, uniqueProjectMapFH);
		List<String> dodStoryIdList = storyDataList.stream().map(JiraIssueCustomHistory::getStoryID)
				.collect(Collectors.toList());
		sprintWiseStoryList.stream().forEach(story -> {
			List<String> storyNumberList = story.getStoryList().stream().filter(dodStoryIdList::contains)
					.collect(Collectors.toList());
			story.setStoryList(storyNumberList);
		});

		Map<String, List<String>> mapOfFiltersWithStoryIds = new LinkedHashMap<>();
		mapOfFiltersWithStoryIds.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));
		mapOfFiltersWithStoryIds.put(JiraFeature.DEFECT_STORY_ID.getFieldValueInFeature(), dodStoryIdList);
		mapOfFiltersWithStoryIds.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
				Arrays.asList(NormalizedJira.DEFECT_TYPE.getValue()));

		// Fetch Defects linked with story ID's
		List<JiraIssue> defectDataList = jiraIssueRepository.findIssuesByType(mapOfFiltersWithStoryIds);

		List<JiraIssue> defectListWoDrop = new ArrayList<>();
		getDefectsWithoutDrop(droppedDefects, defectDataList, defectListWoDrop);
		resultListMap.put(STORY_DATA, sprintWiseStoryList);
		resultListMap.put(DEFECT_DATA, defectListWoDrop);
		resultListMap.put(ISSUE_DATA, jiraIssueRepository.findIssueAndDescByNumber(storyIdList));

		return resultListMap;
	}

	public Map<String, Object> fetchQADDFromDb(List<Node> leafNodeList, KpiRequest kpiRequest) {

		Map<String, Object> resultListMap = new HashMap<>();
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, List<String>> mapOfFiltersFH = new LinkedHashMap<>();
		List<String> sprintList = new ArrayList<>();
		List<String> basicProjectConfigIds = new ArrayList<>();
		Map<String, Map<String, Object>> uniqueProjectMapFH = new HashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		Map<String, Map<String,List<String>>> droppedDefects = new HashMap<>();
		leafNodeList.forEach(leaf -> {
			Map<String, Object> mapOfProjectFiltersFH = new LinkedHashMap<>();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

			sprintList.add(leaf.getSprintFilter().getId());
			basicProjectConfigIds.add(basicProjectConfigId.toString());

			mapOfProjectFiltersFH.put(JiraFeatureHistory.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
					basicProjectConfigId.toString());
			mapOfProjectFiltersFH.put(JiraFeatureHistory.STORY_TYPE.getFieldValueInFeature(),
					CommonUtils.convertToPatternList(fieldMapping.getJiraQADefectDensityIssueType()));

			List<String> dodList = fieldMapping.getJiraDod();
			if (CollectionUtils.isNotEmpty(dodList)) {
				mapOfProjectFiltersFH.put("storySprintDetails.story.fromStatus",
						CommonUtils.convertToPatternList(dodList));
			}
			uniqueProjectMapFH.put(basicProjectConfigId.toString(), mapOfProjectFiltersFH);

			mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
					CommonUtils.convertToPatternList(fieldMapping.getJiraQADefectDensityIssueType()));
			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);
			getDroppedDefectsFilters(droppedDefects, basicProjectConfigId, fieldMapping);
		});

		KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.SCRUM, DEV, flterHelperService);

		mapOfFilters.put(JiraFeature.SPRINT_ID.getFieldValueInFeature(),
				sprintList.stream().distinct().collect(Collectors.toList()));
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		List<SprintWiseStory> sprintWiseStoryList = jiraIssueRepository.findIssuesGroupBySprint(mapOfFilters,
				uniqueProjectMap, kpiRequest.getFilterToShowOnTrend(), DEV);
		List<JiraIssue> issuesBySprintAndType = jiraIssueRepository.findIssuesBySprintAndType(mapOfFilters,
				uniqueProjectMap);
		List<JiraIssue> storyListWoDrop = new ArrayList<>();
		KpiHelperService.getDefectsWithoutDrop(droppedDefects, issuesBySprintAndType, storyListWoDrop);
		removeRejectedStoriesFromSprint(sprintWiseStoryList, storyListWoDrop);
		// Filter stories fetched in above query to get stories that have DOD
		// status
		List<String> storyIdList = new ArrayList<>();
		sprintWiseStoryList.forEach(s -> storyIdList.addAll(s.getStoryList()));
		mapOfFiltersFH.put("storyID", storyIdList);
		List<JiraIssueCustomHistory> storyDataList = jiraIssueCustomHistoryRepository
				.findFeatureCustomHistoryStoryProjectWise(mapOfFiltersFH, uniqueProjectMapFH);
		List<String> dodStoryIdList = storyDataList.stream().map(JiraIssueCustomHistory::getStoryID)
				.collect(Collectors.toList());

		List<JiraIssue> storyList = jiraIssueRepository.findIssuesBySprintAndType(mapOfFilters, uniqueProjectMap);
		storyList = storyList.stream().filter(feature -> dodStoryIdList.contains(feature.getNumber()))
				.collect(Collectors.toList());

		sprintWiseStoryList.stream().forEach(story -> {
			List<String> storyNumberList = story.getStoryList().stream().filter(dodStoryIdList::contains)
					.collect(Collectors.toList());
			story.setStoryList(storyNumberList);
		});
		// remove keys when search defects based on stories
		Map<String, List<String>> mapOfFiltersWithStoryIds = new LinkedHashMap<>();
		mapOfFiltersWithStoryIds.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));
		mapOfFiltersWithStoryIds.put(JiraFeature.DEFECT_STORY_ID.getFieldValueInFeature(), dodStoryIdList);
		mapOfFiltersWithStoryIds.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
				Arrays.asList(NormalizedJira.DEFECT_TYPE.getValue()));

		// Fetch Defects linked with story ID's
		List<JiraIssue> defectDataList = jiraIssueRepository.findIssuesByType(mapOfFiltersWithStoryIds);
		List<JiraIssue> defectListWoDrop = new ArrayList<>();
		getDefectsWithoutDrop(droppedDefects, defectDataList, defectListWoDrop);

		resultListMap.put(STORY_POINTS_DATA, storyList);
		resultListMap.put(STORY_DATA, sprintWiseStoryList);
		resultListMap.put(DEFECT_DATA, defectListWoDrop);

		return resultListMap;
	}

	/**
	 * Fetch sprint velocity data from db map. based upon kpi request and leaf
	 * node list
	 *
	 * @param leafNodeList
	 *            the leaf node list
	 * @param kpiRequest
	 *            the kpi request
	 * @return map
	 */
	public Map<String, Object> fetchSprintVelocityDataFromDb(List<Node> leafNodeList, KpiRequest kpiRequest) {

		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Object> resultListMap = new HashMap<>();

		List<String> sprintList = new ArrayList<>();
		List<String> basicProjectConfigIds = new ArrayList<>();

		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		Map<String, List<String>> closedStatusMap = new HashMap<>();
		Map<String, List<String>> typeNameMap = new HashMap<>();

		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

			sprintList.add(leaf.getSprintFilter().getId());
			basicProjectConfigIds.add(basicProjectConfigId.toString());

			mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
					CommonUtils.convertToPatternList(fieldMapping.getJiraSprintVelocityIssueType()));

			mapOfProjectFilters.put(JiraFeature.STATUS.getFieldValueInFeature(),
					CommonUtils.convertToPatternList(fieldMapping.getJiraIssueDeliverdStatus()));
			closedStatusMap.put(basicProjectConfigId.toString(), fieldMapping.getJiraIssueDeliverdStatus());
			typeNameMap.put(basicProjectConfigId.toString(), fieldMapping.getJiraSprintVelocityIssueType());

			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

		});

		List<SprintDetails> sprintDetails = sprintRepository.findBySprintIDIn(sprintList);

		List<String> totalIssueIds = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(sprintDetails)) {

			sprintDetails.stream().forEach(sprintDetail -> {

				if (CollectionUtils.isNotEmpty(sprintDetail.getCompletedIssues())) {

					/*List<String> closedStatus = closedStatusMap
							.getOrDefault(sprintDetail.getBasicProjectConfigId().toString(), new ArrayList<>());
					List<String> typeName = typeNameMap.getOrDefault(sprintDetail.getBasicProjectConfigId().toString(),
							new ArrayList<>());

					Set<SprintIssue> filterTotalIssues = sprintDetail.getTotalIssues().stream()
							.filter(sprintIssue -> (closedStatus.contains(sprintIssue.getStatus())
									&& typeName.contains(sprintIssue.getTypeName())))
							.collect(Collectors.toSet());
					sprintDetail.setTotalIssues(filterTotalIssues);*/
					List<String> sprintWiseIssueIds = KpiDataHelper
							.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetail, CommonConstant.COMPLETED_ISSUES);
					totalIssueIds.addAll(sprintWiseIssueIds);
				}
			});
			mapOfFilters.put(JiraFeature.ISSUE_NUMBER.getFieldValueInFeature(),
					totalIssueIds.stream().distinct().collect(Collectors.toList()));
		} else {
			mapOfFilters.put(JiraFeature.SPRINT_ID.getFieldValueInFeature(),
					sprintList.stream().distinct().collect(Collectors.toList()));
		}

		/** additional filter **/
		KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.SCRUM, DEV, flterHelperService);

		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		if (CollectionUtils.isNotEmpty(totalIssueIds)) {
			List<JiraIssue> sprintVelocityList = jiraIssueRepository.findIssuesBySprintAndType(mapOfFilters,
					new HashMap<>());
			resultListMap.put(SPRINTVELOCITYKEY, sprintVelocityList);
			resultListMap.put(SPRINT_WISE_SPRINTDETAILS, sprintDetails);
		} else {
			// start: for azure board sprint details collections put is empty due to we did
			// not have required data of issues.
			List<JiraIssue> sprintVelocityList = jiraIssueRepository.findIssuesBySprintAndType(mapOfFilters,
					uniqueProjectMap);
			resultListMap.put(SPRINTVELOCITYKEY, sprintVelocityList);
			resultListMap.put(SPRINT_WISE_SPRINTDETAILS, null);
		}
		// end: for azure board sprint details collections put is empty due to we did
		// not have required data of issues.

		return resultListMap;
	}

	/**
	 * Fetches sprint capacity data from db based upon leaf node list.
	 *
	 * @param leafNodeList
	 *            the leaf node list
	 * @return the list
	 */
	public List<JiraIssue> fetchSprintCapacityDataFromDb(List<Node> leafNodeList) {

		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();

		List<String> sprintList = new ArrayList<>();
		List<String> basicProjectConfigIds = new ArrayList<>();

		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();

		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

			List<String> capacityIssueType = fieldMapping.getJiraSprintCapacityIssueType();
			if (CollectionUtils.isEmpty(capacityIssueType)) {
				capacityIssueType = new ArrayList<>();
				capacityIssueType.add("Story");
			}
			sprintList.add(leaf.getSprintFilter().getId());
			basicProjectConfigIds.add(basicProjectConfigId.toString());

			mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
					CommonUtils.convertToPatternList(capacityIssueType));
			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

		});

		mapOfFilters.put(JiraFeature.SPRINT_ID.getFieldValueInFeature(),
				sprintList.stream().distinct().collect(Collectors.toList()));
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		return jiraIssueRepository.findIssuesBySprintAndType(mapOfFilters, uniqueProjectMap);

	}

	/**
	 * Fetch capacity data from db based upon leaf node list.
	 *
	 * @param leafNodeList
	 *            the leaf node list
	 * @return list
	 */
	public List<CapacityKpiData> fetchCapacityDataFromDB(List<Node> leafNodeList) {
		Map<String, Object> mapOfFilters = new LinkedHashMap<>();
		List<String> sprintList = new ArrayList<>();
		List<ObjectId> basicProjectConfigIds = new ArrayList<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			sprintList.add(leaf.getSprintFilter().getId());
			basicProjectConfigIds.add(basicProjectConfigId);
		});

		mapOfFilters.put(JiraFeature.SPRINT_ID.getFieldValueInFeature(),
				sprintList.stream().distinct().collect(Collectors.toList()));
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		return capacityKpiDataRepository.findByFilters(mapOfFilters, uniqueProjectMap);
	}

	/**
	 * Fetch ticket velocity data from db based upon leaf node list within range
	 * of start date and end date.
	 *
	 * @param leafNodeList
	 *            the leaf node list
	 * @param startDate
	 *            the start date
	 * @param endDate
	 *            the end date
	 * @return {@code Map<String ,Object> map}
	 */
	public Map<String, Object> fetchTicketVelocityDataFromDb(List<Node> leafNodeList, String startDate,
			String endDate) {

		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Object> resultListMap = new HashMap<>();
		List<String> projectList = new ArrayList<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();

		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(basicProjectConfigId);

			projectList.add(basicProjectConfigId.toString());
			if (Optional.ofNullable(fieldMapping.getJiraTicketVelocityIssueType()).isPresent()) {
				mapOfProjectFilters.put(JiraFeatureHistory.STORY_TYPE.getFieldValueInFeature(),
						CommonUtils.convertToPatternList(fieldMapping.getJiraTicketVelocityIssueType()));
			}
			mapOfProjectFilters.put(JiraFeatureHistory.HISTORY_STATUS.getFieldValueInFeature(),
					CommonUtils.convertToPatternList(fieldMapping.getTicketDeliverdStatus()));
			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

		});
		// Add list of subprojects in project wise filters

		String subGroupCategory = Constant.DATE;

		mapOfFilters.put(JiraFeatureHistory.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				projectList.stream().distinct().collect(Collectors.toList()));
		
		List<KanbanIssueCustomHistory> dateVelocityList = kanbanJiraIssueHistoryRepository
				.findIssuesByStatusAndDate(mapOfFilters, uniqueProjectMap, startDate, endDate, IN);
		resultListMap.put(TICKETVELOCITYKEY, dateVelocityList);
		resultListMap.put(SUBGROUPCATEGORY, subGroupCategory);
		return resultListMap;

	}

	/**
	 * Fetch team capacity data from db map.
	 *
	 * @param leafNodeList
	 *            the leaf node list
	 * @param startDate
	 *            the start date
	 * @param endDate
	 *            the end date
	 * @param kpiRequest
	 *            the kpi request
	 * @param capacityKey
	 *            the capacity key
	 * @return the map
	 */
	public Map<String, Object> fetchTeamCapacityDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest, String capacityKey) {
		Map<String, Object> resultListMap = new HashMap<>();
		Map<String, List<ObjectId>> mapOfFilters = new LinkedHashMap<>();
		List<ObjectId> projectList = new ArrayList<>();
		leafNodeList.forEach(leaf ->
			projectList.add(leaf.getProjectFilter().getBasicProjectConfigId()));

		mapOfFilters.put(JiraFeatureHistory.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				projectList.stream().distinct().collect(Collectors.toList()));

		resultListMap.put(capacityKey,
				kanbanCapacityRepository.findIssuesByType(mapOfFilters, startDate, endDate));
		resultListMap.put(SUBGROUPCATEGORY, Constant.DATE);
		return resultListMap;
	}


	/**
	 * Convert string to date local date.
	 *
	 * @param dateString
	 *            the date string
	 * @return the local date
	 */
	public LocalDate convertStringToDate(String dateString) {
		return LocalDate.parse(dateString);
	}


	/**
	 * fetching jira from jiraKanbanhistory for last 15 months and also
	 * returning fieldmapping for closed and open tickets from jira mapping
	 *
	 * @param leafNodeList
	 * @param startDate
	 * @param endDate
	 * @param kpiRequest
	 * @return
	 */
	public Map<String, Object> fetchJiraCustomHistoryDataFromDbForKanban(List<Node> leafNodeList, String startDate,
			String endDate, KpiRequest kpiRequest, String fieldName) {
		Map<String, Object> resultListMap = new HashMap<>();
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		Map<String, List<String>> projectWiseClosedStatusMap = new HashMap<>();
		Map<String, String> projectWiseOpenStatusMap = new HashMap<>();
		Map<String, List<String>> projectWiseIssueTypeMap = new HashMap<>();
		List<String> projectList = new ArrayList<>();
		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			projectList.add(basicProjectConfigId.toString());

			setJiraIssueType(fieldName, projectWiseIssueTypeMap, leaf, mapOfProjectFilters, fieldMapping);

			setJiraClosedStatusMap(projectWiseClosedStatusMap, leaf, fieldMapping);

			if (Optional.ofNullable(fieldMapping.getStoryFirstStatus()).isPresent()) {
				projectWiseOpenStatusMap.put(basicProjectConfigId.toString(), fieldMapping.getStoryFirstStatus());
			}

			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);
		});
		String subGroupCategory = KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.KANBAN,
				DEV, flterHelperService);
		mapOfFilters.put(JiraFeatureHistory.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				projectList.stream().distinct().collect(Collectors.toList()));

		List<KanbanIssueCustomHistory> issuesByCreatedDateAndType = kanbanJiraIssueHistoryRepository
				.findIssuesByCreatedDateAndType(mapOfFilters, uniqueProjectMap, startDate, endDate);
		resultListMap.put(SUBGROUPCATEGORY, subGroupCategory);
		resultListMap.put(PROJECT_WISE_ISSUE_TYPES, projectWiseIssueTypeMap);
		resultListMap.put(PROJECT_WISE_CLOSED_STORY_STATUS, projectWiseClosedStatusMap);
		resultListMap.put(PROJECT_WISE_OPEN_STORY_STATUS, projectWiseOpenStatusMap);
		resultListMap.put(JIRA_ISSUE_HISTORY_DATA, issuesByCreatedDateAndType);
		return resultListMap;
	}

	private void setJiraIssueType(String fieldName, Map<String, List<String>> projectWiseIssueTypeMap, Node leaf,
			Map<String, Object> mapOfProjectFilters, FieldMapping fieldMapping) {
		if (FIELD_RCA.equals(fieldName)) {
			if (Optional.ofNullable(fieldMapping.getKanbanRCACountIssueType()).isPresent()) {
				mapOfProjectFilters.put(JiraFeatureHistory.STORY_TYPE.getFieldValueInFeature(),
						CommonUtils.convertToPatternList(fieldMapping.getKanbanRCACountIssueType()));
				projectWiseIssueTypeMap.put(leaf.getProjectFilter().getBasicProjectConfigId().toString(),
						fieldMapping.getKanbanRCACountIssueType().stream().distinct().collect(Collectors.toList()));
			}
		} else {
			if (Optional.ofNullable(fieldMapping.getTicketCountIssueType()).isPresent()) {
				mapOfProjectFilters.put(JiraFeatureHistory.STORY_TYPE.getFieldValueInFeature(),
						CommonUtils.convertToPatternList(fieldMapping.getTicketCountIssueType()));
				projectWiseIssueTypeMap.put(leaf.getProjectFilter().getBasicProjectConfigId().toString(),
						fieldMapping.getTicketCountIssueType().stream().distinct().collect(Collectors.toList()));
			}
		}
	}

	private void setJiraClosedStatusMap(Map<String, List<String>> projectWiseClosedStatusMap, Node leaf,
			FieldMapping fieldMapping) {
		if (Optional.ofNullable(fieldMapping.getJiraTicketClosedStatus()).isPresent()) {
			List<String> closedStatusList = new ArrayList<>();
			closedStatusList.addAll(fieldMapping.getJiraTicketClosedStatus());
			if (Optional.ofNullable(fieldMapping.getJiraLiveStatus()).isPresent()) {
				closedStatusList.add(fieldMapping.getJiraLiveStatus());
			}
			if (Optional.ofNullable(fieldMapping.getJiraTicketRejectedStatus()).isPresent()) {
				closedStatusList.addAll(fieldMapping.getJiraTicketRejectedStatus());
			}
			projectWiseClosedStatusMap.put(leaf.getProjectFilter().getBasicProjectConfigId().toString(),
					closedStatusList.stream().distinct().collect(Collectors.toList()));
		}
	}

	/**
	 * returning all non-closed tickets from history data project wise the list
	 * will contain the reopen tickets within the range or outside the range if
	 * the current status of that ticket is not close the list will not contain
	 * those tickets which were closed before the filtered range
	 *
	 *
	 * @param resultListMap
	 * @param startDate
	 * @return
	 */
	public Map<String, List<KanbanIssueCustomHistory>> removeClosedTicketsFromHistoryIssuesData(
			Map<String, Object> resultListMap, String startDate) {

		List<KanbanIssueCustomHistory> nonClosedTicketsList = new ArrayList<>();
		List<KanbanIssueCustomHistory> jiraIssueHistoryDataList = (List<KanbanIssueCustomHistory>) resultListMap
				.get(JIRA_ISSUE_HISTORY_DATA);
		Map<String, List<String>> projectWiseClosedStoryStatus = (Map<String, List<String>>) resultListMap
				.get(PROJECT_WISE_CLOSED_STORY_STATUS);
		Map<String, String> projectWiseOpenStoryStatus = (Map<String, String>) resultListMap
				.get(PROJECT_WISE_OPEN_STORY_STATUS);

		jiraIssueHistoryDataList.stream().forEach(issueCustomHistory -> {

			boolean isTicketAdded = false;
			List<String> jiraClosedStatusList = projectWiseClosedStoryStatus
					.get(issueCustomHistory.getBasicProjectConfigId());
			List<String> nonClosedStatusList = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(issueCustomHistory.getHistoryDetails())) {
				prepareClosedListHistoryDetailsWise(startDate, nonClosedTicketsList, issueCustomHistory, isTicketAdded,
						jiraClosedStatusList, nonClosedStatusList);
			} else {
				// checking if history details is empty then we should atleast
				// have one history
				// detail status with an Open status
				KanbanIssueHistory history = new KanbanIssueHistory();
				history.setStatus(projectWiseOpenStoryStatus.getOrDefault(issueCustomHistory.getBasicProjectConfigId(),
						CommonConstant.OPEN));
				history.setActivityDate(issueCustomHistory.getCreatedDate());
				List<KanbanIssueHistory> historyList = new ArrayList<>();
				historyList.add(history);
				issueCustomHistory.setHistoryDetails(historyList);
				nonClosedTicketsList.add(issueCustomHistory);
			}

		});
		return KpiDataHelper.createProjectWiseMapKanbanHistory(nonClosedTicketsList,
				(String) resultListMap.get(SUBGROUPCATEGORY), flterHelperService);
	}

	/**
	 *
	 * @param startDate
	 * @param nonClosedTicketsList
	 * @param issueCustomHistory
	 * @param isTicketAdded
	 * @param jiraClosedStatusList
	 * @param nonClosedStatusList
	 */
	private void prepareClosedListHistoryDetailsWise(String startDate,
			List<KanbanIssueCustomHistory> nonClosedTicketsList, KanbanIssueCustomHistory issueCustomHistory,
			boolean isTicketAdded, List<String> jiraClosedStatusList, List<String> nonClosedStatusList) {
		List<KanbanIssueHistory> statusHistoryDetailsList = issueCustomHistory.getHistoryDetails();
		for (int i = statusHistoryDetailsList.size() - 1; i >= 0; i--) {
			KanbanIssueHistory issueStatusHistory = statusHistoryDetailsList.get(i);
			/*
			 * to check the recent status from history details in case of reopen
			 * nonClosedStatusList will have more status before closed status
			 * under that scenario the ticket will be counted
			 */

			if (!jiraClosedStatusList.contains(issueStatusHistory.getStatus())) {
				nonClosedStatusList.add(issueStatusHistory.getStatus());
			}
			if (checkConditionForClosedStatusTickets(issueStatusHistory.getStatus(), jiraClosedStatusList,
					issueStatusHistory.getActivityDate(), startDate, nonClosedStatusList)) {
				break;
			}

			if (!isTicketAdded) {
				nonClosedTicketsList.add(issueCustomHistory);
				isTicketAdded = true;
			}
		}
	}

	/**
	 * checking if from history details for a particular story contains the
	 * closed status type from fieldmapping or (the activity status is more than
	 * the selected start time of filter) and (that status be the latest status
	 * from history details) then wil return true
	 *
	 * @param historyStatus
	 * @param jiraClosedStatusList
	 * @param activityDate
	 * @param startDate
	 * @param nonClosedStatusList
	 * @return
	 */
	public boolean checkConditionForClosedStatusTickets(String historyStatus, List<String> jiraClosedStatusList,
			String activityDate, String startDate, List<String> nonClosedStatusList) {
		LocalDateTime activityLocalDate = LocalDateTime.parse(activityDate.split("\\.")[0], DATE_TIME_FORMATTER);
		LocalDateTime startLocalDate = LocalDateTime.parse(LocalDate.parse(startDate).atTime(23, 59, 59).toString());
		return jiraClosedStatusList.contains(historyStatus) && activityLocalDate.isBefore(startLocalDate)
				&& CollectionUtils.isEmpty(nonClosedStatusList);
	}

	/**
	 * the non closed stories are processed according to status
	 *
	 * @param projectWiseNonClosedTickets
	 * @param startDate
	 * @param historyDataResultMap
	 * @return
	 */
	public Map<String, Map<String, Map<String, Set<String>>>> computeProjectWiseJiraHistoryByStatusAndDate(
			Map<String, List<KanbanIssueCustomHistory>> projectWiseNonClosedTickets, String startDate,
			Map<String, Object> historyDataResultMap) {
		Map<String, Map<String, Map<String, Set<String>>>> projectWiseJiraHistoryStatusAndDateWiseIssueMap = new HashMap<>();
		Map<String, String> projectWiseOpenStatus = (Map<String, String>) historyDataResultMap
				.get(PROJECT_WISE_OPEN_STORY_STATUS);
		projectWiseNonClosedTickets.entrySet().stream().forEach(nonClosedTicketsList -> {
			String openStatusFromFieldMapping = projectWiseOpenStatus.getOrDefault(nonClosedTicketsList.getKey(),
					CommonConstant.OPEN);
			Map<String, Map<String, Set<String>>> jiraHistoryStatusAndDateWiseIssueMap = new HashMap<>();
			for (KanbanIssueCustomHistory issueCustomHistory : nonClosedTicketsList.getValue()) {
				// if all activity date are before the filter range then this
				// flag will remain
				// true
				boolean dateLessThanStartDate = true;
				// for every ticket this status will always be set to null for
				// first time
				String status = null;
				LocalDate startLocalDateTemp = LocalDate.parse(startDate);
				List<KanbanIssueHistory> statusHistoryDetailsList = issueCustomHistory.getHistoryDetails();
				if (CollectionUtils.isNotEmpty(statusHistoryDetailsList)) {
					for (KanbanIssueHistory statusList : statusHistoryDetailsList) {
						String currentStatus = statusList.getStatus().equals("") ? openStatusFromFieldMapping
								: statusList.getStatus();
						LocalDate activityLocalDate = LocalDate.parse(statusList.getActivityDate().split("\\.")[0],
								DATE_TIME_FORMATTER);
						/*
						 * check if ticket's latest activity was before the
						 * filter's start time then will consider that ticket
						 * and latest status in selected filter range cumulative
						 * way otherwise will move into the loop
						 */
						if (activityLocalDate.isEqual(startLocalDateTemp)
								|| activityLocalDate.isAfter(startLocalDateTemp)) {
							if (status == null) {
								/*
								 * when no change in status happened after the
								 * creation date of ticket
								 */
								status = currentStatus;
								startLocalDateTemp = activityLocalDate;
								populateJiraHistoryFieldAndDateWiseIssues(startLocalDateTemp.toString(), status,
										issueCustomHistory.getStoryID(), jiraHistoryStatusAndDateWiseIssueMap);
							} else {
								/*
								 * if within a ticket some history details are
								 * before the filter range and others are within
								 * the filter range and status change happened
								 * on the same day
								 */
								if (startLocalDateTemp.isEqual(activityLocalDate)) {
									status = currentStatus;
									populateJiraHistoryFieldAndDateWiseIssues(startLocalDateTemp.toString(), status,
											issueCustomHistory.getStoryID(), jiraHistoryStatusAndDateWiseIssueMap);
								} else {
									/*
									 * if within a ticket some history details
									 * are before the filter range and others
									 * are within the filter range and status
									 * change happened on the different day or
									 * the status remain consistent for some
									 * days
									 */
									for (LocalDate loopStartDate = startLocalDateTemp; loopStartDate
											.isBefore(activityLocalDate)
											|| loopStartDate.isEqual(activityLocalDate); loopStartDate = loopStartDate
													.plusDays(1)) {
										if (loopStartDate.isEqual(activityLocalDate)) {
											status = currentStatus;
										}
										populateJiraHistoryFieldAndDateWiseIssues(loopStartDate.toString(), status,
												issueCustomHistory.getStoryID(), jiraHistoryStatusAndDateWiseIssueMap);
									}
									startLocalDateTemp = activityLocalDate;
								}
								dateLessThanStartDate = false;
							}
						}
						// if activity date is less than the filter range then
						// just update the status
						status = statusList.getStatus().equals("") ? openStatusFromFieldMapping
								: statusList.getStatus();
					}

					LocalDate endDate = LocalDate.now();
					/**
					 * when all activity dates are less than the filter range or
					 * when the last status's activity date between the fiilter
					 * range loop will run from last activity date till today's
					 * date for cumulative sum
					 */
					if (dateLessThanStartDate || startLocalDateTemp.isBefore(endDate)) {
						for (LocalDate loopStartDate = startLocalDateTemp; loopStartDate.isBefore(endDate)
								|| loopStartDate.isEqual(endDate); loopStartDate = loopStartDate.plusDays(1)) {
							populateJiraHistoryFieldAndDateWiseIssues(loopStartDate.toString(), status,
									issueCustomHistory.getStoryID(), jiraHistoryStatusAndDateWiseIssueMap);
						}
					}
				}
			}
			projectWiseJiraHistoryStatusAndDateWiseIssueMap.put(nonClosedTicketsList.getKey(),
					jiraHistoryStatusAndDateWiseIssueMap);
		});
		return projectWiseJiraHistoryStatusAndDateWiseIssueMap;
	}

	/**
	 * will create a map of Map<field,Map<Date,List of issues>> field could be
	 * rca, priority or status for each status at passed time what all tickets
	 * to be considered
	 *
	 * @param startDate
	 * @param fieldValue
	 * @param storyId
	 * @param jiraHistoryFieldAndDateWiseIssueMap
	 */
	public void populateJiraHistoryFieldAndDateWiseIssues(String startDate, String fieldValue, String storyId,
			Map<String, Map<String, Set<String>>> jiraHistoryFieldAndDateWiseIssueMap) {
		// if field value is already present in the map then we have to add the
		// story in
		// the already present list of stories
		jiraHistoryFieldAndDateWiseIssueMap.computeIfPresent(fieldValue, (key, value) -> {
			populateJiraDateWiseIssues(startDate, storyId, value);
			return value;
		});

		jiraHistoryFieldAndDateWiseIssueMap.computeIfAbsent(fieldValue, value -> {
			Map<String, Set<String>> dateWiseIssues = new HashMap<>();
			populateJiraDateWiseIssues(startDate, storyId, dateWiseIssues);
			return dateWiseIssues;
		});
	}

	/**
	 * depending upon the startdate passed, we need to create story list for
	 * that particular field field can be status/rca/priority
	 *
	 * @param startDate
	 * @param storyId
	 * @param jiraHistoryDateWiseIssuesMap
	 */
	public void populateJiraDateWiseIssues(String startDate, String storyId,
			Map<String, Set<String>> jiraHistoryDateWiseIssuesMap) {
		jiraHistoryDateWiseIssuesMap.computeIfPresent(startDate, (key, value) -> {
			value.add(storyId);
			return value;
		});

		jiraHistoryDateWiseIssuesMap.computeIfAbsent(startDate, value -> {
			Set<String> issueIIds = new HashSet<>();
			issueIIds.add(storyId);
			return issueIIds;
		});
	}

	/**
	 * the non closed stories are processed according to the field can be
	 * rca/priority
	 *
	 * @param projectWiseNonClosedTickets
	 * @param startDate
	 * @param resultListMap
	 * @param fieldName
	 * @return
	 */
	public Map<String, Map<String, Map<String, Set<String>>>> computeProjectWiseJiraHistoryByFieldAndDate(
			Map<String, List<KanbanIssueCustomHistory>> projectWiseNonClosedTickets, String startDate,
			Map<String, Object> resultListMap, String fieldName) {
		Map<String, Map<String, Map<String, Set<String>>>> projectWiseJiraHistoryFieldAndDateWiseIssueMap = new HashMap<>();
		Map<String, List<String>> projectWiseClosedStoryStatus = (Map<String, List<String>>) resultListMap
				.get(PROJECT_WISE_CLOSED_STORY_STATUS);
		Map<String, String> projectWiseOpenStoryStatus = (Map<String, String>) resultListMap
				.get(PROJECT_WISE_OPEN_STORY_STATUS);

		projectWiseNonClosedTickets.entrySet().stream().forEach(nonClosedTicketsList -> {
			String openStatusFromFieldMapping = projectWiseOpenStoryStatus.getOrDefault(nonClosedTicketsList.getKey(),
					CommonConstant.OPEN);
			Map<String, Map<String, Set<String>>> jiraHistoryStatusAndDateWiseIssueMap = new HashMap<>();
			for (KanbanIssueCustomHistory issueCustomHistory : nonClosedTicketsList.getValue()) {
				// if all activity date are before the filter range then this
				// flag will remain
				// true
				boolean dateLessThanStartDate = true;
				// for every ticket this status will always be set to null for
				// first time
				String status = null;
				List<String> jiraClosedStatusList = projectWiseClosedStoryStatus
						.get(issueCustomHistory.getBasicProjectConfigId());
				LocalDate startLocalDateTemp = LocalDate.parse(startDate);
				String fieldValues = basedOnKPIFieldNameFetchValues(fieldName, issueCustomHistory);
				List<KanbanIssueHistory> statusHistoryDetailsList = issueCustomHistory.getHistoryDetails();
				if (CollectionUtils.isNotEmpty(statusHistoryDetailsList) && StringUtils.isNotEmpty(fieldValues)) {
					for (KanbanIssueHistory statusList : statusHistoryDetailsList) {
						String currentStatus = statusList.getStatus().equals("") ? openStatusFromFieldMapping
								: statusList.getStatus();
						LocalDate activityLocalDate = LocalDate.parse(statusList.getActivityDate().split("\\.")[0],
								DATE_TIME_FORMATTER);
						/*
						 * check if ticket's latest activity was before the
						 * filter's start time then will consider that ticket
						 * and latest status in selected filter range cumulative
						 * way otherwise will move into the loop
						 */
						if ((activityLocalDate.isEqual(startLocalDateTemp)
								|| activityLocalDate.isAfter(startLocalDateTemp))) {
							if (status == null) {
								/*
								 * when no change in status happened after the
								 * creation date of ticket
								 */
								status = currentStatus;
								startLocalDateTemp = activityLocalDate;
								checkStatusAndPopulateJiraHistoryFieldAndDateWiseIssues(
										jiraHistoryStatusAndDateWiseIssueMap, issueCustomHistory, status,
										jiraClosedStatusList, fieldValues, startLocalDateTemp);
							} else {
								/*
								 * if within a ticket some history details are
								 * before the filter range and others are within
								 * the filter range and status change happened
								 * on the same day
								 */
								if (startLocalDateTemp.isEqual(activityLocalDate)) {
									status = currentStatus;
									checkStatusAndPopulateJiraHistoryFieldAndDateWiseIssues(
											jiraHistoryStatusAndDateWiseIssueMap, issueCustomHistory, status,
											jiraClosedStatusList, fieldValues, startLocalDateTemp);
								} else {
									/*
									 * if within a ticket some history details
									 * are before the filter range and others
									 * are within the filter range and status
									 * change happened on the different day or
									 * the status remain consistent for some
									 * days
									 */
									for (LocalDate loopStartDate = startLocalDateTemp; loopStartDate
											.isBefore(activityLocalDate)
											|| loopStartDate.isEqual(activityLocalDate); loopStartDate = loopStartDate
													.plusDays(1)) {
										if (loopStartDate.isEqual(activityLocalDate)) {
											status = currentStatus;
										}
										checkStatusAndPopulateJiraHistoryFieldAndDateWiseIssues(
												jiraHistoryStatusAndDateWiseIssueMap, issueCustomHistory, status,
												jiraClosedStatusList, fieldValues, loopStartDate);
									}
									startLocalDateTemp = activityLocalDate;
								}
								dateLessThanStartDate = false;
							}
						}
						// if activity date is less than the filter range then
						// just update the status
						status = statusList.getStatus().equals("") ? openStatusFromFieldMapping
								: statusList.getStatus();
					}

					LocalDate endDate = LocalDate.now();
					/**
					 * when all activity dates are less than the filter range or
					 * when the last status's activity date between the fiilter
					 * range loop will run from last activity date till today's
					 * date for cumulative sum
					 */
					if (!jiraClosedStatusList.contains(status)
							&& (dateLessThanStartDate || startLocalDateTemp.isBefore(endDate))) {
						for (LocalDate loopStartDate = startLocalDateTemp; loopStartDate.isBefore(endDate)
								|| loopStartDate.isEqual(endDate); loopStartDate = loopStartDate.plusDays(1)) {
							populateJiraHistoryFieldAndDateWiseIssues(loopStartDate.toString(), fieldValues,
									issueCustomHistory.getStoryID(), jiraHistoryStatusAndDateWiseIssueMap);
						}
					}
				}
			}
			projectWiseJiraHistoryFieldAndDateWiseIssueMap.put(nonClosedTicketsList.getKey(),
					jiraHistoryStatusAndDateWiseIssueMap);
		});
		return projectWiseJiraHistoryFieldAndDateWiseIssueMap;
	}

	/**
	 * if status is contains in closed jira list then we did not populate
	 * 
	 * @param jiraHistoryStatusAndDateWiseIssueMap
	 * @param issueCustomHistory
	 * @param status
	 * @param jiraClosedStatusList
	 * @param fieldValues
	 * @param loopStartDate
	 */
	private void checkStatusAndPopulateJiraHistoryFieldAndDateWiseIssues(
			Map<String, Map<String, Set<String>>> jiraHistoryStatusAndDateWiseIssueMap,
			KanbanIssueCustomHistory issueCustomHistory, String status, List<String> jiraClosedStatusList,
			String fieldValues, LocalDate loopStartDate) {
		if (!jiraClosedStatusList.contains(status)) {
			populateJiraHistoryFieldAndDateWiseIssues(loopStartDate.toString(), fieldValues,
					issueCustomHistory.getStoryID(), jiraHistoryStatusAndDateWiseIssueMap);
		}
	}

	/**
	 * based on field name fetch values from db
	 * 
	 * @param fieldName
	 * @param issueCustomHistory
	 * @return
	 */
	private String basedOnKPIFieldNameFetchValues(String fieldName, KanbanIssueCustomHistory issueCustomHistory) {
		if (fieldName.equals(FIELD_PRIORITY)) {
			return KPIHelperUtil.mappingPriority(issueCustomHistory.getPriority(), customApiConfig);
		} else if (fieldName.equals(FIELD_RCA) && CollectionUtils.isNotEmpty(issueCustomHistory.getRootCauseList())) {
			return StringUtils.capitalize(issueCustomHistory.getRootCauseList().get(0));
		}
		return null;
	}

	/**
	 * prepare data for excel for cumulative kpi of Kanban on the basis of
	 * field. field can be RCA/priority/status field values as per field of jira
	 *
	 * @param jiraHistoryFieldAndDateWiseIssueMap
	 * @param fieldName
	 * @param fieldValues
	 * @return
	 */
	public ValidationData prepareExcelForKanbanCumulativeDataMap(
			Map<String, Map<String, Set<String>>> jiraHistoryFieldAndDateWiseIssueMap, String fieldName,
			Set<String> fieldValues) {

		Map<String, Set<String>> fieldWiseIssuesLatestMap = filterKanbanDataBasedOnFieldLatestCumulativeData(
				jiraHistoryFieldAndDateWiseIssueMap, fieldValues);

		ValidationData validationData = new ValidationData();
		List<String> fieldList = new LinkedList<>();
		List<String> ticketsList = new LinkedList<>();
		Map<String, Set<String>> fieldWiseIssues = fieldWiseIssuesLatestMap.entrySet().stream()
				.sorted((i1, i2) -> i1.getKey().compareTo(i2.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		fieldWiseIssues.entrySet().forEach(dateSet -> {
			String field = dateSet.getKey();
			dateSet.getValue().stream().forEach(values -> {
				fieldList.add(field);
				ticketsList.add(values);
			});
		});
		validationData.setTicketKeyList(ticketsList);
		if (fieldName.equalsIgnoreCase(FIELD_STATUS)) {
			validationData.setStatus(fieldList);
		} else if (fieldName.equalsIgnoreCase(FIELD_PRIORITY)) {
			validationData.setDefectPriorityList(fieldList);
		} else if (fieldName.equalsIgnoreCase(FIELD_RCA)) {
			validationData.setDefectRootCauseList(fieldList);
		}
		return validationData;
	}

	/**
	 * prepare excel data only Today Cumulative data so that only latest data
	 * values of field(status/rca/priority)
	 *
	 * @param jiraHistoryFieldAndDateWiseIssueMap
	 * @param fieldValues
	 * @return
	 */
	public Map<String, Set<String>> filterKanbanDataBasedOnFieldLatestCumulativeData(
			Map<String, Map<String, Set<String>>> jiraHistoryFieldAndDateWiseIssueMap, Set<String> fieldValues) {
		String date = LocalDate.now().toString();
		Map<String, Set<String>> fieldWiseIssuesLatestMap = new HashMap<>();
		fieldValues.forEach(field -> {
			Set<String> ids = jiraHistoryFieldAndDateWiseIssueMap.get(field).getOrDefault(date, new HashSet<>())
					.stream().filter(Objects::nonNull).collect(Collectors.toSet());
			fieldWiseIssuesLatestMap.put(field, ids);
		});
		return fieldWiseIssuesLatestMap;
	}

	public static void getDroppedDefectsFilters(Map<String, Map<String, List<String>>> droppedDefects,
												ObjectId basicProjectConfigId, FieldMapping fieldMapping) {
		Map<String, List<String>> filtersMap = new HashMap<>();
		if (CollectionUtils.isNotEmpty(fieldMapping.getResolutionTypeForRejection())) {
			filtersMap.put(Constant.RESOLUTION_TYPE_FOR_REJECTION, fieldMapping.getResolutionTypeForRejection());
		}
		if (StringUtils.isNotEmpty(fieldMapping.getJiraDefectRejectionStatus())) {
			filtersMap.put(Constant.DEFECT_REJECTION_STATUS, Arrays.asList(fieldMapping.getJiraDefectRejectionStatus()));
		}
		droppedDefects.put(basicProjectConfigId.toString(), filtersMap);
	}

	public static void getDefectsWithoutDrop(Map<String, Map<String,List<String>>> droppedDefects, List<JiraIssue> defectDataList,
											 List<JiraIssue> defectListWoDrop) {
		if (CollectionUtils.isNotEmpty(defectDataList)) {
			Set<JiraIssue> defectListWoDropSet = new HashSet<>();
			defectDataList.forEach(jiraIssue -> {
				getDefectsWoDrop(droppedDefects, defectListWoDropSet, jiraIssue);
			});
			defectListWoDrop.addAll(defectListWoDropSet);
		}
	}

	private static void getDefectsWoDrop(Map<String, Map<String, List<String>>> droppedDefects, Set<JiraIssue> defectListWoDropSet, JiraIssue jiraIssue) {
		Map<String, List<String>> defectStatus = droppedDefects.get(jiraIssue.getBasicProjectConfigId());
		if (MapUtils.isNotEmpty(defectStatus)) {
			List<String> rejectedDefect = defectStatus.getOrDefault(Constant.DEFECT_REJECTION_STATUS, new ArrayList<>());
			List<String> resolutionTypeForRejection = defectStatus.getOrDefault(Constant.RESOLUTION_TYPE_FOR_REJECTION, new ArrayList<>());
			if (!rejectedDefect.contains(jiraIssue.getStatus()) && !resolutionTypeForRejection.contains(jiraIssue.getResolution())) {
				defectListWoDropSet.add(jiraIssue);
			}
		} else {
			defectListWoDropSet.add(jiraIssue);
		}
	}

	public static void removeRejectedStoriesFromSprint(List<SprintWiseStory> sprintWiseStories,
												 List<JiraIssue> acceptedStories) {

		Set<String> acceptedStoryIds = acceptedStories.stream().map(JiraIssue::getNumber).collect(Collectors.toSet());

		sprintWiseStories.forEach(sprintWiseStory -> sprintWiseStory.getStoryList()
				.removeIf(storyId -> !acceptedStoryIds.contains(storyId)));
	}

	/**
	 * Fetchs kpi fieldmapping list KpiFieldMapping response.
	 *
	 * @return the KpiFieldMapping response
	 */
	public KPIFieldMappingResponse fetchKpiFieldMappingList() {
		List<KPIFieldMapping> lisOfKpiFieldMapping = (List<KPIFieldMapping>) configHelperService.loadKpiFieldMapping();
		KPIFieldMappingResponse kpiFieldMappingResponse = new KPIFieldMappingResponse();
		kpiFieldMappingResponse.setKpiFieldMappingList(lisOfKpiFieldMapping);
		return kpiFieldMappingResponse;
	}

}