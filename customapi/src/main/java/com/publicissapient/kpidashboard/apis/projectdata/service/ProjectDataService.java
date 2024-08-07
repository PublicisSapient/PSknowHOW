package com.publicissapient.kpidashboard.apis.projectdata.service;

import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.jira.DataRequest;

@Service
public interface ProjectDataService {
    ServiceResponse getProjectJiraIssues(DataRequest dataRequest, int page, int size);

    ServiceResponse getIssueTypes(DataRequest dataRequest);

    ServiceResponse getProjectSprints(DataRequest dataRequest, boolean onlyActive);

    ServiceResponse getProjectReleases(DataRequest dataRequest);

    ServiceResponse getScrumProjects();
}
