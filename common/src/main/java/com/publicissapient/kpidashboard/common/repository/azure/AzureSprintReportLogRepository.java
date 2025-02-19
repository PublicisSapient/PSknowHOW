/*
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.publicissapient.kpidashboard.common.repository.azure;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.common.model.azure.AzureSprintReportLog;

/**
 * @author shunaray
 */
@Repository
public interface AzureSprintReportLogRepository extends MongoRepository<AzureSprintReportLog, ObjectId> {
	/**
	 * find by basic project config id and sprint id
	 *
	 * @param basicProjectConfigId
	 *          basic project config id
	 * @param sprintId
	 *          sprint id
	 * @return azure sprint report log
	 */
	Optional<AzureSprintReportLog> findByBasicProjectConfigIdAndSprintId(ObjectId basicProjectConfigId, String sprintId);

	/**
	 * find by basic project config id
	 *
	 * @param basicProjectConfigId
	 *          basic project config id
	 * @return list of azure sprint report log
	 */
	List<AzureSprintReportLog> findByBasicProjectConfigId(ObjectId basicProjectConfigId);
}
