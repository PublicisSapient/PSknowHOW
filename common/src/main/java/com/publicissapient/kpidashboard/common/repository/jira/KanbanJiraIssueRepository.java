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

package com.publicissapient.kpidashboard.common.repository.jira;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;

/**
 * Interface to provides method for performing operations on kanban_feature
 * collection.
 * 
 * @author prijain3
 */
@Repository
public interface KanbanJiraIssueRepository extends CrudRepository<KanbanJiraIssue, ObjectId>,
		QuerydslPredicateExecutor<KanbanJiraIssue>, KanbanJiraIssueRepoCustom {

	/**
	 * Gets feature id by id.
	 *
	 * @param issueId
	 *            the s id
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 * @return the feature id by id
	 */
	@Query(fields = "{'issueId' : 1}")
	KanbanJiraIssue findByIssueIdAndBasicProjectConfigId(String issueId, String basicProjectConfigId);

	/**
	 * Finds object with max date for given project. This essentially returns the
	 * max change date from the collection, based on the projectkey and last change
	 * date (or default delta change date property) available
	 * 
	 * @param processorId
	 *            Processor ID of source system collector
	 * @param projectKey
	 *            projectKey of the project
	 * @param changeDate
	 *            Last available change date or delta begin date property
	 * @return A single Change Date value that is the maximum value of the existing
	 *         collection
	 */
	@Deprecated
	@Query
	List<KanbanJiraIssue> findTopByProcessorIdAndProjectKeyAndChangeDateGreaterThanOrderByChangeDateDesc(
			ObjectId processorId, String projectKey, String changeDate);

	/**
	 * Finds object with max date for given project. This essentially returns the
	 * max change date from the collection, based on the projectConfigId and last
	 * change date (or default delta change date property) available
	 * 
	 * @param processorId
	 *            Processor ID of source system collector
	 * @param projectConfigId
	 *            project config id of projectConfig
	 * @param startDate
	 *            Last available change date or delta begin date property
	 * @return A single Change Date value that is the maximum value of the existing
	 *         collection
	 */
	List<KanbanJiraIssue> findTopByProcessorIdAndBasicProjectConfigIdAndChangeDateGreaterThanOrderByChangeDateDesc(
			ObjectId processorId, String projectConfigId, String startDate);

	/**
	 * Finds KanbanFeature object based on story number.
	 * 
	 * @param number
	 *            story number
	 * @return list of KanbanFeature
	 */
	@Query(" {'number' : ?0 }")
	List<KanbanJiraIssue> getStoryByNumber(String number);

	List<KanbanJiraIssue> findByNumberAndBasicProjectConfigId(String number, String basicProjectConfigId);

	/**
	 * Gets feature id by id.
	 *
	 * @param issueId
	 *            the s id
	 * @return the feature id by id
	 */
	@Query(value = "{'issueId' : ?0}", fields = "{'issueId' : 1}")
	List<KanbanJiraIssue> findByIssueId(String issueId);

	/**
	 * This essentially returns the max change date from the collection, based on
	 * the last change date (or default delta change date property) available
	 *
	 * @param processorId
	 *            Processor ID of source system processor
	 * @param changeDate
	 *            Last available change date or delta begin date property
	 * @return A single Change Date value that is the maximum value of the existing
	 *         collection
	 */
	List<KanbanJiraIssue> findTopByProcessorIdAndChangeDateGreaterThanOrderByChangeDateDesc(ObjectId processorId,
			String changeDate);

	/**
	 * Find one document for given basicProjectConfigId.
	 * 
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 * @return JiraIssue
	 */
	KanbanJiraIssue findTopByBasicProjectConfigId(String basicProjectConfigId);

	/**
	 * Deletes all documents that matches with given basicProjectConfigId.
	 * 
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 */
	void deleteByBasicProjectConfigId(String basicProjectConfigId);

	/**
	 * This essentially returns the max change date from the collection, based on
	 * the basicProjectConfigId(projectConfigId from projectConfig) and last change
	 * date
	 *
	 * @param processorId
	 *            processorId
	 * @param basicProjectConfigId
	 *            projectCOnfigId of project config
	 * @param typeName
	 *            issue type
	 * @param changeDate
	 *            change date
	 * @return KanbanJiraIssue object
	 */
	KanbanJiraIssue findTopByProcessorIdAndBasicProjectConfigIdAndTypeNameAndChangeDateGreaterThanOrderByChangeDateDesc(
			ObjectId processorId, String basicProjectConfigId, String typeName, String changeDate);

	List<KanbanJiraIssue> findAll();

    List<KanbanJiraIssue> findByBasicProjectConfigId(String basicProjectConfigId);
}
