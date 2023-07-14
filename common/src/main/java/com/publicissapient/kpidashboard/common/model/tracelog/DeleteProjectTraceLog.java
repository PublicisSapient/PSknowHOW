/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicissapient.kpidashboard.common.model.tracelog;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Document;

import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

import lombok.Data;

/**
 * @author anisingh4
 */
@Document(collection = "delete_project_trace_log")
@Data
public class DeleteProjectTraceLog extends BasicModel {
	private ProjectBasicConfig projectBasicConfig;
	private String deletedBy;
	private LocalDateTime deletionDate;

}
