package com.publicissapient.kpidashboard.apis.auth.token;

import java.util.Collection;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;

@Component
public class CookieUtil {
	private static final String AUTH_COOKIE = "authCookie";
	@Autowired
	private CustomApiConfig customApiConfig;

	public Cookie createAccessTokenCookie(String token) {
		Cookie cookie = new Cookie(AUTH_COOKIE, token);

		cookie.setMaxAge(customApiConfig.getAuthCookieDuration());
		cookie.setSecure(customApiConfig.isAuthCookieSecured());
		cookie.setHttpOnly(customApiConfig.isAuthCookieHttpOnly());
		cookie.setPath("/api");
		cookie.setDomain("localhost");
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
}
