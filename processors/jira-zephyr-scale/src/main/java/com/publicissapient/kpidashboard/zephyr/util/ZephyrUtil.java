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

package com.publicissapient.kpidashboard.zephyr.util;

import java.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.zephyr.config.ZephyrConfig;

/**
 * The type Zephyr util.
 */
@Component
public class ZephyrUtil {

	private static final String PROTOCOL = "https";
	private static final String HOST = "www.test.com";
	private static final String API_PATH = "rest/atm/1.0";
	private static final String AUTHORIZATION = "Authorization";
	private static final String AUTH_PREFIX = "Basic ";
	private static final String BEARER = "Bearer ";
	private static final String URL_SEPARATOR = "//";
	@Autowired
	private ZephyrConfig processorConfiguration;
	@Autowired
	private ZephyrConfig zephyrConfig;
	@Autowired
	private AesEncryptionService aesEncryptionService;

	/**
	 * Build the Auth header with the auth credentials as given in the app
	 * properties
	 *
	 * @param credentials
	 *            the credentials
	 * @return Auth Header {@link HttpEntity}
	 */
	public HttpEntity<String> buildAuthenticationHeader(String credentials) {
		HttpHeaders headers = new HttpHeaders();
		headers.set(AUTHORIZATION, AUTH_PREFIX + credentials);
		return new HttpEntity<>(headers);
	}

	/**
	 * Build the API url as per the configurations given in the app properties
	 *
	 * @param url
	 *            the url
	 * @param api
	 *            the api
	 * @return {@link UriComponentsBuilder}
	 */
	public UriComponentsBuilder buildAPIUrl(String url, String api) {
		UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
		return builder.scheme(getProtocol()).host(getHost(url)).path(getAPIPath(api));
	}

	private String getProtocol() {
		return StringUtils.isBlank(processorConfiguration.getProtocol()) ? PROTOCOL
				: processorConfiguration.getProtocol();
	}

	private String getHost(String url) {
		return StringUtils.isBlank(url) ? HOST : url;
	}

	private String getAPIPath(String api) {
		return StringUtils.isBlank(api) ? API_PATH : api;
	}

	public HttpEntity<String> buildAuthHeaderUsingToken(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.set(AUTHORIZATION, accessToken);
		return new HttpEntity<>(headers);
	}

	/**
	 * check that zephyr url contains slash at last character or not if contains
	 * then remove the last slash from the url
	 *
	 * @param toolUrl
	 *            tool url
	 * @return
	 */
	public String getZephyrUrl(String toolUrl) {
		String url = StringUtils.EMPTY;
		String[] hostArr = toolUrl.split(URL_SEPARATOR);
		if (hostArr.length > 1) {
			String hostUrl = hostArr[1];
			// check that Zephyr url contains slash at last character
			// or not
			// if contains then remove the last slash from the url
			if (!StringUtils.isEmpty(hostUrl) && hostUrl.endsWith("/")) {
				url = StringUtils.chop(hostUrl);
			} else {
				url = hostUrl;
			}
		}
		return url;
	}

	/**
	 * Gets credentials as base 64 string
	 *
	 * @param userName
	 * @param password
	 * @return base 64 string
	 */
	public String getCredentialsAsBase64String(String userName, String password) {
		String credentials = userName + ":" + decryptPassword(password);
		return Base64.getEncoder().encodeToString(credentials.getBytes());
	}

	public String decryptPassword(String encryptedPassword) {
		return aesEncryptionService.decrypt(encryptedPassword, zephyrConfig.getAesEncryptionKey());
	}

	public HttpEntity<String> buildBearerHeader(String patOAuthToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.set(AUTHORIZATION, BEARER + patOAuthToken);
		return new HttpEntity<>(headers);
	}
}
