/*
 *
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.publicissapient.kpidashboard.common.model.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AdditionalFilterCategoryTest {
	@Test
	public void testConstructorAndGetters() {
		// Create an instance using the constructor
		AdditionalFilterCategory category = new AdditionalFilterCategory(1, "123", "Test Category");

		// Verify values using getters
		assertEquals(1, category.getLevel());
		assertEquals("123", category.getFilterCategoryId());
		assertEquals("Test Category", category.getFilterCategoryName());
	}

	@Test
	public void testSetterMethods() {
		// Create an instance using the default constructor
		AdditionalFilterCategory category = new AdditionalFilterCategory();

		// Use setter methods to set values
		category.setLevel(2);
		category.setFilterCategoryId("456");
		category.setFilterCategoryName("New Category");

		// Verify values using getters
		assertEquals(2, category.getLevel());
		assertEquals("456", category.getFilterCategoryId());
		assertEquals("New Category", category.getFilterCategoryName());
	}

	@Test
	public void testEqualsAndHashCode() {
		// Create two instances with the same values
		AdditionalFilterCategory category1 = new AdditionalFilterCategory(1, "123", "Test Category");
		AdditionalFilterCategory category2 = new AdditionalFilterCategory(1, "123", "Test Category");

		// Verify equals method
		Assertions.assertTrue(category1.equals(category2));
		Assertions.assertTrue(category2.equals(category1));

		// Verify hashCode method
		assertEquals(category1.hashCode(), category2.hashCode());
	}

	@Test
	public void testNotEquals() {
		// Create two instances with different values
		AdditionalFilterCategory category1 = new AdditionalFilterCategory(1, "123", "Test Category");
		AdditionalFilterCategory category2 = new AdditionalFilterCategory(2, "456", "New Category");

		// Verify equals method
		Assertions.assertFalse(category1.equals(category2));
		Assertions.assertFalse(category2.equals(category1));

		// Verify hashCode method (optional, but recommended)
		assertNotEquals(category1.hashCode(), category2.hashCode());
	}
}
