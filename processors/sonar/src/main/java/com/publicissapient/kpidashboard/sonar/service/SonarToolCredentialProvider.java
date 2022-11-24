package com.publicissapient.kpidashboard.sonar.service;

import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import org.springframework.stereotype.Service;

@Service
public class SonarToolCredentialProvider implements ToolCredentialProvider {
    @Override
    public ToolCredential findCredential(String credRef) {
        return null;
    }
}
