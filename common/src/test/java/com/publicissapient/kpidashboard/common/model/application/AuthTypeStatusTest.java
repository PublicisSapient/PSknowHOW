/*
 *
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.publicissapient.kpidashboard.common.model.application;

import org.junit.Assert;
import org.junit.Test;

public class AuthTypeStatusTest {
	AuthTypeStatus authTypeStatus = new AuthTypeStatus();

	@Test
	public void testSetStandardLogin() throws Exception {
		authTypeStatus.setStandardLogin(true);
	}

	@Test
	public void testSetAdLogin() throws Exception {
		authTypeStatus.setAdLogin(true);
	}

	@Test
	public void testEquals() throws Exception {
		boolean result = authTypeStatus.equals("o");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testCanEqual() throws Exception {
		boolean result = authTypeStatus.canEqual("other");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testToString() throws Exception {
		String result = authTypeStatus.toString();
		Assert.assertNotNull(result);
	}
}

