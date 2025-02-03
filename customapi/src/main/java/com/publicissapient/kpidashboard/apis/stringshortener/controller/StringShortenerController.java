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

import com.publicissapient.kpidashboard.apis.stringshortener.dto.StringShortenerDTO;
import com.publicissapient.kpidashboard.apis.stringshortener.model.StringShortener;
import com.publicissapient.kpidashboard.apis.stringshortener.service.StringShortenerService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/stringShortener")
public class StringShortenerController {

    private final StringShortenerService stringShortenerService;
    private static final String SHORT_STRING_RESPONSE_MESSAGE = "Successfully Created Short String";
    private static final String FAILURE_RESPONSE_MESSAGE = "Invalid URL.";
    private static final String FETCH_SUCCESS_MESSAGE = "Successfully fetched";
    private static final String MESSAGE = "message";
    private static final String DATA = "data";
    private static final String CODE = "code";
    private static final int SUCCESS_RESPONSE_CODE = 200;
    private static final int FAILURE_RESPONSE_CODE = 404;


    @Autowired
    private StringShortenerController(StringShortenerService stringShortenerService) {
        this.stringShortenerService=stringShortenerService;
    }

    @PostMapping("/shorten")
    public ResponseEntity<Map<String, Object>> createShortString(@RequestBody StringShortenerDTO stringShortenerDTO) {
        StringShortener stringShortener = stringShortenerService.createShortString(stringShortenerDTO);
        final ModelMapper modelMapper = new ModelMapper();
        final StringShortenerDTO responseDTO = modelMapper.map(stringShortener, StringShortenerDTO.class);
        Map<String, Object> response = new HashMap<>();
        response.put(CODE, SUCCESS_RESPONSE_CODE);
        response.put(DATA, responseDTO);
        response.put(MESSAGE, SHORT_STRING_RESPONSE_MESSAGE);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/longString")
    public ResponseEntity<Map<String, Object>> getLongString(@RequestParam String kpiFilters, @RequestParam String stateFilters) {
        Optional<StringShortener> stringShortener = stringShortenerService.getLongString(kpiFilters, stateFilters);
        Map<String, Object> response = new HashMap<>();
        if (stringShortener.isPresent()) {
            final ModelMapper modelMapper = new ModelMapper();
            final StringShortenerDTO responseDTO = modelMapper.map(stringShortener.get(), StringShortenerDTO.class);
            response.put(CODE, SUCCESS_RESPONSE_CODE);
            response.put(DATA, responseDTO);
            response.put(MESSAGE, FETCH_SUCCESS_MESSAGE);
            return ResponseEntity.ok(response);
        } else {
            response.put(CODE, FAILURE_RESPONSE_CODE);
            response.put(DATA, null);
            response.put(MESSAGE, FAILURE_RESPONSE_MESSAGE);
            return ResponseEntity.status(FAILURE_RESPONSE_CODE).body(response);
        }
    }
}