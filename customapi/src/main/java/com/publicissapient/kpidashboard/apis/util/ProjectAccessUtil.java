package com.publicissapient.kpidashboard.apis.util;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.impl.UserInfoServiceImpl;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Component
public class ProjectAccessUtil {
	@Autowired
	private TokenAuthenticationService tokenAuthenticationService;
	@Autowired
	private UserAuthorizedProjectsService userAuthorizedProjectsService;
	@Autowired
	private UserInfoServiceImpl userInfoService;

	@Autowired
	private AuthenticationService authenticationService;

	public boolean configIdHasUserAccess(String basicConfigId) {
		Set<String> basicProjectConfigIds = tokenAuthenticationService.getUserProjects();
		return userAuthorizedProjectsService.ifSuperAdminUser()
				|| (Optional.ofNullable(basicProjectConfigIds).isPresent()
						&& basicProjectConfigIds.contains(basicConfigId));
	}

	public boolean ifConnectionNotAccessible(Connection connection) {
		return connection.isConnPrivate()
				&& (!(connection.getCreatedBy().equals(authenticationService.getLoggedInUser())
						|| userAuthorizedProjectsService.ifSuperAdminUser()));
	}

}
