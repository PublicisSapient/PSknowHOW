package com.publicissapient.kpidashboard.jira.fetchData;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.*;
import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.tracelog.PSLogData;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import com.publicissapient.kpidashboard.jira.adapter.impl.async.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;
import com.publicissapient.kpidashboard.jira.util.JiraProcessorUtil;
import io.atlassian.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
public class FetchIssuesBasedOnJQLImpl implements FetchIssuesBasedOnJQL{

    String QUERYDATEFORMAT = "yyyy-MM-dd HH:mm";
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
    private KanbanJiraIssueRepository kanbanJiraRepo;

//    @Autowired
//    private RabbitTemplate template;
    @Autowired
    private TransformFetchedIssueToJiraIssue transformFetchedIssue;

    @Autowired
    private JiraCommonService jiraCommonService;

    @Autowired
    ValidateData validateData;

    @Value("${rabbitmq.exchange.name}")
    String exchange;

    @Value("${rabbitmq.routing.key}")
    String routingKey;

    @Override
    public List<Issue> fetchIssues(Map.Entry<String, ProjectConfFieldMapping> entry, ProcessorJiraRestClient clientIncoming) throws JSONException {

        List<Issue> totalIssues = new ArrayList<>();
        ProjectConfFieldMapping projectConfig=entry.getValue();

        PSLogData psLogData = new PSLogData();
        psLogData.setProjectName(projectConfig.getProjectName());
        int total = 0;
        int savedIsuesCount = 0;

        client=clientIncoming;

        Map<String, LocalDateTime> lastSavedJiraIssueChangedDateByType = new HashMap<>();
        JiraHelper.setStartDate(jiraProcessorConfig);
        boolean processorFetchingComplete = false;

        try {
            boolean dataExist = false;
            if (projectConfig.isKanban()) {
                dataExist = (kanbanJiraRepo
                        .findTopByBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString()) != null);
                psLogData.setKanban("true");
            } else {
                dataExist = (jiraIssueRepository
                        .findTopByBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString()) != null);
                psLogData.setKanban("false");
            }


            Map<String, LocalDateTime> maxChangeDatesByIssueType = getLastChangedDatesByIssueType(
                    projectConfig.getBasicProjectConfigId(), projectConfig.getFieldMapping());

            Map<String, LocalDateTime> maxChangeDatesByIssueTypeWithAddedTime = new HashMap<>();

            maxChangeDatesByIssueType.forEach((k, v) -> {
                long extraMinutes = jiraProcessorConfig.getMinsToReduce();
                maxChangeDatesByIssueTypeWithAddedTime.put(k, v.minusMinutes(extraMinutes));
            });

            int pageSize = jiraCommonService.getPageSize();
            boolean hasMore = true;
            String userTimeZone = jiraCommonService.getUserTimeZone(projectConfig);

            for (int i = 0; hasMore; i += pageSize) {
                Instant startProcessingJiraIssues = Instant.now();
                SearchResult searchResult = getIssues(entry, maxChangeDatesByIssueTypeWithAddedTime,
                        userTimeZone, i, dataExist);
                List<Issue> issues = JiraHelper.getIssuesFromResult(searchResult);
                totalIssues.addAll(issues);
                if (total == 0) {
                    total = JiraHelper.getTotal(searchResult);
                    psLogData.setTotalFetchedIssues(String.valueOf(total));
                }

                if (CollectionUtils.isNotEmpty(issues)) {
                    List<JiraIssue> jiraIssues = transformFetchedIssue.convertToJiraIssue(issues, projectConfig, false);
                    JiraHelper.findLastSavedJiraIssueByType(jiraIssues,lastSavedJiraIssueChangedDateByType);
                    savedIsuesCount += issues.size();
                    jiraCommonService.savingIssueLogs(savedIsuesCount, jiraIssues, startProcessingJiraIssues,false,psLogData);
//                   template.convertAndSend(exchange, routingKey, jiraIssues);
                }

                if (issues.size() < pageSize) {
                    break;
                }
            }
            processorFetchingComplete = true;
        } catch (JSONException e) {
            log.error("Error while updating Story information in scrum client", e,
                    kv(CommonConstant.PSLOGDATA, psLogData));
            lastSavedJiraIssueChangedDateByType.clear();
        } catch (InterruptedException e) {
            log.error("Interrupted exception thrown.", e, kv(CommonConstant.PSLOGDATA, psLogData));
            lastSavedJiraIssueChangedDateByType.clear();
            processorFetchingComplete = false;
        } finally {
            validateData.check(total,savedIsuesCount,processorFetchingComplete,psLogData,lastSavedJiraIssueChangedDateByType,projectConfig);
        }
        return totalIssues;
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

    private SearchResult getIssues(Map.Entry<String, ProjectConfFieldMapping> entry,
                                  Map<String, LocalDateTime> startDateTimeByIssueType, String userTimeZone, int pageStart,
                                  boolean dataExist) throws InterruptedException{
        ProjectConfFieldMapping projectConfig=entry.getValue();
        SearchResult searchResult = null;

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
                                    Math.min(pageStart + jiraCommonService.getPageSize() - 1, searchResult.getTotal()), searchResult.getTotal()),
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

}
