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

import static net.oauth.OAuth.OAUTH_VERIFIER;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.httpclient.api.Request.Builder;
import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import org.htmlunit.FailingHttpStatusCodeException;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlPasswordInput;
import org.htmlunit.html.HtmlSubmitInput;
import org.htmlunit.html.HtmlTextInput;
import com.google.common.collect.ImmutableList;
import com.publicissapient.kpidashboard.jira.config.JiraOAuthProperties;

import lombok.extern.slf4j.Slf4j;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthServiceProvider;
import net.oauth.ParameterStyle;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;
import net.oauth.http.HttpMessage;
import net.oauth.signature.RSA_SHA1;

/**
 * Provides Jira OAuth client authentication services
 * 
 * @author vijkumar18
 *
 */
@Component
@Slf4j
public class JiraOAuthClient implements AuthenticationHandler {

	protected static final String SERVLET_BASE_URL = "/plugins/servlet";
	private OAuthAccessor accessor;

	@Autowired
	private JiraOAuthProperties jiraOAuthProperties;

	public JiraOAuthClient() {
	}

	public JiraOAuthClient(JiraOAuthProperties jiraOAuthProperties) {
		this.jiraOAuthProperties = jiraOAuthProperties;
	}

	/**
	 * Generates requestToken
	 * 
	 * @return RequestToken
	 */
	public TokenSecretVerifierHolder getRequestToken() {
		try {
			accessor = getAccessor();
			OAuthClient oAuthClient = new OAuthClient(new HttpClient4());
			List<OAuth.Parameter> callBack;
			if (getJiraCallbackURL() == null) {
				callBack = Collections.<OAuth.Parameter>emptyList();
			} else {
				callBack = ImmutableList.of(new OAuth.Parameter(OAuth.OAUTH_CALLBACK, getJiraCallbackURL()));
			}

			OAuthMessage message = oAuthClient.getRequestTokenResponse(accessor, "POST", callBack);
			TokenSecretVerifierHolder tokenSecretVerifier = new TokenSecretVerifierHolder();
			tokenSecretVerifier.token = accessor.requestToken;
			tokenSecretVerifier.secret = accessor.tokenSecret;
			tokenSecretVerifier.verifier = message.getParameter(OAUTH_VERIFIER);
			return tokenSecretVerifier;
		} catch (IOException | OAuthException | URISyntaxException e) {
			throw new RuntimeException("Failed to obtain request token", e);// NOSONAR
		}
	}

	/**
	 * Generates accessToken
	 * 
	 * @param requestToken
	 *            request token
	 * @param tokenSecret
	 *            secret
	 * @param oauthVerifier
	 *            oauth verifier
	 * @return accessToken
	 */
	public String swapRequestTokenForAccessToken(String requestToken, String tokenSecret, String oauthVerifier) {
		try {
			accessor = getAccessor();
			OAuthClient client = new OAuthClient(new HttpClient4());
			accessor.requestToken = requestToken;
			accessor.tokenSecret = tokenSecret;
			OAuthMessage message = client.getAccessToken(accessor, "POST",
					ImmutableList.of(new OAuth.Parameter(OAuth.OAUTH_VERIFIER, oauthVerifier)));
			return message.getToken();
		} catch (IOException | OAuthException | URISyntaxException e) {
			throw new RuntimeException("Failed to get Token from Access Token", e);// NOSONAR
		}

	}

	/**
	 * Provides OAuthAccessor
	 * 
	 * @return OAuthAccessor
	 */
	public final OAuthAccessor getAccessor() {
		if (accessor == null) {
			OAuthServiceProvider serviceProvider = new OAuthServiceProvider(getRequestTokenUrl(), getAuthorizeUrl(),
					getAccessTokenUrl());
			OAuthConsumer consumer = new OAuthConsumer(getJiraCallbackURL(), jiraOAuthProperties.getConsumerKey(), null,
					serviceProvider);
			consumer.setProperty(RSA_SHA1.PRIVATE_KEY, jiraOAuthProperties.getPrivateKey());
			consumer.setProperty(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.RSA_SHA1);
			accessor = new OAuthAccessor(consumer);
		}
		return accessor;
	}

	private String getAccessTokenUrl() {
		return jiraOAuthProperties.getJiraBaseURL() + SERVLET_BASE_URL + "/oauth/access-token";
	}

	private String getRequestTokenUrl() {
		return jiraOAuthProperties.getJiraBaseURL() + SERVLET_BASE_URL + "/oauth/request-token";
	}

	public String getAuthorizeUrlForToken(String token) {
		return getAuthorizeUrl() + "?oauth_token=" + token;
	}

