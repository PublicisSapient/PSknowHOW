package com.publicissapient.kpidashboard.common.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
