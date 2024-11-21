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

package com.publicissapient.kpidashboard.azure.service;

import com.publicissapient.kpidashboard.azure.model.AzureSprintReportLog;
import com.publicissapient.kpidashboard.azure.model.RefreshAuditDetails;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AzureSprintReportLogRepositoryCustomImpl implements AzureSprintReportLogRepositoryCustom {

	@Autowired
	private MongoTemplate mongoTemplate;

	/**
	 * it will upsert the refresh sprint toggle time each time when it is toggled on
	 * 
	 * @param projectWiseSprintRefreshToggleMap
	 */
	@Override
	public void addUpdateTimesInBulk(Map<ObjectId, Map<String, LocalDateTime>> projectWiseSprintRefreshToggleMap) {
		// Create a BulkOperations object in ordered mode
		BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.ORDERED,
				AzureSprintReportLog.class);

		// Iterate through the updates map
		for (Map.Entry<ObjectId, Map<String, LocalDateTime>> entry : projectWiseSprintRefreshToggleMap.entrySet()) {
			ObjectId basicProjectConfigId = entry.getKey();
			Map<String, LocalDateTime> sprintUpdates = entry.getValue();

			// Query for the document based on basicProjectConfigId
			Query query = new Query(Criteria.where("basicProjectConfigId").is(basicProjectConfigId));

			for (Map.Entry<String, LocalDateTime> sprintEntry : sprintUpdates.entrySet()) {
				String sprintId = sprintEntry.getKey();
				LocalDateTime updateTime = sprintEntry.getValue();

				// Create a RefreshAuditDetails object to add to the list
				RefreshAuditDetails newEntry = new RefreshAuditDetails(updateTime);

				// Update operation to push the new RefreshAuditDetails into the list
				Update update = new Update().push("sprintRefreshLog." + sprintId, newEntry);

				// Add the update operation with upsert enabled to bulk operations
				bulkOperations.upsert(query, update);
			}
		}

		// Execute the bulk operations
		bulkOperations.execute();

	}
}
