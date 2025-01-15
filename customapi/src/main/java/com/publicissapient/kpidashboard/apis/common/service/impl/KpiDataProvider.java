package com.publicissapient.kpidashboard.apis.common.service.impl;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.excel.CapacityKpiData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.excel.CapacityKpiDataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KpiDataProvider {

	private static final String DEV = "DeveloperKpi";
	private static final String STORY_LIST = "stories";
	private static final String SPRINTSDETAILS = "sprints";
	private static final String JIRA_ISSUE_HISTORY_DATA = "JiraIssueHistoryData";
	private static final String ESTIMATE_TIME = "Estimate_Time";

	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private SprintRepository sprintRepository;
	@Autowired
	private FilterHelperService filterHelperService;
	@Autowired
	private JiraIssueRepository jiraIssueRepository;
	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	@Autowired
	private CapacityKpiDataRepository capacityKpiDataRepository;

	/**
	 * Fetches data from DB for the given project and sprints combination.
	 *
	 * @param kpiRequest
	 * @param basicProjectConfigId
	 * @param sprintList
	 * @param kpiId
	 * @return
	 */
	public Map<String, Object> fetchIssueCountDataFromDB(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList, String kpiId) {
		log.info("Fetching Data for Project {} and KPI {}", basicProjectConfigId.toString(), kpiId);
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
	 * Fetches sprint capacity data from the database for the given project and sprints combination.
	 *
	 * @param kpiRequest The KPI request object.
	 * @param basicProjectConfigId The project config ID.
	 * @param sprintList The list of sprint IDs.
	 * @param kpiId The KPI ID.
	 * @return A map containing estimate time, story list, sprint details, and JiraIssue history.
	 */
	public Map<String, Object> fetchSprintCapacityDataFromDb(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList, String kpiId) {
		log.info("Fetching Data for Project {} and KPI {}", basicProjectConfigId.toString(), kpiId);
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
		capacityMapOfFilters.put(JiraFeature.SPRINT_ID.getFieldValueInFeature(),
				sprintList.stream().distinct().toList());
		capacityMapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().toList());
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
}
