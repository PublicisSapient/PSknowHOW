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

package com.publicissapient.kpidashboard.apis.service;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.config.AuthProperties;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.CookieUtil;

import lombok.AllArgsConstructor;

/**
 * Provides Cookie Service.
 *
 * @author Hiren Babariya
 */
@Service
@AllArgsConstructor
public class CookieService {

	public static final int AUTH_COOKIE_MAX_AGE = 60;

	private static final String REDIRECT_URI_PARAMETER = "redirect_uri";

	@Autowired
	private AuthProperties properties;

	@Autowired
	private CookieUtil cookieUtil;

	public static void addRedirectAfterLoginCookie(@NotNull HttpServletRequest request,
			@NotNull HttpServletResponse response) {
		Optional.ofNullable(request.getParameter(REDIRECT_URI_PARAMETER)).filter(StringUtils::isNotEmpty).ifPresent(
				redirectTo -> CookieUtil.addCookie(response, REDIRECT_URI_PARAMETER, redirectTo, AUTH_COOKIE_MAX_AGE));
	}

	public void removeRedirectCookie(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) {
		cookieUtil.deleteCookie(request, response, REDIRECT_URI_PARAMETER);
	}

	public String getAndRemoveRedirectToOnSuccess(@NotNull HttpServletRequest request,
			@NotNull HttpServletResponse response) throws UnsupportedEncodingException {
		String redirectOnSuccess = getRedirectToOnSuccess(request);
		removeRedirectCookie(request, response);
		return redirectOnSuccess;
	}

	private String getRedirectToOnSuccess(@NotNull HttpServletRequest request) throws UnsupportedEncodingException {
		return CommonUtils.decode(cookieUtil.getCookieValue(request, REDIRECT_URI_PARAMETER)
				.orElseGet(properties::getDefaultRedirectToAfterLogin));
	}

}
