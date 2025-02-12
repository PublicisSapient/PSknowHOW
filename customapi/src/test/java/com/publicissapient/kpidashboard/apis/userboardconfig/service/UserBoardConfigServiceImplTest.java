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
package com.publicissapient.kpidashboard.apis.userboardconfig.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.data.KpiMasterDataFactory;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.mongock.data.FiltersDataFactory;
import com.publicissapient.kpidashboard.apis.mongock.data.KpiCategoryDataFactory;
import com.publicissapient.kpidashboard.apis.mongock.data.KpiCategoryMappingDataFactory;
import com.publicissapient.kpidashboard.common.model.application.KpiCategory;
import com.publicissapient.kpidashboard.common.model.application.KpiCategoryMapping;
import com.publicissapient.kpidashboard.common.model.application.KpiMaster;
import com.publicissapient.kpidashboard.common.model.userboardconfig.Board;
import com.publicissapient.kpidashboard.common.model.userboardconfig.BoardKpis;
import com.publicissapient.kpidashboard.common.model.userboardconfig.ConfigLevel;
import com.publicissapient.kpidashboard.common.model.userboardconfig.ProjectListRequested;
import com.publicissapient.kpidashboard.common.model.userboardconfig.UserBoardConfig;
import com.publicissapient.kpidashboard.common.model.userboardconfig.UserBoardConfigDTO;
import com.publicissapient.kpidashboard.common.repository.application.AdditionalFilterCategoryRepository;
import com.publicissapient.kpidashboard.common.repository.application.FiltersRepository;
import com.publicissapient.kpidashboard.common.repository.application.KpiCategoryMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.KpiCategoryRepository;
import com.publicissapient.kpidashboard.common.repository.application.KpiMasterRepository;
import com.publicissapient.kpidashboard.common.repository.rbac.UserInfoCustomRepository;
import com.publicissapient.kpidashboard.common.repository.userboardconfig.UserBoardConfigRepository;

/**
 * @author yasbano
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class UserBoardConfigServiceImplTest {

	@InjectMocks
	private UserBoardConfigServiceImpl userBoardConfigServiceImpl;

	@Mock
	private UserBoardConfigRepository userBoardConfigRepository;

	@Mock
	private AuthenticationService authenticationService;

	@Mock
	private KpiMasterRepository kpiMasterRepository;

	@Mock
	private UserAuthorizedProjectsService userAuthorizedProjectsService;

	@Mock
	private KpiCategoryRepository kpiCategoryRepository;

	@Mock
	private KpiCategoryMappingRepository kpiCategoryMappingRepository;

	@Mock
	private ConfigHelperService configHelperService;

	@Mock
	private CacheService cacheService;

	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	private UserInfoCustomRepository userInfoCustomRepository;
	@Mock
	private FiltersRepository filtersRepository;
	@Mock
	private AdditionalFilterCategoryRepository additionalFilterCategoryRepository;
	@Mock
	private UserBoardConfigMapper userBoardConfigMapper;

	private List<KpiCategory> kpiCategoryList;
	private List<KpiCategoryMapping> kpiCategoryMappingList;
	private ProjectListRequested listOfReqProjects;
	private final Map<Pair<String, String>, UserBoardConfig> userBoardConfigMap = new HashMap<>();

	@Before
	public void setUp() {
		kpiCategoryList = KpiCategoryDataFactory.newInstance().getKpiCategoryList();
		kpiCategoryMappingList = KpiCategoryMappingDataFactory.newInstance().getKpiCategoryMappingList();
		ProjectListRequested projectListRequested = new ProjectListRequested();
		projectListRequested.setBasicProjectConfigIds(Arrays.asList("proj1", "proj2"));
		listOfReqProjects = projectListRequested;
		UserBoardConfig userBoardConfig1 = getData("test", true);
		userBoardConfig1.setBasicProjectConfigId("proj1");
		UserBoardConfig userBoardConfig2 = getData("test", true);
		userBoardConfig1.setBasicProjectConfigId("proj2");
		userBoardConfigMap.put(Pair.of(null, "proj1"), userBoardConfig1);
		userBoardConfigMap.put(Pair.of(null, "proj2"), userBoardConfig2);
		FiltersDataFactory filtersDataFactory = FiltersDataFactory.newInstance();
		when(configHelperService.loadAllFilters()).thenReturn(filtersDataFactory.getFiltersList());
		UserBoardConfigDTO userBoardConfigDTO = convertToUserBoardConfigDTO(getData("user", true));
		when(userBoardConfigMapper.toEntity(userBoardConfigDTO)).thenReturn(getData("user", true));
		when(userBoardConfigMapper.toDto(any())).thenReturn(userBoardConfigDTO);
	}

	@Test
	public void testSaveUserBoardConfig() {
		String username = "user";
		String projId = "id";
		UserBoardConfigDTO userBoardConfigDTO = convertToUserBoardConfigDTO(getData(username, true));
		when(userBoardConfigMapper.toEntity(userBoardConfigDTO)).thenReturn(getData(username, true));
		when(userBoardConfigMapper.toDto(any())).thenReturn(userBoardConfigDTO);
		when(authenticationService.getLoggedInUser()).thenReturn(username);
		when(userBoardConfigRepository.save(getData(username, true))).thenReturn(getData(username, true));
		ServiceResponse response = userBoardConfigServiceImpl.saveBoardConfig(userBoardConfigDTO, ConfigLevel.USER,
				projId);
		assertNotNull(response);
	}

	@Test
	public void testSaveAdminUserBoardConfig() {
		String username = "user";
		String projId = "id";
		UserBoardConfigDTO userBoardConfigDTO = convertToUserBoardConfigDTO(getData(username, true));
		when(authenticationService.getLoggedInUser()).thenReturn(username);
		when(userBoardConfigRepository.save(getData(username, true))).thenReturn(getData(username, true));
		when(userBoardConfigMapper.toEntity(userBoardConfigDTO)).thenReturn(getData(username, true));

		ServiceResponse response = userBoardConfigServiceImpl.saveBoardConfig(userBoardConfigDTO, ConfigLevel.USER,
				projId);
		assertNotNull(response);
	}

	@Test
	public void testSaveSuperAdminUserBoardConfig() {
		String username = "user";
		String projId = "all";
		UserBoardConfigDTO userBoardConfigDTO = convertToUserBoardConfigDTO(getData(username, true));
		when(userBoardConfigMapper.toEntity(userBoardConfigDTO)).thenReturn(getData(username, true));
		when(userBoardConfigMapper.toDto(any())).thenReturn(userBoardConfigDTO);
		when(authenticationService.getLoggedInUser()).thenReturn(username);
		ServiceResponse response = userBoardConfigServiceImpl.saveBoardConfig(userBoardConfigDTO, ConfigLevel.USER,
				projId);
		assertNotNull(response.getData());
	}

	@Test
	public void testSaveUserBoardConfig_userNotLoggedIn() {
		String username = "user1";
		UserBoardConfigDTO userBoardConfigDTO = convertToUserBoardConfigDTO(getData(username, true));
		when(userBoardConfigMapper.toEntity(userBoardConfigDTO)).thenReturn(getData(username, true));
		when(authenticationService.getLoggedInUser()).thenReturn("invalid");
		assertNull(userBoardConfigServiceImpl.saveBoardConfig(userBoardConfigDTO, ConfigLevel.USER, "proj1").getData());
	}

	@Test
	public void testSaveUserBoardConfig_DTOIsNull() {
		assertNull(userBoardConfigServiceImpl.saveBoardConfig(null, ConfigLevel.USER, "proj1").getData());
	}

	@Test
	public void testSaveUserBoardConfig_userBoardConfigNull() {
		String username = "user";
		String projId = "id";
		UserBoardConfigDTO userBoardConfigDTO = convertToUserBoardConfigDTO(getData(username, true));
		when(userBoardConfigMapper.toEntity(userBoardConfigDTO)).thenReturn(getData(username, true));
		when(userBoardConfigMapper.toDto(any())).thenReturn(userBoardConfigDTO);
		when(authenticationService.getLoggedInUser()).thenReturn(username);
		when(userBoardConfigRepository.save(getData(username, true))).thenReturn(null);
		when(userBoardConfigMapper.toDto(any())).thenReturn(userBoardConfigDTO);
		when(authenticationService.getLoggedInUser()).thenReturn(username);

		assertNotNull(userBoardConfigServiceImpl.saveBoardConfig(userBoardConfigDTO, ConfigLevel.USER, projId).getData());
	}

	@Test
	public void testGetOrPrepareBoardConfig_success() {
		String username = "testuser";
		doReturn(username).when(authenticationService).getLoggedInUser();
		UserBoardConfigDTO userBoardConfigDTO = userBoardConfigServiceImpl.getOrPrepareBoardConfig(ConfigLevel.USER,
				listOfReqProjects.getBasicProjectConfigIds().get(0));
		assertNotNull(userBoardConfigDTO);
		assertEquals(userBoardConfigDTO.getUsername(), username);
	}

	@Test
	public void testGetOrPrepareBoardConfig_null_success() {
		String username = "testuser";
		doReturn(username).when(authenticationService).getLoggedInUser();
		UserBoardConfigDTO userBoardConfigDTO = userBoardConfigServiceImpl.getOrPrepareBoardConfig(ConfigLevel.USER,
				listOfReqProjects.getBasicProjectConfigIds().get(0));
		assertNotNull(userBoardConfigDTO);
		assertEquals(userBoardConfigDTO.getUsername(), username);
	}

	@Test
	public void testGetAdminUserBoardConfig_success() {
		String username = "testuser";
		String projId = "id";
		doReturn(username).when(authenticationService).getLoggedInUser();
		UserBoardConfigDTO userBoardConfigDTO = userBoardConfigServiceImpl.getOrPrepareBoardConfig(ConfigLevel.PROJECT,
				projId);
		assertNotNull(userBoardConfigDTO);
		assertEquals(userBoardConfigDTO.getBasicProjectConfigId(), projId);
	}

	@Test
	public void testGetAdminUserBoardConfigNull_success() {
		String username = "testuser";
		String projId = "id";
		doReturn(username).when(authenticationService).getLoggedInUser();
		UserBoardConfigDTO userBoardConfigDTO = userBoardConfigServiceImpl.getOrPrepareBoardConfig(ConfigLevel.PROJECT,
				projId);
		assertNotNull(userBoardConfigDTO);
		assertEquals(userBoardConfigDTO.getBasicProjectConfigId(), projId);
	}

	@Test
	public void testGetUserBoardConfig_DefaultOrPrepareBoardConfig_success() {
		String username = "testuser";
		doReturn(username).when(authenticationService).getLoggedInUser();
		KpiMasterDataFactory kpiMasterDataFactory = KpiMasterDataFactory.newInstance();
		Iterable<KpiMaster> kpiMasters = kpiMasterDataFactory.getKpiList();
		when(configHelperService.loadKpiMaster()).thenReturn(kpiMasters);
		UserBoardConfigDTO userBoardConfigDTO = userBoardConfigServiceImpl.getOrPrepareBoardConfig(ConfigLevel.USER,
				listOfReqProjects.getBasicProjectConfigIds().get(0));
		assertNotNull(userBoardConfigDTO);
	}

	@Test
	public void testGetBoardConfig_proj() {
		String username = "testuser";
		doReturn(username).when(authenticationService).getLoggedInUser();
		KpiMasterDataFactory kpiMasterDataFactory = KpiMasterDataFactory.newInstance();
		Iterable<KpiMaster> kpiMasters = kpiMasterDataFactory.getKpiList();
		when(configHelperService.loadKpiMaster()).thenReturn(kpiMasters);
		UserBoardConfigDTO userBoardConfigDTO = userBoardConfigServiceImpl.getBoardConfig(ConfigLevel.PROJECT,
				listOfReqProjects);
		assertNotNull(userBoardConfigDTO);
	}
	@Test
	public void testGetBoardConfig_user() {
		String username = "testuser";
		doReturn(username).when(authenticationService).getLoggedInUser();
		KpiMasterDataFactory kpiMasterDataFactory = KpiMasterDataFactory.newInstance();
		Iterable<KpiMaster> kpiMasters = kpiMasterDataFactory.getKpiList();
		when(configHelperService.loadKpiMaster()).thenReturn(kpiMasters);
		when(configHelperService.loadUserBoardConfig()).thenReturn(userBoardConfigMap);

		UserBoardConfigDTO userBoardConfigDTO = userBoardConfigServiceImpl.getBoardConfig(ConfigLevel.USER,
				listOfReqProjects);
		assertNotNull(userBoardConfigDTO);
	}

		@Test
	public void testGetUserBoardConfig_NoOrPrepareBoardConfigFound_success() {
		String username = "testuser";
		doReturn(username).when(authenticationService).getLoggedInUser();
		UserBoardConfigDTO userBoardConfigDTO = userBoardConfigServiceImpl.getOrPrepareBoardConfig(ConfigLevel.USER,
				listOfReqProjects.getBasicProjectConfigIds().get(0));
		assertNotNull(userBoardConfigDTO);
	}

	@Test
	public void testSaveUserBoardConfig_isshownFalse() {
		String username = "ADMIN";
		String projId = "proj1";
		UserBoardConfig data = getData(username, true);
		data.getScrum().get(0).getKpis().get(0).setShown(false);
		UserBoardConfigDTO userBoardConfigDTO1 = convertToUserBoardConfigDTO(data);
		doReturn(username).when(authenticationService).getLoggedInUser();
		when(userBoardConfigMapper.toEntity(userBoardConfigDTO1)).thenReturn(data);
		when(userBoardConfigMapper.toDto(any())).thenReturn(userBoardConfigDTO1);
		when(userBoardConfigRepository.save(data)).thenReturn(data);

		ServiceResponse response = userBoardConfigServiceImpl.saveBoardConfig(userBoardConfigDTO1, ConfigLevel.USER,
				projId);
		UserBoardConfigDTO userBoardConfig = (UserBoardConfigDTO) response.getData();
		boolean shown = userBoardConfig.getScrum().get(0).getKpis().get(0).isShown();
		assertFalse(shown);
	}

	@Test
	public void testSaveUserBoardConfig2() {
		String username = "SUPERADMIN";
		String projId = "id";
		UserBoardConfig data = getData(username, true);
		data.getScrum().get(0).getKpis().get(0).setShown(false);
		UserBoardConfigDTO userBoardConfigDTO1 = convertToUserBoardConfigDTO(data);
		doReturn(username).when(authenticationService).getLoggedInUser();
		when(userBoardConfigRepository.save(data)).thenReturn(data);
		when(userBoardConfigMapper.toEntity(userBoardConfigDTO1)).thenReturn(data);
		when(userBoardConfigMapper.toDto(any())).thenReturn(userBoardConfigDTO1);


		ServiceResponse response = userBoardConfigServiceImpl.saveBoardConfig(userBoardConfigDTO1, ConfigLevel.USER,
				projId);
		UserBoardConfigDTO userBoardConfig = (UserBoardConfigDTO) response.getData();
		boolean shown = userBoardConfig.getScrum().get(0).getKpis().get(0).isShown();
		assertFalse(shown);
	}

	@Test
	public void testGetOrPrepareBoardConfig_AddKpi() {
		String username = "testuser";
		doReturn(username).when(authenticationService).getLoggedInUser();
		KpiMasterDataFactory kpiMasterDataFactory = KpiMasterDataFactory.newInstance();
		Iterable<KpiMaster> kpiMasters = kpiMasterDataFactory.getKpiList();
		when(configHelperService.loadKpiMaster()).thenReturn(kpiMasters);
		when(kpiCategoryRepository.findAll()).thenReturn(kpiCategoryList);
		List<String> kpiCategoryList = Arrays.asList("Iteration", "Backlog", "Kpi Maturity");
		List<KpiMaster> filteredMaster = ((List<KpiMaster>) kpiMasters).stream()
				.filter(master -> (!master.getKanban() && !kpiCategoryList.contains(master.getKpiCategory())))
				.collect(Collectors.toList());
		when(kpiMasterRepository.findByKanbanAndKpiCategoryNotIn(anyBoolean(), anyList())).thenReturn(filteredMaster);
		when(kpiMasterRepository.findByKpiCategoryAndKanban(anyString(), anyBoolean())).thenReturn(filteredMaster);
		when(kpiCategoryMappingRepository.findAll()).thenReturn(kpiCategoryMappingList);
		UserBoardConfigDTO userBoardConfigDTO = userBoardConfigServiceImpl.getOrPrepareBoardConfig(ConfigLevel.USER,
				listOfReqProjects.getBasicProjectConfigIds().get(0));
		assertEquals(userBoardConfigDTO.getOthers().size(), 1);
		assertNotNull(userBoardConfigDTO);
		assertEquals(userBoardConfigDTO.getUsername(), username);
	}

	@Test
	public void testGetOrPrepareBoardConfig_NoChangeInKpis() {
		String username = "user";
		doReturn(username).when(authenticationService).getLoggedInUser();
		KpiMasterDataFactory kpiMasterDataFactory = KpiMasterDataFactory.newInstance();
		List<String> kpiIdList = Arrays.asList("kpi14", "kpi82", "kpi111", "kpi35", "kpi34", "kpi37", "kpi121",
				"kpi119", "kpi128", "kpi75", "kpi55", "kpi54", "kpi50", "kpi51", "kpi48", "kpi997", "kpi63", "kpi79",
				"kpi80", "kpi127", "kpi989");
		List<KpiMaster> kpiMasters = kpiMasterDataFactory.getSpecificKpis(kpiIdList);
		when(configHelperService.loadKpiMaster()).thenReturn(kpiMasters);
		when(kpiCategoryRepository.findAll()).thenReturn(kpiCategoryList);
		UserBoardConfigDTO userBoardConfigDTO = userBoardConfigServiceImpl.getOrPrepareBoardConfig(ConfigLevel.USER,
				listOfReqProjects.getBasicProjectConfigIds().get(0));
		assertEquals(userBoardConfigDTO.getKanban().get(0).getKpis().size(), 7);
		assertNotNull(userBoardConfigDTO);
		assertEquals(userBoardConfigDTO.getUsername(), username);
	}

	@Test
	public void testGetOrPrepareBoardConfig_DeleteKpis() {
		String username = "testuser";
		doReturn(username).when(authenticationService).getLoggedInUser();
		KpiMasterDataFactory kpiMasterDataFactory = KpiMasterDataFactory.newInstance();
		List<String> kpiIdList = Arrays.asList("kpi14", "kpi82", "kpi111", "kpi35", "kpi34", "kpi37", "kpi121",
				"kpi119", "kpi128", "kpi75", "kpi55", "kpi54", "kpi50", "kpi51", "kpi48", "kpi63", "kpi79", "kpi80",
				"kpi127", "kpi989");
		List<KpiMaster> kpiMasters = kpiMasterDataFactory.getSpecificKpis(kpiIdList);
		when(configHelperService.loadKpiMaster()).thenReturn(kpiMasters);
		when(kpiCategoryRepository.findAll()).thenReturn(kpiCategoryList);
		when(configHelperService.loadKpiMaster()).thenReturn(kpiMasters);
		when(kpiCategoryRepository.findAll()).thenReturn(kpiCategoryList);
		List<String> kpiCategoryList = Arrays.asList("Iteration", "Backlog", "Kpi Maturity");
		List<KpiMaster> filteredMaster = kpiMasters.stream()
				.filter(master -> (!master.getKanban() && !kpiCategoryList.contains(master.getKpiCategory())))
				.collect(Collectors.toList());
		when(kpiMasterRepository.findByKanbanAndKpiCategoryNotIn(anyBoolean(), anyList())).thenReturn(filteredMaster);
		when(kpiMasterRepository.findByKpiCategoryAndKanban(anyString(), anyBoolean())).thenReturn(filteredMaster);
		when(kpiCategoryMappingRepository.findAll()).thenReturn(kpiCategoryMappingList);
		UserBoardConfigDTO userBoardConfigDTO = userBoardConfigServiceImpl.getOrPrepareBoardConfig(ConfigLevel.USER,
				listOfReqProjects.getBasicProjectConfigIds().get(0));
		assertEquals(userBoardConfigDTO.getKanban().get(0).getKpis().size(), 6);
		assertNotNull(userBoardConfigDTO);
		assertEquals(userBoardConfigDTO.getUsername(), username);
	}

	@Test
	public void testGetOrPrepareBoardConfig_AddIterationKpi() {
		String username = "testuser";
		doReturn(username).when(authenticationService).getLoggedInUser();
		KpiMasterDataFactory kpiMasterDataFactory = KpiMasterDataFactory.newInstance();
		List<String> kpiIdList = Arrays.asList("kpi14", "kpi82", "kpi111", "kpi35", "kpi34", "kpi37", "kpi121",
				"kpi119", "kpi128", "kpi75", "kpi55", "kpi54", "kpi50", "kpi51", "kpi48", "kpi997", "kpi63", "kpi79",
				"kpi80", "kpi127", "kpi989");
		List<KpiMaster> kpiMasters = kpiMasterDataFactory.getSpecificKpis(kpiIdList);
		kpiMasters.addAll(kpiMasterDataFactory.getSpecificKpis(Arrays.asList("kpi124")));
		when(configHelperService.loadKpiMaster()).thenReturn(kpiMasters);
		when(kpiCategoryRepository.findAll()).thenReturn(kpiCategoryList);
		List<String> kpiCategoryList = Arrays.asList("Iteration", "Backlog", "Kpi Maturity");
		List<KpiMaster> filteredMaster = kpiMasters.stream()
				.filter(master -> (!master.getKanban() && !kpiCategoryList.contains(master.getKpiCategory())))
				.collect(Collectors.toList());
		when(kpiMasterRepository.findByKanbanAndKpiCategoryNotIn(anyBoolean(), anyList())).thenReturn(filteredMaster);
		when(kpiMasterRepository.findByKpiCategoryAndKanban("Iteration", false)).thenReturn(kpiMasters.stream()
				.filter(master -> (!master.getKanban() && "Iteration".equalsIgnoreCase(master.getKpiCategory())))
				.collect(Collectors.toList()));
		when(kpiMasterRepository.findByKpiCategoryAndKanban("KPI Maturity", false)).thenReturn(kpiMasters.stream()
				.filter(master -> (!master.getKanban() && "KPI Maturity".equalsIgnoreCase(master.getKpiCategory())))
				.collect(Collectors.toList()));

		when(kpiMasterRepository.findByKpiCategoryAndKanban("Backlog", false)).thenReturn(kpiMasters.stream()
				.filter(master -> (!master.getKanban() && "Backlog".equalsIgnoreCase(master.getKpiCategory())))
				.collect(Collectors.toList()));
		when(kpiCategoryMappingRepository.findAll()).thenReturn(kpiCategoryMappingList);
		UserBoardConfigDTO userBoardConfigDTO = userBoardConfigServiceImpl.getOrPrepareBoardConfig(ConfigLevel.USER,
				listOfReqProjects.getBasicProjectConfigIds().get(0));

		assertEquals(userBoardConfigDTO.getScrum().get(2).getKpis().size(), 6, "Previously 4 kpis now 5");
		assertNotNull(userBoardConfigDTO);
		assertEquals(userBoardConfigDTO.getUsername(), username);
	}

	private List<UserInfo> createDummyAdminUsers() {
		//Arrays.asList("proj1","proj2")
		List<UserInfo> users = Arrays.asList(
				createUserInfo("poorao", Arrays.asList(
						createProjectAccess("ROLE_PROJECT_ADMIN", Arrays.asList(
								createAccessNode("project", Arrays.asList(
										createAccessItem("proj1", "proj1"),
										createAccessItem("proj2", "proj2")
								))
						))
				)),
				createUserInfo("andtejas", Arrays.asList(
						createProjectAccess("ROLE_PROJECT_ADMIN", Arrays.asList(
								createAccessNode("project", Arrays.asList(
										createAccessItem("proj2", "proj2")
								))
						))
				)),
				createUserInfo("palaggar2", Arrays.asList(
						createProjectAccess("ROLE_SUPERADMIN", Arrays.asList(
								createAccessNode("project", Arrays.asList(
										createAccessItem("proj2","proj2" )
								))
						))
				))
		);
		return users;
	}

	private UserInfo createUserInfo(String name, List<ProjectsAccess> projectsAccess){
		UserInfo info= new UserInfo();
		info.setUsername(name);
		info.setProjectsAccess(projectsAccess);
		return info;
	}
	private ProjectsAccess createProjectAccess(String name, List<AccessNode> list){
		ProjectsAccess node= new ProjectsAccess();
		node.setRole(name);
		node.setAccessNodes(list);
		return node;
	}

	private AccessNode createAccessNode(String name, List<AccessItem> list){
		AccessNode node= new AccessNode();
		node.setAccessLevel(name);
		node.setAccessItems(list);
		return node;
	}
	private AccessItem createAccessItem(String id, String name) {
		AccessItem accessItem = new AccessItem();
		accessItem.setItemId(id);
		return accessItem;
	}

	@Test
	public void testGetOrPrepareBoardConfig_Add2IterationKpi() {
		String username = "testuser";
		doReturn(username).when(authenticationService).getLoggedInUser();
		KpiMasterDataFactory kpiMasterDataFactory = KpiMasterDataFactory.newInstance();
		List<String> kpiIdList = Arrays.asList("kpi14", "kpi82", "kpi111", "kpi35", "kpi34", "kpi37", "kpi121",
				"kpi119", "kpi128", "kpi75", "kpi55", "kpi54", "kpi50", "kpi51", "kpi48", "kpi997", "kpi63", "kpi79",
				"kpi80", "kpi127", "kpi989");
		List<KpiMaster> kpiMasters = kpiMasterDataFactory.getSpecificKpis(kpiIdList);
		kpiMasters.addAll(kpiMasterDataFactory.getSpecificKpis(Arrays.asList("kpi124", "kpi125")));
		when(configHelperService.loadKpiMaster()).thenReturn(kpiMasters);
		when(kpiCategoryRepository.findAll()).thenReturn(kpiCategoryList);
		List<String> kpiCategoryList = Arrays.asList("Iteration", "Backlog", "Kpi Maturity");
		List<KpiMaster> filteredMaster = kpiMasters.stream()
				.filter(master -> (!master.getKanban() && !kpiCategoryList.contains(master.getKpiCategory())))
				.collect(Collectors.toList());
		when(kpiMasterRepository.findByKanbanAndKpiCategoryNotIn(anyBoolean(), anyList())).thenReturn(filteredMaster);
		when(kpiMasterRepository.findByKpiCategoryAndKanban("Iteration", false)).thenReturn(kpiMasters.stream()
				.filter(master -> (!master.getKanban() && "Iteration".equalsIgnoreCase(master.getKpiCategory())))
				.collect(Collectors.toList()));
		when(kpiMasterRepository.findByKpiCategoryAndKanban("KPI Maturity", false)).thenReturn(kpiMasters.stream()
				.filter(master -> (!master.getKanban() && "KPI Maturity".equalsIgnoreCase(master.getKpiCategory())))
				.collect(Collectors.toList()));

		when(kpiMasterRepository.findByKpiCategoryAndKanban("Backlog", false)).thenReturn(kpiMasters.stream()
				.filter(master -> (!master.getKanban() && "Backlog".equalsIgnoreCase(master.getKpiCategory())))
				.collect(Collectors.toList()));
		when(kpiCategoryMappingRepository.findAll()).thenReturn(kpiCategoryMappingList);
		UserBoardConfigDTO userBoardConfigDTO = userBoardConfigServiceImpl.getOrPrepareBoardConfig(ConfigLevel.USER,
				listOfReqProjects.getBasicProjectConfigIds().get(0));
		assertEquals(userBoardConfigDTO.getScrum().get(2).getKpis().size(), 6, "Previously 4 kpis now 6");
		assertNotNull(userBoardConfigDTO);
		assertEquals(userBoardConfigDTO.getUsername(), username);
	}

	@Test
	public void testGetOrPrepareBoardConfig_AddIterationKpiIn_Middle() {
		String username = "testuser";
		doReturn(username).when(authenticationService).getLoggedInUser();
		KpiMasterDataFactory kpiMasterDataFactory = KpiMasterDataFactory.newInstance();
		List<String> kpiIdList = Arrays.asList("kpi14", "kpi82", "kpi111", "kpi35", "kpi34", "kpi37", "kpi121",
				"kpi119", "kpi128", "kpi75", "kpi55", "kpi54", "kpi50", "kpi51", "kpi48", "kpi997", "kpi63", "kpi79",
				"kpi80", "kpi127", "kpi989");
		List<KpiMaster> kpiMasters = kpiMasterDataFactory.getSpecificKpis(kpiIdList);
		kpiMasters.addAll(kpiMasterDataFactory.getSpecificKpis(Arrays.asList("kpi124")));
		when(configHelperService.loadKpiMaster()).thenReturn(kpiMasters);
		when(kpiCategoryRepository.findAll()).thenReturn(kpiCategoryList);
		List<String> kpiCategoryList = Arrays.asList("Iteration", "Backlog", "Kpi Maturity");
		List<KpiMaster> filteredMaster = kpiMasters.stream()
				.filter(master -> (!master.getKanban() && !kpiCategoryList.contains(master.getKpiCategory())))
				.collect(Collectors.toList());
		when(kpiMasterRepository.findByKanbanAndKpiCategoryNotIn(anyBoolean(), anyList())).thenReturn(filteredMaster);
		when(kpiMasterRepository.findByKpiCategoryAndKanban("Iteration", false)).thenReturn(kpiMasters.stream()
				.filter(master -> (!master.getKanban() && "Iteration".equalsIgnoreCase(master.getKpiCategory())))
				.collect(Collectors.toList()));
		when(kpiMasterRepository.findByKpiCategoryAndKanban("KPI Maturity", false)).thenReturn(kpiMasters.stream()
				.filter(master -> (!master.getKanban() && "KPI Maturity".equalsIgnoreCase(master.getKpiCategory())))
				.collect(Collectors.toList()));

		when(kpiMasterRepository.findByKpiCategoryAndKanban("Backlog", false)).thenReturn(kpiMasters.stream()
				.filter(master -> (!master.getKanban() && "Backlog".equalsIgnoreCase(master.getKpiCategory())))
				.collect(Collectors.toList()));
		when(kpiCategoryMappingRepository.findAll()).thenReturn(kpiCategoryMappingList);
		UserBoardConfigDTO userBoardConfigDTO = userBoardConfigServiceImpl.getOrPrepareBoardConfig(ConfigLevel.USER,
				listOfReqProjects.getBasicProjectConfigIds().get(0));
		assertEquals(userBoardConfigDTO.getScrum().get(2).getKpis().size(), 6, "Previously 4 kpis now 6");
		assertNotNull(userBoardConfigDTO);
		assertEquals(userBoardConfigDTO.getUsername(), username);
	}

	@Test
	public void testGetOrPrepareBoardConfig_AddIterationKpiDragDrop() {
		String username = "testuser";
		doReturn(username).when(authenticationService).getLoggedInUser();
		KpiMasterDataFactory kpiMasterDataFactory = KpiMasterDataFactory.newInstance();
		List<String> kpiIdList = Arrays.asList("kpi14", "kpi82", "kpi111", "kpi35", "kpi34", "kpi37", "kpi121",
				"kpi119", "kpi128", "kpi75", "kpi55", "kpi54", "kpi50", "kpi51", "kpi48", "kpi997", "kpi63", "kpi79",
				"kpi80", "kpi127", "kpi989");
		List<KpiMaster> kpiMasters = kpiMasterDataFactory.getSpecificKpis(kpiIdList);
		kpiMasters.addAll(kpiMasterDataFactory.getSpecificKpis(Arrays.asList("kpi124")));
		when(configHelperService.loadKpiMaster()).thenReturn(kpiMasters);
		when(kpiCategoryRepository.findAll()).thenReturn(kpiCategoryList);
		List<String> kpiCategoryList = Arrays.asList("Iteration", "Backlog", "Kpi Maturity");
		List<KpiMaster> filteredMaster = kpiMasters.stream()
				.filter(master -> (!master.getKanban() && !kpiCategoryList.contains(master.getKpiCategory())))
				.collect(Collectors.toList());
		when(kpiMasterRepository.findByKanbanAndKpiCategoryNotIn(anyBoolean(), anyList())).thenReturn(filteredMaster);
		when(kpiMasterRepository.findByKpiCategoryAndKanban("Iteration", false)).thenReturn(kpiMasters.stream()
				.filter(master -> (!master.getKanban() && "Iteration".equalsIgnoreCase(master.getKpiCategory())))
				.collect(Collectors.toList()));
		when(kpiMasterRepository.findByKpiCategoryAndKanban("KPI Maturity", false)).thenReturn(kpiMasters.stream()
				.filter(master -> (!master.getKanban() && "KPI Maturity".equalsIgnoreCase(master.getKpiCategory())))
				.collect(Collectors.toList()));
		when(kpiMasterRepository.findByKpiCategoryAndKanban("Backlog", false)).thenReturn(kpiMasters.stream()
				.filter(master -> (!master.getKanban() && "Backlog".equalsIgnoreCase(master.getKpiCategory())))
				.collect(Collectors.toList()));
		when(kpiCategoryMappingRepository.findAll()).thenReturn(kpiCategoryMappingList);

		UserBoardConfigDTO userBoardConfigDTO = userBoardConfigServiceImpl.getOrPrepareBoardConfig(ConfigLevel.USER,
				listOfReqProjects.getBasicProjectConfigIds().get(0));
		assertEquals(userBoardConfigDTO.getScrum().get(2).getKpis().size(), 6, "Previously 4 kpis now 5");
		assertNotNull(userBoardConfigDTO);
		assertEquals(userBoardConfigDTO.getUsername(), username);
	}

	@Test
	public void testDeleteUser() {
		String username = "testuser";

		// Mock the repository call
		doNothing().when(userBoardConfigRepository).deleteByUsername(username);

		// Call the method to test
		userBoardConfigServiceImpl.deleteUser(username);

		// Verify that the repository method was called
		verify(userBoardConfigRepository, times(1)).deleteByUsername(username);
	}

	@Test
	public void testDeleteProjectBoardConfig() {
		String basicProjectConfigId = "proj1";

		// Mock the repository call
		doNothing().when(userBoardConfigRepository).deleteByBasicProjectConfigId(basicProjectConfigId);

		// Call the method to test
		userBoardConfigServiceImpl.deleteProjectBoardConfig(basicProjectConfigId);

		// Verify that the repository method was called
		verify(userBoardConfigRepository, times(1)).deleteByBasicProjectConfigId(basicProjectConfigId);
	}

	UserBoardConfig getData(String username, boolean shown) {
		UserBoardConfig data = new UserBoardConfig();
		data.setUsername(username);
		data.setScrum(createScrumBoard());
		data.setKanban(createKanbanBoard());
		data.setOthers(createOthers());
		return data;
	}

	private List<Board> createOthers() {
		List<Board> boardList = new ArrayList<>();
		boardList.add(createBackLog());
		boardList.add(createKpiMaturityBoard());
		return boardList;
	}

	private Board createKpiMaturityBoard() {
		Board board = new Board();
		board.setBoardId(7);
		board.setBoardName("Kpi Maturity");
		List<BoardKpis> boardKpisList = new ArrayList<>();
		boardKpisList.add(createKpi("kpi989", "Kpi Maturity", true, true, 1));
		board.setKpis(boardKpisList);
		return board;
	}

	private Board createBackLog() {
		Board board = new Board();
		board.setBoardId(6);
		board.setBoardName("Backlog");
		List<BoardKpis> boardKpisList = new ArrayList<>();
		boardKpisList.add(createKpi("kpi79", "Test Cases Without Story Link", true, true, 1));
		boardKpisList.add(createKpi("kpi80", "Defects Without Story Link", true, true, 2));
		boardKpisList.add(createKpi("kpi127", "Production Defects Ageing", true, true, 3));
		board.setKpis(boardKpisList);
		return board;
	}

	private List<Board> createKanbanBoard() {
		List<Board> boardList = new ArrayList<>();
		boardList.add(createMyKnowHowKanbanBord());
		boardList.add(createCategoryOneKanbanBoard());
		return boardList;
	}

	private List<Board> createScrumBoard() {
		List<Board> boardList = new ArrayList<>();
		boardList.add(createMyKnowBord());
		boardList.add(createCategoryOneBoard());
		boardList.add(createIterationBoard());
		return boardList;
	}

	private Board createIterationBoard() {
		Board board = new Board();
		board.setBoardId(3);
		board.setBoardName("Iteration");
		List<BoardKpis> boardKpisList = new ArrayList<>();
		boardKpisList.add(createKpi("kpi121", "Capacity", true, true, 2));
		boardKpisList.add(createKpi("kpi119", "Work Remaining", true, true, 3));
		boardKpisList.add(createKpi("kpi128", "Work Completed", true, true, 4));
		boardKpisList.add(createKpi("kpi75", "Estimate vs Actual", true, true, 5));
		board.setKpis(boardKpisList);
		return board;

	}

	private Board createMyKnowBord() {
		Board board = new Board();
		board.setBoardId(1);
		board.setBoardName("My KnowHow");
		List<BoardKpis> boardKpisList = new ArrayList<>();
		boardKpisList.add(createKpi("kpi14", "Defect Injection Rate", true, true, 1));
		boardKpisList.add(createKpi("kpi82", "First Time Pass Rate", true, true, 2));
		boardKpisList.add(createKpi("kpi111", "Defect Density", true, true, 3));
		boardKpisList.add(createKpi("kpi35", "Defect Seepage Rate", true, true, 4));
		boardKpisList.add(createKpi("kpi34", "Defect Removal Efficiency", true, true, 5));
		boardKpisList.add(createKpi("kpi37", "Defect Rejection Rate", true, true, 6));
		board.setKpis(boardKpisList);
		return board;

	}

	private Board createMyKnowHowKanbanBord() {
		Board board = new Board();
		board.setBoardId(4);
		board.setBoardName("My KnowHOW");
		List<BoardKpis> boardKpisList = new ArrayList<>();
		boardKpisList.add(createKpi("kpi55", "Ticket Open vs Closed rate by type", true, true, 1));
		boardKpisList.add(createKpi("kpi54", "Ticket Open vs Closed rate by Priority", true, true, 2));
		boardKpisList.add(createKpi("kpi50", "Net Open Ticket Count by Priority", true, true, 3));
		boardKpisList.add(createKpi("kpi51", "Net Open Ticket Count By RCA", true, true, 4));
		boardKpisList.add(createKpi("kpi48", "Net Open Ticket By Status", true, true, 5));
		boardKpisList.add(createKpi("kpi997", "Open Ticket Ageing By Priority", true, true, 6));
		boardKpisList.add(createKpi("kpi63", "Regression Automation Coverage", true, true, 7));
		board.setKpis(boardKpisList);
		return board;

	}

	private Board createCategoryOneBoard() {
		Board board = new Board();
		board.setBoardId(2);
		board.setBoardName("Category One");
		List<BoardKpis> boardKpisList = new ArrayList<>();
		boardKpisList.add(createKpi("kpi14", "Defect Injection Rate", true, true, 1));
		boardKpisList.add(createKpi("kpi34", "Defect Removal Efficiency", true, true, 2));
		boardKpisList.add(createKpi("kpi37", "Defect Rejection Rate", true, true, 3));
		boardKpisList.add(createKpi("kpi82", "First Time Pass Rate", true, true, 4));
		boardKpisList.add(createKpi("kpi111", "Defect Density", true, true, 5));

		board.setKpis(boardKpisList);
		return board;

	}

	private Board createCategoryOneKanbanBoard() {
		Board board = new Board();
		board.setBoardId(5);
		board.setBoardName("Category One");
		List<BoardKpis> boardKpisList = new ArrayList<>();
		boardKpisList.add(createKpi("kpi51", "Net Open Ticket Count By RCA", true, true, 1));
		boardKpisList.add(createKpi("kpi48", "Net Open Ticket By Status", true, true, 2));
		boardKpisList.add(createKpi("kpi997", "Open Ticket Ageing By Priority", true, true, 3));
		boardKpisList.add(createKpi("kpi55", "Ticket Open vs Closed rate by type", true, true, 4));
		boardKpisList.add(createKpi("kpi54", "Ticket Open vs Closed rate by Priority", true, true, 5));
		boardKpisList.add(createKpi("kpi63", "Regression Automation Coverage", true, true, 6));
		boardKpisList.add(createKpi("kpi50", "Net Open Ticket Count by Priority", true, true, 7));
		board.setKpis(boardKpisList);
		return board;

	}

	private BoardKpis createKpi(String id, String name, boolean b, boolean shown, int order) {
		BoardKpis kpi = new BoardKpis();
		kpi.setKpiId(id);
		kpi.setIsEnabled(b);
		kpi.setShown(shown);
		kpi.setKpiName(name);
		kpi.setOrder(order);
		return kpi;
	}

	private UserBoardConfigDTO convertToUserBoardConfigDTO(UserBoardConfig userBoardConfig) {
		UserBoardConfigDTO userBoardConfigDTO = null;
		if (null != userBoardConfig) {
			ModelMapper mapper = new ModelMapper();
			userBoardConfigDTO = mapper.map(userBoardConfig, UserBoardConfigDTO.class);
		}
		return userBoardConfigDTO;
	}
}
