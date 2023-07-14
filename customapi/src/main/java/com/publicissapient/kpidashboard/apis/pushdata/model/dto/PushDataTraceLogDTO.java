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

package com.publicissapient.kpidashboard.apis.pushdata.model.dto;

import java.util.List;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushDataDetail;
import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PushDataTraceLogDTO
 */

@Data
@Getter
@Setter
@NoArgsConstructor
public class PushDataTraceLogDTO extends BasicModel {
	private static PushDataTraceLogDTO pushDataTraceLog = null;

	@JsonProperty("Push Api Source")
	private String pushApiSource;
	@JsonProperty("Project Name")
	private String projectName;
	private ObjectId basicProjectConfigId;
	@JsonProperty("User Name")
	private String userName;
	@JsonProperty("Request Time")
	private String requestTime;
	@JsonProperty("Response Status")
	private String responseStatus;
	@JsonProperty("Response Code")
	private String responseCode;
	@JsonProperty("Total Records")
	private int totalRecord;
	@JsonProperty("Total Saved Records")
	private int totalSavedRecord;
	@JsonProperty("Total Failed Records")
	private int totalFailedRecord;
	@JsonProperty("Message")
	private String errorMessage;
	@JsonProperty("Push Details Info")
	private List<PushDataDetail> pushDataDetails;
}
