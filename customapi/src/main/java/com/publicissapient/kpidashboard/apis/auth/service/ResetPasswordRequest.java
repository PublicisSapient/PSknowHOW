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

import org.hibernate.validator.constraints.NotEmpty;

public class ResetPasswordRequest {

	@NotEmpty
	private String password;

	@NotEmpty
	private String resetToken;

	/**
	 * @return password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets password
	 * 
	 * @param password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return resetToken
	 */
	public String getResetToken() {
		return resetToken;
	}

	/**
	 * Sets resetToken
	 * 
	 * @param resetToken
	 */
	public void setResetToken(String resetToken) {
		this.resetToken = resetToken;
	}

}
