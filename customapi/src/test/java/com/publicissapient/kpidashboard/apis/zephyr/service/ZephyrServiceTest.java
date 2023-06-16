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

package com.publicissapient.kpidashboard.apis.zephyr.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;

/**
 *
 * @author tauakram
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class ZephyrServiceTest {

	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	FilterHelperService filterHelperService;
	@Mock
	KpiHelperService kpiHelperService;
	@InjectMocks
	private ZephyrService zephyrService;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	private CacheService cacheService;
	@Mock
	private UserAuthorizedProjectsService authorizedProjectsService;
	@Mock
	private ZephyrKPIService<?, ?, ?> zephyrAutomationService;
	@Mock
	private ZephyrKPIService<?, ?, ?> zephyrRegressionService;
	@SuppressWarnings("rawtypes")
	@Mock
	private List<ZephyrKPIService> services;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private Map<String, Object> filterLevelMap;
	private KpiRequest kpiRequest;
	private KpiRequest kpiRequestForReg;
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<FieldMapping> fieldMappingList = new ArrayList<>();
	private String[] projectKey;
	private Set<String> projects;

	private KpiElement automationKpiElement;
	private KpiElement regressionKpiElement;

	@Before
	public void setup() {
		projectKey = new String[] { "Regression", "Automation" };
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi16");
		kpiRequest.setLabel("PROJECT");
		kpiRequest.setLevel(2);
		automationKpiElement = kpiRequest.getKpiList().get(0);
		kpiRequestForReg = kpiRequestFactory.findKpiRequest("kpi42");
		kpiRequestForReg.setLabel("PROJECT");
		kpiRequestForReg.setLevel(2);
		regressionKpiElement = kpiRequestForReg.getKpiList().get(0);
		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
	}

	@Test
	public void TestProcess() throws Exception {
		when(filterHelperService.getHierarachyLevelId(kpiRequest.getLevel(), kpiRequest.getLabel(), false))
				.thenReturn("project");
		/*
		 * when(filterHelperService.getFilteredBuilds(kpiRequest,
		 * "Regression")).thenReturn(accountHierarchyDataList);
		 * when(cacheService.getFromApplicationCache(projectKey,
		 * KPISource.ZEPHYR.name(), kpiRequest.getKpiList().get(0).getGroupId(),
		 * kpiRequest.getSprintIncluded())).thenReturn(null);
		 * when(authorizedProjectsService.getProjectKey(accountHierarchyDataList,
		 * kpiRequest)).thenReturn(projectKey);
		 * when(authorizedProjectsService.getProjectNodesForRequest(
		 * accountHierarchyDataList)).thenReturn(projects);
		 * when(authorizedProjectsService.filterProjects(accountHierarchyDataList)).
		 * thenReturn(accountHierarchyDataList);
		 */
		List<KpiElement> resultList = zephyrService.process(kpiRequest);
		resultList.forEach(k -> {
			KPICode kpi = KPICode.getKPI(k.getKpiId());
			switch (kpi) {
			case INSPRINT_AUTOMATION_COVERAGE:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("In-Sprint Automation Coverage"));
				break;
			case REGRESSION_AUTOMATION_COVERAGE:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("Regression Percentage"));
				break;
			default:
				break;
			}
		});

	}

	@After
	public void cleanup() {

	}

}