/*
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.userboardconfig.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.model.userboardconfig.UserBoardConfig;
import com.publicissapient.kpidashboard.common.model.userboardconfig.UserBoardConfigDTO;

/**
 * This class is used to map UserBoardConfigDTO to UserBoardConfig and vice
 * versa
 * 
 * @author shunaray
 */
@Component
public class UserBoardConfigMapper {

	/**
	 * This method convert userBoardConfigDTO to its userBoardConfig K
	 *
	 * @param userBoardConfigDTO
	 *            userBoardConfigDTO
	 * @return UserBoardConfig
	 */
	public UserBoardConfig toEntity(UserBoardConfigDTO userBoardConfigDTO) {
		UserBoardConfig userBoardConfig = null;
		if (null != userBoardConfigDTO) {
			ModelMapper modelMapper = new ModelMapper();
			userBoardConfig = modelMapper.map(userBoardConfigDTO, UserBoardConfig.class);
		}
		return userBoardConfig;
	}

	/**
	 * This method convert user board config to its dto
	 *
	 * @param userBoardConfig
	 *            userBoardConfig
	 * @return UserBoardConfigDTOb
	 */
	public UserBoardConfigDTO toDto(UserBoardConfig userBoardConfig) {
		UserBoardConfigDTO userBoardConfigDTO = null;
		if (null != userBoardConfig) {
			ModelMapper modelMapper = new ModelMapper();
			userBoardConfigDTO = modelMapper.map(userBoardConfig, UserBoardConfigDTO.class);
		}
		return userBoardConfigDTO;
	}

}
