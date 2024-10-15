package com.publicissapient.kpidashboard.apis.service;

import com.publicissapient.kpidashboard.apis.entity.UserVerificationToken;
import com.publicissapient.kpidashboard.apis.enums.ResetPasswordTokenStatusEnum;

import javax.validation.Valid;
import java.util.UUID;

public interface UserVerificationTokenService {

	UserVerificationToken save(@Valid UserVerificationToken userVerificationToken);

	ResetPasswordTokenStatusEnum verifyUserToken(String token);

	void deleteUnVerifiedUser(UUID token);
}
