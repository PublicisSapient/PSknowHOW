package com.publicissapient.kpidashboard.apis.auth.token;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import com.publicissapient.kpidashboard.apis.auth.AuthProperties;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class CookieUtil {
	public static final String AUTH_COOKIE = "authCookie";

	private static final String AUTHORIZATION = "Authorization";
	@Autowired
	private CustomApiConfig customApiConfig; // TODO needed to delete

	@Autowired
	private AuthProperties authProperties;

	public Cookie createAccessTokenCookie(String token) {
		Cookie cookie = new Cookie(AUTH_COOKIE, token);

		cookie.setMaxAge(customApiConfig.getAuthCookieDuration());
		cookie.setSecure(customApiConfig.isAuthCookieSecured());
		cookie.setHttpOnly(customApiConfig.isAuthCookieHttpOnly());
		cookie.setPath("/api");
		if (authProperties.isSubDomainCookie()) {
			cookie.setDomain(authProperties.getDomain());
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

	public Optional<Cookie> getCookie(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			return Arrays.stream(cookies).filter(cookie -> cookie.getName().equals(name)).findFirst();
		} else {
			return Optional.empty();
		}
	}

	public void deleteCookie(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
			@NotNull String name) {
		getCookie(request, name).ifPresent(foundCookie -> {
			foundCookie.setMaxAge(0);
			foundCookie.setValue("");
			foundCookie.setPath("/api");
			if (authProperties.isSubDomainCookie()) {
				foundCookie.setDomain(authProperties.getDomain());
			}
			response.addCookie(foundCookie);
		});
	}

	public static HttpHeaders getHeaders(String apiKey, boolean usingBasicAuth) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		if (apiKey != null && !apiKey.isEmpty()) {
			if (usingBasicAuth) {
				headers.set("x-api-key", apiKey);
			} else {
				headers.add("x-api-key", apiKey);
			}
		}
		return headers;
	}
}
