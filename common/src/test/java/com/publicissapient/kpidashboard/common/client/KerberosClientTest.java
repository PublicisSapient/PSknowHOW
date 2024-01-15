/*
 *
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.publicissapient.kpidashboard.common.client;

import org.apache.http.HttpResponse;
import org.apache.http.auth.Credentials;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import static org.mockito.Mockito.*;

public class KerberosClientTest {
	@Mock
	Credentials credentials;
	@Mock
	HttpClient loginHttpClient;
	@Mock
	HttpClient httpClient;
	@Mock
	BasicCookieStore cookieStore;
	@Mock
	Logger log;
	@InjectMocks
	KerberosClient kerberosClient;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testLogin() throws Exception {
		String result = kerberosClient.login("samlTokenStartString", "samlTokenEndString", "samlUrlStartString",
				"samlUrlEndString");
		Assert.assertEquals("replaceMeWithExpectedResult", result);
	}

	@Test
	public void testGenerateSamlCookies() throws Exception {
		kerberosClient.generateSamlCookies("loginResponse", "samlTokenStartString", "samlTokenEndString",
				"samlUrlStartString", "samlUrlEndString");
	}

	@Test
	public void testGetResponse() throws Exception {
		String result = kerberosClient.getResponse(null);
		Assert.assertEquals("replaceMeWithExpectedResult", result);
	}

	@Test
	public void testGetHttpResponse() throws Exception {
		HttpResponse result = kerberosClient.getHttpResponse(null);
		Assert.assertEquals(null, result);
	}

	@Test
	public void testGetCookies() throws Exception {
		String result = kerberosClient.getCookies();
		Assert.assertEquals("replaceMeWithExpectedResult", result);
	}
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme