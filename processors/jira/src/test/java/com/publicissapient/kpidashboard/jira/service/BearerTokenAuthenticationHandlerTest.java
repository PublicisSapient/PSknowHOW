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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.kafka.common.security.oauthbearer.internals.secured.HttpAccessTokenRetriever;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.atlassian.httpclient.api.Request;

@RunWith(MockitoJUnitRunner.class)
public class BearerTokenAuthenticationHandlerTest {

	@Mock
	private Request.Builder builder;

	@InjectMocks
	private BearerTokenAuthenticationHandler bearerTokenAuthenticationHandler;

	@Test
	public void configureTest() {
		bearerTokenAuthenticationHandler.configure(builder);
		verify(builder, times(1)).setHeader(eq(HttpAccessTokenRetriever.AUTHORIZATION_HEADER), anyString());
	}
}
