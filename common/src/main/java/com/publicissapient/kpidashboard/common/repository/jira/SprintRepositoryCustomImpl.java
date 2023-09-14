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
import java.util.Set;

import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.ReplaceRootOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
/**
 * Repository for {@link SprintDetails} with custom methods implementation.
 */
@Service
public class SprintRepositoryCustomImpl implements SprintRepositoryCustom {

	@Autowired
	private MongoOperations operations;
	@Autowired
	private SprintRepository sprintRepository;

	private static final String BASIC_PROJECT_CONFIG_ID ="basicProjectConfigId";
	private static final String SPRINTS ="sprints";
	private static final String STATE ="state";
	private static final String END_DATE ="endDate";
	private static final String SPRINT_ID ="sprintID";
	private static final String NOT_COMPLETED_ISSUES ="notCompletedIssues";
	private static final String COMPLETED_ISSUES ="completedIssues";
	private static final String SPRINT_NAME ="sprintName";
	private static final String START_DATE ="startDate";
	private static final String COMPLETE_DATE ="completeDate";
	private static final String TOTAL_ISSUES ="totalIssues";
	private static final String SPRINT_DETAILS ="sprint_details";
	@Override
	public List<SprintDetails> findByBasicProjectConfigIdInAndStateInOrderByStartDateDesc(
			Set<ObjectId> basicProjectConfigIds, List<String> sprintStatusList, long limit) {
		MatchOperation matchStage = Aggregation.match(
				Criteria.where(BASIC_PROJECT_CONFIG_ID).in(basicProjectConfigIds).and(STATE).in(sprintStatusList));

		SortOperation sortStage = Aggregation.sort(Sort.Direction.DESC, END_DATE);

		GroupOperation groupStage = Aggregation.group(BASIC_PROJECT_CONFIG_ID).push("$$ROOT").as(SPRINTS);

		ProjectionOperation sliceStage = Aggregation.project().and(SPRINTS).slice((int) limit).as(SPRINTS);

		UnwindOperation unwindStage = Aggregation.unwind(SPRINTS);

		ReplaceRootOperation replaceRootStage = Aggregation.replaceRoot(SPRINTS);
		ProjectionOperation projectStage = Aggregation.project(SPRINT_ID, BASIC_PROJECT_CONFIG_ID, NOT_COMPLETED_ISSUES,
				COMPLETED_ISSUES, SPRINT_NAME, START_DATE, END_DATE, COMPLETE_DATE, TOTAL_ISSUES,STATE);

		Aggregation aggregation = Aggregation.newAggregation(matchStage, sortStage, groupStage, sliceStage, unwindStage,
				replaceRootStage, projectStage);

		return operations.aggregate(aggregation, SPRINT_DETAILS, SprintDetails.class).getMappedResults();
	}

}
