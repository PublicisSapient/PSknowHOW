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

package com.publicissapient.kpidashboard.apis.projectconfig.basic.rest;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.TreeSet;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.publicissapient.kpidashboard.apis.abac.ContextAwarePolicyEnforcement;
import com.publicissapient.kpidashboard.apis.abac.ProjectAccessManager;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.UserInfoService;
import com.publicissapient.kpidashboard.apis.data.ProjectBasicConfigDataFactory;
import com.publicissapient.kpidashboard.apis.projectconfig.basic.service.ProjectBasicConfigService;
import com.publicissapient.kpidashboard.apis.util.TestUtil;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevelSuggestion;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.dto.ProjectBasicConfigDTO;
import com.publicissapient.kpidashboard.common.model.rbac.ProjectsForAccessRequest;
import com.publicissapient.kpidashboard.common.repository.application.HierarchyLevelSuggestionRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelSuggestionsServiceImpl;

/**
 * @author narsingh9
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectBasicConfigControllerTest {

	@Mock
	UserInfoService userInfoService;
	private MockMvc mockMvc;
	@InjectMocks
	private ProjectBasicConfigController projectConfigController;
	@Mock
	private ProjectBasicConfigService projectConfigService;
	@Mock
	private HierarchyLevelSuggestionsServiceImpl hierarchyLevelSuggestionsService;
	@Mock
	private HierarchyLevelSuggestionRepository hierarchyLevelSuggestionRepository;
	@Mock
	private ContextAwarePolicyEnforcement policy;
	@Mock
	private ProjectBasicConfigRepository configRepo;
	@Mock
	private SecurityContext securityContext;

	@Mock
	private Authentication authentication;

	@Mock
	private AuthenticationService authenticationService;

	private ProjectBasicConfigDTO basicConfigDTO;

	private ProjectBasicConfig projectBasicConfig;

	@Mock
	private ProjectAccessManager projectAccessManager;

	private ModelMapper modelMapper = new ModelMapper();

	private HierarchyLevelSuggestion hierarchyLevel1Suggestion = new HierarchyLevelSuggestion();

	/**
	 * method includes pre processes for test cases
	 */
	@Before
	public void before() {
		mockMvc = MockMvcBuilders.standaloneSetup(projectConfigController).build();

		projectBasicConfig = ProjectBasicConfigDataFactory
				.newInstance("/json/basicConfig/project_basic_config_request.json").getProjectBasicConfigs().get(0);
		basicConfigDTO = modelMapper.map(projectBasicConfig, ProjectBasicConfigDTO.class);

		ProjectsForAccessRequest par = new ProjectsForAccessRequest();
		par.setProjectId("5cd16683eef5c3167c799227");
		par.setProjectName("dummy project");

		TreeSet<String> hierarchyLevel1Values = new TreeSet<>();
		hierarchyLevel1Values.add("hierarchyLevel1Value1");
		hierarchyLevel1Suggestion.setId(new ObjectId("60ed70a572dafe33d3e37111"));
		hierarchyLevel1Suggestion.setHierarchyLevelId("hierarchyLevel1Id");
		hierarchyLevel1Suggestion.setValues(hierarchyLevel1Values);
	}

	/**
	 * method includes post processes for test cases
	 */
	@After
	public void after() {
		mockMvc = null;
	}

	/**
	 * method to test add functionality
	 *
	 * add basic config
	 *
	 * @throws Exception
	 *             exception
	 */
	@Test
	public void testAddBasicConfig() throws Exception {
		SecurityContextHolder.setContext(securityContext);
		when(authenticationService.getLoggedInUser()).thenReturn("standarduser");
		when(hierarchyLevelSuggestionsService.addIfNotPresent("hierarchyLevel1Id", "hierarchyLevel1Value1"))
				.thenReturn(hierarchyLevel1Suggestion);
		this.mockMvc.perform(
				MockMvcRequestBuilders.post("/basicconfigs").content(TestUtil.convertObjectToJsonBytes(basicConfigDTO))
						.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
	}

}