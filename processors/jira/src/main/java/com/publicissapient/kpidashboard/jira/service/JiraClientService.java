package com.publicissapient.kpidashboard.jira.service;

import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Service
public class JiraClientService {

    private ProcessorJiraRestClient restClient;

    private KerberosClient kerberosClient;
}
