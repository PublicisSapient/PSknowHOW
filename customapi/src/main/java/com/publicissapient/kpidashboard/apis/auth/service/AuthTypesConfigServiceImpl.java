package com.publicissapient.kpidashboard.apis.auth.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.publicissapient.kpidashboard.apis.activedirectory.service.ADServerDetailsService;
import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.UserInfoService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.common.model.application.AuthTypeConfig;
import com.publicissapient.kpidashboard.common.model.application.AuthTypeStatus;
import com.publicissapient.kpidashboard.common.model.application.GlobalConfig;
import com.publicissapient.kpidashboard.common.repository.application.GlobalConfigRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;

@Service
public class AuthTypesConfigServiceImpl implements AuthTypesConfigService {

	@Autowired
	private ADServerDetailsService adServerDetailsService;

	@Autowired
	private GlobalConfigRepository globalConfigRepository;

	@Autowired
	private AesEncryptionService aesEncryptionService;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private TokenAuthenticationService tokenAuthenticationService;

	@Autowired
	private UserInfoService userInfoService;

	@Override
	public AuthTypeConfig getAuthTypeConfig() {
		GlobalConfig globalConfig = getGlobalConfig();

		AuthTypeConfig authTypeConfig = new AuthTypeConfig();

		if (globalConfig != null) {
			authTypeConfig.setAdServerDetail(globalConfig.getAdServerDetail());
			authTypeConfig.setAuthTypeStatus(globalConfig.getAuthTypeStatus());
		}

		return authTypeConfig;
	}

	@Override
	public AuthTypeStatus getAuthTypesStatus() {
		GlobalConfig globalConfig = getGlobalConfig();
		return globalConfig != null ? globalConfig.getAuthTypeStatus() : null;
	}

	private GlobalConfig getGlobalConfig() {
		List<GlobalConfig> globalConfigs = globalConfigRepository.findAll();
		return CollectionUtils.isEmpty(globalConfigs) ? null : globalConfigs.get(0);
	}
}
