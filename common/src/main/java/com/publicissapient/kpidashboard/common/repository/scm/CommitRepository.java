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

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;

import com.publicissapient.kpidashboard.common.model.generic.ProcessorItem;
import com.publicissapient.kpidashboard.common.model.scm.CommitDetails;

/**
 * Repository for {@link CommitDetails} data.
 */
public interface CommitRepository extends CrudRepository<CommitDetails, ObjectId>,
		QuerydslPredicateExecutor<CommitDetails>, CommitRepositoryCustom {

	/**
	 * Finds the {@link CommitDetails} with the given revision number for a specific
	 * {@link ProcessorItem}.
	 *
	 * @param processorItemId
	 *            processor item id
	 * @param revisionNumber
	 *            revision number
	 * @return a {@link CommitDetails}
	 */
	CommitDetails findByProcessorItemIdAndRevisionNumber(ObjectId processorItemId, String revisionNumber);

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
	List<CommitDetails> findByProcessorItemIdAndRevisionNumberIn(ObjectId processorItemId, List<String> revisionNumber);

}
