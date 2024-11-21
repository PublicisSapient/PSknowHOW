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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import org.bson.types.ObjectId;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
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
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.data.SprintWiseStoryDataFactory;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

/**
 * This J-Unit class tests the functionality of the DIRServiceImpl.
 * 
 * @author tauakram
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class DIRServiceImplTest {

	@InjectMocks
	DIRServiceImpl dirServiceImpl;
	@Mock
	JiraIssueRepository jiraIssueRepository;
	@Mock
	CacheService cacheService;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	KpiHelperService kpiHelperService;
	@Mock
	ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	FieldMappingRepository fieldMappingRepository;
	@Mock
	CustomApiConfig customApiSetting;
	@Mock
	FilterHelperService filterHelperService;

	@Mock
	private CommonService commonService;
	@Mock
	private ConfigHelperService configHelperService;
	private KpiRequest kpiRequest;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private Map<String, Object> filterLevelMap;
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();

	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();

	private Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.DEFECT_INJECTION_RATE.getKpiId());
		kpiRequest.setLabel("PROJECT");

		ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
		projectBasicConfig.setId(new ObjectId("6335363749794a18e8a4479b"));
		projectBasicConfig.setIsKanban(true);
		projectBasicConfig.setProjectName("Scrum Project");
		projectBasicConfig.setProjectNodeId("Scrum Project_6335363749794a18e8a4479b");
		projectConfigList.add(projectBasicConfig);

		projectConfigList.forEach(projectConfig -> {
			projectConfigMap.put(projectConfig.getProjectName(), projectConfig);
		});
		Mockito.when(cacheService.cacheProjectConfigMapData()).thenReturn(projectConfigMap);

		/// set aggregation criteria kpi wise
		kpiWiseAggregation.put("defectInjectionRate", "percentile");

		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put("defectInjectionRate", Arrays.asList("-30", "30-10", "10-5", "5-2", "2-"));

		when(configHelperService.calculateMaturity()).thenReturn(maturityRangeMap);

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		filterLevelMap = new LinkedHashMap<>();
		filterLevelMap.put("PROJECT", Filters.PROJECT);
		filterLevelMap.put("SPRINT", Filters.SPRINT);

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		configHelperService.setFieldMappingMap(fieldMappingMap);

	}

	@Test
	public void testGetQualifierType() {
		assertThat("Kpi Name :", dirServiceImpl.getQualifierType(), equalTo("DEFECT_INJECTION_RATE"));
	}

	@Test
	public void testGetDIR() throws ApplicationException {

		SprintWiseStoryDataFactory sprintWiseStoryDataFactory = SprintWiseStoryDataFactory.newInstance();

		List<SprintWiseStory> storyData = sprintWiseStoryDataFactory.getSprintWiseStories();

		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();

		List<JiraIssue> defectData = jiraIssueDataFactory.getBugs();

		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put("storyData", storyData);
		resultListMap.put("defectData", defectData);
		resultListMap.put("issueData", jiraIssueDataFactory.getJiraIssues());

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		when(kpiHelperService.fetchDIRDataFromDb(any(), any())).thenReturn(resultListMap);
		String kpiRequestTrackerId = "Excel-dirtrack001";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(customApiConfig.getpriorityP1()).thenReturn(Constant.P1);
		when(customApiConfig.getpriorityP2()).thenReturn(Constant.P2);
		when(customApiConfig.getpriorityP3()).thenReturn(Constant.P3);
		when(customApiConfig.getpriorityP4()).thenReturn("p4-minor");
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		FieldMapping fieldMapping = mock(FieldMapping.class);
		//when(fieldMapping.getEstimationCriteria()).thenReturn(CommonConstant.STORY_POINT);
		try {
			KpiElement kpiElement = dirServiceImpl.getKpiData(this.kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("DIR Value size:", ((List<DataCount>) kpiElement.getTrendValueList()).size(), equalTo(1));

		} catch (ApplicationException enfe) {

		}

	}

	@Test
	public void testFetchKPIDataFromDbData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList, false);
		String startDate = leafNodeList.get(0).getSprintFilter().getStartDate();
		String endDate = leafNodeList.get(leafNodeList.size() - 1).getSprintFilter().getEndDate();

		SprintWiseStoryDataFactory sprintWiseStoryDataFactory = SprintWiseStoryDataFactory.newInstance();
		List<SprintWiseStory> storyData = sprintWiseStoryDataFactory.getSprintWiseStories();

		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
		List<JiraIssue> defectData = jiraIssueDataFactory.getBugs();

		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put("storyData", storyData);
		resultListMap.put("defectData", defectData);

		when(kpiHelperService.fetchDIRDataFromDb(any(), any())).thenReturn(resultListMap);

		Map<String, Object> defectDataListMap = dirServiceImpl.fetchKPIDataFromDb(leafNodeList, startDate, endDate,
				kpiRequest);
		assertThat("Total Story value :", ((List<JiraIssue>) (defectDataListMap.get("storyData"))).size(), equalTo(5));
		assertThat("Total Defects value :", ((List<JiraIssue>) (defectDataListMap.get("defectData"))).size(),
				equalTo(20));
	}

}
