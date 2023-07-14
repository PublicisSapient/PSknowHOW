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

package com.publicissapient.kpidashboard.sonar.util;

import org.junit.Assert;
import org.junit.Test;

public class SonarDashboardUrlTest {

	private static final String SLASH = "/";
	private static final String INSTANCE_URL = "http://your.company.org/sonar";
	private static final String PATH = "/dashboard/index/";
	private static final String PROJECT_ID = "8675309";

	private static final String EXPECTED = INSTANCE_URL + PATH + PROJECT_ID;

	@Test
	public void testWithoutTrailingSlash() {
		Assert.assertEquals(EXPECTED, new SonarDashboardUrl(INSTANCE_URL, PROJECT_ID).toString());
	}

	@Test
	public void testWithTrailingSlash() {
		Assert.assertEquals(EXPECTED, new SonarDashboardUrl(INSTANCE_URL + SLASH, PROJECT_ID).toString());
	}

}
