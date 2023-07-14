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

import org.bson.types.ObjectId;
import org.springframework.data.repository.CrudRepository;

import com.publicissapient.kpidashboard.common.model.generic.Processor;

/**
 * Generic Processor repository that contains methods common to any model that
 * extends from Processor.
 *
 * @param <T>
 *            Class that extends {@link Processor}
 */
public interface ProcessorRepository<T extends Processor> extends CrudRepository<T, ObjectId> {

	/**
	 * Finds a {@link Processor} by its name.
	 *
	 * @param processorName
	 *            name
	 * @return a {@link Processor}
	 */
	T findByProcessorName(String processorName);

}
