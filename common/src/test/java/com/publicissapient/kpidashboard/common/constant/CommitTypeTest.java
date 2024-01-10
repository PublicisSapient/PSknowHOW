package com.publicissapient.kpidashboard.common.constant;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CommitTypeTest {

    @Test
    public void testFromStringWithNotBuilt() {
        CommitType commitType = CommitType.fromString("NOT_BUILT");
        assertEquals(CommitType.NOT_BUILT, commitType);
    }

    @Test
    public void testFromStringWithMerge() {
        CommitType commitType = CommitType.fromString("MERGE");
        assertEquals(CommitType.MERGE, commitType);
    }

    @Test
    public void testFromStringWithNew() {
        CommitType commitType = CommitType.fromString("NEW");
        assertEquals(CommitType.NEW, commitType);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromStringWithInvalidValue() {
        CommitType.fromString("INVALID_COMMIT_TYPE");
    }
}
