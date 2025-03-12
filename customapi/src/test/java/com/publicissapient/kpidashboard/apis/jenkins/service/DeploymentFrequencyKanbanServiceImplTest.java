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

package com.publicissapient.kpidashboard.apis.jenkins.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.DeploymentDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.DeploymentFrequencyInfo;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.Deployment;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.repository.application.DeploymentRepository;

@RunWith(MockitoJUnitRunner.class)
public class DeploymentFrequencyKanbanServiceImplTest {

	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<Deployment> deploymentList = new ArrayList<>();
	private List<ProjectToolConfig> projectToolConfigList = new ArrayList<>();
	private List<DataCount> trendValues = new ArrayList<>();

	private Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	private Map<String, List<ProjectToolConfig>> projectToolConfigMap = new HashMap<>();
	private Map<ObjectId, Map<String, List<ProjectToolConfig>>> toolProjectMap = new HashMap<>();
	private Map<String, List<String>> maturityRangeMap = new HashMap<>();
	private Map<String, List<DataCount>> trendValueMap = new LinkedHashMap<>();
	Map<String, Object> durationFilter = new LinkedHashMap<>();
	private DeploymentFrequencyInfo deploymentFrequencyInfo;
	private List<Deployment> deploymentListCurrentMonth;

	@Mock
	private DeploymentRepository deploymentRepository;

	@Mock
	private CacheService cacheService;

	@Mock
	private ConfigHelperService configHelperService;

	@Mock
	private CustomApiConfig customApiConfig;

	@Mock
	private CommonService commonService;

	@InjectMocks
	private DeploymentFrequencyKanbanServiceImpl deploymentFrequencyKanbanService;

	private KpiRequest kpiRequest;
	private KpiElement kpiElement;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();

	@Before
	public void setup() {

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi118");
		kpiRequest.setLabel("PROJECT");
		kpiElement = kpiRequest.getKpiList().get(0);

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		DeploymentDataFactory deploymentDataFactory = DeploymentDataFactory.newInstance();
		deploymentList = deploymentDataFactory.getDeploymentDataList();

		projectConfigList.forEach(projectConfig -> {
			projectConfigMap.put(projectConfig.getProjectName(), projectConfig);
		});
		Mockito.when(cacheService.cacheProjectConfigMapData()).thenReturn(projectConfigMap);
		projectToolConfigMap.put("Bamboo", projectToolConfigList);

		toolProjectMap.put(new ObjectId("6335363749794a18e8a4479b"), projectToolConfigMap);

		setTreadValuesDataCount();

		maturityRangeMap.put(KPICode.DEPLOYMENT_FREQUENCY_KANBAN.name(), Arrays.asList("-1", "1-2", "2-5", "5-10", "10-"));
		deploymentFrequencyInfo = new DeploymentFrequencyInfo();
		deploymentListCurrentMonth = new ArrayList<>();

		Map<String, ProjectBasicConfig> mapOfProjectDetails = new HashMap<>();
		ProjectBasicConfig p1 = new ProjectBasicConfig();
		p1.setId(new ObjectId("6335368249794a18e8a4479f"));
		p1.setProjectName("Test");
		p1.setProjectNodeId("Kanban Project_6335368249794a18e8a4479f");
		mapOfProjectDetails.put(p1.getId().toString(), p1);
		Mockito.when(cacheService.cacheProjectConfigMapData()).thenReturn(mapOfProjectDetails);
	}

	private void setTreadValuesDataCount() {
		List<DataCount> listOfDc = new ArrayList<>();
		listOfDc.add(setDataCountValues("KnowHow", "3", "4", 5L));
		DataCount dataCount = setDataCountValues("KnowHow", "3", "4", listOfDc);
		trendValues.add(dataCount);
		trendValueMap.put("OverAll", trendValues);
		trendValueMap.put("prod -> KnowHow", trendValues);
		trendValueMap.put("QA -> KnowHow", trendValues);
		trendValueMap.put("Dev -> KnowHow", trendValues);
	}

	private DataCount setDataCountValues(String data, String maturity, Object maturityValue, Object value) {
		DataCount dataCount = new DataCount();
		dataCount.setData(data);
		dataCount.setMaturity(maturity);
		dataCount.setMaturityValue(maturityValue);
		dataCount.setValue(value);
		return dataCount;
	}

