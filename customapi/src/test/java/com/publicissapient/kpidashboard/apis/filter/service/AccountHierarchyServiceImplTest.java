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

/**
 * 
 */
package com.publicissapient.kpidashboard.apis.filter.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchiesDataFactory;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.HierachyLevelFactory;
import com.publicissapient.kpidashboard.apis.data.SprintDetailsDataFactory;
import com.publicissapient.kpidashboard.apis.model.AccountFilterRequest;
import com.publicissapient.kpidashboard.apis.model.AccountFilteredData;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.GlobalConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

/**
 * @author tauakram
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class AccountHierarchyServiceImplTest {
	@Mock
	TokenAuthenticationService tokenAuthenticationService;
	@Mock
	UserAuthorizedProjectsService authorizedProjectsService;
	@Mock
	GlobalConfigRepository globalConfigRepository;
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	private AccountHierarchyRepository accountHierarchyRepository;
	@Mock
	private CacheService cacheService;
	@InjectMocks
	private AccountHierarchyServiceImpl accountHierarchyServiceImpl;
	@Mock
	private SprintRepository sprintRepository;

	@Mock
	private FilterHelperService filterHelperService;

	private List<AccountHierarchy> ahdList = new ArrayList<>();
	private Set<String> userAccessProjects = new HashSet<>();
	private List<HierarchyLevel> hierarchyLevels = new ArrayList<>();
	private List<AccountHierarchyData> accountHierarchyDataList;

	@Mock
	private CustomApiConfig customApiConfig;

	@Before
	public void setup() {
		AccountHierarchiesDataFactory ahdFactoryProjectLabel = AccountHierarchiesDataFactory.newInstance();
		ahdList = ahdFactoryProjectLabel.getAccountHierarchies();
		HierachyLevelFactory hierachyLevelFactory = HierachyLevelFactory.newInstance();
		hierarchyLevels = hierachyLevelFactory.getHierarchyLevels();
		SprintDetailsDataFactory sprintDetailsDataFactory = SprintDetailsDataFactory.newInstance();
		List<SprintDetails> sprintDetails = sprintDetailsDataFactory.getSprintDetails();
		when(sprintRepository.findBySprintIDIn(anyList())).thenReturn(sprintDetails);

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
		when(cacheService.cacheAccountHierarchyData()).thenReturn(accountHierarchyDataList);

		Map<String, Object> permissionMap = new HashMap<>();

		permissionMap.put("role", "ROLE_SUPERADMIN");
		userAccessProjects.add("6335363749794a18e8a4479b");

	}

	@Test
	public void testGetFilteredList_nonsuperadmin() {
		AccountFilterRequest request = new AccountFilterRequest();
		request.setKanban(false);
		request.setSprintIncluded(new ArrayList<>());
		when(authorizedProjectsService.ifSuperAdminUser()).thenReturn(false);
		when(tokenAuthenticationService.getUserProjects()).thenReturn(userAccessProjects);
		when(filterHelperService.getAccountHierarchyDataForRequest(anySet(), anyList()))
				.thenReturn(accountHierarchyDataList);

		// no filter selected
		Set<AccountFilteredData> filterList = accountHierarchyServiceImpl.getFilteredList(request);
		Assert.assertEquals(9, filterList.size());
	}

	@Test
	public void testGetFilteredList_superadmin() {
		AccountFilterRequest request = new AccountFilterRequest();
		request.setKanban(false);
		request.setSprintIncluded(Arrays.asList("closed"));
		when(authorizedProjectsService.ifSuperAdminUser()).thenReturn(true);
		when(tokenAuthenticationService.getUserProjects()).thenReturn(userAccessProjects);

		// no filter selected
		Set<AccountFilteredData> filterList = accountHierarchyServiceImpl.getFilteredList(request);
		Assert.assertEquals(0, filterList.size());
	}

	@Test
	public void getQualifierType() {
		String actual = accountHierarchyServiceImpl.getQualifierType();
		assertEquals("Scrum", actual);
	}

	@Test
	public void testGetFilteredList1() {
		AccountFilterRequest request = new AccountFilterRequest();
		request.setKanban(false);
		request.setSprintIncluded(null);
		when(accountHierarchyRepository.findAll()).thenReturn(ahdList);
		when(customApiConfig.getSprintCountForFilters()).thenReturn(15);

		Map<String, Integer> map = new HashMap<>();
		Map<String, HierarchyLevel> hierarchyMap = hierarchyLevels.stream()
				.collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));
		hierarchyMap.entrySet().stream().forEach(k -> map.put(k.getKey(), k.getValue().getLevel()));
		when(filterHelperService.getHierarchyIdLevelMap(false)).thenReturn(map);
		when(filterHelperService.getFirstHierarachyLevel()).thenReturn("hierarchyLevelOne");
		List<AccountHierarchyData> accountHierarchies = accountHierarchyServiceImpl.createHierarchyData();

		Assert.assertEquals(5, accountHierarchies.size());

	}

}