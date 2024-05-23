package com.publicissapient.kpidashboard.apis.controller;

import javax.validation.Valid;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.UUID;

import com.publicissapient.kpidashboard.apis.service.dto.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.publicissapient.kpidashboard.apis.config.AuthConfig;
import com.publicissapient.kpidashboard.apis.config.UserInterfacePathsConfig;
import com.publicissapient.kpidashboard.apis.entity.User;
import com.publicissapient.kpidashboard.apis.enums.ResetPasswordTokenStatusEnum;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.service.*;

import static com.publicissapient.kpidashboard.apis.constant.CommonConstant.*;

@RestController
@AllArgsConstructor
@Slf4j
public class StandardAuthenticationController {

	private final MessageService messageService;

	private final UserApprovalService userApprovalService;

	private final AuthConfig authConfig;

	private final UserInterfacePathsConfig userInterfacePathsConfig;

	private final StandardAuthenticationService standardAuthenticationService;

	private final UserVerificationTokenService userVerificationTokenService;

	@PostMapping(value = "/register-user", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponseDTO> registerUser(@Valid @RequestBody UserDTO request) {
		boolean isSuccess = standardAuthenticationService.registerUser(request);

		return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponseDTO(isSuccess, isSuccess ?
				messageService.getMessage(SUCCESS_SENT_APPROVAL) :
				messageService.getMessage(ERROR_REGISTER_AGAIN), request.getUsername()));
	}

	@GetMapping("/user-approvals/pending")
	public ResponseEntity<ServiceResponseDTO> getAllUnapprovedUsers() {
		return ResponseEntity.status(HttpStatus.OK)
							 .body(new ServiceResponseDTO(true, messageService.getMessage("success_pending_approval"),
														  userApprovalService.findAllUnapprovedUsers()
							 ));
	}

	@PutMapping(value = "/approve/{username}", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponseDTO> approveUserCreationRequest(@PathVariable("username") String username) {
		boolean isSuccess = userApprovalService.approveUser(username);

		return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponseDTO(isSuccess, isSuccess ?
				messageService.getMessage("success_request_approve") :
				messageService.getMessage("error_request_approve"), isSuccess));

	}

	@GetMapping(value = "/reject/{username}", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponseDTO> deleteUser(@PathVariable("username") String username) {
		boolean isSuccess = userApprovalService.rejectUser(username);
		return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponseDTO(isSuccess, isSuccess ?
				messageService.getMessage("rejected_user_deleted") :
				messageService.getMessage("error_delete_user"), isSuccess));

	}

	@PostMapping(value = "/forgot-password", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponseDTO> processForgotPassword(
			@RequestBody ForgotPasswordRequestDTO forgotPasswordRequestDTO) {
		return ResponseEntity.ok().body(standardAuthenticationService.processForgotPassword(forgotPasswordRequestDTO.getEmail()));
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
	public ResponseEntity<ServiceResponseDTO> resetPassword(@RequestBody ResetPasswordRequestDTO updatedPasswordRequest) {
		boolean isSuccess = false;
		log.info("ForgotPasswordController: requested token for update {}", updatedPasswordRequest.getResetToken());
		User user = null;
		try {
			user = standardAuthenticationService.resetPassword(updatedPasswordRequest);
			if (null != user) {
				isSuccess = true;
				User auth = new User();
				auth.setEmail(user.getEmail().toLowerCase());
				user = auth;
			}
		} catch (ApplicationException e) {
			log.error("Error in ForgotPasswordController: resetPassword()", e);
			return ResponseEntity.badRequest().body(new ServiceResponseDTO(isSuccess, e.getMessage(), null));
		}
		return ResponseEntity.ok()
							 .body(new ServiceResponseDTO(isSuccess, messageService.getMessage("success_reset_password"),
														  user
							 ));
	}

	/**
	 * Change password.
	 *
	 * @param response the http servlet response
	 * @param request  the request
	 * @return the response entity
	 * @throws IOException      the io exception
	 * @throws ServletException the servlet exception
	 */
	@PostMapping(value = "/changePassword", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponseDTO> changePassword(@Valid @RequestBody ChangePasswordRequestDTO request,
															 HttpServletResponse response) { // NOSONAR
		return ResponseEntity.ok().body(standardAuthenticationService.changePassword(request, response));
	}



	@GetMapping(value = "/validateEmailToken", produces = APPLICATION_JSON_VALUE) // NOSONAR
	public RedirectView validateToken(@RequestParam("token") UUID token) {
		log.info("ForgotPasswordController: requested token for validate {}", token);

		ResetPasswordTokenStatusEnum tokenStatus = standardAuthenticationService.validateEmailToken(token.toString());

		String baseUiUrl = authConfig.getBaseUiUrl();

		if (tokenStatus != null && tokenStatus.equals(ResetPasswordTokenStatusEnum.VALID)) {
			return new RedirectView(baseUiUrl + userInterfacePathsConfig.getUiResetPath() + token);
		} else {
			return new RedirectView(baseUiUrl + userInterfacePathsConfig.getUiResetPath() + tokenStatus);
		}
	}

	/**
	 * api to verify user
	 *
	 * @param token
	 * @return
	 * @throws UnknownHostException
	 */
	@GetMapping(value = "/verifyUser", produces = APPLICATION_JSON_VALUE) // NOSONAR
	public RedirectView verifyUser(@RequestParam("token") UUID token) {
		log.info("UserController: requested token for validate {}", token);

		ResetPasswordTokenStatusEnum tokenStatus = userVerificationTokenService.verifyUserToken(token.toString());
		String serverPath = authConfig.getBaseUiUrl();

		if (tokenStatus != null && tokenStatus.equals(ResetPasswordTokenStatusEnum.VALID)) {
			return new RedirectView(serverPath);
		} else {
			userVerificationTokenService.deleteUnVerifiedUser(token);
			return new RedirectView(serverPath + userInterfacePathsConfig.getRegisterPath());
		}
	}
}
