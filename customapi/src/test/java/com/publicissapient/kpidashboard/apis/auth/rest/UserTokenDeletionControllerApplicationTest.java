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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.publicissapient.kpidashboard.apis.auth.AuthProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.publicissapient.kpidashboard.apis.auth.service.UserTokenDeletionService;
import com.publicissapient.kpidashboard.apis.auth.token.CookieUtil;
import com.publicissapient.kpidashboard.apis.common.service.impl.UserInfoServiceImpl;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@RunWith(MockitoJUnitRunner.class)
@WebMvcTest(UserTokenDeletionControllerApplication.class)
public class UserTokenDeletionControllerApplicationTest extends Mockito {

	private MockMvc mockMvc;

	@Mock
	private UserTokenDeletionService userTokenDeletionService;

	@Mock
	private CustomApiConfig customApiConfig;

	@Mock
	private CookieUtil cookieUtil;
	@Mock
	UserInfoServiceImpl userInfoService;

	@Mock
	AuthProperties authProperties;

	@InjectMocks
	private UserTokenDeletionControllerApplication userTokenDeletionControllerApplication;

	@Before
	public void before() {
		MockitoAnnotations.openMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(userTokenDeletionControllerApplication).build();

	}

	@After
	public void after() {
		mockMvc = null;
	}

	@Test
	public void testDeleteUserToken() throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		ResponseCookie foo11 = ResponseCookie.from("foo1", "bar1").build();
		when(cookieUtil.deleteAccessTokenCookie()).thenReturn(foo11);
		request.setAttribute("Authorization", "Bearer abcde");
		mockMvc.perform(get("/userlogout").cookie(new Cookie("foo1", "bar1")))
				.andExpect(status().isOk());


	}

}
