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

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;

@ExtendWith(SpringExtension.class)
public class JiraReleaseKPIServiceTest {

    @InjectMocks
    JiraReleaseKPIServiceTestImpl jiraKPIService;

    @Mock
    private CustomApiConfig customApiConfig;

    private Map<String, String> aggregationCriteriaMap;
    @Mock
    private JiraReleaseServiceR jiraService;

    private static List<JiraIssueCustomHistory> getJiraIssueCustomHistories() {
        JiraIssueCustomHistory issueCustomHistory = new JiraIssueCustomHistory();
        issueCustomHistory.setStoryID("DTS-123");
        List<JiraIssueCustomHistory> jiraIssueCustomHistories = new ArrayList<>();
        jiraIssueCustomHistories.add(issueCustomHistory);
        return jiraIssueCustomHistories;
    }

    private static List<JiraIssue> getJiraIssues() {
        JiraIssue jiraIssue = new JiraIssue();
        jiraIssue.setNumber("123");
        List<JiraIssue> jiraIssues = new ArrayList<>();
        jiraIssues.add(jiraIssue);
        return jiraIssues;
    }

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);

        aggregationCriteriaMap = new HashMap<>();
        aggregationCriteriaMap.put("kpi1", Constant.PERCENTILE);
        aggregationCriteriaMap.put("kpi2", Constant.MEDIAN);
        aggregationCriteriaMap.put("kpi3", Constant.AVERAGE);
        aggregationCriteriaMap.put("kpi4", Constant.SUM);
    }

    private List<Map<String, Long>> createAggregationInputData1() {
        List<Map<String, Long>> aggregatedValueList = new ArrayList<>();
        Map<String, Long> aggregatedValuesMap1 = new HashMap<>();
        aggregatedValuesMap1.put("Bug", 1L);
        Map<String, Long> aggregatedValuesMap2 = new HashMap<>();
        aggregatedValuesMap2.put("Bug", 4L);
        Map<String, Long> aggregatedValuesMap3 = new HashMap<>();
        aggregatedValuesMap3.put("Bug", 3L);
        Map<String, Long> aggregatedValuesMap4 = new HashMap<>();
        aggregatedValuesMap4.put("Bug", 0L);
        Map<String, Long> aggregatedValuesMap5 = new HashMap<>();
        aggregatedValuesMap5.put("Bug", 2L);
        Map<String, Long> aggregatedValuesMap6 = new HashMap<>();
        aggregatedValuesMap5.put("Bug", 6L);

        aggregatedValueList.add(aggregatedValuesMap1);
        aggregatedValueList.add(aggregatedValuesMap2);
        aggregatedValueList.add(aggregatedValuesMap3);
        aggregatedValueList.add(aggregatedValuesMap4);
        aggregatedValueList.add(aggregatedValuesMap5);
        aggregatedValueList.add(aggregatedValuesMap6);
        return aggregatedValueList;
    }

    @Test
    public void testGetJiraIssuesCustomHistoryFromBaseClass_WithNoParam() {
        List<JiraIssueCustomHistory> jiraIssueCustomHistories = getJiraIssueCustomHistories();
        when(jiraService.getJiraIssuesCustomHistoryForCurrentSprint()).thenReturn(jiraIssueCustomHistories);
        assertNotNull(jiraKPIService.getJiraIssuesCustomHistoryFromBaseClass());
    }

    @Test
    public void testGetBaseReleaseJiraIssues() {
        List<JiraIssue> jiraIssues = getJiraIssues();
        when(jiraService.getJiraIssuesForSelectedRelease()).thenReturn(jiraIssues);
        assertNotNull(jiraKPIService.getBaseReleaseJiraIssues());
    }

    @Test
    public void testGetBaseReleaseSubTask() {
        JiraIssue jiraIssue = new JiraIssue();
        jiraIssue.setNumber("123");
        Set<JiraIssue> jiraIssues = new HashSet<>();
        jiraIssues.add(jiraIssue);
        when(jiraService.getSubTaskDefects()).thenReturn(jiraIssues);
        assertNotNull(jiraKPIService.getBaseReleaseSubTask());
    }

    @Test
    public void testGetFilteredReleaseJiraIssuesFromBaseClass_EmptyDefectList() {
        FieldMapping fieldMapping = mock(FieldMapping.class);
        when(fieldMapping.getJiradefecttype()).thenReturn(List.of("BUG"));
        List<JiraIssue> jiraIssues = getJiraIssues();
        when(jiraService.getJiraIssuesForSelectedRelease()).thenReturn(jiraIssues);
        assertNotNull(jiraKPIService.getFilteredReleaseJiraIssuesFromBaseClass(fieldMapping));
    }

    @Test
    public void testGetFilteredReleaseJiraIssuesFromBaseClass_WithDefectList() {
        FieldMapping fieldMapping = mock(FieldMapping.class);
        when(fieldMapping.getJiradefecttype()).thenReturn(List.of("BUG"));
        List<JiraIssue> jiraIssues = getJiraIssues();
        JiraIssue defectIssue = new JiraIssue();
        defectIssue.setNumber("123");
        Set<JiraIssue> defectIssues = new HashSet<>();
        defectIssues.add(defectIssue);
        when(jiraService.getJiraIssuesForSelectedRelease()).thenReturn(jiraIssues);
        when(jiraService.getSubTaskDefects()).thenReturn(defectIssues);
        assertNotNull(jiraKPIService.getFilteredReleaseJiraIssuesFromBaseClass(fieldMapping));
    }

    @Test
    public void testGetJiraIssueReleaseStatus() {
        JiraIssueReleaseStatus jiraIssueReleaseStatus = new JiraIssueReleaseStatus();
        when(jiraService.getJiraIssueReleaseForProject()).thenReturn(jiraIssueReleaseStatus);
        assertNotNull(jiraKPIService.getJiraIssueReleaseStatus());
    }

    @Test
    public void testGetReleaseList() {
        List<String> releaseList = List.of("release8.1");
        when(jiraService.getReleaseList()).thenReturn(releaseList);
        assertNotNull(jiraKPIService.getReleaseList());
    }

    public static class JiraReleaseKPIServiceTestImpl extends JiraReleaseKPIService {

        @Override
        public String getQualifierType() {
            return null;
        }

        @Override
        public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node filteredNode) throws ApplicationException {
            return null;
        }

        @Override
        public Map<String, Object> fetchKPIDataFromDb(Node leafNode, String startDate, String endDate, KpiRequest kpiRequest) {
            return null;
        }


    }


}