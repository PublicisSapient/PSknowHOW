package com.publicissapient.kpidashboard.common.model.jira;

import com.publicissapient.kpidashboard.common.model.application.AdditionalFilter;
import com.publicissapient.kpidashboard.common.model.generic.BasicModel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("PMD.TooManyFields")
@Getter
@Setter
@Data
@Document(collection = "issue_backlog_custom_history")
public class IssueBacklogCustomHistory extends BasicModel {
    @Indexed
    private String projectID;
    @Indexed
    private String storyID;
    @Indexed
    private String storyType;

    private Set<String> defectStoryID;

    private String estimate;

    private Integer bufferedEstimateTime; // buffered estimate in days

    private DateTime createdDate;

    /**
     * Device Platform (iOS/Android/Desktop)
     */
    private String devicePlatform;
    private String projectKey;
    private String projectComponentId;

    private String developerId;
    private String developerName;
    private String qaId;
    private String qaName;

    private String buildId;
    private String buildNumber;

    private String projectName;
    private String basicProjectConfigId;
    //Used for azure processor
    private List<JiraIssueSprint> storySprintDetails = new ArrayList<>();
    private List<JiraHistoryChangeLog> statusUpdationLog = new ArrayList<>();
    private List<JiraHistoryChangeLog> assigneeUpdationLog = new ArrayList<>();
    private List<JiraHistoryChangeLog> priorityUpdationLog = new ArrayList<>();
    private List<JiraHistoryChangeLog> fixVersionUpdationLog = new ArrayList<>();
    private List<JiraHistoryChangeLog> labelUpdationLog = new ArrayList<>();
    private List<JiraHistoryChangeLog> dueDateUpdationLog = new ArrayList<>();
    private List<JiraHistoryChangeLog> sprintUpdationLog = new ArrayList<>();

    private List<AdditionalFilter> additionalFilters;

    private String url;
    private String description;

    @Override
    public String toString() {
        return "FeatureCustomHistory [projectID=" + projectID + ", storyID=" + storyID + ", url=" + url + ", storyType="
                + storyType + ", defectStoryID=" + defectStoryID + ", estimate=" + estimate + ", bufferedEstimateTime="
                + bufferedEstimateTime + ", devicePlatform=" + devicePlatform + ", projectKey=" + projectKey
                + ", projectComponentId=" + projectComponentId + ", statusUpdationLog=" + statusUpdationLog
                + ", assigneeUpdationLog=" + assigneeUpdationLog + ", priorityUpdationLog=" + priorityUpdationLog
                + ", fixVersionUpdationLog=" + fixVersionUpdationLog + ", labelUpdationLog=" + labelUpdationLog
                + ", dueDateUpdationLog=" + dueDateUpdationLog + ", sprintUpdationLog=" + sprintUpdationLog + "]";
    }

}
