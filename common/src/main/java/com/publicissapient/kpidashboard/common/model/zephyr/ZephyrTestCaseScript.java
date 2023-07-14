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

package com.publicissapient.kpidashboard.common.model.zephyr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents test case script.
 */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ZephyrTestCaseScript {

	private int id;
	private String type;
	private ZephyrTestCaseStep[] steps;

	/**
	 * Get steps zephyr test case step [ ].
	 *
	 * @return the zephyr test case step [ ]
	 */
	public ZephyrTestCaseStep[] getSteps() {
		return steps == null ? null : steps.clone();
	}

	/**
	 * Sets steps.
	 *
	 * @param steps
	 *            the steps
	 */
	public void setSteps(ZephyrTestCaseStep[] steps) {
		this.steps = steps == null ? null : steps.clone();
	}
}