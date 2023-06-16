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

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.common.model.application.KpiColumnConfig;

@Repository
public interface KpiColumnConfigRepository extends MongoRepository<KpiColumnConfig, ObjectId> {
	/**
	 * Returns KpiColumnConfig from persistence store by BasicProjectConfigId &
	 * KpiId if basicProjectConfigId is present
	 *
	 * @param basicProjectConfigId
	 *            BasicProjectConfigId
	 * @param kpiId
	 *            kpiId
	 * @return KpiColumnConfig if exist
	 */
	KpiColumnConfig findByBasicProjectConfigIdAndKpiId(ObjectId basicProjectConfigId, String kpiId);

}
