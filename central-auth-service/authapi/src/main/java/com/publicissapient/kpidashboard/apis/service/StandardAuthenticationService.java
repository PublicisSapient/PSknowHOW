package com.publicissapient.kpidashboard.apis.service;

import com.publicissapient.kpidashboard.apis.entity.User;
import com.publicissapient.kpidashboard.apis.enums.ResetPasswordTokenStatusEnum;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.common.model.ChangePasswordRequestDTO;
import com.publicissapient.kpidashboard.common.model.ResetPasswordRequestDTO;
import com.publicissapient.kpidashboard.common.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.UserDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;

public interface StandardAuthenticationService {
	Authentication authenticateUser(Authentication authentication);

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
	User resetPassword(ResetPasswordRequestDTO updatedPasswordRequest) throws ApplicationException;


	ServiceResponse changePassword(ChangePasswordRequestDTO request, HttpServletResponse response);


	ServiceResponse processForgotPassword(String email);
}
