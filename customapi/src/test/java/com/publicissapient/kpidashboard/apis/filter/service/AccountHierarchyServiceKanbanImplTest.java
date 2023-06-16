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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
import com.publicissapient.kpidashboard.apis.model.AccountFilterRequest;
import com.publicissapient.kpidashboard.apis.model.AccountFilteredData;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyDataKanban;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.repository.application.GlobalConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;

@RunWith(MockitoJUnitRunner.class)
public class AccountHierarchyServiceKanbanImplTest {
	List<KanbanAccountHierarchy> kanbanAccountHierarchyList = new ArrayList<>();
	@Mock
	private KanbanAccountHierarchyRepository accountHierarchyRepository;
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
	private List<HierarchyLevel> hierarchyLevels;

	@Before
	public void setUp() {
		kanbanAccountHierarchyList = AccountHierarchiesKanbanDataFactory.newInstance().getAccountHierarchies();
		HierachyLevelFactory hierachyLevelFactory = HierachyLevelFactory.newInstance();
		hierarchyLevels = hierachyLevelFactory.getHierarchyLevels();

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
		userProjects.add("6335368249794a18e8a4479f");
		AccountFilterRequest request = FilterRequestDataFactory.newInstance().getFilterRequest();
		when(accountHierarchyRepository.findAll()).thenReturn(kanbanAccountHierarchyList);
		List<AccountHierarchyDataKanban> accountHierarchies = accountHierarchyServiceImpl.createHierarchyData();
		when(cacheService.cacheAccountHierarchyKanbanData()).thenReturn(accountHierarchies);
		when(authorizedProjectsService.ifSuperAdminUser()).thenReturn(true);
		when(tokenAuthenticationService.getUserProjects()).thenReturn(userProjects);
		Set<AccountFilteredData> filterList = accountHierarchyServiceImpl.getFilteredList(request);
	}

	@Test
	public void testGetFilteredList_Project() {
		Set<String> userProjects = new HashSet<>();
		userProjects.add("6335368249794a18e8a4479f");
		AccountFilterRequest request = FilterRequestDataFactory.newInstance().getFilterRequest();
		when(accountHierarchyRepository.findAll()).thenReturn(kanbanAccountHierarchyList);
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
		Assert.assertEquals(4, filterList.size());
	}
}