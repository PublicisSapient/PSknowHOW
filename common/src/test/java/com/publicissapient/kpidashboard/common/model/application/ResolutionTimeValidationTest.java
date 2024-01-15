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

public class ResolutionTimeValidationTest {
	ResolutionTimeValidation resolutionTimeValidation = new ResolutionTimeValidation("issueNumber", "url",
			"issueDescription", "issueType", Double.valueOf(0));

	@Test
	public void testEquals() throws Exception {
		boolean result = resolutionTimeValidation.equals("o");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testCanEqual() throws Exception {
		boolean result = resolutionTimeValidation.canEqual("other");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testSetIssueNumber() throws Exception {
		resolutionTimeValidation.setIssueNumber("issueNumber");
	}

	@Test
	public void testSetUrl() throws Exception {
		resolutionTimeValidation.setUrl("url");
	}

	@Test
	public void testSetIssueDescription() throws Exception {
		resolutionTimeValidation.setIssueDescription("issueDescription");
	}

	@Test
	public void testSetIssueType() throws Exception {
		resolutionTimeValidation.setIssueType("issueType");
	}

	@Test
	public void testSetResolutionTime() throws Exception {
		resolutionTimeValidation.setResolutionTime(Double.valueOf(0));
	}

	@Test
	public void testToString() throws Exception {
		String result = resolutionTimeValidation.toString();
		Assert.assertNotNull(result);
	}

	@Test
	public void testBuilder() throws Exception {
		ResolutionTimeValidation.ResolutionTimeValidationBuilder result = ResolutionTimeValidation.builder();
		Assert.assertNotNull(result);
	}
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme