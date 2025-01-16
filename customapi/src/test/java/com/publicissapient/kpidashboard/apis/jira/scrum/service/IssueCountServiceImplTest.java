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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import java.util.*;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.common.service.KpiDataCacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiDataProvider;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.CommonService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.data.SprintDetailsDataFactory;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.service.JiraServiceR;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

@RunWith(MockitoJUnitRunner.class)
public class IssueCountServiceImplTest {

	private Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	private Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	private static final String STORY_LIST = "stories";
	private static final String SPRINTSDETAILS = "sprints";
	public static final String PROJECT_WISE_STORY_CATEGORIES = "projectWiseStoryCategories";
	public static final String PROJECT_WISE_TOTAL_CATEGORIES = "projectWiseTotalCategories";

	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@InjectMocks
	IssueCountServiceImpl issueCountServiceImpl;
	@Mock
	CustomApiConfig customApiConfig;
	@Mock
	private CommonService commonService;
	@Mock
	private FilterHelperService filterHelperService;
	@Mock
	private KpiDataCacheService kpiDataCacheService;
	@Mock
	private	KpiDataProvider kpiDataProvider;

	private Map<String, Object> filterLevelMap;
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	private List<SprintDetails> sprintDetailsList = new ArrayList<>();
	private List<JiraIssue> totalIssueList = new ArrayList<>();

	private KpiRequest kpiRequest;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.ISSUE_COUNT.getKpiId());
		kpiRequest.setLabel("PROJECT");
		kpiRequest.setLevel(5);

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		filterLevelMap = new LinkedHashMap<>();
		filterLevelMap.put("PROJECT", Filters.PROJECT);
		filterLevelMap.put("SPRINT", Filters.SPRINT);

		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();

		totalIssueList = jiraIssueDataFactory.getJiraIssues();

		ProjectBasicConfig projectConfig = new ProjectBasicConfig();
		projectConfig.setId(new ObjectId("6335363749794a18e8a4479b"));
		projectConfig.setProjectName("Scrum Project");
		projectConfigMap.put(projectConfig.getProjectName(), projectConfig);

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		configHelperService.setProjectConfigMap(projectConfigMap);
		configHelperService.setFieldMappingMap(fieldMappingMap);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		/// set aggregation criteria kpi wise
		kpiWiseAggregation.put(KPICode.ISSUE_COUNT.name(), "sum");

		SprintDetailsDataFactory sprintDetailsDataFactory = SprintDetailsDataFactory.newInstance();
		sprintDetailsList = sprintDetailsDataFactory.getSprintDetails();

	}

	@After
	public void cleanup() {

	}

	@Test
	public void testCalculateKPIMetrics() {
		Map<String, Object> subCategoryMap = new HashMap<>();
		Double storyCount = issueCountServiceImpl.calculateKPIMetrics(subCategoryMap);
		assertThat("Story List : ", storyCount, equalTo(0.0));
	}

	@Test
	public void testFetchKPIDataFromDbData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList);
		String startDate = leafNodeList.get(0).getSprintFilter().getStartDate();
		String endDate = leafNodeList.get(leafNodeList.size() - 1).getSprintFilter().getEndDate();
		Map<String, Object> result = new HashMap<>();
		result.put(SPRINTSDETAILS,sprintDetailsList);
		result.put(STORY_LIST, totalIssueList);
		result.put(PROJECT_WISE_STORY_CATEGORIES, new HashMap<>());
		result.put(PROJECT_WISE_TOTAL_CATEGORIES, new HashMap<>());
		when(filterHelperService.isFilterSelectedTillSprintLevel(5, false)).thenReturn(true);
		when(kpiDataCacheService.fetchIssueCountData(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(result);

		Map<String, Object> storyList = issueCountServiceImpl.fetchKPIDataFromDb(leafNodeList, startDate, endDate,
				kpiRequest);
		assertThat("Total Stories : ", storyList.size(), equalTo(4));
	}

	@Test
	public void testGetStoryList() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		Map<String, List<String>> projectWiseJiraIdentification = new HashMap<>();
		Map<String, List<String>> projectWiseStoryCategories = new HashMap<>();
		treeAggregatorDetail.getMapOfListOfLeafNodes().get("sprint").forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			List<String> jiraStoryIdentification = new ArrayList<>();
			List<String> jiraStoryCategory = new ArrayList<>();
			FieldMapping fieldMapping = fieldMappingMap.get(basicProjectConfigId);
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
		});

		Map<String, Object> result = new HashMap<>();
		result.put(SPRINTSDETAILS,sprintDetailsList);
		result.put(STORY_LIST, totalIssueList);
		result.put(PROJECT_WISE_STORY_CATEGORIES, projectWiseStoryCategories);
		result.put(PROJECT_WISE_TOTAL_CATEGORIES, projectWiseJiraIdentification);

		when(filterHelperService.isFilterSelectedTillSprintLevel(5, false)).thenReturn(false);
		when(kpiDataProvider.fetchIssueCountDataFromDB(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(result);

		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(issueCountServiceImpl.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		when(customApiConfig.getpriorityP1()).thenReturn(Constant.P1);
		when(customApiConfig.getpriorityP2()).thenReturn(Constant.P2);
		when(customApiConfig.getpriorityP3()).thenReturn(Constant.P3);
		when(customApiConfig.getpriorityP4()).thenReturn("p4-minor");

		try {
			KpiElement kpiElement = issueCountServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);

			((List<DataCountGroup>) kpiElement.getTrendValueList()).forEach(dc -> {

				String status = dc.getFilter();
				switch (status) {
				case "Story  Count":
					assertThat("Story  Count :", dc.getValue().size(), equalTo(1));
					break;
				case "Total  Count":
					assertThat("Total  Count :", dc.getValue().size(), equalTo(1));
					break;
				default:
					break;
				}
			});

		} catch (ApplicationException enfe) {

		}

	}

	@Test
	public void testQualifierType() {
		String kpiName = KPICode.ISSUE_COUNT.name();
		String type = issueCountServiceImpl.getQualifierType();
		assertThat("KPI NAME : ", type, equalTo(kpiName));
	}

}
