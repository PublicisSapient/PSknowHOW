package com.publicissapient.kpidashboard.apis.rbac.projectassignee.service;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.application.ProjectAssignee;

public interface ProjectAssigneeService {

    ProjectAssignee getAllAssigness();

    ProjectAssignee getAssigneeByProjectConfigId(String projectConfigid);
}
