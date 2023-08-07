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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

/**
 * This J-Unit class tests the functionality of the DCServiceImpl.
 * 
 * @author tauakram
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class DCServiceImplTest {

	private static final String TOTAL_DEFECT_DATA = "totalBugKey";
	private static final String SPRINT_WISE_STORY_DATA = "storyData";
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	List<JiraIssue> totalBugList = new ArrayList<>();
	String P1 = "p1,p1-blocker,blocker, 1, 0, p0";
	String P2 = "p2, critical, p2-critical, 2";
	String P3 = "p3, p3-major, major, 3";
	String P4 = "p4, p4-minor, minor, 4, p5-trivial, 5,trivial";
	@Mock
	JiraIssueRepository jiraIssueRepository;
	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	KpiHelperService kpiHelperService;
	@InjectMocks
	DCServiceImpl dcServiceImpl;
	@Mock
	ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	FieldMappingRepository fieldMappingRepository;
	@Mock
	CustomApiConfig customApiConfig;
	private List<SprintWiseStory> sprintWiseStoryList = new ArrayList<>();
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private Map<String, Object> filterLevelMap;
	private KpiRequest kpiRequest;
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	private List<DataCount> trendValues = new ArrayList<>();
	private Map<String, List<DataCount>> trendValueMap = new LinkedHashMap<>();
	@Mock
	private FilterHelperService filterHelperService;
	@Mock
	private CommonService commonService;

	@Before
	public void setup() {

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance("");
		kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.DEFECT_COUNT_BY_PRIORITY.getKpiId());
		kpiRequest.setLabel("PROJECT");

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		filterLevelMap = new LinkedHashMap<>();
		filterLevelMap.put("PROJECT", Filters.PROJECT);
		filterLevelMap.put("SPRINT", Filters.SPRINT);

		kpiWiseAggregation.put("cost_Of_Delay", "sum");

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

		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
		totalBugList = jiraIssueDataFactory.getBugs();

		SprintWiseStoryDataFactory sprintWiseStoryDataFactory = SprintWiseStoryDataFactory.newInstance();
		sprintWiseStoryList = sprintWiseStoryDataFactory.getSprintWiseStories();

		// set aggregation criteria kpi wise
		kpiWiseAggregation.put("defectCountByPriority", "sum");

		setTreadValuesDataCount();

	}

	@After
	public void cleanup() {
		jiraIssueRepository.deleteAll();

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetDC() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put("defectCountByPriority", Arrays.asList("-390", "390-309", "309-221", "221-140", "140-"));
		maturityRangeMap.put("defectPriorityWeight", Arrays.asList("10", "7", "5", "3"));

		when(customApiConfig.getApplicationDetailedLogger()).thenReturn("On");

		when(jiraIssueRepository.findIssuesGroupBySprint(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(sprintWiseStoryList);

		when(jiraIssueRepository.findIssuesByType(Mockito.any())).thenReturn(totalBugList);

		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(dcServiceImpl.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		when(customApiConfig.getpriorityP1()).thenReturn(P1);
		when(customApiConfig.getpriorityP2()).thenReturn(P2);
		when(customApiConfig.getpriorityP3()).thenReturn(P3);
		when(customApiConfig.getpriorityP4()).thenReturn(P4);

		try {
			KpiElement kpiElement = dcServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);

			((List<DataCount>) kpiElement.getTrendValueList()).forEach(dc -> {

				String priority = dc.getData();
				switch (priority) {
				case "High":
					assertThat("DC Value :", dc.getCount(), equalTo(1));
					break;
				case "Low":
					assertThat("DC Value :", dc.getCount(), equalTo(1));
					break;
				case "Medium":
					assertThat("DC Value :", dc.getCount(), equalTo(1));
					break;
				case "Critical":
					assertThat("DC Value :", dc.getCount(), equalTo(1));
					break;

				default:
					break;
				}

			});

		} catch (ApplicationException enfe) {

		}
	}

	@Test
	public void testGetQualifierType() {
		assertThat(dcServiceImpl.getQualifierType(), equalTo(KPICode.DEFECT_COUNT_BY_PRIORITY.name()));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFetchKPIDataFromDbData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList);
		String startDate = leafNodeList.get(0).getSprintFilter().getStartDate();
		String endDate = leafNodeList.get(leafNodeList.size() - 1).getSprintFilter().getEndDate();

		when(customApiConfig.getApplicationDetailedLogger()).thenReturn("on");

		when(jiraIssueRepository.findIssuesGroupBySprint(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(sprintWiseStoryList);

		when(jiraIssueRepository.findIssuesByType(Mockito.any())).thenReturn(totalBugList);

		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);

		Map<String, Object> defectDataListMap = dcServiceImpl.fetchKPIDataFromDb(leafNodeList, startDate, endDate,
				kpiRequest);
		assertThat("Total Defects value :", ((List<JiraIssue>) defectDataListMap.get(TOTAL_DEFECT_DATA)).size(),
				equalTo(19));
		assertThat("Total Story :", ((List<JiraIssue>) defectDataListMap.get(SPRINT_WISE_STORY_DATA)).size(),
				equalTo(5));
	}

	@Test
	public void testCalculateKPIMetrics() {
		assertThat("Total Defects value :", dcServiceImpl.calculateKPIMetrics(null), equalTo(0L));
	}

	private void setTreadValuesDataCount() {
		List<DataCount> dataCountList = new ArrayList<>();
		DataCount dataCountValue = new DataCount();
		dataCountValue.setData(String.valueOf(new HashMap()));
		dataCountValue.setValue(new HashMap());
		dataCountList.add(dataCountValue);
		DataCount dataCount = setDataCountValues("Scrum Project", "3", "4", dataCountList);
		trendValues.add(dataCount);
		trendValueMap.put("Overall", trendValues);
		trendValueMap.put("High", trendValues);
		trendValueMap.put("Low", trendValues);
		trendValueMap.put("Medium", trendValues);
		trendValueMap.put("Critical", trendValues);
	}

	private DataCount setDataCountValues(String data, String maturity, Object maturityValue, Object value) {
		DataCount dataCount = new DataCount();
		dataCount.setData(data);
		dataCount.setMaturity(maturity);
		dataCount.setMaturityValue(maturityValue);
		dataCount.setValue(value);
		return dataCount;
	}

}
