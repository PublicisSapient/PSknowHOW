
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

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;

import com.publicissapient.kpidashboard.common.model.excel.CapacityKpiData;

/**
 * Repository for {@link CapacityKpiData}.
 *
 * @author anisingh4
 */
public interface CapacityKpiDataRepository extends CrudRepository<CapacityKpiData, ObjectId>,
		QuerydslPredicateExecutor<CapacityKpiData>, CapacityKpiDataCustomRepository {

	/**
	 * Gets capacity by sprint id project name.
	 *
	 * @param sprintIdProjectName
	 *            the sprint id project name
	 * @return the capacity by sprint id project name
	 */
	@Query(" {'sprintID' : ?0 }")
	List<CapacityKpiData> findBySprintID(String sprintIdProjectName);

	/**
	 * Find all by sprint ids
	 * 
	 * @param sprintIds
	 *            list of sprint ids
	 * @return list of sprints
	 */
	List<CapacityKpiData> findBySprintIDIn(List<String> sprintIds);

	/**
	 * Find by sprint id and basicprojectId
	 * 
	 * @param sprintId
	 *            sprintId
	 * @param basicProjectConfigId
	 *            basic project config id
	 * @return list of sprints
	 */
	CapacityKpiData findBySprintIDAndBasicProjectConfigId(String sprintId, ObjectId basicProjectConfigId);

	/**
	 * delete capacity projectwise
	 * 
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 */
	void deleteByBasicProjectConfigId(ObjectId basicProjectConfigId);
}
