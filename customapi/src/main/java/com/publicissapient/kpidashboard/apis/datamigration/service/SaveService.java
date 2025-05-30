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

package com.publicissapient.kpidashboard.apis.datamigration.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.datamigration.BulkUpdateRepository;
import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectHierarchy;
import com.publicissapient.kpidashboard.common.model.application.ProjectRelease;
import com.publicissapient.kpidashboard.common.model.application.SprintTraceLog;
import com.publicissapient.kpidashboard.common.model.comments.KPIComments;
import com.publicissapient.kpidashboard.common.model.comments.KpiCommentsHistory;
import com.publicissapient.kpidashboard.common.model.excel.CapacityKpiData;
import com.publicissapient.kpidashboard.common.model.excel.KanbanCapacity;
import com.publicissapient.kpidashboard.common.model.jira.HappinessKpiData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.rbac.AccessRequest;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.model.testexecution.KanbanTestExecution;
import com.publicissapient.kpidashboard.common.model.testexecution.TestExecution;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SaveService {

	@Autowired
	BulkUpdateRepository bulkUpdateRepository;

	public void saveToDatabase(Map<String, Object> dataToSave) {
		log.info("Start--Migration Data Saving");
		bulkUpdateRepository
				.saveToOrganizationHierarchy((List<OrganizationHierarchy>) dataToSave.get("ORGANIZATION_HIERARCHY"));
		bulkUpdateRepository.saveToBasicConfig((List<ProjectBasicConfig>) dataToSave.get("PROJECT_BASIC"));
		bulkUpdateRepository.saveToProjectHierarchy((List<ProjectHierarchy>) dataToSave.get("PROJECT_HIERARCHY"));
		bulkUpdateRepository.saveToSprintDetails((List<SprintDetails>) dataToSave.get("SPRINT_DETAILS"));
		bulkUpdateRepository.bulkUpdateCapacityCollections((List<CapacityKpiData>) dataToSave.get("SCRUM_CAPACITY"),
				(List<KanbanCapacity>) dataToSave.get("KANBAN_CAPACITY"));
		bulkUpdateRepository.bulkUpdateHappiness((List<HappinessKpiData>) dataToSave.get("HAPPIENSS"));
		bulkUpdateRepository.bulkUpdateJiraIssue((List<JiraIssue>) dataToSave.get("SCRUM_JIRA_ISSUE"),
				(List<KanbanJiraIssue>) dataToSave.get("KANBAN_JIRA_ISSUE"));
		bulkUpdateRepository.bulkUpdateTestExecution((List<TestExecution>) dataToSave.get("TEST_EXECUTION_SCRUM"),
				(List<KanbanTestExecution>) dataToSave.get("TEST_EXECUTION_KANBAN"));
		bulkUpdateRepository.bulkUpdateProjectRelease((List<ProjectRelease>) dataToSave.get("PROJECT_RELEASE"));
		bulkUpdateRepository.bulkUpdateSprintTraceLog((List<SprintTraceLog>) dataToSave.get("SPRINT_TRACELOG"));
		bulkUpdateRepository.bulkUpdateUserInfo((List<UserInfo>) dataToSave.get("USER_INFO"),
				(List<AccessRequest>) dataToSave.get("ACCESS_REQUEST"));
		bulkUpdateRepository.bulkUpdateComments((List<KPIComments>) dataToSave.get("KPI_COMMENT"));
		bulkUpdateRepository.bulkUpdateCommentsHistory((List<KpiCommentsHistory>) dataToSave.get("KPI_COMMENT_HISTORY"));
	}
}
