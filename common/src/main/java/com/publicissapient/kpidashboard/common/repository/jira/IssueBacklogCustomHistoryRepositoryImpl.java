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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.model.jira.IssueBacklogCustomHistory;

/**
 * The Class FeatureCustomHistoryRepositoryImpl.
 */
@Service
public class IssueBacklogCustomHistoryRepositoryImpl implements IssueBacklogCustomHistoryQueryRepository {

	private static final String STATUS = "statusUpdationLog.changedTo";
	private static final String START_TIME = "T00:00:00.000Z";
	private static final String END_TIME = "T23:59:59.000Z";
	/** The operations. */
	@Autowired
	private MongoOperations operations;

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

	@SuppressWarnings("unchecked")
	@Override
	public List<IssueBacklogCustomHistory> findByFilterAndFromStatusMapWithDateFilter(Map<String, List<String>> mapOfFilters,
																					  Map<String, Map<String, Object>> uniqueProjectMap, String dateFrom, String dateTo) {
		Criteria criteria = new Criteria();

		DateTime startDate = new DateTime(new StringBuilder(dateFrom).append(START_TIME).toString(), DateTimeZone.UTC);
		DateTime endDate = new DateTime(new StringBuilder(dateTo).append(END_TIME).toString(), DateTimeZone.UTC);

		// map of common filters Project and Sprint
		for (Map.Entry<String, List<String>> entry : mapOfFilters.entrySet()) {
			if (CollectionUtils.isNotEmpty(entry.getValue())) {
				criteria = criteria.and(entry.getKey()).in(entry.getValue());
			}
		}

		criteria.and("createdDate").gte(startDate).lte(endDate);

		List<Criteria> projectCriteriaList = new ArrayList<>();
		uniqueProjectMap.forEach((project, filterMap) -> {
			Criteria projectCriteria = new Criteria();
			if (null != filterMap.get("statusUpdationLog.story.changedTo")) {
				projectCriteria.and(STATUS).in((List<Pattern>) filterMap.get("statusUpdationLog.story.changedTo"));
			}
			projectCriteriaList.add(projectCriteria);
		});

		uniqueProjectMap.forEach((project, filterMap) -> {
			Criteria projectCriteria = new Criteria();
			if (null != filterMap.get("storyType")) {
				projectCriteria.and("storyType").in((List<Pattern>) filterMap.get("storyType"));
			}
			projectCriteriaList.add(projectCriteria);
		});

		Criteria criteriaAggregatedAtProjectLevel = new Criteria()
				.andOperator(projectCriteriaList.toArray(new Criteria[0]));
		Criteria criteriaProjectLevelAdded = new Criteria().andOperator(criteria, criteriaAggregatedAtProjectLevel);
		Query query = new Query(criteriaProjectLevelAdded);
		return operations.find(query, IssueBacklogCustomHistory.class);
	}
}