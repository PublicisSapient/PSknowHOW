package com.publicissapient.kpidashboard.apis.auth.token;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;

@Component
public class CookieUtil {
	public static final String AUTH_COOKIE = "authCookie";
	@Autowired
	private CustomApiConfig customApiConfig;

	public Cookie createAccessTokenCookie(String token) {
		Cookie cookie = new Cookie(AUTH_COOKIE, token);

		cookie.setMaxAge(customApiConfig.getAuthCookieDuration());
		cookie.setSecure(customApiConfig.isAuthCookieSecured());
		cookie.setHttpOnly(customApiConfig.isAuthCookieHttpOnly());
		cookie.setPath("/api");
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

	public HttpHeaders setCookieIntoHeader(String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		headers.add(HttpHeaders.COOKIE, AUTH_COOKIE + "=" + token);
		return headers;

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
		getCookie(request, name).ifPresent((foundCookie) -> {
			foundCookie.setMaxAge(0);
			foundCookie.setValue("");
			foundCookie.setPath("/");
			foundCookie.setDomain("");
			response.addCookie(foundCookie);
		});
	}
}
