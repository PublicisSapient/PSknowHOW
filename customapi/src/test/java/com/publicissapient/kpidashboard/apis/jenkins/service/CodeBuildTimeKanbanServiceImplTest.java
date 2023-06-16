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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyKanbanFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.BuildDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyDataKanban;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.Tool;
import com.publicissapient.kpidashboard.common.repository.application.BuildRepository;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;

/**
 * Jenkins KPI - CodeBuildTime Test class
 * 
 * @author Hiren Babariya
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class CodeBuildTimeKanbanServiceImplTest {

	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	Map<String, List<Tool>> toolGroup = new HashMap<>();
	@Mock
	BuildRepository buildRepository;
	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	FilterHelperService filterHelperService;
	@Mock
	ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	FieldMappingRepository fieldMappingRepository;
	@Mock
	CustomApiConfig customApiConfig;
	@Mock
	CommonService commonService;
	@InjectMocks
	CodeBuildTimeKanbanServiceImpl codeBuildTimeKanbanServiceImpl;
	private Map<ObjectId, Map<String, List<Tool>>> toolMap = new HashMap<>();
	private List<Build> buildList = new ArrayList<>();
	private Map<String, List<DataCount>> trendValueMap = new HashMap<>();
	private List<DataCount> trendValues = new ArrayList<>();
	@Mock
	private KpiHelperService kpiHelperService;
	private KpiRequest kpiRequest;
	private List<AccountHierarchyDataKanban> accountHierarchyKanbanDataList = new ArrayList<>();

	@Before
	public void setup() {

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi66");
		kpiRequest.setLabel("PROJECT");

		AccountHierarchyKanbanFilterDataFactory accountHierarchyKanbanFilterDataFactory = AccountHierarchyKanbanFilterDataFactory
				.newInstance();
		accountHierarchyKanbanDataList = accountHierarchyKanbanFilterDataFactory.getAccountHierarchyKanbanDataList();

		BuildDataFactory buildDataFactory = BuildDataFactory
				.newInstance("/json/non-JiraProcessors/build_details_kanban.json");
		buildList = buildDataFactory.getbuildDataList();

		Map<String, String> aggregationMap = new HashMap<>();
		aggregationMap.put("kanbanCodeBuildTime", "average");
		setTreadValuesDataCount();
	}

	@Test
	public void testGetCodeBuildTimeKanban() throws Exception {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				new ArrayList<>(), accountHierarchyKanbanDataList, "hierarchyLevelOne", 5);

		when(buildRepository.findBuildList(any(), any(), any(), any())).thenReturn(buildList);
		String kpiRequestTrackerId = "Excel-Jenkins-5be544de025de212549176a9";

		try {
			KpiElement kpiElement = codeBuildTimeKanbanServiceImpl.getKpiData(kpiRequest,
					kpiRequest.getKpiList().get(0), treeAggregatorDetail);
			assertThat("Code Build Time :", ((List<DataCount>) kpiElement.getTrendValueList()).size(), equalTo(3));
		} catch (Exception enfe) {

		}

	}

	@Test
	public void testGetQualifierType() {

		String result = codeBuildTimeKanbanServiceImpl.getQualifierType();
		assertEquals(result, KPICode.CODE_BUILD_TIME_KANBAN.name());

	}

	@Test
	public void testCalculateMaturity() {
		when(commonService.getMaturityLevel(any(), any(), any())).thenReturn("3");
		String maturity = codeBuildTimeKanbanServiceImpl.calculateMaturity(new ArrayList<>(),
				KPICode.CODE_BUILD_TIME_KANBAN.getKpiId(), "3");
		assertThat("maturity: ", maturity, equalTo("3"));
	}

	private void setTreadValuesDataCount() {
		List<DataCount> dataCountList = new ArrayList<>();
		DataCount dataCountValue = new DataCount();
		dataCountValue.setData("5");
		dataCountValue.setSProjectName("Kanban Project");
		dataCountValue.setDate("2019-03-12");
		dataCountValue.setHoverValue(new HashMap<>());
		dataCountValue.setValue(new HashMap());
		dataCountList.add(dataCountValue);
		DataCount dataCount = new DataCount();
		dataCount.setData("Kanban Project");
		dataCount.setValue(dataCountList);
		trendValues.add(dataCount);
		trendValueMap.put("Overall", trendValues);
		trendValueMap.put("UI_BUILD", trendValues);
		trendValueMap.put("API_BUILD", trendValues);
	}
}
