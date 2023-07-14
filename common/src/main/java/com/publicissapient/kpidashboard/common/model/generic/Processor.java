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

package com.publicissapient.kpidashboard.common.model.generic;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.common.constant.ProcessorType;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Provides Processor
 *
 */
@Document(collection = "processor")
@Getter
@Setter
@NoArgsConstructor
public class Processor extends BasicModel {
	protected String processorName;
	protected ProcessorType processorType;
	protected boolean isActive;
	protected boolean isOnline;
	protected List<ProcessorError> errors = Lists.newArrayList();
	protected long updatedTime;
	protected boolean isLastSuccess;

	@Builder
	public Processor(String processorName, ProcessorType processorType, boolean isActive, boolean isOnline,
			List<ProcessorError> errors, long updatedTime, ObjectId objectId, boolean isLastSuccess) {
		super(objectId);
		this.processorName = processorName;
		this.processorType = processorType;
		this.isActive = isActive;
		this.isOnline = isOnline;
		this.errors = errors;
		this.updatedTime = updatedTime;
		this.isLastSuccess = isLastSuccess;
	}

}