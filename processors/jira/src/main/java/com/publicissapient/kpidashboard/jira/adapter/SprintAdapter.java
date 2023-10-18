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
package com.publicissapient.kpidashboard.jira.adapter;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

/**
 * @author yasbano
 *
 */
public interface SprintAdapter {

	/**
	 * this method fetch sprints
	 * 
	 * @param projectConfFieldMapping
	 *            projectConfFieldMapping
	 * @param boardId
	 *            boardId
	 * @param startAt
	 *            startAt
	 * @param hasMore
	 *            hasMore
	 * @return List of SprintDetails
	 */
	List<SprintDetails> getSprints(ProjectConfFieldMapping projectConfFieldMapping, String boardId, int startAt,
			AtomicBoolean hasMore);

	/**
	 * 
	 * @return page size
	 */
	int getPageSize();

}
