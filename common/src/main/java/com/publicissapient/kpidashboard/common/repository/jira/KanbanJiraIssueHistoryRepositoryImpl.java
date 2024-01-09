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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.model.jira.IssueHistoryMappedData;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;

/**
 * This class provide implementation of KanbanFeatureHistoryRepoCustom
 * interface.
 * 
 * @author prijain3
 */
@Service
public class KanbanJiraIssueHistoryRepositoryImpl implements KanbanJiraIssueHistoryRepoCustom {

	private static final String TICKET_STATUS_FIELD = "historyDetails.status";
	private static final String TICKET_ACTIVITY_DATE = "historyDetails.activityDate";
	private static final String TICKET_PROJECT_ID_FIELD = "basicProjectConfigId";
	private static final String HISTORY_DETAILS = "historyDetails";
	private static final String STORY_ID = "storyID";
	private static final String STORY_TYPE = "storyType";
	private static final String TICKET_CREATED_DATE_FIELD = "createdDate";
	private static final String PRIORITY = "priority";
	private static final String ESTIMATE_TIME = "estimate";
	private static final String NIN = "nin";
	private static final String START_TIME = "T00:00:00.000Z";
	private static final String END_TIME = "T23:59:59.000Z";
	private static final String URL = "url";

	/** The operations. */
	@Autowired
	private MongoOperations operations;

	@SuppressWarnings("unchecked")
	@Override
	public List<KanbanIssueCustomHistory> findIssuesByStatusAndDate(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap, String dateFrom, String dateTo,
			String mapStatusCriteria) {
		List<AggregationOperation> list = new ArrayList<>();

		String startDate = new StringBuilder(dateFrom).append(START_TIME).toString();
		String endDate = new StringBuilder(dateTo).append(END_TIME).toString();
		Criteria criteria = new Criteria();

		// map of common filters
		for (Map.Entry<String, List<String>> entry : mapOfFilters.entrySet()) {
			if (CollectionUtils.isNotEmpty(entry.getValue())) {
				criteria = criteria.and(entry.getKey()).in(entry.getValue());
			}
		}

		list.add(Aggregation.match(criteria));
		list.add(Aggregation.unwind(HISTORY_DETAILS));

		// project level status filter
		if (MapUtils.isNotEmpty(uniqueProjectMap)) {
			List<Criteria> projectCriteriaList = new ArrayList<>();
			uniqueProjectMap.forEach((project, filterMap) -> {
				Criteria projectCriteria = new Criteria();
				projectCriteria.and(TICKET_PROJECT_ID_FIELD).is(project);
				filterMap.forEach((subk, subv) -> {
					if (subk.equals(TICKET_STATUS_FIELD) && mapStatusCriteria.equalsIgnoreCase(NIN)) {
						projectCriteria.and(subk).nin((List<Pattern>) subv);
					} else {
						projectCriteria.and(subk).in((List<Pattern>) subv);
					}
				});
				projectCriteria.and(TICKET_ACTIVITY_DATE).gte(startDate).lte(endDate);
				projectCriteriaList.add(projectCriteria);

			});

			Criteria criteriaAggregatedAtProjectLevelForStatus = new Criteria()
					.orOperator(projectCriteriaList.toArray(new Criteria[0]));

			list.add(Aggregation.match(criteriaAggregatedAtProjectLevelForStatus));
		}
		list.add(Aggregation.group(STORY_ID, STORY_TYPE, TICKET_PROJECT_ID_FIELD, TICKET_CREATED_DATE_FIELD, PRIORITY,
				ESTIMATE_TIME, URL).push(HISTORY_DETAILS).as(HISTORY_DETAILS));
		list.add(Aggregation.project(HISTORY_DETAILS));
		TypedAggregation<KanbanIssueCustomHistory> agg = Aggregation.newAggregation(KanbanIssueCustomHistory.class,
				list);

		List<IssueHistoryMappedData> data = operations
				.aggregate(agg, KanbanIssueCustomHistory.class, IssueHistoryMappedData.class).getMappedResults();

		List<KanbanIssueCustomHistory> resultList = new ArrayList<>();

		data.stream().forEach(result -> {
			KanbanIssueCustomHistory history = new KanbanIssueCustomHistory();
			history.setStoryID(result.getId().getStoryID());
			history.setStoryType(result.getId().getStoryType());
			history.setProjectComponentId(result.getId().getProjectComponentId());
			history.setBasicProjectConfigId(result.getId().getBasicProjectConfigId());
			history.setCreatedDate(result.getId().getCreatedDate());
			history.setPriority(result.getId().getPriority());
			history.setEstimate(result.getId().getEstimate());
			history.setHistoryDetails(result.getHistoryDetails());
			history.setUrl(result.getId().getUrl());
			resultList.add(history);
		});
		return resultList;

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<KanbanIssueCustomHistory> findIssuesByCreatedDateAndType(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap, String dateFrom, String dateTo) {

		String startDate = new StringBuilder(dateFrom).append(START_TIME).toString();
		String endDate = new StringBuilder(dateTo).append(END_TIME).toString();
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
			projectCriteria.and(TICKET_PROJECT_ID_FIELD).is(project);
			filterMap.forEach((subk, subv) -> projectCriteria.and(subk).in((List<Pattern>) subv));
			projectCriteriaList.add(projectCriteria);
		});

