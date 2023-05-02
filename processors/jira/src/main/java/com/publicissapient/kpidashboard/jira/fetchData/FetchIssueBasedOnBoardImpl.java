package com.publicissapient.kpidashboard.jira.fetchData;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.*;
import com.publicissapient.kpidashboard.common.model.tracelog.PSLogData;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import com.publicissapient.kpidashboard.jira.adapter.atlassianbespoke.client.CustomAsynchronousIssueRestClient;
import com.publicissapient.kpidashboard.jira.adapter.impl.async.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;
import io.atlassian.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
public class FetchIssueBasedOnBoardImpl implements FetchIssueBasedOnBoard {

    private static final String MSG_JIRA_CLIENT_SETUP_FAILED = "Jira client setup failed. No results obtained. Check your jira setup.";
    private static final String ERROR_MSG_401 = "Error 401 connecting to JIRA server, your credentials are probably wrong. Note: Ensure you are using JIRA user name not your email address.";
    private static final String ERROR_MSG_NO_RESULT_WAS_AVAILABLE = "No result was available from Jira unexpectedly - defaulting to blank response. The reason for this fault is the following : {}";
    private static final String NO_RESULT_QUERY = "No result available for query: {}";
    protected static final String QUERYDATEFORMAT = "yyyy-MM-dd HH:mm";

    @Autowired
    private JiraProcessorConfig jiraProcessorConfig;

    @Autowired
    private JiraIssueRepository jiraIssueRepository;

    PSLogData psLogData = new PSLogData();

    private ProcessorJiraRestClient client;

    @Autowired
    private KanbanJiraIssueRepository kanbanJiraRepo;

    @Autowired
    private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

    @Autowired
    private JiraCommonService jiraCommonService;

    @Autowired
    private TransformFetchedIssueToJiraIssue transformFetchedIssue;

    @Autowired
    private FetchSprintReport fetchSprintReport;

    @Autowired
    private CreateAccountHierarchy createAccountHierarchy;

    @Autowired
    private SaveData saveData;

    @Autowired
    private CreateAssigneeDetails createAssigneeDetails;
    @Autowired
    ValidateData validateData;

