/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2020 Sapient Limited.
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

package com.publicissapient.kpidashboard.jiratest.adapter.helper;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.publicissapient.kpidashboard.common.util.RestOperationsFactory;
import com.publicissapient.kpidashboard.jiratest.adapter.impl.async.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jiratest.adapter.impl.async.factory.ProcessorAsynchJiraRestClientFactory;
import com.publicissapient.kpidashboard.jiratest.config.JiraTestProcessorConfig;
import com.publicissapient.kpidashboard.jiratest.model.JiraInfo;
import com.publicissapient.kpidashboard.jiratest.oauth.JiraOAuthClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JiraRestClientFactory implements RestOperationsFactory<JiraRestClient> {

	private static final String STR_USERNAME = "username";
	private static final String STR_PASSWORD = "password"; // NOSONAR

	@Autowired
	private JiraTestProcessorConfig jiraTestProcessorConfig;

	@Autowired
	private JiraOAuthClient jiraOAuthClient;

	/**
	 * Decodes JIRA credentials provided by the user
	 * 
	 * @param jiraCredentials
	 *            jiraCredentials
	 * @return Map of decoded username and password
	 */
	public Map<String, String> decodeUserCredentials(String jiraCredentials) {
		Map<String, String> credentialMap = new LinkedHashMap<>();
		if (jiraCredentials != null) {
			StringTokenizer tokenizer = new StringTokenizer(new String(Base64.decodeBase64(jiraCredentials)), ":\n");
			for (int i = 0; tokenizer.hasMoreTokens(); i++) {
				if (i == 0) {
					credentialMap.put(STR_USERNAME, tokenizer.nextToken());
				} else {
					credentialMap.put(STR_PASSWORD, tokenizer.nextToken());
				}
			}
		}

		return credentialMap;

	}

	/**
	 * Created JIRA connection using provided details
	 * 
	 * @param jiraBaseUri
	 *            JIRA server base URL
	 * @param fullProxyUrl
	 *            Jira proxy URL
	 * @param username
	 *            Jira login username
	 * @param password
	 *            Jira Login password
	 * @return created connection URI
	 */
	private URI createJiraConnection(String jiraBaseUri, String fullProxyUrl, String username, String password) {
		final String uname = username;
		final String pword = password;
		Proxy proxy = null;
		URLConnection connection = null;
		try {
			if (StringUtils.isNotEmpty(jiraBaseUri)) {
				URL baseUrl = new URL(jiraBaseUri);
				if (StringUtils.isNotEmpty(fullProxyUrl)) {
					URL proxyUrl = new URL(fullProxyUrl);
					URI proxyUri = new URI(proxyUrl.getProtocol(), proxyUrl.getUserInfo(), proxyUrl.getHost(),
							proxyUrl.getPort(), proxyUrl.getPath(), proxyUrl.getQuery(), null);
					proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyUri.getHost(), proxyUri.getPort()));
					connection = baseUrl.openConnection(proxy);

					if (!StringUtils.isEmpty(username) && (!StringUtils.isEmpty(password))) {
						String creds = uname + ":" + pword;
						Authenticator.setDefault(new Authenticator() {
							@Override
							protected PasswordAuthentication getPasswordAuthentication() {
								return new PasswordAuthentication(uname, pword.toCharArray());
							}
						});
						connection.setRequestProperty("Proxy-Authorization",
								"Basic " + Base64.encodeBase64String((creds).getBytes()));
					}
				} else {
					connection = baseUrl.openConnection();
				}
			} else {
				log.error(
						"The response from Jira was blank or non existant - please check your property configurations");
				return null;
			}

			return connection.getURL().toURI();

		} catch (URISyntaxException | IOException e) {
			try {
				log.error(
						"There was a problem parsing or reading the proxy configuration settings during openning a Jira connection. Defaulting to a non-proxy URI.");
				return new URI(jiraBaseUri);
			} catch (URISyntaxException e1) {
				log.error("Correction:  The Jira connection base URI cannot be read!");
				return null;
			}
		}
	}

	@Override
	public ProcessorJiraRestClient getTypeInstance() {
		return null;
	}

	/**
	 * Cleans the cache in th Custom API
	 * 
	 * @param cacheEndPoint
	 *            URL end point where Custom API cache is created
	 * @param cacheName
	 *            Name of the Custom API cache
	 */
	public boolean cacheRestClient(String cacheEndPoint, String cacheName) {
		boolean cleaned = false;
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

		UriComponentsBuilder uriBuilder = UriComponentsBuilder
				.fromHttpUrl(jiraTestProcessorConfig.getCustomApiBaseUrl());
		uriBuilder.path("/");
		uriBuilder.path(cacheEndPoint);
		uriBuilder.path("/");
		uriBuilder.path(cacheName);

		HttpEntity<?> entity = new HttpEntity<>(headers);

		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = null;
		try {
			response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, entity, String.class);
		} catch (RuntimeException e) {
			log.error("[JIRA-CUSTOMAPI-CACHE-EVICT]. Error while consuming rest service", e);
		}

		if (null != response && response.getStatusCode().is2xxSuccessful()) {
			cleaned = true;
			log.info("[JIRA-CUSTOMAPI-CACHE-EVICT]. Successfully evicted cache {}", cacheName);
		} else {
			log.error("[JIRA-CUSTOMAPI-CACHE-EVICT]. Error while evicting cache {}", cacheName);
		}
		return cleaned;
	}

	/**
	 * Gets Jira Client
	 * 
	 * @param jiraInfo
	 *            Jira Server information
	 * @return jira rest client
	 */
	public ProcessorJiraRestClient getJiraClient(JiraInfo jiraInfo) {

		String username = jiraInfo.getUsername();
		String password = jiraInfo.getPassword();
		String jiraConfigBaseUrl = jiraInfo.getJiraConfigBaseUrl();
		String jiraConfigProxyUrl = jiraInfo.getJiraConfigProxyUrl();
		String jiraConfigProxyPort = jiraInfo.getJiraConfigProxyPort();
		ProcessorJiraRestClient client = null;
		String proxyUri = null;
		String proxyPort = null;

		URI jiraUri = null;

		try {
			if (jiraConfigProxyUrl == null || jiraConfigProxyUrl.isEmpty() || (jiraConfigProxyPort == null)) {
				jiraUri = new URI(jiraConfigBaseUrl);
			} else {
				proxyUri = jiraConfigProxyUrl;
				proxyPort = jiraConfigProxyPort;

				jiraUri = this.createJiraConnection(jiraConfigBaseUrl, proxyUri + ":" + proxyPort, username, password);
			}

			InetAddress.getByName(jiraUri.getHost());// NOSONAR
			client = new ProcessorAsynchJiraRestClientFactory().createWithBasicHttpAuthentication(jiraUri, username,
					password, jiraTestProcessorConfig);

		} catch (UnknownHostException | URISyntaxException e) {
			log.error("The Jira host name is invalid. Further jira collection cannot proceed.");

			log.debug("Exception", e);
		}

		return client;
	}

	/**
	 * Provides Jira client using OAuth
	 * 
	 * @param jiraInfo
	 *            Jira Server information
	 * @return Jira rest client
	 */
	public ProcessorJiraRestClient getJiraOAuthClient(JiraInfo jiraInfo) {
		String username = jiraInfo.getUsername();
		String password = jiraInfo.getPassword();
		String jiraConfigBaseUrl = jiraInfo.getJiraConfigBaseUrl();
		String jiraConfigProxyUrl = jiraInfo.getJiraConfigProxyUrl();
		String jiraConfigProxyPort = jiraInfo.getJiraConfigProxyPort();
		ProcessorJiraRestClient client = null;
		String proxyUri = null;
		String proxyPort = null;

		URI jiraUri = null;

		try {
			if (jiraConfigProxyUrl == null || jiraConfigProxyUrl.isEmpty() || (jiraConfigProxyPort == null)) {
				jiraUri = new URI(jiraConfigBaseUrl);
			} else {
				proxyUri = jiraConfigProxyUrl;
				proxyPort = jiraConfigProxyPort;

				jiraUri = this.createJiraConnection(jiraConfigBaseUrl, proxyUri + ":" + proxyPort, username, password);
			}

			InetAddress.getByName(jiraUri.getHost());// NOSONAR
			client = new ProcessorAsynchJiraRestClientFactory().create(jiraUri, jiraOAuthClient,
					jiraTestProcessorConfig);

		} catch (UnknownHostException | URISyntaxException e) {
			log.error("The Jira host name is invalid. Further jira collection cannot proceed.");

			log.debug("Exception", e);
		}

		return client;
	}

}
