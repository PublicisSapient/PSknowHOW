package com.publicissapient.kpidashboard.common.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;

public class JiraIssueComparatorTest {

    @Test
    public void testCompare() {
        JiraIssueComparator comparator = new JiraIssueComparator();

        JiraIssue issue1 = new JiraIssue();
        issue1.setEpicID("EPIC-100");

        JiraIssue issue2 = new JiraIssue();
        issue2.setEpicID("EPIC-200");

        JiraIssue issue3 = new JiraIssue();
        issue3.setEpicID("EPIC-50");

        List<JiraIssue> issues = Arrays.asList(issue1, issue2, issue3);

        // Sorting in descending order based on EpicID
        Collections.sort(issues, comparator);
        assertEquals("EPIC-50", issues.get(2).getEpicID());
        assertEquals("EPIC-200", issues.get(1).getEpicID());
        assertEquals("EPIC-100", issues.get(0).getEpicID());

    }

    @Test
    public void testCompareWithEqualEpicID() {
        JiraIssueComparator comparator = new JiraIssueComparator();

        JiraIssue issue1 = new JiraIssue();
        issue1.setEpicID("EPIC-100");

        JiraIssue issue2 = new JiraIssue();
        issue2.setEpicID("EPIC-100");

        List<JiraIssue> issues = Arrays.asList(issue1, issue2);

        assertEquals(0, comparator.compare(issue1, issue2));
    }

    @Test
    public void testCompareWithNegativeEpicID() {
        JiraIssueComparator comparator = new JiraIssueComparator();

        JiraIssue issue1 = new JiraIssue();
        issue1.setEpicID("EPIC-101");

        JiraIssue issue2 = new JiraIssue();
        issue2.setEpicID("EPIC-200");

        List<JiraIssue> issues = Arrays.asList(issue1, issue2);

        assertEquals(-1, comparator.compare(issue1, issue2));
    }
}
