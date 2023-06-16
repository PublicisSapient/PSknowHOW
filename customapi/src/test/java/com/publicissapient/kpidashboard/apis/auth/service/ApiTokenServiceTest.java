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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.Sets;
import com.publicissapient.kpidashboard.apis.auth.model.ApiToken;
import com.publicissapient.kpidashboard.apis.auth.repository.ApiTokenRepository;
import com.publicissapient.kpidashboard.common.util.Encryption;

@RunWith(MockitoJUnitRunner.class)
public class ApiTokenServiceTest {

	@Mock
	private ApiTokenRepository apiTokenRepository;

	@InjectMocks
	private ApiTokenServiceImpl service;

	@Test
	public void shouldGetAllApiTokens() {
		ApiToken apiToken = new ApiToken("somesys", "dgferdf1drt5dfgdfh4mh+34dfwr3Wdf", 1496030399999L);
		Collection<ApiToken> apiTokens = Sets.newHashSet(apiToken, apiToken);
		when(apiTokenRepository.findAll()).thenReturn(apiTokens);

		Collection<ApiToken> result = service.getApiTokens();

		assertTrue(result.contains(apiToken));
		assertTrue(result.size() == 1);
	}

	@Test
	public void testGetApiTokenThrowsException() {
		ApiToken apiToken = new ApiToken("somesys", "dgferdf1drt5dfgdfh4mh+34dfwr3Wdf", 1496030399999L);
		String apiKey;
		when(apiTokenRepository.findByApiUserAndExpirationDt("apiUser", 1496030399999L)).thenReturn(apiToken);
		try {
			apiKey = Encryption.getStringKey();
			apiToken = new ApiToken("apiUser", apiKey, 1496030399999L);
			apiTokenRepository.save(apiToken);
			service.getApiToken("apiUser", 1496030399999L);
		} catch (Exception e) {

		}

	}

	@Test
	public void testGetApiToken() {
		ApiToken apiToken = new ApiToken("somesys", "dgferdf1drt5dfgdfh4mh+34dfwr3Wdf", 1496030399999L);
		String apiKey;
		when(apiTokenRepository.findByApiUserAndExpirationDt("apiUser", 1496030399999L)).thenReturn(null);
		try {
			apiKey = Encryption.getStringKey();
			apiToken = new ApiToken("apiUser", apiKey, 1496030399999L);
			apiTokenRepository.save(apiToken);
			service.getApiToken("apiUser", 1496030399999L);
		} catch (Exception e) {

		}
	}

	@Test
	public void testAuthenticateThrowsException() {
		ApiToken apiToken = new ApiToken("somesys", "dgferdf1drt5dfgdfh4mh+34dfwr3Wdf", 1496030399999L);
		List<ApiToken> apiTokens = new ArrayList<>();
		apiTokens.add(apiToken);
		when(apiTokenRepository.findByApiUser("somesys")).thenReturn(apiTokens);
		try {
			service.authenticate("somesys", "dgferdf1drt5dfgdfh4mh+34dfwr3Wdf");
		} catch (Exception e) {

		}
	}

	@Test
	public void testAuthenticate() {
		ApiToken apiToken = new ApiToken("somesys", "dgferdf1drt5dfgdfh4mh+34dfwr3Wdf", 1496030399999L);
		List<ApiToken> apiTokens = new ArrayList<>();
		apiTokens.add(apiToken);
		when(apiTokenRepository.findByApiUser("somesys")).thenReturn(apiTokens);
		Collection<String> roles = new ArrayList<>();
		roles.add("ProjectViewer");
		try {
			service.authenticate("somesys", "dgferdf1drt5dfgdfh4mh+34dfwr3Wdf");
		} catch (Exception e) {

		}
	}

}
