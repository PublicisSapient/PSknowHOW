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

package com.publicissapient.kpidashboard.jira.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;

import com.atlassian.httpclient.api.Request;

@RunWith(MockitoJUnitRunner.class)
public class SpnegoAuthenticationHandlerTest {

	private static final String COOKIE_HEADER = "Cookie";

	@Test
	public void testConfigure() {
		// Arrange
		String authCookies = "sampleAuthCookies";
		SpnegoAuthenticationHandler authenticationHandler = new SpnegoAuthenticationHandler(authCookies);

		Request.Builder mockRequestBuilder = mock(Request.Builder.class);

		// Act
		authenticationHandler.configure(mockRequestBuilder);

		// Assert
		ArgumentCaptor<String> headerNameCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> headerValueCaptor = ArgumentCaptor.forClass(String.class);

		verify(mockRequestBuilder, times(1)).setHeader(headerNameCaptor.capture(), headerValueCaptor.capture());

		assertEquals(COOKIE_HEADER, headerNameCaptor.getValue(), "Cookie");
		assertEquals(authCookies, headerValueCaptor.getValue(), "sampleAuthCookies");
	}
}
