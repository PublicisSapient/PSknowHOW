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

package com.publicissapient.kpidashboard.apis.urlshortener.controller;

import com.publicissapient.kpidashboard.apis.urlshortener.dto.UrlMappingDTO;
import com.publicissapient.kpidashboard.apis.urlshortener.model.UrlMapping;
import com.publicissapient.kpidashboard.apis.urlshortener.service.UrlMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/url")
public class UrlMappingController {

    @Autowired
    private UrlMappingService urlMappingService;

    @PostMapping("/shorten")
    public ResponseEntity<UrlMappingDTO> createShortUrl(@RequestBody UrlMappingDTO urlMappingDTO) {
        UrlMapping urlMapping = urlMappingService.createShortUrl(urlMappingDTO.getLongUrl());
        UrlMappingDTO responseDTO = new UrlMappingDTO();
        responseDTO.setLongUrl(urlMapping.getLongUrl());
        responseDTO.setShortUrl(urlMapping.getShortUrl());
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<String> getLongUrl(@PathVariable String shortUrl) {
        Optional<UrlMapping> urlMapping = urlMappingService.getLongUrl(shortUrl);
        return urlMapping.map(mapping -> ResponseEntity.ok(mapping.getLongUrl()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}