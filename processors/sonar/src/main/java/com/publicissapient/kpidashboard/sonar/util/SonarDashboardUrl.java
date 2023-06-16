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

/**
 * Contains Sonar dashboard url.
 *
 */
public class SonarDashboardUrl {

	private static final String SLASH = "/";
	private static final String PATH = "dashboard/index/";

	private final String projectUrl;
	private final String instanceId;

	/**
	 * Construct object with project url and instanceId.
	 * 
	 * @param projectUrl
	 *            the project URL
	 * @param instanceId
	 *            the instance Id
	 */
	public SonarDashboardUrl(String projectUrl, String instanceId) {
		this.projectUrl = projectUrl;
		this.instanceId = instanceId;
	}

	/**
	 * Overridden method of String's toString.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(projectUrl);
		if (!projectUrl.endsWith(SLASH)) {
			sb.append(SLASH);
		}

		sb.append(PATH).append(instanceId);
		return sb.toString();
	}
}
