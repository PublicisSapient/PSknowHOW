package com.publicissapient.kpidashboard.jira.fetchData;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
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

//    @Autowired
//    private FetchSprintReport fetchSprintReport;

    @Override
    public List<Issue> fetchIssueBasedOnBoard(Map.Entry<String, ProjectConfFieldMapping> entry, ProcessorJiraRestClient clientIncoming){

        List<Issue> totalIssues = new ArrayList<>();
        ProjectConfFieldMapping projectConfig=entry.getValue();

        PSLogData psLogData = new PSLogData();
        psLogData.setProjectName(projectConfig.getProjectName());
        int total = 0;

        client=clientIncoming;

        try {

            boolean dataExist = false;
            if (projectConfig.isKanban()) {
                dataExist = (kanbanJiraRepo
                        .findTopByBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString()) != null);
                psLogData.setKanban("true");
            } else {
//                List<SprintDetails> sprintDetailsList=fetchSprintReport.createSprintDetailBasedOnBoard(projectConfig);
                dataExist = (jiraIssueRepository
                        .findTopByBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString()) != null);
                psLogData.setKanban("false");
            }

            ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLog(
                    projectConfig);
            //write get logic to fetch last successful updated date.
            String queryDate = getDeltaDate(processorExecutionTraceLog.getLastSuccessfulRun());
//            Set<SprintDetails> setForCacheClean = new HashSet<>();
            String userTimeZone = jiraCommonService.getUserTimeZone(projectConfig);
            List<BoardDetails> boardDetailsList = projectConfig.getProjectToolConfig().getBoards();
            for (BoardDetails board : boardDetailsList) {
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

                    if (issues.size() < pageSize) {
                        break;
                    }
                TimeUnit.MILLISECONDS.sleep(jiraProcessorConfig.getSubsequentApiCallDelayInMilli());
                }
            }
        } catch (InterruptedException e) {
            log.error("Interrupted exception thrown.", e, kv(CommonConstant.PSLOGDATA, psLogData));
        }
        return totalIssues;
    }

    public String getDeltaDate(String lastSuccessfulRun) {
        LocalDateTime ldt = DateUtil.stringToLocalDateTime(lastSuccessfulRun,QUERYDATEFORMAT);
        ldt = ldt.minusDays(30);
        return DateUtil.dateTimeFormatter(ldt, QUERYDATEFORMAT);
    }

    private ProcessorExecutionTraceLog createTraceLog(ProjectConfFieldMapping projectConfig) {
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
