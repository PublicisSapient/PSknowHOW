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

package com.publicissapient.kpidashboard.apis.common.service;

import com.publicissapient.kpidashboard.apis.model.ConfigDetails;

/**
 * Contract to get kpi wise aggregation type
 * 
 * @author pkum34
 *
 */
public interface ConfigDetailService {

	/**
	 * Gets kpi wise aggregation type.
	 * 
	 * @return kpi wise aggregation type
	 */
	ConfigDetails getConfigDetails();

}
