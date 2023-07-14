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
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.auth.service.CaptchaService;
import com.publicissapient.kpidashboard.apis.model.CustomCaptcha;

import lombok.extern.slf4j.Slf4j;

/**
 * This controller generates a Captcha in a JSOn response
 * 
 * @author sgoe17
 *
 */
@Slf4j
@RestController
public class CustomCaptchaController {

	private final CaptchaService captchaService;

	/**
	 * Constructor to autowire all dependencies
	 * 
	 * @param captchaService
	 *            CaptchaService
	 */
	@Autowired
	public CustomCaptchaController(CaptchaService captchaService) {

		this.captchaService = captchaService;
	}

	/**
	 * Returns a captcha response witha raondom string text
	 * 
	 * @return CustomCaptcha
	 */
	@RequestMapping(value = "/login/captcha", method = GET, produces = APPLICATION_JSON_VALUE) // NOSONAR
	public CustomCaptcha getCaptcha() {

		log.info("CustomCaptchaController::getCaptcha start");
		return captchaService.getCaptcha();
	}

}
