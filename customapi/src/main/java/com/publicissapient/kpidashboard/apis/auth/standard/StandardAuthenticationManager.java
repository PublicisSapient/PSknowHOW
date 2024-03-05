package com.publicissapient.kpidashboard.apis.auth.standard;

import com.publicissapient.kpidashboard.apis.auth.AuthProperties;
import com.publicissapient.kpidashboard.apis.auth.exceptions.PendingApprovalException;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class StandardAuthenticationManager implements AuthenticationManager {
	private final AuthenticationService authService;
	private final AuthProperties authProperties;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		try {
			Authentication auth = authService.authenticate(authentication.getName(),
					(String) authentication.getCredentials());
			authService.resetFailAttempts(authentication.getName());
			return auth;
		} catch (BadCredentialsException e) {
			DateTime now = DateTime.now(DateTimeZone.UTC);
			authService.updateFailAttempts(authentication.getName(), now);
			throw e;

		} catch (LockedException e) {
			String error = "User account is locked for " + authProperties.getAccountLockedPeriod() + " minutes";
			throw new LockedException(error, e);
		} catch (PendingApprovalException e) {
			throw new PendingApprovalException(e.getMessage());
		}
	}
}
