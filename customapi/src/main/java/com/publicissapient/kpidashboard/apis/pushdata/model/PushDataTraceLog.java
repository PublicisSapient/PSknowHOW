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

package com.publicissapient.kpidashboard.apis.pushdata.model;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * token details for push data via expose api
 */

@Data
@Getter
@Setter
@NoArgsConstructor
@Document(collection = "push_data_trace_log")
public class PushDataTraceLog extends BasicModel {
	private static PushDataTraceLog pushDataTraceLog = null;

	private String pushApiSource;
	private String projectName;
	private ObjectId basicProjectConfigId;
	private String userName;
	private String requestTime;
	private String responseStatus;
	private String responseCode;
	private int totalRecord;
	private int totalSavedRecord;
	private int totalFailedRecord;
	private String errorMessage;
	private List<PushDataDetail> pushDataDetails;

	public static synchronized PushDataTraceLog getInstance() {
		if (pushDataTraceLog == null) {
			pushDataTraceLog = new PushDataTraceLog();
		}
		return pushDataTraceLog;
	}

	public static void destroy() {
		pushDataTraceLog = null;
	}
}
