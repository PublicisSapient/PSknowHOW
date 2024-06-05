package com.publicissapient.kpidashboard.apis.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoggedTimePerIssue {
    private String projectConfigId;
    private String sprintId;
    private String storyId;
    private Double loggedTimeInHours;
}

