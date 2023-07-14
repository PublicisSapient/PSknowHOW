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

import java.util.List;

import org.bson.types.ObjectId;

import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.generic.BasicModel;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectToolConfigProcessorItem extends BasicModel {
	List<Connection> connection;
	List<ProcessorItem> processorItemList;
	private String toolName;
	private ObjectId basicProjectConfigId;
	private ObjectId connectionId;
	private String projectId;
	private String projectKey;
	private String jobName;
	private String branch;
	private String env;
	private String repoSlug;
	private String repositoryName;
	private String bitbucketProjKey;
	private String apiVersion;
	private String newRelicApiQuery;
	private List<String> newRelicAppNames;
	private String createdAt;
	private String updatedAt;
	private boolean queryEnabled;
	private String boardQuery;
}
