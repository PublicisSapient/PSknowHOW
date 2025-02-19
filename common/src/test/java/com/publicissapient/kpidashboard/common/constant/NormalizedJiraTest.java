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

package com.publicissapient.kpidashboard.common.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class NormalizedJiraTest {

	@Test
	public void testGetNormalizedJiraValue() {
		assertEquals(NormalizedJira.DEFECT_TYPE, NormalizedJira.getNormalizedJiraValue("Bug"));
		assertEquals(NormalizedJira.TEST_TYPE, NormalizedJira.getNormalizedJiraValue("Test"));
		assertEquals(NormalizedJira.YES_VALUE, NormalizedJira.getNormalizedJiraValue("Yes"));
		assertEquals(NormalizedJira.NO_VALUE, NormalizedJira.getNormalizedJiraValue("No"));
		assertEquals(NormalizedJira.THIRD_PARTY_DEFECT_VALUE, NormalizedJira.getNormalizedJiraValue("UAT"));
		assertEquals(NormalizedJira.TECHSTORY, NormalizedJira.getNormalizedJiraValue("TechStory"));
		assertEquals(NormalizedJira.INVALID, NormalizedJira.getNormalizedJiraValue("Invalid"));
		assertEquals(NormalizedJira.TO_BE_AUTOMATED, NormalizedJira.getNormalizedJiraValue("To be automated"));
		assertEquals(NormalizedJira.QA_DEFECT_VALUE, NormalizedJira.getNormalizedJiraValue("QA"));
		assertEquals(NormalizedJira.STATUS, NormalizedJira.getNormalizedJiraValue("Closed"));
		assertEquals(NormalizedJira.ISSUE_TYPE, NormalizedJira.getNormalizedJiraValue("Epic"));
	}

	@Test
	public void testInvalidGetNormalizedJiraValue() {
		assertEquals(NormalizedJira.INVALID, NormalizedJira.getNormalizedJiraValue("UnknownType"));
	}

	@Test
	public void testEnumValues() {
		assertEquals("Bug", NormalizedJira.DEFECT_TYPE.getValue());
		assertEquals("Test", NormalizedJira.TEST_TYPE.getValue());
		assertEquals("Yes", NormalizedJira.YES_VALUE.getValue());
		assertEquals("No", NormalizedJira.NO_VALUE.getValue());
		assertEquals("UAT", NormalizedJira.THIRD_PARTY_DEFECT_VALUE.getValue());
		assertEquals("TechStory", NormalizedJira.TECHSTORY.getValue());
		assertEquals("Invalid", NormalizedJira.INVALID.getValue());
		assertEquals("To be automated", NormalizedJira.TO_BE_AUTOMATED.getValue());
		assertEquals("QA", NormalizedJira.QA_DEFECT_VALUE.getValue());
		assertEquals("Closed", NormalizedJira.STATUS.getValue());
		assertEquals("Epic", NormalizedJira.ISSUE_TYPE.getValue());
	}
}
