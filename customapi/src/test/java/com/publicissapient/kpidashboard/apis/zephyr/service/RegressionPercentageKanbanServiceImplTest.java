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
import static org.mockito.ArgumentMatchers.any;
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
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.CommonService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.KanbanJiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.data.TestCaseDetailsDataFactory;
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
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.model.zephyr.TestCaseDetails;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.zephyr.TestCaseDetailsRepository;

@RunWith(MockitoJUnitRunner.class)
public class RegressionPercentageKanbanServiceImplTest {

	private final static String TESTCASEKEY = "testCaseData";
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	List<KanbanJiraIssue> totalTestCaseList = new ArrayList<>();
	List<TestCaseDetails> testCaseDetailsList = new ArrayList<>();
	@Mock
	KanbanJiraIssueRepository kanbanFeatureRepository;
	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	KpiHelperService kpiHelperService;
	@InjectMocks
	RegressionPercentageKanbanServiceImpl regressionPercentageKanbanServiceImpl;
	@Mock
	TestCaseDetailsRepository testCaseDetailsRepository;
	private List<FieldMapping> fieldMappingList = new ArrayList<>();
	private KpiRequest kpiRequest;
	private KpiElement kpiElement;
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	@Mock
	private CommonService commonService;

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
		totalTestCaseList = KanbanJiraIssueDataFactory.newInstance().getKanbanJiraIssueDataList();
		testCaseDetailsList = TestCaseDetailsDataFactory.newInstance().getTestCaseDetailsList();
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi63");
		kpiRequest.setLabel("PROJECT");
		kpiElement = kpiRequest.getKpiList().get(0);
		kpiWiseAggregation.put("defectInjectionRate", "average");
	}

	@Test
	public void testCalculateKPIMetrics() {
		Map<String, Object> filterComponentIdWiseDefectMap = new HashMap<>();
		String kpiRequestTrackerId = "automationpercenttrack001";
		filterComponentIdWiseDefectMap.put(TESTCASEKEY, totalTestCaseList);
		Double automatedValue = regressionPercentageKanbanServiceImpl
				.calculateKPIMetrics(filterComponentIdWiseDefectMap);
		assertThat("Automated Percentage value :", automatedValue, equalTo(null));
	}

	@Test
	public void testGetAutomatedTestPercentage() throws ApplicationException {
		List<Node> leafNodeList = new ArrayList<>();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.SPRINT) {
				leafNodeList.addAll(v);
			}
		});
		when(testCaseDetailsRepository.findTestDetails(any(), any(), any())).thenReturn(testCaseDetailsList);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		String kpiRequestTrackerId = "Excel-Zephyr-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.ZEPHYRKANBAN.name()))
				.thenReturn(kpiRequestTrackerId);
		try {
			KpiElement kpiElement = regressionPercentageKanbanServiceImpl.getKpiData(kpiRequest,
					kpiRequest.getKpiList().get(0), treeAggregatorDetail);
			assertThat("Regression Percentage Value :", ((List<DataCount>) kpiElement.getTrendValueList()).size(),
					equalTo(1));
		} catch (ApplicationException enfe) {

		}

	}

	@Test
	public void testGetQualifierType() {
		assertThat("Kpi Name :", regressionPercentageKanbanServiceImpl.getQualifierType(),
				equalTo("KANBAN_REGRESSION_PASS_PERCENTAGE"));
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
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(testCaseDetailsRepository.findTestDetails(any(), any(), any())).thenReturn(testCaseDetailsList);
		Map<String, Object> defectDataListMap = regressionPercentageKanbanServiceImpl.fetchKPIDataFromDb(leafNodeList,
				null, null, kpiRequest);
		assertThat("Total Test Case value :", (Arrays.asList(defectDataListMap.get(TESTCASEKEY)).size()), equalTo(1));
	}

	@Test
	public void calculateKpiValue() {
		Double kpiValue = regressionPercentageKanbanServiceImpl.calculateKpiValue(Arrays.asList(1.0, 2.0), "kpi14");
		assertThat("Kpi value  :", kpiValue, equalTo(0.0));
	}
}
