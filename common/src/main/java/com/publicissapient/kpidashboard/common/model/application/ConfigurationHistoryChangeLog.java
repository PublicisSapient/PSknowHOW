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
package com.publicissapient.kpidashboard.common.model.application;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.publicissapient.kpidashboard.common.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data

/**
 * maintains the data of fieldmapping history
 * @author shi6
 */
public class ConfigurationHistoryChangeLog {

	@JsonProperty("Changed From")
	private Object changedFrom;
	@JsonProperty("Changed To")
	private Object changedTo;
	@JsonProperty("Changed By")
	private String changedBy;
	@JsonProperty("Changed At")
	private String updatedOn;
	private String  releaseNodeId;

	public ConfigurationHistoryChangeLog(Object changedFrom, Object changedTo, String changedBy, String updatedOn) {
		this.changedFrom = changedFrom;
		this.changedTo = changedTo;
		this.changedBy = changedBy;
		this.updatedOn = updatedOn;
	}



}
