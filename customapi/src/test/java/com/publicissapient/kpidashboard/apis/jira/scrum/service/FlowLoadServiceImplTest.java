package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraServiceR;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;

@RunWith(MockitoJUnitRunner.class)
public class FlowLoadServiceImplTest {
	private static final String ISSUE_BACKLOG_HISTORY = "Issue Backlog History";
	@Mock
	CacheService cacheService;
	@Mock
	private JiraServiceR jiraService;
	@Mock
	private CustomApiConfig customApiConfig;
	List<Node> leafNodeList = new ArrayList<>();
	TreeAggregatorDetail treeAggregatorDetail;
	List<JiraIssueCustomHistory> issueBacklogHistoryDataList = new ArrayList<>();
	@InjectMocks
	private FlowLoadServiceImpl flowLoadService;
	@Mock
	private ConfigHelperService configHelperService;
	private Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	private KpiRequest kpiRequest;

	@Before
	public void setUp() throws ApplicationException {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		List<AccountHierarchyData> accountHierarchyDataList;

		kpiRequest = kpiRequestFactory.findKpiRequest("kpi148");
		kpiRequest.setLabel("PROJECT");
		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();

		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(new ObjectId("6335363749794a18e8a4479b"), fieldMapping);
		configHelperService.setFieldMappingMap(fieldMappingMap);

		leafNodeList = new ArrayList<>();
		treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest, accountHierarchyDataList,
				new ArrayList<>(), "hierarchyLevelOne", 4);
		treeAggregatorDetail.getMapOfListOfProjectNodes().forEach((k, v) -> {
			leafNodeList.addAll(v);
		});

		issueBacklogHistoryDataList = JiraIssueHistoryDataFactory.newInstance().getJiraIssueCustomHistory();
		when(jiraService.getJiraIssuesCustomHistoryForCurrentSprint()).thenReturn(issueBacklogHistoryDataList);
		when(jiraService.getJiraIssueReleaseForProject()).thenReturn(new JiraIssueReleaseStatus());

	}

	@Test
	public void getQualifierType() {
		assertThat(flowLoadService.getQualifierType(), equalTo(KPICode.FLOW_LOAD.name()));
	}

	@Test
	public void getKpiData() throws ApplicationException {
		when(customApiConfig.getFlowKpiMonthCount()).thenReturn(12);
		String kpiRequestTrackerId = "Jira-Excel-QADD-track001";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		List<JiraIssueCustomHistory> expectedResult = new ArrayList<>();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(ISSUE_BACKLOG_HISTORY, issueBacklogHistoryDataList);
		List<Map<String, Object>> typeCountMap = new ArrayList<>();
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		KpiElement responseKpiElement = flowLoadService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
				treeAggregatorDetail);

		assertNotNull(responseKpiElement);
		assertNotNull(responseKpiElement.getTrendValueList());
		assertEquals(responseKpiElement.getKpiId(), kpiRequest.getKpiList().get(0).getKpiId());
	}

	@Test
	public void testGetQualifierType() {
		assertThat(flowLoadService.getQualifierType(), equalTo("FLOW_LOAD"));
	}
}
