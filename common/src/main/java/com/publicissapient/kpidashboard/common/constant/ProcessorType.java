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

public enum ProcessorType {

	BUILD("Build"), @Deprecated
	FEATURE("Feature"), SONAR_ANALYSIS("SonarDetails"), EXCEL("Excel"), APP_PERFORMANCE("AppPerformance"), AGILE_TOOL(
			"AgileTool"), STATIC_SECURITY_SCAN(
					"StaticSecurityScan"), NEW_RELIC("NewRelic"), SCM("Scm"), TESTING_TOOLS("TestingTools");

	private String value;

	ProcessorType(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}
}
