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

package com.publicissapient.kpidashboard.apis.rbac.roles.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.rbac.Permissions;
import com.publicissapient.kpidashboard.common.model.rbac.RoleData;
import com.publicissapient.kpidashboard.common.repository.rbac.RolesRepository;

/**
 * This class provides various methods to TEST operations on RoleData
 *
 * @author anamital
 */
@RunWith(MockitoJUnitRunner.class)
public class RolesHelperServiceImplTest {

	RoleData testRoleData = new RoleData();
	String testId;
	String testRolename;
	String testStatus;
	@InjectMocks
	private RolesHelperServiceImpl rolesHelperServiceImpl;
	@Mock
	private RolesRepository rolesRepository;

	/**
	 * method includes preprocesses for test cases
	 */
	@Before
	public void setup() {
		testRolename = "UnitTest";
		testStatus = "Pending";

		testRoleData.setId(new ObjectId("5ca455aa70c53c4f50076e34"));
		testRoleData.setRoleName(testRolename);
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

	@After
	public void cleanup() {
		testRoleData = new RoleData();
		testRoleData.setId(new ObjectId("5ca455aa70c53c4f50076e34"));
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
	 * 1. database call has an error and returns null
	 *
	 */
	@Test
	public void testGetAllRole1() {
		when(rolesRepository.findAll()).thenReturn(null);
		ServiceResponse response = rolesHelperServiceImpl.getAllRoles();
		assertThat("status: ", response.getSuccess(), equalTo(true));
		assertThat("Data should not exist: ", response.getData(), equalTo(null));
	}

	/**
	 * 2. database call has no records and returns empty array
	 *
	 */
	@Test
	public void testGetAllRole2() {
		when(rolesRepository.findAll()).thenReturn(new ArrayList<RoleData>());
		ServiceResponse response = rolesHelperServiceImpl.getAllRoles();
		assertThat("status: ", response.getSuccess(), equalTo(true));
		assertThat("Data should exist but empty: ", response.getData(), equalTo(new ArrayList<RoleData>()));
	}

	/**
	 * 3. database call has records and returns them as an array
	 *
	 */
	@Test
	public void testGetAllRole3() {
		List<RoleData> a = new ArrayList<RoleData>();
		a.add(testRoleData);
		when(rolesRepository.findAll()).thenReturn(a);
		ServiceResponse response = rolesHelperServiceImpl.getAllRoles();
		assertThat("status: ", response.getSuccess(), equalTo(true));
		assertThat("Data should exist but empty: ", response.getData(), equalTo(a));
	}

	/**
	 * 4. Input String id is null
	 *
	 */
	@Test
	public void testGetRoleById1() {
		testId = null;
		ServiceResponse response = rolesHelperServiceImpl.getRoleById(testId);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertThat("Data should not exist: ", response.getData(), equalTo(null));
	}

	/**
	 * 5. Input String id creates invalid ObjectId
	 *
	 */
	@Test
	public void testGetRoleById2() {
		testId = "UnitTest";
		ServiceResponse response = rolesHelperServiceImpl.getRoleById(testId);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertThat("Data should not exist: ", response.getData(), equalTo(null));
	}

	/**
	 * 6. Input String id is valid but data at this id does not exist in the
	 * database.
	 *
	 */
	@Test
	public void testGetRoleById3() {
		testId = "5ca455aa70c53c4f50076e34";
		when(rolesRepository.findById(new ObjectId(testId))).thenReturn(Optional.ofNullable(null));
		ServiceResponse response = rolesHelperServiceImpl.getRoleById(testId);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertThat("Data should not exist: ", response.getData(), equalTo(null));
	}

	/**
	 * 7. Input String id is valid and data at this id exists in the database.
	 *
	 */
	@Test
	public void testGetRoleById4() {
		testId = "5ca455aa70c53c4f50076e34";
		List<RoleData> a = new ArrayList<RoleData>();
		a.add(testRoleData);
		Optional<RoleData> roleDataOpt = Optional.of(testRoleData);
		when(rolesRepository.findById(new ObjectId(testId))).thenReturn(roleDataOpt);
		ServiceResponse response = rolesHelperServiceImpl.getRoleById(testId);
		assertThat("status: ", response.getSuccess(), equalTo(true));
	}

	/**
	 * 8. Input String id is null
	 *
	 */
	@Test
	public void testModifyRoleById1() {
		testId = null;
		List<RoleData> a = new ArrayList<RoleData>();
		a.add(testRoleData);
		ServiceResponse response = rolesHelperServiceImpl.modifyRoleById(testId, testRoleData);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertEquals(null, response.getData());
	}

	/**
	 * 9. Input String id creates invalid ObjectId
	 *
	 */
	@Test
	public void testModifyRoleById2() {
		testId = "5ca455aa70c53c4f5007";
		List<RoleData> a = new ArrayList<RoleData>();
		a.add(testRoleData);
		ServiceResponse response = rolesHelperServiceImpl.modifyRoleById(testId, testRoleData);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertEquals(null, response.getData());
	}

	/**
	 * 10. Input String id is valid but input roleData has no permissions selected.
	 *
	 */
	@Test
	public void testModifyRoleById3() {
		testId = "5ca455aa70c53c4f50076e34";
		testRoleData.setPermissions(new ArrayList<Permissions>());
		List<RoleData> a = new ArrayList<RoleData>();
		a.add(testRoleData);
		Optional<RoleData> roleDataOpt = Optional.of(testRoleData);
		ServiceResponse response = rolesHelperServiceImpl.modifyRoleById(testId, testRoleData);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertEquals(null, response.getData());
	}

	/**
	 * 11. Input String id is valid but input roleData has no permissions selected.
	 *
	 */
	@Test
	public void testModifyRoleById4() {
		testId = "5ca455aa70c53c4f50076e34";
		Permissions perm1 = new Permissions();
		perm1.setPermissionName("TestProjectForRole1");
		perm1.setResourceId(new ObjectId("5ca455aa70c53c4f50076e34"));
		perm1.setResourceName("resource1");

		ArrayList<Permissions> permissions = new ArrayList<Permissions>();
		testRoleData.setPermissions(permissions);
		List<RoleData> a = new ArrayList<RoleData>();
		a.add(testRoleData);
		Optional<RoleData> roleDataOpt = Optional.of(testRoleData);
		ServiceResponse response = rolesHelperServiceImpl.modifyRoleById(testId, testRoleData);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertEquals(null, response.getData());
	}

	/**
	 * 12. Input String id is valid but input roleData has at least one permission
	 * selected.
	 *
	 */
	@Test
	public void testModifyRoleById5() {
		testId = "5ca455aa70c53c4f50076e34";
		Permissions perm1 = new Permissions();
		perm1.setPermissionName("TestProjectForRole1");
		perm1.setResourceId(new ObjectId("5ca455aa70c53c4f50076e34"));
		perm1.setResourceName("resource1");

		ArrayList<Permissions> permissions = new ArrayList<Permissions>();
		permissions.add(perm1);
		testRoleData.setPermissions(permissions);
		List<RoleData> a = new ArrayList<RoleData>();
		a.add(testRoleData);
		Optional<RoleData> roleDataOpt = Optional.of(testRoleData);
		ServiceResponse response = rolesHelperServiceImpl.modifyRoleById(testId, testRoleData);
		assertThat("status: ", response.getSuccess(), equalTo(true));
		assertEquals(response.getData(), a);
	}

	/**
	 * 13. Input String id is valid but input roleData has no role name.
	 *
	 */
	@Test
	public void testModifyRoleById6() {
		testId = "5ca455aa70c53c4f50076e34";
		testRoleData.setRoleName(null);
		List<RoleData> a = new ArrayList<RoleData>();
		a.add(testRoleData);
		Optional<RoleData> roleDataOpt = Optional.of(testRoleData);
		ServiceResponse response = rolesHelperServiceImpl.modifyRoleById(testId, testRoleData);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertEquals(null, response.getData());
	}

	/**
	 * 14. Input String id is valid but input roleData has no role decription.
	 *
	 */
	@Test
	public void testModifyRoleById7() {
		testId = "5ca455aa70c53c4f50076e34";
		testRoleData.setRoleDescription(null);
		List<RoleData> a = new ArrayList<RoleData>();
		a.add(testRoleData);
		Optional<RoleData> roleDataOpt = Optional.of(testRoleData);
		ServiceResponse response = rolesHelperServiceImpl.modifyRoleById(testId, testRoleData);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertEquals(null, response.getData());
	}

	/**
	 * 15. Input String id is valid and data at this id exists.
	 *
	 */
	@Test
	public void testModifyRoleById8() {
		testId = "5ca455aa70c53c4f50076e34";
		List<RoleData> a = new ArrayList<RoleData>();
		a.add(testRoleData);
		Optional<RoleData> roleDataOpt = Optional.of(testRoleData);
		ServiceResponse response = rolesHelperServiceImpl.modifyRoleById(testId, testRoleData);
		assertThat("status: ", response.getSuccess(), equalTo(true));
		assertEquals(response.getData(), a);
	}

	/**
	 * 16. Creating a role
	 *
	 */
	@Test
	public void testCreateRole1() {
		List<RoleData> a = new ArrayList<RoleData>();
		a.add(testRoleData);
		ServiceResponse response = rolesHelperServiceImpl.createRole(testRoleData);
		assertThat("status: ", response.getSuccess(), equalTo(true));
		assertEquals(response.getData(), a);
	}

	/**
	 * 17. Input roleData has no role name.
	 *
	 */
	@Test
	public void testCreateRole2() {
		testRoleData.setRoleName(null);
		List<RoleData> a = new ArrayList<RoleData>();
		a.add(testRoleData);
		ServiceResponse response = rolesHelperServiceImpl.createRole(testRoleData);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertEquals(null, response.getData());
	}

}