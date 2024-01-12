package com.publicissapient.kpidashboard.jira.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OngoingExecutionsServiceTest {

    @InjectMocks
    private OngoingExecutionsService ongoingExecutionsService;

    @Before
    public void setUp() {
        // Initialize the service before test
    }

    @Test
    public void testIsExecutionInProgress() {
        // Arrange
        String projectConfigId = "project123";

        // Act
        boolean isInProgress = ongoingExecutionsService.isExecutionInProgress(projectConfigId);

        // Assert
        assertFalse("No execution should be in progress initially",isInProgress);
    }

    @Test
    public void testMarkExecutionInProgress() {
        // Arrange
        String projectConfigId = "project123";

        // Act
        ongoingExecutionsService.markExecutionInProgress(projectConfigId);

        // Assert
        assertTrue(
                "Execution should be marked as in progress",ongoingExecutionsService.isExecutionInProgress(projectConfigId));
    }

    @Test
    public void testMarkExecutionAsCompleted() {
        // Arrange
        String projectConfigId = "project123";
        ongoingExecutionsService.markExecutionInProgress(projectConfigId);

        // Act
        ongoingExecutionsService.markExecutionAsCompleted(projectConfigId);

        // Assert
        assertFalse(
                "Execution should be marked as completed",ongoingExecutionsService.isExecutionInProgress(projectConfigId));
    }
}
