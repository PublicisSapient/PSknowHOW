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

package com.publicissapient.kpidashboard.apis.service.impl;

import com.publicissapient.kpidashboard.apis.config.AuthConfig;
import com.publicissapient.kpidashboard.apis.config.UserInterfacePathsConfig;
import com.publicissapient.kpidashboard.apis.constant.CommonConstant;
import com.publicissapient.kpidashboard.apis.entity.User;
import com.publicissapient.kpidashboard.apis.entity.UserVerificationToken;
import com.publicissapient.kpidashboard.apis.enums.AuthType;
import com.publicissapient.kpidashboard.apis.enums.NotificationCustomDataEnum;
import com.publicissapient.kpidashboard.apis.enums.ResetPasswordTokenStatusEnum;
import com.publicissapient.kpidashboard.apis.errors.GenericException;
import com.publicissapient.kpidashboard.apis.repository.UserRepository;
import com.publicissapient.kpidashboard.apis.repository.UserVerificationTokenRepository;
import com.publicissapient.kpidashboard.apis.service.NotificationService;
import com.publicissapient.kpidashboard.apis.service.MessageService;
import com.publicissapient.kpidashboard.apis.service.UserService;
import com.publicissapient.kpidashboard.common.model.UserDTO;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides method to perform CRUD and validation operations on user
 * authentication data.
 *
 * @author hargupta15
 */
