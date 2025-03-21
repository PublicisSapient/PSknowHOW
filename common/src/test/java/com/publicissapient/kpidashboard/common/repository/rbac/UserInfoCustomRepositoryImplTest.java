/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicissapient.kpidashboard.common.repository.rbac;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;

/*
author @shi6
 */
@ExtendWith(SpringExtension.class)
public class UserInfoCustomRepositoryImplTest {

	@Mock
	private MongoOperations operations;

	@InjectMocks
	private UserInfoCustomRepositoryImpl userInfoCustomRepository;

	@Test
	public void testFindByBasicProjectConfigIdInAndStateInOrderByStartDateDesc() {

		when(operations.find(any(Query.class), eq(UserInfo.class))).thenReturn(Collections.emptyList());
		// Call the method and assert the result
		List<UserInfo> result =
				userInfoCustomRepository.findAdminUserOfProject(List.of("basicConfigId"));
		userInfoCustomRepository.findByProjectAccess("basicConfigId");

		// Assert the result or perform further verifications
		assertEquals(Collections.emptyList(), result);
	}
}
