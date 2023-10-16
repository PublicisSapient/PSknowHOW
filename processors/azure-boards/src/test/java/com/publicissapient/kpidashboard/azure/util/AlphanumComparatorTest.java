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

package com.publicissapient.kpidashboard.azure.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
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
		when(fileOne.getName()).thenReturn("TEST_191029");
		when(fileTwo.getName()).thenReturn("TEST_101029");
		int result = alphanumComparator.compare(fileOne, fileTwo);
		assertEquals(9, result);
	}

	@Test
	public void compareCaseTwo() {
		when(fileTwo.getName()).thenReturn("TEST_191029_4");
		when(fileOne.getName()).thenReturn("TEST_10102");
		int result = alphanumComparator.compare(fileOne, fileTwo);
		assertEquals(-1, result);

	}

	@Test
	public void compareNullFileName() {
		when(fileTwo.getName()).thenReturn(null);
		when(fileOne.getName()).thenReturn("TEST_10102");
		int result = alphanumComparator.compare(fileOne, fileTwo);
		assertEquals(0, result);

	}
}