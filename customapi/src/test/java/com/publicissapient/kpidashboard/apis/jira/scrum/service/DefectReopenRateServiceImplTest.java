package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
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
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefectReopenRateServiceImplTest {

  @Mock
  private JiraIssueRepository jiraIssueRepository;

  @Mock
  private ConfigHelperService configHelperService;

  @Mock
  private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

  @InjectMocks
  DefectReopenRateServiceImpl defectReopenRateService;

  List<String> testJiraNumberList = Arrays.asList("DTS-18868", "DTS-17908");
  private KpiRequest kpiRequest;
  private KpiElement kpiElement;
  private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
  List<JiraIssue> totalJiraIssueList = new ArrayList<>();
  List<JiraIssueCustomHistory> totalJiraIssueHistoryList = new ArrayList<>();
  public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
  public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();

  @Before
  public void setup() {
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
    when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
    KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
    kpiRequest = kpiRequestFactory.findKpiRequest("kpi134");
    kpiRequest.setLabel("PROJECT");
    kpiElement = kpiRequest.getKpiList().get(0);
    AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
        .newInstance();
    accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
    totalJiraIssueList = JiraIssueDataFactory.newInstance().findIssueByNumberList(testJiraNumberList);
    totalJiraIssueHistoryList = Arrays.asList(JiraIssueHistoryDataFactory.newInstance().getJiraIssueCustomHistory()
        .stream().filter(issueHistory -> issueHistory.getStoryID().equals("DTS-17908")).findFirst().get());
  }

  @Test
  public void testGetKpiData() throws ApplicationException {
    TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
        accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
    Mockito.doReturn(totalJiraIssueList).when(jiraIssueRepository).findIssuesByFilterAndProjectMapFilter(anyMap(), anyMap(), anyMap());
    Mockito.doReturn(totalJiraIssueHistoryList).when(jiraIssueCustomHistoryRepository).findByFilterAndFromStatusMap(anyMap(), anyMap());
    try {
      KpiElement kpiElement = defectReopenRateService.getKpiData(kpiRequest,
          kpiRequest.getKpiList().get(0), treeAggregatorDetail);
      assertNotNull(kpiElement);
      assertNotNull(kpiElement.getTrendValueList());
      assertNotNull(((DataCount)kpiElement.getTrendValueList()).getValue());
    } catch (ApplicationException applicationException) {

    }
  }

  @Test
  public void testCalculateKPIMetrics() {
    assertNull(defectReopenRateService.calculateKPIMetrics(null));
  }

  @Test
  public void testGetQualifierType() {
    assertEquals("DEFECT_REOPEN_RATE", defectReopenRateService.getQualifierType() );
  }

}
