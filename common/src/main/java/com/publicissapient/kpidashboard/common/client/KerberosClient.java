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

package com.publicissapient.kpidashboard.common.client;

import java.io.IOException;
import java.security.Principal;
import java.security.PrivilegedAction;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.web.client.RestClientException;

import lombok.extern.slf4j.Slf4j;

/**
 * Kerberos client to connect with SPNEGO jira client
 */
@Slf4j
public class KerberosClient {
	private static final String COOKIE_HEADER = "Cookie";
	private static final String AUTH_LOGIN_CONFIG = "java.security.auth.login.config";
	private static final String KERB_CONFIG = "java.security.krb5.conf";
	private static final String AUTH_USE_SUBJECT_CREDS_ONLY = "javax.security.auth.useSubjectCredsOnly";
	private static final String AUTH_PREFERENCES = "http.auth.preference";
	private static final Credentials credentials = new NullCredentials();

	private String jaasConfigFilePath;
	private String krb5ConfigFilePath;
	private HttpClient loginHttpClient;
	private HttpClient httpClient;
	private BasicCookieStore cookieStore;
	private String JaasUser;
	private String samlEndPoint;
	private String jiraHost;

	/**
	 * Kerberos client constructor
	 * 
	 * @param jaasConfigFilePath
	 *            path to a file that contains login information
	 * @param krb5ConfigFilePath
	 *            path to a file that contains kerberos server information
	 * @param JaasUser
	 *            username whose properties need to be fetch from jaasConfigFilePath
	 * @param samlEndPoint
	 *            saml endpoint
	 * @param jiraHost
	 *            jira host.
	 */
	public KerberosClient(String jaasConfigFilePath, String krb5ConfigFilePath, String JaasUser, String samlEndPoint,
			String jiraHost) {
		this.jaasConfigFilePath = jaasConfigFilePath;
		this.krb5ConfigFilePath = krb5ConfigFilePath;
		this.loginHttpClient = buildLoginHttpClient();
		this.cookieStore = new BasicCookieStore();
		this.httpClient = buildHttpClient();
		this.JaasUser = JaasUser;
		this.samlEndPoint = samlEndPoint;
		this.jiraHost = jiraHost;
	}

	/**
	 * Get cookie store
	 * 
	 * @return basic cookie store.
	 */
	public BasicCookieStore getCookieStore() {
		return cookieStore;
	}

	/**
	 * Get jira host
	 * 
	 * @return jira host string
	 */
	public String getJiraHost() {
		return jiraHost;
	}

	/**
	 * This method build a Http client with SPNEGO scheme factory and cookie store
	 * 
	 * @return http client
	 */
	private HttpClient buildLoginHttpClient() {
		HttpClientBuilder builder = HttpClientBuilder.create();
		Lookup<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create()
				.register("negotiate", new SPNegoSchemeFactory(true)).build();
		builder.setDefaultAuthSchemeRegistry(authSchemeRegistry);
		BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(new AuthScope((String) null, -1, (String) null), credentials);
		builder.setDefaultCredentialsProvider(credentialsProvider);
		RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
		CloseableHttpClient httpClient = builder.setDefaultCookieStore(cookieStore)
				.setDefaultRequestConfig(requestConfig).build();
		return httpClient;
	}

	/**
	 * This method build simple Http client with cookie store
	 * 
	 * @return http client
	 */
	private HttpClient buildHttpClient() {
		RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
		return HttpClientBuilder.create().setDefaultCookieStore(cookieStore).setDefaultRequestConfig(requestConfig)
				.build();
	}

	/**
	 * This method set system level configuration for SPNEGO
	 */
	private void setKerberosProperties() {
		System.setProperty(AUTH_LOGIN_CONFIG, this.jaasConfigFilePath);
		System.setProperty(KERB_CONFIG, this.krb5ConfigFilePath);
		System.setProperty(AUTH_USE_SUBJECT_CREDS_ONLY, "false");
		System.setProperty(AUTH_PREFERENCES, "SPNEGO");
	}

	/**
	 * This method clear system level configuration for SPNEGO
	 */
	private void clearKerberosProperties() {
		System.clearProperty(AUTH_LOGIN_CONFIG);
		System.clearProperty(KERB_CONFIG);
		System.clearProperty(AUTH_USE_SUBJECT_CREDS_ONLY);
		System.clearProperty(AUTH_PREFERENCES);
	}

