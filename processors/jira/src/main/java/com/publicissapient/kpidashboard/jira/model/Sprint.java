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

package com.publicissapient.kpidashboard.jira.model;

import com.atlassian.jira.rest.client.api.IdentifiableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * An object representing a com.atlassian.greenhopper.service.sprint.Sprint.
 */
@Getter
@Setter
public class Sprint implements IdentifiableEntity<Long> {
	private Long id;
	private Long rapidViewId;
	private String state;
	private String name;
	private String startDateStr;
	private String endDateStr;
	private String completeDateStr;
	private int sequence;
}
