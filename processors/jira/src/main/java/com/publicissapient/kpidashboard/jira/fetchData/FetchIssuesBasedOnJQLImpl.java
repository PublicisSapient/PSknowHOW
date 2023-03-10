package com.publicissapient.kpidashboard.jira.fetchData;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.*;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.tracelog.PSLogData;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.jira.adapter.helper.JiraRestClientFactory;
import com.publicissapient.kpidashboard.jira.adapter.impl.async.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.JiraInfo;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.oauth.JiraOAuthClient;
import com.publicissapient.kpidashboard.jira.oauth.JiraOAuthProperties;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;
import com.publicissapient.kpidashboard.jira.util.JiraProcessorUtil;
import io.atlassian.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
public class FetchIssuesBasedOnJQLImpl implements FetchIssuesBasedOnJQL{

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    private static final String MSG_JIRA_CLIENT_SETUP_FAILED = "Jira client setup failed. No results obtained. Check your jira setup.";
    private static final String ERROR_MSG_401 = "Error 401 connecting to JIRA server, your credentials are probably wrong. Note: Ensure you are using JIRA user name not your email address.";
    private static final String ERROR_MSG_NO_RESULT_WAS_AVAILABLE = "No result was available from Jira unexpectedly - defaulting to blank response. The reason for this fault is the following : {}";
    private static final String NO_RESULT_QUERY = "No result available for query: {}";

    @Autowired
    private JiraProcessorConfig jiraProcessorConfig;

    @Autowired
    private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

    @Autowired
    private JiraIssueRepository jiraIssueRepository;

    PSLogData psLogData = new PSLogData();

    private ProcessorJiraRestClient client;

    @Autowired
    private ToolCredentialProvider toolCredentialProvider;

    @Autowired
    private AesEncryptionService aesEncryptionService;

    @Autowired
    private ConnectionRepository connectionRepository;

    @Autowired
    private ProjectToolConfigRepository toolRepository;

    @Autowired
    private ProjectBasicConfigRepository projectConfigRepository;

    @Autowired
    private JiraRestClientFactory jiraRestClientFactory;

    @Autowired
    private JiraOAuthProperties jiraOAuthProperties;

    @Autowired
    private JiraOAuthClient jiraOAuthClient;

    @Autowired
    private KanbanJiraIssueRepository kanbanJiraRepo;

    @Autowired
    private RabbitTemplate template;

    @Autowired
    private TransformFetchedIssueToJiraIssue transformFetchedIssue;

    @Autowired
    private JiraCommon jiraCommon;

    @Value("${rabbitmq.exchange.name}")
    String exchange;
    @Value("${rabbitmq.routing.key}")
    String routingKey;

