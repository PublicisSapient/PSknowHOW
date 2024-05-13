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
import java.util.Set;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.common.model.jira.MasterJiraIssue;

/**
 * Repository for FeatureCollector.
 */
@Repository
public interface MasterJiraIssueRepository
		extends CrudRepository<MasterJiraIssue, ObjectId> {
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
	@Query
	List<MasterJiraIssue> findTopByProcessorIdAndChangeDateGreaterThanOrderByChangeDateDesc(ObjectId processorId,
																					  String changeDate);

	/**
	 * This essentially returns the max change date from the collection, based on
	 * the projectkey and last change date (or default delta change date property)
	 * available
	 *
	 * @param processorId
	 *            Processor ID of source system processor
	 * @param projectKey
	 *            projectKey of the project
	 * @param changeDate
	 *            Last available change date or delta begin date property
	 * @return A single Change Date value that is the maximum value of the existing
	 *         collection
	 */
	@Deprecated
	@Query
	List<MasterJiraIssue> findTopByProcessorIdAndProjectKeyAndChangeDateGreaterThanOrderByChangeDateDesc(ObjectId processorId,
																								   String projectKey, String changeDate);

	/**
	 * This essentially returns the max change date from the collection, based on
	 * the projectkey and last change date (or default delta change date property)
	 * available
	 *
	 * @param processorId
	 *            processor id
	 * @param basicProjectConfigId
	 *            config project name
	 * @param changeDate
	 *            change date
	 * @return A single Change Date value that is the maximum value of the existing
	 *         collection
	 */
	@Query
	List<MasterJiraIssue> findTopByProcessorIdAndBasicProjectConfigIdAndChangeDateGreaterThanOrderByChangeDateDesc(
			ObjectId processorId, String basicProjectConfigId, String changeDate);

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
	 * @return MasterJiraIssue object
	 */
	@Query
	MasterJiraIssue findTopByProcessorIdAndBasicProjectConfigIdAndTypeNameAndChangeDateGreaterThanOrderByChangeDateDesc(
			ObjectId processorId, String basicProjectConfigId, String typeName, String changeDate);

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
	MasterJiraIssue findByIssueIdAndBasicProjectConfigId(String issueId, String basicProjectConfigId);

	/**
	 * Gets story by number.
	 *
	 * @param number
	 *            the s number
	 * @return the story by number
	 */
	@Query(" {'number' : ?0 }")
	List<MasterJiraIssue> getStoryByNumber(String number);

	List<MasterJiraIssue> findByNumberAndBasicProjectConfigId(String number, String basicProjectConfigId);

	/**
	 * Find one document for given basicProjectConfigId.
	 *
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 * @return MasterJiraIssue
	 */
	MasterJiraIssue findTopByBasicProjectConfigId(String basicProjectConfigId);

	/**
	 * Deletes all documents that matches with given basicProjectConfigId.
	 *
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 */
	void deleteByBasicProjectConfigId(String basicProjectConfigId);

	/*
	 * Find documents for given numbers and basicProjectConfigId.
	 *
	 * @param numberIds List of numbers
	 *
	 * @param basicProjectConfigId basicProjectConfigId
	 *
	 * @return MasterJiraIssue
	 */
	List<MasterJiraIssue> findByNumberInAndBasicProjectConfigId(List<String> numberIds, String basicProjectConfigId);

	/*
	 * Find documents for given numbers and basicProjectConfigId.
	 *
	 *
	 * @param basicProjectConfigId basicProjectConfigId
	 *
	 * @return MasterJiraIssue
	 */
	List<MasterJiraIssue> findByBasicProjectConfigIdIn(String basicProjectConfigId);

	List<MasterJiraIssue> findByBasicProjectConfigIdAndReleaseVersionsReleaseNameIn(String projectConfigId,
																			  List<String> releaseVersions);

	Set<MasterJiraIssue> findByBasicProjectConfigIdAndDefectStoryIDInAndOriginalTypeIn(String basicProjectConfigID,
																				 Set<String> storyIDs, List<String> originalType);

	/*
	 * Find documents for given types and basicProjectConfigId.
	 *
	 *
	 * @param basicProjectConfigId
	 *
	 * @param typeName
	 *
	 * @return MasterJiraIssue
	 */
	List<MasterJiraIssue> findByBasicProjectConfigIdAndOriginalTypeIn(String basicProjectConfigId, List<String> typeName);

	List<MasterJiraIssue> findByBasicProjectConfigId(String basicProjectConfigId);

	/**
	 * Find set of jira Issues of particular types
	 *
	 * @param numberIds
	 *            numberIds
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 * @param typeName
	 *            typeName
	 * @return set of jiraIssues
	 */
	@Query(value = "{ 'number' : { $in: ?0 }, 'basicProjectConfigId' : ?1, 'typeName' : ?2  }", fields = "{ 'number' : 1, 'basicProjectConfigId' : 1,'url':1, 'name':1, 'status':1 }")
	Set<MasterJiraIssue> findNumberInAndBasicProjectConfigIdAndTypeName(List<String> numberIds, String basicProjectConfigId,
																  String typeName);

	Set<MasterJiraIssue> findByBasicProjectConfigIdAndParentStoryIdInAndOriginalTypeIn(String configId,
																					   Set<String> parentStoryIds, List<String> originalTypes);

	@Query("{ 'basicProjectConfigId' : ?0, 'boardId' : ?1 }")
	List<MasterJiraIssue> findByBasicProjectConfigIdAndBoardId(String basicProjectConfigId, String boardId);
	List<MasterJiraIssue> findByBasicProjectConfigIdAndIssueIdIn(String basicProjectConfigId, List<String> issueIds);
	@Query("{ 'basicProjectConfigId' : ?0, 'boardId' : ?1, 'sprintId' : { $in: ?2 } }")
	List<MasterJiraIssue> findByProjectIdAndBoardIdAndSprintIdIn(String projectId, String boardId, List<String> sprintIds);

	@Query(value = "{ 'boardId' : ?0 }", fields = "{ 'issueType' : 1 }")
	List<MasterJiraIssue> findIssueTypesByBoardId(String boardId);

	@Query(value = "{ 'projectKey' : ?0 }", fields = "{ 'issueType' : 1 }")
	List<MasterJiraIssue> findIssueTypesByProjectKey(String projectKey);

	List<MasterJiraIssue> findByProjectKey(String projectKey);
}