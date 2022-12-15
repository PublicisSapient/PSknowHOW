package com.publicissapient.kpidashboard.common.model.tracelog;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Component
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PSLogData {
    private String projectName;
    private String projectKey;
    private String kanban;
    private String processorStartTime;
    private String processorEndTime;
    private String fieldMappingToDB;
    private String metaDataToDB;
    private String boardId;
    private String sprintId;
    private String totalFetchedIssues;
    private String totalSavedIssues;
    private String totalFetchedSprints;
    private String totalSavedSprints;
    private List<String> issueAndDesc;
    private String url;
    private String jql;
    private String timeTaken;
    private String executionStatus;
    private String userTimeZone;
    private List<String> sprintListFetched;
    private List<String> sprintListSaved;
    private List<String> epicListFetched;
    private List<String> projectVersion;
    private String executionEndedAt;
    private String executionStartedAt;
    private String lastSuccessfulRun;
    private String totalConfiguredProject;
    private List<String> lastSavedJiraIssueChangedDateByType;

}
