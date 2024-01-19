package com.publicissapient.kpidashboard.common.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
