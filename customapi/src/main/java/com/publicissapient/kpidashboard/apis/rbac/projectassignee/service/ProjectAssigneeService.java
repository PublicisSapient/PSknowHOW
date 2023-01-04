package com.publicissapient.kpidashboard.apis.rbac.projectassignee.service;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.application.ProjectAssignee;

public interface ProjectAssigneeService {

	ServiceResponse getAllAssignees();

	ServiceResponse getAssigneeByProjectConfigId(String projectConfigid);

	ServiceResponse updateOrSaveAssineeByProjectConfigId(String projectConfigid, ProjectAssignee assignee);
}
