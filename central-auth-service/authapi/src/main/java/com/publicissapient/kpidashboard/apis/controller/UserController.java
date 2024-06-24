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

package com.publicissapient.kpidashboard.apis.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import com.publicissapient.kpidashboard.common.model.*;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.publicissapient.kpidashboard.apis.config.AuthProperties;
import com.publicissapient.kpidashboard.apis.constant.CommonConstant;
import com.publicissapient.kpidashboard.apis.entity.User;
import com.publicissapient.kpidashboard.apis.entity.UserVerificationToken;
import com.publicissapient.kpidashboard.apis.enums.AuthType;
import com.publicissapient.kpidashboard.apis.enums.ResetPasswordTokenStatusEnum;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filters.AuthenticationResponseService;
import com.publicissapient.kpidashboard.apis.repository.UserRoleRepository;
import com.publicissapient.kpidashboard.apis.repository.UserVerificationTokenRepository;
import com.publicissapient.kpidashboard.apis.service.CommonService;
import com.publicissapient.kpidashboard.apis.service.ForgotPasswordService;
import com.publicissapient.kpidashboard.apis.service.MessageService;
import com.publicissapient.kpidashboard.apis.service.ResourceService;
import com.publicissapient.kpidashboard.apis.service.RoleService;
import com.publicissapient.kpidashboard.apis.service.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.service.UserApprovalService;
import com.publicissapient.kpidashboard.apis.service.UserRoleService;
import com.publicissapient.kpidashboard.apis.service.UserService;
import com.publicissapient.kpidashboard.apis.service.UserTokenDeletionService;
import com.publicissapient.kpidashboard.apis.util.CookieUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Rest Controller to handle authentication requests
 *
 * @author hirenkumar Babariya
 */
@RestController
@Slf4j
@SuppressWarnings("java:S3740")
public class UserController {
	public static final String SUCCESS_VALID_TOKEN = "success_valid_token";
	public static final String ERROR_INVALID_USER = "error_invalid_user";
	public static final String SUCCESS_DELETE_TOKEN = "success_delete_token";
	public static final String ERROR_UNAUTHORIZED_USER = "error_unauthorized_user";
	public static final String SUCCESS_LOGIN = "success_login";
	public static final String SUCCESS_SENT_APPROVAL = "success_sent_approval";
	public static final String ERROR_REGISTER_AGAIN = "error_register_again";

	private static final String AUTH_RESPONSE_HEADER = "X-Authentication-Token";
	@Autowired
	private AuthProperties authConfigurationProperties;

	@Autowired
	private UserService userService;
	@Autowired
	private UserVerificationTokenRepository userVerificationTokenRepository;

	@Autowired
	private CookieUtil cookieUtil;
	@Autowired
	private UserTokenDeletionService userTokenDeletionService;

	@Autowired
	private TokenAuthenticationService tokenAuthenticationService;

	@Autowired
	private UserRoleService userRoleService;
	@Autowired
	private UserApprovalService userApprovalService;

	@Autowired
	private ResourceService resourceService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private MessageService messageService;

	@Autowired
	private ForgotPasswordService forgotPasswordService;

	@Autowired
	private CommonService commonService;

	@Autowired
	private AuthenticationResponseService authenticationResponseService;
	@Autowired
	private UserRoleRepository userRoleRepository;

	@GetMapping("/saml/login")
	public RedirectView login() {
		// Assuming authConfigurationProperties.getLoginCallback() provides the SAML
		// login callback URL
		String samlLoginCallback = authConfigurationProperties.getLoginCallback();
		// Build the final SAML login URL by appending the original URL as a query
		// parameter
		return new RedirectView(samlLoginCallback, true);
	}

	@GetMapping("/login/status/{authToken}")
	public ResponseEntity<?> loginStatusCheck(@PathVariable String authToken, HttpServletResponse response) {
		try {
			if (Objects.nonNull(authToken)) {
				String userName = (String) tokenAuthenticationService.getClaim(authToken, CommonConstant.SUB);
				User user = userService.getAuthentication(userName);
				ModelMapper mapper = new ModelMapper();
				LoginResponse loginResponse = mapper.map(user, LoginResponse.class);
				Cookie cookie = cookieUtil.createAccessTokenCookie(authToken);
				response.addCookie(cookie);
				cookieUtil.addSameSiteCookieAttribute(response);
				ServiceResponse serviceResponse = new ServiceResponse(true,
						messageService.getMessage(SUCCESS_VALID_TOKEN), loginResponse);
				return ResponseEntity.ok(serviceResponse);
			} else {
				ServiceResponse serviceResponse = new ServiceResponse(false,
						messageService.getMessage(ERROR_INVALID_USER), null);
				return ResponseEntity.ok(serviceResponse);
			}
		} catch (Exception e) {
			ServiceResponse serviceResponse = new ServiceResponse(false, messageService.getMessage(ERROR_INVALID_USER),
					null);
			return ResponseEntity.ok(serviceResponse);
		}
	}

