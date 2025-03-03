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
package com.publicissapient.kpidashboard.rally.processor;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.rally.model.HierarchicalRequirement;
import com.publicissapient.kpidashboard.rally.model.Iteration;
import com.publicissapient.kpidashboard.rally.model.ProjectConfFieldMapping;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;

import lombok.extern.slf4j.Slf4j;

/**
 * @author pankumar8
 */
@Slf4j
@Service
public class SprintDataProcessorImpl implements SprintDataProcessor {

	@Autowired
	private SprintRepository sprintRepository;

	@Override
	public Set<SprintDetails> processSprintData(HierarchicalRequirement hierarchicalRequirement, ProjectConfFieldMapping projectConfig, String boardId,
												ObjectId processorId) throws IOException {
		log.info("creating sprint report for the project : {}", projectConfig.getProjectName());
		Iteration iteration = hierarchicalRequirement.getIteration();
		Set<SprintDetails> sprintDetailsSet = new HashSet<>();
		if(iteration!=null) {
			sprintDetailsSet = createSprintDetails(hierarchicalRequirement,iteration, projectConfig, processorId);
		}
		return sprintDetailsSet;
	}

	private Set<SprintDetails> createSprintDetails(HierarchicalRequirement hierarchicalRequirement,Iteration iteration, ProjectConfFieldMapping projectConfig, ObjectId processorId) {
	    Set<SprintDetails> sprintDetailsSet = new HashSet<>();
	    SprintDetails sprintDetails = new SprintDetails();
		// Check if sprintDetails with the same sprintID already exists
		SprintDetails existingSprintDetails = sprintRepository.findBySprintID(iteration.getObjectID());
		if (existingSprintDetails != null) {
			// Update the existing sprintDetails
			initializeSprintDetails(hierarchicalRequirement,iteration, projectConfig, processorId, existingSprintDetails);
			sprintDetailsSet.add(existingSprintDetails);
		} else {
			// Insert new sprintDetails
			sprintDetails.setOriginalSprintId(iteration.getObjectID());
			String sprintId = sprintDetails.getOriginalSprintId() + CommonConstant.ADDITIONAL_FILTER_VALUE_ID_SEPARATOR +
					projectConfig.getProjectBasicConfig().getProjectNodeId();
			sprintDetails.setSprintID(sprintId);
			initializeSprintDetails(hierarchicalRequirement,iteration, projectConfig, processorId, sprintDetails);
			sprintDetailsSet.add(sprintDetails);
	    }

	    return sprintDetailsSet;
	}

	private static void initializeSprintDetails(HierarchicalRequirement hierarchicalRequirement,Iteration iteration, ProjectConfFieldMapping projectConfig, ObjectId processorId, SprintDetails sprintDetails) {
		sprintDetails.setSprintName(iteration.getName());
		sprintDetails.setStartDate(iteration.getStartDate());
		sprintDetails.setEndDate(iteration.getEndDate());
		sprintDetails.setCompleteDate(iteration.getEndDate());
		sprintDetails.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId());
		sprintDetails.setProcessorId(processorId);
		sprintDetails.setState("Closed");
		Set<SprintIssue> totalIssues = new HashSet<>();
		SprintIssue sprintIssue = new SprintIssue();
		sprintIssue.setNumber(hierarchicalRequirement.getFormattedID());
		sprintIssue.setStatus(hierarchicalRequirement.getScheduleState());
		sprintIssue.setTypeName(hierarchicalRequirement.getType());
		sprintIssue.setStoryPoints(hierarchicalRequirement.getPlanEstimate());
		totalIssues.add(sprintIssue);
		sprintDetails.setTotalIssues(totalIssues);
	}
}
