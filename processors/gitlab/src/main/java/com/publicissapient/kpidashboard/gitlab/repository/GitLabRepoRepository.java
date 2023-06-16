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

package com.publicissapient.kpidashboard.gitlab.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;

import com.publicissapient.kpidashboard.common.repository.generic.ProcessorItemRepository;
import com.publicissapient.kpidashboard.gitlab.model.GitLabRepo;

/**
 * GitLabRepoRepository is used to process GitLabRepo.
 */
public interface GitLabRepoRepository extends ProcessorItemRepository<GitLabRepo> {

	/**
	 * Represents a function that accepts one input arguments and returns list of
	 * GitLabRepo.
	 *
	 * @param processorId
	 *            the processor id
	 * @return GitLabRepo list of GitLabRepo
	 */
	@Query("{ 'processorId' : ?0, 'isActive': true}")
	List<GitLabRepo> findActiveRepos(ObjectId processorId);

}
