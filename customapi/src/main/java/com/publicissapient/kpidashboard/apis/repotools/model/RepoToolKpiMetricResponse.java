package com.publicissapient.kpidashboard.apis.repotools.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RepoToolKpiMetricResponse {

    @JsonProperty("project_name")
    private String projectName;
    @JsonProperty("project_code")
    private String projectCode;
    private double projectGrade;
    private long commitCount;
    private List<RepoToolRepositories> repositories;
    private List<RepoToolRepositories> projectRepositories;
    @JsonProperty("date_label")
    private String dateLabel;

}
