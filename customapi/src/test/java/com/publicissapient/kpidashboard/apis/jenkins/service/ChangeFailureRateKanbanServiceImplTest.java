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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
import com.publicissapient.kpidashboard.apis.data.BuildDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.repository.application.BuildRepository;

@RunWith(MockitoJUnitRunner.class)
public class ChangeFailureRateKanbanServiceImplTest {

	@Mock
	BuildRepository buildRepository;
	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	FilterHelperService filterHelperService;
	@Mock
	CustomApiConfig customApiConfig;
	@InjectMocks
	ChangeFailureRateKanbanServiceImpl changeFailureRateKanbanService;

	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<FieldMapping> fieldMappingList = new ArrayList<>();
	private Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	private Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();

	private List<Build> buildList = new ArrayList<>();

	private List<DataCount> trendValues = new ArrayList<>();
	private Map<String, List<DataCount>> trendValueMap = new LinkedHashMap<>();
	Map<String, Object> durationFilter = new LinkedHashMap<>();
	@Mock
	private CommonService commonService;

	private KpiRequest kpiRequest;
	private KpiElement kpiElement;
	private TreeAggregatorDetail treeAggregatorDetail;

	@Before
	public void setup() throws ApplicationException {

		setTreadValuesDataCount();

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi116");
		kpiRequest.setLabel("PROJECT");
		kpiElement = kpiRequest.getKpiList().get(0);

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		BuildDataFactory buildDataFactory = BuildDataFactory.newInstance("/json/non-JiraProcessors/build_details.json");
		buildList = buildDataFactory.getbuildDataList();

		projectConfigList.forEach(projectConfig -> {
			projectConfigMap.put(projectConfig.getProjectName(), projectConfig);
		});
		Mockito.when(cacheService.cacheProjectConfigMapData()).thenReturn(projectConfigMap);
		fieldMappingList.forEach(fieldMapping -> {
			fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		});

		configHelperService.setProjectConfigMap(projectConfigMap);
		configHelperService.setFieldMappingMap(fieldMappingMap);
		treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest, accountHierarchyDataList,
				new ArrayList<>(), "hierarchyLevelOne", 5);
	}

	private void setTreadValuesDataCount() {
		DataCount dataCount = setDataCountValues("KnowHow", "3", "4", new DataCount());
		trendValues.add(dataCount);
		trendValueMap.put("OverAll", trendValues);
		trendValueMap.put("UI_Build -> KnowHow", trendValues);
		trendValueMap.put("API_Build -> KnowHow", trendValues);
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
	public void testGetChangeFailureRateWeek() throws Exception {
		durationFilter.put(Constant.DURATION, CommonConstant.WEEK);
		durationFilter.put(Constant.COUNT, 14);
		kpiElement.setFilterDuration(durationFilter);
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		when(buildRepository.findBuildList(any(), any(), any(), any())).thenReturn(buildList);

		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendValueMap);

		String kpiRequestTrackerId = "Excel-JENKINS-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JENKINSKANBAN.name()))
				.thenReturn(kpiRequestTrackerId);
		when(buildRepository.findBuildList(any(), any(), any(), any())).thenReturn(buildList);

		try {

			KpiElement kpiElement = changeFailureRateKanbanService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("CHANGE-FAILURE-RATE :", ((List<DataCount>) kpiElement.getTrendValueList()).size(), equalTo(3));
		} catch (Exception e) {
		}
	}

	@Test
	public void testGetChangeFailureRateMonth() throws Exception {
		durationFilter.put(Constant.DURATION, CommonConstant.MONTH);
		durationFilter.put(Constant.COUNT, 20);
		kpiElement.setFilterDuration(durationFilter);
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		when(buildRepository.findBuildList(any(), any(), any(), any())).thenReturn(buildList);

		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendValueMap);

		String kpiRequestTrackerId = "Excel-JENKINS-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JENKINSKANBAN.name()))
				.thenReturn(kpiRequestTrackerId);
		when(buildRepository.findBuildList(any(), any(), any(), any())).thenReturn(buildList);

		try {

			KpiElement kpiElement2 = changeFailureRateKanbanService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("CHANGE-FAILURE-RATE :", ((List<DataCount>) kpiElement2.getTrendValueList()).size(), equalTo(3));
		} catch (Exception e) {
		}
	}

	@Test
	public void getQualifierType() {
		String result = changeFailureRateKanbanService.getQualifierType();
		assertEquals(result, KPICode.CHANGE_FAILURE_RATE_KANBAN.name());
	}

	@Test
	public void testTrendValueWithPipelineName()
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		List<Build> buildList = new ArrayList<>();
		Build build = new Build();
		build.setPipelineName("Pipeline1");
		buildList.add(build);

		String trendLineName = "TrendLine1";
		Map<String, List<DataCount>> trendValueMap = new HashMap<>();
		String jobName = "Job1";
		List<DataCount> dataCountList = new ArrayList<>();
		DataCount dataCount = new DataCount();
		dataCountList.add(dataCount);
		Method trendValueMethod = ChangeFailureRateServiceImpl.class.getDeclaredMethod("trendValue", List.class,
				String.class, Map.class, String.class, List.class);
		trendValueMethod.setAccessible(true);
		trendValueMethod.invoke(null, buildList, trendLineName, trendValueMap, jobName, dataCountList);

		assertTrue(trendValueMap.containsKey("Pipeline1" + CommonUtils.getStringWithDelimiters(trendLineName)));
		assertEquals(dataCountList, trendValueMap.get("Pipeline1" + CommonUtils.getStringWithDelimiters(trendLineName)));
	}
}
