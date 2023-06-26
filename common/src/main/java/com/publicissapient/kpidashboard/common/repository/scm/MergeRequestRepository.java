package com.publicissapient.kpidashboard.common.repository.scm;

import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;

import com.publicissapient.kpidashboard.common.model.generic.ProcessorItem;
import com.publicissapient.kpidashboard.common.model.scm.MergeRequests;

/**
 * Repository for {@link MergeRequests} data.
 */
public interface MergeRequestRepository extends CrudRepository<MergeRequests, ObjectId>,
		QuerydslPredicateExecutor<MergeRequests>, MergeRequestRepositoryCustom {

	/**
	 * Finds the {@link MergeRequests} with the given revision number for a specific
	 * {@link ProcessorItem}.
	 *
	 * @param processorItemId
	 *            processor item id
	 * @param revisionNumber
	 *            revision number
	 * @return a {@link MergeRequests}
	 */
	MergeRequests findByProcessorItemIdAndRevisionNumber(ObjectId processorItemId, String revisionNumber);

	/**
	 * delete all documents with matching ids
	 * 
	 * @param processorItemIds
	 *            processor item id
	 */
	void deleteByProcessorItemIdIn(List<ObjectId> processorItemIds);

	/**
	 *
	 * @param processorItemId
	 * @param revisionNumber
	 * @return
	 */
	List<MergeRequests> findByProcessorItemIdAndRevisionNumberIn(ObjectId processorItemId, Set<String> revisionNumber);

}
