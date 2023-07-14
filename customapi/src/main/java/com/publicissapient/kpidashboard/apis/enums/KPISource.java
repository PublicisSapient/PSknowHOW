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

package com.publicissapient.kpidashboard.apis.enums;

import java.util.Arrays;

/**
 * The enum Kpi source.
 *
 * @author tauakram
 */
public enum KPISource {

	/**
	 * Jira kpi source.
	 */
	JIRA("JIRA"),
	/**
	 * Sonar kpi source.
	 */
	SONAR("SONAR"),
	/**
	 * Bitbucket kpi source.
	 */
	BITBUCKET("BITBUCKET"),
	/**
	 * Jenkins kpi source.
	 */
	JENKINS("JENKINS"),
	/**
	 * Zephyr kpi source.
	 */
	ZEPHYR("ZEPHYR"),
	/**
	 * Excel kpi source.
	 */
	EXCEL("EXCEL"),
	/**
	 * Bamboo kpi source.
	 */

	BAMBOO("BAMBOO"),
	/**
	 * Jirakanban kpi source.
	 */

	JIRAKANBAN("JIRAKANBAN"),
	/**
	 * Zephyrkanban kpi source.
	 */
	ZEPHYRKANBAN("ZEPHYRKANBAN"),
	/**
	 * Sonarkanban kpi source.
	 */
	SONARKANBAN("SONARKANBAN"),
	/**
	 * Bitbucketkanban kpi source.
	 */
	BITBUCKETKANBAN("BITBUCKETKANBAN"),
	/**
	 * Jenkinskanban kpi source.
	 */
	JENKINSKANBAN("JENKINSKANBAN"),
	/**
	 * Teamcity kpi source
	 */
	TEAMCITY("TEAMCITY"),
	/**
	 * TeamcityKanban kpi source
	 */
	TEAMCITYKANBAN("TEAMCITYKANBAN"),
	/**
	 * QADEFECTDENSITY kpi source.
	 */
	QADEFECTDENSITY("QADEFECTDENSITY"),

	/**
	 * Invalid.
	 */
	INVALID("INVALID");

	private String value;

	KPISource(String value) {
		this.value = value;
	}

	public static KPISource getKPISource(String value) {
		return Arrays.asList(KPISource.values()).stream().filter(kpi -> kpi.getValue().equalsIgnoreCase(value))
				.findAny().orElse(INVALID);
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
}
