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

package com.publicissapient.kpidashboard.apis.rbac.accessrequests.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.apis.abac.ProjectAccessManager;
import com.publicissapient.kpidashboard.apis.auth.repository.AuthenticationRepository;
import com.publicissapient.kpidashboard.apis.auth.service.UserTokenDeletionService;
import com.publicissapient.kpidashboard.apis.common.service.CommonService;
import com.publicissapient.kpidashboard.apis.common.service.impl.UserInfoServiceImpl;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.constant.AuthType;
import com.publicissapient.kpidashboard.common.constant.NotificationEnum;
import com.publicissapient.kpidashboard.common.model.rbac.AccessItem;
import com.publicissapient.kpidashboard.common.model.rbac.AccessNode;
import com.publicissapient.kpidashboard.common.model.rbac.AccessRequest;
import com.publicissapient.kpidashboard.common.model.rbac.NotificationDataDTO;
import com.publicissapient.kpidashboard.common.model.rbac.ProjectsForAccessRequest;
import com.publicissapient.kpidashboard.common.model.rbac.RoleData;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.repository.rbac.AccessRequestsRepository;
import com.publicissapient.kpidashboard.common.repository.rbac.RolesRepository;
import com.publicissapient.kpidashboard.common.repository.rbac.UserInfoRepository;

/**
 * This class provides various methods to TEST operations on AccessRequestsData
 *
 * @author anamital
 */
@RunWith(MockitoJUnitRunner.class)
public class AccessRequestsHelperServiceImplTest {

	/*
	 * Creating a new test AccessRequestsData object
	 */
	AccessRequest testAccessRequestsData = new AccessRequest();
	/*
	 * Creating a new test authentication object
	 */
	com.publicissapient.kpidashboard.apis.auth.model.Authentication testAuthentication = new com.publicissapient.kpidashboard.apis.auth.model.Authentication();
	/*
	 * Creating a new string object to store test id
	 */
	String testId;
	/*
	 * Creating a new string object to store test username
	 */
	String testUsername;
	/*
	 * Creating a new string object to store test status
	 */
	String testStatus;
	/*
	 * Creating a new string object to store test approvedStatus
	 */
	boolean approvedStatus;
	@InjectMocks
	private AccessRequestsHelperServiceImpl accessRequestsHelperServiceImpl;
	@Mock
	private UserInfoServiceImpl userInfoServiceImpl;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	private AccessRequestsRepository accessRequestsRepository;
	@Mock
	private AuthenticationRepository authenticationRepository;
	@Mock
	private UserInfoRepository userInfoRepository;
	@Mock
	private RolesRepository rolesRepository;
	@Mock
	private UserTokenDeletionService userTokenDeletionService;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private Authentication authentication;
	private UserInfo userInfo;
	private UserInfo nonSuperadminUserInfo;
	private UserInfo projectAdminUserInfo;
	@Mock
	private CommonService commonService;
	@Mock
	private ProjectAccessManager accessManager;

