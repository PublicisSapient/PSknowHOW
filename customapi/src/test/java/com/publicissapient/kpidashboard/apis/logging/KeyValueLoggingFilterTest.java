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

package com.publicissapient.kpidashboard.apis.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collection;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Lists;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;

@RunWith(MockitoJUnitRunner.class)
public class KeyValueLoggingFilterTest {

	KeyValueLoggingFilter filter;
	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletResponse response;
	@Mock
	private FilterChain chain;
	@Mock
	private HttpSession session;
	@Mock
	private Appender appender;
	@Captor
	private ArgumentCaptor<LoggingEvent> logCaptor;
	private String remoteAddress = "http://127.0.0.1";
	private String appName = "appName";
	private String appVersion = "2.0.5-SNAPSHOT";
	private String requestUrl = "http://127.0.0.1/api";
	private String requestMethod = "POST";
	private int statusCode = 200;

	@Before
	public void setup() {
		Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		logger.addAppender(appender);

		filter = new KeyValueLoggingFilter();

		ReflectionTestUtils.setField(filter, "appName", appName);
		ReflectionTestUtils.setField(filter, "version", appVersion);
		StringBuffer buffer = new StringBuffer();
		buffer.append(requestUrl);

	}

	@After
	public void teardown() {
		Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		logger.detachAppender(appender);
	}

	@Test
	public void testMethod() {

	}

	// @Test
	public void shouldLogSplunkEntryNullSessionAndUser() throws IOException, ServletException {
		filter.doFilter(request, response, chain);

		verify(appender).doAppend(logCaptor.capture());
		LoggingEvent loggingEvent = logCaptor.getValue();

		assertEquals(Level.INFO, loggingEvent.getLevel());
		assertTrue(verifyLogContains(loggingEvent, KeyValueLoggingFilter.REMOTE_ADDRESS, remoteAddress));
		assertTrue(verifyLogContains(loggingEvent, KeyValueLoggingFilter.APPLICATION_NAME, appName));
		assertTrue(verifyLogContains(loggingEvent, KeyValueLoggingFilter.APPLICATION_VERSION, appVersion));
		assertTrue(verifyLogContains(loggingEvent, KeyValueLoggingFilter.REQUEST_URL, requestUrl));
		assertTrue(verifyLogContains(loggingEvent, KeyValueLoggingFilter.REQUEST_METHOD, requestMethod));
		assertTrue(verifyLogContains(loggingEvent, KeyValueLoggingFilter.STATUS_CODE, statusCode));
	}

	// @Test
	public void shouldLogSplunkEntryWithUserAndSession() throws IOException, ServletException {
		String sessionId = "sessionId";
		String principal = "username";
		String userDetails = "details";

		when(request.getSession(false)).thenReturn(session);
		when(session.getId()).thenReturn(sessionId);

		Collection<? extends GrantedAuthority> authorities = Lists
				.newArrayList(new SimpleGrantedAuthority("ROLE_ADMIN"));
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal,
				"password", authorities);
		authentication.setDetails(userDetails);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		filter.doFilter(request, response, chain);

		verify(appender).doAppend(logCaptor.capture());
		LoggingEvent loggingEvent = logCaptor.getValue();

		assertTrue(verifyLogContains(loggingEvent, KeyValueLoggingFilter.SESSION_ID, sessionId));
		assertTrue(verifyLogContains(loggingEvent, KeyValueLoggingFilter.USER_NAME, principal));
		assertTrue(verifyLogContains(loggingEvent, KeyValueLoggingFilter.USER_DETAILS, userDetails));
		assertTrue(verifyLogContains(loggingEvent, KeyValueLoggingFilter.USER_AUTHORITIES, "[ROLE_ADMIN]"));
	}

	private boolean verifyLogContains(LoggingEvent loggingEvent, String field, Object value) {
		StringBuilder builder = new StringBuilder();
		builder.append(field).append('=').append('"').append(value).append('"');
		return loggingEvent.getFormattedMessage().contains(builder.toString());

	}

}
