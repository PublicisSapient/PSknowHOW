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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class NotificationEnumTest {

	@Test
	public void testEnumValues() {
		assertEquals("Project Access Request", NotificationEnum.PROJECT_ACCESS.getValue());
		assertEquals("User Access Request", NotificationEnum.USER_APPROVAL.getValue());
	}

	@Test
	public void testEnumEquality() {
		assertEquals(NotificationEnum.PROJECT_ACCESS, NotificationEnum.valueOf("PROJECT_ACCESS"));
		assertEquals(NotificationEnum.USER_APPROVAL, NotificationEnum.valueOf("USER_APPROVAL"));
	}

	/*
	 * @Test(expected = IllegalArgumentException.class) public void
	 * testInvalidEnumValue() { NotificationEnum.valueOf("INVALID_NOTIFICATION"); }
	 */
}
