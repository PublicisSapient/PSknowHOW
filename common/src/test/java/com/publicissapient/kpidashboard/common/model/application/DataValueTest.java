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

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class DataValueTest {
	@Mock
	Object value;
	@Mock
	Map<String, Object> hoverValue;
	@InjectMocks
	DataValue dataValue;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testEquals() throws Exception {
		boolean result = dataValue.equals(new DataValue());
		Assert.assertEquals(false, result);
	}

	@Test
	public void testCanEqual() throws Exception {
		boolean result = dataValue.canEqual("other");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testSetName() throws Exception {
		dataValue.setName("name");
	}

	@Test
	public void testSetLineType() throws Exception {
		dataValue.setLineType("lineType");
	}

	@Test
	public void testSetData() throws Exception {
		dataValue.setData("data");
	}

	@Test
	public void testSetValue() throws Exception {
		dataValue.setValue("value");
	}

	@Test
	public void testSetHoverValue() throws Exception {
		dataValue.setHoverValue(new HashMap<String, Object>() {
			{
				put("String", "hoverValue");
			}
		});
	}

	@Test
	public void testToString() throws Exception {
		String result = dataValue.toString();
		Assert.assertNotNull( result);
	}

	@Test
	public void testBuilder() throws Exception {
		DataValue.DataValueBuilder result = DataValue.builder();
		Assert.assertNotNull( result);
	}
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme