package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraServiceR;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

@RunWith(MockitoJUnitRunner.class)
public class DefectReopenRateServiceImplTest {

	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	@InjectMocks
	DefectReopenRateServiceImpl defectReopenRateService;
	List<JiraIssue> totalJiraIssueList = new ArrayList<>();
	List<JiraIssueCustomHistory> totalJiraIssueHistoryList = new ArrayList<>();
	@Mock
	private JiraIssueRepository jiraIssueRepository;
	@Mock
	private JiraServiceR jiraService;
	@Mock
	private KpiHelperService kpiHelperService;
	@Mock
	private ConfigHelperService configHelperService;
	@Mock
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	private KpiRequest kpiRequest;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();

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
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance("/json/default/kpi_request.json");
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi137");
		kpiRequest.setLabel("PROJECT");
		kpiRequest.setDuration("WEEKS");
		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory
				.newInstance("/json/default/iteration/jira_issues.json");
		JiraIssueHistoryDataFactory jiraIssueHistoryDataFactory = JiraIssueHistoryDataFactory
				.newInstance("/json/default/iteration/jira_issue_custom_history.json");
		totalJiraIssueList = jiraIssueDataFactory.getJiraIssues();
		totalJiraIssueHistoryList = jiraIssueHistoryDataFactory.getUniqueJiraIssueCustomHistory();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetKpiData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		Mockito.doReturn(totalJiraIssueList).when(jiraIssueRepository).findIssuesByFilterAndProjectMapFilter(anyMap(),
				anyMap());
		Mockito.doReturn(totalJiraIssueHistoryList).when(jiraIssueCustomHistoryRepository)
				.findByFilterAndFromStatusMap(anyMap(), anyMap());
		try {

			KpiElement kpiElement = defectReopenRateService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertNotNull(kpiElement);
			assertNotNull(kpiElement.getTrendValueList());
			Object value = ((DataCount) kpiElement.getTrendValueList()).getValue();
			List<IterationKpiValue> iterationKpiValues = (List<IterationKpiValue>) value;
			IterationKpiValue iterationKpiValue = iterationKpiValues.stream()
					.filter(kpiValue -> "Overall".equals(kpiValue.getFilter1())).findFirst().get();
			assertNotNull(iterationKpiValue);
			assertEquals(Optional.of(3.0d).get(), iterationKpiValue.getData().get(0).getValue());
		} catch (ApplicationException applicationException) {

		}
	}

	@Test
	public void testCalculateKPIMetrics() {
		assertNull(defectReopenRateService.calculateKPIMetrics(null));
	}

	@Test
	public void testGetQualifierType() {
		assertEquals("DEFECT_REOPEN_RATE", defectReopenRateService.getQualifierType());
	}

}
