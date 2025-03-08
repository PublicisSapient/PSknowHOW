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
import java.util.List;
import java.util.Set;

import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.rally.model.HierarchicalRequirement;
import com.publicissapient.kpidashboard.rally.model.Iteration;
import com.publicissapient.kpidashboard.rally.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.rally.model.RallyResponse;
import com.publicissapient.kpidashboard.rally.service.RallyCommonService;
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

	@Autowired
	private RallyCommonService rallyCommonService;

	@Override
	public Set<SprintDetails> processSprintData(HierarchicalRequirement hierarchicalRequirement, ProjectConfFieldMapping projectConfig, String boardId,
												ObjectId processorId) throws IOException {
		log.info("creating sprint report for the project : {}", projectConfig.getProjectName());
		int pageStart = 0;
		Iteration iteration = hierarchicalRequirement.getIteration();
		List<HierarchicalRequirement> hierarchicalRequirements = rallyCommonService.getHierarchicalRequirementsByIteration(iteration,hierarchicalRequirement);
		Set<SprintDetails> sprintDetailsSet = new HashSet<>();
		if(iteration!=null) {
			sprintDetailsSet = createSprintDetails(hierarchicalRequirements,iteration, projectConfig, processorId);
		}
		return sprintDetailsSet;
	}

	private Set<SprintDetails> createSprintDetails(List<HierarchicalRequirement> hierarchicalRequirements,Iteration iteration, ProjectConfFieldMapping projectConfig, ObjectId processorId) {
	    Set<SprintDetails> sprintDetailsSet = new HashSet<>();
	    SprintDetails sprintDetails = new SprintDetails();
		// Check if sprintDetails with the same sprintID already exists
		sprintDetails.setOriginalSprintId(iteration.getObjectID());
		String sprintId = sprintDetails.getOriginalSprintId() + CommonConstant.ADDITIONAL_FILTER_VALUE_ID_SEPARATOR +
				projectConfig.getProjectBasicConfig().getProjectNodeId();
		//TODO Girish check iteration.getObjectID()
		SprintDetails existingSprintDetails = sprintRepository.findBySprintID(sprintId);
		if (existingSprintDetails != null) {
			// Update the existing sprintDetails
			initializeSprintDetails(hierarchicalRequirements,iteration, projectConfig, processorId, existingSprintDetails);
			sprintDetailsSet.add(existingSprintDetails);
		} else {
			// Insert new sprintDetails
			sprintDetails.setOriginalSprintId(iteration.getObjectID());
			sprintDetails.setSprintID(sprintId);
			initializeSprintDetails(hierarchicalRequirements,iteration, projectConfig, processorId, sprintDetails);
			sprintDetailsSet.add(sprintDetails);
	    }

	    return sprintDetailsSet;
	}

	private static void initializeSprintDetails(List<HierarchicalRequirement> hierarchicalRequirements, Iteration iteration,
												ProjectConfFieldMapping projectConfig, ObjectId processorId,
												SprintDetails sprintDetails) {
		// Set basic sprint details
		sprintDetails.setSprintName(iteration.getName());
		sprintDetails.setStartDate(iteration.getStartDate());
		sprintDetails.setEndDate(iteration.getEndDate());
		sprintDetails.setCompleteDate(iteration.getEndDate()); // Assuming completion date is the same as end date
		sprintDetails.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId());
		sprintDetails.setProcessorId(processorId);
		sprintDetails.setState("cslosed"); // Assuming the sprint is closed

		// Create a Set<SprintIssue> for the given iteration
		Set<SprintIssue> totalIssues = new HashSet<>();

		// Iterate through all hierarchical requirements
		for (HierarchicalRequirement requirement : hierarchicalRequirements) {
			// Check if the requirement belongs to the given iteration
			if (requirement.getIteration() != null && iteration.getName().equals(requirement.getIteration().getName())) {
				// Create a new SprintIssue for the requirement
				SprintIssue sprintIssue = new SprintIssue();
				sprintIssue.setNumber(requirement.getFormattedID());
				sprintIssue.setStatus(requirement.getScheduleState());
				sprintIssue.setTypeName(requirement.getType());
				sprintIssue.setStoryPoints(requirement.getPlanEstimate());

				// Add the SprintIssue to the set
				totalIssues.add(sprintIssue);
			}
		}

		// Set total issues in sprintDetails
		if(sprintDetails.getSprintID() != null && sprintDetails.getTotalIssues() != null){
			sprintDetails.getTotalIssues().addAll(totalIssues);
		} else {
			sprintDetails.setTotalIssues(totalIssues);
		}

		// Optional: Separate completed and not completed issues
		Set<SprintIssue> completedIssues = new HashSet<>();
		Set<SprintIssue> notCompletedIssues = new HashSet<>();

		for (SprintIssue issue : totalIssues) {
			if ("Accepted".equals(issue.getStatus())) { // Assuming "Accepted" means completed
				completedIssues.add(issue);
			} else {
				notCompletedIssues.add(issue);
			}
		}
		// Set total issues in sprintDetails
		if(sprintDetails.getSprintID() != null && sprintDetails.getCompletedIssues() != null){
			sprintDetails.getCompletedIssues().addAll(completedIssues);
		} else {
			sprintDetails.setCompletedIssues(completedIssues);
		}

		if(sprintDetails.getSprintID() != null && sprintDetails.getNotCompletedIssues() != null){
			sprintDetails.getNotCompletedIssues().addAll(notCompletedIssues);
		} else {
			sprintDetails.setNotCompletedIssues(notCompletedIssues);
		}
	}
}
