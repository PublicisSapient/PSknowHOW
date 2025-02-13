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

package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import com.publicissapient.kpidashboard.apis.common.service.KpiDataCacheService;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.service.JiraServiceR;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.ReleaseWisePI;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

@RunWith(MockitoJUnitRunner.class)
public class PIPredictabilityServiceImplTest {

	@InjectMocks
	private PIPredictabilityServiceImpl piPredictabilityService;

	@Mock
	private ConfigHelperService configHelperService;

	@Mock
	private CacheService cacheService;

	@Mock
	private KPIHelperUtil kpiHelperUtil;

	@Mock
	private JiraIssueRepository jiraIssueRepository;

	@Mock
	private FieldMappingRepository fieldMappingRepository;
	@Mock
	private CustomApiConfig customApiSetting;

    @Mock
    private KpiDataCacheService kpiDataCacheService;

    @Mock
    private FilterHelperService filterHelperService;

    @Mock
	private JiraServiceR jiraKPIService;

	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private KpiRequest kpiRequest;
	private Map<String, Object> filterLevelMap;
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	private List<ReleaseWisePI> releaseWisePIList = new ArrayList<>();
	List<JiraIssue> piWiseEpicList = new ArrayList<>();
	private FieldMapping fieldMapping;

	@Before
	public void setup() {

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.PI_PREDICTABILITY.getKpiId());
		kpiRequest.setLabel("PROJECT");

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		filterLevelMap = new LinkedHashMap<>();
		filterLevelMap.put("PROJECT", Filters.PROJECT);

		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();

		piWiseEpicList = jiraIssueDataFactory.getJiraIssues();

		ProjectBasicConfig projectConfig = new ProjectBasicConfig();
		projectConfig.setId(new ObjectId("6335363749794a18e8a4479b"));
		projectConfig.setProjectName("Scrum Project");

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		configHelperService.setFieldMappingMap(fieldMappingMap);
		// when(configHelperService.getFieldMapping(projectConfig.getId())).thenReturn(fieldMapping);
		// set aggregation criteria kpi wise
		kpiWiseAggregation.put("PI_PREDICTABILITY", "sum");
		prepareReleaseWisePIList(releaseWisePIList);

	}

	@After
	public void cleanup() {
		piWiseEpicList = null;
	}

	@Test
	public void testFetchKPIDataFromDbData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList, false);
		String startDate = leafNodeList.get(0).getSprintFilter().getStartDate();
		String endDate = leafNodeList.get(leafNodeList.size() - 1).getSprintFilter().getEndDate();

		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
		List<JiraIssue> piWiseEpicList = jiraIssueDataFactory.getJiraIssues();

        when(kpiDataCacheService.fetchPiPredictabilityData(Mockito.any(), Mockito.any())).thenReturn(piWiseEpicList);
      	Map<String, Object> defectDataListMap = piPredictabilityService.fetchKPIDataFromDb(leafNodeList, startDate,
				endDate, kpiRequest);
		assertNotNull(defectDataListMap);
	}

	@Test
	public void wrongVersionData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList, false);
		String startDate = leafNodeList.get(0).getSprintFilter().getStartDate();
		String endDate = leafNodeList.get(leafNodeList.size() - 1).getSprintFilter().getEndDate();

		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
		List<JiraIssue> piWiseEpicList = jiraIssueDataFactory.getJiraIssues();

		ReleaseWisePI release1 = new ReleaseWisePI();
		release1.setBasicProjectConfigId("6335363749794a18e8a4479c");
		release1.setReleaseName(new ArrayList<>(Collections.singleton("KnowHOW v7.0.0")));
		release1.setUniqueTypeName("Story");
		List<ReleaseWisePI> objects = new ArrayList<>();

		objects.add(release1);
		Map<String, Object> defectDataListMap = piPredictabilityService.fetchKPIDataFromDb(leafNodeList, startDate,
				endDate, kpiRequest);
		assertNotNull(defectDataListMap);
	}

	private void prepareReleaseWisePIList(List<ReleaseWisePI> releaseWisePIList) {
		ReleaseWisePI release1 = new ReleaseWisePI();
		release1.setBasicProjectConfigId("6335363749794a18e8a4479b");

		release1.setReleaseName(new ArrayList<>(Collections.singleton("KnowHOW v7.0.0")));
		release1.setUniqueTypeName("Story");
		releaseWisePIList.add(release1);

		ReleaseWisePI release2 = new ReleaseWisePI();
		release2.setBasicProjectConfigId("6335363749794a18e8a4479b");
		release2.setReleaseName(new ArrayList<>(Collections.singleton("KnowHOW PI-11")));
		release2.setUniqueTypeName("Epic");
		releaseWisePIList.add(release2);

		// Add more instances for other records...
		ReleaseWisePI release3 = new ReleaseWisePI();
		release3.setBasicProjectConfigId("6335363749794a18e8a4479b");
		release3.setReleaseName(new ArrayList<>(Collections.singleton("KnowHOW PI-12")));
		release3.setUniqueTypeName("Epic");
		releaseWisePIList.add(release3);

		ReleaseWisePI release4 = new ReleaseWisePI();
		release4.setBasicProjectConfigId("6335363749794a18e8a4479b");
		release4.setReleaseName(new ArrayList<>(Collections.singleton("KnowHOW v6.1.0")));
		release4.setUniqueTypeName("Bug");
		releaseWisePIList.add(release4);

		ReleaseWisePI release5 = new ReleaseWisePI();
		release5.setBasicProjectConfigId("6335363749794a18e8a4479b");
		release5.setReleaseName(new ArrayList<>());
		release5.setUniqueTypeName("Bug");
		releaseWisePIList.add(release5);
	}

	@Test
	public void testQualifierType() {
		String kpiName = KPICode.PI_PREDICTABILITY.name();
		String type = piPredictabilityService.getQualifierType();
		assertThat("KPI NAME: ", type, equalTo(kpiName));
	}

	@Test
	public void getPIPredictability1() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);

		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(piPredictabilityService.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		List<ReleaseWisePI> releaseWisePIList = new ArrayList<>();
		when(customApiSetting.getJiraXaxisMonthCount()).thenReturn(5);
        when(kpiDataCacheService.fetchPiPredictabilityData(Mockito.any(), Mockito.any())).thenReturn(piWiseEpicList);
		try {
			KpiElement kpiElement = piPredictabilityService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("PI Predictability TrendValue :", ((List<DataCount>) kpiElement.getTrendValueList()).size(),
					equalTo(1));
		} catch (Exception exception) {
		}
	}

	@Test
	public void inAccurateReleaseData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		String kpiRequestTrackerId = "5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(piPredictabilityService.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		piWiseEpicList.stream().filter(jiraIssue -> CollectionUtils.isNotEmpty(jiraIssue.getReleaseVersions()))
				.forEach(jiraIssue -> jiraIssue.getReleaseVersions().get(0).setReleaseDate(null));
		try {
			KpiElement kpiElement = piPredictabilityService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("PI Predictability TrendValue :", ((List<DataCount>) kpiElement.getTrendValueList()).size(),
					equalTo(1));
		} catch (Exception exception) {
		}
	}

	@Test
	public void noFieldMapping() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping1 = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMapping1.setJiraIssueEpicTypeKPI153(null);
		try {
			KpiElement kpiElement = piPredictabilityService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("PI Predictability TrendValue :", ((List<DataCount>) kpiElement.getTrendValueList()).size(),
					equalTo(1));
		} catch (Exception exception) {
		}
	}

	@Test
	public void calculateKpiMetric() {
		Assert.assertNull(piPredictabilityService.calculateKPIMetrics(Map.of(" ", "")));
	}

	@Test
	public void testCalculateKpiValue() {
		List<Double> valueList = Arrays.asList(10D, 20D, 30D, 40D);
		String kpiId = "kpi153";
		Double result = piPredictabilityService.calculateKpiValue(valueList, kpiId);
		assertEquals(0.0, result);
	}

	@Test
	public void calculateThresholdValue() {
		fieldMapping.setThresholdValueKPI153("15");
		 Assert.assertEquals(Double.valueOf(15D),piPredictabilityService.calculateThresholdValue(fieldMapping.getThresholdValueKPI153(), KPICode.PI_PREDICTABILITY.getKpiId()));
	}
}
