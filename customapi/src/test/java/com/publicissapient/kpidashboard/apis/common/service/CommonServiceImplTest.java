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

package com.publicissapient.kpidashboard.apis.common.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


import com.publicissapient.kpidashboard.common.kafka.producer.NotificationEventProducer;
import jakarta.servlet.http.HttpServletRequest;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mail.MailSendException;
import org.testng.collections.Lists;
import org.thymeleaf.spring5.SpringTemplateEngine;

import com.publicissapient.kpidashboard.apis.auth.model.Authentication;
import com.publicissapient.kpidashboard.apis.auth.repository.AuthenticationRepository;
import com.publicissapient.kpidashboard.apis.common.service.impl.CommonServiceImpl;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.common.constant.AuthType;
import com.publicissapient.kpidashboard.common.model.application.EmailServerDetail;
import com.publicissapient.kpidashboard.common.model.application.GlobalConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.notification.EmailEvent;
import com.publicissapient.kpidashboard.common.model.rbac.AccessItem;
import com.publicissapient.kpidashboard.common.model.rbac.AccessNode;
import com.publicissapient.kpidashboard.common.model.rbac.ProjectsAccess;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.repository.application.GlobalConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.rbac.UserInfoRepository;

@SuppressWarnings("deprecation")
@RunWith(MockitoJUnitRunner.class)
public class CommonServiceImplTest {

	@Mock
	JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	@Mock
	ProjectBasicConfigRepository projectBasicConfigRepository;
	@InjectMocks
	private CommonServiceImpl commonService;
	@Mock
	private CustomApiConfig customAPISettings;
	@Mock
	private JiraIssueRepository jiraIssueRepository;
	@Mock
	private UserInfoRepository userInfoRepository;
	@Mock
	private AuthenticationRepository authenticationRepository;
	@Mock
	private NotificationEventProducer notificationEventProducer;
	@Mock
	private HttpServletRequest request;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	private GlobalConfigRepository globalConfigRepository;
	@Mock
	private SpringTemplateEngine templateEngine;

	private GlobalConfig globalConfig;

	private List<GlobalConfig> globalConfigs = new ArrayList<>();

	@Before
	public void setUp() throws Exception {
		globalConfig = new GlobalConfig();
		globalConfig.setEnv("email");
		EmailServerDetail emailServerDetail = new EmailServerDetail();
		emailServerDetail.setEmailPort(25);
		emailServerDetail.setEmailHost("xyz.smtp.com");
		emailServerDetail.setFromEmail("xyz@abc.com");
		globalConfig.setEmailServerDetail(emailServerDetail);
		globalConfigs.add(globalConfig);
	}

	@Test
	public void testGetMaturityLevel() {
		List<String> list = new ArrayList<>();

		list.add("0-20");
		list.add("20-40");
		list.add("40-60");
		list.add("60-80");
		list.add("80-");
		Assert.assertEquals("5", commonService.getMaturityLevel(list, Constant.AUTOMATED_PERCENTAGE, "90"));
		list.clear();
		list.add("0-20");
		list.add("20-40");
		Assert.assertEquals("0", commonService.getMaturityLevel(list, Constant.AUTOMATED_PERCENTAGE, "90"));
		list.clear();
		list.add("0-20-20");
		list.add("20-40-20");
		list.add("20-40-20");
		list.add("20-40-20");
		Assert.assertEquals("4", commonService.getMaturityLevel(list, Constant.AUTOMATED_PERCENTAGE, "90"));
		list.clear();
		list.add("20-40-20");
		list.add("0-20-20");
		list.add("20-40-20");
		list.add("20-40-20");

		Assert.assertEquals("4", commonService.getMaturityLevel(list, Constant.AUTOMATED_PERCENTAGE, "90"));

	}

