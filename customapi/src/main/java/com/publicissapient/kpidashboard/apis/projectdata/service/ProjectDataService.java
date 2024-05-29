package com.publicissapient.kpidashboard.apis.projectdata.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.model.application.ProjectReleaseV2;
import com.publicissapient.kpidashboard.common.model.jira.DataRequest;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueV2;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetailsV2;

@Service
public interface ProjectDataService {
    List<JiraIssueV2> getProjectJiraIssues(DataRequest dataRequest);

    List<String> getIssueTypes(DataRequest dataRequest);

    List<SprintDetailsV2> getProjectSprints(DataRequest dataRequest);

    ProjectReleaseV2 getProjectReleases(DataRequest dataRequest);
}
