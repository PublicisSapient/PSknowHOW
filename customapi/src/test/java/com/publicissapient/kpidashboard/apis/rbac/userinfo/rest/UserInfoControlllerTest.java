/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *    http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.rbac.userinfo.rest;

import com.publicissapient.kpidashboard.apis.auth.AuthProperties;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.auth.service.UserTokenDeletionService;
import com.publicissapient.kpidashboard.apis.common.service.UserInfoService;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.util.TestUtil;
import com.publicissapient.kpidashboard.common.model.rbac.Permissions;
import com.publicissapient.kpidashboard.common.model.rbac.RoleData;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfoDTO;
import com.publicissapient.kpidashboard.common.repository.rbac.UserInfoRepository;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author narsingh9
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class UserInfoControlllerTest {

	private MockMvc mockMvc;
	private RoleData testRoleData;
	@Mock
	private UserInfo userInfo;

	@Mock
	private UserInfoDTO userInfoDTO;

	private List<String> authorities;

	@InjectMocks
	private UserInfoController userInfoController;

	@Mock
	private UserInfoService userInfoService;

	@Mock
	private AuthenticationService authenticationService;

	@Mock
	private UserInfoRepository userInfoRepository;

	@Mock
	private UserTokenDeletionService userTokenDeletionService;

	@Mock
	AuthProperties authProperties;

	/**
	 * method includes preprocesses for test cases
	 */
	@Before
	public void before() {
		this.mockMvc = MockMvcBuilders.standaloneSetup(this.userInfoController).build();
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

		// userInfo=new UserInfo();
		userInfo.setUsername("testuser");
		authorities = new ArrayList<>();
		authorities.add("ROLE_GUEST");
	}

	/**
	 * method includes post processes for test cases
	 */
	@After
	public void after() {
		this.mockMvc = null;
	}

	/**
	 * method to test /userinfo restPoint ;
	 *
	 * Get all userinfo
	 *
	 * @throws Exception
	 */

	@Test
	public void testGetAllUserInfo() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders.get("/userinfo").contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
	}

	/**
	 * method to test /updateAccessOfUserInfo restPoint ;
	 *
	 * update the user role
	 *
	 * @throws Exception
	 */
	@Test
	public void testupdateUserRole() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/userinfo/updateUserRole")
				.content(TestUtil.convertObjectToJsonBytes(testRoleData)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
	}

	/**
	 * method to test /userinfo restPoint ;
	 * <p>
	 * Delete User
	 *
	 * @throws Exception
	 */
	@Test
	public void testdeleteUser() throws Exception {
		userInfo.setUsername("testuser");
		when(userInfoDTO.getUsername()).thenReturn("testuser");
		when(authenticationService.getLoggedInUser()).thenReturn("SUPERADMIN");
		when(userInfoRepository.findByUsername("testuser")).thenReturn(userInfo);
		when(userInfo.getAuthorities()).thenReturn(authorities);
		doReturn(new ServiceResponse(true, "Deleted Successfully", "Ok")).when(userInfoService).deleteUser("testuser", false);
		ServiceResponse response = userInfoController.deleteUser(userInfoDTO).getBody();
		assert response != null;
		assertEquals(true, response.getSuccess());
	}
	/**
	 * method to test /userinfo restPoint ;
	 * <p>
	 * Delete User SuperAdmin
	 *
	 */
	@Test
	public void testdeleteSuperAdminUser() {
		when(userInfoDTO.getUsername()).thenReturn("testuser");
		when(authenticationService.getLoggedInUser()).thenReturn("testuser");
		when(userInfoRepository.findByUsername("testuser")).thenReturn(userInfo);
		ServiceResponse response = userInfoController.deleteUser(userInfoDTO).getBody();
		assert response != null;
		assertEquals(false, response.getSuccess());
	}


	/**
	 * method to test /userinfo restPoint ;
	 * <p>
	 * Delete User SuperAdmin
	 *
	 */
	@Test
	public void testDelete_UserFromCentral() {
		when(userInfoDTO.getUsername()).thenReturn("testuser");
		when(authenticationService.getLoggedInUser()).thenReturn("testuser");
		when(userInfoRepository.findByUsername("testuser")).thenReturn(userInfo);
		ServiceResponse response = userInfoController.deleteUserFromCentral(userInfoDTO).getBody();
		assert response != null;
		assertEquals(false, response.getSuccess());
	}

	/**
	 * method to test /userinfo restPoint ;
	 * <p>
	 * Delete User SuperAdmin
	 *
	 */
	@Test
	public void testDelete_UserFromCentralForSuperAdmin() {
		userInfo.setUsername("testuser");
		when(userInfoDTO.getUsername()).thenReturn("testuser");
		when(authenticationService.getLoggedInUser()).thenReturn("SUPERADMIN");
		when(userInfoRepository.findByUsername("testuser")).thenReturn(userInfo);
		when(userInfoService.deleteUser("testuser" , true) )
				.thenReturn(new ServiceResponse(true, "Deleted Successfully", "Ok"));
		when(userInfo.getAuthorities()).thenReturn(authorities);
		ServiceResponse response = userInfoController.deleteUserFromCentral(userInfoDTO).getBody();
		assert response != null;
		assertEquals(true, response.getSuccess());
	}

}
