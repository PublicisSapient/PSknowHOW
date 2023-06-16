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
package com.publicissapient.kpidashboard.apis.audit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class ApiAuditLoggingTest {

	@InjectMocks
	ApiAuditLogging apiAuditLogging;
	private MockHttpServletResponse httpServletResponse;
	private MockHttpServletRequest httpServletRequest;

	@Before
	public void setup() {
		httpServletRequest = new MockHttpServletRequest("GET", "/api/jenkins/kpi");
		httpServletRequest.addHeader("Content-Type", "application/json");
		httpServletRequest.addHeader("Authorization",
				"eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJTVVBFUkFETUlOIiwiZGV0YWlscyI6IlNUQU5EQVJEIiwicm9sZXMiOlsiUk9MRV9TVVBFUkFETUlOIl0sImV4cCI6MTYxNDIzOTM4N30.i6u8vrg7eZZ3nCjMM7em2U6MNIs4IFMMG_VrvsfgLWXBdQivenVw5DAKGwcZz_auDsS4u9QbsYXgFj4AmvrDuA");

		httpServletResponse = new MockHttpServletResponse();
		httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
		httpServletResponse.setHeader("Cache-Control", "no-cache");
		httpServletResponse.setContentLength(1000);
	}

	@Test
	public void testGetMethod() throws Exception {

		apiAuditLogging.doDispatch(httpServletRequest, httpServletResponse);

	}

	@Test
	public void testPostMethod() throws Exception {
		httpServletRequest.setMethod("POST");
		apiAuditLogging.doDispatch(httpServletRequest, httpServletResponse);

	}
}