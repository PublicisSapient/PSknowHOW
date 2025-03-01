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

package com.publicissapient.kpidashboard.apis.projectconfig.basic.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import com.publicissapient.kpidashboard.apis.common.service.KpiDataCacheService;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.apis.abac.ContextAwarePolicyEnforcement;
import com.publicissapient.kpidashboard.apis.abac.ProjectAccessManager;
import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.capacity.service.CapacityMasterService;
import com.publicissapient.kpidashboard.apis.cleanup.AgileDataCleanUpService;
import com.publicissapient.kpidashboard.apis.cleanup.ToolDataCleanUpServiceFactory;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.UserInfoService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.ProjectBasicConfigDataFactory;
import com.publicissapient.kpidashboard.apis.errors.ProjectNotFoundException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.projectconfig.basic.model.HierarchyResponseDTO;
import com.publicissapient.kpidashboard.apis.projectconfig.basic.service.DeleteProjectTraceLogService;
import com.publicissapient.kpidashboard.apis.projectconfig.basic.service.ProjectBasicConfigServiceImpl;
import com.publicissapient.kpidashboard.apis.projectconfig.fieldmapping.service.FieldMappingService;
import com.publicissapient.kpidashboard.apis.projectconfig.projecttoolconfig.service.ProjectToolConfigServiceImpl;
import com.publicissapient.kpidashboard.apis.rbac.accessrequests.service.AccessRequestsHelperService;
import com.publicissapient.kpidashboard.apis.testexecution.service.TestExecutionService;
import com.publicissapient.kpidashboard.common.constant.AuthType;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevelSuggestion;
import com.publicissapient.kpidashboard.common.model.application.HierarchyValue;
import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.application.dto.ProjectBasicConfigDTO;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.BoardMetadata;
import com.publicissapient.kpidashboard.common.model.rbac.AccessItem;
import com.publicissapient.kpidashboard.common.model.rbac.AccessNode;
import com.publicissapient.kpidashboard.common.model.rbac.AccessRequest;
import com.publicissapient.kpidashboard.common.model.rbac.ProjectBasicConfigNode;
import com.publicissapient.kpidashboard.common.model.rbac.ProjectsAccess;
import com.publicissapient.kpidashboard.common.model.rbac.RoleData;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.HierarchyLevelSuggestionRepository;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
import com.publicissapient.kpidashboard.common.repository.jira.BoardMetadataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.HappinessKpiDataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;

/**
 * @author narsingh9
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectBasicConfigServiceImplTest {

	List<HierarchyValue> hierarchyValueDTOList = new ArrayList<>();
	private MockMvc mockMvc;
	@InjectMocks
	private ProjectBasicConfigServiceImpl projectBasicConfigServiceImpl;

	@Mock
	private HierarchyLevelSuggestionRepository hierarchyLevelSuggestionRepository;

	@Mock
	private ContextAwarePolicyEnforcement policy;
	@Mock
	private ProjectBasicConfigRepository basicConfigRepository;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private Authentication authentication;
	@Mock
	private UserInfoService userInfoService;
	@Mock
	private FilterHelperService filterHelperService;
	@Mock
	private CacheService cacheService;
	@Mock
	private KpiDataCacheService kpiDataCacheService;
	@Mock
	private AccountHierarchyRepository accountHierarchyRepository;
	@Mock
	private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepository;
	@Mock
	private UserAuthorizedProjectsService userAuthorizedProjectsService;
	@Mock
	private TokenAuthenticationService tokenAuthenticationService;
	@Mock
	private FieldMappingService fieldMappingService;
	@Mock
	private ProjectToolConfigRepository toolRepository;
	@Mock
	private ToolDataCleanUpServiceFactory dataCleanUpServiceFactory;
	@Mock
	private DeleteProjectTraceLogService deleteProjectTraceLogService;
	@Mock
	private AgileDataCleanUpService agileDataCleanUpService;

	@Mock
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

	@Mock
	private ConfigHelperService configHelperService;
	@Mock
	private AccessRequestsHelperService accessRequestsHelperService;
	@Mock
	private ProjectAccessManager projectAccessManager;
	@Mock
	private AuthenticationService authenticationService;
	@Mock
	private ProjectBasicConfigDTO basicConfigDTO;
	@Mock
	private SprintRepository sprintRepository;
	@Mock
	private AssigneeDetailsRepository assigneeDetailsRepository;
	@Mock
	private BoardMetadataRepository boardMetadataRepository;
	@Mock
	private CapacityMasterService capacityMasterService;
	@Mock
	private TestExecutionService testExecutionService;
	@Mock
	private ProjectToolConfigServiceImpl projectToolConfigService;
	private ProjectBasicConfig basicConfig;
	private Optional<ProjectBasicConfig> basicConfigOpt = Optional.empty();
	private ProjectBasicConfig diffbasicConfig;
	private UserInfo userInfo;
	private UserInfo nonSuperadminUserInfo;
	private UserInfo projectViewerUserInfo;
	private HierarchyLevelSuggestion hierarchyLevelSuggestion;
	private AccountHierarchy accountHierarchy1;
	private AccountHierarchy accountHierarchy2;
	private AccountHierarchy accountHierarchy3;
	private KanbanAccountHierarchy accountHierarchy4;
	private KanbanAccountHierarchy accountHierarchy5;
	private KanbanAccountHierarchy accountHierarchy6;

	private ModelMapper modelMapper = new ModelMapper();

	@Mock
	private HappinessKpiDataRepository happinessKpiDataRepository;

	@Mock
	private FieldMappingRepository fieldMappingRepository;

	ProjectToolConfig listProjectTool = new ProjectToolConfig();
	FieldMapping fieldMapping;
	BoardMetadata boardMetadata;


	/**
	 * method includes pre processes for test cases
	 */
	@Before
	public void before() {
		mockMvc = MockMvcBuilders.standaloneSetup(projectBasicConfigServiceImpl).build();
		basicConfigDTO = new ProjectBasicConfigDTO();
		basicConfig = ProjectBasicConfigDataFactory.newInstance("/json/basicConfig/project_basic_config_request.json")
				.getProjectBasicConfigs().get(0);
		boardMetadata=new BoardMetadata();
		boardMetadata.setProjectBasicConfigId(new ObjectId("5fa0023dbb5fa781ccd5ac2c"));
		boardMetadata.setProjectToolConfigId(new ObjectId("5fa0023dbb5fa781ccd5ac2c"));
		boardMetadata.setMetadataTemplateCode("10");
		basicConfig.setId(new ObjectId("5f855dec29cf840345f2d111"));
		basicConfigDTO = modelMapper.map(basicConfig, ProjectBasicConfigDTO.class);
		listProjectTool.setId(new ObjectId("5fa0023dbb5fa781ccd5ac2c"));
		listProjectTool.setToolName("Jira");
		listProjectTool.setConnectionId(new ObjectId("5fb3a6412064a35b8069930a"));
		listProjectTool.setBasicProjectConfigId(new ObjectId("5fb364612064a31c9ccd517a"));
		listProjectTool.setBranch("test1");
		listProjectTool.setJobName("testing1");

		fieldMapping=FieldMappingDataFactory.newInstance("/json/default/project_field_mappings.json").getFieldMappings().get(0);

		basicConfigOpt = Optional.of(basicConfig);

		diffbasicConfig = ProjectBasicConfigDataFactory
				.newInstance("/json/basicConfig/project_basic_config_request.json").getProjectBasicConfigs().get(0);
		diffbasicConfig.setId(new ObjectId("5f855dec29cf840345f2d222"));
		diffbasicConfig.setProjectName("project2");

		userInfo = new UserInfo();
		userInfo.setUsername("SUPERADMIN");
		userInfo.setAuthType(AuthType.STANDARD);
		userInfo.setAuthorities(Lists.newArrayList("ROLE_SUPERADMIN"));

		nonSuperadminUserInfo = new UserInfo();
		nonSuperadminUserInfo.setUsername("guestProjectAdmin");
		nonSuperadminUserInfo.setAuthType(AuthType.STANDARD);
		nonSuperadminUserInfo.setAuthorities(Lists.newArrayList("ROLE_PROJECT_ADMIN"));

		projectViewerUserInfo = new UserInfo();
		projectViewerUserInfo.setUsername("guestProjectViewer");
		projectViewerUserInfo.setAuthType(AuthType.STANDARD);
		projectViewerUserInfo.setAuthorities(Lists.newArrayList("ROLE_PROJECT_VIEWER"));

		AccessItem accessItem = new AccessItem();
		accessItem.setItemId("5cd16683eef5c3167c799227");
		accessItem.setItemName("dummy project");
		AccessNode accessNode = new AccessNode();
		accessNode.setAccessItems(Lists.newArrayList(accessItem));
		ProjectsAccess superAdminProjectAccess = new ProjectsAccess();
		superAdminProjectAccess.setRole("ROLE_SUPERADMIN");
		superAdminProjectAccess.setAccessNodes(Lists.newArrayList());
		userInfo.setProjectsAccess(Lists.newArrayList(superAdminProjectAccess));

		ProjectsAccess projectaccess = new ProjectsAccess();
		projectaccess.setRole("ROLE_PROJECT_ADMIN");
		projectaccess.setAccessNodes(Lists.newArrayList(accessNode));
		nonSuperadminUserInfo.setProjectsAccess(Lists.newArrayList(projectaccess));

		ProjectsAccess viewerProjectAccess = new ProjectsAccess();
		viewerProjectAccess.setRole("ROLE_SUPERADMIN");
		viewerProjectAccess.setAccessNodes(Lists.newArrayList());
		projectViewerUserInfo.setProjectsAccess(Lists.newArrayList(viewerProjectAccess));

		hierarchyLevelSuggestion = new HierarchyLevelSuggestion();
		TreeSet<String> values = new TreeSet<>();
		values.add("hierarchyLevel1Value1");
		values.add("hierarchyLevel1Value2");
		hierarchyLevelSuggestion.setHierarchyLevelId("hierarchyLevel1Id");
		hierarchyLevelSuggestion.setValues(values);

		accountHierarchy1 = new AccountHierarchy();
		accountHierarchy1.setPath("FCA,FCA,fs");
		accountHierarchy1.setFilterCategoryId(new ObjectId("5ca455aa70c53c4f50076e34"));
		accountHierarchy1.setLabelName("Project");
		accountHierarchy1.setBasicProjectConfigId(new ObjectId("5ca455aa70c53c4f50076e34"));

		accountHierarchy2 = new AccountHierarchy();
		accountHierarchy2.setPath("FCA,fs");
		accountHierarchy2.setFilterCategoryId(new ObjectId("5ca455aa70c53c4f50076e35"));
		accountHierarchy2.setLabelName("");
		accountHierarchy2.setBasicProjectConfigId(new ObjectId("5ca455aa70c53c4f50076e34"));

		accountHierarchy3 = new AccountHierarchy();
		accountHierarchy3.setPath("FCA,fs");
		accountHierarchy3.setFilterCategoryId(new ObjectId("5ca455aa70c53c4f50076e36"));
		accountHierarchy3.setLabelName("hierarchyLevel3Id");
		accountHierarchy3.setBasicProjectConfigId(new ObjectId("5ca455aa70c53c4f50076e37"));

		accountHierarchy4 = new KanbanAccountHierarchy();
		accountHierarchy4.setPath("FCA,FCA,fs");
		accountHierarchy4.setFilterCategoryId(new ObjectId("5ca455aa70c53c4f50076e34"));
		accountHierarchy4.setLabelName("Project");
		accountHierarchy4.setBasicProjectConfigId(new ObjectId("5ca455aa70c53c4f50076e34"));

		accountHierarchy5 = new KanbanAccountHierarchy();
		accountHierarchy5.setPath("FCA,fs");
		accountHierarchy5.setFilterCategoryId(new ObjectId("5ca455aa70c53c4f50076e35"));
		accountHierarchy5.setLabelName("hierarchyLevel3Id");
		accountHierarchy5.setBasicProjectConfigId(new ObjectId("5ca455aa70c53c4f50076e34"));

		accountHierarchy6 = new KanbanAccountHierarchy();
		accountHierarchy6.setPath("FCA,fs");
		accountHierarchy6.setFilterCategoryId(new ObjectId("5ca455aa70c53c4f50076e36"));
		accountHierarchy6.setLabelName("hierarchyLevel3Id");
		accountHierarchy6.setBasicProjectConfigId(new ObjectId("5ca455aa70c53c4f50076e37"));

	}

	/**
	 * method includes post processes for test cases
	 */
	@After
	public void after() {
		mockMvc = null;
	}

	/**
	 * test add config
	 */
	@Test
	public void addConfigTest_superAdminAddProject_success() {
		when(basicConfigRepository.save(any(ProjectBasicConfig.class))).thenReturn(basicConfig);
		SecurityContextHolder.setContext(securityContext);
		when(authenticationService.getLoggedInUser()).thenReturn("SUPERADMIN");
		UserInfo userInfo = new UserInfo();
		userInfo.setUsername("GUEST");
		userInfo.setAuthType(AuthType.STANDARD);
		userInfo.setAuthorities(Lists.newArrayList("ROLE_GUEST"));
		when(projectAccessManager.getUserInfo(any())).thenReturn(userInfo);
		ServiceResponse response = projectBasicConfigServiceImpl.addBasicConfig(basicConfigDTO);
		assertThat("Status: ", response.getSuccess(), equalTo(true));
	}

	/**
	 * test add config when project with same name already exist.
	 */
	@Test
	public void addConfigTest_projectNameAlreadyExist_failure() {
		when(basicConfigRepository.findByProjectName(any(String.class))).thenReturn(basicConfig);
		ServiceResponse response = projectBasicConfigServiceImpl.addBasicConfig(basicConfigDTO);
		assertThat("Status: ", response.getSuccess(), equalTo(false));
	}

	/**
	 * test add config when save operation return null.
	 */
	@Test
	public void addConfigTest_saveOperation_failure() {
		when(basicConfigRepository.findByProjectName(any())).thenReturn(basicConfig);
		ServiceResponse response = projectBasicConfigServiceImpl.addBasicConfig(basicConfigDTO);
		assertThat("Status: ", response.getSuccess(), equalTo(false));
	}

	/**
	 * test add config non superadmin.
	 */
	@Test
	public void addConfigTest_nonSuperadminProjectViewerAddProject_success() {
		when(basicConfigRepository.save(any(ProjectBasicConfig.class))).thenReturn(basicConfig);
		SecurityContextHolder.setContext(securityContext);
		when(authenticationService.getLoggedInUser()).thenReturn("guest");
		UserInfo userInfo = new UserInfo();
		userInfo.setUsername("GUEST");
		userInfo.setAuthType(AuthType.STANDARD);
		userInfo.setAuthorities(Lists.newArrayList("ROLE_GUEST"));
		when(projectAccessManager.getUserInfo(any())).thenReturn(userInfo);
		ServiceResponse response = projectBasicConfigServiceImpl.addBasicConfig(basicConfigDTO);
		assertThat("Status: ", response.getSuccess(), equalTo(true));
	}

	/**
	 * test add config non superadmin.
	 */
	@Test
	public void addConfigTest_nonSuperadminAdd_success() {
		when(basicConfigRepository.save(any(ProjectBasicConfig.class))).thenReturn(basicConfig);
		SecurityContextHolder.setContext(securityContext);
		when(authenticationService.getLoggedInUser()).thenReturn("guest");
		UserInfo userInfo = new UserInfo();
		userInfo.setUsername("GUEST");
		userInfo.setAuthType(AuthType.STANDARD);
		userInfo.setAuthorities(Lists.newArrayList("ROLE_GUEST"));
		when(projectAccessManager.getUserInfo(any())).thenReturn(userInfo);
		ServiceResponse response = projectBasicConfigServiceImpl.addBasicConfig(basicConfigDTO);
		assertThat("Status: ", response.getSuccess(), equalTo(true));
	}

	@Test
	public void addConfigTest_nonSuperadminCloneProject_success() {
		basicConfig.setClonedFrom(new ObjectId("5fd9ab0995fe13000165d0ba"));
		when(basicConfigRepository.save(any(ProjectBasicConfig.class))).thenReturn(basicConfig);
		SecurityContextHolder.setContext(securityContext);
		when(authenticationService.getLoggedInUser()).thenReturn("guest");
		UserInfo userInfo = new UserInfo();
		userInfo.setUsername("GUEST");
		userInfo.setAuthType(AuthType.STANDARD);
		userInfo.setAuthorities(Lists.newArrayList("ROLE_GUEST"));
		when(projectAccessManager.getUserInfo(any())).thenReturn(userInfo);
		when(projectToolConfigService.getProjectToolConfigsByProjectId(any())).thenReturn(Arrays.asList(listProjectTool));
		when(projectToolConfigService.saveProjectToolConfigs(any())).thenReturn(Arrays.asList(listProjectTool));
		when(fieldMappingService.getFieldMappingByBasicconfigId(anyString())).thenReturn(fieldMapping);
		when(boardMetadataRepository.findByProjectBasicConfigId(any())).thenReturn(boardMetadata);
		ServiceResponse response = projectBasicConfigServiceImpl.addBasicConfig(basicConfigDTO);
		assertThat("Status: ", response.getSuccess(), equalTo(true));
	}

	@Test
	public void addConfigTest_nonSuperadminCloneProject_success1() {
		basicConfig.setClonedFrom(new ObjectId("5fd9ab0995fe13000165d0ba"));
		when(basicConfigRepository.save(any(ProjectBasicConfig.class))).thenReturn(basicConfig);
		SecurityContextHolder.setContext(securityContext);
		when(authenticationService.getLoggedInUser()).thenReturn("guest");
		UserInfo userInfo = new UserInfo();
		userInfo.setUsername("GUEST");
		userInfo.setAuthType(AuthType.STANDARD);
		userInfo.setAuthorities(Lists.newArrayList("ROLE_GUEST"));
		when(projectAccessManager.getUserInfo(any())).thenReturn(userInfo);
		listProjectTool.setToolName("Jenkins");
		ServiceResponse response = projectBasicConfigServiceImpl.addBasicConfig(basicConfigDTO);
		assertThat("Status: ", response.getSuccess(), equalTo(true));
	}

	/**
	 * test add config non superadmin project viewer.
	 */
	@Test
	public void addConfigTest_nonSuperadmin_success() {
		when(basicConfigRepository.save(any(ProjectBasicConfig.class))).thenReturn(basicConfig);
		SecurityContextHolder.setContext(securityContext);
		when(authenticationService.getLoggedInUser()).thenReturn("guest");
		UserInfo userInfo = new UserInfo();
		userInfo.setUsername("GUEST");
		userInfo.setAuthType(AuthType.STANDARD);
		userInfo.setAuthorities(Lists.newArrayList("ROLE_GUEST"));
		when(projectAccessManager.getUserInfo(any())).thenReturn(userInfo);
		ServiceResponse response = projectBasicConfigServiceImpl.addBasicConfig(basicConfigDTO);
		assertThat("Status: ", response.getSuccess(), equalTo(true));
	}

	/**
	 * test update config for scrum
	 */
	@Test
	public void updateConfigTest_scrumHierarchy_success() {
		basicConfig.setSaveAssigneeDetails(true);
		basicConfig.setIsKanban(false);
		when(basicConfigRepository.findById(any())).thenReturn(basicConfigOpt);
		when(basicConfigRepository.findByProjectNameAndIdNot(any(), any())).thenReturn(null);
		when(basicConfigRepository.save(any(ProjectBasicConfig.class))).thenReturn(basicConfig);
		List<ProcessorExecutionTraceLog> traceLogs= new ArrayList<>();
		traceLogs.add(new ProcessorExecutionTraceLog());
		when(processorExecutionTraceLogRepository.findByProcessorNameAndBasicProjectConfigIdIn(anyString(),anyList())).thenReturn(traceLogs);
		when(assigneeDetailsRepository.findByBasicProjectConfigId(any())).thenReturn(new AssigneeDetails());
		ServiceResponse response = projectBasicConfigServiceImpl.updateBasicConfig("5f855dec29cf840345f2deef",
				basicConfigDTO);
		assertThat("Status: ", response.getSuccess(), equalTo(true));
	}

	/**
	 * test update config for kanban
	 */
	@Test
	public void updateConfigTest_kanbanHierarchy_success() {
		basicConfig.setIsKanban(true);
		when(basicConfigRepository.findById(any())).thenReturn(basicConfigOpt);
		when(basicConfigRepository.findByProjectNameAndIdNot(any(), any())).thenReturn(null);
		when(basicConfigRepository.save(any(ProjectBasicConfig.class))).thenReturn(basicConfig);
		ServiceResponse response = projectBasicConfigServiceImpl.updateBasicConfig("5f855dec29cf840345f2deef",
				basicConfigDTO);
		assertThat("Status: ", response.getSuccess(), equalTo(true));
	}

	/**
	 * test update config when referenced object not found.
	 */
	@Test
	public void updateConfigTest_configNotFoundInDB_failure() {
		when(basicConfigRepository.findById(any())).thenReturn(Optional.ofNullable(null));
		ServiceResponse response = projectBasicConfigServiceImpl.updateBasicConfig("5f855dec29cf840345f2deef",
				basicConfigDTO);
		assertThat("Status: ", response.getSuccess(), equalTo(false));
	}

	/**
	 * test update config when project name to be updated already present.
	 */
	@Test
	public void updateConfigTest_diffIdSameName_failure() {
		when(basicConfigRepository.findById(any())).thenReturn(basicConfigOpt);
		when(basicConfigRepository.findByProjectNameAndIdNot(any(String.class), any(ObjectId.class)))
				.thenReturn(diffbasicConfig);
		ServiceResponse response = projectBasicConfigServiceImpl.updateBasicConfig("5f855dec29cf840345f2deef",
				basicConfigDTO);
		assertThat("Status: ", response.getSuccess(), equalTo(false));
	}

	/**
	 * test update config when save method return false.
	 */
	@Test
	public void updateConfigTest_updateNotOccured_failure() {
		when(basicConfigRepository.findById(any())).thenReturn(basicConfigOpt);
		when(basicConfigRepository.findByProjectNameAndIdNot(any(), any())).thenReturn(basicConfig);
		ServiceResponse response = projectBasicConfigServiceImpl.updateBasicConfig("5f855dec29cf840345f2deef",
				basicConfigDTO);
		assertThat("Status: ", response.getSuccess(), equalTo(false));
	}

	/**
	 * test list all configs
	 */
	@Test
	public void listAllProjectConfigsTestSuperAdminRole() {
		List<ProjectBasicConfig> listOfProjectDetails = new ArrayList<>();
		Mockito.when(userAuthorizedProjectsService.ifSuperAdminUser()).thenReturn(true);
		listOfProjectDetails.add(new ProjectBasicConfig());
		Mockito.when(basicConfigRepository.findAll()).thenReturn(listOfProjectDetails);
		List<ProjectBasicConfig> list = projectBasicConfigServiceImpl.getAllProjectsBasicConfigs();
		assertThat("response list size: ", list.size(), equalTo(1));

	}

	/**
	 * test list particular project config for superadmin role
	 */
	@Test
	public void listProjectConfigTestSuperAdminRole() {
		ObjectId projectId = new ObjectId();
		Mockito.when(userAuthorizedProjectsService.ifSuperAdminUser()).thenReturn(true);
		ProjectBasicConfig project = new ProjectBasicConfig();
		project.setId(projectId);
		Optional<ProjectBasicConfig> projectOpt = Optional.of(project);
		Mockito.when(basicConfigRepository.findById(projectId)).thenReturn(projectOpt);
		ProjectBasicConfig config = projectBasicConfigServiceImpl.getProjectBasicConfigs(projectId.toString());
		assertThat("response : ", config.getId(), equalTo(projectId));
	}

	/**
	 * test list particular project config for non-superadmin role
	 */
	@Test
	public void listProjectConfigTestNonSuperAdminRole() {
		ObjectId projectId = new ObjectId();
		Mockito.when(userAuthorizedProjectsService.ifSuperAdminUser()).thenReturn(false);
		ProjectBasicConfig userproject = new ProjectBasicConfig();
		userproject.setId(projectId);
		Optional<ProjectBasicConfig> userprojectOpt = Optional.of(userproject);
		Set<String> userProjIds = new HashSet<>();
		userProjIds.add(projectId.toString());
		Mockito.when(tokenAuthenticationService.getUserProjects()).thenReturn(userProjIds);
		Mockito.when(basicConfigRepository.findById(projectId)).thenReturn(userprojectOpt);
		ProjectBasicConfig config = projectBasicConfigServiceImpl.getProjectBasicConfigs(projectId.toString());
		assertThat("response : ", config.getId(), equalTo(projectId));
	}

	/**
	 * test list particular project config for non-superadmin role
	 */
	@Test
	public void listProjectConfigTestNonSuperAdminRoleNotMatching() {
		ObjectId projectId = new ObjectId();
		ObjectId projectId2 = new ObjectId();
		Mockito.when(userAuthorizedProjectsService.ifSuperAdminUser()).thenReturn(false);
		Set<String> userProjIds = new HashSet<>();
		userProjIds.add(projectId2.toString());
		Mockito.when(tokenAuthenticationService.getUserProjects()).thenReturn(userProjIds);
		ProjectBasicConfig config = projectBasicConfigServiceImpl.getProjectBasicConfigs(projectId.toString());
		assertNull(config);
	}

	/**
	 * test list a project config for non-superadmin role
	 */

	@Test
	public void listAllProjectConfigTestNonSuperAdminRole() {
		ObjectId projectId = new ObjectId();
		Mockito.when(userAuthorizedProjectsService.ifSuperAdminUser()).thenReturn(false);
		ProjectBasicConfig userproject = new ProjectBasicConfig();
		List<ProjectBasicConfig> userprojectList = new ArrayList<>();
		userprojectList.add(userproject);
		Set<String> userProjIds = new HashSet<>();
		userProjIds.add(projectId.toString());
		Mockito.when(tokenAuthenticationService.getUserProjects()).thenReturn(userProjIds);
		Set<ObjectId> projIdSet = new HashSet<>();
		projIdSet.add(projectId);
		Mockito.when(basicConfigRepository.findByIdIn(projIdSet)).thenReturn(userprojectList);
		List<ProjectBasicConfig> list = projectBasicConfigServiceImpl.getAllProjectsBasicConfigs();
		assertThat("response list size: ", list.size(), equalTo(1));
	}

	/**
	 * test list particular project config for non-superadmin role
	 */
	@Test
	public void listProjectConfigTestNonSuperAdminRoleNoProject() {
		ObjectId projectId = new ObjectId();
		Mockito.when(userAuthorizedProjectsService.ifSuperAdminUser()).thenReturn(false);
		Set<String> userProjIds = new HashSet<>();
		Mockito.when(tokenAuthenticationService.getUserProjects()).thenReturn(userProjIds);
		ProjectBasicConfig config = projectBasicConfigServiceImpl.getProjectBasicConfigs(projectId.toString());
		assertNull(config);
	}

	/**
	 * test list particular project config withoutPermission
	 */

	@Test
	public void getAllProjectsBasicConfigsWithoutPermission() {
		List<ProjectBasicConfig> listOfProjectDetails = new ArrayList<>();
		listOfProjectDetails.add(new ProjectBasicConfig());
		Mockito.when(basicConfigRepository.findAll()).thenReturn(listOfProjectDetails);
		List<ProjectBasicConfig> list = projectBasicConfigServiceImpl.getAllProjectsBasicConfigsWithoutPermission();
		assertThat("response list size: ", list.size(), equalTo(1));

	}

	@Test
	public void deleteProject_Success() {
		String id = "5fc4d61f80b6350f048a93e5";
		ObjectId basicProjectConfigId = new ObjectId(id);

		ProjectBasicConfig p1 = new ProjectBasicConfig();
		p1.setId(basicProjectConfigId);
		p1.setProjectName("Test");

		Optional<ProjectBasicConfig> p1Opt = Optional.of(p1);
		AccessRequest accessRequestsData = createAccessRequestData();
		when(toolRepository.findByBasicProjectConfigId(any(ObjectId.class))).thenReturn(createTools());
		when(userAuthorizedProjectsService.ifSuperAdminUser()).thenReturn(true);
		when(basicConfigRepository.findById(any(ObjectId.class))).thenReturn(p1Opt);
		when(dataCleanUpServiceFactory.getService(ProcessorConstants.JIRA)).thenReturn(agileDataCleanUpService);
		doNothing().when(agileDataCleanUpService).clean(anyString());
		doNothing().when(toolRepository).deleteById(any(ObjectId.class));
		doNothing().when(fieldMappingService).deleteByBasicProjectConfigId(any(ObjectId.class));
		doNothing().when(basicConfigRepository).delete(any(ProjectBasicConfig.class));
		doNothing().when(deleteProjectTraceLogService).save(any(ProjectBasicConfig.class));
		when(accessRequestsHelperService.getAccessRequestsByProject(anyString()))
				.thenReturn(Arrays.asList(accessRequestsData));
		projectBasicConfigServiceImpl.deleteProject(id);

		verify(basicConfigRepository, times(1)).delete(p1);
		verify(toolRepository, times(1)).deleteById(new ObjectId("5fc4d61f80b6350f048a93e6"));

	}


	@Test
	public void deleteAssigneeDetails() {
		String id = "5fc4d61f80b6350f048a93e5";
		ObjectId basicProjectConfigId = new ObjectId(id);

		ProjectBasicConfig p1 = new ProjectBasicConfig();
		p1.setId(basicProjectConfigId);
		p1.setProjectName("Test");

		Optional<ProjectBasicConfig> p1Opt = Optional.of(p1);
		AccessRequest accessRequestsData = createAccessRequestData();
		when(toolRepository.findByBasicProjectConfigId(any(ObjectId.class))).thenReturn(createTools());
		when(userAuthorizedProjectsService.ifSuperAdminUser()).thenReturn(true);
		when(basicConfigRepository.findById(any(ObjectId.class))).thenReturn(p1Opt);
		when(assigneeDetailsRepository.findByBasicProjectConfigId(anyString())).thenReturn(new AssigneeDetails());
		when(dataCleanUpServiceFactory.getService(ProcessorConstants.JIRA)).thenReturn(agileDataCleanUpService);
		doNothing().when(agileDataCleanUpService).clean(anyString());
		doNothing().when(toolRepository).deleteById(any(ObjectId.class));
		doNothing().when(fieldMappingService).deleteByBasicProjectConfigId(any(ObjectId.class));
		doNothing().when(basicConfigRepository).delete(any(ProjectBasicConfig.class));
		doNothing().when(deleteProjectTraceLogService).save(any(ProjectBasicConfig.class));
		when(accessRequestsHelperService.getAccessRequestsByProject(anyString()))
				.thenReturn(Arrays.asList(accessRequestsData));
		projectBasicConfigServiceImpl.deleteProject(id);

		verify(basicConfigRepository, times(1)).delete(p1);
		verify(toolRepository, times(1)).deleteById(new ObjectId("5fc4d61f80b6350f048a93e6"));

	}



	private AccessRequest createAccessRequestData() {
		AccessRequest accessRequestsData = new AccessRequest();
		AccessNode accessNode = new AccessNode();
		accessRequestsData.setAccessNode(accessNode);
		accessRequestsData.setId(new ObjectId("605aaf595a160c3fe46fdbbc"));

		RoleData roleData = new RoleData();
		roleData.setId(new ObjectId("605aaf595a160c3fe46fdbba"));
		roleData.setCreatedDate(new Date());
		roleData.setIsDeleted("False");
		roleData.setLastModifiedDate(new Date());
		roleData.setRoleName("ROLE_PROJECT_VIEWER");
		accessRequestsData.setRole("ROLE_PROJECT_VIEWER");

		return accessRequestsData;

	}

	@Test(expected = ProjectNotFoundException.class)
	public void deleteProject_Exception() {
		String id = "5fc4d61f80b6350f048a93e5";
		ObjectId basicProjectConfigId = new ObjectId(id);
		ProjectBasicConfig p1 = new ProjectBasicConfig();
		p1.setId(basicProjectConfigId);
		projectBasicConfigServiceImpl.deleteProject(id);

	}

	private List<ProjectToolConfig> createTools() {
		List<ProjectToolConfig> tools = new ArrayList<>();
		ProjectToolConfig t1 = new ProjectToolConfig();
		t1.setId(new ObjectId("5fc4d61f80b6350f048a93e6"));
		t1.setToolName(ProcessorConstants.JIRA);
		tools.add(t1);

		return tools;

	}

	/**
	 * Test getAllProjectsBasicConfigsDTOWithoutPermissionTest method
	 */
	@Test
	public void getAllProjectsBasicConfigsDTOWithoutPermissionTest() {
		Map<String, ProjectBasicConfig> mapOfProjectDetails = new HashMap<>();
		mapOfProjectDetails.put(basicConfig.getId().toHexString(), basicConfig);
		mapOfProjectDetails.put(diffbasicConfig.getId().toHexString(), diffbasicConfig);

		Mockito.when(cacheService.cacheProjectConfigMapData()).thenReturn(mapOfProjectDetails);
		List<ProjectBasicConfigDTO> list = projectBasicConfigServiceImpl
				.getAllProjectsBasicConfigsDTOWithoutPermission();
		assertThat("response list size: ", list.size(), equalTo(2));
	}

	/**
	 * Test getAllProjectsBasicConfigsDTOWithoutPermissionTest map method
	 */
	@Test
	public void getBasicConfigsDTOMapWithoutPermissionTest() {
		Map<String, ProjectBasicConfig> mapOfProjectDetails = new HashMap<>();
		mapOfProjectDetails.put(basicConfig.getId().toHexString(), basicConfig);
		mapOfProjectDetails.put(diffbasicConfig.getId().toHexString(), diffbasicConfig);

		Mockito.when(cacheService.cacheProjectConfigMapData()).thenReturn(mapOfProjectDetails);
		Map<String, ProjectBasicConfigDTO> list = projectBasicConfigServiceImpl
				.getBasicConfigsDTOMapWithoutPermission();
		assertThat("response list size: ", list.size(), equalTo(2));
	}


	@Test
	public void testFindLeaf() {
		// Create a sample tree structure for testing
		ProjectBasicConfigNode root = new ProjectBasicConfigNode();
		root.setGroupName("root");
		root.setValue("root");
		ProjectBasicConfigNode child1 = new ProjectBasicConfigNode();
		child1.setGroupName("child1");
		child1.setValue("child1");
		child1.setParent(Arrays.asList(root));
		ProjectBasicConfigNode child2 = new ProjectBasicConfigNode();
		child2.setGroupName("child2");
		child2.setValue("child2");
		child2.setParent(Arrays.asList(root));

		ProjectBasicConfigNode grandchild1 = new ProjectBasicConfigNode();
		grandchild1.setGroupName("grandchild1");
		grandchild1.setValue("grandchild1");
		grandchild1.setParent(Arrays.asList(child1));

		ProjectBasicConfigNode grandchild2 = new ProjectBasicConfigNode();
		grandchild2.setGroupName("grandchild2");
		grandchild2.setValue("grandchild2");
		grandchild2.setParent(Arrays.asList(child2));

		root.setChildren(Arrays.asList(child1));
		root.setChildren(Arrays.asList(child2));
		child1.setChildren(Arrays.asList(grandchild1));
		child2.setChildren(Arrays.asList(grandchild2));
		grandchild1.setChildren(Arrays.asList());
		grandchild2.setChildren(Arrays.asList());

		// Initialize the list to store leaf nodes
		List<ProjectBasicConfigNode> leafNodes = new ArrayList<>();
		List<ProjectBasicConfigNode> parentNode = new ArrayList<>();
		List<ProjectBasicConfigNode> childNodes = new ArrayList<>();

		// Call the findLeaf method
		projectBasicConfigServiceImpl.findLeaf(root, leafNodes);
		projectBasicConfigServiceImpl.findParents(Arrays.asList(child1,child2), parentNode);
		projectBasicConfigServiceImpl.findChildren(child1, childNodes);
		ProjectBasicConfigNode node = projectBasicConfigServiceImpl.findNode(root, "root", "root");
		ProjectBasicConfigNode childNode = projectBasicConfigServiceImpl.findNode(root, "child2", "child2");

		// Verify the result
		assertEquals(1, leafNodes.size());
		assertEquals(2, parentNode.size());
		assertEquals(1, childNodes.size());
		assertEquals(node, root);
	}

	@Test
	public void creatTree(){
		Mockito.when(basicConfigRepository.findAll()).thenReturn(Arrays.asList(basicConfig));
		projectBasicConfigServiceImpl.getBasicConfigTree();
	}

	@Test
	public void addConfigTest_superAdminAddProjects() {
		when(basicConfigRepository.save(any(ProjectBasicConfig.class))).thenReturn(basicConfig);
		SecurityContextHolder.setContext(securityContext);
		when(authenticationService.getLoggedInUser()).thenReturn("SUPERADMIN");
		when(projectAccessManager.getAccessRoleOfNearestParent(any(),any())).thenReturn(Constant.ROLE_SUPERADMIN);
		ServiceResponse response = projectBasicConfigServiceImpl.addBasicConfig(basicConfigDTO);
		assertThat("Status: ", response.getSuccess(), equalTo(true));
	}


	@Test
	public void testFilterHierarchyDTOsWithConnectedTools() {
		// Mock the projectToolConfigMapData
		Map<ObjectId, Map<String, List<ProjectToolConfig>>> projectToolConfigMapData = new HashMap<>();
		ObjectId projectId1 = new ObjectId("5f855dec29cf840345f2d111");
		ObjectId projectId2 = new ObjectId("5f855dec29cf840345f2d222");

		ProjectToolConfig connectedTool = new ProjectToolConfig();
		connectedTool.setToolName("JIRA");
		ProjectToolConfig disconnectedTool = new ProjectToolConfig();
		disconnectedTool.setToolName("GIT");

		Map<String, List<ProjectToolConfig>> toolConfigMap1 = new HashMap<>();
		toolConfigMap1.put("tool1", Arrays.asList(connectedTool));
		Map<String, List<ProjectToolConfig>> toolConfigMap2 = new HashMap<>();
		toolConfigMap2.put("tool1", Arrays.asList(disconnectedTool));

		projectToolConfigMapData.put(projectId1, toolConfigMap1);
		projectToolConfigMapData.put(projectId2, toolConfigMap2);

		when(cacheService.cacheProjectToolConfigMapData()).thenReturn(projectToolConfigMapData);

		// Create sample HierarchyResponseDTOs
		HierarchyResponseDTO dto1 = new HierarchyResponseDTO();
		dto1.setProjectId(projectId1.toString());
		HierarchyResponseDTO dto2 = new HierarchyResponseDTO();
		dto2.setProjectId(projectId2.toString());

		List<HierarchyResponseDTO> hierarchyResponseDTOS = Arrays.asList(dto1, dto2);

		// Call the method
		List<HierarchyResponseDTO> result = projectBasicConfigServiceImpl.filterHierarchyDTOsWithConnectedTools(hierarchyResponseDTOS);

		// Assert the result
		assertEquals(2, result.size());
		assertEquals(dto1.getProjectId(), result.get(0).getProjectId());
	}
}