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
import java.util.Map;

import com.publicissapient.kpidashboard.common.model.testexecution.TestExecution;

/**
 * The interface Test execution detail custom repository.
 */
public interface TestExecutionRepositoryCustom {

	/**
	 * Returns TestExecution documents for selected filters and date range.
	 * 
	 * @param mapOfFilters
	 *            filters applied
	 * @param uniqueProjectMap
	 *            project specific filters
	 * @return result list
	 */
	List<TestExecution> findTestExecutionDetailByFilters(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap);

}
