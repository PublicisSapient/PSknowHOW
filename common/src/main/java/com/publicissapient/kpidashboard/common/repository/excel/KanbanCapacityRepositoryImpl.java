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

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.publicissapient.kpidashboard.common.model.excel.KanbanCapacity;

/**
 * The type Kanban capacity repository.
 */
public class KanbanCapacityRepositoryImpl implements KanbanCapacityRepoCustom {

	private static final String START_DATE = "startDate";
	private static final String END_DATE = "endDate";
	private static final String TICKET_PROJECT_ID_FIELD = "projectId";
	private static final String DATE_PATTERN = "yyyy-MM-dd";
	@Autowired
	private MongoOperations operations;

	@Override
	public List<KanbanCapacity> findIssuesByType(Map<String, List<ObjectId>> mapOfFilters, String dateFrom,
			String dateTo) {
		Criteria criteria = new Criteria();
		DateTime startDateTime = DateTimeFormat.forPattern(DATE_PATTERN).parseDateTime(dateFrom).withTime(0, 0, 0, 0);
		DateTime endDateTime = DateTimeFormat.forPattern(DATE_PATTERN).parseDateTime(dateTo).withTime(0, 0, 0, 0);
		// map of common filters Project and Sprint
		for (Map.Entry<String, List<ObjectId>> entry : mapOfFilters.entrySet()) {
			if (CollectionUtils.isNotEmpty(entry.getValue())) {
				criteria = criteria.and(entry.getKey()).in(entry.getValue());
			}
		}
		criteria = criteria.and(START_DATE).lte(endDateTime.withTime(0, 0, 0, 0));
		criteria = criteria.and(END_DATE).gte(startDateTime.withTime(0, 0, 0, 0));

		Query query = new Query(criteria);
		return operations.find(query, KanbanCapacity.class);

	}

	@Override
	public List<KanbanCapacity> findByFilterMapAndDate(Map<String, String> mapOfFilters, String dateFrom) {
		Criteria criteria = new Criteria();
		DateTime startDateTime = DateTimeFormat.forPattern(DATE_PATTERN).parseDateTime(dateFrom).withTime(0, 0, 0, 0);
		// map of common filters
		for (Map.Entry<String, String> entry : mapOfFilters.entrySet()) {
			if (StringUtils.isNotEmpty(entry.getValue())) {
				criteria = criteria.and(entry.getKey()).in(entry.getValue());
			}
		}
		criteria = criteria.and(START_DATE).lte(startDateTime);
		criteria = criteria.and(END_DATE).gte(startDateTime);

		Query query = new Query(criteria);
		return operations.find(query, KanbanCapacity.class);

	}
}
