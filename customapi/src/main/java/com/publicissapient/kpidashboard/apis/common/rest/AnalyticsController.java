package com.publicissapient.kpidashboard.apis.common.rest;

import com.publicissapient.kpidashboard.apis.common.service.CustomAnalyticsService;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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
    @GetMapping(value = "/analytics/switch", produces = APPLICATION_JSON_VALUE)//NOSONAR
    public ResponseEntity<ServiceResponse> getAnalyticsSwitch() {
        log.info("Analytics Swtich API called");
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("http://localhost:8080/api/auth-types-status"));
        return new ResponseEntity<>(headers, HttpStatus.MOVED_TEMPORARILY);
//        return ResponseEntity.status(HttpStatus.MOVED_TEMPORARILY).body(new ServiceResponse(true, "Success", customAnalyticsService.getAnalyticsCheck()));
    }
}
