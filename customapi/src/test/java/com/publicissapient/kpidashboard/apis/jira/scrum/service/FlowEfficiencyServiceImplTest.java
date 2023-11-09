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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.CommonService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueHistoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;

/**
 * @author kunkambl
 */
@RunWith(MockitoJUnitRunner.class)
public class FlowEfficiencyServiceImplTest {

	@Mock
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@InjectMocks
	FlowEfficiencyServiceImpl flowEfficiencyService;
	@Mock
	ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	CustomApiConfig customApiConfig;
	@Mock
	FieldMappingRepository fieldMappingRepository;
	@Mock
	private CommonService commonService;
	@Mock
	private KpiHelperService kpiHelperService;
	List<Node> leafNodeList = new ArrayList<>();
	TreeAggregatorDetail treeAggregatorDetail;
	private KpiRequest kpiRequest;
	private KpiElement kpiElement;
	private List<JiraIssueCustomHistory> issueBacklogHistoryDataList = new ArrayList<>();
	private Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	private static List<String> xAxisRange = Arrays.asList("< 16 Months", "< 3 Months", "< 1 Months", "< 2 Weeks",
			"< 1 Week");
	private static String HISTORY = "history";

	@Before
	public void setUp() throws ApplicationException {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		List<AccountHierarchyData> accountHierarchyDataList;

		kpiRequest = kpiRequestFactory.findKpiRequest("kpi170");
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
		JiraIssueHistoryDataFactory jiraIssueHistoryDataFactory = JiraIssueHistoryDataFactory.newInstance();

		issueBacklogHistoryDataList = jiraIssueHistoryDataFactory.getJiraIssueCustomHistory();

	}

	@Test
	public void getQualifierTypeTest() {
		assertThat(flowEfficiencyService.getQualifierType(), equalTo(KPICode.FLOW_EFFICIENCY.name()));
	}

	@Test
    public void getKpiDataTest() throws ApplicationException {
        when(customApiConfig.getFlowEfficiencyXAxisRange()).thenReturn(xAxisRange);
        String kpiRequestTrackerId = "Jira-Excel-QADD-track001";
        when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
                .thenReturn(kpiRequestTrackerId);
        when(jiraIssueCustomHistoryRepository.findByFilterAndFromStatusMapWithDateFilter(any(), any(), any(), any()))
                .thenReturn(issueBacklogHistoryDataList);
        List<JiraIssueCustomHistory> expectedResult = new ArrayList<>();
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(HISTORY, issueBacklogHistoryDataList);
        List<Map<String, Object>> typeCountMap = new ArrayList<>();
        when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
        KpiElement responseKpiElement = flowEfficiencyService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
                treeAggregatorDetail);

        assertNotNull(responseKpiElement);
        assertEquals(responseKpiElement.getKpiId(), kpiRequest.getKpiList().get(0).getKpiId());
    }

}