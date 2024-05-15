package com.publicissapient.kpidashboard.apis.controller;

import javax.validation.Valid;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.publicissapient.kpidashboard.apis.config.AuthConfig;
import com.publicissapient.kpidashboard.apis.config.UserInterfacePathsConfig;
import com.publicissapient.kpidashboard.apis.constant.CommonConstant;
import com.publicissapient.kpidashboard.apis.entity.User;
import com.publicissapient.kpidashboard.apis.enums.ResetPasswordTokenStatusEnum;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filters.standard.service.AuthenticationResponseService;
import com.publicissapient.kpidashboard.apis.service.*;
import com.publicissapient.kpidashboard.apis.util.CookieUtil;
import com.publicissapient.kpidashboard.common.model.*;

import static com.publicissapient.kpidashboard.apis.constant.CommonConstant.*;

@RestController
@AllArgsConstructor
@Slf4j
public class StandardAuthenticationController {

	private final TokenAuthenticationService tokenAuthenticationService;

	private final UserService userService;

	private final MessageService messageService;

	private final UserApprovalService userApprovalService;

	private final NotificationService commonService;

	private final ForgotPasswordService forgotPasswordService;

	private final AuthenticationResponseService authenticationResponseService;

	private final AuthConfig authConfigurationProperties;

	private final UserInterfacePathsConfig userInterfacePathsConfig;

	@GetMapping("/login/status/standard")
	public ResponseEntity<ServiceResponse> loginStatusCheck(HttpServletRequest request) {
		try {
			Optional<String> authCookie = CookieUtil.getCookieValue(request, CookieUtil.COOKIE_NAME);

			if (authCookie.isPresent() && !authCookie.get().isEmpty()) {

				String userName = tokenAuthenticationService.getSubject(authCookie.get());

				Optional<User> user = userService.findByUsername(userName);

				UserTokenAuthenticationDTO userTokenAuthenticationDTO = new UserTokenAuthenticationDTO();
				userTokenAuthenticationDTO.setUsername(userName);
				userTokenAuthenticationDTO.setEmail(user.get().getEmail());

				ServiceResponse serviceResponse = new ServiceResponse(true,
																	  messageService.getMessage(SUCCESS_VALID_TOKEN),
																	  userTokenAuthenticationDTO
				);
				return ResponseEntity.ok(serviceResponse);
			} else {
				ServiceResponse serviceResponse = new ServiceResponse(false,
																	  messageService.getMessage(ERROR_INVALID_USER),
																	  null
				);
				return ResponseEntity.ok(serviceResponse);
			}
		} catch (Exception e) {
			ServiceResponse serviceResponse = new ServiceResponse(false, messageService.getMessage(ERROR_INVALID_USER),
																  null
			);
			return ResponseEntity.ok(serviceResponse);
		}
	}

	/**
	 * Post Method To create new User
	 *
	 * @param request request
	 * @return ServiceResponse
	 */
	@PostMapping(value = "/register-user", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> registerUser(@Valid @RequestBody UserDTO request) {
		boolean isSuccess = userService.registerUser(request);
		return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(isSuccess, isSuccess ?
				messageService.getMessage(SUCCESS_SENT_APPROVAL) :
				messageService.getMessage(ERROR_REGISTER_AGAIN), request.getUsername()));
	}


	@GetMapping("/user-approvals/pending")
	public ResponseEntity<ServiceResponse> getAllUnapprovedUsers() {
		return ResponseEntity.status(HttpStatus.OK)
							 .body(new ServiceResponse(true,
													   messageService.getMessage("success_pending_approval"),
													   userApprovalService.findAllUnapprovedUsers()
							 ));
	}

