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

import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.common.model.jira.MetadataValue;

/**
 * @author jagmongr
 */
public interface EditKpiConfigService {
	/**
	 * Gets data by type for the Edit config.
	 *
	 * @param projectconfigid
	 *          projectconfigid
	 * @param kpiCode
	 *          kpiCode
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */
	Map<String, List<MetadataValue>> getDataForType(String projectconfigid, String kpiCode);
}
