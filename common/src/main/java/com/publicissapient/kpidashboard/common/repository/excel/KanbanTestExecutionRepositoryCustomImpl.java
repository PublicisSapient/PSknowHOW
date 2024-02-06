package com.publicissapient.kpidashboard.common.repository.excel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.publicissapient.kpidashboard.common.model.testexecution.KanbanTestExecution;

/**
 * Provides implementation for methods available in interface
 * KanbanTestExecutionDetailRepository.
 *
 */
public class KanbanTestExecutionRepositoryCustomImpl implements KanbanTestExecutionRepositoryCustom {

	private static final String PROJECT_ID = "basicProjectConfigId";
	private static final String EXECUTION_DATE = "executionDate";

	@Autowired
	private MongoOperations operations;

	@SuppressWarnings("unchecked")
	@Override
	public List<KanbanTestExecution> findTestExecutionDetailByFilters(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap, String dateFrom, String dateTo) {

		Criteria criteria = new Criteria();

		// map of common filters Project and Sprint
		for (Map.Entry<String, List<String>> entry : mapOfFilters.entrySet()) {
			if (CollectionUtils.isNotEmpty(entry.getValue())) {
				criteria = criteria.and(entry.getKey()).in(entry.getValue());
			}
		}
		criteria.and(EXECUTION_DATE).gte(dateFrom).lte(dateTo);

		Query query = new Query(criteria);

		// Project level filters
		if (MapUtils.isNotEmpty(uniqueProjectMap)) {
			List<Criteria> projectCriteriaList = new ArrayList<>();
			uniqueProjectMap.forEach((project, filterMap) -> {
				Criteria projectCriteria = new Criteria();
				projectCriteria.and(PROJECT_ID).is(project);
				filterMap.forEach((subk, subv) -> projectCriteria.and(subk).in((List<Pattern>) subv));
				projectCriteriaList.add(projectCriteria);
			});
			Criteria criteriaAggregatedAtProjectLevel = new Criteria()
					.orOperator(projectCriteriaList.toArray(new Criteria[0]));

			Criteria criteriaProjectLevelAdded = new Criteria().andOperator(criteria, criteriaAggregatedAtProjectLevel);
			query = new Query(criteriaProjectLevelAdded);
		}

		return operations.find(query, KanbanTestExecution.class);
	}

}
