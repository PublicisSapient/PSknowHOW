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
package com.publicissapient.kpidashboard.jira.service;

import java.util.List;
import java.util.Set;

import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

/**
 * @author pankumar8
 *
 */
public interface FetchSprintReport {

	/**
	 * @param projectConfig
	 * @param sprintDetailsSet
	 * @param setForCacheClean
	 * @param krb5Client
	 * @return Set<SprintDetails>
	 * @throws InterruptedException
	 */
	Set<SprintDetails> fetchSprints(ProjectConfFieldMapping projectConfig, Set<SprintDetails> sprintDetailsSet,
			Set<SprintDetails> setForCacheClean, KerberosClient krb5Client) throws InterruptedException;

	/**
	 * @param projectConfig
	 * @param setForCacheClean
	 * @param krb5Client
	 * @return List<SprintDetails>
	 * @throws InterruptedException
	 */
	List<SprintDetails> createSprintDetailBasedOnBoard(ProjectConfFieldMapping projectConfig,
			Set<SprintDetails> setForCacheClean, KerberosClient krb5Client) throws InterruptedException;

	/**
	 * @param projectConfig
	 * @param boardId
	 * @param krb5Client
	 * @return List<SprintDetails>
	 */
	List<SprintDetails> getSprints(ProjectConfFieldMapping projectConfig, String boardId, KerberosClient krb5Client);
}
