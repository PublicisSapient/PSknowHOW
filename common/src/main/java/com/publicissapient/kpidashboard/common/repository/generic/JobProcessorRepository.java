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

package com.publicissapient.kpidashboard.common.repository.generic;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;

import com.publicissapient.kpidashboard.common.model.generic.JobProcessorItem;

/**
 * The interface Job repository.
 *
 * @param <T>
 *            the type parameter
 */
public interface JobProcessorRepository<T extends JobProcessorItem> extends ProcessorItemRepository<T> {
	/**
	 * Find job t.
	 *
	 * @param processorId
	 *            the processor id
	 * @param instanceUrl
	 *            the instance url
	 * @param jobName
	 *            the job name
	 * @return the t
	 */
	@Query("{ 'processorId' : ?0, 'toolDetailsMap.instanceUrl' : ?1, 'toolDetailsMap.jobName' : ?2}")
	T findJob(ObjectId processorId, String instanceUrl, String jobName);

	/**
	 * Find job t.
	 *
	 * @param processorId
	 *            the processor id
	 * @param instanceUrl
	 *            the instance url
	 * @param jobName
	 *            the job name
	 * @return the t
	 */
	@Query("{ 'processorId' : ?0, 'toolDetailsMap.instanceUrl' : ?1, 'toolDetailsMap.jobName' : ?2, 'toolConfigId' : ?3}")
	T findJob(ObjectId processorId, String instanceUrl, String jobName, ObjectId toolConfigId);

	/**
	 * Find enabled jobs list.
	 *
	 * @param processorId
	 *            the processor id
	 * @param instanceUrl
	 *            the instance url
	 * @return the list
	 */
	@Query("{ 'processorId' : ?0, 'toolDetailsMap.instanceUrl' : ?1, 'isActive': true}")
	List<T> findEnabledJobs(ObjectId processorId, String instanceUrl);

	/**
	 * Find by processor id list.
	 *
	 * @param processorId
	 *            the processor id
	 * @return the list
	 */
	@Override
	List<T> findByProcessorId(ObjectId processorId);
}
