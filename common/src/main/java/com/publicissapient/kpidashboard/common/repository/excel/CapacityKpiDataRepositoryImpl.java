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

package com.publicissapient.kpidashboard.common.repository.excel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.publicissapient.kpidashboard.common.model.excel.CapacityKpiData;

import lombok.extern.slf4j.Slf4j;

/**
 * Repository for {@link CapacityKpiData}
 *
 * @author anisingh4
 */
@Slf4j
public class CapacityKpiDataRepositoryImpl implements CapacityKpiDataCustomRepository {

	private static final String CONFIG_ID = "basicProjectConfigId";
	@Autowired
	private MongoOperations operations;

	/*
	 * Find Data by filters
	 *
	 * @see
	 * com.publicissapient.kpidashboard.repository.CapacityKpiDataCustomRepository#
	 * findByFilters(java.util.Map)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<CapacityKpiData> findByFilters(Map<String, Object> mapofFilters,
			Map<String, Map<String, Object>> uniqueProjectMap) {
		Criteria criteria = new Criteria();

		// map of common filters Project, Project and Sprint
		for (Map.Entry<String, Object> entry : mapofFilters.entrySet()) {
			if (CollectionUtils.isNotEmpty((List<Pattern>) entry.getValue())) {
				criteria = criteria.and(entry.getKey()).in((List<Pattern>) entry.getValue());
			}
		}
		// Project level storyType filters
		List<Criteria> projectCriteriaList = new ArrayList<>();
		uniqueProjectMap.forEach((project, filterMap) -> {
			Criteria projectCriteria = new Criteria();
			projectCriteria.and(CONFIG_ID).is(project);
			filterMap.forEach((subk, subv) -> projectCriteria.and(subk).in((List<Pattern>) subv));
			projectCriteriaList.add(projectCriteria);

		});
		Query query = new Query(criteria);
		if (CollectionUtils.isNotEmpty(projectCriteriaList)) {
			Criteria criteriaAggregatedAtProjectLevel = new Criteria()
					.orOperator(projectCriteriaList.toArray(new Criteria[0]));
			Criteria criteriaProjectLevelAdded = new Criteria().andOperator(criteria, criteriaAggregatedAtProjectLevel);
			query = new Query(criteriaProjectLevelAdded);
		}
		List<CapacityKpiData> data = operations.find(query, CapacityKpiData.class);
		if (CollectionUtils.isEmpty(data)) {
			log.info("No Data found for filters");
		}
		return data;
	}
}
