package com.publicissapient.kpidashboard.apis.service.impl;

import static com.publicissapient.kpidashboard.apis.constant.CommonConstant.EMAIL_PATTERN;
import static com.publicissapient.kpidashboard.apis.constant.CommonConstant.WRONG_CREDENTIALS_ERROR_MESSAGE;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.config.AuthConfig;
import com.publicissapient.kpidashboard.apis.constant.CommonConstant;
import com.publicissapient.kpidashboard.apis.entity.ForgotPasswordToken;
import com.publicissapient.kpidashboard.apis.entity.User;
import com.publicissapient.kpidashboard.apis.entity.UserVerificationToken;
import com.publicissapient.kpidashboard.apis.enums.AuthType;
import com.publicissapient.kpidashboard.apis.enums.ResetPasswordTokenStatusEnum;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.errors.EmailNotFoundException;
import com.publicissapient.kpidashboard.apis.errors.GenericException;
import com.publicissapient.kpidashboard.apis.errors.IdenticalPasswordException;
import com.publicissapient.kpidashboard.apis.errors.PasswordContainsUsernameException;
import com.publicissapient.kpidashboard.apis.errors.PasswordPatternException;
import com.publicissapient.kpidashboard.apis.errors.PendingApprovalException;
import com.publicissapient.kpidashboard.apis.errors.UsernameNotFoundException;
import com.publicissapient.kpidashboard.apis.errors.WrongPasswordException;
import com.publicissapient.kpidashboard.apis.service.ForgotPasswordService;
import com.publicissapient.kpidashboard.apis.service.ForgotPasswordTokenService;
import com.publicissapient.kpidashboard.apis.service.MessageService;
import com.publicissapient.kpidashboard.apis.service.NotificationService;
import com.publicissapient.kpidashboard.apis.service.StandardAuthenticationService;
import com.publicissapient.kpidashboard.apis.service.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.service.UserRoleService;
import com.publicissapient.kpidashboard.apis.service.UserService;
import com.publicissapient.kpidashboard.apis.service.UserVerificationTokenService;
import com.publicissapient.kpidashboard.apis.service.dto.ChangePasswordRequestDTO;
import com.publicissapient.kpidashboard.apis.service.dto.ResetPasswordRequestDTO;
import com.publicissapient.kpidashboard.apis.service.dto.ServiceResponseDTO;
import com.publicissapient.kpidashboard.apis.service.dto.UserDTO;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Service
@Slf4j
public class StandardAuthenticationServiceImpl implements StandardAuthenticationService {

	private final AuthConfig authConfig;

	private final UserService userService;

	private final UserRoleService userRoleService;

	private final TokenAuthenticationService tokenAuthenticationService;

	private final MessageService messageService;

	private final NotificationService notificationService;

	private final UserVerificationTokenService userVerificationTokenService;

	private final ForgotPasswordTokenService forgotPasswordTokenService;

	private final ForgotPasswordService forgotPasswordService;

