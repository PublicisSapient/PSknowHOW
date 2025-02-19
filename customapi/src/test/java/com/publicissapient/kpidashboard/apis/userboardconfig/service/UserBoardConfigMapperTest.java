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

import static org.junit.Assert.assertNotNull;
import static org.testng.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.common.model.userboardconfig.UserBoardConfig;
import com.publicissapient.kpidashboard.common.model.userboardconfig.UserBoardConfigDTO;

@RunWith(MockitoJUnitRunner.class)
public class UserBoardConfigMapperTest {

	@InjectMocks
	private UserBoardConfigMapper userBoardConfigMapper;

	@Test
	public void testToDto() {
		UserBoardConfig userBoardConfig = new UserBoardConfig();
		userBoardConfig.setUsername("testuser");
		userBoardConfig.setBasicProjectConfigId("proj1");

		UserBoardConfigDTO userBoardConfigDTO = userBoardConfigMapper.toDto(userBoardConfig);

		assertNotNull(userBoardConfigDTO);
		assertEquals(userBoardConfigDTO.getUsername(), "testuser");
		assertEquals(userBoardConfigDTO.getBasicProjectConfigId(), "proj1");
	}

	@Test
	public void testToEntity() {
		UserBoardConfigDTO userBoardConfigDTO = new UserBoardConfigDTO();
		userBoardConfigDTO.setUsername("testuser");
		userBoardConfigDTO.setBasicProjectConfigId("proj1");

		UserBoardConfig userBoardConfig = userBoardConfigMapper.toEntity(userBoardConfigDTO);

		assertNotNull(userBoardConfig);
		assertEquals(userBoardConfig.getUsername(), "testuser");
		assertEquals(userBoardConfig.getBasicProjectConfigId(), "proj1");
	}
}