	@GetMapping("/login/status/standard")
	public ResponseEntity<?> loginStatusCheck(HttpServletRequest request, HttpServletResponse response) {
		try {
			Cookie authCookie = cookieUtil.getAuthCookie(request);
			if (Objects.nonNull(authCookie) && StringUtils.isNotEmpty(authCookie.getValue())) {
				String authToken = authCookie.getValue();
				String userName = (String) tokenAuthenticationService.getClaim(authToken, CommonConstant.SUB);
				User user = userService.getAuthentication(userName);
				UserTokenAuthenticationDTO userTokenAuthenticationDTO = new UserTokenAuthenticationDTO();
				userTokenAuthenticationDTO.setUsername(userName);
				userTokenAuthenticationDTO.setEmail(user.getEmail());
				Cookie cookie = cookieUtil.createAccessTokenCookie(authToken);
				response.addCookie(cookie);
				userTokenAuthenticationDTO.setAuthToken(cookie.getValue());
				cookieUtil.addSameSiteCookieAttribute(response);
				ServiceResponse serviceResponse = new ServiceResponse(true,
						messageService.getMessage(SUCCESS_VALID_TOKEN), userTokenAuthenticationDTO);
				return ResponseEntity.ok(serviceResponse);
			} else {
				ServiceResponse serviceResponse = new ServiceResponse(false,
						messageService.getMessage(ERROR_INVALID_USER), null);
				return ResponseEntity.ok(serviceResponse);
			}
		} catch (Exception e) {
			ServiceResponse serviceResponse = new ServiceResponse(false, messageService.getMessage(ERROR_INVALID_USER),
					null);
			return ResponseEntity.ok(serviceResponse);
		}
	}

