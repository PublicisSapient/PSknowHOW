package com.publicissapient.kpidashboard.apis.pushdata.controller;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.ExposeAPITokenRequestDTO;
import com.publicissapient.kpidashboard.apis.pushdata.service.AuthExposeAPIService;
import com.publicissapient.kpidashboard.common.util.EncryptionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/exposeAPI")
@Slf4j
public class ExposeAPIController {

	@Autowired
	private AuthExposeAPIService authExposeAPIService;

	@PreAuthorize("hasPermission(null, 'SAVE_PROJECT_TOOL')")
	@RequestMapping(value = "/generateToken", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> generateAndSaveToken(
			@RequestBody @Valid ExposeAPITokenRequestDTO exposeAPITokenRequestDTO) throws EncryptionException {
		return ResponseEntity.status(HttpStatus.OK)
				.body(authExposeAPIService.generateAndSaveToken(exposeAPITokenRequestDTO));
	}
}
