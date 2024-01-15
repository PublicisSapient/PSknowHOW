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

package com.publicissapient.kpidashboard.common.model.application;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class RequestLogTest {
	@Mock
	Object requestBody;
	@Mock
	Object responseBody;
	// Field id of type ObjectId - was not mocked since Mockito doesn't mock a Final
	// class when 'mock-maker-inline' option is not set
	@InjectMocks
	RequestLog requestLog;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testToString() throws Exception {
		String result = requestLog.toString();
		Assert.assertNotNull(result);
	}

	@Test
	public void testEquals() throws Exception {
		boolean result = requestLog.equals("o");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testCanEqual() throws Exception {
		boolean result = requestLog.canEqual("other");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testSetClient() throws Exception {
		requestLog.setClient("client");
	}

	@Test
	public void testSetEndpoint() throws Exception {
		requestLog.setEndpoint("endpoint");
	}

	@Test
	public void testSetMethod() throws Exception {
		requestLog.setMethod("method");
	}

	@Test
	public void testSetParameter() throws Exception {
		requestLog.setParameter("parameter");
	}

	@Test
	public void testSetRequestSize() throws Exception {
		requestLog.setRequestSize(0L);
	}

	@Test
	public void testSetRequestContentType() throws Exception {
		requestLog.setRequestContentType("requestContentType");
	}

	@Test
	public void testSetRequestBody() throws Exception {
		requestLog.setRequestBody("requestBody");
	}

	@Test
	public void testSetResponseSize() throws Exception {
		requestLog.setResponseSize(0L);
	}

	@Test
	public void testSetResponseContentType() throws Exception {
		requestLog.setResponseContentType("responseContentType");
	}

	@Test
	public void testSetResponseBody() throws Exception {
		requestLog.setResponseBody("responseBody");
	}

	@Test
	public void testSetResponseCode() throws Exception {
		requestLog.setResponseCode(0);
	}

	@Test
	public void testSetTimestamp() throws Exception {
		requestLog.setTimestamp(0L);
	}

	@Test
	public void testBuilder() throws Exception {
		RequestLog.RequestLogBuilder result = RequestLog.builder();
		Assert.assertNotNull(result);
	}

	@Test
	public void testSetId() throws Exception {
		requestLog.setId(null);
	}
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme