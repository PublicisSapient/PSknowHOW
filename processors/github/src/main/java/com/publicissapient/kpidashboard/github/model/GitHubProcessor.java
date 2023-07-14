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

package com.publicissapient.kpidashboard.github.model;

import java.util.List;

import org.bson.types.ObjectId;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.constant.ProcessorType;
import com.publicissapient.kpidashboard.common.model.generic.Processor;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorError;

import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * GitHubProcessor represents a class which holds github prototyping and
 * executes the github data and stores in DB.
 * 
 * @see Processor
 */
@NoArgsConstructor
public class GitHubProcessor extends Processor {

	/**
	 * The constructor.
	 *
	 * @param processorName
	 *            processorName
	 * @param processorType
	 *            processorType
	 * @param enabled
	 *            enabled
	 * @param online
	 *            online
	 * @param errors
	 *            errors
	 * @param lastExecuted
	 *            lastExecuted
	 * @param objectId
	 *            objectId
	 * @param isLastSuccess
	 *            isLastSuccess
	 */
	@Builder(builderMethodName = "processorBuilder")
	public GitHubProcessor(String processorName, ProcessorType processorType, boolean enabled, boolean online,
			List<ProcessorError> errors, long lastExecuted, ObjectId objectId, boolean isLastSuccess) {
		super(processorName, processorType, enabled, online, errors, lastExecuted, objectId, isLastSuccess);
	}

	/**
	 * This method return githubprocessor object
	 *
	 * @return GitHubProcessor
	 */
	public static GitHubProcessor prototype() {
		return GitHubProcessor.processorBuilder().processorName(ProcessorConstants.GITHUB).online(true).enabled(true)
				.processorType(ProcessorType.SCM).lastExecuted(System.currentTimeMillis()).isLastSuccess(false).build();
	}

}
