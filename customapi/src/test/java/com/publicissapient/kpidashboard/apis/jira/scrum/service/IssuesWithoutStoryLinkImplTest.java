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

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.data.*;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.*;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.zephyr.TestCaseDetails;
import com.publicissapient.kpidashboard.common.repository.jira.IssueBacklogRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.zephyr.TestCaseDetailsRepository;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IssuesWithoutStoryLinkImplTest {

    @Mock
    private ConfigHelperService configHelperService;
    @Mock
    private CacheService cacheService;
    @Mock
    private IssueBacklogRepository issueBacklogRepository;
    @Mock
    private JiraIssueRepository jiraIssueRepository;
    @Mock
    private KpiHelperService kpiHelperService;
    @Mock
    private TestCaseDetailsRepository testCaseDetailsRepository;

    @InjectMocks
    private IssuesWithoutStoryLinkImpl issuesWithoutStoryLink;

    private Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
    private Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
    private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
    private KpiRequest kpiRequest;
    private KpiElement kpiElement;
    List<TestCaseDetails> totalTestCaseList = new ArrayList<>();

    @Before
    public void setup() {
        AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
                .newInstance();
        accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
        KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
        kpiRequest = kpiRequestFactory.findKpiRequest("kpi129");
        kpiRequest.setLabel("PROJECT");
        kpiElement = kpiRequest.getKpiList().get(0);
        totalTestCaseList = TestCaseDetailsDataFactory.newInstance().getTestCaseDetailsList();
        setMockProjectConfig();
        FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
                .newInstance("/json/default/scrum_project_field_mappings.json");
        FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
        fieldMapping.setJiraStoryIdentification(Arrays.asList("Story"));
        fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
        configHelperService.setProjectConfigMap(projectConfigMap);
        configHelperService.setFieldMappingMap(fieldMappingMap);
    }

    private void setMockProjectConfig() {
        ProjectBasicConfig projectConfig = new ProjectBasicConfig();
        projectConfig.setId(new ObjectId("6335363749794a18e8a4479b"));
        projectConfig.setProjectName("Scrum Project");
        projectConfigMap.put(projectConfig.getProjectName(), projectConfig);
    }

    @Test
    public void testFetchKPIDataFromDbForTestWithoutStory() throws ApplicationException {
        TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
                accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
        List<Node> leafNodeList = new ArrayList<>();
        leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList);
        when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
        when(testCaseDetailsRepository.findNonRegressionTestDetails(Mockito.anyMap(),
                Mockito.anyMap(), Mockito.anyString())).thenReturn(totalTestCaseList);
        Map<String, Object> defectDataListMap = issuesWithoutStoryLink.fetchKPIDataFromDbForTestWithoutStory(leafNodeList);
        assertNotNull(defectDataListMap);
    }

    @Test
    public void testFetchKPIDataFromDbForDefectsWithoutStoryLink() throws ApplicationException {
        TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
                accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
        List<Node> leafNodeList = new ArrayList<>();
        leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList);
        when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
        Map<String, Object> defectDataListMap = issuesWithoutStoryLink.fetchKPIDataFromDbForDefectsWithoutStoryLink(leafNodeList);
        assertNotNull(defectDataListMap);
    }

    @Test
    public void testFetchKPIDataFromDb() throws ApplicationException {
        TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
                accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
        List<Node> leafNodeList = new ArrayList<>();
        leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList);
        when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
        Map<String, Object> defectDataListMap = issuesWithoutStoryLink.fetchKPIDataFromDb(leafNodeList,
                null, null, kpiRequest);
        assertNotNull(defectDataListMap);
    }

    @Test
    public void testGetQualifierType() {
        assertThat(issuesWithoutStoryLink.getQualifierType(), equalTo("ISSUES_WITHOUT_STORY_LINK"));
    }

    @Test
    public void testCalculateKPIMetrics() {
        assertThat("Total Defects value :", issuesWithoutStoryLink.calculateKPIMetrics(null), equalTo(null));
    }

    @Test
    public void testGetKpiData() throws ApplicationException {
        List<Node> leafNodeList = new ArrayList<>();
        TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
                accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
        treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
            if (Filters.getFilter(k) == Filters.SPRINT) {
                leafNodeList.addAll(v);
            }
        });
        when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
        KpiElement kpiData = issuesWithoutStoryLink.getKpiData(kpiRequest, kpiElement, treeAggregatorDetail);
        assertEquals(null, kpiData.getValue());
    }
}
