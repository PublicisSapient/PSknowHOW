package com.publicissapient.kpidashboard.apis.projectdata.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.model.application.MasterProjectRelease;
import com.publicissapient.kpidashboard.common.model.jira.DataRequest;
import com.publicissapient.kpidashboard.common.model.jira.MasterJiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.MasterSprintDetails;

@Service
public interface ProjectDataService {
    List<MasterJiraIssue> getProjectJiraIssues(DataRequest dataRequest);

    List<String> getIssueTypes(DataRequest dataRequest);

    List<MasterSprintDetails> getProjectSprints(DataRequest dataRequest);

    MasterProjectRelease getProjectReleases(DataRequest dataRequest);
}
