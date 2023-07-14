package com.publicissapient.kpidashboard.common.repository.jira;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.model.jira.IssueBacklog;

@Service
public class IssueBacklogRepositoryImpl implements IssueBacklogRespositoryCustom {

	private static final String UNCHECKED = "unchecked";
	private static final String CONFIG_ID = "basicProjectConfigId";
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
	private static final String SPRINT_NAME = "sprintName";

	@Autowired
	private MongoTemplate operations;

	@SuppressWarnings(UNCHECKED)
	@Override
	public List<IssueBacklog> findIssuesBySprintAndType(Map<String, List<String>> mapOfFilters,
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

		return operations.find(query, IssueBacklog.class);

	}

	private Criteria getCommonFiltersCriteria(Map<String, List<String>> mapOfFilters, Criteria criteria) {
		Criteria theCriteria = criteria;
		for (Map.Entry<String, List<String>> entry : mapOfFilters.entrySet()) {
			if (CollectionUtils.isNotEmpty(entry.getValue())) {
				theCriteria = theCriteria.and(entry.getKey()).in(entry.getValue());
			}
		}
		return theCriteria;
	}

	@Override
	public List<IssueBacklog> findIssuesByDateAndTypeAndStatus(Map<String, List<String>> mapOfFilters,
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
		return operations.find(query, IssueBacklog.class);

	}

	@Override
	public List<IssueBacklog> findDefectsWithoutStoryLink(Map<String, List<String>> mapOfFilters,
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

		return operations.find(query, IssueBacklog.class);
	}

	@SuppressWarnings(UNCHECKED)
	@Override
	public List<IssueBacklog> findIssuesByFilterAndProjectMapFilter(Map<String, List<String>> mapOfFilters,
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
		return operations.find(query, IssueBacklog.class);
	}

	@Override
	public List<IssueBacklog> findUnassignedIssues(String startDate, String endDate,
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
		criteria.orOperator(Criteria.where(SPRINT_NAME).isNull(), Criteria.where(SPRINT_NAME).is(""), orCriteria);
		Query query = new Query(criteria);

		return operations.find(query, IssueBacklog.class);
	}

}
