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

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.atlassian.httpclient.api.Request;
import com.publicissapient.kpidashboard.jira.config.JiraOAuthProperties;

import net.oauth.*;

@RunWith(MockitoJUnitRunner.class)
public class JiraOAuthClientTest {

	@Mock
	private OAuthAccessor mockAccessor;

	@Spy
	private static JiraOAuthProperties jiraOAuthPropertiesSpy;

	@Mock
	private Request.Builder builder;

	@Mock
	private Request request;

	@Mock
	private OAuthMessage oAuthMessage;

	@Mock
	private InputStream inputStream;

	/*
	 * @Spy private static OAuthConsumer consumer;
	 */

	@Mock
	private static OAuthServiceProvider serviceProvider;

	@InjectMocks
	JiraOAuthClient jiraOAuthClient;

	@BeforeClass
	public static void setUpMethod() {
		jiraOAuthPropertiesSpy = new JiraOAuthProperties();
		jiraOAuthPropertiesSpy.setJiraBaseURL("http://www.basedummyurl.com");
		jiraOAuthPropertiesSpy.setConsumerKey("consumerKey");
		jiraOAuthPropertiesSpy.setPrivateKey("pvtKey");
		jiraOAuthPropertiesSpy.setAccessToken("accesstoken");
		// consumer =new
		// OAuthConsumer("https://www.callbkurl.com/","consumerKey","consumerSecret",serviceProvider);
		// consumer.setProperty("RSA-SHA1.PrivateKey","pvtKey");
		// consumer.
	}

	@Test(expected = RuntimeException.class)
	public void getRequestTokenNullTest() {
		JiraOAuthClient.TokenSecretVerifierHolder tokenSecretVerifierHolder = jiraOAuthClient.getRequestToken();
	}

	@Test(expected = RuntimeException.class)
	public void swapRequestTokenForAccessTokenTest() {
		jiraOAuthClient.swapRequestTokenForAccessToken("requestToken", "tokenSecret", "oauthVerifier");
	}

	@Test
	public void getAccessorTest() {
		OAuthAccessor oAuthAccessor = jiraOAuthClient.getAccessor();
		assertNotNull(oAuthAccessor);
		assertEquals("http://www.basedummyurl.com/token/", oAuthAccessor.consumer.callbackURL);
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
		String callBack = null;
		try {
			callBack = jiraOAuthClient.getOAuthVerifier("https://www.baseurl.com/", "uName", "password");
		} catch (Exception ex) {
			assertNull(callBack);
		}
	}

	@Test(expected = RuntimeException.class)
	public void getAccessTokenTest() throws IOException {
		String tokenStr = jiraOAuthClient.getAccessToken("uName", "password");
		// assertNotNull(tokenStr);
	}

	@Test(expected = RuntimeException.class)
	public void getAccessTokenNullTest() throws IOException {
		jiraOAuthClient.getAccessToken("uName", "password");
	}

	@Test
	public void configureTest() throws URISyntaxException, OAuthException, IOException {

		when(builder.build()).thenReturn(request);
		when(request.getUri()).thenReturn(new URI("https://www.baseurl.com/"));
		when(request.getEntityStream()).thenReturn(inputStream);

		// OAuthAccessor accessor=new OAuthAccessor(consumer);
		// OAuthAccessor spyOAuthAccessor = Mockito.spy(accessor);
		OAuthMessage oAuthMessage = new OAuthMessage("method", "https://www.baseurl.com?ss=k", null);
		OAuthMessage spyOAuthMessage = Mockito.spy(oAuthMessage);
		// accessor.consumer.setProperty("RSA-SHA1.PrivateKey","pvtKey");
		// when(spyOAuthAccessor.newRequestMessage(anyString(),anyString(),
		// anySet(),eq(inputStream))).thenReturn(spyOAuthMessage);
		// when(accessor.consumer.getProperty("RSA-SHA1.PrivateKey")).thenReturn("pvt");
		// doNothing().when(oAuthMessage).addRequiredParameters(any());
		// when(oAuthMessage.sign(accessor)).thenReturn()
		// MockedStatic<OAuth> oAuthMockedStatic = Mockito.mockStatic(OAuth.class);
		jiraOAuthClient.configure(builder);
		verify(builder, times(2)).build();
	}
}
