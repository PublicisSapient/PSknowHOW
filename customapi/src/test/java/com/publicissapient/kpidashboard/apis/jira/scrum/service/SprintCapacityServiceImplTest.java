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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import org.bson.types.ObjectId;
import org.junit.Assert;
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
import com.publicissapient.kpidashboard.apis.data.JiraIssueHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.data.SprintDetailsDataFactory;
import com.publicissapient.kpidashboard.apis.enums.Filters;
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
import com.publicissapient.kpidashboard.common.model.excel.CapacityKpiData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.excel.CapacityKpiDataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;


@RunWith(MockitoJUnitRunner.class)
public class SprintCapacityServiceImplTest {
	private final static String SPRINTCAPACITYKEY = "sprintCapacityKey";
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	List<JiraIssue> totalJiraIssueList = new ArrayList<>();
	List<CapacityKpiData> dataList = new ArrayList<>();
	@Mock
	JiraIssueRepository jiraIssueRepository;
	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	FilterHelperService filterHelperService;
	@Mock
	KpiHelperService kpiHelperService;
	@InjectMocks
	SprintCapacityServiceImpl sprintCapacityServiceImpl;
	@Mock
	ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	FieldMappingRepository fieldMappingRepository;
	@Mock
	CustomApiConfig customApiConfig;
	@Mock
	CapacityKpiDataRepository capacityKpiDataRepository;
	@Mock
	FieldMapping fieldMapping;
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	private Map<String, Object> resultMap = new HashMap<>();
	private KpiRequest kpiRequest;
	private KpiElement kpiElement;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	@Mock
	private CommonService commonService;
	@Mock
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	private List<SprintDetails> sprintDetailsList;
	private List<JiraIssueCustomHistory> jiraIssueCustomHistories;

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi70");
		kpiRequest.setLabel("PROJECT");
		kpiElement = kpiRequest.getKpiList().get(0);
		kpiWiseAggregation.put("testExecutionPercentage", "average");
		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
		configHelperService.setProjectConfigMap(projectConfigMap);
		configHelperService.setFieldMappingMap(fieldMappingMap);
		totalJiraIssueList = JiraIssueDataFactory.newInstance().getJiraIssues();
		SprintDetailsDataFactory sprintDetailsDataFactory = SprintDetailsDataFactory.newInstance();
		sprintDetailsList = sprintDetailsDataFactory.getSprintDetails();
		JiraIssueHistoryDataFactory jiraIssueCustomHistoryDataFactory = JiraIssueHistoryDataFactory.newInstance();
		jiraIssueCustomHistories = jiraIssueCustomHistoryDataFactory.getJiraIssueCustomHistory();
		JiraHistoryChangeLog worklog=new JiraHistoryChangeLog("","28800", LocalDateTime.of(2022, 8, 10, 12, 0,0,0));
		jiraIssueCustomHistories.stream().filter(j->j.getStoryID().equalsIgnoreCase("TEST-17918")).toList().get(0).setWorkLog(Collections.singletonList(worklog));
		CapacityKpiData capacityKpiData=new CapacityKpiData();
		capacityKpiData.setCapacityPerSprint(22d);
		capacityKpiData.setBasicProjectConfigId(new ObjectId("6658551f9851452c969edaaa"));
		capacityKpiData.setSprintID("abc");
		CapacityKpiData capacityKpiData1=new CapacityKpiData();
		capacityKpiData1.setCapacityPerSprint(23d);
		capacityKpiData1.setBasicProjectConfigId(new ObjectId("6658551f9851452c969edaaa"));
		capacityKpiData1.setSprintID("abc");
		dataList.add(capacityKpiData1);
		dataList.add(capacityKpiData);
		resultMap.put("stories",totalJiraIssueList);
		resultMap.put("sprints",sprintDetailsList);
		resultMap.put("JiraIssueHistoryData",jiraIssueCustomHistories);

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		configHelperService.setFieldMappingMap(fieldMappingMap);
	}

	@Test
	public void testCalculateKPIMetrics() {
		Double capacityValue = sprintCapacityServiceImpl.calculateKPIMetrics(new HashMap<>());
		assertThat(capacityValue, equalTo(null));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFetchKPIDataFromDbData() throws ApplicationException {
		List<Node> leafNodeList = new ArrayList<>();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.SPRINT) {
				leafNodeList.addAll(v);
			}
		});
		when(kpiHelperService.fetchSprintCapacityDataFromDb(Mockito.any(), Mockito.any())).thenReturn(resultMap);
		kpiWiseAggregation.put("sprintCapacity", "average");

		when(kpiHelperService.fetchCapacityDataFromDB(Mockito.any(), Mockito.any())).thenReturn(dataList);
		Map<String, Object> capacityListMap = sprintCapacityServiceImpl.fetchKPIDataFromDb(leafNodeList, null, null,
				kpiRequest);
		Assert.assertNull(capacityListMap.get(SPRINTCAPACITYKEY));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetSprintCapacity() throws ApplicationException {
		List<Node> leafNodeList = new ArrayList<>();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.SPRINT) {
				leafNodeList.addAll(v);
			}
		});
		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put("sprintCapacity", Arrays.asList("-5", "5-25", "25-50", "50-75", "75-"));
		when(kpiHelperService.fetchSprintCapacityDataFromDb(Mockito.any(), Mockito.any())).thenReturn(resultMap);
		kpiWiseAggregation.put("sprintCapacity", "average");
		when(configHelperService.calculateMaturity()).thenReturn(maturityRangeMap);
		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(sprintCapacityServiceImpl.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		when(kpiHelperService.fetchCapacityDataFromDB(Mockito.any(), Mockito.any())).thenReturn(dataList);
		when(customApiConfig.getpriorityP1()).thenReturn(Constant.P1);
		when(customApiConfig.getpriorityP2()).thenReturn(Constant.P2);
		when(customApiConfig.getpriorityP3()).thenReturn(Constant.P3);
		when(customApiConfig.getpriorityP4()).thenReturn("p4-minor");
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		FieldMapping fieldMapping = mock(FieldMapping.class);
		try {
			KpiElement kpiElement = sprintCapacityServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("Capacity estimateTimeCount :",
					((ArrayList) ((List<DataCount>) kpiElement.getTrendValueList()).get(0).getValue()).size(),
					equalTo(5));
		} catch (ApplicationException enfe) {

		}

	}

	@Test
	public void testGetQualifierType() {
		assertThat(sprintCapacityServiceImpl.getQualifierType(), equalTo("SPRINT_CAPACITY_UTILIZATION"));
	}

	@Test
	public void calculateKpiValueTest() {
		Double kpiValue = sprintCapacityServiceImpl.calculateKpiValue(Arrays.asList(1.0, 2.0), "kpi14");
		assertThat("Kpi value  :", kpiValue, equalTo(0.0));
	}

	@Test
	public void calculateThresholdValue(){
		fieldMapping.setThresholdValueKPI46("abc");
		sprintCapacityServiceImpl.calculateThresholdValue(fieldMapping);
	}

}
