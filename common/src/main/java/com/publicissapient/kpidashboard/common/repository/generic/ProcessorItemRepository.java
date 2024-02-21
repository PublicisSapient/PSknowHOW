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

import java.util.Collection;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.publicissapient.kpidashboard.common.model.generic.ProcessorItem;

/**
 * Base {@link ProcessorItem} repository that provides methods useful for any
 * {@link ProcessorItem} implementation.
 *
 * @param <T>
 *            Class that extends {@link ProcessorItem}
 */
public interface ProcessorItemRepository<T extends ProcessorItem> extends PagingAndSortingRepository<T, ObjectId> , CrudRepository<T,ObjectId> {

	/**
	 * Finds all {@link ProcessorItem}s that match the provided id's.
	 *
	 * @param ids
	 *            {@link Collection} of ids
	 * @return list of {@link ProcessorItem}s
	 */
	List<T> findByProcessorIdIn(Collection<ObjectId> ids);

	/**
	 * Finds all {@link ProcessorItem}s that match the provided id.
	 *
	 * @param processorId
	 *            the processor id
	 * @return list of {@link ProcessorItem}s
	 */
	List<T> findByProcessorId(ObjectId processorId);

	/**
	 * Find by toolConfigId
	 * 
	 * @param toolConfigId
	 *            tool config id
	 * @return list of items
	 */
	List<T> findByToolConfigId(ObjectId toolConfigId);

	/**
	 * Delete by toolConfigId
	 * 
	 * @param toolConfigId
	 *            toolConfigId
	 */
	void deleteByToolConfigId(ObjectId toolConfigId);

	/**
	 * Finds all {@link ProcessorItem}s by processorId and toolConfigId
	 *
	 * @param processorId
	 * @param toolConfigId
	 * @return
	 */
	List<T> findByProcessorIdAndToolConfigId(ObjectId processorId, ObjectId toolConfigId);

}
