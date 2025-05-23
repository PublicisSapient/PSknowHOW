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

package com.publicissapient.kpidashboard.common.repository.excel;

import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;

import com.publicissapient.kpidashboard.common.model.excel.KanbanCapacity;

/** The interface Kanban capacity repository. */
public interface KanbanCapacityRepository
		extends
			CrudRepository<KanbanCapacity, ObjectId>,
			QuerydslPredicateExecutor<KanbanCapacity>,
			KanbanCapacityRepoCustom {

	List<KanbanCapacity> findByBasicProjectConfigId(ObjectId basicProjectConfigId);

	/**
	 * delete capacity projectwise
	 *
	 * @param basicProjectConfigId
	 *          basicProjectConfigId
	 */
	void deleteByBasicProjectConfigId(ObjectId basicProjectConfigId);

	@Query(value = "{ 'basicProjectConfigId': { $in: ?0 } }", fields = "{ '_id': 1 , 'basicProjectConfigId':1 }")
	List<KanbanCapacity> findByBasicProjectConfigIdIn(Set<ObjectId> basicProjectConfigId);
}
