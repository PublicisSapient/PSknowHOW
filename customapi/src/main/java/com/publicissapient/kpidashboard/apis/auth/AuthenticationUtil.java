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

package com.publicissapient.kpidashboard.apis.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.publicissapient.kpidashboard.common.constant.AuthType;

/**
 * This class provides utility methods for authentication.
 * 
 * @author anisingh4
 */
public final class AuthenticationUtil {

	private AuthenticationUtil() {
	}

	/**
	 * Gets username from context.
	 *
	 * @return the username from context
	 */
	public static String getUsernameFromContext() {
		Authentication authentication = getAuthentication();
		if (authentication != null) {
			return authentication.getName();
		}

		return null;
	}

	/**
	 * Gets auth type from context.
	 *
	 * @return the auth type from context
	 */
	public static AuthType getAuthTypeFromContext() {
		Authentication authentication = getAuthentication();
		if (authentication != null && authentication.getDetails() instanceof String) {
			return AuthType.valueOf((String) authentication.getDetails());
		} else if (authentication != null && authentication.getDetails() instanceof AuthType) {
			return (AuthType) authentication.getDetails();
		}

		return null;
	}

	private static Authentication getAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}

}
