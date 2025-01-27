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

import com.google.common.collect.Lists;
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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.function.Function;

@Repository
@Slf4j
public class BulkUpdateRepository {
	public static final String SPRINT_ID = "sprintID";
	public static final String PROJECT_ID = "projectId";
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
		if (CollectionUtils.isNotEmpty(capacityUpdates)) {
			processBulkUpdatesInBatches(capacityUpdates, CapacityKpiData.class,
					data -> Pair.of(new Query(Criteria.where("_id").is(data.getId())),
							new Update().set(SPRINT_ID, data.getSprintID()).set(PROJECT_ID, data.getProjectId())),
					"CapacityKpi Data", 1);
		}

		// Bulk operations for another collection (e.g., AdditionalFilterCapacity)
		if (CollectionUtils.isNotEmpty(kanbanCapacityList)) {

			processBulkUpdatesInBatches(kanbanCapacityList, KanbanCapacity.class,
					data -> Pair.of(new Query(Criteria.where("_id").is(data.getId())),
							new Update().set(PROJECT_ID, data.getProjectId())),
					"KanbanCapacity Data", 1);

		}
	}

	public void bulkUpdateHappiness(List<HappinessKpiData> happienss) {
		if (CollectionUtils.isNotEmpty(happienss)) {
			processBulkUpdatesInBatches(happienss, HappinessKpiData.class,
					data -> Pair.of(new Query(Criteria.where("_id").is(data.getId())),
							new Update().set(SPRINT_ID, data.getSprintID())),
					"Happiness Kpi Data", 1);
			// Execute bulk operations for CapacityKpiData

			log.info("HappienessKpiData successfully saved to the database.");
		}
	}

	public void bulkUpdateJiraIssue(List<JiraIssue> scrumJiraIssueList, List<KanbanJiraIssue> kanbanJiraIssueList) {
		// Process Scrum Jira Issues in batches
		if (CollectionUtils.isNotEmpty(scrumJiraIssueList)) {
			processBulkUpdatesInBatches(scrumJiraIssueList, JiraIssue.class,
					data -> Pair.of(new Query(Criteria.where("_id").is(data.getId())),
							new Update().set(SPRINT_ID, data.getSprintID())),
					"Scrum JiraIssue", 10000);
		}

		// Process Kanban Jira Issues in batches
		if (CollectionUtils.isNotEmpty(kanbanJiraIssueList)) {
			processBulkUpdatesInBatches(kanbanJiraIssueList, KanbanJiraIssue.class,
					data -> Pair.of(new Query(Criteria.where("_id").is(data.getId())),
							new Update().set("projectID", data.getProjectID())),
					"Kanban JiraIssue", 10000);
		}

	}

	public void bulkUpdateTestExecution(List<TestExecution> testExecutionList,
			List<KanbanTestExecution> kanbanTestExecutionList) {
		if (CollectionUtils.isNotEmpty(testExecutionList)) {
			processBulkUpdatesInBatches(testExecutionList, TestExecution.class,
					data -> Pair.of(new Query(Criteria.where("_id").is(data.getId())),
							new Update().set("sprintId", data.getSprintId()).set(PROJECT_ID, data.getProjectId())),
					"Scrum Test Execution", 1);
			log.info("Scrum Test Execution Data successfully saved to the database.");
		}
		if (CollectionUtils.isNotEmpty(kanbanTestExecutionList)) {
			processBulkUpdatesInBatches(kanbanTestExecutionList, KanbanTestExecution.class,
					data -> Pair.of(new Query(Criteria.where("_id").is(data.getId())),
							new Update().set("projectNodeId", data.getProjectNodeId())),
					"Kanban Test Execution", 1);

			log.info("Kanban Test Execution Data successfully saved to the database.");
		}
	}

	public void bulkUpdateProjectRelease(List<ProjectRelease> projectReleaseList) {
		if (CollectionUtils.isNotEmpty(projectReleaseList)) {

			processBulkUpdatesInBatches(projectReleaseList, ProjectRelease.class,
					data -> Pair.of(new Query(Criteria.where("_id").is(data.getId())), new Update()
							.set(PROJECT_ID, data.getProjectId()).set("projectName", data.getProjectName())),
					"Project Release", 1);
			log.info("Project Release Data successfully saved to the database.");
		}
	}

	public void bulkUpdateSprintTraceLog(List<SprintTraceLog> sprintTraceLogList) {
		if (CollectionUtils.isNotEmpty(sprintTraceLogList)) {

			processBulkUpdatesInBatches(sprintTraceLogList, SprintTraceLog.class,
					data -> Pair.of(new Query(Criteria.where("_id").is(data.getId())),
							new Update().set("sprintId", data.getSprintId())),
					"Sprint Trace Logs", 100);
			log.info("Sprint Trace Log Data successfully saved to the database.");
		}

	}

	public void bulkUpdateUserInfo(List<UserInfo> userInfoList, List<AccessRequest> accessRequestList) {
		if (CollectionUtils.isNotEmpty(userInfoList)) {
			processBulkUpdatesInBatches(userInfoList, UserInfo.class,
					data -> Pair.of(new Query(Criteria.where("_id").is(data.getId())),
							new Update().set("projectsAccess", data.getProjectsAccess())),
					"User Info", 100);
			log.info("UserInfo Data successfully saved to the database.");
		}
		if (CollectionUtils.isNotEmpty(accessRequestList)) {
			processBulkUpdatesInBatches(accessRequestList, AccessRequest.class,
					data -> Pair.of(new Query(Criteria.where("_id").is(data.getId())),
							new Update().set("accessNode", data.getAccessNode())),
					"Access Request", 100);
			log.info("Access Request Data successfully saved to the database.");
		}
	}

	public void bulkUpdateComments(List<KPIComments> kpiCommentsList) {
		if (CollectionUtils.isNotEmpty(kpiCommentsList)) {
			processBulkUpdatesInBatches(kpiCommentsList, KPIComments.class,
					data -> Pair.of(new Query(Criteria.where("_id").is(data.getId())),
							new Update().set("node", data.getNode()).set("nodeChildId", data.getNodeChildId())),
					"KpiComments", 1000);
			log.info("Kpi Comments Data successfully saved to the database.");
		}
	}

	public void saveToSprintDetails(List<SprintDetails> sprintDetailsList) {
		if (CollectionUtils.isNotEmpty(sprintDetailsList)) {
			processBulkUpdatesInBatches(sprintDetailsList, SprintDetails.class,
					data -> Pair.of(new Query(Criteria.where("_id").is(data.getId())),
							new Update().set(SPRINT_ID, data.getSprintID())),
					"SprintDetails", 1000);
			log.info("Sprint Details Data successfully saved to the database.");
		}
	}

	public void bulkUpdateCommentsHistory(List<KpiCommentsHistory> kpiCommentHistoryList) {
		if (CollectionUtils.isNotEmpty(kpiCommentHistoryList)) {
			processBulkUpdatesInBatches(kpiCommentHistoryList, KpiCommentsHistory.class,
					data -> Pair.of(new Query(Criteria.where("_id").is(data.getId())),
							new Update().set("node", data.getNode()).set("nodeChildId", data.getNodeChildId())),
					"Kpi Comments History", 1000);

			log.info("Kpi Comments History Data successfully saved to the database.");
		}
	}

	private <T> void processBulkUpdatesInBatches(List<T> updates, Class<T> entityClass,
			Function<T, Pair<Query, Update>> updateMapper, String entityType, int batchCollection) {
		int batchSize = batchCollection; // Set an appropriate batch size
		List<List<T>> batches = Lists.partition(updates, batchSize);

		log.info("Starting bulk update for {}. Total records: {}, Batch size: {}", entityType, updates.size(),
				batchSize);
		int batchNumber = 1;

		for (List<T> batch : batches) {
			try {
				BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, entityClass);
				for (T item : batch) {
					Pair<Query, Update> queryUpdatePair = updateMapper.apply(item);
					bulkOps.updateOne(queryUpdatePair.getLeft(), queryUpdatePair.getRight());
				}
				bulkOps.execute();
				log.info("Batch {} for {} successfully processed. Records in batch: {}", batchNumber, entityType,
						batch.size());
			} catch (Exception e) {
				log.error("Error in processing batch {} for {}. Error: {}", batchNumber, entityType, e.getMessage(), e);
				log.error("Recommended to restore your backup and restart customapi");
				throw e;
			}
			batchNumber++;
		}

		log.info("Bulk update for {} completed successfully.", entityType);
	}
}
