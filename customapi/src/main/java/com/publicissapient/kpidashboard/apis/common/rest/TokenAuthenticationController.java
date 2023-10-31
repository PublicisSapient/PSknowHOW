package com.publicissapient.kpidashboard.apis.common.rest;

import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.common.UserTokenAuthenticationDTO;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Collection;

@RestController
public class TokenAuthenticationController {

	public static final String AUTH_DETAILS_UPDATED_FLAG = "auth-details-updated";

	@Autowired
	private TokenAuthenticationService tokenAuthenticationService;

	@PostMapping(value = "/validateToken")
	public ResponseEntity<ServiceResponse> validateToken(
			@Valid @RequestBody UserTokenAuthenticationDTO tokenAuthenticationDTO, HttpServletRequest request,
			HttpServletResponse response) {
		Authentication authentication = tokenAuthenticationService.getAuthentication(tokenAuthenticationDTO, request,
				response);
		ServiceResponse serviceResponse;
		if (null != authentication) {
			Collection<String> authDetails = response.getHeaders(AUTH_DETAILS_UPDATED_FLAG);
			boolean value = authDetails != null && authDetails.stream().anyMatch("true"::equals);
			if (value) {
				serviceResponse = new ServiceResponse(true, "success_valid_token", null);
			} else {
				serviceResponse = new ServiceResponse(false, "token is expired", null);
			}
		} else {
			serviceResponse = new ServiceResponse(false, "Unauthorized", null);
		}
		return ResponseEntity.status(HttpStatus.OK).body(serviceResponse);
	}
}
