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

package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1010;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shunaray
 */
@ChangeUnit(id = "r_conn_share_with_everyone", order = "010104", author = "shunaray", systemVersion = "10.1.0")
public class ConnectionShare {

	public static final String SHARED_CONNECTION = "sharedConnection";
	public static final String CONN_PRIVATE = "connPrivate";
	private final MongoTemplate mongoTemplate;

	public ConnectionShare(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		rollbackUpdateConnections();
	}

	public void rollbackUpdateConnections() {
		MongoCollection<Document> collection = mongoTemplate.getCollection("connections");
		FindIterable<Document> iterable = collection.find(Filters.exists(SHARED_CONNECTION));

		for (Document doc : iterable) {
			Boolean sharedConnection = doc.getBoolean(SHARED_CONNECTION);
			if (sharedConnection != null) {
				doc.put(CONN_PRIVATE, !sharedConnection);
				doc.remove(SHARED_CONNECTION);
				collection.replaceOne(Filters.eq("_id", doc.get("_id")), doc);
			}
		}
	}

	@RollbackExecution
	public void rollback() {
		updateConnections();
	}

	public void updateConnections() {
		MongoCollection<Document> collection = mongoTemplate.getCollection("connections");
		FindIterable<Document> iterable = collection.find(Filters.exists(CONN_PRIVATE));

		for (Document doc : iterable) {
			Boolean connPrivate = doc.getBoolean(CONN_PRIVATE);
			if (connPrivate != null) {
				doc.put(SHARED_CONNECTION, !connPrivate);
				doc.remove(CONN_PRIVATE);
				collection.replaceOne(Filters.eq("_id", doc.get("_id")), doc);
			}
		}
	}
}
