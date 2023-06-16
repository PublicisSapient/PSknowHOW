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

package com.publicissapient.kpidashboard.apis.auth.rest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.auth.service.CaptchaValidationService;
import com.publicissapient.kpidashboard.apis.model.CaptchaValidationData;

/**
 * Rest controller to handle captcha validation requests.
 */
@RestController
public class CaptchaValidationController {

	private final CaptchaValidationService captchaValidationService;

	/**
	 * Instantiates a new Captcha validation controller.
	 *
	 * @param captchaValidationService
	 *            the captcha validation service
	 */
	@Autowired
	public CaptchaValidationController(CaptchaValidationService captchaValidationService) {
		this.captchaValidationService = captchaValidationService;
	}

	/**
	 * Validate captcha.
	 *
	 * @param captchaValidationData
	 *            the captcha validation data
	 * @return true if valid captcha
	 */
	@RequestMapping(value = "/login/captchavalidate", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE) // NOSONAR
	public boolean validateCaptcha(@RequestBody CaptchaValidationData captchaValidationData) {

		return captchaValidationService.validateCaptcha(captchaValidationData.getEncryptedString(),
				captchaValidationData.getResult());

	}
}
