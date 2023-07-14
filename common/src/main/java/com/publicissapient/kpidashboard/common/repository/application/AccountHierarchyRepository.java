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

/**
 * 
 */
package com.publicissapient.kpidashboard.common.repository.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;

/**
 * The Account hierarchy repository.
 *
 * @author prigupta8
 */
@Repository
public interface AccountHierarchyRepository extends MongoRepository<AccountHierarchy, ObjectId> {

	/**
	 * Find distinct by label list.
	 *
	 * @return the {@link AccountHierarchy} list
	 */
	@Query("{'labelName': ?0}")
	List<AccountHierarchy> findDistinctByLabel(String labelName);

	/**
	 * Find by id list.
	 *
	 * @return the {@link AccountHierarchy} list
	 */
	Optional<AccountHierarchy> findById(ObjectId objectId);

	/**
	 * Find by parent id list.
	 *
	 * @return the {@link AccountHierarchy} list
	 */
	List<AccountHierarchy> findByParentId(String parentId);

	/**
	 * Find by parentId and path
	 * 
	 * @param parentId
	 *            the parent id
	 * @param path
	 *            path
	 * @return list of AccountHierarchy
	 */
	List<AccountHierarchy> findByParentIdAndPath(String parentId, String path);

	/**
	 * Find by label and project config Id.
	 * 
	 * @param labelName
	 *            the label name
	 * @param basicProjectConfigId
	 *            the basic project config id
	 * @return the {@link AccountHierarchy} list
	 */
	List<AccountHierarchy> findByLabelNameAndBasicProjectConfigId(String labelName, ObjectId basicProjectConfigId);

	/**
	 * Find by label and nodeId.
	 *
	 * @param labelName
	 *            the label name
	 * @param nodeId
	 *            the project Name
	 * @return the {@link AccountHierarchy}
	 */
	List<AccountHierarchy> findByLabelNameAndNodeId(String labelName, String nodeId);

	/**
	 * find by node id and path
	 * 
	 * @param nodeId
	 *            the node id
	 * @param path
	 *            the path
	 * @return list of AccountHierarchy
	 */
	List<AccountHierarchy> findByNodeIdAndPath(String nodeId, String path);

	/**
	 * Find by node Id.
	 * 
	 * @param nodeId
	 *            the node id
	 * @return the {@link AccountHierarchy} list
	 */
	List<AccountHierarchy> findByNodeId(String nodeId);

	/**
	 * Finds all Account Hierarchy created post provided Datetime
	 * 
	 * @param dateTime
	 * @return
	 */
	@Query("{'createdDate' :{$gt : ?0},'labelName': {$in : ?1 }}")
	List<AccountHierarchy> findByCreatedDateGreaterThan(LocalDateTime dateTime, List<String> labelList);

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
	 * delete by basicProjectConfigId and LabelName
	 * 
	 * @param basicProjectConfigId
	 * @param labelName
	 */
	void deleteByBasicProjectConfigIdAndLabelName(ObjectId basicProjectConfigId, String labelName);

	/**
	 * finds all List all AccountHierachies with provided label list
	 * 
	 * @param labelList
	 * @return List of accountHierachy
	 */
	@Query("{'labelName': {$in : ?0 }}")
	List<AccountHierarchy> findByLabelList(List<String> labelList);

	/**
	 * find list of the documents for path match.
	 * 
	 * @param labelName
	 *            the label name
	 * @param path
	 *            path
	 * 
	 * @return the {@link AccountHierarchy} list
	 */
	List<AccountHierarchy> findByLabelNameAndPath(String labelName, String path);

	/**
	 * Delete by ids
	 * 
	 * @param ids
	 *            list of ids to be deleted
	 */
	void deleteByIdIn(List<ObjectId> ids);

	void deleteByBasicProjectConfigIdAndLabelNameIn(ObjectId basicProjectConfigId, List<String> labelName);
}
