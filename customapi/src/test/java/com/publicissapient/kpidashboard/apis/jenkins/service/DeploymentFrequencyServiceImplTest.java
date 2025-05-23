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

package com.publicissapient.kpidashboard.apis.jenkins.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.repository.application.DeploymentRepository;

@RunWith(MockitoJUnitRunner.class)
public class DeploymentFrequencyServiceImplTest {

	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<Deployment> deploymentList = new ArrayList<>();
	private List<ProjectToolConfig> projectToolConfigList = new ArrayList<>();
	private List<AccountHierarchyData> ahdList = new ArrayList<>();
	private List<DataCount> trendValues = new ArrayList<>();

	private Map<String, Object> filterLevelMap;
	private Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	private Map<String, List<ProjectToolConfig>> projectToolConfigMap = new HashMap<>();
	private Map<ObjectId, Map<String, List<ProjectToolConfig>>> toolProjectMap = new HashMap<>();
	private Map<String, List<String>> maturityRangeMap = new HashMap<>();
	private Map<String, List<DataCount>> trendValueMap = new LinkedHashMap<>();
	Map<String, Object> durationFilter = new LinkedHashMap<>();
	private List<Deployment> deploymentListCurrentMonth;
	private DeploymentFrequencyInfo deploymentFrequencyInfo;
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
	private DeploymentFrequencyServiceImpl deploymentFrequencyService;

	private KpiRequest kpiRequest;
	private KpiElement kpiElement;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();

	@Before
	public void setup() {

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi118");
		kpiRequest.setLabel("PROJECT");
		kpiElement = kpiRequest.getKpiList().get(0);

		ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
		projectBasicConfig.setId(new ObjectId("6335363749794a18e8a4479b"));
		projectBasicConfig.setIsKanban(true);
		projectBasicConfig.setProjectName("Scrum Project");
		projectBasicConfig.setProjectNodeId("Scrum Project_6335363749794a18e8a4479b");
		projectConfigList.add(projectBasicConfig);

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
		Mockito.when(cacheService.cacheProjectConfigMapData()).thenReturn(projectConfigMap);

		toolProjectMap.put(new ObjectId("6335363749794a18e8a4479b"), projectToolConfigMap);

		setTreadValuesDataCount();

		maturityRangeMap.put(KPICode.DEPLOYMENT_FREQUENCY.name(), Arrays.asList("-1", "1-2", "2-5", "5-10", "10-"));
		deploymentFrequencyInfo = new DeploymentFrequencyInfo();
		deploymentListCurrentMonth = new ArrayList<>();
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
		deploymentList.stream().filter(deployment -> deployment.getNumber().equalsIgnoreCase("1847"))
				.forEach(deployment -> {
					deployment.setStartTime(
							LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
				});
		when(deploymentRepository.findDeploymentList(anyMap(), anySet(), anyString(), anyString()))
				.thenReturn(deploymentList);
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JENKINS.name()))
				.thenReturn("Excel-Jenkins-7b0ed9dd-43f6-4086-adc6-fa555fdf6842");
		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put(KPICode.DEPLOYMENT_FREQUENCY.name(), Arrays.asList("-1", "1-2", "2-5", "5-10", "10-"));
		when(configHelperService.calculateMaturity()).thenReturn(maturityRangeMap);
		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendValueMap);

		Map<String, String> kpiWiseAggregation = new HashMap<>();
		kpiWiseAggregation.put(KPICode.DEPLOYMENT_FREQUENCY.name(), "sum");
		when(configHelperService.calculateCriteria()).thenReturn(kpiWiseAggregation);

		try {
			KpiElement kpiElement = deploymentFrequencyService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
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

		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JENKINS.name()))
				.thenReturn(kpiRequest.getRequestTrackerId());

		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put(KPICode.DEPLOYMENT_FREQUENCY.name(), Arrays.asList("-1", "1-2", "2-5", "5-10", "10-"));
		when(configHelperService.calculateMaturity()).thenReturn(maturityRangeMap);
		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendValueMap);

		Map<String, String> kpiWiseAggregation = new HashMap<>();
		kpiWiseAggregation.put(KPICode.DEPLOYMENT_FREQUENCY.name(), "sum");
		when(configHelperService.calculateCriteria()).thenReturn(kpiWiseAggregation);

		try {
			KpiElement kpiElement = deploymentFrequencyService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
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
		Map<ObjectId, List<Deployment>> deploymentListMap = deploymentFrequencyService.fetchKPIDataFromDb(leafNodeList,
				startDate, endDate, null);
		assertThat("Total Deployments Group: ", deploymentListMap.size(), equalTo(1));
	}

	@Test
	public void getQualifierType() {
		String result = deploymentFrequencyService.getQualifierType();
		assertEquals(result, KPICode.DEPLOYMENT_FREQUENCY.name());
	}
}
