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
package com.publicissapient.kpidashboard.rally.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueReleaseStatusRepository;
import com.publicissapient.kpidashboard.rally.constant.RallyConstants;
import com.publicissapient.kpidashboard.rally.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.rally.model.RallyResponse;
import com.publicissapient.kpidashboard.rally.model.RallyStateResponse;
import com.publicissapient.kpidashboard.rally.util.RallyRestClient;

import lombok.extern.slf4j.Slf4j;

/**
 * @author pankumar8
 */
@Slf4j
@Service
public class CreateRallyIssueReleaseStatusImpl implements CreateRallyIssueReleaseStatus {

    @Autowired
    private JiraIssueReleaseStatusRepository jiraIssueReleaseStatusRepository;
    
    @Autowired
    private ProjectBasicConfigRepository projectBasicConfigRepository;
    
    @Autowired
    private ProjectToolConfigRepository projectToolConfigRepository;
    
    @Autowired
    private RallyRestClient rallyRestClient;

    @Override
    public void processAndSaveProjectStatusCategory(String basicProjectConfigId) {
        JiraIssueReleaseStatus jiraIssueReleaseStatus = jiraIssueReleaseStatusRepository
                .findByBasicProjectConfigId(basicProjectConfigId);
                
        if (null == jiraIssueReleaseStatus) {
            List<RallyStateResponse.State> listOfProjectStatus = fetchRallyStates(basicProjectConfigId);
            
            if (CollectionUtils.isNotEmpty(listOfProjectStatus)) {
                Map<Long, String> toDosList = new HashMap<>();
                Map<Long, String> inProgressList = new HashMap<>();
                Map<Long, String> closedList = new HashMap<>();

                listOfProjectStatus.forEach(status -> {
                    String category = status.getStateCategory() != null ? status.getStateCategory().getName() : "";
                    String name = status.getName();
                    String ref = status.getRef();
                    Long id = extractIdFromRef(ref);

                    if (id != null) {
                        if (isToDoState(category, name)) {
                            toDosList.put(id, name);
                        } else if (isClosedState(category, name)) {
                            closedList.put(id, name);
                        } else {
                            inProgressList.put(id, name);
                        }
                    }
                });

                saveProjectStatusCategory(basicProjectConfigId, toDosList, inProgressList, closedList);
                log.info("Saved Rally project status category for the project: {}", basicProjectConfigId);
            }
        } else {
            log.info("Project status category is already in db for the project: {}", basicProjectConfigId);
        }
    }

    private List<RallyStateResponse.State> fetchRallyStates(String basicProjectConfigId) {
        try {
            ProjectBasicConfig basicConfig = projectBasicConfigRepository.findById(new ObjectId(basicProjectConfigId)).orElse(null);
            if (basicConfig == null) {
                log.error("Project basic config not found for id: {}", basicProjectConfigId);
                return new ArrayList<>();
            }

            List<ProjectToolConfig> toolConfigs = projectToolConfigRepository.findByToolNameAndBasicProjectConfigId(
                RallyConstants.RALLY,
                new ObjectId(basicProjectConfigId)
            );

            if (CollectionUtils.isEmpty(toolConfigs)) {
                log.error("No Rally tool config found for project: {}", basicProjectConfigId);
                return new ArrayList<>();
            }

            ProjectConfFieldMapping projectConfig = ProjectConfFieldMapping.builder()
                .basicProjectConfigId(new ObjectId(basicProjectConfigId))
                .projectToolConfig(toolConfigs.get(0))
                .build();

//             String statesUrl = String.format("%s/state", rallyRestClient.getBaseUrl());
//            ResponseEntity<RallyResponse<RallyStateResponse.State>> response = rallyRestClient.get(
//               statesUrl,
//                projectConfig,
//                new ParameterizedTypeReference<RallyResponse<RallyStateResponse.State>>() {}
//            );
//
//            if (response != null && response.getBody() != null) {
//                RallyResponse.QueryResult<RallyStateResponse.State> queryResult = response.getBody().getQueryResult();
//                if (queryResult != null) {
//                    if (!queryResult.getErrors().isEmpty()) {
//                        log.error("Rally API returned errors: {}", queryResult.getErrors());
//                        return new ArrayList<>();
//                    }
//
//                    if (!queryResult.getWarnings().isEmpty()) {
//                        log.warn("Rally API returned warnings: {}", queryResult.getWarnings());
//                    }
//
//                    return queryResult.getResults();
//                }
//            }
//
        } catch (Exception e) {
            log.error("Error fetching Rally states for project: " + basicProjectConfigId, e);
        }
        return new ArrayList<>();
    }

    private Long extractIdFromRef(String ref) {
        if (ref != null && ref.contains("/")) {
            String[] parts = ref.split("/");
            try {
                return Long.parseLong(parts[parts.length - 1]);
            } catch (NumberFormatException e) {
                log.error("Invalid ID format in ref URL: {}", ref);
            }
        }
        return null;
    }

    private boolean isToDoState(String category, String name) {
        return RallyConstants.TO_DO.equals(category) ||
               "Defined".equals(name) ||
               "Ready".equals(name);
    }

    private boolean isClosedState(String category, String name) {
        return RallyConstants.DONE.equals(category) ||
               "Completed".equals(name) ||
               "Accepted".equals(name);
    }

    private void saveProjectStatusCategory(String projectConfigId, Map<Long, String> toDosList,
            Map<Long, String> inProgressList, Map<Long, String> closedList) {
        JiraIssueReleaseStatus jiraIssueReleaseStatus = new JiraIssueReleaseStatus();
        jiraIssueReleaseStatus.setBasicProjectConfigId(projectConfigId);
        jiraIssueReleaseStatus.setToDoList(toDosList);
        jiraIssueReleaseStatus.setInProgressList(inProgressList);
        jiraIssueReleaseStatus.setClosedList(closedList);
        jiraIssueReleaseStatusRepository.save(jiraIssueReleaseStatus);
    }
}
