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

import com.publicissapient.kpidashboard.apis.config.AuthProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * Provides Cookie utils.
 *
 * @author Hiren Babariya
 */
@NoArgsConstructor
public class CookieUtil {
    public static final String DEFAULT_COOKIE_PATH = "/";

    public static final String API_COOKIE_PATH = "/api";

    public static final String COOKIE_NAME = "authCookie";

    public static final String EXPIRY_COOKIE_NAME = COOKIE_NAME + "_EXPIRY";

    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies).filter(cookie -> cookie.getName().equals(name)).findFirst();
        } else {
            return Optional.empty();
        }
    }

    public static Optional<String> getCookieValue(@NotNull HttpServletRequest request, @NotNull String name) {
        return getCookie(request, name).map(Cookie::getValue);
    }

    public static void addCookie(
            @NotNull HttpServletResponse response,
            @NotNull String name,
            @NotNull String path,
            @NotNull String value,
            boolean httpOnly,
            int maxAge,
            String domain
    ) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(path);
        cookie.setHttpOnly(httpOnly);
        cookie.setMaxAge(maxAge);
        cookie.setDomain(domain);
        response.addCookie(cookie);
    }

    public static void addCookie(
            @NotNull HttpServletResponse response,
            @NotNull String name,
            @NotNull String value,
            boolean httpOnly,
            int maxAge,
            String domain
    ) {
        addCookie(response, name, DEFAULT_COOKIE_PATH, value, httpOnly, maxAge, domain);
    }

    public static void addCookie(
            @NotNull HttpServletResponse response,
            @NotNull String name,
            @NotNull String value,
            int maxAge,
            String domain
    ) {
        addCookie(response, name, API_COOKIE_PATH, value, Boolean.TRUE, maxAge, domain);
    }

    public static void deleteCookie(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull String name) {
        getCookie(request, name).ifPresent(foundCookie -> {
            foundCookie.setMaxAge(0);
            foundCookie.setValue(StringUtils.EMPTY);
            response.addCookie(foundCookie);
        });
    }

    // TODO: delete if not needed.
    public static void addSameSiteCookieAttribute(HttpServletResponse response, AuthProperties authProperties) {
        Collection<String> headers = response.getHeaders(HttpHeaders.SET_COOKIE);
        boolean firstHeader = true;
        for (String header : headers) { // there can be multiple Set-Cookie attributes
            if (firstHeader) {
                response.setHeader(HttpHeaders.SET_COOKIE, String.format("%s; %s", header, authProperties.getAuthCookieSameSite()));
                firstHeader = false;
                continue;
            }
            response.addHeader(HttpHeaders.SET_COOKIE, String.format("%s; %s", header, authProperties.getAuthCookieSameSite()));
        }
    }

}