	/**
	 * method includes preprocesses for test cases
	 */
	@Before
	public void setUp() {
		testStatus = "Pending";
		testUsername = "testuser";
		testAccessRequestsData.setId(new ObjectId("5f7ee917485b2c09bc8bac7a"));
		testAccessRequestsData.setUsername("testuser");
		testAccessRequestsData.setStatus("Approved");
		testAccessRequestsData.setRole("ROLE_PROJECT_VIEWER");

		AccessNode node = new AccessNode();
		node.setAccessLevel(Constant.PROJECT);

		AccessItem item = new AccessItem();
		item.setItemId("605aaf595a160c3fe46fdbbc");
		item.setItemName("testproject");
		List<AccessItem> itemList = new ArrayList<>();
		itemList.add(item);
		node.setAccessItems(itemList);
		testAccessRequestsData.setAccessNode(node);

		userInfo = new UserInfo();
		userInfo.setUsername("SUPERADMIN");
		userInfo.setAuthType(AuthType.STANDARD);
		userInfo.setAuthorities(Lists.newArrayList("ROLE_SUPERADMIN"));

		nonSuperadminUserInfo = new UserInfo();
		nonSuperadminUserInfo.setUsername("guest");
		nonSuperadminUserInfo.setAuthType(AuthType.STANDARD);
		nonSuperadminUserInfo.setAuthorities(Lists.newArrayList("ROLE_PROJECT_VIEWER"));

		projectAdminUserInfo = new UserInfo();
		projectAdminUserInfo.setUsername("guest");
		projectAdminUserInfo.setAuthType(AuthType.STANDARD);
		projectAdminUserInfo.setAuthorities(Lists.newArrayList("ROLE_PROJECT_ADMIN"));

		testAuthentication.setEmail("testuser@gmail.com");
		testAuthentication.setUsername("testuser");
		testAuthentication.setApproved(false);
	}

	@After
	public void cleanup() {
		testAccessRequestsData = new AccessRequest();
	}

	/**
	 * 1. database call has an error and returns null
	 *
	 */
	@Test
	public void testGetAllAccessRequests1() {
		when(accessRequestsRepository.findAll()).thenReturn(null);
		ServiceResponse response = accessRequestsHelperServiceImpl.getAllAccessRequests();
		assertThat("status: ", response.getSuccess(), equalTo(true));
		assertThat("Data should not exist: ", response.getData(), equalTo(null));
	}

	/**
	 * 2. database call has no records and returns empty array
	 *
	 */
	@Test
	public void testGetAllAccessRequests2() {
		when(accessRequestsRepository.findAll()).thenReturn(new ArrayList<>());
		ServiceResponse response = accessRequestsHelperServiceImpl.getAllAccessRequests();
		assertThat("status: ", response.getSuccess(), equalTo(true));
		assertThat("Data should exist but empty: ", response.getData(), equalTo(new ArrayList<>()));
	}

	/**
	 * 3. database call has records and returns them as an array
	 *
	 */
	@Test
	public void testGetAllAccessRequests3() {
		List<AccessRequest> testListAccessRequestsData = new ArrayList<>();
		testListAccessRequestsData.add(testAccessRequestsData);
		when(accessRequestsRepository.findAll()).thenReturn(testListAccessRequestsData);
		ServiceResponse response = accessRequestsHelperServiceImpl.getAllAccessRequests();
		assertThat("status: ", response.getSuccess(), equalTo(true));
		assertThat("Data should exist but empty: ", response.getData(), equalTo(testListAccessRequestsData));
	}

	/**
	 * 4. Input String id is null
	 *
	 */
	@Test
	public void testGetAccessRequestById1() {
		testId = null;
		ServiceResponse response = accessRequestsHelperServiceImpl.getAccessRequestById(testId);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertThat("Data should not exist: ", response.getData(), equalTo(null));
	}

	/**
	 * 5. Input String id creates invalid ObjectId
	 *
	 */
	@Test
	public void testGetAccessRequestById2() {
		testId = "UnitTest";
		ServiceResponse response = accessRequestsHelperServiceImpl.getAccessRequestById(testId);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertThat("Data should not exist: ", response.getData(), equalTo(null));
	}

	/**
	 * 6. Input String id is valid but data at this id does not exist in the
	 * database.
	 *
	 */
	@Test
	public void testGetAccessRequestById3() {
		testId = "5ca455aa70c53c4f50076e34";
		when(accessRequestsRepository.findById(new ObjectId(testId))).thenReturn(Optional.ofNullable(null));
		ServiceResponse response = accessRequestsHelperServiceImpl.getAccessRequestById(testId);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertThat("Data should not exist: ", response.getData(), equalTo(null));
	}

