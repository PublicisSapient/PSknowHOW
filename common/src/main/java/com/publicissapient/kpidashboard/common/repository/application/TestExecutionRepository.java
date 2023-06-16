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

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;

import com.publicissapient.kpidashboard.common.model.testexecution.TestExecution;
import com.publicissapient.kpidashboard.common.repository.excel.TestExecutionRepositoryCustom;

/**
 * @author sansharm13
 *
 */
public interface TestExecutionRepository extends CrudRepository<TestExecution, ObjectId>,
		QuerydslPredicateExecutor<TestExecution>, TestExecutionRepositoryCustom {

	/**
	 * Gets Test Execution by sprint id project name.
	 *
	 * @param sprintIdProjectName
	 *            the sprint id project name
	 * @return the TestExecution by sprint id project name
	 */

	TestExecution findBySprintId(String sprintIdProjectName);

	/**
	 * Find Test execution of sprints
	 * 
	 * @param sprintIds
	 * @return list of test execution data
	 */
	List<TestExecution> findBySprintIdIn(List<String> sprintIds);

	/**
	 * delete Testexecution project wise
	 * 
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 */
	void deleteByBasicProjectConfigId(String basicProjectConfigId);
}
