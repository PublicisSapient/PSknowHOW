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

package com.publicissapient.kpidashboard.common.repository.zephyr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.model.zephyr.TestCaseDetails;

/**
 * Repository for {@link TestCaseDetails} with custom methods implementation.
 */
@Service
public class TestCaseDetailsRepositoryImpl implements TestCaseDetailsRepositoryCustom {

	private static final String UNCHECKED = "unchecked";
	private static final String BASIC_PROJ_CONF_ID = "basicProjectConfigId";
	private static final String NIN = "nin";
	private static final String TEST_CASE_STATUS = "testCaseStatus";
	@Autowired
	private MongoTemplate operations;

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

	public List<TestCaseDetails> findNonRegressionTestDetails(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap, String mapStatusCriteria) {
		Criteria criteria = new Criteria();

		criteria = getCommonFiltersCriteria(mapOfFilters, criteria);
		// Project level storyType filters
		List<Criteria> projectCriteriaList = new ArrayList<>();
		uniqueProjectMap.forEach((project, filterMap) -> {
			Criteria projectCriteria = new Criteria();
			projectCriteria.and(BASIC_PROJ_CONF_ID).is(project);
			filterMap.forEach((subk, subv) -> {
				if (subk.equalsIgnoreCase("labels")) {
					projectCriteria.and(subk).nin((List<Pattern>) subv);
				} else if (subk.equals(TEST_CASE_STATUS) && mapStatusCriteria.equalsIgnoreCase(NIN)) {
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

		return operations.find(query, TestCaseDetails.class);
	}

	@Override
	public List<TestCaseDetails> findTestDetails(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap, String mapStatusCriteria) {
		Criteria criteria = new Criteria();

		// map of common filters Project and Sprint
		criteria = getCommonFiltersCriteria(mapOfFilters, criteria);
		// Project level storyType filters
		List<Criteria> projectCriteriaList = new ArrayList<>();
		uniqueProjectMap.forEach((project, filterMap) -> {
			Criteria projectCriteria = new Criteria();
			projectCriteria.and(BASIC_PROJ_CONF_ID).is(project);
			filterMap.forEach((subk, subv) -> {
				if (subk.equals(TEST_CASE_STATUS) && mapStatusCriteria.equalsIgnoreCase(NIN)) {
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

		return operations.find(query, TestCaseDetails.class);
	}
}