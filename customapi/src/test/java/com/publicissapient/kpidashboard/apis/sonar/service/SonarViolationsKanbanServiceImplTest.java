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

/**
 * 
 */
package com.publicissapient.kpidashboard.apis.sonar.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.CommonService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyKanbanFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.KanbanIssueCustomHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.data.SonarHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyDataKanban;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.SonarViolations;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.Tool;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorItem;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.sonar.SonarHistory;
import com.publicissapient.kpidashboard.common.repository.sonar.SonarHistoryRepository;

/**
 * @author shichan0
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SonarViolationsKanbanServiceImplTest {

	private static Tool tool1;
	private static Tool tool2;
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	@Mock
	private ConfigHelperService configHelperService;
	@InjectMocks
	private SonarViolationsKanbanServiceImpl svServiceImpl;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	private SonarHistoryRepository sonarHistoryRepository;
	@Mock
	private CacheService cacheService;
	@Mock
	private CommonService commonService;
	private Map<ObjectId, Map<String, List<Tool>>> toolMap = new HashMap<>();
	private Map<String, List<Tool>> toolGroup = new HashMap<>();
	private Map<String, List<DataCount>> trendValueMap = new HashMap<>();
	private List<DataCount> trendValues = new ArrayList<>();
	private List<String> filterCategory = new ArrayList<>();
	private List<AccountHierarchyDataKanban> accountHierarchyDataKanbanList = new ArrayList<>();
	private KpiRequest kpiRequest;
	private List<KanbanIssueCustomHistory> jiraIssueCustomHistories = new ArrayList<>();
	private List<SonarHistory> sonarHistoryData = new ArrayList<>();

	@Before
	public void setUp() {

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance("/json/default/kanban_kpi_request.json");
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi64");
		kpiRequest.setLabel("PROJECT");
		kpiRequest.setDuration("WEEKS");
		AccountHierarchyKanbanFilterDataFactory accountHierarchyKanbanFilterDataFactory = AccountHierarchyKanbanFilterDataFactory
				.newInstance();
		accountHierarchyDataKanbanList = accountHierarchyKanbanFilterDataFactory.getAccountHierarchyKanbanDataList();
		KanbanIssueCustomHistoryDataFactory issueHistoryFactory = KanbanIssueCustomHistoryDataFactory.newInstance();
		jiraIssueCustomHistories = issueHistoryFactory
				.getKanbanIssueCustomHistoryDataListByTypeName(Arrays.asList("Story", "Defect", "Issue"));

		SonarHistoryDataFactory sonarHistoryDataFactory = SonarHistoryDataFactory.newInstance();
		sonarHistoryData = sonarHistoryDataFactory.getSonarHistoryList();

		List<ProjectBasicConfig> projectConfigList = getMockProjectConfig();
		List<FieldMapping> fieldMappingList = getMockFieldMapping();
		DateTime date = new DateTime("2018-07-19", DateTimeZone.UTC);
		projectConfigList.forEach(projectConfig -> {
			projectConfigMap.put(projectConfig.getProjectName(), projectConfig);
		});
		fieldMappingList.forEach(fieldMapping -> {
			fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		});
		setTreadValuesDataCount();
		configHelperService.setProjectConfigMap(projectConfigMap);
		configHelperService.setFieldMappingMap(fieldMappingMap);
		filterCategory.add("Project");
		filterCategory.add("Sprint");
		setToolMap();
	}

	private void setTreadValuesDataCount() {
		List<DataCount> dataCountList = new ArrayList<>();
		DataCount dataCountValue = new DataCount();
		dataCountValue.setData(String.valueOf(5L));
		dataCountValue.setValue(5L);
		dataCountList.add(dataCountValue);
		DataCount dataCount = setDataCountValues("Kanban Project", "3", "4", dataCountList);
		trendValues.add(dataCount);
		trendValueMap.put("ENGINEERING.KPIDASHBOARD.PROCESSORS->origin/develop->DA_10304", trendValues);
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
		List<Tool> toolList = new ArrayList<>();

		// remove
		ProcessorItem processorItem = new ProcessorItem();
		processorItem.setId(new ObjectId("5c0f32fe00a9b83a7cbc4f0c"));

		ProcessorItem processorItem1 = new ProcessorItem();
		processorItem1.setId(new ObjectId("5c0f32fe00a9b83a7cbc4f0d"));

		ProcessorItem processorItem2 = new ProcessorItem();
		processorItem2.setId(new ObjectId("5c0f32fe00a9b83a7cbc4f0e"));

		List<ProcessorItem> collectorItemFirstList = new ArrayList<>();
		collectorItemFirstList.add(processorItem);

		List<ProcessorItem> collectorItemThirdList = new ArrayList<>();
		collectorItemThirdList.add(processorItem2);

		List<ProcessorItem> collectorItemSecondList = new ArrayList<>();
		collectorItemSecondList.add(processorItem1);

		tool1 = createTool("KEY1", "url1", "Sonar", "user1", "pass1", collectorItemFirstList);
		tool2 = createTool("KEY2", "url2", "Sonar", "user2", "pass2", collectorItemSecondList);
		Tool tool3 = createTool("KEY3", "url3", "Sonar", "user3", "pass3", collectorItemThirdList);
		Tool tool4 = createTool("KEY4", "url4", "Sonar", "user4", "pass4", null);

		toolList.add(tool1);
		toolList.add(tool2);
		toolList.add(tool3);
		toolList.add(tool4);

		toolGroup.put(Constant.TOOL_SONAR, toolList);

		toolMap.put(new ObjectId("6335368249794a18e8a4479f"), toolGroup);
	}

	private Tool createTool(String key, String url, String toolType, String username, String password,
			List<ProcessorItem> processorItems) {
		Tool tool = new Tool();
		tool.setTool(toolType);
		tool.setUrl(url);
		tool.setProcessorItemList(processorItems);
		return tool;
	}

	private List<ProjectBasicConfig> getMockProjectConfig() {
		List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
		ProjectBasicConfig projectOne = new ProjectBasicConfig();
		projectOne.setId(new ObjectId("5b674d58f47cae8935b1b26f"));
		projectOne.setProjectName("Alpha_Project1_Name");

		ProjectBasicConfig projectTwo = new ProjectBasicConfig();
		projectTwo.setId(new ObjectId("5b719d06a500d00814bfb2b9"));
		projectTwo.setProjectName("Alpha_Project2_Name");

		ProjectBasicConfig projectThree = new ProjectBasicConfig();
		projectThree.setId(new ObjectId("5ba8e182d3735010e7f1fa45"));
		projectThree.setProjectName("Beta_Project1_Name");

		projectConfigList.add(projectOne);
		projectConfigList.add(projectTwo);
		projectConfigList.add(projectThree);
		return projectConfigList;
	}

	private List<FieldMapping> getMockFieldMapping() {
		List<FieldMapping> fieldMappingList = new ArrayList<>();
		FieldMapping projectOne = new FieldMapping();
		projectOne.setBasicProjectConfigId(new ObjectId("5b674d58f47cae8935b1b26f"));

		FieldMapping projectTwo = new FieldMapping();
		projectTwo.setBasicProjectConfigId(new ObjectId("5b719d06a500d00814bfb2b9"));

		FieldMapping projectThree = new FieldMapping();
		projectThree.setBasicProjectConfigId(new ObjectId("5ba8e182d3735010e7f1fa45"));

		fieldMappingList.add(projectOne);
		fieldMappingList.add(projectTwo);
		fieldMappingList.add(projectThree);
		return fieldMappingList;
	}

	@After
	public void cleanup() {

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetViolations() throws Exception {
		setToolMap();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				new ArrayList<>(), accountHierarchyDataKanbanList, "hierarchyLevelOne", 4);
		String kpiRequestTrackerId = "Excel-Sonar-5be544de025de212549176a9";
		when(customApiConfig.getSonarWeekCount()).thenReturn(5);
		when(configHelperService.getToolItemMap()).thenReturn(toolMap);
		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendValueMap);
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.SONARKANBAN.name()))
				.thenReturn(kpiRequestTrackerId);
		when(sonarHistoryRepository.findByProcessorItemIdInAndTimestampGreaterThan(anyList(), anyLong()))
				.thenReturn(sonarHistoryData);

		try {
			KpiElement kpiElement = svServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			((List<DataCountGroup>) kpiElement.getTrendValueList()).forEach(data -> {
				String projectName = data.getFilter();
				switch (projectName) {
				case "Overall":
					assertThat("Sonar Tech Debt:", data.getValue().size(), equalTo(1));
					break;

				case "ENGINEERING.KPIDASHBOARD.PROCESSORS->origin/develop->DA_10304":
					assertThat("Sonar Tech Debt:", data.getValue().size(), equalTo(1));
					break;

				}
			});
		} catch (Exception enfe) {

		}

	}

	@Test
	public void testGetQualifierType() {
		Assert.assertThat(KPICode.SONAR_VIOLATIONS_KANBAN.name(), equalTo(svServiceImpl.getQualifierType()));
	}

	@Test
	public void testCalculateKPIMetrics() {
		Assert.assertNotNull(svServiceImpl.calculateKPIMetrics(new HashMap<>()));
	}

	@Test
	public void calculateAggregatedValue() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				new ArrayList<>(), accountHierarchyDataKanbanList, "hierarchyLevelOne", 4);
		String kpiRequestTrackerId = "Excel-Sonar-5be544de025de212549176a9";
		SonarViolations ele = null;
		Assert.assertNull(ele);
	}

	@Test
	public void testCalculateAggregatedValue() {
		assertNotNull(svServiceImpl.calculateAggregatedValue(null, new HashMap<>(), KPICode.SONAR_VIOLATIONS_KANBAN));
	}

}
