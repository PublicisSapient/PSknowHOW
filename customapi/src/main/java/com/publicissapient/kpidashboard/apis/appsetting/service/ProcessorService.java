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

package com.publicissapient.kpidashboard.apis.appsetting.service;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionBasicConfig;

/**
 *
 * @author pansharm5
 *
 */
public interface ProcessorService {
	/**
	 * Gets details of all processors on the running instance including: Last
	 * executed time of the processor to fetch new data and Status Success/Failure
	 *
	 * @return {@code ServiceResponse}
	 */
	ServiceResponse getAllProcessorDetails();

	/**
	 * Triggers the processor to fetch the latest data based on the processor name
	 * 
	 * @param processorName
	 *            name of the processor same as in the processor collection
	 * @return {@code ServiceResponse}
	 */
	ServiceResponse runProcessor(String processorName, ProcessorExecutionBasicConfig processorExecutionBasicConfig);

	/**
	 * Fetches the active sprint data for the project
	 * 
	 * @param sprintId
	 *            id of the sprint
	 * @return {@code ServiceResponse}
	 */
	ServiceResponse fetchActiveSprint(String sprintId);
}
