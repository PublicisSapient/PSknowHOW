
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

import org.bson.types.ObjectId;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;

import com.publicissapient.kpidashboard.common.model.application.KpiMaster;

/**
 * Repository for {@link KpiMaster}.
 */
public interface KpiMasterRepository extends CrudRepository<KpiMaster, ObjectId>, QuerydslPredicateExecutor<KpiMaster> {
	/**
	 * Finds kpis based on dashboard name.
	 * 
	 * @param dashboardName
	 *            dashboard name
	 * @return list of KpiMaster
	 */
	List<KpiMaster> findBykpiOnDashboard(String dashboardName);

	/**
	 * Finds kpis based on isInFeed
	 * 
	 * @param isInFeed
	 *            true or false
	 * @return list of KpiMaster
	 */
	List<KpiMaster> findBykpiInAggregatedFeed(String isInFeed);

	/**
	 * Finds kpis based on kpi category.
	 * 
	 * @param kpiCategory
	 *            kpi category
	 * @param kanban
	 * @return list of KpiMaster
	 */
	List<KpiMaster> findByKpiCategoryAndKanban(String kpiCategory, boolean kanban);

	/**
	 *
	 * @param kanban
	 * @param kpiCategory
	 * @return
	 */
	List<KpiMaster> findByKanbanAndKpiCategoryNotIn(boolean kanban, List<String> kpiCategory);
}