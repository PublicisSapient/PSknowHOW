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
import java.util.List;

import static org.mockito.Mockito.*;

public class TotalDefectAgingResponseTest {
	@Mock
	List<TotalDefectAgingProjectWise> value;
	@InjectMocks
	TotalDefectAgingResponse totalDefectAgingResponse;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testSetRange() throws Exception {
		totalDefectAgingResponse.setRange("range");
	}

	@Test
	public void testSetValue() throws Exception {
		totalDefectAgingResponse.setValue(Arrays.<TotalDefectAgingProjectWise>asList(
				new TotalDefectAgingProjectWise("projectName", Integer.valueOf(0))));
	}

	@Test
	public void testEquals() throws Exception {
		boolean result = totalDefectAgingResponse.equals("o");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testCanEqual() throws Exception {
		boolean result = totalDefectAgingResponse.canEqual("other");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testToString() throws Exception {
		String result = totalDefectAgingResponse.toString();
		Assert.assertNotNull(result);
	}

	@Test
	public void testBuilder() throws Exception {
		TotalDefectAgingResponse.TotalDefectAgingResponseBuilder result = TotalDefectAgingResponse.builder();
		Assert.assertNotNull(result);
	}
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme