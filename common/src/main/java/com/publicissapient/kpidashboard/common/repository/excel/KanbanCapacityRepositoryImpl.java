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
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.publicissapient.kpidashboard.common.model.application.LeafNodeCapacity;
import com.publicissapient.kpidashboard.common.model.excel.KanbanCapacity;

/** The type Kanban capacity repository. */
public class KanbanCapacityRepositoryImpl implements KanbanCapacityRepoCustom {

	private static final String START_DATE = "startDate";
	private static final String END_DATE = "endDate";
	private static final String DATE_PATTERN = "yyyy-MM-dd";
	@Autowired
	private MongoOperations operations;

	@Override
	public List<KanbanCapacity> findIssuesByType(Map<String, Object> mapOfFilters, String dateFrom, String dateTo) {
		Criteria criteria = new Criteria();
		DateTime startDateTime = DateTimeFormat.forPattern(DATE_PATTERN).parseDateTime(dateFrom).withTime(0, 0, 0, 0);
		DateTime endDateTime = DateTimeFormat.forPattern(DATE_PATTERN).parseDateTime(dateTo).withTime(0, 0, 0, 0);
		// map of common filters Project and Sprint
		for (Map.Entry<String, Object> entry : mapOfFilters.entrySet()) {
			String key = entry.getKey();
			if (ObjectUtils.isNotEmpty(entry.getValue()) &&
					!key.equalsIgnoreCase("additionalFilterCapacityList.nodeCapacityList.additionalFilterId") &&
					!key.equalsIgnoreCase("additionalFilterCapacityList.filterId")) {
				if (entry.getValue() instanceof List<?>) {
					List<ObjectId> value = (List<ObjectId>) entry.getValue();
					criteria = criteria.and(key).in(value);
				} else {
					criteria = criteria.and(key).in(entry.getValue());
				}
			}
		}
		criteria = criteria.and(START_DATE).lte(endDateTime.withTime(0, 0, 0, 0));
		criteria = criteria.and(END_DATE).gte(startDateTime.withTime(0, 0, 0, 0));

		Query query = new Query(criteria);
		List<KanbanCapacity> kanbanCapacityList = operations.find(query, KanbanCapacity.class);
		if (mapOfFilters.containsKey("additionalFilterCapacityList.nodeCapacityList.additionalFilterId")) {
			kanbanCapacityList.stream().forEach(capacityKpiData -> {
				if (CollectionUtils.isNotEmpty(capacityKpiData.getAdditionalFilterCapacityList())) {
					List<String> additionalFilter = (List<String>) mapOfFilters
							.get("additionalFilterCapacityList.nodeCapacityList.additionalFilterId");
					List<String> upperCaseKey = ((List<String>) mapOfFilters.get("additionalFilterCapacityList.filterId"))
							.stream().map(String::toUpperCase).toList();
					capacityKpiData.setCapacity(capacityKpiData.getAdditionalFilterCapacityList().stream()
							.filter(additionalFilterCapacity -> upperCaseKey
									.contains(additionalFilterCapacity.getFilterId().toUpperCase()))
							.flatMap(additionalFilterCapacity -> additionalFilterCapacity.getNodeCapacityList().stream())
							.filter(leaf -> additionalFilter.contains(leaf.getAdditionalFilterId()))
							.mapToDouble(LeafNodeCapacity::getAdditionalFilterCapacity).sum());
				} else {
					capacityKpiData.setCapacity(0D);
				}
			});
		}
		return kanbanCapacityList;
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
