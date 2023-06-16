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

package com.publicissapient.kpidashboard.bamboo.model;

import java.util.List;

import org.bson.types.ObjectId;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.constant.ProcessorType;
import com.publicissapient.kpidashboard.common.model.generic.Processor;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorError;

import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * This class provides extension of processor that stores current build server
 * configuration.
 */
@NoArgsConstructor
public class BambooProcessor extends Processor {

	@Builder(builderMethodName = "processorBuilder")
	public BambooProcessor(String name, ProcessorType processorType, boolean enabled, boolean online,
			List<ProcessorError> errors, long lastExecuted, ObjectId objectId, boolean isLastSuccess) {
		super(name, processorType, enabled, online, errors, lastExecuted, objectId, isLastSuccess);
	}

	public static BambooProcessor prototype() {
		return BambooProcessor.processorBuilder().name(ProcessorConstants.BAMBOO).online(true).enabled(true)
				.processorType(ProcessorType.BUILD).lastExecuted(System.currentTimeMillis()).isLastSuccess(false)
				.build();
	}
}
