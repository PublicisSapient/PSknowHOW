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
package com.publicissapient.kpidashboard.jira.client.sprint;

import java.util.Set;

import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.jira.adapter.JiraAdapter;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

/**
 * @author yasbano
 *
 */

public interface SprintClient {

	/**
	 * This method handles sprint detailsList
	 * 
	 * @param projectConfig
	 *            projectConfig
	 * @param sprintDetailsList
	 *            sprintDetailsList
	 */
	public void processSprints(ProjectConfFieldMapping projectConfig, Set<SprintDetails> sprintDetailsList,
			JiraAdapter jiraAdapter, boolean isSprintFetch) throws InterruptedException;

	/**
	 * This method fetch sprint report
	 * 
	 * @param projectConfig
	 * @param jiraAdapter
	 */
	public void createSprintDetailBasedOnBoard(ProjectConfFieldMapping projectConfig, JiraAdapter jiraAdapter)
			throws InterruptedException;
}
