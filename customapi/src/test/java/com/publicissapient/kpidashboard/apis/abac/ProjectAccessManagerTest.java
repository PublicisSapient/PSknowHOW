package com.publicissapient.kpidashboard.apis.abac;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.common.service.NotificationService;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.apis.auth.model.Authentication;
import com.publicissapient.kpidashboard.apis.auth.repository.AuthenticationRepository;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.auth.service.UserTokenDeletionService;
import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.autoapprove.service.AutoApproveAccessService;
import com.publicissapient.kpidashboard.apis.common.service.CommonService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.ProjectBasicConfigDataFactory;
import com.publicissapient.kpidashboard.apis.projectconfig.basic.service.ProjectBasicConfigService;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.rbac.AccessItem;
import com.publicissapient.kpidashboard.common.model.rbac.AccessNode;
import com.publicissapient.kpidashboard.common.model.rbac.AccessRequest;
import com.publicissapient.kpidashboard.common.model.rbac.ProjectBasicConfigNode;
import com.publicissapient.kpidashboard.common.model.rbac.ProjectsAccess;
import com.publicissapient.kpidashboard.common.model.rbac.RoleData;
import com.publicissapient.kpidashboard.common.model.rbac.RoleWiseProjects;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.rbac.AccessRequestsRepository;
import com.publicissapient.kpidashboard.common.repository.rbac.RolesRepository;
import com.publicissapient.kpidashboard.common.repository.rbac.UserInfoCustomRepository;
import com.publicissapient.kpidashboard.common.repository.rbac.UserInfoRepository;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;

