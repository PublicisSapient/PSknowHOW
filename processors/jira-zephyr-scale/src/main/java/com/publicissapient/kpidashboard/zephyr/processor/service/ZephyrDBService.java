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

package com.publicissapient.kpidashboard.zephyr.processor.service;

import java.util.List;

import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.zephyr.ZephyrTestCaseDTO;

public interface ZephyrDBService {

	/**
	 * Persist Zephyr test case data into mongo db in test_case_details collection
	 *
	 * @param testCases
	 * @param processorToolConnection
	 * @param isZephyrCloud
	 */
	void processTestCaseInfoToDB(final List<ZephyrTestCaseDTO> testCases,
			ProcessorToolConnection processorToolConnection, boolean isKanban, boolean isZephyrCloud);
}
