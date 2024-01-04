package com.publicissapient.kpidashboard.apis.common.service;

import com.publicissapient.kpidashboard.common.model.rbac.UserTokenData;

import java.util.List;

public interface UserTokenService {

	UserTokenData findByUserToken(String token);

	void deleteByUserNameIn(List<String> users);

	List<UserTokenData> findAllByUserName(String username);

	void saveAll(List<UserTokenData> userTokenDataList);

	void deleteByUserName(String userName);
}
