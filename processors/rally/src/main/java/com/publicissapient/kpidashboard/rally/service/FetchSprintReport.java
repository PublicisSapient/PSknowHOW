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
package com.publicissapient.kpidashboard.rally.service;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.publicissapient.kpidashboard.rally.model.ProjectConfFieldMapping;
import org.bson.types.ObjectId;

import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

/**
 * @author pankumar8
 */
public interface FetchSprintReport {

	/**
	 * @param projectConfig
	 *          projectConfig
	 * @param sprintDetailsSet
	 *          sprintDetailsSet
	 * @param krb5Client
	 *          krb5Client
	 * @param isSprintFetch
	 *          isSprintFetch
	 * @param processorId
	 * @return Set of SprintDetails
	 * @throws IOException
	 *           throws IOException
	 */
	Set<SprintDetails> fetchSprints(ProjectConfFieldMapping projectConfig, Set<SprintDetails> sprintDetailsSet,
									KerberosClient krb5Client, boolean isSprintFetch, ObjectId processorId) throws IOException;

	/**
	 * @param projectConfig
	 *          projectConfig
	 * @param krb5Client
	 *          krb5Client
	 * @param boardDetails
	 *          boardDetails
	 * @param objectId
	 * @return List of SprintDetails
	 * @throws IOException
	 *           throws IOException
	 */
	List<SprintDetails> createSprintDetailBasedOnBoard(ProjectConfFieldMapping projectConfig, KerberosClient krb5Client,
			BoardDetails boardDetails, ObjectId objectId) throws IOException;

	/**
	 * @param projectConfig
	 *          projectConfig
	 * @param boardId
	 *          boardId
	 * @param krb5Client
	 *          krb5Client
	 * @return List of SprintDetails
	 * @throws IOException
	 *           throws IOException
	 */
	List<SprintDetails> getSprints(ProjectConfFieldMapping projectConfig, String boardId, KerberosClient krb5Client)
			throws IOException;
}
