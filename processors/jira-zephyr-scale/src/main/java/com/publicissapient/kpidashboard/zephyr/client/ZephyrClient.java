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

package com.publicissapient.kpidashboard.zephyr.client;

import java.util.List;

import com.publicissapient.kpidashboard.common.model.zephyr.ZephyrTestCaseDTO;
import com.publicissapient.kpidashboard.zephyr.model.ProjectConfFieldMapping;

/**
 * Client code which fetches the Zephyr related information from the Zephyr
 * server or cloud using the REST services
 */
public interface ZephyrClient {

	/**
	 * Method to fetch list of test cases from Zephyr server/cloud based on project
	 * key
	 *
	 * @param startAt
	 * @param projectConfig
	 * @return List<ZephyrTestCaseDTO>
	 */
	List<ZephyrTestCaseDTO> getTestCase(int startAt, ProjectConfFieldMapping projectConfig);
}
