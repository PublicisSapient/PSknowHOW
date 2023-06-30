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

package com.publicissapient.kpidashboard.common.repository.scm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCursor;
import com.publicissapient.kpidashboard.common.model.scm.CommitDetails;

public class CommitRepositoryCustomImpl implements CommitRepositoryCustom {

	private static final String IDENT_SCM_COMMIT_TIMESTAMP = "$commitTimestamp";
	private static final String SCM_COMMIT_TIMESTAMP = "commitTimestamp";
	private static final String COUNT = "count";
	private static final String IDENT_PROJECT = "$project";
	private static final String PROCESSOR_ITEM_ID = "processorItemId";
	private static final String ID = "_id";
	private static final String DATE = "date";

	@Autowired
	private MongoOperations operations;

	// currently used for fetching commits list
	@Override
	public List<CommitDetails> findCommitList(List<ObjectId> collectorItemIdList, Long startDate, Long endDate,
			BasicDBList filterList) {
		List<BasicDBObject> pipeline;
		Object[] array = { new Date(0), IDENT_SCM_COMMIT_TIMESTAMP };
		pipeline = Arrays.asList(
				new BasicDBObject("$match",
						new BasicDBObject("$or", filterList).append(SCM_COMMIT_TIMESTAMP,
								new BasicDBObject("$gte", startDate).append("$lte", endDate))),
				new BasicDBObject(IDENT_PROJECT,
						new BasicDBObject(SCM_COMMIT_TIMESTAMP, new BasicDBObject("$add", array))
								.append(PROCESSOR_ITEM_ID, 1)),
				new BasicDBObject("$group", new BasicDBObject(ID, new BasicDBObject(DATE,
						new BasicDBObject("$dateToString",
								new BasicDBObject("format", "%Y-%m-%d").append(DATE, IDENT_SCM_COMMIT_TIMESTAMP)))
						.append(PROCESSOR_ITEM_ID, "$processorItemId")).append(COUNT, new BasicDBObject("$sum", 1))),
				new BasicDBObject(IDENT_PROJECT,
						new BasicDBObject(ID, 0).append(DATE, "$_id.date")
								.append(PROCESSOR_ITEM_ID, "$_id.processorItemId").append(COUNT, 1)),
				new BasicDBObject("$sort", new BasicDBObject(DATE, 1)));
		AggregateIterable<Document> cursor = operations.getCollection("commit_details").aggregate(pipeline);
		MongoCursor<Document> itr = cursor.iterator();
		List<CommitDetails> returnList = new ArrayList<>();
		while (itr.hasNext()) {
			Document obj = itr.next();
			CommitDetails commitList = operations.getConverter().read(CommitDetails.class, obj);
			returnList.add(commitList);
		}
		return returnList;
	}

}
