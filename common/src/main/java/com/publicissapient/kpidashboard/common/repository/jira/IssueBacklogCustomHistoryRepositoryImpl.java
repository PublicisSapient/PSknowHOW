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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.mongodb.BasicDBObject;
import com.publicissapient.kpidashboard.common.model.jira.IssueBacklogCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
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
	/** The operations. */
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
	public Map<String, Map<String, Integer>> getStoryTypeCountByDateRange(String basicProjectConfigId, String startDate,
																		  String endDate) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

		LocalDateTime startDateTime = LocalDateTime.parse(new StringBuilder(startDate).append(START_TIME).toString(),
				formatter);
		LocalDateTime endDateTime = LocalDateTime.parse(new StringBuilder(endDate).append(END_TIME).toString(),
				formatter);

		Map<String, Map<String, Integer>> storyTypeCountMap = new LinkedHashMap<>();

		LocalDate currentDate = startDateTime.toLocalDate();
		while (!currentDate.isAfter(endDateTime.toLocalDate())) {
			String formattedDate = currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
			storyTypeCountMap.put(formattedDate, new HashMap<>());

			currentDate = currentDate.plusDays(1);
		}

		Criteria criteria = Criteria.where(BASIC_PROJ_CONF_ID).is(basicProjectConfigId).and(TICKET_CREATED_DATE_FIELD)
				.gte(startDateTime).lte(endDateTime);


		// create a new aggregation instance,then adds a $match stage to the aggregation pipeline, filtering the docs based on the provided criteria.
		//then $project stage to the pipeline. It projects two fields: date,& , and type,$group stage to the pipeline.
		//It groups the documents by date and type fields and calculates the count of each group using the $count operator.
		//and finally $project stage to reshape the output. It renames the _id field to date and keeps the typeCountMap
		TypedAggregation<JiraIssueCustomHistory> aggregation = Aggregation.newAggregation(JiraIssueCustomHistory.class,
				Aggregation.match(criteria),
				Aggregation.project().andExpression("dateToString('%Y-%m-%d', " + TICKET_CREATED_DATE_FIELD + ")")
						.as("date").and(STORY_TYPE).as("type"),
				Aggregation.group("date", "type").count().as(COUNT),
				Aggregation.group("_id.date").push(new BasicDBObject("type", "$_id.type").append(COUNT, "$count"))
						.as(TYPE_COUNT_MAP),
				Aggregation.project().and("_id").as("date").and(TYPE_COUNT_MAP).as(TYPE_COUNT_MAP));

		AggregationResults<Map> results = operations.aggregate(aggregation, Map.class);
		List<Map> mappedResults = results.getMappedResults();

		for (Map map : mappedResults) {
			String date = (String) map.get("date");
			LocalDate createdDate = LocalDateTime
					.parse(new StringBuilder(date).append(START_TIME).toString(), formatter).toLocalDate();
			List<Map<String, Integer>> typeCountMap = (List<Map<String, Integer>>) map.get(TYPE_COUNT_MAP);

			for (LocalDate createdDate1 = createdDate; !createdDate1
					.isAfter(endDateTime.toLocalDate()); createdDate1 = createdDate1.plusDays(1)) {
				String formattedDate = createdDate1.format(DateTimeFormatter.ISO_LOCAL_DATE);
				Map<String, Integer> typeCounts = storyTypeCountMap.get(formattedDate);

				for (Map<String, Integer> typeCount : typeCountMap) {
					String type = String.valueOf(typeCount.get("type"));
					int count = typeCount.get(COUNT);

					typeCounts.merge(type, count, Integer::sum);
				}
			}
		}
		return storyTypeCountMap;
	}

}