	@PutMapping(value = "/approve/{username}", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> approveUserCreationRequest(@PathVariable("username") String username) {
		boolean isSuccess = userApprovalService.approveUser(username);

		return ResponseEntity.status(HttpStatus.OK)
							 .body(new ServiceResponse(
									 isSuccess,
									 isSuccess
											 ? messageService.getMessage("success_request_approve")
											 : messageService.getMessage("error_request_approve"),
									 isSuccess
							 ));

	}

	@GetMapping(value = "/reject/{username}", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> deleteUser(@PathVariable("username") String username) {
		boolean isSuccess = userApprovalService.rejectUser(username);
		return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(isSuccess, isSuccess ?
				messageService.getMessage("rejected_user_deleted") :
				messageService.getMessage("error_delete_user"), isSuccess));

	}

	@PostMapping(value = "/forgot-password", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> processForgotPassword(
			@RequestBody ForgotPasswordRequestDTO forgotPasswordRequestDTO) {
		boolean isSuccess = false;
		log.info("ForgotPasswordController: requested mail {}", forgotPasswordRequestDTO.getEmail());
		User user = null;
		try {
			String serverPath = commonService.getApiHost();
			log.info("ForgotPasswordController: serverPath {}", serverPath);
			user = forgotPasswordService.processForgotPassword(forgotPasswordRequestDTO.getEmail(),
															   commonService.getApiHost()
			);
			if (null != user) {
				isSuccess = true;
				User auth = new User();
				auth.setEmail(user.getEmail().toLowerCase());
				user = auth;
				return ResponseEntity.ok().body(new ServiceResponse(isSuccess, messageService.getMessage(
						"success_forgot_password"), user));
			} else {
				return ResponseEntity.badRequest().body(new ServiceResponse(isSuccess, messageService.getMessage(
						"error_email_not_exist"), null));
			}
		} catch (UnknownHostException e) {
			log.error("UnknownHostException", e);
			log.error("ForgotPasswordController: Mail can not be sent to {}", forgotPasswordRequestDTO.getEmail());
			return ResponseEntity.badRequest().body(new ServiceResponse(isSuccess, messageService.getMessage(
					"error_forgot_password"), null));
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
	 * incase of a invalid request appends the logError message with
	 * response code <tt>-14</tt>
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
							 .body(new ServiceResponse(isSuccess, messageService.getMessage("success_reset_password"),
													   user
							 ));
	}

	/**
	 * Change password.
	 *
	 * @param httpServletResponse the http servlet response
	 * @param request             the request
	 * @return the response entity
	 * @throws IOException      the io exception
	 * @throws ServletException the servlet exception
	 */
	@PostMapping(value = "/changePassword", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	// NOSONAR
	public ResponseEntity<ServiceResponse> changePassword(HttpServletResponse httpServletResponse, @Valid @RequestBody
	ChangePasswordRequestDTO request) { // NOSONAR
		try {
			Pattern pattern = Pattern.compile(CommonConstant.PASSWORD_PATTERN);
			Matcher matcher = pattern.matcher(request.getPassword());
			boolean flag = matcher.matches();
			boolean isEmailExist = userService.isEmailExist(request.getEmail().toLowerCase());
			boolean isPasswordIdentical = userService.isPasswordIdentical(request.getOldPassword(),
																		  request.getPassword()
			);
			if (isEmailExist) {
				if (flag) {
					if (!doesPasswordContainUsername(request.getPassword(), request.getUser())) {
						if (isPasswordIdentical) {
							return ResponseEntity.ok().body(new ServiceResponse(false, messageService.getMessage(
									"error_same_old_password"), null));
						} else {
							Optional<User> user = userService.findByUserName(request.getUser());
							boolean isValidUserCheck = user.get().checkPassword(request.getOldPassword());
							return isValidUser(isValidUserCheck, request, httpServletResponse);
						}
					} else {
						return ResponseEntity.ok().body(new ServiceResponse(false, messageService.getMessage(
								"error_password_contain"), null));
					}
				} else {
					return ResponseEntity.ok().body(new ServiceResponse(false, messageService.getMessage(
							"error_password_pattern"), null));
				}
			} else {
				return ResponseEntity.ok().body(new ServiceResponse(false,
																	messageService.getMessage("error_email_not_exist"),
																	null
				));
			}
		} catch (DuplicateKeyException dke) {
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new ServiceResponse(false,
																								   messageService.getMessage(
																										   "error_exception_unprocessable"),
																								   null
			));
		}
	}

	private boolean doesPasswordContainUsername(String reqPassword, String username) {
		return reqPassword.toLowerCase().contains(username.toLowerCase());
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
			Authentication authentication = userService.changePassword(request.getEmail(), request.getPassword());
			authenticationResponseService.handle(httpServletResponse, authentication);
			return ResponseEntity.ok().body(new ServiceResponse(true, getResponse(httpServletResponse), null));
		} else {
			return ResponseEntity.ok()
								 .body(new ServiceResponse(false, messageService.getMessage("error_wrong_password"),
														   null
								 ));
		}
	}

	private String getResponse(HttpServletResponse response) {
		JSONObject json = new JSONObject();
		json.put(CommonConstant.AUTH_RESPONSE_HEADER, response.getHeader(CommonConstant.AUTH_RESPONSE_HEADER));
		json.put(CommonConstant.STATUS, CommonConstant.STATUS);
		return json.toJSONString();
	}

	@GetMapping(value = "/validateEmailToken", produces = APPLICATION_JSON_VALUE) // NOSONAR
	public RedirectView validateToken(@RequestParam("token") UUID token) {
		log.info("ForgotPasswordController: requested token for validate {}", token);

		ResetPasswordTokenStatusEnum tokenStatus = forgotPasswordService.validateEmailToken(token.toString());

		String baseUiUrl = authConfigurationProperties.getBaseUiUrl();

		if (tokenStatus != null && tokenStatus.equals(ResetPasswordTokenStatusEnum.VALID)) {
			return new RedirectView(baseUiUrl + userInterfacePathsConfig.getUiResetPath() + token);
		} else {
			return new RedirectView(baseUiUrl + userInterfacePathsConfig.getUiResetPath() + tokenStatus);
		}
	}
}
