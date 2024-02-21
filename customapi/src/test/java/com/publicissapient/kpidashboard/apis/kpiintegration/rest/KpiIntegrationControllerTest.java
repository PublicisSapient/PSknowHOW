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

package com.publicissapient.kpidashboard.apis.kpiintegration.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.kpiintegration.service.KpiIntegrationServiceImpl;
import com.publicissapient.kpidashboard.apis.util.RestAPIUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.pushdata.service.AuthExposeAPIService;

/**
 * @author kunkambl
 */
@RunWith(MockitoJUnitRunner.class)
public class KpiIntegrationControllerTest {

    @InjectMocks
    KpiIntegrationController kpiIntegrationController;

    @Mock
    KpiIntegrationServiceImpl maturityService;

    @Mock
    AuthExposeAPIService authExposeAPIService;

    @Mock
    RestAPIUtils restAPIUtils;

    @Mock
    CustomApiConfig customApiConfig;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Before
    public void setUp() {
        when(customApiConfig.getxApiKey()).thenReturn("testKey");
        when(restAPIUtils.decryptPassword("testKey")).thenReturn("valid-token");
    }

    @Test
    public void getMaturityValuesUnauthorized() {

        KpiRequest kpiRequest = new KpiRequest();
        when(httpServletRequest.getHeader("X-Api-Key")).thenReturn("invalid-token");
        ResponseEntity<List<KpiElement>> responseEntity = kpiIntegrationController.getMaturityValues(httpServletRequest,
                kpiRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().isEmpty());
        verify(maturityService, never()).getKpiResponses(any());
    }

    @Test
    public void testGetMaturityValuesSuccess() {
        KpiRequest kpiRequest = new KpiRequest();
        when(httpServletRequest.getHeader("X-Api-Key")).thenReturn("valid-token");
        when(maturityService.getKpiResponses(kpiRequest)).thenReturn(Collections.singletonList(new KpiElement()));

        ResponseEntity<List<KpiElement>> responseEntity = kpiIntegrationController.getMaturityValues(httpServletRequest, kpiRequest);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertFalse(responseEntity.getBody().isEmpty());
        verify(maturityService).getKpiResponses(kpiRequest);
    }

    @Test
    public void testGetMaturityValuesForbidden() {
        KpiRequest kpiRequest = new KpiRequest();
        when(httpServletRequest.getHeader("X-Api-Key")).thenReturn("valid-token");
        when(maturityService.getKpiResponses(kpiRequest)).thenReturn(Collections.singletonList(new KpiElement()));
        when(maturityService.getKpiResponses(kpiRequest)).thenReturn(Collections.emptyList());

        ResponseEntity<List<KpiElement>> responseEntity = kpiIntegrationController.getMaturityValues(httpServletRequest, kpiRequest);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().isEmpty());
        verify(maturityService).getKpiResponses(kpiRequest);
    }
}