package com.publicissapient.kpidashboard.apis.common.service.impl;

import java.util.List;

import com.publicissapient.kpidashboard.apis.common.service.UserTokenService;
import com.publicissapient.kpidashboard.common.model.rbac.UserTokenData;

public class UserTokenServiceImpl implements UserTokenService {

	@Override
	public UserTokenData findByUserToken(String token) {
		// todo change
		return new UserTokenData();
	}

	@Override
	public void deleteByUserNameIn(List<String> users) {
		// todo change
	}

	@Override
	public List<UserTokenData> findAllByUserName(String username) {
		return null;
	}

	@Override
	public void saveAll(List<UserTokenData> userTokenDataList) {

	}

	@Override public void deleteByUserName(String userName) {

	}
}
