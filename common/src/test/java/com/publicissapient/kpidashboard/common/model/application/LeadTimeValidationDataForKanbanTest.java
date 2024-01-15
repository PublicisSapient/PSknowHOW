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

public class LeadTimeValidationDataForKanbanTest {
	// Field intakeDate of type DateTime - was not mocked since Mockito doesn't mock
	// a Final class when 'mock-maker-inline' option is not set
	// Field triageDate of type DateTime - was not mocked since Mockito doesn't mock
	// a Final class when 'mock-maker-inline' option is not set
	// Field completedDate of type DateTime - was not mocked since Mockito doesn't
	// mock a Final class when 'mock-maker-inline' option is not set
	// Field liveDate of type DateTime - was not mocked since Mockito doesn't mock a
	// Final class when 'mock-maker-inline' option is not set
	LeadTimeValidationDataForKanban leadTimeValidationDataForKanban = new LeadTimeValidationDataForKanban("url",
			"issueDesc", "issueNumber", null, null, null, null);

	@Test
	public void testEquals() throws Exception {
		boolean result = leadTimeValidationDataForKanban.equals("o");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testCanEqual() throws Exception {
		boolean result = leadTimeValidationDataForKanban.canEqual("other");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testSetUrl() throws Exception {
		leadTimeValidationDataForKanban.setUrl("url");
	}

	@Test
	public void testSetIssueDesc() throws Exception {
		leadTimeValidationDataForKanban.setIssueDesc("issueDesc");
	}

	@Test
	public void testSetIssueNumber() throws Exception {
		leadTimeValidationDataForKanban.setIssueNumber("issueNumber");
	}

	@Test
	public void testSetIntakeDate() throws Exception {
		leadTimeValidationDataForKanban.setIntakeDate(null);
	}

	@Test
	public void testSetTriageDate() throws Exception {
		leadTimeValidationDataForKanban.setTriageDate(null);
	}

	@Test
	public void testSetCompletedDate() throws Exception {
		leadTimeValidationDataForKanban.setCompletedDate(null);
	}

	@Test
	public void testSetLiveDate() throws Exception {
		leadTimeValidationDataForKanban.setLiveDate(null);
	}

	@Test
	public void testToString() throws Exception {
		String result = leadTimeValidationDataForKanban.toString();
		Assert.assertNotNull(result);
	}

	@Test
	public void testBuilder() throws Exception {
		LeadTimeValidationDataForKanban.LeadTimeValidationDataForKanbanBuilder result = LeadTimeValidationDataForKanban
				.builder();
		Assert.assertNotNull(result);
	}
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme