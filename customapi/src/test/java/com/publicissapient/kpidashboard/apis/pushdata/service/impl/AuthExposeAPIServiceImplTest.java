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

package com.publicissapient.kpidashboard.apis.pushdata.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.time.LocalDate;


import jakarta.servlet.http.HttpServletRequest;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.abac.ProjectAccessManager;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.pushdata.model.ExposeApiToken;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.ExposeAPITokenRequestDTO;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.ExposeAPITokenResponseDTO;
import com.publicissapient.kpidashboard.apis.pushdata.repository.ExposeApiTokenRepository;
import com.publicissapient.kpidashboard.apis.pushdata.service.PushDataTraceLogService;
import com.publicissapient.kpidashboard.apis.pushdata.util.PushDataException;

@RunWith(MockitoJUnitRunner.class)
public class AuthExposeAPIServiceImplTest {

	@InjectMocks
	private AuthExposeAPIServiceImpl authExposeAPIService;

	@Mock
	private ExposeApiTokenRepository exposeApiTokenRepository;

	@Mock
	private HttpServletRequest httpServletRequest;

	@Mock
	private ProjectAccessManager projectAccessManager;

	@Mock
	private CustomApiConfig customApiConfig;

	@Mock
	private PushDataTraceLogService pushDataTraceLogService;

	private ExposeApiToken exposeApiTokenDbExist;

	@Before
	public void setup() {
		exposeApiTokenDbExist = new ExposeApiToken();
		exposeApiTokenDbExist.setId(new ObjectId("6335363749794a18e8a4479b"));
		exposeApiTokenDbExist.setBasicProjectConfigId(new ObjectId("61e4f7852747353d4405c762"));
		exposeApiTokenDbExist.setProjectName("Test_Old_project");
		exposeApiTokenDbExist.setApiToken("valid_token");
		exposeApiTokenDbExist.setUserName("SUPERADMIN");
		exposeApiTokenDbExist.setCreatedAt(LocalDate.now());
		exposeApiTokenDbExist.setExpiryDate(LocalDate.now().plusDays(30));

		when(customApiConfig.getExposeAPITokenExpiryDays()).thenReturn(30);
	}

	@Test
	public void generateTokenForNewProjectOrNewUser() {
		ExposeAPITokenRequestDTO exposeAPITokenRequestNew = new ExposeAPITokenRequestDTO();
		exposeAPITokenRequestNew.setBasicProjectConfigId("632824e949794a18e8a44787");
		exposeAPITokenRequestNew.setProjectName("Test_New_Project");
		exposeAPITokenRequestNew.setUserName("SUPERADMIN");

		ServiceResponse response = authExposeAPIService.generateAndSaveToken(exposeAPITokenRequestNew);
		Assert.assertNotNull(response.getData());
		Assert.assertNotNull(((ExposeAPITokenResponseDTO) response.getData()).getApiToken());
	}

	@Test
	public void generateTokenForExistingProject() {
		ExposeAPITokenRequestDTO exposeAPITokenRequestDbExist = new ExposeAPITokenRequestDTO();
		exposeAPITokenRequestDbExist.setBasicProjectConfigId("61e4f7852747353d4405c762");
		exposeAPITokenRequestDbExist.setProjectName("Test_Old_Project");
		exposeAPITokenRequestDbExist.setUserName("SUPERADMIN");

		when(exposeApiTokenRepository.findByUserNameAndBasicProjectConfigId("SUPERADMIN",
				new ObjectId("61e4f7852747353d4405c762"))).thenReturn(exposeApiTokenDbExist);

		ServiceResponse response = authExposeAPIService.generateAndSaveToken(exposeAPITokenRequestDbExist);
		Assert.assertNotNull(response.getData());
		Assert.assertNotNull(((ExposeAPITokenResponseDTO) response.getData()).getApiToken());
	}

	@Test
	public void validateTokenPushDataWithValidToken() {
		when(httpServletRequest.getHeader("Api-Key")).thenReturn("valid_token");
		when(exposeApiTokenRepository.findByApiToken("valid_token")).thenReturn(exposeApiTokenDbExist);

		when(projectAccessManager.hasProjectEditPermission(new ObjectId("61e4f7852747353d4405c762"), "SUPERADMIN"))
				.thenReturn(true);
		ExposeApiToken exposeApiToken = authExposeAPIService.validateToken(httpServletRequest);

		Assert.assertEquals(exposeApiToken, exposeApiTokenDbExist);
	}

	@Test(expected = PushDataException.class)
	public void validateTokenPushDataWithInValidToken() {
		when(httpServletRequest.getHeader("Api-Key")).thenReturn("invalid_token");
		when(exposeApiTokenRepository.findByApiToken("invalid_token")).thenReturn(null);
		doThrow(new PushDataException()).when(pushDataTraceLogService).setExceptionTraceLog(anyString(),
				any(Object.class));
		authExposeAPIService.validateToken(httpServletRequest);

	}
}
