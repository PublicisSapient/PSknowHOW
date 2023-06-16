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

package com.publicissapient.kpidashboard.apis.auth.exceptions;

import com.publicissapient.kpidashboard.common.constant.AuthType;

/**
 * Can be thrown if user does not exists.
 */
public class UserNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -8596676033217258687L;

	private static final String MESSAGE = "No user found with name: %1$2s, and authorization type %2$2s.";

	/**
	 * Instantiates a new User not found exception.
	 *
	 * @param username
	 *            the username
	 * @param authType
	 *            the auth type
	 */
	public UserNotFoundException(String username, AuthType authType) {
		super(String.format(MESSAGE, username, authType.name()));
	}

}
