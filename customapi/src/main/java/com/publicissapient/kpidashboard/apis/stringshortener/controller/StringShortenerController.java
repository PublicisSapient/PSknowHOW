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

import java.util.Optional;

@RestController
@RequestMapping("/stringShortener")
public class StringShortenerController {

    private final StringShortenerService stringShortenerService;

    @Autowired
    private StringShortenerController(StringShortenerService stringShortenerService) {
        this.stringShortenerService=stringShortenerService;
    }

    @PostMapping("/shorten")
    public ResponseEntity<StringShortenerDTO> createShortString(@RequestBody StringShortenerDTO stringShortenerDTO) {
        StringShortener stringShortener = stringShortenerService.createShortString(stringShortenerDTO);
        final ModelMapper modelMapper = new ModelMapper();
        final StringShortenerDTO responseDTO = modelMapper.map(stringShortener, StringShortenerDTO.class);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/longString")
    public ResponseEntity<StringShortenerDTO> getLongString(@RequestParam String kpiFilters,@RequestParam String stateFilters) {
        Optional<StringShortener> stringShortener = stringShortenerService.getLongString(kpiFilters,stateFilters);
        if (stringShortener.isPresent()) {
            final ModelMapper modelMapper = new ModelMapper();
            final StringShortenerDTO responseDTO = modelMapper.map(stringShortener.get(), StringShortenerDTO.class);
            return ResponseEntity.ok(responseDTO);
        }
        return ResponseEntity.notFound().build();
    }
}