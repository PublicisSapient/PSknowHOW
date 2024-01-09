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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.model.jira.IssueHistoryMappedData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;

/**
 * The Class FeatureCustomHistoryRepositoryImpl.
 */
@Service
public class JiraIssueCustomHistoryRepositoryImpl implements JiraIssueHistoryCustomQueryRepository {

	private static final String STATUS_CHANGE_LOG = "statusUpdationLog";
	private static final String VERSION_CHANGE_LOG = "fixVersionUpdationLog";
	private static final String UPDATED_ON = "statusUpdationLog.updatedOn";
	private static final String STORY_ID = "storyID";
	private static final String STORY_TYPE = "storyType";
	private static final String TICKET_CREATED_DATE_FIELD = "createdDate";
	private static final String STATUS = "statusUpdationLog.changedTo";
	private static final String START_TIME = "T00:00:00.000Z";
	private static final String END_TIME = "T23:59:59.000Z";
	private static final String BASIC_PROJ_CONF_ID = "basicProjectConfigId";
	private static final String FIXVERSION_CHANGEDTO = "fixVersionUpdationLog.changedTo";
	private static final String FIXVERSION_CHANGEDFROM = "fixVersionUpdationLog.changedFrom";
	public static final String STATUS_UPDATION_LOG_STORY_CHANGED_TO = "statusUpdationLog.story.changedTo";
	public static final String URL = "url";
	public static final String DESCRIPTION = "description";
	public static final String ESTIMATE = "estimate";
	/** The operations. */
	@Autowired
	private MongoOperations operations;

	/**
	 * To iso 8601 utc string.
	 *
	 * @param date
	 *            the date
	 * @return the string
	 */
	public static String toISO8601UTC(Date date) {
		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US);
		df.setTimeZone(tz);
		return df.format(date);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<JiraIssueCustomHistory> findFeatureCustomHistoryStoryProjectWise(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap , Sort.Direction sortMethod) {

		List<AggregationOperation> list = new ArrayList<>();
		Criteria criteria = new Criteria();
		// map of common filters Project and Sprint
		for (Map.Entry<String, List<String>> entry : mapOfFilters.entrySet()) {
			if (CollectionUtils.isNotEmpty(entry.getValue())) {
				criteria = criteria.and(entry.getKey()).in(entry.getValue());
			}
		}

		// Project level storyType filters
		List<Criteria> projectCriteriaList = new ArrayList<>();
		uniqueProjectMap.forEach((project, filterMap) -> {
			Criteria projectCriteria = new Criteria();
			projectCriteria.and(BASIC_PROJ_CONF_ID).is(project);
			projectCriteria.and(STORY_TYPE).in((List<Pattern>) filterMap.get(STORY_TYPE));
			projectCriteriaList.add(projectCriteria);
		});

		Criteria criteriaAggregatedAtProjectLevel = new Criteria()
				.orOperator(projectCriteriaList.toArray(new Criteria[0]));
		Criteria criteriaAggregatedForFirstMatchStage = new Criteria().andOperator(criteria,
				criteriaAggregatedAtProjectLevel);

		list.add(Aggregation.match(criteriaAggregatedForFirstMatchStage));
		list.add(Aggregation.unwind(STATUS_CHANGE_LOG));

		// project level status filter
		List<Criteria> storyStatuscriteriaList = new ArrayList<>();
		uniqueProjectMap.forEach((project, filterMap) -> {
			Criteria projectCriteria = new Criteria();
			projectCriteria.and(BASIC_PROJ_CONF_ID).is(project);
			projectCriteria.and(STATUS).in((List<Pattern>) filterMap.get(STATUS_UPDATION_LOG_STORY_CHANGED_TO));
			storyStatuscriteriaList.add(projectCriteria);
		});

		Criteria criteriaAggregatedAtProjectLevelForStatus = new Criteria()
				.orOperator(storyStatuscriteriaList.toArray(new Criteria[0]));
		list.add(Aggregation.match(criteriaAggregatedAtProjectLevelForStatus));

		list.add(Aggregation.sort(sortMethod, UPDATED_ON));
		list.add(Aggregation.group(STORY_ID, BASIC_PROJ_CONF_ID, URL).push(STATUS_CHANGE_LOG).as(STATUS_CHANGE_LOG));
		list.add(Aggregation.project(STATUS_CHANGE_LOG));
		TypedAggregation<JiraIssueCustomHistory> agg = Aggregation.newAggregation(JiraIssueCustomHistory.class, list);

		List<IssueHistoryMappedData> data = operations
				.aggregate(agg, JiraIssueCustomHistory.class, IssueHistoryMappedData.class).getMappedResults();

		List<JiraIssueCustomHistory> resultList = new ArrayList<>();

		data.stream().forEach(result -> {
			JiraIssueCustomHistory history = new JiraIssueCustomHistory();
			history.setStoryID(result.getId().getStoryID());
			history.setBasicProjectConfigId(result.getId().getBasicProjectConfigId());
			history.setStatusUpdationLog(result.getStatusUpdationLog());
			history.setUrl(result.getId().getUrl());
			resultList.add(history);
		});
		return resultList;

	}