	@Override
	public Authentication authenticateUser(Authentication authentication)
			throws BadCredentialsException, LockedException, PendingApprovalException, UsernameNotFoundException {
		String username = authentication.getPrincipal().toString();
		String password = authentication.getCredentials().toString();

		Optional<User> userOptional = userService.findByUsername(username);

		if (userOptional.isEmpty()) {
			throw new UsernameNotFoundException(username, AuthType.STANDARD);
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
				return new UsernamePasswordAuthenticationToken(userDTO, user.getPassword(),
						this.tokenAuthenticationService.createAuthorities(this.userRoleService.getRolesNamesByUsername(username)));
			} else {
				throw new BadCredentialsException(WRONG_CREDENTIALS_ERROR_MESSAGE);
			}
		}
	}

	private boolean hasUserReachedTheAttemptThreshold(User user) {
		return user.getFailedLoginAttemptCount() >= authConfig.getAccountLockedThreshold();
	}

	private boolean isTheUserAccountStillLocked(User user) {
		return Objects.nonNull(user.getLastUnsuccessfulLoginTime()) && LocalDateTime.now()
				.isBefore(user.getLastUnsuccessfulLoginTime().plusMinutes(authConfig.getAccountLockedPeriod()));
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
		String jwt = this.tokenAuthenticationService.createJWT(
				this.tokenAuthenticationService.extractUsernameFromAuthentication(authentication), AuthType.STANDARD,
				authentication.getAuthorities());

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

	@Override
	public boolean registerUser(UserDTO request) {
		if (isRequestValid(request)) {
			User user = saveUserDetails(request);

			notificationService.sendVerificationMailToRegisterUser(user.getUsername(), user.getEmail(),
					createUserVerificationToken(user.getUsername(), user.getEmail()));

			return true;
		}

		return false;
	}

	private boolean isRequestValid(UserDTO request) throws GenericException {
		if (userService.findByUsername(request.getUsername()).isPresent()) {
			throw new GenericException("Cannot complete the registration process, Try with different username");
		}
		if (!Pattern.compile(EMAIL_PATTERN).matcher(request.getEmail()).matches()) {
			throw new GenericException("Cannot complete the registration process, Invalid Email");
		}
		if (this.userService.findByEmail(request.getEmail().toLowerCase()).isPresent()) {
			throw new GenericException("Cannot complete the registration process, Try with different email");
		}
		if (!Pattern.compile(CommonConstant.PASSWORD_PATTERN).matcher(request.getPassword()).matches()) {
			throw new GenericException(this.messageService.getMessage("error_register_password"));
		}
		if (!Pattern.compile(CommonConstant.USERNAME_PATTERN).matcher(request.getUsername()).matches()) {
			throw new GenericException(this.messageService.getMessage("error_register_username"));
		}

		return true;
	}

	/**
	 * Validates Email Token sent to the user via email.
	 *
	 * <p>
	 * validateEmailToken method checks the token received from request, exists in
	 * the database.If the token is found in the database method will forward the
	 * token to validate it
	 *
	 * @param token
	 * @return one of the enum <tt>INVALID, VALID, EXPIRED</tt> of type
	 *         ResetPasswordTokenStatusEnum
	 */
	@Override
	public ResetPasswordTokenStatusEnum validateEmailToken(String token) {
		Optional<ForgotPasswordToken> forgotPasswordTokenOptional = forgotPasswordTokenService.findByToken(token);
		if (forgotPasswordTokenOptional.isPresent()) {
			return checkTokenValidity(forgotPasswordTokenOptional.get());
		} else {
			return ResetPasswordTokenStatusEnum.INVALID;
		}
	}

	private User saveUserDetails(UserDTO request) {
		String username = request.getUsername();
		String password = request.getPassword();
		String email = request.getEmail().toLowerCase();
		String firstName = request.getFirstName();
		String lastName = request.getLastName();
		String displayName = request.getDisplayName();
		LocalDateTime createdDate = LocalDateTime.now();
		LocalDateTime modifiedDate = LocalDateTime.now();
		return this.userService.save(new User(username, password, firstName, lastName, displayName, email, createdDate,
				AuthType.STANDARD.name(), modifiedDate, false, false));
	}

	private String createUserVerificationToken(String username, String email) {
		String token = UUID.randomUUID().toString();

		UserVerificationToken userVerificationToken = new UserVerificationToken();
		userVerificationToken.setToken(token);
		userVerificationToken.setUsername(username);
		userVerificationToken.setExpiryDate(Integer.parseInt(authConfig.getVerifyUserTokenExpiryDays()));
		userVerificationToken.setEmail(email);

		this.userVerificationTokenService.save(userVerificationToken);

		return token;
	}

	/**
	 * Resets password after validating token
	 *
	 * <p>
	 * resetPassword checks if the reset token exists in the database.Later checks
	 * the validity of the token. If the token is valid,searches for the username
	 * from the <tt>forgotPasswordToken</tt> in the <tt>authentication</tt>
	 * collection in the database. Saves the reset password if the username exists
	 *
	 * @param resetPasswordRequest
	 * @return authentication if the <tt>token</tt> is valid and <tt>username</tt>
	 *         from forgotPasswordToken exists in the database
	 * @throws ApplicationException
	 *           if either <tt>forgotPasswordToken</tt> is invalid or
	 *           <tt>username</tt> doen't exist in the database.
	 */
	@Override
	public UserDTO resetPassword(ResetPasswordRequestDTO resetPasswordRequest) throws ApplicationException {
		Optional<ForgotPasswordToken> forgotPasswordTokenOptional = forgotPasswordTokenService
				.findByToken(resetPasswordRequest.getResetToken());
		if (forgotPasswordTokenOptional.isPresent()) {
			ForgotPasswordToken forgotPasswordToken = forgotPasswordTokenOptional.get();

			ResetPasswordTokenStatusEnum tokenStatus = checkTokenValidity(forgotPasswordToken);

			if (tokenStatus.equals(ResetPasswordTokenStatusEnum.VALID)) {
				Optional<User> user = userService.findByUsername(forgotPasswordToken.getUsername());
				if (user.isEmpty()) {
					log.error("User {} Does not Exist", forgotPasswordToken.getUsername());
					throw new ApplicationException("User Does not Exist", ApplicationException.BAD_DATA);
				} else {
					validatePasswordRules(forgotPasswordToken.getUsername(), resetPasswordRequest.getPassword(), user.get());
					return userService.getUserDTO(user.get());
				}
			} else {
				throw new ApplicationException("Token is " + tokenStatus.name(), ApplicationException.BAD_DATA);
			}
		} else {
			throw new ApplicationException("Token is " + ResetPasswordTokenStatusEnum.INVALID, ApplicationException.BAD_DATA);
		}
	}

	private boolean isPassContainUser(String reqPassword, String username) {

		return !(StringUtils.containsIgnoreCase(reqPassword, username));
	}

	private boolean isOldPassword(String reqPassword, String savedPassword) {

		return !(StringUtils.containsIgnoreCase(User.hash(reqPassword), savedPassword));
	}

	/**
	 * Checks the validity of <tt>forgotPasswordToken</tt>
	 *
	 * @param forgotPasswordToken
	 * @return ResetPasswordTokenStatusEnum <tt>INVALID</tt> if token is
	 *         <tt>null</tt>, <tt>VALID</tt> if token is not expired,
	 *         <tt>EXPIRED</tt> if token is expired
	 */
	private ResetPasswordTokenStatusEnum checkTokenValidity(ForgotPasswordToken forgotPasswordToken) {
		if (forgotPasswordToken == null) {
			return ResetPasswordTokenStatusEnum.INVALID;
		} else if (isExpired(forgotPasswordToken.getExpiryDate())) {
			return ResetPasswordTokenStatusEnum.EXPIRED;
		} else {
			return ResetPasswordTokenStatusEnum.VALID;
		}
	}

	/**
	 * Validates if the given <tt>expiryDate</tt> is in the past
	 *
	 * <p>
	 * isExpired method checks the validity of token by comparing the validity of
	 * token expriy date with current Time and Date
	 *
	 * @param expiryDate
	 * @return boolean <tt>true</tt> if expiryDate is invalid/expired,<tt>false</tt>
	 *         if token is valid
	 */
	private boolean isExpired(Date expiryDate) {
		return new Date().before(expiryDate);
	}

	private void validatePasswordRules(String username, String password, User user) throws ApplicationException {

		Pattern pattern = Pattern.compile(CommonConstant.PASSWORD_PATTERN);
		Matcher matcher = pattern.matcher(password);
		if (matcher.matches()) {
			if (isPassContainUser(password, username)) {
				if (isOldPassword(password, user.getPassword())) {
					user.setPassword(password);
					userService.save(user);
				} else {
					throw new ApplicationException("Password should not be old password", ApplicationException.BAD_DATA);
				}
			} else {
				throw new ApplicationException("Password should not contain userName", ApplicationException.BAD_DATA);
			}
		} else {
			throw new ApplicationException(
					"At least 8 characters in length with Lowercase letters, Uppercase letters, Numbers and Special characters($,@,$,!,%,*,?,&)",
					ApplicationException.BAD_DATA);
		}
	}

	@Override
	public ServiceResponseDTO changePassword(ChangePasswordRequestDTO changePasswordRequestDTO,
			HttpServletResponse response) {
		try {
			Authentication authentication = forgotPasswordService
					.changePasswordAndReturnAuthentication(changePasswordRequestDTO);

			addAuthentication(response, authentication);

			return new ServiceResponseDTO(true, messageService.getMessage("success_change_password"), null);
		} catch (EmailNotFoundException e) {
			return new ServiceResponseDTO(false, messageService.getMessage("error_invalid_user_email"), null);
		} catch (PasswordPatternException e) {
			return new ServiceResponseDTO(false, messageService.getMessage("error_password_pattern"), null);
		} catch (IdenticalPasswordException e) {
			return new ServiceResponseDTO(false, messageService.getMessage("error_same_old_password"), null);
		} catch (PasswordContainsUsernameException e) {
			new ServiceResponseDTO(false, messageService.getMessage("error_password_contain"), null);
		} catch (WrongPasswordException e) {
			new ServiceResponseDTO(false, messageService.getMessage("error_wrong_password"), null);
		}

		return null;
	}

	@Override
	public ServiceResponseDTO processForgotPassword(String email) {
		return forgotPasswordService.validateUserAndSendForgotPasswordEmail(email);
	}
}
