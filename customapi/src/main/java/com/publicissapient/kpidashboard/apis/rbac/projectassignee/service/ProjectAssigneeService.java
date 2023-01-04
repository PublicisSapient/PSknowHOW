package com.publicissapient.kpidashboard.apis.rbac.projectassignee.service;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.application.ProjectAssignee;

import java.util.List;

public interface ProjectAssigneeService {

    List<ProjectAssignee> getAllAssignees();

    ProjectAssignee saveProjectAssignee(ProjectAssignee projectAssignee);

    ProjectAssignee getAssigneeByProjectConfigId(String projectConfigid);
}
