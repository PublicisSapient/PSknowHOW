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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.stringshortener.dto.StringShortenerDTO;
import com.publicissapient.kpidashboard.apis.stringshortener.model.StringShortener;
import com.publicissapient.kpidashboard.apis.stringshortener.repository.StringShortenerRepository;

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

		stringShortener = new StringShortener();
		stringShortener.setLongKPIFiltersString("longKPI");
		stringShortener.setShortKPIFilterString("shortKPI");
		stringShortener.setLongStateFiltersString("longState");
		stringShortener.setShortStateFiltersString("shortState");
	}

	@Test
	public void testCreateShortString_NullInput() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			stringShortenerService.createShortString(null);
		});
		assertEquals("Please provide a valid stringShortenerDTO", exception.getMessage());
	}

	@Test
	public void testCreateShortString_ExistingMapping() {
		when(stringShortenerRepository.findByLongKPIFiltersStringAndLongStateFiltersString(
						"longKPI", "longState"))
				.thenReturn(Optional.of(stringShortener));

		StringShortener result = stringShortenerService.createShortString(stringShortenerDTO);
		assertEquals(stringShortener, result);
	}

	@Test
	public void testGetLongString_Found() {
		when(stringShortenerRepository.findByShortKPIFilterStringAndShortStateFiltersString(
						"shortKPI", "shortState"))
				.thenReturn(Optional.of(stringShortener));

		Optional<StringShortener> result =
				stringShortenerService.getLongString("shortKPI", "shortState");
		assertEquals(stringShortener, result.get());
	}

	@Test
	public void testGetLongString_NotFound() {
		when(stringShortenerRepository.findByShortKPIFilterStringAndShortStateFiltersString(
						"shortKPI", "shortState"))
				.thenReturn(Optional.empty());

		Optional<StringShortener> result =
				stringShortenerService.getLongString("shortKPI", "shortState");
		assertEquals(Optional.empty(), result);
	}

	@Test
	public void testCreateShortString_NewMapping() {
		// Arrange
		when(stringShortenerRepository.findByLongKPIFiltersStringAndLongStateFiltersString(
						"longKPI", "longState"))
				.thenReturn(Optional.empty());

		when(stringShortenerRepository.save(any(StringShortener.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		StringShortener result = stringShortenerService.createShortString(stringShortenerDTO);

		// Assert
		assertEquals("longKPI", result.getLongKPIFiltersString());
		assertEquals("longState", result.getLongStateFiltersString());
		assertEquals(8, result.getShortKPIFilterString().length());
		assertEquals(8, result.getShortStateFiltersString().length());
	}
}
