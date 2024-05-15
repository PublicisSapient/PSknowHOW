package com.publicissapient.kpidashboard.apis.service.impl;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import com.publicissapient.kpidashboard.apis.config.AuthConfig;
import com.publicissapient.kpidashboard.apis.service.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.service.UserRoleService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.entity.User;
import com.publicissapient.kpidashboard.apis.enums.AuthType;
import com.publicissapient.kpidashboard.apis.errors.PendingApprovalException;
import com.publicissapient.kpidashboard.apis.errors.UserNotFoundException;
import com.publicissapient.kpidashboard.apis.service.StandardAuthenticationService;
import com.publicissapient.kpidashboard.apis.service.UserService;
import com.publicissapient.kpidashboard.common.model.UserDTO;

import static com.publicissapient.kpidashboard.apis.constant.CommonConstant.WRONG_CREDENTIALS_ERROR_MESSAGE;

@Service
@AllArgsConstructor
public class StandardAuthenticationServiceImpl implements StandardAuthenticationService {

	private final AuthConfig authConfig;

	private final UserService userService;

	private final UserRoleService userRoleService;

	private final TokenAuthenticationService tokenAuthenticationService;

	@Override
	public Authentication authenticateUser(Authentication authentication)
			throws BadCredentialsException, LockedException, PendingApprovalException {
		String username = authentication.getPrincipal().toString();
		String password = authentication.getCredentials().toString();

		Optional<User> userOptional = userService.findByUserName(username);

		if (userOptional.isEmpty()) {
			throw new UserNotFoundException(username, AuthType.STANDARD);
		} else {
			User user = userOptional.get();

			if (hasUserReachedTheAttemptThreshold(user)) {
				if (isTheUserAccountStillLocked(user)) {
					throw new LockedException("Account Locked: Invalid Login Limit Reached " + username);
				} else {
					user = resetFailedLoginAttemptsCount(user.getUsername());
				}
			}

			if (!user.isUserVerified()) {
				throw new PendingApprovalException(
						"Login Failed: Your verification is pending. Please check your registered mail for verification");
			}

			if (!user.isApproved()) {
				throw new PendingApprovalException("Login Failed: Your access request is pending for approval");
			}

			UserDTO userDTO = userService.getUserDTO(user);

			if (user.checkPassword(password)) {
				return new UsernamePasswordAuthenticationToken(
						userDTO,
						user.getPassword(),
						this.tokenAuthenticationService.createAuthorities(
								this.userRoleService.getRolesNamesByUsername(username)
						)
				);
			} else {
				throw new BadCredentialsException(WRONG_CREDENTIALS_ERROR_MESSAGE);
			}
		}
	}

	private boolean hasUserReachedTheAttemptThreshold(User user) {
		return user.getFailedLoginAttemptCount() >= authConfig.getAccountLockedThreshold();
	}

	private boolean isTheUserAccountStillLocked(User user) {
		return Objects.nonNull(user.getLastUnsuccessfulLoginTime()) && LocalDateTime.now().isBefore(
				user.getLastUnsuccessfulLoginTime().plusMinutes(authConfig.getAccountLockedPeriod()));
	}

	private User resetFailedLoginAttemptsCount(String userName) {
		Optional<User> user = userService.findByUsername(userName);

		if (user.isPresent()) {
			user.get().setFailedLoginAttemptCount(0);
			user.get().setLastUnsuccessfulLoginTime(null);

			return userService.save(user.get());
		} else {
			return null;
		}
	}

	@Override
	public String addAuthentication(HttpServletResponse response, Authentication authentication) {
		String jwt = this.tokenAuthenticationService.createJWT(userService.getUsername(authentication),
															   AuthType.STANDARD, authentication.getAuthorities()
		);

		this.tokenAuthenticationService.addStandardCookies(jwt, response);

		return jwt;
	}

	@Override
	public void updateFailAttempts(String userName, LocalDateTime unsuccessfulLoginTime) {
		Optional<User> userOptional = userService.findByUsername(userName);

		if (userOptional.isPresent()) {
			User user = userOptional.get();

			user.setFailedLoginAttemptCount(user.getFailedLoginAttemptCount() + 1);
			user.setLastUnsuccessfulLoginTime(unsuccessfulLoginTime);

			userService.save(user);
		}
	}

}
