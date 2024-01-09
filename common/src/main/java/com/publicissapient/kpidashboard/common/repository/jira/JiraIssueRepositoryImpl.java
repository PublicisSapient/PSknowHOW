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

package com.publicissapient.kpidashboard.common.repository.jira;//NOPMD

//Do not remove NOPMD comment. This is for ignoring ExcessivePublicCount violation

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.ReleaseWisePI;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;

/**
 * Repository for {@link JiraIssue} with custom methods implementation.
 */
@Service
public class JiraIssueRepositoryImpl implements JiraIssueRepositoryCustom {// NOPMD
	// to avoid tooManyMethods

	private static final String PROJECT_ID = "projectID";
	private static final String SPRINT_ID = "sprintID";
	private static final String SPRINT = "sprint";
	private static final String NUMBER = "number";
	private static final String NAME = "name";
	private static final String URL = "url";
	private static final String STORY_LIST = "storyList";
	private static final String SPRINT_BEGIN_DATE = "sprintBeginDate";
	private static final String SPRINT_NAME = "sprintName";
	private static final String STORY_POINTS = "storyPoints";
	private static final String EFFORT_SUM = "effortSum";
	private static final String ROOT_CAUSE = "rootCauseList";
	private static final String UNCHECKED = "unchecked";
	private static final String DEFECT_STORY_ID = "defectStoryID";
	private static final String CONFIG_ID = "basicProjectConfigId";
	private static final String TYPE_NAME = "typeName";
	private static final String TICKET_CREATED_DATE_FIELD = "createdDate";
	private static final String PRODUCTION_DEFECT = "productionDefect";
	private static final String RANGE = "range";
	private static final String LESS = "less";
	private static final String PAST = "past";
	private static final String START_TIME = "T00:00:00.0000000";
	private static final String END_TIME = "T23:59:59.0000000";
	private static final String JIRA_ISSUE_STATUS = "jiraStatus";
	private static final String NIN = "nin";
	private static final String JIRA_UPDATED_DATE = "updateDate";
	private static final String RELEASE_VERSION = "releaseVersions.releaseName";
	private static final String STATUS = "status";
	private static final String RESOLUTION = "resolution";
	private static final String PRIORITY = "priority";
	private static final String PROJECT_NAME = "projectName";
	private static final String DEFECT_RAISED_BY_QA = "defectRaisedByQA";
	private static final String DEFECT_RAISED_BY = "defectRaisedBy";
	private static final String ORIGINAL_ESTIMATE_MINUTES = "originalEstimateMinutes";
	private static final String ESTIMATE = "estimate";
	private static final String AGGREGATE_TIME_REMAINING_ESTIMATE_MINUTES = "aggregateTimeRemainingEstimateMinutes";
	private static final String AGGREGATE_TIME_ORIGINAL_ESTIMATE_MINUTES = "aggregateTimeOriginalEstimateMinutes";
	private static final String LOGGED_WORK_MINUTES = "timeSpentInMinutes";
	private static final String SPRINT_ASSET_STATE = "sprintAssetState";
	private static final String FUTURE = "FUTURE";
	private static final String CLOSED = "CLOSED";
	private static final String COUNT = "count";
	private static final String STATE = "state";
	private static final String ISSUE_ID = "issueId";
	private static final String SPRINT_END_DATE = "sprintEndDate";

	@Autowired
	private MongoTemplate operations;

	@SuppressWarnings(UNCHECKED)
	@Override
	public List<SprintWiseStory> findIssuesGroupBySprint(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap, String filterToShowOnTrend, String individualDevOrQa) {

		Criteria criteria = new Criteria();

		// map of common filters Project and Sprint
		criteria = getCommonFiltersCriteria(mapOfFilters, criteria);
		// Project level storyType filters
		List<Criteria> projectCriteriaList = new ArrayList<>();
		uniqueProjectMap.forEach((project, filterMap) -> {
			Criteria projectCriteria = new Criteria();
			projectCriteria.and(CONFIG_ID).is(project);
			filterMap.forEach((subk, subv) -> projectCriteria.and(subk).in((List<Pattern>) subv));
			projectCriteriaList.add(projectCriteria);

		});
		Criteria criteriaAggregatedAtProjectLevel = new Criteria()
				.orOperator(projectCriteriaList.toArray(new Criteria[0]));
		Criteria criteriaProjectLevelAdded = new Criteria().andOperator(criteria, criteriaAggregatedAtProjectLevel);
		MatchOperation matchStage = Aggregation.match(criteriaProjectLevelAdded);

		GroupOperation groupBySprint = Aggregation.group(SPRINT_ID).last(SPRINT_ID).as(SPRINT).last(SPRINT_NAME)
				.as(SPRINT_NAME).last(CONFIG_ID).as(CONFIG_ID).addToSet(NUMBER).as(STORY_LIST);

		Aggregation aggregation = Aggregation.newAggregation(matchStage, groupBySprint);
		return operations.aggregate(aggregation, JiraIssue.class, SprintWiseStory.class).getMappedResults();
	}

