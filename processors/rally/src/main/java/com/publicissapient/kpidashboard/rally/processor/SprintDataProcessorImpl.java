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
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.rally.constant.RallyConstants;
import com.publicissapient.kpidashboard.rally.model.HierarchicalRequirement;
import com.publicissapient.kpidashboard.rally.model.Iteration;
import com.publicissapient.kpidashboard.rally.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.rally.service.FetchSprintReport;
import com.publicissapient.kpidashboard.rally.service.JiraClientService;
import com.publicissapient.kpidashboard.rally.util.JiraIssueClientUtil;
import com.publicissapient.kpidashboard.rally.util.JiraProcessorUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;

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
			sprintDetailsSet = createSprintDetails(iteration, projectConfig, processorId);
		}
		return sprintDetailsSet;
	}

	private Set<SprintDetails> createSprintDetails(Iteration iteration, ProjectConfFieldMapping projectConfig, ObjectId processorId) {
	    Set<SprintDetails> sprintDetailsSet = new HashSet<>();
	    SprintDetails sprintDetails = new SprintDetails();
		// Check if sprintDetails with the same sprintID already exists
		SprintDetails existingSprintDetails = sprintRepository.findBySprintID(iteration.getObjectID());
		if (existingSprintDetails != null) {
			// Update the existing sprintDetails
			sprintDetails(iteration, projectConfig, processorId, existingSprintDetails);
			sprintDetailsSet.add(existingSprintDetails);
		} else {
			// Insert new sprintDetails
			sprintDetails.setOriginalSprintId(iteration.getObjectID());
			String sprintId = sprintDetails.getOriginalSprintId() + CommonConstant.ADDITIONAL_FILTER_VALUE_ID_SEPARATOR +
					projectConfig.getProjectBasicConfig().getProjectNodeId();
			sprintDetails.setSprintID(sprintId);
			sprintDetails(iteration, projectConfig, processorId, sprintDetails);
			sprintDetailsSet.add(sprintDetails);
	    }

	    return sprintDetailsSet;
	}

	private static void sprintDetails(Iteration iteration, ProjectConfFieldMapping projectConfig, ObjectId processorId, SprintDetails sprintDetails) {
		sprintDetails.setSprintName(iteration.getName());
		sprintDetails.setStartDate(iteration.getStartDate());
		sprintDetails.setEndDate(iteration.getEndDate());
		sprintDetails.setCompleteDate(iteration.getEndDate());
		sprintDetails.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId());
		sprintDetails.setProcessorId(processorId);
		sprintDetails.setState(iteration.getState());
	}
}
