package com.publicissapient.kpidashboard.jira.service;

import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;


import java.util.concurrent.ConcurrentHashMap;

/**
 * @author purgupta2
 *
 */
@Service
public class JiraClientService {

    private final ConcurrentHashMap<String,ProcessorJiraRestClient> restClientMap= new ConcurrentHashMap<>();

    public boolean isContainRestClient(String basicProjectConfigId) {
        return restClientMap.containsKey(basicProjectConfigId);
    }

    public void setRestClientMap(String basicProjectConfigId,ProcessorJiraRestClient client) {
        restClientMap.put(basicProjectConfigId, client);
    }

    public ProcessorJiraRestClient getRestClientMap(String basicProjectConfigId) {
        return restClientMap.get(basicProjectConfigId);
    }

    public void removeRestClientMapClientForKey(String basicProjectConfigId) {
        restClientMap.remove(basicProjectConfigId);
    }

    private final ConcurrentHashMap<String,KerberosClient> kerberosClientMap= new ConcurrentHashMap<>();

    public boolean isContainKerberosClient(String basicProjectConfigId) {
        return kerberosClientMap.containsKey(basicProjectConfigId);
    }

    public void setKerberosClientMap(String basicProjectConfigId,KerberosClient client) {
        kerberosClientMap.put(basicProjectConfigId, client);
    }

    public KerberosClient getKerberosClientMap(String basicProjectConfigId) {
        return kerberosClientMap.get(basicProjectConfigId);
    }

    public void removeKerberosClientMapClientForKey(String basicProjectConfigId) {
        kerberosClientMap.remove(basicProjectConfigId);
    }
}
