/*
 *  Copyright 2024 <Sapient Corporation>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and limitations under the
 *  License.
 */

package com.publicissapient.kpidashboard.apis.m2mauth.service;

import com.publicissapient.kpidashboard.apis.m2mauth.config.M2MAuthConfig;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@AllArgsConstructor
@Service
public class M2MAuthService {

    private final M2MAuthConfig m2MAuthConfig;

    public String generateServiceAuthToken(String audience) {
        return Jwts.builder()
                .setSubject(m2MAuthConfig.getIssuerServiceId())
                .setAudience(audience)
                .setIssuer(m2MAuthConfig.getIssuerServiceId())
                .setExpiration(Date.from(Instant.now().plusSeconds(m2MAuthConfig.getDuration())))
                .signWith(SignatureAlgorithm.HS512, m2MAuthConfig.getSecret())
                .compact();
    }
}
