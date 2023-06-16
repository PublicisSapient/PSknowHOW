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

/**
 * 
 */
package com.publicissapient.kpidashboard.apis.auth.service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.auth.model.Authentication;
import com.publicissapient.kpidashboard.apis.auth.model.ForgotPasswordToken;
import com.publicissapient.kpidashboard.apis.auth.repository.AuthenticationRepository;
import com.publicissapient.kpidashboard.apis.auth.repository.ForgotPasswordTokenRepository;
import com.publicissapient.kpidashboard.apis.common.service.CommonService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.ResetPasswordTokenStatusEnum;
import com.publicissapient.kpidashboard.common.exceptions.ApplicationException;

/**
 * This class managed all the services for forgot password and reset new
 * password
 * 
 * @author vijmishr1
 *
 */
@Service
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ForgotPasswordServiceImpl.class);
	private static final String FORGOT_PASSWORD_TEMPLATE = "Forgot_Password_Template";

	@Autowired
	private AuthenticationRepository authenticationRepository;

	@Autowired
	private ForgotPasswordTokenRepository forgotPasswordTokenRepository;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private CommonService commonService;

	/*
	 * validatePath
	 */
	private static final String VALIDATE_PATH = "/validateToken?token="; // NOSONAR
	private static final String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[$@$!%*?&]).{8,20})";
	private static final String FORGOT_PASSWORD_NOTIFICATION_KEY = "Forgot_Password";

	/**
	 * Process forgotPassword request.
	 * 
	 * <p>
	 * processForgotPassword checks whether the email in the ForgotPasswordRequest
	 * object exists in the database.If the email exists,creates a token for the
	 * user account and sends an email with token and reset url info
	 * 
	 * @param email
	 * @param url
	 * @return authentication
	 */
	@Override
	public Authentication processForgotPassword(String email, String url) {
		LOGGER.info("ForgotPasswordServiceImpl: Requested mail {}", email);
		Authentication authentication = getEmailExistsInDB(email);
		if (authentication != null) {
			String token = createForgetPasswordToken(authentication);
			Map<String, String> customData = createCustomData(authentication.getUsername(), token, url,
					customApiConfig.getForgotPasswordExpiryInterval());
			LOGGER.info("Notification message sent to kafka with key : {}", FORGOT_PASSWORD_NOTIFICATION_KEY);
			commonService.sendEmailWithoutKafka(Arrays.asList(email), customData, customApiConfig.getEmailSubject(),
					FORGOT_PASSWORD_NOTIFICATION_KEY, customApiConfig.getKafkaMailTopic(),FORGOT_PASSWORD_TEMPLATE);
			return authentication;
		}
		return null;
	}

	/**
	 * Validates Email Token sent to the user via email.
	 * 
	 * <p>
	 * validateEmailToken method checks the token received from request, exists in
	 * the database.If the token is found in the database method will forward the
	 * token to validate it
	 * </p>
	 * 
	 * @param token
	 * @return one of the enum <tt>INVALID, VALID, EXPIRED</tt> of type
	 *         ResetPasswordTokenStatusEnum
	 */
	@Override
	public ResetPasswordTokenStatusEnum validateEmailToken(String token) {
		LOGGER.info("ForgotPasswordServiceImpl: Validate the token {}", token);
		ForgotPasswordToken forgotPasswordToken = forgotPasswordTokenRepository.findByToken(token);
		return checkTokenValidity(forgotPasswordToken);
	}

	/**
	 * Resets password after validating token
	 * 
	 * <p>
	 * resetPassword checks if the reset token exists in the database.Later checks
	 * the validity of the token. If the token is valid,searches for the username
	 * from the <tt>forgotPasswordToken</tt> in the <tt>authentication</tt>
	 * collection in the database. Saves the reset password if the username exists
	 * </p>
	 * 
	 * @param resetPasswordRequest
	 * @return authentication if the <tt>token</tt> is valid and <tt>username</tt>
	 *         from forgotPasswordToken exists in the database
	 * @throws ApplicationException
	 *             if either <tt>forgotPasswordToken</tt> is invalid or
	 *             <tt>username</tt> doen't exist in the database.
	 * 
	 */
	@Override
	public Authentication resetPassword(ResetPasswordRequest resetPasswordRequest) throws ApplicationException {
		LOGGER.info("ForgotPasswordServiceImpl: Reset token is {}", resetPasswordRequest.getResetToken());
		ForgotPasswordToken forgotPasswordToken = forgotPasswordTokenRepository
				.findByToken(resetPasswordRequest.getResetToken());
		ResetPasswordTokenStatusEnum tokenStatus = checkTokenValidity(forgotPasswordToken);
		if (tokenStatus.equals(ResetPasswordTokenStatusEnum.VALID)) {
			Authentication authentication = authenticationRepository.findByUsername(forgotPasswordToken.getUsername());
			if (null == authentication) {
				LOGGER.error("User {} Does not Exist", forgotPasswordToken.getUsername());
				throw new ApplicationException("User Does not Exist", ApplicationException.BAD_DATA);
			} else {
				validatePasswordRules(forgotPasswordToken.getUsername(), resetPasswordRequest.getPassword(),
						authentication);
				return authentication;
			}
		} else {
			LOGGER.error("Token is {}", resetPasswordRequest.getResetToken());
			throw new ApplicationException("Token is " + tokenStatus.name(), ApplicationException.BAD_DATA);
		}
	}

	private boolean isPassContainUser(String reqPassword, String username) {

		return !(StringUtils.containsIgnoreCase(reqPassword, username));
	}

	private boolean isOldPassword(String reqPassword, String savedPassword) {

		return !(StringUtils.containsIgnoreCase(Authentication.hash(reqPassword), savedPassword));

	}

	/**
	 * Checks if the email exists in the database.
	 * 
	 * @param email
	 * @return Authentication if email exits or <tt>null</tt> if the email doesn't
	 *         exist
	 */
	private Authentication getEmailExistsInDB(String email) {
		List<Authentication> authenticateList = authenticationRepository.findByEmail(email);
		if (CollectionUtils.isNotEmpty(authenticateList)) {
			return authenticateList.get(0);
		}
		return null;
	}

	/**
	 * Creates UUID token and sets it to ForgotPasswordToken along with username and
	 * expiry date and saves it to <tt>forgotPasswordToken</tt> collection in
	 * database.
	 * 
	 * @param authentication
	 * @return token
	 */
	private String createForgetPasswordToken(Authentication authentication) {
		String token = UUID.randomUUID().toString();
		ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken();
		forgotPasswordToken.setToken(token);
		forgotPasswordToken.setUsername(authentication.getUsername());
		forgotPasswordToken.setExpiryDate(Integer.parseInt(customApiConfig.getForgotPasswordExpiryInterval()));
		forgotPasswordTokenRepository.save(forgotPasswordToken);
		return token;
	}

	/**
	 * Checks the validity of <tt>forgotPasswordToken</tt>
	 * 
	 * @param forgotPasswordToken
	 * @return ResetPasswordTokenStatusEnum <tt>INVALID</tt> if token is
	 *         <tt>null</tt>, <tt>VALID</tt> if token is not expired,
	 *         <tt>EXPIRED</tt> if token is expired
	 */
	private ResetPasswordTokenStatusEnum checkTokenValidity(ForgotPasswordToken forgotPasswordToken) {
		if (forgotPasswordToken == null) {
			return ResetPasswordTokenStatusEnum.INVALID;
		} else if (isExpired(forgotPasswordToken.getExpiryDate())) {
			return ResetPasswordTokenStatusEnum.EXPIRED;
		} else {
			return ResetPasswordTokenStatusEnum.VALID;
		}
	}

	/**
	 * Validates if the given <tt>expiryDate</tt> is in the past
	 * <p>
	 * isExpired method checks the validity of token by comparing the validity of
	 * token expriy date with current Time and Date
	 * </p>
	 * 
	 * @param expiryDate
	 * @return boolean <tt>true</tt> if expiryDate is invalid/expired,<tt>false</tt>
	 *         if token is valid
	 */
	private boolean isExpired(Date expiryDate) {
		return new Date().after(expiryDate);
	}

	private void validatePasswordRules(String username, String password, Authentication authentication)
			throws ApplicationException {

		Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
		Matcher matcher = pattern.matcher(password);
		if (matcher.matches()) {
			if (isPassContainUser(password, username)) {
				if (isOldPassword(password, authentication.getPassword())) {
					authentication.setPassword(password);
					authenticationRepository.save(authentication);
				} else {
					throw new ApplicationException("Password should not be old password",
							ApplicationException.BAD_DATA);
				}
			} else {
				throw new ApplicationException("Password should not contain userName", ApplicationException.BAD_DATA);
			}
		} else {
			throw new ApplicationException(
					"At least 8 characters in length with Lowercase letters, Uppercase letters, Numbers and Special characters($,@,$,!,%,*,?,&)",
					ApplicationException.BAD_DATA);
		}

	}

	/**
	 * * create custom data for email
	 *
	 * @param username emailId
	 * @param token token
	 * @param url url
	 * @param expiryTime expiryTime in Min
	 * @return Map<String, String>
	 */
	private Map<String, String> createCustomData(String username, String token, String url, String expiryTime) {
		Map<String, String> customData = new HashMap<>();
		customData.put("token", token);
		customData.put("user", username);
		customData.put("resetUrl", url + VALIDATE_PATH + token);
		customData.put("expiryTime", expiryTime);
		return customData;
	}

}
