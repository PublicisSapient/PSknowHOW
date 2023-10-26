package com.publicissapient.kpidashboard.apis.appsetting.rest;

import com.publicissapient.kpidashboard.apis.appsetting.service.GlobalFieldsServiceImpl;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/globalfields")
@Slf4j
public class GlobalFieldsController {

    @Autowired
    GlobalFieldsServiceImpl globalFieldsService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServiceResponse> getGlobalFields() {
        return ResponseEntity.status(HttpStatus.OK).body(globalFieldsService.getGlobalFields());
    }

}
