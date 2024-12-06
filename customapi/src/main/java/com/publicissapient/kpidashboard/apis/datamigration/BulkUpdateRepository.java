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
package com.publicissapient.kpidashboard.apis.datamigration;

import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectHierarchy;
import com.publicissapient.kpidashboard.common.model.application.ProjectRelease;
import com.publicissapient.kpidashboard.common.model.application.SprintTraceLog;
import com.publicissapient.kpidashboard.common.model.comments.KPIComments;
import com.publicissapient.kpidashboard.common.model.excel.CapacityKpiData;
import com.publicissapient.kpidashboard.common.model.excel.KanbanCapacity;
import com.publicissapient.kpidashboard.common.model.jira.HappinessKpiData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.model.rbac.AccessRequest;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.model.testexecution.KanbanTestExecution;
import com.publicissapient.kpidashboard.common.model.testexecution.TestExecution;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.AdditionalFilterCategoryRepository;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.KanbanTestExecutionRepository;
import com.publicissapient.kpidashboard.common.repository.application.OrganizationHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectReleaseRepo;
import com.publicissapient.kpidashboard.common.repository.application.SprintTraceLogRepository;
import com.publicissapient.kpidashboard.common.repository.application.TestExecutionRepository;
import com.publicissapient.kpidashboard.common.repository.comments.KpiCommentsRepository;
import com.publicissapient.kpidashboard.common.repository.excel.CapacityKpiDataRepository;
import com.publicissapient.kpidashboard.common.repository.excel.KanbanCapacityRepository;
import com.publicissapient.kpidashboard.common.repository.jira.HappinessKpiDataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.repository.rbac.AccessRequestsRepository;
import com.publicissapient.kpidashboard.common.repository.rbac.UserInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Slf4j
public class BulkUpdateRepository {
	@Autowired
	private ProjectBasicConfigRepository basicConfigRepository;
	@Autowired
	private OrganizationHierarchyRepository organizationHierarchyRepository;

	@Autowired
	private AccountHierarchyRepository accountHierarchyRepository;
	@Autowired
	private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepository;
	@Autowired
	private SprintRepository sprintRepository;
	@Autowired
	private CapacityKpiDataRepository capacityKpiDataRepository;
	@Autowired
	private KanbanCapacityRepository kanbanCapacityRepository;

	@Autowired
	private HappinessKpiDataRepository happinessKpiDataRepository;
	@Autowired
	private JiraIssueRepository jiraIssueRepository;
	@Autowired
	private KanbanJiraIssueRepository kanbanJiraIssueRepository;
	@Autowired
	private AdditionalFilterCategoryRepository additionalFilterCategoryRepository;
	@Autowired
	private TestExecutionRepository testExecutionRepository;
	@Autowired
	private KanbanTestExecutionRepository kanbanTestExecutionRepository;
	@Autowired
	private ProjectReleaseRepo projectReleaseRepo;
	@Autowired
	private SprintTraceLogRepository sprintTraceLogRepository;
	@Autowired
	private UserInfoRepository userInfoRepository;
	@Autowired
	private AccessRequestsRepository accessRequestsRepository;
	@Autowired
	private KpiCommentsRepository kpiCommentsRepository;
	@Autowired
	private ProjectHierarchyRepository projectHierarchyRepository;
	@Autowired
	private MongoTemplate mongoTemplate;

	public void saveToOrganizationHierarchy(List<OrganizationHierarchy> nodeWiseOrganizationHierarchyList) {
		// Save all data to the repository
		if (organizationHierarchyRepository.count() > 0) {
			organizationHierarchyRepository.deleteAll(); // Delete existing records
		}

		// Save new data
		organizationHierarchyRepository.saveAll(nodeWiseOrganizationHierarchyList);
		log.info("Organization Hierarchy successfully saved to the database.");

	}

	public void saveToBasicConfig(List<ProjectBasicConfig> projectBasicConfigList) {

		if (basicConfigRepository.count() > 0) {
			basicConfigRepository.deleteAll();
			throw new RuntimeException("Jaan kr Exception");// Delete existing records
		}
		// Save new data
		basicConfigRepository.saveAll(projectBasicConfigList);

		log.info("Project Basic Config successfully saved to the database.");
	}

	public void saveToProjectHierarchy(List<ProjectHierarchy> projectHierarchy) {
		// Save all data to the repository
		if (projectHierarchyRepository.count() > 0) {
			projectHierarchyRepository.deleteAll(); // Delete existing records
		}

		// Save new data
		projectHierarchyRepository.saveAll(projectHierarchy);
		log.info("Project Hierarchy successfully saved to the database.");
	}

	public void bulkUpdateCapacityCollections(List<CapacityKpiData> capacityUpdates,
			List<KanbanCapacity> kanbanCapacityList) {
		// Bulk operations for CapacityKpiData collection
		BulkOperations capacityBulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED,
				CapacityKpiData.class);
		for (CapacityKpiData data : capacityUpdates) {
			Query query = new Query(Criteria.where("_id").is(data.getId()));
			Update update = new Update().set("sprintID", data.getSprintID()).set("projectId", data.getProjectId());
			capacityBulkOps.updateOne(query, update);
		}

		// Execute bulk operations for CapacityKpiData
		capacityBulkOps.execute();

