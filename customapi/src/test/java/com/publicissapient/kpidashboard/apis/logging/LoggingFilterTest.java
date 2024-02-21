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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.common.model.application.RequestLog;
import com.publicissapient.kpidashboard.common.repository.application.RequestLogRepository;

@RunWith(MockitoJUnitRunner.class)
public class LoggingFilterTest {

	@Mock
	private RequestLogRepository requestLogRepository;

	@InjectMocks
	@Autowired
	private LoggingFilter loggingFilter;

	@Mock
	private CustomApiConfig settings;

	@Mock
	private HttpServletRequest httpServletRequest;

	@Mock
	private HttpServletResponse httpServletResponse;

	@Mock
	private FilterChain filterChain;

	@Mock
	private ServletInputStream servletInputStream;

	@Test
	public void testDoFilterPut() throws Exception {
		when(httpServletRequest.getInputStream()).thenReturn(servletInputStream);
		when(httpServletRequest.getRequestURI()).thenReturn("Success");
		when(httpServletRequest.getMethod()).thenReturn(HttpMethod.PUT.toString());

		when(requestLogRepository.save(any(RequestLog.class))).thenReturn(new RequestLog());
		when(httpServletRequest.getContentType()).thenReturn("application/json;charset=UTF-8");
		when(httpServletResponse.getContentType()).thenReturn("application/json;charset=UTF-8");
		loggingFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
		verify(requestLogRepository, times(1)).save(any(RequestLog.class));
	}

	@Test
	public void testDoFilterGet() throws Exception {
		when(httpServletRequest.getMethod()).thenReturn(HttpMethod.GET.toString());
		when(settings.isCorsEnabled()).thenReturn(true);
		when(settings.getCorsWhitelist()).thenReturn("url1,url2,origin");

		loggingFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
		verify(settings, times(1)).getCorsWhitelist();
	}

	@Test
	public void testDoFilterPost() throws Exception {
		when(httpServletRequest.getInputStream()).thenReturn(servletInputStream);
		when(httpServletRequest.getRequestURI()).thenReturn("Success");
		when(httpServletRequest.getMethod()).thenReturn(HttpMethod.POST.toString());
		when(requestLogRepository.save(any(RequestLog.class))).thenReturn(new RequestLog());
		when(httpServletRequest.getContentType()).thenReturn("application/json;charset=UTF-8");
		when(httpServletResponse.getContentType()).thenReturn("application/json;charset=UTF-8");

		loggingFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
		verify(requestLogRepository, times(1)).save(any(RequestLog.class));
	}

	@Test
	public void testDoFilterDelete() throws Exception {
		// when(httpServletRequest.get)
		when(httpServletRequest.getInputStream()).thenReturn(servletInputStream);
		when(httpServletRequest.getRequestURI()).thenReturn("Success");
		when(httpServletRequest.getMethod()).thenReturn(HttpMethod.DELETE.toString());
		when(requestLogRepository.save(any(RequestLog.class))).thenReturn(new RequestLog());
		when(httpServletRequest.getContentType()).thenReturn("application/json;charset=UTF-8");
		when(httpServletResponse.getContentType()).thenReturn("application/json;charset=UTF-8");

		loggingFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
		verify(requestLogRepository, times(1)).save(any(RequestLog.class));
	}

}