package com.publicissapient.kpidashboard.apis.auth.rest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.common.service.UserInfoService;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.constant.AuthType;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfoDTO;

@RestController
@RequestMapping("/sso")
public class SSOController {
	@Autowired
	private UserInfoService userInfoService;

	@PostMapping(value = "/users/{username}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> fetchOrSaveUserInfo(@PathVariable String username) {
		ServiceResponse response = new ServiceResponse(false, "Unauthorized", null);
		UserInfoDTO userInfoDTO = userInfoService.getOrSaveDefaultUserInfo(username, AuthType.SSO, null);
		if (null != userInfoDTO && userInfoDTO.getAuthType().equals(AuthType.SSO)) {
			response = new ServiceResponse(true, "Success", userInfoDTO);
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

}
