package com.publicissapient.kpidashboard.common.constant;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
