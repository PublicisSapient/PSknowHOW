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

package com.publicissapient.kpidashboard.apis.model;

/**
 * Represents Captcha validation data.
 */
public class CaptchaValidationData {

	private String encryptedString;
	private String result;

	/**
	 * Gets encrypted string.
	 *
	 * @return the encrypted string
	 */
	public String getEncryptedString() {
		return encryptedString;
	}

	/**
	 * Sets encrypted string.
	 *
	 * @param encryptedString
	 *            the encrypted string
	 */
	public void setEncryptedString(String encryptedString) {
		this.encryptedString = encryptedString;
	}

	/**
	 * Gets result.
	 *
	 * @return the result
	 */
	public String getResult() {
		return result;
	}

	/**
	 * Sets result.
	 *
	 * @param result
	 *            the result
	 */
	public void setResult(String result) {
		this.result = result;
	}
}
