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
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.publicissapient.kpidashboard.common.model.application.Build;

/**
 * An implementation of {@link BuildRepositoryCustom}
 */
public class BuildRepositoryImpl implements BuildRepositoryCustom {

	@Autowired
	private MongoOperations operations;

	@Override
	public List<Build> findBuildList(Map<String, List<String>> mapOfFilters, Set<ObjectId> projectBasicConfigIds,
			String startDate, String endDate) {
		Criteria criteria = new Criteria();

		// map of common filters
		criteria = getCommonFiltersCriteria(mapOfFilters, criteria);

		// date level filters
		if (StringUtils.isNotEmpty(startDate) && StringUtils.isNotEmpty(endDate)) {
			long startDateUTC = new DateTime(startDate, DateTimeZone.UTC).toDate().getTime();
			long endDateUTC = new DateTime(endDate, DateTimeZone.UTC).toDate().getTime();
			criteria = criteria.and("startTime").gte(startDateUTC).and("endTime").lte(endDateUTC);
		}

		criteria.and("basicProjectConfigId").in(projectBasicConfigIds);

		Query query = new Query(criteria);

		return operations.find(query, Build.class);
	}

	private Criteria getCommonFiltersCriteria(Map<String, List<String>> mapOfFilters, Criteria criteria) {
		Criteria theCriteria = criteria;
		if (MapUtils.isNotEmpty(mapOfFilters)) {
			for (Map.Entry<String, List<String>> entry : mapOfFilters.entrySet()) {
				if (CollectionUtils.isNotEmpty(entry.getValue())) {
					theCriteria = theCriteria.and(entry.getKey()).in(entry.getValue());
				}
			}
		}
		return theCriteria;
	}

}
