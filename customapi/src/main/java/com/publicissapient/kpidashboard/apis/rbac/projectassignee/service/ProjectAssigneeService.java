package com.publicissapient.kpidashboard.apis.rbac.projectassignee.service;

import com.publicissapient.kpidashboard.common.model.application.ProjectAssignee;

import java.util.List;

public interface ProjectAssigneeService {

    List<ProjectAssignee> getAllAssigness();

    ProjectAssignee getAssigneeByProjectConfigId(String projectConfigid);
}
