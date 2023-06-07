package com.publicissapient.kpidashboard.apis.jira.scrum.service.release;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
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
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ReleaseBurnupServiceImplTest {
    @InjectMocks
    private ReleaseBurnupServiceImpl releaseBurnupService;
    @Mock
    CacheService cacheService;
    @Mock
    ConfigHelperService configHelperService;
    @Mock
    private JiraServiceR jiraService;

    private KpiRequest kpiRequest;
    private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
    private List<JiraIssue> bugList = new ArrayList<>();
    private Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();

    @Before
    public void setUp() {
        KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance("");
        kpiRequest = kpiRequestFactory.findKpiRequest("kpi141");
        kpiRequest.setLabel("RELEASE");
        AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
                .newInstance("/json/default/account_hierarchy_filter_data_release.json");
        accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
        JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
        bugList = jiraIssueDataFactory.getBugs();
    }

    @Test
    public void getQualifierType() {
        assertThat(releaseBurnupService.getQualifierType(),
                equalTo(KPICode.RELEASE_BURNUP.name()));
    }

    @Test
    public void getKpiData() throws ApplicationException {
        TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
                accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
        String kpiRequestTrackerId = "Jira-Excel-QADD-track001";
        when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
                .thenReturn(kpiRequestTrackerId);
        when(jiraService.getJiraIssuesForCurrentSprint()).thenReturn(bugList);
        KpiElement kpiElement = releaseBurnupService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
                treeAggregatorDetail);
        List<IterationKpiValue> trendValueList = (List<IterationKpiValue>) kpiElement.getTrendValueList();
        Map<String, Integer> value = (Map<String, Integer>) ((DataCount) ((ArrayList) trendValueList.get(0).getValue()
                .get(0).getValue()).get(0)).getValue();
        //assertEquals(value, expectedResult(bugList));
    }


}