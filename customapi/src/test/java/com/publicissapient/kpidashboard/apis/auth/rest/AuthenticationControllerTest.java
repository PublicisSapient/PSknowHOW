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

package com.publicissapient.kpidashboard.apis.auth.rest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.publicissapient.kpidashboard.apis.auth.AuthProperties;
import com.publicissapient.kpidashboard.apis.auth.AuthenticationResponseService;
import com.publicissapient.kpidashboard.apis.auth.service.AuthTypesConfigService;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.UserInfoService;
import com.publicissapient.kpidashboard.apis.rbac.signupapproval.service.SignupManager;
import com.publicissapient.kpidashboard.common.constant.AuthType;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationControllerTest {

	private AuthType standardAuthType = AuthType.STANDARD;
	private AuthType ldapAuthType = AuthType.LDAP;

	@InjectMocks
	private AuthenticationController authController;

	private MockMvc mockMvc;

	@Mock
	private AuthProperties authProperties;

	@Mock
	private AuthenticationService authenticationService;

	@Mock
	private AuthenticationResponseService authenticationResponseService;

	@Mock
	private UserInfoService userInfoService;

	@Mock
	private Authentication authentication;

	@Mock
	private SignupManager signupManager;

	@Mock
	private AuthTypesConfigService authTypesConfigService;

	@Before
	public void before() {
		mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
	}

	@After
	public void after() {
		mockMvc = null;
	}

	@Test
	public void multipleAuthTypes() throws Exception {
		List<AuthType> expectedReturn = new ArrayList<>();

		expectedReturn.add(standardAuthType);
		expectedReturn.add(ldapAuthType);

		when(authProperties.getAuthenticationProviders()).thenReturn(expectedReturn);

		List<AuthType> result = authController.getAuthenticationProviders();

		assertNotNull(result);
		assertTrue(result.equals(expectedReturn));
		verify(authProperties).getAuthenticationProviders();
	}

	@Test
	public void oneType() throws Exception {
		List<AuthType> expectedReturn = new ArrayList<>();

		expectedReturn.add(ldapAuthType);

		when(authProperties.getAuthenticationProviders()).thenReturn(expectedReturn);

		List<AuthType> result = authController.getAuthenticationProviders();

		assertNotNull(result);
		assertTrue(result.equals(expectedReturn));
		verify(authProperties).getAuthenticationProviders();
	}

	@Test
	public void zeroTypes() throws Exception {
		List<AuthType> expectedReturn = new ArrayList<>();

		when(authProperties.getAuthenticationProviders()).thenReturn(expectedReturn);

		List<AuthType> result = authController.getAuthenticationProviders();

		assertNotNull(result);
		assertTrue(result.equals(expectedReturn));
		verify(authProperties).getAuthenticationProviders();
	}

	@Test
	public void registerUser_success() throws Exception {

		String request = "{\"username\":\"test\",\"password\":\"Test@123\",\"email\":\"test@gmail.com\"}";

		UserInfo userInfo = new UserInfo();
		userInfo.setUsername("test");
		userInfo.setEmailAddress("test@gmail.com");
		userInfo.setAuthType(AuthType.STANDARD);
		userInfo.setAuthorities(Arrays.asList("ROLE_VIEWER"));

		when(authenticationService.isEmailExist(anyString())).thenReturn(false);
		when(userInfoService.save(any())).thenReturn(userInfo);
		signupManager.sendUserPreApprovalRequestEmailToAdmin(anyString(), anyString());
		mockMvc.perform(post("/registerUser").accept(MediaType.APPLICATION_JSON).content(request)
				.contentType(MediaType.APPLICATION_JSON)

		).andExpect(status().isAccepted());

	}

	@Test
	public void registerUser_emailAlreadyExists() throws Exception {

		String request = "{\"username\":\"test\",\"password\":\"Test@123\",\"email\":\"test@gmail.com\"}";

		when(authenticationService.isEmailExist(anyString())).thenReturn(true);
		mockMvc.perform(post("/registerUser").accept(MediaType.APPLICATION_JSON).content(request)
				.contentType(MediaType.APPLICATION_JSON)

		).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON));

	}

	@Test
	public void registerUser_invalidPassword() throws Exception {

		String request = "{\"username\":\"test\",\"password\":\"12345\",\"email\":\"test@gmail.com\"}";

		when(authenticationService.isEmailExist(anyString())).thenReturn(false);
		mockMvc.perform(post("/registerUser").accept(MediaType.APPLICATION_JSON).content(request)
				.contentType(MediaType.APPLICATION_JSON)

		).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON));

	}

	@Test
	public void registerUser_Exception() throws Exception {

		String request = "{\"username\":\"test\",\"password\":\"Test@123\",\"email\":\"test@gmail.com\"}";

		when(authenticationService.isEmailExist(anyString())).thenReturn(false);
		when(authenticationService.create(anyString(), anyString(), anyString())).thenReturn(authentication);
		doThrow(DuplicateKeyException.class).when(authenticationResponseService).handle(any(HttpServletResponse.class),
				any(Authentication.class));

		mockMvc.perform(post("/registerUser").accept(MediaType.APPLICATION_JSON).content(request)
				.contentType(MediaType.APPLICATION_JSON)

		).andExpect(status().isUnprocessableEntity()).andExpect(content().contentType(MediaType.APPLICATION_JSON));

	}

	@Test
	public void updateUser() throws Exception {

		String request = "{\"username\":\"test\",\"password\":\"Test@123\",\"email\":\"test@gmail.com\"}";

		when(authenticationService.update(anyString(), anyString())).thenReturn("User is updated");

		mockMvc.perform(post("/updateUser").accept(MediaType.APPLICATION_JSON).content(request)
				.contentType(MediaType.APPLICATION_JSON)

		).andExpect(status().isOk());

	}

	@Test
	public void getUser_Success() throws Exception {
		UserInfo userInfo = new UserInfo();
		userInfo.setAuthorities(Arrays.asList("ROLE_SUPERADMIN"));
		userInfo.setUsername("SUPERADMIN");
		userInfo.setAuthType(AuthType.STANDARD);
		userInfo.setEmailAddress("test.superadmin@gmail.com");

		when(userInfoService.getUserInfo("SUPERADMIN")).thenReturn(userInfo);

		com.publicissapient.kpidashboard.apis.auth.model.Authentication authentication1 = new com.publicissapient.kpidashboard.apis.auth.model.Authentication();
		authentication1.setUsername("SUPERADMIN");
		authentication1.setEmail("test.superadmin@gmail.com");

		when(authenticationService.getAuthentication("SUPERADMIN")).thenReturn(authentication1);

		String expectedResponse = "{'message':'User details','success':true,'data':{'username':'SUPERADMIN','authorities':['ROLE_SUPERADMIN'],'authType':'STANDARD','emailAddress':'test.superadmin@gmail.com'}}";

		Principal principal = Mockito.mock(Principal.class);
		when(principal.getName()).thenReturn("SUPERADMIN");

		mockMvc.perform(get("/users/SUPERADMIN").accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).principal(principal)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().json(expectedResponse));
	}

	@Test
	public void getUser_UserNotFound() throws Exception {

		String expectedResponse = "{'message':'user not found with username testUser','success':false}";

		Principal principal = Mockito.mock(Principal.class);
		when(principal.getName()).thenReturn("SUPERADMIN");

		mockMvc.perform(get("/users/testUser").accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).principal(principal)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().json(expectedResponse));
	}

	@Test
	public void getUser_UnAuthorizedAccess() throws Exception {

		UserInfo userInfo = createUserInfo("testUser", "ROLE_VIEWER", "test@gmail.com");

		when(userInfoService.getUserInfo("testUser")).thenReturn(userInfo);

		com.publicissapient.kpidashboard.apis.auth.model.Authentication authentication1 = new com.publicissapient.kpidashboard.apis.auth.model.Authentication();
		authentication1.setUsername("testUser");
		when(authenticationService.getAuthentication("testUser")).thenReturn(authentication1);

		UserInfo loggedInUserInfo = createUserInfo("anotherUser", "ROLE_VIEWER", "anotherUser@gmail.com");
		when(userInfoService.getUserInfo("anotherUser")).thenReturn(loggedInUserInfo);

		String expectedResponse = "{\"message\":\"You are not authorised to get this user's details\",\"success\":false}";

		Principal principal = Mockito.mock(Principal.class);
		when(principal.getName()).thenReturn("anotherUser");

		mockMvc.perform(get("/users/testUser").accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).principal(principal)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().json(expectedResponse));
	}

	@Test
	public void updateUserInfo_Success() throws Exception {

		UserInfo userInfo = createUserInfo("SUPERADMIN", "ROLE_SUPERADMIN");

		when(userInfoService.getUserInfo("SUPERADMIN")).thenReturn(userInfo);

		when(authenticationService.isEmailExist(anyString())).thenReturn(false);

		com.publicissapient.kpidashboard.apis.auth.model.Authentication authentication = new com.publicissapient.kpidashboard.apis.auth.model.Authentication();
		authentication.setUsername("SUPERADMIN");

		when(authenticationService.getAuthentication("SUPERADMIN")).thenReturn(authentication);

		String expectedResponse = "{\"message\":\"Email updated successfully\",\"success\":true,\"data\":{\"username\":\"SUPERADMIN\",\"authorities\":[\"ROLE_SUPERADMIN\"],\"authType\":\"STANDARD\",\"emailAddress\":\"test@gmail.com\"}}";

		Principal principal = Mockito.mock(Principal.class);
		when(principal.getName()).thenReturn("SUPERADMIN");

		mockMvc.perform(put("/users/SUPERADMIN/updateEmail").accept(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"test@gmail.com\"}").contentType(MediaType.APPLICATION_JSON).principal(principal))
				.andExpect(status().isOk()).andExpect(MockMvcResultMatchers.content().json(expectedResponse));
	}

	@Test
	public void updateUserInfo_UserNotFound() throws Exception {

		String expectedResponse = "{'message':'user not found with username testUser','success':false}";

		Principal principal = Mockito.mock(Principal.class);
		when(principal.getName()).thenReturn("SUPERADMIN");

		mockMvc.perform(put("/users/testUser/updateEmail").accept(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"test@gmail.com\"}").contentType(MediaType.APPLICATION_JSON).principal(principal))
				.andExpect(status().isOk()).andExpect(MockMvcResultMatchers.content().json(expectedResponse));
	}

	@Test
	public void updateUserInfo_UnAuthorizedAccess() throws Exception {

		com.publicissapient.kpidashboard.apis.auth.model.Authentication authentication1 = new com.publicissapient.kpidashboard.apis.auth.model.Authentication();
		authentication1.setUsername("testUser");
		when(authenticationService.getAuthentication("testUser")).thenReturn(authentication1);

		String expectedResponse = "{\"message\":\"You are not authorised to update the email\",\"success\":false}";

		Principal principal = Mockito.mock(Principal.class);
		when(principal.getName()).thenReturn("anotherUser");

		mockMvc.perform(put("/users/testUser/updateEmail").accept(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"test@gmail.com\"}").contentType(MediaType.APPLICATION_JSON).principal(principal))
				.andExpect(status().isOk()).andExpect(MockMvcResultMatchers.content().json(expectedResponse));
	}

	@Test
	public void updateUserInfo_EmailAlreadyRegistered() throws Exception {

		when(authenticationService.isEmailExist(anyString())).thenReturn(true);

		com.publicissapient.kpidashboard.apis.auth.model.Authentication authentication = new com.publicissapient.kpidashboard.apis.auth.model.Authentication();
		authentication.setUsername("SUPERADMIN");

		when(authenticationService.getAuthentication("SUPERADMIN")).thenReturn(authentication);

		String expectedResponse = "{\"message\":\"Email already registered. Try with a different email id\",\"success\":false}";

		Principal principal = Mockito.mock(Principal.class);
		when(principal.getName()).thenReturn("SUPERADMIN");

		mockMvc.perform(put("/users/SUPERADMIN/updateEmail").accept(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"test@gmail.com\"}").contentType(MediaType.APPLICATION_JSON).principal(principal))
				.andExpect(status().isOk()).andExpect(MockMvcResultMatchers.content().json(expectedResponse));
	}

	@Test
	public void updateUserInfo_EmptyEmail() throws Exception {
		com.publicissapient.kpidashboard.apis.auth.model.Authentication authentication = new com.publicissapient.kpidashboard.apis.auth.model.Authentication();
		authentication.setUsername("SUPERADMIN");

		when(authenticationService.getAuthentication("SUPERADMIN")).thenReturn(authentication);

		String expectedResponse = "{\"message\":\"Provide a valid email id\",\"success\":false}";

		Principal principal = Mockito.mock(Principal.class);
		when(principal.getName()).thenReturn("SUPERADMIN");

		mockMvc.perform(put("/users/SUPERADMIN/updateEmail").accept(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"\"}").contentType(MediaType.APPLICATION_JSON).principal(principal))
				.andExpect(status().isOk()).andExpect(MockMvcResultMatchers.content().json(expectedResponse));
	}

	private UserInfo createUserInfo(String username, String role, String email) {
		UserInfo userInfo = new UserInfo();
		userInfo.setAuthorities(Arrays.asList(role));
		userInfo.setUsername(username);
		userInfo.setAuthType(AuthType.STANDARD);
		if (email == null) {
			userInfo.setEmailAddress("test@gmail.com");
		} else {
			userInfo.setEmailAddress(email);
		}

		return userInfo;
	}

	private UserInfo createUserInfo(String username, String role) {
		return createUserInfo(username, role, null);
	}

}
