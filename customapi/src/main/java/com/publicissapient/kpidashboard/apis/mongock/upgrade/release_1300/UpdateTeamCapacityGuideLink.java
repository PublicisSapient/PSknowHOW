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

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1300;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

@ChangeUnit(id = "update_team_capacity_guide_link", order = "13003", author = "kunkambl", systemVersion = "13.0.0")
public class UpdateTeamCapacityGuideLink {

	private final MongoTemplate mongoTemplate;

	public UpdateTeamCapacityGuideLink(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {

		mongoTemplate.getCollection("kpi_master").updateOne(new Document("kpiId", "kpi58"),
				new Document("$set", new Document("kpiInfo.details.1.kpiLinkDetail.link",
						"https://speed.suite.publicissapient.com/wiki/spaces/PS/pages/43057154/Step+6A+-+Capacity+Data+Reporting")));
	}

	@RollbackExecution
	public void rollback() {
		mongoTemplate.getCollection("kpi_master").updateOne(new Document("kpiId", "kpi58"),
				new Document("$set", new Document("kpiInfo.details.1.kpiLinkDetail.link",
						"https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/32473095/Capacity+Management")));
	}
}
