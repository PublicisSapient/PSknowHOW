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

package com.publicissapient.kpidashboard.common.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.common.data.HierachyLevelFactory;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.repository.application.HierarchyLevelRepository;

@ExtendWith(SpringExtension.class)
public class HierarchyLevelServiceImplTest {

	@Mock
	private HierarchyLevelRepository hierarchyLevelRepository;

	@Mock
	private AdditionalFilterCategoryService filterCategoryLevelService;

	@InjectMocks
	private HierarchyLevelServiceImpl hierarchyLevelService;

	List<HierarchyLevel> mockHierarchyLevels;
	List<AdditionalFilterCategory> mockCategories;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		HierachyLevelFactory hierachyLevelFactory = HierachyLevelFactory.newInstance();
		mockHierarchyLevels = hierachyLevelFactory.getHierarchyLevels();

		AdditionalFilterCategory category1 = new AdditionalFilterCategory();
		category1.setLevel(1);
		category1.setFilterCategoryId("Category ID1");
		category1.setFilterCategoryName("Category A");
		AdditionalFilterCategory category2 = new AdditionalFilterCategory();
		category2.setLevel(2);
		category2.setFilterCategoryId("Category ID2");
		category2.setFilterCategoryName("Category B");
		mockCategories = Arrays.asList(category1, category2);
	}

	@Test
	public void testGetTopHierarchyLevels() {
		when(hierarchyLevelRepository.findAllByOrderByLevel()).thenReturn(mockHierarchyLevels);

		// Calling the actual method
		List<HierarchyLevel> result = hierarchyLevelService.getTopHierarchyLevels();

		// Verifying the interactions
		verify(hierarchyLevelRepository, times(1)).findAllByOrderByLevel();

		// Asserting the result
		assertEquals(mockHierarchyLevels, result);
	}

	@Test
	public void testGetFullHierarchyLevels_Kanban() {
		// Mocking data and dependencies
		when(hierarchyLevelRepository.findAllByOrderByLevel()).thenReturn(mockHierarchyLevels);
		when(filterCategoryLevelService.getAdditionalFilterCategories()).thenReturn(mockCategories);

		// Calling the actual method
		List<HierarchyLevel> result = hierarchyLevelService.getFullHierarchyLevels(true);

		// Asserting the result
		assertNotNull(result);
		// Add more assertions based on your specific logic and expectations
	}

	@Test
	public void testGetFullHierarchyLevels_NotKanban() {
		// Mocking data and dependencies
		when(hierarchyLevelRepository.findAllByOrderByLevel()).thenReturn(mockHierarchyLevels);
		when(filterCategoryLevelService.getAdditionalFilterCategories())
				.thenReturn(Collections.emptyList());

		// Calling the actual method
		List<HierarchyLevel> result = hierarchyLevelService.getFullHierarchyLevels(false);

		// Asserting the result
		assertNotNull(result);
		// Add more assertions based on your specific logic and expectations
	}

	@Test
	public void testGetProjectHierarchyLevel() {
		// Mocking data and dependencies
		when(hierarchyLevelRepository.findAllByOrderByLevel()).thenReturn(mockHierarchyLevels);

		// Calling the actual method
		HierarchyLevel result = hierarchyLevelService.getProjectHierarchyLevel();

		// Asserting the result
		assertNotNull(result);
		// Add more assertions based on your specific logic and expectations
	}

	@Test
	public void testGetSprintHierarchyLevel() {
		// Mocking data and dependencies
		when(hierarchyLevelRepository.findAllByOrderByLevel()).thenReturn(mockHierarchyLevels);

		// Calling the actual method
		HierarchyLevel result = hierarchyLevelService.getSprintHierarchyLevel();

		// Asserting the result
		assertNotNull(result);
		// Add more assertions based on your specific logic and expectations
	}

	@Test
	public void testGetReleaseHierarchyLevel() {
		// Mocking data and dependencies
		when(hierarchyLevelRepository.findAllByOrderByLevel()).thenReturn(mockHierarchyLevels);

		// Calling the actual method
		HierarchyLevel result = hierarchyLevelService.getReleaseHierarchyLevel();

		// Asserting the result
		assertNotNull(result);
		// Add more assertions based on your specific logic and expectations
	}
}
