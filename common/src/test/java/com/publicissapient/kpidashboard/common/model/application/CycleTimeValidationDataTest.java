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

public class CycleTimeValidationDataTest {
	// Field intakeDate of type DateTime - was not mocked since Mockito doesn't mock
	// a Final class when 'mock-maker-inline' option is not set
	// Field dorDate of type DateTime - was not mocked since Mockito doesn't mock a
	// Final class when 'mock-maker-inline' option is not set
	// Field dodDate of type DateTime - was not mocked since Mockito doesn't mock a
	// Final class when 'mock-maker-inline' option is not set
	// Field liveDate of type DateTime - was not mocked since Mockito doesn't mock a
	// Final class when 'mock-maker-inline' option is not set
	CycleTimeValidationData cycleTimeValidationData = new CycleTimeValidationData("issueNumber", "url", "issueDesc",
			"issueType", null, null, null, null, Long.valueOf(1), Long.valueOf(1), Long.valueOf(1), Long.valueOf(1));

	@Test
	public void testEquals() throws Exception {
		boolean result = cycleTimeValidationData.equals(new CycleTimeValidationData());
		Assert.assertEquals(false, result);
	}

	@Test
	public void testCanEqual() throws Exception {
		boolean result = cycleTimeValidationData.canEqual(new CycleTimeValidationData());
		Assert.assertEquals(true, result);
	}

	@Test
	public void testSetIssueNumber() throws Exception {
		cycleTimeValidationData.setIssueNumber("issueNumber");
	}

	@Test
	public void testSetUrl() throws Exception {
		cycleTimeValidationData.setUrl("url");
	}

	@Test
	public void testSetIssueDesc() throws Exception {
		cycleTimeValidationData.setIssueDesc("issueDesc");
	}

	@Test
	public void testSetIssueType() throws Exception {
		cycleTimeValidationData.setIssueType("issueType");
	}

	@Test
	public void testSetIntakeDate() throws Exception {
		cycleTimeValidationData.setIntakeDate(null);
	}

	@Test
	public void testSetDorDate() throws Exception {
		cycleTimeValidationData.setDorDate(null);
	}

	@Test
	public void testSetDodDate() throws Exception {
		cycleTimeValidationData.setDodDate(null);
	}

	@Test
	public void testSetLiveDate() throws Exception {
		cycleTimeValidationData.setLiveDate(null);
	}

	@Test
	public void testSetLeadTime() throws Exception {
		cycleTimeValidationData.setLeadTime(Long.valueOf(1));
	}

	@Test
	public void testSetIntakeTime() throws Exception {
		cycleTimeValidationData.setIntakeTime(Long.valueOf(1));
	}

	@Test
	public void testSetDorTime() throws Exception {
		cycleTimeValidationData.setDorTime(Long.valueOf(1));
	}

	@Test
	public void testSetDodTime() throws Exception {
		cycleTimeValidationData.setDodTime(Long.valueOf(1));
	}

	@Test
	public void testToString() throws Exception {
		String result = cycleTimeValidationData.toString();
		Assert.assertNotNull(result);
	}

	@Test
	public void testBuilder() throws Exception {
		CycleTimeValidationData.CycleTimeValidationDataBuilder result = CycleTimeValidationData.builder();
		Assert.assertNotNull(result);
	}
}

