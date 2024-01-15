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
import org.junit.Test;

public class KpiCategoryTest {
	// Field id of type ObjectId - was not mocked since Mockito doesn't mock a Final
	// class when 'mock-maker-inline' option is not set
	KpiCategory kpiCategory = new KpiCategory("categoryId", "categoryName");

	@Test
	public void testEquals() throws Exception {
		boolean result = kpiCategory.equals("o");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testCanEqual() throws Exception {
		boolean result = kpiCategory.canEqual("other");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testToString() throws Exception {
		String result = kpiCategory.toString();
		Assert.assertNotNull(result);
	}

	@Test
	public void testSetCategoryId() throws Exception {
		kpiCategory.setCategoryId("categoryId");
	}

	@Test
	public void testSetCategoryName() throws Exception {
		kpiCategory.setCategoryName("categoryName");
	}

	@Test
	public void testBuilder() throws Exception {
		KpiCategory.KpiCategoryBuilder result = KpiCategory.builder();
		Assert.assertNotNull(result);
	}

	@Test
	public void testSetId() throws Exception {
		kpiCategory.setId(null);
	}
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme