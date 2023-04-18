package com.publicissapient.kpidashboard.jira.adapter.impl;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.jira.adapter.JiraAdapter;
import com.publicissapient.kpidashboard.jira.adapter.helper.JiraRestClientFactory;
import com.publicissapient.kpidashboard.jira.adapter.impl.async.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.JiraInfo;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.oauth.JiraOAuthClient;
import com.publicissapient.kpidashboard.jira.oauth.JiraOAuthProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class OnlineAdapterFactoryImpl implements  OnlineAdapterFactory{

    public JiraAdapter getOnlineAdapter(JiraProcessorConfig jiraProcessorConfig, ProcessorJiraRestClient client,
                                        AesEncryptionService aesEncryptionService
                                        , ToolCredentialProvider toolCredentialProvider){
        JiraAdapter jiraAdapter = new OnlineAdapter(jiraProcessorConfig, client,
                aesEncryptionService, toolCredentialProvider);

        return jiraAdapter;
    }
}
