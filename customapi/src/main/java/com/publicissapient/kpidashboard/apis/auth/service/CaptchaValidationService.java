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

/**
 * This service validates captcha input text with the actual result
 * 
 * @author sgoe17
 *
 */
public interface CaptchaValidationService {

	/**
	 * Compares Captcha input string with the actual string in image and returns a
	 * boolean match
	 * 
	 * @param encryptedString
	 *            the encrypted string
	 * @param result
	 *            the result
	 * @return true if captcha input string is matched with captcha.
	 */
	boolean validateCaptcha(String encryptedString, String result);

}
