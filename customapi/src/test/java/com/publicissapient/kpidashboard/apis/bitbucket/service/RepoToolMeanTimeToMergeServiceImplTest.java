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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
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
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.data.RepoToolsKpiRequestDataFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
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
import com.publicissapient.kpidashboard.common.model.scm.BranchMergeReqCount;

@RunWith(MockitoJUnitRunner.class)
public class RepoToolMeanTimeToMergeServiceImplTest {

	private static Tool tool1;

	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();

	@Mock
	ConfigHelperService configHelperService;
	@Mock
	CacheService cacheService;
	@InjectMocks
	RepoToolMeanTimeToMergeServiceImpl meanTimeToMergeServiceImpl;
	@Mock
	CustomApiConfig customApiConfig;
	@Mock
	KpiHelperService kpiHelperService;
	@Mock
	RepoToolsConfigServiceImpl repoToolsConfigService;
	@Mock
	private CommonService commonService;

	private List<RepoToolKpiMetricResponse> repoToolKpiMetricResponseList = new ArrayList<>();
	private Map<ObjectId, Map<String, List<Tool>>> toolMap = new HashMap<>();
	private Map<String, List<Tool>> toolGroup = new HashMap<>();
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<FieldMapping> fieldMappingList = new ArrayList<>();
	private List<DataCount> trendValues = new ArrayList<>();
	private Map<String, List<DataCount>> trendValueMap = new LinkedHashMap<>();
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private KpiRequest kpiRequest;

	@Before
	public void setup() {

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi84");
		kpiRequest.setLabel("PROJECT");
		kpiRequest.getSelectedMap().put(CommonConstant.date, Arrays.asList("DAYS"));
		kpiRequest.setXAxisDataPoints(5);
		kpiRequest.setDuration("WEEKS");

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		RepoToolsKpiRequestDataFactory repoToolsKpiRequestDataFactory = RepoToolsKpiRequestDataFactory.newInstance();
		repoToolKpiMetricResponseList = repoToolsKpiRequestDataFactory.getRepoToolsKpiRequest();

		projectConfigList.forEach(projectConfig -> {
			projectConfigMap.put(projectConfig.getProjectName(), projectConfig);
		});
		fieldMappingList.forEach(fieldMapping -> {
			fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		});
		configHelperService.setProjectConfigMap(projectConfigMap);
		configHelperService.setFieldMappingMap(fieldMappingMap);
		setToolMap();
		setTreadValuesDataCount();

	}

	private void setTreadValuesDataCount() {
		DataCount dataCount = setDataCountValues("KnowHow", "3", "4", new DataCount());
		trendValues.add(dataCount);
		trendValueMap.put("OverAll", trendValues);
		trendValueMap.put("BRANCH1 -> PR", trendValues);
	}

	private DataCount setDataCountValues(String data, String maturity, Object maturityValue, Object value) {
		DataCount dataCount = new DataCount();
		dataCount.setData(data);
		dataCount.setMaturity(maturity);
		dataCount.setMaturityValue(maturityValue);
		dataCount.setValue(value);
		return dataCount;
	}

	private void setToolMap() {
		List<Tool> toolList1 = new ArrayList<>();

		ProcessorItem processorItem = new ProcessorItem();
		processorItem.setProcessorId(new ObjectId("63242d00aaf87a5b01de7ad6"));
		processorItem.setId(new ObjectId("63316e5667446e5ec838b67e"));

		ProcessorItem processorItem1 = new ProcessorItem();
		processorItem1.setProcessorId(new ObjectId("63378301e7d2665a7944f675"));
		processorItem1.setId(new ObjectId("633abcd1e7d2665a7944f678"));

		List<ProcessorItem> collectorItemList = new ArrayList<>();
		collectorItemList.add(processorItem);
		List<ProcessorItem> collectorItemList1 = new ArrayList<>();
		collectorItemList1.add(processorItem1);

		tool1 = createTool("URL3", "BRANCH3", "Repo_Tools", "USER3", "PASS3", collectorItemList1);

		toolList1.add(tool1);

		toolGroup.put(Constant.REPO_TOOLS, toolList1);
		toolMap.put(new ObjectId("6335363749794a18e8a4479b"), toolGroup);

	}

	private Tool createTool(String url, String branch, String toolType, String username, String password,
			List<ProcessorItem> collectorItemList) {
		Tool tool = new Tool();
		tool.setUrl(url);
		tool.setBranch(branch);
		tool.setTool(toolType);
		tool.setProcessorItemList(collectorItemList);
		tool.setProjectIds(new ObjectId("5c09a60800a9b822d3c794e0"));
		return tool;
	}

	@Test
	public void testGetKpiData() throws Exception {

		setup();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		Map<String, String> aggregationMap = new HashMap<>();
		aggregationMap.put("meanTimeToMerge", "average");
		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();

		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.BITBUCKET.name()))
				.thenReturn("Jira-Excel-5be544de025de212549176a9");
		when(configHelperService.getToolItemMap()).thenReturn(toolMap);
		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendValueMap);
		when(repoToolsConfigService.getRepoToolKpiMetrics(any(), any(), any(), any(), any()))
				.thenReturn(repoToolKpiMetricResponseList);
		KpiElement kpiElement = meanTimeToMergeServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
				treeAggregatorDetail);
		List<BranchMergeReqCount> out = (List<BranchMergeReqCount>) kpiElement.getTrendValueList();
		assertThat("merge requests", out.size(), equalTo(2));
	}

	@Test
	public void testGetQualifierType() {
		String result = meanTimeToMergeServiceImpl.getQualifierType();
		assertEquals(result, KPICode.REPO_TOOL_MEAN_TIME_TO_MERGE.name());

	}

	@Test
	public void testCalculateKPIMetrics() {
		Map<String, Object> stringObjectMap = new HashMap<>();
		assertNull(null, meanTimeToMergeServiceImpl.calculateKPIMetrics(stringObjectMap));
	}
}
