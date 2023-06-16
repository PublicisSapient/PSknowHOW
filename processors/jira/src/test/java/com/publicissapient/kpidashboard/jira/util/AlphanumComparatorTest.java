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

package com.publicissapient.kpidashboard.jira.util;

import java.io.File;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class AlphanumComparatorTest {

	AlphanumComparator alphanumComparator = new AlphanumComparator();

	@Mock
	File fileOne;
	@Mock
	File fileTwo;

	@BeforeEach
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void compareCaseOne() {
		Mockito.when(fileOne.getName()).thenReturn("TEST_191029");
		Mockito.when(fileTwo.getName()).thenReturn("TEST_101029");
		int result = alphanumComparator.compare(fileOne, fileTwo);
		Assert.assertTrue(result >= 1);

	}

	@Test
	public void compareCaseTwo() {
		Mockito.when(fileTwo.getName()).thenReturn("TEST_191029_4");
		Mockito.when(fileOne.getName()).thenReturn("TEST_10102");
		int result = alphanumComparator.compare(fileOne, fileTwo);
		Assert.assertTrue(result <= -1);

	}

	@Test
	public void compareNullFileName() {
		Mockito.when(fileTwo.getName()).thenReturn(null);
		Mockito.when(fileOne.getName()).thenReturn("TEST_10102");
		int result = alphanumComparator.compare(fileOne, fileTwo);
		Assert.assertEquals(0, result);

	}
}