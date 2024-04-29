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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.bitbucket.factory.BitBucketKPIServiceFactory;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.HierachyLevelFactory;
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
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;

@RunWith(MockitoJUnitRunner.class)
public class BitBucketServiceRTest {

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

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.CODE_COMMIT.getKpiId());
		kpiRequest.setLabel("PROJECT");
		String[] ids = { "5" };
		kpiRequest.setIds(ids);

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

		HierachyLevelFactory hierachyLevelFactory = HierachyLevelFactory.newInstance();
		List<HierarchyLevel> hierarchyLevels = hierachyLevelFactory.getHierarchyLevels();
		Map<String, Integer> map = new HashMap<>();
		Map<String, HierarchyLevel> hierarchyMap = hierarchyLevels.stream()
				.collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));
		hierarchyMap.entrySet().stream().forEach(k -> map.put(k.getKey(), k.getValue().getLevel()));
		when(filterHelperService.getHierarchyIdLevelMap(false)).thenReturn(map);

		when(authorizedProjectsService.filterProjects(accountHierarchyDataList)).thenReturn(accountHierarchyDataList);

		// when(filterHelperService.getHierarachyLevelId(5, "project",
		// false)).thenReturn("project");

		// when(filterHelperService.getFilteredBuilds(kpiRequest,
		// GROUP_PROJECT)).thenReturn(accountHierarchyDataList);

		commitKpiElement = kpiRequest.getKpiList().get(0);

	}

	@After
	public void cleanup() {

	}

	@Test
	public void TestProcess_emptyFilteredACH() throws EntityNotFoundException {

		// when(filterHelperService.getFilteredBuilds(kpiRequest, GROUP_PROJECT))
		// .thenThrow(HttpMessageNotWritableException.class);

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
		// when(authorizedProjectsService.getProjectNodesForRequest(accountHierarchyDataList)).thenReturn(projects);

		// when(mcokAbstract.getKpiData(Mockito.any(), Mockito.any(),
		// Mockito.any())).thenReturn(commitKpiElement);

		List<KpiElement> resultList = null;
		try (MockedStatic<BitBucketKPIServiceFactory> mockedStatic = mockStatic(BitBucketKPIServiceFactory.class)) {
			CodeCommitServiceImpl mockService = mock(CodeCommitServiceImpl.class);
			mockedStatic.when(() -> BitBucketKPIServiceFactory.getBitBucketKPIService(eq(KPICode.CODE_COMMIT.name())))
					.thenReturn(mockService);
			resultList = bitbucketServiceR.process(kpiRequest);
			mockedStatic
					.verify(() -> BitBucketKPIServiceFactory.getBitBucketKPIService(eq(KPICode.CODE_COMMIT.name())));
		}

		resultList.forEach(k -> {

			KPICode kpi = KPICode.getKPI(k.getKpiId());

			switch (kpi) {

			case CODE_COMMIT:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("Check-Ins & Merge Requests"));
				break;

			default:
				break;
			}

		});

	}

	@Test
	public void TestProcessApplicationException() throws Exception {
		bitbucketServiceCache.put(KPISource.EXCEL.name(), codeCommitServiceImpl);
		when(filterHelperService.getFilteredBuilds(Mockito.any(), Mockito.any())).thenReturn(accountHierarchyDataList);
		when(kpiHelperService.isMandatoryFieldValuePresentOrNot(any(), any())).thenReturn(true);
		List<KpiElement> resultList = null;
		try (MockedStatic<BitBucketKPIServiceFactory> mockedStatic = mockStatic(BitBucketKPIServiceFactory.class)) {
			CodeCommitServiceImpl mockService = mock(CodeCommitServiceImpl.class);
			mockedStatic.when(() -> BitBucketKPIServiceFactory.getBitBucketKPIService(eq(KPICode.CODE_COMMIT.name())))
					.thenReturn(mockService);
			when(mockService.getKpiData(any(), any(), any())).thenThrow(ApplicationException.class);
			resultList = bitbucketServiceR.process(kpiRequest);
			mockedStatic
					.verify(() -> BitBucketKPIServiceFactory.getBitBucketKPIService(eq(KPICode.CODE_COMMIT.name())));
		}
		assertThat("Kpi Name :", resultList.get(0).getResponseCode(), equalTo(CommonConstant.KPI_FAILED));

	}


	@Test
	public void TestProcessNullPointer() throws Exception {
		bitbucketServiceCache.put(KPISource.EXCEL.name(), codeCommitServiceImpl);
		when(filterHelperService.getFilteredBuilds(Mockito.any(), Mockito.any())).thenReturn(accountHierarchyDataList);
		when(kpiHelperService.isMandatoryFieldValuePresentOrNot(any(), any())).thenReturn(true);
		List<KpiElement> resultList = null;
		try (MockedStatic<BitBucketKPIServiceFactory> mockedStatic = mockStatic(BitBucketKPIServiceFactory.class)) {
			CodeCommitServiceImpl mockService = mock(CodeCommitServiceImpl.class);
			mockedStatic.when(() -> BitBucketKPIServiceFactory.getBitBucketKPIService(eq(KPICode.CODE_COMMIT.name())))
					.thenReturn(mockService);
			when(mockService.getKpiData(any(), any(), any())).thenThrow(NullPointerException.class);
			resultList = bitbucketServiceR.process(kpiRequest);
			mockedStatic
					.verify(() -> BitBucketKPIServiceFactory.getBitBucketKPIService(eq(KPICode.CODE_COMMIT.name())));
		}
		assertThat("Kpi Name :", resultList.get(0).getResponseCode(), equalTo(CommonConstant.KPI_FAILED));

	}


	@Test
	public void TestProcessExcel_Mandatory() throws Exception {
		bitbucketServiceCache.put(KPISource.EXCEL.name(), codeCommitServiceImpl);
		when(filterHelperService.getFilteredBuilds(Mockito.any(), Mockito.any())).thenReturn(accountHierarchyDataList);
		when(kpiHelperService.isMandatoryFieldValuePresentOrNot(any(), any())).thenReturn(false);
		List<KpiElement> resultList = null;
		try (MockedStatic<BitBucketKPIServiceFactory> mockedStatic = mockStatic(BitBucketKPIServiceFactory.class)) {
			CodeCommitServiceImpl mockService = mock(CodeCommitServiceImpl.class);
			mockedStatic.when(() -> BitBucketKPIServiceFactory.getBitBucketKPIService(eq(KPICode.CODE_COMMIT.name())))
					.thenReturn(mockService);
			resultList = bitbucketServiceR.process(kpiRequest);
			mockedStatic
					.verify(() -> BitBucketKPIServiceFactory.getBitBucketKPIService(eq(KPICode.CODE_COMMIT.name())));
		}
		assertThat("Kpi Name :", resultList.get(0).getResponseCode(), equalTo(CommonConstant.MANDATORY_FIELD_MAPPING));

	}


	@Test
	public void TestProcessExcel() throws Exception {
		bitbucketServiceCache.put(KPISource.EXCEL.name(), codeCommitServiceImpl);
		when(filterHelperService.getFilteredBuilds(Mockito.any(), Mockito.any())).thenReturn(accountHierarchyDataList);
		when(kpiHelperService.isMandatoryFieldValuePresentOrNot(any(), any())).thenReturn(true);
		List<KpiElement> resultList = null;
		try (MockedStatic<BitBucketKPIServiceFactory> mockedStatic = mockStatic(BitBucketKPIServiceFactory.class)) {
			CodeCommitServiceImpl mockService = mock(CodeCommitServiceImpl.class);
			when(mockService.getKpiData(any(), any(), any())).thenReturn(commitKpiElement);
			mockedStatic.when(() -> BitBucketKPIServiceFactory.getBitBucketKPIService(eq(KPICode.CODE_COMMIT.name())))
					.thenReturn(mockService);
			when(mockService.getKpiData(any(), any(), any())).thenReturn(kpiRequest.getKpiList().get(0));
			resultList = bitbucketServiceR.process(kpiRequest);
			mockedStatic
					.verify(() -> BitBucketKPIServiceFactory.getBitBucketKPIService(eq(KPICode.CODE_COMMIT.name())));
		}
		assertThat("Kpi Name :", resultList.get(0).getResponseCode(), equalTo(CommonConstant.KPI_PASSED));

	}


	@SuppressWarnings("unchecked")
	@Test
	public void TestProcessgetFromCache() throws Exception {
		@SuppressWarnings("rawtypes")
		BitBucketKPIService mcokAbstract = codeCommitServiceImpl;
		bitbucketServiceCache.put(KPISource.EXCEL.name(), mcokAbstract);

		when(filterHelperService.getFilteredBuilds(Mockito.any(), Mockito.any())).thenReturn(accountHierarchyDataList);

		// when(cacheService.getFromApplicationCache(Mockito.any(String[].class),
		// anyString(), anyInt(),
		// ArgumentMatchers.anyList())).thenReturn(new ArrayList<KpiElement>());

		List<KpiElement> resultList = null;
		try (MockedStatic<BitBucketKPIServiceFactory> mockedStatic = mockStatic(BitBucketKPIServiceFactory.class)) {
			CodeCommitServiceImpl mockService = mock(CodeCommitServiceImpl.class);
			mockedStatic.when(() -> BitBucketKPIServiceFactory.getBitBucketKPIService(eq(KPICode.CODE_COMMIT.name())))
					.thenReturn(mockService);
			resultList = bitbucketServiceR.process(kpiRequest);
			mockedStatic
					.verify(() -> BitBucketKPIServiceFactory.getBitBucketKPIService(eq(KPICode.CODE_COMMIT.name())));
		}

		resultList.forEach(k -> {

			KPICode kpi = KPICode.getKPI(k.getKpiId());

			switch (kpi) {

			case CODE_COMMIT:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("Check-Ins & Merge Requests"));
				break;

			default:
				break;
			}

		});

	}
}