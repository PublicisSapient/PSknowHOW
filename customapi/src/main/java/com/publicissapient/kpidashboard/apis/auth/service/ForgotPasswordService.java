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

package com.publicissapient.kpidashboard.apis.auth.service;

import com.publicissapient.kpidashboard.apis.auth.model.Authentication;
import com.publicissapient.kpidashboard.apis.enums.ResetPasswordTokenStatusEnum;
import com.publicissapient.kpidashboard.common.exceptions.ApplicationException;

public interface ForgotPasswordService {

	/**
	 * Process forgotPassword request.
	 * 
	 * 
	 * @param email
	 * @param url
	 * @return Authentication
	 */
	Authentication processForgotPassword(String email, String url);

	/**
	 * Validate Email Token sent to the user via email.
	 * 
	 * @param token
	 * @return ResetPasswordTokenStatusEnum
	 */
	ResetPasswordTokenStatusEnum validateEmailToken(String token);

	/**
	 * Reset password after validating token
	 * 
	 * @param updatedPasswordRequest
	 * @return Authentication
	 * @throws ApplicationException
	 */
	Authentication resetPassword(ResetPasswordRequest updatedPasswordRequest) throws ApplicationException;
}