    @Override
    public List<Issue> fetchIssues(Map.Entry<String, ProjectConfFieldMapping> entry) throws InterruptedException, JSONException {
        List<Issue> issues = new ArrayList<>();
        ProjectConfFieldMapping projectConfig=entry.getValue();

        boolean dataExist=false;
        if (projectConfig.isKanban()) {
            dataExist = (kanbanJiraRepo
                    .findTopByBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString()) != null);
        }
        else {
            dataExist = (jiraIssueRepository
                    .findTopByBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString()) != null);
        }


        Map<String, LocalDateTime> maxChangeDatesByIssueType = getLastChangedDatesByIssueType(
                projectConfig.getBasicProjectConfigId(), projectConfig.getFieldMapping());

        Map<String, LocalDateTime> maxChangeDatesByIssueTypeWithAddedTime = new HashMap<>();

        maxChangeDatesByIssueType.forEach((k, v) -> {
            long extraMinutes = jiraProcessorConfig.getMinsToReduce();
            maxChangeDatesByIssueTypeWithAddedTime.put(k, v.minusMinutes(extraMinutes));
        });

        int pageSize = getPageSize();
        boolean hasMore = true;
        String userTimeZone = getUserTimeZone(projectConfig);

        Set<SprintDetails> setForCacheClean = new HashSet<>();

        for (int i = 0; hasMore; i += pageSize) {
            SearchResult searchResult = getIssues(entry, maxChangeDatesByIssueTypeWithAddedTime,
                    userTimeZone, i, dataExist);
            issues = getIssuesFromResult(searchResult);

            if (CollectionUtils.isNotEmpty(issues)) {
                List<JiraIssue> jiraIssues=transformFetchedIssue.convertToJiraIssue(issues, projectConfig, setForCacheClean, false);
                template.convertAndSend(exchange,routingKey,jiraIssues);
            }

			if (issues.size() < pageSize) {
				break;
			}
        }
        return issues;
    }
    private Map<String, LocalDateTime> getLastChangedDatesByIssueType(ObjectId basicProjectConfigId,
                                                                      FieldMapping fieldMapping) {

        String[] jiraIssueTypeNames = fieldMapping.getJiraIssueTypeNames();
        Set<String> uniqueIssueTypes = new HashSet<>(Arrays.asList(jiraIssueTypeNames));

        Map<String, LocalDateTime> lastUpdatedDateByIssueType = new HashMap<>();

        List<ProcessorExecutionTraceLog> traceLogs = processorExecutionTraceLogService
                .getTraceLogs(ProcessorConstants.JIRA, basicProjectConfigId.toHexString());
        ProcessorExecutionTraceLog projectTraceLog = null;

        if (CollectionUtils.isNotEmpty(traceLogs)) {
            projectTraceLog = traceLogs.get(0);
        }

        LocalDateTime configuredStartDate = LocalDateTime.parse(jiraProcessorConfig.getStartDate(),
                DateTimeFormatter.ofPattern(QUERYDATEFORMAT));

        for (String issueType : uniqueIssueTypes) {

            if (projectTraceLog != null) {
                Map<String, LocalDateTime> lastSavedEntryUpdatedDateByType = projectTraceLog
                        .getLastSavedEntryUpdatedDateByType();
                if (MapUtils.isNotEmpty(lastSavedEntryUpdatedDateByType)) {
                    LocalDateTime maxDate = lastSavedEntryUpdatedDateByType.get(issueType);
                    lastUpdatedDateByIssueType.put(issueType, maxDate != null ? maxDate : configuredStartDate);
                } else {
                    lastUpdatedDateByIssueType.put(issueType, configuredStartDate);
                }

            } else {
                lastUpdatedDateByIssueType.put(issueType, configuredStartDate);
            }
        }

        return lastUpdatedDateByIssueType;
    }

    private List<Issue> getIssuesFromResult(SearchResult searchResult) {
        if (searchResult != null) {
            return Lists.newArrayList(searchResult.getIssues());
        }
        return new ArrayList<>();
    }

    public SearchResult getIssues(Map.Entry<String, ProjectConfFieldMapping> entry,
                                  Map<String, LocalDateTime> startDateTimeByIssueType, String userTimeZone, int pageStart,
                                  boolean dataExist) throws InterruptedException{
        ProjectConfFieldMapping projectConfig=entry.getValue();
        SearchResult searchResult = null;
        client=getClient(entry);

        if (client == null) {
            log.warn(MSG_JIRA_CLIENT_SETUP_FAILED);
        } else if (StringUtils.isEmpty(projectConfig.getProjectToolConfig().getProjectKey()) ||
                StringUtils.isEmpty(projectConfig.getProjectToolConfig().getBoardQuery())) {
            log.info("Either Project key is empty or boardQuery not provided. key {} boardquery {}"
                    , projectConfig.getProjectToolConfig().getProjectKey(), projectConfig.getProjectToolConfig().getBoardQuery());
        } else {
            StringBuilder query = new StringBuilder("project in (")
                    .append(projectConfig.getProjectToolConfig().getProjectKey()).append(") AND ");
            try {
                Map<String, String> startDateTimeStrByIssueType = new HashMap<>();

                startDateTimeByIssueType.forEach((type, localDateTime) -> {
                    ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of(userTimeZone));
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
                    String dateTimeStr = zonedDateTime.format(formatter);
                    startDateTimeStrByIssueType.put(type, dateTimeStr);

                });

                query.append(JiraProcessorUtil.processJql(projectConfig.getJira().getBoardQuery(),
                        startDateTimeStrByIssueType, dataExist));
                psLogData.setUserTimeZone(userTimeZone);
                psLogData.setSprintId(null);
                psLogData.setJql(query.toString());
                Instant start = Instant.now();
                Promise<SearchResult> promisedRs = client.getProcessorSearchClient().searchJql(query.toString(),
                        jiraProcessorConfig.getPageSize(), pageStart, JiraConstants.ISSUE_FIELD_SET);
                searchResult = promisedRs.claim();
                psLogData.setTimeTaken(String.valueOf(Duration.between(start,Instant.now()).toMillis()));
                log.debug("jql query processed for JQL", kv(CommonConstant.PSLOGDATA, psLogData));
                if (searchResult != null) {
                    psLogData.setTotalFetchedIssues(String.valueOf(searchResult.getTotal()));
                    psLogData.setAction(CommonConstant.FETCHING_ISSUE);
                    log.info(String.format("Processing issues %d - %d out of %d", pageStart,
                                    Math.min(pageStart + getPageSize() - 1, searchResult.getTotal()), searchResult.getTotal()),
                            kv(CommonConstant.PSLOGDATA, psLogData));
                }
                TimeUnit.MILLISECONDS.sleep(jiraProcessorConfig.getSubsequentApiCallDelayInMilli());
            } catch (RestClientException e) {
                if (e.getStatusCode().isPresent() && e.getStatusCode().get() == 401) {
                    log.error(ERROR_MSG_401);
                } else {
                    log.info(NO_RESULT_QUERY, query);
                    log.error(ERROR_MSG_NO_RESULT_WAS_AVAILABLE, e.getCause());
                }
            }

        }

        return searchResult;
    }

    private ProcessorJiraRestClient getClient(Map.Entry<String, ProjectConfFieldMapping> entry){
        ProjectConfFieldMapping projectConfig=entry.getValue();
        List<ProjectBasicConfig> projectConfigList = getSelectedProjects();
        List<ProjectToolConfig> jiraDetails = toolRepository.findByToolNameAndBasicProjectConfigId(
                ProcessorConstants.JIRA, projectConfig.getBasicProjectConfigId());
        if (CollectionUtils.isNotEmpty(jiraDetails) && jiraDetails.get(0).getConnectionId() != null) {
            Optional<Connection> jiraConn = connectionRepository.findById(jiraDetails.get(0).getConnectionId());
            if (jiraConn.isPresent() && projectConfig.getJira().getConnection().isPresent()) {
                projectConfig.setProjectToolConfig(jiraDetails.get(0));
                boolean isOauth = jiraConn.get().getIsOAuth();
                Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();
                if (connectionOptional.isPresent()) {
                    Connection conn = connectionOptional.get();
                    client = getProcessorJiraRestClient(projectConfigList, entry, isOauth, conn);
                }}}
        return client;
    }

    private List<ProjectBasicConfig> getSelectedProjects() {
        List<ProjectBasicConfig> allProjects = projectConfigRepository.findAll();

        psLogData.setTotalConfiguredProject(String.valueOf(CollectionUtils.emptyIfNull(allProjects).size()));
        List<String> selectedProjectsBasicIds = getProjectsBasicConfigIds();
        if (CollectionUtils.isEmpty(selectedProjectsBasicIds)) {
            return allProjects;
        }

        return CollectionUtils.emptyIfNull(allProjects).stream().filter(
                        projectBasicConfig -> selectedProjectsBasicIds.contains(projectBasicConfig.getId().toHexString()))
                .collect(Collectors.toList());

    }

    public List<String> getProjectsBasicConfigIds() {
        return Arrays.asList("63bfa0d5b7617e260763ca21");
    }

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
                userTimeZone = getUserTimeZone2(jiraCommon.getDataFromServer(projectConfig, (HttpURLConnection) connection));
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

    private String getUserTimeZone2(String timezoneObj) {
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

    private ProcessorJiraRestClient getProcessorJiraRestClient(List<ProjectBasicConfig> projectConfigList, Map.Entry<String, ProjectConfFieldMapping> entry, boolean isOauth, Connection conn) {
        ProcessorJiraRestClient client;

        String username = "";
        String password = "";
        if (conn.isVault()) {
            ToolCredential toolCredential = toolCredentialProvider.findCredential(conn.getUsername());
            if(toolCredential != null){
                username = toolCredential.getUsername();
                password = toolCredential.getPassword();
            }

        } else {
            username = conn.getUsername();
            password = jiraCommon.decryptJiraPassword(conn.getPassword());
        }


        if (isOauth) {
            // Sets Jira OAuth properties
            jiraOAuthProperties.setJiraBaseURL(conn.getBaseUrl());
            jiraOAuthProperties.setConsumerKey(conn.getConsumerKey());
            jiraOAuthProperties.setPrivateKey(jiraCommon.decryptJiraPassword(conn.getPrivateKey()));

            // Generate and save accessToken
            saveAccessToken(entry);
            jiraOAuthProperties.setAccessToken(conn.getAccessToken());

            client = jiraRestClientFactory.getJiraOAuthClient(JiraInfo.builder()
                    .jiraConfigBaseUrl(conn.getBaseUrl()).username(username)
                    .password(password)
                    .jiraConfigAccessToken(conn.getAccessToken()).jiraConfigProxyUrl(null)
                    .jiraConfigProxyPort(null).build());

        } else {

            client = jiraRestClientFactory.getJiraClient(JiraInfo.builder()
                    .jiraConfigBaseUrl(conn.getBaseUrl()).username(username)
                    .password(password).jiraConfigProxyUrl(null)
                    .jiraConfigProxyPort(null).build());

        }
        return client;
    }

    public void saveAccessToken(Map.Entry<String, ProjectConfFieldMapping> entry) {
        Optional<Connection> connectionOptional = entry.getValue().getJira().getConnection();
        if (connectionOptional.isPresent()) {
            Optional<String> checkNull = Optional
                    .ofNullable(connectionOptional.get().getAccessToken());
            if (!checkNull.isPresent() || checkNull.get().isEmpty()) {

                JiraToolConfig jiraToolConfig = entry.getValue().getJira();
                generateAndSaveAccessToken(jiraToolConfig);
            }
        }
    }

    private void generateAndSaveAccessToken(JiraToolConfig jiraToolConfig) {

        Optional<Connection> connectionOptional = jiraToolConfig.getConnection();
        if (connectionOptional.isPresent()) {
            String username = connectionOptional.get().getUsername();
            String plainTextPassword = jiraCommon.decryptJiraPassword(connectionOptional.get().getPassword());

            String accessToken;
            try {
                accessToken = jiraOAuthClient.getAccessToken(username, plainTextPassword);
                connectionOptional.get().setAccessToken(accessToken);
                connectionRepository.save(connectionOptional.get());
            } catch (FailingHttpStatusCodeException e) {
                log.error("HTTP Status code error while generating accessToken", e);
            } catch (MalformedURLException e) {
                log.error("Malformed URL error while generating accessToken", e);
            } catch (IOException e) {
                log.error("Error while generating accessToken", e);
            }
        }
    }


}
