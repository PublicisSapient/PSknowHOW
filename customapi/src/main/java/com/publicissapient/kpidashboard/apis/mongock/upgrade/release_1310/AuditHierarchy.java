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

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1310;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.publicissapient.kpidashboard.apis.auth.model.SystemUser;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * add modified date, createdby and modifiedby
 *
 * @author shi6
 */
@ChangeUnit(id = "audit_hierarchy", order = "13104", author = "shi6", systemVersion = "13.1.0")
public class AuditHierarchy {
	private static final String MODIFIED_DATE = "modifiedDate";

	private final MongoTemplate mongoTemplate;

	public AuditHierarchy(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		Date currentDate = Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC));
		mongoTemplate.getCollection("organization_hierarchy").updateMany(
				new Document(MODIFIED_DATE, new Document("$exists", false)), // Only update if "modifiedDate" is missing
				new Document("$set", new Document("createdBy", SystemUser.SYSTEM).append("updatedBy", SystemUser.SYSTEM)
						.append(MODIFIED_DATE, currentDate)) // Manually set "modifiedDate"
		);
	}

	@RollbackExecution
	public void rollback() {
		mongoTemplate.getCollection("organization_hierarchy").updateMany(new Document(), // Apply to all documents
				new Document("$unset", new Document(MODIFIED_DATE, "").append("createdBy", "").append("updatedBy", "")));
	}
}