	private String getAuthorizeUrl() {
		return jiraOAuthProperties.getJiraBaseURL() + SERVLET_BASE_URL + "/oauth/authorize";
	}

	public String getJiraCallbackURL() {
		return jiraOAuthProperties.getJiraBaseURL() + "/token/";
	}

	/**
	 * Provides oauthVerifier
	 *
	 * @param authorizationUrl
	 *            authorizationUrl
	 * @param username
	 *            username
	 * @param password
	 *            password
	 * @return oauthVerifier
	 * @throws IOException
	 *             IOException
	 */
	public String getOAuthVerifier(String authorizationUrl, String username, String password) throws IOException {
		String oauthVerifier = null;

		try (final WebClient webClient = new WebClient()) {
			webClient.getOptions().setJavaScriptEnabled(true);
			webClient.getOptions().setRedirectEnabled(true);
			webClient.getOptions().setThrowExceptionOnScriptError(false);
			webClient.getOptions().setCssEnabled(true);
			webClient.getOptions().setPrintContentOnFailingStatusCode(false);
			webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);

			HtmlPage page = webClient.getPage(authorizationUrl);
			String loginUrl = page.getUrl().toString();

			try {

				final HtmlPage loginPage = webClient.getPage(loginUrl);
				final HtmlForm loginForm = loginPage.getHtmlElementById("login-form");

				final HtmlTextInput txtUser = loginForm.getInputByName("os_username");
				txtUser.setValueAttribute(username);

				final HtmlPasswordInput txtpass = loginForm.getInputByName("os_password");
				txtpass.setValueAttribute(password);

				final HtmlSubmitInput submitLogin = loginForm.getInputByName("login");

				final HtmlPage returnPage = submitLogin.click();

				final HtmlForm allowForm = returnPage.getForms().get(0);

				final HtmlSubmitInput submitAllowForm = allowForm.getInputByName("approve");
				final HtmlPage returnToVerifierPage = submitAllowForm.click();

				String queryData = returnToVerifierPage.getUrl().getQuery();
				String[] data = queryData.split("oauth_verifier=");
				oauthVerifier = data[1];

			} catch (FailingHttpStatusCodeException e) {
				log.error("HTTP Status code error while generating oauth_verifier", e);
			} catch (Exception e) {
				log.error("Error while generating oauth_verifier", e);
			}
			return oauthVerifier;
		}

	}

	/**
	 * Provides acessToken
	 *
	 * @param username
	 *            username
	 * @param password
	 *            password
	 * @return acessToken acessToken
	 * @throws IOException
	 *             IOException
	 */
	public String getAccessToken(String username, String password) throws IOException {

		JiraOAuthClient jiraOauthClient = new JiraOAuthClient(jiraOAuthProperties);
		TokenSecretVerifierHolder requestToken = jiraOauthClient.getRequestToken();

		// Provides Authorization URL
		final String authorizeUrl = jiraOauthClient.getAuthorizeUrlForToken(requestToken.token);

		// Provides oauthVerifier
		String oauthVerifier = getOAuthVerifier(authorizeUrl, username, password);

		// Provides accessToken
		return jiraOauthClient.swapRequestTokenForAccessToken(requestToken.token, jiraOAuthProperties.getConsumerKey(),
				oauthVerifier);

	}

	@Override
	public void configure(Builder request) {
		try {
			accessor = getAccessor();
			accessor.accessToken = jiraOAuthProperties.getAccessToken();
			OAuthMessage request2 = accessor.newRequestMessage(null, request.build().getUri().toString(),
					Collections.<Map.Entry<?, ?>>emptySet(), request.build().getEntityStream());
			Object accepted = accessor.consumer.getProperty(OAuthConsumer.ACCEPT_ENCODING);
			if (accepted != null) {
				request2.getHeaders().add(new OAuth.Parameter(HttpMessage.ACCEPT_ENCODING, accepted.toString()));
			}
			Object ps = accessor.consumer.getProperty(OAuthClient.PARAMETER_STYLE);
			ParameterStyle style = (ps == null) ? ParameterStyle.BODY
					: Enum.valueOf(ParameterStyle.class, ps.toString());
			HttpMessage httpRequest = HttpMessage.newRequest(request2, style);
			for (Entry<String, String> ap : httpRequest.headers)
				request.setHeader(ap.getKey(), ap.getValue());
			request.setUri(httpRequest.url.toURI());

		} catch (Exception e) {
			log.error("Error while authenticating jira OAuth", e);
		}

	}

	@SuppressWarnings("unused")
	final class TokenSecretVerifierHolder {
		private String token;
		private String verifier;// NOSONAR
		private String secret;// NOSONAR
	}

}
