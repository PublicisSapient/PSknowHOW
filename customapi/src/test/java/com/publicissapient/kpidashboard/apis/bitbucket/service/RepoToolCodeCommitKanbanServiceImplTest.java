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
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
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
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyKanbanFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.data.RepoToolsKpiRequestDataFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyDataKanban;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolKpiMetricResponse;
import com.publicissapient.kpidashboard.apis.repotools.service.RepoToolsConfigServiceImpl;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.Tool;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorItem;
import com.publicissapient.kpidashboard.common.model.scm.CommitDetails;
import com.publicissapient.kpidashboard.common.repository.scm.CommitRepository;

@RunWith(MockitoJUnitRunner.class)
public class RepoToolCodeCommitKanbanServiceImplTest {

	private static Tool tool;
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	Map<String, List<Tool>> toolGroup = new HashMap<>();
	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	RepoToolsConfigServiceImpl repoToolsConfigService;
	@Mock
	private CustomApiConfig customApiConfig;
	@InjectMocks
	RepoToolCodeCommitKanbanServiceImpl codeCommitServiceImpl;
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<FieldMapping> fieldMappingList = new ArrayList<>();
	private Map<ObjectId, Map<String, List<Tool>>> toolMap = new HashMap<>();
	private List<String> filterCategory = new ArrayList<>();
	private Map<String, List<DataCount>> trendValueMap = new HashMap<>();
	private List<DataCount> trendValues = new ArrayList<>();
	@Mock
	private CommonService commonService;

	private KpiRequest kpiRequest;

	private List<AccountHierarchyDataKanban> accountHierarchyKanbanDataList = new ArrayList<>();
	private List<RepoToolKpiMetricResponse> repoToolKpiMetricResponseList = new ArrayList<>();

	@Before
	public void setup() {

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi65");
		Map<String, List<String>> selectedMap = kpiRequest.getSelectedMap();
		selectedMap.put(CommonConstant.date, Arrays.asList("DAYS"));
		kpiRequest.setSelectedMap(selectedMap);
		kpiRequest.setLabel("PROJECT");

		AccountHierarchyKanbanFilterDataFactory accountHierarchyKanbanFilterDataFactory = AccountHierarchyKanbanFilterDataFactory
				.newInstance();
		accountHierarchyKanbanDataList = accountHierarchyKanbanFilterDataFactory.getAccountHierarchyKanbanDataList();

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
		filterCategory.add("Project");
		filterCategory.add("Sprint");

	}

	private void setTreadValuesDataCount() {
		List<DataCount> dataCountList = new ArrayList<>();
		dataCountList.add(createDataCount("2022-07-26", 0l));
		dataCountList.add(createDataCount("2022-07-27", 35l));
		dataCountList.add(createDataCount("2022-07-28", 44l));
		dataCountList.add(createDataCount("2022-07-29", 0l));
		dataCountList.add(createDataCount("2022-07-30", 0l));
		dataCountList.add(createDataCount("2022-07-31", 12l));
		dataCountList.add(createDataCount("2022-08-01", 0l));

		DataCount dataCount = createDataCount(null, 0l);
		dataCount.setData("");
		dataCount.setValue(dataCountList);
		trendValues.add(dataCount);
		trendValueMap.put("Overall", trendValues);
		trendValueMap.put("BRANCH1->PR_10304", trendValues);

	}

	private DataCount createDataCount(String date, Long data) {
		DataCount dataCount = new DataCount();
		dataCount.setData(data.toString());
		dataCount.setSProjectName("PR_10304");
		dataCount.setDate(date);
		dataCount.setHoverValue(new HashMap<>());
		dataCount.setValue(Long.valueOf(data));
		return dataCount;
	}

	private void setToolMap() {
		List<Tool> toolList = new ArrayList<>();

		ProcessorItem processorItem = new ProcessorItem();
		processorItem.setId(new ObjectId("633bcf9e26878c56f03ebd38"));
		processorItem.setProcessorId(new ObjectId("63282180160f5b4eb2ac380b"));

		List<ProcessorItem> processorItemList = new ArrayList<>();
		processorItemList.add(processorItem);

		tool = createTool("URL1", "BRANCH3", "RepoTool", "USER3", "PASS3", processorItemList);

		toolList.add(tool);

		toolGroup.put(Constant.REPO_TOOLS, toolList);

		toolMap.put(new ObjectId("6335368249794a18e8a4479f"), toolGroup);
	}

	private Tool createTool(String url, String branch, String toolType, String username, String password,
			List<ProcessorItem> processorItemList) {
		Tool tool = new Tool();
		tool.setUrl(url);
		tool.setBranch(branch);
		tool.setTool(toolType);
		tool.setProcessorItemList(processorItemList);
		return tool;
	}

	@Test
	public void testCodeCommit_final() throws ApplicationException {

		setup();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				new ArrayList<>(), accountHierarchyKanbanDataList, "hierarchyLevelOne", 5);
		when(configHelperService.getToolItemMap()).thenReturn(toolMap);
		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendValueMap);
		when(repoToolsConfigService.getRepoToolKpiMetrics(any(), any(), any(), any(), any()))
				.thenReturn(repoToolKpiMetricResponseList);
		String kpiRequestTrackerId = "Excel-Bitbucket-5be544de025de212549176a9";

		when(cacheService
				.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.BITBUCKETKANBAN.name()))
				.thenReturn(kpiRequestTrackerId);

		KpiElement kpiElement = codeCommitServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
				treeAggregatorDetail);
		((List<DataCountGroup>) kpiElement.getTrendValueList()).forEach(data -> {
			String projectName = data.getFilter();
			switch (projectName) {
			case "Overall":
				assertThat("Overall Commit Details:", data.getValue().size(), equalTo(2));
				break;

			case "BRANCH1->PR_10304":
				assertThat("Branch1 Commit Details:", data.getValue().size(), equalTo(2));
				break;

			}
		});

	}

	@Test
	public void getQualifierType() {
		assertThat(KPICode.REPO_TOOL_NUMBER_OF_CHECK_INS.name(), equalTo(codeCommitServiceImpl.getQualifierType()));
	}

	@Test
	public void testCalculateKPIMetrics() {
		assertThat(codeCommitServiceImpl.calculateKPIMetrics(null), equalTo(null));
	}

}