package com.publicissapient.kpidashboard.apis.common.rest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.common.service.CustomAnalyticsService;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;

import lombok.extern.java.Log;

@Log
@RestController
public class AnalyticsController {

	@Autowired
	private CustomAnalyticsService customAnalyticsService;

	/**
	 * Gets logo image file
	 *
	 * @return Logo
	 */
	@GetMapping(value = "/analytics/switch", produces = APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> getAnalyticsSwitch() {
		log.info("Analytics Swtich API called");
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ServiceResponse(true, "Success", customAnalyticsService.getAnalyticsCheck()));
	}
}
