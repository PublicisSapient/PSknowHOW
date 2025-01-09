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
package com.publicissapient.kpidashboard.apis.stringshortener.rest;

import static org.powermock.api.mockito.PowerMockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicissapient.kpidashboard.apis.stringshortener.controller.StringShortenerController;
import com.publicissapient.kpidashboard.apis.stringshortener.dto.StringShortenerDTO;
import com.publicissapient.kpidashboard.apis.stringshortener.model.StringShortener;
import com.publicissapient.kpidashboard.apis.stringshortener.service.StringShortenerService;

@RunWith(MockitoJUnitRunner.class)
public class StringShortenerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StringShortenerService stringShortenerService;

    private ObjectMapper objectMapper;

    @InjectMocks
    private StringShortenerController stringShortenerController;

    @Before
    public void before() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(stringShortenerController).build();
    }

    @After
    public void after() {
        mockMvc = null;
    }


    @Test
    public void testCreateShortUrl() throws Exception {
        String longString = "exampleString";
        String shortString = "2c26b46b";

        StringShortenerDTO requestDTO = new StringShortenerDTO();
        requestDTO.setLongString(longString);

        StringShortener stringShortener = new StringShortener();
        stringShortener.setLongString(longString);
        stringShortener.setShortString(shortString);

        when(stringShortenerService.createShortString(longString)).thenReturn(stringShortener);
        mockMvc.perform(MockMvcRequestBuilders.post("/stringShortener/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetLongUrl() throws Exception {
        String shortString = "2c26b46b";
        String longString = "exampleString";

        StringShortener stringShortener = new StringShortener();
        stringShortener.setLongString(longString);
        stringShortener.setShortString(shortString);

        when(stringShortenerService.getLongString(shortString)).thenReturn(Optional.of(stringShortener));

        mockMvc.perform(MockMvcRequestBuilders.get("/stringShortener/{shortString}", shortString))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetLongUrlNotFound() throws Exception {
        String shortString = "nonExistingShortString";

        when(stringShortenerService.getLongString(shortString)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/stringShortener/{shortString}", shortString))
                .andExpect(status().isNotFound());
    }
}