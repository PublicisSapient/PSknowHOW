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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.bitbucket.factory.BitBucketKPIServiceFactory;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyKanbanFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.errors.EntityNotFoundException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyDataKanban;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;

@RunWith(MockitoJUnitRunner.class)
public class BitBucketServiceKanbanRTest {
	private static String GROUP_PROJECT = "PROJECT";
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	@Mock
	FilterHelperService filterHelperService;
	@Mock
	KpiHelperService kpiHelperService;
	@InjectMocks
	private BitBucketServiceKanbanR bitbucketServiceKanbanR;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	private CacheService cacheService;
	@Mock
	private CodeCommitKanbanServiceImpl codeCommitKanbanServiceImpl;
	@Mock
	private UserAuthorizedProjectsService authorizedProjectsService;
	@SuppressWarnings("rawtypes")
	@Mock
	private List<BitBucketKPIService> services;
	private List<AccountHierarchyDataKanban> accountHierarchyDataKanbanList = new ArrayList<>();
	private String[] projectKey;
	private Set<String> projects;
	private KpiElement commitKpiElement;
	private Map<String, BitBucketKPIService<?, ?, ?>> bitbucketServiceCache = new HashMap<>();
	@Mock
	private BitBucketKPIServiceFactory bitbucketKPIServiceFactory;

	@Before
	public void setup() throws EntityNotFoundException {
		KpiRequest kpiRequestBitBucket = createKpiRequest(2, "Bitbucket");

		// bitbucketKPIServiceFactory.initMyServiceCache();
		AccountHierarchyKanbanFilterDataFactory accountHierarchyKanbanFilterDataFactory = AccountHierarchyKanbanFilterDataFactory
				.newInstance();
		accountHierarchyDataKanbanList = accountHierarchyKanbanFilterDataFactory.getAccountHierarchyKanbanDataList();

		ProjectBasicConfig projectConfig = new ProjectBasicConfig();
		projectConfig.setId(new ObjectId("6335368249794a18e8a4479f"));
		projectConfig.setProjectName("Kanban Project");
		projectConfigMap.put(projectConfig.getProjectName(), projectConfig);

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/kanban/kanban_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		commitKpiElement = setKpiElement(KPICode.NUMBER_OF_CHECK_INS.getKpiId(), "Code Commit Time");

	}

	private KpiElement setKpiElement(String kpiId, String kpiName) {

		KpiElement kpiElement = new KpiElement();
		kpiElement.setKpiId(kpiId);
		kpiElement.setKpiName(kpiName);

		return kpiElement;
	}

	@After
	public void cleanup() {

	}

	@Test
	public void testProcess() throws Exception {

		KpiRequest kpiRequest = createKpiRequest(2, "Bitbucket");

		BitBucketKPIService mcokAbstract = codeCommitKanbanServiceImpl;
		bitbucketServiceCache.put(KPICode.NUMBER_OF_CHECK_INS.name(), mcokAbstract);

		try (MockedStatic<BitBucketKPIServiceFactory> utilities = Mockito
				.mockStatic(BitBucketKPIServiceFactory.class)) {
			utilities.when((MockedStatic.Verification) BitBucketKPIServiceFactory
					.getBitBucketKPIService(KPICode.NUMBER_OF_CHECK_INS.name())).thenReturn(mcokAbstract);
		}

		when(filterHelperService.getFilteredBuildsKanban(Mockito.any(), Mockito.any()))
				.thenReturn(accountHierarchyDataKanbanList);
		when(authorizedProjectsService.getKanbanProjectKey(accountHierarchyDataKanbanList, kpiRequest))
				.thenReturn(projectKey);
		when(mcokAbstract.getKpiData(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(commitKpiElement);

		List<KpiElement> resultList = bitbucketServiceKanbanR.process(kpiRequest);

		resultList.forEach(k -> {

			KPICode kpi = KPICode.getKPI(k.getKpiId());

			switch (kpi) {

			case NUMBER_OF_CHECK_INS:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("NUMBER_OF_CHECK_INS"));
				break;

			default:
				break;
			}

		});

	}

	// level = sprint
	@Test
	public void testProcess1() throws Exception {

		KpiRequest kpiRequest = createKpiRequest(5, "Bitbucket");

		@SuppressWarnings("rawtypes")
		BitBucketKPIService mcokAbstract = codeCommitKanbanServiceImpl;
		bitbucketServiceCache.put(KPICode.NUMBER_OF_CHECK_INS.name(), mcokAbstract);

		try (MockedStatic<BitBucketKPIServiceFactory> utilities = Mockito
				.mockStatic(BitBucketKPIServiceFactory.class)) {
			utilities.when((MockedStatic.Verification) BitBucketKPIServiceFactory
					.getBitBucketKPIService(KPICode.NUMBER_OF_CHECK_INS.name())).thenReturn(mcokAbstract);
		}

		when(filterHelperService.getFilteredBuildsKanban(Mockito.any(), Mockito.any()))
				.thenReturn(accountHierarchyDataKanbanList);

		when(mcokAbstract.getKpiData(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(commitKpiElement);

		List<KpiElement> resultList = bitbucketServiceKanbanR.process(kpiRequest);

		resultList.forEach(k -> {

			KPICode kpi = KPICode.getKPI(k.getKpiId());

			switch (kpi) {

			case NUMBER_OF_CHECK_INS:
				assertThat("Kpi Name :", k.getKpiName(), equalTo("NUMBER_OF_CHECK_INS"));
				break;

			default:
				break;
			}

		});

	}

	@Test
	public void testProcessCachedData() throws Exception {

		KpiRequest kpiRequest = createKpiRequest(2, "Bitbucket");

		when(cacheService.getFromApplicationCache(Mockito.any(), Mockito.any(), Mockito.any(),
				ArgumentMatchers.anyList())).thenReturn(new ArrayList<>());

		List<KpiElement> resultList = bitbucketServiceKanbanR.process(kpiRequest);
		assertThat("Kpi list :", resultList.size(), equalTo(0));

	}

	private KpiRequest createKpiRequest(int level, String source) {
		KpiRequest kpiRequest = new KpiRequest();
		List<KpiElement> kpiList = new ArrayList<>();

		addKpiElement(kpiList, KPICode.NUMBER_OF_CHECK_INS.getKpiId(), KPICode.NUMBER_OF_CHECK_INS.name(),
				"Productivity", "", source);
		kpiRequest.setLevel(level);
		kpiRequest.setIds(new String[] { "Kanban Project_6335368249794a18e8a4479f" });
		kpiRequest.setKpiList(kpiList);
		Map<String, List<String>> selectedMap = new HashMap<>();
		selectedMap.put("Project", Arrays.asList("Kanban Project_6335368249794a18e8a4479f"));
		kpiRequest.setSelectedMap(selectedMap);
		kpiRequest.setRequestTrackerId();
		return kpiRequest;
	}

	private void addKpiElement(List<KpiElement> kpiList, String kpiId, String kpiName, String category, String kpiUnit,
			String source) {
		KpiElement kpiElement = new KpiElement();
		kpiElement.setKpiId(kpiId);
		kpiElement.setKpiName(kpiName);
		kpiElement.setKpiCategory(category);
		kpiElement.setKpiUnit(kpiUnit);
		kpiElement.setKpiSource(source);
		kpiElement.setMaxValue("500");
		kpiElement.setChartType("gaugeChart");
		kpiList.add(kpiElement);
	}
}