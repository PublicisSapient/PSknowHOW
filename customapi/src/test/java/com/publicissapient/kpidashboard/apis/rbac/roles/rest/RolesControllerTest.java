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
package com.publicissapient.kpidashboard.apis.rbac.roles.rest;

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

import com.publicissapient.kpidashboard.apis.rbac.roles.service.RolesHelperService;
import com.publicissapient.kpidashboard.apis.util.TestUtil;
import com.publicissapient.kpidashboard.common.model.rbac.Permissions;
import com.publicissapient.kpidashboard.common.model.rbac.RoleData;

/**
 * @author anamital
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class RolesControllerTest {

	private MockMvc mockMvc;

	private RoleData testRoleData;
	private String testId;
	@InjectMocks
	private RolesController rolesController;

	@Mock
	private RolesHelperService rolesHelperService;

	/**
	 * method includes preprocesses for test cases
	 */
	@Before
	public void before() {
		mockMvc = MockMvcBuilders.standaloneSetup(rolesController).build();
		testId = "5da46000e645ca33dc927b4a";
		testRoleData = new RoleData();
		testRoleData.setId(new ObjectId("5da46000e645ca33dc927b4a"));
		testRoleData.setRoleName("UnitTest");
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
	}

	/**
	 * method includes post processes for test cases
	 */
	@After
	public void after() {
		mockMvc = null;
		testId = null;
	}

	/**
	 * method to test /roles restPoint ;
	 * 
	 * Get all access requests
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetAllRoles() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/roles").contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
	}

	/**
	 * method to test /roles/{id} restPoint ; Get single role using id
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetRoleById() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/roles/" + testId).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
	}

	/**
	 * method to test /roles/{id} restPoint ; Modify single role using id
	 * 
	 * @throws Exception
	 */
	@Test
	public void testModifyRoleById() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.put("/roles/" + testId)
				.content(TestUtil.convertObjectToJsonBytes(testRoleData)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
	}

	/**
	 * method to test /roles/{id} restPoint ; Create new role
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateRole() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/roles/").content(TestUtil.convertObjectToJsonBytes(testRoleData))
				.contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isOk());
	}

}
