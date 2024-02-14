/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package com.publicissapient.kpidashboard.apis.jira.service.releasedashboard;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.HierachyLevelFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueReleaseStatusDataFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.factory.JiraNonTrendKPIServiceFactory;
import com.publicissapient.kpidashboard.apis.jira.scrum.service.release.ReleaseBurnUpServiceImpl;
import com.publicissapient.kpidashboard.apis.jira.service.NonTrendKPIService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueReleaseStatusRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class JiraReleaseServiceRTest {

    public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
    public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
    @Mock
    KpiHelperService kpiHelperService;
    @Mock
    FilterHelperService filterHelperService;
    @Mock
    SprintRepository sprintRepository;
    @Mock
    JiraIssueRepository jiraIssueRepository;
    @Mock
    JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
    @Mock
    ConfigHelperService configHelperService;
    @InjectMocks
    @Spy
    private JiraReleaseServiceR jiraServiceR;
    @Mock
    private CacheService cacheService;
    @Mock
    private ReleaseBurnUpServiceImpl releaseBurnupService;
    @SuppressWarnings("rawtypes")
    @Mock
    private List<JiraReleaseKPIService> services;
    private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
    private String[] projectKey;
    private List<HierarchyLevel> hierarchyLevels = new ArrayList<>();
    private Map<String, JiraReleaseKPIService> jiraServiceCache = new HashMap<>();
    @Mock
    private JiraNonTrendKPIServiceFactory jiraKPIServiceFactory;
    @Mock
    private UserAuthorizedProjectsService authorizedProjectsService;
    @Mock
    private JiraIssueReleaseStatusRepository jiraIssueReleaseStatusRepository;

    @Before
    public void setup() throws ApplicationException {
        MockitoAnnotations.openMocks(this);
        List<NonTrendKPIService> mockServices = Arrays.asList(releaseBurnupService);
        JiraNonTrendKPIServiceFactory serviceFactory = JiraNonTrendKPIServiceFactory.builder().services(mockServices).build();
        doReturn(KPICode.RELEASE_BURNUP.name()).when(releaseBurnupService).getQualifierType();
        doReturn(new KpiElement()).when(releaseBurnupService).getKpiData(any(), any(), any());
        serviceFactory.initMyServiceCache();

        AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
                .newInstance("/json/default/account_hierarchy_filter_data_release.json");
        accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
        HierachyLevelFactory hierachyLevelFactory = HierachyLevelFactory.newInstance();
        hierarchyLevels = hierachyLevelFactory.getHierarchyLevels();

        ProjectBasicConfig projectConfig = new ProjectBasicConfig();
        projectConfig.setId(new ObjectId("6335363749794a18e8a4479b"));
        projectConfig.setProjectName("Scrum Project");
        projectConfigMap.put(projectConfig.getProjectName(), projectConfig);

        FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
                .newInstance("/json/default/scrum_project_field_mappings.json");
        FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
        fieldMapping.setJiraSubTaskDefectType(Arrays.asList("Bug"));
        fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
        when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);

        when(filterHelperService.getHierarachyLevelId(5, "release", false)).thenReturn("release");

    }

    @org.junit.Test(expected = Exception.class)
    public void testProcessException() throws Exception {

        KpiRequest kpiRequest = createKpiRequest(6);

        jiraServiceR.process(kpiRequest);

    }

    @org.junit.Test
    public void TestProcess_pickFromCache() throws Exception {

        KpiRequest kpiRequest = createKpiRequest(5);

        when(cacheService.getFromApplicationCache(any(), Mockito.anyString(), any(), ArgumentMatchers.anyList()))
                .thenReturn(new ArrayList<KpiElement>());
        when(authorizedProjectsService.ifSuperAdminUser()).thenReturn(true);
        when(kpiHelperService.getAuthorizedFilteredList(any(), any())).thenReturn(accountHierarchyDataList);
        when(kpiHelperService.getProjectKeyCache(any(), any())).thenReturn(kpiRequest.getIds());
        when(cacheService.cacheAccountHierarchyData()).thenReturn(accountHierarchyDataList);

        List<KpiElement> resultList = jiraServiceR.process(kpiRequest);

        assertEquals(0, resultList.size());
    }

    @SuppressWarnings("unchecked")
    @org.junit.Test
    public void TestProcess() throws Exception {

        KpiRequest kpiRequest = createKpiRequest(5);

        @SuppressWarnings("rawtypes")
        JiraReleaseKPIService mcokAbstract = releaseBurnupService;
        jiraServiceCache.put(KPICode.RELEASE_BURNUP.name(), mcokAbstract);

        try (MockedStatic<JiraNonTrendKPIServiceFactory> utilities = Mockito.mockStatic(JiraNonTrendKPIServiceFactory.class)) {
            utilities.when((MockedStatic.Verification) JiraNonTrendKPIServiceFactory.getJiraKPIService(KPICode.RELEASE_BURNUP.name()))
                    .thenReturn(mcokAbstract);
        }

        Map<String, Integer> map = new HashMap<>();
        Map<String, HierarchyLevel> hierarchyMap = hierarchyLevels.stream()
                .collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));
        hierarchyMap.entrySet().stream().forEach(k -> map.put(k.getKey(), k.getValue().getLevel()));
        when(filterHelperService.getHierarchyIdLevelMap(false)).thenReturn(map);
        when(cacheService.getFromApplicationCache(any(), any(), any(), any())).thenReturn(null);
        when(cacheService.cacheAccountHierarchyData()).thenReturn(accountHierarchyDataList);
        when(authorizedProjectsService.getProjectKey(accountHierarchyDataList, kpiRequest)).thenReturn(projectKey);
        when(authorizedProjectsService.filterProjects(any())).thenReturn(accountHierarchyDataList.stream().filter(s -> s.getLeafNodeId().equalsIgnoreCase("38296_Scrum Project_6335363749794a18e8a4479b")).collect(Collectors.toList()));
        when(filterHelperService.getFirstHierarachyLevel()).thenReturn("hierarchyLevelOne");
        when(cacheService.cacheFieldMappingMapData()).thenReturn(fieldMappingMap);
        when(kpiHelperService.getAuthorizedFilteredList(any(), any())).thenReturn(accountHierarchyDataList);
        when(kpiHelperService.getProjectKeyCache(any(), any())).thenReturn(kpiRequest.getIds());
        JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
        when(jiraIssueRepository.findByBasicProjectConfigIdAndReleaseVersionsReleaseNameIn(anyString(), anyList()))
                .thenReturn(jiraIssueDataFactory.getJiraIssues());
        when(jiraIssueRepository.findByBasicProjectConfigIdAndDefectStoryIDInAndOriginalTypeIn(anyString(), anySet(),
                anyList())).thenReturn(new HashSet<>());
        JiraIssueReleaseStatusDataFactory jiraIssueReleaseStatusDataFactory = JiraIssueReleaseStatusDataFactory
                .newInstance("/json/default/jira_issue_release_status.json");
        when(jiraIssueReleaseStatusRepository.findByBasicProjectConfigId((anyString())))
                .thenReturn(jiraIssueReleaseStatusDataFactory.getJiraIssueReleaseStatusList().get(0));
        List<KpiElement> resultList = jiraServiceR.process(kpiRequest);

        resultList.forEach(k -> {

            KPICode kpi = KPICode.getKPI(k.getKpiId());

            switch (kpi) {

                case RELEASE_BURNUP:
                    assertThat("Kpi Name :", k.getKpiName(), equalTo("RELEASE_BURNUP"));
                    break;

                default:
                    break;
            }

        });

    }

    @org.junit.Test
    public void TestProcessWithApplicationException() throws Exception {

        KpiRequest kpiRequest = createKpiRequest(5);

        @SuppressWarnings("rawtypes")
        JiraReleaseKPIService mcokAbstract = releaseBurnupService;
        jiraServiceCache.put(KPICode.RELEASE_BURNUP.name(), mcokAbstract);

        try (MockedStatic<JiraNonTrendKPIServiceFactory> utilities = Mockito.mockStatic(JiraNonTrendKPIServiceFactory.class)) {
            utilities.when((MockedStatic.Verification) JiraNonTrendKPIServiceFactory.getJiraKPIService(KPICode.RELEASE_BURNUP.name()))
                    .thenReturn(mcokAbstract);
        }

        doThrow(ApplicationException.class).when(releaseBurnupService).getKpiData(any(), any(), any());
        Map<String, Integer> map = new HashMap<>();
        Map<String, HierarchyLevel> hierarchyMap = hierarchyLevels.stream()
                .collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));
        hierarchyMap.entrySet().stream().forEach(k -> map.put(k.getKey(), k.getValue().getLevel()));
        when(filterHelperService.getHierarchyIdLevelMap(false)).thenReturn(map);
        when(cacheService.getFromApplicationCache(any(), any(), any(), any())).thenReturn(null);
        when(cacheService.cacheAccountHierarchyData()).thenReturn(accountHierarchyDataList);
        when(authorizedProjectsService.getProjectKey(accountHierarchyDataList, kpiRequest)).thenReturn(projectKey);
        when(authorizedProjectsService.filterProjects(any())).thenReturn(accountHierarchyDataList.stream().filter(s -> s.getLeafNodeId().equalsIgnoreCase("38296_Scrum Project_6335363749794a18e8a4479b")).collect(Collectors.toList()));
        when(filterHelperService.getFirstHierarachyLevel()).thenReturn("hierarchyLevelOne");
        when(cacheService.cacheFieldMappingMapData()).thenReturn(fieldMappingMap);
        when(kpiHelperService.getAuthorizedFilteredList(any(), any())).thenReturn(accountHierarchyDataList);
        when(kpiHelperService.getProjectKeyCache(any(), any())).thenReturn(kpiRequest.getIds());
        List<KpiElement> resultList = jiraServiceR.process(kpiRequest);

        resultList.forEach(k -> {

            KPICode kpi = KPICode.getKPI(k.getKpiId());

            switch (kpi) {

                case RELEASE_BURNUP:
                    assertThat("Kpi Name :", k.getKpiName(), equalTo("RELEASE_BURNUP"));
                    break;

                default:
                    break;
            }

        });

    }


    private KpiRequest createKpiRequest(int level) {
        KpiRequest kpiRequest = new KpiRequest();
        List<KpiElement> kpiList = new ArrayList<>();

        addKpiElement(kpiList, KPICode.RELEASE_BURNUP.getKpiId(), KPICode.RELEASE_BURNUP.name(),
                "Release", "");
        kpiRequest.setLevel(level);
        kpiRequest.setIds(new String[]{"38296_Scrum Project_6335363749794a18e8a4479b"});
        kpiRequest.setKpiList(kpiList);
        kpiRequest.setRequestTrackerId();
        kpiRequest.setLabel("release");
        Map<String, List<String>> s = new HashMap<>();
        s.put("release", Arrays.asList("38296_Scrum Project_6335363749794a18e8a4479b"));
        kpiRequest.setSelectedMap(s);
        kpiRequest.setSprintIncluded(Arrays.asList());
        return kpiRequest;
    }

    private void addKpiElement(List<KpiElement> kpiList, String kpiId, String kpiName, String category,
                               String kpiUnit) {
        KpiElement kpiElement = new KpiElement();
        kpiElement.setKpiId(kpiId);
        kpiElement.setKpiName(kpiName);
        kpiElement.setKpiCategory(category);
        kpiElement.setKpiUnit(kpiUnit);
        kpiElement.setKpiSource("Jira");
        kpiElement.setGroupId(1);
        kpiElement.setMaxValue("500");
        kpiElement.setChartType("gaugeChart");
        kpiList.add(kpiElement);
    }


}