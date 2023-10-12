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

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.excel.CapacityKpiData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.excel.CapacityKpiDataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

/**
 * test case for Sprint Capcacity Utilizatin
 * author @shi6
 */
@RunWith(MockitoJUnitRunner.class)
public class SprintCapacityServiceImplTest {
	private final static String SPRINTCAPACITYKEY = "sprintCapacityKey";
	private Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	private Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	private List<JiraIssue> totalJiraIssueList = new ArrayList<>();
	private List<CapacityKpiData> dataList = new ArrayList<>();
	@Mock
	private JiraIssueRepository jiraIssueRepository;
	@Mock
	private CacheService cacheService;
	@Mock
	private ConfigHelperService configHelperService;
	@Mock
	private KpiHelperService kpiHelperService;
	@InjectMocks
	private SprintCapacityServiceImpl sprintCapacityServiceImpl;
	@Mock
	private ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	private FieldMappingRepository fieldMappingRepository;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	private CapacityKpiDataRepository capacityKpiDataRepository;
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	private KpiRequest kpiRequest;
	private FieldMappingDataFactory fieldMappingDataFactory;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	@Mock
	private CommonService commonService;

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi70");
		kpiRequest.setLabel("PROJECT");
		kpiWiseAggregation.put("testExecutionPercentage", "average");
		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
		configHelperService.setProjectConfigMap(projectConfigMap);
		totalJiraIssueList = JiraIssueDataFactory.newInstance().getJiraIssues();
		fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		CapacityKpiData capacityKpiData = new CapacityKpiData();
		capacityKpiData.setBasicProjectConfigId(new ObjectId("6335363749794a18e8a4479b"));
		capacityKpiData.setCapacityPerSprint(12.0);
		capacityKpiData.setSprintID("438294_Scrum Project_6335363749794a18e8a4479b");
		dataList.add(capacityKpiData);
		when(commonService.sortTrendValueMap(anyMap())).thenReturn(createTrendMap());

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
		when(customApiConfig.getApplicationDetailedLogger()).thenReturn("Off");
		when(kpiHelperService.fetchSprintCapacityDataFromDb(Mockito.any())).thenReturn(totalJiraIssueList);
		kpiWiseAggregation.put("sprintCapacity", "average");

		when(kpiHelperService.fetchCapacityDataFromDB(Mockito.any())).thenReturn(dataList);
		Map<String, Object> capacityListMap = sprintCapacityServiceImpl.fetchKPIDataFromDb(leafNodeList, null, null,
				kpiRequest);
		Assert.assertEquals("Capacity value :", 42,
				((List<JiraIssue>) (capacityListMap.get(SPRINTCAPACITYKEY))).size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetSprintCapacity() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put("sprintCapacity", Arrays.asList("-5", "5-25", "25-50", "50-75", "75-"));
		when(customApiConfig.getApplicationDetailedLogger()).thenReturn("On");
		when(kpiHelperService.fetchSprintCapacityDataFromDb(Mockito.any())).thenReturn(totalJiraIssueList);
		kpiWiseAggregation.put("sprintCapacity", "average");
		when(configHelperService.calculateMaturity()).thenReturn(maturityRangeMap);
		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(sprintCapacityServiceImpl.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		when(kpiHelperService.fetchCapacityDataFromDB(Mockito.any())).thenReturn(dataList);

		KpiElement kpiElement = sprintCapacityServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
				treeAggregatorDetail);
		Assert.assertEquals("Capacity estimateTimeCount :", 1,
				((List<DataCountGroup>) kpiElement.getTrendValueList()).get(0).getValue().size());

	}

	@Test
	public void testGetSprintCapacitySpilledConfiguration() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put("sprintCapacity", Arrays.asList("-5", "5-25", "25-50", "50-75", "75-"));
		when(customApiConfig.getApplicationDetailedLogger()).thenReturn("On");
		when(kpiHelperService.fetchSprintCapacityDataFromDb(Mockito.any())).thenReturn(totalJiraIssueList);
		kpiWiseAggregation.put("sprintCapacity", "average");
		when(configHelperService.calculateMaturity()).thenReturn(maturityRangeMap);
		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(sprintCapacityServiceImpl.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		when(kpiHelperService.fetchCapacityDataFromDB(Mockito.any())).thenReturn(dataList);
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMapping.setExcludeSpilledKpi46("On");
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);

		KpiElement kpiElement = sprintCapacityServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
				treeAggregatorDetail);
		Assert.assertEquals("Capacity estimateTimeCount :", 1,
				((List<DataCountGroup>) kpiElement.getTrendValueList()).get(0).getValue().size());
	}

	@Test
	public void testGetSprintCapacitySpilledConfigurationWithOriginalEstimate() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put("sprintCapacity", Arrays.asList("-5", "5-25", "25-50", "50-75", "75-"));
		when(customApiConfig.getApplicationDetailedLogger()).thenReturn("On");
		when(kpiHelperService.fetchSprintCapacityDataFromDb(Mockito.any())).thenReturn(totalJiraIssueList);
		kpiWiseAggregation.put("sprintCapacity", "average");
		when(configHelperService.calculateMaturity()).thenReturn(maturityRangeMap);
		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(sprintCapacityServiceImpl.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		when(kpiHelperService.fetchCapacityDataFromDB(Mockito.any())).thenReturn(dataList);
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMapping.setExcludeSpilledKpi46("On");
		fieldMapping.setEstimationCriteria("Original Estimate");
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);

		KpiElement kpiElement = sprintCapacityServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
				treeAggregatorDetail);
		int size = ((List<DataCountGroup>) kpiElement.getTrendValueList()).get(0).getValue().size();
		Assert.assertEquals(1, size);

	}

	@Test
	public void testGetQualifierType() {
		Assert.assertEquals("SPRINT_CAPACITY_UTILIZATION", sprintCapacityServiceImpl.getQualifierType());
	}

	@Test
	public void calculateKpiValueTest() {
		Double kpiValue = sprintCapacityServiceImpl.calculateKpiValue(Arrays.asList(1.0, 2.0), "kpi14");
		Assert.assertEquals(0.0, kpiValue, 0.0);
	}

	@Test
	public void testCalculateKPIMetrics() {
		Map<String, Object> filterComponentIdWiseDefectMap = new HashMap<>();
		filterComponentIdWiseDefectMap.put(SPRINTCAPACITYKEY, totalJiraIssueList);
		Assert.assertNull(sprintCapacityServiceImpl.calculateKPIMetrics(filterComponentIdWiseDefectMap));

	}

	private Map<String, List<DataCount>> createTrendMap() {
		Map<String, List<DataCount>> map = new HashMap<>();
		DataCount d1 = new DataCount();
		List<DataCount> d1OuterList = new ArrayList<>();
		List<DataCount> d1InnerList = new ArrayList<>();
		DataCount d1Inner = new DataCount();
		d1Inner.setData("0.0");
		d1Inner.setSProjectName("Scrum Project");
		d1Inner.setSSprintID("sprint1");
		d1Inner.setValue(0.0d);
		d1InnerList.add(d1Inner);
		d1.setValue(d1InnerList);
		d1.setData("Scrum Project");
		d1OuterList.add(d1);
		map.put("Planned View", d1OuterList);
		map.put("Execution View", d1OuterList);
		return map;
	}

}
