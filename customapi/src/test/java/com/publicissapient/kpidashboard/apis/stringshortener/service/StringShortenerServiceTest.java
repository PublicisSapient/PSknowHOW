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
package com.publicissapient.kpidashboard.apis.stringshortener.service;

import com.publicissapient.kpidashboard.apis.stringshortener.model.StringShortener;
import com.publicissapient.kpidashboard.apis.stringshortener.repository.StringShortenerRepository;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StringShortenerServiceTest {

    @Mock
    private StringShortenerRepository stringShortenerRepository;

    @InjectMocks
    private StringShortenerService stringShortenerService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateShortString() {
        String longString = "exampleString";
        String shortString = "2c26b46b";

        StringShortener stringShortener = new StringShortener();
        stringShortener.setLongString(longString);
        stringShortener.setShortString(shortString);

        when(stringShortenerRepository.findByLongString(longString)).thenReturn(Optional.empty());
        when(stringShortenerRepository.save(any(StringShortener.class))).thenReturn(stringShortener);

        StringShortener result = stringShortenerService.createShortString(longString);
        assertEquals(longString, result.getLongString());
        assertEquals(shortString, result.getShortString());
    }

    @Test
    public void testCreateShortStringWithExistingMapping() {
        String longString = "exampleString";
        String shortString = "2c26b46b";

        StringShortener stringShortener = new StringShortener();
        stringShortener.setLongString(longString);
        stringShortener.setShortString(shortString);

        when(stringShortenerRepository.findByLongString(longString)).thenReturn(Optional.of(stringShortener));

        StringShortener result = stringShortenerService.createShortString(longString);
        assertEquals(longString, result.getLongString());
        assertEquals(shortString, result.getShortString());
    }

    @Test
    public void testCreateShortStringWithNullInput() {
        assertThrows(IllegalArgumentException.class, () -> {
            stringShortenerService.createShortString(null);
        });
    }

    @Test
    public void testGetLongString() {
        String shortString = "2c26b46b";
        String longString = "exampleString";

        StringShortener stringShortener = new StringShortener();
        stringShortener.setLongString(longString);
        stringShortener.setShortString(shortString);

        when(stringShortenerRepository.findByShortString(shortString)).thenReturn(Optional.of(stringShortener));

        Optional<StringShortener> result = stringShortenerService.getLongString(shortString);
        assertEquals(longString, result.get().getLongString());
    }

    @Test
    public void testGetLongStringWithNonExistingMapping() {
        String shortString = "nonExistingShortString";

        when(stringShortenerRepository.findByShortString(shortString)).thenReturn(Optional.empty());

        Optional<StringShortener> result = stringShortenerService.getLongString(shortString);
        assertEquals(Optional.empty(), result);
    }
}