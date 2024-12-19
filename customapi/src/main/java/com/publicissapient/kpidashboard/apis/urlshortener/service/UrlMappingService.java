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

package com.publicissapient.kpidashboard.apis.urlshortener.service;

import com.publicissapient.kpidashboard.apis.urlshortener.model.UrlMapping;
import com.publicissapient.kpidashboard.apis.urlshortener.repository.UrlMappingRepository;
import com.publicissapient.kpidashboard.apis.urlshortener.util.MurmurHash3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UrlMappingService {

    @Autowired
    private UrlMappingRepository urlMappingRepository;

    public UrlMapping createShortUrl(String longUrl) {
        Optional<UrlMapping> existingMapping = urlMappingRepository.findByLongUrl(longUrl);
        if (existingMapping.isPresent()) {
            return existingMapping.get();
        }

        String shortUrl = generateShortUrl(longUrl);
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setLongUrl(longUrl);
        urlMapping.setShortUrl(shortUrl);
        return urlMappingRepository.save(urlMapping);
    }

    public Optional<UrlMapping> getLongUrl(String shortUrl) {
        return urlMappingRepository.findByShortUrl(shortUrl);
    }

    private String generateShortUrl(String longUrl) {
        return Integer.toHexString(MurmurHash3.hash32x86(longUrl.getBytes()));
    }
}