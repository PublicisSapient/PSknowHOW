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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;

/**
 * Kanban Feature repository class provides implementation of
 * KanbanFeatureRepoCustom.
 */
@Service
public class KanbanJiraIssueRepositoryImpl implements KanbanJiraIssueRepoCustom {

	private static final String TICKET_PROJECT_ID_FIELD = "basicProjectConfigId";
	private static final String TICKET_CREATED_DATE_FIELD = "createdDate";
	private static final String RANGE = "range";
	private static final String LESS = "less";
	private static final String PAST = "past";
	private static final String START_TIME = "T00:00:00.0000000";
	private static final String END_TIME = "T23:59:59.0000000";
	private static final String COST_OF_DELAY = "costOfDelay";
	private static final String TYPE_NAME = "typeName";
	private static final String JIRA_ISSUE_STATUS = "jiraStatus";
	private static final String NIN = "nin";
	private static final String NUMBER = "number";
	private static final String NAME = "name";
	private static final String URL = "url";
	private static final String CREATED_DATE = "createdDate";
	@Autowired
	private MongoOperations operations;

	@Override
	public List<KanbanJiraIssue> findIssuesByType(Map<String, List<String>> mapOfFilters, String dateFrom,
			String dateTo) {
		String startDate = new StringBuilder(dateFrom).append(START_TIME).toString();
		String endDate = new StringBuilder(dateTo).append(END_TIME).toString();
		Criteria criteria = new Criteria();

		// map of common filters Project and Sprint
		for (Map.Entry<String, List<String>> entry : mapOfFilters.entrySet()) {
			if (CollectionUtils.isNotEmpty(entry.getValue())) {
				criteria = criteria.and(entry.getKey()).in(entry.getValue());
			}
		}
		criteria = criteria.and(TICKET_CREATED_DATE_FIELD).gte(startDate).lte(endDate);
		Criteria criteriaProjectLevelAdded = new Criteria().andOperator(criteria);

		Query query = new Query(criteriaProjectLevelAdded);
		return operations.find(query, KanbanJiraIssue.class);

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<KanbanJiraIssue> findIssuesByDateAndType(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap, String dateFrom, String dateTo, String dateCriteria) {

		String startDate = new StringBuilder(dateFrom).append(START_TIME).toString();
		String endDate = new StringBuilder(dateTo).append(END_TIME).toString();

		Criteria criteria = new Criteria();

		// map of common filters Project and Sprint
		for (Map.Entry<String, List<String>> entry : mapOfFilters.entrySet()) {
			if (CollectionUtils.isNotEmpty(entry.getValue())) {
				criteria = criteria.and(entry.getKey()).in(entry.getValue());
			}
		}
		if (dateCriteria.equals(RANGE)) {
			criteria = criteria.and(TICKET_CREATED_DATE_FIELD).gte(startDate).lte(endDate);
		} else if (dateCriteria.equals(LESS)) {
			criteria = criteria.and(TICKET_CREATED_DATE_FIELD).lt(startDate);
		} else if (dateCriteria.equals(PAST)) {
			criteria = criteria.and(TICKET_CREATED_DATE_FIELD).lt(endDate);
		}
		// Project level storyType filters
		List<Criteria> projectCriteriaList = new ArrayList<>();
		uniqueProjectMap.forEach((project, filterMap) -> {
			Criteria projectCriteria = new Criteria();
			projectCriteria.and(TICKET_PROJECT_ID_FIELD).is(project);
			filterMap.forEach((subk, subv) -> projectCriteria.and(subk).in((List<Pattern>) subv));
			projectCriteriaList.add(projectCriteria);

		});

		Query query = new Query(criteria);
		if (!CollectionUtils.isEmpty(projectCriteriaList)) {
			Criteria criteriaAggregatedAtProjectLevel = new Criteria()
					.orOperator(projectCriteriaList.toArray(new Criteria[0]));
			Criteria criteriaProjectLevelAdded = new Criteria().andOperator(criteria, criteriaAggregatedAtProjectLevel);
			query = new Query(criteriaProjectLevelAdded);
		}
		return operations.find(query, KanbanJiraIssue.class);

	}

	@Override
	public List<KanbanJiraIssue> findIssuesByDateAndTypeAndStatus(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap, String dateFrom, String dateTo, String dateCriteria,
			String mapStatusCriteria) {

		String startDate = new StringBuilder(dateFrom).append(START_TIME).toString();
		String endDate = new StringBuilder(dateTo).append(END_TIME).toString();

		Criteria criteria = new Criteria();

		// map of common filters Project and Sprint
		for (Map.Entry<String, List<String>> entry : mapOfFilters.entrySet()) {
			if (CollectionUtils.isNotEmpty(entry.getValue())) {
				criteria = criteria.and(entry.getKey()).in(entry.getValue());
			}
		}
		if (dateCriteria.equals(RANGE)) {
			criteria = criteria.and(TICKET_CREATED_DATE_FIELD).gte(startDate).lte(endDate);
		} else if (dateCriteria.equals(LESS)) {
			criteria = criteria.and(TICKET_CREATED_DATE_FIELD).lt(startDate);
		} else if (dateCriteria.equals(PAST)) {
			criteria = criteria.and(TICKET_CREATED_DATE_FIELD).lt(endDate);
		}
		// Project level storyType filters
		List<Criteria> projectCriteriaList = new ArrayList<>();
		uniqueProjectMap.forEach((project, filterMap) -> {
			Criteria projectCriteria = new Criteria();
			projectCriteria.and(TICKET_PROJECT_ID_FIELD).is(project);
			filterMap.forEach((subk, subv) -> {
				if (subk.equals(JIRA_ISSUE_STATUS) && mapStatusCriteria.equalsIgnoreCase(NIN)) {
					projectCriteria.and(subk).nin((List<Pattern>) subv);
				} else {
					projectCriteria.and(subk).in((List<Pattern>) subv);
				}
			});
			projectCriteriaList.add(projectCriteria);
		});

		Query query = new Query(criteria);
		if (!CollectionUtils.isEmpty(projectCriteriaList)) {
			Criteria criteriaAggregatedAtProjectLevel = new Criteria()
					.orOperator(projectCriteriaList.toArray(new Criteria[0]));
			Criteria criteriaProjectLevelAdded = new Criteria().andOperator(criteria, criteriaAggregatedAtProjectLevel);
			query = new Query(criteriaProjectLevelAdded);
		}
		return operations.find(query, KanbanJiraIssue.class);

	}

	public List<KanbanJiraIssue> findCostOfDelayByType(Map<String, List<String>> mapOfFilters) {

		Criteria criteria = new Criteria();

		// map of common filters Project and Sprint
		for (Map.Entry<String, List<String>> entry : mapOfFilters.entrySet()) {
			if (CollectionUtils.isNotEmpty(entry.getValue())) {
				criteria = criteria.and(entry.getKey()).in(entry.getValue());
			}
		}

		Query query = new Query(criteria);
		return operations.find(query, KanbanJiraIssue.class);

	}

	@Override
	public void updateByBasicProjectConfigId(String basicProjectConfigId, List<String> fieldsToUnset) {
		Criteria criteria = new Criteria();
		criteria.and(TICKET_PROJECT_ID_FIELD).is(basicProjectConfigId);
		Query query = new Query(criteria);

		if (CollectionUtils.isNotEmpty(fieldsToUnset)) {
			Update update = new Update();
			fieldsToUnset.stream().forEach(field -> update.unset(field));

			operations.updateMulti(query, update, KanbanJiraIssue.class);
		}

	}

}