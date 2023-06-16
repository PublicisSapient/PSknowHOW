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

package com.publicissapient.kpidashboard.azurerepo.model;

import java.util.List;

import org.bson.types.ObjectId;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.constant.ProcessorType;
import com.publicissapient.kpidashboard.common.model.generic.Processor;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorError;

import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * AzurerepoProcessor represents a class which holds azurerepo prototyping and
 * executes the azurerepo data and stores in DB .
 * 
 * @see Processor
 */
@NoArgsConstructor
public class AzureRepoProcessor extends Processor {

	@Builder(builderMethodName = "processorBuilder")
	public AzureRepoProcessor(String processorName, ProcessorType processorType, boolean enabled, boolean online,
			List<ProcessorError> errors, long lastExecuted, ObjectId objectId, boolean isLastSuccess) {
		super(processorName, processorType, enabled, online, errors, lastExecuted, objectId, isLastSuccess);
	}

	public static AzureRepoProcessor prototype() {
		return AzureRepoProcessor.processorBuilder().processorName(ProcessorConstants.AZUREREPO).online(true)
				.enabled(true).processorType(ProcessorType.SCM).lastExecuted(System.currentTimeMillis())
				.isLastSuccess(false).build();
	}

}