@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
	private static final String STANDARD = "STANDARD";
	private static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

	private final UserRepository userRepository;

	private final AuthConfig authProperties;

	private final UserInterfacePathsConfig userInterfacePathsConfig;

	private final NotificationService commonService;

	private final UserVerificationTokenRepository userVerificationTokenRepository;

	private final MessageService messageService;

	@Override
	public User save(@Valid User user) {
		return this.userRepository.save(user);
	}



	@Override
	public Optional<User> findByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	/**
	 * Creating and populating new User data
	 *
	 * @param user user
	 * @return Authentication
	 */
	private Authentication generateUserAuthToken(User user) {
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user, user.getPassword(),
																							new ArrayList<>()
		);
		token.setDetails(AuthType.STANDARD);
		return token;
	}

	private User saveUserDetails(UserDTO authenticationRequest) {
		String username = authenticationRequest.getUsername();
		String password = authenticationRequest.getPassword();
		String email = authenticationRequest.getEmail().toLowerCase();
		String firstName = authenticationRequest.getFirstName();
		String lastName = authenticationRequest.getLastName();
		String displayName = authenticationRequest.getDisplayName();
		LocalDateTime createdDate = LocalDateTime.now();
		LocalDateTime modifiedDate = LocalDateTime.now();
		return userRepository.save(
				new User(username, password, firstName, lastName, displayName, email, createdDate, STANDARD,
						 modifiedDate, false
				));
	}



	@Override
	public String getUsername(Authentication authentication) {

		if (authentication == null) {
			return null;
		}

		String username;
		if (authentication.getPrincipal() instanceof UserDTO) {
			username = ((UserDTO) authentication.getPrincipal()).getUsername();
		} else if (authentication.getPrincipal() instanceof String) {
			username = authentication.getPrincipal().toString();
		} else {
			username = null;
		}
		return username;
	}

	@Override
	public boolean isEmailExist(String email) {
		return userRepository.findByEmail(email) != null;
	}

	@Override
	public Optional<User> findByUserName(String userName) {
		return userRepository.findByUsername(userName);
	}

	@Override
	@Transactional
	public boolean deleteByUserName(String username) {
		try {
			userRepository.deleteByUsername(username);
			log.info("User: {} has been deleted.", username);
			return true;
		} catch (Exception e) {
			log.error("Error while deleting user: {}", username, e);
			return false;
		}
	}

	@Override
	public List<User> findAllUnapprovedUsers() {
		return userRepository.findByUserVerifiedAndApprovedOrderByIdDesc(true, false);
	}

	public User approveUser(User user) {
		return userRepository.save(user);
	}

	public UserDTO getUserDTO(User user) {
		UserDTO dto = null;
		if (null != user) {
			dto = UserDTO.builder().id(user.getId()).username(user.getUsername()).email(user.getEmail())
						 //                    .approved(user.isApproved())
						 .firstName(user.getFirstName()).lastName(user.getLastName()).displayName(user.getDisplayName())
						 .authType(user.getAuthType())
						 //                         .userVerified(user.isUserVerified())
						 .build();
		}
		return dto;
	}

	/**
	 * Method To create and save details of new Users
	 *
	 * @param request request
	 * @return boolean
	 */
	@Override
	public boolean registerUser(UserDTO request) {
		if (validateUserDetails(request)) {
			User user = saveUserDetails(request);
			generateUserAuthToken(user);
			sendVerificationMailToRegisterUser(user.getUsername(), user.getEmail());
			return true;
		}
		throw new GenericException(messageService.getMessage("error_register_password"));
	}

	/**
	 * delete user whose verification not completed before verification token
	 * expiration
	 *
	 * @param token
	 */
	@Override
	public void deleteUnVerifiedUser(UUID token) {
		UserVerificationToken userVerificationToken = userVerificationTokenRepository.findByToken(token.toString());
		log.info("UserController: User {}", token);

		if (userVerificationToken.getUsername() != null && userVerificationToken.getEmail() != null) {
			sendVerificationFailedMailUser(userVerificationToken.getUsername(), userVerificationToken.getEmail());
		}
	}

	/**
	 * method for sending verification mail to user
	 *
	 * @param username
	 * @param email
	 */
	private void sendVerificationMailToRegisterUser(String username, String email) {
		String serverPath = getServerPath();
		log.info("UserServiceImpl: serverPath {}", serverPath);
		log.info("UserServiceImpl: registered mail {}", email);
		String token = createUserVerificationToken(username, email);
		Map<String, String> customData = createCustomData(username, email, serverPath,
														  authProperties.getVerifyUserTokenExpiryInterval(), token
		);
		commonService.sendEmailNotification(Arrays.asList(email), customData,
											CommonConstant.USER_VERIFICATION_NOTIFICATION_KEY,
											CommonConstant.USER_VERIFICATION_TEMPLATE_KEY
		);
	}

	/**
	 * Verification Failed Mail
	 *
	 * @param username
	 * @param email
	 */
	private void sendVerificationFailedMailUser(String username, String email) {
		String serverPath = getServerPath();
		log.info("UserServiceImpl: registered mail {}", email);
		User user = userRepository.findByEmail(email);
		if (user != null) {
			Map<String, String> customData = createCustomData(username, email, serverPath, "", " ");
			commonService.sendEmailNotification(Arrays.asList(email), customData,
												CommonConstant.USER_VERIFICATION_FAILED_NOTIFICATION_KEY,
												CommonConstant.USER_VERIFICATION_FAILED_TEMPLATE_KEY
			);
			userRepository.deleteByUsername(user.getUsername());
		}
	}

	/**
	 * method to create verification token
	 *
	 * @param username
	 * @param email
	 * @return
	 */
	private String createUserVerificationToken(String username, String email) {
		String token = UUID.randomUUID().toString();
		UserVerificationToken userVerificationToken = new UserVerificationToken();
		userVerificationToken.setToken(token);
		userVerificationToken.setUsername(username);
		userVerificationToken.setExpiryDate(Integer.parseInt(authProperties.getVerifyUserTokenExpiryInterval()));
		userVerificationToken.setEmail(email);
		userVerificationTokenRepository.save(userVerificationToken);
		return token;
	}

	/**
	 * Validating User Details with Existing Entries
	 *
	 * @param request request
	 * @return boolean
	 */
	private boolean validateUserDetails(UserDTO request) {
		Pattern pattern = Pattern.compile(CommonConstant.PASSWORD_PATTERN);
		Matcher matcher = pattern.matcher(request.getPassword());
		boolean flag = matcher.matches();
		boolean isEmailExist = isEmailExist(request.getEmail().toLowerCase());
		boolean isUsernameExists = findByUserName(request.getUsername()).isPresent();

		if (isUsernameExists)
			throw new GenericException("Cannot complete the registration process, Try with different username");
		if (!Pattern.compile(EMAIL_PATTERN).matcher(request.getEmail()).matches())
			throw new GenericException("Cannot complete the registration process, Invalid Email");
		if (isEmailExist)
			throw new GenericException("Cannot complete the registration process, Try with different email");
		return flag;
	}

	/**
	 * Update user profile
	 *
	 * @param username
	 * @param request
	 * @return
	 */
	@Override
	public boolean updateUserProfile(String username, UserDTO request) {

		Optional<User> user = userRepository.findByUsername(username);
		if (user.isPresent()) {
			User userData = user.get();
			userData.setUsername(request.getUsername());
			userData.setEmail(request.getEmail().toLowerCase());
			userData.setFirstName(request.getFirstName());
			userData.setLastName(request.getLastName());
			userData.setDisplayName(request.getDisplayName());
			userRepository.save(userData);
			return true;
		}
		return false;
	}

	@Override
	public boolean isPasswordIdentical(String oldPassword, String newPassword) {
		return oldPassword.equals(newPassword);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Authentication changePassword(String email, String password) {
		UsernamePasswordAuthenticationToken token = null;
		User user = userRepository.findByEmail(email);
		if (Objects.nonNull(user)) {
			user.setPassword(password);
			User authentication = userRepository.save(user);
			token = new UsernamePasswordAuthenticationToken(authentication.getUsername(), authentication.getPassword(),
															new ArrayList<>()
			);
			token.setDetails(AuthType.STANDARD);
		}
		return token;
	}

	/**
	 * @param username
	 * @param email
	 */

	public void sendUserPreApprovalRequestEmailToAdmin(String username, String email) {
		List<String> emailAddresses = commonService.getEmailAddressBasedOnRoles(
				Arrays.asList(CommonConstant.ROLE_SUPERADMIN));
		String serverPath = getServerPath();
		Map<String, String> customData = createCustomData(username, email, serverPath, "", "");
		commonService.sendEmailNotification(emailAddresses, customData,
											CommonConstant.PRE_APPROVAL_NOTIFICATION_SUBJECT_KEY,
											CommonConstant.PRE_APPROVAL_NOTIFICATION_KEY
		);
	}

	/**
	 * * create custom data for email
	 *
	 * @param username
	 * @param email
	 * @param url
	 * @param expiryTime
	 * @param token
	 * @return
	 */
	private Map<String, String> createCustomData(String username, String email, String url, String expiryTime,
												 String token) {
		Map<String, String> customData = new HashMap<>();

		customData.put(NotificationCustomDataEnum.USER_NAME.getValue(), username);
		customData.put(NotificationCustomDataEnum.USER_EMAIL.getValue(), email);
		customData.put(NotificationCustomDataEnum.SERVER_HOST.getValue(), url);

		if (StringUtils.isNotEmpty(token) && StringUtils.isNotEmpty(expiryTime)) {
			customData.put(NotificationCustomDataEnum.USER_TOKEN.getValue(), token);
			customData.put(NotificationCustomDataEnum.USER_TOKEN_EXPIRY.getValue(), expiryTime);

			String resetUrl = url + userInterfacePathsConfig.getValidateUser() + token;
			customData.put("resetUrl", resetUrl);
		}

		return customData;
	}

	/**
	 * common method to get server path
	 *
	 * @return
	 */
	private String getServerPath() {
		String serverPath = "";
		try {
			serverPath = commonService.getApiHost();
		} catch (UnknownHostException e) {
			log.error("ApproveRequestController: Server Host name is not bind with Approval Request mail ");
		}
		return serverPath;
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
	 * ResetPasswordTokenStatusEnum
	 */
	@Override
	public ResetPasswordTokenStatusEnum verifyUserToken(String token) {
		log.info("UserServiceImpl: Validate the token {}", token);
		UserVerificationToken userVerificationToken = userVerificationTokenRepository.findByToken(token);
		return checkTokenValidity(userVerificationToken);
	}

	/**
	 * Checks the validity of <tt>userVerificationToken</tt>
	 *
	 * @param userVerificationToken
	 * @return ResetPasswordTokenStatusEnum <tt>INVALID</tt> if token is
	 * <tt>null</tt>, <tt>VALID</tt> if token is not expired,
	 * <tt>EXPIRED</tt> if token is expired
	 */
	private ResetPasswordTokenStatusEnum checkTokenValidity(UserVerificationToken userVerificationToken) {
		if (userVerificationToken == null) {
			return ResetPasswordTokenStatusEnum.INVALID;
		} else if (isExpired(userVerificationToken.getExpiryDate())) {
			return ResetPasswordTokenStatusEnum.EXPIRED;
		} else {
			Optional<User> user = userRepository.findByUsername(userVerificationToken.getUsername());
			if (user.isPresent()) {
				User userData = user.get();
				userData.setUserVerified(true);
				userRepository.save(userData);
				sendUserPreApprovalRequestEmailToAdmin(userData.getUsername(), userData.getEmail());
			}
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
	 * if token is valid
	 */
	private boolean isExpired(Date expiryDate) {
		return new Date().after(expiryDate);
	}

}
