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

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

public class SonarMetricStatusTest {

	@Test
	public void testSonarMetricStatusEnumValues() {
		assertEquals(SonarMetricStatus.OK, SonarMetricStatus.valueOf("OK"));
		assertEquals(SonarMetricStatus.WARNING, SonarMetricStatus.valueOf("WARNING"));
		assertEquals(SonarMetricStatus.ALERT, SonarMetricStatus.valueOf("ALERT"));
	}

	@Test
	public void testSonarMetricStatusEnumToString() {
		assertEquals("OK", SonarMetricStatus.OK.toString());
		assertEquals("WARNING", SonarMetricStatus.WARNING.toString());
		assertEquals("ALERT", SonarMetricStatus.ALERT.toString());
	}
}
