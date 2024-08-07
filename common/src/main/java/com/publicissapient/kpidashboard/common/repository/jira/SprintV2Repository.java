/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
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

import com.publicissapient.kpidashboard.common.model.jira.SprintDetailsV2;

/**
 * @author yasbano
 *
 */

@Repository
public interface SprintV2Repository extends MongoRepository<SprintDetailsV2, ObjectId> {

	/**
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 * @return SprintDetailsV2
	 */
	SprintDetailsV2 findTopByBasicProjectConfigId(ObjectId basicProjectConfigId);

	/**
	 * @param basicProjectConfigId
	 *            basicProjectConfigId in object form
	 * @param state
	 *            state
	 * @return SprintDetailsV2
	 */
	SprintDetailsV2 findTopByBasicProjectConfigIdAndState(ObjectId basicProjectConfigId, String state);

	/**
	 * @param id
	 *            id
	 * @return SprintDetailsV2
	 */
	SprintDetailsV2 findBySprintID(String id);

	/**
	 * Find all which matches provided ids
	 *
	 * @param sprintIDs
	 *            sprint ids
	 * @return list of sprint details
	 */
	List<SprintDetailsV2> findBySprintIDIn(List<String> sprintIDs);

	/**
	 * delete using project basic config id
	 *
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 */
	void deleteByBasicProjectConfigId(ObjectId basicProjectConfigId);

	/**
	 * find all the sprints of the project
	 *
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 * @return list of sprints
	 */
	List<SprintDetailsV2> findByBasicProjectConfigId(ObjectId basicProjectConfigId);

	/**
	 * find all sprints of projects and based on status of sprint
	 *
	 * @param basicProjectConfigIds
	 *            basicProjectConfigIds
	 * @param sprintStatusList
	 *            sprintStatusList
	 * @return SprintDetailsV2
	 */
	@Query(value = "{ 'basicProjectConfigId' : { $in: ?0 }, 'state' : { $in: ?1} }", fields = "{ 'sprintID' : 1, 'state': 1, 'basicProjectConfigId' : 1, 'notCompletedIssues' : 1, 'completedIssues' : 1, 'sprintName' : 1, 'startDate' : 1, 'completeDate' : 1, 'totalIssues' : 1}", sort = "{ 'startDate' : -1 }")
	List<SprintDetailsV2> findByBasicProjectConfigIdInAndStateInOrderByStartDateDesc(Set<ObjectId> basicProjectConfigIds,
                                                                                     List<String> sprintStatusList);

	/**
	 * Find all which matches provided ids
	 *
	 * @param sprintIDs
	 *            sprint ids
	 * @return list of sprint details
	 */
	@Query(value = "{ 'sprintID' : { $in: ?0 } }", fields = "{ 'sprintID' : 1, 'state' : 1 , 'startDate' : 1 , 'endDate': 1 }")
	List<SprintDetailsV2> findBySprintIDInGetStatus(List<String> sprintIDs);

	@Query(value = "{ 'basicProjectConfigId' : ?0, 'state' : { $in: [?1] } }", fields = "{'sprintName' : 1, 'startDate' : 1}", sort = "{ 'startDate' : 1 }")
	List<SprintDetailsV2> findByBasicProjectConfigIdAndStateIgnoreCaseOrderByStartDateASC(ObjectId basicProjectConfigId,
                                                                                          String sprintState);

	@Query(value = "{ 'basicProjectConfigId' : ?0, 'state' : { $regex: ?1, $options: 'i' } }")
	List<SprintDetailsV2> findByBasicProjectConfigIdAndStateIgnoreCase(ObjectId basicProjectConfigId, String sprintState);
}
