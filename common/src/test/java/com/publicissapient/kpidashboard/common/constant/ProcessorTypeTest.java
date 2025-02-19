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

public class ProcessorTypeTest {

	@Test
	public void testEnumValues() {
		assertEquals("Build", ProcessorType.BUILD.toString());
		assertEquals("Feature", ProcessorType.FEATURE.toString()); // Deprecated, consider removing this enum constant
		assertEquals("SonarDetails", ProcessorType.SONAR_ANALYSIS.toString());
		assertEquals("Excel", ProcessorType.EXCEL.toString());
		assertEquals("AppPerformance", ProcessorType.APP_PERFORMANCE.toString());
		assertEquals("AgileTool", ProcessorType.AGILE_TOOL.toString());
		assertEquals("StaticSecurityScan", ProcessorType.STATIC_SECURITY_SCAN.toString());
		assertEquals("NewRelic", ProcessorType.NEW_RELIC.toString());
		assertEquals("Scm", ProcessorType.SCM.toString());
		assertEquals("TestingTools", ProcessorType.TESTING_TOOLS.toString());
	}
}
