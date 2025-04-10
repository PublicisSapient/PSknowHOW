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

package com.publicissapient.kpidashboard.apis.zephyr.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
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
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyKanbanFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.KanbanTestExecutionDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyDataKanban;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.testexecution.KanbanTestExecution;
import com.publicissapient.kpidashboard.common.repository.application.KanbanTestExecutionRepository;

/**
 * @author anisingh4
 */
@RunWith(MockitoJUnitRunner.class)
public class TestExecutionKanbanServiceImplTest {

	private static final String TEST_EXECUTION_DETAIL = "testExecutionDetail";
	private static final String TESTCASEKEY = "testCaseData";
	private static final String AUTOMATEDTESTCASEKEY = "automatedTestCaseData";
	@InjectMocks
	TestExecutionKanbanServiceImpl testExecutionKanbanService;
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	KpiHelperService kpiHelperService;
	@Mock
	CacheService cacheService;
	@Mock
	KanbanTestExecutionRepository kanbanTestExecutionRepository;
	@Mock
	FilterHelperService flterHelperService;
	private List<KanbanTestExecution> testExecutionList = new ArrayList<>();
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	private KpiRequest kpiRequest;
	private List<AccountHierarchyDataKanban> accountHierarchyDataList = new ArrayList<>();
	@Mock
	private CommonService commonService;

	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();

	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi71");
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

		kpiWiseAggregation.put("testExecutionPercentage", "average");
		AccountHierarchyKanbanFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyKanbanFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyKanbanDataList();
		testExecutionList = KanbanTestExecutionDataFactory.newInstance().getKanbanTestExecutionList();
	}

	@Test
	public void getQualifierType() {
		Assert.assertEquals(KPICode.TEST_EXECUTION_KANBAN.name(), testExecutionKanbanService.getQualifierType());
	}

	@Test
	public void getKpiData() throws ApplicationException {
		List<Node> leafNodeList = new ArrayList<>();

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				new ArrayList<>(), accountHierarchyDataList, "hierarchyLevelOne", 5);
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.SPRINT) {
				leafNodeList.addAll(v);
			}
		});
		Map<String, AdditionalFilterCategory> additionalFilterCategoryList = new HashMap<>();
		AdditionalFilterCategory additionalFilterCategory = new AdditionalFilterCategory();
		additionalFilterCategory.setLevel(1);
		additionalFilterCategory.setFilterCategoryId("12");
		additionalFilterCategory.setFilterCategoryName("Test");
		additionalFilterCategoryList.put("AdditionalFilterCategory", additionalFilterCategory);

		kpiWiseAggregation.put("testExecutionKanbanPercentage", "average");
		String kpiRequestTrackerId = "EXCEL-Zephyr-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.ZEPHYRKANBAN.name()))
				.thenReturn(kpiRequestTrackerId);

		when(kanbanTestExecutionRepository.findTestExecutionDetailByFilters(any(), any(), any(), any()))
				.thenReturn(testExecutionList);

		try {
			fetchKPIDataFromDb();
			KpiElement kpiElement = testExecutionKanbanService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("Test Exceution Value :", ((List<DataCount>) kpiElement.getTrendValueList()).size(), equalTo(0));
		} catch (ApplicationException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void calculateKPIMetrics() {
		Map<String, Object> filterComponentIdWiseDefectMap = new HashMap<>();
		filterComponentIdWiseDefectMap.put(AUTOMATEDTESTCASEKEY, null);
		filterComponentIdWiseDefectMap.put(TESTCASEKEY, null);
		Double automatedValue = testExecutionKanbanService.calculateKPIMetrics(filterComponentIdWiseDefectMap);
		assertThat("Automated Percentage value :", automatedValue, equalTo(null));
	}

	@Test
	public void fetchKPIDataFromDb() throws ApplicationException {
		List<Node> leafNodeList = new ArrayList<>();

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				new ArrayList<>(), accountHierarchyDataList, "hierarchyLevelOne", 5);
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.SPRINT) {
				leafNodeList.addAll(v);
			}
		});
		when(kanbanTestExecutionRepository.findTestExecutionDetailByFilters(any(), any(), any(), any()))
				.thenReturn(testExecutionList);
		Map<String, Object> resultMap = testExecutionKanbanService.fetchKPIDataFromDb(leafNodeList, null, null, kpiRequest);
		assertThat("Total Result Count :", ((List<KanbanTestExecution>) (resultMap.get(TEST_EXECUTION_DETAIL))).size(),
				equalTo(3));
	}

	@Test
	public void calculateKpiValueTest() {
		Double kpiValue = testExecutionKanbanService.calculateKpiValue(Arrays.asList(1.0, 2.0), "kpi70");
		assertThat("Kpi value  :", kpiValue, equalTo(0.0));
	}
}