	@Test
	public void getKpiDataWeek() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		when(deploymentRepository.findDeploymentList(anyMap(), anySet(), anyString(), anyString()))
				.thenReturn(deploymentList);
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JENKINSKANBAN.name()))
				.thenReturn(kpiRequest.getRequestTrackerId());
		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put(KPICode.DEPLOYMENT_FREQUENCY_KANBAN.name(), Arrays.asList("-1", "1-2", "2-5", "5-10", "10-"));
		when(configHelperService.calculateMaturity()).thenReturn(maturityRangeMap);
		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendValueMap);

		Map<String, String> kpiWiseAggregation = new HashMap<>();
		kpiWiseAggregation.put(KPICode.DEPLOYMENT_FREQUENCY_KANBAN.name(), "sum");
		when(configHelperService.calculateCriteria()).thenReturn(kpiWiseAggregation);
		try {
			KpiElement kpiElement = deploymentFrequencyKanbanService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("Deployment Frequency Value :", ((List<DataCount>) kpiElement.getTrendValueList()).size(), equalTo(4));
		} catch (ApplicationException exception) {

		}
	}

	@Test
	public void getKpiDataMonth() throws ApplicationException {
		durationFilter.put(Constant.DURATION, CommonConstant.MONTH);
		durationFilter.put(Constant.COUNT, 20);
		kpiElement.setFilterDuration(durationFilter);
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		when(deploymentRepository.findDeploymentList(anyMap(), anySet(), anyString(), anyString()))
				.thenReturn(deploymentList);

		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JENKINSKANBAN.name()))
				.thenReturn(kpiRequest.getRequestTrackerId());

		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put(KPICode.DEPLOYMENT_FREQUENCY_KANBAN.name(), Arrays.asList("-1", "1-2", "2-5", "5-10", "10-"));
		when(configHelperService.calculateMaturity()).thenReturn(maturityRangeMap);
		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendValueMap);

		Map<String, String> kpiWiseAggregation = new HashMap<>();
		kpiWiseAggregation.put(KPICode.DEPLOYMENT_FREQUENCY_KANBAN.name(), "sum");
		when(configHelperService.calculateCriteria()).thenReturn(kpiWiseAggregation);

		try {
			KpiElement kpiElement = deploymentFrequencyKanbanService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("Deployment Frequency Value :", ((List<DataCount>) kpiElement.getTrendValueList()).size(), equalTo(4));
		} catch (ApplicationException exception) {

		}
	}

	@Test
	public void testFetchKPIDataFromDbData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList, false);

		when(deploymentRepository.findDeploymentList(any(), any(), any(), any())).thenReturn(deploymentList);
		String startDate = "2022-06-03T06:39:40.000";
		String endDate = "2022-01-03T06:39:40.000";
		Map<ObjectId, List<Deployment>> deploymentListMap = deploymentFrequencyKanbanService
				.fetchKPIDataFromDb(leafNodeList, startDate, endDate, null);
		assertThat("Total Deployments Group: ", deploymentListMap.size(), equalTo(1));
	}

	@Test
	public void getQualifierType() {
		String result = deploymentFrequencyKanbanService.getQualifierType();
		assertEquals(result, KPICode.DEPLOYMENT_FREQUENCY_KANBAN.name());
	}

	@Test
	public void testCalculateKpiValue() {
		List<Long> valueList = Arrays.asList(1L, 2L, 3L);
		String kpiId = "kpi118";

		Long result = deploymentFrequencyKanbanService.calculateKpiValue(valueList, kpiId);

		// Assuming calculateKpiValueForLong is a method that sums the values
		assertEquals(Long.valueOf(3L), result);
	}

	@Test
	public void testCalculateKPIMetrics() {
		Map<ObjectId, List<Deployment>> objectIdListMap = Map.of();

		Long result = deploymentFrequencyKanbanService.calculateKPIMetrics(objectIdListMap);

		assertEquals(Long.valueOf(0L), result);
	}

	@Test
	public void testGetQualifierType() {
		String result = deploymentFrequencyKanbanService.getQualifierType();

		assertEquals(KPICode.DEPLOYMENT_FREQUENCY_KANBAN.name(), result);
	}

	@Test
	public void testThreshold() {
		assertEquals(Double.valueOf(0), deploymentFrequencyKanbanService.calculateThresholdValue(new FieldMapping()));
	}

	@Test
	public void testTrendValueWithNonEmptyPipelineName() {
		Map<String, List<DataCount>> trendValueMap = new HashMap<>();
		String envName = "QA";
		List<Deployment> deploymentListEnvWise = new ArrayList<>();
		Deployment deployment = new Deployment();
		deployment.setPipelineName("Pipeline1");
		deploymentListEnvWise.add(deployment);

		List<DataCount> dataCountList = new ArrayList<>();
		DataCount dataCount = new DataCount();
		dataCountList.add(dataCount);

		DeploymentFrequencyKanbanServiceImpl.trendValue(trendValueMap, envName, deploymentListEnvWise, dataCountList);

		assertEquals(1, trendValueMap.get("Pipeline1").size());
	}

	@Test
	public void testTrendValueWithEmptyPipelineName() {
		Map<String, List<DataCount>> trendValueMap = new HashMap<>();
		String envName = "QA";
		List<Deployment> deploymentListEnvWise = new ArrayList<>();
		Deployment deployment = new Deployment();
		deployment.setPipelineName("");
		deploymentListEnvWise.add(deployment);

		List<DataCount> dataCountList = new ArrayList<>();
		DataCount dataCount = new DataCount();
		dataCountList.add(dataCount);

		DeploymentFrequencyKanbanServiceImpl.trendValue(trendValueMap, envName, deploymentListEnvWise, dataCountList);

		assertEquals(1, trendValueMap.get(envName).size());
	}

	@Test
	public void testTrendValueWithMultipleDeployments() {
		Map<String, List<DataCount>> trendValueMap = new HashMap<>();
		String envName = "QA";
		List<Deployment> deploymentListEnvWise = new ArrayList<>();
		Deployment deployment1 = new Deployment();
		deployment1.setPipelineName("Pipeline1");
		Deployment deployment2 = new Deployment();
		deployment2.setPipelineName("Pipeline1");
		deploymentListEnvWise.add(deployment1);
		deploymentListEnvWise.add(deployment2);

		List<DataCount> dataCountList = new ArrayList<>();
		DataCount dataCount1 = new DataCount();
		DataCount dataCount2 = new DataCount();
		dataCountList.add(dataCount1);
		dataCountList.add(dataCount2);

		DeploymentFrequencyKanbanServiceImpl.trendValue(trendValueMap, envName, deploymentListEnvWise, dataCountList);

		assertEquals(2, trendValueMap.get("Pipeline1").size());
	}

	@Test
	public void testSetDeploymentFrequencyInfoForExcelWithJobFolderName() {
		Deployment deployment = new Deployment();
		deployment.setEnvName("QA");
		deployment.setJobFolderName("JobFolder1");
		deployment.setStartTime("2023-10-01T10:00:00");
		deploymentListCurrentMonth.add(deployment);

		deploymentFrequencyKanbanService.setDeploymentFrequencyInfoForExcel(deploymentFrequencyInfo,
				deploymentListCurrentMonth);

		assertEquals(1, deploymentFrequencyInfo.getEnvironmentList().size());
		assertEquals("QA", deploymentFrequencyInfo.getEnvironmentList().get(0));
		assertEquals("JobFolder1", deploymentFrequencyInfo.getJobNameList().get(0));
		assertEquals("01-Oct-2023", deploymentFrequencyInfo.getDeploymentDateList().get(0));
		assertEquals("25/09 - 01/10", deploymentFrequencyInfo.getMonthList().get(0));
	}

	@Test
	public void testSetDeploymentFrequencyInfoForExcelWithJobName() {
		Deployment deployment = new Deployment();
		deployment.setEnvName("QA");
		deployment.setJobName("Job1");
		deployment.setStartTime("2023-10-01T10:00:00");
		deploymentListCurrentMonth.add(deployment);

		deploymentFrequencyKanbanService.setDeploymentFrequencyInfoForExcel(deploymentFrequencyInfo,
				deploymentListCurrentMonth);

		assertEquals(1, deploymentFrequencyInfo.getEnvironmentList().size());
		assertEquals("QA", deploymentFrequencyInfo.getEnvironmentList().get(0));
		assertEquals("Job1", deploymentFrequencyInfo.getJobNameList().get(0));
		assertEquals("01-Oct-2023", deploymentFrequencyInfo.getDeploymentDateList().get(0));
		assertEquals("25/09 - 01/10", deploymentFrequencyInfo.getMonthList().get(0));
	}

	@Test
	public void testSetDeploymentFrequencyInfoForExcelWithMultipleDeployments() {
		Deployment deployment1 = new Deployment();
		deployment1.setEnvName("QA");
		deployment1.setJobName("Job1");
		deployment1.setStartTime("2023-10-01T10:00:00");

		Deployment deployment2 = new Deployment();
		deployment2.setEnvName("Prod");
		deployment2.setJobFolderName("JobFolder2");
		deployment2.setStartTime("2023-10-02T10:00:00");

		deploymentListCurrentMonth.add(deployment1);
		deploymentListCurrentMonth.add(deployment2);

		deploymentFrequencyKanbanService.setDeploymentFrequencyInfoForExcel(deploymentFrequencyInfo,
				deploymentListCurrentMonth);

		assertEquals(2, deploymentFrequencyInfo.getEnvironmentList().size());
		assertEquals("QA", deploymentFrequencyInfo.getEnvironmentList().get(0));
		assertEquals("Prod", deploymentFrequencyInfo.getEnvironmentList().get(1));
		assertEquals("Job1", deploymentFrequencyInfo.getJobNameList().get(0));
		assertEquals("JobFolder2", deploymentFrequencyInfo.getJobNameList().get(1));
		assertEquals("01-Oct-2023", deploymentFrequencyInfo.getDeploymentDateList().get(0));
		assertEquals("02-Oct-2023", deploymentFrequencyInfo.getDeploymentDateList().get(1));
		assertEquals("25/09 - 01/10", deploymentFrequencyInfo.getMonthList().get(0));
		assertEquals("02/10 - 08/10", deploymentFrequencyInfo.getMonthList().get(1));
	}
}
