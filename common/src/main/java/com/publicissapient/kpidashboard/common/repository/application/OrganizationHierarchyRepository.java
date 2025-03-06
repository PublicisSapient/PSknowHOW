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

import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;

@Repository
public interface OrganizationHierarchyRepository extends MongoRepository<OrganizationHierarchy, ObjectId> {

    @Query(value = "{ 'nodeId': ?0 }", delete = true)
    void deleteByNodeId(String nodeId);

    /**
     * Find organization hierarchies by matching name against both nodeName and nodeDisplayName
     * Uses MongoDB's $in operator with case-insensitive collation for accurate matching.
     *
     * Only returns essential fields to minimize memory usage:
     * - nodeId: for unique identification
     * - nodeName and nodeDisplayName: for matching and display
     * - hierarchyLevelId: for level-based processing
     * - parentId: for hierarchy relationships
     * - externalId: for synchronization
     * - _id: required for MongoDB operations
     *
     * @param nodeNames Set of node names to search for (case-insensitive)
     * @param hierarchyLevel Hierarchy level to filter by
     * @return List of matching organization hierarchies with only required fields
     */
    @Query(value = "{ $and: [ " +
                    "  { $or: [ " +
                    "    { 'nodeName': { $in: ?0 } }, " +
                    "    { 'nodeDisplayName': { $in: ?0 } } " +
                    "  ]}, " +
                    "  { 'hierarchyLevelId': ?1 } " +
                    "] }", 
           collation = "{ locale: 'en', strength: 1 }",
           fields = "{ " +
                    "'nodeId': 1, " +
                    "'nodeName': 1, " +
                    "'nodeDisplayName': 1, " +
                    "'hierarchyLevelId': 1, " +
                    "'parentId': 1, " +
                    "'externalId': 1, " +
                    "'_id': 1" +
                    "}")
    List<OrganizationHierarchy> findByNodeNamesAndLevel(Set<String> nodeNames, String hierarchyLevel);

    /**
     * Batch save organization hierarchies with minimal memory usage
     *
     * @param hierarchies List of hierarchies to save
     * @return List of saved hierarchies
     */
    @Override
    <S extends OrganizationHierarchy> List<S> saveAll(Iterable<S> hierarchies);
}
