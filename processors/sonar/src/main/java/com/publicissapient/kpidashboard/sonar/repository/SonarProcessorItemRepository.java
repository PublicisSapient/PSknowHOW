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

package com.publicissapient.kpidashboard.sonar.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.common.repository.generic.ProcessorItemRepository;
import com.publicissapient.kpidashboard.sonar.model.SonarProcessorItem;

/** Sonar Project Configuration Repository. */
@Repository
public interface SonarProcessorItemRepository extends ProcessorItemRepository<SonarProcessorItem> {

	@Query("{ 'processorId' : ?0, 'toolConfigId': ?1, 'toolDetailsMap.instanceUrl' : ?2, 'isActive': true}")
	List<SonarProcessorItem> findEnabledProjectsForTool(ObjectId processorId, ObjectId toolConfigId, String instanceUrl);
}