	/**
	 * 7. Input String id is valid and data at this id exists in the database.
	 *
	 */
	@Test
	public void testGetAccessRequestById4() {
		testId = "5ca455aa70c53c4f50076e34";
		List<AccessRequest> testListAccessRequestsData = new ArrayList<>();
		testListAccessRequestsData.add(testAccessRequestsData);
		Optional<AccessRequest> testAccessRequestsDataOpt = Optional.ofNullable(testAccessRequestsData);
		when(accessRequestsRepository.findById(new ObjectId(testId))).thenReturn(testAccessRequestsDataOpt);
		ServiceResponse response = accessRequestsHelperServiceImpl.getAccessRequestById(testId);
		assertThat("status: ", response.getSuccess(), equalTo(true));
	}

	/**
	 * 8. Input String testUsername is null.
	 *
	 */
	@Test
	public void testGetAccessRequestByUsername1() {
		testUsername = null;
		ServiceResponse response = accessRequestsHelperServiceImpl.getAccessRequestByUsername(testUsername);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertEquals(null, response.getData());
	}

	/**
	 * 9. Input String testUsername is not null but data at this username does not
	 * exist in the database.
	 *
	 */
	@Test
	public void testGetAccessRequestByUsername2() {
		testUsername = "UnitTest";
		when(accessRequestsRepository.findByUsername(testUsername)).thenReturn(new ArrayList<>());
		ServiceResponse response = accessRequestsHelperServiceImpl.getAccessRequestByUsername(testUsername);
		assertThat("status: ", response.getSuccess(), equalTo(true));
		assertEquals(response.getMessage(), "access_requests do not exist for username " + testUsername);
		assertEquals(response.getData(), new ArrayList<>());
	}

	/**
	 * 10. Input String testUsername is not null but data is returned null.
	 *
	 */
	@Test
	public void testGetAccessRequestByUsername3() {
		testUsername = "UnitTest";
		List<AccessRequest> testListAccessRequestsData = new ArrayList<>();
		testListAccessRequestsData.add(testAccessRequestsData);
		when(accessRequestsRepository.findByUsername(testUsername)).thenReturn(null);
		ServiceResponse response = accessRequestsHelperServiceImpl.getAccessRequestByUsername(testUsername);
		assertThat("status: ", response.getSuccess(), equalTo(true));
		assertEquals(null, response.getData());
	}

	/**
	 * 11. Input String testUsername is not null and data at this username exists in
	 * the database and is returned as array.
	 *
	 */
	@Test
	public void testGetAccessRequestByUsername4() {
		testUsername = "UnitTest";
		List<AccessRequest> testListAccessRequestsData = new ArrayList<>();
		testListAccessRequestsData.add(testAccessRequestsData);
		when(accessRequestsRepository.findByUsername(testUsername)).thenReturn(testListAccessRequestsData);
		ServiceResponse response = accessRequestsHelperServiceImpl.getAccessRequestByUsername(testUsername);
		assertThat("status: ", response.getSuccess(), equalTo(true));
		assertEquals(testListAccessRequestsData, response.getData());
	}

	/**
	 * 12. Input String testStatus is null.
	 *
	 */
	@Test
	public void testGetAccessRequestByStatus1() {
		testStatus = null;
		List<AccessRequest> testListAccessRequestsData = new ArrayList<>();
		testListAccessRequestsData.add(testAccessRequestsData);
		ServiceResponse response = accessRequestsHelperServiceImpl.getAccessRequestByStatus(testStatus);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertEquals(null, response.getData());
	}

	/**
	 * 13. Input String testStatus is not null but database did not return anything.
	 *
	 */
	@Test
	public void testGetAccessRequestByStatus2() {
		List<AccessRequest> testListAccessRequestsData = new ArrayList<>();
		testListAccessRequestsData.add(testAccessRequestsData);
		when(accessRequestsRepository.findByStatus(testStatus)).thenReturn(null);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
		when(authentication.getPrincipal()).thenReturn("superadmin");
		when(userInfoServiceImpl.getUserInfo(any())).thenReturn(userInfo);
		ServiceResponse response = accessRequestsHelperServiceImpl.getAccessRequestByStatus(testStatus);
		assertThat("status: ", response.getSuccess(), equalTo(true));
		assertEquals(null, response.getData());
	}