	@SuppressWarnings(UNCHECKED)
	@Override
	public List<JiraIssue> findIssueByStoryNumber(Map<String, List<String>> mapOfFilters, List<String> storyNumber,
			Map<String, Map<String, Object>> uniqueProjectMapFolder) {
		Criteria criteria = new Criteria();

		// map of common filters Project and Sprint
		criteria = getCommonFiltersCriteria(mapOfFilters, criteria);

		// Query created to search for test cases which starts with folder name
		// as
		// mentioned in admin config screen
		if (MapUtils.isNotEmpty(uniqueProjectMapFolder)) {
			List<Criteria> hierarchyCriteriaList = new ArrayList<>();
			uniqueProjectMapFolder.forEach((project, filterMap) -> {
				Criteria projectCriteria = new Criteria();
				projectCriteria.and(CONFIG_ID).is(project);
				filterMap.forEach((subk, subv) -> projectCriteria.and(subk).in((List<Pattern>) subv));
				hierarchyCriteriaList.add(projectCriteria);

			});
			Criteria criteriaAggregatedAtProjectLevel = new Criteria()
					.andOperator(hierarchyCriteriaList.toArray(new Criteria[0]));
			criteria = new Criteria().andOperator(criteria, criteriaAggregatedAtProjectLevel);

		}
		criteria = criteria.and(DEFECT_STORY_ID).in(storyNumber);

		Query query = new Query(criteria);
		query.fields().include(NUMBER);
		query.fields().include(DEFECT_STORY_ID);
		query.fields().include("testAutomated");
		query.fields().include("isTestAutomated");
		query.fields().include("defectRaisedBy");
		query.fields().include("status");
		query.fields().include(CONFIG_ID);
		query.fields().include("labels");
		query.fields().include("resolution");
		query.fields().include(NAME);
		query.fields().include(URL);
		return operations.find(query, JiraIssue.class);

	}

