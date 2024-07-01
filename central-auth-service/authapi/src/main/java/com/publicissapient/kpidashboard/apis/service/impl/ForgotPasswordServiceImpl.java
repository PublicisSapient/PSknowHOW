package com.publicissapient.kpidashboard.apis.service.impl;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.config.ForgotPasswordConfig;
import com.publicissapient.kpidashboard.apis.constant.CommonConstant;
import com.publicissapient.kpidashboard.apis.entity.ForgotPasswordToken;
import com.publicissapient.kpidashboard.apis.entity.User;
import com.publicissapient.kpidashboard.apis.enums.AuthType;
import com.publicissapient.kpidashboard.apis.errors.*;
import com.publicissapient.kpidashboard.apis.service.*;
import com.publicissapient.kpidashboard.apis.service.dto.ChangePasswordRequestDTO;
import com.publicissapient.kpidashboard.apis.service.dto.ServiceResponseDTO;


@AllArgsConstructor
@Service
@Slf4j
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

	private final ForgotPasswordConfig forgotPasswordConfig;

	private final UserService userService;

	private final ForgotPasswordTokenService forgotPasswordTokenService;

	private final MessageService messageService;

	private final NotificationService notificationService;

	@Override
	public Authentication changePasswordAndReturnAuthentication(ChangePasswordRequestDTO requestDTO) {
		return changePassword(requestDTO.getEmail(), requestDTO.getOldPassword(), requestDTO.getPassword());
	}

	private Authentication changePassword(String email, String oldPassword, String newPassword)
			throws PasswordContainsUsernameException, IdenticalPasswordException, PasswordPatternException,
			WrongPasswordException, EmailNotFoundException {
		Optional<User> userOptional = userService.findByEmail(email);

		if (userOptional.isPresent()) {
			User user = userOptional.get();

			if (isOldPasswordCorrect(user, oldPassword)) {
				if (isPasswordPatternValid(newPassword)) {
					if (isPasswordDifferent(user.getPassword(), newPassword)) {
						if (!doesPasswordContainUsername(newPassword, user.getUsername())) {
							return saveNewPasswordAndReturnAuthenticationToken(user, newPassword);
						} else {
							throw new PasswordContainsUsernameException();
						}
					} else {
						throw new IdenticalPasswordException();
					}
				} else {
					throw new PasswordPatternException();
				}
			} else {
				throw new WrongPasswordException();
			}
		} else {
			throw new EmailNotFoundException(email, AuthType.STANDARD);
		}
	}

	private Authentication saveNewPasswordAndReturnAuthenticationToken(User user, String newPassword) {
		user.setPassword(newPassword);
		User savedUser = userService.save(user);

		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
				savedUser.getUsername(),
				savedUser.getPassword(),
				new ArrayList<>()
		);

		token.setDetails(AuthType.STANDARD);

		return token;
	}

	private boolean isPasswordDifferent(String oldPassword, String newPassword) {
		return oldPassword.equals(newPassword);
	}

	private boolean isPasswordPatternValid(String password) {
		return Pattern.compile(CommonConstant.PASSWORD_PATTERN).matcher(password).matches();
	}

	private boolean doesPasswordContainUsername(String newPassword, String username) {
		return newPassword.toLowerCase().contains(username.toLowerCase());
	}

	private boolean isOldPasswordCorrect(User user, String oldPassword) {
		return user.checkPassword(oldPassword);
	}


	@Override
	public ServiceResponseDTO validateUserAndSendForgotPasswordEmail(String email) {
		Optional<User> userOptional = userService.findByEmail(email);

		if (userOptional.isPresent()) {
			User user = userOptional.get();

			String token = createForgetPasswordToken(user);

			notificationService.sendRecoverPasswordEmail(email, user.getUsername(), token);

			return new ServiceResponseDTO(true, messageService.getMessage(
					"success_forgot_password"), userService.getUserDTO(user));
		}

		return new ServiceResponseDTO(false, messageService.getMessage(
				"error_email_not_exist"), null);
	}

	private String createForgetPasswordToken(User user) {
		String token = UUID.randomUUID().toString();

		ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken();
		forgotPasswordToken.setToken(token);
		forgotPasswordToken.setUsername(user.getUsername());
		forgotPasswordToken.setExpiryDate(forgotPasswordConfig.getExpiryInterval());

		forgotPasswordTokenService.save(forgotPasswordToken);

		return token;
	}
}
