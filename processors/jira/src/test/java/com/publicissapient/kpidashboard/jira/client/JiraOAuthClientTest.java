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

package com.publicissapient.kpidashboard.jira.client;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.atlassian.httpclient.api.Request;
import com.publicissapient.kpidashboard.jira.config.JiraOAuthProperties;

import net.oauth.OAuthAccessor;

@RunWith(MockitoJUnitRunner.class)
public class JiraOAuthClientTest {

	@Mock
	private OAuthAccessor accessor;

	@Mock
	private JiraOAuthProperties jiraOAuthProperties;

	@Mock
	private Request.Builder builder;

	@Mock
	private Request request;

	@InjectMocks
	JiraOAuthClient jiraOAuthClient;

	@Test(expected = NullPointerException.class)
	public void getRequestTokenTest() {
		jiraOAuthClient.getRequestToken();
	}

	@Test(expected = NullPointerException.class)
	public void swapRequestTokenForAccessTokenTest() {
		jiraOAuthClient.swapRequestTokenForAccessToken("requestToken", "tokenSecret", "oauthVerifier");
	}

	@Test
	public void getAccessorTest() {
		OAuthAccessor oAuthAccessor = jiraOAuthClient.getAccessor();
		assertNotNull(oAuthAccessor);
		assertEquals("null/token/", oAuthAccessor.consumer.callbackURL);
	}

	@Test
	public void getAuthorizeUrlForTokenTest() {
		String token = jiraOAuthClient.getAuthorizeUrlForToken("token");
		assertNotNull(token);
	}

	@Test
	public void getJiraCallbackURLTest() {
		String callBack = jiraOAuthClient.getJiraCallbackURL();
		assertNotNull(callBack);
	}

	@Test
	public void getOAuthVerifierTest() throws IOException {
		String callBack = jiraOAuthClient.getOAuthVerifier("https://www.baseurl.com/", "uName", "password");
		assertNull(callBack);
	}

	@Test(expected = NullPointerException.class)
	public void getAccessTokenTest() throws IOException {
		jiraOAuthClient.getAccessToken("uName", "password");

	}

	@Test
	public void configureTest() throws URISyntaxException {

		when(builder.build()).thenReturn(request);
		when(request.getUri()).thenReturn(new URI("https://www.baseurl.com/"));
		when(request.getEntityStream()).thenReturn(new InputStream() {
			@Override
			public int read() throws IOException {
				return 7;
			}
		});
		jiraOAuthClient.configure(builder);
		verify(builder,times(2)).build();

	}

}
