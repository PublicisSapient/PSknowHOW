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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.rally.constant.RallyConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.domain.Status;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueReleaseStatusRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author pankumar8
 */
@Slf4j
@Service
public class CreateRallyIssueReleaseStatusImpl implements CreateRallyIssueReleaseStatus {

	@Autowired
	private JiraIssueReleaseStatusRepository jiraIssueReleaseStatusRepository;

	@Override
	public void processAndSaveProjectStatusCategory(String basicProjectConfigId) {

		JiraIssueReleaseStatus jiraIssueReleaseStatus = jiraIssueReleaseStatusRepository
				.findByBasicProjectConfigId(basicProjectConfigId);
		if (null == jiraIssueReleaseStatus) {
			List<Status> listOfProjectStatus = null; // RallyHelper.getStatus(client);
			//TODO: Check in Rally if we have status category and status concept if yes fetch those status.
			if (CollectionUtils.isNotEmpty(listOfProjectStatus)) {
				jiraIssueReleaseStatus = new JiraIssueReleaseStatus();
				jiraIssueReleaseStatus.setBasicProjectConfigId(basicProjectConfigId);
				Map<Long, String> toDosList = new HashMap<>();
				Map<Long, String> inProgressList = new HashMap<>();
				Map<Long, String> closedList = new HashMap<>();

				listOfProjectStatus.stream().forEach(status -> {
					if (RallyConstants.TO_DO.equals(status.getStatusCategory().getName())) {
						toDosList.put(status.getId(), status.getName());
					} else if (RallyConstants.DONE.equals(status.getStatusCategory().getName())) {
						closedList.put(status.getId(), status.getName());
					} else {
						inProgressList.put(status.getId(), status.getName());
					}
				});
				jiraIssueReleaseStatus.setToDoList(toDosList);
				jiraIssueReleaseStatus.setInProgressList(inProgressList);
				jiraIssueReleaseStatus.setClosedList(closedList);
				jiraIssueReleaseStatusRepository.save(jiraIssueReleaseStatus);
				log.info("saved project status category for the project : {}", basicProjectConfigId);
			}
		} else {
			log.info("project status category is already in db for the project : {} ", basicProjectConfigId);
		}
	}
}
