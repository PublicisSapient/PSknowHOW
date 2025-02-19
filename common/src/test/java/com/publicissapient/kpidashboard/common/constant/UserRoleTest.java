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

package com.publicissapient.kpidashboard.common.constant;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

public class UserRoleTest {

	@Test
	public void testUserRoleEnumValues() {
		assertEquals(UserRole.ROLE_USER, UserRole.valueOf("ROLE_USER"));
		assertEquals(UserRole.ROLE_ADMIN, UserRole.valueOf("ROLE_ADMIN"));
		assertEquals(UserRole.ROLE_API, UserRole.valueOf("ROLE_API"));
	}

	@Test
	public void testUserRoleEnumToString() {
		assertEquals("ROLE_USER", UserRole.ROLE_USER.toString());
		assertEquals("ROLE_ADMIN", UserRole.ROLE_ADMIN.toString());
		assertEquals("ROLE_API", UserRole.ROLE_API.toString());
	}
}
