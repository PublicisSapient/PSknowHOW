package com.publicissapient.kpidashboard.apis.rbac.projectassignee.service;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.application.ProjectAssignee;
import org.bson.types.ObjectId;

import java.util.List;

public interface ProjectAssigneeService {

    ServiceResponse getAllAssigness();

    ServiceResponse getAssigneeByProjectConfigId(String projectConfigid);

    ServiceResponse updateOrSaveAssineeByProjectConfigId(String projectConfigid, ProjectAssignee assignee);
}
