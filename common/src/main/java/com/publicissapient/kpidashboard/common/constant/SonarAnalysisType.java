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

import com.publicissapient.kpidashboard.common.model.sonar.SonarDetails;

/**
 * Enumerates the possible {@link SonarDetails} types.
 *
 * @author anisingh4
 */
public enum SonarAnalysisType {

	STATIC_ANALYSIS(ProcessorType.SONAR_ANALYSIS), SECURITY_ANALYSIS(ProcessorType.STATIC_SECURITY_SCAN);

	private final ProcessorType processorType;

	SonarAnalysisType(ProcessorType processorType) {
		this.processorType = processorType;
	}

	public static SonarAnalysisType fromString(String value) {
		for (SonarAnalysisType qualityType : values()) {
			if (qualityType.toString().equalsIgnoreCase(value)) {
				return qualityType;
			}
		}
		throw new IllegalArgumentException(value + " is not a valid SonarAnalysisType.");
	}

	public ProcessorType processorType() {
		return processorType;
	}
}
