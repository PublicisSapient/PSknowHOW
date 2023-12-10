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

import com.publicissapient.kpidashboard.common.repository.application.TestExecutionRepository;
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
import com.publicissapient.kpidashboard.apis.data.TestCaseDetailsDataFactory;
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
import com.publicissapient.kpidashboard.common.model.zephyr.TestCaseDetails;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.zephyr.TestCaseDetailsRepository;

@RunWith(MockitoJUnitRunner.class)
public class AutomationPercentageServiceImplTest {

	private final static String TESTCASEKEY = "testCaseData";
	private final static String AUTOMATEDTESTCASEKEY = "automatedTestCaseData";
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	@Mock
	JiraIssueRepository featureRepository;
	@Mock
	CacheService cacheService;
	@Mock
	KpiHelperService kpiHelperService;
	@Mock
	FilterHelperService filterHelperService;
	@Mock
	ConfigHelperService configHelperService;
	@InjectMocks
	AutomationPercentageServiceImpl automationPercentageServiceImpl;
	@Mock
	ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	FieldMappingRepository fieldMappingRepository;
	@Mock
	TestCaseDetailsRepository testCaseDetailsRepository;
	List<TestCaseDetails> totalTestCaseList = new ArrayList<>();
	List<TestCaseDetails> automatedTestCaseList = new ArrayList<>();
	List<JiraIssue> issues = new ArrayList<>();
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	private CommonService commonService;
	@Mock
	private TestExecutionRepository testExecutionRepository;
	private List<FieldMapping> fieldMappingList = new ArrayList<>();
	private KpiRequest kpiRequest;
	private KpiElement kpiElement;
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi16");
		kpiRequest.setLabel("PROJECT");
		kpiElement = kpiRequest.getKpiList().get(0);
		kpiWiseAggregation.put("defectInjectionRate", "average");
		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
		totalTestCaseList = TestCaseDetailsDataFactory.newInstance().getTestCaseDetailsList();
		automatedTestCaseList = TestCaseDetailsDataFactory.newInstance().findAutomatedTestCases();
		issues = new ArrayList<>(JiraIssueDataFactory.newInstance().getStories());
		setMockFieldMapping();
		fieldMappingList.forEach(fieldMapping -> {
			fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		});
	}

	@Test
	public void getQualifierTypeTest() {
		String qualifierType = automationPercentageServiceImpl.getQualifierType();
		assertThat("Qualifier type :", qualifierType, equalTo(KPICode.INSPRINT_AUTOMATION_COVERAGE.name()));
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
		when(featureRepository.findIssuesGroupBySprint(any(), any(), any(), any()))
				.thenReturn(JiraIssueDataFactory.newInstance().getSprintWiseStories());
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		Map<String, Object> defectDataListMap = automationPercentageServiceImpl.fetchKPIDataFromDb(leafNodeList, null,
				null, kpiRequest);
		assertThat("Total Test Case value :", ((List<JiraIssue>) (defectDataListMap.get(TESTCASEKEY))).size(),
				equalTo(0));
	}

	@Test
	public void getKpiDataTest() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put("automationPercentage", Arrays.asList("-20", "20-40", "40-60", "60-79", "80-"));
		when(configHelperService.calculateMaturity()).thenReturn(maturityRangeMap);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(featureRepository.findIssueAndDescByNumber(any())).thenReturn(issues);
		when(cacheService.getFromApplicationCache(Mockito.anyString()))
				.thenReturn(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.ZEPHYR.name());
		testFetchKPIDataFromDbData();
		try {
			kpiElement = automationPercentageServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("Automated Percentage Value :",
					((ArrayList) ((List<DataCount>) kpiElement.getTrendValueList()).get(0).getValue()).size(),
					equalTo(5));
		} catch (ApplicationException enfe) {

		}
	}
	@Test
	public void getKpiDataTestUploadData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put("automationPercentage", Arrays.asList("-20", "20-40", "40-60", "60-79", "80-"));
		when(configHelperService.calculateMaturity()).thenReturn(maturityRangeMap);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(featureRepository.findIssueAndDescByNumber(any())).thenReturn(issues);
		when(cacheService.getFromApplicationCache(Mockito.anyString()))
				.thenReturn(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.ZEPHYR.name());
		testFetchKPIDataFromDbData();
		try {
			kpiElement = automationPercentageServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("Automated Percentage Value :",
					((ArrayList) ((List<DataCount>) kpiElement.getTrendValueList()).get(0).getValue()).size(),
					equalTo(5));
		} catch (ApplicationException enfe) {

		}
	}

	@Test
	public void testCalculateKPIMetrics() {
		Map<String, Object> filterComponentIdWiseDefectMap = new HashMap<>();
		filterComponentIdWiseDefectMap.put(AUTOMATEDTESTCASEKEY, automatedTestCaseList);
		filterComponentIdWiseDefectMap.put(TESTCASEKEY, totalTestCaseList);
		Double automatedValue = automationPercentageServiceImpl.calculateKPIMetrics(filterComponentIdWiseDefectMap);
		assertThat("Automated Percentage value :", automatedValue, equalTo(4.0));
	}

	@Test
	public void calculateKpiValueTest() {
		Double kpiValue = automationPercentageServiceImpl.calculateKpiValue(Arrays.asList(1.0, 2.0), "kpi14");
		assertThat("Kpi value  :", kpiValue, equalTo(0.0));
	}

	private void setMockFieldMapping() {

		FieldMapping projectOne = new FieldMapping();
		projectOne.setBasicProjectConfigId(new ObjectId("63284960fdd20276d60e4df5"));
		projectOne.setJiraDefectInjectionIssueTypeKPI14(Arrays.asList("Story", "Tech Story"));
		projectOne.setJiradefecttype(Arrays.asList("Bug"));
		projectOne.setJiraDodKPI171(Arrays.asList("Done"));
		projectOne.setJiraDefectCreatedStatusKPI14("Open");
		projectOne.setUploadDataKPI16(false);
		projectOne.setJiraTestAutomationIssueType(Arrays.asList("Yes", "No"));

		FieldMapping projectTwo = new FieldMapping();
		projectTwo.setBasicProjectConfigId(new ObjectId("6335363749794a18e8a4479b"));
		projectTwo.setJiraDefectInjectionIssueTypeKPI14(Arrays.asList("Story", "Tech Story"));
		projectOne.setJiradefecttype(Arrays.asList("Bug"));
		projectTwo.setJiraDodKPI171(Arrays.asList("Done"));
		projectTwo.setJiraDefectCreatedStatusKPI14("Open");
		projectTwo.setUploadDataKPI16(false);
		projectTwo.setJiraTestAutomationIssueType(Arrays.asList("Yes", "No"));

		FieldMapping projectThree = new FieldMapping();
		projectThree.setBasicProjectConfigId(new ObjectId("63284960fdd20276d60e4df5"));
		projectThree.setJiraDefectInjectionIssueTypeKPI14(Arrays.asList("Story", "Tech Story"));
		projectOne.setJiradefecttype(Arrays.asList("Bug"));
		projectOne.setUploadDataKPI16(true);
		projectThree.setJiraDodKPI171(Arrays.asList("Done"));
		projectThree.setJiraDefectCreatedStatusKPI14("Open");
		projectThree.setJiraTestAutomationIssueType(Arrays.asList("Yes", "No"));

		fieldMappingList.add(projectOne);
		fieldMappingList.add(projectTwo);
		fieldMappingList.add(projectThree);

	}

}
