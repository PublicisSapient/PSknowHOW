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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

public class AdditionalFilterConfigTest {
	@Mock
	Set<String> values;
	@InjectMocks
	AdditionalFilterConfig additionalFilterConfig;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testSetFilterId() throws Exception {
		additionalFilterConfig.setFilterId("filterId");
	}

	@Test
	public void testSetIdentifyFrom() throws Exception {
		additionalFilterConfig.setIdentifyFrom("identifyFrom");
	}

	@Test
	public void testSetIdentificationField() throws Exception {
		additionalFilterConfig.setIdentificationField("identificationField");
	}

	@Test
	public void testSetValues() throws Exception {
		additionalFilterConfig.setValues(new HashSet<String>(Arrays.asList("String")));
	}

	@Test
	public void testEquals() throws Exception {
		boolean result = additionalFilterConfig.equals("o");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testCanEqual() throws Exception {
		boolean result = additionalFilterConfig.canEqual("other");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testToString() throws Exception {
		String result = additionalFilterConfig.toString();
		Assert.assertNotNull( result);
	}
}