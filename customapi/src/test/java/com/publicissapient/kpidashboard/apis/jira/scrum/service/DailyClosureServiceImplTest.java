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
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.*;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.*;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DailyClosureServiceImplTest {
    private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
    private Map<String, Object> filterLevelMap;

    private KpiRequest kpiRequest;
    public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
    public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
    List<JiraIssue> jiraIssues = new ArrayList<>();
    List<JiraIssueCustomHistory> jiraIssuesCustomHistory = new ArrayList<>();
    List<SprintWiseStory> sprintWiseStoryList = new ArrayList<>();
    List<SprintDetails> sprintDetailsList = new ArrayList<>();

    @Mock
    JiraIssueRepository jiraIssueRepository;

    @Mock
    CacheService cacheService;

    @Mock
    ConfigHelperService configHelperService;

    @InjectMocks
    DailyClosureServiceImpl dailyClosureService;

    @Mock
    SprintRepository sprintRepository;

    @Mock
    JiraIssueCustomHistoryRepository jiraIssueHistoryRepository;

    @Before
    public void setup() {
        KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
        kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.DAILY_CLOSURES.getKpiId());
        kpiRequest.setLabel("PROJECT");
        SprintWiseStoryDataFactory sprintWiseStoryDataFactory = SprintWiseStoryDataFactory.newInstance();
        sprintWiseStoryList = sprintWiseStoryDataFactory.getSprintWiseStories();
        AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
                .newInstance();
        accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
        filterLevelMap = new LinkedHashMap<>();
        filterLevelMap.put("PROJECT", Filters.PROJECT);
        filterLevelMap.put("SPRINT", Filters.SPRINT);
        JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
        jiraIssues = jiraIssueDataFactory.getJiraIssues();
        SprintDetailsDataFactory sprintDetailsDataFactory = SprintDetailsDataFactory.newInstance();
        sprintDetailsList = sprintDetailsDataFactory.getSprintDetails();
        JiraIssueHistoryDataFactory jiraIssueHistoryDataFactory = JiraIssueHistoryDataFactory.newInstance();
        jiraIssuesCustomHistory = jiraIssueHistoryDataFactory.findIssueInTypeNames(Arrays.asList("Closed"));
        ProjectBasicConfig projectConfig = new ProjectBasicConfig();
        projectConfig.setId(new ObjectId("6335363749794a18e8a4479b"));
        projectConfig.setProjectName("Scrum Project");
        projectConfigMap.put(projectConfig.getProjectName(), projectConfig);
        FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
                .newInstance("/json/default/scrum_project_field_mappings.json");
        FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
        fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
        configHelperService.setProjectConfigMap(projectConfigMap);
        configHelperService.setFieldMappingMap(fieldMappingMap);
    }

    @After
    public void cleanup() {
        jiraIssueRepository.deleteAll();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFetchKPIDataFromDbData() throws ApplicationException {
        TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
                accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
        List<Node> leafNodeList = new ArrayList<>();
        leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList);
        String startDate = leafNodeList.get(0).getSprintFilter().getStartDate();
        String endDate = leafNodeList.get(leafNodeList.size() - 1).getSprintFilter().getEndDate();
        when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
        when(sprintRepository.findBySprintID(Mockito.anyString())).thenReturn(sprintDetailsList.get(0));
        when(jiraIssueRepository
                .findByNumberInAndBasicProjectConfigId(Mockito.anyList(), Mockito.anyString())).thenReturn(jiraIssues);
        when(jiraIssueHistoryRepository
                .findByStoryIDInAndBasicProjectConfigIdIn(Mockito.anyList(), Mockito.anyList())).thenReturn(jiraIssuesCustomHistory);
        Map<String, Object> defectDataListMap = dailyClosureService.fetchKPIDataFromDb(leafNodeList, startDate, endDate,
                kpiRequest);
        assertNotNull(defectDataListMap);
    }

    @Test
    public void testGetDSR() throws ApplicationException {
        TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
                accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
        Map<String, List<String>> maturityRangeMap = new HashMap<>();
        maturityRangeMap.put("defectSeepageRate", Arrays.asList("-30", "30-10", "10-5", "5-2", "2-"));
        when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
        String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
        when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
                .thenReturn(kpiRequestTrackerId);
        when(dailyClosureService.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
        try {
            KpiElement kpiElement = dailyClosureService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
                    treeAggregatorDetail);
            assertNotNull(kpiElement);
        } catch (ApplicationException enfe) {
        }
    }
}
