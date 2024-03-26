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
package com.publicissapient.kpidashboard.apis.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import com.publicissapient.kpidashboard.apis.config.AuthProperties;

/**
 * Provides Cookie utils.
 *
 * @author Hiren Babariya
 */
@Component
public class CookieUtil {
	public static final int AUTH_COOKIE_MAX_AGE = 60;
	public static final String AUTH_COOKIE = "authCookie";
	private static final String DEFAULT_COOKIE_PATH = "/";
	@Autowired
	private AuthProperties customApiConfig;

	public static void addCookie(@NotNull HttpServletResponse response, @NotNull String name, @NotNull String path,
			@NotNull String value, boolean httpOnly, int maxAge) {
		Cookie cookie = new Cookie(name, value);
		cookie.setPath(path);
		cookie.setHttpOnly(httpOnly);
		cookie.setMaxAge(maxAge);
		response.addCookie(cookie);
	}

	public static void addCookie(@NotNull HttpServletResponse response, @NotNull String name, @NotNull String value,
			boolean httpOnly, int maxAge) {
		addCookie(response, name, "/", value, httpOnly, maxAge);
	}

	public static void addCookie(@NotNull HttpServletResponse response, @NotNull String name, @NotNull String value,
			int maxAge) {
		addCookie(response, name, "/", value, Boolean.TRUE, maxAge);
	}

	public static <T extends Serializable> String serializeForCookie(@NotNull T toSerialize) {
		return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(toSerialize));
	}

	public static <T> T deserializeCookie(@NotNull Cookie cookie, @NotNull Class<T> deserializedClass) {
		return deserializedClass.cast(SerializationUtils.deserialize(Base64.getUrlDecoder().decode(cookie.getValue())));
	}

	public Optional<Cookie> getCookie(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			return Arrays.stream(cookies).filter(cookie -> cookie.getName().equals(name)).findFirst();
		} else {
			return Optional.empty();
		}
	}

	public Optional<String> getCookieValue(@NotNull HttpServletRequest request, @NotNull String name) {
		return getCookie(request, name).map(Cookie::getValue);
	}

	public void deleteCookie(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
			@NotNull String name) {
		getCookie(request, name).ifPresent(foundCookie -> {
			foundCookie.setMaxAge(-100);
			foundCookie.setValue("");
			foundCookie.setPath("/api");
			if (customApiConfig.isSubDomainCookie()) {
				foundCookie.setDomain(customApiConfig.getDomain());
			}
			response.addCookie(foundCookie);
		});
	}

	public Cookie createAccessTokenCookie(String token) {
		Cookie cookie = new Cookie(AUTH_COOKIE, token);

		cookie.setMaxAge(customApiConfig.getAuthCookieDuration());
		cookie.setSecure(customApiConfig.isAuthCookieSecured());
		cookie.setHttpOnly(true);
		cookie.setPath("/api");
		if (customApiConfig.isSubDomainCookie()) {
			cookie.setDomain(customApiConfig.getDomain());
		}
		return cookie;

	}

	public ResponseCookie deleteAccessTokenCookie() {
		return ResponseCookie.from(AUTH_COOKIE, "").build();

	}

	public Cookie getAuthCookie(HttpServletRequest request) {
		return WebUtils.getCookie(request, AUTH_COOKIE);
	}

	public void addSameSiteCookieAttribute(HttpServletResponse response) {
		Collection<String> headers = response.getHeaders(HttpHeaders.SET_COOKIE);
		boolean firstHeader = true;
		for (String header : headers) { // there can be multiple Set-Cookie attributes
			if (firstHeader) {
				response.setHeader(HttpHeaders.SET_COOKIE,
						String.format("%s; %s", header, customApiConfig.getAuthCookieSameSite()));
				firstHeader = false;
				continue;
			}
			response.addHeader(HttpHeaders.SET_COOKIE,
					String.format("%s; %s", header, customApiConfig.getAuthCookieSameSite()));
		}
	}

}
