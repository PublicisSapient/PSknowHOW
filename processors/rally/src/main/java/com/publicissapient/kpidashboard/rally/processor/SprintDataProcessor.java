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
package com.publicissapient.kpidashboard.rally.processor;

import java.io.IOException;
import java.util.Set;

import com.publicissapient.kpidashboard.rally.model.HierarchicalRequirement;
import com.publicissapient.kpidashboard.rally.model.ProjectConfFieldMapping;
import org.bson.types.ObjectId;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

/**
 * @author pankumar8
 */
public interface SprintDataProcessor {
	/**
	 * @param projectConfig
	 *          projectConfig
	 * @param boardId
	 *          boardId
	 * @param processorId
	 * @return Set of SprintDetails
	 * @throws IOException
	 *           throws io exception
	 */
	Set<SprintDetails> processSprintData(HierarchicalRequirement hierarchicalRequirement, ProjectConfFieldMapping projectConfig, String boardId,
										 ObjectId processorId) throws IOException;
}