	@Test
	public void testGetMaturityLevel3() {
		List<String> list = new ArrayList<>();

		list.add("20-40-20");
		list.add("0-20-20");
		list.add("20-40-20");
		list.add("20-40-20");

		Assert.assertEquals("4", commonService.getMaturityLevel(list, KPICode.CODE_COMMIT.getKpiId(), "90"));

	}

	@Test
	public void testGetEmailAddressBasedOnRoles() {

		String username = "SUPERADMIN";
		AuthType authType = AuthType.STANDARD;
		UserInfo user = new UserInfo();
		user.setUsername(username);
		user.setAuthType(authType);
		user.setId(new ObjectId("5ddf69f6a592816aa30c4fbe"));
		List<String> auth = new ArrayList<>();
		auth.add("ROLE_SUPERADMIN");
		user.setAuthorities(auth);
		List<AccessNode> accessNodes = new ArrayList<>();
		ProjectsAccess projectsAccess = new ProjectsAccess();
		projectsAccess.setRole("ROLE_SUPERADMIN");
		projectsAccess.setAccessNodes(accessNodes);
		user.setProjectsAccess(Arrays.asList(projectsAccess));
		List<UserInfo> users = new ArrayList<>();
		users.add(user);

		List<String> emailList = new ArrayList<>();
		emailList.add("abc@xyz.com");

		final String pw = "pass1";
		Authentication authentication = new Authentication("SUPERADMIN", pw, "abc@xyz.com");
		List<Authentication> authentications = new ArrayList<>();
		authentications.add(authentication);

		when(userInfoRepository.findByAuthoritiesIn(Arrays.asList("ROLE_SUPERADMIN"))).thenReturn(users);
		when(authenticationRepository.findByUsernameIn(Arrays.asList(username))).thenReturn(authentications);
		commonService.getEmailAddressBasedOnRoles(Arrays.asList("ROLE_SUPERADMIN"));

	}

	@Test
	public void testGetEmailAddressBasedOnRoles1() {

		String username = "";
		AuthType authType = AuthType.STANDARD;
		UserInfo user = new UserInfo();
		user.setUsername(username);
		user.setAuthType(authType);
		user.setId(new ObjectId("5ddf69f6a592816aa30c4fbe"));
		List<String> auth = new ArrayList<>();
		auth.add("ROLE_SUPERADMIN");
		user.setAuthorities(auth);
		List<AccessNode> accessNodes = new ArrayList<>();
		ProjectsAccess projectsAccess = new ProjectsAccess();
		projectsAccess.setRole("");
		projectsAccess.setAccessNodes(accessNodes);
		;

		user.setProjectsAccess(Arrays.asList(projectsAccess));
		List<UserInfo> users = new ArrayList<>();
		users.add(user);

		List<String> emailList = new ArrayList<>();
		emailList.add("abc@xyz.com");

		final String pw = "pass1";
		Authentication authentication = new Authentication("SUPERADMIN", pw, "abc@xyz.com");
		List<Authentication> authentications = new ArrayList<>();
		authentications.add(authentication);

		when(userInfoRepository.findByAuthoritiesIn(Arrays.asList("ROLE_SUPERADMIN"))).thenReturn(null);
		commonService.getEmailAddressBasedOnRoles(Arrays.asList("ROLE_SUPERADMIN"));

	}

