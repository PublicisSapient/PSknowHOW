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
	 * This method return user board config if present in db else return a default
	 * configuration.
	 * 
	 * @return UserBoardConfigDTO
	 */
	UserBoardConfigDTO getUserBoardConfig(ProjectListRequested listOfRequestedProj);

	/**
	 * This method save user board config
	 * 
	 * @param userBoardConfigDTO
	 *            userBoardConfigDTO
	 * @return UserBoardConfigDTO
	 */
	UserBoardConfigDTO saveUserBoardConfig(UserBoardConfigDTO userBoardConfigDTO);

	/**
	 * delete UserBoard Data by userName
	 *
	 * @param userName
	 */
	void deleteUser(String userName);

	/**
	 * This method fetch admin / superAdmin project level board config
	 * 
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 * @return admin user board config
	 */
	UserBoardConfigDTO getProjBoardConfigAdmin(String basicProjectConfigId);

	/**
	 * This method save project board config of proj/Super admin with
	 * basicProjectConfigId, also modify other admin configs of that project
	 *
	 * @param userBoardConfigDTO
	 *            userBoardConfigDTO
	 * @param basicProjectConfigId
	 *            basicProjConfigId
	 * @return UserBoardConfigDTO
	 */
	UserBoardConfigDTO saveUserBoardConfigAdmin(UserBoardConfigDTO userBoardConfigDTO, String basicProjectConfigId);
}
