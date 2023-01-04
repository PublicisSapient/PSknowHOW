/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.rbac.projectassignee.service;

import com.publicissapient.kpidashboard.common.repository.rbac.ProjectAssigneeRepository;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.application.ProjectAssignee;
import com.publicissapient.kpidashboard.common.repository.rbac.ProjectAssigneeRepository;

import java.util.List;

@Service
@Slf4j
public class ProjectAssigneeServiceImpl implements ProjectAssigneeService {

	@Autowired
	private ProjectAssigneeRepository projectAssigneeRepository;

	@Override
	public ServiceResponse getAllAssignees() {
		List<ProjectAssignee> projectAssignee =  projectAssigneeRepository.findAll();
		if(CollectionUtils.isEmpty(projectAssignee)){
			return new ServiceResponse(false,"No Assignees Found",null);
		}
		return new ServiceResponse(true,"List of Assignees",projectAssignee);
	}


	@Override
	public ServiceResponse getAssigneeByProjectConfigId(String projectConfigid) {
		ProjectAssignee projectAssignee = projectAssigneeRepository.findByBasicProjectConfigId(new ObjectId(projectConfigid));
		if(projectAssignee==null){
			return new ServiceResponse(false,"No Assignees Found for project",null);
		}
		return new ServiceResponse(true,"Found Assignees for project",projectAssignee);
	}

	@Override
	public ServiceResponse updateOrSaveAssineeByProjectConfigId(String projectConfigid, ProjectAssignee assignee) {
		ProjectAssignee existingProjectAssignee = projectAssigneeRepository.findByBasicProjectConfigId(new ObjectId(projectConfigid));

		if (existingProjectAssignee == null) {
			projectAssigneeRepository.save(assignee);
			return new ServiceResponse(true, "Assignees Saved", assignee);
		}
		existingProjectAssignee = updateAssineeRoles(existingProjectAssignee, assignee);
		if (existingProjectAssignee == null) {
			return new ServiceResponse(false, "Unable to Update Role.", null);
		}
		projectAssigneeRepository.save(existingProjectAssignee);
		return new ServiceResponse(true, "Updated the Role Successfully", existingProjectAssignee);

	}

	public ProjectAssignee updateAssineeRoles(ProjectAssignee existingProjectAssignee, ProjectAssignee assignee) {
		existingProjectAssignee.setAssigneeRoles(assignee.getAssigneeRoles());
		return existingProjectAssignee;
	}


}