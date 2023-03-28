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

package com.publicissapient.kpidashboard.apis.projectconfig.projecttoolconfig.service;

import java.util.List;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfigDTO;

/**
 * @author yasbano
 * @author dilipkr
 *
 */
public interface ProjectToolConfigService {

	/**
	 * Get all tools.
	 * 
	 * @return ServiceResponse with data object,message and status flag. Status flag
	 *         is true, if data is found else false.
	 */
	ServiceResponse getAllProjectTool();

	/**
	 * Get tool by type.
	 * 
	 * @param type
	 * @return ServiceResponse with data object,message and status flag. Status flag
	 *         is true, if data is found else false.
	 */
	ServiceResponse getProjectToolByType(String type);

	/**
	 * Creates and save proejectTool. *
	 * 
	 * @param projectToolConfig*
	 * 
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */
	ServiceResponse saveProjectToolDetails(ProjectToolConfig projectToolConfig);

	/**
	 * Modifies a project_tool_configs. Finds by @param proejcToolId and replaces it
	 * with @param projectToolConfig *
	 * 
	 * @param projectToolConfig
	 * @param proejcToolId
	 *            *
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */
	ServiceResponse modifyProjectToolById(ProjectToolConfig projectToolConfig, String proejcToolId);

	/**
	 * Gets tools of the project
	 * 
	 * @param basicProjectConfigId
	 *            mongo id of
	 * @return list of tools
	 */
	List<ProjectToolConfigDTO> getProjectToolConfigs(String basicProjectConfigId);

	/**
	 * Gets tools of the project filtered by type
	 * 
	 * @param basicProjectConfigId
	 * @param type
	 * @return
	 */
	List<ProjectToolConfigDTO> getProjectToolConfigs(String basicProjectConfigId, String type);

	boolean deleteTool(String basicProjectConfigId, String projectToolId);

	boolean cleanToolData(String basicProjectConfigId, String projectToolId);

}
