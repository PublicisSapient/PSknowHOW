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
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.File;
import java.net.UnknownHostException;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.publicissapient.kpidashboard.apis.auth.model.Authentication;
import com.publicissapient.kpidashboard.apis.auth.service.ForgotPasswordRequest;
import com.publicissapient.kpidashboard.apis.auth.service.ForgotPasswordService;
import com.publicissapient.kpidashboard.apis.auth.service.ResetPasswordRequest;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.ResetPasswordTokenStatusEnum;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * This controller class managed all forgot password and reset new password rest
 * requests
 * 
 * @author vijmishr1
 *
 */
@RestController
@Slf4j
public class ForgotPasswordController {

	/**
	 * Relative path of reset password of UI
	 */
	private static final String UI_RESET_PATH = "/authentication/resetPassword?resetToken="; // NOSONAR
	/**
	 * Relative path of accountType of UI
	 */
	private static final String UI_ACCOUNT_PATH = "/authentication/accountType?resetTokenStatus="; // NOSONAR
	@Autowired
	private ForgotPasswordService forgotPasswordService;
	@Autowired
	private CustomApiConfig customApiConfig;

	/**
	 * Creates token for an user account.
	 * 
	 * <p>
	 * processForgotPassword creates a token for an user account by validating email
	 * id from <tt>ForgotPasswordRequest</tt> object. Sends an email to the user
	 * account if the mail id is valid
	 * </p>
	 * 
	 * @param httpServletRequest
	 * @param request
	 * @return ServiceResponse with <tt>success</tt> message and
	 *         <tt>authentication</tt> object if email is sent successfully.
	 *         <tt>logError</tt> message and <tt>null</tt> incase of
	 *         <tt>UnknownHostException</tt> occurred.
	 */

	@RequestMapping(value = "/forgotPassword", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> processForgotPassword(@RequestBody ForgotPasswordRequest request,
			HttpServletRequest httpServletRequest) {
		boolean isSuccess = false;
		log.info("ForgotPasswordController: requested mail {}", request.getEmail());
		Authentication authentication = null;
		try {
			String serverPath = httpServletRequest.getScheme() + getApiHost() + httpServletRequest.getContextPath();
			authentication = forgotPasswordService.processForgotPassword(request.getEmail(), serverPath);
			if (null != authentication) {
				isSuccess = true;
				Authentication auth = new Authentication();
				auth.setEmail(authentication.getEmail());
				authentication = auth;
			}
			return ResponseEntity.ok().body(new ServiceResponse(isSuccess, "Success", authentication));
		} catch (UnknownHostException e) {
			log.error("UnknownHostException", e);
			log.error("ForgotPasswordController: Mail can not be sent to {}", request.getEmail());
			return ResponseEntity.badRequest().body(new ServiceResponse(isSuccess, "logError", null));
		}
	}

	/**
	 * Validates token generated for reset password.
	 *
	 * <p>
	 * validateToken method forwards the request to ForgotPasswordService to
	 * validate 128-bit UUID token
	 * </p>
	 *
	 * @param httpServletRequest
	 * @param token
	 * @return RedirectView with <tt>UI_RESET_PATH</tt> if the token valid and
	 *         <tt>UI_ACCOUNT_PATH</tt> incase of invalid token.
	 * @throws UnknownHostException
	 */
	@RequestMapping(value = "/validateToken", method = GET, produces = APPLICATION_JSON_VALUE) // NOSONAR
	public RedirectView validateToken(HttpServletRequest httpServletRequest, @RequestParam("token") UUID token)
			throws UnknownHostException {
		log.info("ForgotPasswordController: requested token for validate {}", token);
		ResetPasswordTokenStatusEnum tokenStatus = forgotPasswordService.validateEmailToken(token.toString());
		String serverPath = httpServletRequest.getScheme() + getUIHost();
		if (tokenStatus != null && tokenStatus.equals(ResetPasswordTokenStatusEnum.VALID)) {
			return new RedirectView(serverPath + UI_RESET_PATH + token);
		} else {
			return new RedirectView(serverPath + UI_ACCOUNT_PATH + tokenStatus);
		}

	}

	/**
	 * Resets the password after validating the token
	 * <p>
	 * resetPassword method accepts ResetPasswordRequest object as param and
	 * forwards the request to ForgotPasswordService to validate the request.
	 *
	 * </p>
	 * 
	 * @param updatedPasswordRequest
	 * @return ServiceResponse with <tt>sucess</tt> if the request is valid and
	 *         incase of a invalid request appends the logError message with
	 *         response code <tt>-14</tt>
	 */
	@RequestMapping(value = "/resetPassword", method = POST, produces = APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> resetPassword(@RequestBody ResetPasswordRequest updatedPasswordRequest) {
		boolean isSuccess = false;
		log.info("ForgotPasswordController: requested token for update {}", updatedPasswordRequest.getResetToken());
		Authentication authentication = null;
		try {
			authentication = forgotPasswordService.resetPassword(updatedPasswordRequest);
			if (null != authentication) {
				isSuccess = true;
				Authentication auth = new Authentication();
				auth.setEmail(authentication.getEmail());
				authentication = auth;
			}
		} catch (com.publicissapient.kpidashboard.common.exceptions.ApplicationException e) {
			log.error("Error in ForgotPasswordController: resetPassword()", e);
			return ResponseEntity.badRequest().body(new ServiceResponse(isSuccess, e.getMessage(), null));
		}
		return ResponseEntity.ok().body(new ServiceResponse(isSuccess, "Success", authentication));
	}

	/**
	 * 
	 * Gets api host
	 **/
	private String getApiHost() throws UnknownHostException {

		StringBuilder urlPath = new StringBuilder();
		if (StringUtils.isNotEmpty(customApiConfig.getUiHost())) {
			urlPath.append(':').append(File.separator + File.separator).append(customApiConfig.getUiHost().trim());
			// append port if local setup
			if (StringUtils.isNotEmpty(customApiConfig.getServerPort())) {
				urlPath.append(':').append(customApiConfig.getServerPort());
			}
		} else {
			throw new UnknownHostException("Api host not found in properties.");
		}

		return urlPath.toString();
	}

	/**
	 * Returns a String <tt>uiHost</tt> from customapi.properties with separator
	 * 
	 * @return
	 * @throws UnknownHostException
	 */
	private String getUIHost() throws UnknownHostException {
		StringBuilder urlPath = new StringBuilder();
		urlPath.append(':').append(File.separator + File.separator);

		if (StringUtils.isNotEmpty(customApiConfig.getUiHost())) {

			if (StringUtils.isNotEmpty(customApiConfig.getUiPort())) {
				urlPath.append(customApiConfig.getUiHost());
				urlPath.append(':').append(customApiConfig.getUiPort());
			} else {
				urlPath.append(customApiConfig.getUiHost());
			}

		} else {
			throw new UnknownHostException("Ui host not found in properties.");
		}
		urlPath.append(File.separator).append('#');
		return urlPath.toString();
	}

}
