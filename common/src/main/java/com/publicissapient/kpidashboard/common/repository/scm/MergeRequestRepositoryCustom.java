package com.publicissapient.kpidashboard.common.repository.scm;

import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.publicissapient.kpidashboard.common.model.scm.MergeRequests;

public interface MergeRequestRepositoryCustom {

	List<MergeRequests> findMergeList(List<ObjectId> collectorItemIdList, Long startDate, Long endDate,
			BasicDBList filterList);

	List<MergeRequests> findMergeRequestList(List<ObjectId> collectorItemIdList, Long startDate, Long endDate,
			BasicDBList filterList);

	List<MergeRequests> findMergeRequestListBasedOnBasicProjectConfigId(Set<ObjectId> basicProjectConfigIds,
			List<String>  fromBranches , List<String> mergeRequestStatusList);
}
