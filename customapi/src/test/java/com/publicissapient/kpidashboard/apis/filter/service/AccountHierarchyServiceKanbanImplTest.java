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

package com.publicissapient.kpidashboard.apis.filter.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testng.Assert;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchiesKanbanDataFactory;
import com.publicissapient.kpidashboard.apis.data.FilterRequestDataFactory;
import com.publicissapient.kpidashboard.apis.data.HierachyLevelFactory;
import com.publicissapient.kpidashboard.apis.data.OrganizationHierarchyDataFactory;
import com.publicissapient.kpidashboard.apis.data.ProjectBasicConfigDataFactory;
import com.publicissapient.kpidashboard.apis.data.ProjectHierarchyDataFactory;
import com.publicissapient.kpidashboard.apis.hierarchy.service.OrganizationHierarchyService;
import com.publicissapient.kpidashboard.apis.model.AccountFilterRequest;
import com.publicissapient.kpidashboard.apis.model.AccountFilteredData;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyDataKanban;
import com.publicissapient.kpidashboard.apis.projectconfig.basic.service.ProjectBasicConfigService;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.HierarchyValue;
import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.repository.application.GlobalConfigRepository;
import com.publicissapient.kpidashboard.common.service.ProjectHierarchyService;

@RunWith(MockitoJUnitRunner.class)
public class AccountHierarchyServiceKanbanImplTest {
	List<KanbanAccountHierarchy> kanbanAccountHierarchyList = new ArrayList<>();
	@Mock
	private CacheService cacheService;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	private TokenAuthenticationService tokenAuthenticationService;
	@Mock
	private UserAuthorizedProjectsService authorizedProjectsService;
	@Mock
	private GlobalConfigRepository globalConfigRepository;
	@Mock
	private ConfigHelperService configHelperService;
	@InjectMocks
	private AccountHierarchyServiceKanbanImpl accountHierarchyServiceImpl;
	@Mock
	private FilterHelperService filterHelperService;
	@Mock
	private OrganizationHierarchyService organizationHierarchyService;

	@Mock
	private ProjectHierarchyService projectHierarchyService;

	@Mock
	private ProjectBasicConfigService projectBasicConfigService;
	private List<HierarchyLevel> hierarchyLevels;

	@Before
	public void setUp() {
		kanbanAccountHierarchyList = AccountHierarchiesKanbanDataFactory.newInstance().getAccountHierarchies();
		HierachyLevelFactory hierachyLevelFactory = HierachyLevelFactory.newInstance();
		hierarchyLevels = hierachyLevelFactory.getHierarchyLevels();
		ProjectBasicConfigDataFactory projectBasicConfigDataFactory = ProjectBasicConfigDataFactory
				.newInstance("/json/basicConfig/project_basic_config_request.json");
		ProjectBasicConfig projectBasicConfig = projectBasicConfigDataFactory.getProjectBasicConfigs().get(1);
		projectBasicConfig.setIsKanban(true);

		List<HierarchyValue> hierarchyList = new ArrayList<>();
		hierarchyList.add(new HierarchyValue(new HierarchyLevel(1, "hierarchyLevelOne", "BU", ""),
				"hierarchyLevelOne_unique_001", "Sample One Value"));
		hierarchyList.add(new HierarchyValue(new HierarchyLevel(2, "hierarchyLevelTwo", "Vertical", ""),
				"hierarchyLevelTwo_unique_001", "Sample Two Value"));
		hierarchyList.add(new HierarchyValue(new HierarchyLevel(3, "hierarchyLevelThree", "Account", ""),
				"hierarchyLevelThree_unique_001", "Sample Three Value"));
		projectBasicConfig.setHierarchy(hierarchyList);

		when(projectBasicConfigService.getAllProjectsBasicConfigs(anyBoolean()))
				.thenReturn(Arrays.asList(projectBasicConfig));

		OrganizationHierarchyDataFactory organizationHierarchyDataFactory = OrganizationHierarchyDataFactory.newInstance();
		ProjectHierarchyDataFactory projectHierarchyDataFactory = ProjectHierarchyDataFactory.newInstance();
		List<OrganizationHierarchy> organizationHierarchies = organizationHierarchyDataFactory.getOrganizationHierarchies();
		when(organizationHierarchyService.findAll()).thenReturn(organizationHierarchies);
		when(projectHierarchyService.findAllByBasicProjectConfigIds(anyList()))
				.thenReturn(projectHierarchyDataFactory.getProjectHierarchies());
	}

	@Test
	public void getQualifierType() {
		String actual = accountHierarchyServiceImpl.getQualifierType();
		assertEquals("Kanban", actual);
	}

	@Test
	public void testGetFilteredList() {
		Set<String> userProjects = new HashSet<>();
		userProjects.add("6335368249794a18e8a4479f");
		AccountFilterRequest request = new AccountFilterRequest();
		request.setKanban(true);
		List<AccountHierarchyDataKanban> accountHierarchies = accountHierarchyServiceImpl.createHierarchyData();
		when(cacheService.cacheAccountHierarchyKanbanData()).thenReturn(accountHierarchies);
		when(authorizedProjectsService.ifSuperAdminUser()).thenReturn(true);
		when(tokenAuthenticationService.getUserProjects()).thenReturn(userProjects);
		Set<AccountFilteredData> filterList = accountHierarchyServiceImpl.getFilteredList(request);
	}

	@Test
	public void testGetFilteredList_ProjectLevelWithFilter() {
		Set<String> userProjects = new HashSet<>();
		userProjects.add("66f88deaed6a46340d6ab05e");
		AccountFilterRequest request = FilterRequestDataFactory.newInstance().getFilterRequest();
		List<AccountHierarchyDataKanban> accountHierarchies = accountHierarchyServiceImpl.createHierarchyData();
		when(cacheService.cacheAccountHierarchyKanbanData()).thenReturn(accountHierarchies);
		when(authorizedProjectsService.ifSuperAdminUser()).thenReturn(true);
		when(tokenAuthenticationService.getUserProjects()).thenReturn(userProjects);
		Set<AccountFilteredData> filterList = accountHierarchyServiceImpl.getFilteredList(request);
	}

	@Test
	public void testGetFilteredList_Project() {
		Set<String> userProjects = new HashSet<>();
		userProjects.add("66f88deaed6a46340d6ab05e");
		AccountFilterRequest request = FilterRequestDataFactory.newInstance().getFilterRequest();
		Map<String, Integer> map = new HashMap<>();
		Map<String, HierarchyLevel> hierarchyMap = hierarchyLevels.stream()
				.collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));
		hierarchyMap.entrySet().stream().forEach(k -> map.put(k.getKey(), k.getValue().getLevel()));

		when(filterHelperService.getHierarchyIdLevelMap(true)).thenReturn(map);
		when(filterHelperService.getFirstHierarachyLevel()).thenReturn("hierarchyLevelOne");

		List<AccountHierarchyDataKanban> accountHierarchies = accountHierarchyServiceImpl.createHierarchyData();
		when(cacheService.cacheAccountHierarchyKanbanData()).thenReturn(accountHierarchies);
		when(authorizedProjectsService.ifSuperAdminUser()).thenReturn(false);
		when(tokenAuthenticationService.getUserProjects()).thenReturn(userProjects);
		Set<AccountFilteredData> filterList = accountHierarchyServiceImpl.getFilteredList(request);
		Assert.assertEquals(filterList.size(), 7);
	}
}