	/**
	 * 14. Input String testUsername is not null but data at this status does not
	 * exist in the database.
	 *
	 */
	@Test
	public void testGetAccessRequestByStatus3() {
		testStatus = "Pendin";
		List<AccessRequest> testListAccessRequestsData = new ArrayList<>();
		testListAccessRequestsData.add(testAccessRequestsData);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
		when(authentication.getPrincipal()).thenReturn("superadmin");
		when(userInfoServiceImpl.getUserInfo(any())).thenReturn(userInfo);
		ServiceResponse response = accessRequestsHelperServiceImpl.getAccessRequestByStatus(testStatus);
		assertThat("status: ", response.getSuccess(), equalTo(true));
		assertEquals(response.getData(), new ArrayList<>());
	}

	/**
	 * 15. Input String testUsername is not null and data at this status exists in
	 * the database.
	 *
	 */
	@Test
	public void testGetAccessRequestByStatus4() {
		List<AccessRequest> testListAccessRequestsData = new ArrayList<>();
		testListAccessRequestsData.add(testAccessRequestsData);
		when(accessRequestsRepository.findByStatus(testStatus)).thenReturn(testListAccessRequestsData);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
		when(authentication.getPrincipal()).thenReturn("superadmin");
		when(userInfoServiceImpl.getUserInfo(any())).thenReturn(userInfo);
		ServiceResponse response = accessRequestsHelperServiceImpl.getAccessRequestByStatus(testStatus);
		assertThat("status: ", response.getSuccess(), equalTo(true));
		assertEquals(testListAccessRequestsData, response.getData());
	}

	/**
	 * 16. Input String testStatus is null.
	 *
	 */
	@Test
	public void testGetAccessRequestByUsernameAndStatus1() {
		testStatus = null;
		testUsername = "UnitTest";
		List<AccessRequest> testListAccessRequestsData = new ArrayList<>();
		testListAccessRequestsData.add(testAccessRequestsData);
		ServiceResponse response = accessRequestsHelperServiceImpl.getAccessRequestByUsernameAndStatus(testUsername,
				testStatus);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertEquals(null, response.getData());
	}

	/**
	 * 17. Input String testUsername is null.
	 *
	 */
	@Test
	public void testGetAccessRequestByUsernameAndStatus2() {
		testUsername = null;
		List<AccessRequest> testListAccessRequestsData = new ArrayList<>();
		testListAccessRequestsData.add(testAccessRequestsData);
		ServiceResponse response = accessRequestsHelperServiceImpl.getAccessRequestByUsernameAndStatus(testUsername,
				testStatus);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertEquals(null, response.getData());
	}

	/**
	 * 18. Input String testUsername and testStatus are not null but database did
	 * not return anything.
	 *
	 */
	@Test
	public void testGetAccessRequestByUsernameAndStatus3() {
		testUsername = "UnitTest";
		List<AccessRequest> testListAccessRequestsData = new ArrayList<>();
		testListAccessRequestsData.add(testAccessRequestsData);
		when(accessRequestsRepository.findByUsernameAndStatus(testUsername, testStatus)).thenReturn(null);
		ServiceResponse response = accessRequestsHelperServiceImpl.getAccessRequestByUsernameAndStatus(testUsername,
				testStatus);
		assertThat("status: ", response.getSuccess(), equalTo(true));
		assertEquals(null, response.getData());
	}

