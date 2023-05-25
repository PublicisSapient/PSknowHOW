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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.mongodb.BasicDBObject;
import com.publicissapient.kpidashboard.common.model.jira.IssueBacklogCustomHistory;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;



/**
 * The Class FeatureCustomHistoryRepositoryImpl.
 */
@Service
public class IssueBacklogCustomHistoryRepositoryImpl implements IssueBacklogCustomHistoryQueryRepository {

	public static final String COUNT = "count";
	public static final String TYPE_COUNT_MAP = "typeCountMap";
	public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	/**
	 * The operations.
	 */
	@Autowired
	private MongoOperations operations;

	private static final String STATUS = "statusUpdationLog.changedTo";
	private static final String STORY_TYPE = "storyType";
	private static final String BASIC_PROJ_CONF_ID = "basicProjectConfigId";
	private static final String START_TIME = "T00:00:00.000Z";
	private static final String END_TIME = "T23:59:59.000Z";
	private static final String TICKET_CREATED_DATE_FIELD = "createdDate";

	@SuppressWarnings("unchecked")
	@Override
	public List<IssueBacklogCustomHistory> findByFilterAndFromStatusMap(Map<String, List<String>> mapOfFilters,
																		Map<String, Map<String, Object>> uniqueProjectMap) {
		Criteria criteria = new Criteria();
		// map of common filters Project and Sprint
		for (Map.Entry<String, List<String>> entry : mapOfFilters.entrySet()) {
			if (CollectionUtils.isNotEmpty(entry.getValue())) {
				criteria = criteria.and(entry.getKey()).in(entry.getValue());
			}
		}

		List<Criteria> projectCriteriaList = new ArrayList<>();
		uniqueProjectMap.forEach((project, filterMap) -> {
			Criteria projectCriteria = new Criteria();
			projectCriteria.and(STATUS).in((List<Pattern>) filterMap.get("statusUpdationLog.story.changedTo"));
			projectCriteriaList.add(projectCriteria);
		});

		Criteria criteriaAggregatedAtProjectLevel = new Criteria()
				.andOperator(projectCriteriaList.toArray(new Criteria[0]));
		Criteria criteriaProjectLevelAdded = new Criteria().andOperator(criteria, criteriaAggregatedAtProjectLevel);
		Query query = new Query(criteriaProjectLevelAdded);
		return operations.find(query, IssueBacklogCustomHistory.class);
	}
	@SuppressWarnings("rawtypes")
	@Override
	public List<Map> getStoryTypeCountByDateRange(String basicProjectConfigId, String startDate,
												  String endDate) {

		LocalDateTime startDateTime = LocalDateTime.parse(startDate + START_TIME, FORMATTER);
		LocalDateTime endDateTime = LocalDateTime.parse(endDate + END_TIME, FORMATTER);

		Criteria criteria = Criteria.where(BASIC_PROJ_CONF_ID).is(basicProjectConfigId)
				.and(TICKET_CREATED_DATE_FIELD).gte(startDateTime).lte(endDateTime);

		TypedAggregation<IssueBacklogCustomHistory> aggregation = Aggregation.newAggregation(IssueBacklogCustomHistory.class,
				Aggregation.match(criteria),
				Aggregation.project()
						.andExpression("dateToString('%Y-%m-%d', " + TICKET_CREATED_DATE_FIELD + ")").as("date")
						.and(STORY_TYPE).as("type"),
				Aggregation.group("date", "type").count().as(COUNT),
				Aggregation.group("date").push(new BasicDBObject("type", "$_id.type").append(COUNT, "$count"))
						.as(TYPE_COUNT_MAP),
				Aggregation.project().and("_id").as("date").and(TYPE_COUNT_MAP).as(TYPE_COUNT_MAP));

		AggregationResults<Map> results = operations.aggregate(aggregation, Map.class);
		return results.getMappedResults();

	}

}