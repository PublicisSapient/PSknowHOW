package com.publicissapient.kpidashboard.apis.common.service;

import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.model.rbac.UsersLoginHistory;

public interface UserLoginHistoryService {

	UsersLoginHistory createUserLoginHistoryInfo(UserInfo userInfo, String status);
}
