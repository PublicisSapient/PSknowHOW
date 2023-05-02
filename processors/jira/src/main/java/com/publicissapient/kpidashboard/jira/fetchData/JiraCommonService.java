package com.publicissapient.kpidashboard.jira.fetchData;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.tracelog.PSLogData;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
public class JiraCommonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraCommonService.class);

    @Autowired
    private JiraProcessorConfig jiraProcessorConfig;

    @Autowired
    private ToolCredentialProvider toolCredentialProvider;

    @Autowired
    private AesEncryptionService aesEncryptionService;

    @Autowired
    private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

    public int getPageSize() {
        return jiraProcessorConfig.getPageSize();
    }

    public String getUserTimeZone(ProjectConfFieldMapping projectConfig) {
        String userTimeZone = StringUtils.EMPTY;
        try {
            JiraToolConfig jiraToolConfig = projectConfig.getJira();

            if (null != jiraToolConfig) {
                Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();
                String username = connectionOptional.map(Connection::getUsername).orElse(null);
                URL url = getUrl(projectConfig, username);
                URLConnection connection;

                connection = url.openConnection();
                userTimeZone = getUserTimeZone(getDataFromServer(projectConfig, (HttpURLConnection) connection));
            }
        } catch (RestClientException rce) {
            log.error("Client exception when loading statuses", rce);
            throw rce;
        } catch (MalformedURLException mfe) {
            log.error("Malformed url for loading statuses", mfe);
        } catch (IOException ioe) {
            log.error("IOException", ioe);
        }

        return userTimeZone;
    }

    private String getUserTimeZone(String timezoneObj) {
        String userTimeZone = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(timezoneObj)) {
            try {
                Object obj = new JSONParser().parse(timezoneObj);
                JSONArray userInfoList = new JSONArray();
                userInfoList.add(obj);
                for (Object userInfo : userInfoList) {
                    JSONArray jsonUserInfo = (JSONArray) userInfo;
                    for (Object timeZone : jsonUserInfo) {
                        JSONObject timeZoneObj = (JSONObject) timeZone;
                        userTimeZone = (String) timeZoneObj.get("timeZone");
                    }
                }

            } catch (ParseException pe) {
                log.error("Parser exception when parsing statuses", pe);
            }
        }
        return userTimeZone;
    }

    private URL getUrl(ProjectConfFieldMapping projectConfig, String jiraUserName) throws MalformedURLException {

        Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();
        boolean isCloudEnv = connectionOptional.map(Connection::isCloudEnv).orElse(false);
        String serverURL = jiraProcessorConfig.getJiraServerGetUserApi();
        if (isCloudEnv) {
            serverURL = jiraProcessorConfig.getJiraCloudGetUserApi();
        }

        String baseUrl = connectionOptional.map(Connection::getBaseUrl).orElse("");
        String apiEndPoint = connectionOptional.map(Connection::getApiEndPoint).orElse("");

        return new URL(baseUrl + (baseUrl.endsWith("/") ? "" : "/") + apiEndPoint
                + (apiEndPoint.endsWith("/") ? "" : "/") + serverURL + jiraUserName);

    }

    public String getDataFromServer(ProjectConfFieldMapping projectConfig, HttpURLConnection connection)
            throws IOException {
        HttpURLConnection request = connection;
        Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();

        String username = null;
        String password = null;

        if(connectionOptional.isPresent()) {
            Connection conn = connectionOptional.get();
            if (conn.isVault()) {
                ToolCredential toolCredential = toolCredentialProvider.findCredential(conn.getUsername());
                if (toolCredential != null) {
                    username = toolCredential.getUsername();
                    password = toolCredential.getPassword();
                }

            } else {
                username = connectionOptional.map(Connection::getUsername).orElse(null);
                password = decryptJiraPassword(connectionOptional.map(Connection::getPassword).orElse(null));
            }
        }
        request.setRequestProperty("Authorization", "Basic " + encodeCredentialsToBase64(username, password)); // NOSONAR
        request.connect();
        StringBuilder sb = new StringBuilder();
        try (InputStream in = (InputStream) request.getContent();
             BufferedReader inReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));) {
            int cp;
            while ((cp = inReader.read()) != -1) {
                sb.append((char) cp);
            }
        } catch (IOException ie) {
            log.error("Read exception when connecting to server {}", ie);
        }
        return sb.toString();
    }

    public String decryptJiraPassword(String encryptedPassword) {
        return aesEncryptionService.decrypt(encryptedPassword, jiraProcessorConfig.getAesEncryptionKey());
    }

    public String encodeCredentialsToBase64(String username, String password) {
        String cred = username + ":" + password;
        return Base64.getEncoder().encodeToString(cred.getBytes());
    }

    public boolean cleanCache() {
        boolean accountHierarchyCleaned = cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT,
                CommonConstant.CACHE_ACCOUNT_HIERARCHY);
        boolean kpiDataCleaned = cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT,
                CommonConstant.JIRA_KPI_CACHE);
        return accountHierarchyCleaned && kpiDataCleaned;
    }

    public boolean cacheRestClient(String cacheEndPoint, String cacheName) {
        boolean cleaned = false;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(jiraProcessorConfig.getCustomApiBaseUrl());
        uriBuilder.path("/");
        uriBuilder.path(cacheEndPoint);
        uriBuilder.path("/");
        uriBuilder.path(cacheName);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, entity, String.class);
        } catch (RuntimeException e) {
            LOGGER.error("[JIRA-CUSTOMAPI-CACHE-EVICT]. Error while consuming rest service", e);
        }

        if (null != response && response.getStatusCode().is2xxSuccessful()) {
            cleaned = true;
            LOGGER.info("[JIRA-CUSTOMAPI-CACHE-EVICT]. Successfully evicted cache {}", cacheName);
        } else {
            LOGGER.error("[JIRA-CUSTOMAPI-CACHE-EVICT]. Error while evicting cache {}", cacheName);
        }
        return cleaned;
    }

    public void savingIssueLogs(int savedIssuesCount, List<JiraIssue> jiraIssues, Instant startProcessingJiraIssues,
                                 boolean isEpic, PSLogData psLogData) {
        PSLogData saveIssueLog = new PSLogData();
        saveIssueLog.setIssueAndDesc(jiraIssues.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));
        saveIssueLog.setTotalSavedIssues(String.valueOf(savedIssuesCount));
        psLogData.setTotalSavedIssues(String.valueOf(savedIssuesCount));
        psLogData.setTimeTaken(String.valueOf(Duration.between(startProcessingJiraIssues, Instant.now()).toMillis()));
        psLogData.setSprintListFetched(null);
        psLogData.setTotalFetchedSprints(null);
        if (!isEpic) {
            saveIssueLog.setAction(CommonConstant.SAVED_ISSUES);
            psLogData.setAction(CommonConstant.SAVED_ISSUES);
            saveIssueLog.setTotalFetchedIssues(psLogData.getTotalFetchedIssues());
            log.debug("Saved Issues for project {}", MDC.get(CommonConstant.PROJECTNAME),
                    kv(CommonConstant.PSLOGDATA, saveIssueLog));
            log.info("Processed Issues for project {}", MDC.get(CommonConstant.PROJECTNAME),
                    kv(CommonConstant.PSLOGDATA, psLogData));
        } else {
            saveIssueLog.setAction(CommonConstant.SAVED_EPIC_ISSUES);
            psLogData.setAction(CommonConstant.SAVED_EPIC_ISSUES);
            saveIssueLog.setEpicIssuesFetched(psLogData.getEpicIssuesFetched());
            log.debug("Saved Epic Issues for project {}", MDC.get(CommonConstant.PROJECTNAME),
                    kv(CommonConstant.PSLOGDATA, saveIssueLog));
            log.info("Processed Epic Issues for project {}", MDC.get(CommonConstant.PROJECTNAME),
                    kv(CommonConstant.PSLOGDATA, psLogData));

        }
    }

    public ProcessorExecutionTraceLog createTraceLog(ProjectConfFieldMapping projectConfig) {
        List<ProcessorExecutionTraceLog> traceLogs = processorExecutionTraceLogService
                .getTraceLogs(ProcessorConstants.JIRA, projectConfig.getBasicProjectConfigId().toHexString());
        ProcessorExecutionTraceLog processorExecutionTraceLog = null;

        if (CollectionUtils.isNotEmpty(traceLogs)) {
            processorExecutionTraceLog = traceLogs.get(0);
            if (null == processorExecutionTraceLog.getLastSuccessfulRun() || projectConfig.getProjectBasicConfig()
                    .isSaveAssigneeDetails() != processorExecutionTraceLog.isLastEnableAssigneeToggleState()) {
                processorExecutionTraceLog.setLastSuccessfulRun(jiraProcessorConfig.getStartDate());
            }
        } else {
            processorExecutionTraceLog = new ProcessorExecutionTraceLog();
            processorExecutionTraceLog.setProcessorName(ProcessorConstants.JIRA);
            processorExecutionTraceLog.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toHexString());
            processorExecutionTraceLog.setExecutionStartedAt(System.currentTimeMillis());
            processorExecutionTraceLog.setLastSuccessfulRun(jiraProcessorConfig.getStartDate());
        }
        return processorExecutionTraceLog;
    }

}
