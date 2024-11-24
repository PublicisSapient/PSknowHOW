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

package com.publicissapient.kpidashboard.apis.filters;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.publicissapient.kpidashboard.apis.config.AuthConfig;
import com.publicissapient.kpidashboard.apis.config.AuthEndpointsProperties;
import com.publicissapient.kpidashboard.apis.util.CookieUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String NO_JWT_EXCEPTION = "No JWT session token found on request.";

	private static final String NO_RESOURCE_API_KEY_EXCEPTION = "No resource API key found on request.";

	private static final String JWT_FILTER_GENERIC_EXCEPTION = "JWT filtering failed for URI {} with message: {}.";

	private static final String X_API_KEY = "x-api-key";

	private static final String RESOURCE_KEY = "resource";

	private final AuthEndpointsProperties authEndpointsProperties;

	private final AuthConfig authConfig;

	private static boolean isRequestForURI(@NonNull HttpServletRequest request, @NotNull String uri) {
		return new AntPathRequestMatcher(uri).matches(request);
	}

	private static boolean isRequestForAnyURI(@NonNull HttpServletRequest request, @NotNull String[] uris) {
		return Arrays.stream(uris).anyMatch(uri -> isRequestForURI(request, uri));
	}

	private boolean isRequestForPublicURI(@NonNull HttpServletRequest request) {
		return isRequestForAnyURI(request, authEndpointsProperties.getPublicEndpoints());
	}

	private boolean isRequestForExternalURI(@NonNull HttpServletRequest request) {
		return isRequestForAnyURI(request, authEndpointsProperties.getExternalEndpoints());
	}

	@Override
	public void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
			@NotNull FilterChain filterChain) {
		try {
			if (isRequestForPublicURI(request)) {
				// * public endpoints should just pass without any authentication.
				filterChain.doFilter(request, response);
			}
			if (isRequestForExternalURI(request)) {
				String resourceAPIKey = request.getHeader(X_API_KEY);
				String resource = request.getHeader(RESOURCE_KEY);

				if (resourceAPIKey.isEmpty() || resource.isEmpty() || !resourceAPIKey.equals(authConfig.getServerApiKey())) {
					throw new BadCredentialsException(NO_RESOURCE_API_KEY_EXCEPTION);
				} else {
					filterChain.doFilter(request, response);
				}
			} else {
				Optional<Cookie> authCookie = CookieUtil.getCookie(request, CookieUtil.COOKIE_NAME);

				if (authCookie.isEmpty()) {
					throw new BadCredentialsException(NO_JWT_EXCEPTION);
				} else {
					filterChain.doFilter(request, response);
				}
			}
		} catch (Exception exception) {
			log.error(JWT_FILTER_GENERIC_EXCEPTION, request.getRequestURI(), exception.getMessage());
			response.setStatus(HttpStatus.FORBIDDEN.value());
		}
	}
}
