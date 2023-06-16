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
package com.publicissapient.kpidashboard.apis.rbac.accessrequests.rest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicissapient.kpidashboard.apis.auth.service.UserTokenDeletionService;
import com.publicissapient.kpidashboard.apis.rbac.accessrequests.service.AccessRequestsHelperService;
import com.publicissapient.kpidashboard.common.model.rbac.AccessRequestsDataDTO;
import com.publicissapient.kpidashboard.common.model.rbac.Permissions;
import com.publicissapient.kpidashboard.common.model.rbac.ProjectsForAccessRequest;
import com.publicissapient.kpidashboard.common.model.rbac.RoleData;

/**
 * @author anamital
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class AccessRequestsControllerTest {

	ObjectMapper mapper = new ObjectMapper();
	private MockMvc mockMvc;
	private AccessRequestsDataDTO testAccessRequestsData;
	private String testUsername;
	private String testId;
	private String testStatus;
	@InjectMocks
	private AccessRequestsController accessRequestsController;
	@Mock
	private AccessRequestsHelperService accessRequestsHelperService;
	@Mock
	private UserTokenDeletionService userTokenDeletionService;

	/**
	 * method includes preprocesses for test cases
	 */
	@Before
	public void before() {
		testUsername = "user1";
		testId = "5dbfcc60e645ca2ee4075381";
		testStatus = "Pending";

		mockMvc = MockMvcBuilders.standaloneSetup(accessRequestsController).build();

		testAccessRequestsData = new AccessRequestsDataDTO();
		testAccessRequestsData.setUsername("UnitTest");

		List<ProjectsForAccessRequest> testProjects = new ArrayList<>();
		testProjects.add(createProjectsForAccessRequest("TestProjectForAccessRequest1", "46290"));
		testProjects.add(createProjectsForAccessRequest("TestProjectForAccessRequest2", "64920"));
		testAccessRequestsData.setProjects(testProjects);

		List<RoleData> roles = new ArrayList<>();
		RoleData testRoleData = new RoleData();
		testRoleData.setId(new ObjectId("5ca455aa70c53c4f50076e34"));
		testRoleData.setRoleName("ROLE_SUPERADMIN");
		testRoleData.setRoleDescription("Pending");
		List<Permissions> testPermissions = new ArrayList<Permissions>();

		Permissions perm1 = new Permissions();
		perm1.setPermissionName("TestProjectForRole1");
		perm1.setResourceId(new ObjectId("5ca455aa70c53c4f50076e34"));
		perm1.setResourceName("resource1");

		testPermissions.add(perm1);

		Permissions perm2 = new Permissions();
		perm2.setPermissionName("TestProjectForRole1");
		perm2.setResourceId(new ObjectId("5ca455aa70c53c4f50076e34"));
		perm2.setResourceName("resource1");

		testPermissions.add(perm2);
		testRoleData.setPermissions(testPermissions);

		roles.add(testRoleData);
		testAccessRequestsData.setRoles(roles);
	}

	/**
	 * method includes post processes for test cases
	 */
	@After
	public void after() {
		mockMvc = null;
		testAccessRequestsData = null;
		testId = null;
		testUsername = null;
		testStatus = null;
	}

	/**
	 * method to test GET /accessrequests restPoint ;
	 * 
	 * Get all access requests
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetAllAccessRequests() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/accessrequests/").contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
	}

	/**
	 * method to test GET /accessrequests/user/{username} restPoint ; Get access
	 * requests created by username
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetAccessRequestByUsername() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/accessrequests/user/" + testUsername)
				.contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isOk());
	}

	/**
	 * method to test GET /accessrequests/status/{status} restPoint ; Get access
	 * requests with current status
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetAccessRequestByStatus() throws Exception {
		testStatus = "Pending";
		mockMvc.perform(MockMvcRequestBuilders.get("/accessrequests/status/" + testStatus)
				.contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isOk());
	}

	/**
	 * method to test GET /accessrequests/{id} restPoint ; Get access request with
	 * id
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetAccessRequestById() throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders.get("/accessrequests/" + testId).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
	}

	/**
	 * method to test PUT /accessrequests/{id} restPoint ; Modify access request
	 * with id
	 * 
	 * @throws Exception
	 */
	@Test
	public void testModifyAccessRequest() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.put("/accessrequests/" + testId)
				.content(mapper.writeValueAsString(testAccessRequestsData))
				.contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isOk());

	}

	/**
	 * create ProjectsForAccessRequest object
	 * 
	 * @param projectName
	 *            projectName
	 * @param projectId
	 *            projectId
	 * @return object
	 */
	private ProjectsForAccessRequest createProjectsForAccessRequest(String projectName, String projectId) {
		ProjectsForAccessRequest par = new ProjectsForAccessRequest();
		par.setProjectName(projectName);
		par.setProjectId(projectId);
		return par;
	}

}