    @Override
    public List<Issue> fetchIssueBasedOnBoard(Map.Entry<String, ProjectConfFieldMapping> entry, ProcessorJiraRestClient clientIncoming, KerberosClient krb5Client){

        List<Issue> totalIssues = new ArrayList<>();
        ProjectConfFieldMapping projectConfig=entry.getValue();

        PSLogData psLogData = new PSLogData();
        psLogData.setProjectName(projectConfig.getProjectName());
        int total = 0;
        int savedIsuesCount = 0;

        Map<String, LocalDateTime> lastSavedJiraIssueChangedDateByType = new HashMap<>();
        JiraHelper.setStartDate(jiraProcessorConfig);
        boolean processorFetchingComplete = false;
        client=clientIncoming;
        ProcessorExecutionTraceLog processorExecutionTraceLog = jiraCommonService.createTraceLog(projectConfig);

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

            Set<SprintDetails> setForCacheClean = new HashSet<>();
            List<SprintDetails> sprintDetailsList=fetchSprintReport.createSprintDetailBasedOnBoard(projectConfig,setForCacheClean,krb5Client);
            saveData.saveData(null,null,sprintDetailsList,null,null);


            //write get logic to fetch last successful updated date.
            String queryDate = getDeltaDate(processorExecutionTraceLog.getLastSuccessfulRun());
            String userTimeZone = jiraCommonService.getUserTimeZone(projectConfig,krb5Client);
            List<BoardDetails> boardDetailsList = projectConfig.getProjectToolConfig().getBoards();

            int sprintCount = jiraProcessorConfig.getSprintCountForCacheClean();
            boolean latestDataFetched=false;

            for (BoardDetails board : boardDetailsList) {
                Instant startProcessingJiraIssues = Instant.now();
                psLogData.setBoardId(board.getBoardId());
                int boardTotal = 0;
                int pageSize = jiraCommonService.getPageSize();
                boolean hasMore = true;
                for (int i = 0; hasMore; i += pageSize) {

                    SearchResult searchResult = getIssues(board, projectConfig, queryDate,
                            userTimeZone, i, dataExist);
                    List<Issue> issues = JiraHelper.getIssuesFromResult(searchResult);
                    totalIssues.addAll(issues);
                    if (boardTotal == 0) {
                        boardTotal = JiraHelper.getTotal(searchResult);
                        total += boardTotal;
                        psLogData.setTotalFetchedIssues(String.valueOf(total));
                    }

                    if (CollectionUtils.isNotEmpty(issues)) {
                        List<JiraIssueCustomHistory> jiraIssueHistoryToSave = new ArrayList<>();
                        Set<SprintDetails> sprintDetailsSet=new HashSet<>();
                        Set<Assignee> assigneeSetToSave = new HashSet<>();
                        boolean dataFromBoard =true;
                        List<JiraIssue> jiraIssues = transformFetchedIssue.convertToJiraIssue(issues, projectConfig, dataFromBoard, jiraIssueHistoryToSave,sprintDetailsSet,assigneeSetToSave);
                        Set<AccountHierarchy> createAccountHierarchySet=createAccountHierarchy.createAccountHierarchy(jiraIssues,projectConfig);
                        AssigneeDetails assigneeDetails=createAssigneeDetails.createAssigneeDetails(projectConfig,assigneeSetToSave);
                        saveData.saveData(jiraIssues,jiraIssueHistoryToSave,sprintDetailsList,createAccountHierarchySet,assigneeDetails);
                        JiraHelper.findLastSavedJiraIssueByType(jiraIssues,lastSavedJiraIssueChangedDateByType);
                        savedIsuesCount += issues.size();
                        jiraCommonService.savingIssueLogs(savedIsuesCount, jiraIssues, startProcessingJiraIssues,false,psLogData);
                    }

                    if (!dataExist && !latestDataFetched && setForCacheClean.size() > sprintCount) {
                        latestDataFetched = jiraCommonService.cleanCache();
                        setForCacheClean.clear();
                        log.info("latest sprint fetched cache cleaned.");
                    }

                    if (issues.size() < pageSize) {
                        break;
                    }
                TimeUnit.MILLISECONDS.sleep(jiraProcessorConfig.getSubsequentApiCallDelayInMilli());
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
            validateData.check(total,savedIsuesCount,processorFetchingComplete,psLogData,lastSavedJiraIssueChangedDateByType,projectConfig,processorExecutionTraceLog);
        }
        return totalIssues;
    }

    public String getDeltaDate(String lastSuccessfulRun) {
        LocalDateTime ldt = DateUtil.stringToLocalDateTime(lastSuccessfulRun,QUERYDATEFORMAT);
        ldt = ldt.minusDays(30);
        return DateUtil.dateTimeFormatter(ldt, QUERYDATEFORMAT);
    }

    public SearchResult getIssues(BoardDetails boardDetails, ProjectConfFieldMapping projectConfig,
                                  String startDateTimeByIssueType, String userTimeZone, int pageStart,
                                  boolean dataExist) throws InterruptedException{
        SearchResult searchResult = null;

        if (client == null) {
            log.warn(MSG_JIRA_CLIENT_SETUP_FAILED);
        } else {
            String query = StringUtils.EMPTY;
            try {
                query = "updatedDate>='" + startDateTimeByIssueType + "' order by updatedDate desc";
                psLogData.setUserTimeZone(userTimeZone);
                psLogData.setJql(query);
                psLogData.setBoardId(boardDetails.getBoardId());
                Instant start = Instant.now();
                CustomAsynchronousIssueRestClient issueRestClient=client.getCustomIssueClient();
                Promise<SearchResult> promisedRs = issueRestClient.searchBoardIssue(boardDetails.getBoardId(), query,
                        jiraProcessorConfig.getPageSize(), pageStart, JiraConstants.ISSUE_FIELD_SET);
                searchResult = promisedRs.claim();
                psLogData.setTimeTaken(String.valueOf(Duration.between(start,Instant.now()).toMillis()));
                log.debug("jql query processed for board", kv(CommonConstant.PSLOGDATA,psLogData));
                if (searchResult != null) {
                    psLogData.setTotalFetchedIssues(String.valueOf(searchResult.getTotal()));
                    psLogData.setAction(CommonConstant.FETCHING_ISSUE);
                    log.info(String.format("Processing issues %d - %d out of %d", pageStart,
                                    Math.min(pageStart + jiraCommonService.getPageSize() - 1, searchResult.getTotal()), searchResult.getTotal()),
                            kv(CommonConstant.PSLOGDATA,psLogData));
                }
                TimeUnit.MILLISECONDS.sleep(jiraProcessorConfig.getSubsequentApiCallDelayInMilli());
            } catch (RestClientException e) {
                if (e.getStatusCode().isPresent() && e.getStatusCode().get() == 401) {
                    log.error(ERROR_MSG_401, kv(CommonConstant.PSLOGDATA,psLogData));
                } else {
                    log.info(NO_RESULT_QUERY, query,kv(CommonConstant.PSLOGDATA,psLogData));
                    log.error(ERROR_MSG_NO_RESULT_WAS_AVAILABLE, e.getCause(), kv(CommonConstant.PSLOGDATA,psLogData));
                }
            }

        }

        return searchResult;
    }
}
