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

package com.publicissapient.kpidashboard.apis.auth.service;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.auth.model.Authentication;
import com.publicissapient.kpidashboard.apis.auth.repository.AuthenticationRepository;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthenticationServiceTest {

	@Mock
	AuthenticationRepository authRepo;
	@InjectMocks
	DefaultAuthenticationServiceImpl authService;

	@Test
	public void testOldPwAuthentication() throws Exception {
		final String pw = "pass1";

		Authentication nonHashPass = new Authentication("u1", pw, "abc@xyz.com");
		Field pwField = nonHashPass.getClass().getDeclaredField("password");
		pwField.setAccessible(true);
		pwField.set(nonHashPass, pw);
		nonHashPass.setApproved(true);
		when(authRepo.findByUsername(Mockito.anyString())).thenReturn(nonHashPass);
		assertNotNull(authService.authenticate("u1", "pass1"));
	}

	@Test
	public void testHashedPwAuthentication() throws Exception {
		final String pw = "pass1";

		Authentication auth = new Authentication("u1", pw, "abc@xyz.com");
		auth.setApproved(true);
		when(authRepo.findByUsername(Mockito.anyString())).thenReturn(auth);
		assertNotNull(authService.authenticate("u1", "pass1"));
	}
}
