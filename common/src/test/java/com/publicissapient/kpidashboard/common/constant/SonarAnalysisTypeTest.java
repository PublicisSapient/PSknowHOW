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

public class SonarAnalysisTypeTest {

	@Test
	public void testEnumValues() {
		assertEquals(ProcessorType.SONAR_ANALYSIS, SonarAnalysisType.STATIC_ANALYSIS.processorType());
		assertEquals(ProcessorType.STATIC_SECURITY_SCAN, SonarAnalysisType.SECURITY_ANALYSIS.processorType());
	}

	@Test
	public void testFromString() {
		assertEquals(SonarAnalysisType.STATIC_ANALYSIS, SonarAnalysisType.fromString("STATIC_ANALYSIS"));
		assertEquals(SonarAnalysisType.SECURITY_ANALYSIS, SonarAnalysisType.fromString("SECURITY_ANALYSIS"));
	}
}
