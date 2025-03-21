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
package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1200;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shunaray
 */
@ChangeUnit(id = "r_ttl_index_users_session", order = "012001", author = "shunaray", systemVersion = "12.0.0")
public class UsersSessionChangeUnit {

	private final MongoTemplate mongoTemplate;

	public UsersSessionChangeUnit(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		IndexOperations indexOps = mongoTemplate.indexOps("users_session");
		indexOps.dropIndex("expiresOn_1");
	}

	@RollbackExecution
	public void rollback() {
		IndexOperations indexOps = mongoTemplate.indexOps("users_session");
		indexOps.ensureIndex(new Index().on("expiresOn", Sort.Direction.ASC).expire(0));
	}
}
