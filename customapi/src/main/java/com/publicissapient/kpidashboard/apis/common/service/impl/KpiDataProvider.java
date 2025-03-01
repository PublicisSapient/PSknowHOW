package com.publicissapient.kpidashboard.apis.common.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.BuildStatus;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.application.BuildRepository;
import com.publicissapient.kpidashboard.common.repository.excel.CapacityKpiDataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KpiDataProvider {

	private static final String DEV = "DeveloperKpi";
	private static final String STORY_LIST = "stories";
	private static final String SPRINTSDETAILS = "sprints";
	private static final String JIRA_ISSUE_HISTORY_DATA = "JiraIssueHistoryData";
	private static final String ESTIMATE_TIME = "Estimate_Time";
	public static final String TOTAL_ISSUE = "totalIssue";
	public static final String SPRINT_DETAILS = "sprintDetails";
	public static final String SCOPE_CHANGE_ISSUE_HISTORY = "scopeChangeIssuesHistories";
	private static final String PROJECT_WISE_TOTAL_ISSUE = "projectWiseTotalIssues";

	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private SprintRepository sprintRepository;
	@Autowired
	private FilterHelperService filterHelperService;
	@Autowired
	private KpiHelperService kpiHelperService;
	@Autowired
	private JiraIssueRepository jiraIssueRepository;
	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	@Autowired
	private CapacityKpiDataRepository capacityKpiDataRepository;
	@Autowired
	private BuildRepository buildRepository;

	/**
	 * Fetches data from DB for the given project and sprints combination.
	 *
	 * @param kpiRequest
	 * @param basicProjectConfigId
	 * @param sprintList
	 * @return
	 */
	public Map<String, Object> fetchIssueCountDataFromDB(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList) {

		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Object> resultListMap = new HashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		// for storing projectWise Total Count type Categories
		Map<String, List<String>> projectWiseJiraIdentification = new HashMap<>();
		// for storing projectWise Story Count type Categories
		Map<String, List<String>> projectWiseStoryCategories = new HashMap<>();

		Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
		List<String> basicProjectConfigIds = List.of(basicProjectConfigId.toString());

		List<String> jiraStoryIdentification = new ArrayList<>();
		List<String> jiraStoryCategory = new ArrayList<>();
		if (Optional.ofNullable(fieldMapping.getJiraStoryIdentificationKpi40()).isPresent()) {
			jiraStoryIdentification = fieldMapping.getJiraStoryIdentificationKpi40().stream().map(String::toLowerCase)
					.collect(Collectors.toList());
		}
		if (Optional.ofNullable(fieldMapping.getJiraStoryCategoryKpi40()).isPresent()) {
			jiraStoryCategory = fieldMapping.getJiraStoryCategoryKpi40().stream().map(String::toLowerCase)
					.collect(Collectors.toList());
		}
		projectWiseJiraIdentification.put(basicProjectConfigId.toString(), jiraStoryIdentification);
		projectWiseStoryCategories.put(basicProjectConfigId.toString(), jiraStoryCategory);
		List<String> categories = new ArrayList<>(jiraStoryIdentification);
		categories.addAll(jiraStoryCategory);
		categories = categories.stream().map(String::toLowerCase) // Convert to lowercase for case-insensitive
				// comparison
				.distinct().collect(Collectors.toList());

		KpiDataHelper.prepareFieldMappingDefectTypeTransformation(mapOfProjectFilters, fieldMapping.getJiradefecttype(),
				categories, JiraFeature.ISSUE_TYPE.getFieldValueInFeature());
		uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

		List<SprintDetails> sprintDetails = sprintRepository.findBySprintIDIn(sprintList);
		Set<String> totalIssue = new HashSet<>();
		sprintDetails.forEach(dbSprintDetail -> {
			if (CollectionUtils.isNotEmpty(dbSprintDetail.getTotalIssues())) {
				totalIssue.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(dbSprintDetail,
						CommonConstant.TOTAL_ISSUES));
			}

		});

		// additional filter
		KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.SCRUM, DEV, filterHelperService);

		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(), basicProjectConfigIds);

		if (CollectionUtils.isNotEmpty(totalIssue)) {
			resultListMap.put(STORY_LIST,
					jiraIssueRepository.findIssueByNumber(mapOfFilters, totalIssue, uniqueProjectMap));
			resultListMap.put(SPRINTSDETAILS, sprintDetails);
		}
		resultListMap.put("projectWiseStoryCategories", projectWiseStoryCategories);
		resultListMap.put("projectWiseTotalCategories", projectWiseJiraIdentification);
		return resultListMap;
	}

	/**
	 * Fetch data from data for given project.
	 *
	 * @param basicProjectConfigId
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public List<Build> fetchBuildFrequencydata(ObjectId basicProjectConfigId, String startDate, String endDate) {
		List<String> statusList = List.of(BuildStatus.SUCCESS.name());
		Map<String, List<String>> mapOfFilters = new HashMap<>();
		mapOfFilters.put("buildStatus", statusList);
		Set<ObjectId> projectBasicConfigIds = new HashSet<>();
		projectBasicConfigIds.add(basicProjectConfigId);
		return buildRepository.findBuildList(mapOfFilters, projectBasicConfigIds, startDate, endDate);
	}

	/**
	 * Fetches sprint capacity data from the database for the given project and
	 * sprints combination.
	 *
	 * @param kpiRequest
	 *            The KPI request object.
	 * @param basicProjectConfigId
	 *            The project config ID.
	 * @param sprintList
	 *            The list of sprint IDs.
	 * @return A map containing estimate time, story list, sprint details, and
	 *         JiraIssue history.
	 */
	public Map<String, Object> fetchSprintCapacityDataFromDb(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList) {
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		List<String> basicProjectConfigIds = new ArrayList<>();

		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMapForSubTask = new HashMap<>();
		Map<String, Object> resultListMap = new HashMap<>();

		/** additional filter **/
		KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.SCRUM, CommonConstant.QA,
				filterHelperService);

		Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
		Map<String, Object> mapOfProjectFiltersForSubTask = new LinkedHashMap<>();

		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

		List<String> capacityIssueType = fieldMapping.getJiraSprintCapacityIssueTypeKpi46();
		if (CollectionUtils.isEmpty(capacityIssueType)) {
			capacityIssueType = new ArrayList<>();
			capacityIssueType.add("Story");
		}

		List<String> taskType = fieldMapping.getJiraSubTaskIdentification();
		basicProjectConfigIds.add(basicProjectConfigId.toString());

		mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
				CommonUtils.convertToPatternList(capacityIssueType));
		mapOfProjectFilters.putAll(mapOfFilters);
		mapOfProjectFiltersForSubTask.put(JiraFeature.ORIGINAL_ISSUE_TYPE.getFieldValueInFeature(),
				CommonUtils.convertToPatternList(taskType));
		uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);
		uniqueProjectMapForSubTask.put(basicProjectConfigId.toString(), mapOfProjectFiltersForSubTask);

		Map<String, Object> capacityMapOfFilters = new HashMap<>();
		KpiDataHelper.createAdditionalFilterMapForCapacity(kpiRequest, capacityMapOfFilters, filterHelperService);

		capacityMapOfFilters.put(JiraFeature.SPRINT_ID.getFieldValueInFeature(),
				sprintList.stream().distinct().toList());
		capacityMapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().map(ObjectId::new).distinct().toList());
		resultListMap.put(ESTIMATE_TIME,
				capacityKpiDataRepository.findByFilters(capacityMapOfFilters, new HashMap<>()));
		List<SprintDetails> sprintDetails = sprintRepository.findBySprintIDIn(sprintList);
		Set<String> totalIssue = new HashSet<>();
		sprintDetails.forEach(dbSprintDetail -> {
			if (CollectionUtils.isNotEmpty(dbSprintDetail.getTotalIssues())) {
				totalIssue.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(dbSprintDetail,
						CommonConstant.TOTAL_ISSUES));
			}
		});

		if (CollectionUtils.isNotEmpty(totalIssue)) {
			List<JiraIssue> jiraIssueList = jiraIssueRepository.findIssueByNumberOrParentStoryIdAndType(totalIssue,
					uniqueProjectMap, CommonConstant.NUMBER);
			List<JiraIssue> subTaskList = jiraIssueRepository.findIssueByNumberOrParentStoryIdAndType(
					jiraIssueList.stream().map(JiraIssue::getNumber).collect(Collectors.toSet()),
					uniqueProjectMapForSubTask, CommonConstant.PARENT_STORY_ID);
			List<JiraIssue> jiraIssues = new ArrayList<>();
			jiraIssues.addAll(subTaskList);
			jiraIssues.addAll(jiraIssueList);
			List<JiraIssueCustomHistory> jiraIssueCustomHistoryList = jiraIssueCustomHistoryRepository
					.findByStoryIDInAndBasicProjectConfigIdIn(jiraIssues.stream().map(JiraIssue::getNumber).toList(),
							basicProjectConfigIds.stream().distinct().toList());

			resultListMap.put(STORY_LIST, jiraIssues);
			resultListMap.put(SPRINTSDETAILS, sprintDetails);
			resultListMap.put(JIRA_ISSUE_HISTORY_DATA, jiraIssueCustomHistoryList);
		}

		return resultListMap;
	}

	public Map<String, Object> fetchScopeChurnData(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList) {
		log.info("Fetching Scope Churn KPI Data for Project {}", basicProjectConfigId.toString());

		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Object> resultListMap = new HashMap<>();
		List<String> basicProjectConfigIds = List.of(basicProjectConfigId.toString());
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		Set<String> totalIssue = new HashSet<>();
		Set<String> scopeChangeIssue = new HashSet<>();

		Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
		if (CollectionUtils.isNotEmpty(fieldMapping.getJiraStoryIdentificationKPI164())) {
			KpiDataHelper.prepareFieldMappingDefectTypeTransformation(mapOfProjectFilters,
					fieldMapping.getJiradefecttype(), fieldMapping.getJiraStoryIdentificationKPI164(),
					JiraFeature.ISSUE_TYPE.getFieldValueInFeature());
			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);
		} else {
			// In Case of no issue type fetching all the issueType for that proj
			uniqueProjectMap.put(basicProjectConfigId.toString(), new HashMap<>());
		}
		uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

		List<SprintDetails> sprintDetails = new ArrayList<>(sprintRepository.findBySprintIDIn(sprintList));
		sprintDetails.forEach(dbSprintDetail -> {
			if (CollectionUtils.isNotEmpty(dbSprintDetail.getCompletedIssues())) {
				totalIssue.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(dbSprintDetail,
						CommonConstant.COMPLETED_ISSUES));
			}
			if (CollectionUtils.isNotEmpty(dbSprintDetail.getNotCompletedIssues())) {
				totalIssue.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(dbSprintDetail,
						CommonConstant.NOT_COMPLETED_ISSUES));
			}
			if (CollectionUtils.isNotEmpty(dbSprintDetail.getPuntedIssues())) {
				List<String> removedIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(dbSprintDetail,
						CommonConstant.PUNTED_ISSUES);
				totalIssue.addAll(removedIssues);
				scopeChangeIssue.addAll(removedIssues);
			}
			if (CollectionUtils.isNotEmpty(dbSprintDetail.getAddedIssues())) {
				List<String> addedIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(dbSprintDetail,
						CommonConstant.ADDED_ISSUES);
				totalIssue.addAll(addedIssues);
				scopeChangeIssue.addAll(addedIssues);
			}
		});

		/** additional filter **/
		KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.SCRUM, DEV, filterHelperService);

		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		if (CollectionUtils.isNotEmpty(totalIssue)) {
			List<JiraIssue> totalJiraIssue = jiraIssueRepository.findIssueByNumber(mapOfFilters, totalIssue,
					uniqueProjectMap);
			resultListMap.put(SPRINT_DETAILS, sprintDetails);
			resultListMap.put(TOTAL_ISSUE, totalJiraIssue);
			// Fetching history only for change/removed issue date for Excel req
			List<JiraIssueCustomHistory> scopeChangeIssueHistories = jiraIssueCustomHistoryRepository
					.findByStoryIDInAndBasicProjectConfigIdIn(new ArrayList<>(scopeChangeIssue),
							basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));
			resultListMap.put(SCOPE_CHANGE_ISSUE_HISTORY, scopeChangeIssueHistories);

		}
		return resultListMap;
	}

	public Map<String, Object> fetchCommitmentReliabilityData(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList) {
		log.info("Fetching Commitment Reliability KPI Data for Project {}", basicProjectConfigId.toString());

		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Object> resultListMap = new HashMap<>();
		List<String> basicProjectConfigIds = List.of(basicProjectConfigId.toString());
		List<SprintDetails> sprintDetails = new ArrayList<>(sprintRepository.findBySprintIDIn(sprintList));

		Map<ObjectId, List<SprintDetails>> projectWiseTotalSprintDetails = sprintDetails.stream()
				.collect(Collectors.groupingBy(SprintDetails::getBasicProjectConfigId));

		Map<ObjectId, Set<String>> duplicateIssues = kpiHelperService
				.getProjectWiseTotalSprintDetail(projectWiseTotalSprintDetails);
		Map<ObjectId, Map<String, List<LocalDateTime>>> projectWiseDuplicateIssuesWithMinCloseDate = null;
		Map<ObjectId, FieldMapping> fieldMappingMap = configHelperService.getFieldMappingMap();

		if (MapUtils.isNotEmpty(fieldMappingMap) && !duplicateIssues.isEmpty()) {
			Map<ObjectId, List<String>> customFieldMapping = duplicateIssues.keySet().stream()
					.filter(fieldMappingMap::containsKey).collect(Collectors.toMap(Function.identity(), key -> {
						FieldMapping fieldMapping = fieldMappingMap.get(key);
						return Optional.ofNullable(fieldMapping)
								.map(FieldMapping::getJiraIterationCompletionStatusKpi72)
								.orElse(Collections.emptyList());
					}));
			projectWiseDuplicateIssuesWithMinCloseDate = kpiHelperService
					.getMinimumClosedDateFromConfiguration(duplicateIssues, customFieldMapping);
		}

		Map<ObjectId, Map<String, List<LocalDateTime>>> finalProjectWiseDuplicateIssuesWithMinCloseDate = projectWiseDuplicateIssuesWithMinCloseDate;
		Set<String> totalIssue = new HashSet<>();
		sprintDetails.stream().forEach(dbSprintDetail -> {
			FieldMapping fieldMapping = fieldMappingMap.get(dbSprintDetail.getBasicProjectConfigId());
			// to modify sprintdetails on the basis of configuration for the project
			SprintDetails sprintDetail = KpiDataHelper.processSprintBasedOnFieldMappings(dbSprintDetail,
					fieldMapping.getJiraIterationIssuetypeKpi72(), fieldMapping.getJiraIterationCompletionStatusKpi72(),
					finalProjectWiseDuplicateIssuesWithMinCloseDate);
			if (CollectionUtils.isNotEmpty(sprintDetail.getTotalIssues())) {
				totalIssue.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetail,
						CommonConstant.TOTAL_ISSUES));
			}
			if (CollectionUtils.isNotEmpty(sprintDetail.getPuntedIssues())) {
				totalIssue.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetail,
						CommonConstant.PUNTED_ISSUES));

			}
			if (CollectionUtils.isNotEmpty(sprintDetail.getAddedIssues())) {
				totalIssue.addAll(sprintDetail.getAddedIssues());
			}

		});

		/** additional filter **/
		KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.SCRUM, DEV, filterHelperService);
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().toList());
		if (CollectionUtils.isNotEmpty(totalIssue)) {
			resultListMap.put(PROJECT_WISE_TOTAL_ISSUE,
					jiraIssueRepository.findIssueByNumber(mapOfFilters, totalIssue, new HashMap<>()));
			resultListMap.put(SPRINT_DETAILS, sprintDetails);
		}
		return resultListMap;
	}
}
