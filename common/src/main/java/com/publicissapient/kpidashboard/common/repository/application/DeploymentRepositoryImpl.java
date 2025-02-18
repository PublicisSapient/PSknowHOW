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

package com.publicissapient.kpidashboard.common.repository.application;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.publicissapient.kpidashboard.common.model.application.Deployment;

public class DeploymentRepositoryImpl implements DeploymentRepositoryCustom {

	@Autowired
	private MongoTemplate mongoTemplate;

	/**
	 * find deployment list by given projectToolConfigIds , deployment status ,
	 * start and end date wise
	 *
	 * @param mapOfFilters
	 * @param projectBasicConfigIds
	 * @param startDateLD
	 * @param endDateLD
	 * @return
	 */
	@Override
	public List<Deployment> findDeploymentList(Map<String, List<String>> mapOfFilters,
			Set<ObjectId> projectBasicConfigIds, String startDateLD, String endDateLD) {
		Criteria criteria = new Criteria();

		// map of common filters hierarchy and Sprint
		criteria = getCommonFiltersCriteria(mapOfFilters, criteria);

		// date level filters
		criteria = criteria.and("startTime").gte(startDateLD).and("endTime").lte(endDateLD);

		criteria = criteria.and("basicProjectConfigId").in(projectBasicConfigIds);

		Query query = new Query(criteria);

		return mongoTemplate.find(query, Deployment.class);
	}

	/**
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
}
