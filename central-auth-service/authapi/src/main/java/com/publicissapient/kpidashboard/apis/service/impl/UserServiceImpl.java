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

import java.io.StringReader;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.transaction.Transactional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml.spi.DefaultSamlAuthentication;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.publicissapient.kpidashboard.apis.config.AuthProperties;
import com.publicissapient.kpidashboard.apis.constant.CommonConstant;
import com.publicissapient.kpidashboard.apis.entity.User;
import com.publicissapient.kpidashboard.apis.entity.UserVerificationToken;
import com.publicissapient.kpidashboard.apis.enums.AuthType;
import com.publicissapient.kpidashboard.apis.enums.NotificationCustomDataEnum;
import com.publicissapient.kpidashboard.apis.enums.ResetPasswordTokenStatusEnum;
import com.publicissapient.kpidashboard.apis.errors.GenericException;
import com.publicissapient.kpidashboard.apis.errors.PendingApprovalException;
import com.publicissapient.kpidashboard.apis.repository.UserRepository;
import com.publicissapient.kpidashboard.apis.repository.UserVerificationTokenRepository;
import com.publicissapient.kpidashboard.apis.service.CommonService;
import com.publicissapient.kpidashboard.apis.service.MessageService;
import com.publicissapient.kpidashboard.apis.service.UserRoleService;
import com.publicissapient.kpidashboard.apis.service.UserService;
import com.publicissapient.kpidashboard.common.model.UserDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * This class provides method to perform CRUD and validation operations on user
 * authentication data.
 * 
 * @author hargupta15
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {
	public static final String ERROR_INVALID_USER = "error_invalid_user";
	public static final String INVALID_USER = "error_invalid_user";
	private static final String STANDARD = "STANDARD";
	private static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AuthProperties authProperties;
	@Autowired
	private CommonService commonService;
	@Autowired
	private UserVerificationTokenRepository userVerificationTokenRepository;
	@Autowired
	private UserRoleService userRoleService;
	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;
	private MessageService messageService;

	@Autowired
	public UserServiceImpl(@Lazy MessageService messageService) {
		this.messageService = messageService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean updateFailAttempts(String userName, LocalDateTime unsuccessAttemptTime) {
		User user = userRepository.findByUsername(userName);
		if (null == user) {
			return Boolean.FALSE;
		} else {
			long attemptCount = user.getFailedLoginAttemptCount();
			if (0 == attemptCount) {
				attemptCount = 1;
			} else {
				attemptCount++;
			}
			user.setFailedLoginAttemptCount(attemptCount);
			user.setLastUnsuccessfulLoginTime(unsuccessAttemptTime);
			userRepository.save(user);
			return Boolean.TRUE;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void resetFailAttempts(String userName) {
		User user = userRepository.findByUsername(userName);
		if (null != user) {
			user.setFailedLoginAttemptCount(0);
			user.setLastUnsuccessfulLoginTime(null);
			userRepository.save(user);
		}
	}

	public org.springframework.security.core.Authentication authenticate(
			org.springframework.security.core.Authentication authentication, String authType) {
		String fullEmail = authentication.getName();
		// Extract the substring before '@' as the username
		String username = fullEmail;
		if (fullEmail != null && fullEmail.contains("@")) {
			username = fullEmail.substring(0, fullEmail.indexOf("@"));
		}
		String password = (String) authentication.getCredentials();
		User dbUsers = getUserObject(authentication, authType, username);

		if (checkForResetFailAttempts(dbUsers, LocalDateTime.now())) {
			resetFailAttempts(username);
		} else if (checkForLockedUser(dbUsers)) {
			throw new LockedException("Account Locked: Invalid Login Limit Reached " + username);
		}

		if (dbUsers != null && !dbUsers.isUserVerified()) {
			throw new PendingApprovalException(
					"Login Failed: Your verification is pending. Please check your registered mail for verification");
		}

		if (dbUsers != null && !dbUsers.isApproved()) {
			throw new PendingApprovalException("Login Failed: Your access request is pending for approval");
		}

		if (dbUsers != null) {
			UserDTO userDTO = getUserDTO(dbUsers);
			if (authType.equalsIgnoreCase(AuthType.SAML.name())) {
				return new UsernamePasswordAuthenticationToken(userDTO, null, new ArrayList<>());
			} else if (!userDTO.getAuthType().equalsIgnoreCase(authType)) {
				throw new BadCredentialsException(
						"Login Failed: You have previously logged-in using SSO credentials. Please use SSO Authentication");
			} else {
				if (dbUsers.getPassword() != null && dbUsers.checkPassword(password)) {
					return new UsernamePasswordAuthenticationToken(userDTO, dbUsers.getPassword(), new ArrayList<>());
				} else {
					throw new BadCredentialsException("Login Failed: The username or password entered is incorrect");
				}
			}
		}

		throw new BadCredentialsException("Login Failed: The username or password entered is incorrect");
	}

	/**
	 * Creating and populating new User data
	 *
	 * @param user
	 *            user
	 * @return Authentication
	 */
	private Authentication generateUserAuthToken(User user) {
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user, user.getPassword(),
				new ArrayList<>());
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
		return userRepository.save(new User(username, password, firstName, lastName, displayName, email, createdDate,
				STANDARD, modifiedDate, false));
	}

	private User getUserObject(org.springframework.security.core.Authentication authentication, String authType,
			String username) {
		User dbUser = userRepository.findByUsername(username);
		if (null == dbUser && authType.equalsIgnoreCase(AuthType.SAML.name())) {
			dbUser = userRepository.save(createSamlAuthenticationObject((DefaultSamlAuthentication) authentication));
		}
		return dbUser;
	}

	private User createSamlAuthenticationObject(DefaultSamlAuthentication authentication) {
		User newUserObject = null;
		try {
			Response res = parseSAMLResponse(authentication.getResponseXml());
			newUserObject = extractUserInfo(res);
			newUserObject.setApproved(true);
			newUserObject.setUserVerified(true);
			newUserObject.setAuthType(AuthType.SAML.name());
			newUserObject.setCreatedDate(LocalDateTime.now());
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		return newUserObject;
	}

	private Response parseSAMLResponse(String samlResponseXML) throws Exception {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();

		// Parse the XML string into a DOM Document
		Document document = docBuilder.parse(new InputSource(new StringReader(samlResponseXML)));
		Element element = document.getDocumentElement();

		// Get an instance of the response unmarshaller
		UnmarshallerFactory unmarshallerFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
		Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);

		// Unmarshall the DOM Element into a SAML Response object
		XMLObject responseXmlObj = unmarshaller.unmarshall(element);
		if (responseXmlObj instanceof Response) {
			return (Response) responseXmlObj;
		} else {
			throw new IllegalArgumentException("Invalid SAML response");
		}
	}

	private User extractUserInfo(Response samlResponse) {
		User newUsersObject = null;
		Status status = samlResponse.getStatus();
		StatusCode statusCode = status.getStatusCode();
		if (StatusCode.SUCCESS.equals(statusCode.getValue())) {
			Assertion assertion = samlResponse.getAssertions().get(0);
			AttributeStatement attrStatement = assertion.getAttributeStatements().get(0);
			newUsersObject = new User();
			for (Attribute attribute : attrStatement.getAttributes()) {
				if ("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress".equals(attribute.getName())) {
					newUsersObject.setEmail(getAttribute(attribute));
				} else if ("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name".equals(attribute.getName())) {
					String fullEmail = getAttribute(attribute);
					if (fullEmail != null && fullEmail.contains("@")) {
						// Extract the substring before '@' as the username
						String userName = fullEmail.substring(0, fullEmail.indexOf("@"));
						newUsersObject.setUsername(userName);
					}
					newUsersObject.setSamlEmail(getAttribute(attribute));
				} else if ("http://schemas.microsoft.com/identity/claims/displayname"
						.equalsIgnoreCase(attribute.getName())) {
					newUsersObject.setDisplayName(getAttribute(attribute));
				} else if ("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname"
						.equalsIgnoreCase(attribute.getName())) {
					newUsersObject.setFirstName(getAttribute(attribute));
				} else if ("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname"
						.equalsIgnoreCase(attribute.getName())) {
					newUsersObject.setLastName(getAttribute(attribute));
				}
			}
		} else {
			log.error("Authentication failed: " + statusCode.getValue());
		}
		return newUsersObject;
	}

	private String getAttribute(Attribute attribute) {
		String value = attribute.getAttributeValues().get(0).getDOM().getTextContent();
		log.debug("value: {}", value);
		return value;
	}

	/**
	 * Checks if user is locked
	 *
	 * @param user
	 *            the Authentication
	 * @return true if user is locked
	 */
	private boolean checkForLockedUser(User user) {

		return user != null && user.getFailedLoginAttemptCount() != 0
				&& user.getFailedLoginAttemptCount() == authProperties.getAccountLockedThreshold();
	}

	/**
	 * Checks if need to reset fail attempts.
	 *
	 * @param user
	 *            Authentication
	 * @param now
	 *            current date time
	 * @return true or false
	 */
	private boolean checkForResetFailAttempts(User user, LocalDateTime now) {
		return user != null && null != user.getLastUnsuccessfulLoginTime() && now
				.isAfter(user.getLastUnsuccessfulLoginTime().plusMinutes(authProperties.getAccountLockedPeriod()));
	}

	@Override
	public User getAuthentication(String username) {
		return userRepository.findByUsername(username);
	}

	@Override
	public String getUsername(org.springframework.security.core.Authentication authentication) {

		if (authentication == null) {
			return null;
		}

		String username;
		if (authentication.getPrincipal() instanceof User) {
			username = ((User) authentication.getPrincipal()).getUsername();
		} else if (authentication.getPrincipal() instanceof UserDTO) {
			username = ((UserDTO) authentication.getPrincipal()).getUsername();
		} else if (authentication.getPrincipal() instanceof String) {
			username = authentication.getPrincipal().toString();
		} else {
			username = null;
		}
		return username;
	}

	@Override
	public UserDTO getOrSaveSSOAuthentication(String username, String authType, String email) {
		User user = getAuthentication(username);
		if (null == user) {
			user = User.builder().username(username).email(email).authType(authType).approved(true).build();
			user = userRepository.save(user);
		}
		return getUserDTO(user);
	}

	@Override
	public boolean isEmailExist(String email) {
		return userRepository.findByEmail(email) != null;
	}

	@Override
	public boolean isUsernameExists(String username) {
		return userRepository.findByUsername(username) != null;
	}

	@Override
	public User findByUserName(String userName) {
		return userRepository.findByUsername(userName);
	}

	/**
	 * method to delete user details from user collection
	 * 
	 * @param userName
	 * @return
	 */
	@Override
	@Transactional
	public boolean deleteByUserName(String userName) {
		try {
			userRepository.deleteByUsername(userName);
			return true;
		} catch (Exception e) {
			log.info("error while delete user", e);
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
					.approved(user.isApproved()).firstName(user.getFirstName()).lastName(user.getLastName())
					.displayName(user.getDisplayName()).authType(user.getAuthType()).userVerified(user.isUserVerified())
					.build();
		}
		return dto;
	}

	/**
	 * Method To create and save details of new Users
	 *
	 * @param request
	 *            request
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
	 * @param request
	 */
	@Override
	public void deleteUnVerifiedUser(UserVerificationToken request) {
		if (request.getUsername() != null && request.getEmail() != null) {
			sendVerificationFailedMailUser(request.getUsername(), request.getEmail());
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
				authProperties.getVerifyUserTokenExpiryInterval(), token);
		commonService.sendEmailNotification(Arrays.asList(email), customData,
				CommonConstant.USER_VERIFICATION_NOTIFICATION_KEY, CommonConstant.USER_VERIFICATION_TEMPLATE_KEY);
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
					CommonConstant.USER_VERIFICATION_FAILED_TEMPLATE_KEY);
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
	 * @param request
	 *            request
	 * @return boolean
	 */
	private boolean validateUserDetails(UserDTO request) {
		Pattern pattern = Pattern.compile(CommonConstant.PASSWORD_PATTERN);
		Matcher matcher = pattern.matcher(request.getPassword());
		boolean flag = matcher.matches();
		boolean isEmailExist = isEmailExist(request.getEmail().toLowerCase());
		boolean isUsernameExists = isUsernameExists(request.getUsername());

		if (isUsernameExists)
			throw new GenericException("Cannot complete the registration process, Try with different username");
		if (!Pattern.compile(EMAIL_PATTERN).matcher(request.getEmail()).matches())
			throw new GenericException("Cannot complete the registration process, Invalid Email");
		if (isEmailExist)
			throw new GenericException("Cannot complete the registration process, Try with different email");
		return flag;
	}

	/**
	 * Validate user details
	 *
	 * @param username
	 *            username
	 * @return User
	 */
	@Override
	public User validateUser(String username) {
		if (StringUtils.isEmpty(username)) {
			log.error(messageService.getMessage(INVALID_USER) + " : " + username);
			throw new GenericException(messageService.getMessage(INVALID_USER));
		}
		User user = userRepository.findByUsername(username);
		if (user == null) {
			log.error(messageService.getMessage(INVALID_USER) + " : " + username);
			throw new GenericException(messageService.getMessage(INVALID_USER));
		}
		return user;
	}

	/**
	 * get logged current user
	 *
	 * @return String
	 */
	@Override
	public String getLoggedInUser() {
		org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext()
				.getAuthentication();
		String username;

		if (authentication.getPrincipal() instanceof User) {
			username = ((User) authentication.getPrincipal()).getUsername();
		} else if (authentication.getPrincipal() instanceof UserDTO) {
			username = ((UserDTO) authentication.getPrincipal()).getUsername();
		} else if (authentication.getPrincipal() instanceof String) {
			username = authentication.getPrincipal().toString();
		} else {
			username = null;
		}
		return username;
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

		User user = userRepository.findByUsername(username);
		if (user != null) {
			user.setUsername(request.getUsername());
			user.setEmail(request.getEmail().toLowerCase());
			user.setFirstName(request.getFirstName());
			user.setLastName(request.getLastName());
			user.setDisplayName(request.getDisplayName());
			userRepository.save(user);
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
	public org.springframework.security.core.Authentication changePassword(String email, String password) {
		UsernamePasswordAuthenticationToken token = null;
		User user = userRepository.findByEmail(email);
		if (Objects.nonNull(user)) {
			user.setPassword(password);
			User authentication = userRepository.save(user);
			token = new UsernamePasswordAuthenticationToken(authentication.getUsername(), authentication.getPassword(),
					new ArrayList<>());
			token.setDetails(AuthType.STANDARD);
		}
		return token;
	}

	/**
	 *
	 * @param username
	 * @param email
	 */

	public void sendUserPreApprovalRequestEmailToAdmin(String username, String email) {
		List<String> emailAddresses = commonService
				.getEmailAddressBasedOnRoles(Arrays.asList(CommonConstant.ROLE_SUPERADMIN));
		String serverPath = getServerPath();
		Map<String, String> customData = createCustomData(username, email, serverPath, "", "");
		commonService.sendEmailNotification(emailAddresses, customData,
				CommonConstant.PRE_APPROVAL_NOTIFICATION_SUBJECT_KEY, CommonConstant.PRE_APPROVAL_NOTIFICATION_KEY);
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
			String resetUrl = url + authProperties.getValidateUser() + token;
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
	 *         ResetPasswordTokenStatusEnum
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
	 *         <tt>null</tt>, <tt>VALID</tt> if token is not expired,
	 *         <tt>EXPIRED</tt> if token is expired
	 */
	private ResetPasswordTokenStatusEnum checkTokenValidity(UserVerificationToken userVerificationToken) {
		if (userVerificationToken == null) {
			return ResetPasswordTokenStatusEnum.INVALID;
		} else if (isExpired(userVerificationToken.getExpiryDate())) {
			return ResetPasswordTokenStatusEnum.EXPIRED;
		} else {
			User user = userRepository.findByUsername(userVerificationToken.getUsername());
			if (user != null) {
				user.setUserVerified(true);
				userRepository.save(user);
				sendUserPreApprovalRequestEmailToAdmin(user.getUsername(), user.getEmail());
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
	 *         if token is valid
	 */
	private boolean isExpired(Date expiryDate) {
		return new Date().after(expiryDate);
	}

}
