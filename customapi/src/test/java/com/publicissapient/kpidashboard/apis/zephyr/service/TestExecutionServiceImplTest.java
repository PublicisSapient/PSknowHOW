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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.publicissapient.kpidashboard.apis.data.AdditionalFilterCategoryFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.data.TestExecutionDataFactory;
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
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.testexecution.TestExecution;
import com.publicissapient.kpidashboard.common.repository.application.TestExecutionRepository;

@RunWith(MockitoJUnitRunner.class)
public class TestExecutionServiceImplTest {

	private static final String TEST_EXECUTION_DETAIL = "testExecutionDetail";
	private final static String TESTCASEKEY = "testCaseData";
	private final static String AUTOMATEDTESTCASEKEY = "automatedTestCaseData";
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	KpiHelperService kpiHelperService;
	@Mock
	CacheService cacheService;
	@Mock
	TestExecutionRepository testExecutionRepository;
	private List<TestExecution> testExecutionList = new ArrayList<>();
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	private KpiRequest kpiRequest;
	private KpiElement kpiElement;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private List<AdditionalFilterCategory> additionalFilterCategoryList;
	@InjectMocks
	private TestExecutionServiceImpl testExecutionServiceImpl;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	private CommonService commonService;

	@Mock
	private FilterHelperService filterHelperService;

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
		testExecutionList = TestExecutionDataFactory.newInstance().getTestExecutionList();
		additionalFilterCategoryList = AdditionalFilterCategoryFactory.newInstance().getAdditionalFilterCategoryList();
	}

	@Test
	public void testCalculateKPIMetrics() {
		Map<String, Object> filterComponentIdWiseDefectMap = new HashMap<>();
		filterComponentIdWiseDefectMap.put(AUTOMATEDTESTCASEKEY, null);
		filterComponentIdWiseDefectMap.put(TESTCASEKEY, null);
		Double automatedValue = testExecutionServiceImpl.calculateKPIMetrics(filterComponentIdWiseDefectMap);
		assertThat("Automated Percentage value :", automatedValue, equalTo(null));
	}

	@Test
	public void testGetQualifierType() {
		String qualifierType = testExecutionServiceImpl.getQualifierType();
		assertThat("Qualifier type :", qualifierType, equalTo(KPICode.TEST_EXECUTION_AND_PASS_PERCENTAGE.name()));
	}

	@Test
	public void fetchKPIDataFromDb() throws ApplicationException {
		List<Node> leafNodeList = new ArrayList<>();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.SPRINT) {
				leafNodeList.addAll(v);
			}
		});
		when(testExecutionRepository.findTestExecutionDetailByFilters(Mockito.anyMap(), Mockito.anyMap()))
				.thenReturn(testExecutionList);
		Map<String, AdditionalFilterCategory> addFilterCategory = additionalFilterCategoryList.stream()
				.collect(Collectors.toMap(entry -> entry.getFilterCategoryId(), entry -> entry));
		when(filterHelperService.getAdditionalFilterHierarchyLevel()).thenReturn(addFilterCategory);
		Map<String, Object> defectDataListMap = testExecutionServiceImpl.fetchKPIDataFromDb(leafNodeList, null, null,
				kpiRequest);
		Map<String, Object> outputMap = new HashMap<>();
		outputMap.put(TEST_EXECUTION_DETAIL, testExecutionList);
		assertThat("fetch KPI data from DB :", defectDataListMap, equalTo(outputMap));

	}

	@Test
	public void testGetKpiData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		String kpiRequestTrackerId = "Zephyr-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.ZEPHYR.name()))
				.thenReturn(kpiRequestTrackerId);

		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put("testExecutionPercentage", Arrays.asList("-20", "20-40", "40-60", "60-79", "80-"));

		when(configHelperService.calculateMaturity()).thenReturn(maturityRangeMap);
		Map<String, AdditionalFilterCategory> addFilterCategory = additionalFilterCategoryList.stream()
				.collect(Collectors.toMap(entry -> entry.getFilterCategoryId(), entry -> entry));
		when(filterHelperService.getAdditionalFilterHierarchyLevel()).thenReturn(addFilterCategory);
		kpiWiseAggregation.put("testExecutionPercentage", "average");

		when(testExecutionRepository.findTestExecutionDetailByFilters(Mockito.any(), Mockito.any()))
				.thenReturn(testExecutionList);
		fetchKPIDataFromDb();
		try {
			KpiElement kpiElement = testExecutionServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("Test Exceution Value :",
					((List<DataCount>) ((List<DataCount>) kpiElement.getTrendValueList()).get(0).getValue()).size(),
					equalTo(5));
		} catch (ApplicationException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void calculateKpiValueTest() {
		Double kpiValue = testExecutionServiceImpl.calculateKpiValue(Arrays.asList(1.0, 2.0), "kpi70");
		assertThat("Kpi value  :", kpiValue, equalTo(0.0));
	}

}