	@Test
	public void testGetEmailAddressBasedOnRoles2() {

		String username = "user";
		AuthType authType = AuthType.STANDARD;
		UserInfo user = new UserInfo();
		user.setUsername(username);
		user.setAuthType(authType);
		user.setId(new ObjectId("5ddf69f6a592816aa30c4fbe"));
		List<String> auth = new ArrayList<>();
		auth.add("ROLE_SUPERADMIN");
		user.setAuthorities(auth);
		List<AccessNode> accessNodes = new ArrayList<>();
		ProjectsAccess projectsAccess = new ProjectsAccess();
		projectsAccess.setRole("ROLE_SUPERADMIN");
		projectsAccess.setAccessNodes(accessNodes);
		user.setProjectsAccess(Arrays.asList(projectsAccess));
		List<UserInfo> users = new ArrayList<>();
		users.add(user);

		List<String> emailList = new ArrayList<>();
		emailList.add("abc@xyz.com");

		final String pw = "pass1";
		Authentication authentication = new Authentication("SUPERADMIN", pw, "abc@xyz.com");
		List<Authentication> authentications = new ArrayList<>();
		authentications.add(authentication);

		when(userInfoRepository.findByAuthoritiesIn(Arrays.asList("ROLE_SUPERADMIN"))).thenReturn(users);
		when(authenticationRepository.findByUsernameIn(Arrays.asList(username))).thenReturn(null);
		commonService.getEmailAddressBasedOnRoles(Arrays.asList("ROLE_SUPERADMIN"));

	}

	@Test
	public void testGetApiHost() throws UnknownHostException {
		when(customApiConfig.getUiHost()).thenReturn("localhost");
		when(customApiConfig.getUiPort()).thenReturn("9999");
		when(request.getScheme()).thenReturn("http://");
		commonService.getApiHost();

	}

	@Test
	public void testGetApiHost1() {
		when(customApiConfig.getUiHost()).thenReturn("");
		try {
			commonService.getApiHost();
		} catch (UnknownHostException e) {

		}

	}

	@Test
	public void testGetApiHost2() {
		when(customApiConfig.getUiHost()).thenReturn("localhost");
		when(customApiConfig.getUiPort()).thenReturn("");
		when(request.getScheme()).thenReturn("http://");
		try {
			commonService.getApiHost();
		} catch (UnknownHostException e) {

		}

	}

	@Test
	public void getProjectAdminEmailAddressBasedProjectId() {

		String username = "user";
		AuthType authType = AuthType.STANDARD;
		UserInfo user = new UserInfo();
		user.setUsername(username);
		user.setAuthType(authType);
		user.setId(new ObjectId("5ddf69f6a592816aa30c4fbe"));
		List<String> auth = new ArrayList<>();
		auth.add(Constant.ROLE_PROJECT_ADMIN);
		user.setAuthorities(auth);
		List<AccessNode> accessNodes = new ArrayList<>();
		AccessNode acc = new AccessNode();
		acc.setAccessLevel("Project");
		AccessItem accessItem = new AccessItem();
		accessItem.setItemId("61e4f7852747353d4405c765");
		accessItem.setItemName("project");
		acc.setAccessItems(Lists.newArrayList(accessItem));
		accessNodes.add(acc);
		ProjectsAccess projectsAccess = new ProjectsAccess();
		projectsAccess.setRole(Constant.ROLE_PROJECT_ADMIN);
		projectsAccess.setAccessNodes(accessNodes);
		user.setProjectsAccess(Arrays.asList(projectsAccess));
		List<UserInfo> users = new ArrayList<>();
		users.add(user);

		List<String> emailList = new ArrayList<>();
		emailList.add("abc@xyz.com");

		final String pw = "pass1";
		Authentication authentication = new Authentication("SUPERADMIN", pw, "abc@xyz.com");
		List<Authentication> authentications = new ArrayList<>();
		authentications.add(authentication);

		when(projectBasicConfigRepository.findById(ArgumentMatchers.any()))
				.thenReturn(Optional.of(projectBasicConfigObj()));

		when(userInfoRepository.findByAuthoritiesIn(Arrays.asList(Constant.ROLE_PROJECT_ADMIN))).thenReturn(users);
		commonService.getProjectAdminEmailAddressBasedProjectId("5ddf69f6a592816aa30c4fbe");

	}

	ProjectBasicConfig projectBasicConfigObj() {
		ProjectBasicConfig basicConfig = new ProjectBasicConfig();
		basicConfig.setId(new ObjectId("61e4f7852747353d4405c765"));

		basicConfig.setProjectName("project");
		return basicConfig;
	}
}
