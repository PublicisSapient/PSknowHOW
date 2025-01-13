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
        StringShortener stringShortener = stringShortenerService.createShortString(stringShortenerDTO.getLongString());
        StringShortenerDTO responseDTO = new StringShortenerDTO();
        responseDTO.setLongString(stringShortener.getLongString());
        responseDTO.setShortString(stringShortener.getShortString());
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/longString")
    public ResponseEntity<String> getLongString(@RequestParam String shortString) {
        Optional<StringShortener> stringShortener = stringShortenerService.getLongString(shortString);
        if (stringShortener.isPresent()) {
            return stringShortener.map(mapping -> ResponseEntity.ok(mapping.getLongString()))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        }
        return ResponseEntity.notFound().build();
    }
}