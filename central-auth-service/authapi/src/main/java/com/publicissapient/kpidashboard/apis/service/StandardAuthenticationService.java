package com.publicissapient.kpidashboard.apis.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;

public interface StandardAuthenticationService {
	Authentication authenticateUser(Authentication authentication);

	/**
	 * Add authentication.
	 *
	 * @param response       the response
	 * @param authentication the authentication
	 */
	String addAuthentication(HttpServletResponse response, Authentication authentication);

	/**
	 * update failed attempt and date
	 *
	 * @param userName             userName
	 * @param unsuccessAttemptTime unsuccessAttemptTime
	 */
	void updateFailAttempts(String userName, LocalDateTime unsuccessAttemptTime);


}
