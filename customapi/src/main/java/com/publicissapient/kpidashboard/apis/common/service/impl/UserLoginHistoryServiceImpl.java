package com.publicissapient.kpidashboard.apis.common.service.impl;

import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.common.service.UserLoginHistoryService;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.model.rbac.UsersLoginHistory;
import com.publicissapient.kpidashboard.common.repository.rbac.UserLoginHistoryRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UserLoginHistoryServiceImpl implements UserLoginHistoryService {

	@Autowired
	private UserLoginHistoryRepository userLoginHistoryRepository;

	@Override
	public UsersLoginHistory createUserLoginHistoryInfo(UserInfo userInfo, String status) {
		UsersLoginHistory usersLoginHistoryInfo = new UsersLoginHistory();
		usersLoginHistoryInfo.setUserId(userInfo.getId());
		usersLoginHistoryInfo.setUserName(userInfo.getUsername());
		usersLoginHistoryInfo.setEmailId(userInfo.getEmailAddress());
		usersLoginHistoryInfo.setLoginType(userInfo.getAuthType().toString());
		usersLoginHistoryInfo.setStatus(status);
		usersLoginHistoryInfo.setDateAndTime(String.valueOf(Calendar.getInstance().getTime()));
		return userLoginHistoryRepository.save(usersLoginHistoryInfo);
	}
}
