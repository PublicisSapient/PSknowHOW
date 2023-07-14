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

package com.publicissapient.kpidashboard.bamboo.repository;

import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.bamboo.model.BambooProcessor;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorRepository;

/**
 * Represents a repository to perform CRUD operation from processor collection
 * by the {@link BambooProcessor}
 * 
 * @see BambooProcessor
 * @see ProcessorRepository
 */
@Repository
public interface BambooProcessorRepository extends ProcessorRepository<BambooProcessor> {
}
