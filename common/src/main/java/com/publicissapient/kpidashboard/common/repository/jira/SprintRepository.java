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
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

/**
 * @author yasbano
 *
 */

@Repository
public interface SprintRepository extends MongoRepository<SprintDetails, ObjectId> {

	/**
	 * @param basicProjectConfigId
	 * @return SprintDetails
	 */
	SprintDetails findTopByBasicProjectConfigId(ObjectId basicProjectConfigId);

	/**
	 * @param basicProjectConfigId
	 * @param state
	 * @return SprintDetails
	 */
	SprintDetails findTopByBasicProjectConfigIdAndState(ObjectId basicProjectConfigId, String state);

	/**
	 * @param id
	 * @return SprintDetails
	 */
	SprintDetails findBySprintID(String id);

	/**
	 * Find all which matches provided ids
	 * 
	 * @param sprintIDs
	 *            sprint ids
	 * @return list of sprint details
	 */
	List<SprintDetails> findBySprintIDIn(List<String> sprintIDs);

	/**
	 * delete using projectbasic config id
	 * 
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 */
	void deleteByBasicProjectConfigId(ObjectId basicProjectConfigId);

	/**
	 * find all the sprints of the project
	 * 
	 * @param basicProjectConfigId
	 * @return list of sprints
	 */
	List<SprintDetails> findByBasicProjectConfigId(ObjectId basicProjectConfigId);

	/**
	 * find all sprints of projects and based on status of sprint
	 * 
	 * @param basicProjectConfigIds
	 * @param state
	 * @return
	 */
	List<SprintDetails> findByBasicProjectConfigIdInAndStateOrderByStartDateDesc(Set<ObjectId> basicProjectConfigIds,
			String state);

	@Query(value = "{ 'basicProjectConfigId' : { $in: ?0 }, 'state' : ?1 }", fields = "{ 'sprintID' : 1, 'basicProjectConfigId' : 1, 'completedIssues' : 1, 'sprintName' : 1, 'startDate' : 1}", sort = "{ 'startDate' : -1 }")
	List<SprintDetails> findByBasicProjectConfigIdInAndStateOrderByStartDateDescQuery(
			Set<ObjectId> basicProjectConfigIds, String state);

	/**
	 * Find all which matches provided ids
	 * 
	 * @param sprintIDs
	 *            sprint ids
	 * @return list of sprint details
	 */
	@Query(value = "{ 'sprintID' : { $in: ?0 } }", fields = "{ 'sprintID' : 1, 'state' : 1 }")
	List<SprintDetails> findBySprintIDInGetStatus(List<String> sprintIDs);
}