	@SuppressWarnings(UNCHECKED)
	@Override
	public List<JiraIssue> findIssuesBySprintAndType(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap) {
		Criteria criteria = new Criteria();

		// map of common filters Project and Sprint
		criteria = getCommonFiltersCriteria(mapOfFilters, criteria);
		// Project level storyType filters
		List<Criteria> projectCriteriaList = new ArrayList<>();
		uniqueProjectMap.forEach((project, filterMap) -> {
			Criteria projectCriteria = new Criteria();
			projectCriteria.and(CONFIG_ID).is(project);
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
		query.fields().include(CONFIG_ID);
		query.fields().include(NUMBER);
		query.fields().include(STATUS);
		query.fields().include(RESOLUTION);
		query.fields().include(PROJECT_NAME);
		query.fields().include(SPRINT_ID);
		query.fields().include(SPRINT_NAME);
		query.fields().include(STORY_POINTS);
		query.fields().include(JIRA_ISSUE_STATUS);
		query.fields().include(DEFECT_STORY_ID);
		query.fields().include(ORIGINAL_ESTIMATE_MINUTES);
		query.fields().include(ESTIMATE);
		query.fields().include(URL);
		query.fields().include(NAME);
		query.fields().include(TYPE_NAME);
		query.fields().include(PRIORITY);
		query.fields().include(AGGREGATE_TIME_REMAINING_ESTIMATE_MINUTES);
		query.fields().include(AGGREGATE_TIME_ORIGINAL_ESTIMATE_MINUTES);
		query.fields().include(LOGGED_WORK_MINUTES);
		query.fields().include(SPRINT_ASSET_STATE);
		query.fields().include(SPRINT_END_DATE);
		return operations.find(query, JiraIssue.class);

	}

	/**
	 * Find issues by sprint and type list.
	 *
	 * @param mapOfFilters
	 *            the map of filters
	 * @param uniqueProjectMap
	 *            the unique project map
	 * @param uniqueProjectMapNotIn
	 *            for not in query
	 * @return list of feature
	 */
	@SuppressWarnings(UNCHECKED)
	@Override
	public List<JiraIssue> findIssuesBySprintAndType(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap, Map<String, Map<String, Object>> uniqueProjectMapNotIn) {
		Criteria criteria = new Criteria();

		// map of common filters Project and Sprint
		criteria = getCommonFiltersCriteria(mapOfFilters, criteria);
		// Project level storyType filters
		List<Criteria> projectCriteriaList = new ArrayList<>();
		uniqueProjectMap.forEach((project, filterMap) -> {
			Criteria projectCriteria = new Criteria();
			projectCriteria.and(PROJECT_ID).is(project);
			filterMap.forEach((subk, subv) -> projectCriteria.and(subk).in((List<Pattern>) subv));
			projectCriteriaList.add(projectCriteria);
		});

		uniqueProjectMapNotIn.forEach((project, filterMap) -> {
			Criteria projectCriteria = new Criteria();
			projectCriteria.and(PROJECT_ID).is(project);
			filterMap.forEach((subk, subv) -> projectCriteria.and(subk).nin((List<Pattern>) subv));
			projectCriteriaList.add(projectCriteria);
		});

		Query query = new Query(criteria);
		if (!CollectionUtils.isEmpty(projectCriteriaList)) {
			Criteria criteriaAggregatedAtProjectLevel = new Criteria()
					.orOperator(projectCriteriaList.toArray(new Criteria[0]));
			Criteria criteriaProjectLevelAdded = new Criteria().andOperator(criteria, criteriaAggregatedAtProjectLevel);

			query = new Query(criteriaProjectLevelAdded);
		}

		return operations.find(query, JiraIssue.class);
	}

	@Override
	public List<JiraIssue> findIssuesByType(Map<String, List<String>> mapOfFilters) {
		Criteria criteria = new Criteria();

		// map of common filters Project and Sprint
		criteria = getCommonFiltersCriteria(mapOfFilters, criteria);

		Criteria criteriaProjectLevelAdded = new Criteria().andOperator(criteria);

		Query query = new Query(criteriaProjectLevelAdded);
		query.fields().include(CONFIG_ID);
		query.fields().include(NUMBER);
		query.fields().include(STATUS);
		query.fields().include(RESOLUTION);
		query.fields().include(PRIORITY);
		query.fields().include(ROOT_CAUSE);
		query.fields().include(DEFECT_STORY_ID);
		query.fields().include(STORY_POINTS);
		query.fields().include(DEFECT_RAISED_BY_QA);
		query.fields().include(DEFECT_RAISED_BY);
		query.fields().include(JIRA_ISSUE_STATUS);
		query.fields().include(URL);
		query.fields().include(NAME);
		return operations.find(query, JiraIssue.class);

	}

	@Override
	public List<JiraIssue> findUnassignedIssues(String startDate, String endDate,
			Map<String, List<String>> mapOfFilters) {
		Criteria criteria = new Criteria();
		Criteria orCriteria = new Criteria();
		List<Criteria> filter = new ArrayList<>();
		for (String val : mapOfFilters.keySet()) {
			Criteria expression = new Criteria();
			expression.and(val).is(mapOfFilters.get(val));
			filter.add(expression);
		}
		orCriteria.orOperator(filter.toArray(filter.toArray(new Criteria[filter.size()])));
		criteria.and(JIRA_UPDATED_DATE).gte(startDate).lte(endDate);
		criteria.orOperator(Criteria.where(SPRINT_ASSET_STATE).in("", null, FUTURE, FUTURE.toLowerCase(), CLOSED,
				CLOSED.toLowerCase()), orCriteria);
		Query query = new Query(criteria);
		query.fields().include(SPRINT_ASSET_STATE);
		query.fields().include(NUMBER);
		query.fields().include(STATUS);
		query.fields().include(PROJECT_ID);
		query.fields().include(TICKET_CREATED_DATE_FIELD);
		query.fields().include(NAME);
		query.fields().include(PRIORITY);
		return operations.find(query, JiraIssue.class);
	}

	@Override
	public List<SprintWiseStory> findStoriesByType(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap, String filterToShowOnTrend, String individualDevOrQa) {

		Criteria criteria = new Criteria();

		// map of common filters Project and Sprint
		criteria = getCommonFiltersCriteria(mapOfFilters, criteria);
		// Project level storyType filters
		List<Criteria> projectCriteriaList = projectLevelStoryTypeFilters(uniqueProjectMap);

		Criteria criteriaAggregatedAtProjectLevel = new Criteria()
				.orOperator(projectCriteriaList.toArray(new Criteria[0]));
		Criteria criteriaProjectLevelAdded = new Criteria().andOperator(criteria, criteriaAggregatedAtProjectLevel);
		MatchOperation matchStage = Aggregation.match(criteriaProjectLevelAdded);

		GroupOperation groupBySprintFilter = Aggregation.group(CONFIG_ID, SPRINT_ID);
		GroupOperation groupBySprint = groupBySprintFilter.last(CONFIG_ID).as(CONFIG_ID).last(SPRINT_ID).as(SPRINT)
				.last(SPRINT_NAME).as(SPRINT_NAME).addToSet(NUMBER).as(STORY_LIST);
		groupBySprint = groupBySprint.last(SPRINT_BEGIN_DATE).as(SPRINT_BEGIN_DATE);
		SortOperation sortByDate = Aggregation.sort(Sort.Direction.DESC, SPRINT_BEGIN_DATE);
		groupBySprint = groupBySprint.sum(STORY_POINTS).as(EFFORT_SUM);
		Aggregation aggregation = Aggregation.newAggregation(matchStage, groupBySprint, sortByDate);
		return operations.aggregate(aggregation, JiraIssue.class, SprintWiseStory.class).getMappedResults();
	}

	/**
	 * 
	 * @param mapOfFilters
	 * @param criteria
	 * @return
	 */
	private Criteria getCommonFiltersCriteria(Map<String, List<String>> mapOfFilters, Criteria criteria) {
		Criteria theCriteria = criteria;
		for (Map.Entry<String, List<String>> entry : mapOfFilters.entrySet()) {
			if (CollectionUtils.isNotEmpty(entry.getValue())) {
				theCriteria = theCriteria.and(entry.getKey()).in(entry.getValue());
			}
		}
		return theCriteria;
	}

	/**
	 * 
	 * @param uniqueProjectMap
	 * @return
	 */
	@SuppressWarnings(UNCHECKED)
	private List<Criteria> projectLevelStoryTypeFilters(Map<String, Map<String, Object>> uniqueProjectMap) {
		List<Criteria> projectCriteriaList = new ArrayList<>();
		uniqueProjectMap.forEach((project, filterMap) -> {
			Criteria projectCriteria = new Criteria();
			projectCriteria.and(CONFIG_ID).is(project);
			filterMap.forEach((subk, subv) -> {
				if (SPRINT_BEGIN_DATE.equalsIgnoreCase(subk)) {
					projectCriteria.and(subk).lte(subv);
				} else {
					projectCriteria.and(subk).in((List<Pattern>) subv);
				}
			});
			projectCriteriaList.add(projectCriteria);

		});
		return projectCriteriaList;
	}

	@Override
	public List<JiraIssue> findDefectLinkedWithSprint(Map<String, List<String>> mapOfFilters) {
		Criteria criteria = new Criteria();

		// map of common filters Project and Sprint
		criteria = getCommonFiltersCriteria(mapOfFilters, criteria);

		Query query = new Query(criteria);
		return operations.find(query, JiraIssue.class);

	}

	@Override
	public List<JiraIssue> findDefectCountByRCA(Map<String, List<String>> mapOfFilters) {

		Criteria criteria = new Criteria();
		for (Map.Entry<String, List<String>> entry : mapOfFilters.entrySet()) {
			if (null != entry.getValue() && !entry.getValue().isEmpty() && null != entry.getValue().get(0)) {
				criteria = criteria.and(entry.getKey()).in(entry.getValue());
			}
		}
		criteria = criteria.and(ROOT_CAUSE).exists(Boolean.TRUE);

		Query query = new Query(criteria);
		query.fields().include(CONFIG_ID);
		query.fields().include(STATUS);
		query.fields().include(RESOLUTION);
		query.fields().include(NUMBER);
		query.fields().include(DEFECT_STORY_ID);
		query.fields().include(ROOT_CAUSE);
		query.fields().include(URL);
		query.fields().include(NAME);

		return operations.find(query, JiraIssue.class);

	}

	/**
	 * Method to fetch true value of fieldName
	 * 
	 * @param mapOfFilters
	 *            mapOfFilters
	 * @param fieldName
	 *            fieldName
	 * @param flag
	 *            boolean flag
	 * @param dateFrom
	 *            dateFrom
	 * @param dateTo
	 *            dateTo
	 * @return List<JiraIssue>
	 */
	@Override
	public List<JiraIssue> findIssuesWithBoolean(Map<String, List<String>> mapOfFilters, String fieldName, boolean flag,
			String dateFrom, String dateTo) {

		String startDate = dateFrom + START_TIME;
		String endDate = dateTo + END_TIME;

		Criteria criteria = new Criteria();

		criteria = getCommonFiltersCriteria(mapOfFilters, criteria);
		criteria = criteria.and(TICKET_CREATED_DATE_FIELD).gte(startDate).lte(endDate);
		// Field to check for true
		criteria = criteria.and(fieldName).is(flag);

		Query query = new Query(criteria);
		query.fields().include(NUMBER);

		return operations.find(query, JiraIssue.class);

	}

	/**
	 * Find defects without story link.
	 *
	 * @param mapOfFilters
	 *            the map of filters
	 * @param uniqueProjectMapNotIn
	 *            for not in query
	 * @return list of feature
	 */
	@SuppressWarnings(UNCHECKED)
	@Override
	public List<JiraIssue> findDefectsWithoutStoryLink(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMapNotIn) {
		Criteria criteria = new Criteria();

		criteria = getCommonFiltersCriteria(mapOfFilters, criteria);
		// Project level storyType filters
		List<Criteria> projectCriteriaList = new ArrayList<>();

		uniqueProjectMapNotIn.forEach((project, filterMap) -> {
			Criteria projectCriteria = new Criteria();
			projectCriteria.and(CONFIG_ID).is(project);
			filterMap.forEach((subk, subv) -> projectCriteria.and(subk).nin((List<Pattern>) subv));
			projectCriteriaList.add(projectCriteria);
		});

		Criteria criteriaAggregatedAtProjectLevel = new Criteria()
				.andOperator(projectCriteriaList.toArray(new Criteria[0]));
		Criteria criteriaProjectLevelAdded = new Criteria().andOperator(criteria, criteriaAggregatedAtProjectLevel);

		Query query = new Query(criteriaProjectLevelAdded);

		return operations.find(query, JiraIssue.class);
	}

	@Override
	public List<JiraIssue> findByTypeNameAndDefectStoryIDIn(String typeName, List<String> defectStoryIds) {

		Criteria criteria = new Criteria();
		criteria.and(TYPE_NAME).is(typeName).and(DEFECT_STORY_ID).in(defectStoryIds);
		Query query = new Query(criteria);
		query.fields().include(CONFIG_ID);
		query.fields().include(STATUS);
		query.fields().include(RESOLUTION);
		query.fields().include(PROJECT_NAME);
		query.fields().include(PRIORITY);
		query.fields().include(ROOT_CAUSE);
		query.fields().include(NUMBER);
		query.fields().include(DEFECT_STORY_ID);
		query.fields().include(URL);
		query.fields().include(NAME);

		return operations.find(query, JiraIssue.class);
	}

	@Override
	public List<JiraIssue> findIssueByNumber(Map<String, List<String>> mapOfFilters, Set<String> storyNumber,
			Map<String, Map<String, Object>> uniqueProjectMap) {
		Criteria criteria = new Criteria();

		// map of common filters Project and Sprint
		criteria = getCommonFiltersCriteria(mapOfFilters, criteria);

		// Project level storyType filters
		List<Criteria> projectCriteriaList = new ArrayList<>();
		uniqueProjectMap.forEach((project, filterMap) -> {
			Criteria projectCriteria = new Criteria();
			projectCriteria.and(CONFIG_ID).is(project);
			filterMap.forEach((subk, subv) -> projectCriteria.and(subk).in((List<Pattern>) subv));
			projectCriteriaList.add(projectCriteria);
		});

		if (!CollectionUtils.isEmpty(projectCriteriaList)) {
			Criteria criteriaAggregatedAtProjectLevel = new Criteria()
					.orOperator(projectCriteriaList.toArray(new Criteria[0]));
			criteria = new Criteria().andOperator(criteria, criteriaAggregatedAtProjectLevel);

		}
		criteria = criteria.and(NUMBER).in(storyNumber);
		Query query = new Query(criteria);
		query.fields().include(ISSUE_ID);
		query.fields().include(NUMBER);
		query.fields().include(STORY_POINTS);
		query.fields().include(NAME);
		query.fields().include(STATE);
		query.fields().include(COUNT);
		query.fields().include(ESTIMATE);
		query.fields().include(STATUS);
		query.fields().include(CONFIG_ID);
		query.fields().include(TICKET_CREATED_DATE_FIELD);
		query.fields().include(SPRINT_NAME);
		query.fields().include(SPRINT_ID);
		query.fields().include(URL);
		query.fields().include(RESOLUTION);
		query.fields().include(JIRA_ISSUE_STATUS);
		query.fields().include(AGGREGATE_TIME_ORIGINAL_ESTIMATE_MINUTES);
		return operations.find(query, JiraIssue.class);

	}

	/**
	 * This method is used to find stories for a given list of sprints
	 * 
	 * @param mapOfFilters
	 *            filters
	 * @param storyNumber
	 *            story number
	 * @return list of Feature
	 */
	@SuppressWarnings("javadoc")
	@Override
	public List<JiraIssue> findStoriesBySprints(Map<String, List<String>> mapOfFilters, List<String> storyNumber) {
		Criteria criteria = new Criteria();

		// map of common filters Project and Sprint
		criteria = getCommonFiltersCriteria(mapOfFilters, criteria);

		criteria = criteria.and(NUMBER).in(storyNumber);

		Query query = new Query(criteria);
		query.fields().include(NUMBER);
		query.fields().include(STORY_POINTS);
		query.fields().include("name");
		query.fields().include(STATE);
		query.fields().include("status");
		query.fields().include(SPRINT_NAME);
		query.fields().include(SPRINT_ID);
		query.fields().include(URL);
		return operations.find(query, JiraIssue.class);

	}

	public List<JiraIssue> findCostOfDelayByType(Map<String, List<String>> mapOfFilters) {

		Criteria criteria = new Criteria();

		// map of common filters Project and Sprint
		for (Map.Entry<String, List<String>> entry : mapOfFilters.entrySet()) {
			if (CollectionUtils.isNotEmpty(entry.getValue())) {
				criteria = criteria.and(entry.getKey()).in(entry.getValue());
			}
		}

		Query query = new Query(criteria);
		return operations.find(query, JiraIssue.class);
	}

	@Override
	public void updateByBasicProjectConfigId(String basicProjectConfigId, List<String> fieldsToUnset) {
		Criteria criteria = new Criteria();
		criteria.and(CONFIG_ID).is(basicProjectConfigId);
		Query query = new Query(criteria);

		if (CollectionUtils.isNotEmpty(fieldsToUnset)) {
			Update update = new Update();
			fieldsToUnset.stream().forEach(field -> update.unset(field));

			operations.updateMulti(query, update, JiraIssue.class);
		}

	}

	@Override
	@SuppressWarnings(UNCHECKED)
	public List<JiraIssue> findNonRegressionTestCases(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap) {
		Criteria criteria = new Criteria();

		criteria = getCommonFiltersCriteria(mapOfFilters, criteria);
		// Project level storyType filters
		List<Criteria> projectCriteriaList = new ArrayList<>();
		uniqueProjectMap.forEach((project, filterMap) -> {
			Criteria projectCriteria = new Criteria();
			projectCriteria.and(PROJECT_ID).is(project);
			filterMap.forEach((subk, subv) -> {
				if (subk.equalsIgnoreCase("labels")) {
					projectCriteria.and(subk).nin((List<Pattern>) subv);
				} else {
					projectCriteria.and(subk).in((List<Pattern>) subv);
				}

			});
			projectCriteriaList.add(projectCriteria);
		});
		Criteria criteriaAggregatedAtProjectLevel = new Criteria()
				.andOperator(projectCriteriaList.toArray(new Criteria[0]));
		Criteria criteriaProjectLevelAdded = new Criteria().andOperator(criteria, criteriaAggregatedAtProjectLevel);
		Query query = new Query(criteriaProjectLevelAdded);

		return operations.find(query, JiraIssue.class);
	}

	@Override
	public List<JiraIssue> findIssuesByDateAndTypeAndStatus(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap, String dateFrom, String dateTo, String dateCriteria,
			String mapStatusCriteria, boolean isProductionDefect) {

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
		criteria = criteria.and(PRODUCTION_DEFECT).is(isProductionDefect);
		// Project level storyType filters
		List<Criteria> projectCriteriaList = new ArrayList<>();
		uniqueProjectMap.forEach((project, filterMap) -> {
			Criteria projectCriteria = new Criteria();
			projectCriteria.and(CONFIG_ID).is(project);
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
		return operations.find(query, JiraIssue.class);

	}

	@Override
	public List<SprintWiseStory> findIssuesAndTestDetailsGroupBySprint(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap, String filterToShowOnTrend, String individualDevOrQa,
			Map<String, Map<String, Object>> uniqueProjectMapNotIn) {

		Criteria criteria = new Criteria();

		// map of common filters Project and Sprint
		criteria = getCommonFiltersCriteria(mapOfFilters, criteria);
		// Project level storyType filters
		List<Criteria> projectCriteriaList = new ArrayList<>();
		uniqueProjectMap.forEach((project, filterMap) -> {
			Criteria projectCriteria = new Criteria();
			projectCriteria.and(CONFIG_ID).is(project);
			filterMap.forEach((subk, subv) -> projectCriteria.and(subk).in((List<Pattern>) subv));
			projectCriteriaList.add(projectCriteria);

		});

		uniqueProjectMapNotIn.forEach((project, filterMap) -> {
			Criteria projectCriteria = new Criteria();
			projectCriteria.and(CONFIG_ID).is(project);
			filterMap.forEach((subk, subv) -> projectCriteria.and(subk).nin((List<Pattern>) subv));
			projectCriteriaList.add(projectCriteria);

		});
		Criteria criteriaAggregatedAtProjectLevel = new Criteria()
				.orOperator(projectCriteriaList.toArray(new Criteria[0]));
		Criteria criteriaProjectLevelAdded = new Criteria().andOperator(criteria, criteriaAggregatedAtProjectLevel);
		MatchOperation matchStage = Aggregation.match(criteriaProjectLevelAdded);

		GroupOperation groupBySprint = Aggregation.group(SPRINT_ID).last(SPRINT_ID).as(SPRINT).last(SPRINT_NAME)
				.as(SPRINT_NAME).last(CONFIG_ID).as(CONFIG_ID).addToSet(NUMBER).as(STORY_LIST);

		Aggregation aggregation = Aggregation.newAggregation(matchStage, groupBySprint);
		return operations.aggregate(aggregation, JiraIssue.class, SprintWiseStory.class).getMappedResults();
	}

	@Override
	public List<JiraIssue> findIssueAndDescByNumber(List<String> storyNumber) {

		Criteria criteria = new Criteria();
		criteria = criteria.and(NUMBER).in(storyNumber);

		Query query = new Query(criteria);
		query.fields().include(NUMBER);
		query.fields().include(NAME);
		query.fields().include(URL);
		query.fields().include(CONFIG_ID);
		return new ArrayList<>(operations.find(query, JiraIssue.class));

	}

	/**
	 * find linked defects of given stories and filters
	 * 
	 * @param mapOfFilters
	 * @param defectsStoryIds
	 * @param uniqueProjectMap
	 * @return
	 */
	@Override
	public List<JiraIssue> findLinkedDefects(Map<String, List<String>> mapOfFilters, Set<String> defectsStoryIds,
			Map<String, Map<String, Object>> uniqueProjectMap) {
		Criteria criteria = new Criteria();

		// map of common filters Project and Sprint
		criteria = getCommonFiltersCriteria(mapOfFilters, criteria);

		// Project level storyType filters
		List<Criteria> projectCriteriaList = new ArrayList<>();
		uniqueProjectMap.forEach((project, filterMap) -> {
			Criteria projectCriteria = new Criteria();
			projectCriteria.and(CONFIG_ID).is(project);
			filterMap.forEach((subk, subv) -> projectCriteria.and(subk).in((List<Pattern>) subv));
			projectCriteriaList.add(projectCriteria);
		});

		if (!CollectionUtils.isEmpty(projectCriteriaList)) {
			Criteria criteriaAggregatedAtProjectLevel = new Criteria()
					.orOperator(projectCriteriaList.toArray(new Criteria[0]));
			criteria = new Criteria().andOperator(criteria, criteriaAggregatedAtProjectLevel);

		}

		criteria = criteria.and(DEFECT_STORY_ID).in(defectsStoryIds);
		Query query = new Query(criteria);

		return operations.find(query, JiraIssue.class);

	}

	/**
	 * Find issues filtered by map of filters, type name and defectStoryIds
	 *
	 * @param mapOfFilters
	 *            filters
	 * @param uniqueProjectMap
	 *            project map filters
	 * @return list of jira issues
	 */
	@SuppressWarnings(UNCHECKED)
	@Override
	public List<JiraIssue> findIssuesByFilterAndProjectMapFilter(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap) {
		Criteria criteria = new Criteria();
		// map of common filters Project and Sprint
		criteria = getCommonFiltersCriteria(mapOfFilters, criteria);
		Criteria criteriaProjectLevelAdded = new Criteria().andOperator(criteria);

		List<Criteria> projectCriteriaList = new ArrayList<>();
		uniqueProjectMap.forEach((project, filterMap) -> {
			Criteria projectCriteria = new Criteria();
			filterMap.forEach((subk, subv) -> projectCriteria.and(subk).in((List<Pattern>) subv));
			projectCriteriaList.add(projectCriteria);
		});
		Query query = new Query(criteriaProjectLevelAdded);
		if (!projectCriteriaList.isEmpty()) {
			Criteria criteriaAggregatedAtProjectLevel = new Criteria()
					.andOperator(projectCriteriaList.toArray(new Criteria[0]));
			Criteria updatedCriteria = new Criteria().andOperator(criteriaProjectLevelAdded,
					criteriaAggregatedAtProjectLevel);
			query = new Query(updatedCriteria);
		}
		return operations.find(query, JiraIssue.class);
	}

	@Override
	public List<JiraIssue> findByRelease(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap) {
		Criteria criteria = new Criteria();
		// map of common filters Project and Release
		for (Map.Entry<String, List<String>> entry : mapOfFilters.entrySet()) {
			if (CollectionUtils.isNotEmpty(entry.getValue())) {
				criteria = criteria.and(entry.getKey()).in(entry.getValue());
			}
		}
		List<Criteria> projectCriteriaList = new ArrayList<>();
		uniqueProjectMap.forEach((project, filterMap) -> {
			Criteria projectCriteria = new Criteria();
			projectCriteria.and(CONFIG_ID).is(project);
			filterMap.forEach((subk, subv) -> {
				if (subk.equalsIgnoreCase(CommonConstant.RELEASE)) {
					projectCriteria.and(RELEASE_VERSION).in((List<Pattern>) filterMap.get(CommonConstant.RELEASE));
				} else {
					projectCriteria.and(subk).in((List<Pattern>) subv);
				}
			});
			projectCriteriaList.add(projectCriteria);
		});

		Criteria criteriaAggregatedAtProjectLevel = new Criteria()
				.orOperator(projectCriteriaList.toArray(new Criteria[0]));
		Criteria criteriaProjectLevelAdded = new Criteria().andOperator(criteria, criteriaAggregatedAtProjectLevel);
		Query query = new Query(criteriaProjectLevelAdded);
		// add projection
		return operations.find(query, JiraIssue.class);

	}

	/**
	 * find unique Release Version Name group by type name
	 * 
	 * @param mapOfFilters
	 * @return
	 */
	@Override
	public List<ReleaseWisePI> findUniqueReleaseVersionByUniqueTypeName(Map<String, List<String>> mapOfFilters) {

		Criteria criteria = new Criteria();
		// map of common filters Project and Sprint
		criteria = getCommonFiltersCriteria(mapOfFilters, criteria);

		MatchOperation matchStage = Aggregation.match(criteria);

		GroupOperation groupOperation = Aggregation.group(TYPE_NAME, "basicProjectConfigId",
				"releaseVersions.releaseName");

		ProjectionOperation projectionOperation = Aggregation.project().andExpression("_id.typeName")
				.as("uniqueTypeName").andExpression("_id.releaseName").as("releaseName")
				.andExpression("_id.basicProjectConfigId").as("basicProjectConfigId");

		Aggregation aggregation = Aggregation.newAggregation(matchStage, groupOperation, projectionOperation);
		return operations.aggregate(aggregation, JiraIssue.class, ReleaseWisePI.class).getMappedResults();
	}

}