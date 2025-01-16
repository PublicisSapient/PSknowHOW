package com.publicissapient.kpidashboard.apis.common.service.impl;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.BuildStatus;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.application.BuildRepository;
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

	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private SprintRepository sprintRepository;
	@Autowired
	private FilterHelperService filterHelperService;
	@Autowired
	private JiraIssueRepository jiraIssueRepository;
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
}
