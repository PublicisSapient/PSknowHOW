package com.publicissapient.kpidashboard.apis.auth.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.auth.service.AuthTypesConfigService;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.application.AuthTypeConfig;
import com.publicissapient.kpidashboard.common.model.application.AuthTypeStatus;

@RestController
public class AuthTypesConfigController {

	@Autowired
	private AuthTypesConfigService authTypesConfigService;

	private static final String ERROR_MSG="Something went wrong";

	@PostMapping("/auth-types")
	@PreAuthorize("hasPermission(null,'CONFIGURE_LOGIN_TYPE')")
	public ResponseEntity<ServiceResponse> addAuthTypesConfig(@RequestBody AuthTypeConfig authTypeConfig) {

		AuthTypeConfig savedAuthTypeConfig = authTypesConfigService.saveAuthTypeConfig(authTypeConfig);
		ServiceResponse serviceResponse = null;

		if (savedAuthTypeConfig == null) {
			serviceResponse = new ServiceResponse(false, ERROR_MSG, null);
		} else {
			if (savedAuthTypeConfig.getAdServerDetail() != null) {
				savedAuthTypeConfig.getAdServerDetail().setPassword("");
			}
			serviceResponse = new ServiceResponse(true, "Saved successfully", savedAuthTypeConfig);
		}

		return ResponseEntity.status(HttpStatus.OK).body(serviceResponse);
	}

	@GetMapping("/auth-types")
	@PreAuthorize("hasPermission(null,'GET_LOGIN_TYPES_CONFIG')")
	public ResponseEntity<ServiceResponse> getAuthTypesConfig() {
		ServiceResponse serviceResponse = null;
		AuthTypeConfig authTypeConfig = authTypesConfigService.getAuthTypeConfig();

		if (authTypeConfig == null) {
			serviceResponse = new ServiceResponse(false, ERROR_MSG, null);
		} else {
			if (authTypeConfig.getAdServerDetail() != null) {
				authTypeConfig.getAdServerDetail().setPassword("");
			}
			serviceResponse = new ServiceResponse(true, "types of authentication config", authTypeConfig);
		}

		return ResponseEntity.status(HttpStatus.OK).body(serviceResponse);
	}

	@GetMapping("/auth-types-status")
	public ResponseEntity<ServiceResponse> getAuthTypesStatus() {

		ServiceResponse serviceResponse = null;
		AuthTypeStatus authTypeStatus = authTypesConfigService.getAuthTypesStatus();
		if (authTypeStatus == null) {
			serviceResponse = new ServiceResponse(false, ERROR_MSG, null);
		} else {
			serviceResponse = new ServiceResponse(true, "auth types active status", authTypeStatus);
		}

		return ResponseEntity.status(HttpStatus.OK).body(serviceResponse);
	}

}
