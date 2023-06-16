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

package com.publicissapient.kpidashboard.apis.bitbucket.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.bitbucket.factory.BitBucketKPIServiceFactory;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.EntityNotFoundException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;

@RunWith(MockitoJUnitRunner.class)
public class BitBucketServiceRTest {
	private static String GROUP_PROJECT = "PROJECT";
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	KpiHelperService kpiHelperService;
	@Mock
	FilterHelperService filterHelperService;
	@InjectMocks
	private BitBucketServiceR bitbucketServiceR;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	private CacheService cacheService;
	@Mock
	private CodeCommitServiceImpl codeCommitServiceImpl;
	@Mock
	private UserAuthorizedProjectsService authorizedProjectsService;
	@SuppressWarnings("rawtypes")
	@Mock
	private List<BitBucketKPIService> services;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private Map<String, Object> filterLevelMap;
	private String[] projectKey;
	private Set<String> projects;
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<FieldMapping> fieldMappingList = new ArrayList<>();
	private KpiElement commitKpiElement;
	private KpiRequest kpiRequest;
	private Map<String, BitBucketKPIService<?, ?, ?>> bitbucketServiceCache = new HashMap<>();
	@Mock
	private BitBucketKPIServiceFactory bitbucketKPIServiceFactory;

	@Before
	public void setup() {

		// bitbucketKPIServiceFactory.initMyServiceCache();

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.CODE_COMMIT.getKpiId());
		kpiRequest.setLabel("PROJECT");

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		filterLevelMap = new LinkedHashMap<>();
		filterLevelMap.put("PROJECT", Filters.PROJECT);
		filterLevelMap.put("SPRINT", Filters.SPRINT);

		ProjectBasicConfig projectConfig = new ProjectBasicConfig();
		projectConfig.setId(new ObjectId("6335363749794a18e8a4479b"));
		projectConfig.setProjectName("Scrum Project");
		projectConfigMap.put(projectConfig.getProjectName(), projectConfig);

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);

		when(filterHelperService.getHierarachyLevelId(5, "project", false)).thenReturn("project");

		when(filterHelperService.getFilteredBuilds(kpiRequest, GROUP_PROJECT)).thenReturn(accountHierarchyDataList);

		commitKpiElement = kpiRequest.getKpiList().get(0);

	}

	@After
	public void cleanup() {

	}

	@Test
	public void TestProcess_emptyFilteredACH() throws EntityNotFoundException {

		when(filterHelperService.getFilteredBuilds(kpiRequest, GROUP_PROJECT))
				.thenThrow(HttpMessageNotWritableException.class);

		bitbucketServiceR.process(kpiRequest);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void TestProcess() throws Exception {

		@SuppressWarnings("rawtypes")
		BitBucketKPIService mcokAbstract = codeCommitServiceImpl;
		bitbucketServiceCache.put(KPICode.CODE_COMMIT.name(), mcokAbstract);

		try (MockedStatic<BitBucketKPIServiceFactory> utilities = Mockito
				.mockStatic(BitBucketKPIServiceFactory.class)) {
			utilities.when((MockedStatic.Verification) BitBucketKPIServiceFactory
					.getBitBucketKPIService(KPICode.CODE_COMMIT.name())).thenReturn(mcokAbstract);
		}

		when(filterHelperService.getFilteredBuilds(Mockito.any(), Mockito.any())).thenReturn(accountHierarchyDataList);
		when(authorizedProjectsService.getProjectKey(accountHierarchyDataList, kpiRequest)).thenReturn(projectKey);
		when(authorizedProjectsService.getProjectNodesForRequest(accountHierarchyDataList)).thenReturn(projects);

		when(mcokAbstract.getKpiData(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(commitKpiElement);

		List<KpiElement> resultList = bitbucketServiceR.process(kpiRequest);

		resultList.forEach(k -> {

			KPICode kpi = KPICode.getKPI(k.getKpiId());

			switch (kpi) {

			case CODE_COMMIT:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("Code Commit Time"));
				break;

			default:
				break;
			}

		});

	}

	@SuppressWarnings("unchecked")
	@Test
	public void TestProcessExcel() throws Exception {

		@SuppressWarnings("rawtypes")
		BitBucketKPIService mcokAbstract = codeCommitServiceImpl;
		bitbucketServiceCache.put(KPISource.EXCEL.name(), mcokAbstract);

		try (MockedStatic<BitBucketKPIServiceFactory> utilities = Mockito
				.mockStatic(BitBucketKPIServiceFactory.class)) {
			utilities.when((MockedStatic.Verification) BitBucketKPIServiceFactory
					.getBitBucketKPIService(KPICode.CODE_COMMIT.name())).thenReturn(mcokAbstract);
		}

		when(filterHelperService.getFilteredBuilds(Mockito.any(), Mockito.any())).thenReturn(accountHierarchyDataList);

		when(mcokAbstract.getKpiData(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(commitKpiElement);

		List<KpiElement> resultList = bitbucketServiceR.process(kpiRequest);

		resultList.forEach(k -> {

			KPICode kpi = KPICode.getKPI(k.getKpiId());

			switch (kpi) {

			case CODE_COMMIT:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("Code Commit Time"));
				break;

			default:
				break;
			}

		});

	}

	@SuppressWarnings("unchecked")
	@Test
	public void TestProcessgetFromCache() throws Exception {
		@SuppressWarnings("rawtypes")
		BitBucketKPIService mcokAbstract = codeCommitServiceImpl;
		bitbucketServiceCache.put(KPISource.EXCEL.name(), mcokAbstract);

		try (MockedStatic<BitBucketKPIServiceFactory> utilities = Mockito
				.mockStatic(BitBucketKPIServiceFactory.class)) {
			utilities.when((MockedStatic.Verification) BitBucketKPIServiceFactory
					.getBitBucketKPIService(KPICode.CODE_COMMIT.name())).thenReturn(mcokAbstract);
		}

		when(filterHelperService.getFilteredBuilds(Mockito.any(), Mockito.any())).thenReturn(accountHierarchyDataList);

		when(mcokAbstract.getKpiData(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(commitKpiElement);

		when(cacheService.getFromApplicationCache(Mockito.any(String[].class), anyString(), anyInt(),
				ArgumentMatchers.anyList())).thenReturn(new ArrayList<KpiElement>());

		List<KpiElement> resultList = bitbucketServiceR.process(kpiRequest);

		resultList.forEach(k -> {

			KPICode kpi = KPICode.getKPI(k.getKpiId());

			switch (kpi) {

			case CODE_COMMIT:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("Code Commit Time"));
				break;

			default:
				break;
			}

		});

	}
}