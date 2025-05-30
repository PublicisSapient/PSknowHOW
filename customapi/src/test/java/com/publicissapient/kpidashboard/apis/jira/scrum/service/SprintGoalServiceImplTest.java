/*
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.CommonService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiDataProvider;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.data.SprintDetailsDataFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
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
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

@RunWith(MockitoJUnitRunner.class)
public class SprintGoalServiceImplTest {

	@Mock
	SprintRepository sprintRepository;
	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@InjectMocks
	SprintGoalServiceImpl sprintGoalService;
	@Mock
	CustomApiConfig customApiSetting;
	@Mock
	private FilterHelperService filterHelperService;
	@Mock
	private KpiDataProvider kpiDataProvider;
	@Mock
	private CommonService commonService;

	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	private KpiRequest kpiRequest;
	private List<SprintDetails> sprintDetailsList = new ArrayList<>();
	private Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.SPRINT_GOALS.getKpiId());
		kpiRequest.setLabel("PROJECT");
		kpiRequest.setLevel(5);

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		SprintDetailsDataFactory sprintDetailsDataFactory = SprintDetailsDataFactory.newInstance();
		sprintDetailsList = sprintDetailsDataFactory.getSprintDetails();

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);

		ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
		projectBasicConfig.setId(new ObjectId("6335363749794a18e8a4479b"));
		projectBasicConfig.setIsKanban(true);
		projectBasicConfig.setProjectName("Scrum Project");
		projectBasicConfig.setProjectNodeId("Scrum Project_6335363749794a18e8a4479b");
		projectConfigList.add(projectBasicConfig);

		projectConfigList.forEach(projectConfigs -> {
			projectConfigMap.put(projectConfigs.getProjectName(), projectConfigs);
		});

	}

	@Test
	public void testFetchKPIDataFromDbData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList, false);

		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put("sprintDetails", sprintDetailsList);

		Map<String, Object> defectDataListMap = sprintGoalService.fetchKPIDataFromDb(leafNodeList, null, null,
				kpiRequest);
		assertNotNull(defectDataListMap);
	}

	@Test
	public void testFetchKPIDataFromDbEmptyData_BadScenario() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList, false);

		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put("sprintDetails", new ArrayList<>());
		Map<String, Object> defectDataListMap = sprintGoalService.fetchKPIDataFromDb(leafNodeList, null, null,
				kpiRequest);
		assertNotNull(defectDataListMap);
	}

	@Test
	public void testGetData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put("sprintDetails", sprintDetailsList);
		when(sprintRepository.findBySprintIDInWithFieldsSorted(any())).thenReturn(sprintDetailsList);
		when(configHelperService.getProjectConfig(any())).thenReturn(projectConfigList.get(0));

		try {
			KpiElement kpiElement = sprintGoalService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("Sprint goal value :", ((List<DataCount>) kpiElement.getTrendValueList()).size(), equalTo(1));
		} catch (Exception exception) {
		}
	}

	@Test
	public void testGetData_BadScenario() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		Map<String, Object> resultListMap = new HashMap<>();
		resultListMap.put("sprintDetails", new ArrayList<>());
		when(sprintRepository.findBySprintIDInWithFieldsSorted(any())).thenReturn(sprintDetailsList);
		when(configHelperService.getProjectConfig(any())).thenReturn(projectConfigList.get(0));

		try {
			KpiElement kpiElement = sprintGoalService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("Sprint goal value :", ((List<DataCount>) kpiElement.getTrendValueList()).size(), equalTo(1));
		} catch (Exception exception) {
		}
	}

	@Test
	public void testQualifierType() {
		String kpiName = KPICode.SPRINT_GOALS.name();
		String type = sprintGoalService.getQualifierType();
		assertThat("KPI NAME: ", type, equalTo(kpiName));
	}

}