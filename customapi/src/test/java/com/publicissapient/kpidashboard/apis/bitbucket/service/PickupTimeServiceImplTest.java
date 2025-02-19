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

package com.publicissapient.kpidashboard.apis.bitbucket.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.data.RepoToolsKpiRequestDataFactory;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolKpiMetricResponse;
import com.publicissapient.kpidashboard.apis.repotools.service.RepoToolsConfigServiceImpl;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.Tool;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorItem;
import com.publicissapient.kpidashboard.common.model.jira.Assignee;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;

@RunWith(MockitoJUnitRunner.class)
public class PickupTimeServiceImplTest {

	private static Tool tool3;
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	Map<String, List<Tool>> toolGroup = new HashMap<>();
	Map<String, Object> commitMap = new HashMap<String, Object>();

	List<Tool> toolList1;
	List<Tool> toolList2;
	List<Tool> toolList3;

	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<FieldMapping> fieldMappingList = new ArrayList<>();
	private Map<ObjectId, Map<String, List<Tool>>> toolMap = new HashMap<>();
	private KpiElement kpiElement;
	private KpiRequest kpiRequest;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private Map<String, List<DataCount>> trendValueMap = new HashMap<>();
	private List<DataCount> trendValues = new ArrayList<>();
	private List<RepoToolKpiMetricResponse> repoToolKpiMetricResponseList = new ArrayList<>();

	@InjectMocks
	PickupTimeServiceImpl pickupTimeService;

	@Mock
	private ConfigHelperService configHelperService;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	private RepoToolsConfigServiceImpl repoToolsConfigService;
	@Mock
	CacheService cacheService;
	@Mock
	private CommonService commonService;
	@Mock
	private AssigneeDetailsRepository assigneeDetailsRepository;
	@Mock
	private KpiHelperService kpiHelperService;

	@Before
	public void setup() {
		setToolMap();
		setTreadValuesDataCount();

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi11");
		Map<String, List<String>> selectedMap = kpiRequest.getSelectedMap();
		selectedMap.put(CommonConstant.date, Arrays.asList("DAYS"));
		kpiRequest.setSelectedMap(selectedMap);
		kpiRequest.setLabel("Project");
		kpiElement = kpiRequest.getKpiList().get(0);
		kpiRequest.setXAxisDataPoints(5);
		kpiRequest.setDuration("DAYS");

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
		RepoToolsKpiRequestDataFactory repoToolsKpiRequestDataFactory = RepoToolsKpiRequestDataFactory.newInstance();
		repoToolKpiMetricResponseList = repoToolsKpiRequestDataFactory.getRepoToolsKpiRequest();
		ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
		projectBasicConfig.setId(new ObjectId("6335363749794a18e8a4479b"));
		projectBasicConfig.setIsKanban(true);
		projectBasicConfig.setProjectName("Scrum Project");
		projectBasicConfig.setProjectNodeId("Scrum Project_6335363749794a18e8a4479b");
		projectConfigList.add(projectBasicConfig);
		LocalDate date = LocalDate.now();
		while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
			date = date.minusDays(1);
		}
		repoToolKpiMetricResponseList.get(0).setDateLabel(date.toString());
		projectConfigList.forEach(projectConfig -> {
			projectConfigMap.put(projectConfig.getProjectName(), projectConfig);
		});
		Mockito.when(cacheService.cacheProjectConfigMapData()).thenReturn(projectConfigMap);
		fieldMappingList.forEach(fieldMapping -> {
			fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		});

		configHelperService.setProjectConfigMap(projectConfigMap);
		configHelperService.setFieldMappingMap(fieldMappingMap);

