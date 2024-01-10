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

package com.publicissapient.kpidashboard.apis.jira.scrum.service.release;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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

@RunWith(MockitoJUnitRunner.class)
public class ReleaseDefectCountByRCAServiceImplTest {

	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@InjectMocks
	private ReleaseDefectCountByRCAServiceImpl defectCountByRCAService;
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
		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
	}

	@Test
	public void getQualifierType() {
		assertThat(defectCountByRCAService.getQualifierType(), equalTo(KPICode.DEFECT_COUNT_BY_RCA_RELEASE.name()));
	}

	@Test
	public void getKpiData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		String kpiRequestTrackerId = "Jira-Excel-QADD-track001";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(jiraService.getJiraIssuesForSelectedRelease()).thenReturn(bugList);
		KpiElement kpiElement = defectCountByRCAService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
				treeAggregatorDetail);
		List<IterationKpiValue> trendValueList = (List<IterationKpiValue>) kpiElement.getTrendValueList();
		Map<String, Integer> value = (Map<String, Integer>) ((DataCount) ((ArrayList) trendValueList.get(1).getValue()
				.get(0).getValue()).get(0)).getValue();
		assertEquals(value, expectedResult(bugList));
	}

	@Test
	public void getKpiDataWithoutRCA() throws ApplicationException {
		bugList.forEach(jiraIssue -> jiraIssue.setRootCauseList(new ArrayList<>()));
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		String kpiRequestTrackerId = "Jira-Excel-QADD-track001";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(jiraService.getJiraIssuesForSelectedRelease()).thenReturn(bugList);
		KpiElement kpiElement = defectCountByRCAService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
				treeAggregatorDetail);
		List<IterationKpiValue> trendValueList = (List<IterationKpiValue>) kpiElement.getTrendValueList();
		Map<String, Integer> value = (Map<String, Integer>) ((DataCount) ((ArrayList) trendValueList.get(1).getValue()
				.get(0).getValue()).get(0)).getValue();
		assertEquals(value, expectedResult(bugList));
	}

	private Map<String, Integer> expectedResult(List<JiraIssue> bugList) {
		Map<String, Integer> finalMap = new HashMap<>();
		Map<String, List<JiraIssue>> collect = bugList.stream().filter(jiraIssue -> {
			if (CollectionUtils.isEmpty(jiraIssue.getRootCauseList())) {
				List<String> rcaDummy = new ArrayList<>();
				rcaDummy.add("-");
				jiraIssue.setRootCauseList(rcaDummy);
			}
			return true;
		}).collect(Collectors.groupingBy(jiraIssue -> jiraIssue.getRootCauseList().get(0)));
		collect.forEach((k, v) -> finalMap.put(k, v.size()));
		return finalMap;
	}
}