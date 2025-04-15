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
import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.BuildStatus;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectRelease;
import com.publicissapient.kpidashboard.common.model.jira.HappinessKpiData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.ReleaseWisePI;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;
import com.publicissapient.kpidashboard.common.repository.application.BuildRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectReleaseRepo;
import com.publicissapient.kpidashboard.common.repository.excel.CapacityKpiDataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.HappinessKpiDataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepositoryCustom;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KpiDataProvider {

	private static final String DEV = "DeveloperKpi";
	private static final String QA = "QaKpi";
	private static final String STORIES = "stories";
	private static final String SPRINTSDETAILS = "sprints";
	private static final String FIRST_TIME_PASS_STORIES = "ftpStories";
	private static final String SPRINT_WISE_CLOSED_STORIES = "sprintWiseClosedStories";
	private static final String ISSUE_DATA = "Issue Data";
	public static final String DEFECT_FOR_EXCEL = "defect for Excel";
	private static final String JIRA_ISSUE_HISTORY_DATA = "JiraIssueHistoryData";
	private static final String ESTIMATE_TIME = "Estimate_Time";
	private static final Integer SP_CONSTANT = 3;
	private static final String TOTAL_ISSUE = "totalIssue";
	private static final String SPRINT_DETAILS = "sprintDetails";
	private static final String HAPPINESS_INDEX_DETAILS = "happinessIndexDetails";
	private static final String SCOPE_CHANGE_ISSUE_HISTORY = "scopeChangeIssuesHistories";
	private static final String PROJECT_WISE_TOTAL_ISSUE = "projectWiseTotalIssues";
	private static final String SPRINT_WISE_PREDICTABILITY = "predictability";
	private static final String SPRINT_WISE_SPRINT_DETAILS = "sprintWiseSprintDetails";
	private static final String COD_DATA = "costOfDelayData";
	private static final String COD_DATA_HISTORY = "costOfDelayDataHistory";
	private static final String FIELD_MAPPING = "fieldMapping";
	private static final String CREATED_VS_RESOLVED_KEY = "createdVsResolvedKey";
	private static final String SPRINT_WISE_SPRINT_DETAIL_MAP = "sprintWiseSprintDetailMap";
	private static final String SPRINT_WISE_SUB_TASK_BUGS = "sprintWiseSubTaskBugs";
	private static final String SUB_TASK_BUGS_HISTORY = "SubTaskBugsHistory";
	private static final String STORY_LIST = "storyList";
	private static final String REJECTED_DEFECT_DATA = "rejectedBugKey";
	private static final String TOTAL_DEFECT_LIST = "totalDefectList";
	private static final String TOTAL_SPRINT_SUBTASK_DEFECTS = "totalSprintSubtaskDefects";
	private static final String TOTALBUGKEY = "totalBugData";
	private static final String SPRINTSTORIES = "storyData";
	private static final String PROJFMAPPING = "projectFieldMapping";

	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private SprintRepository sprintRepository;
	@Autowired
	private SprintRepositoryCustom sprintRepositoryCustom;
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
	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private ProjectReleaseRepo projectReleaseRepo;
	@Autowired
	private HappinessKpiDataRepository happinessKpiDataRepository;

	/**
	 * Fetches data from DB for the given project and sprints combination.
	 *
	 * @param kpiRequest
	 *          The KPI request object.
	 * @param basicProjectConfigId
	 *          The project config ID.
	 * @param sprintList
	 *          The list of sprint IDs.
	 * @return A map containing story list, sprint details, and JiraIssue history.
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
				totalIssue.addAll(
						KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(dbSprintDetail, CommonConstant.TOTAL_ISSUES));
			}
		});

		// additional filter
		KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.SCRUM, DEV, filterHelperService);

		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(), basicProjectConfigIds);

		if (CollectionUtils.isNotEmpty(totalIssue)) {
			resultListMap.put(STORIES, jiraIssueRepository.findIssueByNumber(mapOfFilters, totalIssue, uniqueProjectMap));
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
	 *          The project config ID.
	 * @param startDate
	 *          The start date
	 * @param endDate
	 *          The end date
	 * @return The list of Build
	 */
	public List<Build> fetchBuildFrequencyData(ObjectId basicProjectConfigId, String startDate, String endDate) {
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
	 *          The KPI request object.
	 * @param basicProjectConfigId
	 *          The project config ID.
	 * @param sprintList
	 *          The list of sprint IDs.
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

		// additional filter **/
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

		capacityMapOfFilters.put(JiraFeature.SPRINT_ID.getFieldValueInFeature(), sprintList.stream().distinct().toList());
		capacityMapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().map(ObjectId::new).distinct().toList());
		resultListMap.put(ESTIMATE_TIME, capacityKpiDataRepository.findByFilters(capacityMapOfFilters, new HashMap<>()));
		List<SprintDetails> sprintDetails = sprintRepository.findBySprintIDIn(sprintList);
		Set<String> totalIssue = new HashSet<>();
		sprintDetails.forEach(dbSprintDetail -> {
			if (CollectionUtils.isNotEmpty(dbSprintDetail.getTotalIssues())) {
				totalIssue.addAll(
						KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(dbSprintDetail, CommonConstant.TOTAL_ISSUES));
			}
		});

		if (CollectionUtils.isNotEmpty(totalIssue)) {
			List<JiraIssue> jiraIssueList = jiraIssueRepository.findIssueByNumberOrParentStoryIdAndType(totalIssue,
					uniqueProjectMap, CommonConstant.NUMBER);
			List<JiraIssue> subTaskList = jiraIssueRepository.findIssueByNumberOrParentStoryIdAndType(
					jiraIssueList.stream().map(JiraIssue::getNumber).collect(Collectors.toSet()), uniqueProjectMapForSubTask,
					CommonConstant.PARENT_STORY_ID);
			List<JiraIssue> jiraIssues = new ArrayList<>();
			jiraIssues.addAll(subTaskList);
			jiraIssues.addAll(jiraIssueList);
			List<JiraIssueCustomHistory> jiraIssueCustomHistoryList = jiraIssueCustomHistoryRepository
					.findByStoryIDInAndBasicProjectConfigIdIn(jiraIssues.stream().map(JiraIssue::getNumber).toList(),
							basicProjectConfigIds.stream().distinct().toList());

			resultListMap.put(STORIES, jiraIssues);
			resultListMap.put(SPRINTSDETAILS, sprintDetails);
			resultListMap.put(JIRA_ISSUE_HISTORY_DATA, jiraIssueCustomHistoryList);
		}

		return resultListMap;
	}

	/**
	 * Fetches sprint Velocity data from the database for the given project and
	 * sprints combination.
	 *
	 * @param kpiRequest
	 *          The KPI request object.
	 * @param basicProjectConfigId
	 *          The project config ID.
	 * @return A map containing estimate time, story list, sprint details, and
	 *         JiraIssue history.
	 */
	public Map<String, Object> fetchSprintVelocityDataFromDb(KpiRequest kpiRequest, ObjectId basicProjectConfigId) {

		Map<String, Object> resultListMap = new HashMap<>();
		Set<ObjectId> basicProjectConfigObjectIds = new HashSet<>();
		basicProjectConfigObjectIds.add(basicProjectConfigId);
		List<String> basicProjectConfigIds = new ArrayList<>();
		basicProjectConfigIds.add(basicProjectConfigId.toString());

		List<String> sprintStatusList = new ArrayList<>();
		sprintStatusList.add(SprintDetails.SPRINT_STATE_CLOSED);
		sprintStatusList.add(SprintDetails.SPRINT_STATE_CLOSED.toLowerCase());
		long time2 = System.currentTimeMillis();
		List<SprintDetails> totalSprintDetails = sprintRepositoryCustom
				.findByBasicProjectConfigIdInAndStateInOrderByStartDateDesc(basicProjectConfigObjectIds, sprintStatusList,
						(long) customApiConfig.getSprintVelocityLimit() + customApiConfig.getSprintCountForFilters());
		log.info("Sprint Velocity findByBasicProjectConfigIdInAndStateInOrderByStartDateDesc method time taking {}",
				System.currentTimeMillis() - time2);

		if (CollectionUtils.isNotEmpty(totalSprintDetails)) {
			resultListMap = kpiHelperService.fetchSprintVelocityDataFromDb(kpiRequest, basicProjectConfigIds,
					totalSprintDetails);
		}
		return resultListMap;
	}

	/**
	 * Fetches sprint Predictability data from the database for the given project
	 * and sprints combination.
	 *
	 * @param kpiRequest
	 *          The KPI request object.
	 * @param basicProjectConfigId
	 *          The project config ID.
	 * @param sprintList
	 *          The list of sprint IDs.
	 * @return A map containing sprint details and JiraIssue list.
	 */
	public Map<String, Object> fetchSprintPredictabilityDataFromDb(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList) {
		Map<String, Object> resultListMap = new HashMap<>();
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		List<String> sprintStatusList = new ArrayList<>();
		List<String> basicProjectConfigIds = new ArrayList<>();
		Set<ObjectId> basicProjectConfigObjectIds = new HashSet<>();
		basicProjectConfigIds.add(basicProjectConfigId.toString());
		basicProjectConfigObjectIds.add(basicProjectConfigId);
		sprintStatusList.add(SprintDetails.SPRINT_STATE_CLOSED);
		sprintStatusList.add(SprintDetails.SPRINT_STATE_CLOSED.toLowerCase());

		List<SprintDetails> totalSprintDetails = sprintRepositoryCustom
				.findByBasicProjectConfigIdInAndStateInOrderByStartDateDesc(basicProjectConfigObjectIds, sprintStatusList,
						(long) customApiConfig.getSprintCountForFilters() + SP_CONSTANT);

		List<String> totalIssueIds = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(totalSprintDetails)) {

			Map<ObjectId, List<SprintDetails>> projectWiseTotalSprintDetails = totalSprintDetails.stream()
					.collect(Collectors.groupingBy(SprintDetails::getBasicProjectConfigId));

			Map<ObjectId, Set<String>> duplicateIssues = kpiHelperService
					.getProjectWiseTotalSprintDetail(projectWiseTotalSprintDetails);
			Map<ObjectId, Map<String, List<LocalDateTime>>> projectWiseDuplicateIssuesWithMinCloseDate = null;
			Map<ObjectId, FieldMapping> fieldMappingMap = configHelperService.getFieldMappingMap();

			if (MapUtils.isNotEmpty(fieldMappingMap) && !duplicateIssues.isEmpty()) {
				Map<ObjectId, List<String>> customFieldMapping = duplicateIssues.keySet().stream()
						.filter(fieldMappingMap::containsKey).collect(Collectors.toMap(Function.identity(), key -> {
							FieldMapping fieldMapping = fieldMappingMap.get(key);
							return Optional.ofNullable(fieldMapping).map(FieldMapping::getJiraIterationCompletionStatusKpi5)
									.orElse(Collections.emptyList());
						}));
				projectWiseDuplicateIssuesWithMinCloseDate = kpiHelperService
						.getMinimumClosedDateFromConfiguration(duplicateIssues, customFieldMapping);
			}

			Map<ObjectId, Map<String, List<LocalDateTime>>> finalProjectWiseDuplicateIssuesWithMinCloseDate = projectWiseDuplicateIssuesWithMinCloseDate;

			List<SprintDetails> projectWiseSprintDetails = new ArrayList<>();
			projectWiseTotalSprintDetails.forEach((projectConfigId, sprintDetailsList) -> {
				List<SprintDetails> sprintDetails = sprintDetailsList.stream()
						.limit((long) customApiConfig.getSprintCountForFilters() + SP_CONSTANT).toList();
				sprintDetails.forEach(dbSprintDetail -> {
					FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
							.get(dbSprintDetail.getBasicProjectConfigId());
					// to modify sprint details on the basis of configuration for the project
					SprintDetails sprintDetail = KpiDataHelper.processSprintBasedOnFieldMappings(dbSprintDetail,
							fieldMapping.getJiraIterationIssuetypeKpi5(), fieldMapping.getJiraIterationCompletionStatusKpi5(),
							finalProjectWiseDuplicateIssuesWithMinCloseDate);
					if (CollectionUtils.isNotEmpty(sprintDetail.getCompletedIssues())) {
						List<String> sprintWiseIssueIds = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetail,
								CommonConstant.COMPLETED_ISSUES);
						totalIssueIds.addAll(sprintWiseIssueIds);
					}
					projectWiseSprintDetails.addAll(sprintDetails);
				});
				resultListMap.put(SPRINT_WISE_SPRINT_DETAIL_MAP, projectWiseSprintDetails);
				mapOfFilters.put(JiraFeature.ISSUE_NUMBER.getFieldValueInFeature(),
						totalIssueIds.stream().distinct().collect(Collectors.toList()));
			});
		} else {
			mapOfFilters.put(JiraFeature.SPRINT_ID.getFieldValueInFeature(),
					sprintList.stream().distinct().collect(Collectors.toList()));
		}

		// additional filter **/
		KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.SCRUM, DEV, filterHelperService);

		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		if (CollectionUtils.isNotEmpty(totalIssueIds)) {
			List<JiraIssue> sprintWiseJiraList = jiraIssueRepository.findIssuesBySprintAndType(mapOfFilters, new HashMap<>());
			resultListMap.put(SPRINT_WISE_PREDICTABILITY, sprintWiseJiraList);
		}
		return resultListMap;
	}

	/**
	 * Fetches Scope Churn KPI data from the database for the given project and
	 * sprints combination.
	 *
	 * @param kpiRequest
	 *          The KPI request object.
	 * @param basicProjectConfigId
	 *          The project config ID.
	 * @param sprintList
	 *          The list of sprint IDs.
	 * @return A map containing story list, sprint details, and Scope change issue
	 *         history.
	 */
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
			KpiDataHelper.prepareFieldMappingDefectTypeTransformation(mapOfProjectFilters, fieldMapping.getJiradefecttype(),
					fieldMapping.getJiraStoryIdentificationKPI164(), JiraFeature.ISSUE_TYPE.getFieldValueInFeature());
			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);
		} else {
			// In Case of no issue type fetching all the issueType for that proj
			uniqueProjectMap.put(basicProjectConfigId.toString(), new HashMap<>());
		}
		uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

		List<SprintDetails> sprintDetails = new ArrayList<>(sprintRepository.findBySprintIDIn(sprintList));
		sprintDetails.forEach(dbSprintDetail -> {
			if (CollectionUtils.isNotEmpty(dbSprintDetail.getCompletedIssues())) {
				totalIssue.addAll(
						KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(dbSprintDetail, CommonConstant.COMPLETED_ISSUES));
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

		// additional filter **/
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

	/**
	 * Fetches Commitment Reliability KPI data from the database for the given
	 * project and sprints combination.
	 *
	 * @param kpiRequest
	 *          The KPI request object.
	 * @param basicProjectConfigId
	 *          The project config ID.
	 * @param sprintList
	 *          The list of sprint IDs.
	 * @return A map containing story list, sprint details.
	 */
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
						return Optional.ofNullable(fieldMapping).map(FieldMapping::getJiraIterationCompletionStatusKpi72)
								.orElse(Collections.emptyList());
					}));
			projectWiseDuplicateIssuesWithMinCloseDate = kpiHelperService
					.getMinimumClosedDateFromConfiguration(duplicateIssues, customFieldMapping);
		}

		Map<ObjectId, Map<String, List<LocalDateTime>>> finalProjectWiseDuplicateIssuesWithMinCloseDate = projectWiseDuplicateIssuesWithMinCloseDate;
		Set<String> totalIssue = new HashSet<>();
		sprintDetails.forEach(dbSprintDetail -> {
			FieldMapping fieldMapping = fieldMappingMap.get(dbSprintDetail.getBasicProjectConfigId());
			// to modify sprint details on the basis of configuration for the project
			SprintDetails sprintDetail = KpiDataHelper.processSprintBasedOnFieldMappings(dbSprintDetail,
					fieldMapping.getJiraIterationIssuetypeKpi72(), fieldMapping.getJiraIterationCompletionStatusKpi72(),
					finalProjectWiseDuplicateIssuesWithMinCloseDate);
			if (CollectionUtils.isNotEmpty(sprintDetail.getTotalIssues())) {
				totalIssue.addAll(
						KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetail, CommonConstant.TOTAL_ISSUES));
			}
			if (CollectionUtils.isNotEmpty(sprintDetail.getPuntedIssues())) {
				totalIssue.addAll(
						KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetail, CommonConstant.PUNTED_ISSUES));
			}
			if (CollectionUtils.isNotEmpty(sprintDetail.getAddedIssues())) {
				totalIssue.addAll(sprintDetail.getAddedIssues());
			}
		});

		// additional filter **/
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

	/**
	 * Fetches Cost of Delay KPI data from the database for the given project and
	 * sprints combination.
	 *
	 * @param basicProjectConfigId
	 *          The project config ID.
	 * @return A map containing cost of delay data.
	 */
	public Map<String, Object> fetchCostOfDelayData(ObjectId basicProjectConfigId) {
		Map<String, Object> resultListMap = new HashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		Map<ObjectId, FieldMapping> fieldMappingMap = configHelperService.getFieldMappingMap();
		Map<String, List<String>> closedStatusMap = new HashMap<>();
		Map<String, Object> mapOfFilters = new LinkedHashMap<>();
		List<String> basicProjectConfigIds = List.of(basicProjectConfigId.toString());

		FieldMapping fieldMapping = fieldMappingMap.get(basicProjectConfigId);
		List<String> jiraCloseStatuses = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(fieldMapping.getClosedIssueStatusToConsiderKpi113())) {
			jiraCloseStatuses.addAll(fieldMapping.getClosedIssueStatusToConsiderKpi113());
		}
		List<String> jiraIssueType = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(fieldMapping.getIssueTypesToConsiderKpi113())) {
			jiraIssueType.addAll(fieldMapping.getIssueTypesToConsiderKpi113());
		}
		closedStatusMap.put(basicProjectConfigId.toString(), jiraCloseStatuses.stream().map(String::toLowerCase).toList());
		mapOfFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(), jiraIssueType);
		mapOfFilters.put(JiraFeature.STATUS.getFieldValueInFeature(), jiraCloseStatuses);
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().toList());
		uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfFilters);

		List<JiraIssue> codList = jiraIssueRepository.findIssuesByFilterAndProjectMapFilter(new HashMap<>(),
				uniqueProjectMap);
		List<JiraIssueCustomHistory> codHistory = jiraIssueCustomHistoryRepository.findByStoryIDInAndBasicProjectConfigIdIn(
				codList.stream().map(JiraIssue::getNumber).toList(), new ArrayList<>(uniqueProjectMap.keySet()));
		resultListMap.put(COD_DATA, codList);
		resultListMap.put(COD_DATA_HISTORY, codHistory);
		resultListMap.put(FIELD_MAPPING, closedStatusMap);

		return resultListMap;
	}

	/**
	 * Fetches Release Frequency KPI data from the database for the given project
	 * and sprints combination.
	 *
	 * @param basicProjectConfigId
	 *          The project config ID.
	 * @return A list containing Project releases data.
	 */
	public List<ProjectRelease> fetchProjectReleaseData(ObjectId basicProjectConfigId) {
		log.info("Fetching Release Frequency KPI Data for Project {}", basicProjectConfigId.toString());
		return projectReleaseRepo.findByConfigIdIn(List.of(basicProjectConfigId));
	}

	/**
	 * Fetches PI Predictability KPI data from the database for the given project
	 * and sprints combination.
	 *
	 * @param basicProjectConfigId
	 *          The project config ID.
	 * @return A list containing Project releases data.
	 */
	public List<JiraIssue> fetchPiPredictabilityData(ObjectId basicProjectConfigId) {
		log.info("Fetching PI Predictability KPI Data for Project {}", basicProjectConfigId.toString());
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();

		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
		List<String> issueTypeList;
		if (Optional.ofNullable(fieldMapping.getJiraIssueEpicTypeKPI153()).isPresent()) {
			issueTypeList = fieldMapping.getJiraIssueEpicTypeKPI153();
		} else {
			issueTypeList = new ArrayList<>();
		}

		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				List.of(basicProjectConfigId.toString()));
		mapOfFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(), issueTypeList);

		Map<String, List<String>> projectWisePIList = new HashMap<>();
		List<ReleaseWisePI> releaseWisePIList = jiraIssueRepository.findUniqueReleaseVersionByUniqueTypeName(mapOfFilters);
		Map<String, List<ReleaseWisePI>> projectWiseData = releaseWisePIList.stream()
				.collect(Collectors.groupingBy(ReleaseWisePI::getBasicProjectConfigId));

		projectWiseData.forEach((projectId, releaseWiseData) -> {
			Map<String, List<ReleaseWisePI>> versionWiseData = releaseWiseData.stream()
					.filter(releaseWisePI -> CollectionUtils.isNotEmpty(releaseWisePI.getReleaseName()))
					.collect(Collectors.groupingBy(releaseWisePI -> releaseWisePI.getReleaseName().get(0)));
			versionWiseData.forEach((version, piData) -> {
				if (CollectionUtils.isNotEmpty(piData) && CollectionUtils.isNotEmpty(issueTypeList) &&
						piData.stream().anyMatch(releaseWisePI -> issueTypeList.contains(releaseWisePI.getUniqueTypeName()))) {
					projectWisePIList.putIfAbsent(projectId, new ArrayList<>());
					projectWisePIList.computeIfPresent(projectId, (k, v) -> {
						Optional<ReleaseWisePI> epicPIData = piData.stream()
								.filter(releaseWisePI -> issueTypeList.contains(releaseWisePI.getUniqueTypeName())).findFirst();
						epicPIData.ifPresent(releaseWisePI -> v.add(releaseWisePI.getReleaseName().get(0)));
						return v;
					});
				}
			});
		});

		projectWisePIList.forEach((projectId, piDataList) -> {
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
			mapOfProjectFilters.put(CommonConstant.RELEASE, CommonUtils.convertToPatternListForSubString(piDataList));
			uniqueProjectMap.put(projectId, mapOfProjectFilters);
		});

		return jiraIssueRepository.findByRelease(mapOfFilters, uniqueProjectMap);
	}

	/**
	 * Fetches Created vs Resolved KPI data from the database for the given project
	 * and sprints combination.
	 *
	 * @param kpiRequest
	 *          The KPI request object.
	 * @param basicProjectConfigId
	 *          The project config ID.
	 * @param sprintList
	 *          The list of sprint IDs.
	 * @return A map containing list of sub-tasks, list of sub-task history, sprint
	 *         details.
	 */
	public Map<String, Object> fetchCreatedVsResolvedData(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList) {
		log.info("Fetching Created vs Resolved KPI Data for Project {}", basicProjectConfigId.toString());

		Map<String, Object> resultListMap = new HashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		List<String> basicProjectConfigIds = List.of(basicProjectConfigId.toString());

		List<String> defectType = new ArrayList<>();
		Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
		defectType.add(NormalizedJira.DEFECT_TYPE.getValue());
		mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(), defectType);
		uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

		List<SprintDetails> sprintDetails = sprintRepository.findBySprintIDIn(sprintList);
		Set<String> totalNonBugIssues = new HashSet<>();
		Set<String> totalIssue = new HashSet<>();
		Set<String> totalIssueInSprint = new HashSet<>();
		sprintDetails.forEach(sprintDetail -> {
			if (CollectionUtils.isNotEmpty(sprintDetail.getTotalIssues())) {
				FieldMapping fieldMapping = configHelperService.getFieldMapping(sprintDetail.getBasicProjectConfigId());
				totalNonBugIssues.addAll(sprintDetail.getTotalIssues().stream()
						.filter(sprintIssue -> !fieldMapping.getJiradefecttype().contains(sprintIssue.getTypeName()))
						.map(SprintIssue::getNumber).collect(Collectors.toSet()));
				totalIssue.addAll(
						KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetail, CommonConstant.TOTAL_ISSUES));
			}
			totalIssueInSprint
					.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetail, CommonConstant.TOTAL_ISSUES));
			totalIssueInSprint.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetail,
					CommonConstant.COMPLETED_ISSUES_ANOTHER_SPRINT));
			totalIssueInSprint.addAll(
					KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetail, CommonConstant.PUNTED_ISSUES));
			totalIssueInSprint
					.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetail, CommonConstant.ADDED_ISSUES));
		});

		// additional filter **/
		KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.SCRUM, DEV, filterHelperService);

		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		if (CollectionUtils.isNotEmpty(totalIssue)) {
			List<JiraIssue> totalSprintReportDefects = jiraIssueRepository.findIssueByNumber(mapOfFilters, totalIssue,
					uniqueProjectMap);
			resultListMap.put(CREATED_VS_RESOLVED_KEY, totalSprintReportDefects);

			List<JiraIssue> subTaskBugs = jiraIssueRepository
					.findLinkedDefects(mapOfFilters, totalNonBugIssues, uniqueProjectMap).stream()
					.filter(jiraIssue -> !totalIssueInSprint.contains(jiraIssue.getNumber())).collect(Collectors.toList());
			List<JiraIssueCustomHistory> subTaskBugsCustomHistory = jiraIssueCustomHistoryRepository
					.findByStoryIDInAndBasicProjectConfigIdIn(
							subTaskBugs.stream().map(JiraIssue::getNumber).collect(Collectors.toList()),
							basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));
			resultListMap.put(SPRINT_WISE_SUB_TASK_BUGS, subTaskBugs);
			resultListMap.put(SUB_TASK_BUGS_HISTORY, subTaskBugsCustomHistory);
			resultListMap.put(SPRINT_WISE_SPRINT_DETAIL_MAP, sprintDetails);
			resultListMap.put(STORY_LIST, jiraIssueRepository.findIssueAndDescByNumber(new ArrayList<>(totalIssue)));
		}
		return resultListMap;
	}

	/**
	 * Fetches sprint Predictability data from the database for the given project
	 * and sprints combination.
	 *
	 * @param sprintList
	 *          The list of sprint IDs.
	 * @return A map containing sprint details and Happiness KPI Data list.
	 */
	public Map<String, Object> fetchHappinessIndexDataFromDb(List<String> sprintList) {

		Map<String, Object> resultListMap = new HashMap<>();
		List<SprintDetails> sprintDetails = sprintRepository.findBySprintIDIn(sprintList);
		List<HappinessKpiData> happinessKpiDataList = happinessKpiDataRepository.findBySprintIDIn(sprintList);
		// filtering rating of 0 i.e not entered any rating
		happinessKpiDataList.forEach(happinessKpiData -> happinessKpiData.getUserRatingList()
				.removeIf(userRatingData -> userRatingData.getRating() == null || userRatingData.getRating().equals(0)));
		resultListMap.put(SPRINT_DETAILS, sprintDetails);
		resultListMap.put(HAPPINESS_INDEX_DETAILS, happinessKpiDataList);

		return resultListMap;
	}

	/**
	 * Fetches DIR KPI data from the database for the given project and sprints
	 * combination.
	 *
	 * @param kpiRequest
	 *          The KPI request object.
	 * @param basicProjectConfigId
	 *          The project config ID.
	 * @param sprintList
	 *          The list of sprint IDs.
	 * @return A map containing list of jira issues and list of defects
	 */
	public Map<String, Object> fetchDefectInjectionRateDataFromDb(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList) {

		return kpiHelperService.fetchDIRDataFromDb(basicProjectConfigId, kpiRequest, sprintList);
	}

	/**
	 * Fetches DD KPI data from the database for the given project and sprints
	 * combination.
	 *
	 * @param kpiRequest
	 *          The KPI request object.
	 * @param basicProjectConfigId
	 *          The project config ID.
	 * @param sprintList
	 *          The list of sprint IDs.
	 * @return A map containing list of jira issues and list of defects
	 */
	public Map<String, Object> fetchDefectDensityDataFromDb(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList) {

		return kpiHelperService.fetchQADDFromDb(basicProjectConfigId, kpiRequest, sprintList);
	}

	/**
	 * Fetches Defect Rejection Rate KPI data from the database for the given
	 * project and sprints combination.
	 *
	 * @param kpiRequest
	 *          The KPI request object.
	 * @param basicProjectConfigId
	 *          The project config ID.
	 * @param sprintList
	 *          The list of sprint IDs.
	 * @return A map containing list of sub-tasks, list of sub-task history, sprint
	 *         details.
	 */
	public Map<String, Object> fetchDRRData(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList) {
		log.info("Fetching Defect Rejection Rate KPI Data for Project {}", basicProjectConfigId.toString());

		Map<String, Object> resultListMap = new HashMap<>();
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		Map<String, Map<String, List<String>>> defectResolutionRejectionMap = new HashMap<>();
		List<String> defectType = new ArrayList<>();

		List<String> basicProjectConfigIds = List.of(basicProjectConfigId.toString());
		Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
		defectType.add(NormalizedJira.DEFECT_TYPE.getValue());
		KpiHelperService.getDroppedDefectsFilters(defectResolutionRejectionMap, basicProjectConfigId,
				fieldMapping.getResolutionTypeForRejectionKPI37(), fieldMapping.getJiraDefectRejectionStatusKPI37());
		mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
				CommonUtils.convertToPatternList(defectType));
		uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

		List<SprintDetails> sprintDetails = sprintRepository.findBySprintIDIn(sprintList);

		Set<String> totalSprintReportStories = new HashSet<>();
		Set<String> totalIssue = new HashSet<>();
		Set<String> totalIssueInSprint = new HashSet<>();
		sprintDetails.forEach(sprintDetail -> {
			if (CollectionUtils.isNotEmpty(sprintDetail.getTotalIssues())) {
				FieldMapping fieldMapping1 = configHelperService.getFieldMapping(sprintDetail.getBasicProjectConfigId());
				totalSprintReportStories.addAll(sprintDetail.getTotalIssues().stream()
						.filter(sprintIssue -> !fieldMapping1.getJiradefecttype().contains(sprintIssue.getTypeName()))
						.map(SprintIssue::getNumber).collect(Collectors.toSet()));
				totalIssue.addAll(
						KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetail, CommonConstant.TOTAL_ISSUES));
			}
			totalIssueInSprint
					.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetail, CommonConstant.TOTAL_ISSUES));
			totalIssueInSprint.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetail,
					CommonConstant.COMPLETED_ISSUES_ANOTHER_SPRINT));
			totalIssueInSprint.addAll(
					KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetail, CommonConstant.PUNTED_ISSUES));
			totalIssueInSprint
					.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetail, CommonConstant.ADDED_ISSUES));
		});

		// additional filter * */
		KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.SCRUM, DEV, filterHelperService);

		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));
		List<JiraIssue> totalDefectList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(totalIssue)) {
			List<JiraIssue> totalSprintReportDefects = jiraIssueRepository.findIssueByNumber(mapOfFilters, totalIssue,
					uniqueProjectMap);

			List<JiraIssue> totalSubTaskDefects = jiraIssueRepository
					.findLinkedDefects(mapOfFilters, totalSprintReportStories, uniqueProjectMap).stream()
					.filter(jiraIssue -> !totalIssueInSprint.contains(jiraIssue.getNumber())).collect(Collectors.toList());

			List<JiraIssueCustomHistory> subTaskBugsCustomHistory = jiraIssueCustomHistoryRepository
					.findByStoryIDInAndBasicProjectConfigIdIn(
							totalSubTaskDefects.stream().map(JiraIssue::getNumber).collect(Collectors.toList()),
							basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));
			totalDefectList.addAll(totalSprintReportDefects);
			totalDefectList.addAll(totalSubTaskDefects);
			resultListMap.put(TOTAL_SPRINT_SUBTASK_DEFECTS, totalSubTaskDefects);
			resultListMap.put(SUB_TASK_BUGS_HISTORY, subTaskBugsCustomHistory);
			resultListMap.put(SPRINT_WISE_SPRINT_DETAILS, sprintDetails);
			resultListMap.put(STORY_LIST, jiraIssueRepository.findIssueAndDescByNumber(new ArrayList<>(totalIssue)));
		}
		// Find defect with rejected status. Avoided making dB query
		if (!defectResolutionRejectionMap.isEmpty()) {
			List<JiraIssue> canceledDefectList = new ArrayList<>();
			getDefectsWithDrop(defectResolutionRejectionMap, totalDefectList, canceledDefectList);
			resultListMap.put(REJECTED_DEFECT_DATA, canceledDefectList);
		} else {
			resultListMap.put(REJECTED_DEFECT_DATA, Lists.newArrayList());
		}
		resultListMap.put(TOTAL_DEFECT_LIST, totalDefectList);

		return resultListMap;
	}

	private void getDefectsWithDrop(Map<String, Map<String, List<String>>> droppedDefects, List<JiraIssue> defectDataList,
			List<JiraIssue> defectListWthDrop) {
		if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(defectDataList)) {
			Set<JiraIssue> defectListWthDropSet = new HashSet<>();
			defectDataList.forEach(jiraIssue -> {
				if (!StringUtils.isBlank(jiraIssue.getStatus())) {
					Map<String, List<String>> defectStatus = droppedDefects.get(jiraIssue.getBasicProjectConfigId());
					if (!defectStatus.isEmpty() && (StringUtils.isNotEmpty(jiraIssue.getResolution()) &&
							CollectionUtils.isNotEmpty(defectStatus.get(Constant.RESOLUTION_TYPE_FOR_REJECTION)) &&
							defectStatus.get(Constant.RESOLUTION_TYPE_FOR_REJECTION).stream().map(String::toLowerCase).toList()
									.contains(jiraIssue.getResolution().toLowerCase()) ||
							(StringUtils.isNotEmpty(jiraIssue.getStatus()) &&
									CollectionUtils.isNotEmpty(defectStatus.get(Constant.DEFECT_REJECTION_STATUS)) &&
									defectStatus.get(Constant.DEFECT_REJECTION_STATUS).stream().map(String::toLowerCase).toList()
											.contains(jiraIssue.getStatus().toLowerCase())))) {
						defectListWthDropSet.add(jiraIssue);
					}
				}
			});
			defectListWthDrop.addAll(defectListWthDropSet);
		}
	}

	public Map<String, Object> fetchFirstTimePassRateDataFromDb(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList) {

		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Object> resultListMap = new HashMap<>();
		List<String> basicProjectConfigIds = new ArrayList<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		Map<String, Map<String, List<String>>> statusConfigsOfRejectedStoriesByProject = new HashMap<>();
		Map<String, Map<String, Integer>> projectWisePriorityCount = new HashMap<>();
		Map<String, List<String>> configPriority = customApiConfig.getPriority();
		Map<String, Set<String>> projectWiseRCA = new HashMap<>();

		Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
		basicProjectConfigIds.add(basicProjectConfigId.toString());

		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

		KpiHelperService.addPriorityCountProjectWiseForQuality(projectWisePriorityCount, configPriority,
				basicProjectConfigId, fieldMapping.getDefectPriorityKPI82());
		KpiHelperService.addRCAProjectWiseForQualityKPIs(projectWiseRCA, basicProjectConfigId,
				fieldMapping.getIncludeRCAForKPI82());

		if (Optional.ofNullable(fieldMapping.getJiraKPI82StoryIdentification()).isPresent()) {
			KpiDataHelper.prepareFieldMappingDefectTypeTransformation(mapOfProjectFilters, fieldMapping.getJiradefecttype(),
					fieldMapping.getJiraKPI82StoryIdentification(), JiraFeature.ISSUE_TYPE.getFieldValueInFeature());
		}
		if (CollectionUtils.isNotEmpty(fieldMapping.getJiraLabelsKPI82())) {
			mapOfProjectFilters.put(JiraFeature.LABELS.getFieldValueInFeature(),
					CommonUtils.convertToPatternList(fieldMapping.getJiraLabelsKPI82()));
		}

		mapOfProjectFilters.put(JiraFeature.JIRA_ISSUE_STATUS.getFieldValueInFeature(),
				fieldMapping.getJiraIssueDeliverdStatusKPI82());
		KpiHelperService.getDroppedDefectsFilters(statusConfigsOfRejectedStoriesByProject, basicProjectConfigId,
				fieldMapping.getResolutionTypeForRejectionKPI82(), fieldMapping.getJiraDefectRejectionStatusKPI82());

		uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

		/** additional filter * */
		KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.SCRUM, DEV, filterHelperService);

		mapOfFilters.put(JiraFeature.SPRINT_ID.getFieldValueInFeature(),
				sprintList.stream().distinct().collect(Collectors.toList()));
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		// Fetch Story ID grouped by Sprint
		List<SprintWiseStory> sprintWiseStories = jiraIssueRepository.findIssuesGroupBySprint(mapOfFilters,
				uniqueProjectMap, kpiRequest.getFilterToShowOnTrend(), DEV);
		List<JiraIssue> issuesBySprintAndType = jiraIssueRepository.findIssuesBySprintAndType(mapOfFilters,
				uniqueProjectMap);
		// do not change the order of remove methods
		List<JiraIssue> defectListWoDrop = new ArrayList<>();
		KpiHelperService.getDefectsWithoutDrop(statusConfigsOfRejectedStoriesByProject, issuesBySprintAndType,
				defectListWoDrop);

		KpiHelperService.removeRejectedStoriesFromSprint(sprintWiseStories, defectListWoDrop);
		Set<JiraIssue> defectForExcel = new HashSet<>();
		removeStoriesWithDefect(defectListWoDrop, projectWisePriorityCount, projectWiseRCA,
				statusConfigsOfRejectedStoriesByProject, defectForExcel);

		List<String> storyIds = getIssueIds(defectListWoDrop);
		List<JiraIssueCustomHistory> storiesHistory = jiraIssueCustomHistoryRepository.findByStoryIDIn(storyIds);
		defectListWoDrop.removeIf(issue -> kpiHelperService.hasReturnTransactionOrFTPRRejectedStatus(issue, storiesHistory,
				fieldMapping.getJiraStatusForDevelopmentKPI82(), fieldMapping.getJiraStatusForQaKPI82(),
				fieldMapping.getJiraFtprRejectStatusKPI82()));

		List<String> storyIdList = new ArrayList<>();
		sprintWiseStories.forEach(s -> storyIdList.addAll(s.getStoryList()));
		resultListMap.put(SPRINT_WISE_CLOSED_STORIES, sprintWiseStories);
		resultListMap.put(FIRST_TIME_PASS_STORIES, defectListWoDrop);
		resultListMap.put(ISSUE_DATA, jiraIssueRepository.findIssueAndDescByNumber(storyIdList));
		resultListMap.put(DEFECT_FOR_EXCEL, defectForExcel);
		return resultListMap;
	}

	/**
	 * @param issuesBySprintAndType
	 * @param projectWisePriority
	 * @param projectWiseRCA
	 * @param defectForExcel
	 */
	private void removeStoriesWithDefect(List<JiraIssue> issuesBySprintAndType,
			Map<String, Map<String, Integer>> projectWisePriority, Map<String, Set<String>> projectWiseRCA,
			Map<String, Map<String, List<String>>> statusConfigsOfRejectedStoriesByProject, Set<JiraIssue> defectForExcel) {
		List<JiraIssue> allDefects = jiraIssueRepository
				.findByTypeNameAndDefectStoryIDIn(NormalizedJira.DEFECT_TYPE.getValue(), getIssueIds(issuesBySprintAndType));
		Set<JiraIssue> defects = new HashSet<>();
		List<JiraIssue> defectListWoDrop = new ArrayList<>();
		allDefects.stream().forEach(d -> issuesBySprintAndType.stream().forEach(i -> {
			if (i.getProjectName().equalsIgnoreCase(d.getProjectName())) {
				defects.add(d);
			}
		}));
		KpiHelperService.getDefectsWithoutDrop(statusConfigsOfRejectedStoriesByProject, new ArrayList<>(defects),
				defectListWoDrop);

		final List<JiraIssue> remainingDefects = KpiHelperService.excludeDefectByPriorityCount(projectWisePriority,
				defects);

		List<JiraIssue> notFTPRDefects = new ArrayList<>();
		for (JiraIssue jiraIssue : defects) {
			// Filter priorityRemaining based on configured Root Causes (RCA) for the
			// project, or include if no RCA is configured.
			rcaDefects(projectWiseRCA, notFTPRDefects, jiraIssue);
		}

		Set<String> storyIdsWithDefect = new HashSet<>();

		remainingDefects.stream().forEach(pi -> notFTPRDefects.stream().forEach(ri -> {
			if (pi.getNumber().equalsIgnoreCase(ri.getNumber())) {
				storyIdsWithDefect.addAll(ri.getDefectStoryID());
				defectForExcel.add(ri);
			}
		}));
		issuesBySprintAndType.removeIf(issue -> storyIdsWithDefect.contains(issue.getNumber()));
	}

	private static void rcaDefects(Map<String, Set<String>> projectWiseRCA, List<JiraIssue> notFTPRDefects,
			JiraIssue jiraIssue) {
		if (CollectionUtils.isNotEmpty(projectWiseRCA.get(jiraIssue.getBasicProjectConfigId()))) {
			for (String toFindRca : jiraIssue.getRootCauseList()) {
				if ((projectWiseRCA.get(jiraIssue.getBasicProjectConfigId()).contains(toFindRca.toLowerCase()))) {
					notFTPRDefects.add(jiraIssue);
				}
			}
		} else {
			notFTPRDefects.add(jiraIssue);
		}
	}

	@NotNull
	private List<String> getIssueIds(List<JiraIssue> issuesBySprintAndType) {
		List<String> storyIds = new ArrayList<>();
		CollectionUtils.emptyIfNull(issuesBySprintAndType).forEach(story -> storyIds.add(story.getNumber()));
		return storyIds;
	}

	/**
	 * Fetches data from DB for the given project and sprints combination.
	 *
	 * @param kpiRequest
	 *          The KPI request object.
	 * @param basicProjectConfigId
	 *          The project config ID.
	 * @param sprintList
	 *          The list of sprint IDs.
	 * @return A map containing story list, sprint details, and JiraIssue history.
	 */
	public Map<String, Object> fetchDSRData(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList) {

		Map<String, Object> resultListMap = new HashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, FieldMapping> projFieldMapping = new HashMap<>();
		Map<String, Map<String, List<String>>> droppedDefects = new HashMap<>();
		Map<String, List<String>> projectWisePriority = new HashMap<>();
		Map<String, List<String>> configPriority = customApiConfig.getPriority();
		Map<String, Set<String>> projectWiseRCA = new HashMap<>();

		List<String> basicProjectConfigIds = List.of(basicProjectConfigId.toString());

		Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
		projFieldMapping.put(basicProjectConfigId.toString(), fieldMapping);

		KpiHelperService.addPriorityProjectWise(projectWisePriority, configPriority, basicProjectConfigId.toString(),
				fieldMapping.getDefectPriorityKPI35());
		KpiHelperService.addRCAProjectWise(projectWiseRCA, basicProjectConfigId.toString(),
				fieldMapping.getIncludeRCAForKPI35());

		mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
				CommonUtils.convertToPatternList(fieldMapping.getJiraIssueTypeKPI35()));
		uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);
		KpiHelperService.getDroppedDefectsFilters(droppedDefects, basicProjectConfigId,
				fieldMapping.getResolutionTypeForRejectionKPI35(), fieldMapping.getJiraDefectRejectionStatusKPI35());

		// additional filter * */
		KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.SCRUM, QA, filterHelperService);

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
				List.of(NormalizedJira.DEFECT_TYPE.getValue()));

		List<JiraIssue> totalDefectList = jiraIssueRepository.findIssuesByType(mapOfFiltersWithStoryIds);

		List<JiraIssue> defectListWoDrop = new ArrayList<>();
		List<JiraIssue> remainingDefect = new ArrayList<>();

		KpiHelperService.getDefectsWithoutDrop(droppedDefects, totalDefectList, defectListWoDrop);
		exludePriorityDefect(projectWisePriority, projectWiseRCA, new HashSet<>(defectListWoDrop), remainingDefect);

		resultListMap.put(SPRINTSTORIES, sprintWiseStoryList);
		resultListMap.put(TOTALBUGKEY, remainingDefect);
		resultListMap.put(PROJFMAPPING, projFieldMapping);
		resultListMap.put(STORY_LIST, storyListWoDrop);
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
}