		Mockito.when(cacheService.getFromApplicationCache(Mockito.anyString())).thenReturn("trackerid");
		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendValueMap);

		when(configHelperService.getToolItemMap()).thenReturn(toolMap);

		String kpiRequestTrackerId = "Bitbucket-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.BITBUCKET.name()))
				.thenReturn(kpiRequestTrackerId);

		AssigneeDetails assigneeDetails = new AssigneeDetails();
		assigneeDetails.setBasicProjectConfigId("634fdf4ec859a424263dc035");
		assigneeDetails.setSource("Jira");
		Set<Assignee> assigneeSet = new HashSet<>();
		assigneeSet.add(new Assignee("aks", "Akshat Shrivastava",
				new HashSet<>(Arrays.asList("akshat.shrivastav@publicissapient.com"))));
		assigneeSet
				.add(new Assignee("llid", "Hiren", new HashSet<>(Arrays.asList("99163630+hirbabar@users.noreply.github.com"))));
		assigneeDetails.setAssignee(assigneeSet);
		when(assigneeDetailsRepository.findByBasicProjectConfigId(any())).thenReturn(assigneeDetails);
		when(kpiHelperService.populateSCMToolsRepoList(anyMap())).thenReturn(toolList3);
	}

	private void setToolMap() {
		toolList3 = new ArrayList<>();

		ProcessorItem processorItem = new ProcessorItem();
		processorItem.setProcessorId(new ObjectId("63282180160f5b4eb2ac380b"));
		processorItem.setId(new ObjectId("633ab3fb26878c56f03ebd36"));

		ProcessorItem processorItem1 = new ProcessorItem();
		processorItem1.setProcessorId(new ObjectId("63378301e7d2665a7944f675"));
		processorItem1.setId(new ObjectId("633abcd1e7d2665a7944f678"));

		List<ProcessorItem> collectorItemList = new ArrayList<>();
		collectorItemList.add(processorItem);
		List<ProcessorItem> collectorItemList1 = new ArrayList<>();
		collectorItemList1.add(processorItem1);

		tool3 = createTool("url3", "Bitbucket", collectorItemList1);

		toolList3.add(tool3);

		toolGroup.put(Constant.TOOL_BITBUCKET, toolList3);
		toolGroup.put(Constant.TOOL_AZUREREPO, toolList1);
		toolGroup.put(Constant.REPO_TOOLS, toolList2);
		toolMap.put(new ObjectId("6335363749794a18e8a4479b"), toolGroup);
	}

	private Tool createTool(String url, String toolType, List<ProcessorItem> collectorItemList) {
		Tool tool = new Tool();
		tool.setTool(toolType);
		tool.setUrl(url);

		tool.setProcessorItemList(collectorItemList);
		return tool;
	}

	private void setTreadValuesDataCount() {
		List<DataCount> dataCountList = new ArrayList<>();
		DataCount dataCountValue = new DataCount();
		dataCountValue.setData(String.valueOf(5L));
		dataCountValue.setValue(5L);
		dataCountList.add(dataCountValue);
		DataCount dataCount = setDataCountValues("Scrum Project", "3", "4", dataCountList);
		trendValues.add(dataCount);
		trendValueMap.put("Overall#Overall", trendValues);
		trendValueMap.put("Overall#Hiren", trendValues);
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
	public void testGetQualifierType() {
		assertThat("KPI name: ", pickupTimeService.getQualifierType(), equalTo("PICKUP_TIME"));
	}

	@Test
	public void testGetKpiData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendValueMap);

		when(configHelperService.getToolItemMap()).thenReturn(toolMap);

		String kpiRequestTrackerId = "Bitbucket-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.BITBUCKET.name()))
				.thenReturn(kpiRequestTrackerId);

		when(kpiHelperService.getRepoToolsKpiMetricResponse(any(), any(), any(), any(), any(), any()))
				.thenReturn(repoToolKpiMetricResponseList);
		try {
			KpiElement kpiElement = pickupTimeService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail.getMapOfListOfProjectNodes().get("project").get(0));
		} catch (ApplicationException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetKpiDataDays() throws ApplicationException {
		kpiRequest.setDuration(Constant.DAYS);
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		try {
			KpiElement kpiElement = pickupTimeService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail.getMapOfListOfProjectNodes().get("project").get(0));
			assertThat("Trend Size: ", ((List) kpiElement.getTrendValueList()).size(), equalTo(2));
		} catch (ApplicationException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testCalculateKPIMetrics() {
		assertThat("Value: ", pickupTimeService.calculateKPIMetrics(commitMap), equalTo(null));
	}

	@Test
	public void testCalculateKpiValue() {
		assertThat("Value: ", pickupTimeService.calculateKPIMetrics(commitMap), equalTo(null));
	}
}
