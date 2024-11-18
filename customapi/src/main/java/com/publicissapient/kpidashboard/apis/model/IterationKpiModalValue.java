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

package com.publicissapient.kpidashboard.apis.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * object used to bind iteration kpi's value
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class IterationKpiModalValue extends IssueKpiModalValue {

	private Map<String, List<String>> statusLogGroup;
	private Map<String, List<String>> workLogGroup;
	private Map<String, List<String>> assigneeLogGroup;
	private String timeWithUser;
	private String timeWithStatus;
	private Long loggedWorkInSeconds;
	private String epicName;
	private boolean spill;
	private Long remainingEstimateInSeconds;
	private Long originalEstimateInSeconds;
	private Set<String> subTask;
	private Set<String> parentStory;
	private boolean preClosed;

}
