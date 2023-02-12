package com.publicissapient.kpidashboard.apis.common.service.impl;

import javax.servlet.http.HttpServletRequest;

import com.publicissapient.kpidashboard.apis.pushdata.model.ExposeApiToken;
import com.publicissapient.kpidashboard.apis.pushdata.repository.ExposeApiTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.publicissapient.kpidashboard.apis.abac.ProjectAccessManager;
import com.publicissapient.kpidashboard.apis.common.service.PushDataValidationService;
import com.publicissapient.kpidashboard.apis.pushdata.util.PushDataException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Slf4j
public class PushDataValidationServiceImpl implements PushDataValidationService {
//ValidateAPIKey
	@Autowired
	private ExposeApiTokenRepository exposeApiTokenRepository;

	@Autowired
	ProjectAccessManager projectAccessManager;

	@Override
	public ExposeApiToken validateToken(HttpServletRequest response) {
		String token = response.getHeader("Push-Api");
		ExposeApiToken exposeApiToken = exposeApiTokenRepository.findByApiToken(token);
		if (exposeApiToken == null) {
			throw new PushDataException("Create Token To Push Data", HttpStatus.UNAUTHORIZED);
		}

		checkExpiryToken(exposeApiToken);
		checkProjectAccessPermission(exposeApiToken);
		exposeApiToken.setExpiryDate(exposeApiToken.getExpiryDate().plusDays(30));
		exposeApiToken.setUpdatedAt(LocalDate.now());
		return exposeApiToken;
	}

	private void checkProjectAccessPermission(ExposeApiToken exposeApiToken) {
		if (!projectAccessManager.hasProjectEditPermission(exposeApiToken.getBasicProjectConfigId(), exposeApiToken.getUserName() //if not user based, then loggedinuser
		)) {
			throw new PushDataException("Permission Denied", HttpStatus.UNAUTHORIZED);
		}
	}

	private void checkExpiryToken(ExposeApiToken exposeApiToken) {
		if(exposeApiToken.getExpiryDate().isBefore(LocalDate.now())){
			throw new PushDataException("Token Expired", HttpStatus.UNAUTHORIZED);
		}
	}
}