		// Bulk operations for another collection (e.g., AdditionalFilterCapacity)
		BulkOperations additionalBulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED,
				KanbanCapacity.class);
		for (KanbanCapacity data : kanbanCapacityList) {
			Query query = new Query(Criteria.where("_id").is(data.getId()));
			Update update = new Update().set("projectId", data.getProjectId());
			additionalBulkOps.updateOne(query, update);
		}

		// Execute bulk operations for AdditionalFilterCapacity
		additionalBulkOps.execute();
	}

	public void bulkUpdateHappiness(List<HappinessKpiData> happienss) {
		BulkOperations capacityBulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED,
				HappinessKpiData.class);
		for (HappinessKpiData data : happienss) {
			Query query = new Query(Criteria.where("_id").is(data.getId()));
			Update update = new Update().set("sprintID", data.getSprintID());
			capacityBulkOps.updateOne(query, update);
		}

		// Execute bulk operations for CapacityKpiData
		capacityBulkOps.execute();
		log.info("HappienessKpiData successfully saved to the database.");

	}

	public void bulkUpdateJiraIssue(List<JiraIssue> scrumJiraIssueList, List<KanbanJiraIssue> kanbanJiraIssueList) {

		BulkOperations scrumJiraIssue = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, JiraIssue.class);
		for (JiraIssue data : scrumJiraIssueList) {
			Query query = new Query(Criteria.where("_id").is(data.getId()));
			Update update = new Update().set("sprintID", data.getSprintID());
			scrumJiraIssue.updateOne(query, update);
		}

		// Execute bulk operations for CapacityKpiData
		scrumJiraIssue.execute();
		log.info("Scrum JiraIssue successfully saved to the database.");

		BulkOperations kanabanBulpOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, KanbanJiraIssue.class);
		for (KanbanJiraIssue data : kanbanJiraIssueList) {
			Query query = new Query(Criteria.where("_id").is(data.getId()));
			Update update = new Update().set("projectID", data.getProjectID());
			kanabanBulpOps.updateOne(query, update);
		}

		// Execute bulk operations for CapacityKpiData
		kanabanBulpOps.execute();
		log.info("Kanban JiraIssue successfully saved to the database.");
	}

	public void bulkUpdateTestExecution(List<TestExecution> testExecutionList,
			List<KanbanTestExecution> kanbanTestExecutionList) {
		BulkOperations testExecutionOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, TestExecution.class);
		for (TestExecution data : testExecutionList) {
			Query query = new Query(Criteria.where("_id").is(data.getId()));
			Update update = new Update().set("sprintId", data.getSprintId()).set("projectId", data.getProjectId());
			testExecutionOps.updateOne(query, update);
		}

		// Execute bulk operations for CapacityKpiData
		testExecutionOps.execute();
		log.info("Scrum Test Execution Data successfully saved to the database.");

		BulkOperations kanabanBulpOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED,
				KanbanTestExecution.class);
		for (KanbanTestExecution data : kanbanTestExecutionList) {
			Query query = new Query(Criteria.where("_id").is(data.getId()));
			Update update = new Update().set("projectNodeId", data.getProjectNodeId());
			kanabanBulpOps.updateOne(query, update);
		}

		// Execute bulk operations for CapacityKpiData
		kanabanBulpOps.execute();
		log.info("Kanban Test Execution Data successfully saved to the database.");
	}

	public void bulkUpdateProjectRelease(List<ProjectRelease> projectReleaseList) {
		BulkOperations projectReleaseOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED,
				ProjectRelease.class);
		for (ProjectRelease data : projectReleaseList) {
			Query query = new Query(Criteria.where("_id").is(data.getId()));
			Update update = new Update().set("projectId", data.getProjectId()).set("projectName",
					data.getProjectName());
			projectReleaseOps.updateOne(query, update);
		}

		// Execute bulk operations for CapacityKpiData
		projectReleaseOps.execute();
		log.info("Project Release Data successfully saved to the database.");
	}

	public void bulkUpdateSprintTraceLog(List<SprintTraceLog> sprintTraceLogList) {
		BulkOperations sprintTraceLogsOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED,
				SprintTraceLog.class);
		for (SprintTraceLog data : sprintTraceLogList) {
			Query query = new Query(Criteria.where("_id").is(data.getId()));
			Update update = new Update().set("sprintId", data.getSprintId());
			sprintTraceLogsOps.updateOne(query, update);
		}

		// Execute bulk operations for CapacityKpiData
		sprintTraceLogsOps.execute();
		log.info("Sprint Trace Log Data successfully saved to the database.");

	}

	public void bulkUpdateUserInfo(List<UserInfo> userInfoList, List<AccessRequest> accessRequestList) {
		BulkOperations userInfoOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED,
				UserInfo.class);
		for (UserInfo data : userInfoList) {
			Query query = new Query(Criteria.where("_id").is(data.getId()));
			Update update = new Update().set("projectsAccess", data.getProjectsAccess());
			userInfoOps.updateOne(query, update);
		}

		// Execute bulk operations for CapacityKpiData
		userInfoOps.execute();
		log.info("UserInfo Data successfully saved to the database.");

		BulkOperations accessReqOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED,
				AccessRequest.class);
		for (AccessRequest data : accessRequestList) {
			Query query = new Query(Criteria.where("_id").is(data.getId()));
			Update update = new Update().set("accessNode", data.getAccessNode());
			accessReqOps.updateOne(query, update);
		}

		// Execute bulk operations for CapacityKpiData
		accessReqOps.execute();
		log.info("Access Request Data successfully saved to the database.");
	}

	public void bulkUpdateComments(List<KPIComments> kpiCommentsList) {
		BulkOperations bulkOpsComments = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED,
				KPIComments.class);
		for (KPIComments data : kpiCommentsList) {
			Query query = new Query(Criteria.where("_id").is(data.getId()));
			Update update = new Update().set("node", data.getNode()).set("nodeChildId", data.getNodeChildId());
			bulkOpsComments.updateOne(query, update);
		}

		// Execute bulk operations for CapacityKpiData
		bulkOpsComments.execute();
		log.info("Kpi Comments Data successfully saved to the database.");
	}
}
