package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.apis.data.IssueBacklogCustomHistoryDataFactory;
import com.publicissapient.kpidashboard.common.model.jira.IssueBacklogCustomHistory;
import com.publicissapient.kpidashboard.common.repository.jira.IssueBacklogCustomHistoryQueryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.IssueBacklogCustomHistoryRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.CustomDateRange;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;

@RunWith(MockitoJUnitRunner.class)
public class FlowLoadServiceImplTest {
    @InjectMocks
    private FlowLoadServiceImpl flowLoadService;
    @Mock
    CacheService cacheService;
    @Mock
    CustomApiConfig customApiConfig;
    @Mock
    private IssueBacklogCustomHistoryRepository issueBacklogCustomHistoryRepository;
    private KpiRequest kpiRequest;
    List<Node> leafNodeList = new ArrayList<>();
    TreeAggregatorDetail treeAggregatorDetail;
    List<IssueBacklogCustomHistory> issueBacklogHistoryDataList = new ArrayList<>();
    Map<String, Map<String, Integer>> dateTypeCountMap = new HashMap<>();

    @Before
    public void setUp() throws ApplicationException {
        KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
        List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();

        kpiRequest = kpiRequestFactory.findKpiRequest("kpi146");
        kpiRequest.setLabel("PROJECT");
        AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
                .newInstance();

        accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

        leafNodeList = new ArrayList<>();
        treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest, accountHierarchyDataList,
                new ArrayList<>(), "hierarchyLevelOne", 4);
        treeAggregatorDetail.getMapOfListOfProjectNodes().forEach((k, v) -> {
            leafNodeList.addAll(v);
        });

        issueBacklogHistoryDataList = IssueBacklogCustomHistoryDataFactory.newInstance().getIssueBacklogCustomHistory();

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
        List<IssueBacklogCustomHistory> expectedResult = new ArrayList<>();
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("_id", "2023-02-17");
        List<Map<String, Object>> typeCountMap = new ArrayList<>();
        when(issueBacklogCustomHistoryRepository.findByBasicProjectConfigId(Mockito.any()))
                .thenReturn(issueBacklogHistoryDataList);
        KpiElement responseKpiElement = flowLoadService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
                treeAggregatorDetail);

        assertNotNull(responseKpiElement);
        assertNotNull(responseKpiElement.getTrendValueList());
        assertEquals(responseKpiElement.getKpiId(), kpiRequest.getKpiList().get(0).getKpiId());
    }
}
