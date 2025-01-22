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

import com.publicissapient.kpidashboard.apis.stringshortener.dto.StringShortenerDTO;
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

    public StringShortener createShortString(StringShortenerDTO stringShortenerDTO){
        if (stringShortenerDTO == null) {
            log.warn("Provided stringShortenerDTO is null");
            throw new IllegalArgumentException("Please provide a valid stringShortenerDTO");
        }
        Optional<StringShortener> stringShortenerOptional = stringShortenerRepository.findByLongKPIFiltersStringAndLongStateFiltersString(stringShortenerDTO.getLongKPIFiltersString(),stringShortenerDTO.getLongStateFiltersString());
        if (stringShortenerOptional.isPresent()) {
            log.info("Existing mapping found for long strings: {},{}", stringShortenerDTO.getLongKPIFiltersString(),stringShortenerDTO.getLongStateFiltersString());
            return stringShortenerOptional.get();
        }
        String shortKPIFiltersString = generateShortKey(stringShortenerDTO.getLongKPIFiltersString());
        String shortStateFiltersString = generateShortKey(stringShortenerDTO.getLongStateFiltersString());
        StringShortener stringMapping = new StringShortener();
        stringMapping.setLongKPIFiltersString(stringShortenerDTO.getLongKPIFiltersString());
        stringMapping.setShortKPIFilterString(shortKPIFiltersString);
        stringMapping.setLongStateFiltersString(stringShortenerDTO.getLongStateFiltersString());
        stringMapping.setShortStateFiltersString(shortStateFiltersString);

        StringShortener savedMapping = stringShortenerRepository.save(stringMapping);
        return savedMapping;
    }

    public Optional<StringShortener> getLongString(String kpiFilters, String stateFilters) {
        Optional<StringShortener> stringMapping = stringShortenerRepository.findByShortKPIFilterStringAndShortStateFiltersString(kpiFilters,stateFilters);
        return stringMapping;
    }
}