/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1120;

import com.mongodb.client.model.UpdateOptions;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

@ChangeUnit(id = "dashboard_label_update", order = "11201", author = "kunkambl", systemVersion = "11.2.0")
public class DashboardLabelUpdate {

	private static final String OLD_BOARD_NAME_MY_KNOWHOW = "My KnowHow";
	private static final String NEW_BOARD_NAME_MY_KNOWHOW = "My KnowHOW";
	private static final String OLD_BOARD_NAME_KPI_MATURITY = "Kpi Maturity";
	private static final String NEW_BOARD_NAME_KPI_MATURITY = "KPI Maturity";

	private final MongoTemplate mongoTemplate;

	public DashboardLabelUpdate(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		updateBoardNames(OLD_BOARD_NAME_MY_KNOWHOW, NEW_BOARD_NAME_MY_KNOWHOW, OLD_BOARD_NAME_KPI_MATURITY,
				NEW_BOARD_NAME_KPI_MATURITY);
	}

	private void updateBoardNames(String oldBoardNameKnowHow, String newBoardNameKnowHow, String oldBoardNameMaturity,
			String newBoardNameMaturity) {
		Document query = new Document("$or",
				List.of(new Document("scrum.boardName", oldBoardNameKnowHow),
						new Document("kanban.boardName", oldBoardNameKnowHow),
						new Document("others.boardName", oldBoardNameMaturity)));

		Document update = new Document("$set",
				new Document().append("scrum.$[scrumElem].boardName", newBoardNameKnowHow)
						.append("kanban.$[kanbanElem].boardName", newBoardNameKnowHow)
						.append("others.$[othersElem].boardName", newBoardNameMaturity));

		List<Document> arrayFilters = List.of(new Document("scrumElem.boardName", oldBoardNameKnowHow),
				new Document("kanbanElem.boardName", oldBoardNameKnowHow),
				new Document("othersElem.boardName", oldBoardNameMaturity));

		UpdateOptions options = new UpdateOptions().arrayFilters(arrayFilters);

		mongoTemplate.getCollection("user_board_config").updateMany(query, update, options);

		mongoTemplate.getCollection("kpi_master").updateOne(new Document("kpiId", "kpi989"),
				new Document("$set", new Document("kpiCategory", newBoardNameMaturity)));

	}

	@RollbackExecution
	public void rollback() {
		updateBoardNames(NEW_BOARD_NAME_MY_KNOWHOW, OLD_BOARD_NAME_MY_KNOWHOW, NEW_BOARD_NAME_KPI_MATURITY,
				OLD_BOARD_NAME_KPI_MATURITY);
	}

}
