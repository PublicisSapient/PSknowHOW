package com.publicissapient.kpidashboard.apis.common.service.impl;

import javax.servlet.http.HttpServletResponse;

import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.publicissapient.kpidashboard.apis.abac.ProjectAccessManager;
import com.publicissapient.kpidashboard.apis.common.service.PushDataValidationService;
import com.publicissapient.kpidashboard.apis.pushdata.util.PushDataException;
import com.publicissapient.kpidashboard.common.model.rbac.UserTokenData;
import com.publicissapient.kpidashboard.common.repository.rbac.UserTokenReopository;

public class PushDataValidationServiceImpl implements PushDataValidationService {

	@Autowired
	UserTokenReopository userTokenReopository;

	@Autowired
	ProjectAccessManager projectAccessManager;

	@Autowired
	private AuthenticationService authenticationService;

	@Override
	public String validateToken(HttpServletResponse response) {
		String token = response.getHeader("");
		UserTokenData userTokenData = userTokenReopository.findByUserToken(token);
		if (userTokenData == null) {
			throw new PushDataException("Create Token To Push Data", HttpStatus.UNAUTHORIZED);
		}
		// checkUserDetails and its permission of the project
		// provide ObjectId of the project
		//projectid will be taken from token repository
		authenticationService.getLoggedInUser();

		if (!projectAccessManager.hasProjectEditPermission(new ObjectId("projectId"), userTokenData.getUserName() //if not user based, then loggedinuser
		)) {
			throw new PushDataException("Permission Denied", HttpStatus.UNAUTHORIZED);
		}
		return " ";//return the projectString
	}
}