/**
 * @author yasbano
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class ProjectAccessManagerTest {

	@InjectMocks
	ProjectAccessManager projectAccessManager;
	@Mock
	AccessRequestsRepository accessRequestsRepository;
	@Mock
	UserInfoRepository userInfoRepository;
	@Mock
	ProjectBasicConfigService projectBasicConfigService;
	@Mock
	AccessRequestListener accessRequestListener;
	@Mock
	RejectAccessListener rejectAccessListener;
	@Mock
	AutoApproveAccessService autoApproveAccessService;
	@Mock
	UserTokenDeletionService userTokenDeletionService;
	@Mock
	CommonService commonService;
	@Mock
	CustomApiConfig customApiConfig;
	@Mock
	RolesRepository rolesRepository;
	@Mock
	AuthenticationRepository authenticationRepository;
	@Mock
	ProjectBasicConfigRepository projectBasicConfigRepository;
	@Mock
	AuthenticationService authenticationService;
	@Mock
	UserInfoCustomRepository userInfoCustomRepository;
	@Mock
	HierarchyLevelService hierarchyLevelService;
	@Mock
	TokenAuthenticationService tokenAuthenticationService;
	@Mock
	NotificationService notificationService;

	@Test
	public void testCreateAccessRequest_hasPendingAccessRequest() {
		when(accessRequestsRepository.findByUsernameAndStatus(ArgumentMatchers.any(), ArgumentMatchers.any()))
				.thenReturn(Lists.newArrayList(accessRequestObj(Constant.ROLE_PROJECT_ADMIN,
						Constant.ACCESS_REQUEST_STATUS_PENDING, "hierarchyLevel3Id")));
		projectAccessManager.createAccessRequest(accessRequestObj(Constant.ROLE_PROJECT_ADMIN,
				Constant.ACCESS_REQUEST_STATUS_PENDING, "hierarchyLevel3Id"), accessRequestListener);
		verify(accessRequestListener, atLeastOnce()).onFailure(ArgumentMatchers.anyString());
	}

	@Test
	public void testCreateAccessRequest_isNewUser() throws UnknownHostException {
		UserInfo userInfo = new UserInfo();
		userInfo.setUsername("user");
		userInfo.setId(new ObjectId("61e4f7852747353d4405c762"));
		userInfo.setAuthorities(Lists.newArrayList(Constant.ROLE_VIEWER));
		userInfo.setProjectsAccess(Lists.newArrayList());
		Map<String, String> notificationSubjects = new HashMap<String, String>();
		notificationSubjects.put("Subject", "subject");
		Authentication authentication = new Authentication();
		authentication.setEmail("email@email.com");
		when(userInfoRepository.findByUsername(userInfo.getUsername())).thenReturn(userInfo);
		when(accessRequestsRepository.findByUsernameAndStatus(ArgumentMatchers.any(), ArgumentMatchers.any()))
				.thenReturn(null);
		when(accessRequestsRepository.saveAll(getAccessRequestList(Constant.ROLE_PROJECT_ADMIN,
				Constant.ACCESS_REQUEST_STATUS_PENDING, "hierarchyLevel3Id")))
						.thenReturn(getAccessRequestList(Constant.ROLE_PROJECT_ADMIN,
								Constant.ACCESS_REQUEST_STATUS_PENDING, "hierarchyLevel3Id"));
		when(autoApproveAccessService.isAutoApproveEnabled(ArgumentMatchers.anyString())).thenReturn(true);
		when(accessRequestsRepository.findById(ArgumentMatchers.anyString())).thenReturn(accessRequestObj(
				Constant.ROLE_PROJECT_ADMIN, Constant.ACCESS_REQUEST_STATUS_PENDING, "hierarchyLevel3Id"));
		when(projectBasicConfigService.getBasicConfigTree()).thenReturn(createProjectBasicConfigNode());
		userTokenDeletionService.invalidateSession(ArgumentMatchers.anyString());
		when(commonService.getEmailAddressBasedOnRoles(Arrays.asList(Constant.ROLE_SUPERADMIN)))
				.thenReturn(Arrays.asList(Constant.ROLE_SUPERADMIN));
		when(customApiConfig.getNotificationSubject()).thenReturn(notificationSubjects);
		when(commonService.getApiHost()).thenReturn("serverPath");
		when(rolesRepository.findByRoleName(Constant.ROLE_PROJECT_ADMIN)).thenReturn(roleDataObj());
		when(authenticationRepository.findByUsername(userInfo.getUsername())).thenReturn(authentication);
		projectAccessManager.createAccessRequest(accessRequestObj(Constant.ROLE_PROJECT_ADMIN,
				Constant.ACCESS_REQUEST_STATUS_PENDING, "hierarchyLevel3Id"), accessRequestListener);
		assertNotNull(accessRequestObj(Constant.ROLE_PROJECT_ADMIN, Constant.ACCESS_REQUEST_STATUS_PENDING,
				"hierarchyLevel3Id"));
	}

	@Test
	public void testCreateAccessRequest_isNewUser_ROLE_SUPERADMIN() throws UnknownHostException {
		UserInfo userInfo = new UserInfo();
		userInfo.setUsername("user");
		userInfo.setId(new ObjectId("61e4f7852747353d4405c762"));
		userInfo.setAuthorities(Lists.newArrayList(Constant.ROLE_VIEWER));
		userInfo.setProjectsAccess(Lists.newArrayList());
		when(accessRequestsRepository.findByUsernameAndStatus(ArgumentMatchers.any(), ArgumentMatchers.any()))
				.thenReturn(null);
		projectAccessManager.createAccessRequest(
				accessRequestObj(Constant.ROLE_SUPERADMIN, Constant.ACCESS_REQUEST_STATUS_PENDING, "hierarchyLevel3Id"),
				accessRequestListener);
		assertNotNull(accessRequestObj(Constant.ROLE_SUPERADMIN, Constant.ACCESS_REQUEST_STATUS_PENDING,
				"hierarchyLevel3Id"));
	}

	@Test
	public void testCreateAccessRequest_hasAccessToParentLevel() throws UnknownHostException {
		UserInfo userInfo = new UserInfo();
		userInfo.setUsername("user");
		userInfo.setId(new ObjectId("61e4f7852747353d4405c762"));
		userInfo.setAuthorities(Lists.newArrayList());
		ProjectsAccess projectsAccess = new ProjectsAccess();
		projectsAccess.setRole(Constant.ROLE_PROJECT_ADMIN);
		AccessItem accessItem = new AccessItem();
		accessItem.setItemId("hierarchyLevel3Value");
		accessItem.setItemName("hierarchyLevel3Value");
		AccessNode accessNode = new AccessNode();
		accessNode.setAccessLevel("HIERARCHYLEVEL3ID");
		accessNode.setAccessItems(Lists.newArrayList(accessItem));
		projectsAccess.setAccessNodes(Lists.newArrayList(accessNode));
		userInfo.setProjectsAccess(Lists.newArrayList(projectsAccess));

		projectAccessManager.createAccessRequest(
				accessRequestObj(Constant.ROLE_SUPERADMIN, Constant.ACCESS_REQUEST_STATUS_PENDING, "hierarchyLevel3Id"),
				accessRequestListener);
		verify(accessRequestListener, atLeastOnce()).onFailure(ArgumentMatchers.anyString());
	}

	@Test
	public void testModifyUserInfoForAccess() throws UnknownHostException {
		UserInfo userInfo = new UserInfo();
		userInfo.setUsername("user");
		userInfo.setId(new ObjectId("61e4f7852747353d4405c762"));
		userInfo.setAuthorities(Lists.newArrayList());
		ProjectsAccess projectsAccess = new ProjectsAccess();
		projectsAccess.setRole(Constant.ROLE_PROJECT_ADMIN);
		AccessItem accessItem = new AccessItem();
		accessItem.setItemId("hierarchyLevel3Id");
		accessItem.setItemName("hierarchyLevel3Id");
		AccessNode accessNode = new AccessNode();
		accessNode.setAccessLevel("hierarchyLevel3Id");
		accessNode.setAccessItems(Lists.newArrayList(accessItem));
		projectsAccess.setAccessNodes(Lists.newArrayList(accessNode));
		userInfo.setProjectsAccess(Lists.newArrayList(projectsAccess));
		when(userInfoRepository.findByUsername(userInfo.getUsername())).thenReturn(userInfo);
		when(accessRequestsRepository.findByUsernameAndStatus(ArgumentMatchers.any(), ArgumentMatchers.any()))
				.thenReturn(null);
		when(accessRequestsRepository.saveAll(getAccessRequestList(Constant.ROLE_PROJECT_ADMIN,
				Constant.ACCESS_REQUEST_STATUS_PENDING, "hierarchyLevel3Id")))
						.thenReturn(getAccessRequestList(Constant.ROLE_PROJECT_ADMIN,
								Constant.ACCESS_REQUEST_STATUS_PENDING, "hierarchyLevel3Id"));
		when(autoApproveAccessService.isAutoApproveEnabled(ArgumentMatchers.anyString())).thenReturn(true);
		when(accessRequestsRepository.findById(ArgumentMatchers.anyString())).thenReturn(accessRequestObj(
				Constant.ROLE_PROJECT_ADMIN, Constant.ACCESS_REQUEST_STATUS_PENDING, "hierarchyLevel3Id"));
		projectAccessManager.createAccessRequest(accessRequestObj(Constant.ROLE_PROJECT_ADMIN,
				Constant.ACCESS_REQUEST_STATUS_PENDING, "hierarchyLevel3Id"), accessRequestListener);
		assertNotNull(accessRequestObj(Constant.ROLE_PROJECT_ADMIN, Constant.ACCESS_REQUEST_STATUS_PENDING,
				"hierarchyLevel3Id"));
	}

	@Test
	public void testMoveItemIntoNewRole() throws UnknownHostException {
		UserInfo userInfo = new UserInfo();
		userInfo.setUsername("user");
		userInfo.setId(new ObjectId("61e4f7852747353d4405c762"));
		userInfo.setAuthorities(Lists.newArrayList());
		ProjectsAccess projectsAccess = new ProjectsAccess();
		projectsAccess.setRole(Constant.ROLE_PROJECT_ADMIN);
		AccessItem accessItem = new AccessItem();
		accessItem.setItemId("hierarchyLevel3Value");
		accessItem.setItemName("hierarchyLevel3Value");
		AccessNode accessNode = new AccessNode();
		accessNode.setAccessLevel("hierarchyLevel3Id");
		accessNode.setAccessItems(Lists.newArrayList(accessItem));
		projectsAccess.setAccessNodes(Lists.newArrayList(accessNode));
		userInfo.setProjectsAccess(Lists.newArrayList(projectsAccess));
		when(userInfoRepository.findByUsername(userInfo.getUsername())).thenReturn(userInfo);
		when(accessRequestsRepository.findByUsernameAndStatus(ArgumentMatchers.any(), ArgumentMatchers.any()))
				.thenReturn(null);
		when(accessRequestsRepository.saveAll(getAccessRequestList(Constant.ROLE_PROJECT_VIEWER,
				Constant.ACCESS_REQUEST_STATUS_PENDING, "hierarchyLevel3Id")))
						.thenReturn(getAccessRequestList(Constant.ROLE_PROJECT_VIEWER,
								Constant.ACCESS_REQUEST_STATUS_PENDING, "hierarchyLevel3Id"));
		when(autoApproveAccessService.isAutoApproveEnabled(ArgumentMatchers.anyString())).thenReturn(true);
		when(accessRequestsRepository.findById(ArgumentMatchers.anyString())).thenReturn(accessRequestObj(
				Constant.ROLE_PROJECT_VIEWER, Constant.ACCESS_REQUEST_STATUS_PENDING, "hierarchyLevel3Id"));
		projectAccessManager.createAccessRequest(accessRequestObj(Constant.ROLE_PROJECT_VIEWER,
				Constant.ACCESS_REQUEST_STATUS_PENDING, "hierarchyLevel3Id"), accessRequestListener);
		assertNotNull(accessRequestObj(Constant.ROLE_PROJECT_VIEWER, Constant.ACCESS_REQUEST_STATUS_PENDING,
				"hierarchyLevel3Id"));
	}

	@Test
	public void testExistingRoleForItemIsNull() throws UnknownHostException {
		UserInfo userInfo = new UserInfo();
		userInfo.setUsername("user");
		userInfo.setId(new ObjectId("61e4f7852747353d4405c762"));
		userInfo.setAuthorities(Lists.newArrayList());
		userInfo.setProjectsAccess(Lists.newArrayList());
		when(userInfoRepository.findByUsername(userInfo.getUsername())).thenReturn(userInfo);
		when(accessRequestsRepository.findByUsernameAndStatus(ArgumentMatchers.any(), ArgumentMatchers.any()))
				.thenReturn(null);
		when(accessRequestsRepository.saveAll(getAccessRequestList(Constant.ROLE_PROJECT_VIEWER,
				Constant.ACCESS_REQUEST_STATUS_PENDING, "hierarchyLevel3Id")))
						.thenReturn(getAccessRequestList(Constant.ROLE_PROJECT_VIEWER,
								Constant.ACCESS_REQUEST_STATUS_PENDING, "hierarchyLevel3Id"));
		projectAccessManager.createAccessRequest(accessRequestObj(Constant.ROLE_PROJECT_VIEWER,
				Constant.ACCESS_REQUEST_STATUS_PENDING, "hierarchyLevel3Id"), accessRequestListener);
		assertNotNull(accessRequestObj(Constant.ROLE_PROJECT_VIEWER, Constant.ACCESS_REQUEST_STATUS_PENDING,
				"hierarchyLevel3Id"));
	}

	@Test
	public void testUpdateAccessOfUserInfo_makeItDefaultNewUser() {

		projectAccessManager.updateAccessOfUserInfo(userInfoObj(Constant.ROLE_PROJECT_ADMIN),
				userInfoObj(Constant.ROLE_PROJECT_ADMIN));
		assertNotNull(userInfoObj(Constant.ROLE_PROJECT_ADMIN));
	}

	@Test
	public void testUpdateAccessOfUserInfo_makeItSuperAdmin() {

		projectAccessManager.updateAccessOfUserInfo(userInfoObj(Constant.ROLE_SUPERADMIN),
				userInfoObj(Constant.ROLE_SUPERADMIN));
		assertNotNull(userInfoObj(Constant.ROLE_SUPERADMIN));
	}

	@Test
	public void testUpdateAccessOfUserInfo_modifyUser() {
		UserInfo userInfo = new UserInfo();
		userInfo.setUsername("user");
		userInfo.setAuthorities(Lists.newArrayList());
		ProjectsAccess projectsAccess = new ProjectsAccess();
		projectsAccess.setRole(Constant.ROLE_PROJECT_ADMIN);
		AccessItem accessItem = new AccessItem();
		accessItem.setItemId("hierarchyLevel3Value");
		accessItem.setItemName("hierarchyLevel3Value");
		AccessNode accessNode = new AccessNode();
		accessNode.setAccessLevel("hierarchyLevel3Id");
		accessNode.setAccessItems(Lists.newArrayList(accessItem));
		projectsAccess.setAccessNodes(Lists.newArrayList(accessNode));
		userInfo.setProjectsAccess(Lists.newArrayList(projectsAccess));

		projectAccessManager.updateAccessOfUserInfo(userInfo, userInfo);
		assertNotNull(userInfo);
	}

	@Test
	public void testRejectAccessRequestSuccess() throws Exception {
		when(accessRequestsRepository.findById("61e4f7852747353d4405c763")).thenReturn(accessRequestObj(
				Constant.ROLE_PROJECT_ADMIN, Constant.ACCESS_REQUEST_STATUS_REJECTED, "hierarchyLevel3Id"));
		when(accessRequestsRepository.save(ArgumentMatchers.any())).thenReturn(accessRequestObj(
				Constant.ROLE_PROJECT_ADMIN, Constant.ACCESS_REQUEST_STATUS_REJECTED, "hierarchyLevel3Id"));
		projectAccessManager.rejectAccessRequest("61e4f7852747353d4405c763", ArgumentMatchers.anyString(),
				rejectAccessListener);
		verify(rejectAccessListener, atLeastOnce()).onSuccess(accessRequestObj(Constant.ROLE_PROJECT_ADMIN,
				Constant.ACCESS_REQUEST_STATUS_REJECTED, "hierarchyLevel3Id"));
	}

	@Test
	public void testRejectAccessRequestFailure() throws Exception {
		when(accessRequestsRepository.findById("61e4f7852747353d4405c761")).thenReturn(accessRequestObj(
				Constant.ROLE_PROJECT_ADMIN, Constant.ACCESS_REQUEST_STATUS_PENDING, "hierarchyLevel3Id"));
		when(accessRequestsRepository.save(ArgumentMatchers.any())).thenReturn(accessRequestObj(
				Constant.ROLE_PROJECT_ADMIN, Constant.ACCESS_REQUEST_STATUS_PENDING, "hierarchyLevel3Id"));
		projectAccessManager.rejectAccessRequest("61e4f7852747353d4405c761", ArgumentMatchers.anyString(),
				rejectAccessListener);
		verify(rejectAccessListener, atLeastOnce()).onFailure(ArgumentMatchers.any(), ArgumentMatchers.anyString());
	}

	@Test
	public void cleanUserInfo_Guest() {
		UserInfo userInfo = new UserInfo();
		userInfo.setUsername("user");
		userInfo.setAuthorities(Lists.newArrayList());
		ProjectsAccess projectsAccess = new ProjectsAccess();
		projectsAccess.setRole(Constant.ROLE_GUEST);
		AccessItem accessItem = new AccessItem();
		accessItem.setItemId("hierarchyLevel3Value");
		accessItem.setItemName("hierarchyLevel3Value");
		AccessNode accessNode = new AccessNode();
		accessNode.setAccessLevel("hierarchyLevel3Id");
		accessNode.setAccessItems(Lists.newArrayList(accessItem));
		projectsAccess.setAccessNodes(Lists.newArrayList(accessNode));
		userInfo.setProjectsAccess(Lists.newArrayList(projectsAccess));

		projectAccessManager.updateAccessOfUserInfo(userInfo, userInfo);
		assertNotNull(userInfo);
	}

	@Test
	public void testGetProjectAccessesWithRole() {
		when(userInfoRepository.findByUsername(ArgumentMatchers.anyString()))
				.thenReturn(userInfoObj(Constant.ROLE_PROJECT_ADMIN));
		when(projectBasicConfigRepository.findByHierarchyLevelIdAndValues(anyString(), anyList()))
				.thenReturn(Lists.newArrayList(projectBasicConfigObj()));
		List<RoleWiseProjects> list = projectAccessManager.getProjectAccessesWithRole(ArgumentMatchers.anyString());
		assertEquals(list.size(), 1);
	}

	@Test
	public void testHasProjectEditPermission_Null() {
		assertFalse(projectAccessManager.hasProjectEditPermission(null, null));
	}

	@Test
	public void testHasProjectEditPermission_Superadmin() {
		UserInfo userInfo = new UserInfo();
		userInfo.setUsername("user");
		userInfo.setAuthorities(Lists.newArrayList(Constant.ROLE_SUPERADMIN));
		when(userInfoRepository.findByUsername(ArgumentMatchers.anyString())).thenReturn(userInfo);
		assertTrue(projectAccessManager.hasProjectEditPermission(new ObjectId(), userInfo.getUsername()));
	}

	@Test
	public void testHasProjectEditPermission_EmptyPA() {
		UserInfo userInfo = new UserInfo();
		userInfo.setUsername("user");
		userInfo.setAuthorities(Lists.newArrayList(Constant.ROLE_PROJECT_ADMIN));
		userInfo.setProjectsAccess(Lists.newArrayList());
		when(userInfoRepository.findByUsername(ArgumentMatchers.anyString())).thenReturn(userInfo);
		assertFalse(projectAccessManager.hasProjectEditPermission(new ObjectId(), userInfo.getUsername()));
	}

	@Test
	public void testHasProjectEditPermission_getProjectAccessesWithRole() {
		when(userInfoRepository.findByUsername(ArgumentMatchers.anyString()))
				.thenReturn(userInfoObj(Constant.ROLE_PROJECT_ADMIN));
		when(projectBasicConfigRepository.findByHierarchyLevelIdAndValues(anyString(), ArgumentMatchers.anyList()))
				.thenReturn(Lists.newArrayList(projectBasicConfigObj()));
		assertTrue(projectAccessManager.hasProjectEditPermission(new ObjectId("61e4f7852747353d4405c765"),
				userInfoObj(Constant.ROLE_PROJECT_ADMIN).getUsername()));
	}

	@Test
	public void testDeleteAccessRequestById() {
		when(accessRequestsRepository.findById("61e4f7852747353d4405c761")).thenReturn(accessRequestObj(
				Constant.ROLE_PROJECT_ADMIN, Constant.ACCESS_REQUEST_STATUS_PENDING, "hierarchyLevel3Id"));
		when(authenticationService.getLoggedInUser()).thenReturn("user");
		when(userInfoRepository.findByUsername(ArgumentMatchers.anyString()))
				.thenReturn(userInfoObj(Constant.ROLE_PROJECT_ADMIN));
		assertTrue(projectAccessManager.deleteAccessRequestById("61e4f7852747353d4405c761"));
	}

	@Test
	public void testGetAccessRoleOfNearestParent() {
		when(userInfoRepository.findByUsername(ArgumentMatchers.anyString()))
				.thenReturn(userInfoObj(Constant.ROLE_PROJECT_ADMIN));
		assertNull(projectAccessManager.getAccessRoleOfNearestParent(projectBasicConfigObj(), "user"));
	}

	@Test
	public void testAddNewProjectIntoUserInfo_projectAdmin() {
		when(userInfoRepository.findByUsername(ArgumentMatchers.anyString()))
				.thenReturn(userInfoObj(Constant.ROLE_PROJECT_ADMIN));
		projectAccessManager.addNewProjectIntoUserInfo(projectBasicConfigObj(), "user");
		assertNotNull(userInfoObj(Constant.ROLE_PROJECT_ADMIN));
	}

	@Test
	public void testAddNewProjectIntoUserInfo_SuperAdmin() {
		when(userInfoRepository.findByUsername(ArgumentMatchers.anyString()))
				.thenReturn(userInfoObj(Constant.ROLE_SUPERADMIN));
		projectAccessManager.addNewProjectIntoUserInfo(projectBasicConfigObj(), "user");
		assertNotNull(userInfoObj(Constant.ROLE_SUPERADMIN));
	}

	@Test
	public void testRemoveProjectAccessFromAllUsers() {
		when(userInfoCustomRepository.findByProjectAccess("61e4f7852747353d4405c761"))
				.thenReturn(Lists.newArrayList(userInfoObj(Constant.ROLE_PROJECT_ADMIN)));
		projectAccessManager.removeProjectAccessFromAllUsers("61e4f7852747353d4405c761");
		assertNotNull(userInfoObj(Constant.ROLE_PROJECT_ADMIN));
	}

	/**
	 * project level conditions check
	 * 
	 * @throws UnknownHostException
	 *             exception
	 */
	@Test
	public void testCreateAccessRequest_isNewUser_project() throws UnknownHostException {
		UserInfo userInfo = new UserInfo();
		userInfo.setUsername("user");
		userInfo.setId(new ObjectId("61e4f7852747353d4405c762"));
		userInfo.setAuthorities(Lists.newArrayList(Constant.ROLE_VIEWER));
		userInfo.setProjectsAccess(Lists.newArrayList());
		AccessItem item = new AccessItem();
		item.setItemName("Test Project");
		item.setItemId("61d6d4235c76563333369f01");
		Map<String, String> notificationSubjects = new HashMap<>();
		notificationSubjects.put("Subject", "subject");
		Authentication authentication = new Authentication();
		authentication.setEmail("email@email.com");
		when(userInfoRepository.findByUsername(userInfo.getUsername())).thenReturn(userInfo);
		when(accessRequestsRepository.findByUsernameAndStatus(ArgumentMatchers.any(), ArgumentMatchers.any()))
				.thenReturn(null);
		when(accessRequestsRepository.saveAll(ArgumentMatchers.any())).thenReturn(
				getAccessRequestList(Constant.ROLE_PROJECT_ADMIN, Constant.ACCESS_REQUEST_STATUS_PENDING, "Project"));
		when(autoApproveAccessService.isAutoApproveEnabled(ArgumentMatchers.anyString())).thenReturn(true);
		when(accessRequestsRepository.findById(ArgumentMatchers.anyString())).thenReturn(
				accessRequestObj(Constant.ROLE_PROJECT_ADMIN, Constant.ACCESS_REQUEST_STATUS_PENDING, "Project"));
		when(projectBasicConfigService.getBasicConfigTree()).thenReturn(createProjectBasicConfigNode());
		userTokenDeletionService.invalidateSession(ArgumentMatchers.anyString());
		when(commonService.getEmailAddressBasedOnRoles(Arrays.asList(Constant.ROLE_SUPERADMIN)))
				.thenReturn(Lists.newArrayList(Constant.ROLE_SUPERADMIN));

		when(customApiConfig.getNotificationSubject()).thenReturn(notificationSubjects);
		when(commonService.getApiHost()).thenReturn("serverPath");
		when(rolesRepository.findByRoleName(Constant.ROLE_PROJECT_ADMIN)).thenReturn(roleDataObj());
		when(authenticationRepository.findByUsername(userInfo.getUsername())).thenReturn(authentication);
		projectAccessManager.createAccessRequest(
				accessRequestObj(Constant.ROLE_PROJECT_ADMIN, Constant.ACCESS_REQUEST_STATUS_PENDING, "Project"),
				accessRequestListener);
		assertNotNull(accessRequestObj(Constant.ROLE_PROJECT_ADMIN, Constant.ACCESS_REQUEST_STATUS_PENDING, "Project"));
	}

	@Test
	public void getProjectBasicOnRoleListTest() {
		UserInfo userInfo = new UserInfo();
		userInfo.setUsername("test");
		userInfo.setAuthorities(Lists.newArrayList(Constant.ROLE_PROJECT_ADMIN));
		List<ProjectsAccess> projectAccesses = new ArrayList<>();
		ProjectsAccess pa = new ProjectsAccess();
		pa.setRole(Constant.ROLE_PROJECT_ADMIN);
		List<AccessNode> accessNodes = new ArrayList<>();
		AccessNode accessNode = new AccessNode();
		accessNode.setAccessLevel("Project");
		List<AccessItem> items = new ArrayList<>();

		AccessItem item = new AccessItem();
		item.setItemName("Test Project");
		item.setItemId("61d6d4235c76563333369f01");
		items.add(item);
		accessNode.setAccessItems(items);
		accessNodes.add(accessNode);
		pa.setAccessNodes(accessNodes);
		projectAccesses.add(pa);
		userInfo.setProjectsAccess(projectAccesses);

		List<String> list = projectAccessManager.getProjectBasicOnRoleList(userInfo,
				Lists.newArrayList(Constant.ROLE_PROJECT_ADMIN));
		assertEquals(list.size(), 0);
	}

	ProjectBasicConfig projectBasicConfigObj() {
		ProjectBasicConfig basicConfig = ProjectBasicConfigDataFactory
				.newInstance("/json/basicConfig/project_basic_config_request.json").getProjectBasicConfigs().get(0);
		basicConfig.setId(new ObjectId("61e4f7852747353d4405c765"));
		return basicConfig;
	}

	ProjectBasicConfigNode createProjectBasicConfigNode() {
		ProjectBasicConfigNode projectBasicConfigNode = new ProjectBasicConfigNode();
		ProjectBasicConfigNode parent = new ProjectBasicConfigNode();
		ProjectBasicConfigNode child = new ProjectBasicConfigNode();
		parent.setGroupName("hierarchyLevel2Id");
		parent.setValue("hierarchyLevel2Value");
		projectBasicConfigNode.setGroupName("HIERARCHYLEVEL3ID");
		projectBasicConfigNode.setValue("hierarchyLevel3Value");
		child.setGroupName("hierarchyLevel4Id");
		child.setValue("hierarchyLevel4Value");
		projectBasicConfigNode.setParent(Lists.newArrayList(parent));
		projectBasicConfigNode.setChildren(Lists.newArrayList(child));
		return projectBasicConfigNode;
	}

	public UserInfo userInfoObj(String role) {
		UserInfo userInfo = new UserInfo();
		userInfo.setUsername("user");
		AccessItem accessItem = new AccessItem();
		accessItem.setItemId("Test1");
		accessItem.setItemName("Test1");
		AccessNode accessNode = new AccessNode();
		accessNode.setAccessLevel("hierarchyLevel3Id");
		accessNode.setAccessItems(Lists.newArrayList(accessItem));
		ProjectsAccess projectsAccess = new ProjectsAccess();
		projectsAccess.setAccessNodes(Lists.newArrayList(accessNode));
		userInfo.setProjectsAccess(Lists.newArrayList(projectsAccess));
		projectsAccess.setRole(role);
		userInfo.setAuthorities(Lists.newArrayList(role));
		return userInfo;
	}

	List<AccessRequest> getAccessRequestList(String role, String status, String accessLevel) {
		List<AccessRequest> request = new ArrayList<>();
		request.add(accessRequestObj(role, status, accessLevel));
		return request;
	}

	AccessRequest accessRequestObj(String role, String status, String accessLevel) {
		AccessRequest accessRequest = new AccessRequest();
		accessRequest.setUsername("user");
		accessRequest.setRole(role);
		accessRequest.setId(new ObjectId("61e4f7852747353d4405c761"));
		accessRequest.setStatus(status);
		AccessNode accessNode = new AccessNode();
		AccessItem accessItem = new AccessItem();
		accessItem.setItemId("hierarchyLevel3Value");
		accessItem.setItemName("hierarchyLevel3Value");
		accessNode.setAccessLevel(accessLevel);
		accessNode.setAccessItems(Lists.newArrayList(accessItem));
		accessRequest.setAccessNode(accessNode);
		return accessRequest;
	}

	RoleData roleDataObj() {
		RoleData roleData = new RoleData();
		roleData.setRoleName(Constant.ROLE_PROJECT_ADMIN);
		roleData.setDisplayName(Constant.ROLE_PROJECT_ADMIN);
		return roleData;
	}

	@Test
	public void canTriggerProcessorFor_Success() {

		UserInfo userInfo = new UserInfo();
		userInfo.setUsername("test");
		userInfo.setAuthorities(Lists.newArrayList(Constant.ROLE_SUPERADMIN));
		when(userInfoRepository.findByUsername(ArgumentMatchers.anyString())).thenReturn(userInfo);

		List<String> projectBasicConfigIds = new ArrayList<>();
		projectBasicConfigIds.add("61d6d4235c76563333369f02");
		projectBasicConfigIds.add("61d6d4235c76563333369f01");

		boolean result = projectAccessManager.canTriggerProcessorFor(projectBasicConfigIds, userInfo.getUsername());
		assertTrue(result);

	}

	@Test
	public void canTriggerProcessorFor_EmptyProjectList() {

		boolean result = projectAccessManager.canTriggerProcessorFor(new ArrayList<>(), "test");
		assertFalse(result);

	}

	@Test
	public void canTriggerProcessorFor_Failure() {
		UserInfo userInfo = new UserInfo();
		userInfo.setUsername("test");
		userInfo.setAuthorities(Lists.newArrayList(Constant.ROLE_PROJECT_ADMIN));
		List<ProjectsAccess> projectAccesses = new ArrayList<>();
		ProjectsAccess pa = new ProjectsAccess();
		pa.setRole(Constant.ROLE_PROJECT_ADMIN);
		List<AccessNode> accessNodes = new ArrayList<>();
		AccessNode accessNode = new AccessNode();
		accessNode.setAccessLevel("Project");
		List<AccessItem> items = new ArrayList<>();

		AccessItem item = new AccessItem();
		item.setItemName("Test Project");
		item.setItemId("61d6d4235c76563333369f01");
		items.add(item);
		accessNode.setAccessItems(items);
		accessNodes.add(accessNode);
		pa.setAccessNodes(accessNodes);
		userInfo.setProjectsAccess(projectAccesses);
		when(userInfoRepository.findByUsername(ArgumentMatchers.anyString())).thenReturn(userInfo);
		List<String> projectBasicConfigIds = new ArrayList<>();
		projectBasicConfigIds.add("61d6d4235c76563333369f02");

		boolean result = projectAccessManager.canTriggerProcessorFor(projectBasicConfigIds, "test");
		assertFalse(result);

	}

}
