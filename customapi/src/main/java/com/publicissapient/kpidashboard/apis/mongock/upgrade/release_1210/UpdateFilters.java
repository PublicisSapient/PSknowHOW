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
package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1210;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author prijain3
 */
@ChangeUnit(id = "update_filters_board_id", order = "12101", author = "prijain3", systemVersion = "12.1.0")
public class UpdateFilters {

	private static final String BOARD_ID = "boardId";
	private final MongoTemplate mongoTemplate;

	public UpdateFilters(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		updateFilterBoardId(15, 8);
		updateFilterBoardId(16, 9);
		updateFilterBoardId(17, 19);
	}

	/**
	 * Moving dora to scrum, kanban thus changing the boardId
	 *
	 * @param oldBoardId
	 *          older board id
	 * @param newBoardId
	 *          new board id
	 */
	private void updateFilterBoardId(int oldBoardId, int newBoardId) {
		mongoTemplate.getCollection("filters").updateMany(new Document(BOARD_ID, oldBoardId),
				new Document("$set", new Document(BOARD_ID, newBoardId)));
	}

	@RollbackExecution
	public void rollback() {
		updateFilterBoardId(8, 15);
		updateFilterBoardId(9, 16);
		updateFilterBoardId(19, 17);
	}
}