	/**
	 * @param authToken
	 *            authToken
	 * @return ResponseEntity
	 */
	@GetMapping(value = "/userlogout/{authToken}", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity deleteUserToken(@PathVariable String authToken, HttpServletRequest request,
			HttpServletResponse response) {
		log.info("UserTokenDeletionController::deleteUserToken start");
		if (null != authToken) {
			String userName = (String) tokenAuthenticationService.getClaim(authToken, CommonConstant.SUB);
			if (null != userName) {
				userTokenDeletionService.invalidateSession(userName);
				cookieUtil.deleteCookie(request, response, CookieUtil.AUTH_COOKIE);
				HttpSession session;
				SecurityContextHolder.clearContext();

				session = request.getSession(false);
				if (session != null) {
					session.invalidate();
				}
				Cookie authCookieRemove = new Cookie("authCookie", "");
				resetHeader(response, "", authCookieRemove);
				log.info("UserTokenDeletionController::deleteUserToken end");
				return ResponseEntity.ok()
						.body(new ServiceResponse(true, messageService.getMessage(SUCCESS_DELETE_TOKEN), true));
			} else {
				return ResponseEntity.badRequest()
						.body(new ServiceResponse(false, messageService.getMessage(ERROR_INVALID_USER), userName));
			}
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	private void resetHeader(HttpServletResponse response, String authToken, Cookie cookie) {
		response.addHeader(AUTH_RESPONSE_HEADER, authToken);
		response.addHeader("CLEAR_VIA_CENTRAL", "true");
		response.addCookie(cookie);
		cookieUtil.addSameSiteCookieAttribute(response);
	}

	/**
	 * @param username
	 *            username
	 * @return ServiceResponse
	 */
	@PostMapping(value = "/sso/users/{username}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> fetchOrSaveUserInfo(@PathVariable String username) {
		ServiceResponse response = new ServiceResponse(false, messageService.getMessage(ERROR_UNAUTHORIZED_USER), null);
		UserDTO userDTO = userService.getOrSaveSSOAuthentication(username, AuthType.SSO.name(), null);
		if (null != userDTO && userDTO.getAuthType().equalsIgnoreCase(AuthType.SSO.name())) {
			response = new ServiceResponse(true, messageService.getMessage(SUCCESS_LOGIN), userDTO);
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 * @param httpServletRequest
	 *            httpServletRequest
	 * @param httpServletResponse
	 *            httpServletResponse
	 * @return ServiceResponse
	 */
	@GetMapping("/saml/logout")
	public RedirectView samlLogout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		Cookie cookie = cookieUtil.getAuthCookie(httpServletRequest);
		String token = cookie.getValue();
		userTokenDeletionService.deleteUserDetailsByToken(token);
		String subject = tokenAuthenticationService.getSubject(token);
		String afterLogout = (StringUtils.isNotEmpty(subject))
				? String.format(authConfigurationProperties.getLogoutCallback(), subject)
				: authConfigurationProperties.getDefaultRedirectToAfterLogout();
		ResponseCookie authCookie = cookieUtil.deleteAccessTokenCookie();
		httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, authCookie.toString());
		return new RedirectView(afterLogout);
	}

	/**
	 * @param username
	 *            username
	 * @return ServiceResponse
	 */
	@Deprecated
	@GetMapping(value = "/user/{username}", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> fetchUserInfo(@PathVariable String username) {
		ServiceResponse response = new ServiceResponse(false, messageService.getMessage(ERROR_UNAUTHORIZED_USER), null);
		User user = userService.getAuthentication(username);
		if (null != user) {
			UserDTO userDTO = userService.getUserDTO(user);
			response = new ServiceResponse(true, messageService.getMessage(SUCCESS_VALID_TOKEN), userDTO);
			return ResponseEntity.status(HttpStatus.OK).body(response);
		} else {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}
	}

	/**
	 * @param userNameRequestDTO
	 *            username
	 * @return ServiceResponse
	 */
	@PostMapping(value = "/user", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> fetchUserInfo(@Valid @RequestBody UserNameRequestDTO userNameRequestDTO) {
		ServiceResponse response = new ServiceResponse(false, messageService.getMessage(ERROR_UNAUTHORIZED_USER), null);
		User user = userService.getAuthentication(userNameRequestDTO.getUserName());
		if (null != user) {
			UserDTO userDTO = userService.getUserDTO(user);
			response = new ServiceResponse(true, messageService.getMessage(SUCCESS_VALID_TOKEN), userDTO);
			return ResponseEntity.status(HttpStatus.OK).body(response);
		} else {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}
	}
	/**
	 * Retrieve user info of the current user from the cookie
	 *
	 * @param request
	 *            request
	 * @return ResponseEntity
	 */
	@GetMapping(value = "/user-info", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> fetchUserInfoFromAuthCookie(HttpServletRequest request) {
		try {
			ServiceResponse response = new ServiceResponse(false, messageService.getMessage(ERROR_UNAUTHORIZED_USER),
					null);

			Cookie authCookie = cookieUtil.getAuthCookie(request);
			if (Objects.nonNull(authCookie) && StringUtils.isNotEmpty(authCookie.getValue())) {
				String authToken = authCookie.getValue();
				String userName = (String) tokenAuthenticationService.getClaim(authToken, CommonConstant.SUB);

				User user = userService.getAuthentication(userName);
				UserDTO userDTO = userService.getUserDTO(user);

				response = new ServiceResponse(true, messageService.getMessage(SUCCESS_VALID_TOKEN), userDTO);

				return ResponseEntity.status(HttpStatus.OK).body(response);
			} else {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
			}
		} catch (Exception e) {
			ServiceResponse serviceResponse = new ServiceResponse(false, messageService.getMessage(ERROR_INVALID_USER),
					null);
			return ResponseEntity.ok(serviceResponse);
		}
	}

	/**
	 * Post Method To create new User
	 *
	 * @param httpServletResponse
	 *            httpServletResponse
	 * @param request
	 *            request
	 * @return ServiceResponse
	 */
	@PostMapping(value = "/registerUser", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> registerUser(HttpServletResponse httpServletResponse,
			@Valid @RequestBody UserDTO request) {
		boolean isSuccess = userService.registerUser(request);
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ServiceResponse(isSuccess, isSuccess ? messageService.getMessage(SUCCESS_SENT_APPROVAL)
						: messageService.getMessage(ERROR_REGISTER_AGAIN), request.getUsername()));
	}

	/**
	 * Get Method To Fetch All unapproved Requests
	 *
	 * @return ServiceResponse
	 */
	@GetMapping("/user-approvals/pending")
	public ResponseEntity<ServiceResponse> unApprovedUsers() {
		return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(true,
				messageService.getMessage("success_pending_approval"), userApprovalService.findAllUnapprovedUsers()));
	}


	/**
	 * Put Method to Approve request
	 *
	 * @param userNameRequestDTO
	 *            username
	 * @return ServiceResponse
	 */
	@PutMapping(value = "/update-userApproval", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> updateApprovalRequest(@Valid @RequestBody UserNameRequestDTO userNameRequestDTO) {
		boolean isSuccess = userApprovalService.updateApprovalRequest(userNameRequestDTO.getUserName());
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ServiceResponse(isSuccess, isSuccess ? messageService.getMessage("success_request_approve")
						: messageService.getMessage("error_request_approve"), isSuccess));

	}

	/**
	 * delete user api
	 *
	 * @param username
	 * @return
	 */
	@Deprecated
	@GetMapping(value = "/deleteUser/{username}", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> deleteUser(@PathVariable("username") String username) {

		boolean isSuccess = userApprovalService.deleteRejectUser(username);
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ServiceResponse(isSuccess, isSuccess ? messageService.getMessage("rejected_user_deleted")
						: messageService.getMessage("error_delete_user"), isSuccess));

	}

	/**
	 * delete user api
	 *
	 * @param userNameRequestDTO
	 * @return
	 */
	@PostMapping(value = "/deleteUser", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> deleteUser(@Valid @RequestBody UserNameRequestDTO userNameRequestDTO) {

		boolean isSuccess = userApprovalService.deleteRejectUser(userNameRequestDTO.getUserName());
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ServiceResponse(isSuccess, isSuccess ? messageService.getMessage("rejected_user_deleted")
						: messageService.getMessage("error_delete_user"), isSuccess));

	}

	/**
	 * Put method to update user profile
	 *
	 * @param request  updated data
	 * @return
	 */
	@PutMapping(value = "/users/updateProfile", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> updateUserProfile(@Valid @RequestBody UserDTO request) {
		boolean isSuccess = userService.updateUserProfile(request);

		return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(isSuccess, isSuccess ? messageService.getMessage("success_profile_user") : messageService.getMessage("error_update_profile"), null));
	}

	@PostMapping(value = "/forgotPassword", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> processForgotPassword(
			@RequestBody ForgotPasswordRequestDTO forgotPasswordRequestDTO, HttpServletRequest httpServletRequest) {
		boolean isSuccess = false;
		log.info("ForgotPasswordController: requested mail {}", forgotPasswordRequestDTO.getEmail());
		User user = null;
		try {
			String serverPath = commonService.getApiHost();
			log.info("ForgotPasswordController: serverPath {}", serverPath);
			user = forgotPasswordService.processForgotPassword(forgotPasswordRequestDTO.getEmail(),
					commonService.getApiHost());
			if (null != user) {
				isSuccess = true;
				User auth = new User();
				auth.setEmail(user.getEmail().toLowerCase());
				user = auth;
				return ResponseEntity.ok().body(
						new ServiceResponse(isSuccess, messageService.getMessage("success_forgot_password"), user));
			} else {
				return ResponseEntity.badRequest()
						.body(new ServiceResponse(isSuccess, messageService.getMessage("error_email_not_exist"), null));
			}
		} catch (UnknownHostException e) {
			log.error("UnknownHostException", e);
			log.error("ForgotPasswordController: Mail can not be sent to {}", forgotPasswordRequestDTO.getEmail());
			return ResponseEntity.badRequest()
					.body(new ServiceResponse(isSuccess, messageService.getMessage("error_forgot_password"), null));
		}
	}

	/**
	 * Change password.
	 *
	 * @param httpServletRequest
	 *            the http servlet request
	 * @param httpServletResponse
	 *            the http servlet response
	 * @param request
	 *            the request
	 * @return the response entity
	 * @throws IOException
	 *             the io exception
	 * @throws ServletException
	 *             the servlet exception
	 */
	@PostMapping(value = "/changePassword", produces = APPLICATION_JSON_VALUE)
	// NOSONAR
	public ResponseEntity<ServiceResponse> changePassword(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, @Valid @RequestBody ChangePasswordRequestDTO request)
			throws IOException, ServletException { // NOSONAR
		try {
			Pattern pattern = Pattern.compile(CommonConstant.PASSWORD_PATTERN);
			Matcher matcher = pattern.matcher(request.getPassword());
			boolean flag = matcher.matches();
			boolean isEmailExist = userService.isEmailExist(request.getEmail().toLowerCase());
			boolean isPasswordIdentical = userService.isPasswordIdentical(request.getOldPassword(),
					request.getPassword());
			if (isEmailExist) {
				if (flag) {
					if (isPassContainUser(request.getPassword(), request.getUser())) {
						if (isPasswordIdentical) {
							return ResponseEntity.ok().body(new ServiceResponse(false,
									messageService.getMessage("error_same_old_password"), null));
						} else {
							User user = userService.findByUserName(request.getUser());
							boolean isValidUserCheck = user.checkPassword(request.getOldPassword());
							return isValidUser(isValidUserCheck, request, httpServletResponse);
						}
					} else {
						return ResponseEntity.ok().body(
								new ServiceResponse(false, messageService.getMessage("error_password_contain"), null));
					}
				} else {
					return ResponseEntity.ok().body(
							new ServiceResponse(false, messageService.getMessage("error_password_pattern"), null));
				}
			} else {
				return ResponseEntity.ok()
						.body(new ServiceResponse(false, messageService.getMessage("error_email_not_exist"), null));
			}
		} catch (DuplicateKeyException dke) {
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
					.body(new ServiceResponse(false, messageService.getMessage("error_exception_unprocessable"), null));
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
	@PostMapping(value = "/resetPassword", produces = APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> resetPassword(@RequestBody ResetPasswordRequestDTO updatedPasswordRequest) {
		boolean isSuccess = false;
		log.info("ForgotPasswordController: requested token for update {}", updatedPasswordRequest.getResetToken());
		User user = null;
		try {
			user = forgotPasswordService.resetPassword(updatedPasswordRequest);
			if (null != user) {
				isSuccess = true;
				User auth = new User();
				auth.setEmail(user.getEmail().toLowerCase());
				user = auth;
			}
		} catch (ApplicationException e) {
			log.error("Error in ForgotPasswordController: resetPassword()", e);
			return ResponseEntity.badRequest().body(new ServiceResponse(isSuccess, e.getMessage(), null));
		}
		return ResponseEntity.ok()
				.body(new ServiceResponse(isSuccess, messageService.getMessage("success_reset_password"), user));
	}

	private boolean isPassContainUser(String reqPassword, String username) {
		return !(StringUtils.containsIgnoreCase(reqPassword, username));
	}

	/**
	 * @param isValidUser
	 * @param request
	 * @param httpServletResponse
	 * @return
	 */
	private ResponseEntity<ServiceResponse> isValidUser(boolean isValidUser, @Valid ChangePasswordRequestDTO request,
			HttpServletResponse httpServletResponse) {
		if (isValidUser) {
			userService.changePassword(request.getEmail(), request.getPassword());
			return ResponseEntity.ok().body(new ServiceResponse(true,
					messageService.getMessage("success_change_password"), request.getUser()));
		} else {
			return ResponseEntity.ok()
					.body(new ServiceResponse(false, messageService.getMessage("error_wrong_password"), null));
		}
	}

	@GetMapping(value = "/validateEmailToken", produces = APPLICATION_JSON_VALUE) // NOSONAR
	public RedirectView validateToken(HttpServletRequest httpServletRequest, @RequestParam("token") UUID token)
			throws UnknownHostException {
		log.info("ForgotPasswordController: requested token for validate {}", token);
		ResetPasswordTokenStatusEnum tokenStatus = forgotPasswordService.validateEmailToken(token.toString());
		String baseUiUrl = authConfigurationProperties.getBaseUiUrl();
		if (tokenStatus != null && tokenStatus.equals(ResetPasswordTokenStatusEnum.VALID)) {
			return new RedirectView(baseUiUrl + authConfigurationProperties.getUiResetPath() + token);
		} else {
			return new RedirectView(baseUiUrl + authConfigurationProperties.getUiResetPath() + tokenStatus);
		}
	}

	/**
	 * api to verify user
	 *
	 * @param httpServletRequest
	 * @param token
	 * @return
	 * @throws UnknownHostException
	 */
	@GetMapping(value = "/verifyUser", produces = APPLICATION_JSON_VALUE) // NOSONAR
	public RedirectView verifyUser(HttpServletRequest httpServletRequest, @RequestParam("token") UUID token)
			throws UnknownHostException {
		log.info("UserController: requested token for validate {}", token);
		ResetPasswordTokenStatusEnum tokenStatus = userService.verifyUserToken(token.toString());
		String serverPath = authConfigurationProperties.getBaseUiUrl();
		if (tokenStatus != null && tokenStatus.equals(ResetPasswordTokenStatusEnum.VALID)) {
			return new RedirectView(serverPath);
		} else {
			UserVerificationToken userVerificationToken = userVerificationTokenRepository.findByToken(token.toString());
			log.info("UserController: User ", token);
			userService.deleteUnVerifiedUser(userVerificationToken);
			return new RedirectView(serverPath + authConfigurationProperties.getRegisterPath());
		}
	}

}
