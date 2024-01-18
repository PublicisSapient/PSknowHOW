package com.publicissapient.kpidashboard.common.constant;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

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
