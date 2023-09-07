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

/**
 * 
 */

package com.publicissapient.kpidashboard.common.model.testexecution;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author sansharm13
 *
 */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TestExecutionData {
	private String projectNodeId;
	private String projectName;
	private String sprintName;
	private String sprintId;
	private String sprintState;
	private Integer totalTestCases;
	private Integer executedTestCase;
	private Integer passedTestCase;
	private Integer automatedTestCases;
	private Integer automatableTestCases;
	private Integer automatedRegressionTestCases;
	private Integer totalRegressionTestCases;
	private String executionDate;
	private String basicProjectConfigId;
	private boolean kanban;
	private boolean uploadEnable;
}