	/**
	 * 19. Input String testUsername and testStatus are not null but data at this
	 * status does not exist in the database.
	 *
	 */
	@Test
	public void testGetAccessRequestByUsernameAndStatus4() {
		testStatus = "Pendin";
		testUsername = "UnitTest";
		List<AccessRequest> testListAccessRequestsData = new ArrayList<>();
		testListAccessRequestsData.add(testAccessRequestsData);
		when(accessRequestsRepository.findByUsernameAndStatus(testUsername, testStatus)).thenReturn(new ArrayList<>());
		ServiceResponse response = accessRequestsHelperServiceImpl.getAccessRequestByUsernameAndStatus(testUsername,
				testStatus);
		assertThat("status: ", response.getSuccess(), equalTo(true));
		assertEquals(response.getData(), new ArrayList<>());
	}

	/**
	 * 20. Input String testUsername and testStatus are not null but data at this
	 * username does not exist in the database.
	 *
	 */
	@Test
	public void testGetAccessRequestByUsernameAndStatus5() {
		testUsername = "UnitTes";
		List<AccessRequest> testListAccessRequestsData = new ArrayList<>();
		testListAccessRequestsData.add(testAccessRequestsData);
		when(accessRequestsRepository.findByUsernameAndStatus(testUsername, testStatus)).thenReturn(new ArrayList<>());
		ServiceResponse response = accessRequestsHelperServiceImpl.getAccessRequestByUsernameAndStatus(testUsername,
				testStatus);
		assertThat("status: ", response.getSuccess(), equalTo(true));
		assertEquals(response.getData(), new ArrayList<>());
	}

