package com.publicissapient.kpidashboard.apis.zephyr.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.apis.data.*;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.model.*;
import com.publicissapient.kpidashboard.common.model.zephyr.TestCaseDetails;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.zephyr.TestCaseDetailsRepository;

@RunWith(MockitoJUnitRunner.class)
public class TestWithoutStoryServiceImplTest {
    @Mock
    private JiraIssueRepository jiraIssueRepository;

    @Mock
    private ConfigHelperService configHelperService;

    @Mock
    CacheService cacheService;

    @InjectMocks
    private TestWithoutStoryServiceImpl testWithoutStoryService;
    
	
	@Mock
	TestCaseDetailsRepository testCaseDetailsRepository;

    private List<JiraIssue> totalTestCase;
    private List<JiraIssue> stories;

    private KpiRequest kpiRequest;
    private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
    private Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
    private List<JiraIssue> storyList = new ArrayList<>();
    private List<TestCaseDetails> totalTestCaseList;
    private KpiElement kpiElement;

    @Before
    public void setup() {
        JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
        totalTestCase = jiraIssueDataFactory.getJiraIssues();

        jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
        stories = jiraIssueDataFactory.getJiraIssues();

        AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
                .newInstance();
        accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
        KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
        kpiRequest = kpiRequestFactory.findKpiRequest("kpi79");
        kpiElement = kpiRequest.getKpiList().get(0);
        FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory.newInstance("/json/default/scrum_project_field_mappings.json");
        FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
        fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
        when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
        when(jiraIssueRepository.findIssuesBySprintAndType(Mockito.anyMap(),Mockito.anyMap())).thenReturn(stories);
        storyList = JiraIssueDataFactory.newInstance().getJiraIssues();
        totalTestCaseList = TestCaseDetailsDataFactory.newInstance().getTestCaseDetailsList();

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
        KpiElement kpiData = testWithoutStoryService.getKpiData(kpiRequest, kpiElement, treeAggregatorDetail);
        assertEquals(Long.parseLong("0"),kpiData.getValue());
    }

    @Test
    public void fetchKPIDataFromDb() throws ApplicationException {
        List<Node> leafNodeList = new ArrayList<>();
        TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
                accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
        treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
            if (Filters.getFilter(k) == Filters.SPRINT) {
                leafNodeList.addAll(v);
            }
        });
        when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
        when(jiraIssueRepository.findIssuesBySprintAndType(Mockito.anyMap(), Mockito.anyMap())).thenReturn(storyList);
        when(testCaseDetailsRepository.findNonRegressionTestDetails(Mockito.anyMap(), Mockito.anyMap(),Mockito.anyString())).thenReturn(totalTestCaseList);
        Map<String, Object> map = testWithoutStoryService.fetchKPIDataFromDb(leafNodeList, null, null, kpiRequest);
        assertThat("output map size :", map.size(),
                equalTo(2));
    }

    @Test
    public void testGetQualifierType() {

        String result = testWithoutStoryService.getQualifierType();
        assertEquals(result, KPICode.TEST_WITHOUT_STORY.name());

    }

    @Test
    public void testCalculateKPIMetrics() {
        Map<String, Object> filterComponentIdWiseDefectMap = new HashMap<>();
        Double automatedValue = testWithoutStoryService.calculateKPIMetrics(filterComponentIdWiseDefectMap);
        assertThat("Automated Percentage value :", automatedValue, equalTo(null));
    }


}