		Criteria criteriaAggregatedAtProjectLevel = new Criteria()
				.orOperator(projectCriteriaList.toArray(new Criteria[0]));
		Criteria criteriaProjectLevelAdded = new Criteria().andOperator(criteria, criteriaAggregatedAtProjectLevel);
		Query query = new Query(criteriaProjectLevelAdded);
		return operations.find(query, KanbanIssueCustomHistory.class);

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<KanbanIssueCustomHistory> findIssuesInWipByDate(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap, Map<String, Map<String, Object>> uniqueWipProjectMap,
			String dateFrom, String dateTo) {
		List<AggregationOperation> list = new ArrayList<>();

		String startDate = new StringBuilder(dateFrom).append(START_TIME).toString();
		String endDate = new StringBuilder(dateTo).append(END_TIME).toString();
		Criteria criteria = new Criteria();

		// map of common filters
		for (Map.Entry<String, List<String>> entry : mapOfFilters.entrySet()) {
			if (CollectionUtils.isNotEmpty(entry.getValue())) {
				criteria = criteria.and(entry.getKey()).in(entry.getValue());
			}
		}

		list.add(Aggregation.match(criteria));
		// project level status filter
		List<Criteria> projectCriteriaList = new ArrayList<>();
		uniqueWipProjectMap.forEach((project, filterMap) -> {
			Criteria projectCriteria = new Criteria();
			Criteria closedIssueCriteria = new Criteria();
			Criteria historyCriteria = new Criteria();
			Map<String, Object> closedIssueMap = uniqueProjectMap.get(project);
			projectCriteria.and(TICKET_PROJECT_ID_FIELD).is(project);
			filterMap.forEach((subk, subv) -> {
				if (subk.equals(TICKET_STATUS_FIELD)) {
					historyCriteria.andOperator(Criteria.where(subk).in((List<Pattern>) subv),
							Criteria.where(subk).nin((List<Pattern>) closedIssueMap.get(TICKET_STATUS_FIELD)));
				}
			});
			historyCriteria.and(TICKET_ACTIVITY_DATE).lte(endDate);

			closedIssueMap.forEach((subk, subv) -> {
				if (subk.equals(TICKET_STATUS_FIELD)) {
					closedIssueCriteria.and(subk).in((List<Pattern>) subv);
				} else {
					projectCriteria.and(subk).in((List<Pattern>) subv);
				}
			});
			closedIssueCriteria.and(TICKET_ACTIVITY_DATE).gt(startDate);
			projectCriteria.orOperator(historyCriteria, closedIssueCriteria);
			projectCriteriaList.add(projectCriteria);

		});
		Criteria criteriaAggregatedAtProjectLevelForStatus = new Criteria()
				.orOperator(projectCriteriaList.toArray(new Criteria[0]));

		list.add(Aggregation.match(criteriaAggregatedAtProjectLevelForStatus));
		TypedAggregation<KanbanIssueCustomHistory> agg = Aggregation.newAggregation(KanbanIssueCustomHistory.class,
				list);
		return operations.aggregate(agg, KanbanIssueCustomHistory.class, KanbanIssueCustomHistory.class)
				.getMappedResults();
	}

}
