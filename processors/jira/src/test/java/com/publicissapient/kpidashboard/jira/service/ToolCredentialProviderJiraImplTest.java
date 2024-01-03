package com.publicissapient.kpidashboard.jira.service;

import com.publicissapient.kpidashboard.common.model.ToolCredential;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.*;

public class ToolCredentialProviderJiraImplTest {

    @Test
    public void testFindCredential() {
        // Arrange
        ToolCredentialProviderJiraImpl credentialProvider = new ToolCredentialProviderJiraImpl();
        String validCredRef = "validReference";

        // Act
        ToolCredential result = credentialProvider.findCredential(validCredRef);

        // Assert
        assertNull(result, "Expected null for an invalid credential reference");
    }
}
