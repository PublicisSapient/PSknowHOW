package com.publicissapient.kpidashboard.apis.service.impl;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.entity.User;
import com.publicissapient.kpidashboard.apis.entity.UserVerificationToken;
import com.publicissapient.kpidashboard.apis.enums.ResetPasswordTokenStatusEnum;
import com.publicissapient.kpidashboard.apis.repository.UserVerificationTokenRepository;
import com.publicissapient.kpidashboard.apis.service.NotificationService;
import com.publicissapient.kpidashboard.apis.service.UserService;
import com.publicissapient.kpidashboard.apis.service.UserVerificationTokenService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class UserVerificationTokenServiceImpl implements UserVerificationTokenService {

	private final UserVerificationTokenRepository userVerificationTokenRepository;

	private final UserService userService;

	private final NotificationService notificationService;

	@Override
	public UserVerificationToken save(UserVerificationToken userVerificationToken) {
		return userVerificationTokenRepository.save(userVerificationToken);
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
	public ResetPasswordTokenStatusEnum verifyUserToken(String token) {
		UserVerificationToken userVerificationToken = userVerificationTokenRepository.findByToken(token);
		return checkTokenValidity(userVerificationToken);
	}

	/**
	 * Checks the validity of <tt>userVerificationToken</tt>
	 *
	 * @param userVerificationToken
	 * @return ResetPasswordTokenStatusEnum <tt>INVALID</tt> if token is
	 *         <tt>null</tt>, <tt>VALID</tt> if token is not expired,
	 *         <tt>EXPIRED</tt> if token is expired
	 */
	private ResetPasswordTokenStatusEnum checkTokenValidity(UserVerificationToken userVerificationToken) {
		if (userVerificationToken == null) {
			return ResetPasswordTokenStatusEnum.INVALID;
		} else if (new Date().after(userVerificationToken.getExpiryDate())) {
			return ResetPasswordTokenStatusEnum.EXPIRED;
		} else {
			Optional<User> user = userService.findByUsername(userVerificationToken.getUsername());
			if (user.isPresent()) {
				User userData = user.get();
				userData.setUserVerified(true);
				userService.save(userData);
				notificationService.sendUserPreApprovalRequestEmailToAdmin(userData.getUsername(), userData.getEmail());
			}
			return ResetPasswordTokenStatusEnum.VALID;
		}
	}

	/**
	 * delete user whose verification not completed before verification token
	 * expiration
	 *
	 * @param token
	 */
	@Override
	public void deleteUnVerifiedUser(UUID token) {
		UserVerificationToken userVerificationToken = userVerificationTokenRepository.findByToken(token.toString());

		if (userVerificationToken.getUsername() != null && userVerificationToken.getEmail() != null) {
			this.notificationService.sendVerificationFailedMailUser(userVerificationToken.getUsername(),
					userVerificationToken.getEmail());
		}
	}
}
