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
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.*;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RefinementRejectionRateServiceImplTest {
    public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
    public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();

    private static final String READY_FOR_REFINEMENT_ISSUE = "Ready For Refinement";
    private static final String ACCEPTED_IN_REFINEMENT_ISSUE = "Accepted In Refinement";
    private static final String REJECTED_IN_REFINEMENT_ISSUE = "Rejected In Refinement";
    private static final String UNASSIGNED_JIRA_ISSUE = "Unassigned Jira Issue";
    private static final String UNASSIGNED_JIRA_ISSUE_HISTORY = "Unassigned Jira Issue History";
    List<JiraIssue> jiraIssueList = new ArrayList<>();
    List<JiraIssueCustomHistory> unassignedJiraHistoryDataList = new ArrayList<>();
    private KpiRequest kpiRequest;
    private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
    Map<String, Object> refinementList = new HashMap<>();
    List<Node> leafNodeList = new ArrayList<>();
    Map<String, List<Map<String, Object>>> projectWiseMap = new HashMap<>();
    TreeAggregatorDetail treeAggregatorDetail;
    Map<String, Object> defaultMap = new HashMap<>();

    @Mock
    ConfigHelperService configHelperService;

    @Mock
    KpiHelperService kpiHelperService;

    @InjectMocks
    RefinementRejectionRateServiceImpl refinementRejectionRateService;

    @Mock
    CustomDateRange customDateRange;

    @Before
    public void setup() throws ApplicationException {
        KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
        kpiRequest = kpiRequestFactory.findKpiRequest("kpi137");
        kpiRequest.setLabel("PROJECT");
        List<KpiElement> kpiElementList = new ArrayList<>();
        kpiElementList.add(new KpiElement());
        kpiRequest.setKpiList(kpiElementList);
        AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
                .newInstance();
        accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

        leafNodeList = new ArrayList<>();
        treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
                accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
        treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {

            leafNodeList.addAll(v);
        });

        customDateRange = new CustomDateRange();
        customDateRange.setStartDate(LocalDate.now());
        customDateRange.setEndDate(LocalDate.now().minusDays(45));

        configHelperService.setProjectConfigMap(projectConfigMap);
        configHelperService.setFieldMappingMap(fieldMappingMap);

        jiraIssueList = JiraIssueDataFactory.newInstance().getJiraIssues();
        unassignedJiraHistoryDataList = JiraIssueHistoryDataFactory.newInstance().getJiraIssueCustomHistory();

        refinementList = new HashMap<>();
        refinementList.put(UNASSIGNED_JIRA_ISSUE, jiraIssueList);
        refinementList.put(UNASSIGNED_JIRA_ISSUE_HISTORY, unassignedJiraHistoryDataList);


        defaultMap.put(READY_FOR_REFINEMENT_ISSUE, jiraIssueList);
        defaultMap.put(REJECTED_IN_REFINEMENT_ISSUE, jiraIssueList);
        defaultMap.put(ACCEPTED_IN_REFINEMENT_ISSUE, jiraIssueList);

        FieldMapping fieldMapping = new FieldMapping();
        fieldMappingMap.put(new ObjectId(), fieldMapping);

        List<Map<String, Object>> dataList = new ArrayList<>();
        for (String key : defaultMap.keySet()) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put(key, defaultMap.get(key));
            dataList.add(dataMap);
        }

        projectWiseMap.put(leafNodeList.get(0).getId(), dataList);

        unassignedJiraHistoryDataList = JiraIssueHistoryDataFactory.newInstance().getJiraIssueCustomHistory();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFetchKPIDataFromDbData() throws ApplicationException {

        when(kpiHelperService.fetchUnAssignedJiraIssues(leafNodeList, customDateRange.getStartDate().toString(), customDateRange.getEndDate().toString())).thenReturn(jiraIssueList);
        when(kpiHelperService.fetchJiraCustomHistory(leafNodeList, jiraIssueList)).thenReturn(unassignedJiraHistoryDataList);
        Map<String, Object> refinementList = refinementRejectionRateService.fetchKPIDataFromDb(leafNodeList, customDateRange.getStartDate().toString(),
                customDateRange.getEndDate().toString(), kpiRequest);
        assertNotNull(refinementList);
        assertNotNull(refinementList.get(UNASSIGNED_JIRA_ISSUE));
        assertNotNull(refinementList.get(UNASSIGNED_JIRA_ISSUE_HISTORY));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetKpiData() throws ApplicationException {
        when(kpiHelperService.getProjectWiseDataMap(Mockito.any(), Mockito.anyMap())).thenReturn(projectWiseMap);
        when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
        KpiElement kpiElement = refinementRejectionRateService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
                treeAggregatorDetail);

        assertNotNull(kpiElement);
        assertNotNull(kpiElement.getTrendValueList());

    }

    @Test
    public void testGetQualifierType() {
        assertThat(refinementRejectionRateService.getQualifierType(), equalTo("REFINEMENT_REJECTION_RATE"));
    }
}
