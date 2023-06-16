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

package com.publicissapient.kpidashboard.jenkins.model;

import java.util.List;

import org.bson.types.ObjectId;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.constant.ProcessorType;
import com.publicissapient.kpidashboard.common.model.generic.Processor;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorError;

import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * Extension of Processor that stores current build server configuration.
 */

@NoArgsConstructor
public class JenkinsProcessor extends Processor {

	@Builder(builderMethodName = "processorBuilder")
	public JenkinsProcessor(String processorName, ProcessorType processorType, boolean active, boolean online,
			List<ProcessorError> errors, long lastExecuted, ObjectId objectId, boolean lastJobSuccess) {
		super(processorName, processorType, active, online, errors, lastExecuted, objectId, lastJobSuccess);
	}

	/**
	 * Provides buildProcessor processor.
	 *
	 *
	 * @return the processor object with initial values
	 */
	public static JenkinsProcessor buildProcessor() {

		return JenkinsProcessor.processorBuilder().processorName(ProcessorConstants.JENKINS)
				.processorType(ProcessorType.BUILD).online(true).active(true).lastExecuted(System.currentTimeMillis())
				.lastJobSuccess(false).build();
	}
}
