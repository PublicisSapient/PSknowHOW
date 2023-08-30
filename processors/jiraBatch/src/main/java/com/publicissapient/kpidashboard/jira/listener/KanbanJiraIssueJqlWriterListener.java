package com.publicissapient.kpidashboard.jira.listener;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.model.CompositeResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class KanbanJiraIssueJqlWriterListener implements ItemWriteListener<CompositeResult> {

    @Autowired
    private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepo;

    @Override
    public void beforeWrite(List<? extends CompositeResult> compositeResult) {

    }

    @Override
    public void afterWrite(List<? extends CompositeResult> compositeResults) {
        log.info("Saving status in Processor execution Trace log");

        List<ProcessorExecutionTraceLog> processorExecutionToSave = new ArrayList<>();
        List<KanbanJiraIssue> jiraIssues = compositeResults.stream().map(CompositeResult::getKanbanJiraIssue)
                .collect(Collectors.toList());

        Map<String, List<KanbanJiraIssue>> projectWiseIssues = jiraIssues.stream()
                .collect(Collectors.groupingBy(KanbanJiraIssue::getBasicProjectConfigId));

        for (Map.Entry<String, List<KanbanJiraIssue>> entry : projectWiseIssues.entrySet()) {
            String basicProjectConfigId = entry.getKey();
            KanbanJiraIssue firstIssue = entry.getValue().stream()
                    .sorted(Comparator
                            .comparing((KanbanJiraIssue jiraIssue) -> LocalDateTime.parse(jiraIssue.getChangeDate(),
                                    DateTimeFormatter.ofPattern(JiraConstants.JIRA_ISSUE_CHANGE_DATE_FORMAT)))
                            .reversed())
                    .findFirst().orElse(null);
            if (firstIssue != null) {
                Optional<ProcessorExecutionTraceLog> procTraceLog = processorExecutionTraceLogRepo
                        .findByProcessorNameAndBasicProjectConfigId(ProcessorConstants.JIRA, basicProjectConfigId);
                if (procTraceLog.isPresent()) {
                    ProcessorExecutionTraceLog processorExecutionTraceLog = procTraceLog.get();
                    setTraceLog(processorExecutionTraceLog, basicProjectConfigId, firstIssue.getChangeDate(),
                            processorExecutionToSave);
                } else {
                    ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
                    setTraceLog(processorExecutionTraceLog, basicProjectConfigId, firstIssue.getChangeDate(),
                            processorExecutionToSave);
                }
            }

        }
        if (CollectionUtils.isNotEmpty(processorExecutionToSave)) {
            processorExecutionTraceLogRepo.saveAll(processorExecutionToSave);
        }
    }

    private void setTraceLog(ProcessorExecutionTraceLog processorExecutionTraceLog, String basicProjectConfigId,
                             String changeDate, List<ProcessorExecutionTraceLog> processorExecutionToSave) {
        processorExecutionTraceLog.setBasicProjectConfigId(basicProjectConfigId);
        processorExecutionTraceLog.setExecutionSuccess(true);
        processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
        processorExecutionTraceLog.setLastSuccessfulRun(DateUtil.dateTimeConverter(changeDate,
                JiraConstants.JIRA_ISSUE_CHANGE_DATE_FORMAT, DateUtil.DATE_TIME_FORMAT));
        processorExecutionTraceLog.setProcessorName(JiraConstants.JIRA);
        processorExecutionToSave.add(processorExecutionTraceLog);
    }

    @Override
    public void onWriteError(Exception exception, List<? extends CompositeResult> compositeResult) {
        log.error("Exception occured while writing Kanban jira Issue ", exception);
    }
}
