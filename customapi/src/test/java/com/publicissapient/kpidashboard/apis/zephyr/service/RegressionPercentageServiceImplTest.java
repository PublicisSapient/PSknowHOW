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
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.publicissapient.kpidashboard.apis.data.AdditionalFilterCategoryFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.data.TestCaseDetailsDataFactory;
import com.publicissapient.kpidashboard.apis.data.TestExecutionDataFactory;
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
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;
import com.publicissapient.kpidashboard.common.model.testexecution.TestExecution;
import com.publicissapient.kpidashboard.common.model.zephyr.TestCaseDetails;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.TestExecutionRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.zephyr.TestCaseDetailsRepository;

@RunWith(MockitoJUnitRunner.class)
public class RegressionPercentageServiceImplTest {

	private static final String TESTCASEKEY = "testCaseData";
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	@Mock
	JiraIssueRepository jiraIssueRepository;
	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@InjectMocks
	RegressionPercentageServiceImpl regressionPercentageServiceImpl;
	@Mock
	ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	FieldMappingRepository fieldMappingRepository;
	@Mock
	TestCaseDetailsRepository testCaseDetailsRepository;
	@Mock
	private CustomApiConfig customApiConfig;
	List<JiraIssue> totalTestCaseList = new ArrayList<>();
	List<SprintWiseStory> sprintWiseStoryList = new ArrayList<>();
	List<TestCaseDetails> testCaseDetailsList = new ArrayList<>();
	List<TestCaseDetails> automatedTestCaseList = new ArrayList<>();
	@Mock
	private KpiHelperService kpiHelperService;
	@Mock
	FilterHelperService filterHelperService;
	@Mock
	private CommonService commonService;
	@Mock
	private TestExecutionRepository testExecutionRepository;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private KpiRequest kpiRequest;
	private KpiElement kpiElement;
	private Map<String, String> kpiWiseAggregation = new HashMap<>();

	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi42");
		kpiRequest.setLabel("PROJECT");
		kpiElement = kpiRequest.getKpiList().get(0);
		kpiWiseAggregation.put("defectInjectionRate", "average");

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

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMappingDataFactory.getFieldMappings().get(0));
		configHelperService.setFieldMappingMap(fieldMappingMap);
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
		totalTestCaseList = JiraIssueDataFactory.newInstance().getJiraIssues();
		automatedTestCaseList = TestCaseDetailsDataFactory.newInstance().findAutomatedTestCases();
		testCaseDetailsList = TestCaseDetailsDataFactory.newInstance().getTestCaseDetailsList();
		List<TestExecution> testExecutionList = TestExecutionDataFactory.newInstance().getTestExecutionList();
		testExecutionList.forEach(test -> {
			test.setAutomatedRegressionTestCases(1);
			test.setTotalRegressionTestCases(1);
		});
		when(testExecutionRepository.findTestExecutionDetailByFilters(anyMap(), anyMap())).thenReturn(testExecutionList);

		AdditionalFilterCategoryFactory additionalFilterCategoryFactory = AdditionalFilterCategoryFactory.newInstance();
		List<AdditionalFilterCategory> additionalFilterCategoryList = additionalFilterCategoryFactory
				.getAdditionalFilterCategoryList();
		Map<String, AdditionalFilterCategory> additonalFilterMap = additionalFilterCategoryList.stream()
				.collect(Collectors.toMap(AdditionalFilterCategory::getFilterCategoryId, x -> x));
		when(cacheService.getAdditionalFilterHierarchyLevel()).thenReturn(additonalFilterMap);
		when(filterHelperService.getAdditionalFilterHierarchyLevel()).thenReturn(additonalFilterMap);

		Map<ObjectId, Map<String, List<ProjectToolConfig>>> toolMap = new HashMap<>();
		Map<String, List<ProjectToolConfig>> projectTool = new HashMap<>();
		ProjectToolConfig zephyConfig = new ProjectToolConfig();
		zephyConfig.setRegressionAutomationLabels(Arrays.asList("test1"));
		zephyConfig.setTestRegressionValue(Arrays.asList("test1"));
		zephyConfig.setRegressionAutomationFolderPath(Arrays.asList("test1"));
		projectTool.put(ProcessorConstants.ZEPHYR, Arrays.asList(zephyConfig));
		ProjectToolConfig jiraTest = new ProjectToolConfig();
		jiraTest.setJiraRegressionTestValue(Arrays.asList("test1"));
		jiraTest.setTestCaseStatus(Arrays.asList("test1"));
		projectTool.put(ProcessorConstants.ZEPHYR, Arrays.asList(zephyConfig));
		projectTool.put(ProcessorConstants.JIRA_TEST, Arrays.asList(jiraTest));
		toolMap.put(new ObjectId("6335363749794a18e8a4479b"), projectTool);
		when(cacheService.cacheProjectToolConfigMapData()).thenReturn(toolMap);
	}

	@Test
	public void testGetQualifierType() {
		assertThat("Kpi Name :", regressionPercentageServiceImpl.getQualifierType(),
				equalTo("REGRESSION_AUTOMATION_COVERAGE"));
	}

	@Test
	public void getKpiDataTest() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put("automationPercentage", Arrays.asList("-20", "20-40", "40-60", "60-79", "80-"));

		when(configHelperService.calculateMaturity()).thenReturn(maturityRangeMap);
		when(testCaseDetailsRepository.findTestDetails(any(), any(), any())).thenReturn(testCaseDetailsList);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		String kpiRequestTrackerId = "Excel-Zephyr-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.ZEPHYR.name()))
				.thenReturn(kpiRequestTrackerId);
		try {
			KpiElement kpiElement = regressionPercentageServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat(((List<DataCount>) kpiElement.getTrendValueList()).size(), equalTo(1));
		} catch (ApplicationException exception) {

		}
	}

	@Test
	public void fetchKPIDataFromDbTest() throws ApplicationException {
		List<Node> leafNodeList = new ArrayList<>();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.SPRINT) {
				leafNodeList.addAll(v);
			}
		});
		when(testCaseDetailsRepository.findTestDetails(any(), any(), any())).thenReturn(testCaseDetailsList);
		Map<String, Object> map = regressionPercentageServiceImpl.fetchKPIDataFromDb(leafNodeList, null, null, kpiRequest);
		Map<String, List<TestCaseDetails>> testCaseData = testCaseDetailsList.stream()
				.collect(Collectors.groupingBy(TestCaseDetails::getBasicProjectConfigId, Collectors.toList()));
		assertThat("fetch KPI dats from DB :", map.get("testCaseData"), equalTo(testCaseData));
	}

	@Test
	public void fetchKPIDataFromDbTest_fieldMapping() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put("automationPercentage", Arrays.asList("-20", "20-40", "40-60", "60-79", "80-"));

		when(configHelperService.calculateMaturity()).thenReturn(maturityRangeMap);
		fieldMappingMap.computeIfPresent(new ObjectId("6335363749794a18e8a4479b"), (key, mapping) -> {
			mapping.setUploadDataKPI42(true);
			return mapping;
		});
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		String kpiRequestTrackerId = "Excel-Zephyr-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.ZEPHYR.name()))
				.thenReturn(kpiRequestTrackerId);
		try {
			KpiElement kpiElement = regressionPercentageServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat(((List<DataCount>) kpiElement.getTrendValueList()).size(), equalTo(1));
		} catch (ApplicationException exception) {

		}
	}

	@Test
	public void testCalculateKPIMetrics() {
		Map<String, Object> filterComponentIdWiseDefectMap = new HashMap<>();
		filterComponentIdWiseDefectMap.put(TESTCASEKEY, totalTestCaseList);
		Double automatedValue = regressionPercentageServiceImpl.calculateKPIMetrics(filterComponentIdWiseDefectMap);
		assertThat("Automated Percentage value :", automatedValue, equalTo(null));
	}

	@Test
	public void calculateKpiValueTest() {
		Double kpiValue = regressionPercentageServiceImpl.calculateKpiValue(Arrays.asList(1.0, 2.0), "kpi14");
		assertThat("Kpi value  :", kpiValue, equalTo(0.0));
	}
}
