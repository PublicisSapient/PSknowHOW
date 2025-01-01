package com.publicissapient.kpidashboard.apis.service;

import java.util.UUID;
import javax.validation.Valid;

import com.publicissapient.kpidashboard.apis.entity.UserVerificationToken;
import com.publicissapient.kpidashboard.apis.enums.ResetPasswordTokenStatusEnum;

public interface UserVerificationTokenService {

	UserVerificationToken save(@Valid UserVerificationToken userVerificationToken);

	ResetPasswordTokenStatusEnum verifyUserToken(String token);

	void deleteUnVerifiedUser(UUID token);
}
