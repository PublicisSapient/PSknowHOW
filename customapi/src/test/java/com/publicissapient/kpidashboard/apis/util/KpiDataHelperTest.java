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

package com.publicissapient.kpidashboard.apis.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AdditionalFilterCategoryFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.SprintWiseStoryDataFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;

/**
 * @author anisingh4
 */
@RunWith(MockitoJUnitRunner.class)
public class KpiDataHelperTest {

	@Mock
	FilterHelperService flterHelperService;
	@Mock
	private CacheService cacheService;
	private Map<String, AdditionalFilterCategory> additonalFilterMap;
	private List<SprintWiseStory> sprintWiseStoryList;
	private List<JiraIssue> stories;
	private List<JiraIssueCustomHistory> jiraIssueCustomHistory;
	private FieldMapping fieldMapping;

	@Before
	public void setUp() {
		AdditionalFilterCategoryFactory additionalFilterCategoryFactory = AdditionalFilterCategoryFactory.newInstance();
		List<AdditionalFilterCategory> additionalFilterCategoryList = additionalFilterCategoryFactory
				.getAdditionalFilterCategoryList();
		additonalFilterMap = additionalFilterCategoryList.stream()
				.collect(Collectors.toMap(AdditionalFilterCategory::getFilterCategoryId, x -> x));
		when(flterHelperService.getAdditionalFilterHierarchyLevel()).thenReturn(additonalFilterMap);

		SprintWiseStoryDataFactory sprintWiseStoryDataFactory = SprintWiseStoryDataFactory.newInstance();
		sprintWiseStoryList = sprintWiseStoryDataFactory.getSprintWiseStories();

		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory
				.newInstance("/json/default/iteration/jira_issues_new_structure.json");
		stories = jiraIssueDataFactory.getStories();
		List<String> storyNumber = stories.stream().map(issue -> issue.getNumber()).collect(Collectors.toList());

		JiraIssueHistoryDataFactory jiraIssueHistoryDataFactory = JiraIssueHistoryDataFactory
				.newInstance("/json/default/iteration/jira_issue_custom_history_new_structure.json");
		jiraIssueCustomHistory = jiraIssueHistoryDataFactory.getJiraIssueCustomHistory();
		this.jiraIssueCustomHistory = this.jiraIssueCustomHistory.stream()
				.filter(jiraIssueCustomHistory1 -> storyNumber.contains(jiraIssueCustomHistory1.getStoryID()))
				.collect(Collectors.toList());

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);


	}

	@Test
	public void createAdditionalFilterMap_SPRINT() {

		KpiRequest kpiRequest = createKpiRequest();
		Map<String, List<String>> selectedMap = new HashMap<>();
		selectedMap.put(Constant.SPRINT, Arrays.asList("Test"));
		selectedMap.put("SQD", Arrays.asList("Squad Test"));

		kpiRequest.setSelectedMap(selectedMap);

		Map<String, List<String>> mapOfFilters = new HashMap<>();

		String actual = KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.SCRUM, "",
				flterHelperService);
		assertEquals("SQD", actual);
	}

	@Test
	public void testCreateSubCategoryWiseMap() {
		Map<Pair<String, String>, Map<String, List<String>>> subCategoryWiseMap = KpiDataHelper
				.createSubCategoryWiseMap(Constant.SPRINT, sprintWiseStoryList, "");
		assertEquals(5, subCategoryWiseMap.size());

		Map<Pair<String, String>, Map<String, List<String>>> subCategoryWiseMapNegative = KpiDataHelper
				.createSubCategoryWiseMap(Constant.SCRUM, sprintWiseStoryList, "");
		assertEquals(5, subCategoryWiseMapNegative.size());
	}

	@Test
	public void testProcessSprintIssues_NoSprintMatch() {
		Map<String, List<JiraHistoryChangeLog>> issueKeyWiseHistoryMap = jiraIssueCustomHistory.stream()
				.collect(Collectors.toMap(JiraIssueCustomHistory::getStoryID,
						JiraIssueCustomHistory::getSprintUpdationLog, (existingValue, newValue) -> newValue,
						LinkedHashMap::new));
		Map<String, String> stringMap = KpiDataHelper.processSprintIssues(stories, "sprint1", issueKeyWiseHistoryMap,
				CommonConstant.ADDED);
		assertEquals(0, stringMap.size());
	}

	@Test
	public void testProcessSprintIssues_Added() {

		Map<String, List<JiraHistoryChangeLog>> issueKeyWiseHistoryMap = jiraIssueCustomHistory.stream()
				.collect(Collectors.toMap(JiraIssueCustomHistory::getStoryID,
						JiraIssueCustomHistory::getSprintUpdationLog, (existingValue, newValue) -> newValue,
						LinkedHashMap::new));

		issueKeyWiseHistoryMap.forEach((history, list) -> {
			list.forEach(a -> a.setChangedTo("sprint1"));
		});
		Map<String, String> stringMap = KpiDataHelper.processSprintIssues(stories, "sprint1", issueKeyWiseHistoryMap,
				CommonConstant.ADDED);
		assertEquals(20, stringMap.size());
	}

	@Test
	public void testProcessSprintIssues_Removed() {

		Map<String, List<JiraHistoryChangeLog>> issueKeyWiseHistoryMap = jiraIssueCustomHistory.stream()
				.collect(Collectors.toMap(JiraIssueCustomHistory::getStoryID,
						JiraIssueCustomHistory::getSprintUpdationLog, (existingValue, newValue) -> newValue,
						LinkedHashMap::new));

		issueKeyWiseHistoryMap.forEach((history, list) -> {
			list.forEach(a -> a.setChangedFrom("sprint1"));
		});
		Map<String, String> stringMap = KpiDataHelper.processSprintIssues(stories, "sprint1", issueKeyWiseHistoryMap,
				CommonConstant.REMOVED);
		assertEquals(20, stringMap.size());
	}

	@Test
	public void testCalculateStoryPoints(){
		fieldMapping.setEstimationCriteria("test");
		double v = KpiDataHelper.calculateStoryPoints(stories, fieldMapping);
		assertEquals(92.25,v, 0.0d);
		fieldMapping.setEstimationCriteria("");
		v = KpiDataHelper.calculateStoryPoints(stories, fieldMapping);
		assertEquals(92.25,v, 0.0d);


	}

	private KpiRequest createKpiRequest() {
		KpiRequest kpiRequest = new KpiRequest();
		List<KpiElement> kpiList = new ArrayList<>();
		KpiElement kpiElement = new KpiElement();
		kpiElement.setKpiId(KPICode.DEFECT_COUNT_BY_PRIORITY.getKpiId());
		kpiElement.setKpiName("Defect Count");
		kpiElement.setKpiCategory("Quality");
		kpiElement.setKpiUnit("%");
		kpiElement.setKpiSource("Jira");

		kpiElement.setMaxValue("500");
		kpiElement.setChartType("gaugeChart");
		kpiList.add(kpiElement);
		kpiRequest.setLevel(2);
		kpiRequest.setIds(new String[] { "Alpha_Tower_Id" }); // This is
		// immaterial as
		// all the sprint
		// in Account
		// Hierarchy List created above is sent for processing.
		kpiRequest.setKpiList(kpiList);
		kpiRequest.setRequestTrackerId();
		Map<String, List<String>> selectedMap = new HashMap<>();
		selectedMap.put("ACCOUNT", Arrays.asList("Speedy"));
		kpiRequest.setSelectedMap(selectedMap);
		return kpiRequest;
	}

}