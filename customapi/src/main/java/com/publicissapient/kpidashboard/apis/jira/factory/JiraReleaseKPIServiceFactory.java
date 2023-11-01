package com.publicissapient.kpidashboard.apis.jira.factory;

import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.releasedashboard.JiraReleaseKPIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JiraReleaseKPIServiceFactory {
    private static final Map<String, JiraReleaseKPIService<?, ?, ?>> JIRA_ITERATION_SERVICE_CACHE = new HashMap<>();
    @Autowired
    private List<JiraReleaseKPIService<?, ?, ?>> services;

    /**
     * This method return KPI service object on the basis of KPI Id.
     *
     * @param type KPI id
     * @return Jira Service object
     * @throws ApplicationException
     */
    @SuppressWarnings("rawtypes")
    public static JiraReleaseKPIService getJiraKPIService(String type) throws ApplicationException {
        JiraReleaseKPIService<?, ?, ?> service = JIRA_ITERATION_SERVICE_CACHE.get(type);
        if (service == null) {
            throw new ApplicationException(JiraReleaseKPIService.class, "Jira KPI Service Factory not initalized");
        }
        return service;
    }

    /**
     * This method put all available Jira services to Map where key is the KPI id
     * and value is the service object.
     */
    @PostConstruct
    public void initMyServiceCache() {
        for (JiraReleaseKPIService<?, ?, ?> service : services) {
            JIRA_ITERATION_SERVICE_CACHE.put(service.getQualifierType(), service);
        }
    }

}

