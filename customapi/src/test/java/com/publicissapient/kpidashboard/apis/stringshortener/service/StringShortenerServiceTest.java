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

import com.publicissapient.kpidashboard.apis.stringshortener.dto.StringShortenerDTO;
import com.publicissapient.kpidashboard.apis.stringshortener.model.StringShortener;
import com.publicissapient.kpidashboard.apis.stringshortener.repository.StringShortenerRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StringShortenerServiceTest {

	@Mock
	private StringShortenerRepository stringShortenerRepository;

	@InjectMocks
	private StringShortenerService stringShortenerService;

	private StringShortenerDTO stringShortenerDTO;
	private StringShortener stringShortener;

	@Before
	public void setUp() {
		stringShortenerDTO = new StringShortenerDTO();
		stringShortenerDTO.setLongKPIFiltersString("longKPI");
		stringShortenerDTO.setLongStateFiltersString("longState");
		stringShortenerDTO.setShortKPIFilterString("shortKPI");
		stringShortenerDTO.setShortStateFiltersString("shortState");

		stringShortener = new StringShortener();
		stringShortener.setLongKPIFiltersString("longKPI");
		stringShortener.setShortKPIFilterString("shortKPI");
		stringShortener.setLongStateFiltersString("longState");
		stringShortener.setShortStateFiltersString("shortState");
		MockitoAnnotations.openMocks(this);
	}

//	@Test
//	public void testCreateShortString_NewMapping() {
//		when(stringShortenerRepository.findByLongKPIFiltersStringAndLongStateFiltersString(any(), any()))
//				.thenReturn(Optional.empty());
//		when(stringShortenerRepository.save(any(StringShortener.class))).thenReturn(stringShortener);
//
//		StringShortener result = stringShortenerService.createShortString(stringShortenerDTO);
//
//		assertNotNull(result);
//		assertEquals("longKPI", result.getLongKPIFiltersString());
//		assertEquals("shortKPI", result.getShortKPIFilterString());
//		assertEquals("longState", result.getLongStateFiltersString());
//		assertEquals("shortState", result.getShortStateFiltersString());
//	}

//	@Test
//	public void testCreateShortString_ExistingMapping() {
//		when(stringShortenerRepository.findByLongKPIFiltersStringAndLongStateFiltersString(any(), any()))
//				.thenReturn(Optional.of(stringShortener));
//
//		StringShortener result = stringShortenerService.createShortString(stringShortenerDTO);
//
//		assertNotNull(result);
//		assertEquals("longKPI", result.getLongKPIFiltersString());
//		assertEquals("shortKPI", result.getShortKPIFilterString());
//		assertEquals("longState", result.getLongStateFiltersString());
//		assertEquals("shortState", result.getShortStateFiltersString());
//	}

	@Test
	public void testCreateShortString_NullInput() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			stringShortenerService.createShortString(null);
		});

		assertEquals("Please provide a valid stringShortenerDTO", exception.getMessage());
	}

//	@Test
//	public void testGetLongString_ExistingMapping() {
//		when(stringShortenerRepository.findByShortKPIFilterStringAndShortStateFiltersString(any(), any()))
//				.thenReturn(Optional.of(stringShortener));
//
//		Optional<StringShortener> result = stringShortenerService.getLongString("shortKPI", "shortState");
//
//		assertTrue(result.isPresent());
//		assertEquals("longKPI", result.get().getLongKPIFiltersString());
//		assertEquals("longState", result.get().getLongStateFiltersString());
//	}
}