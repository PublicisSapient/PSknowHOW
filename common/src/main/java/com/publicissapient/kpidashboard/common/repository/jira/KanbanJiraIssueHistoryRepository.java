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

package com.publicissapient.kpidashboard.common.repository.jira;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;

/**
 * The interface Kanban feature history repository.
 */
@Repository
public interface KanbanJiraIssueHistoryRepository extends CrudRepository<KanbanIssueCustomHistory, ObjectId>,
		QuerydslPredicateExecutor<KanbanIssueCustomHistory>, KanbanJiraIssueHistoryRepoCustom {

	/**
	 * Find by story id list.
	 *
	 * @param storyID
	 *            the story id
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 * @return the KanbanIssueCustomHistory
	 */
	@Query(fields = "{ 'storyID' : 1, 'createdDate' : 1, 'estimate' : 1, 'bufferedEstimateTime': 1 }")
	KanbanIssueCustomHistory findByStoryIDAndBasicProjectConfigId(String storyID, String basicProjectConfigId);

	/**
	 * Deletes all documents that matches with given projectID.
	 * 
	 * @param projectID
	 *            String projectID
	 */
	void deleteByBasicProjectConfigId(String projectID);

    List<KanbanIssueCustomHistory> findByBasicProjectConfigId(String basicProjectConfigId);
}
