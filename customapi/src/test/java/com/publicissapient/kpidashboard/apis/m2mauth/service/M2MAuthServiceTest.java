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
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class M2MAuthServiceTest {

    @Mock
    private M2MAuthConfig m2MAuthConfig;

    @InjectMocks
    private M2MAuthService m2MAuthService;

    private final String secret = "testSecret";
    private final String issuerServiceId = "testIssuer";
    private final int duration = 3600; // 1 hour

    @BeforeEach
    public void setUp() {
        when(m2MAuthConfig.getSecret()).thenReturn(secret);
        when(m2MAuthConfig.getIssuerServiceId()).thenReturn(issuerServiceId);
        when(m2MAuthConfig.getDuration()).thenReturn(duration);
    }

    @Test
    void testGenerateServiceAuthToken() {
        String audience = "testAudience";
        String token = m2MAuthService.generateServiceAuthToken(audience);

        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();

        assertEquals(issuerServiceId, claims.getSubject());
        assertEquals(audience, claims.getAudience());
        assertEquals(issuerServiceId, claims.getIssuer());
        assertEquals(new Date().getTime() + duration * 1000, claims.getExpiration().getTime(), 1000);
    }
}
