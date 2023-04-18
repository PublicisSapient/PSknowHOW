package com.publicissapient.kpidashboard.jira.adapter.impl;

import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.jira.adapter.JiraAdapter;
import com.publicissapient.kpidashboard.jira.adapter.impl.async.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;

public interface OnlineAdapterFactory {
    JiraAdapter getOnlineAdapter(JiraProcessorConfig jiraProcessorConfig, ProcessorJiraRestClient client,
                                 AesEncryptionService aesEncryptionService
            , ToolCredentialProvider toolCredentialProvider);
}
