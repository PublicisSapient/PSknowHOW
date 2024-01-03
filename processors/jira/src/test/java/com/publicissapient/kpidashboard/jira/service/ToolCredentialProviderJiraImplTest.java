package com.publicissapient.kpidashboard.jira.service;

import com.publicissapient.kpidashboard.common.model.ToolCredential;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ToolCredentialProviderJiraImplTest {

    @InjectMocks
    ToolCredentialProviderJiraImpl credentialProvider;

    @Test
    public void testFindCredential() {
        // Arrange
        String validCredRef = "validReference";

        // Act
        ToolCredential result = credentialProvider.findCredential(validCredRef);

        // Assert
        assertNull("Expected null for an invalid credential reference",result);
    }
}