	/**
	 * find jira issue based on filter and date
	 *
	 * @param mapOfFilters
	 * @param uniqueProjectMap
	 * @param dateFrom
	 * @param dateTo
	 * @return List<JiraIssueCustomHistory>
	 */
	@Override
	public List<JiraIssueCustomHistory> findIssuesByCreatedDateAndType(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap, String dateFrom, String dateTo) {

		DateTime startDate = new DateTime(new StringBuilder(dateFrom).append(START_TIME).toString(), DateTimeZone.UTC);
		DateTime endDate = new DateTime(new StringBuilder(dateTo).append(END_TIME).toString(), DateTimeZone.UTC);
		Criteria criteria = new Criteria();

		// map of common filters Project and Sprint
		for (Map.Entry<String, List<String>> entry : mapOfFilters.entrySet()) {
			if (CollectionUtils.isNotEmpty(entry.getValue())) {
				criteria = criteria.and(entry.getKey()).in(entry.getValue());
			}
		}
		criteria.and(TICKET_CREATED_DATE_FIELD).gte(startDate).lte(endDate);
		// Project level storyType filters
		List<Criteria> projectCriteriaList = new ArrayList<>();
		uniqueProjectMap.forEach((project, filterMap) -> {
			Criteria projectCriteria = new Criteria();
			projectCriteria.and(BASIC_PROJ_CONF_ID).is(project);
			filterMap.forEach((subk, subv) -> projectCriteria.and(subk).in((List<Pattern>) subv));
			projectCriteriaList.add(projectCriteria);
		});

		Criteria criteriaAggregatedAtProjectLevel = new Criteria()
				.orOperator(projectCriteriaList.toArray(new Criteria[0]));
		Criteria criteriaProjectLevelAdded = new Criteria().andOperator(criteria, criteriaAggregatedAtProjectLevel);
		Query query = new Query(criteriaProjectLevelAdded);
		query.fields().include(STORY_ID);
		query.fields().include(STORY_TYPE);
		query.fields().include(BASIC_PROJ_CONF_ID);
		query.fields().include(STATUS_CHANGE_LOG);
		query.fields().include(TICKET_CREATED_DATE_FIELD);
		query.fields().include(URL);
		query.fields().include(DESCRIPTION);
		return operations.find(query, JiraIssueCustomHistory.class);
	}


