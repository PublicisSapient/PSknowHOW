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

import static org.mockito.Mockito.*;

public class HierarchyValueTest {
	@Mock
	HierarchyLevel hierarchyLevel;
	@InjectMocks
	HierarchyValue hierarchyValue;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testEquals() throws Exception {
		boolean result = hierarchyValue.equals("o");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testSetHierarchyLevel() throws Exception {
		hierarchyValue.setHierarchyLevel(new HierarchyLevel(0, "hierarchyLevelId", "hierarchyLevelName"));
	}

	@Test
	public void testSetValue() throws Exception {
		hierarchyValue.setValue("value");
	}

	@Test
	public void testToString() throws Exception {
		String result = hierarchyValue.toString();
		Assert.assertNotNull(result);
	}
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme