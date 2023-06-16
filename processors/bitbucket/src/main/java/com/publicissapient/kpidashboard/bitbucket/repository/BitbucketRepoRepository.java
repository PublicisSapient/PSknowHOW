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

package com.publicissapient.kpidashboard.bitbucket.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;

import com.publicissapient.kpidashboard.bitbucket.model.BitbucketRepo;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorItemRepository;

/**
 * BitbucketRepoRepository is used for to process BitbucketRepo.
 * 
 * @see BitbucketRepo
 */
public interface BitbucketRepoRepository extends ProcessorItemRepository<BitbucketRepo> {

	/**
	 * Represents a function that accepts one input arguments and returns list of
	 * BitbucketRepo.
	 *
	 * @param processorId
	 *            the processor id
	 * @return BitbucketRepo list of BitbucketRepo
	 */
	@Query("{ 'processorId' : ?0, 'isActive': true}")
	List<BitbucketRepo> findActiveRepos(ObjectId processorId);

	/**
	 * 
	 * @param processorId
	 *            the processor id
	 * @param toolConfigId
	 *            the toolConfig id
	 * @return list of BitbucketRepo
	 */
	List<BitbucketRepo> findByProcessorIdAndToolConfigId(ObjectId processorId, ObjectId toolConfigId);

}
