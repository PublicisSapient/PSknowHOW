/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.publicissapient.kpidashboard.common.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import com.publicissapient.kpidashboard.common.repository.application.AdditionalFilterCategoryRepository;

@ExtendWith(SpringExtension.class)
public class AdditionalFilterCategoryServiceImplTest {

	@Mock
	private AdditionalFilterCategoryRepository additionalFilterCategoryRepository;

	@InjectMocks
	private AdditionalFilterCategoryServiceImpl additionalFilterCategoryService;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testGetAdditionalFilterCategories() {
		// Arrange
		AdditionalFilterCategory category1 = new AdditionalFilterCategory();
		category1.setLevel(1);
		category1.setFilterCategoryId("Category ID1");
		category1.setFilterCategoryName("Category A");
		AdditionalFilterCategory category2 = new AdditionalFilterCategory();
		category2.setLevel(2);
		category2.setFilterCategoryId("Category ID2");
		category2.setFilterCategoryName("Category B");
		List<AdditionalFilterCategory> mockCategories = Arrays.asList(category1, category2);

		// Mocking repository behavior
		when(additionalFilterCategoryRepository.findAllByOrderByLevel()).thenReturn(mockCategories);

		// Act
		List<AdditionalFilterCategory> result = additionalFilterCategoryService.getAdditionalFilterCategories();

		// Assert
		assertEquals(2, result.size());
		assertEquals("Category ID1", result.get(0).getFilterCategoryId());
		assertEquals(1, result.get(0).getLevel());
		assertEquals("Category ID2", result.get(1).getFilterCategoryId());
		assertEquals(2, result.get(1).getLevel());

		// Verify that the repository method was called
		verify(additionalFilterCategoryRepository, times(1)).findAllByOrderByLevel();
	}
}
