package com.publicissapient.kpidashboard.apis.jira.service;

import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class JiraToolCredentialProviderForCustomApi implements ToolCredentialProvider {
    @Override
    public ToolCredential findCredential(String credRef) {
        return null;
    }
}
