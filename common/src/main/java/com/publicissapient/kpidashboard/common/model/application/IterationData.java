/*
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.publicissapient.kpidashboard.common.model.application;

import java.util.List;

import lombok.Data;

/**
 * Represents data for a specific iteration.
 *
 * @author shunaray
 */
@Data
public class IterationData {
	private String iterationName;
	private List<String> issuesTagged;

	/**
	 * Constructs an IterationData object with the specified iteration name and
	 * issues tagged.
	 *
	 * @param iterationName
	 *            the name of the iteration
	 * @param issuesTagged
	 *            the list of issues tagged in the iteration
	 */
	public IterationData(String iterationName, List<String> issuesTagged) {
		this.iterationName = iterationName;
		this.issuesTagged = issuesTagged;
	}
}
