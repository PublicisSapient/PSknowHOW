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

package com.publicissapient.kpidashboard.common.model.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class AdditionalFilterTest {
	/** Method under test: {@link AdditionalFilter#canEqual(Object)} */
	@Test
	void testCanEqual() {
		assertFalse((new AdditionalFilter()).canEqual("Other"));
	}

	/** Method under test: {@link AdditionalFilter#canEqual(Object)} */
	@Test
	void testCanEqual2() {
		AdditionalFilter additionalFilter = new AdditionalFilter();

		AdditionalFilter additionalFilter1 = new AdditionalFilter();
		additionalFilter1.setFilterId("42");
		additionalFilter1.setFilterValues(new ArrayList<>());
		assertTrue(additionalFilter.canEqual(additionalFilter1));
	}

	/**
	 * Methods under test:
	 *
	 * <ul>
	 * <li>default or parameterless constructor of {@link AdditionalFilter}
	 * <li>{@link AdditionalFilter#setFilterId(String)}
	 * <li>{@link AdditionalFilter#setFilterValues(List)}
	 * <li>{@link AdditionalFilter#toString()}
	 * <li>{@link AdditionalFilter#getFilterId()}
	 * <li>{@link AdditionalFilter#getFilterValues()}
	 * </ul>
	 */
	@Test
	void testConstructor() {
		AdditionalFilter actualAdditionalFilter = new AdditionalFilter();
		actualAdditionalFilter.setFilterId("42");
		ArrayList<AdditionalFilterValue> additionalFilterValueList = new ArrayList<>();
		actualAdditionalFilter.setFilterValues(additionalFilterValueList);
		String actualToStringResult = actualAdditionalFilter.toString();
		assertEquals("42", actualAdditionalFilter.getFilterId());
		assertSame(additionalFilterValueList, actualAdditionalFilter.getFilterValues());
		assertEquals("AdditionalFilter(filterId=42, filterValues=[])", actualToStringResult);
	}

	/** Method under test: {@link AdditionalFilter#equals(Object)} */
	@Test
	void testEquals() {
		AdditionalFilter additionalFilter = new AdditionalFilter();
		additionalFilter.setFilterId("42");
		additionalFilter.setFilterValues(new ArrayList<>());
		assertNotEquals(additionalFilter, null);
	}

	/** Method under test: {@link AdditionalFilter#equals(Object)} */
	@Test
	void testEquals2() {
		AdditionalFilter additionalFilter = new AdditionalFilter();
		additionalFilter.setFilterId("42");
		additionalFilter.setFilterValues(new ArrayList<>());
		assertNotEquals(additionalFilter, "Different type to AdditionalFilter");
	}

	/**
	 * Methods under test:
	 *
	 * <ul>
	 * <li>{@link AdditionalFilter#equals(Object)}
	 * <li>{@link AdditionalFilter#hashCode()}
	 * </ul>
	 */
	@Test
	void testEquals3() {
		AdditionalFilter additionalFilter = new AdditionalFilter();
		additionalFilter.setFilterId("42");
		additionalFilter.setFilterValues(new ArrayList<>());
		assertEquals(additionalFilter, additionalFilter);
		int expectedHashCodeResult = additionalFilter.hashCode();
		assertEquals(expectedHashCodeResult, additionalFilter.hashCode());
	}

	/**
	 * Methods under test:
	 *
	 * <ul>
	 * <li>{@link AdditionalFilter#equals(Object)}
	 * <li>{@link AdditionalFilter#hashCode()}
	 * </ul>
	 */
	@Test
	void testEquals4() {
		AdditionalFilter additionalFilter = new AdditionalFilter();
		additionalFilter.setFilterId("42");
		additionalFilter.setFilterValues(new ArrayList<>());

		AdditionalFilter additionalFilter1 = new AdditionalFilter();
		additionalFilter1.setFilterId("42");
		additionalFilter1.setFilterValues(new ArrayList<>());
		assertEquals(additionalFilter, additionalFilter1);
		int expectedHashCodeResult = additionalFilter.hashCode();
		assertEquals(expectedHashCodeResult, additionalFilter1.hashCode());
	}

	/** Method under test: {@link AdditionalFilter#equals(Object)} */
	@Test
	void testEquals5() {
		AdditionalFilter additionalFilter = new AdditionalFilter();
		additionalFilter.setFilterId("Filter Id");
		additionalFilter.setFilterValues(new ArrayList<>());

		AdditionalFilter additionalFilter1 = new AdditionalFilter();
		additionalFilter1.setFilterId("42");
		additionalFilter1.setFilterValues(new ArrayList<>());
		assertNotEquals(additionalFilter, additionalFilter1);
	}

	/** Method under test: {@link AdditionalFilter#equals(Object)} */
	@Test
	void testEquals6() {
		AdditionalFilter additionalFilter = new AdditionalFilter();
		additionalFilter.setFilterId(null);
		additionalFilter.setFilterValues(new ArrayList<>());

		AdditionalFilter additionalFilter1 = new AdditionalFilter();
		additionalFilter1.setFilterId("42");
		additionalFilter1.setFilterValues(new ArrayList<>());
		assertNotEquals(additionalFilter, additionalFilter1);
	}

	/** Method under test: {@link AdditionalFilter#equals(Object)} */
	@Test
	void testEquals7() {
		AdditionalFilterValue additionalFilterValue = new AdditionalFilterValue();
		additionalFilterValue.setValue("42");
		additionalFilterValue.setValueId("42");

		ArrayList<AdditionalFilterValue> additionalFilterValueList = new ArrayList<>();
		additionalFilterValueList.add(additionalFilterValue);

		AdditionalFilter additionalFilter = new AdditionalFilter();
		additionalFilter.setFilterId("42");
		additionalFilter.setFilterValues(additionalFilterValueList);

		AdditionalFilter additionalFilter1 = new AdditionalFilter();
		additionalFilter1.setFilterId("42");
		additionalFilter1.setFilterValues(new ArrayList<>());
		assertNotEquals(additionalFilter, additionalFilter1);
	}

	/**
	 * Methods under test:
	 *
	 * <ul>
	 * <li>{@link AdditionalFilter#equals(Object)}
	 * <li>{@link AdditionalFilter#hashCode()}
	 * </ul>
	 */
	@Test
	void testEquals8() {
		AdditionalFilter additionalFilter = new AdditionalFilter();
		additionalFilter.setFilterId(null);
		additionalFilter.setFilterValues(new ArrayList<>());

		AdditionalFilter additionalFilter1 = new AdditionalFilter();
		additionalFilter1.setFilterId(null);
		additionalFilter1.setFilterValues(new ArrayList<>());
		assertEquals(additionalFilter, additionalFilter1);
		int expectedHashCodeResult = additionalFilter.hashCode();
		assertEquals(expectedHashCodeResult, additionalFilter1.hashCode());
	}

	/** Method under test: {@link AdditionalFilter#equals(Object)} */
	@Test
	void testEquals9() {
		AdditionalFilterValue additionalFilterValue = mock(AdditionalFilterValue.class);
		doNothing().when(additionalFilterValue).setValue((String) any());
		doNothing().when(additionalFilterValue).setValueId((String) any());
		additionalFilterValue.setValue("42");
		additionalFilterValue.setValueId("42");

		ArrayList<AdditionalFilterValue> additionalFilterValueList = new ArrayList<>();
		additionalFilterValueList.add(additionalFilterValue);

		AdditionalFilter additionalFilter = new AdditionalFilter();
		additionalFilter.setFilterId("42");
		additionalFilter.setFilterValues(additionalFilterValueList);

		AdditionalFilter additionalFilter1 = new AdditionalFilter();
		additionalFilter1.setFilterId("42");
		additionalFilter1.setFilterValues(new ArrayList<>());
		assertNotEquals(additionalFilter, additionalFilter1);
	}
}
