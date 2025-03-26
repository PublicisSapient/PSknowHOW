package com.publicissapient.kpidashboard.rally.config;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Component
@ConfigurationProperties(prefix = "rally")
@Data
public class RallyProcessorConfig {
    private String cron;
    private String username;
    private String password;
    private String apiEndpoint;
    private int pageSize = 100;
    private int maxRetries = 3;
    private long retryDelay = 5000;
    private String[] workspaceIds;
    private String[] projectIds;
    private String[] storyTypes = {"HierarchicalRequirement", "Defect", "Task", "TestCase", "DefectSuite", "Feature"};
    private String[] statusTypes = {"Defined", "In-Progress", "Completed", "Accepted", "Backlog", "Ready", "InDevelopment", "Testing", "Done"};
    private String[] workflowStates = {"Development", "QA", "Delivered", "DOR", "DOD"};
    
    private String customApiBaseUrl;
    private Integer socketTimeOut;
    private int threadPoolSize;
    private Integer prevMonthCountToFetchData = 3;
    private Integer daysToReduce;
    private Integer chunkSize;
    private String uiHost;
    private String rallyApiBaseUrl;
    private String rallyApiKey;
    private boolean fetchMetadata;
    private long subsequentApiCallDelayInMilli;
    private List<String> rcaValuesForCodeIssue;
    private List<String> excludeLinks;
    private String jiraCloudGetUserApi;
    private String jiraServerGetUserApi;
    private String jiraCloudSprintReportApi;
    private String jiraServerSprintReportApi;
    private String jiraDirectTicketLinkKey;
    private String jiraCloudDirectTicketLinkKey;
    private String jiraSprintByBoardUrlApi;
    private String jiraEpicApi;
    private Integer sprintReportCountToBeFetched;
    private boolean considerStartDate;
    private Map<String, String> notificationSubject;
    private Map<String, String> mailTemplate;
    private String samlTokenStartString;
    private String samlTokenEndString;
    private String samlUrlStartString;
    private String samlUrlEndString;
    private String jiraVersionApi;
    private String jiraCloudVersionApi;
    private String jiraServerVersionReportApi;
    private String jiraCloudVersionReportApi;
    private List<String> domainNames;

    @Value("${aesEncryptionKey}")
    private String aesEncryptionKey;

    @Value("${notification.switch}")
    private boolean notificationSwitch;

    @Value("${flag.mailWithoutKafka}")
    private boolean mailWithoutKafka;

    @Value("${kafka.mailtopic}")
    private String kafkaMailTopic;

    public List<String> getDomainNames() {
        return domainNames;
    }

    public void setDomainNames(List<String> domainNames) {
        this.domainNames = domainNames;
    }
}
