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
package com.publicissapient.kpidashboard.apis.mongock.installation;

import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;

import com.publicissapient.kpidashboard.apis.data.FieldMappingStructureDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiCategoryDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiCategoryMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiColumnConfigDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiDefinationDataFactory;
import com.publicissapient.kpidashboard.apis.mongock.FieldMappingStructureForMongock;
import com.publicissapient.kpidashboard.apis.util.MongockUtil;
import com.publicissapient.kpidashboard.common.model.application.KpiCategory;
import com.publicissapient.kpidashboard.common.model.application.KpiCategoryMapping;
import com.publicissapient.kpidashboard.common.model.application.KpiColumnConfig;
import com.publicissapient.kpidashboard.common.model.application.KpiMaster;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.extern.slf4j.Slf4j;

/**
 * @author hargupta15
 */
@Slf4j
@ChangeUnit(id = "ddl5", order = "005", author = "hargupta15")
public class KpiDefinition {
	private final MongoTemplate mongoTemplate;
	private static final String KPI_MASTER_COLLECTION = "kpi_master";
	private static final String KPI_CATEGORY_COLLECTION = "kpi_category";

	private static final String KPI_CATEGORY_MAPPING_COLLECTION = "kpi_category_mapping";

	private static final String KPI_COLUMN_CONFIGS_COLLECTION = "kpi_column_configs";

	private static final String FIELD_MAPPING_STRUCTURE_COLLECTION = "field_mapping_structure";

	List<KpiMaster> kpiList;
	List<KpiColumnConfig> kpiColumnConfigs;
	List<KpiCategoryMapping> kpiCategoryMappingList;
	List<KpiCategory> kpiCategoryList;
	List<FieldMappingStructureForMongock> fieldMappingStructureList;

	public KpiDefinition(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;

		KpiDefinationDataFactory kpiDefinationDataFactory = KpiDefinationDataFactory.newInstance();
		KpiColumnConfigDataFactory kpiColumnConfigDataFactory = KpiColumnConfigDataFactory.newInstance();
		KpiCategoryMappingDataFactory kpiCategoryMappingDataFactory = KpiCategoryMappingDataFactory.newInstance();
		KpiCategoryDataFactory kpiCategoryDataFactory = KpiCategoryDataFactory.newInstance();
		FieldMappingStructureDataFactory fieldMappingStructureDataFactory = FieldMappingStructureDataFactory
				.newInstance();

		kpiList = kpiDefinationDataFactory.getKpiList();
		kpiColumnConfigs = kpiColumnConfigDataFactory.getKpiColumnConfigs();
		kpiCategoryMappingList = kpiCategoryMappingDataFactory.getKpiCategoryMappingList();
		kpiCategoryList = kpiCategoryDataFactory.getKpiCategoryList();
		fieldMappingStructureList = fieldMappingStructureDataFactory.getFieldMappingStructureList();
	}

	@Execution
	public boolean changeSet() {
		MongockUtil.saveListToDB(kpiList, KPI_MASTER_COLLECTION, mongoTemplate);
		MongockUtil.saveListToDB(kpiColumnConfigs, KPI_COLUMN_CONFIGS_COLLECTION, mongoTemplate);
		MongockUtil.saveListToDB(kpiCategoryMappingList, KPI_CATEGORY_MAPPING_COLLECTION, mongoTemplate);
		MongockUtil.saveListToDB(kpiCategoryList, KPI_CATEGORY_COLLECTION, mongoTemplate);
		MongockUtil.saveListToDB(fieldMappingStructureList, FIELD_MAPPING_STRUCTURE_COLLECTION, mongoTemplate);
		return true;
	}

	@RollbackExecution
	public void rollback() {
		// We are inserting the documents through DDL, no rollback to any collections.
	}
}
