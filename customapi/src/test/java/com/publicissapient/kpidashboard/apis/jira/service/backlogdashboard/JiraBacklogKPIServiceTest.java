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
package com.publicissapient.kpidashboard.apis.jira.service.backlogdashboard;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;
import com.publicissapient.kpidashboard.common.model.zephyr.TestCaseDetails;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;

@ExtendWith(SpringExtension.class)
public class JiraBacklogKPIServiceTest {

    @InjectMocks
    JiraBacklogKPIServiceTestImpl jiraKPIService;

    @Mock
    private CustomApiConfig customApiConfig;

    private Map<String, String> aggregationCriteriaMap;
    @Mock
    private JiraBacklogServiceR jiraService;

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

    @Test
    public void testPopulateIterationDataForTestWithoutStory() {
        TestCaseDetails testCaseDetails = mock(TestCaseDetails.class);
        when(testCaseDetails.getNumber()).thenReturn("123");
        when(testCaseDetails.getName()).thenReturn("sprint1");
        List<IterationKpiModalValue> overAllmodalValues = new ArrayList<>();
        jiraKPIService.populateIterationDataForTestWithoutStory(overAllmodalValues, testCaseDetails);
        assertNotNull(overAllmodalValues);
    }

    @Test
    public void testPopulateIterationDataForDefectWithoutStory() {
        JiraIssue jiraIssue = mock(JiraIssue.class);
        when(jiraIssue.getNumber()).thenReturn("123");
        when(jiraIssue.getName()).thenReturn("sprint1");
        when(jiraIssue.getUrl()).thenReturn("abc.com");
        List<IterationKpiModalValue> overAllmodalValues = new ArrayList<>();
        jiraKPIService.populateIterationDataForDefectWithoutStory(overAllmodalValues, jiraIssue);
        assertNotNull(overAllmodalValues);
    }

    @Test
    public void testGetJiraIssuesCustomHistoryFromBaseClass_WithNoParam() {
        List<JiraIssueCustomHistory> jiraIssueCustomHistories = getJiraIssueCustomHistories();
        when(jiraService.getJiraIssuesCustomHistoryForCurrentSprint()).thenReturn(jiraIssueCustomHistories);
        assertNotNull(jiraKPIService.getJiraIssuesCustomHistoryFromBaseClass());
    }

    @Test
    public void testGetBackLogJiraIssuesFromBaseClass() {
        List<JiraIssue> jiraIssues = getJiraIssues();
        when(jiraService.getJiraIssuesForCurrentSprint()).thenReturn(jiraIssues);
        assertNotNull(jiraKPIService.getBackLogJiraIssuesFromBaseClass());
    }

    @Test
    public void testGetJiraIssueReleaseStatus() {
        JiraIssueReleaseStatus jiraIssueReleaseStatus = new JiraIssueReleaseStatus();
        when(jiraService.getJiraIssueReleaseForProject()).thenReturn(jiraIssueReleaseStatus);
        assertNotNull(jiraKPIService.getJiraIssueReleaseStatus());
    }

    @Test
    public void testPopulateBackLogData() {
        JiraIssue jiraIssue = new JiraIssue();
        jiraIssue.setTypeName("bug");
        jiraIssue.setUrl("abc");
        jiraIssue.setNumber("1");
        jiraIssue.setPriority("5");
        jiraIssue.setName("Testing");
        List<IterationKpiModalValue> overAllmodalValues = new ArrayList<>();
        List<IterationKpiModalValue> modalValues = new ArrayList<>();
        jiraKPIService.populateBackLogData(overAllmodalValues, modalValues, jiraIssue);
        assertNotNull(modalValues);
        assertNotNull(overAllmodalValues);
    }

    public static class JiraBacklogKPIServiceTestImpl extends JiraBacklogKPIService {

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