	/**
	 * 21. Input String testUsername and testStatus are not null and data at this
	 * status and username exists in the database.
	 *
	 */
	@Test
	public void testGetAccessRequestByUsernameAndStatus6() {
		testUsername = "UnitTest";
		List<AccessRequest> testListAccessRequestsData = new ArrayList<>();
		testListAccessRequestsData.add(testAccessRequestsData);
		when(accessRequestsRepository.findByUsernameAndStatus(testUsername, testStatus))
				.thenReturn(testListAccessRequestsData);
		ServiceResponse response = accessRequestsHelperServiceImpl.getAccessRequestByUsernameAndStatus(testUsername,
				testStatus);
		assertThat("status: ", response.getSuccess(), equalTo(true));
		assertEquals(testListAccessRequestsData, response.getData());
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

	private AccessRequest createAccessRequest(String status, String username) {
		AccessRequest data = new AccessRequest();
		data.setUsername(username);
		data.setStatus(status);
		return data;
	}

	/**
	 * 44. Input String testStatus is not null but database did not return anything
	 * and approvedStatus is false then database did not return anything for
	 * Superadmin.
	 *
	 */
	@Test
	public void testGetNotificationByStatus1_superadmin() {
		testStatus = "Pending";
		approvedStatus = false;
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
		when(authentication.getPrincipal()).thenReturn("superadmin");
		when(userInfoServiceImpl.getUserInfo(any())).thenReturn(userInfo);
		when(accessRequestsRepository.findByStatus(testStatus)).thenReturn(null);
		when(authenticationRepository.findByApproved(approvedStatus)).thenReturn(null);
		ServiceResponse response = accessRequestsHelperServiceImpl.getNotificationByStatus(testStatus);
		assertThat("status: ", response.getSuccess(), equalTo(true));
		assertEquals(createNotificationDataResponseTest(0, 0), response.getData());
	}

	/**
	 * 45. Input String testStatus is not null but database return data and
	 * approvedStatus is false then database did not return anything for Superadmin.
	 *
	 */
	@Test
	public void testGetNotificationByStatus2_superadmin() {
		testStatus = "Pending";
		approvedStatus = false;
		List<AccessRequest> testListAccessRequestsData = new ArrayList<>();
		testListAccessRequestsData.add(testAccessRequestsData);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
		when(authentication.getPrincipal()).thenReturn("superadmin");
		when(userInfoServiceImpl.getUserInfo(any())).thenReturn(userInfo);
		when(accessRequestsRepository.findByStatus(testStatus)).thenReturn(testListAccessRequestsData);
		when(authenticationRepository.findByApproved(approvedStatus)).thenReturn(null);
		ServiceResponse response = accessRequestsHelperServiceImpl.getNotificationByStatus(testStatus);
		assertThat("status: ", response.getSuccess(), equalTo(true));
		assertEquals(createNotificationDataResponseTest(1, 0), response.getData());
	}

	/**
	 * 46. Input String testStatus is not null but database did not return anything
	 * and approvedStatus is false then database return data for Superadmin
	 *
	 */
	@Test
	public void testGetNotificationByStatus3_superadmin() {
		testStatus = "Pending";
		approvedStatus = false;
		List<com.publicissapient.kpidashboard.apis.auth.model.Authentication> testListAuthentication = new ArrayList<>();
		testListAuthentication.add(testAuthentication);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
		when(authentication.getPrincipal()).thenReturn("superadmin");
		when(userInfoServiceImpl.getUserInfo(any())).thenReturn(userInfo);
		when(accessRequestsRepository.findByStatus(testStatus)).thenReturn(null);
		when(authenticationRepository.findByApproved(approvedStatus)).thenReturn(testListAuthentication);
		ServiceResponse response = accessRequestsHelperServiceImpl.getNotificationByStatus(testStatus);
		assertThat("status: ", response.getSuccess(), equalTo(true));
		assertEquals(createNotificationDataResponseTest(0, 1), response.getData());
	}

	/**
	 * if PROJECT_ACCESS count and USER_APPROVAL count is zero
	 *
	 */
	private List<NotificationDataDTO> createNotificationDataResponseTest(int count1, int count2) {
		NotificationDataDTO notificationDataDTO1 = new NotificationDataDTO();
		List<NotificationDataDTO> notificationDataDTOList = new ArrayList<>();
		notificationDataDTO1.setType(NotificationEnum.PROJECT_ACCESS.getValue());
		notificationDataDTO1.setCount(count1);
		NotificationDataDTO notificationDataDTO2 = new NotificationDataDTO();
		notificationDataDTO2.setType(NotificationEnum.USER_APPROVAL.getValue());
		notificationDataDTO2.setCount(count2);
		notificationDataDTOList.add(notificationDataDTO2);
		notificationDataDTOList.add(notificationDataDTO1);
		return notificationDataDTOList;
	}

	/**
	 * 47. Input String testStatus is not null but database did not return anything
	 * for Non-Superadmin user.
	 *
	 */
	@Test
	public void testGetNotificationByStatus1_Nonsuperadmin() {
		testStatus = "Pending";
		testUsername = "guest";
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
		when(authentication.getPrincipal()).thenReturn("guest");
		when(userInfoServiceImpl.getUserInfo(any())).thenReturn(nonSuperadminUserInfo);
		when(accessRequestsRepository.findByUsernameAndStatus(testUsername, testStatus)).thenReturn(null);

		ServiceResponse response = accessRequestsHelperServiceImpl.getNotificationByStatus(testStatus);
		assertThat("status: ", response.getSuccess(), equalTo(true));
		assertEquals(createNotificationDataResponseTest4(0), response.getData());
	}

	/**
	 * 45. Input String testStatus is not null but database return data for
	 * Non-Superadmin user.
	 *
	 */
	@Test
	public void testGetNotificationByStatus2_Nonsuperadmin() {
		testStatus = "Pending";
		testUsername = "guest";
		List<AccessRequest> testListAccessRequestsData = new ArrayList<>();
		testListAccessRequestsData.add(testAccessRequestsData);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
		when(authentication.getPrincipal()).thenReturn("guest");
		when(userInfoServiceImpl.getUserInfo(any())).thenReturn(nonSuperadminUserInfo);
		when(accessRequestsRepository.findByUsernameAndStatus(testUsername, testStatus))
				.thenReturn(testListAccessRequestsData);
		ServiceResponse response = accessRequestsHelperServiceImpl.getNotificationByStatus(testStatus);
		assertThat("status: ", response.getSuccess(), equalTo(true));
		assertEquals(createNotificationDataResponseTest4(1), response.getData());
	}

	private List<NotificationDataDTO> createNotificationDataResponseTest4(int count) {
		NotificationDataDTO notificationDataDTO1 = new NotificationDataDTO();
		List<NotificationDataDTO> notificationDataDTOList = new ArrayList<>();
		notificationDataDTO1.setType(NotificationEnum.PROJECT_ACCESS.getValue());
		notificationDataDTO1.setCount(count);
		notificationDataDTOList.add(notificationDataDTO1);
		return notificationDataDTOList;
	}

	@Test
	public void updateAccessRequest() {
		AccessRequest accessRequestForUpdate = createAccessRequestForUpdate();
		when(accessRequestsRepository.findById(anyString())).thenReturn(accessRequestForUpdate);
		when(accessRequestsRepository.save(any(AccessRequest.class))).thenReturn(accessRequestForUpdate);
		AccessRequest updatedRequest = accessRequestsHelperServiceImpl.updateAccessRequest(accessRequestForUpdate);
		assertEquals("605aaf595a160c3fe46fdbbc", updatedRequest.getId().toHexString());
	}

	/**
	 * access request by status -> projectadmin
	 */
	@Test
	public void testAccessRequestByStatus_ProjectAdmin() {
		List<String> basicconfigList = new ArrayList<>();
		basicconfigList.add("605aaf595a160c3fe46fdbbc");
		basicconfigList.add("605aaf595a160c3fe46fdbba");
		List<AccessRequest> testListAccessRequestsData = new ArrayList<>();
		testListAccessRequestsData.add(testAccessRequestsData);
		when(accessRequestsRepository.findByStatusAndAccessLevel(anyString(), anyString()))
				.thenReturn(testListAccessRequestsData);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
		when(authentication.getPrincipal()).thenReturn("guest2");
		when(userInfoServiceImpl.getUserInfo(any())).thenReturn(projectAdminUserInfo);
		when(accessManager.getProjectBasicOnRoleList(any(), any())).thenReturn(basicconfigList);

		ServiceResponse response = accessRequestsHelperServiceImpl.getAccessRequestByStatus(testStatus);
		assertEquals(Boolean.TRUE, response.getSuccess());
	}

	/**
	 * notification method test by project admin
	 */
	@Test
	public void testGetNotificationByStatus_projectadmin() {
		testStatus = "Pending";
		List<String> basicconfigList = new ArrayList<>();
		basicconfigList.add("605aaf595a160c3fe46fdbbc");
		basicconfigList.add("605aaf595a160c3fe46fdbba");
		List<AccessRequest> testListAccessRequestsData = new ArrayList<>();
		testListAccessRequestsData.add(testAccessRequestsData);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
		when(authentication.getPrincipal()).thenReturn("guest2");
		when(userInfoServiceImpl.getUserInfo(any())).thenReturn(projectAdminUserInfo);
		when(accessRequestsRepository.findByStatusAndAccessLevel(anyString(), anyString()))
				.thenReturn(testListAccessRequestsData);
		when(accessManager.getProjectBasicOnRoleList(any(), any())).thenReturn(basicconfigList);
		ServiceResponse response = accessRequestsHelperServiceImpl.getNotificationByStatus(testStatus);
		assertEquals(Boolean.TRUE, response.getSuccess());
		assertEquals(createNotificationDataResponseTest4(1), response.getData());
	}

	private AccessRequest createAccessRequestForUpdate() {
		AccessRequest accessRequestsData = new AccessRequest();
		accessRequestsData.setAccessNode(new AccessNode());
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

}