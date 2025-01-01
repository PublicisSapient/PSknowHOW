package com.publicissapient.kpidashboard.apis.service;

import org.springframework.security.core.Authentication;

import com.publicissapient.kpidashboard.apis.service.dto.ChangePasswordRequestDTO;
import com.publicissapient.kpidashboard.apis.service.dto.ServiceResponseDTO;

public interface ForgotPasswordService {

	Authentication changePasswordAndReturnAuthentication(ChangePasswordRequestDTO requestDTO);

	/**
	 * Process forgotPassword request.
	 *
	 * <p>
	 * processForgotPassword checks whether the email in the ForgotPasswordRequest
	 * object exists in the database.If the email exists,creates a token for the
	 * user account and sends an email with token and reset url info
	 *
	 * @param email
	 * @return authentication
	 */
	ServiceResponseDTO validateUserAndSendForgotPasswordEmail(String email);
}
