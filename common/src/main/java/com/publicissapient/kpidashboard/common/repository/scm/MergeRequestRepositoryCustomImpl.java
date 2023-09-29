package com.publicissapient.kpidashboard.common.repository.scm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;

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
	private static final String SCM_MERGED_TIMESTAMP = "closedDate";

	private static final String MERGE_REQUESTS = "merge_requests";
	@Autowired
	private MongoOperations operations;

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
						new BasicDBObject(SCM_CREATED_DATE, new BasicDBObject("$add", array)).append(PROCESSOR_ITEM_ID,
								1)),
				new BasicDBObject("$group",
						new BasicDBObject(ID, new BasicDBObject(DATE,
								new BasicDBObject("$dateToString",
										new BasicDBObject("format", "%Y-%m-%d").append(DATE, IDENT_CREATED_DATE)))
												.append(PROCESSOR_ITEM_ID, "$processorItemId")).append(COUNT,
														new BasicDBObject("$sum", 1))),
				new BasicDBObject(IDENT_PROJECT,
						new BasicDBObject(ID, 0).append(DATE, "$_id.date")
								.append(PROCESSOR_ITEM_ID, "$_id.processorItemId").append(COUNT, 1)),
				new BasicDBObject("$sort", new BasicDBObject(DATE, 1)));

		AggregateIterable<Document> cursor = operations.getCollection(MERGE_REQUESTS).aggregate(pipeline);
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

		AggregateIterable<Document> cursor = operations.getCollection(MERGE_REQUESTS).aggregate(pipeline);
		MongoCursor<Document> itr = cursor.iterator();
		List<MergeRequests> returnList = new ArrayList<>();
		while (itr.hasNext()) {
			Document obj = itr.next();
			MergeRequests mergeRequestList = operations.getConverter().read(MergeRequests.class, obj);
			returnList.add(mergeRequestList);

		}
		return returnList;
	}

	/**
	 * find merge request list based on basic config id and from branch , to branch
	 * and state matches
	 * 
	 * @param basicProjectConfigId
	 * @param fromBranches
	 * @param toBranch
	 * @return
	 */
	@Override
	public List<MergeRequests> findMergeRequestListBasedOnBasicProjectConfigId(ObjectId basicProjectConfigId,
			List<Pattern> fromBranches, String toBranch) {
		LookupOperation lookupProcessorItem = LookupOperation.newLookup().from("processor_items")
				.localField(PROCESSOR_ITEM_ID).foreignField("_id").as("processorItem");

		LookupOperation lookupProjectToolConfig = LookupOperation.newLookup().from("project_tool_configs")
				.localField("processorItem.toolConfigId").foreignField("_id").as("projectToolConfig");

		MatchOperation matchStage = Aggregation.match(
				Criteria.where("projectToolConfig.basicProjectConfigId").is(basicProjectConfigId).and("fromBranch")
						.in(fromBranches).and("toBranch").is(toBranch).and("state").is("MERGED"));

		ProjectionOperation projectStage = Aggregation.project(PROCESSOR_ITEM_ID, "title", "state", "revisionNumber",
				SCM_CREATED_DATE, "updatedDate", SCM_MERGED_TIMESTAMP, "fromBranch", "toBranch");

		Aggregation aggregation = Aggregation.newAggregation(lookupProcessorItem, Aggregation.unwind("processorItem"),
				lookupProjectToolConfig, Aggregation.unwind("projectToolConfig"), matchStage, projectStage);

		return operations.aggregate(aggregation, MERGE_REQUESTS, MergeRequests.class).getMappedResults();
		//add to index
	}

}
