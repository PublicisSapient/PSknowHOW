package com.publicissapient.kpidashboard.jira.service;

import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class JiraToolCredentialProvider implements ToolCredentialProvider {
    @Override
    public ToolCredential findCredential(String credRef) {

        return null;
    }
}
