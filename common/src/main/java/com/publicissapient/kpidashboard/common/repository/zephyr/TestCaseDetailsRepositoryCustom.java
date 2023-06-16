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

package com.publicissapient.kpidashboard.common.repository.zephyr;//NOPMD

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.common.model.zephyr.TestCaseDetails;

/**
 * Repository for TestCaseDetails with custom methods.
 */
@Repository
public interface TestCaseDetailsRepositoryCustom {

	/**
	 * Find TestCase by unique parameters and Folder list. if label is in query will
	 * not be considered
	 *
	 * @param mapOfFilters
	 *            the map of filters
	 * @param uniqueProjectMap
	 *            the unique project map
	 * @return list of feature
	 */
	List<TestCaseDetails> findNonRegressionTestDetails(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap, String mapStatusCriteria);

	/**
	 * Find TestCase by unique parameters and Folder list.
	 *
	 * @param mapOfFilters
	 *            the map of filters
	 * @param uniqueProjectMap
	 *            the unique project map
	 * @return list of feature
	 */
	List<TestCaseDetails> findTestDetails(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap, String mapStatusCriteria);

}
