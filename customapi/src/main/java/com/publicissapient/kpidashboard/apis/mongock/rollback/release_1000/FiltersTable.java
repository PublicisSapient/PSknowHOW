/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1000;

import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.publicissapient.kpidashboard.apis.mongock.data.FiltersDataFactory;
import com.publicissapient.kpidashboard.apis.util.MongockUtil;
import com.publicissapient.kpidashboard.common.model.application.Filters;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.extern.slf4j.Slf4j;

/**
 * @author purgupta2
 */
@Slf4j
@ChangeUnit(id = "r_new_filters", order = "10001", author = "purgupta2", systemVersion = "10.0.0")
public class FiltersTable {

	private final MongoTemplate mongoTemplate;
	List<Filters> filtersList;
	private static final String KPI_ID = "kpiId";
	private static final String KPI_MASTER = "kpi_master";
	private static final String KPI_WIDTH = "kpiWidth";
	private static final String KPI_HEIGHT = "kpiHeight";
	private static final String KPI150 = "kpi150";
	private static final String KPI120 = "kpi120";

	public FiltersTable(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
		FiltersDataFactory filtersDataFactory = FiltersDataFactory.newInstance();
		filtersList = filtersDataFactory.getFiltersList();
	}

	@Execution
	public boolean changeSet() {
		removeListFromDB();
		rollbackAddKpiWidthHeightFields();
		return true;
	}

	public void addKpiWidthHeightFields() {
		// Add kpiWidth: 100 for specific KPIs
		updateKpiWidth(new String[]{"kpi169", "kpi165", "kpi147", "kpi154", "kpi125", KPI150, KPI120});

		// Add kpiHeight: 100 for specific KPIs
		updateKpiHeight(new String[]{KPI150, KPI120});
	}

	private void updateKpiWidth(String[] kpiIds) {
		for (String kpiId : kpiIds) {
			Query query = new Query(Criteria.where(KPI_ID).is(kpiId));
			Update update = new Update().set(KPI_WIDTH, 100);
			mongoTemplate.updateFirst(query, update, KPI_MASTER);
		}
	}

	private void updateKpiHeight(String[] kpiIds) {
		for (String kpiId : kpiIds) {
			Query query = new Query(Criteria.where(KPI_ID).is(kpiId));
			Update update = new Update().set(KPI_HEIGHT, 100);
			mongoTemplate.updateFirst(query, update, KPI_MASTER);
		}
	}

	@RollbackExecution
	public void rollback() {
		// We are inserting the documents through DDL, no rollback to any collections.
		MongockUtil.saveListToDB(filtersList, "filters", mongoTemplate);
		addKpiWidthHeightFields();
	}

	public void removeListFromDB() {
		filtersList.forEach(filter -> mongoTemplate.remove(filter, KPI_MASTER));
	}

	public void rollbackAddKpiWidthHeightFields() {
		// Remove kpiWidth for specific KPIs
		removeKpiWidth(new String[]{"kpi169", "kpi165", "kpi147", "kpi154", "kpi125", KPI150, KPI120});

		// Remove kpiWidth and kpiHeight for specific KPIs
		removeKpiWidthAndHeight(new String[]{KPI150, KPI120});
	}

	private void removeKpiWidth(String[] kpiIds) {
		for (String kpiId : kpiIds) {
			Query query = new Query(Criteria.where(KPI_ID).is(kpiId));
			Update update = new Update().unset(KPI_WIDTH);
			mongoTemplate.updateFirst(query, update, KPI_MASTER);
		}
	}

	private void removeKpiWidthAndHeight(String[] kpiIds) {
		for (String kpiId : kpiIds) {
			Query query = new Query(Criteria.where(KPI_ID).is(kpiId));
			Update update = new Update().unset(KPI_HEIGHT);
			mongoTemplate.updateFirst(query, update, KPI_MASTER);
		}
	}
}
