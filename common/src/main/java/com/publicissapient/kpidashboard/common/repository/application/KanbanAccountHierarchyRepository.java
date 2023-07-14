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

package com.publicissapient.kpidashboard.common.repository.application;

import java.time.LocalDateTime;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;

/**
 * The interface Kanban account hierarchy repository.
 *
 * @author prijain3
 */
@Repository
public interface KanbanAccountHierarchyRepository extends MongoRepository<KanbanAccountHierarchy, ObjectId> {
	/**
	 * Find distinct by label list.
	 *
	 * @param labelName
	 *            the label name
	 * @return the list
	 */
	@Query("{'labelName': ?0}")
	List<KanbanAccountHierarchy> findDistinctByLabel(String labelName);

	/**
	 * Find by label and project config Id.
	 *
	 * @param labelName
	 *            the label name
	 * @param basicProjectConfigId
	 *            the basic project config id
	 * @return the KanbanAccountHierarchy list
	 */
	List<KanbanAccountHierarchy> findByLabelNameAndBasicProjectConfigId(String labelName,
			ObjectId basicProjectConfigId);

	/**
	 * Find by node Id.
	 *
	 * @param nodeId
	 *            the node id
	 * @return the KanbanAccountHierarchy list
	 */
	List<KanbanAccountHierarchy> findByNodeId(String nodeId);

	/**
	 * Find by label name and node id.
	 * 
	 * @param labelName
	 *            the label name
	 * @param nodeId
	 *            the node id
	 * @return the KanbanAccountHierarchy
	 */
	List<KanbanAccountHierarchy> findByLabelNameAndNodeId(String labelName, String nodeId);

	/**
	 * Finds all the Kanban Account Hierarchy created Post dateTime
	 * 
	 * @param dateTime
	 * @return
	 */

	@Query("{'createdDate' :{$gt : ?0},'labelName': {$in : ?1 }}")
	List<KanbanAccountHierarchy> findByCreatedDateGreaterThan(LocalDateTime dateTime, List<String> labelList);

	/**
	 * find by node id and path
	 * 
	 * @param nodeId
	 *            the node id
	 * @param path
	 *            the path
	 * @return list of AccountHierarchy
	 */
	List<KanbanAccountHierarchy> findByNodeIdAndPath(String nodeId, String path);

	/**
	 * Deletes the documents for which path ends with given string.
	 * 
	 * @param path
	 *            path
	 */
	void deleteByPathEndsWith(String path);

	/**
	 * Deletes the documents that matches with given node id and path.
	 * 
	 * @param nodeId
	 *            node id
	 * @param path
	 *            path
	 */
	void deleteByNodeIdAndPath(String nodeId, String path);

	/**
	 * delete by basicProjectConfigId
	 * 
	 * @param basicProjectConfigId
	 *            basic project config id
	 */
	void deleteByBasicProjectConfigId(ObjectId basicProjectConfigId);

	/**
	 * finds all List all AccountHierachies with provided label list
	 * 
	 * @param labelList
	 * @return List of accountHierachy
	 */
	@Query("{'labelName': {$in : ?0 }}")
	List<KanbanAccountHierarchy> findByLabelList(List<String> labelList);

	/**
	 * find list of the documents for path match.
	 * 
	 * @param labelName
	 *            the label name
	 * @param path
	 *            path
	 * 
	 * @return the {@link KanbanAccountHierarchy} list
	 */
	List<KanbanAccountHierarchy> findByLabelNameAndPath(String labelName, String path);

	/**
	 * Delete by ids
	 * 
	 * @param ids
	 *            list of ids to be deleted
	 */
	void deleteByIdIn(List<ObjectId> ids);

	void deleteByBasicProjectConfigIdAndLabelNameIn(ObjectId basicProjectConfigId, List<String> labelName);
}
