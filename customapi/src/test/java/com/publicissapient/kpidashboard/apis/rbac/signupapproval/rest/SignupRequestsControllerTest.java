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


package com.publicissapient.kpidashboard.apis.rbac.signupapproval.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicissapient.kpidashboard.apis.auth.AuthProperties;
import com.publicissapient.kpidashboard.apis.auth.model.Authentication;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.auth.token.CookieUtil;
import com.publicissapient.kpidashboard.apis.common.service.impl.UserInfoServiceImpl;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.rbac.signupapproval.service.SignupManager;
import com.publicissapient.kpidashboard.common.constant.AuthType;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfoDTO;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class SignupRequestsControllerTest {

	Authentication authentication;
	ObjectMapper mapper = new ObjectMapper();
	@Mock
	AuthenticationService authenticationService;
	@Mock
	SignupManager signupManager;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	UserInfoServiceImpl userInfoService;
	private MockMvc mockMvc;
	private String testId;
	@Mock
	private CookieUtil cookieUtil;
	@InjectMocks
	private SignupRequestsController signupRequestsController;

	@Mock
	AuthProperties authProperties;

	@Before
	public void before() {
		testId = "5dbfcc60e645ca2ee4075381";
		authentication = new Authentication();
		authentication.setId(new ObjectId(testId));
		authentication.setUsername("testUser");
		authentication.setEmail("testUser@gmail.com");
		authentication.setApproved(false);
		mockMvc = MockMvcBuilders.standaloneSetup(signupRequestsController).build();
	}

	@After
	public void after() {
		mockMvc = null;
	}

	/**
	 * method to get all unapproved requests when CA switch is Off
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetUnApprovedRequests() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/userapprovals").contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
	}

	/**
	 * method to get all unapproved requests when CA switch is Off
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetUnApprovedRequests_ForCentralAuth() throws Exception {
		List<UserInfoDTO> userInfoDTOS =new ArrayList<>();
		UserInfoDTO userInfoDTO = new UserInfoDTO();
		userInfoDTO.setAuthType(AuthType.APIKEY);
		userInfoDTO.setEmailAddress("abc.test@test.com");
		userInfoDTOS.add(userInfoDTO);
		Mockito.when(userInfoService.findAllUnapprovedUsersForCentralAuth()).thenReturn(userInfoDTOS);
		mockMvc.perform(MockMvcRequestBuilders.get("/userapprovals/central").contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
	}

	/**
	 * method to test GET /grantrequest/all restPoint ;
	 *
	 * Get all signup requests
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetAllRequests() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/userapprovals/all").contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
	}

}
