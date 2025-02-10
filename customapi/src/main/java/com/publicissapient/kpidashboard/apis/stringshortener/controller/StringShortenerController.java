/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.stringshortener.controller;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.stringshortener.dto.StringShortenerDTO;
import com.publicissapient.kpidashboard.apis.stringshortener.model.StringShortener;
import com.publicissapient.kpidashboard.apis.stringshortener.service.StringShortenerService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/stringShortener")
public class StringShortenerController {

    private final StringShortenerService stringShortenerService;
    private static final String SHORT_STRING_RESPONSE_MESSAGE = "Successfully Created Short String";
    private static final String FAILURE_RESPONSE_MESSAGE = "Invalid URL.";
    private static final String FETCH_SUCCESS_MESSAGE = "Successfully fetched";


    @Autowired
    private StringShortenerController(StringShortenerService stringShortenerService) {
        this.stringShortenerService=stringShortenerService;
    }

    @PostMapping("/shorten")
    public ResponseEntity<ServiceResponse> createShortString(@RequestBody StringShortenerDTO stringShortenerDTO) {
        ServiceResponse response = null;
        StringShortener stringShortener = stringShortenerService.createShortString(stringShortenerDTO);
        final ModelMapper modelMapper = new ModelMapper();
        final StringShortenerDTO responseDTO = modelMapper.map(stringShortener, StringShortenerDTO.class);
        if (responseDTO != null && !responseDTO.toString().isEmpty()) {
            response = new ServiceResponse(true, SHORT_STRING_RESPONSE_MESSAGE, responseDTO);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            response = new ServiceResponse(false, FAILURE_RESPONSE_MESSAGE, null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/longString")
    public ResponseEntity<ServiceResponse> getLongString(@RequestParam String kpiFilters, @RequestParam String stateFilters) {
        ServiceResponse response = null;
        Optional<StringShortener> stringShortener = stringShortenerService.getLongString(kpiFilters, stateFilters);
        if (stringShortener.isPresent()) {
            final ModelMapper modelMapper = new ModelMapper();
            final StringShortenerDTO responseDTO = modelMapper.map(stringShortener.get(), StringShortenerDTO.class);
            response = new ServiceResponse(true, FETCH_SUCCESS_MESSAGE, responseDTO);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            response = new ServiceResponse(false, FAILURE_RESPONSE_MESSAGE, null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}