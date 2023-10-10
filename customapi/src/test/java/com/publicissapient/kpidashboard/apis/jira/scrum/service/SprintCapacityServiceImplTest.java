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
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
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
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.excel.CapacityKpiData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.excel.CapacityKpiDataRepository;
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
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	private KpiRequest kpiRequest;
	private KpiElement kpiElement;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	@Mock
	private CommonService commonService;

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
	}

	@Test
	public void testCalculateKPIMetrics() {
		Map<String, Object> filterComponentIdWiseDefectMap = new HashMap<>();
		String kpiRequestTrackerId = "automationpercenttrack001";

		filterComponentIdWiseDefectMap.put(SPRINTCAPACITYKEY, totalJiraIssueList);
		Double capacityValue = sprintCapacityServiceImpl.calculateKPIMetrics(filterComponentIdWiseDefectMap);

		assertThat("Capacity value :", capacityValue, equalTo(180.0));
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
		assertThat("Capacity value :", ((List<JiraIssue>) (capacityListMap.get(SPRINTCAPACITYKEY))).size(),
				equalTo(44));
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
		when(customApiConfig.getApplicationDetailedLogger()).thenReturn("On");
		when(kpiHelperService.fetchSprintCapacityDataFromDb(Mockito.any())).thenReturn(totalJiraIssueList);
		kpiWiseAggregation.put("sprintCapacity", "average");
		when(configHelperService.calculateMaturity()).thenReturn(maturityRangeMap);
		// when(customApiConfig.getSprintCountForFilters()).thenReturn(5);
		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(sprintCapacityServiceImpl.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		when(kpiHelperService.fetchCapacityDataFromDB(Mockito.any())).thenReturn(dataList);

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

}
