package com.publicissapient.kpidashboard.apis.auth.service;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.common.activedirectory.modal.ADServerDetail;
import com.publicissapient.kpidashboard.common.model.application.AuthTypeConfig;
import com.publicissapient.kpidashboard.common.model.application.AuthTypeStatus;
import com.publicissapient.kpidashboard.common.model.application.ValidationMessage;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.repository.rbac.UserInfoRepository;

@Service
public class AuthTypeConfigValidator {

	private static final String AUTH_TYPE_AD_LOGIN = "LDAP";
	private static final String AUTH_TYPE_STANDARD = "STANDARD";

	@Autowired
	private UserInfoRepository userInfoRepository;

	public ValidationMessage validateConfig(AuthTypeConfig authTypeConfig) {
		ValidationMessage validationMessage = new ValidationMessage();
		AuthTypeStatus authTypeStatus = authTypeConfig.getAuthTypeStatus();
		if (!authTypeStatus.isStandardLogin() && !authTypeStatus.isAdLogin()) {
			validationMessage.setValid(false);
			validationMessage.setMessage("At least one login method should be enabled");
		} else if (authTypeStatus.isStandardLogin() && !authTypeStatus.isAdLogin()) {
			if (hasSuperAdminUserOfType(AUTH_TYPE_STANDARD)) {
				validationMessage.setValid(true);
			} else {
				validationMessage.setValid(false);
				validationMessage.setMessage("At least one SUPERADMIN user of Standard type should exists");
			}

		} else if (!authTypeStatus.isStandardLogin() && authTypeStatus.isAdLogin()) {
			ValidationMessage validationMessageAdConfig = validateAdServerConfig(authTypeConfig.getAdServerDetail());
			if (!validationMessageAdConfig.isValid()) {
				validationMessage.setValid(false);
				validationMessage.setMessage(validationMessageAdConfig.getMessage());
			} else if (!hasSuperAdminUserOfType(AUTH_TYPE_AD_LOGIN)) {
				validationMessage.setValid(false);
				validationMessage.setMessage("At least one SUPERADMIN user of AD type should exists");
			} else {
				validationMessage.setValid(true);
			}
		} else if (authTypeStatus.isStandardLogin() && authTypeStatus.isAdLogin()) {
			ValidationMessage validationMessageAdConfig = validateAdServerConfig(authTypeConfig.getAdServerDetail());
			if (!validationMessageAdConfig.isValid()) {
				validationMessage.setValid(false);
				validationMessage.setMessage(validationMessageAdConfig.getMessage());
			} else {
				validationMessage.setValid(true);
			}
		} else {
			validationMessage.setValid(true);
		}
		return validationMessage;
	}

	private boolean hasSuperAdminUserOfType(String authType) {
		List<UserInfo> users = userInfoRepository.findByAuthTypeAndAuthoritiesIn(authType,
				Arrays.asList(Constant.ROLE_SUPERADMIN));

		return users.size() > 0;
	}

	private ValidationMessage validateAdServerConfig(ADServerDetail adServerDetail) {
		ValidationMessage validationMessage = new ValidationMessage();
		validationMessage.setValid(true);
		if (adServerDetail == null) {
			validationMessage.setValid(false);
			validationMessage.setMessage("Please provide AD config");
			return validationMessage;
		}

		if (StringUtils.isEmpty(adServerDetail.getUsername())) {
			validationMessage.setValid(false);
			validationMessage.setMessage("username can't be empty");
			return validationMessage;
		}
		if (adServerDetail.getPort() <= 0) {
			validationMessage.setValid(false);
			validationMessage.setMessage("Invalid port");
			return validationMessage;
		}
		if (StringUtils.isEmpty(adServerDetail.getPassword())) {
			validationMessage.setValid(false);
			validationMessage.setMessage("Password can't be empty");
			return validationMessage;
		}

		if (StringUtils.isEmpty(adServerDetail.getRootDn())) {
			validationMessage.setValid(false);
			validationMessage.setMessage("RootDN can't be empty");
			return validationMessage;
		}
		if (StringUtils.isEmpty(adServerDetail.getHost())) {
			validationMessage.setValid(false);
			validationMessage.setMessage("Host can't be empty");
			return validationMessage;
		}
		if (StringUtils.isEmpty(adServerDetail.getDomain())) {
			validationMessage.setValid(false);
			validationMessage.setMessage("Domain can't be empty");
			return validationMessage;
		}

		return validationMessage;
	}
}
