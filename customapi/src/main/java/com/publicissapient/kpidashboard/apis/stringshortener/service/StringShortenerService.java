/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.stringshortener.service;

import static com.publicissapient.kpidashboard.apis.stringshortener.util.UniqueShortKeyGenerator.generateShortKey;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.stringshortener.model.StringShortener;
import com.publicissapient.kpidashboard.apis.stringshortener.repository.StringShortenerRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class StringShortenerService {

    private final StringShortenerRepository stringShortenerRepository;

    @Autowired
    public StringShortenerService(StringShortenerRepository stringMappingRepository) {
        this.stringShortenerRepository = stringMappingRepository;
    }

    public StringShortener createShortString(String longString) {
        if (longString == null || longString.isEmpty()) {
            log.warn("Provided long string is null or empty");
            throw new IllegalArgumentException("Please provide a valid long string");
        }
        log.info("Creating short string for long string: {}", longString);
        Optional<StringShortener> existingMapping = stringShortenerRepository.findByLongString(longString);
        if (existingMapping.isPresent()) {
            log.info("Existing mapping found for long string: {}", longString);
            return existingMapping.get();
        }

        String shortstring = generateShortKey(longString);
        log.info("Generated short string: {} for long string: {}", shortstring, longString);
        StringShortener stringMapping = new StringShortener();
        stringMapping.setLongString(longString);
        stringMapping.setShortString(shortstring);
        StringShortener savedMapping = stringShortenerRepository.save(stringMapping);
        log.info("Successfully created and saved short string: {} for long string: {}", shortstring, longString);
        return savedMapping;
    }

    public Optional<StringShortener> getLongString(String shortstring) {
        log.info("Retrieving long string for short string: {}", shortstring);
        Optional<StringShortener> stringMapping = stringShortenerRepository.findByShortString(shortstring);
        if (stringMapping.isPresent()) {
            log.info("Found long string: {} for short string: {}", stringMapping.get().getLongString(), shortstring);
        } else {
            log.warn("No mapping found for short string: {}", shortstring);
        }
        return stringMapping;
    }
}