package com.publicissapient.kpidashboard.apis.service;

import java.time.LocalDateTime;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;

import com.publicissapient.kpidashboard.apis.enums.ResetPasswordTokenStatusEnum;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.errors.PendingApprovalException;
import com.publicissapient.kpidashboard.apis.errors.UsernameNotFoundException;
import com.publicissapient.kpidashboard.apis.service.dto.ChangePasswordRequestDTO;
import com.publicissapient.kpidashboard.apis.service.dto.ResetPasswordRequestDTO;
import com.publicissapient.kpidashboard.apis.service.dto.ServiceResponseDTO;
import com.publicissapient.kpidashboard.apis.service.dto.UserDTO;

import jakarta.servlet.http.HttpServletResponse;

public interface StandardAuthenticationService {
	Authentication authenticateUser(Authentication authentication)
			throws BadCredentialsException, LockedException, PendingApprovalException, UsernameNotFoundException;

	String addAuthentication(HttpServletResponse response, Authentication authentication);

	void updateFailAttempts(String userName, LocalDateTime unsuccessAttemptTime);

	boolean registerUser(UserDTO request);

	/**
	 * Validate Email Token sent to the user via email.
	 *
	 * @param token
	 * @return ResetPasswordTokenStatusEnum
	 */
	ResetPasswordTokenStatusEnum validateEmailToken(String token);

	/**
	 * Reset password after validating token
	 *
	 * @param updatedPasswordRequest
	 * @return Authentication
	 * @throws ApplicationException
	 */
	UserDTO resetPassword(ResetPasswordRequestDTO updatedPasswordRequest) throws ApplicationException;

	ServiceResponseDTO changePassword(ChangePasswordRequestDTO request, HttpServletResponse response);

	ServiceResponseDTO processForgotPassword(String email);
}
