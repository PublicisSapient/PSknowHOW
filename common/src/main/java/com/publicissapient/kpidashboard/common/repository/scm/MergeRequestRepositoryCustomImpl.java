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
import com.publicissapient.kpidashboard.common.model.scm.MergeRequests;

public class MergeRequestRepositoryCustomImpl implements MergeRequestRepositoryCustom {



	private static final String IDENT_CREATED_DATE = "$createdDate";
	private static final String SCM_CREATED_DATE = "createdDate";
	private static final String COUNT = "count";
	private static final String IDENT_PROJECT = "$project";
	private static final String PROCESSOR_ITEM_ID = "processorItemId";
	private static final String ID = "_id";
	private static final String DATE = "date";

	@Autowired
	private MongoOperations operations;
	private static final String SCM_MERGED_TIMESTAMP = "closedDate";
	
	@Override
	public List<MergeRequests> findMergeList(List<ObjectId> collectorItemIdList, Long startDate, Long endDate,
			BasicDBList filterList) {
		List<BasicDBObject> pipeline;
		Object[] array = { new Date(0), IDENT_CREATED_DATE };
		pipeline = Arrays.asList(
				new BasicDBObject("$match",
						new BasicDBObject("$or", filterList).append(SCM_CREATED_DATE,
								new BasicDBObject("$gte", startDate).append("$lte", endDate))),
				new BasicDBObject(IDENT_PROJECT,
						new BasicDBObject(SCM_CREATED_DATE, new BasicDBObject("$add", array))
								.append(PROCESSOR_ITEM_ID, 1)),
				new BasicDBObject("$group", new BasicDBObject(ID, new BasicDBObject(DATE,
						new BasicDBObject("$dateToString",
								new BasicDBObject("format", "%Y-%m-%d").append(DATE, IDENT_CREATED_DATE)))
										.append(PROCESSOR_ITEM_ID, "$processorItemId")).append(COUNT,
												new BasicDBObject("$sum", 1))),
				new BasicDBObject(IDENT_PROJECT,
						new BasicDBObject(ID, 0).append(DATE, "$_id.date")
								.append(PROCESSOR_ITEM_ID, "$_id.processorItemId").append(COUNT, 1)),
				new BasicDBObject("$sort", new BasicDBObject(DATE, 1))
				);
		

		AggregateIterable<Document> cursor = operations.getCollection("merge_requests").aggregate(pipeline);
		MongoCursor<Document> itr = cursor.iterator();
		List<MergeRequests> returnList = new ArrayList<>();
		while (itr.hasNext()) {
			Document obj = itr.next();
			MergeRequests mergeList = operations.getConverter().read(MergeRequests.class, obj);
			returnList.add(mergeList);
		}
		return returnList;
	}

	

	@Override
	public List<MergeRequests> findMergeRequestList(List<ObjectId> collectorItemIdList, Long startDate, Long endDate,
			BasicDBList filterList) {
		List<BasicDBObject> pipeline;
		pipeline = Arrays.asList(new BasicDBObject("$match", new BasicDBObject("$or", filterList)
				.append(SCM_MERGED_TIMESTAMP, new BasicDBObject("$gte", startDate).append("$lte", endDate))));

		AggregateIterable<Document> cursor = operations.getCollection("merge_requests").aggregate(pipeline);
		MongoCursor<Document> itr = cursor.iterator();
		List<MergeRequests> returnList = new ArrayList<>();
		while (itr.hasNext()) {
			Document obj = itr.next();
			MergeRequests mergeRequestList = operations.getConverter().read(MergeRequests.class, obj);
			returnList.add(mergeRequestList);

		}
		return returnList;
	}


}
