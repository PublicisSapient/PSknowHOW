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

import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents test case.
 */
@SuppressWarnings("PMD.TooManyFields")
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ZephyrTestCaseDTO {

	private String owner;
	private String updatedBy; // unused
	private String updatedOn;
	private int majorVersion; // unused
	private String priority; // unused
	private String createdOn;
	private String objective; // unused
	private String component; // unused
	private String projectKey; // unused
	private String folder;
	private String createdBy; // unused
	private boolean latestVersion; // unused
	private String lastTestResultStatus; // unused
	private String name; // unused
	private String key;
	private String status; // unused
	private Map<String, String> customFields;
	private ZephyrTestCaseScript testScript; // unused
	private Set<String> issueLinks;
	private List<String> labels;

}