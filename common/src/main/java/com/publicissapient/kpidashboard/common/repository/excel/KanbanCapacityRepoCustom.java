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

import org.bson.types.ObjectId;

import com.publicissapient.kpidashboard.common.model.excel.KanbanCapacity;

/**
 * The interface Kanban capacity repo custom.
 */
public interface KanbanCapacityRepoCustom {

	/**
	 * Find issues by type list.
	 *
	 * @param mapOfFilters
	 *            the map of filters
	 * @param uniqueProjectMap
	 *            the unique project map
	 * @param dateFrom
	 *            the date from
	 * @param dateTo
	 *            the date to
	 * @return list of feature
	 */
	List<KanbanCapacity> findIssuesByType(Map<String, List<ObjectId>> mapOfFilters, String dateFrom, String dateTo);

	/**
	 * find already existing kanban capacity
	 * 
	 * @param mapOfFilters
	 *            the map of filters
	 * @param dateFrom
	 *            the date from
	 * @return list of feature
	 */
	public List<KanbanCapacity> findByFilterMapAndDate(Map<String, String> mapOfFilters, String dateFrom);
}