	/**
	 * This method fetch login cookies necessary to establish connection with spnego
	 * jira client
	 * 
	 * @param samlTokenStartString
	 * @param samlTokenEndString
	 * @param samlUrlStartString
	 * @param samlUrlEndString
	 * @return login response
	 */
	public String login(String samlTokenStartString, String samlTokenEndString, String samlUrlStartString,
			String samlUrlEndString) {
		try {
			String loginURL = this.samlEndPoint + this.jiraHost;
			setKerberosProperties();
			LoginContext lc = new LoginContext(this.JaasUser);
			lc.login();
			Subject serviceSubject = lc.getSubject();
			PrivilegedAction<String> action = () -> {
				try {
					return loginCall(loginURL, samlTokenStartString, samlTokenEndString, samlUrlStartString,
							samlUrlEndString);
				} catch (IOException e) {
					throw new RestClientException("error while logging in" + e.getMessage());
				}
			};
			return Subject.doAs(serviceSubject, action);
		} catch (Exception ex) {
			log.info("Exception thrown by kerberos login call: {}", ex.getMessage());
			throw new RestClientException("Error running rest call " + ex.getMessage());
		}
	}

	/**
	 * This method execute login call with http client
	 * 
	 * @param loginURL
	 * @param samlTokenStartString
	 * @param samlTokenEndString
	 * @param samlUrlStartString
	 * @param samlUrlEndString
	 * @return login response
	 * @throws IOException
	 */
	private String loginCall(String loginURL, String samlTokenStartString, String samlTokenEndString,
			String samlUrlStartString, String samlUrlEndString) throws IOException {
		HttpUriRequest getRequest = RequestBuilder.get().setUri(loginURL).build();
		HttpResponse response = this.loginHttpClient.execute(getRequest);
		HttpEntity entity = response.getEntity();
		String loginResponse = EntityUtils.toString(entity, "UTF-8");
		if (null != loginResponse && !loginResponse.equalsIgnoreCase("")) {
			log.debug("login response : {}", loginResponse);
			generateSamlCookies(loginResponse, samlTokenStartString, samlTokenEndString, samlUrlStartString,
					samlUrlEndString);
		} else {
			loginResponse = null;
		}
		clearKerberosProperties();
		return loginResponse;
	}

	/**
	 * This method generate the cookies required for connection
	 * 
	 * @param loginResponse
	 * @param samlTokenStartString
	 * @param samlTokenEndString
	 * @param samlUrlStartString
	 * @param samlUrlEndString
	 * @throws IOException
	 */
	public void generateSamlCookies(String loginResponse, String samlTokenStartString, String samlTokenEndString,
			String samlUrlStartString, String samlUrlEndString) throws IOException {
		String samlToken = extractString(loginResponse, samlTokenStartString, samlTokenEndString);
		String samlURL = extractString(loginResponse, samlUrlStartString, samlUrlEndString);
		log.debug("Saml Token extracted from login response: {}", samlToken);
		log.debug("Saml URL extracted from login response: {}", samlURL);
		HttpUriRequest postRequest = RequestBuilder.post().setUri(samlURL)
				.setHeader(HttpHeaders.ACCEPT, "application/json")
				.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
				.addParameter("SAMLResponse", samlToken).build();

		this.httpClient.execute(postRequest);
	}

	/**
	 * This method return the response of http request submitted
	 * 
	 * @param httpUriRequest
	 *            httpUriRequest
	 * @return string response
	 * @throws IOException
	 */
	public String getResponse(HttpUriRequest httpUriRequest) throws IOException {
		HttpResponse response = getHttpResponse(httpUriRequest);
		HttpEntity entity = response.getEntity();
		return EntityUtils.toString(entity, "UTF-8");
	}

	/**
	 * This method perform http request provided by user
	 * 
	 * @param httpUriRequest
	 *            httpUriRequest
	 * @return string http response
	 * @throws IOException
	 */
	public HttpResponse getHttpResponse(HttpUriRequest httpUriRequest) throws IOException {
		httpUriRequest.addHeader(COOKIE_HEADER, getCookies());
		return this.httpClient.execute(httpUriRequest);
	}

	/**
	 * This is a utility method which fetch data from login response
	 * 
	 * @param input
	 *            input string
	 * @param start
	 *            start string
	 * @param end
	 *            end string
	 * @return fetched string based on start and end
	 */
	private String extractString(String input, String start, String end) {
		String[] strArray = input.split(start);
		if (strArray.length > 1) {
			String[] value = strArray[1].split(end);
			if (value.length > 1) {
				return value[0];
			}
		}
		return null;
	}

	/**
	 * This method get Cookie object and convert it into string
	 * 
	 * @return string containing cookie.
	 */
	public String getCookies() {
		StringBuilder cookieHeaderBuilder = new StringBuilder();
		this.getCookieStore().getCookies().forEach(cookie -> cookieHeaderBuilder.append(cookie.getName()).append("=")
				.append(cookie.getValue()).append(";"));
		return cookieHeaderBuilder.toString();
	}

	/**
	 * Null credential inner class used to create login client.
	 */
	private static class NullCredentials implements Credentials {
		private NullCredentials() {
		}

		public Principal getUserPrincipal() {
			return null;
		}

		public String getPassword() {
			return null;
		}
	}
}
