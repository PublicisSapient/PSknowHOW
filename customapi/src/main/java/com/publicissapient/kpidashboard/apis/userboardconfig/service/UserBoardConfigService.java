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

package com.publicissapient.kpidashboard.apis.userboardconfig.service;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.userboardconfig.ConfigLevel;
import com.publicissapient.kpidashboard.common.model.userboardconfig.ProjectListRequested;
import com.publicissapient.kpidashboard.common.model.userboardconfig.UserBoardConfigDTO;

/**
 * Service class for user board config
 * 
 * @author narsingh9
 *
 */
public interface UserBoardConfigService {

	/**
	 * Retrieves the board configuration based on the specified configuration level
	 * and list of requested projects.
	 *
	 * @param configLevel
	 *            {@link ConfigLevel}
	 * @param listOfRequestedProj
	 *            the list of requested projects
	 * @return the board configuration DTO
	 */
	UserBoardConfigDTO getBoardConfig(ConfigLevel configLevel, ProjectListRequested listOfRequestedProj);

	/**
	 * Saves the board configuration based on the specified configuration level and
	 * project configuration ID.
	 *
	 * @param userBoardConfigDTO
	 *            the user board configuration DTO
	 * @param configLevel
	 *            {@link ConfigLevel}
	 * @param basicProjectConfigId
	 *            the ID of the basic project configuration
	 * @return the service response indicating success or failure
	 */
	ServiceResponse saveBoardConfig(UserBoardConfigDTO userBoardConfigDTO, ConfigLevel configLevel,
			String basicProjectConfigId);

	/**
	 * delete UserBoard config by userName
	 *
	 * @param userName
	 *            userName
	 */
	void deleteUser(String userName);

	/**
	 * Deletes the project board config.
	 *
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 */
	void deleteProjectBoardConfig(String basicProjectConfigId);

}
