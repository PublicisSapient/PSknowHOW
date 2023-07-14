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
package com.publicissapient.kpidashboard.apis.bitbucket.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyKanbanFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.CommitDetailsDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyDataKanban;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.Tool;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorItem;
import com.publicissapient.kpidashboard.common.model.scm.CommitDetails;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.scm.CommitRepository;

/**
 * @author shichand0
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class CodeCommitKanbanServiceImplTest {

	private static Tool tool1;
	private static Tool tool2;
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	Map<String, List<Tool>> toolGroup = new HashMap<>();
	@Mock
	CommitRepository commitRepository;
	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	KpiHelperService kpiHelperService;
	@Mock
	ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	FieldMappingRepository fieldMappingRepository;
	@Mock
	CustomApiConfig customApiSetting;
	@InjectMocks
	CodeCommitKanbanServiceImpl codeCommitServiceImpl;
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<FieldMapping> fieldMappingList = new ArrayList<>();
	private Map<ObjectId, Map<String, List<Tool>>> toolMap = new HashMap<>();
	private List<CommitDetails> commitList = new ArrayList<>();
	private List<String> filterCategory = new ArrayList<>();
	private Map<String, List<DataCount>> trendValueMap = new HashMap<>();
	private List<DataCount> trendValues = new ArrayList<>();
	@Mock
	private CommonService commonService;

	private KpiRequest kpiRequest;

	private List<AccountHierarchyDataKanban> accountHierarchyKanbanDataList = new ArrayList<>();

	@Before
	public void setup() {

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi65");
		kpiRequest.setLabel("PROJECT");

		AccountHierarchyKanbanFilterDataFactory accountHierarchyKanbanFilterDataFactory = AccountHierarchyKanbanFilterDataFactory
				.newInstance();
		accountHierarchyKanbanDataList = accountHierarchyKanbanFilterDataFactory.getAccountHierarchyKanbanDataList();

		CommitDetailsDataFactory commitDetailsDataFactory = CommitDetailsDataFactory
				.newInstance("/json/non-JiraProcessors/commit_details_kanban.json");
		commitList = commitDetailsDataFactory.getcommitDetailsList();

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

		tool1 = createTool("URL1", "BRANCH1", "Bitbucket", "USER1", "PASS1", processorItemList);
		tool2 = createTool("URL1", "BRANCH2", "Bitbucket", "USER2", "PASS2", processorItemList);

		toolList.add(tool1);
		toolList.add(tool2);

		toolGroup.put(Constant.TOOL_BITBUCKET, toolList);

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

	private CommitDetails createCommit(String url, String branch, String date, Long count, ObjectId processorItemId) {
		CommitDetails commitDetails = new CommitDetails();
		commitDetails.setUrl(url);
		commitDetails.setBranch(branch);
		commitDetails.setDate(date);
		commitDetails.setCount(count);
		commitDetails.setProcessorItemId(processorItemId);
		return commitDetails;
	}

	@Test
	public void testCodeCommit_final() throws ApplicationException {

		setup();
		Map<String, Node> mapTmp = new HashMap<>();
		List<Node> leafNodeList = new ArrayList<>();

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				new ArrayList<>(), accountHierarchyKanbanDataList, "hierarchyLevelOne", 5);

		when(commitRepository.findCommitList(any(), any(), any(), any())).thenReturn(commitList);
		when(configHelperService.getToolItemMap()).thenReturn(toolMap);
		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendValueMap);

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
		assertThat(KPICode.NUMBER_OF_CHECK_INS.name(), equalTo(codeCommitServiceImpl.getQualifierType()));
	}

	@Test
	public void testCalculateKPIMetrics() {
		assertThat(codeCommitServiceImpl.calculateKPIMetrics(null), equalTo(null));
	}
}
