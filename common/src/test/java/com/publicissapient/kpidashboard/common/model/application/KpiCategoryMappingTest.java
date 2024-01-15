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

public class KpiCategoryMappingTest {
	// Field id of type ObjectId - was not mocked since Mockito doesn't mock a Final
	// class when 'mock-maker-inline' option is not set
	KpiCategoryMapping kpiCategoryMapping = new KpiCategoryMapping("kpiId", "categoryId", Integer.valueOf(0), true);

	@Test
	public void testEquals() throws Exception {
		boolean result = kpiCategoryMapping.equals("o");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testCanEqual() throws Exception {
		boolean result = kpiCategoryMapping.canEqual("other");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testToString() throws Exception {
		String result = kpiCategoryMapping.toString();
		Assert.assertNotNull(result);
	}

	@Test
	public void testSetKpiId() throws Exception {
		kpiCategoryMapping.setKpiId("kpiId");
	}

	@Test
	public void testSetCategoryId() throws Exception {
		kpiCategoryMapping.setCategoryId("categoryId");
	}

	@Test
	public void testSetKpiOrder() throws Exception {
		kpiCategoryMapping.setKpiOrder(Integer.valueOf(0));
	}

	@Test
	public void testSetKanban() throws Exception {
		kpiCategoryMapping.setKanban(true);
	}

	@Test
	public void testBuilder() throws Exception {
		KpiCategoryMapping.KpiCategoryMappingBuilder result = KpiCategoryMapping.builder();
		Assert.assertNotNull(result);
	}

	@Test
	public void testSetId() throws Exception {
		kpiCategoryMapping.setId(null);
	}
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme