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

package com.publicissapient.kpidashboard.apis.testexecution.service;

import java.util.List;

import com.publicissapient.kpidashboard.common.model.testexecution.TestExecutionData;

/**
 * @author sansharm13
 *
 */
public interface TestExecutionService {
	/**
	 * This method process the test Execution data.
	 * 
	 * @param testExecution
	 * @return TestExecution object
	 */
	TestExecutionData processTestExecutionData(TestExecutionData testExecution);

	/**
	 * Gets test execution percentage
	 * 
	 * @param basicProjectConfigId
	 * @return return list of test executions
	 */
	List<TestExecutionData> getTestExecutions(String basicProjectConfigId);

	void deleteTestExecutionByProject(boolean isKanban, String basicProjectConfigId);

}