	@Override
	public List<JiraIssueCustomHistory> findByFilterAndFromStatusMap(Map<String, List<String>> mapOfFilters,
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
			projectCriteria.and(STATUS).in((List<Pattern>) filterMap.get(STATUS_UPDATION_LOG_STORY_CHANGED_TO));
			if (null != filterMap.get(STORY_TYPE)) {
				projectCriteria.and(STORY_TYPE).in((List<Pattern>) filterMap.get(STORY_TYPE));
			}
			projectCriteriaList.add(projectCriteria);
		});
		Query query;
		if (CollectionUtils.isEmpty(projectCriteriaList)) {
			query = new Query(criteria);
		} else {
			Criteria criteriaAggregatedAtProjectLevel = new Criteria()
					.andOperator(projectCriteriaList.toArray(new Criteria[0]));
			Criteria criteriaProjectLevelAdded = new Criteria().andOperator(criteria, criteriaAggregatedAtProjectLevel);
			query = new Query(criteriaProjectLevelAdded);
		}
		query.fields().include(STORY_ID);
		query.fields().include(BASIC_PROJ_CONF_ID);
		query.fields().include(STATUS_CHANGE_LOG);
		//projectID,storyID,basicProjectConfigId,statusUpdationLog
		return operations.find(query, JiraIssueCustomHistory.class);

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<JiraIssueCustomHistory> findByFilterAndFromReleaseMap(List<String> basicProjectConfigId,
			List<Pattern> releaseList) {
		Criteria criteria = new Criteria();
		criteria = criteria.and(BASIC_PROJ_CONF_ID).in(basicProjectConfigId);
		List<Criteria> projectCriteriaList = new ArrayList<>();
		Criteria projectCriteria1 = new Criteria();
		Criteria projectCriteria2 = new Criteria();
		projectCriteria1.and(FIXVERSION_CHANGEDTO).in(releaseList);
		projectCriteria2.and(FIXVERSION_CHANGEDFROM).in(releaseList);
		projectCriteriaList.add(projectCriteria1);
		projectCriteriaList.add(projectCriteria2);

		Criteria criteriaAggregatedAtProjectLevel = new Criteria()
				.orOperator(projectCriteriaList.toArray(new Criteria[0]));
		Criteria criteriaProjectLevelAdded = new Criteria().andOperator(criteria, criteriaAggregatedAtProjectLevel);
		Query query = new Query(criteriaProjectLevelAdded);
		query.fields().include(STORY_ID);
		query.fields().include(BASIC_PROJ_CONF_ID);
		query.fields().include(STATUS_CHANGE_LOG);
		query.fields().include(VERSION_CHANGE_LOG);
		return operations.find(query, JiraIssueCustomHistory.class);

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<JiraIssueCustomHistory> findByFilterAndFromStatusMapWithDateFilter(
			Map<String, List<String>> mapOfFilters, Map<String, Map<String, Object>> uniqueProjectMap, String dateFrom,
			String dateTo) {
		Criteria criteria = new Criteria();

		DateTime startDate = new DateTime(new StringBuilder(dateFrom).append(START_TIME).toString(), DateTimeZone.UTC);
		DateTime endDate = new DateTime(new StringBuilder(dateTo).append(END_TIME).toString(), DateTimeZone.UTC);

		// map of common filters Project and Sprint
		for (Map.Entry<String, List<String>> entry : mapOfFilters.entrySet()) {
			if (CollectionUtils.isNotEmpty(entry.getValue())) {
				criteria = criteria.and(entry.getKey()).in(entry.getValue());
			}
		}

		// project level status filter
		List<Criteria> projectCriteriaList = new ArrayList<>();
		uniqueProjectMap.forEach((project, filterMap) -> {
			Criteria projectCriteria = new Criteria();
			if (null != filterMap.get(STATUS_UPDATION_LOG_STORY_CHANGED_TO)) {
				projectCriteria.and(STATUS).in((List<Pattern>) filterMap.get(STATUS_UPDATION_LOG_STORY_CHANGED_TO));
				projectCriteria.and(UPDATED_ON).gte(startDate).lte(endDate);
			}
			projectCriteriaList.add(projectCriteria);
		});

		uniqueProjectMap.forEach((project, filterMap) -> {
			Criteria projectCriteria = new Criteria();
			if (null != filterMap.get(STORY_TYPE)) {
				projectCriteria.and(STORY_TYPE).in((List<Pattern>) filterMap.get(STORY_TYPE));
			}
			projectCriteriaList.add(projectCriteria);
		});

		Criteria criteriaAggregatedAtProjectLevel = new Criteria()
				.andOperator(projectCriteriaList.toArray(new Criteria[0]));
		Criteria criteriaProjectLevelAdded = new Criteria().andOperator(criteria, criteriaAggregatedAtProjectLevel);
		Query query = new Query(criteriaProjectLevelAdded);
		query.fields().include(STORY_ID);
		query.fields().include(STORY_TYPE);
		query.fields().include(BASIC_PROJ_CONF_ID);
		query.fields().include(STATUS_CHANGE_LOG);
		query.fields().include(TICKET_CREATED_DATE_FIELD);
		query.fields().include(URL);
		query.fields().include(DESCRIPTION);
		query.fields().include(ESTIMATE);
		return operations.find(query, JiraIssueCustomHistory.class);
	}

}