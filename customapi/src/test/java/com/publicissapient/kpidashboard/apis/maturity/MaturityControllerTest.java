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

package com.publicissapient.kpidashboard.apis.maturity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

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
import com.publicissapient.kpidashboard.apis.pushdata.model.ExposeApiToken;
import com.publicissapient.kpidashboard.apis.pushdata.service.AuthExposeAPIService;

/**
 * @author kunkambl
 */
@RunWith(MockitoJUnitRunner.class)
public class MaturityControllerTest {

    @InjectMocks
    MaturityController maturityController;

    @Mock
    MaturityServiceImpl maturityService;

    @Mock
    AuthExposeAPIService authExposeAPIService;

    @Before
    public void setUp() {
    }

    @Test
    public void getMaturityValuesUnauthorized() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        KpiRequest kpiRequest = new KpiRequest();
        when(authExposeAPIService.validateToken(request)).thenReturn(null);

        ResponseEntity<List<KpiElement>> responseEntity = maturityController.getMaturityValues(request, kpiRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().isEmpty());
        verify(authExposeAPIService).validateToken(request);
        verify(maturityService, never()).getMaturityValues(any());
    }

    @Test
    public void testGetMaturityValuesSuccess() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        KpiRequest kpiRequest = new KpiRequest();
        ExposeApiToken exposeApiToken = new ExposeApiToken();
        when(authExposeAPIService.validateToken(request)).thenReturn(exposeApiToken);
        when(maturityService.getMaturityValues(kpiRequest)).thenReturn(Collections.singletonList(new KpiElement()));

        ResponseEntity<List<KpiElement>> responseEntity = maturityController.getMaturityValues(request, kpiRequest);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertFalse(responseEntity.getBody().isEmpty());
        verify(authExposeAPIService).validateToken(request);
        verify(maturityService).getMaturityValues(kpiRequest);
    }

    @Test
    public void testGetMaturityValuesForbidden() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        KpiRequest kpiRequest = new KpiRequest();
        ExposeApiToken exposeApiToken = new ExposeApiToken();
        when(authExposeAPIService.validateToken(request)).thenReturn(exposeApiToken);
        when(maturityService.getMaturityValues(kpiRequest)).thenReturn(Collections.emptyList());

        ResponseEntity<List<KpiElement>> responseEntity = maturityController.getMaturityValues(request, kpiRequest);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().isEmpty());
        verify(authExposeAPIService).validateToken(request);
        verify(maturityService).getMaturityValues(kpiRequest);
    }
}